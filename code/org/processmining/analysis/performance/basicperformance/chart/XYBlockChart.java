package org.processmining.analysis.performance.basicperformance.chart;

import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Day;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.xy.XYDataset;

import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.analysis.performance.basicperformance.model.instance.InstancePerformance;
import org.processmining.analysis.performance.basicperformance.model.task.InstOriTPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.InstanceTaskTPerformance;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GUIPropertyInteger;

public class XYBlockChart extends AbstractChart {

	protected GUIPropertyListEnumeration measSort = null;
	protected long timeUnit = 86400000L;
	protected RegularTimePeriod periodUnit = new Day();
	protected Date startDate, endDate;
	protected GUIPropertyInteger startTime, endTime;
	protected HashMap<String, TaskSeries> tsMap;
	protected GUIPropertyListEnumeration lineType = null;
	protected double sum = 0.0;
	protected boolean[] holidays;

	public XYBlockChart() {
		super("Time Chart", "Time Chart");
		initGUI();
	}

	public XYBlockChart(BasicPerformanceAnalysisUI pm) {
		super("Time Chart", "Time Chart", pm);
		initGUI();
	}

	public void initGUI() {
		isItemA = false;
		ArrayList<String> measList = new ArrayList<String>();
		measList.add("time");
		measList.add("frequency");
		measSort = new GUIPropertyListEnumeration("Measure:", "", measList,
				this, 150);
		ArrayList<String> lineList = new ArrayList<String>();
		lineList.add("step");
		lineList.add("curve");
		lineType = new GUIPropertyListEnumeration("Line type:", "", lineList,
				this, 150);
		ArrayList<String> displayList = new ArrayList<String>();
		displayList.add("overall");
		if (performanceUI.isTaskDim()) {
			displayList.add("task");
		}
		if (performanceUI.isOriginatorDim()) {
			displayList.add("originator");
		}
		if (performanceUI.isInstanceDim()) {
			displayList.add("instance");
		}
		dim1Sort = new GUIPropertyListEnumeration("Display:", "", displayList,
				this, 150);
	}

	protected long getPeriodUnit() {
		if (periodUnit instanceof Day) {
			return 86400000L;
		} else if (periodUnit instanceof Hour) {
			return 3600000L;
		} else {// if(periodUnit instanceof Minute)
			return 60000;
		}
	}

	protected int initDuration(HashMap<String, TaskSeries> taskSeriesMap) {
		int duration;
		for (TaskSeries ts : taskSeriesMap.values()) {
			for (int i = 0; i < ts.getTasks().size(); i++) {
				Task task = ts.get(i);
				SimpleTimePeriod stp = (SimpleTimePeriod) task.getDuration();
				if (startDate == null || startDate.after(stp.getStart())) {
					startDate = stp.getStart();
				}
				if (endDate == null || endDate.before(stp.getEnd())) {
					endDate = stp.getEnd();
				}
			}
		}
		if (endDate != null && startDate != null) {
			periodUnit = new Day();
			duration = (int) ((endDate.getTime() - startDate.getTime()) / getPeriodUnit());
			duration = Math.max(1, duration);
		} else {
			duration = -1;
		}
		if (duration == 1) {
			periodUnit = new Hour();
			duration = (int) ((endDate.getTime() - startDate.getTime()) / getPeriodUnit());
			duration = Math.max(1, duration);
		}
		if (duration == 1) {
			periodUnit = new Minute();
			duration = (int) ((endDate.getTime() - startDate.getTime()) / getPeriodUnit());
			duration = Math.max(1, duration);
		}

		if (performanceUI.isConsiderHoliday()) {
			holidays = performanceUI.getHolidayArray();
		}

		return duration;
	}

