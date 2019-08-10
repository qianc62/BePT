package org.processmining.analysis.socialnetwork;

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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GUIPropertySetEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.algorithms.cluster.ClusterSet;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.ArchetypeGraph;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.AbstractVertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStringer;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.EdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValueStringer;
import edu.uci.ics.jung.graph.decorators.NumberVertexValue;
import edu.uci.ics.jung.graph.decorators.NumberVertexValueStringer;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.UserDatumNumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.UserDatumNumberVertexValue;
import edu.uci.ics.jung.graph.decorators.VertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexSizeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.MultiPickedState;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.subLayout.CircularSubLayout;
import edu.uci.ics.jung.visualization.subLayout.SubLayout;
import edu.uci.ics.jung.visualization.subLayout.SubLayoutDecorator;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class SocialNetworkAnalysisUI extends JPanel {

	private SocialNetworkMatrix snMatrix;
	private JButton showNetButton = new JButton("Show social network");
	private JButton showCentralityButton = new JButton("Show centrality");

	private JScrollPane netContainer;
	private JScrollPane textContainer;
	private JSplitPane splitPane;

	// left part
	private JPanel buttonsPanel = new JPanel();
	private JPanel graphPanel = new JPanel();
	private JSplitPane leftSplitPane;
	private JTextArea resultArea;

	// right part
	private JSplitPane rightSplitPane;
	private JPanel menuPanel = new JPanel();
	private JTabbedPane tabbedPane = new JTabbedPane();

	// graph related variables
	private double inner_max = 0.0;
	private double freq_max = 0.0;

	private edu.uci.ics.jung.graph.Graph g = new DirectedSparseGraph();
	private VisualizationViewer vv = null;
	private PluggableRenderer pr = null;
	private NumberEdgeValue edge_weight = new UserDatumNumberEdgeValue(
			"edge_weight");
	private NumberVertexValue vertex_weight = new UserDatumNumberVertexValue(
			"vertex_weight");
	private EdgeStringer es = new NumberEdgeValueStringer(edge_weight);
	protected EdgeStringer es_none = new ConstantEdgeStringer(null);

	private VertexStringer vs = new NumberVertexValueStringer(vertex_weight);
	private DefaultModalGraphMouse gm;

	protected VertexShapeSizeAspect vssa;

	private SNAlgorithmJung algorithm = new SNAlgorithmJung(g, edge_weight);;

	private HashSet advancedSettingsFrames = new HashSet();

	private Vertex[] v = null; // vertex for originator
	private Vertex[] vr = null; // vertex for role
	private Vertex[] vo = null; // vertex for org unit

	// cluster and grouping
	private SubLayoutDecorator layout = new SubLayoutDecorator(new FRLayout(g));
	private PickedState ps = new MultiPickedState();
	private static final Object DEMOKEY = "DEMOKEY";

	final String CLUSTERSTRING = "Edges removed for clusters: ";

	public final Color[] similarColors = { new Color(216, 134, 134),
			new Color(135, 137, 211), new Color(134, 206, 189),
			new Color(206, 176, 134), new Color(194, 204, 134),
			new Color(145, 214, 134), new Color(133, 178, 209),
			new Color(103, 148, 255), new Color(60, 220, 220),
			new Color(30, 250, 100) };

	// GUI property
	// centrality
	private GUIPropertyListEnumeration centralityEnumeration = null;

	// vertex options
	private GUIPropertyBoolean myRoleModelSelected = new GUIPropertyBoolean(
			"Show role nodes", false, new OrgModelListener());
	private GUIPropertyBoolean myOrgUnitModelSelected = new GUIPropertyBoolean(
			"Show org unit nodes", false, new OrgUnitModelListener());
	private GUIPropertyBoolean myVertexSizeSelected = new GUIPropertyBoolean(
			"Vertex size", false, new VertexSizeListener());
	private GUIPropertySetEnumeration mySizePropertyEnumeration;

	private GUIPropertyBoolean myVertexStretchSelected = new GUIPropertyBoolean(
			"Vertex degree ratio stretch", false, new VertexStretchListener());
	// edge options
	private GUIPropertyBoolean myEdgeWeightSelected = new GUIPropertyBoolean(
			"Edge weight", false, new EdgeWeightListener());

	private JComboBox layoutBox = null;
	private JComboBox clusterBox = null;

	// cluster
	// private JComboBox clusterBox = null;
	private JSlider edgeBetweennessSlider = new JSlider(JSlider.HORIZONTAL);
	private JSlider edgeWeightSlider = new JSlider(JSlider.HORIZONTAL, 0, 1001,
			0);
	private GUIPropertyBoolean myClusterSelected = new GUIPropertyBoolean(
			"Group Clusters", false, new GroupClusterListener());
	private TitledBorder sliderWeightBorder;
	private TitledBorder sliderBetweennessBorder;
	private JPanel clusterWeightControlPanel = new JPanel();
	private JPanel clusterBetweennessControlPanel = new JPanel();

	public static final String ST_ClusterWeight = "Weight";
	public static final String ST_ClusterBetweenness = "Betweenness";

	// layout
	public static final String ST_KKLayout = "KKLayout";
	public static final String ST_CircleLayout = "CircleLayout";
	public static final String ST_FRLayout = "FRLayout";
	public static final String ST_SpringLayout = "SpringLayout";
	public static final String ST_ISOMLayout = "ISOMLayout";

	// centality
	public static final String ST_DEGREE = "Degree";
	public static final String ST_BETWEENNESS = "Betweenness";
	public static final String ST_BARYRANKER = "BaryRanker";
	public static final String ST_HITS = "HITS";

	// degree centrality
	public static final String INDEGREE = "In degree";
	public static final String OUTDEGREE = "Out degree";
	public static final String RATIO = "Ratio (otherwise Absolute value)";
	public static final String WEIGHT = "Consider weight";
	public static final String[] DEGREE_CENTRALITY_OPTIONS = { INDEGREE,
			OUTDEGREE, RATIO, WEIGHT };
	private boolean[] selectedDegreeOptions = { true, true, true, false };

	// node size property
	public static final String INTERNALFLOW = "Interner flow";
	public static final String FREQUENCY = "Frequency";

	// node property
	public static final Object ROLEKEY = "role";
	public static final Object ORGUNITKEY = "orgunit";
	public static final Object ORIKEY = "originator";
	public static final Object NODETYPE = "type";
	public static final Object SIZEPROPERTY = "sizeproperty";

	public SocialNetworkAnalysisUI(SocialNetworkMatrix snMatrix) {
		this.snMatrix = snMatrix;
		try {
			// Initialize Graph
			initGraph();
			// Initialize clustering
			initClustering();

			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {

		// left
		// init centrality menu
		ArrayList<String> values = new ArrayList<String>();
		values.add(ST_DEGREE);
		values.add(ST_BETWEENNESS);
		values.add(ST_BARYRANKER);
		values.add(ST_HITS);
		centralityEnumeration = new GUIPropertyListEnumeration("Centrality",
				values);

		buttonsPanel.add(centralityEnumeration.getPropertyPanel());
		buttonsPanel.add(showCentralityButton);

		resultArea = new JTextArea(20, 30);
		resultArea.setEditable(false);
		textContainer = new JScrollPane(resultArea);

		leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buttonsPanel,
				textContainer);

		// right part
		JPanel vertexEdgePanel = new JPanel();
		vertexEdgePanel.setLayout(new BoxLayout(vertexEdgePanel,
				BoxLayout.PAGE_AXIS));
		vertexEdgePanel.add(myRoleModelSelected.getPropertyPanel());
		if (!snMatrix.hasRoleModel())
			myRoleModelSelected.disable();
		vertexEdgePanel.add(myOrgUnitModelSelected.getPropertyPanel());
		if (!snMatrix.hasOrgUnitModel())
			myOrgUnitModelSelected.disable();

		HashSet<String> valuesForNodeSizeProperty = new HashSet<String>();
		valuesForNodeSizeProperty.add(INTERNALFLOW);
		valuesForNodeSizeProperty.add(FREQUENCY);
		mySizePropertyEnumeration = new GUIPropertySetEnumeration(
				"Size property:", valuesForNodeSizeProperty,
				new VertexSizePropertyListener());
		vertexEdgePanel.add(myVertexSizeSelected.getPropertyPanel());
		vertexEdgePanel.add(mySizePropertyEnumeration.getPropertyPanel());
		vertexEdgePanel.add(myVertexStretchSelected.getPropertyPanel());
		vertexEdgePanel.add(myEdgeWeightSelected.getPropertyPanel());
		menuPanel.add(vertexEdgePanel);

		// Layout
		String[] layoutStrings = { ST_KKLayout, ST_CircleLayout, ST_FRLayout,
				ST_SpringLayout, ST_ISOMLayout };
		layoutBox = new JComboBox(layoutStrings);
		layoutBox.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel layoutPanel = new JPanel(new BorderLayout()) {
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		layoutPanel.setBorder(BorderFactory.createTitledBorder("Layout"));
		layoutPanel.add(layoutBox);

		// mouse model
		JComboBox modeBox = gm.getModeComboBox();
		modeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel modePanel = new JPanel(new BorderLayout()) {
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		modePanel.add(modeBox);

		JPanel imsiPanel = new JPanel();
		imsiPanel.setLayout(new BoxLayout(imsiPanel, BoxLayout.PAGE_AXIS));
		imsiPanel.add(layoutPanel);
		imsiPanel.add(modePanel);
		menuPanel.add(imsiPanel);

		initWeightCluster();
		tabbedPane.add(clusterWeightControlPanel, ST_ClusterWeight);
		initBetweennessCluster();
		tabbedPane.add(clusterBetweennessControlPanel, ST_ClusterBetweenness);

		JPanel south = new JPanel();
		south.setLayout(new BoxLayout(south, BoxLayout.PAGE_AXIS));
		south.setAlignmentX(Component.LEFT_ALIGNMENT);
		south.add(myClusterSelected.getPropertyPanel());
		south.add(tabbedPane);
		// end cluster

		menuPanel.add(south);
		rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, menuPanel,
				new GraphZoomScrollPane(vv));

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane,
				rightSplitPane);
		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);

		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
		addListener();

	}

	public void initWeightCluster() {
		// Create slider to adjust the number of edges to remove when clustering
		// init edgeBetweennessSlider
		edgeWeightSlider.setBackground(Color.WHITE);
		edgeWeightSlider.setPreferredSize(new Dimension(210, 50));
		edgeWeightSlider.setPaintTicks(true);
		edgeWeightSlider.setValue(0);
		edgeWeightSlider.setPaintLabels(true);
		edgeWeightSlider.setPaintTicks(true);

		clusterWeightControlPanel.setOpaque(true);
		clusterWeightControlPanel.setLayout(new BoxLayout(
				clusterWeightControlPanel, BoxLayout.Y_AXIS));
		clusterWeightControlPanel.add(Box.createVerticalGlue());
		clusterWeightControlPanel.add(edgeWeightSlider);

		final String eastSize = CLUSTERSTRING + edgeWeightSlider.getValue();

		sliderWeightBorder = BorderFactory.createTitledBorder(eastSize);
		clusterWeightControlPanel.setBorder(sliderWeightBorder);
		clusterWeightControlPanel.add(Box.createVerticalGlue());

		clusterAndRecolorByEdgeWeight(layout, 0, similarColors,
				myClusterSelected.getValue());
		// end cluster
	}

	public void initBetweennessCluster() {
		// Create slider to adjust the number of edges to remove when clustering
		// init edgeBetweennessSlider
		edgeBetweennessSlider.setBackground(Color.WHITE);
		edgeBetweennessSlider.setPreferredSize(new Dimension(210, 50));
		edgeBetweennessSlider.setPaintTicks(true);
		edgeBetweennessSlider.setMaximum(g.numEdges());
		edgeBetweennessSlider.setMinimum(0);
		edgeBetweennessSlider.setValue(0);
		edgeBetweennessSlider.setMajorTickSpacing(10);
		edgeBetweennessSlider.setPaintLabels(true);
		edgeBetweennessSlider.setPaintTicks(true);

		clusterBetweennessControlPanel.setOpaque(true);
		clusterBetweennessControlPanel.setLayout(new BoxLayout(
				clusterBetweennessControlPanel, BoxLayout.Y_AXIS));
		clusterBetweennessControlPanel.add(Box.createVerticalGlue());

		clusterBetweennessControlPanel.add(edgeBetweennessSlider);

		final String eastSize2 = CLUSTERSTRING
				+ edgeBetweennessSlider.getValue();

		sliderBetweennessBorder = BorderFactory.createTitledBorder(eastSize2);
		clusterBetweennessControlPanel.setBorder(sliderBetweennessBorder);
		clusterBetweennessControlPanel.add(Box.createVerticalGlue());

		clusterAndRecolor(layout, 0, similarColors, myClusterSelected
				.getValue());
	}

	public void initGraph() {

		convertSNMatrixToGraph();

		// create a graphdraw
		vssa = new VertexShapeSizeAspect(vertex_weight, inner_max, freq_max);

		pr = new PluggableRenderer();
		pr.setVertexStringer(StringLabeller.getLabeller(g));

		pr.setEdgeStringer(es_none);

		pr.setVertexShapeFunction(vssa);

		vv = new VisualizationViewer(layout, pr);
		vv.getModel().setRelaxerThreadSleepTime(500);
		vv.setPickSupport(new ShapePickSupport());
		gm = new DefaultModalGraphMouse();

		vv.setGraphMouse(gm);
	}

	public void convertSNMatrixToGraph() {
		StringLabeller sl = StringLabeller.getLabeller(g);
		int nSize = snMatrix.getNodeNames().length;
		v = new Vertex[nSize];

		for (int i = 0; i < nSize; i++) {
			v[i] = g.addVertex(new SparseVertex());
			try {
				String nodeName = snMatrix.getNodeNames()[i];
				double freq = snMatrix.getFrequency(nodeName);
				sl.setLabel(v[i], nodeName);
				v[i].addUserDatum(NODETYPE, ORIKEY, UserData.CLONE);
				v[i].addUserDatum(SIZEPROPERTY, freq, UserData.REMOVE);
				if (freq_max < freq)
					freq_max = freq;
			} catch (StringLabeller.UniqueLabelException e) {
			}
		}

		if (snMatrix.hasRoleModel()) {
			for (int i = 0; i < nSize; i++) {
				v[i].addUserDatum(ROLEKEY, snMatrix.getRoleOfOriginator(i),
						UserData.CLONE);
			}

		}

		if (snMatrix.hasOrgUnitModel()) {
			for (int i = 0; i < nSize; i++) {
				v[i].addUserDatum(ORGUNITKEY, snMatrix
						.getOrgUnitOfOriginator(i), UserData.CLONE);
			}
		}

		DoubleMatrix2D matrix = snMatrix.getMatrix();

		for (int i = 0; i < nSize; i++) {
			for (int j = 0; j < nSize; j++) {
				if (i == j) {
					vertex_weight.setNumber(v[i], new Double(matrix.get(i, j)));
					if (inner_max < matrix.get(i, j))
						inner_max = matrix.get(i, j);
				}
				if (i != j && matrix.get(i, j) != 0) {
					Edge ed = new DirectedSparseEdge(v[i], v[j]);
					g.addEdge(ed);
					edge_weight.setNumber(ed, new Double(matrix.get(i, j)));
				}
			}
		}
	}

	public void showRoleOrgNodes() {
		int nSize = snMatrix.getNodeNames().length;
		int nOrgSize = 0;
		DoubleMatrix2D matrix = null;
		StringLabeller sl = StringLabeller.getLabeller(g);

		// add role nodes
		if (snMatrix.hasRoleModel()) {
			nOrgSize = snMatrix.getRoleName().size();
			if (vr == null) {
				vr = new Vertex[nOrgSize];
				for (int i = 0; i < nOrgSize; i++) {
					vr[i] = g.addVertex(new SparseVertex());
					try {
						sl.setLabel(vr[i], snMatrix.getRoleName().get(i));
						vertex_weight.setNumber(vr[i], new Double(0.0));
						vr[i].addUserDatum(NODETYPE, ROLEKEY, UserData.CLONE);
					} catch (StringLabeller.UniqueLabelException e) {
					}
				}
			} else {
				for (int i = 0; i < nOrgSize; i++) {
					g.addVertex(vr[i]);
				}
			}

			matrix = snMatrix.getRoleMatrix();
			for (int i = 0; i < nSize; i++) {
				for (int j = 0; j < nOrgSize; j++) {
					if (matrix.get(i, j) != 0) {
						Edge ed = new DirectedSparseEdge(v[i], vr[j]);
						g.addEdge(ed);
						edge_weight.setNumber(ed, new Double(matrix.get(i, j)));
					}
				}
			}

			// update clustring bar
			// edgeBetweennessSlider.setMaximum(g.numEdges());
			// edgeBetweennessSlider.repaint();

		}

	}

	public void hideRoleOrgNodes() {

		int nOrgSize = 0;

		if (snMatrix.hasRoleModel() && vr != null) {
			nOrgSize = snMatrix.getRoleName().size();
			for (int i = 0; i < nOrgSize; i++) {
				g.removeVertex(vr[i]);
			}
		}

		// update clustring bar
		// edgeBetweennessSlider.setMaximum(g.numEdges());
		// edgeBetweennessSlider.repaint();
	}

	public void showOrgUnitNodes() {
		int nSize = snMatrix.getNodeNames().length;
		int nOrgSize = 0;
		DoubleMatrix2D matrix = null;
		StringLabeller sl = StringLabeller.getLabeller(g);

		// add org unit nodes
		if (snMatrix.hasOrgUnitModel()) {
			nOrgSize = snMatrix.getOrgUnitName().size();
			if (vo == null) {
				vo = new Vertex[nOrgSize];
				for (int i = 0; i < nOrgSize; i++) {
					vo[i] = g.addVertex(new SparseVertex());
					try {
						sl.setLabel(vo[i], snMatrix.getOrgUnitName().get(i));
						vertex_weight.setNumber(vo[i], new Double(0.0));
						vo[i]
								.addUserDatum(NODETYPE, ORGUNITKEY,
										UserData.CLONE);
					} catch (StringLabeller.UniqueLabelException e) {
					}
				}
			} else {
				for (int i = 0; i < nOrgSize; i++) {
					g.addVertex(vo[i]);
				}
			}
		}

		matrix = snMatrix.getOrgUnitMatrix();
		for (int i = 0; i < nSize; i++) {
			for (int j = 0; j < nOrgSize; j++) {
				if (matrix.get(i, j) != 0) {
					Edge ed = new DirectedSparseEdge(v[i], vo[j]);
					g.addEdge(ed);
					edge_weight.setNumber(ed, new Double(matrix.get(i, j)));
				}
			}
		}

		// update clustring bar
		// edgeBetweennessSlider.setMaximum(g.numEdges());
		// edgeBetweennessSlider.repaint();
	}

	public void hideOrgUnitNodes() {

		int nOrgSize = 0;

		if (snMatrix.hasOrgUnitModel() && vo != null) {
			nOrgSize = snMatrix.getOrgUnitName().size();
			for (int i = 0; i < nOrgSize; i++) {
				g.removeVertex(vo[i]);
			}
		}

		// update clustring bar
		// edgeBetweennessSlider.setMaximum(g.numEdges());
		// edgeBetweennessSlider.repaint();
	}

	public void initClustering() {
		pr.setVertexPaintFunction(new VertexPaintFunction() {
			public Paint getFillPaint(Vertex v) {
				Color k = (Color) v.getUserDatum(DEMOKEY);
				if (v.getUserDatum(NODETYPE).equals(ROLEKEY))
					return Color.green;
				if (v.getUserDatum(NODETYPE).equals(ORGUNITKEY))
					return Color.magenta;
				if (k != null)
					return k;
				return Color.white;
			}

			public Paint getDrawPaint(Vertex v) {
				if (ps.isPicked(v)) {
					return Color.cyan;
				} else {
					return Color.BLACK;
				}
			}
		});

		pr.setEdgePaintFunction(new EdgePaintFunction() {
			public Paint getDrawPaint(Edge e) {
				Color k = (Color) e.getUserDatum(DEMOKEY);
				if (k != null)
					return k;
				return Color.blue;
			}

			public Paint getFillPaint(Edge e) {
				return null;
			}
		});

		pr.setEdgeStrokeFunction(new EdgeStrokeFunction() {
			protected final Stroke THIN = new BasicStroke(1);
			protected final Stroke THICK = new BasicStroke(2);

			public Stroke getStroke(Edge e) {
				Color c = (Color) e.getUserDatum(DEMOKEY);
				if (c == Color.LIGHT_GRAY)
					return THIN;
				else
					return THICK;
			}
		});
	}

	public void addListener() {
		showNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				remove(netContainer);

				netContainer = new JScrollPane(graphPanel);
				add(netContainer, BorderLayout.CENTER);

				validate();
				repaint();
			}
		});

		showCentralityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCentrality(centralityEnumeration.getValue().toString());
				;
			}
		});

		layoutBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeLayout();
			}
		});

		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane pane = (JTabbedPane) evt.getSource();
				if (tabbedPane.getSelectedIndex() == 0) {
					clusterAndRecolorByEdgeWeight(layout,
							getThresholdFromSlider(), similarColors,
							myClusterSelected.getValue());
				} else {
					clusterAndRecolor(layout, edgeBetweennessSlider.getValue(),
							similarColors, myClusterSelected.getValue());
				}
			}
		});

		edgeBetweennessSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int numEdgesToRemove = source.getValue();
					clusterAndRecolor(layout, numEdgesToRemove, similarColors,
							myClusterSelected.getValue());
					sliderBetweennessBorder.setTitle(CLUSTERSTRING
							+ edgeBetweennessSlider.getValue());
					clusterBetweennessControlPanel.repaint();
					vv.validate();
					vv.repaint();
				}
			}
		});
		// work start
		edgeWeightSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					double thresholdValue = getThresholdFromSlider();
					clusterAndRecolorByEdgeWeight(layout, thresholdValue,
							similarColors, myClusterSelected.getValue());
					String stThresholdValue = String
							.valueOf(getThresholdFromSlider());
					if (stThresholdValue.length() > 5)
						stThresholdValue = stThresholdValue.substring(0, 5);
					sliderWeightBorder.setTitle(CLUSTERSTRING
							+ stThresholdValue);
					clusterWeightControlPanel.repaint();
					vv.validate();
					vv.repaint();
				}
			}
		});
	}

	protected double getThresholdFromSlider() {
		double threshold = (double) edgeWeightSlider.getValue() / 1000.0;
		// normalize threshold to minimal node frequency
		threshold = (snMatrix.getMaxFlowValue() - snMatrix.getMinFlowValue())
				* threshold + snMatrix.getMinFlowValue();
		return threshold;
	}

	// clustering methods
	private void clusterAndRecolor(SubLayoutDecorator layout,
			int numEdgesToRemove, Color[] colors, boolean groupClusters) {

		Graph g = layout.getGraph();
		layout.removeAllSubLayouts();

		EdgeBetweennessClusterer clusterer = new EdgeBetweennessClusterer(
				numEdgesToRemove);
		ClusterSet clusterSet = clusterer.extract(g);
		List edges = clusterer.getEdgesRemoved();

		int i = 0;
		// Set the colors of each node so that each cluster's vertices have the
		// same color
		for (Iterator cIt = clusterSet.iterator(); cIt.hasNext();) {

			Set vertices = (Set) cIt.next();
			Color c = colors[i % colors.length];

			colorCluster(vertices, c);
			if (groupClusters == true) {
				groupCluster(layout, vertices);
			}
			i++;
		}
		for (Iterator it = g.getEdges().iterator(); it.hasNext();) {
			Edge e = (Edge) it.next();
			if (edges.contains(e)) {
				e.setUserDatum(DEMOKEY, Color.LIGHT_GRAY, UserData.REMOVE);
			} else {
				e.setUserDatum(DEMOKEY, Color.BLACK, UserData.REMOVE);
			}
		}

	}

	// edge weight based clusters
	private void clusterAndRecolorByEdgeWeight(SubLayoutDecorator layout,
			double thresholdvalue, Color[] colors, boolean groupClusters) {

		Graph g = layout.getGraph();
		layout.removeAllSubLayouts();

		/*
		 * EdgeBetweennessClusterer clusterer = new
		 * EdgeBetweennessClusterer(numEdgesToRemove); ClusterSet clusterSet =
		 * clusterer.extract(g); List edges = clusterer.getEdgesRemoved();
		 */
		ClusterSet clusterSet = extractbyWeight(g, thresholdvalue);
		List edges = extractbyWeightList(g, thresholdvalue);

		int i = 0;
		// Set the colors of each node so that each cluster's vertices have the
		// same color
		for (Iterator cIt = clusterSet.iterator(); cIt.hasNext();) {

			Set vertices = (Set) cIt.next();
			Color c = colors[i % colors.length];

			colorCluster(vertices, c);
			if (groupClusters == true) {
				groupCluster(layout, vertices);
			}
			i++;
		}
		for (Iterator it = g.getEdges().iterator(); it.hasNext();) {
			Edge e = (Edge) it.next();
			if (edges.contains(e)) {
				e.setUserDatum(DEMOKEY, Color.LIGHT_GRAY, UserData.REMOVE);
			} else {
				e.setUserDatum(DEMOKEY, Color.BLACK, UserData.REMOVE);
			}
		}

	}

	public ClusterSet extractbyWeight(ArchetypeGraph g, double thresholdvalue) {// ,
		// List
		// edges)
		// {

		if (!(g instanceof Graph))
			throw new IllegalArgumentException(
					"Argument must be of type Graph.");

		Graph graph = (Graph) g;

		List edgesRemoved = new ArrayList();
		edgesRemoved.clear();
		for (Iterator it = g.getEdges().iterator(); it.hasNext();) {
			Edge tempEdge = (Edge) it.next();
			if (edge_weight.getNumber(tempEdge).doubleValue() < thresholdvalue) {
				edgesRemoved.add(tempEdge.getEqualEdge(graph));
			}
		}

		for (Iterator it = edgesRemoved.iterator(); it.hasNext();) {
			graph.removeEdge((Edge) it.next());
		}

		// edges = mEdgesRemoved;

		WeakComponentClusterer wcSearch = new WeakComponentClusterer();
		ClusterSet clusterSet = wcSearch.extract(graph);
		for (Iterator iter = edgesRemoved.iterator(); iter.hasNext();)
			graph.addEdge((Edge) iter.next());

		return clusterSet;
	}

	public List extractbyWeightList(ArchetypeGraph g, double thresholdvalue) {// ,
		// List
		// edges)
		// {

		if (!(g instanceof Graph))
			throw new IllegalArgumentException(
					"Argument must be of type Graph.");

		Graph graph = (Graph) g;

		List mEdgesRemoved = new ArrayList();
		for (Iterator it = g.getEdges().iterator(); it.hasNext();) {
			Edge tempEdge = (Edge) it.next();
			if (edge_weight.getNumber(tempEdge).doubleValue() < thresholdvalue) {
				mEdgesRemoved.add(tempEdge.getEqualEdge(graph));
			}
		}
		return mEdgesRemoved;
	}

	// / work end
	private void colorCluster(Set vertices, Color c) {
		for (Iterator iter = vertices.iterator(); iter.hasNext();) {
			Vertex v = (Vertex) iter.next();
			v.setUserDatum(DEMOKEY, c, UserData.REMOVE);
		}
	}

	private void groupCluster(SubLayoutDecorator layout, Set vertices) {
		if (vertices.size() < layout.getGraph().numVertices()) {
			Point2D center = layout.getLocation((ArchetypeVertex) vertices
					.iterator().next());
			SubLayout subLayout = new CircularSubLayout(vertices, 20, center);
			layout.addSubLayout(subLayout);
		}
	}

	public void showDegreePanel() {
		DegreeCheckBoxPanel degreecheckBoxPanel = new DegreeCheckBoxPanel(this,
				selectedDegreeOptions);
		MainUI.getInstance().createFrame("Degree options", degreecheckBoxPanel);
		JInternalFrame advancedSettingsFrame = MainUI.getInstance()
				.getDesktop().getSelectedFrame();
		advancedSettingsFrame.setMinimumSize(new Dimension(250, 1800));
		advancedSettingsFrame.setSize(new Dimension(250, 180));
		advancedSettingsFrames.add(advancedSettingsFrame);
		degreecheckBoxPanel.setInternalFrame(advancedSettingsFrame);
	}

	public void showCentrality(String st_centrality) {
		if (st_centrality == ST_DEGREE)
			showDegreePanel();
		else if (st_centrality == ST_BETWEENNESS)
			resultArea.append(algorithm.calculateBetweenness());
		else if (st_centrality == ST_BARYRANKER)
			resultArea.append(algorithm.calculateBaryRanker());
		else if (st_centrality == ST_HITS)
			resultArea.append(algorithm.calculateHITS());
	}

	public void changeLayout() {
		vv.stop();
		String st_layout = (String) layoutBox.getSelectedItem();

		if (st_layout == ST_KKLayout)
			layout = new SubLayoutDecorator(new KKLayout(g));
		else if (st_layout == ST_CircleLayout)
			layout = new SubLayoutDecorator(new CircleLayout(g));
		else if (st_layout == ST_FRLayout)
			layout = new SubLayoutDecorator(new FRLayout(g));
		else if (st_layout == ST_SpringLayout)
			layout = new SubLayoutDecorator(
					new edu.uci.ics.jung.visualization.SpringLayout(g));
		else if (st_layout == ST_ISOMLayout)
			layout = new SubLayoutDecorator(new ISOMLayout(g));

		vv.setGraphLayout(layout);
		vv.restart();

		clusterAndRecolorByEdgeWeight(layout, getThresholdFromSlider(),
				similarColors, myClusterSelected.getValue());
		// to do
	}

	/*
	 * public void changeCluster() { String st_layout = (String)
	 * clusterBox.getSelectedItem(); clusterControlPanel = null;
	 * clusterControlPanel = new JPanel();
	 * 
	 * if(st_layout.equals("Weight")){ initCluster(); } else initBetweenness();
	 * clusterControlPanel.re vv.setGraphLayout(layout); vv.restart();
	 * clusterAndRecolorByEdgeWeight(layout, edgeBetweennessSlider.getValue(),
	 * similarColors,myClusterSelected.getValue()); // to do }
	 */

	public void calculateDegree() {
		resultArea.append(algorithm.calculateDegree(selectedDegreeOptions));
	}

	/**
	 * Closes all advanced settings frames that were opened from this social
	 * network analysis
	 */
	public void closeAdvancedFrames() {
		try {
			// close all advanced settings screens
			Iterator it = advancedSettingsFrames.iterator();
			while (it.hasNext()) {
				JInternalFrame frame = (JInternalFrame) it.next();
				frame.doDefaultCloseAction();
			}
			advancedSettingsFrames.clear();
		} catch (Exception ex) {
		}
	}

	/**
	 * Closes an advanced settings frames that was opened from this social
	 * network analysis
	 */
	public void closeAdvancedFrame(JInternalFrame internalFrame) {

		try {
			advancedSettingsFrames.remove(internalFrame);
			internalFrame.doDefaultCloseAction();
		} catch (Exception ex) {
		}
	}

	/**
	 * Class handling the selection state change of the time check button.
	 */
	class VertexSizeListener implements GuiNotificationTarget {
		public void updateGUI() {
			vssa.setScaling(myVertexSizeSelected.getValue());
			vv.repaint();
		}
	}

	/**
	 * Class handling the selection state change of the time check button.
	 */
	class VertexSizePropertyListener implements GuiNotificationTarget {
		public void updateGUI() {
			vssa.setScalingProperty((String) mySizePropertyEnumeration
					.getValue());
			vv.repaint();
		}
	}

	/**
	 * Class handling the selection state change of the time check button.
	 */
	class VertexStretchListener implements GuiNotificationTarget {
		public void updateGUI() {
			vssa.setStretch(myVertexStretchSelected.getValue());
			vv.repaint();
		}
	}

	/**
	 * Class handling the selection state change of the time check button.
	 */
	class OrgModelListener implements GuiNotificationTarget {
		public void updateGUI() {
			if (myRoleModelSelected.getValue())
				showRoleOrgNodes();
			else
				hideRoleOrgNodes();
			vv.stop();
			vv.restart();
		}
	}

	/**
	 * Class handling the selection state change of the time check button.
	 */
	class OrgUnitModelListener implements GuiNotificationTarget {
		public void updateGUI() {
			if (myOrgUnitModelSelected.getValue())
				showOrgUnitNodes();
			else
				hideOrgUnitNodes();

			vv.stop();
			vv.restart();
		}
	}

	/**
	 * Class handling the selection state change of the time check button.
	 */
	class EdgeWeightListener implements GuiNotificationTarget {
		public void updateGUI() {
			if (myEdgeWeightSelected.getValue())
				pr.setEdgeStringer(es);
			else
				pr.setEdgeStringer(es_none);
			vv.repaint();
		}
	}

	/**
	 * Class handling the selection state change of the time check button.
	 */
	class GroupClusterListener implements GuiNotificationTarget {
		public void updateGUI() {
			if (tabbedPane.getSelectedIndex() == 0) {
				clusterAndRecolorByEdgeWeight(layout, getThresholdFromSlider(),
						similarColors, myClusterSelected.getValue());
			} else {
				clusterAndRecolor(layout, edgeBetweennessSlider.getValue(),
						similarColors, myClusterSelected.getValue());
			}
		}
	}

	/**
	 * Controls the shape, size, and aspect ratio for each vertex.
	 * 
	 * @author Joshua O'Madadhain
	 */
	private final static class VertexShapeSizeAspect extends
			AbstractVertexShapeFunction implements VertexSizeFunction,
			VertexAspectRatioFunction {
		protected boolean scale = false;
		protected boolean stretch = false;
		protected boolean funny_shapes = false;
		protected NumberVertexValue vertex_weight;
		protected double inner_max;
		protected double freq_max;
		protected String sizeProperty = SocialNetworkAnalysisUI.FREQUENCY;

		public VertexShapeSizeAspect(NumberVertexValue vertex_weight,
				double inner_max, double freq_max) {
			this.vertex_weight = vertex_weight;
			this.inner_max = inner_max;
			this.freq_max = freq_max;
			setSizeFunction(this);
			setAspectRatioFunction(this);
		}

		public void setScaling(boolean scale) {
			this.scale = scale;
		}

		public void setStretch(boolean stretch) {
			this.stretch = stretch;
		}

		public void setScalingProperty(String property) {
			this.sizeProperty = property;
		}

		public void useFunnyShapes(boolean use) {
			this.funny_shapes = use;
		}

		public int getSize(Vertex v) {
			if (scale)
				if (sizeProperty.endsWith(INTERNALFLOW))
					return 20 + (int) (vertex_weight.getNumber(v).floatValue()
							/ inner_max * 40);
				else {
					Double value = 0.0;
					if (v.getUserDatum(NODETYPE).equals(ORIKEY))
						value = (Double) v.getUserDatum(SIZEPROPERTY);
					return 20 + (int) ((double) value / freq_max * 40);
				}
			else
				return 20;
		}

		public float getAspectRatio(Vertex v) {
			if (stretch)
				return (float) (v.inDegree() + 1) / (v.outDegree() + 1);
			else
				return 1.0f;
		}

		public Shape getShape(Vertex v) {
			if (funny_shapes) {
				if (v.degree() < 5) {
					int sides = Math.max(v.degree(), 3);
					return factory.getRegularPolygon(v, sides);
				} else
					return factory.getRegularStar(v, v.degree());
			} else
				return factory.getEllipse(v);
		}
	}

}
