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
import java.util.Iterator;

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
import org.processmining.framework.ui.Message;
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
public class KMeansAlgorithm extends ClusteringAlgorithm implements
		DotFileWriter, GuiNotificationTarget {

	protected ArrayList<Integer> traceList;
	protected int clusterSize;
	protected int traceSize;
	protected double currentDistanceSum;
	protected ArrayList<ClusterSet> clustersList;
	protected ArrayList<Integer> frequencyList;
	protected ClusterSet clusters;
	protected ClusterSet clustersforOthers;
	protected AggregateProfile agProfiles;
	protected InstancePoint[] instancePoints;
	protected DistanceMetric distanceMeasures;
	protected DoubleMatrix2D distanceMatrix;
	protected DoubleMatrix2D distances;
	protected static String ST_FREQUENCY = "Frequency";
	protected static String ST_DISTANCE = "Distance";

	// GUI
	protected JPanel rootPanel;
	protected JScrollPane graphScrollPane;
	protected GUIPropertyInteger numberOfRunsBox = new GUIPropertyInteger(
			"Number of Runs = ", 50, 1, 100);
	protected GUIPropertyInteger clusterSizeBox = new GUIPropertyInteger(
			"Number of Clusters = ", 3, 1, 100);
	protected GUIPropertyInteger maxIterationBox = new GUIPropertyInteger(
			"Number of Iterations = ", 50, 1, 1000);
	protected GUIPropertyListEnumeration selectResultBox;
	protected GUIPropertyBoolean hideNullCluster = new GUIPropertyBoolean(
			"Hide null clusters", true, this);
	protected JButton startButton;
	protected ProgressPanel progress;
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);

	public KMeansAlgorithm() {
		super("K-Means Clustering",
				"K-Means Clustering allows the user to specify"
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

		progress.setMinMax(0, numberOfRunsBox.getValue() + 1);
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
		double temp_sum = Double.MAX_VALUE;
		// initalize variables
		clusters = new ClusterSet(input.getLog());
		traceList = new ArrayList<Integer>();
		for (int i = 0; i < traceSize; i++)
			traceList.add(Integer.valueOf(i));
		this.clusterSize = clusterSizeBox.getValue();
		this.clustersList = new ArrayList<ClusterSet>();
		this.frequencyList = new ArrayList<Integer>();

		for (int l = 0; l < numberOfRunsBox.getValue(); l++) {
			progress.setProgress(l + 1);
			progress.setNote((l + 1) + "th iteration");
			ClusterSet tempCluster = runKmean();
			// currentDistanceSum
			if (selectResultBox.getValue().equals(ST_DISTANCE)) {
				if (temp_sum > currentDistanceSum) {
					temp_sum = currentDistanceSum;
					clusters = tempCluster;
				}
			} else {
				if (clustersList.size() == 0) {
					clustersList.add(tempCluster);
					frequencyList.add(1);
				} else {
					boolean flag = true;
					for (int o = 0; o < clustersList.size(); o++) {
						if (clustersList.get(o).equals(tempCluster)) {
							frequencyList.set(o, frequencyList.get(o) + 1);
							flag = false;
						}
					}
					if (flag) {
						clustersList.add(tempCluster);
						frequencyList.add(1);
					}
				}
			}
		}
		int max = 0;
		if (selectResultBox.getValue().equals(ST_FREQUENCY)) {
			for (int i = 0; i < frequencyList.size(); i++) {
				if (frequencyList.get(i) > max) {
					clusters = clustersList.get(i);
					max = frequencyList.get(i);
				}
			}
			Message.add("K-means clustering: frequency = " + max,
					Message.NORMAL);
		} else {
			Message.add("K-means clustering: distance = " + temp_sum,
					Message.NORMAL);
		}
	}

	private ClusterSet runKmean() {
		ClusterSet clusters = new ClusterSet(input.getLog());
		// initial points
		initCenters();

		// initialize clusters
		ClusterSet oldClusterSet = new ClusterSet(input.getLog());
		currentDistanceSum = 0.0;

		for (int k = 0; k < maxIterationBox.getValue(); k++) {
			clusters.clear();
			for (int i = 0; i < clusterSize; i++)
				clusters.addCluster(new Cluster(input.getLog(), "Cluster" + i));

			// calculate distance between each center point and each instance
			calcualteDistance();

			for (int i = 0; i < distances.rows(); i++) {

				int index = -1;
				double value = Double.MAX_VALUE;
				for (int j = 0; j < distances.columns(); j++) {
					if (value > distances.get(i, j)) {
						index = j;
						value = distances.get(i, j);
					}
				}
				// assign cluster
				((Cluster) clusters.getClusters().get(index))
						.addTrace(traceList.get(i));
				currentDistanceSum += value;
			}

			// recalculate center
			try {
				if (k == 0) {
					for (int i = 0; i < clusterSize; i++)
						oldClusterSet = (ClusterSet) clusters.clone();
				} else {
					if (clusters.equals(oldClusterSet)) {
						break;
					} else {
						oldClusterSet = (ClusterSet) clusters.clone();
					}
				}
			} catch (Exception e) {
			}
			calculateCenter(clusters);
		}

		return clusters;
	}

	private void initCenters() {
		instancePoints = new InstancePoint[clusterSize];
		for (int i = 0; i < clusterSize; i++) {
			instancePoints[i] = new InstancePoint();
			for (int j = 0; j < agProfiles.numberOfItems(); j++) {
				instancePoints[i].set(agProfiles.getItemKey(j), agProfiles
						.getRandomValue(j));
			}
		}
	}

	private void calcualteDistance() {
		distances = DoubleFactory2D.dense.make(traceSize, clusterSize, 0);
		for (int i = 0; i < traceSize; i++)
			for (int j = 0; j < clusterSize; j++)
				distances.set(i, j, distanceMeasures.getDistance(agProfiles
						.getPoint(i), instancePoints[j]));
	}

	private void calculateCenter(ClusterSet clusters) {
		for (int i = 0; i < clusterSize; i++) {
			Cluster tempCluster = (Cluster) clusters.getClusters().get(i);
			if (tempCluster.size() == 0)
				continue;
			instancePoints[i] = null;
			instancePoints[i] = new InstancePoint();
			Iterator itr = tempCluster.getTraceIndices().iterator();
			while (itr.hasNext()) {
				int index = (Integer) itr.next();
				InstancePoint iPoint = agProfiles.getPoint(index);
				Iterator itr2 = iPoint.getItemKeys().iterator();
				while (itr2.hasNext()) {
					String key = (String) itr2.next();
					if (instancePoints[i].getItemKeys().contains(key))
						instancePoints[i].set(key, instancePoints[i].get(key)
								+ iPoint.get(key));
					else
						instancePoints[i].set(key, iPoint.get(key));
				}
			}
			itr = instancePoints[i].getItemKeys().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				instancePoints[i].set(key, instancePoints[i].get(key)
						/ tempCluster.size());
			}
		}
	}

	// protected DoubleMatrix2D calculateDistance(InstancePoint[] m) {
	// DoubleMatrix2D D = DoubleFactory2D.dense.make(m.length, m.length, 0);
	// for (int i = 0; i < m.length - 1; i++) {
	// for (int j = i + 1; j < m.length; j++) {
	// double temp = distanceMeasures.getDistance(m[i],m[j]);
	// D.set(i, j, temp);
	// D.set(j, i, temp);
	// }
	// }
	// return D;
	// }

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

		SmoothPanel menuPanel = new SmoothPanel();
		menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.PAGE_AXIS));

		menuPanel.add(clusterSizeBox.getPropertyPanel());
		menuPanel.add(maxIterationBox.getPropertyPanel());
		menuPanel.add(numberOfRunsBox.getPropertyPanel());

		ArrayList<String> values2 = new ArrayList<String>();
		values2.add(ST_FREQUENCY);
		values2.add(ST_DISTANCE);
		selectResultBox = new GUIPropertyListEnumeration(
				"Choose a cluster based on =", null, values2, null, 230);
		menuPanel.add(selectResultBox.getPropertyPanel());

		menuPanel.add(hideNullCluster.getPropertyPanel());

		startButton = new JButton("cluster");
		startButton.setOpaque(false);
		startButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clusterSize = clusterSizeBox.getValue();
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
		// double[] centers = calculateIntraClusterDistance(clusters);

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
							+ "\\n");// + centers[clusterCounter-1] +
			// " - \"];\n");
		}
		// todo
		/*
		 * DoubleMatrix2D length =
		 * calculateInterClusterDistance(clustersforOthers); // write edges for
		 * (int i = 0; i < clusters.size() - 1; i++) { Cluster cluster1 =
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
