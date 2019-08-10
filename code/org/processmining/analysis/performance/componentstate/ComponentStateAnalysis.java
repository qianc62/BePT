package org.processmining.analysis.performance.componentstate;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.processmining.analysis.*;
import org.processmining.framework.log.*;
import org.processmining.framework.ui.*;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.plugin.ProvidedObject;

//import org.processmining.analysis.performance.dottedchart.DottedChartPlugin;
//import org.processmining.analysis.performance.dottedchart.ui.DottedChartPanel;
import org.processmining.analysis.performance.dottedchart.ui.ColorReference; //import org.processmining.analysis.performance.dottedchart.ui.OverviewPanel;
//import org.processmining.analysis.performance.dottedchart.model.*;
import org.processmining.analysis.performance.dottedchart.ui.MetricsPanel; //import org.processmining.analysis.performance.dottedchart.ui.SettingPanel;

import org.processmining.analysis.performance.componentstate.ComponentStatePlugin;
import org.processmining.analysis.performance.componentstate.ui.ComponentStatePanel;
import org.processmining.analysis.performance.componentstate.ui.SettingPanel;
import org.processmining.analysis.performance.componentstate.ui.OverviewPanel;
import org.processmining.analysis.performance.componentstate.model.*;

/*
 * @author 
 */

public class ComponentStateAnalysis extends JPanel implements Provider {

	private ComponentStateModel csModel; // = new ComponentStateModel();

	// final attributes
	final ComponentStatePlugin myAlgorithm;
	final AnalysisInputItem[] myInput;
	final LogReader inputLog;

	public static final String STR_NONE = "None";
	public static final String ST_ORIG = "Originator";
	public static final String ST_TASK = "Task ID";
	public static final String ST_INST = "Instance ID";
	public static final String ST_EVEN = "Event";
	public static final String ST_DATA = "Data";

	private int[] selectedInstanceIndices; // indices of the process instances
	// used

	// data for options
	private long widthDivider = 2592000000L;
	protected LogEvents eventsToKeep;
	protected ArrayList eventTypeToKeep;
	protected DoubleClickTable processInstanceIDsTable;
	private ArrayList instanceIDs = new ArrayList();
	private ArrayList selectedIDs = new ArrayList();

	// GUI attributes
	// panel
	private JPanel westPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private SettingPanel settingPanel;
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JSplitPane sp = new JSplitPane();
	private JSplitPane split = new JSplitPane();
	private JSplitPane spom = new JSplitPane();

	// /////////////// make a componentStatePanel.......

	private ComponentStatePanel componentStatePanel = null;
	private OverviewPanel ovPanel = null;
	private JScrollPane chartPane;
	private JScrollPane metricsPane;
	private MetricsPanel metricsPanel;

	public ComponentStateAnalysis(ComponentStatePlugin algorithm,
			AnalysisInputItem[] input, LogReader log) {
		myAlgorithm = algorithm;
		myInput = input;
		inputLog = log;
		csModel = new ComponentStateModel(inputLog);
		int number = inputLog.getLogSummary().getNumberOfProcessInstances();
		// initially, all instances are selected
		selectedInstanceIndices = new int[number];
		for (int i = 0; i < number; i++) {
			selectedInstanceIndices[i] = i;
		}

		analyse();
	}

	/**
	 * Creates the thread in which the log relations are mined out from the used
	 * log
	 */
	public void analyse() {
		initOptions(); // initialize eventTypeToKeep and InstanceIDs
		jbInit();
		settingPanel.initSettingPanel();
	}

	/**
	 * Actually builds the GUI
	 */

	public void jbInit() {

		int height = this.getHeight() - this.getInsets().bottom
				- this.getInsets().top - 50;
		int width = this.getWidth() - this.getInsets().left
				- this.getInsets().right - 200;

		// initialize
		csModel.setEventTypeToKeep(eventTypeToKeep);
		csModel.setInstanceTypeToKeep(instanceIDs);

		componentStatePanel = new ComponentStatePanel(this, csModel);

		// init west
		westPanel = componentStatePanel.getComponentStateOptionPanel();

		// initailze settings of taskMap
		chartPane = new JScrollPane(componentStatePanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		chartPane.setAutoscrolls(true);
		// repaint after moving scrollbar
		AdjustmentListener adjListener = new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				componentStatePanel.repaint();

				// we don't display the metrics
				// now...====================================================
				ovPanel.repaint();
			}
		};
		chartPane.getHorizontalScrollBar().addAdjustmentListener(adjListener);
		chartPane.getVerticalScrollBar().addAdjustmentListener(adjListener);
		chartPane.setPreferredSize(new Dimension(width - 200, height - 10));

		// ============================================================================================//

		// init overview Panel;
		ovPanel = new OverviewPanel(this);
		ovPanel.setPreferredSize(new Dimension(200, 130));

		// init MetricsPanel
		// /metricsPanel = new MetricsPanel(csModel,componentStatePanel);
		// /metricsPane = new JScrollPane(metricsPanel);
		// /metricsPane.setPreferredSize(new Dimension(200, height - 10));

		// /spom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ovPanel,
		// metricsPane);

		// /spom.setOneTouchExpandable(false);
		// /spom.setDividerLocation(131);
		// /spom.setDividerSize(3);

