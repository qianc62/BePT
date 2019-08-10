package org.processmining.analysis.performance.basicperformance.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.time.Day;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.gantt.Task;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleAnchor;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.axis.DateTickUnit;

import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.analysis.performance.basicperformance.model.task.InstOriTPerformance;
import org.processmining.analysis.performance.basicperformance.model.task.InstanceTaskTPerformance;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

public class XYBlockHourChart extends AbstractChart implements
		GuiNotificationTarget {

	protected double max = Double.MIN_VALUE;
	protected double[] maxArray;
	protected static GUIPropertyListEnumeration colorSort;
	protected Date startDateOverall = null;
	protected Date endDateOverall = null;
	protected ArrayList<DefaultXYZDataset> datasetList = null;
	protected HashMap<String, TaskSeries> tsMap;
	protected GUIPropertyListEnumeration measSort = null;
	protected boolean[] holidays;

	public XYBlockHourChart() {
		super("Day-Hour Chart", "Time Block Chart");
		initGUI();
	}

	public XYBlockHourChart(BasicPerformanceAnalysisUI pm) {
		super("Day-Hour Chart", "Time Block Chart", pm);
		initGUI();
	}

	public void initGUI() {
		ArrayList<String> measList = new ArrayList<String>();
		measList.add("time");
		measList.add("frequency");
		measSort = new GUIPropertyListEnumeration("Measure:", "", measList,
				this, 150);
		ArrayList<String> colorList = new ArrayList<String>();
		colorList.add("gray");
		colorList.add("red");
		colorList.add("blue");
		colorList.add("yellow");
		colorSort = new GUIPropertyListEnumeration("Color base:", "",
				colorList, this, 150);
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
		dim1Sort = new GUIPropertyListEnumeration("Dim:", "", displayList,
				this, 150);

	}

	private ArrayList<DefaultXYZDataset> getXYBlockChartDatasetList(
			HashMap<String, TaskSeries> tsMap) {

		ArrayList<DefaultXYZDataset> datasetList = new ArrayList<DefaultXYZDataset>();
		DefaultXYZDataset datasetOverall = new DefaultXYZDataset();
		datasetList.add(datasetOverall);
		maxArray = new double[tsMap.keySet().size() + 1];
		maxArray[0] = Double.MIN_VALUE;
		int k = 0;

		if (performanceUI.isConsiderHoliday()) {
			holidays = performanceUI.getHolidayArray();
		}
		int startTime = 0;
		int endTime = 23;
		if (performanceUI.isConsiderWorkingHour()) {
			startTime = performanceUI.getStartHour();
			endTime = performanceUI.getEndHour();
		}

		for (String key : tsMap.keySet()) {
			// for the individual dataset
			Date startDate = null;
			Date endDate = null;
			maxArray[k + 1] = Double.MIN_VALUE;

			TaskSeries ts = tsMap.get(key);
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

			int duration;
			if (endDate != null && startDate != null) {
				duration = (int) ((endDate.getTime() / 86400000L - startDate
						.getTime() / 86400000L)) + 1;
			} else {
				duration = -1;
			}

			if (duration == -1)
				continue;

			double[] xvalues = new double[duration * 24];
			double[] yvalues = new double[duration * 24];
			double[] zvalues = new double[duration * 24];

			RegularTimePeriod t = new Day(startDate);
			for (int days = 0; days < duration; days++) {
				for (int hour = 0; hour < 24; hour++) {
					xvalues[days * 24 + hour] = t.getFirstMillisecond();
					yvalues[days * 24 + hour] = hour;
					zvalues[days * 24 + hour] = 0.0;
				}
				t = t.next();
			}

			for (int i = 0; i < ts.getTasks().size(); i++) {
				Task task = ts.get(i);
				SimpleTimePeriod stp = (SimpleTimePeriod) task.getDuration();

				int day1 = (int) ((stp.getStart().getTime() / 86400000L) - (startDate
						.getTime() / 86400000L));
				int hour1 = (int) ((stp.getStart().getTime() - (stp.getStart()
						.getTime() / 86400000L) * 86400000L) / 3600000L);
				double time1 = (double) (stp.getStart().getTime() - (stp
						.getStart().getTime() / 3600000L) * 3600000L)
						/ ((double) getTimeUnit());
				time1 = ((double) (3600000L - time1)) / (double) getTimeUnit();
				int day2 = (int) ((stp.getEnd().getTime() / 86400000L) - (startDate
						.getTime() / 86400000L));
				int hour2 = (int) ((stp.getEnd().getTime() - (stp.getEnd()
						.getTime() / 86400000L) * 86400000L) / 3600000L);
				double time2 = ((double) (stp.getEnd().getTime() - (stp
						.getEnd().getTime() / 3600000L) * 3600000L))
						/ ((double) getTimeUnit());

				zvalues[day1 * 24 + hour1] = zvalues[day1 * 24 + hour1] + time1;
				maxArray[k + 1] = Math.max(zvalues[day1 * 24 + hour1],
						maxArray[k + 1]);
				if ((day1 * 24 + hour1) != (day2 * 24 + hour2)) {
					zvalues[day2 * 24 + hour2] = zvalues[day2 * 24 + hour2]
							+ time2;
					maxArray[k + 1] = Math.max(zvalues[day2 * 24 + hour2],
							maxArray[k + 1]);
				}
				for (int j = (day1 * 24 + hour1) + 1; j < (day2 * 24 + hour2); j++) {
					if (!performanceUI.isConsiderHoliday() || holidays[j / 24]) {
						if ((j % 24) >= startTime && (j % 24) <= endTime) {
							zvalues[j] = zvalues[j]
									+ (3600000.0 / getTimeUnit());
							maxArray[k + 1] = Math.max(zvalues[j],
									maxArray[k + 1]);
						}
					}
				}
			}
			DefaultXYZDataset dataset = new DefaultXYZDataset();
			dataset
					.addSeries(key,
							new double[][] { xvalues, yvalues, zvalues });
			datasetList.add(dataset);
			k++;

			if (startDateOverall == null || startDateOverall.after(startDate)) {
				startDateOverall = startDate;
			}
			if (endDateOverall == null || endDateOverall.before(endDate)) {
				endDateOverall = endDate;
			}
		}

		int duration;
		if (endDateOverall != null && startDateOverall != null) {
			duration = (int) ((endDateOverall.getTime() / 86400000L - startDateOverall
					.getTime() / 86400000L)) + 1;
		} else {
			duration = -1;
		}
		if (duration == -1)
			return datasetList;
		double[] xvalues = new double[duration * 24];
		double[] yvalues = new double[duration * 24];
		double[] zvalues = new double[duration * 24];

		RegularTimePeriod t = new Day(startDateOverall);
		for (int days = 0; days < duration; days++) {
			for (int hour = 0; hour < 24; hour++) {
				xvalues[days * 24 + hour] = t.getFirstMillisecond();
				yvalues[days * 24 + hour] = hour;
				zvalues[days * 24 + hour] = 0.0;
			}
			t = t.next();
		}

		for (TaskSeries ts : tsMap.values()) {
			for (int i = 0; i < ts.getTasks().size(); i++) {
				Task task = ts.get(i);
				SimpleTimePeriod stp = (SimpleTimePeriod) task.getDuration();

				int day1 = (int) ((stp.getStart().getTime() / 86400000L) - (startDateOverall
						.getTime() / 86400000L));
				int hour1 = (int) ((stp.getStart().getTime() - (stp.getStart()
						.getTime() / 86400000L) * 86400000L) / 3600000L);
				double time1 = (double) (stp.getStart().getTime() - (stp
						.getStart().getTime() / 3600000L) * 3600000L)
						/ ((double) getTimeUnit());
				time1 = ((double) (3600000L - time1)) / (double) getTimeUnit();
				int day2 = (int) ((stp.getEnd().getTime() / 86400000L) - (startDateOverall
						.getTime() / 86400000L));
				int hour2 = (int) ((stp.getEnd().getTime() - (stp.getEnd()
						.getTime() / 86400000L) * 86400000L) / 3600000L);
				double time2 = ((double) (stp.getEnd().getTime() - (stp
						.getEnd().getTime() / 3600000L) * 3600000L))
						/ ((double) getTimeUnit());

				zvalues[day1 * 24 + hour1] = zvalues[day1 * 24 + hour1] + time1;
				maxArray[0] = Math.max(zvalues[day1 * 24 + hour1], maxArray[0]);
				if ((day1 * 24 + hour1) != (day2 * 24 + hour2)) {
					zvalues[day2 * 24 + hour2] = zvalues[day2 * 24 + hour2]
							+ time2;
					maxArray[0] = Math.max(zvalues[day2 * 24 + hour2],
							maxArray[0]);
				}
				for (int j = (day1 * 24 + hour1) + 1; j < (day2 * 24 + hour2); j++) {
					if (!performanceUI.isConsiderHoliday() || holidays[j / 24]) {
						if ((j % 24) >= startTime && (j % 24) <= endTime) {
							zvalues[j] = zvalues[j]
									+ (3600000.0 / getTimeUnit());
							maxArray[0] = Math.max(zvalues[j], maxArray[0]);
						}
					}
				}
			}
		}

		datasetOverall.addSeries("Overall", new double[][] { xvalues, yvalues,
				zvalues });

		return datasetList;
	}

	private ArrayList<DefaultXYZDataset> getXYBlockChartDatasetList_freq(
			HashMap<String, TaskSeries> tsMap) {

		ArrayList<DefaultXYZDataset> datasetList = new ArrayList<DefaultXYZDataset>();
		DefaultXYZDataset datasetOverall = new DefaultXYZDataset();
		datasetList.add(datasetOverall);
		maxArray = new double[tsMap.keySet().size() + 1];
		maxArray[0] = Double.MIN_VALUE;
		int k = 0;

		if (performanceUI.isConsiderHoliday()) {
			holidays = performanceUI.getHolidayArray();
		}
		int startTime = 0;
		int endTime = 23;
		if (performanceUI.isConsiderWorkingHour()) {
			startTime = performanceUI.getStartHour();
			endTime = performanceUI.getEndHour();
		}

		for (String key : tsMap.keySet()) {
			// for the individual dataset
			Date startDate = null;
			Date endDate = null;
			maxArray[k + 1] = Double.MIN_VALUE;

			TaskSeries ts = tsMap.get(key);
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

			int duration;
			if (endDate != null && startDate != null) {
				duration = (int) ((endDate.getTime() / 86400000L - startDate
						.getTime() / 86400000L)) + 1;
			} else {
				duration = -1;
			}

			if (duration == -1)
				continue;
			double[] xvalues = new double[duration * 24];
			double[] yvalues = new double[duration * 24];
			double[] zvalues = new double[duration * 24];

			RegularTimePeriod t = new Day(startDate);
			for (int days = 0; days < duration; days++) {
				for (int hour = 0; hour < 24; hour++) {
					xvalues[days * 24 + hour] = t.getFirstMillisecond();
					yvalues[days * 24 + hour] = hour;
					zvalues[days * 24 + hour] = 0.0;
				}
				t = t.next();
			}

			for (int i = 0; i < ts.getTasks().size(); i++) {
				Task task = ts.get(i);
				SimpleTimePeriod stp = (SimpleTimePeriod) task.getDuration();

				int day1 = (int) ((stp.getStart().getTime() / 86400000L) - (startDate
						.getTime() / 86400000L));
				int hour1 = (int) ((stp.getStart().getTime() - (stp.getStart()
						.getTime() / 86400000L) * 86400000L) / 3600000L);

				int day2 = (int) ((stp.getEnd().getTime() / 86400000L) - (startDate
						.getTime() / 86400000L));
				int hour2 = (int) ((stp.getEnd().getTime() - (stp.getEnd()
						.getTime() / 86400000L) * 86400000L) / 3600000L);
				for (int j = (day1 * 24 + hour1); j <= (day2 * 24 + hour2); j++) {
					if (!performanceUI.isConsiderHoliday() || holidays[j / 24]) {
						if ((j % 24) >= startTime && (j % 24) <= endTime) {
							zvalues[j] = zvalues[j] + 1.0;
							maxArray[k + 1] = Math.max(zvalues[j],
									maxArray[k + 1]);
						}
					}
				}
			}
			DefaultXYZDataset dataset = new DefaultXYZDataset();
			dataset
					.addSeries(key,
							new double[][] { xvalues, yvalues, zvalues });
			datasetList.add(dataset);
			k++;

			if (startDateOverall == null || startDateOverall.after(startDate)) {
				startDateOverall = startDate;
			}
			if (endDateOverall == null || endDateOverall.before(endDate)) {
				endDateOverall = endDate;
			}
		}

		int duration;
		if (endDateOverall != null && startDateOverall != null) {
			duration = (int) ((endDateOverall.getTime() / 86400000L - startDateOverall
					.getTime() / 86400000L)) + 1;
		} else {
			duration = -1;
		}
		if (duration == -1)
			return datasetList;
		double[] xvalues = new double[duration * 24];
		double[] yvalues = new double[duration * 24];
		double[] zvalues = new double[duration * 24];

		RegularTimePeriod t = new Day(startDateOverall);
		for (int days = 0; days < duration; days++) {
			for (int hour = 0; hour < 24; hour++) {
				xvalues[days * 24 + hour] = t.getFirstMillisecond();
				yvalues[days * 24 + hour] = hour;
				zvalues[days * 24 + hour] = 0.0;
			}
			t = t.next();
		}

		for (TaskSeries ts : tsMap.values()) {
			for (int i = 0; i < ts.getTasks().size(); i++) {
				Task task = ts.get(i);
				SimpleTimePeriod stp = (SimpleTimePeriod) task.getDuration();

				int day1 = (int) ((stp.getStart().getTime() / 86400000L) - (startDateOverall
						.getTime() / 86400000L));
				int hour1 = (int) ((stp.getStart().getTime() - (stp.getStart()
						.getTime() / 86400000L) * 86400000L) / 3600000L);

				int day2 = (int) ((stp.getEnd().getTime() / 86400000L) - (startDateOverall
						.getTime() / 86400000L));
				int hour2 = (int) ((stp.getEnd().getTime() - (stp.getEnd()
						.getTime() / 86400000L) * 86400000L) / 3600000L);
				for (int j = (day1 * 24 + hour1); j <= (day2 * 24 + hour2); j++) {
					if (!performanceUI.isConsiderHoliday() || holidays[j / 24]) {
						if ((j % 24) >= startTime && (j % 24) <= endTime) {
							zvalues[j] = zvalues[j] + 1.0;
							maxArray[0] = Math.max(zvalues[j], maxArray[0]);
						}
					}
				}
			}
		}

		datasetOverall.addSeries("Overall", new double[][] { xvalues, yvalues,
				zvalues });

		return datasetList;
	}

	// XY Block Chart
	// //////////////////////////////////////////////////////////////////////////////
	protected JScrollPane getGraphPanel() {
		max = Double.MIN_VALUE;
		updatePerformance();
		if (measSort.getValue().equals("time")) {
			datasetList = getXYBlockChartDatasetList(tsMap);
		} else {
			datasetList = getXYBlockChartDatasetList_freq(tsMap);
		}

		return drawGraphPanel();
	}

	protected JScrollPane drawGraphPanel() {
		JPanel tempPanel = new JPanel();
		if (dim1Sort.getValue().equals("overall")) {
			tempPanel.setLayout(new BorderLayout());
			tempPanel.add(getChartPanel(datasetList.get(0), 0));
		} else {
			GridLayout tempLayout = new GridLayout(0, 2);
			tempPanel.setLayout(tempLayout);
			for (int i = 1; i < datasetList.size(); i++) {
				tempPanel.add(getChartPanel(datasetList.get(i), i));
			}
			tempPanel.setPreferredSize(new java.awt.Dimension(300 * 2,
					230 * (datasetList.size() / 3 + 1)));
		}
		scrollPane = new JScrollPane(tempPanel);
		return scrollPane;
	}

	private ChartPanel getChartPanel(DefaultXYZDataset dataset, int i) {
		String key = (String) dataset.getSeriesKey(0);
		DateAxis yAxis = new DateAxis("Date");
		yAxis.setLowerMargin(0.0);
		yAxis.setUpperMargin(0.0);
		yAxis.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 1));

		NumberAxis xAxis = new NumberAxis("Hour");
		xAxis.setUpperMargin(0.0);
		xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		XYBlockRenderer renderer = new XYBlockRenderer();
		renderer.setBlockWidth(1000.0 * 60.0 * 60.0 * 24.0);
		renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);

		// range scaling
		PaintScale paintScale;
		if (colorSort.getValue().equals("gray")) // gray scaling
		{
			paintScale = new GrayPaintScale(0.0, maxArray[i]);
		} else {

			int color_max, color_min;
			if (colorSort.getValue().equals("red")) { // red scaling
				color_max = 0xFF33FF;
				color_min = 0xFF3300;
			} else if (colorSort.getValue().equals("blue")) { // blue scaling
				color_max = 0x0033FF;
				color_min = 0x003300;
			} else { // yellow scaling
				color_max = 0xFFFFFF;
				color_min = 0xFFFF00;
			}
			paintScale = new LookupPaintScale(0.0, maxArray[i], new Color(
					color_max));
			int d = Math.max((color_max - color_min) / ((int) maxArray[i] + 1),
					1);
			for (int k = 0; k <= maxArray[i]; k++) {
				((LookupPaintScale) paintScale).add(new Double(k + 0.5),
						new Color(color_max - (k + 1) * d));
			}
		}
		renderer.setPaintScale(paintScale);

		XYPlot plot = new XYPlot(dataset, yAxis, xAxis, renderer);
		plot.setOrientation(PlotOrientation.HORIZONTAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setForegroundAlpha(0.66F);
		JFreeChart chart = new JFreeChart(key, plot);
		chart.removeLegend();
		chart.setBackgroundPaint(Color.white);

		// adding data ranges
		NumberAxis scaleAxis = new NumberAxis(null);
		scaleAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		scaleAxis.setLowerBound(0.0);
		scaleAxis.setUpperBound(maxArray[i]);
		PaintScaleLegend psl = new PaintScaleLegend(paintScale, scaleAxis);
		psl.setAxisOffset(5.0);
		psl.setPosition(RectangleEdge.BOTTOM);
		psl.setMargin(new RectangleInsets(5, 5, 5, 5));
		chart.addSubtitle(psl);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		return chartPanel;
	}

	protected JPanel getOptionPanel() {
		initGUI();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		if (absPerformance instanceof AbstractPerformance2D) {
			panel.add(dim1Sort.getPropertyPanel());
		}
		panel.add(measSort.getPropertyPanel());
		panel.add(timeUnitSort.getPropertyPanel());
		panel.add(colorSort.getPropertyPanel());
		panel.setBackground(colorBg);
		return panel;
	}

	public void updateGUI() {
		splitPane.remove(scrollPane);
		JScrollPane jscrollPanel = getGraphPanel();
		jscrollPanel.setBackground(colorBg);
		splitPane.setRightComponent(jscrollPanel);
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

	protected long getTimeUnit() {
		if (timeUnitSort.getValue().equals("seconds")) {
			return 1000;
		} else if (timeUnitSort.getValue().equals("minutes")) {
			return 60000;
		} else if (timeUnitSort.getValue().equals("hours")) {
			return 3600000L;
		} else if (timeUnitSort.getValue().equals("days")) {
			return 86400000L;
		} else if (timeUnitSort.getValue().equals("weeks")) {
			return 604800000L;
		} else if (timeUnitSort.getValue().equals("months")) {
			return 2592000000L;
		} else {
			return 31536000000L;
		}
	}
}