	protected int initDuration(TaskSeries taskSeries) {
		int duration;

		for (int i = 0; i < taskSeries.getTasks().size(); i++) {
			Task task = taskSeries.get(i);
			SimpleTimePeriod stp = (SimpleTimePeriod) task.getDuration();
			if (startDate == null || startDate.after(stp.getStart())) {
				startDate = stp.getStart();
			}
			if (endDate == null || endDate.before(stp.getEnd())) {
				endDate = stp.getEnd();
			}
		}

		if (endDate != null && startDate != null) {
			periodUnit = new Day();
			duration = (int) ((endDate.getTime() - startDate.getTime()) / getPeriodUnit());
			duration = Math.max(1, duration);
		} else {
			duration = -1;
		}
		if (duration == 1) {
			periodUnit = new Hour();
			duration = (int) ((endDate.getTime() - startDate.getTime()) / getPeriodUnit());
			duration = Math.max(1, duration);
		}
		if (duration == 1) {
			periodUnit = new Minute();
			duration = (int) ((endDate.getTime() - startDate.getTime()) / getPeriodUnit());
			duration = Math.max(1, duration);
		}

		if (performanceUI.isConsiderHoliday()) {
			holidays = performanceUI.getHolidayArray();
		}

		return duration;
	}

	// generating data set
	// ///////////////////////////////////////////////////////////////////////
	private ArrayList<XYDataset> getXYBlockChartDataset(
			HashMap<String, TaskSeries> taskSeriesMap) {
		ArrayList<XYDataset> datasetList = new ArrayList<XYDataset>();

		TimeSeries series1;

		for (String key : taskSeriesMap.keySet()) {
			TaskSeries ts = taskSeriesMap.get(key);
			int duration = initDuration(ts);
			series1 = new TimeSeries(key, periodUnit.getClass());

			for (int j = 0; j < duration; j++) {
				Date currentDate = new Date(startDate.getTime() + j
						* getPeriodUnit());
				double num = 0;
				if (!performanceUI.isConsiderHoliday() || holidays[j]) {
					Date date1 = new Date(
							(currentDate.getTime() / getPeriodUnit())
									* getPeriodUnit());
					Date date2 = new Date((currentDate.getTime()
							/ getPeriodUnit() + 1)
							* getPeriodUnit());
					for (int i = 0; i < ts.getTasks().size(); i++) {
						Task task = ts.get(i);
						SimpleTimePeriod stp = (SimpleTimePeriod) task
								.getDuration();

						if (stp.getStart().before(date1)
								&& stp.getEnd().after(date2)) {
							if (periodUnit instanceof Day) {
								num += (double) (endTime.getValue() - startTime
										.getValue())
										* (double) 3600000L
										/ (double) getTimeUnit();
							} else {
								num += (double) getPeriodUnit()
										/ (double) getTimeUnit();
							}
						} else if ((stp.getStart().equals(date1) || stp
								.getStart().after(date1))
								&& (stp.getEnd().equals(date2) || stp.getEnd()
										.before(date2))) {
							double diff = ((double) (stp.getEnd().getTime() - stp
									.getStart().getTime()))
									/ (double) getTimeUnit();
							num += Math.max(0.0, diff);
						} else if ((stp.getStart().equals(date1) || stp
								.getStart().after(date1))
								&& stp.getStart().before(date2)) {
							double diff = ((double) (date2.getTime() - stp
									.getStart().getTime()))
									/ (double) getTimeUnit();
							if (getPeriodUnit() == 86400000L) {
								diff = diff
										- (double) (24 - endTime.getValue())
										* (double) 3600000L
										/ (double) getTimeUnit();
							}
							num += Math.max(0, diff); // 24-6
						} else if ((stp.getEnd().equals(date1) || stp.getEnd()
								.after(date1))
								&& stp.getEnd().before(date2)) {
							double diff = ((double) (date1.getTime() - stp
									.getEnd().getTime()))
									/ (double) getTimeUnit();
							if (getPeriodUnit() == 86400000L) {
								diff = diff - (double) startTime.getValue()
										* (double) 3600000L
										/ (double) getTimeUnit();
							}
							num += Math.max(0, diff); // 9-0
						}
					}
				}
				if (periodUnit instanceof Hour) {
					series1.add(new Hour(currentDate), num);
				} else if (periodUnit instanceof Day) {
					series1.add(new Day(currentDate), num);
				} else if (periodUnit instanceof Minute) {
					series1.add(new Minute(currentDate), num);
				}
			}
			datasetList.add(new TimeSeriesCollection(series1));
		}

		return datasetList;
	}

