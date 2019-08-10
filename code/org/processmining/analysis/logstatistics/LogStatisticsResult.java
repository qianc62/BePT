package org.processmining.analysis.logstatistics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.analysis.hierarchicaldatavisualization.HierarchicalData;
import org.processmining.analysis.hierarchicaldatavisualization.logstatistics.LogStatisticsToHierarchicalDataSet;
import org.processmining.analysis.hierarchicaldatavisualization.logstatistics.ProcessingTimeFactory;
import org.processmining.analysis.hierarchicaldatavisualization.logstatistics.ThroughputTimeFactory;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.activitygraph.ActivityGraph;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.att.HLAttributeManager;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution;
import org.processmining.framework.models.hlprocess.hlmodel.HLActivitySet;
import org.processmining.framework.models.hlprocess.view.HLAttributeView;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GenericTableModelPanel;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiPropertyStringTextarea;

public class LogStatisticsResult extends JPanel implements Provider {

	private static final long serialVersionUID = -5007185682032530324L;
	/**
	 * The log reader providing access to the log.
	 */
	private LogReader myLogReader;
	/**
	 * Keeps the statistics for each process instance. [key = name of the
	 * process instance, value = LogStatistic object]
	 */
	private HashMap<String, LogStatistic> myInstanceStatistics;
	/**
	 * The insance IDs in a string array (to be sorted and displayed in table)
	 */
	private ArrayList<String> myInstanceIDs;
	/**
	 * Keeps the statistic for the whole log in an aggregate fashion.
	 */
	private LogStatistic myGlobalStatistic;
	/**
	 * Keeps simmple statistics about the times between the arrival of two new
	 * cases.
	 */
	private ArrayList<Date> myArrivalDates = new ArrayList<Date>();
	/**
	 * Holds all the display options like viewing model (textual or graphical)
	 * and time units for each of the result values.
	 */
	private OptionPanel myDisplaySettings = new OptionPanel();
	/**
	 * The activity statistics will be provided as an activity-based simulation
	 * model (i.e., without an underlying process model).
	 */
	private HLActivitySet actSet = new HLActivitySet(new ActivityGraph());
	/**
	 * If this plugin is used multiple times, each time the simulation model to
	 * be provided will have an incremented number (in order to distinguish them
	 * later when they e.g., need to be joined)
	 */
	private static int simulationModelCounter = 0;

	// user interface related attributes
	private JTabbedPane tabPane = new JTabbedPane();
	private JPanel myGlobalContentPanel = new JPanel(new BorderLayout());
	private JPanel myInstanceContentPanel = new JPanel(new BorderLayout());
	/**
	 * @todo anne: remove this workaround - invisible button for invoking the
	 *       double click table action
	 */
	private JButton updateViewButton = new JButton();
	private JButton applyChangesButton = new JButton("Apply Changes");
	private DoubleClickTable instanceNameTable;
	protected JPanel attributesPanel = new JPanel();

	/**
	 * Constructor. Calculate and displays basic log statistics under the
	 * assumption that (1) each activity is representec by both a Start and an
	 * End event, (2) each Start event is immediately followed by a
	 * corresponding End event, and (3) both events have a timestamp associated.
	 * 
	 * @param log
	 *            the log to be analyzed
	 */
	public LogStatisticsResult(LogReader log) {
		this(log, true);

		// fill the activity set
		generateActivitySet();
		// write test log - TODO: only do if test flag enabled
		writeTestLog();
		// display results
		jbInit();
		registerGuiActionListener();
	}

	private LogStatisticsResult(LogReader log, boolean computeStatisticsOnly) {
		myInstanceStatistics = new HashMap<String, LogStatistic>();
		myGlobalStatistic = new LogStatistic("the whole log", null,
				myDisplaySettings);
		myInstanceIDs = new ArrayList<String>();
		myLogReader = log;
		calculate();
	}

	public static HierarchicalData getProcessingTimes(LogReader log) {
		LogStatisticsResult stats = new LogStatisticsResult(log, true);
		return new LogStatisticsToHierarchicalDataSet(
				stats.myInstanceStatistics, log, new ProcessingTimeFactory());
	}

