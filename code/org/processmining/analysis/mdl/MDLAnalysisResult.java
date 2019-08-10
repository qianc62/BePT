/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/
package org.processmining.analysis.mdl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.logreaderconnection.PetriNetLogReaderConnectionPlugin;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.GuiUtilities;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * Displays the settings that are available for the MDL analysis. <br>
 * For example, a number of different metrics is available in the case that the
 * log does not fit given process model.
 * 
 * @author Christian Guenther, Anne Rozinat
 */
public class MDLAnalysisResult extends JPanel implements Provider {

	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	public static Color colorTextAreaBg = new Color(160, 160, 160);

	protected JComponent view;
	protected JPanel confPanel;
	protected JPanel resultPanel;
	protected ProgressPanel progress;
	protected JComboBox compactnessMetricsBox;
	protected JComboBox precisenessMetricsBox;
	protected JTextArea compactnessDescription;
	protected JTextArea precisenessDescription;

	protected PetriNet inputNet;
	protected LogReader inputLog;
	protected PetriNet frmNet;
	protected LogReader frmLog;
	protected PetriNet ermNet;
	protected LogReader ermLog;
	protected ArrayList<MDLCompactnessMetric> compactenessMetrics;
	protected ArrayList<MDLPrecisenessMetric> precisenessMetrics;
	protected LogEvent startEvent = new LogEvent("Artificial Start", "complete");
	protected LogEvent endEvent = new LogEvent("Artificial End", "complete");
	protected Place virtualPlace;
	protected AnalysisPlugin myAlgorithm;