	private ArrayList<XYDataset> getXYBlockChartDataset_overall(
			HashMap<String, TaskSeries> taskSeriesMap) {
		ArrayList<XYDataset> datasetList = new ArrayList<XYDataset>();

		TimeSeries series1;
		sum = 0;
		int duration = initDuration(taskSeriesMap);
		series1 = new TimeSeries("working", periodUnit.getClass());

		for (int j = 0; j < duration; j++) {
			Date currentDate = new Date(startDate.getTime() + j
					* getPeriodUnit());
			double num = 0;
			if (!performanceUI.isConsiderHoliday() || holidays[j]) {
				for (TaskSeries ts : taskSeriesMap.values()) {
					Date date1 = new Date(
							(currentDate.getTime() / getPeriodUnit())
									* getPeriodUnit());
					Date date2 = new Date((currentDate.getTime()
							/ getPeriodUnit() + 1)
							* getPeriodUnit());
					for (int i = 0; i < ts.getTasks().size(); i++) {
						Task task = ts.get(i);
						SimpleTimePeriod stp = (SimpleTimePeriod) task
								.getDuration();

						if (stp.getStart().before(date1)
								&& stp.getEnd().after(date2)) {
							if (periodUnit instanceof Day) {
								num += (double) (endTime.getValue() - startTime
										.getValue())
										* (double) 3600000L
										/ (double) getTimeUnit();
							} else {
								num += (double) getPeriodUnit()
										/ (double) getTimeUnit();
							}
						} else if ((stp.getStart().equals(date1) || stp
								.getStart().after(date1))
								&& (stp.getEnd().equals(date2) || stp.getEnd()
										.before(date2))) {
							double diff = ((double) (stp.getEnd().getTime() - stp
									.getStart().getTime()))
									/ (double) getTimeUnit();
							num += Math.max(0.0, diff);
						} else if ((stp.getStart().equals(date1) || stp
								.getStart().after(date1))
								&& stp.getStart().before(date2)) {
							double diff = ((double) (date2.getTime() - stp
									.getStart().getTime()))
									/ (double) getTimeUnit();
							if (getPeriodUnit() == 86400000L) {
								diff = diff
										- (double) (24 - endTime.getValue())
										* (double) 3600000L
										/ (double) getTimeUnit();
							}
							num += Math.max(0, diff); // 24-6
						} else if ((stp.getEnd().equals(date1) || stp.getEnd()
								.after(date1))
								&& stp.getEnd().before(date2)) {
							double diff = ((double) (date1.getTime() - stp
									.getEnd().getTime()))
									/ (double) getTimeUnit();
							if (getPeriodUnit() == 86400000L) {
								diff = diff - (double) startTime.getValue()
										* (double) 3600000L
										/ (double) getTimeUnit();
							}
							num += Math.max(0, diff); // 9-0
						}
					}
				}
			}
			if (periodUnit instanceof Hour) {
				series1.add(new Hour(currentDate), num);
			} else if (periodUnit instanceof Day) {
				series1.add(new Day(currentDate), num);
			} else if (periodUnit instanceof Minute) {
				series1.add(new Minute(currentDate), num);
			}
			sum += num;
		}
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
		timeSeriesCollection.addSeries(series1);
		datasetList.add(timeSeriesCollection);
		sum = sum / duration;
		Message.add("Average:" + sum, Message.NORMAL);
		return datasetList;
	}

