/*
 * Created on July. 02, 2007
 *
 * Author: Minseok Song
 * (c) 2006 Technische Universiteit Eindhoven, Minseok Song
 * all rights reserved
 *
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */

package org.processmining.analysis.performance.basicperformance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.InstOriTPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.InstanceTPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.InstanceTaskTPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.OriginatorTPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.TaskOriginatorTPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.TaskTPerformance;
import org.processmining.analysis.performance.basicperformance.model.instance.InstancePerformance;

import org.processmining.analysis.performance.basicperformance.chart.AbstractChart;
import org.processmining.analysis.performance.basicperformance.chart.GanttChart;
import org.processmining.analysis.performance.basicperformance.chart.BoxAndWhiskerChart;
import org.processmining.analysis.performance.basicperformance.chart.MeterChart;
import org.processmining.analysis.performance.basicperformance.chart.XYBlockChart;
import org.processmining.analysis.performance.basicperformance.chart.XYBlockHourChart;
import org.processmining.analysis.performance.basicperformance.chart.BubblesChart;
import org.processmining.analysis.performance.basicperformance.chart.TableChart;
import org.processmining.analysis.performance.basicperformance.chart.defaultcategory.PieChart;
import org.processmining.analysis.performance.basicperformance.chart.defaultcategorylist.SpiderChart;
import org.processmining.analysis.performance.basicperformance.chart.defaultcategorylist.BarListChart;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.StopWatch;
import java.util.GregorianCalendar;
import java.util.Calendar;

