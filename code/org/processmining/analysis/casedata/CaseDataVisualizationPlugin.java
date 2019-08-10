package org.processmining.analysis.casedata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.processmining.analysis.Analyzer;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.NaturalSort;

public class CaseDataVisualizationPlugin {

	private static final long serialVersionUID = -6992662065030532069L;
	private AxisSettings xAxis;
	private AxisSettings yAxis;
	private JTable table;
	private VisTableModel data;
	private JPanel chartPanel;
	private JComboBox coloring;

	@Analyzer(name = "Case data visualization", names = { "Log" })
	public JComponent analyse(LogReader log) {
		JPanel controls = new JPanel(new BorderLayout());
		JTabbedPane axisControls = new JTabbedPane();
		JPanel colorControls = new JPanel(new GridLayout(0, 1));
		JPanel buttonControls = new JPanel();
		Vector<String> attributes = getAttributes(log);

		if (attributes.isEmpty()) {
			JOptionPane.showMessageDialog(MainUI.getInstance(),
					"The log does not contain any case data attributes, "
							+ "so there is nothing to visualize.");
			return null;
		}

		data = new VisTableModel(log);
		table = new JTable(data);
		chartPanel = new JPanel(new BorderLayout());

		JPanel main = new ResultComponent(data);

		xAxis = new AxisSettings(this, true, attributes);
		yAxis = new AxisSettings(this, false, attributes);

		coloring = new JComboBox(new String[] {
				MyPaintScale.LINEAR_BLACK_TO_RED,
				MyPaintScale.LINEAR_GREEN_TO_RED,
				MyPaintScale.BINNED_BLACK_TO_RED,
				MyPaintScale.BINNED_GREEN_TO_RED });

		colorControls.add(coloring);
		colorControls.setBorder(BorderFactory
				.createTitledBorder("Color settings"));

		axisControls.addTab("X axis", xAxis);
		axisControls.addTab("Y axis", yAxis);

		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		buttonControls.add(updateButton);

		JPanel buttonsAndColors = new JPanel(new BorderLayout());
		buttonsAndColors.add(colorControls, BorderLayout.NORTH);
		buttonsAndColors.add(buttonControls, BorderLayout.SOUTH);

		controls.add(axisControls, BorderLayout.NORTH);
		controls.add(buttonsAndColors, BorderLayout.SOUTH);

		JScrollPane controlsScroll = new JScrollPane(controls);
		controlsScroll.setPreferredSize(new Dimension(220, 200));

		JScrollPane tableScroll = new JScrollPane(table);
		tableScroll.setPreferredSize(new Dimension(220, 200));

		JScrollPane chartScroll = new JScrollPane(chartPanel);
		chartScroll.setPreferredSize(new Dimension(220, 200));

		JSplitPane splitLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				controlsScroll, chartScroll);
		JSplitPane splitRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				splitLeft, tableScroll);
		splitRight.setResizeWeight(1);

		main.add(splitRight, BorderLayout.CENTER);

		update();

		return main;
	}

	private Vector<String> getAttributes(LogReader log) {
		Set<String> attributes = new HashSet<String>();

		for (ProcessInstance pi : log.getInstances()) {
			attributes.addAll(pi.getAttributes().keySet());
		}

		Vector<String> result = new Vector<String>();
		result.addAll(attributes);
		Collections.sort(result);
		return result;
	}

	void update() {
		data.update(xAxis.getAttributeName(), xAxis.isDiscreteAttribute(),
				xAxis.getMin(), xAxis.getMax(), xAxis.getStep(), xAxis
						.getBuckets(), xAxis.isRelative(), xAxis.getTop(),
				xAxis.isSortedAlphabetically(), yAxis.getAttributeName(), yAxis
						.isDiscreteAttribute(), yAxis.getMin(), yAxis.getMax(),
				yAxis.getStep(), yAxis.getBuckets(), yAxis.isRelative(), yAxis
						.getTop(), yAxis.isSortedAlphabetically());
		DefaultTableCellRenderer renderer = new MyCellRenderer(data);

		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		for (int i = 1; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}

		chartPanel.removeAll();
		chartPanel.add(new ChartPanel(createChart(createDataset(data))));

		chartPanel.revalidate();
		chartPanel.repaint();
		table.revalidate();
		table.repaint();
	}

	private JFreeChart createChart(XYZDataset xyzdataset) {
		double min = data.getMinValue();
		double max = data.getMaxValue();

		if (min >= max) {
			max = min + 1;
		}

		SymbolAxis numberaxis = new SymbolAxis(xAxis.getAttributeName(), data
				.getColumnNames());
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		numberaxis.setLowerMargin(0.0D);
		numberaxis.setUpperMargin(0.0D);
		numberaxis.setAxisLinePaint(Color.white);
		numberaxis.setTickMarkPaint(Color.white);
		numberaxis.setVerticalTickLabels(true);

		NumberAxis numberaxis1 = new SymbolAxis(yAxis.getAttributeName(), data
				.getRowNames());
		numberaxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		numberaxis1.setLowerMargin(0.0D);
		numberaxis1.setUpperMargin(0.0D);
		numberaxis1.setAxisLinePaint(Color.white);
		numberaxis1.setTickMarkPaint(Color.white);
		numberaxis1.setInverted(true);

		XYBlockRenderer xyblockrenderer = new XYBlockRenderer();
		MyPaintScale graypaintscale = new MyPaintScale(min, max,
				(String) coloring.getSelectedItem(), data);
		MyPaintScale graypaintscale2 = new MyPaintScale(min, max,
				(String) coloring.getSelectedItem(), data);
		xyblockrenderer.setPaintScale(graypaintscale);

		XYPlot xyplot = new XYPlot(xyzdataset, numberaxis, numberaxis1,
				xyblockrenderer);
		xyplot.setBackgroundPaint(Color.lightGray);
		xyplot.setDomainGridlinesVisible(false);
		xyplot.setRangeGridlinePaint(Color.white);
		xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
		xyplot.setOutlinePaint(Color.blue);

		JFreeChart jfreechart = new JFreeChart(null, xyplot);
		jfreechart.removeLegend();

		NumberAxis numberaxis2 = new NumberAxis("Scale");
		numberaxis2.setAxisLinePaint(Color.white);
		numberaxis2.setTickMarkPaint(Color.white);
		numberaxis2.setLowerBound(min);
		numberaxis2.setUpperBound(max);
		numberaxis2.setTickLabelFont(new Font("Dialog", 0, 7));

		PaintScaleLegend paintscalelegend = new PaintScaleLegend(
				graypaintscale2, numberaxis2);
		paintscalelegend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		paintscalelegend.setAxisOffset(5D);
		paintscalelegend.setMargin(new RectangleInsets(5D, 5D, 5D, 5D));
		paintscalelegend.setPadding(new RectangleInsets(10D, 10D, 10D, 10D));
		paintscalelegend.setStripWidth(10D);
		paintscalelegend.setPosition(RectangleEdge.RIGHT);
		paintscalelegend.setBackgroundPaint(new Color(120, 120, 180));

		jfreechart.addSubtitle(paintscalelegend);
		jfreechart.setBackgroundPaint(new Color(180, 180, 250));

		return jfreechart;
	}

	private XYZDataset createDataset(final VisTableModel data) {
		return new XYZDataset() {
			public int getSeriesCount() {
				return 1;
			}

			public int getItemCount(int i) {
				return cols() * data.getRowCount();
			}

			public Number getX(int i, int j) {
				return new Double(getXValue(i, j));
			}

			public Number getY(int i, int j) {
				return new Double(getYValue(i, j));
			}

			public Number getZ(int i, int j) {
				return new Double(getZValue(i, j));
			}

			public double getXValue(int i, int j) {
				return j % cols();
			}

			public double getYValue(int i, int j) {
				return j / cols();
			}

			public double getZValue(int i, int j) {
				return (Double) data.getValueAt(j / cols(), j % cols() + 1);
			}

			public Comparable getSeriesKey(int i) {
				return "Case data";
			}

			private int cols() {
				return data.getColumnCount() - 1;
			}

			public void addChangeListener(
					DatasetChangeListener datasetchangelistener) {
			}

			public void removeChangeListener(
					DatasetChangeListener datasetchangelistener) {
			}

			public DatasetGroup getGroup() {
				return null;
			}

			public void setGroup(DatasetGroup datasetgroup) {
			}

			public int indexOf(Comparable comparable) {
				return 0;
			}

			public DomainOrder getDomainOrder() {
				return DomainOrder.ASCENDING;
			}
		};
	}

}