	private ArrayList<XYDataset> getXYBlockChartDataset_freq(
			HashMap<String, TaskSeries> taskSeriesMap) {
		ArrayList<XYDataset> datasetList = new ArrayList<XYDataset>();

		TimeSeries series1, series2, series3;

		for (String key : taskSeriesMap.keySet()) {
			TaskSeries ts = taskSeriesMap.get(key);
			int duration = initDuration(ts);
			series1 = new TimeSeries("# of elements", periodUnit.getClass());
			series2 = new TimeSeries("# of start", periodUnit.getClass());
			series3 = new TimeSeries("# of end", periodUnit.getClass());

			for (int j = 0; j < duration; j++) {
				Date currentDate = new Date(startDate.getTime() + j
						* getPeriodUnit());
				int num = 0;
				int num_start = 0;
				int num_end = 0;
				if (!performanceUI.isConsiderHoliday() || holidays[j]) {
					Date date1 = new Date(
							(currentDate.getTime() / getPeriodUnit())
									* getPeriodUnit());
					Date date2 = new Date((currentDate.getTime()
							/ getPeriodUnit() + 1)
							* getPeriodUnit());
					for (int i = 0; i < ts.getTasks().size(); i++) {
						Task task = ts.get(i);
						SimpleTimePeriod stp = (SimpleTimePeriod) task
								.getDuration();

						if (stp.getStart().before(date1)
								&& stp.getEnd().after(date2)) {
							num++;
						} else if ((stp.getStart().equals(date1) || stp
								.getStart().after(date1))
								&& (stp.getEnd().equals(date2) || stp.getEnd()
										.before(date2))) {
							num++;
							num_start++;
							num_end++;
						} else if ((stp.getStart().equals(date1) || stp
								.getStart().after(date1))
								&& stp.getStart().before(date2)) {
							num++;
							num_start++;
						} else if ((stp.getEnd().equals(date1) || stp.getEnd()
								.after(date1))
								&& stp.getEnd().before(date2)) {
							num++;
							num_end++;
						}
					}
				}
				if (periodUnit instanceof Hour) {
					series1.add(new Hour(currentDate), num);
					series2.add(new Hour(currentDate), num_start);
					series3.add(new Hour(currentDate), num_end);
				} else if (periodUnit instanceof Day) {
					series1.add(new Day(currentDate), num);
					series2.add(new Day(currentDate), num_start);
					series3.add(new Day(currentDate), num_end);
				} else if (periodUnit instanceof Minute) {
					series1.add(new Minute(currentDate), num);
					series2.add(new Minute(currentDate), num_start);
					series3.add(new Minute(currentDate), num_end);
				}
			}
			datasetList.add(new TimeSeriesCollection(series1));
			datasetList.add(new TimeSeriesCollection(series2));
			datasetList.add(new TimeSeriesCollection(series3));
		}
		return datasetList;
	}

	private ArrayList<XYDataset> getXYBlockChartDataset_freq_overall(
			HashMap<String, TaskSeries> taskSeriesMap) {
		ArrayList<XYDataset> datasetList = new ArrayList<XYDataset>();
		sum = 0.0;
		TimeSeries series1, series2, series3;
		int duration = initDuration(taskSeriesMap);

		series1 = new TimeSeries("# of elements", periodUnit.getClass());
		series2 = new TimeSeries("# of start", periodUnit.getClass());
		series3 = new TimeSeries("# of end", periodUnit.getClass());

		for (int j = 0; j < duration; j++) {
			Date currentDate = new Date(startDate.getTime() + j
					* getPeriodUnit());
			int num = 0;
			int num_start = 0;
			int num_end = 0;
			if (!performanceUI.isConsiderHoliday() || holidays[j]) {
				for (TaskSeries ts : taskSeriesMap.values()) {
					Date date1 = new Date(
							(currentDate.getTime() / getPeriodUnit())
									* getPeriodUnit());
					Date date2 = new Date((currentDate.getTime()
							/ getPeriodUnit() + 1)
							* getPeriodUnit());
					for (int i = 0; i < ts.getTasks().size(); i++) {
						Task task = ts.get(i);
						SimpleTimePeriod stp = (SimpleTimePeriod) task
								.getDuration();

						if (stp.getStart().before(date1)
								&& stp.getEnd().after(date2)) {
							num++;
						} else if ((stp.getStart().equals(date1) || stp
								.getStart().after(date1))
								&& (stp.getEnd().equals(date2) || stp.getEnd()
										.before(date2))) {
							num++;
							num_start++;
							num_end++;
						} else if ((stp.getStart().equals(date1) || stp
								.getStart().after(date1))
								&& stp.getStart().before(date2)) {
							num++;
							num_start++;
						} else if ((stp.getEnd().equals(date1) || stp.getEnd()
								.after(date1))
								&& stp.getEnd().before(date2)) {
							num++;
							num_end++;
						}
					}
				}
			}
			if (periodUnit instanceof Hour) {
				series1.add(new Hour(currentDate), num);
				series2.add(new Hour(currentDate), num_start);
				series3.add(new Hour(currentDate), num_end);
			} else if (periodUnit instanceof Day) {
				series1.add(new Day(currentDate), num);
				series2.add(new Day(currentDate), num_start);
				series3.add(new Day(currentDate), num_end);
			} else if (periodUnit instanceof Minute) {
				series1.add(new Minute(currentDate), num);
				series2.add(new Minute(currentDate), num_start);
				series3.add(new Minute(currentDate), num_end);
			}
			sum += num;
		}
		datasetList.add(new TimeSeriesCollection(series1));
		datasetList.add(new TimeSeriesCollection(series2));
		datasetList.add(new TimeSeriesCollection(series3));

		sum = sum / duration;
		Message.add("Average:" + sum, Message.NORMAL);
		return datasetList;
	}

