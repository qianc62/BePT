package org.processmining.analysis.petrinet.structuredness;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.processmining.framework.models.bpel.util.Triple;
import org.processmining.framework.ui.MainUI;

import weka.gui.arffviewer.FileChooser;

public class SpreadPanel extends JPanel implements ChangeListener,
		FocusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8556794770720038387L;

	private double minValue;

	private double maxValue;

	private final double maxValueInValues, minValueInValues;

	private int intervalCount;

	private double intervalSize;

	private final List<Triple<Double, Double, Integer>> spread = new ArrayList<Triple<Double, Double, Integer>>();

	private final List<Double> values;

	private final JSpinner spinner;

	private final JFormattedTextField minValueField, maxValueField,
			intervalField;

	private JScrollPane pane;

	public SpreadPanel(final String title, int intervalCount,
			final List<Double> values) {
		super(new BorderLayout());
		minValue = Double.MIN_VALUE;
		maxValue = Double.MAX_VALUE;
		minValue = getMinValue(maxValue, values);
		maxValue = getMaxValue(minValue, values);
		minValueInValues = minValue;
		maxValueInValues = maxValue;
		this.intervalCount = intervalCount;
		this.values = values;
		intervalSize = (this.minValue - this.maxValue) / intervalCount;

		generateSpread(true);

		pane = createSpreadChart();
		add(pane, BorderLayout.CENTER);

		JPanel bottomMain = new JPanel(new BorderLayout());
		JPanel bottom = new JPanel(new FlowLayout());
		bottomMain.add(bottom, BorderLayout.WEST);
		add(new JScrollPane(bottomMain), BorderLayout.SOUTH);
		SpinnerModel model = new SpinnerNumberModel(intervalCount, 1,
				Integer.MAX_VALUE, 1);
		spinner = new JSpinner(model);
		bottom = new JPanel(new FlowLayout());
		bottomMain.add(bottom, BorderLayout.WEST);
		bottom.add(new JLabel("Intervals: "));
		bottom.add(spinner);
		spinner.addChangeListener(this);
		intervalField = new JFormattedTextField(new DecimalFormat());
		intervalField.addFocusListener(this);
		intervalField.setValue(intervalSize);
		bottom.add(new JLabel("Interval size: "));
		bottom.add(intervalField);
		minValueField = new JFormattedTextField(new DecimalFormat());
		minValueField.addFocusListener(this);
		minValueField.setValue(minValue);
		bottom.add(new JLabel("Min value: "));
		bottom.add(minValueField);
		maxValueField = new JFormattedTextField(new DecimalFormat());
		maxValueField.setValue(maxValue);
		maxValueField.addFocusListener(this);
		bottom.add(new JLabel("Max value: "));
		bottom.add(maxValueField);

		JButton button = new JButton("Export raw data");
		bottom.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileChooser fc = new FileChooser();
				fc.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {
						return f.getAbsolutePath().endsWith(".csv");
					}

					@Override
					public String getDescription() {
						return "Comma-separated values (csv)";
					}
				});
				int returnVal = fc.showSaveDialog(MainUI.getInstance());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (!file.getName().endsWith(".csv"))
						file = new File(file.getAbsolutePath() + ".csv");
					try {
						BufferedWriter out = new BufferedWriter(new FileWriter(
								file));
						out.write(exportRawData(title, values));
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		button = new JButton("Copy raw data");
		bottom.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Clipboard clipboard = Toolkit.getDefaultToolkit()
						.getSystemClipboard();

				clipboard.setContents(new StringSelection(exportRawData(title,
						values)), null);
			}
		});
	}

	private static String exportRawData(String title, List<Double> values) {
		StringBuilder out = new StringBuilder();
		out.append(title + "\n");
		int count = 0;
		Double current = null;
		int last = 0;
		for (Double d : values) {
			for (int i = (int) last; i < d; i++)
				out.append("0\n");
			last = d.intValue();
			if (!d.equals(current)) {
				if (current != null)
					out.append(count + "\n");
				current = d;
				count = 0;
			}
			count++;
		}
		out.append(count + "\n");
		return out.toString();
	}

	private double getMinValue(double min, List<Double> values) {
		if (values.isEmpty())
			return min;
		double minFirst = Math.min(min, values.get(0));
		double maxLast = Math.max(minFirst, minValue);
		return getMinValue(maxLast, values.subList(1, values.size()));
	}

	private double getMaxValue(double max, List<Double> values) {
		if (values.isEmpty())
			return max;
		double head = values.get(0);
		double maxFirst = Math.max(max, head);
		double minLast = Math.min(maxFirst, maxValue);
		return getMaxValue(minLast, values.subList(1, values.size()));
	}

	private void generateSpread(boolean calculateIntervalSize) {
		spread.clear();
		double min = getMinValue(maxValue, values);
		double max = getMaxValue(minValue, values);
		if (min == max || min == maxValue) {
			if (min >= minValue && min <= maxValue) {
				spread.add(Triple.create(min, max, values.size()));
			} else {
				spread.add(Triple.create(0.0, 0.0, 0));
			}
			return;
		}
		if (calculateIntervalSize)
			intervalSize = (max - min) / intervalCount;
		else {
			intervalCount = (int) Math.ceil((max - min) / intervalSize);
		}
		if (min > minValueInValues) {
			int count = 0;
			for (double v : values)
				if (v < minValue)
					count++;
			spread.add(Triple
					.create(round(minValueInValues), round(min), count));
		}
		for (int i = 0; i < intervalCount; i++) {
			int count = 0;
			for (double v : values) {
				if (min + i * intervalSize <= v
						&& (v <= min + (i + 1) * intervalSize))
					count++;
			}
			spread.add(Triple.create(round(min + i * intervalSize), round(min
					+ (i + 1) * intervalSize), count));
		}
		if (maxValueInValues > max) {
			int count = 0;
			for (double v : values)
				if (v >= maxValue)
					count++;
			spread.add(Triple
					.create(round(max), round(maxValueInValues), count));
		}
	}

	private double round(double d) {
		return Math.round(d * 100) / (double) 100;
	}

	private JScrollPane createSpreadChart() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Triple<Double, Double, Integer> s : spread) {
			dataset.addValue(s.third, new Integer(0), s.first + "-" + s.second);
		}
		JFreeChart matches = ChartFactory.createBarChart("", // chart title
				"Intervals", // domain axis label
				"Occurrences", // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // orientation
				false, // include legend
				true, // tooltips?
				false // URLs?
				);
		matches.getCategoryPlot().getRenderer().setItemLabelGenerator(
				new CategoryItemLabelGenerator() {

					public String generateColumnLabel(CategoryDataset dataset,
							int column) {
						return null;
					}

					public String generateLabel(CategoryDataset dataset,
							int row, int column) {
						return "" + (spread.get(column).third);
					}

					public String generateRowLabel(CategoryDataset dataset,
							int row) {
						return null;
					}
				});
		matches.getCategoryPlot().getRenderer().setItemLabelsVisible(true);
		ChartPanel chartPanel = new ChartPanel(matches, false);
		return new JScrollPane(chartPanel);
	}

	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource() == spinner) {
			JSpinner spinner = (JSpinner) arg0.getSource();
			if (intervalCount != (Integer) spinner.getValue()) {
				intervalCount = (Integer) spinner.getValue();
				generateSpread(true);
				pane.setViewportView(createSpreadChart());
				intervalField.setValue(round(intervalSize));
				validate();
				repaint();
			}
		}
	}

	public void focusGained(FocusEvent arg0) {
	}

	public void focusLost(FocusEvent arg0) {
		if ((arg0.getSource() == minValueField && hasChanged(minValue,
				minValueField))
				|| (arg0.getSource() == maxValueField && hasChanged(maxValue,
						maxValueField))) {
			if (arg0.getSource() == minValueField) {
				try {
					minValueField.commitEdit();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				minValue = round(((Number) minValueField.getValue())
						.doubleValue());
			} else {
				try {
					maxValueField.commitEdit();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				maxValue = round(((Number) maxValueField.getValue())
						.doubleValue());
			}
			generateSpread(true);
			pane.setViewportView(createSpreadChart());
			spinner.setValue(intervalCount);
			validate();
			repaint();
		} else if (arg0.getSource() == intervalField
				&& hasChanged(intervalSize, intervalField)) {
			try {
				intervalField.commitEdit();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			intervalSize = round(((Number) intervalField.getValue())
					.doubleValue());
			generateSpread(false);
			pane.setViewportView(createSpreadChart());
			spinner.setValue(intervalCount);
			validate();
			repaint();
		}
	}

	private boolean hasChanged(double value, JFormattedTextField valueField) {
		try {
			valueField.commitEdit();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return value != ((Number) valueField.getValue()).doubleValue();
	}

}