class MyPaintScale implements PaintScale {

	public final static String BINNED_BLACK_TO_RED = "Binned black to red";
	public final static String BINNED_GREEN_TO_RED = "Binned blue to white";
	public final static String LINEAR_BLACK_TO_RED = "Linear black to red";
	public final static String LINEAR_GREEN_TO_RED = "Linear blue to white";

	private double lower;
	private double upper;
	private String type;
	private double[] bins;

	public MyPaintScale(double lower, double upper, String type,
			VisTableModel data) {
		this.lower = lower;
		this.upper = upper;
		this.type = type;

		if (type.startsWith("Binned")) {
			Set<Double> values = new HashSet<Double>();
			for (int col = 1; col < data.getColumnCount(); col++) {
				for (int row = 0; row < data.getRowCount(); row++) {
					values.add((Double) data.getValueAt(row, col));
				}
			}
			List<Double> sortedValues = new ArrayList<Double>(values);
			Collections.sort(sortedValues);

			bins = new double[256];
			for (int i = 0; i < 256; i++) {
				int index = Math.max(0, Math.min(sortedValues.size(),
						(int) (((double) i / 256.0) * sortedValues.size())));
				bins[i] = sortedValues.get(index);
			}
		}
	}

	public double getLowerBound() {
		return lower;
	}

