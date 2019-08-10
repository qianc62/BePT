package org.processmining.analysis.performance.basicperformance.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.awt.BasicStroke;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.deckfour.slickerbox.components.SmoothPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.DialShape;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.processmining.analysis.performance.basicperformance.model.instance.InstancePerformance;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class MeterChart extends AbstractChart implements ActionListener {

	protected JPanel optionPanel;
	protected JTextField minTextField;
	protected JTextField normalTextField;
	protected JTextField warningTextField;
	protected JButton updateButton;
	protected boolean bFirst;
	protected double minValue;
	protected double normalValue;
	protected double warningValue;
	protected double maxValue;

	public MeterChart() {
		super("Meter Chart", "Meter Chart");
		initRangeGUI();
	}

	public MeterChart(BasicPerformanceAnalysisUI pm) {
		super("Meter Chart", "Meter Chart", pm);
		initRangeGUI();
	}

	public void initRangeGUI() {
		minTextField = new JTextField("0.0", 20);
		normalTextField = new JTextField("0.0", 20);
		warningTextField = new JTextField("0.0", 20);
		updateButton = new JButton("Update");
		updateButton.setOpaque(false);
		bFirst = false;
	}

	public ChartPanel drawMeterPlot(String name, double value) {

		MeterPlot plot = new MeterPlot();
		plot.setDialShape(DialShape.PIE);
		plot.setUnits("time");
		plot.setRange(new Range(0, maxValue));
		getMinMaxValue();
		MeterInterval mInterval = new MeterInterval("Normal", new Range(
				minValue, normalValue), Color.lightGray, new BasicStroke(2.0f),
				new Color(0, 255, 0, 64));
		MeterInterval wInterval = new MeterInterval("warning", new Range(
				normalValue, warningValue), Color.lightGray, new BasicStroke(
				2.0f), new Color(255, 255, 0, 64));
		MeterInterval cInterval = new MeterInterval("critical", new Range(
				warningValue, maxValue), Color.lightGray,
				new BasicStroke(2.0f), new Color(255, 0, 0, 128));
		plot.setNeedlePaint(Color.darkGray);
		plot.setDialBackgroundPaint(Color.white);
		plot.setDialOutlinePaint(Color.gray);
		plot.setMeterAngle(260);
		plot.addInterval(mInterval);
		plot.addInterval(wInterval);
		plot.addInterval(cInterval);
		plot.setDataset(new DefaultValueDataset(value));
		plot.setTickSize(5.0);
		plot.setTickPaint(Color.lightGray);

		JFreeChart chart2 = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT,
				plot, false);
		ChartPanel chartPanel = new ChartPanel(chart2);
		chartPanel.setPreferredSize(new java.awt.Dimension(150, 180));
		return chartPanel;
	}

	// Meter Chart
	// //////////////////////////////////////////////////////////////////////////////
	public JScrollPane getPanel2(AbstractPerformance absPer) {
		SmoothPanel panel = new SmoothPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());

		List<String> items = absPer.getItems();

		JPanel jtempPanel = new JPanel();
		GridLayout tempLayout = new GridLayout(0, 3);
		jtempPanel.setLayout(tempLayout);

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		for (String aItem : items) {
			StatisticUnit su2;
			if (performanceSort.getValue().equals("Working Time")) {
				su2 = absPer.getExecutionTimeSU(aItem);
			} else if (performanceSort.getValue().equals("Waiting Time")) {
				su2 = absPer.getWaitingTimeSU(aItem);
			} else {
				su2 = absPer.getSojournTimeSU(aItem);
			}
			if (su2 != null && su2.getStatistics().getN() != 0) {
				min = Math.min(getMeasrueValue(su2.getStatistics()), min);
				max = Math.max(getMeasrueValue(su2.getStatistics()), max);
			}
		}
		maxValue = max;
		setMeasureValue(min, max);
		for (String aItem : items) {
			StatisticUnit su;
			if (performanceSort.getValue().equals("Working Time")) {
				su = absPer.getExecutionTimeSU(aItem);
			} else if (performanceSort.getValue().equals("Waiting Time")) {
				su = absPer.getWaitingTimeSU(aItem);
			} else {
				su = absPer.getSojournTimeSU(aItem);
			}
			if (su != null && su.getStatistics().getN() != 0) {
				ChartPanel chartPanel = drawMeterPlot(aItem, getMeasrueValue(su
						.getStatistics()));
				jtempPanel.add(chartPanel);
			}
		}
		scrollPane = new JScrollPane(jtempPanel);
		scrollPane.setBackground(colorBg);
		return scrollPane;
	}

	protected void setMeasureValue(double min, double max) {
		if (bFirst)
			return;
		minTextField.setText(String.valueOf(min));
		normalTextField.setText(String.valueOf(min + (max - min) * 0.6));
		warningTextField.setText(String.valueOf(min + (max - min) * 0.8));
		bFirst = false;
	}

	protected void getMinMaxValue() {
		warningValue = Double.valueOf(warningTextField.getText());
		if (warningValue > maxValue) {
			warningValue = maxValue;
			warningTextField.setText(String.valueOf(warningValue));
		}
		normalValue = Double.valueOf(normalTextField.getText());
		if (normalValue > warningValue) {
			normalValue = warningValue;
			normalTextField.setText(String.valueOf(normalValue));
		}
		minValue = Double.valueOf(minTextField.getText());
		if (minValue > normalValue) {
			minValue = normalValue;
			minTextField.setText(String.valueOf(minValue));
		}
	}

	protected double getMeasrueValue(DescriptiveStatistics ds) {
		String sort = (String) measureSort.getValue();
		double value;
		if (sort.equals("Minimum")) {
			value = ds.getMin();
		} else if (sort.equals("Average")) {
			value = ds.getMean();
		} else if (sort.equals("Median")) {
			value = ds.getPercentile(50);
		} else if (sort.equals("Maximum")) {
			value = ds.getMax();
		} else if (sort.equals("Sum")) {
			value = ds.getSum();
		} else if (sort.equals("StandDev")) {
			value = ds.getStandardDeviation();
		} else {
			value = (double) ds.getN();
		}
		return value / getTimeUnit();

	}

	protected JScrollPane getGraphPanel() {
		SmoothPanel panel = new SmoothPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());
		if (!(absPerformance instanceof AbstractPerformance2D)) {
			return getPanel2(absPerformance);
		}

		AbstractPerformance2D performance2D = (AbstractPerformance2D) absPerformance;
		List<String> items, items2;

		AbstractPerformance refPerformance = null;
		if (isItemA) {
			refPerformance = performance2D.getSecondRefPerformance();
			items = performance2D.getSecondItems();
			items2 = performance2D.getItems();
		} else {
			refPerformance = performance2D.getFirstRefPerformance();
			items = performance2D.getItems();
			items2 = performance2D.getSecondItems();
		}
		scrollPane = new JScrollPane(drawMeterPanel(items, items2));
		scrollPane.setBackground(colorBg);
		return scrollPane;
	}

	public JPanel drawMeterPanel(List<String> items, List<String> items2) {
		JPanel jtempPanel = new JPanel();
		GridLayout tempLayout = new GridLayout(0, 3);
		jtempPanel.setLayout(tempLayout);

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (String aItem : items) {
			for (String bItem : items2) {
				String key;
				if (isItemA)
					key = bItem + " - " + aItem;
				else
					key = aItem + " - " + bItem;
				StatisticUnit su;
				if (performanceSort.getValue().equals("Working Time")) {
					su = ((AbstractPerformance2D) absPerformance)
							.getExecutionTimeSU(key);
				} else {
					su = ((AbstractPerformance2D) absPerformance)
							.getWaitingTimeSU(key);
				}
				if (su != null && su.getStatistics().getN() != 0) {
					min = Math.min(getMeasrueValue(su.getStatistics()), min);
					max = Math.max(getMeasrueValue(su.getStatistics()), max);
				}
			}
		}
		minValue = min;
		maxValue = max;
		setMeasureValue(minValue, maxValue);
		for (String aItem : items) {
			for (String bItem : items2) {
				String key;
				if (isItemA)
					key = bItem + " - " + aItem;
				else
					key = aItem + " - " + bItem;
				StatisticUnit su;
				if (performanceSort.getValue().equals("Working Time")) {
					su = ((AbstractPerformance2D) absPerformance)
							.getExecutionTimeSU(key);
				} else {
					su = ((AbstractPerformance2D) absPerformance)
							.getWaitingTimeSU(key);
				}
				if (su != null && su.getStatistics().getN() != 0) {
					ChartPanel chartPanel = drawMeterPlot(
							aItem + " - " + bItem, getMeasrueValue(su
									.getStatistics()));
					jtempPanel.add(chartPanel);
				}
			}
		}
		return jtempPanel;
	}

	protected JPanel getOptionPanel() {
		initTimeSort();
		initDimSort();
		initRangeGUI();
		optionPanel = new JPanel();
		optionPanel.setBackground(colorBg);
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		if (!absPerformance.getName().equals(InstancePerformance.getNameCode())) {
			optionPanel.add(dim1Sort.getPropertyPanel());
			optionPanel.add(dim2Sort.getPropertyPanel());
		}
		optionPanel.add(timeUnitSort.getPropertyPanel());
		optionPanel.add(measureSort.getPropertyPanel());

		ArrayList<String> performanceList = new ArrayList<String>();
		if (absPerformance.getName().equals(InstancePerformance.getNameCode())) {
			performanceList.add("Sojourn Time");
		}
		performanceList.add("Working Time");
		performanceList.add("Waiting Time");
		performanceSort = new GUIPropertyListEnumeration("Performance Sort:",
				"", performanceList, this, 150);
		optionPanel.add(performanceSort.getPropertyPanel());

		// minimum
		JPanel panel1 = new JPanel();
		panel1.setPreferredSize(new Dimension(280, 25));
		panel1.setMaximumSize(new Dimension(280, 25));
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
		panel1.setOpaque(false);
		panel1.setBorder(BorderFactory.createEmptyBorder());
		JLabel myNameLabel = new JLabel(" Minimum ");
		myNameLabel.setOpaque(false);
		panel1.add(myNameLabel);
		panel1.add(Box.createRigidArea(new Dimension(72, 25)));
		panel1.add(Box.createHorizontalGlue());
		panel1.add(minTextField);
		optionPanel.add(panel1);
		// normal
		JPanel panel2 = new JPanel();
		panel2.setPreferredSize(new Dimension(280, 25));
		panel2.setMaximumSize(new Dimension(280, 25));
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.LINE_AXIS));
		panel2.setOpaque(false);
		panel2.setBorder(BorderFactory.createEmptyBorder());
		JLabel myNameLabel2 = new JLabel(" Upper value (normal) ");
		myNameLabel2.setOpaque(false);
		panel2.add(myNameLabel2);
		panel2.add(Box.createRigidArea(new Dimension(6, 25)));
		panel2.add(Box.createHorizontalGlue());
		panel2.add(normalTextField);
		optionPanel.add(panel2);
		// warning
		JPanel panel3 = new JPanel();
		panel3.setPreferredSize(new Dimension(280, 25));
		panel3.setMaximumSize(new Dimension(280, 25));
		panel3.setLayout(new BoxLayout(panel3, BoxLayout.LINE_AXIS));
		panel3.setOpaque(false);
		panel3.setBorder(BorderFactory.createEmptyBorder());
		JLabel myNameLabel3 = new JLabel(" Upper value (warning) ");
		myNameLabel3.setOpaque(false);
		panel3.add(myNameLabel3);
		panel3.add(Box.createHorizontalGlue());
		panel3.add(warningTextField);
		optionPanel.add(panel3);
		optionPanel.add(updateButton);
		updateButton.addActionListener(this);

		return optionPanel;
	}

	public void actionPerformed(ActionEvent e) {
		bFirst = true;
		updateGUI();
		bFirst = false;
	}
}
