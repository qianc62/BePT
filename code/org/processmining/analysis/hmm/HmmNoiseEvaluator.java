package org.processmining.analysis.hmm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.analysis.hmm.GnuplotScript.PlotType;
import org.processmining.analysis.hmm.metrics.FitnessEventLevel;
import org.processmining.analysis.hmm.metrics.FitnessImprovedContinuous;
import org.processmining.analysis.hmm.metrics.FitnessModelLevel;
import org.processmining.analysis.hmm.metrics.FitnessTokenBased;
import org.processmining.analysis.hmm.metrics.FitnessTraceAverage;
import org.processmining.analysis.hmm.metrics.FitnessTraceBased;
import org.processmining.analysis.hmm.metrics.HmmExpMetric;
import org.processmining.analysis.logreaderconnection.PetriNetLogReaderConnectionPlugin;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.mining.petrinetmining.PetriNetResult;

public abstract class HmmNoiseEvaluator {

	protected Map<String, PetriNet> inputModels;
	protected HmmExpConfiguration conf;
	protected Map<Double, LogReader> noisyLogs; // noise level and the
	// corresponding log
	protected HashMap<Double, ArrayList<LogReader>> replicatedNoisyLogs; // noise
	// level
	// and
	// list
	// of
	// replicated
	// logs
	protected ArrayList<HmmExpMetric> metrics;

	protected String typeNoiseFolder; // where to read the noisy logs from
	protected String typeEvalFolder; // where to put the results of the
	// evaluation

	protected String modelName; // current model name

	/**
	 * Creates a new noise evaluator.
	 * 
	 * @param model
	 */
	public HmmNoiseEvaluator(Map<String, PetriNet> models,
			HmmExpConfiguration aConf) {
		inputModels = models;
		conf = aConf;
		typeNoiseFolder = getNoisyLogFolder();
		typeEvalFolder = getEvaluationFolder();
		new File(typeEvalFolder).mkdir();
	}

	protected abstract String getNoisyLogFolder();

	protected abstract String getEvaluationFolder();

