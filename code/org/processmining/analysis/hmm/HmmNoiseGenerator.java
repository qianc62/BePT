package org.processmining.analysis.hmm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import org.processmining.analysis.hmm.jahmmext.GenericHmmDrawerDot;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;
import org.processmining.lib.mxml.AuditTrailEntry;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;

public abstract class HmmNoiseGenerator {

	protected Map<String, PetriNet> inputModels;
	protected HmmExpConfiguration conf;

	protected String typeNoiseFolder; // where to put the logs with varying
	// degrees of noise
	protected String noiseHmmFolder; // where to put the generator hmms

	protected Hmm<ObservationInteger> noisyHmm; // current hmm
	protected Hmm<ObservationInteger> noiseFreeHmm; // initial model hmm
	protected ArrayList<LogEvent> observationMapping; // current mapping
	protected String modelName; // current model name

	/**
	 * Creates a noise generator with the given parameters.
	 * 
	 * @param anHmm
	 *            the hmm to be used as a basis for generating the noisy logs
	 * @param noNoiseLevels
	 *            the number of noise levels (without the 0% level). Should be
	 *            between 1 and 100
	 * @param noTraces
	 *            the number of traces that should be generated for each of the
	 *            noise levels
	 * @param traceLength
	 *            the maximum trace length per generated instance (in the
	 *            presence of loops might be otherwise infinite)
	 * @param eventMapping
	 *            the observation element mapping matching the given hmm
	 */
	public HmmNoiseGenerator(Map<String, PetriNet> models,
			HmmExpConfiguration aConf) {
		inputModels = models;
		conf = aConf;
		typeNoiseFolder = getNoisyLogFolder();
		noiseHmmFolder = getGeneratorHmmFolder();
		new File(typeNoiseFolder).mkdir();
		new File(noiseHmmFolder).mkdir();
	}

	public abstract String getNoisyLogFolder();

	public abstract String getGeneratorHmmFolder();

	/**
	 * Generate noisy logs based on the given parameters.
	 */
	public void generate() {
		for (Entry<String, PetriNet> input : inputModels.entrySet()) {
			modelName = input.getKey();
			typeNoiseFolder = getNoisyLogFolder() + "/" + modelName;
			noiseHmmFolder = getGeneratorHmmFolder() + "/" + modelName;
			new File(typeNoiseFolder).mkdir(); // make sub directory for model
			new File(noiseHmmFolder).mkdir(); // make sub directory for model
			PetriNet model = input.getValue();
			HmmAnalyzer analyzer = new HmmAnalyzer(model, null);
			noisyHmm = analyzer.getInputHmm();
			HmmAnalyzer analyzer2 = new HmmAnalyzer(model, null);
			noiseFreeHmm = analyzer2.getInputHmm();
			observationMapping = analyzer.getObservationMapping();
			// first: 0% noise
			writeHmm(0);
			generateNoise(0);
			// now distribute noise levels up to 100%
			double stepSize = 100.0 / (double) conf.getNoiseLevels();
			// now generate different noise levels
			for (int i = 1; i < conf.getNoiseLevels() + 1; i++) {
				adjustHmm(i);
				double currentNoiseLevel = stepSize * i;
				writeHmm(currentNoiseLevel);
				generateNoise(currentNoiseLevel);
			}
		}
	}

	/**
	 * Generates either a single log or replicated logs (depending on the user
	 * configuration) for the current noise level.
	 * 
	 * @param currentNoiseLevel
	 *            the current noise level (between 0 and 100)
	 */
	private void generateNoise(double currentNoiseLevel) {
		if (conf.isReplicate() == false) {
			generateLog(currentNoiseLevel);
		} else {
			generateReplicatedLog(currentNoiseLevel);
		}
	}

	/**
	 * Generates an MXML event log based noisy HMM and stores file on disk while
	 * encoding the current noise level in the file name.
	 * 
	 * @param noiseLevel
	 *            the current noise level (between 0 and 100)
	 */
	protected void generateLog(double noiseLevel) {
		List<List<ObservationInteger>> sequences = generateSequences();
		File outputFile = new File(typeNoiseFolder, noiseLevel
				+ "_PerCent.mxml.gz");
		writeLog(sequences, noiseLevel, outputFile);
	}

