package org.processmining.analysis.hierarchicaldatavisualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.TreeNode;

import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.models.DotFormatter;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ui.treecomponent.ModelGraphTreeComponent;
import org.processmining.framework.models.ui.treecomponent.NameProvider;
import org.processmining.framework.models.ui.treecomponent.SelectionChangeListener;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

public class HierarchicalDataVisualizationResult extends JComponent implements
		Provider {

	private static final long serialVersionUID = 3566339914787127535L;
	private static final int TABLE_WIDTH = 250;

	private List<ModelGraph> hierarchies;
	private List<Aggregator> aggregators;

	private JTabbedPane tabs;
	private List<JPanel> modelViews;
	private List<ModelGraphTreeComponent> filters;
	private List<Integer> coloringScheme;
	private List<Boolean> autoUpdate;
	private List<Integer> format;
	private HierarchicalData data;
	private JTable table;
	private DotFormatter dotFormatter;

	private HierarchyModel hierarchyModel;

	public HierarchicalDataVisualizationResult(HierarchicalData data) {
		this.data = data;
		this.dotFormatter = data.getDotFormatter();

		getRelevantHierarchies();

		if (hierarchies.size() == 0) {
			initEmptyUI();
		} else {
			initUI();
		}
	}

	private void getRelevantHierarchies() {
		Set<ModelGraph> usedHierarchies = new HashSet<ModelGraph>();
		final List<ModelGraph> exclude = data.graphsToExclude();

		for (HierarchicalDataElement element : data) {
			for (ModelGraphVertex node : element.getNodes()) {
				if (node != null) {
					usedHierarchies.add((ModelGraph) node.getGraph());
				}
			}
		}

		hierarchies = new ArrayList<ModelGraph>();
		for (ModelGraph hierarchy : usedHierarchies) {
			hierarchies.add(hierarchy);
		}
		Collections.sort(hierarchies, new Comparator<ModelGraph>() {
			public int compare(ModelGraph o1, ModelGraph o2) {
				boolean excludeO1 = exclude != null && exclude.contains(o1);
				boolean excludeO2 = exclude != null && exclude.contains(o2);

				if (excludeO1) {
					return excludeO2 ? 0 : 1;
				} else if (excludeO2) {
					return -1;
				} else {
					return o1.getIdentifier().compareTo(o2.getIdentifier());
				}
			}
		});
		aggregators = new ArrayList<Aggregator>();
		for (ModelGraph hierarchy : hierarchies) {
			aggregators.add(new Aggregator(hierarchy));
		}
	}

	private void initEmptyUI() {
		JPanel messagePanel = new JPanel();

		messagePanel.add(new JLabel("No "
				+ data.getPluralHierarchyName().toLowerCase()
				+ " found, no data available, or no links between data and "
				+ data.getPluralHierarchyName().toLowerCase() + " found."));
		this.setLayout(new BorderLayout());
		this.add(messagePanel, BorderLayout.CENTER);
	}

	private void initUI() {
		List<ModelGraph> exclude = data.graphsToExclude();

		tabs = new JTabbedPane();
		tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		modelViews = new ArrayList<JPanel>();
		filters = new ArrayList<ModelGraphTreeComponent>();
		coloringScheme = new ArrayList<Integer>();
		autoUpdate = new ArrayList<Boolean>();
		format = new ArrayList<Integer>();

		int index = 0;
		for (ModelGraph element : hierarchies) {
			JPanel panel = new JPanel(new BorderLayout());
			String graphName = dotFormatter.dotFormatHeader(element).get(
					"graphname");

			modelViews.add(panel);
			coloringScheme.add(ColorScheme.TOTAL);
			autoUpdate.add(true);
			format.add(0);
			tabs.addTab(
					graphName == null ? element.getIdentifier() : graphName,
					null, panel, "View the data from the perspective of '"
							+ element.getIdentifier() + "'");

			if (exclude != null && exclude.contains(element)) {
				tabs.setEnabledAt(tabs.getTabCount() - 1, false);
				tabs.setTitleAt(tabs.getTabCount() - 1, "");
			}

			ModelGraphTreeComponent filter = new ModelGraphTreeComponent(
					getRelevantHierarchiesForFiltering(element), null, null,
					BorderLayout.SOUTH, new NameProvider() {
						public String getName(Object vertex) {
							String name = dotFormatter.dotFormatHeader(
									(ModelGraph) vertex).get("graphname");
							return name == null ? ((ModelGraph) vertex)
									.getIdentifier() : name;
						}
					}, new NameProvider() {
						public String getName(Object vertex) {
							String name = dotFormatter.dotFormatVertex(
									(ModelGraph) ((ModelGraphVertex) vertex)
											.getGraph(),
									(ModelGraphVertex) vertex).get("label");
							return name == null ? ((ModelGraphVertex) vertex)
									.getIdentifier() : name;
						}
					});
			final int finalIndex = index;
			filter.addSelectionChangeListener(new SelectionChangeListener() {
				public void selectionChanged(TreeNode root) {
					if (autoUpdate.get(finalIndex)) {
						reloadModelViewPanel();
					}
				}
			});
			filters.add(filter);
			index++;
		}
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateModelViewPanel();
			}
		});
		tabs.setSelectedIndex(0);
		updateModelViewPanel();

		this.setLayout(new BorderLayout());
		this.add(tabs, BorderLayout.CENTER);
	}

	private List<ModelGraph> getRelevantHierarchiesForFiltering(
			ModelGraph element) {
		List<ModelGraph> hierarchiesToFilterOn = new ArrayList<ModelGraph>();

		for (ModelGraph hierarchy : hierarchies) {
			if (hierarchy != element) { // pointer comparison is OK here
				hierarchiesToFilterOn.add(hierarchy);
			}
		}
		return hierarchiesToFilterOn;
	}

	protected void reloadModelViewPanel() {
		int index = tabs.getSelectedIndex();

		if (index >= 0) {
			fillModelView(index);
		}
	}

	protected void updateModelViewPanel() {
		int index = tabs.getSelectedIndex();

		if (index >= 0 && modelViews.get(index).getComponentCount() == 0) {
			reloadModelViewPanel();
		}
	}

	private void fillModelView(int index) {
		Map<ModelGraphVertex, AggrStatistics> stats = new HashMap<ModelGraphVertex, AggrStatistics>();
		JPanel panel = modelViews.get(index);
		ModelGraph hierarchy = hierarchies.get(index);
		Integer colorSchemeType = coloringScheme.get(index);
		Set<ModelGraphVertex> selection = filters.get(index).getSelectedNodes();

		panel.removeAll();

		for (HierarchicalDataElement element : data) {
			boolean show;

			if (filters.get(index).isEmpty()) {
				show = true;
			} else {
				show = false;
				for (ModelGraphVertex node : element.getNodes()) {
					if (selection.contains(node)) {
						show = true;
						break;
					}
				}
			}

			if (show) {
				for (ModelGraphVertex node : element.getNodes()) {
					AggrStatistics nodeStats = stats.get(node);

					if (nodeStats == null) {
						nodeStats = new AggrStatistics();
						stats.put(node, nodeStats);
					}
					// TODO I think we are assuming that only one concept in
					// each hierarchy is referenced:
					// if one value refers to two concepts in the same
					// hierarchy, then the value will be counted twice in their
					// super nodes
					nodeStats.addValue(element.getValue());
				}
			}
		}

		aggregators.get(index).aggregate(stats);

		SummarizedDataTableModel rawData = new SummarizedDataTableModel(data,
				format.get(index), hierarchy, stats, dotFormatter);
		ColorScheme coloring = new ColorScheme(rawData
				.getMin(colorSchemeType + 1), rawData
				.getMax(colorSchemeType + 1), colorSchemeType);

		JPanel leftPanel = createFilterPanel(index);
		hierarchyModel = new HierarchyModel(hierarchy, stats, coloring,
				dotFormatter);
		JPanel centerPanel = hierarchyModel.getGrappaVisualization();
		JPanel rightPanel = createTableAndLegendAndFormatting(rawData,
				coloring, index);

		Dimension minimumSize = new Dimension(100, 50);
		leftPanel.setMinimumSize(minimumSize);
		centerPanel.setMinimumSize(minimumSize);
		rightPanel.setMinimumSize(minimumSize);

		JPanel leftPanelWithHeader = createPanelWithHeader(leftPanel,
				"Filtering", "Only include elements selected below");
		JPanel centerPanelWithHeader = createPanelWithHeader(centerPanel, data
				.getSingularHierarchyName(), "A visual representation of the "
				+ data.getSingularHierarchyName().toLowerCase());
		JPanel rightPanelWithHeader = createPanelWithHeader(rightPanel,
				"Numbers", "The raw numbers for each element");

		JSplitPane splitPaneRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				centerPanelWithHeader, rightPanelWithHeader);
		splitPaneRight.setOneTouchExpandable(true);
		splitPaneRight.setDividerLocation(splitPaneRight.getSize().width
				- splitPaneRight.getInsets().right
				- splitPaneRight.getDividerSize() - TABLE_WIDTH);
		splitPaneRight.setResizeWeight(1.0);

		JSplitPane splitPaneLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanelWithHeader, splitPaneRight);
		splitPaneLeft.setOneTouchExpandable(true);
		splitPaneLeft.setDividerLocation(TABLE_WIDTH);
		splitPaneLeft.setResizeWeight(0.0);

		panel.add(splitPaneLeft, BorderLayout.CENTER);

		revalidate();
		repaint();
	}

	private JPanel createFilterPanel(final int index) {
		JPanel updatePanel = new JPanel(new BorderLayout());
		JButton updateButton = new JButton("Update");
		final JCheckBox autoUpdateBox = new JCheckBox("Automatic");

		autoUpdateBox.setSelected(autoUpdate.get(index));
		autoUpdateBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				autoUpdate.set(index, autoUpdateBox.isSelected());
			}
		});
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reloadModelViewPanel();
			}
		});

		updatePanel.add(updateButton, BorderLayout.WEST);
		updatePanel.add(autoUpdateBox, BorderLayout.EAST);

		JPanel updatePanel2 = new JPanel();
		updatePanel2.add(updatePanel);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(filters.get(index), BorderLayout.CENTER);
		panel.add(updatePanel2, BorderLayout.SOUTH);
		return panel;
	}

	private JPanel createPanelWithHeader(JPanel panel, String title, String help) {
		JPanel result = new JPanel(new BorderLayout());
		JPanel header = new JPanel(new BorderLayout());
		JLabel titleLabel = new JLabel(title);
		JLabel helpLabel = new JLabel(help);

		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14));
		helpLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 11));

		header.add(titleLabel, BorderLayout.NORTH);
		header.add(helpLabel, BorderLayout.SOUTH);

		result.add(header, BorderLayout.NORTH);
		result.add(panel, BorderLayout.CENTER);

		result.setBorder(BorderFactory.createEmptyBorder(3, 5, 5, 5));
		return result;
	}

	private JPanel createTableAndLegendAndFormatting(
			SummarizedDataTableModel rawData, ColorScheme coloring, int index) {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel legendPanel = new LegendPanel(data, format.get(index), coloring);
		JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 3, 3));
		JPanel legendAndColorSettingPanel = new JPanel(new BorderLayout());

		table = new JTable(rawData);
		for (int i = 0; i < rawData.getColumnCount(); i++) {
			DefaultTableCellRenderer renderer = new ColoredCellRenderer(
					coloring);
			if (i > 0) {
				renderer.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}

		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		if (coloring.getNumColors() > 0) {
			final JComboBox colorSelect = new JComboBox(new String[] { "Total",
					"Average", "Frequency" });

			colorSelect.setSelectedIndex(coloring.getType());

			settingsPanel.add(new JLabel("Colors based on:"));
			settingsPanel.add(colorSelect);
			colorSelect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setColoring(Math.max(0, colorSelect.getSelectedIndex()));
				}
			});
		}

		if (data.getAvailableNumberFormatNames().size() > 0) {
			final JComboBox formatSelect = new JComboBox(new Vector<String>(
					data.getAvailableNumberFormatNames()));

			formatSelect.setEditable(false);
			formatSelect.setSelectedIndex(format.get(index));
			settingsPanel.add(new JLabel("Number format:"));
			settingsPanel.add(formatSelect);

			final int finalIndex = index;
			formatSelect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					format.set(finalIndex, formatSelect.getSelectedIndex());
					reloadModelViewPanel();
				}
			});
			if (coloring.getType() == ColorScheme.FREQUENCY) {
				formatSelect.setEnabled(false);
			}
		}

		legendAndColorSettingPanel.add(settingsPanel, BorderLayout.NORTH);
		legendAndColorSettingPanel.add(legendPanel, BorderLayout.SOUTH);

		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(legendAndColorSettingPanel, BorderLayout.SOUTH);
		panel.setPreferredSize(new Dimension(TABLE_WIDTH, 200));
		return panel;
	}

	protected void setColoring(int scheme) {
		if (tabs.getSelectedIndex() >= 0) {
			coloringScheme.set(tabs.getSelectedIndex(), scheme);
			reloadModelViewPanel();
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		if (table == null) {
			return new ProvidedObject[0];
		} else if (hierarchyModel == null) {
			return new ProvidedObject[] { new ProvidedObject(
					"Table with values", new Object[] { table.getModel() }) };
		} else {
			return new ProvidedObject[] {
					new ProvidedObject("Table with values",
							new Object[] { table.getModel() }),
					new ProvidedObject("Ontology visualization",
							new Object[] { new DotFileWriter() {
								public void writeToDot(Writer bw)
										throws IOException {
									hierarchyModel.writeToDot(bw);
								}
							} }) };
		}
	}
}