		/**
		 * split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartPane,
		 * metricsPane);
		 * 
		 * // makes panel split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		 * chartPane, spom); split.setDividerLocation(split.getSize().width -
		 * split.getInsets().right - split.getDividerSize() - 200);
		 * 
		 * split.setOneTouchExpandable(true); split.setResizeWeight(1.0);
		 * split.setDividerSize(3); split.setOneTouchExpandable(true);
		 * 
		 * 
		 * // Add dotted Panel and Setting Panel tabbedPane.add("Dotted Chart",
		 * split);
		 **/

		// ============================================================================
		// remove the split pane, with overview and metrics panels
		tabbedPane.add("Element State Chart", chartPane); // grx

		settingPanel = new SettingPanel(inputLog, this, componentStatePanel,
				csModel);
		settingPanel.setMinimumSize(new Dimension(width, height));
		JScrollPane settingScroll = new JScrollPane(settingPanel);
		tabbedPane.add("Settings", settingScroll);
		tabbedPane.setSelectedIndex(0);

		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(tabbedPane, BorderLayout.CENTER);
		centerPanel.setMinimumSize(new Dimension(width, height));
		centerPanel.setAlignmentX(LEFT_ALIGNMENT);
		sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPanel, centerPanel);
		sp.setDividerLocation(165);
		sp.setDividerSize(3);
		sp.setOneTouchExpandable(true);

		this.setLayout(new BorderLayout());
		this.add(sp, BorderLayout.CENTER);
		this.validate();
		this.repaint();

		// ============================================================================================//

		// inintialize performace metrics
		// metricsPanel.displayPerformanceMetrics();

	}

	public ComponentStatePanel getComponentStatePanel() {
		return componentStatePanel;
	}

	// ============================================================================
	/*
	 * public OverviewPanel getOverviewPanel() { return ovPanel; }
	 * 
	 * public MetricsPanel getMetricsPanel() { return metricsPanel; }
	 */

	public SettingPanel getSettingPanel() {
		return settingPanel;
	}

	public LogReader getLogReader() {
		return inputLog;
	}

	public int[] getSelectedInstanceIndices() {
		return selectedInstanceIndices;
	}

	public void setSelectedInstanceIndices(int[] instances) {
		selectedInstanceIndices = instances;
	}

	public ProvidedObject[] getProvidedObjects() {
		try {
			if (selectedInstanceIndices != null) {
				// fill the high level PN with simulation information
				ProvidedObject[] objects = {
						new ProvidedObject("Whole Log",
								new Object[] { inputLog }),
						new ProvidedObject("Log Selection",
								new Object[] { LogReaderFactory.createInstance(
										inputLog, selectedInstanceIndices) }) };
				return objects;
			} else {
				ProvidedObject[] objects = new ProvidedObject[] { new ProvidedObject(
						"Component State", new Object[] { inputLog }) };
				return objects;
			}
		} catch (Exception e) {
			System.err.println("Fatal error creating new log reader instance:");
			System.err.println("(" + this.getClass() + ")");
			e.printStackTrace();
			return null;
		}
	}

	// //////////////////////////
	// initialize
	public void initOptions() {
		// todo : init eventTypetoKeep
		eventTypeToKeep = new ArrayList(Arrays.asList(inputLog.getLogSummary()
				.getEventTypes()));
		ArrayList<String> atr = new ArrayList<String>();
		for (int i = 0; i < eventTypeToKeep.size(); i++)
			atr.add((String) eventTypeToKeep.get(i));
		obtainInstanceIDs();
	}

	// methods for dealing with zoom
	public Dimension getViewportSize() {
		return chartPane.getViewport().getSize();
	}

	public int getVerticalPosition() {
		return chartPane.getViewport().getViewPosition().y;
	}

	public int getHorizontalPosition() {
		return chartPane.getViewport().getViewPosition().x;
	}

	public JScrollPane getScrollPane() {
		return chartPane;
	}

	public void setScrollBarPosition(Point aP) {
		chartPane.getViewport().validate();
		if (aP.x < 0)
			aP.x = 0;
		if (aP.y < 0)
			aP.y = 0;
		if ((aP.x + chartPane.getViewport().getSize().getWidth()) > componentStatePanel
				.getWidth())
			aP.x = (int) (componentStatePanel.getWidth() - chartPane
					.getViewport().getSize().getWidth());
		if ((aP.y + chartPane.getViewport().getSize().getHeight()) > componentStatePanel
				.getHeight())
			aP.y = (int) (componentStatePanel.getHeight() - chartPane
					.getViewport().getSize().getHeight());
		chartPane.getViewport().setViewPosition(aP);
	}

	// Obtains the names of the process instances in the log and stores them in
	// the instanceIDs list
	public void obtainInstanceIDs() {
		// note that there is not just the keyset returned to keep
		// the order of the instances
		instanceIDs.clear();
		inputLog.reset();
		Iterator allTraces = inputLog.instanceIterator();
		while (allTraces.hasNext()) {
			ProcessInstance currentTrace = (ProcessInstance) allTraces.next();
			instanceIDs.add(currentTrace.getName());
			selectedIDs.add(currentTrace.getName());
		}
	}

}
