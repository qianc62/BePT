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

package org.processmining.analysis.performance.dottedchart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.performance.dottedchart.model.DottedChartModel;
import org.processmining.analysis.performance.dottedchart.ui.DottedChartPanel;
import org.processmining.analysis.performance.dottedchart.ui.MetricsPanel;
import org.processmining.analysis.performance.dottedchart.ui.OverviewPanel;
import org.processmining.analysis.performance.dottedchart.ui.SettingPanel;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;

/**
 * Dotted chart analysis
 * 
 * @author Minseok Song
 */
public class DottedChartAnalysis extends JPanel implements Provider {

	public static final String STR_NONE = "None";
	public static final String ST_ORIG = "Originator";
	public static final String ST_TASK = "Task ID";
	public static final String ST_INST = "Instance ID";
	public static final String ST_EVEN = "Event";
	public static final String ST_DATA = "Data";

	// final attributes
	final DottedChartPlugin myAlgorithm;
	final AnalysisInputItem[] myInput;
	final LogReader inputLog;
	private DottedChartModel dcModel;

	// data for options
	private ArrayList eventTypeToKeep;
	private ArrayList instanceIDs = new ArrayList();
	private ArrayList selectedIDs = new ArrayList();
	private int[] selectedInstanceIndices; // indices of the process instances
	// used
	private int[] selectedInstanceIndicesfromScreen; // indices of the process
	// instances used

	// GUI attributes
	private JPanel westPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private SettingPanel settingPanel;
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JSplitPane sp = new JSplitPane();
	private JSplitPane split = new JSplitPane();
	private JSplitPane spom = new JSplitPane();
	private DottedChartPanel dottedChartPanel = null;
	private OverviewPanel ovPanel = null;
	private JScrollPane chartPane;
	private JScrollPane metricsPane;
	private MetricsPanel metricsPanel;

	public DottedChartAnalysis(DottedChartPlugin algorithm,
			AnalysisInputItem[] input, LogReader log) {
		myAlgorithm = algorithm;
		myInput = input;
		inputLog = log;
		dcModel = new DottedChartModel(inputLog);
		int number = inputLog.getLogSummary().getNumberOfProcessInstances();
		// initially, all instances are selected
		selectedInstanceIndices = new int[number];
		for (int i = 0; i < number; i++) {
			selectedInstanceIndices[i] = i;
		}
		analyse();
	}

	/**
	 * Creates thread in which the log relations are mined out of the used log
	 */
	public void analyse() {
		initOptions(); // initialize eventTypeToKeep and InstanceIDs
		jbInit();
		settingPanel.initSettingPanel();
	}

	/**
	 * Actually builds the GUI
	 */
	private void jbInit() {

		int height = this.getHeight() - this.getInsets().bottom
				- this.getInsets().top - 50;
		int width = this.getWidth() - this.getInsets().left
				- this.getInsets().right - 200;

		// initialize
		dcModel.setEventTypeToKeep(eventTypeToKeep);
		dcModel.setInstanceTypeToKeep(instanceIDs);
		dottedChartPanel = new DottedChartPanel(this);
		dottedChartPanel.setSelectedIDs(selectedIDs);

		// init west
		westPanel = dottedChartPanel.getDottedChartOptionPanel();

		// initailze settings of taskMap
		chartPane = new JScrollPane(dottedChartPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		chartPane.setAutoscrolls(true);
		// repaint after moving scrollbar
		AdjustmentListener adjListener = new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				dottedChartPanel.repaint();
				ovPanel.repaint();
			}
		};
		chartPane.getHorizontalScrollBar().addAdjustmentListener(adjListener);
		chartPane.getVerticalScrollBar().addAdjustmentListener(adjListener);
		chartPane.setPreferredSize(new Dimension(width - 200, height - 10));

		// init overview Panel;
		ovPanel = new OverviewPanel(this);
		ovPanel.setPreferredSize(new Dimension(200, 130));

		// init MetricsPanel
		metricsPanel = new MetricsPanel(dcModel, dottedChartPanel);
		metricsPane = new JScrollPane(metricsPanel);
		metricsPane.setPreferredSize(new Dimension(200, height - 10));

