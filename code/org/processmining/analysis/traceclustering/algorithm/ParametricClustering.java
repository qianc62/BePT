package org.processmining.analysis.traceclustering.algorithm;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.traceclustering.distance.DistanceMetric;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.Dot;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

import att.grappa.Graph;
import att.grappa.GrappaPanel;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Minseok Song
 */
public class ParametricClustering extends ClusteringAlgorithm implements
		DotFileWriter, GuiNotificationTarget {

	protected ArrayList<Integer> traceList;
	protected int traceSize;
	protected ArrayList<Cluster> clusterList;
	protected ArrayList<Integer> frequencyList;
	protected ClusterSet clusters;
	protected ClusterSet clustersforOthers;
	protected AggregateProfile agProfiles;
	protected InstancePoint[] instancePoints;
	protected DistanceMetric distanceMeasures;
	protected DoubleMatrix2D distanceMatrix;
	protected DoubleMatrix2D distances;
	protected int clusterSize;
	protected static String ST_UNIT = "the same unit size";
	protected static String ST_NUMBER = "the same unit number";

	// GUI
	protected JPanel rootPanel;
	protected JScrollPane graphScrollPane;
	protected GUIPropertyInteger numberOfCluster = new GUIPropertyInteger(
			"Number of Clusters= ", 3, 1, Integer.MAX_VALUE);
	protected GUIPropertyListEnumeration selectItemBox;
	protected GUIPropertyListEnumeration selectMethodmBox;
	protected GUIPropertyBoolean hideNullCluster = new GUIPropertyBoolean(
			"Hide null clusters", true, this);
	protected JButton startButton;
	protected ProgressPanel progress;
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);

	public ParametricClustering() {
		super("Parametric Clustering",
				"Parametric Clustering allows the user to specify"
						+ " the number of clusters. The algorithm will return"
						+ " the number of clusters which users want.");
		clusters = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.traceclustering.algorithm.ClusteringAlgorithm
	 * #getClusters()
	 */
	@Override
	public ClusterSet getClusters() {
		return clustersforOthers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.traceclustering.algorithm.ClusteringAlgorithm
	 * #getUI()
	 */
	@Override
	public JComponent getUI() {
		clusters = null;
		agProfiles = (AggregateProfile) input.getProfile();
		distanceMeasures = input.getDistanceMetric();
		rootPanel = null;
		traceSize = input.getLog().numberOfInstances();
		setupGUI();
		return rootPanel;
	}

	public void cluster() {
		// prepare progress display
		progress = new ProgressPanel("Clustering");
		progress.setNote("processing...");
		graphScrollPane.getViewport().setView(progress.getPanel());

		progress.setMinMax(0, agProfiles.numberOfInstances());
		progress.setProgress(0);
		graphScrollPane.getViewport().setView(progress.getPanel());
		Thread clusterThread = new Thread() {
			public void run() {
				// do cluster
				build();
				// update ui
				updateUI();
				startButton.setEnabled(true);
			}
		};
		startButton.setEnabled(false);
		clusterThread.start();
	}

	public void build() {
		// initalize variables

		clusters = new ClusterSet(input.getLog());
		traceList = new ArrayList<Integer>();
		clusterSize = numberOfCluster.getValue();

		clusters = clustering();
	}

	private ClusterSet clustering() {
		ClusterSet clusters = new ClusterSet(input.getLog());

		for (int i = 0; i < clusterSize; i++)
			clusters.addCluster(new Cluster(input.getLog(), "Cluster" + i));

		String itemName = (String) selectItemBox.getValue();
		if (selectMethodmBox.getValue().equals(ST_UNIT)) {
			double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
			double temp;
			for (int k = 0; k < agProfiles.numberOfInstances(); k++) {
				temp = agProfiles.getValue(k, itemName);
				if (temp < min)
					min = temp;
				if (temp > max)
					max = temp;
			}
			double unit = (max - min) / clusterSize;

			for (int k = 0; k < agProfiles.numberOfInstances(); k++) {
				progress.setProgress(k);
				temp = agProfiles.getValue(k, itemName);
				int index = (int) ((temp - min) / unit);
				// System.out.println("index = " + index);
				if (index >= clusterSize)
					index = clusterSize - 1;
				clusters.getClusters().get(index).addTrace(k);
			}
		} else {
			int instances[] = new int[agProfiles.numberOfInstances()];
			double itemValues[] = new double[agProfiles.numberOfInstances()];
			for (int i = 0; i < agProfiles.numberOfInstances(); i++) {
				instances[i] = i;
				itemValues[i] = agProfiles.getValue(i, itemName);
			}

			for (int i = 0; i < agProfiles.numberOfInstances() - 1; i++) {
				for (int j = i + 1; j < agProfiles.numberOfInstances(); j++) {
					if (itemValues[i] > itemValues[j]) {
						double temp = itemValues[i];
						itemValues[i] = itemValues[j];
						itemValues[j] = temp;
						int temp2 = instances[i];
						instances[i] = instances[j];
						instances[j] = temp2;
					}
				}
			}
			int unit = agProfiles.numberOfInstances() / clusterSize;
			for (int i = 0; i < agProfiles.numberOfInstances() - 1; i++) {
				int index = i / unit;
				if (index >= clusterSize)
					index = clusterSize - 1;
				clusters.getClusters().get(index).addTrace(instances[i]);
			}
			clusters.getClusters().get(clusterSize - 1).addTrace(
					instances[agProfiles.numberOfInstances() - 1]);
		}

		return clusters;
	}

	protected DoubleMatrix2D calculateDistance(InstancePoint[] m) {
		DoubleMatrix2D D = DoubleFactory2D.dense.make(m.length, m.length, 0);
		for (int i = 0; i < m.length - 1; i++) {
			for (int j = i + 1; j < m.length; j++) {
				double temp = distanceMeasures.getDistance(m[i], m[j]);
				D.set(i, j, temp);
				D.set(j, i, temp);
			}
		}
		return D;
	}

	// GUI methods
	public void updateGUI() {
		updateUI();
	}

	protected void setupGUI() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		graphScrollPane = new JScrollPane(new JPanel());
		graphScrollPane.setBorder(BorderFactory.createEmptyBorder());
		rootPanel.add(graphScrollPane, BorderLayout.CENTER);

		JPanel emptyPanel = new JPanel();
		emptyPanel.setBackground(new Color(100, 100, 100));
		emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
		JPanel innerPanel = new JPanel();
		innerPanel.setOpaque(false);
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
		JLabel emptyLabel = new JLabel(
				"click to start clustering on the right panel");
		emptyLabel.setOpaque(false);
		innerPanel.add(Box.createHorizontalGlue());
		innerPanel.add(emptyLabel);
		innerPanel.add(Box.createHorizontalGlue());
		emptyPanel.add(Box.createVerticalGlue());
		emptyPanel.add(innerPanel);
		emptyPanel.add(Box.createVerticalGlue());

		graphScrollPane.getViewport().setView(emptyPanel);

		JPanel menuPanel = new SmoothPanel();
		menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.PAGE_AXIS));

		ArrayList<String> values = new ArrayList<String>();
		for (int i = 0; i < agProfiles.numberOfItems(); i++)
			values.add(agProfiles.getItemKey(i));
		selectItemBox = new GUIPropertyListEnumeration("Select items =", null,
				values, null, 230);
		menuPanel.add(selectItemBox.getPropertyPanel());

		ArrayList<String> values2 = new ArrayList<String>();
		values2.add(ST_UNIT);
		values2.add(ST_NUMBER);
		selectMethodmBox = new GUIPropertyListEnumeration("Method =", null,
				values2, null, 230);
		menuPanel.add(selectMethodmBox.getPropertyPanel());

		menuPanel.add(numberOfCluster.getPropertyPanel());
		menuPanel.add(hideNullCluster.getPropertyPanel());
		startButton = new JButton("cluster");
		startButton.setOpaque(false);
		startButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cluster();
			}
		});
		menuPanel.add(startButton);

		rootPanel.add(menuPanel, BorderLayout.EAST);

	}

	protected void updateUI() {
		try {
			graphScrollPane.getViewport().setView(getGraphPanel());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JPanel getGraphPanel() throws Exception {
		File dotFile = File.createTempFile("pmt", ".dot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(dotFile, false));
		writeToDot(bw);
		bw.close();
		System.out.println(dotFile.getAbsolutePath());
		Graph graph = Dot.execute(dotFile.getAbsolutePath());
		dotFile.deleteOnExit();
		graph.setEditable(true);
		graph.setMenuable(true);
		graph.setErrorWriter(new PrintWriter(System.err, true));
		GrappaPanel gp = new GrappaPanel(graph);
		gp.setBackgroundColor(new Color(100, 100, 100));
		gp.setScaleToFit(true);
		return gp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.DotFileWriter#writeToDot(java.io.Writer
	 * )
	 */
	public void writeToDot(Writer bw) throws IOException {
		bw
				.write("graph G { ranksep=\".3\"; compound=true; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Helvetica\";rankdir=\"TB\"; \n");
		bw
				.write("edge [decorate=false,fontname=\"Helvetica\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Helvetica\",fontsize=\"8\",shape=\"box\",style=\"filled\",fillcolor=\"palegoldenrod\"];\n");
		int clusterCounter = 0;
		clustersforOthers = new ClusterSet(input.getLog(), agProfiles);
		for (Cluster cluster : clusters.getClusters()) {
			if (hideNullCluster.getValue() == true && cluster.size() == 0)
				continue;
			cluster.setName("Cluster " + clusterCounter++);
			clustersforOthers.addCluster(cluster);
			bw
					.write(cluster.getName().replace(" ", "")
							+ " [shape=\"octagon\",style=\"filled\",fillcolor=\"chartreuse3\",label=\""
							+ cluster.getName() + "\\n" + cluster.size()
							+ "\"];\n");
		}
		// todo
		// DoubleMatrix2D length = calculateDistance(instancePoints);
		// write edges
		/*
		 * for (int i = 0; i < clusters.size() - 1; i++) { Cluster cluster1 =
		 * clusters.getClusters().get(i); if (hideNullCluster.getValue() == true
		 * && clusters.getClusters().get(i).size() == 0) continue; for (int k =
		 * i + 1; k < clusters.size(); k++) { if (hideNullCluster.getValue() ==
		 * true && clusters.getClusters().get(k).size() == 0) continue; Cluster
		 * cluster2 = clusters.getClusters().get(k);
		 * bw.write(cluster1.getName().replace(" ", "") + " -- " +
		 * cluster2.getName().replace(" ", "") + " [label=\"" + length.get(i, k)
		 * + "\"];\n"); } }
		 */
		// close graph
		bw.write("}\n");
	}

}