class HierarchyModel extends ModelGraph {

	private NumberFormat numberFormatter = NumberFormat.getInstance();
	private Map<ModelGraphVertex, AggrStatistics> stats;
	private DotFormatter dotFormatter;
	private ColorScheme coloring;

	public HierarchyModel(ModelGraph graph,
			Map<ModelGraphVertex, AggrStatistics> stats, ColorScheme coloring,
			DotFormatter dotFormatter) {
		super(graph.getIdentifier());

		this.stats = stats;
		this.dotFormatter = dotFormatter;
		this.coloring = coloring;
		this.numberFormatter.setMaximumFractionDigits(2);

		for (ModelGraphVertex v : graph.getVerticeList()) {
			addVertex(v);
		}
		for (Object e : graph.getEdges()) {
			addEdge((ModelGraphEdge) e);
		}
	}

	private Map<String, String> format(Map<String, String> attributes,
			ModelGraphVertex vertex) {
		if (!attributes.containsKey("do_not_modify_color")) {
			AggrStatistics statistics = stats.get(vertex);
			attributes.put("color", coloring.getColor(statistics));
		}
		return attributes;
	}

	@Override
	public void writeToDot(Writer bw) throws IOException {
		List<ModelGraphVertex> vertices = new ArrayList<ModelGraphVertex>();
		boolean isFirst;

		bw.write("digraph G {");
		for (Map.Entry<String, String> item : dotFormatter
				.dotFormatHeader(this).entrySet()) {
			bw.write(item.getKey() + "=\"" + item.getValue() + "\";");
		}

		nodeMapping.clear();
		int i = 0;
		for (ModelGraphVertex v : getVerticeList()) {
			bw.write("node" + i + " [");
			isFirst = true;
			for (Map.Entry<String, String> item : format(
					dotFormatter.dotFormatVertex(this, v), v).entrySet()) {
				if (!item.getKey().equals("do_not_modify_color")) {
					if (isFirst) {
						isFirst = false;
					} else {
						bw.write(",");
					}
					bw.write(item.getKey() + "=\"" + item.getValue() + "\"");
				}
			}
			bw.write("];\n");

			nodeMapping.put("node" + i, v);
			vertices.add(v);
			i++;
		}

		for (Object edge : getEdges()) {
			ModelGraphEdge e = (ModelGraphEdge) edge;
			int i1 = vertices.indexOf(e.getSource());
			int i2 = vertices.indexOf(e.getDest());

			bw.write("node" + i1 + " -> node" + i2 + " [");
			isFirst = true;
			for (Map.Entry<String, String> item : dotFormatter.dotFormatEdge(
					this, e).entrySet()) {
				if (isFirst) {
					isFirst = false;
				} else {
					bw.write(",");
				}
				bw.write(item.getKey() + "=\"" + item.getValue() + "\"");
			}
			bw.write("];\n");
		}

		bw.write("}\n");
	}
}

class SummarizedDataTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1297578143261938931L;
	private List<String> names;
	private List<AggrStatistics> values;
	private HierarchicalData data;
	private int format;
	private static final String[] columnNames = { "Name", "Total", "Average",
			"Frequency" };

	public SummarizedDataTableModel(HierarchicalData data, int format,
			ModelGraph hierarchy, Map<ModelGraphVertex, AggrStatistics> stats,
			DotFormatter formatter) {
		Map<String, ModelGraphVertex> sortedById = new TreeMap<String, ModelGraphVertex>();

		this.data = data;
		this.format = format;

		for (ModelGraphVertex v : hierarchy.getVerticeList()) {
			String label = formatter.dotFormatVertex(hierarchy, v).get("label");
			sortedById.put(label == null ? "" : label, v);
		}

		names = new ArrayList<String>();
		values = new ArrayList<AggrStatistics>();
		for (Map.Entry<String, ModelGraphVertex> item : sortedById.entrySet()) {
			AggrStatistics stat = stats.get(item.getValue());

			if (stat != null) {
				names.add(item.getKey());
				values.add(stat);
			}
		}
	}

	public double getMin(int col) {
		double result = Double.MAX_VALUE;
		for (int row = 0; row < getRowCount(); row++) {
			result = Math.min(result, getDoubleAt(row, col));
		}
		return result;
	}

	public double getMax(int col) {
		double result = Double.MIN_VALUE;
		for (int row = 0; row < getRowCount(); row++) {
			result = Math.max(result, getDoubleAt(row, col));
		}
		return result;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return names.size();
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public Object getValueAt(int row, int col) {
		if (col == 0) {
			return names.get(row);
		} else if (col - 1 == ColorScheme.FREQUENCY) {
			return "" + values.get(row).getN();
		} else {
			return data.formatNumber(format, getDoubleAt(row, col));
		}
	}

	public Double getDoubleAt(int row, int col) {
		switch (col - 1) {
		case ColorScheme.TOTAL:
			return values.get(row).getSum();
		case ColorScheme.AVERAGE:
			return values.get(row).getMean();
		case ColorScheme.FREQUENCY:
			return (double) values.get(row).getN();
		default:
			assert (false);
			return null;
		}
	}
}