		// init Panel including overview and metrics Panel
		spom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ovPanel, metricsPane);
		spom.setOneTouchExpandable(false);
		spom.setDividerLocation(131);
		spom.setDividerSize(3);

		// init main split panel
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartPane, spom);
		split.setDividerLocation(split.getSize().width
				- split.getInsets().right - split.getDividerSize() - 200);

		split.setOneTouchExpandable(true);
		split.setResizeWeight(1.0);
		split.setDividerSize(3);
		split.setOneTouchExpandable(true);

		// Add dotted Panel and Setting Panel
		tabbedPane.add("Dotted Chart", split);
		settingPanel = new SettingPanel(inputLog, this, dottedChartPanel,
				dcModel);
		settingPanel.setMinimumSize(new Dimension(width, height));
		JScrollPane settingScroll = new JScrollPane(settingPanel);
		tabbedPane.add("Settings", settingScroll);
		tabbedPane.setSelectedIndex(0);
		tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				switch (tabbedPane.getSelectedIndex()) {
				case 0:
					getOverviewPanel().setDrawBox(true);
					dottedChartPanel.changeWidthSort();
					break;
				default:
					break;
				}
			}
		});

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

		// inintialize performace metrics
		metricsPanel.displayPerformanceMetrics();
	}

	private void initOptions() {
		// todo : init eventTypetoKeep
		eventTypeToKeep = new ArrayList(Arrays.asList(inputLog.getLogSummary()
				.getEventTypes()));
		ArrayList<String> atr = new ArrayList<String>();
		for (int i = 0; i < eventTypeToKeep.size(); i++)
			atr.add((String) eventTypeToKeep.get(i));
		obtainInstanceIDs();
	}

	// public methods for attributes
	public DottedChartModel getDottedChartModel() {
		return dcModel;
	}

	public DottedChartPanel getDottedChartPanel() {
		return dottedChartPanel;
	}

	public OverviewPanel getOverviewPanel() {
		return ovPanel;
	}

	public MetricsPanel getMetricsPanel() {
		return metricsPanel;
	}

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

	public int[] getSelectedInstanceIndicesfromScreen() {
		return selectedInstanceIndicesfromScreen;
	}

	public void setSelectedInstanceIndicesfromScreen(int[] instances) {
		selectedInstanceIndicesfromScreen = instances;
	}

	public ProvidedObject[] getProvidedObjects() {
		try {
			if (selectedInstanceIndices != null) {
				// fill the high level PN with simulation information
				if (selectedInstanceIndicesfromScreen == null) {
					ProvidedObject[] objects = {
							new ProvidedObject("Whole Log", new Object[] {
									inputLog,
									dottedChartPanel.getColorReference() }),
							new ProvidedObject("Log Selection", new Object[] {
									LogReaderFactory.createInstance(inputLog,
											selectedInstanceIndices),
									dottedChartPanel.getColorReference() }) };
					return objects;
				} else {
					ProvidedObject[] objects = {
							new ProvidedObject("Whole Log", new Object[] {
									inputLog,
									dottedChartPanel.getColorReference() }),
							new ProvidedObject("Log Selection", new Object[] {
									LogReaderFactory.createInstance(inputLog,
											selectedInstanceIndices),
									dottedChartPanel.getColorReference() }),
							new ProvidedObject(
									"Log Selection from Screen",
									new Object[] {
											LogReaderFactory
													.createInstance(inputLog,
															selectedInstanceIndicesfromScreen),
											dottedChartPanel
													.getColorReference() }),

					};
					return objects;
				}
			} else {
				ProvidedObject[] objects = new ProvidedObject[] { new ProvidedObject(
						"Dotted Chart", new Object[] { inputLog,
								dottedChartPanel.getColorReference() }) };
				return objects;

			}
		} catch (Exception e) {
			System.err.println("Fatal error creating new log reader instance:");
			System.err.println("(" + this.getClass() + ")");
			e.printStackTrace();
			return null;
		}
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
		if ((aP.x + chartPane.getViewport().getSize().getWidth()) > dottedChartPanel
				.getWidth())
			aP.x = (int) (dottedChartPanel.getWidth() - chartPane.getViewport()
					.getSize().getWidth());
		if ((aP.y + chartPane.getViewport().getSize().getHeight()) > dottedChartPanel
				.getHeight())
			aP.y = (int) (dottedChartPanel.getHeight() - chartPane
					.getViewport().getSize().getHeight());
		chartPane.getViewport().setViewPosition(aP);
	}

	// Obtains the names of the process instances in the log and stores them in
	// the instanceIDs list
	public void obtainInstanceIDs() {
		// note that there is not just the keyset returned to keep
		// the oder of the instances
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
