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

package org.processmining.framework.log.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.analysis.summary.ExtendedLogSummary;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.LightweightLogSummary;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;
import org.w3c.dom.Node;

/**
 * This logFilter filters the log based on the modelelements of
 * audittrailentries. Furthermore, it is possible to filter for tasks of which
 * the frequency (in percentages) in the log is less than a given percentage and
 * you can filter for tasks for which the freqency (in percentages) of in how
 * many different process instances they occur is less than a given percentage.
 * 
 * @author Ronny Mans
 * @version 1.0
 */
public class LogEventLogFilterEnh extends LogEventLogFilter {

	/**
	 * default value of the percentage to filter for tasks of which the
	 * frequency (in percentages) in the log is less than a given percentage
	 */
	private double percentageTask = 0.0;

	/**
	 * default value of the percentage for filtering for tasks of which the
	 * freqency (in percentages) of in how many different process instances they
	 * occur is less than a given percentage.
	 */
	private double percentagePI = 0.0;

	/**
	 * the selected item of the combobox.
	 */
	private String selectedItemComboBox = "AND";

	/**
	 * Default constructor.
	 */
	public LogEventLogFilterEnh() {
		super(LogFilter.MODERATE, "Enhanced event log filter");
	}

	/**
	 * Constructor. The logevents that have to be kept after filtering can be
	 * provided.
	 * 
	 * @param eventsToKeep
	 *            LogEvents the logevents that have to be kept after filtering.
	 */
	public LogEventLogFilterEnh(LogEvents eventsToKeep) {
		super(LogFilter.MODERATE, "Enhanced event log filter", eventsToKeep);
	}

	/**
	 * Constructor. The events that have to be kept after filtering can be
	 * provided. Also, the percentage values for filtering for tasks of which
	 * the frequency is less than a given percentage and for filtering for tasks
	 * for which the freqency (in percentages) of in how many different process
	 * instances they occur is less than a given percentage can be provided.
	 * 
	 * @param eventsToKeep
	 *            LogEvents the logevents that have to be kept after filtering.
	 * @param percTask
	 *            double the percentage for filtering for tasks of which the
	 *            frequency is less than a given percentage
	 * @param percPI
	 *            double the percentage for filtering for tasks of which the
	 *            frequency in how many many different process instances they
	 *            occur is less than a given percentage
	 */
	public LogEventLogFilterEnh(LogEvents eventsToKeep, double percTask,
			double percPI) {
		super(LogFilter.MODERATE, "Enhanced event log filter", eventsToKeep);
		this.percentageTask = percTask;
		this.percentagePI = percPI;
	}

	public LogEventLogFilterEnh(LogEvents eventsToKeep, double percTask,
			double percPI, String selectedItemComboBox) {
		super(LogFilter.MODERATE, "Enhanced event log filter", eventsToKeep);
		this.percentageTask = percTask;
		this.percentagePI = percPI;
		this.selectedItemComboBox = selectedItemComboBox;
	}

	/**
	 * Returns the help for this LogFilter as HTML text.
	 * 
	 * @return the help as string.
	 */
	protected String getHelpForThisLogFilter() {
		return super.getHelpForThisLogFilter()
				+ " Additionally you can use the interface at the top of the "
				+ "screen to filter for tasks of which the frequency (in percentages) in the log "
				+ "is less than the percentage in the first box and you can filter for tasks for "
				+ " which the freqency (in percentages) of in how many different process instances they occur is less than "
				+ "the percentage in the second box. With the combobox you can set whether the relationship "
				+ "between these two percentages values should be AND or OR. By clicking on the set button you can see which tasks "
				+ "will be deselected with regard to these two percentage values. "
				+ "The percentage next to the task indicates the frequency of the task in the log.";
	}

