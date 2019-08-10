package org.processmining.framework.models.petrinet.pattern.log;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.bpel.util.Quadruple;
import org.processmining.framework.models.bpel.util.Quintuple;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.pattern.Component;
import org.processmining.framework.models.petrinet.pattern.LibraryComponent;
import org.processmining.framework.models.petrinet.pattern.PatternMatcher;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

import att.grappa.Node;

/**
 * <p>
 * Title: Log
 * </p>
 * 
 * <p>
 * Description: Presents a log to the user of how the conversion went.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: University of Aarhus
 * </p>
 * 
 * @author Kristian Bisgaard Lassen (<a
 *         href="mailto:K.B.Lassen@daimi.au.dk">mailto
 *         :K.B.Lassen@daimi.au.dk</a>)
 * @version 1.0
 */
public class Log extends JTabbedPane implements ListSelectionListener,
		MiningResult, Provider {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9159921592164065815L;

	private static final double angle = -0.5;

	private DefaultListModel componentModel;

	private JList componentList;

	// Quintuple<PetriNet, Component, Double, Integer, Integer>
	private List<Reduction> reductions;

	private Map<String, Integer> usedComponentCount;

	private JPanel componentPanel;

	private JPanel isomorphismPanel;

	private DefaultListModel petriNetModel;

	private JList petriNetList;

	private DefaultListModel nodeModel;

	private JPanel presentationDialogPanel;

	private PetriNet chosenWfnet;

	private final PetriNet[] partWfnet;

	private final DefaultMutableTreeNode[] selectedNode;

	private final Map<DefaultMutableTreeNode, Boolean> isExpanded = new LinkedHashMap<DefaultMutableTreeNode, Boolean>();

	private final boolean showCost;

	private boolean initializedGraphics;

	public Log(boolean showCost) {
		this.showCost = showCost;
		initializedGraphics = false;
		reductions = new ArrayList<Reduction>();
		usedComponentCount = new TreeMap<String, Integer>(
				new Comparator<String>() {
					public int compare(String arg0, String arg1) {
						return arg0.toLowerCase().compareTo(arg1.toLowerCase());
					}
				});
		partWfnet = new PetriNet[1];
		selectedNode = new DefaultMutableTreeNode[1];
	}

	private void initializeGraphics() {
		if (!initializedGraphics) {
			presentationDialogPanel = new JPanel(new BorderLayout());
			presentationDialogPanel.add(this, BorderLayout.CENTER);
			initializedGraphics = true;
		}
	}

	private java.awt.Component createReductionsPane(PetriNet wfnet,
			Map<String, Component> transition2Component) {
		JTabbedPane result = new JTabbedPane();

		result.add("Summary", createSummaryPane(wfnet, null, true));
		result.add("Graphs", createGraphPane(wfnet));
		result.add("Reductions", createReductionPane());
		result.add("Reduction tree", createReductionTreePane(wfnet,
				transition2Component));

		return result;
	}

	private java.awt.Component createReductionTreePane(PetriNet wfnet,
			final Map<String, Component> transition2Component) {
		JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		final DefaultMutableTreeNode top = new DefaultMutableTreeNode(
				"Decomposition of " + wfnet.getIdentifier());
		selectedNode[0] = top;
		String name = wfnet.getTransitions().get(0).getName();

		final Map<DefaultMutableTreeNode, String> node2name = new LinkedHashMap<DefaultMutableTreeNode, String>();
		populateTree(top, name, transition2Component.get(name),
				transition2Component, node2name);

		final JTree tree = new JTree(top);
		tree.setSelectionPath(new TreePath(top));
		expandAll(tree, new TreePath(top), true);
		panel.add(tree, JSplitPane.LEFT);

		final JPanel petriPanel = new JPanel(new GridLayout(1, 1));
		final PetriNet petriNet = reductions.get(0).petriNet;
		partWfnet[0] = petriNet;
		petriPanel.add(petriNet.getGrappaVisualization());
		panel.add(petriPanel, JSplitPane.RIGHT);

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				JTree tree = (JTree) e.getSource();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
						.getLastSelectedPathComponent();
				if (node != null && node != top) {
					selectedNode[0] = node;
					String name = node2name.get(selectedNode[0]);
					partWfnet[0] = getPetriNetFromComponent(name,
							transition2Component, petriNet);
					presentTree(tree, (DefaultMutableTreeNode) node,
							partWfnet[0], node2name, transition2Component);
					petriPanel.removeAll();
					petriPanel.add(partWfnet[0].getGrappaVisualization());
					petriPanel.revalidate();
					petriPanel.repaint();
				}
			}
		});

		tree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) event
						.getPath().getLastPathComponent();
				if (selectedNode[0].isNodeDescendant(node)) {
					String name = node2name.get(selectedNode[0]);
					partWfnet[0] = getPetriNetFromComponent(name,
							transition2Component, petriNet);
					isExpanded.put((DefaultMutableTreeNode) event.getPath()
							.getLastPathComponent(), false);
					presentTree(tree, (DefaultMutableTreeNode) event.getPath()
							.getLastPathComponent(), partWfnet[0], node2name,
							transition2Component);
				}
				petriPanel.removeAll();
				petriPanel.add(partWfnet[0].getGrappaVisualization());
			}

			public void treeExpanded(TreeExpansionEvent event) {
				String name = node2name.get(selectedNode[0]);
				partWfnet[0] = getPetriNetFromComponent(name,
						transition2Component, petriNet);
				isExpanded.put((DefaultMutableTreeNode) event.getPath()
						.getLastPathComponent(), true);
				presentTree(tree, (DefaultMutableTreeNode) event.getPath()
						.getLastPathComponent(), partWfnet[0], node2name,
						transition2Component);
				petriPanel.removeAll();
				petriPanel.add(partWfnet[0].getGrappaVisualization());
			}
		});

		panel.revalidate();
		panel.repaint();
		return panel;
	}

	private final void presentTree(JTree tree, DefaultMutableTreeNode node,
			PetriNet petriNet, Map<DefaultMutableTreeNode, String> node2name,
			Map<String, Component> transition2Component) {
		if (!tree.getModel().isLeaf(node)) {
			if (isExpanded.get(node) == null || isExpanded.get(node)) {
				for (Enumeration en = node.children(); en.hasMoreElements();)
					presentTree(tree,
							(DefaultMutableTreeNode) en.nextElement(),
							petriNet, node2name, transition2Component);
			} else {
				for (Enumeration en = node.children(); en.hasMoreElements();) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) en
							.nextElement();
					Component component = transition2Component.get(node2name
							.get(child));
					Node source = getSource(component, transition2Component);
					Node sink = getSink(component, transition2Component);
					boolean foundSource = false, foundSink = false;
					for (Node n : partWfnet[0].getNodes()) {
						if (n.getName().equals(source.getName())) {
							source = n;
							foundSource = true;
						} else if (n.getName().equals(sink.getName())) {
							sink = n;
							foundSink = true;
						}
						if (foundSource && foundSink)
							break;
					}
					Set<Node> nodes = new LinkedHashSet<Node>();
					try {
						PatternMatcher.getNodesInComponent(source, source,
								sink, nodes, partWfnet[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
					PetriNet extract = partWfnet[0].extractNet(nodes);
					PatternMatcher.reduce(partWfnet[0], extract);
				}
			}
		}
	}

	private final Node getSource(Component component,
			Map<String, Component> transition2Component) {
		Node source = component.getWfnet().getSource();
		Component subcomponent = transition2Component.get(source.getName());
		if (subcomponent != null)
			return getSource(subcomponent, transition2Component);
		return source;
	}

	private final Node getSink(Component component,
			Map<String, Component> transition2Component) {
		Node sink = component.getWfnet().getSink();
		Component subcomponent = transition2Component.get(sink.getName());
		if (subcomponent != null)
			return getSink(subcomponent, transition2Component);
		return sink;
	}

	private final PetriNet getPetriNetFromComponent(String name,
			Map<String, Component> transition2Component, PetriNet petriNet) {
		Component component = transition2Component.get(name);
		Node componentSource = getSource(component, transition2Component);
		Node componentSink = getSink(component, transition2Component);
		Node source = null, sink = null;
		for (Node pnode : petriNet.getNodes()) {
			if (pnode.getName().equals(componentSource.getName()))
				source = pnode;
			else if (pnode.getName().equals(componentSink.getName()))
				sink = pnode;
			if (source != null && sink != null)
				break;
		}
		Set<Node> nodes = new LinkedHashSet<Node>();
		try {
			PatternMatcher.getNodesInComponent(source, source, sink, nodes,
					petriNet);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return petriNet.extractNet(nodes);
	}

	private void expandAll(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}

		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	private void populateTree(DefaultMutableTreeNode father, String name,
			Component component, Map<String, Component> transition2Component,
			Map<DefaultMutableTreeNode, String> node2name) {
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(component
				.toString());
		node2name.put(child, name);
		father.add(child);
		for (Transition transition : component.getWfnet().getTransitions()) {
			Component subcomponent = transition2Component.get(transition
					.getName());
			if (subcomponent != null) {
				populateTree(child, transition.getName(), subcomponent,
						transition2Component, node2name);
			}
		}
	}

	private JSplitPane createReductionPane() {
		JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JPanel leftPart = new JPanel(new FlowLayout());
		JSplitPane reductionListPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT);
		reductionListPanel.setResizeWeight(0.5);
		reductionListPanel.add(new JScrollPane(leftPart), JSplitPane.TOP);

		componentPanel = new JPanel(new GridLayout(1, 1));
		panel.add(componentPanel, JSplitPane.RIGHT);

		petriNetModel = new DefaultListModel();
		petriNetList = new JList(petriNetModel);
		petriNetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		petriNetList.addListSelectionListener(this);
		petriNetList.setSelectedIndex(0);
		chosenWfnet = reductions.get(0).petriNet;

		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Net"), BorderLayout.NORTH);
		listPanel.add(petriNetList, BorderLayout.CENTER);

		JScrollPane listScroller = new JScrollPane(listPanel);
		JPanel backListPanel = new JPanel(new BorderLayout());
		backListPanel.add(listScroller, BorderLayout.CENTER);

		leftPart.add(backListPanel);

		componentModel = new DefaultListModel();
		componentList = new JList(componentModel);
		componentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		componentList.addListSelectionListener(this);

		listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Component"), BorderLayout.NORTH);
		listPanel.add(componentList, BorderLayout.CENTER);

		listScroller = new JScrollPane(listPanel);
		backListPanel = new JPanel(new BorderLayout());
		backListPanel.add(listScroller, BorderLayout.CENTER);

		leftPart.add(backListPanel);

		DefaultListModel structurednessModel = new DefaultListModel();
		JList structurednessList = new JList(structurednessModel);

		if (showCost) {
			listPanel = new JPanel(new BorderLayout());
			listPanel.add(new JLabel("Score"), BorderLayout.NORTH);
			listPanel.add(structurednessList, BorderLayout.CENTER);

			listScroller = new JScrollPane(listPanel);
			backListPanel = new JPanel(new BorderLayout());
			backListPanel.add(listScroller, BorderLayout.CENTER);

			leftPart.add(backListPanel);
		}

		for (int index = 1; index <= reductions.size(); index++) {
			petriNetModel.addElement("Before reduction " + index);
			componentModel.addElement(reductions.get(index - 1).component
					.toString());
			if (showCost) {
				structurednessModel
						.addElement("("
								+ reductions.get(index - 1).smResult
								+ "; "
								+ reductions.get(index - 1).cardosoMetric
								+ "; "
								+ (reductions.get(index - 1).cyclomaticMetric < 0 ? "N/A"
										: reductions.get(index - 1).cyclomaticMetric)
								+ ")");
			}
		}

		componentPanel.add(reductions.get(0).petriNet.getGrappaVisualization());

		nodeModel = new DefaultListModel();
		JList nodeList = new JList(nodeModel);

		listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Isomorphism"), BorderLayout.NORTH);
		listPanel.add(nodeList, BorderLayout.CENTER);

		listScroller = new JScrollPane(listPanel);
		isomorphismPanel = new JPanel(new BorderLayout());
		isomorphismPanel.add(listScroller, BorderLayout.CENTER);

		reductionListPanel.add(new JScrollPane(isomorphismPanel),
				JSplitPane.BOTTOM);

		panel.add(reductionListPanel, JSplitPane.LEFT);
		return panel;
	}

	private JPanel createGraphPane(PetriNet finalNet) {
		JPanel result = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		result.add(tabbedPane, BorderLayout.CENTER);

		ChartPanel chartPanel = createMatchesPanel(usedComponentCount);
		tabbedPane.add("Matches", chartPanel);

		XYSeriesCollection stacked = new XYSeriesCollection();
		Map<String, Integer> counter = new LinkedHashMap<String, Integer>();
		Map<String, XYSeries> series = new TreeMap<String, XYSeries>(
				new Comparator<String>() {
					public int compare(String arg0, String arg1) {
						return arg0.toLowerCase().compareTo(arg1.toLowerCase());
					}
				});
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int index = 1;
		for (Reduction reduction : reductions) {
			Component comp = reduction.component;
			String component = comp.toString();
			if (series.get(component) == null) {
				counter.put(component, 0);
				series.put(component, new XYSeries(component, true, true));
			}
			int beforeCount = counter.get(component);
			int count = beforeCount + 1;
			counter.put(component, count);
			series.get(component).add(index - 1, beforeCount);
			series.get(component).add(index, counter.get(component));
			for (String key : series.keySet()) {
				series.get(key).add(index, counter.get(key));
			}
			double max = 0;
			for (Integer count1 : counter.values()) {
				max += count1;
			}
			for (String key : counter.keySet()) {
				dataset.addValue(counter.get(key) / max, key, comp.toString()
						+ " (" + Integer.toString(index) + ")");
			}
			index++;
		}
		for (XYSeries xy : series.values())
			stacked.addSeries(xy);
		JFreeChart steps = ChartFactory.createXYLineChart("", "Step",
				"Total matches", stacked, PlotOrientation.VERTICAL, true, true,
				false);
		chartPanel = new ChartPanel(steps, false);
		setXYChartProperties(steps);
		tabbedPane.add("Used", chartPanel);

		JFreeChart matches = ChartFactory.createStackedBarChart3D("", // chart
				// title
				"Step", // domain axis label
				"Spread", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
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
						String value = Double.toString(100 * dataset.getValue(
								row, column).doubleValue());
						if (value.equals("100.0"))
							value = "100";
						if (value.length() > 5) {
							value = value.substring(0, 4);
						}
						value += "%";
						return value;
					}

					public String generateRowLabel(CategoryDataset dataset,
							int row) {
						return null;
					}
				});
		matches.getCategoryPlot().getRenderer().setItemLabelsVisible(true);

		setCategoryChartProperties(matches, 1);
		chartPanel = new ChartPanel(matches, false);
		tabbedPane.add("Spread", chartPanel);

		stacked = new XYSeriesCollection();
		XYSeries total = new XYSeries("Nodes", true, true);
		XYSeries places = new XYSeries("Places", true, true);
		XYSeries transitions = new XYSeries("Transitions", true, true);
		dataset = new DefaultCategoryDataset();

		index = 0;
		for (Reduction reduction : reductions) {
			PetriNet petriNet = reduction.petriNet;
			total.add(index, petriNet.numberOfNodes());
			places.add(index, petriNet.numberOfPlaces());
			transitions.add(index, petriNet.numberOfTransitions());
			index++;
		}
		total.add(index, finalNet.numberOfNodes());
		places.add(index, finalNet.numberOfPlaces());
		transitions.add(index, finalNet.numberOfTransitions());
		stacked.addSeries(total);
		stacked.addSeries(places);
		stacked.addSeries(transitions);

		steps = ChartFactory.createXYLineChart("", "Step", "Total matches",
				stacked, PlotOrientation.VERTICAL, true, true, false);
		chartPanel = new ChartPanel(steps, false);
		setXYChartProperties(steps);
		tabbedPane.add("Petri net size", chartPanel);

		steps.getXYPlot().getRenderer().setItemLabelGenerator(
				new XYItemLabelGenerator() {
					public String generateLabel(XYDataset arg0, int arg1,
							int arg2) {
						if (arg1 == 0 && arg2 > 0 && arg2 <= reductions.size())
							return reductions.get(arg2 - 1).component
									.toString();
						else
							return null;
					}
				});
		steps.getXYPlot().getRenderer().setPositiveItemLabelPosition(
				new ItemLabelPosition() {
					private static final long serialVersionUID = -672737986202150388L;

					/**
					 * @see org.jfree.chart.labels.ItemLabelPosition#getAngle()
					 */
					public double getAngle() {
						return angle;
					}

				});
		steps.getXYPlot().getRenderer().setItemLabelsVisible(true);

		stacked = new XYSeriesCollection();
		total = new XYSeries("Nodes", true, true);
		places = new XYSeries("Places", true, true);
		transitions = new XYSeries("Transitions", true, true);
		dataset = new DefaultCategoryDataset();
		index = 1;
		for (Reduction reduction : reductions) {
			total.add(index, reduction.component.getWfnet().numberOfNodes());
			places.add(index, reduction.component.getWfnet().numberOfPlaces());
			transitions.add(index, reduction.component.getWfnet()
					.numberOfTransitions());
			index++;
		}
		stacked.addSeries(total);
		stacked.addSeries(places);
		stacked.addSeries(transitions);

		steps = ChartFactory.createXYLineChart("", "Step", "Total matches",
				stacked, PlotOrientation.VERTICAL, true, true, false);
		steps.getXYPlot().getRenderer().setItemLabelGenerator(
				new XYItemLabelGenerator() {
					public String generateLabel(XYDataset arg0, int arg1,
							int arg2) {
						if (arg1 == 0)
							return reductions.get(arg2).component.toString();
						else
							return null;
					}
				});
		steps.getXYPlot().getRenderer().setPositiveItemLabelPosition(
				new ItemLabelPosition() {
					private static final long serialVersionUID = -672737986202150388L;

					/**
					 * @see org.jfree.chart.labels.ItemLabelPosition#getAngle()
					 */
					public double getAngle() {
						return angle;
					}

				});
		steps.getXYPlot().getRenderer().setItemLabelsVisible(true);
		chartPanel = new ChartPanel(steps, false);
		setXYChartProperties(steps);
		tabbedPane.add("Component size", chartPanel);

		if (showCost) {
			stacked = new XYSeriesCollection();
			total = new XYSeries("Cost", true, true);
			index = 1;
			double cost = 0;
			for (Reduction reduction : reductions) {
				cost += reduction.smResult;
				total.add(index, cost);
				index++;
			}
			stacked.addSeries(total);

			steps = ChartFactory.createXYLineChart("", "Step",
					"Structuredness cost", stacked, PlotOrientation.VERTICAL,
					false, false, false);
			steps.getXYPlot().getRenderer().setItemLabelGenerator(
					new XYItemLabelGenerator() {
						public String generateLabel(XYDataset arg0, int arg1,
								int arg2) {
							if (arg1 == 0)
								return reductions.get(arg2).component
										.toString();
							else
								return null;
						}
					});
			steps.getXYPlot().getRenderer().setPositiveItemLabelPosition(
					new ItemLabelPosition() {
						private static final long serialVersionUID = -672737986202150388L;

						/**
						 * @see org.jfree.chart.labels.ItemLabelPosition#getAngle()
						 */
						public double getAngle() {
							return angle;
						}

					});
			steps.getXYPlot().getRenderer().setItemLabelsVisible(true);
			chartPanel = new ChartPanel(steps, false);
			setXYChartProperties(steps);
			tabbedPane.add("Acumulated structuredness cost", chartPanel);

			dataset = new DefaultCategoryDataset();
			index = 1;
			for (Reduction reduction : reductions) {
				dataset.addValue(reduction.smResult, "Structuredness metric",
						reduction.component.toString() + " ("
								+ new Integer(index) + ")");
				dataset.addValue(reduction.cardosoMetric, "Cardoso metric",
						reduction.component.toString() + " ("
								+ new Integer(index) + ")");
				if (reduction.cyclomaticMetric >= 0)
					dataset.addValue(reduction.cyclomaticMetric,
							"Cyclomatic metric", reduction.component.toString()
									+ " (" + new Integer(index) + ")");
				index++;
			}
			matches = ChartFactory.createBarChart3D("", // chart title
					"Step", // domain axis label
					"Score", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					true, // include legend
					true, // tooltips?
					false // URLs?
					);
			matches.getCategoryPlot().getRenderer().setItemLabelGenerator(
					new CategoryItemLabelGenerator() {

						public String generateColumnLabel(
								CategoryDataset dataset, int column) {
							return reductions.get(column).component.toString();
						}

						public String generateLabel(CategoryDataset dataset,
								int row, int column) {
							Reduction r = reductions.get(column);
							switch (row) {
							case 0:
								return String.valueOf(r.smResult);
							case 1:
								return String.valueOf(r.cardosoMetric);
							case 2:
								return String.valueOf(r.cyclomaticMetric);
							}
							return null;
						}

						public String generateRowLabel(CategoryDataset dataset,
								int row) {
							return null;
						}
					});
			matches.getCategoryPlot().getRenderer().setItemLabelsVisible(true);
			setCategoryChartProperties(matches);
			chartPanel = new ChartPanel(matches, false);
			tabbedPane.add("Cost per component", chartPanel);
		}

		return result;
	}

	public static ChartPanel createMatchesPanel(
			final Map<String, Integer> usedComponentCount) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (String key : usedComponentCount.keySet()) {
			dataset.addValue(usedComponentCount.get(key), key, key);
		}
		JFreeChart matches = ChartFactory.createStackedBarChart3D("", // chart
				// title
				"Component", // domain axis label
				"Matches", // range axis label
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
						for (String key : usedComponentCount.keySet()) {
							if (row == 0) {
								return usedComponentCount.get(key).toString();
							}
							row--;
						}
						return null;
					}

					public String generateRowLabel(CategoryDataset dataset,
							int row) {
						return null;
					}
				});
		matches.getCategoryPlot().getRenderer().setItemLabelsVisible(true);
		setCategoryChartProperties(matches);
		ChartPanel chartPanel = new ChartPanel(matches, false);
		return chartPanel;
	}

	private void setCategoryChartProperties(JFreeChart chart, int i) {
		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setDomainGridlinePaint(Color.black);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		NumberAxis axis = (NumberAxis) plot.getRangeAxis();
		axis.setUpperBound(i);
		axis.setLowerBound(0);
	}

	private void setXYChartProperties(JFreeChart chart) {
		chart.setBackgroundPaint(Color.white);
		XYPlot plot = chart.getXYPlot();
		plot.setDomainGridlinePaint(Color.black);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		NumberAxis axis = (NumberAxis) plot.getRangeAxis();
		axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		axis = (NumberAxis) plot.getDomainAxis();
		axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
	}

	/**
	 * @param chart
	 */
	private static void setCategoryChartProperties(JFreeChart chart) {
		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setDomainGridlinePaint(Color.black);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	}

	/**
	 * @param usedComponentCount
	 * @param reductions
	 * @param component
	 * @param wfnet
	 * @param logSpecificMessage
	 * @param showSummary
	 * @return
	 * 
	 */
	private JScrollPane createSummaryPane(PetriNet wfnet,
			String logSpecificMessage, boolean showSummary) {
		StringBuilder str = new StringBuilder();
		str.append("<html><body>");
		// if (wfnet.getTransitions().size() == 1) {
		// str.append("<h3>Petri net reduced</h3>");
		// } else
		// str.append("<h3>Petri <i>not</i> reduced</h3>");
		if (logSpecificMessage != null) {
			str.append(logSpecificMessage);
			if (showSummary)
				str.append("<hr>");
		}
		if (showSummary) {
			str.append("Number of reductions: ");
			str.append(reductions.size());
			str.append("<br>Number of different types of components matched: ");
			str.append(usedComponentCount.size());
			if (!reductions.isEmpty()) {
				str
						.append("<h3>Stats</h3><table border=\"1\"><tr><th>Component</th><th>Times used</th><th>Percentage used</th></tr>");
				for (String key : usedComponentCount.keySet()) {
					str.append("<tr><td>");
					str.append(key);
					str.append("</td><td align=\"center\">");
					str.append(usedComponentCount.get(key));
					str.append("</td><td align=\"center\">");
					String percent = Double.toString(100
							* usedComponentCount.get(key)
							/ ((double) reductions.size()));

					int part = 4;
					if (percent.lastIndexOf(".") + part >= percent.length()) {
						part = percent.length() - percent.lastIndexOf(".");
					}
					str.append(percent.substring(0, percent.lastIndexOf(".")
							+ part));
					str.append("%</td></tr>");
				}
				str.append("</table>");
			}
		}
		str.append("</body></html>");
		return new JScrollPane(new JEditorPane("text/html", str.toString()));
	}

	public void valueChanged(ListSelectionEvent arg0) {
		int selectedIndex = 0;
		chosenWfnet = null;
		if (arg0.getSource() == componentList
				&& componentList.getSelectedIndex() >= 0) {
			selectedIndex = componentList.getSelectedIndex();
			chosenWfnet = reductions.get(selectedIndex).component.getWfnet();
			nodeModel.clear();
			Component component = reductions.get(selectedIndex).component;
			if (component instanceof LibraryComponent) {
				Map<Node, Node> isomorphism = ((LibraryComponent) component)
						.getIsomorphism();
				if (isomorphism != null) {
					for (Node node : isomorphism.keySet()) {
						nodeModel.addElement(new String(node.toString() + " = "
								+ isomorphism.get(node).toString()));
					}
				}
			}
			petriNetList.clearSelection();
		} else if (arg0.getSource() == petriNetList
				&& petriNetList.getSelectedIndex() >= 0) {
			selectedIndex = petriNetList.getSelectedIndex();
			chosenWfnet = reductions.get(selectedIndex).petriNet;
			nodeModel.clear();
			componentList.clearSelection();
		}
		if (chosenWfnet != null) {
			componentPanel.removeAll();
			componentPanel.add(chosenWfnet.getGrappaVisualization());
		}
	}

	public void storeLogMatch(PetriNet wfnet, Component component,
			double penalty, double unstructuredPenalty, int cardosoMetric,
			int cyclomatic) {
		reductions.add(new Reduction((PetriNet) wfnet.clone(), component
				.cloneComponent(), penalty, unstructuredPenalty, cardosoMetric,
				cyclomatic));
		String componentName;
		componentName = component.toString();
		if (usedComponentCount.get(componentName) == null)
			usedComponentCount.put(componentName, 0);
		usedComponentCount.put(componentName, usedComponentCount
				.get(componentName) + 1);
	}

	public void prepareToShowLog(PetriNet wfnet) {
		prepareToShowLog(wfnet, null, true, null, null, null, null, null);
	}

	public void prepareToShowLog(
			PetriNet wfnet,
			String logSpecificMessage,
			boolean showSummary,
			Integer cardosoMetric,
			List<Pair<Place, Set<Set<Place>>>> cardosoCalculation,
			Integer cyclomatic,
			Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>> cyclomaticCalculation,
			Map<String, Component> transition2Component) {
		initializeGraphics();
		removeAll();
		JScrollPane summaryPane = createSummaryPane(wfnet, logSpecificMessage,
				showSummary);
		add("Summary", summaryPane);
		if (cardosoMetric != null && cardosoCalculation != null)
			add("Cardoso", createCardosoPane(cardosoMetric, cardosoCalculation));
		if (cyclomatic != null && cyclomaticCalculation != null)
			add("Cyclomatic", createCyclomaticPane(cyclomatic,
					cyclomaticCalculation));
		if (!reductions.isEmpty())
			add("Structuredness", createReductionsPane(wfnet,
					transition2Component));
		validate();
		repaint();
	}

	private java.awt.Component createCyclomaticPane(
			Integer cyclomatic,
			Quadruple<StateSpace, Integer, Integer, List<List<ModelGraphVertex>>> cyclomaticCalculation) {
		JTabbedPane tabs = new JTabbedPane();

		StringBuilder html = new StringBuilder();

		html.append("<html><head/><body>");
		html
				.append("<b>Extended Cyclomatic metric:</b> " + cyclomatic
						+ "<br>");
		html
				.append("Formula: |E| - |V| + p (edges - vertices + number of strongly connected components)");
		html
				.append("<table border=\"1\"><tr><th>Category</th><th>Value</th></tr>");
		html
				.append("<tr><td>Edges (E)</td><td>"
						+ cyclomaticCalculation.second);
		html.append("<tr><td>Vertices (V)</td><td>"
				+ cyclomaticCalculation.third);
		html.append("<tr><td>Components (p)</td><td><ol>");
		for (List<ModelGraphVertex> list : cyclomaticCalculation.fourth) {
			html.append("<li>{");
			boolean first = true;
			for (ModelGraphVertex state : list) {
				if (first)
					first = false;
				else
					html.append(",");
				html.append(state.toString());
			}
			html.append("}</li>");
		}
		html.append("</ol></td></tr></table>");
		html.append("</body></html>");

		tabs.add("Calculation", new JScrollPane(new JEditorPane("text/html",
				html.toString())));
		tabs.add("Reachability graph", cyclomaticCalculation.first
				.getGrappaVisualization());

		return tabs;
	}

	private java.awt.Component createCardosoPane(int cardosoMetric,
			List<Pair<Place, Set<Set<Place>>>> cardosoCalculation) {
		StringBuilder html = new StringBuilder();

		html.append("<html><head/><body>");
		html
				.append("<b>Extended Cardoso metric:</b> " + cardosoMetric
						+ "<br>");
		html
				.append("Formula: &#8721;<sub>p&#8712;P</sub> |{t&#8226;|t&#8712;p&#8226;}|");
		html
				.append("<table border=\"1\"><tr><th>Place</th><th>Count</th><th>Postsets</th></tr>");
		for (Pair<Place, Set<Set<Place>>> pair : cardosoCalculation) {
			html.append("<tr><td>" + pair.first.getIdentifier() + "</td><td>"
					+ pair.second.size() + "</td><td>");
			boolean first1 = true;
			for (Set<Place> set : pair.second) {
				if (!first1)
					html.append(",");
				else
					first1 = false;
				html.append("{");
				boolean first2 = true;
				for (Place place : set) {
					if (!first2)
						html.append(",");
					else
						first2 = false;
					html.append(place.getIdentifier());
				}
				html.append("}");
			}
			html.append("</td></tr>");
		}
		html.append("</table>");
		html.append("</body></html>");

		return new JScrollPane(new JEditorPane("text/html", html.toString()));
	}

	public LogReader getLogReader() {
		return null;
	}

	public JComponent getVisualization() {
		return this;
	}

	public ProvidedObject[] getProvidedObjects() {
		if (getSelectedIndex() == 3) {
			JTabbedPane subpane = (JTabbedPane) getSelectedComponent();
			if (subpane.getSelectedIndex() == 2)
				return new ProvidedObject[] { new ProvidedObject(
						"Selected Petri net", new Object[] { chosenWfnet }) };
			else if (subpane.getSelectedIndex() == 3)
				return new ProvidedObject[] { new ProvidedObject(
						"Selected Petri net", new Object[] { partWfnet[0] }) };
		}
		return new ProvidedObject[] {};
	}

	public Map<String, Integer> getUsedComponentCount() {
		return usedComponentCount;
	}

	public List<Reduction> getReductions() {
		return reductions;
	}

}
