package org.processmining.analysis.sequenceclustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.gantzgraf.ui.GGGraphView;
import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SmoothPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.analysis.clustering.model.LogSequence;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.slicker.logdialog.InspectorUI;
import org.processmining.framework.ui.slicker.logdialog.SlickerOpenLogSettings;
import org.processmining.framework.util.Dot;

import att.grappa.Graph;
import att.grappa.GrappaPanel;

/**
 * @author Gabriel Veiga, IST - Technical University of Lisbon
 * @author Supervisor: Prof. Diogo Ferreira
 */
public class MarkovChain extends JPanel {

	protected Color colorEnclosureBg = new Color(40, 40, 40);
	protected Color colorNonFocus = new Color(70, 70, 70);
	protected Color colorListBg = new Color(60, 60, 60);
	protected Color colorListBgLower = new Color(45, 45, 45);
	protected Color colorListFg = new Color(180, 180, 180);
	protected Color colorListSelectionBg = new Color(80, 0, 0);
	protected Color colorListSelectionBgLower = new Color(30, 10, 10);
	protected Color colorListSelectionFg = new Color(240, 240, 240);
	protected Color labelColor = new Color(30, 30, 30);
	protected Color backGroundColor = new Color(20, 20, 20);
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);

	protected static Color panelBackgroundColor = new Color(140, 140, 140, 140);
	protected static Color colorBg = new Color(120, 120, 120);
	protected static Color colorFg = new Color(30, 30, 30);

	protected static DecimalFormat format = new DecimalFormat("0.000");

	protected InspectorUI inspectorUI;

	protected JList clustersList;

	protected JLabel clusterNameLabel;
	protected JLabel clusterSizeLabel;
	protected JLabel midNodeLabel, midEdgeLabel;
	protected JLabel edgeLabel, edgeInvertedLabel, nodeLabel,
			nodeInvertedLabel;
	protected JSlider edgeSlider, edgeInvertedSlider, nodeSlider,
			nodeInvertedSlider;

	protected FlatTabbedPane tabPane;

	protected SlickerOpenLogSettings parent;
	protected LogReader log = null;
	protected int numClusters;

	protected double minEventSupport;
	protected double nodeSignificance = 0.0;
	protected double edgeSignificance = 0.0;
	protected double nodeInvertedSignificance = 1.0;
	protected double edgeInvertedSignificance = 1.0;
	protected LogSummary logSummary;
	protected LogSequence logS = null;

	protected SCLogFilter filter = null;
	protected ArrayList<LogEvent> events = new ArrayList<LogEvent>();
	protected ArrayList<String> instancesToRemove = new ArrayList<String>();
	protected Cluster currentCluster;

	protected SCAlgorithm scAlgorithm;
	protected JScrollPane graphScrollPane;
	protected GGGraphView graphPanel = null;

	public MarkovChain(LogReader log, int numberClusters, SCAlgorithm scAlg) {
		this.log = log;
		this.numClusters = numberClusters;
		this.scAlgorithm = scAlg;

		this.setOpaque(false);
		this.setBackground(backGroundColor);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				updateView();
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});

		tabPane = new FlatTabbedPane("Sequence Clustering", new Color(240, 240,
				240, 230), new Color(180, 180, 180, 120), new Color(220, 220,
				220, 150));
		// create clusters list
		clustersList = new JList();
		clustersList.setBackground(colorListBg);
		clustersList.setForeground(colorListFg);
		clustersList.setSelectionBackground(colorListSelectionBg);
		clustersList.setSelectionForeground(colorListSelectionFg);
		clustersList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		clustersList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				clustersSelectionChanged();
			}
		});
		JScrollPane clustersScrollPane = new JScrollPane(clustersList);
		clustersScrollPane.setOpaque(false);
		clustersScrollPane.setBorder(BorderFactory.createEmptyBorder());
		clustersScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		clustersScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar vBar = clustersScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), colorNonFocus, 4, 12));
		vBar.setOpaque(false);

		// assemble clusters list
		JLabel clustersListLabel = new JLabel("Clusters");
		clustersListLabel.setOpaque(false);
		clustersListLabel.setForeground(colorListSelectionFg);
		clustersListLabel.setFont(clustersListLabel.getFont().deriveFont(13f));
		clustersListLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		clustersListLabel.setHorizontalAlignment(JLabel.CENTER);
		clustersListLabel.setHorizontalTextPosition(JLabel.CENTER);

		RoundedPanel clustersPanel = new RoundedPanel(10, 3, 0);
		clustersPanel.setBackground(colorEnclosureBg);
		clustersPanel.setLayout(new BoxLayout(clustersPanel, BoxLayout.Y_AXIS));
		clustersPanel.add(clustersListLabel);
		clustersPanel.add(Box.createVerticalStrut(8));
		clustersPanel.add(clustersScrollPane);

		clustersPanel.setMinimumSize(new Dimension(180, 100));
		clustersPanel.setMaximumSize(new Dimension(300, 1000));
		clustersPanel.setPreferredSize(new Dimension(200, 500));

		// create cluster list header
		clusterNameLabel = new JLabel("(no cluster selected)");
		clusterNameLabel.setOpaque(false);
		clusterNameLabel.setForeground(colorNonFocus);
		clusterNameLabel.setFont(clusterNameLabel.getFont().deriveFont(13f));
		clusterNameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		clusterNameLabel.setHorizontalAlignment(JLabel.CENTER);
		clusterNameLabel.setHorizontalTextPosition(JLabel.CENTER);
		clusterSizeLabel = new JLabel("select single cluster to browse");
		clusterSizeLabel.setOpaque(false);
		clusterSizeLabel.setForeground(colorNonFocus);
		clusterSizeLabel.setFont(clusterSizeLabel.getFont().deriveFont(11f));
		clusterSizeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		clusterSizeLabel.setHorizontalAlignment(JLabel.CENTER);
		clusterSizeLabel.setHorizontalTextPosition(JLabel.CENTER);

		// create the Markov chain menu

		graphScrollPane = new JScrollPane(new JPanel());
		graphScrollPane.setBorder(BorderFactory.createEmptyBorder());

		JPanel emptyPanel = new JPanel();
		emptyPanel.setBackground(new Color(100, 100, 100));
		emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
		JPanel innerPanel = new JPanel();
		innerPanel.setOpaque(false);
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
		JLabel emptyLabel = new JLabel("choose a cluster to visualize");
		emptyLabel.setOpaque(false);
		innerPanel.add(Box.createHorizontalGlue());
		innerPanel.add(emptyLabel);
		innerPanel.add(Box.createHorizontalGlue());
		emptyPanel.add(Box.createVerticalGlue());
		emptyPanel.add(innerPanel);
		emptyPanel.add(Box.createVerticalGlue());

		graphScrollPane.getViewport().setView(emptyPanel);

		JPanel controlPanel = new SmoothPanel();
		controlPanel.setMinimumSize(new Dimension(210, 200)); // 160
		controlPanel.setMaximumSize(new Dimension(210, 2000));
		controlPanel.setPreferredSize(new Dimension(210, 500));
		controlPanel.setBackground(colorBg);
		controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		controlPanel.setLayout(new BorderLayout());

		JPanel nodePanel = new SmoothPanel();
		nodePanel.setMinimumSize(new Dimension(90, 200));
		nodePanel.setMaximumSize(new Dimension(90, 2000));
		nodePanel.setPreferredSize(new Dimension(90, 500));
		nodePanel.setBackground(colorBg);
		nodePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		nodePanel.setLayout(new BorderLayout());

		JPanel edgePanel = new SmoothPanel();
		edgePanel.setMinimumSize(new Dimension(90, 200));
		edgePanel.setMaximumSize(new Dimension(90, 2000));
		edgePanel.setPreferredSize(new Dimension(90, 500));
		edgePanel.setBackground(colorBg);
		edgePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		edgePanel.setLayout(new BorderLayout());

		JLabel header = new JLabel("Threshold:");
		header.setOpaque(false);
		header.setForeground(colorFg);
		header.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		JLabel nodeHeader = new JLabel("Node");
		nodeHeader.setOpaque(false);
		nodeHeader.setForeground(colorFg);
		nodeHeader.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		JLabel edgeHeader = new JLabel("Edge");
		edgeHeader.setOpaque(false);
		edgeHeader.setForeground(colorFg);
		edgeHeader.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		nodeLabel = new JLabel(format.format(0.0));
		nodeLabel.setOpaque(false);
		nodeLabel.setForeground(colorFg);

		midNodeLabel = new JLabel("   ");
		midNodeLabel.setOpaque(false);
		midNodeLabel.setForeground(colorFg);

		midEdgeLabel = new JLabel("   ");
		midEdgeLabel.setOpaque(false);
		midEdgeLabel.setForeground(colorFg);

		nodeInvertedLabel = new JLabel(format.format(0.0));
		nodeInvertedLabel.setOpaque(false);
		nodeInvertedLabel.setForeground(colorFg);

		edgeLabel = new JLabel(format.format(0.0));
		edgeLabel.setOpaque(false);
		edgeLabel.setForeground(colorFg);

		edgeInvertedLabel = new JLabel(format.format(0.0));
		edgeInvertedLabel.setOpaque(false);
		edgeInvertedLabel.setForeground(colorFg);

		nodeSlider = new JSlider();
		nodeSlider.setAlignmentX(JSlider.LEFT_ALIGNMENT);
		nodeSlider.setMinimum(0);
		nodeSlider.setMaximum(1000);
		nodeSlider.setValue(0);
		nodeSlider.setOrientation(JSlider.VERTICAL);
		nodeSlider.setOpaque(false);
		nodeSlider.setEnabled(false);
		nodeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = (double) nodeSlider.getValue() / 1000.0;
				nodeLabel.setText(format.format(value));
				if (nodeSlider.getValueIsAdjusting() == false) {
					// slider released
					nodeSignificance = value;
					clustersSelectionChanged();
				}
			}
		});

		nodeInvertedSlider = new JSlider();
		nodeInvertedSlider.setAlignmentX(JSlider.RIGHT_ALIGNMENT);
		nodeInvertedSlider.setMinimum(0);
		nodeInvertedSlider.setMaximum(1000);
		nodeInvertedSlider.setValue(1000);
		nodeInvertedSlider.setOrientation(JSlider.VERTICAL);
		nodeInvertedSlider.setOpaque(false);
		nodeInvertedSlider.setEnabled(false);
		nodeInvertedLabel.setText(format.format(1.000));
		nodeInvertedSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = (double) nodeInvertedSlider.getValue() / 1000.0;
				nodeInvertedLabel.setText(format.format(value));
				if (nodeInvertedSlider.getValueIsAdjusting() == false) {
					// slider released
					nodeInvertedSignificance = value;
					clustersSelectionChanged();
				}
			}
		});

		edgeSlider = new JSlider();
		edgeSlider.setAlignmentX(JSlider.LEFT_ALIGNMENT);
		edgeSlider.setMinimum(0);
		edgeSlider.setMaximum(1000);
		edgeSlider.setValue(0);
		edgeSlider.setOrientation(JSlider.VERTICAL);
		edgeSlider.setOpaque(false);
		edgeSlider.setEnabled(false);
		edgeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = (double) edgeSlider.getValue() / 1000.0;
				edgeLabel.setText(format.format(value));
				if (edgeSlider.getValueIsAdjusting() == false) {
					// slider released
					edgeSignificance = value;
					clustersSelectionChanged();
				}
			}
		});

		edgeInvertedSlider = new JSlider();
		edgeInvertedSlider.setAlignmentX(JSlider.RIGHT_ALIGNMENT);
		edgeInvertedSlider.setMinimum(0);
		edgeInvertedSlider.setMaximum(1000);
		edgeInvertedSlider.setValue(1000);
		edgeInvertedSlider.setOrientation(JSlider.VERTICAL);
		edgeInvertedSlider.setOpaque(false);
		edgeInvertedSlider.setEnabled(false);
		edgeInvertedLabel.setText(format.format(1.000));
		edgeInvertedSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = (double) edgeInvertedSlider.getValue() / 1000.0;
				edgeInvertedLabel.setText(format.format(value));
				if (edgeInvertedSlider.getValueIsAdjusting() == false) {
					// slider released
					edgeInvertedSignificance = value;
					clustersSelectionChanged();
				}
			}
		});

		JPanel lowerNodePanel = new JPanel();
		lowerNodePanel
				.setLayout(new BoxLayout(lowerNodePanel, BoxLayout.X_AXIS));
		lowerNodePanel.setOpaque(false);
		lowerNodePanel.add(nodeLabel, BorderLayout.WEST);
		lowerNodePanel.add(midNodeLabel, BorderLayout.CENTER);
		lowerNodePanel.add(nodeInvertedLabel, BorderLayout.EAST);

		JPanel lowerEdgePanel = new JPanel();
		lowerEdgePanel
				.setLayout(new BoxLayout(lowerEdgePanel, BoxLayout.X_AXIS));
		lowerEdgePanel.setOpaque(false);
		lowerEdgePanel.add(edgeLabel, BorderLayout.WEST);
		lowerEdgePanel.add(midEdgeLabel, BorderLayout.CENTER);
		lowerEdgePanel.add(edgeInvertedLabel, BorderLayout.EAST);

		nodePanel.add(nodeHeader, BorderLayout.NORTH);
		nodePanel.add(nodeSlider, BorderLayout.WEST);
		nodePanel.add(nodeInvertedSlider, BorderLayout.EAST);
		nodePanel.add(lowerNodePanel, BorderLayout.SOUTH);

		edgePanel.add(edgeHeader, BorderLayout.NORTH);
		edgePanel.add(edgeSlider, BorderLayout.WEST);
		edgePanel.add(edgeInvertedSlider, BorderLayout.EAST);
		edgePanel.add(lowerEdgePanel, BorderLayout.SOUTH);

		controlPanel.add(header, BorderLayout.NORTH);
		controlPanel.add(nodePanel, BorderLayout.WEST);
		controlPanel.add(edgePanel, BorderLayout.EAST);

		// assemble GUI
		this.add(clustersPanel);
		this.add(graphScrollPane, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.EAST);
	}

	protected JComponent packLeftAligned(JComponent comp) {
		Box enclosure = Box.createHorizontalBox();
		enclosure.setOpaque(false);
		enclosure.add(comp);
		enclosure.add(Box.createHorizontalGlue());
		return enclosure;
	}

	protected void clustersSelectionChanged() {
		int[] selectedIndices = clustersList.getSelectedIndices();
		if (selectedIndices.length == 0 || selectedIndices.length > 1) {
			clusterNameLabel.setForeground(colorNonFocus);
			clusterNameLabel.setText("(no cluster selected)");
			clusterSizeLabel.setForeground(colorNonFocus);
			clusterSizeLabel.setText("select single cluster to browse");
		} else {
			Cluster cluster = scAlgorithm.clusterList.get(selectedIndices[0]);
			currentCluster = cluster;
			List<ProcessInstance> piList = cluster.getLog().getInstances();
			clusterNameLabel.setForeground(colorListSelectionFg);
			clusterNameLabel.setText(cluster.getName());
			clusterSizeLabel.setForeground(colorListSelectionFg);
			clusterSizeLabel.setText(piList.size() + " instances");
		}
		showSelectedclusterData();
	}

	protected void showSelectedclusterData() {
		int index;
		int[] selectedIndices = clustersList.getSelectedIndices();

		if (selectedIndices.length == 1) {
			index = selectedIndices[0];
			updateUI(index);
		}
	}

	public ActionListener getActivationListener() {
		ActionListener activationListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateView();
			}
		};
		return activationListener;
	}

	public void updateUI(int index) {
		try {
			graphScrollPane.getViewport()
					.setView(
							getGraphPanel(index, nodeSignificance,
									edgeSignificance, nodeInvertedSignificance,
									edgeInvertedSignificance));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JPanel getGraphPanel(int index, double nodeSignificance,
			double edgeSignificance, double nodeInvertedSignificance,
			double edgeInvertedSignificance) throws Exception {
		File dotFile = File.createTempFile("pmt", ".dot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(dotFile, false));
		scAlgorithm.clusterList.get(index).writeToDot(bw, nodeSignificance,
				edgeSignificance, nodeInvertedSignificance,
				edgeInvertedSignificance);
		bw.close();
		Graph graph = Dot.execute(dotFile.getAbsolutePath());
		dotFile.deleteOnExit();
		graph.setEditable(true);
		graph.setMenuable(true);
		graph.setErrorWriter(new PrintWriter(System.err, true));
		GrappaPanel gp = new GrappaPanel(graph);
		gp.setBackgroundColor(new Color(100, 100, 100));
		gp.setScaleToFit(true);

		nodeSlider.setEnabled(true);
		nodeInvertedSlider.setEnabled(true);
		edgeSlider.setEnabled(true);
		edgeInvertedSlider.setEnabled(true);

		return gp;
	}

	protected void updateView() {

		logSummary = log.getLogSummary();
		String[] clusterNames = new String[numClusters];
		for (int i = 0; i < clusterNames.length; i++) {
			clusterNames[i] = scAlgorithm.clusterList.get(i).getName() + "  ("
					+ scAlgorithm.clusterList.get(i).instancesToKeep.size()
					+ " Instances)";

		}
		clustersList.setListData(clusterNames);
		clustersList.clearSelection();
		revalidate();
		repaint();
	}

	public LogReader getResultReader() {
		if (log == null || clustersList.getSelectedIndices().length == 0) {
			return null;
		} else {
			try {
				return LogReaderFactory.createInstance(log, clustersList
						.getSelectedIndices());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public JComponent getclusters() {
		updateView();
		return this;
	}

}