public class BasicPerformanceAnalysisUI extends JPanel implements
		GuiNotificationTarget {

	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	public static Color colorTextAreaBg = new Color(160, 160, 160);

	public static final String STR_NONE = "None";
	public static final String ST_ORIG = "Originator";
	public static final String ST_TASK = "Task ID";
	public static final String ST_INST = "Instance ID";
	public static final String ST_EVEN = "Event";
	public static final String ST_DATA = "Data";

	public static final String GT_BoxandWhisker = "Box and Whisker";

	// Performance objects
	private TaskTPerformance taskTPerformance;
	private OriginatorTPerformance oriTPerformance;
	private InstanceTPerformance instTPerformance;
	private TaskOriginatorTPerformance toTPerformance;
	private InstanceTaskTPerformance itTPerformance;
	private InstOriTPerformance ioTPerformance;
	private InstancePerformance instPerformance;

	protected LogReader inputLog;
	private JPanel mainPanel = new JPanel();
	private JSplitPane splitPane = new JSplitPane();
	private JPanel menuPanel = new JPanel();
	private JPanel chartPanel = new JPanel();
	protected JPanel configurationPanel;
	protected JPanel performancePanel;
	protected ProgressPanel progressPanel;

	protected GUIPropertyBoolean originatorDim;
	protected GUIPropertyBoolean taskDim;
	protected GUIPropertyBoolean instanceDim;

	protected GUIPropertyListEnumeration chartSort;
	protected GUIPropertyListEnumeration componentSort;
	protected GUIPropertyBoolean removeOutlier;
	protected GUIPropertyBoolean sameOriginatorPair;
	protected GUIPropertyInteger minOutlier;
	protected GUIPropertyInteger maxOutlier;
	protected GUIPropertyBoolean workingHour;
	protected GUIPropertyInteger beginTime;
	protected GUIPropertyInteger endTime;
	protected GUIPropertyBoolean holiday;
	protected GUIPropertyInteger timeOffset;

	protected boolean[] holidayArray;
	protected Date startDate, endDate;
	protected JPanel parent = null;

	public BasicPerformanceAnalysisUI(LogReader aInputLog) {
		this.setLayout(new BorderLayout());
		this.setBackground(colorBg);
		inputLog = aInputLog;
		this.removeAll();
		buildMainMenuGui();
		// initGraphMenu();
		parent = this;
	}

	public void initGraphMenu() {
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerSize(0);

		ArrayList<String> componentList = new ArrayList<String>();
		componentList.add("processing (task)");
		componentList.add("throughput (instance)");
		componentSort = new GUIPropertyListEnumeration("Measure:", "",
				componentList, this, 150);

		ArrayList<AbstractChart> chartList = new ArrayList<AbstractChart>();
		chartList.add(new BarListChart(this));
		chartList.add(new PieChart(this));
		chartList.add(new SpiderChart(this));
		chartList.add(new BoxAndWhiskerChart(this));
		if (numDim() > 1) {
			chartList.add(new GanttChart(this));
			if (isInstanceDim()) {
				chartList.add(new XYBlockChart(this));
				chartList.add(new XYBlockHourChart(this));
			}
		}
		chartList.add(new MeterChart(this));
		chartList.add(new TableChart(this));

		chartSort = new GUIPropertyListEnumeration("Chart", "", chartList,
				this, 200);

		menuPanel = new JPanel();
		menuPanel.add(componentSort.getPropertyPanel());
		menuPanel.add(chartSort.getPropertyPanel());
		mainPanel = new JPanel();
		mainPanel.setBackground(colorBg);
		menuPanel.setBackground(colorBg);
		chartPanel.setBackground(colorBg);
		splitPane.setBackground(colorBg);

		splitPane.setLeftComponent(menuPanel);
		splitPane.setRightComponent(chartPanel);
	}

	public void buildMainMenuGui() {
		// create configuration panel
		JPanel confLowerPanel = new JPanel();
		confLowerPanel.setBackground(colorBg);
		confLowerPanel.setLayout(new BorderLayout());
		confLowerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		configurationPanel = new JPanel();
		configurationPanel.setBackground(colorTextAreaBg);
		configurationPanel.setForeground(colorFg);
		configurationPanel.setLayout(new BoxLayout(configurationPanel,
				BoxLayout.Y_AXIS));
		configurationPanel.setBorder(BorderFactory.createEmptyBorder());
		// add scroll pane to the left of the configuration panel
		JPanel configSuperPanel = new JPanel();
		configSuperPanel.setBorder(BorderFactory
				.createEmptyBorder(5, 5, 10, 10));
		configSuperPanel.setBackground(colorBg);
		configSuperPanel.setMinimumSize(new Dimension(400, 2000));
		configSuperPanel.setMaximumSize(new Dimension(450, 2000));
		configSuperPanel.setPreferredSize(new Dimension(440, 2000));
		configSuperPanel.setLayout(new BorderLayout());
		JLabel configSuperLabel = new JLabel("Configuration");
		configSuperLabel.setBorder(BorderFactory
				.createEmptyBorder(0, 0, 10, 10));
		configSuperLabel.setForeground(colorFg);
		configSuperLabel.setOpaque(false);
		configSuperLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		configSuperLabel.setFont(configSuperLabel.getFont().deriveFont(16.0f));
		configSuperPanel.add(configSuperLabel, BorderLayout.NORTH);
		configSuperPanel.add(configurationPanel, BorderLayout.CENTER);

		confLowerPanel.add(configSuperPanel, BorderLayout.CENTER);

		// init dim menu..
		JPanel dimPanel = new JPanel();
		dimPanel.setBackground(colorTextAreaBg);
		dimPanel.setForeground(colorFg);
		dimPanel.setLayout(new BoxLayout(dimPanel, BoxLayout.Y_AXIS));
		dimPanel.setBorder(BorderFactory.createEmptyBorder());
		dimPanel.setPreferredSize(new Dimension(350, 250));
		dimPanel.setMaximumSize(new Dimension(350, 250));
		taskDim = new GUIPropertyBoolean("Task dimension", true);
		originatorDim = new GUIPropertyBoolean("Originator dimension", true);
		instanceDim = new GUIPropertyBoolean(
				"Instance dimension (required for Time/DayHour Chart)", false);
		dimPanel.add(taskDim.getPropertyPanel());
		dimPanel.add(originatorDim.getPropertyPanel());
		dimPanel.add(instanceDim.getPropertyPanel());
		configurationPanel.add(dimPanel);

		// consider different originators for the same task
		JPanel originatorPanel = new JPanel();
		originatorPanel.setBackground(colorTextAreaBg);
		originatorPanel.setForeground(colorFg);
		originatorPanel.setLayout(new BoxLayout(originatorPanel,
				BoxLayout.Y_AXIS));
		originatorPanel.setBorder(BorderFactory.createEmptyBorder());
		originatorPanel.setPreferredSize(new Dimension(350, 250));
		originatorPanel.setMaximumSize(new Dimension(350, 250));
		GregorianCalendar calendar = new GregorianCalendar();
		int k = (calendar.getTimeZone().getDefault().getRawOffset() + calendar
				.getTimeZone().getDSTSavings()) / 3600000;
		timeOffset = new GUIPropertyInteger("Timezone Offset: ", "", k, 0, 24);
		;
		originatorPanel.add(timeOffset.getPropertyPanel());
		sameOriginatorPair = new GUIPropertyBoolean(
				"Consider different originators for a single task", false);
		originatorPanel.add(sameOriginatorPair.getPropertyPanel());
		configurationPanel.add(originatorPanel);

		// remove outliers
		JPanel removePanel = new JPanel();
		removePanel.setBackground(colorTextAreaBg);
		removePanel.setForeground(colorFg);
		removePanel.setLayout(new BoxLayout(removePanel, BoxLayout.Y_AXIS));
		removePanel.setBorder(BorderFactory.createEmptyBorder());
		removePanel.setPreferredSize(new Dimension(350, 250));
		removePanel.setMaximumSize(new Dimension(350, 250));
		removeOutlier = new GUIPropertyBoolean("Remove outliers", false);
		minOutlier = new GUIPropertyInteger("lower bound (percentile): ", "",
				10, 0, 100);
		maxOutlier = new GUIPropertyInteger("upper bound (percentile): ", "",
				90, 0, 100);
		removePanel.add(removeOutlier.getPropertyPanel());
		removePanel.add(minOutlier.getPropertyPanel());
		removePanel.add(maxOutlier.getPropertyPanel());
		configurationPanel.add(removePanel);

		JPanel workingPanel = new JPanel();
		workingPanel.setBackground(colorTextAreaBg);
		workingPanel.setForeground(colorFg);
		workingPanel.setLayout(new BoxLayout(workingPanel, BoxLayout.Y_AXIS));
		workingPanel.setBorder(BorderFactory.createEmptyBorder());
		workingPanel.setPreferredSize(new Dimension(350, 250));
		workingPanel.setMaximumSize(new Dimension(350, 250));
		workingHour = new GUIPropertyBoolean("Consider working hour", false);
		confLowerPanel.add(configSuperPanel, BorderLayout.CENTER);
		beginTime = new GUIPropertyInteger("Begin time: ", "", 9, 0, 24);
		endTime = new GUIPropertyInteger("End time: ", "", 17, 0, 24);
		workingPanel.add(workingHour.getPropertyPanel());
		workingPanel.add(beginTime.getPropertyPanel());
		workingPanel.add(endTime.getPropertyPanel());
		holiday = new GUIPropertyBoolean("Considering weekends/holidays", false);
		workingPanel.add(holiday.getPropertyPanel());
		configurationPanel.add(workingPanel);

		// create right side, i.e. distance metric and clustering algorithm
		// choice
		SmoothPanel rightPanel = new SmoothPanel();
		rightPanel.setBackground(new Color(140, 140, 140));
		rightPanel.setHighlight(new Color(160, 160, 160));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		rightPanel.add(Box.createHorizontalStrut(5));
		// add right side to configuration panel at center position
		JButton startButton = new AutoFocusButton("start calculation");
		startButton.setOpaque(false);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startCalculating();
			}
		});
		rightPanel.add(startButton);
		confLowerPanel.add(rightPanel, BorderLayout.EAST);

		// add header
		configurationPanel = new JPanel();
		configurationPanel.setBorder(BorderFactory.createEmptyBorder());
		configurationPanel.setLayout(new BorderLayout());
		configurationPanel.add(confLowerPanel, BorderLayout.CENTER);

		HeaderBar header = new HeaderBar("Basic Performance Analysis");
		header.setHeight(40);
		configurationPanel.add(header, BorderLayout.NORTH);
		// set configuration panel as displayed
		configurationPanel.revalidate();
		this.removeAll();
		this.add(configurationPanel, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	public void updateGUI() {
		splitPane.remove(chartPanel);
		chartPanel = null;
		chartPanel = ((AbstractChart) chartSort.getValue())
				.getPanel(getPerformanceSort());
		chartPanel.setBackground(colorBg);
		splitPane.setRightComponent(chartPanel);
	}

	protected void startCalculating() {
		this.removeAll();

		progressPanel = new ProgressPanel("Calculation");
		progressPanel
				.setNote("Building set of performance measures from log...");
		this.add(progressPanel.getPanel(), BorderLayout.CENTER);
		progressPanel.setMinMax(0, 9);

		Thread constructionThread = new Thread() {
			public void run() {
				StopWatch watch = new StopWatch();
				watch.start();
				progressPanel.setProgress(0);
				progressPanel.setNote("task measure ...");
				if (taskDim.getValue()) {
					taskTPerformance = new TaskTPerformance(inputLog);
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}
				if (originatorDim.getValue()) {
					oriTPerformance = new OriginatorTPerformance(inputLog);
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}
				if (instanceDim.getValue()) {
					instTPerformance = new InstanceTPerformance(inputLog);
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}
				if (taskDim.getValue() && originatorDim.getValue()) {
					toTPerformance = new TaskOriginatorTPerformance(inputLog);
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}
				if (taskDim.getValue() && instanceDim.getValue()) {
					itTPerformance = new InstanceTaskTPerformance(inputLog);
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}
				if (instanceDim.getValue() && originatorDim.getValue()) {
					ioTPerformance = new InstOriTPerformance(inputLog);
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}
				progressPanel.setNote("add measurement references ...");
				if (taskDim.getValue() && originatorDim.getValue()) {
					toTPerformance.setRefPerformance(taskTPerformance,
							oriTPerformance);
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}
				if (taskDim.getValue() && instanceDim.getValue()) {
					itTPerformance.setRefPerformance(instTPerformance,
							taskTPerformance);
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}
				if (instanceDim.getValue() && originatorDim.getValue()) {
					ioTPerformance.setRefPerformance(instTPerformance,
							oriTPerformance);
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}
				progressPanel.setNote("instance measure ...");
				instPerformance = new InstancePerformance(inputLog);
				watch.stop();
				if (holiday.getValue()) {
					initHoliday();
				}
				construct();
				if (removeOutlier.getValue()) {
					reconstruct();
					progressPanel.setNote("drawing graph ...");
				}
				initGraphMenu();
				initGraphPanel();
			}
		};
		constructionThread.start();
	}

	protected void initGraphPanel() {
		String str = "(";
		if (removeOutlier.getValue())
			str += " outlier ";
		if (workingHour.getValue())
			str += " working_hour ";
		if (holiday.getValue())
			str += " holiday ";
		if (str.equals("(")) {
			str = "";
		} else {
			str += ")";
		}
		HeaderBar header = new HeaderBar("Basic Performance Analysis " + str);
		header.setHeight(40);
		header.setCloseActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// reset to configuration view
				removeAll();
				add(configurationPanel, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		});
		splitPane.remove(chartPanel);
		chartPanel = ((AbstractChart) chartSort.getValue())
				.getPanel(getPerformanceSort());
		chartPanel.setBackground(colorBg);
		splitPane.setRightComponent(chartPanel);

		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(splitPane);
		performancePanel = new JPanel();
		performancePanel.setBorder(BorderFactory.createEmptyBorder());
		performancePanel.setLayout(new BorderLayout());
		performancePanel.add(header, BorderLayout.NORTH);
		performancePanel.add(mainPanel, BorderLayout.CENTER);
		removeAll();
		add(performancePanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	public LogReader getLogReader() {
		return inputLog;
	}

	/**
	 * calculate performance measures
	 */
	public void construct() {

		// initialize list that is used to keep track of the data-elements that
		// appear in the process log
		AbstractPerformance.initIndex();

		Iterator<ProcessInstance> it = inputLog.instanceIterator();
		progressPanel.setNote("Calculating ...");
		this.add(progressPanel.getPanel(), BorderLayout.CENTER);
		progressPanel.setMinMax(0, inputLog.numberOfInstances());
		int i = 0;
		while (it.hasNext()) {
			progressPanel.setProgress(i);
			progressPanel.setNote("instance: " + i + "/"
					+ inputLog.numberOfInstances());
			ProcessInstance pi = it.next();
			ArrayList<AuditTrailEntry> durationReferenceList = new ArrayList<AuditTrailEntry>();
			ArrayList<AuditTrailEntry> scheduleReferenceList = new ArrayList<AuditTrailEntry>();
			AuditTrailEntry lastATE = null;

			Iterator<AuditTrailEntry> ates = pi.getAuditTrailEntryList()
					.iterator();
			long waitingInstance = 0; // waiting for instance
			long workingInstance = 0; // working for instance
			int index = 0;
			AuditTrailEntry startATE = null;
			AuditTrailEntry endATE = null;
			AuditTrailEntry ate = null;
			while (ates.hasNext()) {
				ate = ates.next();
				if (ate.getTimestamp() != null) {
					if (lastATE == null)
						lastATE = ate;
					if (startATE == null) {
						startATE = ate;
						endATE = ate;
					}
					if (endATE.getTimestamp().before(ate.getTimestamp())) {
						endATE = ate;
					}
					String originator = ate.getOriginator();
					String task = ate.getElement();
					String task_org = task + " - " + originator;
					String inst_task = pi.getName() + " - " + task;
					String inst_org = pi.getName() + " - " + originator;
					if (ate.getType().equals("schedule")) {
						scheduleReferenceList.add(ate);
					} else if (ate.getType().equals("start")) {
						durationReferenceList.add(ate);
						AuditTrailEntry ateRef = null;
						for (AuditTrailEntry ate1 : scheduleReferenceList) {
							if (ate1.getElement().equals(task)) {
								scheduleReferenceList.remove(ate1);
								ateRef = ate1;
								break;
							}
						}
						if (ateRef == null)
							ateRef = lastATE;
						if (ateRef != null) {
							double diff = calculateDiff(ateRef, ate);

							if (sameOriginatorPair.getValue()) {
								try {
									AuditTrailEntryList entries = pi
											.getAuditTrailEntryList();
									for (int j = index + 1; j < entries.size(); j++) {
										AuditTrailEntry entry2 = entries.get(j);
										if (ate.getElement().equals(
												entry2.getElement())
												&& entry2.getType().equals(
														"complete")) {
											ate.setOriginator(entry2
													.getOriginator());
											originator = entry2.getOriginator();
											task_org = task + " - "
													+ originator;
											inst_org = pi.getName() + " - "
													+ originator;
										}
									}

								} catch (IOException ex) {
								} catch (IndexOutOfBoundsException ex) {
								}
							}

							if (taskDim.getValue()) {
								taskTPerformance.addWaitingTime(task, diff);
							}
							if (originatorDim.getValue()) {
								oriTPerformance
										.addWaitingTime(originator, diff);
							}
							if (instanceDim.getValue()) {
								instTPerformance.addWaitingTime(pi.getName(),
										diff);
							}
							if (taskDim.getValue() && originatorDim.getValue()) {
								toTPerformance.addWaitingTime(task_org, diff);
							}
							if (taskDim.getValue() && instanceDim.getValue()) {
								itTPerformance.addWaitingTime(inst_task, diff);
							}
							if (originatorDim.getValue()
									&& instanceDim.getValue()) {
								ioTPerformance.addWaitingTime(inst_org, diff);
							}
							waitingInstance += diff;
						}
					} else if (ate.getType().equals("complete")) {

						AuditTrailEntry ateRef = null;
						for (AuditTrailEntry ate1 : durationReferenceList) {
							if (ate1.getElement().equals(task)) {
								durationReferenceList.remove(ate1);
								ateRef = ate1;
								break;
							}
						}
						// for waiting time
						if (ateRef == null)
							ateRef = lastATE;
						double diff = calculateDiff(ateRef, ate);

						workingInstance += diff;
						if (taskDim.getValue()) {
							taskTPerformance.addExecutionTime(task, diff);
						}
						if (originatorDim.getValue()) {
							oriTPerformance.addExecutionTime(originator, diff);
						}
						if (instanceDim.getValue()) {
							instTPerformance.addExecutionTime(pi.getName(),
									diff);
						}
						if (taskDim.getValue() && originatorDim.getValue()) {
							toTPerformance.addExecutionTime(task_org, diff);
							toTPerformance.addTaskSeries(task, originator,
									ateRef.getTimestamp(), ate.getTimestamp());
						}
						if (taskDim.getValue() && instanceDim.getValue()) {
							itTPerformance.addExecutionTime(inst_task, diff);
							itTPerformance.addTaskSeries(pi.getName(), task,
									ateRef.getTimestamp(), ate.getTimestamp());

						}
						if (originatorDim.getValue() && instanceDim.getValue()) {
							ioTPerformance.addExecutionTime(inst_org, diff);
							ioTPerformance.addTaskSeries(pi.getName(),
									originator, ateRef.getTimestamp(), ate
											.getTimestamp());
						}
						lastATE = ate;
					}
				}
				index++;
			}
			double diff = calculateDiff(startATE, endATE);
			instPerformance.addExecutionTime(pi.getName(), workingInstance);
			instPerformance.addSojournTime(pi.getName(), diff);
			instPerformance.addWaitingTime(pi.getName(), waitingInstance);
			instPerformance.addTaskSeries(pi.getName(),
					startATE.getTimestamp(), endATE.getTimestamp());
			i++;
		}
	}

	private long calculateDiff(AuditTrailEntry startATE, AuditTrailEntry endATE) {
		if (workingHour.getValue() && !holiday.getValue()) {
			return workingTime(startATE, endATE);
		} else if (workingHour.getValue() && holiday.getValue()) {
			return workingTimeHoliday(startATE, endATE);
		} else if (!workingHour.getValue() && holiday.getValue()) {
			return holiday(startATE, endATE);
		} else {
			return endATE.getTimestamp().getTime()
					- startATE.getTimestamp().getTime();
		}
	}

	protected long workingTime(AuditTrailEntry lastATE, AuditTrailEntry ate) {
		long diff = 0;
		long start = lastATE.getTimestamp().getTime() + timeOffset.getValue()
				* 3600000L;
		long end = ate.getTimestamp().getTime() + timeOffset.getValue()
				* 3600000L;
		long duration = end / (long) 86400000L - start / (long) 86400000L;
		long adjustedEnd = Math.max(end % (long) 86400000L - 3600000L
				* beginTime.getValue(), 0);
		adjustedEnd = Math.min(adjustedEnd,
				3600000L * (endTime.getValue() - beginTime.getValue()));
		long adjustedBegin = Math.max(start % (long) 86400000L - 3600000L
				* beginTime.getValue(), 0);
		adjustedBegin = Math.min(adjustedBegin,
				3600000L * (endTime.getValue() - beginTime.getValue()));

		if (duration == 0) {
			diff = adjustedEnd - adjustedBegin;
		} else if (duration >= 1) {
			long temp = 3600000L * (endTime.getValue() - beginTime.getValue())
					* (duration - 1);
			diff += Math.max(temp, 0);
			diff += 3600000L * (endTime.getValue() - beginTime.getValue())
					- adjustedBegin + adjustedEnd;
		}
		return diff;
	}

	protected long holiday(AuditTrailEntry lastATE, AuditTrailEntry ate) {
		long diff = 0;
		long start = lastATE.getTimestamp().getTime() + timeOffset.getValue()
				* 3600000L;
		long end = ate.getTimestamp().getTime() + timeOffset.getValue()
				* 3600000L;
		long duration = end / (long) 86400000L - start / (long) 86400000L;

		if (duration == 0) {
			if (!isWeekend(start))
				diff = end - start;
		} else if (duration >= 1) {
			long temp = start;
			diff = 0;
			if (!isWeekend(start))
				diff += Math.max(0, 3600000L * 24 - start % (long) 86400000L);
			;
			for (int i = 1; i < duration; i++) {
				temp += 86400000L;
				if (!isWeekend(temp))
					diff += 86400000L;
			}
			if (!isWeekend(end)) {
				diff += end % (long) 86400000L;
			}
		}
		return diff;
	}

	protected long workingTimeHoliday(AuditTrailEntry lastATE,
			AuditTrailEntry ate) {
		long diff = 0;
		long start = lastATE.getTimestamp().getTime() + timeOffset.getValue()
				* 3600000L;
		long end = ate.getTimestamp().getTime() + timeOffset.getValue()
				* 3600000L;
		long duration = end / (long) 86400000L - start / (long) 86400000L;
		long adjustedEnd = Math.max(end % (long) 86400000L - 3600000L
				* beginTime.getValue(), 0);
		adjustedEnd = Math.min(adjustedEnd,
				3600000L * (endTime.getValue() - beginTime.getValue()));
		long adjustedBegin = Math.max(start % (long) 86400000L - 3600000L
				* beginTime.getValue(), 0);
		adjustedBegin = Math.min(adjustedBegin,
				3600000L * (endTime.getValue() - beginTime.getValue()));

		if (duration == 0) {
			if (!isWeekend(start))
				diff = adjustedEnd - adjustedBegin;
		} else if (duration >= 1) {
			long temp = start;
			diff = 0;
			if (!isWeekend(start))
				diff += 3600000L * (endTime.getValue() - beginTime.getValue())
						- adjustedBegin;
			for (int i = 1; i < duration; i++) {
				temp += 86400000L;
				if (!isWeekend(temp))
					diff += 3600000L * (endTime.getValue() - beginTime
							.getValue());
			}
			if (!isWeekend(end)) {
				diff += adjustedEnd;
			}
		}
		return diff;
	}

	protected boolean isWeekend(long date) {
		boolean bResult = false;

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date(date));
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY)
			bResult = true;
		return bResult;
	}

	public boolean[] getHolidayArray() {
		return holidayArray;
	}

	public boolean isConsiderHoliday() {
		return holiday.getValue();
	}

	public boolean isConsiderWorkingHour() {
		return workingHour.getValue();
	}

	public int getStartHour() {
		return beginTime.getValue();
	}

	public int getEndHour() {
		return endTime.getValue();
	}

	protected void initHoliday() {
		long duration;
		startDate = inputLog.getLogSummary().getStartTime(
				inputLog.getProcess(0).getName());
		endDate = inputLog.getLogSummary().getEndTime(
				inputLog.getProcess(0).getName());
		duration = endDate.getTime() / (long) 86400000L - startDate.getTime()
				/ (long) 86400000L;
		holidayArray = new boolean[(int) duration + 1];
		for (int k = 0; k < duration + 1; k++) {
			holidayArray[k] = false;
		}

		Iterator<ProcessInstance> it = inputLog.instanceIterator();
		progressPanel.setNote("initializing holidays ...");
		this.add(progressPanel.getPanel(), BorderLayout.CENTER);
		progressPanel.setMinMax(0, inputLog.numberOfInstances());
		int i = 0;
		while (it.hasNext()) {
			progressPanel.setProgress(i);
			ProcessInstance pi = it.next();

			Iterator<AuditTrailEntry> ates = pi.getAuditTrailEntryList()
					.iterator();
			while (ates.hasNext()) {
				AuditTrailEntry ate = ates.next();
				int index = (int) (ate.getTimestamp().getTime()
						/ (long) 86400000L - startDate.getTime()
						/ (long) 86400000L);
				holidayArray[index] = true;
			}
			i++;
		}
	}

	/**
	 * calculate performance measures
	 */
	public void reconstruct() {
		int min = minOutlier.getValue();
		int max = maxOutlier.getValue();
		progressPanel.setMinMax(0, 7);
		progressPanel.setNote("removing outlier ...");
		progressPanel.setProgress(0);
		if (taskDim.getValue()) {
			taskTPerformance.removeOutlier(min, max);
			progressPanel.setProgress(1);
		}
		if (originatorDim.getValue()) {
			oriTPerformance.removeOutlier(min, max);
			progressPanel.setProgress(2);
		}
		if (instanceDim.getValue()) {
			instTPerformance.removeOutlier(min, max);
			progressPanel.setProgress(3);
		}
		if (taskDim.getValue() && originatorDim.getValue()) {
			toTPerformance.removeOutlier(min, max);
			progressPanel.setProgress(4);
		}
		if (taskDim.getValue() && instanceDim.getValue()) {
			itTPerformance.removeOutlier(min, max);
			progressPanel.setProgress(5);
		}
		if (originatorDim.getValue() && instanceDim.getValue()) {
			ioTPerformance.removeOutlier(min, max);
			progressPanel.setProgress(6);
		}
		instPerformance.removeOutlier(min, max);
	}

	public AbstractPerformance getPerformance(String name) {
		if (name.equals("Task")) {
			return taskTPerformance;
		} else if (name.equals("Originator")) {
			return oriTPerformance;
		} else if (name.equals("Instance")) {
			return instTPerformance;
		} else if (name.equals("Task-Originator")) {
			return toTPerformance;
		} else if (name.equals("Instance-Task")) {
			return itTPerformance;
		} else if (name.equals("Instance-Originator")) {
			return ioTPerformance;
		} else {// if(a.equals(instPerformance.getName())) {
			return instPerformance;
		}
	}

	public boolean isTaskDim() {
		return taskDim.getValue();
	}

	public boolean isOriginatorDim() {
		return originatorDim.getValue();
	}

	public boolean isInstanceDim() {
		return instanceDim.getValue();
	}

	public int numDim() {
		int k = 0;
		if (taskDim.getValue()) {
			k++;
		}
		if (originatorDim.getValue()) {
			k++;
		}
		if (instanceDim.getValue()) {
			k++;
		}
		return k;
	}

	public AbstractPerformance getPerformanceSort() {
		if (componentSort.getValue().equals("throughput (instance)")) {
			return instPerformance;
		} else if (chartSort.getValue() instanceof BubblesChart) {
			return toTPerformance;
		} else if (chartSort.getValue() instanceof GanttChart) {
			return null;
		} else if (chartSort.getValue() instanceof XYBlockChart
				|| chartSort.getValue() instanceof XYBlockHourChart) {
			if (taskDim.getValue()) {
				return itTPerformance;
			} else {
				return ioTPerformance;
			}
		} else {
			return taskTPerformance;
		}
	}
}