	public static HierarchicalData getThroughputTimes(LogReader log) {
		LogStatisticsResult stats = new LogStatisticsResult(log, true);
		return new LogStatisticsToHierarchicalDataSet(
				stats.myInstanceStatistics, log, new ThroughputTimeFactory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = {
				new ProvidedObject("Processing times",
						new Object[] { new LogStatisticsToHierarchicalDataSet(
								myInstanceStatistics, myLogReader,
								new ProcessingTimeFactory()) }),
				new ProvidedObject("Throughput times",
						new Object[] { new LogStatisticsToHierarchicalDataSet(
								myInstanceStatistics, myLogReader,
								new ThroughputTimeFactory()) }),
				new ProvidedObject(
						"Simulation Model with Log Analysis results No."
								+ simulationModelCounter,
						new Object[] { actSet }),
				new ProvidedObject("Whole Log", new Object[] { myLogReader }) };
		return objects;
	}

	/**
	 * Fills the exported simulation models with values obtained from the
	 * calculation.
	 */
	private void generateActivitySet() {
		// increment static sim model counter for this plugin
		simulationModelCounter = simulationModelCounter + 1;

		actSet.getHLProcess().getGlobalInfo().setName(
				"Simulation Model with Log Analysis results No."
						+ simulationModelCounter);
		// TODO : do not hardcode the time unit but respect user configuration
		actSet.getHLProcess().getGlobalInfo().setTimeUnit(
				HLTypes.TimeUnit.SECONDS);
		actSet.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.TIMING_EXECTIME);
		actSet.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.CASE_GEN_SCHEME);
		actSet.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.DATA_VALUE_RANGE);

		// add actual activity values
		HashMap<String, SummaryStatistics> globalDurations = myGlobalStatistic
				.getActivityDurations();

		Iterator<String> it = globalDurations.keySet().iterator();
		String actName = null;
		SummaryStatistics actStatistic = null;
		HLDistribution dist = null;
		HLActivity act = null;
		while (it.hasNext()) {
			actName = it.next();
			actStatistic = globalDurations.get(actName);
			act = new HLActivity(actName, actSet.getHLProcess());
			dist = LogStatistic.getDistributionObject(actStatistic);
			act.setExecutionTime(dist);
			actSet.addActivity(act);
		}

		// add case generation scheme
		SummaryStatistics arrivalStatistic = SummaryStatistics.newInstance();
		Date[] arrivals = (Date[]) myArrivalDates.toArray(new Date[0]);
		// make sure arrival dates are sorted
		Arrays.sort(arrivals);
		if (arrivals.length > 1) {
			for (int i = 1; i < arrivals.length; i++) {
				long iat = arrivals[i].getTime() - arrivals[i - 1].getTime();
				if (iat >= 0) {
					// normalize value to seconds
					iat = iat / 1000;
					arrivalStatistic.addValue(iat);
				}
			}
		}
		HLDistribution caseArrivals = LogStatistic
				.getDistributionObject(arrivalStatistic);
		actSet.getHLProcess().getGlobalInfo().setCaseGenerationScheme(
				caseArrivals);
	}

	/**
	 * Calculate basic log statistics.
	 */
	private void calculate() {
		// walk through the log
		Iterator it = myLogReader.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();
			myInstanceIDs.add(pi.getName());

			// evaluate attributes
			for (String attName : pi.getAttributes().keySet()) {
				HLAttribute att = actSet.getHLProcess().findAttributeByName(
						attName);
				if (att == null) {
					att = new HLNominalAttribute(attName, actSet.getHLProcess());
				}
				((HLNominalAttribute) att).addPossibleValue(pi.getAttributes()
						.get(attName));
			}

			// init reference data structures
			HashMap<String, Date> durationReferenceList = new HashMap<String, Date>();
			ArrayList<Date> distanceReferenceList = new ArrayList<Date>();
			// create statistics object for current process instance
			LogStatistic statistic = new LogStatistic(pi.getName(), pi
					.getModelReferences(), myDisplaySettings);
			statistic
					.initialize(myLogReader.getLogSummary().getModelElements());

			// keep flag to know whether ate is the first one in instance
			boolean isFirstEntry = true;

			// walk through the instance
			Iterator ates = pi.getAuditTrailEntryList().iterator();
			while (ates.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ates.next();
				Date timeStamp = ate.getTimestamp();

				// evaluate attributes
				for (String attName : ate.getAttributes().keySet()) {
					HLAttribute att = actSet.getHLProcess()
							.findAttributeByName(attName);
					if (att == null) {
						att = new HLNominalAttribute(attName, actSet
								.getHLProcess());
					}
					((HLNominalAttribute) att).addPossibleValue(ate
							.getAttributes().get(attName));
				}

				// keep only track of the first date per process instance for
				// arrival times
				if (isFirstEntry == true && timeStamp != null) {
					myArrivalDates.add(timeStamp);
					isFirstEntry = false;
				}

				// do nothing if no timestamp is available
				if (timeStamp != null) {
					long current = timeStamp.getTime();
					myGlobalStatistic.addTimestampValue(current);
					statistic.addTimestampValue(current);
					if (ate.getType().equals("start")) {
						// keep value for the next duration calculation
						durationReferenceList.put(ate.getElement(), timeStamp);
						// calculate difference
						if (distanceReferenceList.size() != 0) {
							// use stored references to calculate a difference
							// value each
							Iterator allRefs = distanceReferenceList.iterator();
							while (allRefs.hasNext()) {
								Date distanceReference = (Date) allRefs.next();
								long reference = distanceReference.getTime();
								long distance = current - reference;
								// normalize value to seconds
								distance = distance / 1000;
								// collect measurement for current instance ..
								statistic.addActivityDistanceValue(distance);
								// .. and globally
								myGlobalStatistic
										.addActivityDistanceValue(distance);
							}
							// clean the references as they have been processed
							// already
							distanceReferenceList = new ArrayList<Date>();
						}
					} else if (ate.getType().equals("complete")) {
						// keep value for the next distance calculation
						// distanceReference = timeStamp;
						distanceReferenceList.add(timeStamp);
						// calculate duration
						Date durationReference = (Date) durationReferenceList
								.get(ate.getElement());
						if (durationReference != null) {
							long reference = durationReference.getTime();
							long duration = current - reference;
							// normalize value to seconds
							duration = duration / 1000;
							// collect measurement for current instance ..
							statistic.addActivityDurationValue(
									ate.getElement(), ate
											.getElementModelReferences(), ate
											.getOriginatorModelReferences(),
									duration);
							// .. and globally
							myGlobalStatistic.addActivityDurationValue(ate
									.getElement(), ate
									.getElementModelReferences(), ate
									.getOriginatorModelReferences(), duration);
						}
					}
				}
			}
			// create statistics entry for current instance
			myInstanceStatistics.put(pi.getName(), statistic);
		}
	}

	/**
	 * Writes test logs to the Message.TEST output in order to make test cases
	 * for this plugin.
	 */
	private void writeTestLog() {
		// // PLUGIN TEST START
		Message.add("<BasicLogStatistics>", Message.TEST);
		// TODO: also provide more detailed test logs as soon as the presence of
		// the test flag
		// is checked before actually writing the test logs
		Date earliest = new Date((long) myGlobalStatistic
				.getTimeStampStatistics().getMin());
		Date latest = new Date((long) myGlobalStatistic
				.getTimeStampStatistics().getMax());
		Message.add("Earliest Timestamp: " + earliest.toString(), Message.TEST);
		Message.add("Latest Timestamp: " + latest.toString(), Message.TEST);
		Message.add("No. of measured Timestamps: "
				+ myGlobalStatistic.getTimeStampStatistics().getN(),
				Message.TEST);
		Message.add("</BasicLogStatistics>", Message.TEST);
		// PLUGIN TEST END
	}

	/**
	 * Display basic log statistics.
	 */
	private void jbInit() {

		instanceNameTable = new DoubleClickTable(new ProcessInstanceTable(),
				updateViewButton);
		instanceNameTable.getSelectionModel().addSelectionInterval(0, 0);

		// options panel
		JPanel optionsPanel = new JPanel();
		optionsPanel
				.setLayout(new BoxLayout(optionsPanel, BoxLayout.LINE_AXIS));
		optionsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		optionsPanel.add(myDisplaySettings.getPanel());
		optionsPanel.add(Box.createHorizontalGlue());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
		buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(applyChangesButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		optionsPanel.add(buttonPanel);
		optionsPanel.add(Box.createRigidArea(new Dimension(20, 0)));

		// global statistics
		JPanel globalViewPanel = new JPanel();
		globalViewPanel.setLayout(new BoxLayout(globalViewPanel,
				BoxLayout.PAGE_AXIS));
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				"The following options allow you to specify the "
						+ "display settings for the basic statistics that have been calculated. As soon as you press the button 'Apply Changes' "
						+ "the current settings will be applied to both the global and instance-wise statistics view.");
		globalViewPanel.add(helpText.getPropertyPanel());
		globalViewPanel.add(optionsPanel);
		myGlobalContentPanel.add(myGlobalStatistic
				.getResultPanel(myDisplaySettings.getCurrentViewMode()));
		globalViewPanel.add(myGlobalContentPanel);

		// instance-wise view
		LogStatistic statistic = (LogStatistic) myInstanceStatistics
				.get(myInstanceIDs.get(0));
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		leftPanel.add(new JScrollPane(instanceNameTable));
		leftPanel.add(Box.createVerticalGlue());
		JPanel rightPanel = new JPanel(new BorderLayout());
		myInstanceContentPanel.add(statistic.getResultPanel(myDisplaySettings
				.getCurrentViewMode()));
		rightPanel.add(myInstanceContentPanel);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, rightPanel);
		splitPane.setDividerLocation(150);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(3);
		JPanel instanceViewPanel = new JPanel(new BorderLayout());
		instanceViewPanel.add(splitPane, BorderLayout.CENTER);

		// attribute view
		attributesPanel.setLayout(new BoxLayout(attributesPanel,
				BoxLayout.PAGE_AXIS));
		attributesPanel.add(getDataAttributesPanel());

		// build tab views
		tabPane.addTab("Global Activity Statistics", null, globalViewPanel,
				"View basic statistics for the whole log");
		tabPane.addTab("Instance-wise Activity Statistics", null,
				instanceViewPanel,
				"View basic statistics for each process instance");
		tabPane.addTab("Attribute Statistics", null, attributesPanel,
				"View occurrence statistics for each data attribute");

		this.setLayout(new BorderLayout());
		this.add(tabPane, BorderLayout.CENTER);
	}

	/**
	 * Creates a GUI panel containing a list of all those attributes that are
	 * contained in this simulation model. <br>
	 * Note that the panel can be readily displayed and modifications to the
	 * displayed attributes are propagated to the internally maintained values.
	 * 
	 * @return the GUI panel containing the data attributes available
	 */
	public JPanel getDataAttributesPanel() {
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		resultPanel.add(Box.createVerticalGlue());
		List<GuiDisplayable> dataAttrHL = new ArrayList<GuiDisplayable>();
		for (HLAttribute attrib : actSet.getHLProcess().getAttributes()) {
			attrib = HLAttributeManager
					.autoChangeType((HLNominalAttribute) attrib);
			HLAttributeView attGui = new HLAttributeView(attrib);
			dataAttrHL.add(attGui);
		}
		resultPanel
				.add(new GenericTableModelPanel(
						dataAttrHL,
						"Attributes",
						"Here you can view occurrence statistics for data values for each attribute in the log.",
						null));
		resultPanel.add(Box.createVerticalGlue());
		return resultPanel;
	}

	/**
	 * Connects GUI elements like, e.g., buttons with functionality to create
	 * interaction.
	 */
	private void registerGuiActionListener() {
		updateViewButton.addActionListener(new ActionListener() {
			// specifies the action to be taken when a process instance has been
			// double-clicked
			public void actionPerformed(ActionEvent e) {
				updateInstancePanel();
			}
		});
		applyChangesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGlobalPanel();
				updateInstancePanel();
			}
		});
	}

	/**
	 * Redraws the contents of the currently viewed instance panel according to
	 * the current display settings.
	 */
	private void updateInstancePanel() {
		// determine current selection (only one can be selected as it has been
		// doubleclicked)
		int[] indexArray = instanceNameTable.getSelectedRows();
		// retrieve new statistics get
		LogStatistic statistic = (LogStatistic) myInstanceStatistics
				.get(myInstanceIDs.get(indexArray[0]));
		myInstanceContentPanel.removeAll();
		myInstanceContentPanel.add(statistic.getResultPanel(myDisplaySettings
				.getCurrentViewMode()));
		myInstanceContentPanel.validate();
		myInstanceContentPanel.repaint();
	}

	/**
	 * Redraws the contents of the global panel according to the current display
	 * settings.
	 */
	private void updateGlobalPanel() {
		myGlobalContentPanel.removeAll();
		myGlobalContentPanel.add(myGlobalStatistic
				.getResultPanel(myDisplaySettings.getCurrentViewMode()));
		myGlobalContentPanel.validate();
		myGlobalContentPanel.repaint();
	}

	/**
	 * Private data structure for the table containing the process instance
	 * names.
	 */
	private class ProcessInstanceTable extends AbstractTableModel {

		/**
		 * Required for a serializable class (generated quickfix). Not directly
		 * used.
		 */
		private static final long serialVersionUID = -6165029231436957878L;

		/**
		 * Specify the headings for the columns.
		 * 
		 * @param col
		 *            The column specified.
		 * @return The heading of the respective column.
		 */
		public String getColumnName(int col) {
			// heading of single column
			return "Process Instances";
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of traces in the log.
		 */
		public int getRowCount() {
			return myInstanceIDs.size();
		}

		/**
		 * Specifiy the number of columns.
		 * 
		 * @return Always 1.
		 */
		public int getColumnCount() {
			return 1;
		}

		/**
		 * Method to fill a certain field of the table with contents.
		 * 
		 * @param row
		 *            The specified row.
		 * @param column
		 *            The specified column.
		 * @return The content to display at the table field specified.
		 */
		public Object getValueAt(int row, int column) {
			// fill in name of the process instance
			return myInstanceIDs.get(row);
		}
	}

}