	/**
	 * Evaluates all logs in the 'NoisyLogs' directory with respect to the given
	 * Petri net model and outputs the result in a file 'NoiseEvaluation.gpl'.
	 */
	public void evaluate() {
		for (Entry<String, PetriNet> input : inputModels.entrySet()) {
			modelName = input.getKey();
			typeNoiseFolder = getNoisyLogFolder() + "/" + modelName;
			typeEvalFolder = getEvaluationFolder() + "/" + modelName;
			new File(typeEvalFolder).mkdir(); // make sub directory for model
			PetriNet model = input.getValue();
			try {
				initialize();
				readNoisyLogs();
				if (conf.isReplicate() == false) {
					evaluateNoisyLogs(model);
					writeGnuplotScripts();
				} else {
					evaluateReplicatedNoisyLogs(model);
					writeReplicateGnuplotScripts();
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void initialize() {
		metrics = new ArrayList<HmmExpMetric>();
		metrics.add(new FitnessTokenBased(typeEvalFolder));
		metrics.add(new FitnessImprovedContinuous(typeEvalFolder));
		metrics.add(new FitnessTraceBased(typeEvalFolder));
		metrics.add(new FitnessModelLevel(typeEvalFolder));
		metrics.add(new FitnessEventLevel(typeEvalFolder));
		metrics.add(new FitnessTraceAverage(typeEvalFolder));
	}

	private void writeGnuplotScripts() throws IOException {
		// values with lines
		GnuplotScript gnuplot = new GnuplotScript(typeEvalFolder,
				"FitnessEvaluation",
				"Fitness Measurements for Different Noise Levels",
				"Noise Level", "Fitness Value", PlotType.lines);
		for (HmmExpMetric metric : metrics) {
			gnuplot.addPlot(metric.getFilename(), metric.getName());
		}
		gnuplot.finish();
		// values with points
		gnuplot = new GnuplotScript(typeEvalFolder, "FitnessEvaluation",
				"Fitness Measurements for Different Noise Levels",
				"Noise Level", "Fitness Value", PlotType.points);
		for (HmmExpMetric metric : metrics) {
			gnuplot.addPlot(metric.getFilename(), metric.getName());
		}
		gnuplot.finish();
		// ratios with lines
		gnuplot = new GnuplotScript(typeEvalFolder, "FitnessRatioEvaluation",
				"Fitness Ratios for Different Noise Levels", "Noise Level",
				"Fitness Value", PlotType.lines);
		for (HmmExpMetric metric : metrics) {
			gnuplot.addPlot(metric.getFilename() + "Ratio", metric.getName());
		}
		gnuplot.finish();
		// individual ratios
		for (HmmExpMetric metric : metrics) {
			metric.writeRatioGnuplot();
		}
	}

	private void writeReplicateGnuplotScripts() throws IOException {
		// values with lines
		GnuplotScript gnuplot = new GnuplotScript(typeEvalFolder,
				"FitnessReplicationEvaluation",
				"Average Fitness Measurements for Different Noise Levels",
				"Noise Level", "Average Fitness Value", PlotType.lines);
		for (HmmExpMetric metric : metrics) {
			gnuplot.addPlot(metric.getFilename() + "Average", metric.getName());
		}
		gnuplot.finish();
		// values with points
		gnuplot = new GnuplotScript(typeEvalFolder,
				"FitnessReplicationEvaluation",
				"Average Fitness Measurements for Different Noise Levels",
				"Noise Level", "Average Fitness Value", PlotType.points);
		for (HmmExpMetric metric : metrics) {
			gnuplot.addPlot(metric.getFilename() + "Average", metric.getName());
		}
		gnuplot.finish();
		// ratios with lines
		gnuplot = new GnuplotScript(typeEvalFolder,
				"FitnessRatioReplicationEvaluation",
				"Average Fitness Ratios for Different Noise Levels",
				"Noise Level", "Fitness Value", PlotType.lines);
		for (HmmExpMetric metric : metrics) {
			gnuplot.addPlot(metric.getFilename() + "AverageRatio", metric
					.getName());
		}
		gnuplot.finish();
		// individual ratios
		for (HmmExpMetric metric : metrics) {
			metric.writeAverageRatioGnuplot();
		}
	}

	private void evaluateNoisyLogs(PetriNet inputModel) throws IOException {
		Object[] noiseLevels = noisyLogs.keySet().toArray();
		Arrays.sort(noiseLevels);
		LogReader currentLog;
		for (int i = 0; i < noiseLevels.length; i++) {
			currentLog = noisyLogs.get(noiseLevels[i]);
			PetriNetResult result1 = connectModelWithLog(inputModel, currentLog);
			inputModel = result1.getPetriNet();
			currentLog = result1.getLogReader();
			HmmAnalyzer analyzer = new HmmAnalyzer(inputModel, currentLog);
			for (HmmExpMetric metric : metrics) {
				double value = metric.calculateValue(analyzer, inputModel,
						currentLog);
				writeToDat(metric.getValueWriter(), noiseLevels[i].toString(),
						value);
				writeToDat(metric.getRatioWriter(), noiseLevels[i].toString(),
						calculateRatio(((Double) noiseLevels[i]).doubleValue(),
								value));
			}
		}
		for (HmmExpMetric metric : metrics) {
			metric.finishDatFiles();
		}
	}

	private void evaluateReplicatedNoisyLogs(PetriNet inputModel)
			throws IOException {
		BufferedWriter globalMeasures = HmmExpUtils.createWriter(
				typeEvalFolder, "Global", "txt");
		HashMap<HmmExpMetric, SummaryStatistics> globalAverageRatio = new HashMap<HmmExpMetric, SummaryStatistics>();
		HashMap<HmmExpMetric, SummaryStatistics> globalAverageVariance = new HashMap<HmmExpMetric, SummaryStatistics>();
		HashMap<HmmExpMetric, SummaryStatistics> globalRatioStatistics = new HashMap<HmmExpMetric, SummaryStatistics>();
		for (HmmExpMetric metric : metrics) {
			globalAverageRatio.put(metric, SummaryStatistics.newInstance());
			globalAverageVariance.put(metric, SummaryStatistics.newInstance());
			globalRatioStatistics.put(metric, SummaryStatistics.newInstance());
		}
		Object[] noiseLevels = replicatedNoisyLogs.keySet().toArray();
		Arrays.sort(noiseLevels);
		for (int i = 0; i < noiseLevels.length; i++) {
			HashMap<HmmExpMetric, SummaryStatistics> values = new HashMap<HmmExpMetric, SummaryStatistics>();
			for (HmmExpMetric metric : metrics) {
				values.put(metric, SummaryStatistics.newInstance());
			}
			System.out.println("\nNoise Level " + noiseLevels[i].toString()
					+ ":");
			for (LogReader log : replicatedNoisyLogs.get(noiseLevels[i])) {
				PetriNetResult result1 = connectModelWithLog(inputModel, log);
				inputModel = result1.getPetriNet();
				log = result1.getLogReader();
				HmmAnalyzer analyzer = new HmmAnalyzer(inputModel, log);
				for (HmmExpMetric metric : metrics) {
					SummaryStatistics metValues = values.get(metric);
					double val = metric.calculateValue(analyzer, inputModel,
							log);
					metValues.addValue(val);
					SummaryStatistics ratioValues = globalRatioStatistics
							.get(metric);
					ratioValues.addValue(calculateRatio(
							((Double) noiseLevels[i]).doubleValue(), val));
				}
			}
			for (HmmExpMetric metric : metrics) {
				SummaryStatistics metValues = values.get(metric);
				System.out.println("Average " + metric.getName() + ": "
						+ metValues.getMean());
				writeToDat(metric.getAverageWriter(),
						noiseLevels[i].toString(), metValues.getMean());
				writeToDat(metric.getVarianceWriter(), noiseLevels[i]
						.toString(), metValues.getVariance());
				globalAverageVariance.get(metric).addValue(
						metValues.getVariance());
				double ratio = calculateRatio(((Double) noiseLevels[i])
						.doubleValue(), metValues.getMean());
				writeToDat(metric.getAverageRatioWriter(), noiseLevels[i]
						.toString(), ratio);
				globalAverageRatio.get(metric).addValue(ratio);
			}
		}
		for (HmmExpMetric metric : metrics) {
			SummaryStatistics avRatio = globalAverageRatio.get(metric);
			SummaryStatistics avVariance = globalAverageVariance.get(metric);
			SummaryStatistics ratioValues = globalRatioStatistics.get(metric);
			globalMeasures.write("\nGlobal Average Ratio " + metric.getName()
					+ ": " + avRatio.getMean() + "\n");
			globalMeasures.write("Global Average Variance " + metric.getName()
					+ ": " + avVariance.getMean() + "\n");
			globalMeasures.write("Total Mean ratio " + metric.getName() + ": "
					+ ratioValues.getMean() + "\n");
			globalMeasures.write("Total Variance ratio " + metric.getName()
					+ ": " + ratioValues.getVariance() + "\n");
			metric.finishDatFiles();
		}
		HmmExpUtils.finishFile(globalMeasures);
	}

	/**
	 * Calculates the noise-fitness ratio, assuming that they should be inverse
	 * proportional to each other (the bigger the degree of noise, the lower the
	 * fitness). <br>
	 * Return value 0.0 is ideal. Positive value indicates optimistic fitness
	 * measurement. Negative value indicates pessimistic fitness measurement.
	 * 
	 * @param noise
	 *            the current noise level in per cent
	 * @param fitness
	 *            the fitness value in [0, 1]
	 * @return the fitness noise ratio: (fitness / 1-noise) - 1
	 */
	public double calculateRatio(double noise, double fitness) {
		if (fitness != 1.0) {
			return (((noise) / (100.0 - (100.0 * fitness))) - 1);
		} else if (noise != 100.0) {
			return (((100.0 * fitness) / (100.0 - noise)) - 1);
		} else {
			Message.add("Both fitness and noise are 100%", Message.DEBUG);
			return 0;
		}
	}

	/**
	 * Writes a new value to the data file.
	 * 
	 * @param writer
	 *            the file to which it should be written
	 * @param explevel
	 *            the current level of noise or completeness
	 * @param value
	 *            the value to be added
	 * @throws IOException
	 */
	private void writeToDat(BufferedWriter writer, String explevel, double value)
			throws IOException {
		writer.write(explevel + " " + value + "\n");
	}

	private void readNoisyLogs() throws Exception {
		initNoisyLogs();
		File dir = new File(typeNoiseFolder);
		File[] files = dir.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			String[] noiseLevelSeparated = fileName.split("_");
			Double noiseLevel = Double.valueOf(noiseLevelSeparated[0]);
			getLogs(file, noiseLevel);
		}
	}

	private void getLogs(File file, Double noiseLevel) throws Exception {
		if (conf.isReplicate() == false) { // read logs directly form noisy log
			// directory
			LogReader log = readlLogFromFile(file);
			noisyLogs.put(noiseLevel, log);
		} else { // read logs directly form noisy log directory
			File[] files = file.listFiles();
			ArrayList<LogReader> replications = new ArrayList<LogReader>();
			for (File subfile : files) {
				LogReader log = readlLogFromFile(subfile);
				replications.add(log);
			}
			replicatedNoisyLogs.put(noiseLevel, replications);
		}
	}

	private LogReader readlLogFromFile(File file) throws Exception {
		LogFile logFile = LogFile.getInstance(file.getAbsolutePath());
		LogReader log = BufferedLogReader.createInstance(new DefaultLogFilter(
				DefaultLogFilter.INCLUDE), logFile);
		return log;
	}

	private void initNoisyLogs() {
		if (conf.isReplicate() == false) { // read logs directly form noisy log
			// directory
			noisyLogs = new HashMap<Double, LogReader>();
		} else { // read logs directly form noisy log directory
			replicatedNoisyLogs = new HashMap<Double, ArrayList<LogReader>>();
		}
	}

	/**
	 * Connects the given model to the given log (needed for log replay).
	 * 
	 * @param model
	 *            the model that should be connected
	 * @param log
	 *            the log that it should be connected to
	 * @return the result of the connection
	 */
	protected PetriNetResult connectModelWithLog(PetriNet model, LogReader log) {
		PetriNetResult result = new PetriNetResult(model);
		PetriNetLogReaderConnectionPlugin conn = new PetriNetLogReaderConnectionPlugin();
		MainUI.getInstance()
				.connectResultWithLog(result, log, conn, true, true);
		return result;
	}

}