	// Drawing XY Block Chart
	// //////////////////////////////////////////////////////////////////////////
	protected JScrollPane getGraphPanel() {
		ArrayList<XYDataset> datasetList = null;
		startDate = null;
		endDate = null;

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());

		timeUnit = 86400000L;

		updatePerformance();

		if (measSort.getValue().equals("time")) {
			if (dim1Sort.getValue().equals("overall")) {
				datasetList = getXYBlockChartDataset_overall(tsMap);
			} else {
				datasetList = getXYBlockChartDataset(tsMap);
			}
		} else if (measSort.getValue().equals("frequency")) {
			if (dim1Sort.getValue().equals("overall")) {
				datasetList = getXYBlockChartDataset_freq_overall(tsMap);
			} else {
				datasetList = getXYBlockChartDataset_freq(tsMap);
			}
		}

		if (dim1Sort.getValue().equals("overall")) {
			if (measSort.getValue().equals("frequency")) {
				scrollPane = new JScrollPane(getChartPanel(datasetList.get(0),
						datasetList.get(1), datasetList.get(2), null));
			} else {
				scrollPane = new JScrollPane(getChartPanel(datasetList.get(0),
						null));
			}
		} else {
			JPanel tempPanel = new JPanel();
			GridLayout tempLayout = new GridLayout(0, 3);
			tempPanel.setLayout(tempLayout);
			int k = 0;
			for (String key : tsMap.keySet()) {
				ChartPanel chartPanel;
				if (measSort.getValue().equals("frequency")) {
					chartPanel = getChartPanel(datasetList.get(k * 3),
							datasetList.get(k * 3 + 1), datasetList
									.get(k * 3 + 2), key);
				} else {
					chartPanel = getChartPanel(datasetList.get(k), key);
				}
				chartPanel.setPreferredSize(new java.awt.Dimension(250, 160));
				tempPanel.add(chartPanel, BorderLayout.CENTER);
				k++;
			}
			tempPanel.setPreferredSize(new java.awt.Dimension(250 * 3,
					160 * (k / 3 + 1)));
			scrollPane = new JScrollPane(tempPanel);
		}
		scrollPane.setBackground(colorBg);
		return scrollPane;
	}

	// return time panel
	private ChartPanel getChartPanel(XYDataset dataSet, String key) {
		if (lineType.getValue().equals("step")) {
			chart = ChartFactory.createXYStepChart(null, key, null, dataSet,
					PlotOrientation.VERTICAL, false, true, false);
		} else {
			chart = ChartFactory.createTimeSeriesChart(null, key, null,
					dataSet, false, true, false);
		}

		XYPlot plot = chart.getXYPlot();
		NumberAxis rangeAxis1 = (NumberAxis) plot.getRangeAxis();
		rangeAxis1.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		rangeAxis1.setLowerMargin(0.40);
		ChartPanel chartPanel = new ChartPanel(chart, true);
		return chartPanel;
	}

	// return frequency panel
	private ChartPanel getChartPanel(XYDataset dataSet, XYDataset dataSet1,
			XYDataset dataSet2, String key) {
		if (lineType.getValue().equals("step")) {
			chart = ChartFactory.createXYStepChart(null, key, null, dataSet,
					PlotOrientation.VERTICAL, true, true, false);
		} else {
			chart = ChartFactory.createTimeSeriesChart(null, key, null,
					dataSet, true, true, false);
		}

		XYPlot plot = chart.getXYPlot();
		NumberAxis rangeAxis1 = (NumberAxis) plot.getRangeAxis();
		rangeAxis1.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		rangeAxis1.setLowerMargin(0.40);

		rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		XYItemRenderer rederer1, rederer2;
		if (lineType.getValue().equals("step")) {
			rederer1 = new XYStepRenderer();
			rederer2 = new XYStepRenderer();
		} else {
			rederer1 = new XYLineAndShapeRenderer(true, false);
			rederer2 = new XYLineAndShapeRenderer(true, false);
		}
		plot.setDataset(1, dataSet1);
		rederer1.setBasePaint(Color.blue);
		plot.setRenderer(1, rederer1);
		plot.setDataset(2, dataSet2);
		rederer2.setBasePaint(Color.green);
		plot.setRenderer(2, rederer2);

		ChartPanel chartPanel = new ChartPanel(chart, true);
		return chartPanel;
	}

	// Drawing Option Panel
	// //////////////////////////////////////////////////////////////////////////
	protected JPanel getOptionPanel() {
		initGUI();
		if (performanceUI.isConsiderWorkingHour()) {
			startTime = new GUIPropertyInteger("Start time:", performanceUI
					.getStartHour(), 0, 24, this);
			endTime = new GUIPropertyInteger("End time:", performanceUI
					.getEndHour(), 0, 24, this);
		} else {
			startTime = new GUIPropertyInteger("Start time:", 0, 0, 24, this);
			endTime = new GUIPropertyInteger("End time:", 24, 0, 24, this);
		}
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		if (absPerformance != null
				&& !absPerformance.getName().equals(
						InstancePerformance.getNameCode())) {
			panel.add(dim1Sort.getPropertyPanel());
		}
		panel.add(timeUnitSort.getPropertyPanel());
		panel.add(measSort.getPropertyPanel());
		panel.add(lineType.getPropertyPanel());
		panel.add(startTime.getPropertyPanel());
		panel.add(endTime.getPropertyPanel());
		panel.setBackground(colorBg);
		return panel;
	}

	public void updatePerformance() {
		if (absPerformance instanceof AbstractPerformance2D) {

			if (dim1Sort.getValue().equals("overall")
					|| dim1Sort.getValue().equals("task")) {
				absPerformance = performanceUI
						.getPerformance(InstanceTaskTPerformance.getNameCode());
				tsMap = ((AbstractPerformance2D) absPerformance)
						.getSecondTaskSeriesMap();
			}
			if (dim1Sort.getValue().equals("instance")) {
				absPerformance = performanceUI
						.getPerformance(InstanceTaskTPerformance.getNameCode());
				if (absPerformance != null) {
					tsMap = ((AbstractPerformance2D) absPerformance)
							.getFirstTaskSeriesMap();
				} else {
					absPerformance = performanceUI
							.getPerformance(InstOriTPerformance.getNameCode());
					tsMap = ((AbstractPerformance2D) absPerformance)
							.getFirstTaskSeriesMap();
				}
			}
			if (dim1Sort.getValue().equals("originator")) {
				absPerformance = performanceUI
						.getPerformance(InstOriTPerformance.getNameCode());
				tsMap = ((AbstractPerformance2D) absPerformance)
						.getSecondTaskSeriesMap();
			}
		} else {
			tsMap = absPerformance.getFirstTaskSeriesMap();
		}
	}
}