class ColorScheme {

	/**
	 * Colors as they are understood by DOT. Should correspond to colorObjects.
	 */
	private final static String[] colorNames = { "limegreen", "greenyellow",
			"yellow", "orange", "firebrick1" };

	/**
	 * Colors as they are understood by Java. Should correspond to colorNames.
	 */
	private final static Color[] colorObjects = { new Color(0x32, 0xcd, 0x32),
			new Color(0xad, 0xff, 0x2f), new Color(0xff, 0xff, 0x00),
			new Color(0xff, 0xa5, 0x00), new Color(0xff, 0x30, 0x30) };

	private final static String unusedColorName = "azure";
	private final static Color unusedColorObject = new Color(0xF0, 0xFF, 0xFF);

	private double minValue;
	private double maxValue;
	private int schemeType;

	// Possible scheme types.
	// Values MUST correspond with the combo box 'colorSelect' above AND with
	// the columns in the table with numbers!
	public static final int TOTAL = 0;
	public static final int AVERAGE = 1;
	public static final int FREQUENCY = 2;

	public ColorScheme(double minValue, double maxValue, int schemeType) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.schemeType = schemeType;
	}

	public int getType() {
		return schemeType;
	}

	public int getNumColors() {
		return minValue < Double.MAX_VALUE && maxValue > Double.MIN_VALUE ? colorNames.length
				: 0;
	}

	public String getColorNameAtIndex(int i) {
		assert (0 <= i && i < getNumColors());
		return colorNames[i];
	}

	public Color getColorObjectAtIndex(int i) {
		assert (0 <= i && i < getNumColors());
		return colorObjects[i];
	}

	public Color getUnusedColorObject() {
		return unusedColorObject;
	}

	public String getColor(AggrStatistics statistic) {
		if (statistic != null) {
			double value = 0;

			switch (schemeType) {
			case TOTAL:
				value = statistic.getSum();
				break;
			case AVERAGE:
				value = statistic.getMean();
				break;
			case FREQUENCY:
				value = statistic.getN();
				break;
			default:
				return unusedColorName;
			}
			int index = getColor(value);
			return index >= 0 ? getColorNameAtIndex(index) : unusedColorName;
		}
		return unusedColorName;
	}

	public double getFrom(int i) {
		assert (0 <= i && i < getNumColors());
		return minValue + (maxValue - minValue) / colorNames.length * i;
	}

	public double getTo(int i) {
		assert (0 <= i && i < getNumColors());
		return minValue + (maxValue - minValue) / colorNames.length * (i + 1);
	}

	public Color getColorObject(double value) {
		int index = getColor(value);
		return index >= 0 ? getColorObjectAtIndex(index) : unusedColorObject;
	}

	private int getColor(double value) {
		// I'm sure this can be computed without a loop... :)
		for (int i = 0; i < getNumColors(); i++) {
			if (getFrom(i) <= value && value <= getTo(i)) {
				return i;
			}
		}
		return -1;
	}
}