	/**
	 * Generates replicated MXML event logs and stores the files on disk in a
	 * folder encoding the current noise level in the directory name. file on
	 * disk while encoding the current noise level in the file name.
	 * 
	 * @param noiseLevel
	 *            the current noise level (between 0 and 100)
	 */
	private void generateReplicatedLog(double noiseLevel) {
		List<List<ObservationInteger>> sequences;
		new File(typeNoiseFolder + "/" + noiseLevel + "_PerCent").mkdir();
		for (int j = 1; j < conf.getReplications() + 1; j++) {
			sequences = generateSequences();
			File outputFile = new File(typeNoiseFolder + "/" + noiseLevel
					+ "_PerCent", "Replication_" + j + ".mxml.gz");
			writeLog(sequences, noiseLevel, outputFile);
		}
	}

	/**
	 * Adjusts the Hmm to the current noise level. To be specified by each noise
	 * type in deriving sub class
	 */
	protected abstract void adjustHmm(int noiseInterval);

	/**
	 * Writes the current Hmm to file while encoding the current noise level in
	 * the file name.
	 * 
	 * @param noiseLevel
	 *            the current noise level (between 0 and 100)
	 */
	protected void writeHmm(double noiseLevel) {
		try {
			(new GenericHmmDrawerDot()).write(noisyHmm, noiseHmmFolder + "/"
					+ noiseLevel + "_PerCent.dot", observationMapping);
		} catch (Exception ex) {
			Message.add("Error writing the HMM to Dot file at noise level "
					+ noiseLevel, Message.ERROR);
			ex.printStackTrace();
		}
	}

	/**
	 * Actually writes the generated log to the disk.
	 * 
	 * @param sequences
	 *            the log traces as HMM sequences
	 * @param noiseLevel
	 *            the current noise level
	 * @param outputFile
	 *            the output file to write the log to
	 */
	private void writeLog(List<List<ObservationInteger>> sequences,
			double noiseLevel, File outputFile) {
		try {
			FileOutputStream output = new FileOutputStream(outputFile);
			BufferedOutputStream out = new BufferedOutputStream(
					new GZIPOutputStream(output));
			LogPersistencyStream persistency = new LogPersistencyStream(out,
					false);
			String name = "UnnamedProcess";
			String description = name + " exported by MXMLib @ P-stable";
			String source = "ProM Hmm-based Noise Generator";
			persistency.startLogfile(name, description, source);
			HashMap<String, String> attributes = new HashMap<String, String>();
			attributes.put("Noise level", "" + noiseLevel);
			persistency.startProcess(name, description, attributes);
			for (int j = 0; j < sequences.size(); j++) {
				List<ObservationInteger> currentSeq = sequences.get(j);
				name = "Instance";
				description = name + " exported by MXMLib @ P-stable";
				persistency.startProcessInstance(name, description,
						new HashMap<String, String>());
				for (int k = 0; k < currentSeq.size(); k++) {
					int observation = currentSeq.get(k).value;
					if (observation != observationMapping.size()) {
						LogEvent event = observationMapping.get(observation);
						AuditTrailEntry ate = new AuditTrailEntry();
						ate
								.setWorkflowModelElement(event
										.getModelElementName());
						ate.setEventType(EventType.COMPLETE);
						persistency.addAuditTrailEntry(ate);
					} else {
						break; // once null observations are thrown this final
						// state is not left anymore
					}
				}
				persistency.endProcessInstance();
			}
			persistency.endProcess();
			persistency.endLogfile();
			persistency.finish();
		} catch (Exception ex) {
			Message.add("Error while writing log at noise level " + noiseLevel,
					Message.ERROR);
			ex.printStackTrace();
		}
	}

	/**
	 * Generates observation sequences based on the given input Hmm.
	 * 
	 * @return the sequences generated
	 */
	protected List<List<ObservationInteger>> generateSequences() {
		MarkovGenerator<ObservationInteger> mg = new MarkovGenerator<ObservationInteger>(
				noisyHmm);
		List<List<ObservationInteger>> sequences = new ArrayList<List<ObservationInteger>>();
		for (int i = 0; i < conf.getTraces(); i++) {
			sequences.add(mg.observationSequence(conf.getTraceLength()));
		}
		return sequences;
	}

}