	/**
	 * Returns a Panel for the setting of parameters. When a LogFilter can be
	 * added to a list in the framework. This panel is shown, and parameters can
	 * be set. When the dialog is closed, a new instance of a LogFilter is
	 * created by the framework by calling the <code>getNewLogFilter</code>
	 * method of the dialog.
	 * 
	 * @param summary
	 *            A LogSummary to be used for setting parameters.
	 * @return JPanel
	 */
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary, LogEventLogFilterEnh.this) {

			LogEventCheckBoxEnh[] checks;
			JSpinner percTaskSpinner;
			JSpinner percPiSpinner;
			JComboBox choiceBox;

			/**
			 * Keep the statistics for all the tasks
			 */
			SummaryStatistics taskStatistics = null;

			/**
			 * Keep the statistics for the occurrence of tasks in process
			 * instances
			 */
			SummaryStatistics piStatistics = null;

			public LogFilter getNewLogFilter() {
				LogEvents e = new LogEvents();
				for (int i = 0; i < checks.length; i++) {
					if (checks[i].isSelected()) {
						e.add(checks[i].getLogEvent());
					}
				}
				return new LogEventLogFilterEnh(e,
						getDoubleValueFromSpinner(percTaskSpinner.getValue()),
						getDoubleValueFromSpinner(percPiSpinner.getValue()),
						choiceBox.getSelectedItem().toString());
			}

			protected JPanel getPanel() {
				// add message to the test log for this plugin
				Message.add("<EnhEvtLogFilter>", Message.TEST);
				// statistics
				taskStatistics = SummaryStatistics.newInstance();
				piStatistics = SummaryStatistics.newInstance();
				// Set up an percentformatter
				NumberFormat percentFormatter = NumberFormat
						.getPercentInstance();
				percentFormatter.setMinimumFractionDigits(2);
				percentFormatter.setMaximumFractionDigits(2);
				// Instantiate the spinners
				percTaskSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.0,
						100.0, 1.0));
				percPiSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.0,
						100.0, 1.0));
				// generate the buttons that are needed
				JButton jButtonCalculate = new JButton("Calculate");
				JButton jButtonInvert = new JButton("Invert selection");
				// set up a choicebox to indicate whether the relationship
				// between the two
				// percentages is AND or OR.

				percTaskSpinner.setValue(new Double(percentageTask));
				percPiSpinner.setValue(new Double(percentagePI));

				choiceBox = new JComboBox();
				choiceBox.addItem("AND");
				choiceBox.addItem("OR");
				choiceBox.setSelectedItem(selectedItemComboBox);

				// Some values that are needed for the sequel.
				int size = summary.getLogEvents().size();
				// For the new log reader sumATEs should be calculated in
				// another way
				int sumATEs = 0;
				if (summary instanceof ExtendedLogSummary) {
					sumATEs = summary.getNumberOfAuditTrailEntries();
				} else if (summary instanceof LightweightLogSummary) {
					HashSet<ProcessInstance> pis = new HashSet<ProcessInstance>();
					Iterator logEvents = summary.getLogEvents().iterator();
					while (logEvents.hasNext()) {
						LogEvent evt = (LogEvent) logEvents.next();
						pis.addAll(summary.getInstancesForEvent(evt));
					}
					Iterator pis2 = pis.iterator();
					while (pis2.hasNext()) {
						ProcessInstance pi = (ProcessInstance) pis2.next();
						int simPis = MethodsForWorkflowLogDataStructures
								.getNumberSimilarProcessInstances(pi);
						int numberATEs = pi.getAuditTrailEntryList().size();
						sumATEs += simPis * numberATEs;
						pi.isEmpty();
					}
				} else {

				}
				// calculate the number of process Instances, taking into
				// account
				// the number of similar instances
				int sumPIs = 0;
				if (summary instanceof LightweightLogSummary) {
					sumPIs = calculateSumPIs(summary);
				}
				checks = new LogEventCheckBoxEnh[size];

				// create panels and labels
				JPanel global = new JPanel(new BorderLayout());
				JPanel sub2 = new JPanel(new BorderLayout());
				sub2.setBackground(Color.white);
				JLabel labelPercTask = new JLabel("percentage task");
				JLabel labelPercPI = new JLabel("percentage PI");

				// create panel sub1 to put the checkboxes on
				JPanel sub1 = new JPanel(new SpringLayout());
				sub1.setBackground(Color.lightGray);

				// Get percentage of task in the log and percentage of in how
				// many
				// different PIs it appears.
				Iterator it = summary.getLogEvents().iterator();
				int i = 0;
				while (it.hasNext()) {
					LogEvent evt = (LogEvent) it.next();
					double percent = 0.0;
					if (summary instanceof ExtendedLogSummary) {
						percent = ((double) evt.getOccurrenceCount() / (double) sumATEs);
					} else if (summary instanceof LightweightLogSummary) {
						Set instances = summary.getInstancesForEvent(evt);
						// getFrequencyTasks(evt, instances)
						percent = (double) getFrequencyTasks(evt, instances)
								/ (double) sumATEs;
					} else {

					}
					// String percString = percentFormatter.format(percent);
					LogEventCheckBoxEnh check = new LogEventCheckBoxEnh(evt);
					check.setPercentageTask(percent * 100);
					// add percentage to the statistics for the tasks
					taskStatistics.addValue(percent);
					// Get percentage of in how many different PIs a task
					// appears,
					// taking into account whether the new or old logreader is
					// used
					if (summary instanceof LightweightLogSummary) {
						Set<ProcessInstance> pis = summary
								.getInstancesForEvent(evt);
						int numberInstancesTask = 0;
						Iterator it2 = pis.iterator();
						while (it2.hasNext()) {
							ProcessInstance pi = (ProcessInstance) it2.next();
							numberInstancesTask += MethodsForWorkflowLogDataStructures
									.getNumberSimilarProcessInstances(pi);
						}
						double fPI = (double) numberInstancesTask
								/ (double) sumPIs;

						check.setPercentagePI(fPI * 100);
						// add percentage to the statistics for the PIs
						piStatistics.addValue(fPI);
					} else if (summary instanceof ExtendedLogSummary) {
						double percPIcheck = getPercentagePI(evt);
						check.setPercentagePI(percPIcheck);
						piStatistics.addValue(percPIcheck / 100);
					} else {
						// raise exception, unknown logreader
					}
					// add to the checks array
					checks[i++] = check;
				}
				// fill sub1 with statistics information
				sub1.add(new JLabel(" Statistics    ( #tasks = "
						+ taskStatistics.getN() + " )"));
				sub1.add(new JLabel(" "));
				sub1.add(new JLabel(" "));
				sub1.add(new JLabel(" Arithmetic Mean "));
				sub1.add(new JLabel(percentFormatter.format(taskStatistics
						.getMean())));
				sub1.add(new JLabel(percentFormatter.format(piStatistics
						.getMean())));
				sub1.add(new JLabel(" Geometric Mean "));
				sub1.add(new JLabel(percentFormatter.format(taskStatistics
						.getGeometricMean())));
				sub1.add(new JLabel(percentFormatter.format(piStatistics
						.getGeometricMean())));
				sub1.add(new JLabel(" Standard Deviation "));
				sub1.add(new JLabel(percentFormatter.format(taskStatistics
						.getStandardDeviation())));
				sub1.add(new JLabel(percentFormatter.format(piStatistics
						.getStandardDeviation())));
				sub1.add(new JLabel(" Min "));
				sub1.add(new JLabel(percentFormatter.format(taskStatistics
						.getMin())));
				sub1.add(new JLabel(percentFormatter.format(piStatistics
						.getMin())));
				sub1.add(new JLabel(" Max "));
				sub1.add(new JLabel(percentFormatter.format(taskStatistics
						.getMax())));
				sub1.add(new JLabel(percentFormatter.format(piStatistics
						.getMax())));
				sub1.add(new JLabel(" ------------------ "));
				sub1.add(new JLabel(" --------------- "));
				sub1.add(new JLabel(" --------------- "));
				sub1.add(new JLabel(" Tasks "));
				sub1.add(new JLabel(" percentage task "));
				sub1.add(new JLabel(" percentage PI "));
				// generate messages for the test case for this plugin
				Message.add("number tasks: " + taskStatistics.getN(),
						Message.TEST);
				Message.add("<percentage task>", Message.TEST);
				Message.add("arithmetic mean: " + taskStatistics.getMean(),
						Message.TEST);
				Message.add("geometric mean: "
						+ taskStatistics.getGeometricMean(), Message.TEST);
				Message.add("standard deviation: "
						+ taskStatistics.getStandardDeviation(), Message.TEST);
				Message.add("min: " + taskStatistics.getMin(), Message.TEST);
				Message.add("max: " + taskStatistics.getMax(), Message.TEST);
				Message.add("<percentage task/>", Message.TEST);
				Message.add("<percentage PI>", Message.TEST);
				Message.add("arithmetic mean: " + piStatistics.getMean(),
						Message.TEST);
				Message.add("geometric mean: "
						+ piStatistics.getGeometricMean(), Message.TEST);
				Message.add("standard deviation: "
						+ piStatistics.getStandardDeviation(), Message.TEST);
				Message.add("min: " + piStatistics.getMin(), Message.TEST);
				Message.add("max: " + piStatistics.getMax(), Message.TEST);
				Message.add("<percentage PI/>", Message.TEST);
				// add the checkboxes to the GUI.
				Arrays.sort(checks);
				for (i = 0; i < checks.length; i++) {
					sub1.add(checks[i]);
					if ((eventsToKeep != null)
							&& (!eventsToKeep.contains(checks[i].getLogEvent()))) {
						checks[i].setSelected(false);
					}
					// put the percentages on the GUI
					sub1.add(new JLabel(percentFormatter.format(checks[i]
							.getPercentageTask() / 100)));
					sub1.add(new JLabel(percentFormatter.format(checks[i]
							.getPercentagePI() / 100)));
				}
				//
				SpringUtilities util = new SpringUtilities();
				util.makeCompactGrid(sub1, checks.length + 8, 3, 3, 3, 8, 3);
				// put the contents on the respective panels
				global.add(sub2, java.awt.BorderLayout.CENTER);
				global.add(sub1, java.awt.BorderLayout.SOUTH);
				//
				JPanel sub21 = new JPanel(new SpringLayout());
				// sub21.setLayout(new BoxLayout(sub21, BoxLayout.PAGE_AXIS));
				sub2.setBackground(Color.red);
				JPanel textPanel = new JPanel(new BorderLayout());
				textPanel.setBackground(Color.yellow);
				JPanel sub221 = new JPanel(new FlowLayout());
				sub221.setBackground(Color.yellow);
				JPanel sub222 = new JPanel(new FlowLayout());
				sub222.setBackground(Color.yellow);
				// two different panels to be places on sub21
				// JPanel sub21First = new JPanel();
				// sub21First.setLayout(new BoxLayout(sub21First,
				// BoxLayout.LINE_AXIS));
				// sub21First.setMaximumSize(new Dimension(1000, 25));
				// sub21First.add(Box.createHorizontalGlue());
				// sub21First.add(labelPercTask);
				// sub21First.add(percTaskSpinner, null);
				// percTaskSpinner.setMaximumSize(new Dimension(10, 20));
				// sub21First.add(jButtonCalculate);
				// jButtonCalculate.setMaximumSize(new Dimension(10, 20));
				// sub21First.add(labelPercPI);
				// sub21First.add(percPiSpinner, null);
				// percPiSpinner.setMaximumSize(new Dimension(10, 20));
				// sub21First.add(choiceBox);
				// choiceBox.setMaximumSize(new Dimension(10, 20));
				// sub21First.add(Box.createHorizontalGlue());
				// JPanel sub21Second = new JPanel();
				// sub21Second.setLayout(new BoxLayout(sub21Second,
				// BoxLayout.LINE_AXIS));
				// sub21Second.setMaximumSize(new Dimension(1000, 25));
				// sub21Second.add(Box.createHorizontalGlue());
				// sub21Second.add(jButtonInvert);
				// sub21Second.add(Box.createHorizontalGlue());
				//
				// sub21.add(sub21First);
				// sub21.add(sub21Second);

				sub21.add(labelPercTask);
				sub21.add(percTaskSpinner, null);
				sub21.add(jButtonCalculate);
				sub21.add(labelPercPI);
				sub21.add(percPiSpinner, null);
				sub21.add(choiceBox);
				// add the invert button
				sub21.add(new JLabel(" "));
				sub21.add(new JLabel(" "));
				sub21.add(jButtonInvert);
				sub21.add(new JLabel(" "));
				sub21.add(new JLabel(" "));
				sub21.add(new JLabel(" "));
				sub21.setMaximumSize(sub21.getPreferredSize());
				sub2.add(sub21, java.awt.BorderLayout.CENTER);
				sub2.add(textPanel, java.awt.BorderLayout.SOUTH);
				textPanel
						.add(
								new JLabel(
										"The Calculate button needs to be clicked to calculate which tasks need to be selected!!!"),
								java.awt.BorderLayout.CENTER);
				textPanel
						.add(
								new JLabel(
										"Clicking the OK button only accepts, but nothing is again calculated!!!!"),
								java.awt.BorderLayout.SOUTH);
				util.makeCompactGrid(sub21, 2, 6, 3, 3, 8, 3);
				//

				// specify button action for the button ButtonPreview
				jButtonCalculate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// The preview button is clicked
						buttonClicked();
						// end for
					}
				});

				// specify button action for the button Invert
				jButtonInvert.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						invertButtonClicked();
					}
				});

				return global;

			}

			/**
			 * When the preview button is clicked
			 */
			public void buttonClicked() {
				for (int k = 0; k < checks.length; k++) {
					boolean firstCheck = false;
					boolean secondCheck = false;
					LogEventCheckBoxEnh c = checks[k];
					// check for the task in c whether its percentage is higher
					// than
					// perc
					firstCheck = checkTask(c, percTaskSpinner.getValue());

					// Also check whether the task occurs in more than percTr
					// percent of the traces
					secondCheck = checkPI(c, percPiSpinner.getValue());

					// Check whether for choiceBox OR or AND is selected
					boolean logicalResult = true;
					if (((String) choiceBox.getSelectedItem()).equals("AND")) {
						logicalResult = firstCheck && secondCheck;
					} else if (((String) choiceBox.getSelectedItem())
							.equals("OR")) {
						logicalResult = firstCheck || secondCheck;
					}
					// set the checkbox selected or not
					if (logicalResult == true) {
						c.setSelected(true);
					} else {
						c.setSelected(false);
					}
				}
				// add messages to the test log for this case
				int numberCheckedBoxes = 0;
				for (int i = 0; i < checks.length; i++) {
					if (checks[i].isSelected()) {
						numberCheckedBoxes++;
					}
				}
				Message.add("number of selected tasks: " + numberCheckedBoxes,
						Message.TEST);
				Message.add("<EnhEvtLogFilter/>", Message.TEST);
			}

			/**
			 *
			 */
			public void invertButtonClicked() {
				for (int i = 0; i < checks.length; i++) {
					checks[i].setSelected(!checks[i].isSelected());
				}
			}

			/**
			 * Checks whether the task in c occurs with a lower percentage in
			 * the log than the percentage given by percTask.
			 * 
			 * @param c
			 *            LogEventCheckBoxEnh the checkbox that contains the
			 *            task.
			 * @param percTask
			 *            Object the percentage
			 * @return boolean True if the percentage of which the task in c
			 *         occurs in the log is greater or equal than percTaks,
			 *         false otherwise.
			 */
			private boolean checkTask(LogEventCheckBoxEnh c, Object percTask) {
				boolean returnBoolean = false;
				double percT = 0.0;
				percT = getDoubleValueFromSpinner(percTask);
				// check whether its percentage is higher than percT
				if (c.getPercentageTask() >= percT) {
					returnBoolean = true;
				} else {
					returnBoolean = false;
				}

				return returnBoolean;
			}

			/**
			 * Checks whether the task in c occurs with a lower percentage in
			 * different process instances than the percentage given by
			 * percTrace.
			 * 
			 * @param c
			 *            LogEventCheckBoxEnh the checkbox that contains the
			 *            task.
			 * @param percTrace
			 *            Object the percentage.
			 * @return boolean True, if the percentage of which the task in
			 *         different process instances occurs in the log is greater
			 *         or equal than percTrace, false otherwise.
			 */
			private boolean checkPI(LogEventCheckBoxEnh c, Object percPIobj) {
				boolean returnBoolean = false;
				double percPI = 0.0;

				percPI = getDoubleValueFromSpinner(percPIobj);
				// check whether its percentage is higher than percPI
				if (c.getPercentagePI() >= percPI) {
					returnBoolean = true;
				} else {
					returnBoolean = false;
				}

				return returnBoolean;
			}

			/**
			 * Get the percentage of that this task occurs in different PIs
			 * 
			 * @param evt
			 *            LogEvent the logEvent in which the task can be found
			 * @return double the percentage of which this task in the log
			 *         occurs
			 */
			private double getPercentagePI(LogEvent evt) {
				double returnPercent = 0.0;
				HashMap mapping = ((ExtendedLogSummary) summary)
						.getMappingAtesToNumberPIs();
				int numberPI = summary.getNumberOfProcessInstances();

				// Get the frequency of PI in which the task occurs
				Object value = null;
				Iterator it = mapping.keySet().iterator();
				while (it.hasNext()) {
					Object keyObj = it.next();
					String key = (String) keyObj;
					if (key.equals(evt.getModelElementName().trim() + " " + "("
							+ evt.getEventType().trim() + ")")) {
						value = mapping.get(keyObj);
						break;
					}
				}

				if (value != null) {
					// calculate frequency
					returnPercent = (((Integer) value).doubleValue() / new Double(
							numberPI).doubleValue()) * 100;
				}

				return returnPercent;
			}

			private int getFrequencyTasks(LogEvent evt, Set instances) {
				int returnFrequency = 0;
				Iterator instIterator = instances.iterator();
				while (instIterator.hasNext()) {
					ProcessInstance pi = (ProcessInstance) instIterator.next();
					Iterator ates = pi.getAuditTrailEntryList().iterator();
					while (ates.hasNext()) {
						AuditTrailEntry ate = (AuditTrailEntry) ates.next();
						if (ate.getElement().trim().equals(
								evt.getModelElementName().trim())
								&& ate.getType().equals(evt.getEventType())) {
							returnFrequency += MethodsForWorkflowLogDataStructures
									.getNumberSimilarProcessInstances(pi);
						}
					}
				}
				return returnFrequency;
			}

			/**
			 * Gets the double value of an object, provided that value is a
			 * Double or Long object
			 * 
			 * @param value
			 *            Object
			 * @return double the double value
			 */
			private double getDoubleValueFromSpinner(Object value) {
				double returnDouble = 0.0;

				if (value instanceof Long) {
					returnDouble = (((Long) value).doubleValue());
				} else if (value instanceof Double) {
					returnDouble = (((Double) value).doubleValue());
				}

				return returnDouble;
			}

			/**
			 * Returns the number of process instances, taking into account the
			 * number of similar instances
			 * 
			 * @param summary
			 *            LogSummary the log summary
			 * @return int the number of process instances
			 */
			private int calculateSumPIs(LogSummary summary) {
				int returnSum = 0;
				HashSet pis = new HashSet<ProcessInstance>();
				Iterator it = summary.getLogEvents().iterator();
				while (it.hasNext()) {
					LogEvent evt = (LogEvent) it.next();
					pis.addAll(summary.getInstancesForEvent(evt));
				}
				// for each process instance in pis, get the number of similar
				// instances
				Iterator it2 = pis.iterator();
				while (it2.hasNext()) {
					ProcessInstance pi = (ProcessInstance) it2.next();
					returnSum += MethodsForWorkflowLogDataStructures
							.getNumberSimilarProcessInstances(pi);
				}
				return returnSum;
			}

			protected boolean getAllParametersSet() {
				// calculate values
				// buttonClicked();
				return true;
			}

		};
	}

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		output.write("<percentageTask>" + this.percentageTask
				+ "</percentageTask>\n");
		output
				.write("<percentagePI>" + this.percentagePI
						+ "</percentagePI>\n");
		output.write("<selectedItemComboBox>" + this.selectedItemComboBox
				+ "</selectedItemComboBox>\n");
		super.writeSpecificXML(output);
	}

	/**
	 * Read the inside of the <FilterSpecific> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		eventsToKeep = new LogEvents();
		for (int i = 0; i < logFilterSpecifcNode.getChildNodes().getLength(); i++) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);
			if (n.getNodeName().equals("logEvent")) {
				String name = n.getAttributes().getNamedItem("name")
						.getNodeValue();
				String type = n.getAttributes().getNamedItem("type")
						.getNodeValue();
				int occ = Integer.parseInt(n.getAttributes().getNamedItem(
						"numsim").getNodeValue());
				eventsToKeep.add(new LogEvent(name, type, occ));
			} else if (n.getNodeName().equals("percentageTask")) {
				percentageTask = Double.parseDouble(n.getFirstChild()
						.getNodeValue());
			} else if (n.getNodeName().equals("percentagePI")) {
				percentagePI = Double.parseDouble(n.getFirstChild()
						.getNodeValue());
			} else if (n.getNodeName().equals("selectedItemComboBox")) {
				selectedItemComboBox = n.getFirstChild().getNodeValue();
			}
		}
	}

	/**
	 * A standard CheckBox for which the text is according to a fixed format.
	 * 
	 * @author Ronny Mans
	 * @version 1.0
	 */
	public class LogEventCheckBoxEnh extends JCheckBox implements Comparable {

		private LogEvent le;

		private double percTask;
		private double percPI;

		public LogEventCheckBoxEnh(LogEvent le) {
			super("<html><B>" + le.getModelElementName() + " ("
					+ le.getEventType() + ") " + " </B></html>", true);
			this.le = le;
		}

		public LogEvent getLogEvent() {
			return le;
		}

		public void setPercentageTask(double percentage) {
			this.percTask = percentage;
		}

		public double getPercentageTask() {
			return this.percTask;
		}

		public void setPercentagePI(double percentage) {
			this.percPI = percentage;
		}

		public double getPercentagePI() {
			return this.percPI;
		}

		public int compareTo(Object o) {
			return getText().compareTo(((LogEventCheckBoxEnh) o).getText());
		}

	}

	/**
	 * A 1.4 file that provides utility methods for creating form- or grid-style
	 * layouts with SpringLayout. These utilities are used by several programs,
	 * such as SpringBox and SpringCompactGrid.
	 */
	public class SpringUtilities {
		/**
		 * A debugging utility that prints to stdout the component's minimum,
		 * preferred, and maximum sizes.
		 */
		public void printSizes(Component c) {
			System.out.println("minimumSize = " + c.getMinimumSize());
			System.out.println("preferredSize = " + c.getPreferredSize());
			System.out.println("maximumSize = " + c.getMaximumSize());
		}

		/**
		 * Aligns the first <code>rows</code> * <code>cols</code> components of
		 * <code>parent</code> in a grid. Each component is as big as the
		 * maximum preferred width and height of the components. The parent is
		 * made just big enough to fit them all.
		 * 
		 * @param rows
		 *            number of rows
		 * @param cols
		 *            number of columns
		 * @param initialX
		 *            x location to start the grid at
		 * @param initialY
		 *            y location to start the grid at
		 * @param xPad
		 *            x padding between cells
		 * @param yPad
		 *            y padding between cells
		 */
		public void makeGrid(Container parent, int rows, int cols,
				int initialX, int initialY, int xPad, int yPad) {
			SpringLayout layout;
			try {
				layout = (SpringLayout) parent.getLayout();
			} catch (ClassCastException exc) {
				System.err
						.println("The first argument to makeGrid must use SpringLayout.");
				return;
			}

			Spring xPadSpring = Spring.constant(xPad);
			Spring yPadSpring = Spring.constant(yPad);
			Spring initialXSpring = Spring.constant(initialX);
			Spring initialYSpring = Spring.constant(initialY);
			int max = rows * cols;

			// Calculate Springs that are the max of the width/height so that
			// all
			// cells have the same size.
			Spring maxWidthSpring = layout.getConstraints(
					parent.getComponent(0)).getWidth();
			Spring maxHeightSpring = layout.getConstraints(
					parent.getComponent(0)).getWidth();
			for (int i = 1; i < max; i++) {
				SpringLayout.Constraints cons = layout.getConstraints(parent
						.getComponent(i));

				maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
				maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
			}

			// Apply the new width/height Spring. This forces all the
			// components to have the same size.
			for (int i = 0; i < max; i++) {
				SpringLayout.Constraints cons = layout.getConstraints(parent
						.getComponent(i));

				cons.setWidth(maxWidthSpring);
				cons.setHeight(maxHeightSpring);
			}

			// Then adjust the x/y constraints of all the cells so that they
			// are aligned in a grid.
			SpringLayout.Constraints lastCons = null;
			SpringLayout.Constraints lastRowCons = null;
			for (int i = 0; i < max; i++) {
				SpringLayout.Constraints cons = layout.getConstraints(parent
						.getComponent(i));
				if (i % cols == 0) { // start of new row
					lastRowCons = lastCons;
					cons.setX(initialXSpring);
				} else { // x position depends on previous component
					cons.setX(Spring.sum(lastCons
							.getConstraint(SpringLayout.EAST), xPadSpring));
				}

				if (i / cols == 0) { // first row
					cons.setY(initialYSpring);
				} else { // y position depends on previous row
					cons.setY(Spring.sum(lastRowCons
							.getConstraint(SpringLayout.SOUTH), yPadSpring));
				}
				lastCons = cons;
			}

			// Set the parent's size.
			SpringLayout.Constraints pCons = layout.getConstraints(parent);
			pCons.setConstraint(SpringLayout.SOUTH, Spring
					.sum(Spring.constant(yPad), lastCons
							.getConstraint(SpringLayout.SOUTH)));
			pCons
					.setConstraint(SpringLayout.EAST, Spring.sum(Spring
							.constant(xPad), lastCons
							.getConstraint(SpringLayout.EAST)));
		}

		/* Used by makeCompactGrid. */
		private SpringLayout.Constraints getConstraintsForCell(int row,
				int col, Container parent, int cols) {
			SpringLayout layout = (SpringLayout) parent.getLayout();
			Component c = parent.getComponent(row * cols + col);
			return layout.getConstraints(c);
		}

		/**
		 * Aligns the first <code>rows</code> * <code>cols</code> components of
		 * <code>parent</code> in a grid. Each component in a column is as wide
		 * as the maximum preferred width of the components in that column;
		 * height is similarly determined for each row. The parent is made just
		 * big enough to fit them all.
		 * 
		 * @param rows
		 *            number of rows
		 * @param cols
		 *            number of columns
		 * @param initialX
		 *            x location to start the grid at
		 * @param initialY
		 *            y location to start the grid at
		 * @param xPad
		 *            x padding between cells
		 * @param yPad
		 *            y padding between cells
		 */
		public void makeCompactGrid(Container parent, int rows, int cols,
				int initialX, int initialY, int xPad, int yPad) {
			SpringLayout layout;
			try {
				layout = (SpringLayout) parent.getLayout();
			} catch (ClassCastException exc) {
				System.err
						.println("The first argument to makeCompactGrid must use SpringLayout.");
				return;
			}

			// Align all cells in each column and make them the same width.
			Spring x = Spring.constant(initialX);
			for (int c = 0; c < cols; c++) {
				Spring width = Spring.constant(0);
				for (int r = 0; r < rows; r++) {
					width = Spring.max(width, getConstraintsForCell(r, c,
							parent, cols).getWidth());
				}
				for (int r = 0; r < rows; r++) {
					SpringLayout.Constraints constraints = getConstraintsForCell(
							r, c, parent, cols);
					constraints.setX(x);
					constraints.setWidth(width);
				}
				x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
			}

			// Align all cells in each row and make them the same height.
			Spring y = Spring.constant(initialY);
			for (int r = 0; r < rows; r++) {
				Spring height = Spring.constant(0);
				for (int c = 0; c < cols; c++) {
					height = Spring.max(height, getConstraintsForCell(r, c,
							parent, cols).getHeight());
				}
				for (int c = 0; c < cols; c++) {
					SpringLayout.Constraints constraints = getConstraintsForCell(
							r, c, parent, cols);
					constraints.setY(y);
					constraints.setHeight(height);
				}
				y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
			}

			// Set the parent's size.
			SpringLayout.Constraints pCons = layout.getConstraints(parent);
			pCons.setConstraint(SpringLayout.SOUTH, y);
			pCons.setConstraint(SpringLayout.EAST, x);
		}
	}

}