	/**
	 * Default constructor - creates a new MDL settings panel.
	 * 
	 * @param net
	 *            the input Petri net
	 * @param log
	 *            the input event log
	 */
	public MDLAnalysisResult(PetriNet net, LogReader log,
			AnalysisPlugin algorithm) {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(80, 80, 80));
		HeaderBar header = new HeaderBar("MDL Metric");
		this.add(header, BorderLayout.NORTH);
		myAlgorithm = algorithm;
		inputNet = net;
		inputLog = log;
		view = null;
		// create baseline models and preprocess inputs
		createFRMModel();
		createERMModel();
		addVirtualPlaceToModel();
		addStartAndEndToModelAndLog();
		updateLogSummary();
		connectModelsToLog();
		// start up screen
		progress = new ProgressPanel("Measuring...");
		confPanel = constructConfigurationPanel();
		resultPanel = null;
		setView(confPanel);
	}

	/**
	 * Adds a place that is connected to every transition in the model. This
	 * will be factored out for the model complexity calculation, but is needed
	 * to actually perform the log replay for the most general model (i.e., set
	 * of disconnected transitions). <br>
	 * It is assumed that this place does not have an effect on the compression
	 * calculations as only the number of enabled tasks (but not the number of
	 * tokens) is counted there.
	 */
	protected void addVirtualPlaceToModel() {
		virtualPlace = new Place("Middle", inputNet);
		inputNet.addPlace(virtualPlace);
		for (Transition current : inputNet.getTransitions()) {
			if (current.isInvisibleTask() == false) {
				PNEdge from = new PNEdge(virtualPlace, current);
				PNEdge to = new PNEdge(current, virtualPlace);
				inputNet.addEdge(from);
				inputNet.addEdge(to);
			}
		}
	}

	/**
	 * Adds artificial start and end tasks and log events to transparently
	 * fulfill pre-conditions. Furthermore, adds a place that is connected to
	 * every (real) transition in the model.
	 */
	protected void addStartAndEndToModelAndLog() {
		// add artificial start and end to log
		for (ProcessInstance pi : inputLog.getInstances()) {
			// make start ate
			AuditTrailEntry startAte = new AuditTrailEntryImpl();
			startAte.setElement("Artificial Start");
			startAte.setType("complete");
			// make end ate
			AuditTrailEntry endAte = new AuditTrailEntryImpl();
			endAte.setElement("Artificial End");
			endAte.setType("complete");
			try {
				// adding the new ates at the first and last position
				AuditTrailEntryList ates = pi.getAuditTrailEntryList();
				ates.insert(startAte, 0);
				ates.append(endAte);
			} catch (IOException e) {
				Message.add("Fatal error in class " + this.getClass() + ":",
						Message.ERROR);
			}
		}
		// add artificial start and end to pn
		// assume single start and end place for adding the tasks
		Transition startTrans = null;
		Place startPlace = null;
		PNEdge startEdge1 = null;
		PNEdge startEdge2 = null;
		Transition endTrans = null;
		Place endPlace = null;
		PNEdge endEdge1 = null;
		PNEdge endEdge2 = null;
		for (Place p : inputNet.getPlaces()) {
			if (p.inDegree() == 0) {
				startTrans = new Transition(startEvent, inputNet);
				startPlace = new Place("Start", inputNet);
				startEdge1 = new PNEdge(startPlace, startTrans);
				startEdge2 = new PNEdge(startTrans, p);
			}
			if (p.outDegree() == 0) {
				endTrans = new Transition(endEvent, inputNet);
				endPlace = new Place("End", inputNet);
				endEdge1 = new PNEdge(endTrans, endPlace);
				endEdge2 = new PNEdge(p, endTrans);
			}
		}
		// check for the situation that there is no clear start
		if (startTrans == null) {
			startTrans = new Transition(startEvent, inputNet);
			startPlace = new Place("Start", inputNet);
			startEdge1 = new PNEdge(startPlace, startTrans);
			inputNet.addTransition(startTrans);
			inputNet.addPlace(startPlace);
			inputNet.addEdge(startEdge1);
		} else {
			inputNet.addTransition(startTrans);
			inputNet.addPlace(startPlace);
			inputNet.addEdge(startEdge1);
			inputNet.addEdge(startEdge2);
		}
		// check for the situation that there is no clear end
		if (endTrans == null) {
			endTrans = new Transition(endEvent, inputNet);
			endPlace = new Place("End", inputNet);
			endEdge1 = new PNEdge(endTrans, endPlace);
			inputNet.addTransition(endTrans);
			inputNet.addPlace(endPlace);
			inputNet.addEdge(endEdge1);
		} else {
			inputNet.addTransition(endTrans);
			inputNet.addPlace(endPlace);
			inputNet.addEdge(endEdge1);
			inputNet.addEdge(endEdge2);
		}
		// connect virtual place to start and end transitions
		PNEdge toVirt = new PNEdge(startTrans, virtualPlace);
		PNEdge fromVirt = new PNEdge(virtualPlace, endTrans);
		inputNet.addEdge(toVirt);
		inputNet.addEdge(fromVirt);
	}

	/**
	 * Creates the most general model that can be thought of. This is needed for
	 * baseline calculation.
	 */
	protected void createFRMModel() {
		PetriNet result = new PetriNet();
		Place start = new Place("Start", result);
		Place end = new Place("End", result);
		Place middle = new Place("Middle", result);
		result.addPlace(start);
		result.addPlace(end);
		result.addPlace(middle);
		Transition first = new Transition(startEvent, result);
		Transition last = new Transition(endEvent, result);
		result.addTransition(first);
		result.addTransition(last);
		PNEdge startToFirst = new PNEdge(start, first);
		PNEdge firstToMiddle = new PNEdge(first, middle);
		PNEdge middleToLast = new PNEdge(middle, last);
		PNEdge lastToEnd = new PNEdge(last, end);
		result.addEdge(startToFirst);
		result.addEdge(firstToMiddle);
		result.addEdge(middleToLast);
		result.addEdge(lastToEnd);
		for (LogEvent current : inputLog.getLogSummary().getLogEvents()) {
			Transition flowerTrans = new Transition(current, result);
			result.addTransition(flowerTrans);
			PNEdge from = new PNEdge(middle, flowerTrans);
			PNEdge to = new PNEdge(flowerTrans, middle);
			result.addEdge(from);
			result.addEdge(to);
		}
		frmNet = result;
	}

	/**
	 * Creates the most precise model one that can be thought of. This is needed
	 * for the baseline calculations.
	 */
	protected void createERMModel() {
		// artificial start and end
		PetriNet result = new PetriNet();
		Place start = new Place("Start", result);
		Place end = new Place("End", result);
		Place middle = new Place("Middle", result);
		result.addPlace(start);
		result.addPlace(end);
		result.addPlace(middle);
		Place p0 = new Place("p0", result);
		Place p1 = new Place("p1", result);
		result.addPlace(p0);
		result.addPlace(p1);
		// construct trace sequences
		HashMap<String, LogEvent> logEvents = new HashMap<String, LogEvent>();
		for (LogEvent ev : inputLog.getLogSummary().getLogEvents()) {
			logEvents.put(ev.getModelElementName(), ev);
		}
		int placeCounter = 2;
		for (ProcessInstance pi : inputLog.getInstances()) {
			Place previousPlace = p0;
			Transition en = null;
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			Iterator<AuditTrailEntry> it = ates.iterator();
			while (it.hasNext()) {
				AuditTrailEntry ate = it.next();
				LogEvent event = logEvents.get(ate.getElement());
				Transition current = new Transition(event, result);
				result.addTransition(current);
				PNEdge edge1 = new PNEdge(previousPlace, current);
				result.addEdge(edge1);
				if (it.hasNext()) {
					Place nextPlace = new Place("p" + placeCounter, result);
					result.addPlace(nextPlace);
					PNEdge edge2 = new PNEdge(current, nextPlace);
					result.addEdge(edge2);
					placeCounter++;
					previousPlace = nextPlace;
				}
				en = current; // so far the last
			}
			PNEdge to = new PNEdge(en, p1);
			result.addEdge(to);
		}
		for (Transition current : result.getTransitions()) {
			if (current.isInvisibleTask() == false) {
				PNEdge from = new PNEdge(middle, current);
				PNEdge to = new PNEdge(current, middle);
				result.addEdge(from);
				result.addEdge(to);
			}
		}
		// add start and end
		Transition first = new Transition(startEvent, result);
		Transition last = new Transition(endEvent, result);
		result.addTransition(first);
		result.addTransition(last);
		PNEdge startToFirst = new PNEdge(start, first);
		PNEdge lastToEnd = new PNEdge(last, end);
		result.addEdge(startToFirst);
		result.addEdge(lastToEnd);
		PNEdge firstToP0 = new PNEdge(first, p0);
		PNEdge p1ToLast = new PNEdge(p1, last);
		result.addEdge(firstToP0);
		result.addEdge(p1ToLast);
		// connect virtual place to start and end transitions
		PNEdge toVirt = new PNEdge(first, middle);
		PNEdge fromVirt = new PNEdge(middle, last);
		result.addEdge(toVirt);
		result.addEdge(fromVirt);
		ermNet = result;
	}

	/**
	 * Connects the input and baseline models to the logs to prepare for log
	 * replay.
	 */
	protected void connectModelsToLog() {
		try {
			PetriNetResult result1 = connectModelWithLog(inputNet, inputLog);
			inputNet = result1.getPetriNet();
			inputLog = result1.getLogReader();
			frmLog = BufferedLogReader.createInstance(inputLog,
					new DefaultLogFilter(DefaultLogFilter.INCLUDE));
			PetriNetResult result2 = connectModelWithLog(frmNet, frmLog);
			frmNet = result2.getPetriNet();
			frmLog = result2.getLogReader();
			ermLog = BufferedLogReader.createInstance(inputLog,
					new DefaultLogFilter(DefaultLogFilter.INCLUDE));
			PetriNetResult result3 = connectModelWithLog(ermNet, ermLog);
			ermNet = result3.getPetriNet();
			ermLog = result3.getLogReader();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Update the log summary as the newly added start and end events are
	 * otherwise not recognized.
	 */
	protected void updateLogSummary() {
		// update log summary as this is used when establishing the mapping
		LogEvents logEvents = inputLog.getLogSummary().getLogEvents();
		logEvents.add(startEvent);
		logEvents.add(endEvent);

		try {
			File outputFile = File.createTempFile("MDLTemp", ".mxml.gz");
			FileOutputStream output = new FileOutputStream(outputFile);
			BufferedOutputStream out = new BufferedOutputStream(
					new GZIPOutputStream(output));
			LogPersistencyStream persistency = new LogPersistencyStream(out,
					false);
			Process process = inputLog.getProcess(0);
			ProcessInstance instance = null;
			AuditTrailEntryList ateList = null;
			String name = process.getName();
			if (name == null || name.length() == 0) {
				name = "UnnamedProcess";
			}
			String description = process.getDescription();
			if (description == null || description.length() == 0) {
				description = name + " exported by MXMLib @ P-stable";
			}
			String source = inputLog.getLogSummary().getSource().getName();
			if (source == null || source.length() == 0) {
				source = "UnknownSource";
			}
			persistency.startLogfile(name, description, source);
			for (int i = 0; i < inputLog.numberOfProcesses(); i++) {
				process = inputLog.getProcess(i);
				name = process.getName();
				if (name == null || name.length() == 0) {
					name = "UnnamedProcess";
				}
				description = process.getDescription();
				if (description == null || description.length() == 0) {
					description = name + " exported by MXMLib @ P-stable";
				}
				persistency.startProcess(name, description, process
						.getAttributes());
				for (int j = 0; j < process.size(); j++) {
					instance = process.getInstance(j);
					name = instance.getName();
					if (name == null || name.length() == 0) {
						name = "UnnamedProcessInstance";
					}
					description = instance.getDescription();
					if (description == null || description.length() == 0) {
						description = name + " exported by MXMLib @ P-stable";
					}
					ateList = instance.getAuditTrailEntryList();
					persistency.startProcessInstance(name, description,
							instance.getAttributes());
					for (int k = 0; k < ateList.size(); k++) {
						persistency
								.addAuditTrailEntry(promAte2mxmlibAte(ateList
										.get(k)));
					}
					persistency.endProcessInstance();
				}
				persistency.endProcess();
			}
			// clean up
			persistency.endLogfile();
			persistency.finish();
			// read back again
			LogFile logFile = LogFile.getInstance(outputFile.getAbsolutePath());
			inputLog = BufferedLogReader.createInstance(new DefaultLogFilter(
					DefaultLogFilter.INCLUDE), logFile);
			outputFile.deleteOnExit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected org.processmining.lib.mxml.AuditTrailEntry promAte2mxmlibAte(
			AuditTrailEntry promAte) {
		org.processmining.lib.mxml.AuditTrailEntry mxmlibAte = new org.processmining.lib.mxml.AuditTrailEntry();
		mxmlibAte.setWorkflowModelElement(promAte.getElement());
		mxmlibAte.setEventType(EventType.getType(promAte.getType()));
		mxmlibAte.setOriginator(promAte.getOriginator());
		if (promAte.getTimestamp() != null) {
			mxmlibAte.setTimestamp(promAte.getTimestamp());
		}
		mxmlibAte.setAttributes(promAte.getAttributes());
		return mxmlibAte;
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

	/**
	 * Start analysis based on current "compactness" and "preciseness" measure
	 * and switch to result view.
	 */
	protected void runAnalysis() {
		setView(progress.getPanel());
		// get settings from configuration GUI
		int modelCompactness = calculateCompactnessInputModel();
		int modelPreciseness = calculatePrecisenessInputModel();
		int frmCompactness = calculateCompactnessFRMModel();
		int frmPreciseness = calculatePrecisenessFRMModel();
		int ermCompactness = calculateCompactnessERMModel();
		int ermPreciseness = calculatePrecisenessERMModel();
		resultPanel = constructResultPanel(modelCompactness, modelPreciseness,
				frmCompactness, frmPreciseness, ermCompactness, ermPreciseness);
		setView(resultPanel);
	}

	// //////////////// Calculation methods //////////////

	/**
	 * Returns "compactness" metric based on the current encoding scheme.
	 * 
	 * @return amount of bits needed to encode the model according to current
	 *         scheme
	 */
	public int calculateCompactnessInputModel() {
		MDLCompactnessMetric metric = (MDLCompactnessMetric) compactnessMetricsBox
				.getSelectedItem();
		return metric.getEncodingCost(progress, inputNet, null);
	}

	/**
	 * Returns "preciseness" metric based on the current encoding scheme.
	 * 
	 * @return amount of bits needed to encode the log in the model according to
	 *         current scheme
	 */
	public int calculatePrecisenessInputModel() {
		MDLPrecisenessMetric metric = (MDLPrecisenessMetric) precisenessMetricsBox
				.getSelectedItem();
		return metric.getEncodingCost(progress, inputNet, inputLog);
	}

	/**
	 * Returns "compactness" metric for FRM based on the current encoding
	 * scheme.
	 * 
	 * @return amount of bits needed to encode the model according to current
	 *         scheme
	 */
	public int calculateCompactnessFRMModel() {
		MDLCompactnessMetric metric = (MDLCompactnessMetric) compactnessMetricsBox
				.getSelectedItem();
		return metric.getEncodingCost(progress, frmNet, null);
	}

	/**
	 * Returns "preciseness" metric for FRM based on the current encoding
	 * scheme.
	 * 
	 * @return amount of bits needed to encode the log in the model according to
	 *         current scheme
	 */
	public int calculatePrecisenessFRMModel() {
		MDLPrecisenessMetric metric = (MDLPrecisenessMetric) precisenessMetricsBox
				.getSelectedItem();
		return metric.getEncodingCost(progress, frmNet, frmLog);
	}

	/**
	 * Returns "compactness" metric for ERM based on the current encoding
	 * scheme.
	 * 
	 * @return amount of bits needed to encode the model according to current
	 *         scheme
	 */
	public int calculateCompactnessERMModel() {
		MDLCompactnessMetric metric = (MDLCompactnessMetric) compactnessMetricsBox
				.getSelectedItem();
		return metric.getEncodingCost(progress, ermNet, null);
	}

	/**
	 * Returns "preciseness" metric for ERM based on the current encoding
	 * scheme.
	 * 
	 * @return amount of bits needed to encode the log in the model according to
	 *         current scheme
	 */
	public int calculatePrecisenessERMModel() {
		MDLPrecisenessMetric metric = (MDLPrecisenessMetric) precisenessMetricsBox
				.getSelectedItem();
		return metric.getEncodingCost(progress, ermNet, ermLog);
	}

	/**
	 * Calculates the model complexity with respect to the baseline models. <br>
	 * Yields a value within (-infinite, 1].
	 * 
	 * @param model
	 *            the compactness encoding cost for the model to be evaluated
	 * @param frm
	 *            the compactness encoding cost for the frm baseline model
	 * @param erm
	 *            the compactness encoding cost for the erm baseline model
	 * @return the calculated complexity value
	 */
	public double calculateComplexity(int model, int frm, int erm) {
		double modelD = (double) model;
		double frmD = (double) frm;
		double ermD = (double) erm;
		return (ermD - modelD) / (ermD - frmD);
	}

	/**
	 * Calculates the model complexity only in reference to the ERM baseline
	 * model.
	 * 
	 * @param model
	 *            the compactness encoding cost for the model to be evaluated
	 * @param erm
	 *            the compactness encoding cost for the erm baseline model
	 * @return the calculated complexity value
	 */
	public double calculateToonComplexity(int model, int erm) {
		double modelD = (double) model;
		double ermD = (double) erm;
		return 1 - modelD / ermD;
	}

	/**
	 * Cuts off negative values of the given parameter at zero.
	 * 
	 * @param value
	 *            the parameter that should be stripped into [0, 1].
	 * @return the rounded value
	 */
	public double getRoundedValue(double value) {
		if (value < 0) {
			return 0.0;
		} else {
			return value;
		}
	}

	/**
	 * Calculates the log compression with respect to the baseline models. <br>
	 * Yields a value within (-infinite, 1].
	 * 
	 * @param model
	 *            the precisenss encoding cost for the model to be evaluated
	 * @param frm
	 *            the precisenss encoding cost for the frm baseline model
	 * @param erm
	 *            the precisenss encoding cost for the erm baseline model
	 * @return the calculated compression value
	 */
	public double calculateCompression(int model, int frm, int erm) {
		double modelD = (double) model;
		double frmD = (double) frm;
		double ermD = (double) erm;
		return (frmD - modelD) / (frmD - ermD);
	}

	/**
	 * Calculates the log compression only in reference to the FRM baseline
	 * model.
	 * 
	 * @param model
	 *            the precisenss encoding cost for the model to be evaluated
	 * @param frm
	 *            the precisenss encoding cost for the frm baseline model
	 * @return the calculated compression value
	 */
	public double calculateToonCompression(int model, int frm) {
		double modelD = (double) model;
		double frmD = (double) frm;
		return 1 - modelD / frmD;
	}

	/**
	 * Calculates MDL metric value based on the given complexity and compression
	 * values for the current alpha value.
	 * 
	 * @param complexity
	 *            the rounded model complexity
	 * @param compression
	 *            the rounded log compression
	 * @return the MDL metric [0, 1]
	 */
	public double calculateMDLMetric(double complexity, double compression) {
		// TODO: make alpha a parameter and put slider in GUI
		double alpha = 0.5;
		return alpha * compression + (1 - alpha) * complexity;
	}

	/**
	 * Creates a CSV representation of the MDL metrics (to be opened in
	 * MSExcel).
	 * 
	 * @return the string to be written to the CSV file
	 */
	public String getCsvRepresentation() {
		StringBuffer sb = new StringBuffer();
		// write meta header
		sb.append("Model encoding costs;;;");
		sb.append("Log encoding costs;;;");
		sb.append("MDL metrics;;;;;;;;;;;");
		sb.append("\n");
		// write table header
		sb.append("Model compactness;");
		sb.append("FRM compactness;");
		sb.append("ERM compactness;");
		sb.append("Model preciseness;");
		sb.append("FRM preciseness;");
		sb.append("ERM preciseness;");
		sb.append("Added Model and Log encoding cost;");
		sb.append("Model complexity;");
		sb.append("Projected Model complexity;");
		sb.append("New Model complexity;");
		sb.append("Projected New Model complexity;");
		sb.append("Model compression;");
		sb.append("Projected Model compression;");
		sb.append("New Model compression;");
		sb.append("Projected New Model compression;");
		sb.append("MDL metric (alpha 0.5);");
		sb.append("New MDL metric (alpha 0.5)");
		sb.append("\n");
		// write values
		int modelCompactness = calculateCompactnessInputModel();
		int modelPreciseness = calculatePrecisenessInputModel();
		int frmCompactness = calculateCompactnessFRMModel();
		int frmPreciseness = calculatePrecisenessFRMModel();
		int ermCompactness = calculateCompactnessERMModel();
		int ermPreciseness = calculatePrecisenessERMModel();
		double compression = calculateCompression(modelPreciseness,
				frmPreciseness, ermPreciseness);
		double complexity = calculateComplexity(modelCompactness,
				frmCompactness, ermCompactness);
		double newCompression = calculateToonCompression(modelPreciseness,
				frmPreciseness);
		double newComplexity = calculateToonComplexity(modelCompactness,
				ermCompactness);
		sb.append(modelCompactness + ";");
		sb.append(frmCompactness + ";");
		sb.append(ermCompactness + ";");
		sb.append(modelPreciseness + ";");
		sb.append(frmPreciseness + ";");
		sb.append(ermPreciseness + ";");
		sb.append((modelCompactness + modelPreciseness) + ";");
		sb.append(complexity + ";");
		sb.append(getRoundedValue(complexity) + ";");
		sb.append(newComplexity + ";");
		sb.append(getRoundedValue(newComplexity) + ";");
		sb.append(compression + ";");
		sb.append(getRoundedValue(compression) + ";");
		sb.append(newCompression + ";");
		sb.append(getRoundedValue(newCompression) + ";");
		sb.append(calculateMDLMetric(getRoundedValue(complexity),
				getRoundedValue(compression))
				+ ";");
		sb.append(calculateMDLMetric(getRoundedValue(newComplexity),
				getRoundedValue(newCompression))
				+ "");
		sb.append("\n");
		return sb.toString();
	}

	// /////////////// GUI methods ////////////////////////

	/**
	 * Updates the view by updating the content of the window by the given GUI
	 * component.
	 * 
	 * @param comp
	 *            the component to be displayed in the result frame.
	 */
	protected void setView(JComponent comp) {
		if (view != null) {
			this.remove(view);
		}
		view = comp;
		this.add(view, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	/**
	 * Constructs the configuration view.
	 * 
	 * @return the GUI panel containing the configuration elements for the MDL
	 *         plugin.
	 * @see #setView(JComponent)
	 */
	protected JPanel constructConfigurationPanel() {
		// assemble list of available compactness metrics
		compactenessMetrics = new ArrayList<MDLCompactnessMetric>();
		compactenessMetrics.add(new PetriNetEncodingCompactness()); // default
		compactenessMetrics.add(new ElementBasedCompactness());
		compactenessMetrics.add(new EdgeEncodingCompactness());
		compactenessMetrics.add(new TransitionEncodingCompactness());
		// assemble list of available clustering algorithms
		precisenessMetrics = new ArrayList<MDLPrecisenessMetric>();
		precisenessMetrics.add(new EEVTPreciseness()); // default
		precisenessMetrics.add(new ErrorFreePreciseness());
		precisenessMetrics.add(new EETLPreciseness());
		precisenessMetrics.add(new IETLPreciseness());
		precisenessMetrics.add(new IEVTPreciseness());

		// prepare comboboxes
		compactnessMetricsBox = new JComboBox(compactenessMetrics.toArray());
		compactnessMetricsBox.setPreferredSize(new Dimension(700,
				compactnessMetricsBox.getPreferredSize().height));
		compactnessMetricsBox.setMinimumSize(new Dimension(700,
				compactnessMetricsBox.getPreferredSize().height));
		compactnessMetricsBox.setMaximumSize(new Dimension(700,
				compactnessMetricsBox.getPreferredSize().height));
		compactnessMetricsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MDLCompactnessMetric alg = (MDLCompactnessMetric) compactnessMetricsBox
						.getSelectedItem();
				compactnessDescription.setText(alg.getDescription());
			}
		});
		compactnessMetricsBox.setOpaque(false);
		precisenessMetricsBox = new JComboBox(precisenessMetrics.toArray());
		precisenessMetricsBox.setPreferredSize(new Dimension(700,
				precisenessMetricsBox.getPreferredSize().height));
		precisenessMetricsBox.setMinimumSize(new Dimension(700,
				precisenessMetricsBox.getPreferredSize().height));
		precisenessMetricsBox.setMaximumSize(new Dimension(700,
				precisenessMetricsBox.getPreferredSize().height));
		precisenessMetricsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MDLPrecisenessMetric metric = (MDLPrecisenessMetric) precisenessMetricsBox
						.getSelectedItem();
				precisenessDescription.setText(metric.getDescription());
			}
		});
		precisenessMetricsBox.setOpaque(false);

		JLabel metricsLabel = new JLabel("Model Complexity metric");
		metricsLabel.setOpaque(false);
		metricsLabel.setForeground(colorFg);
		metricsLabel.setFont(metricsLabel.getFont().deriveFont(16.0f));
		JLabel algorithmsLabel = new JLabel("Log Encoding Cost metric");
		algorithmsLabel.setOpaque(false);
		algorithmsLabel.setForeground(colorFg);
		algorithmsLabel.setFont(algorithmsLabel.getFont().deriveFont(16.0f));
		compactnessDescription = new JTextArea();// 1, 60);
		compactnessDescription.setWrapStyleWord(true);
		compactnessDescription.setLineWrap(true);
		compactnessDescription.setFont(compactnessDescription.getFont()
				.deriveFont(11f));
		compactnessDescription.setBorder(BorderFactory.createEmptyBorder(3, 3,
				3, 3));
		compactnessDescription.setBackground(colorTextAreaBg);
		compactnessDescription.setForeground(colorFg);
		compactnessDescription.setMaximumSize(new Dimension(700, 70));
		compactnessDescription.setMinimumSize(new Dimension(700, 70));
		compactnessDescription.setPreferredSize(new Dimension(700, 70));
		compactnessDescription
				.setText(((MDLCompactnessMetric) compactnessMetricsBox
						.getSelectedItem()).getDescription());
		precisenessDescription = new JTextArea();// 1, 60);
		precisenessDescription.setWrapStyleWord(true);
		precisenessDescription.setLineWrap(true);
		precisenessDescription.setFont(precisenessDescription.getFont()
				.deriveFont(11f));
		precisenessDescription.setBorder(BorderFactory.createEmptyBorder(3, 3,
				3, 3));
		precisenessDescription.setBackground(colorTextAreaBg);
		precisenessDescription.setForeground(colorFg);
		precisenessDescription.setMaximumSize(new Dimension(700, 70));
		precisenessDescription.setMinimumSize(new Dimension(700, 70));
		precisenessDescription.setPreferredSize(new Dimension(700, 70));
		precisenessDescription
				.setText(((MDLPrecisenessMetric) precisenessMetricsBox
						.getSelectedItem()).getDescription());

		RoundedPanel confPanel = new RoundedPanel(10, 5, 5);
		confPanel.setBackground(new Color(140, 140, 140));
		confPanel.setLayout(new BoxLayout(confPanel, BoxLayout.Y_AXIS));
		JButton startButton = new AutoFocusButton("Start analysis");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			startButton.setOpaque(false);
		}
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runAnalysis();
			}
		});
		JButton docsButton = new SlickerButton("Help...");
		docsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(myAlgorithm);
			}
		});

		confPanel.add(packHorizontallyLeftAligned(metricsLabel, 5));
		confPanel.add(Box.createVerticalStrut(10));
		confPanel.add(packHorizontallyLeftAligned(compactnessMetricsBox, 25));
		confPanel.add(Box.createVerticalStrut(7));
		confPanel.add(packHorizontallyLeftAligned(compactnessDescription, 25));
		confPanel.add(Box.createVerticalStrut(40));
		confPanel.add(packHorizontallyLeftAligned(algorithmsLabel, 5));
		confPanel.add(Box.createVerticalStrut(10));
		confPanel.add(packHorizontallyLeftAligned(precisenessMetricsBox, 25));
		confPanel.add(Box.createVerticalStrut(7));
		confPanel.add(packHorizontallyLeftAligned(precisenessDescription, 25));
		confPanel.add(Box.createVerticalGlue());

		confPanel.add(Box.createVerticalStrut(20));
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalStrut(10));
		packed.add(docsButton);
		packed.add(Box.createHorizontalGlue());
		packed.add(startButton);
		int height = (int) startButton.getMinimumSize().getHeight();
		packed.setMinimumSize(startButton.getMinimumSize());
		packed.setMaximumSize(new Dimension(4000, (int) height));
		packed.setPreferredSize(new Dimension(4000, (int) height));
		confPanel.add(packed);
		return confPanel;
	}

	protected static JPanel packHorizontallyLeftAligned(JComponent comp,
			int leftOffset) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		if (leftOffset > 0) {
			packed.add(Box.createHorizontalStrut(leftOffset));
		}
		packed.add(comp);
		packed.add(Box.createHorizontalGlue());
		int height = (int) comp.getMinimumSize().getHeight();
		packed.setMinimumSize(comp.getMinimumSize());
		packed.setMaximumSize(new Dimension(4000, (int) height));
		packed.setPreferredSize(new Dimension(4000, (int) height));
		return packed;
	}

	protected static JPanel packHorizontallyRightAligned(JComponent comp,
			int rightOffset) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalGlue());
		packed.add(comp);
		if (rightOffset > 0) {
			packed.add(Box.createHorizontalStrut(rightOffset));
		}
		int height = (int) comp.getMinimumSize().getHeight();
		packed.setMinimumSize(comp.getMinimumSize());
		packed.setMaximumSize(new Dimension(4000, (int) height));
		packed.setPreferredSize(new Dimension(4000, (int) height));
		return packed;
	}

	/**
	 * Constructs the result view.
	 * 
	 * @return the GUI panel containing the results for the MDL plugin.
	 * @see #setView(JComponent)
	 */
	protected JPanel constructResultPanel(int modelCompactness,
			int modelPreciseness, int frmCompactness, int frmPreciseness,
			int ermCompactness, int ermPreciseness) {
		RoundedPanel resultPanel = new RoundedPanel(10, 5, 5);
		resultPanel.setBackground(new Color(160, 160, 160));
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		JButton backButton = new AutoFocusButton("  Go back  ");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			backButton.setOpaque(false);
		}
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setView(confPanel);
			}
		});
		JButton saveCSVButton = new SlickerButton("Save as CSV");
		saveCSVButton.setToolTipText("Saves the MDL metrics as CSV file");
		saveCSVButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// actually save to file
				JFileChooser saveDialog = new JFileChooser();
				saveDialog.setSelectedFile(new File("MDL_Metrics.csv"));
				if (saveDialog.showSaveDialog(MainUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
					File outFile = saveDialog.getSelectedFile();
					try {
						BufferedWriter outWriter = new BufferedWriter(
								new FileWriter(outFile));
						outWriter.write(getCsvRepresentation());
						outWriter.flush();
						outWriter.close();
						JOptionPane.showMessageDialog(MainUI.getInstance(),
								"MDL metrics have been saved\nto CSV file!",
								"MDL metrics saved.",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JPanel jointPanel = new JPanel();
		jointPanel.setOpaque(false);
		jointPanel.setBorder(BorderFactory.createEmptyBorder());
		jointPanel.setLayout(new GridLayout(1, 2));

		double compression = calculateCompression(modelPreciseness,
				frmPreciseness, ermPreciseness);
		double complexity = calculateComplexity(modelCompactness,
				frmCompactness, ermCompactness);
		double newCompression = calculateToonCompression(modelPreciseness,
				frmPreciseness);
		double newComplexity = calculateToonComplexity(modelCompactness,
				ermCompactness);

		HashMap<String, String> nameAndValue;
		ArrayList<HashMap<String, String>> compactnessResults = new ArrayList<HashMap<String, String>>();
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("Model Compactness", Integer
				.toString(modelCompactness));
		compactnessResults.add(nameAndValue);
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("FRM Compactness", Integer.toString(frmCompactness));
		compactnessResults.add(nameAndValue);
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("ERM Compactness", Integer.toString(ermCompactness));
		compactnessResults.add(nameAndValue);
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("Model Complexity", Double.toString(complexity));
		compactnessResults.add(nameAndValue);
		nameAndValue = new HashMap<String, String>();
		nameAndValue
				.put("New Model Complexity", Double.toString(newComplexity));
		compactnessResults.add(nameAndValue);

		ArrayList<HashMap<String, String>> precisenessResults = new ArrayList<HashMap<String, String>>();
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("Model Preciseness", Integer
				.toString(modelPreciseness));
		precisenessResults.add(nameAndValue);
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("FRM Preciseness", Integer.toString(frmPreciseness));
		precisenessResults.add(nameAndValue);
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("ERM Preciseness", Integer.toString(ermPreciseness));
		precisenessResults.add(nameAndValue);
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("Log Compression", Double.toString(compression));
		precisenessResults.add(nameAndValue);
		nameAndValue = new HashMap<String, String>();
		nameAndValue
				.put("New Log Compression", Double.toString(newCompression));
		precisenessResults.add(nameAndValue);

		jointPanel
				.add(GuiUtilities
						.configureAnyScrollable(
								packResult(compactnessResults),
								"Model Complexity",
								"The following results were computed for your chosen model complexity (i.e., compactness) metric.",
								new Color(190, 190, 190)));
		jointPanel
				.add(GuiUtilities
						.configureAnyScrollable(
								packResult(precisenessResults),
								"Log Encoding Cost",
								"The following results were computed for your chosen log encoding cost (i.e., preciseness) metric.",
								new Color(190, 190, 190)));
		resultPanel.add(jointPanel, BorderLayout.CENTER);

		ArrayList<HashMap<String, String>> metricResults = new ArrayList<HashMap<String, String>>();
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("MDL Metric (alpha = 0.5)", Double
				.toString(calculateMDLMetric(getRoundedValue(complexity),
						getRoundedValue(compression))));
		metricResults.add(nameAndValue);
		nameAndValue = new HashMap<String, String>();
		nameAndValue.put("New MDL Metric (added up)", Double
				.toString(newComplexity + newCompression));
		metricResults.add(nameAndValue);
		resultPanel
				.add(
						GuiUtilities
								.configureAnyScrollable(
										packResult(metricResults),
										"MDL Metric",
										"The following MDL metric value was calculated based on the rounded complexity and compression.",
										new Color(190, 190, 190)),
						BorderLayout.SOUTH);

		resultPanel.add(Box.createVerticalStrut(20));
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalStrut(10));
		packed.add(saveCSVButton);
		packed.add(Box.createHorizontalGlue());
		packed.add(backButton);
		int height = (int) backButton.getMinimumSize().getHeight();
		packed.setMinimumSize(backButton.getMinimumSize());
		packed.setMaximumSize(new Dimension(4000, (int) height));
		packed.setPreferredSize(new Dimension(4000, (int) height));
		resultPanel.add(packed);
		return resultPanel;
	}

	protected JPanel packResult(
			ArrayList<HashMap<String, String>> nameValuePairs) {
		JPanel resultPanel = new JPanel();
		resultPanel.setMaximumSize(new Dimension(2000, 30));
		resultPanel.setBackground(new Color(190, 190, 190));
		resultPanel.setBorder(BorderFactory.createEmptyBorder());
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		for (HashMap<String, String> nameVal : nameValuePairs) {
			Entry<String, String> entry = nameVal.entrySet().iterator().next();
			String name = entry.getKey();
			String value = entry.getValue();
			JPanel rowPanel = new JPanel();
			rowPanel.setBackground(new Color(190, 190, 190));
			rowPanel.setBorder(BorderFactory.createEmptyBorder());
			rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
			JLabel titleLabel = new JLabel(name + ":");
			titleLabel.setOpaque(false);
			titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
			JLabel valueLabel = new JLabel(value);
			valueLabel.setOpaque(false);
			rowPanel.add(titleLabel);
			rowPanel.add(Box.createHorizontalStrut(20));
			rowPanel.add(valueLabel);
			rowPanel.add(Box.createHorizontalGlue());
			resultPanel.add(rowPanel);
		}
		return resultPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		try {
			ProvidedObject[] objects = {
					new ProvidedObject("Petri net", new Object[] { inputNet }),
					new ProvidedObject("FRM baseline model",
							new Object[] { frmNet }),
					new ProvidedObject("ERM baseline model",
							new Object[] { ermNet }),
					new ProvidedObject("Log", new Object[] { inputLog }),
					new ProvidedObject("FRM Log", new Object[] { frmLog }),
					new ProvidedObject("ERM Log", new Object[] { ermLog }) };
			return objects;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