	public Paint getPaint(double value) {
		int g;

		if (type.equals(LINEAR_BLACK_TO_RED)) {
			g = Math.max(0, Math.min(255, (int) ((value - lower)
					/ (upper - lower) * 255.0)));
			return new Color(g, 0, 0);
		} else if (type.equals(LINEAR_GREEN_TO_RED)) {
			g = Math.max(0, Math.min(255, (int) ((value - lower)
					/ (upper - lower) * 255.0)));
			return new Color(255 - g, 255 - g, 255);
		} else if (type.equals(BINNED_BLACK_TO_RED)) {
			g = Arrays.binarySearch(bins, value);
			if (g < 0) {
				g = -g + 1;
			}
			g = Math.max(0, Math.min(255, g));
			return new Color(g, 0, 0);
		} else if (type.equals(BINNED_GREEN_TO_RED)) {
			g = Arrays.binarySearch(bins, value);
			if (g < 0) {
				g = -g + 1;
			}
			g = Math.max(0, Math.min(255, g));
			return new Color(255 - g, 255 - g, 255);
		} else {
			assert (false);
			return Color.BLACK;
		}
	}

	public double getUpperBound() {
		return upper;
	}

}

class ResultComponent extends JPanel implements Provider {

	private static final long serialVersionUID = -7334275711386339567L;
	private VisTableModel data;

	public ResultComponent(VisTableModel data) {
		this.data = data;
		setLayout(new BorderLayout());
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("Matrix", data) };
	}
}

class VisTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -3418662822163035492L;

	private static final Double ZERO = new Double(0.0);

	private List<String> colNames;
	private List<String> rowNames;
	private Map<String, Map<String, Double>> values;
	private LogReader log;
	private double minValue;
	private double maxValue;

	public VisTableModel(LogReader log) {
		this.log = log;
		colNames = new ArrayList<String>();
		rowNames = new ArrayList<String>();
		values = new HashMap<String, Map<String, Double>>();
		minValue = ZERO;
		maxValue = ZERO;
	}

	public String[] getColumnNames() {
		return colNames.toArray(new String[0]);
	}

	public String[] getRowNames() {
		return rowNames.toArray(new String[0]);
	}

	public int getColumnCount() {
		return colNames.size() + 1;
	}

	public int getRowCount() {
		return rowNames.size();
	}

	public String getColumnName(int col) {
		return col == 0 ? "" : colNames.get(col - 1);
	}

	public Object getValueAt(int row, int col) {
		if (col == 0) {
			// return a row name
			return rowNames.get(row);
		} else {
			// return a value in the table
			Map<String, Double> rowValues = values.get(rowNames.get(row));

			if (rowValues == null) {
				return ZERO;
			} else {
				Double value = rowValues.get(colNames.get(col - 1));
				return value == null ? ZERO : (Double) value;
			}
		}
	}

	public void update(String xAttr, boolean xDiscrete, Double xMin,
			Double xMax, Double xStep, Double xBuckets, boolean xRelative,
			Double xTop, boolean xSortAlpha, String yAttr, boolean yDiscrete,
			Double yMin, Double yMax, Double yStep, Double yBuckets,
			boolean yRelative, Double yTop, boolean ySortAlpha) {

		if (!xDiscrete || !yDiscrete) {
			boolean needXmin = xMin == null;
			boolean needXmax = xMax == null;
			boolean needYmin = yMin == null;
			boolean needYmax = yMax == null;

			for (ProcessInstance pi : log.getInstances()) {
				try {
					Double x = Double
							.parseDouble(pi.getAttributes().get(xAttr));
					if (needXmin) {
						xMin = xMin == null ? x : Math.min(xMin, x);
					}
					if (needXmax) {
						xMax = xMax == null ? x : Math.max(xMax, x);
					}
				} catch (Throwable t) {
				}
				try {
					Double y = Double
							.parseDouble(pi.getAttributes().get(yAttr));
					if (needYmin) {
						yMin = yMin == null ? y : Math.min(yMin, y);
					}
					if (needYmax) {
						yMax = yMax == null ? y : Math.max(yMax, y);
					}
				} catch (Throwable t) {
				}
			}
		}

		Set<String> rows = new HashSet<String>();
		Set<String> cols = new HashSet<String>();

		minValue = ZERO;
		maxValue = ZERO;
		values.clear();

		for (ProcessInstance pi : log.getInstances()) {
			String xKey = getKey(xDiscrete, xMin, xMax, xStep, xBuckets, pi
					.getAttributes().get(xAttr));
			String yKey = getKey(yDiscrete, yMin, yMax, yStep, yBuckets, pi
					.getAttributes().get(yAttr));

			if (xKey != null) {
				cols.add(xKey);
			}
			if (yKey != null) {
				rows.add(yKey);
			}

			if (xKey != null && yKey != null) {
				Map<String, Double> rowValues = values.get(yKey);

				if (rowValues == null) {
					rowValues = new HashMap<String, Double>();
					values.put(yKey, rowValues);
				}

				Double v = rowValues.get(xKey);

				if (v == null) {
					v = 0.0;
				}
				double newValue = v + 1.0;
				rowValues.put(xKey, newValue);

				minValue = Math.min(minValue, newValue);
				maxValue = Math.max(maxValue, newValue);
			}
		}

		colNames.clear();
		colNames.addAll(cols);
		rowNames.clear();
		rowNames.addAll(rows);

		if (xSortAlpha) {
			Collections.sort(colNames, NaturalSort.getNaturalComparator());
		} else {
			Collections.sort(colNames, new Comparator<String>() {
				public int compare(String o1, String o2) {
					double s1 = sum(o1);
					double s2 = sum(o2);

					return (int) (s2 - s1);
				}

				private double sum(String o1) {
					double total = 0;
					for (Map<String, Double> row : values.values()) {
						Double value = row.get(o1);
						total += value == null ? 0 : value;
					}
					return total;
				}
			});
		}

		if (ySortAlpha) {
			Collections.sort(rowNames, NaturalSort.getNaturalComparator());
		} else {
			Collections.sort(rowNames, new Comparator<String>() {
				public int compare(String o1, String o2) {
					Map<String, Double> r1 = values.get(o1);
					Map<String, Double> r2 = values.get(o2);
					double s1 = sum(r1);
					double s2 = sum(r2);

					return (int) (s2 - s1);
				}

				private double sum(Map<String, Double> r1) {
					double total = 0;
					if (r1 == null)
						return 0;
					for (Double value : r1.values()) {
						total += value;
					}
					return total;
				}
			});
		}

		if (xDiscrete && xTop != null) {
			colNames = colNames.subList(0, Math.min(colNames.size(), xTop
					.intValue()));
		}
		if (yDiscrete && yTop != null) {
			rowNames = rowNames.subList(0, Math.min(rowNames.size(), yTop
					.intValue()));
		}

		if (xRelative) {
			makeRelative(rowNames, colNames);
		} else if (yRelative) {
			// makeRelative(colNames, rowNames);
		}

		fireTableStructureChanged();
	}

	private void makeRelative(List<String> first, List<String> second) {
		minValue = ZERO;
		maxValue = ZERO;
		for (String firstKey : first) {
			Map<String, Double> m = values.get(firstKey);
			double total = ZERO;

			if (m != null) {
				for (String secondKey : second) {
					Double v = m.get(secondKey);

					if (v != null) {
						total += v;
					}
				}
				for (String secondKey : second) {
					Double v = m.get(secondKey);

					if (v != null) {
						double newValue = (total <= 0 ? ZERO : v / total
								* 100.0);

						m.put(secondKey, newValue);
						minValue = Math.min(minValue, newValue);
						maxValue = Math.max(maxValue, newValue);
					}
				}
			}

		}
	}

	private String getKey(boolean discrete, Double min, Double max,
			Double step, Double buckets, String value) {
		if (discrete) {
			return value;
		} else {
			Double d = null;

			try {
				d = Double.parseDouble(value);
			} catch (Throwable t) {
				return null;
			}

			if (d == null || min == null || max == null || d < min || d > max) {
				return null;
			}

			if (buckets == null) {
				if (step != null) {
					buckets = (max - min) / step;
				} else {
					buckets = 10.0;
				}
			}
			double range = (max - min) / buckets;
			int bucket = range <= 0.0 ? 0 : (int) Math.floor((d - min) / range);
			return "" + (min + bucket * range) + " - "
					+ (min + (bucket + 1) * range);
		}
	}

	public double getRatio(double value) {
		double range = maxValue - minValue;

		assert (range <= 0.0 || (minValue - 0.000001 <= value && value <= maxValue + 0.000001));
		return range <= 0.0 ? 0.0 : (value - minValue) / range;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}
}

class MyCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -8540474103226881230L;
	private VisTableModel model;

	public MyCellRenderer(VisTableModel model) {
		this.model = model;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		Double d = (Double) value;
		int color = Math.max(0, Math.min(0xFF, (int) Math.round(model
				.getRatio(d) * 0xFF)));

		setText("" + d);
		setForeground(table.getForeground());
		setBackground(new Color(0xFF - color, 0xFF, 0xFF - color));
		return this;
	}
}

class AxisSettings extends JPanel {

	private static final long serialVersionUID = -5622638107029263720L;

	private JRadioButton isDiscrete = new JRadioButton("Discrete (string)");
	private JRadioButton isContinuous = new JRadioButton("Continuous (number)");
	private JRadioButton sortAlphabetical = new JRadioButton("Attribute name");
	private JRadioButton sortNumerical = new JRadioButton(
			"Sum of attribute values");
	private JCheckBox isRelativeChk = new JCheckBox("Use percentages of total");
	private JComboBox attributeList = null;
	private JTextField contMin;
	private JTextField contMax;
	private JTextField contStep;
	private JTextField contBuckets;
	private JTextField discrMax;

	public AxisSettings(final CaseDataVisualizationPlugin parent,
			final boolean isX, Vector<String> attributes) {
		JPanel typePanel = new JPanel(new GridLayout(0, 1));
		JPanel discrMaxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel contMinPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel contMaxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel contStepPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel contBucketPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel sortPanel = new JPanel(new GridLayout(0, 1));

		discrMax = new JTextField("", 10);

		contMin = new JTextField("", 10);
		contMax = new JTextField("", 10);
		contStep = new JTextField("", 10);
		contBuckets = new JTextField("10", 10);

		discrMaxPanel.add(new JLabel("Top #:"));
		discrMaxPanel.add(discrMax);

		contMinPanel.add(new JLabel("Min:"));
		contMinPanel.add(contMin);
		contMaxPanel.add(new JLabel("Max:"));
		contMaxPanel.add(contMax);
		contBucketPanel.add(new JLabel("Buckets:"));
		contBucketPanel.add(contBuckets);
		contStepPanel.add(new JLabel("Step:"));
		contStepPanel.add(contStep);

		attributeList = new JComboBox(attributes);

		ButtonGroup group = new ButtonGroup();
		group.add(isDiscrete);
		group.add(isContinuous);
		isDiscrete.setSelected(true);
		typePanel.add(isDiscrete);
		typePanel.add(discrMaxPanel);
		typePanel.add(isContinuous);
		typePanel.add(contMinPanel);
		typePanel.add(contMaxPanel);
		typePanel.add(contBucketPanel);
		typePanel.add(contStepPanel);
		if (isX) {
			// not implemented for Y axis
			typePanel.add(isRelativeChk);
		}
		typePanel.setBorder(BorderFactory
				.createTitledBorder("Type of attribute"));

		ButtonGroup sortGroup = new ButtonGroup();
		sortGroup.add(sortAlphabetical);
		sortGroup.add(sortNumerical);
		sortNumerical.setSelected(true);
		sortPanel.add(sortNumerical);
		sortPanel.add(sortAlphabetical);
		sortPanel.setBorder(BorderFactory.createTitledBorder("Sort "
				+ (isX ? "columns" : "rows") + "  on"));

		JPanel attributeListPanel = new JPanel();
		attributeListPanel.add(attributeList);
		attributeListPanel.setBorder(BorderFactory
				.createTitledBorder("Attribute"));

		this.setLayout(new BorderLayout());
		this.add(attributeListPanel, BorderLayout.NORTH);
		this.add(typePanel, BorderLayout.CENTER);
		this.add(sortPanel, BorderLayout.SOUTH);
		this.setBorder(null);
	}

	public Double getBuckets() {
		return getValue(contBuckets);
	}

	public Double getStep() {
		return getValue(contStep);
	}

	public Double getMin() {
		return getValue(contMin);
	}

	public Double getMax() {
		return getValue(contMax);
	}

	private Double getValue(JTextField field) {
		return field.getText() == null || field.getText().equals("") ? null
				: Double.parseDouble(field.getText());
	}

	public String getAttributeName() {
		return (String) attributeList.getSelectedItem();
	}

	public boolean isDiscreteAttribute() {
		return isDiscrete.isSelected();
	}

	public boolean isSortedAlphabetically() {
		return sortAlphabetical.isSelected();
	}

	public Double getTop() {
		return getValue(discrMax);
	}

	public boolean isRelative() {
		return isRelativeChk.isSelected();
	}
}