class LegendPanel extends JPanel {

	private static final long serialVersionUID = 4385445925386887232L;
	private final static int LABEL_X = 100;
	private final static int LABEL_HEIGHT = 10;
	private final static int PYRAMID_MAX_WIDTH = 90;
	private final static int PYRAMID_MIN_WIDTH = 30;
	private final static int PYRAMID_CELL_HEIGHT = 23;
	private final static int MARGIN = 5;

	private ColorScheme coloring;
	private Vector<String> labels;

	public LegendPanel(HierarchicalData data, int format, ColorScheme coloring) {
		this.coloring = coloring;
		this.labels = new Vector<String>();

		if (coloring.getNumColors() > 0) {
			labels.add(getString(data, format, coloring.getFrom(0)));
			for (int i = 0; i < coloring.getNumColors(); i++) {
				labels.add(getString(data, format, coloring.getTo(i)));
			}
		}
	}

	private String getString(HierarchicalData data, int format, double value) {
		if (coloring.getType() == ColorScheme.FREQUENCY) {
			return "" + (int) Math.round(value);
		} else {
			return "" + data.formatNumber(format, value);
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(LABEL_X + 200, MARGIN + LABEL_HEIGHT / 2
				+ PYRAMID_CELL_HEIGHT * (coloring.getNumColors() + 1) + MARGIN);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// draw pyramid with colors (from bottom to top)
		if (coloring.getNumColors() > 0) {
			int slope = (PYRAMID_MAX_WIDTH - PYRAMID_MIN_WIDTH)
					/ coloring.getNumColors() / 2;

			for (int i = 0; i < coloring.getNumColors(); i++) {
				int x[] = new int[] { i * slope, PYRAMID_MAX_WIDTH - i * slope,
						PYRAMID_MAX_WIDTH - (i + 1) * slope, (i + 1) * slope };
				int y[] = new int[] {
						MARGIN + LABEL_HEIGHT / 2 + i * PYRAMID_CELL_HEIGHT,
						MARGIN + LABEL_HEIGHT / 2 + i * PYRAMID_CELL_HEIGHT,
						MARGIN + LABEL_HEIGHT / 2 + (i + 1)
								* PYRAMID_CELL_HEIGHT,
						MARGIN + LABEL_HEIGHT / 2 + (i + 1)
								* PYRAMID_CELL_HEIGHT };

				g.setColor(coloring.getColorObjectAtIndex(coloring
						.getNumColors()
						- 1 - i));
				g.fillPolygon(x, y, 4);
			}
		}

		// draw labels and lines going from pyramid to labels
		g.setColor(Color.BLACK);
		int index = labels.size() - 1;
		for (String label : labels) {
			int labelY = MARGIN + PYRAMID_CELL_HEIGHT * index + LABEL_HEIGHT;
			int slope = (PYRAMID_MAX_WIDTH - PYRAMID_MIN_WIDTH)
					/ coloring.getNumColors() / 2;

			g.drawString(label, LABEL_X, labelY);
			g.drawLine(index * slope, labelY - LABEL_HEIGHT / 2, LABEL_X
					- MARGIN, labelY - LABEL_HEIGHT / 2);

			index--;
		}

		int noStatsX = (PYRAMID_MAX_WIDTH - PYRAMID_MIN_WIDTH) / 2;
		int noStatsY = MARGIN + LABEL_HEIGHT / 2 + PYRAMID_CELL_HEIGHT
				* coloring.getNumColors() + MARGIN;
		g.setColor(coloring.getUnusedColorObject());
		g.fillRect(noStatsX, noStatsY, PYRAMID_MIN_WIDTH, PYRAMID_CELL_HEIGHT);
		g.setColor(Color.BLACK);
		g.drawString("No statistics available", LABEL_X, noStatsY
				+ PYRAMID_CELL_HEIGHT / 2 + LABEL_HEIGHT - MARGIN / 2);
		g.drawLine(noStatsX + PYRAMID_MIN_WIDTH + MARGIN, noStatsY
				+ PYRAMID_CELL_HEIGHT / 2 + LABEL_HEIGHT / 2 - MARGIN / 2,
				LABEL_X - MARGIN, noStatsY + PYRAMID_CELL_HEIGHT / 2
						+ LABEL_HEIGHT / 2 - MARGIN / 2);
	}
}

class ColoredCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -8540474103226881230L;
	private ColorScheme coloring;

	public ColoredCellRenderer(ColorScheme coloring) {
		this.coloring = coloring;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		int col = this.coloring.getType() + 1;

		setText(value.toString());
		setForeground(table.getForeground());
		setBackground(coloring.getColorObject(((SummarizedDataTableModel) table
				.getModel()).getDoubleAt(row, col)));
		return this;
	}
}

class AggrStatistics {

	private double sum;
	private long n;

	public AggrStatistics() {
		sum = 0;
		n = 0;
	}

	public long getN() {
		return n;
	}

	public double getMean() {
		return n > 0 ? sum / n : Double.NaN;
	}

	public double getSum() {
		return sum;
	}

	public void addValue(double value) {
		sum += value;
		n++;
	}

	public void addStatistics(AggrStatistics stats) {
		sum += stats.sum;
		n += stats.n;
	}

	public String toString() {
		return "Stat(sum=" + sum + ",n=" + n + ")";
	}
}

class Aggregator {

	private DoubleMatrix2D connections;
	private Map<ModelGraphVertex, Integer> vertexToIndex;
	private Map<Integer, ModelGraphVertex> indexToVertex;
	private int n;

	public Aggregator(ModelGraph hierarchy) {
		computeConnections(hierarchy);
	}

	public void aggregate(Map<ModelGraphVertex, AggrStatistics> statistics) {
		List<ModelGraphVertex> keys = new ArrayList<ModelGraphVertex>(
				statistics.keySet());

		for (ModelGraphVertex key : keys) {
			Integer from = vertexToIndex.get(key);
			AggrStatistics fromStat = statistics.get(key);

			if (from == null) {
				continue;
			}

			for (int to = 0; to < n; to++) {
				if (0 < connections.get(from, to)
						&& connections.get(from, to) < n) {
					ModelGraphVertex toVertex = indexToVertex.get(to);
					AggrStatistics toStat = statistics.get(toVertex);

					if (toStat == null) {
						toStat = new AggrStatistics();
						statistics.put(toVertex, toStat);
					}
					toStat.addStatistics(fromStat);
				}
			}
		}
	}

	private void computeConnections(ModelGraph hierarchy) {
		n = 0;
		vertexToIndex = new HashMap<ModelGraphVertex, Integer>();
		indexToVertex = new HashMap<Integer, ModelGraphVertex>();
		for (ModelGraphVertex vertex : hierarchy.getVerticeList()) {
			vertexToIndex.put(vertex, n);
			indexToVertex.put(n, vertex);
			n++;
		}

		connections = new DenseDoubleMatrix2D(n, n);
		connections.assign(Integer.MAX_VALUE);
		for (Object edge : hierarchy.getEdges()) {
			connections.set(vertexToIndex
					.get(((ModelGraphEdge) edge).getDest()), vertexToIndex
					.get(((ModelGraphEdge) edge).getSource()), 1);
		}

		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					connections.set(i, j, Math.min(connections.get(i, j),
							connections.get(i, k) + connections.get(k, j)));
				}
			}
		}
	}
}
