package org.processmining.analysis.traceclustering.algorithm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.traceclustering.distance.DistanceMatrix;
import org.processmining.analysis.traceclustering.model.AHCTreeNode;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.analysis.traceclustering.ui.ColorReference;
import org.processmining.analysis.traceclustering.ui.DendroGramUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * @author R. P. Jagadeesh Chandra Bose
 * 
 */
public class AgglomerativeHierarchicalAlgorithm extends ClusteringAlgorithm
		implements GuiNotificationTarget {

	protected ClusterSet clusters;
	protected AggregateProfile agProfiles;
	protected String currentMethod;

	protected boolean bDrawing = true;

	protected int noTraces;
	protected int noClusters;
	protected AHCTreeNode rootAHCTree = null;
	protected int upperBound, lowerBound, upperBoundUI, lowerBoundUI, cutPoint,
			upperMaxBound, kGramSize;
	protected DistanceMatrix distances;
	// GUI
	protected JPanel rootPanel;
	protected JScrollPane graphScrollPane;
	protected JPanel menuPanel;
	protected JPanel menuUpperPanel;
	protected JPanel middlePanel;
	protected JPanel lowerPanel;
	protected JPanel sliderPanel;
	protected JPanel sliderPanel_Dendo;
	protected JSlider sliderUpperUI;
	protected JSlider sliderCutPointUI;
	protected JSlider sliderLowerUI;
	protected JSlider sliderUpper;
	protected JSlider sliderLower;
	protected JLabel diameterLabel;
	protected JLabel diameterLabel2;
	protected JLabel diameterLabel3;
	protected JLabel diameterLabel4;
	protected JLabel diameterLabel5;
	protected JLabel subScoreFileName;
	protected JLabel indelScoreFileName;

	protected ButtonGroup normalizationTypeButtonGrp;
	protected ButtonGroup genSubScoreButtonGrp;
	protected ButtonGroup genIndelScoreButtonGrp;
	protected ButtonGroup processRepeatsButtonGrp;

	protected JRadioButton normalizationSumLengths;
	protected JRadioButton normalizationMaxLength;

	protected JRadioButton genSubScoreYes;
	protected JRadioButton genSubScoreNo;
	protected JRadioButton genIndelScoreYes;
	protected JRadioButton genIndelScoreNo;
	protected JRadioButton processRepeatsYes;
	protected JRadioButton processRepeatsNo;

	protected DecimalFormat format = new DecimalFormat("0");
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);
	protected JButton startButton;
	protected GUIPropertyListEnumeration clusteringMethod;
	protected GUIPropertyListEnumeration displayMethod;
	protected ProgressPanel progress;
	protected DendroGramUI dendrogram;
	protected NewDendroGramUI newDendroGram;

	// STATIC
	static protected String ST_Single_linkage = "Single linkage";
	static protected String ST_Complete_linkage = "Complete linkage";
	static protected String ST_Centroid_linkage = "Centroid linkage";
	static protected String ST_Average_linkage = "Average linkage";
	static protected String ST_MinVariance_linkage = "Minimum Variance";

	static protected String ST_DendroGram = "Dendrogram";
	static protected String ST_Tree = "Tree";

	/*
	 * Clustering related Attributes
	 */

	int K; // No of clusters created so far
	int N;
	MyCluster[] myClusters; // The nodes (clusters) of the resulting hierarchy
	String clusterCriteria;
	int[][] itemsJoined;

	public AgglomerativeHierarchicalAlgorithm() {
		super("New Agglomerative Hierarchical Clustering",
				"New Agglomerative Hierarchical clustering");
		clusters = null;
	}

	protected void setupGui() {

		currentMethod = "";
		noTraces = input.getProfile().numberOfInstances();

		upperBound = noTraces;
		lowerBound = 0;
		upperBoundUI = noTraces;
		lowerBoundUI = 0;
		upperMaxBound = noTraces;
		cutPoint = 2;

		rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());

		rootPanel.setBorder(BorderFactory.createEmptyBorder());
		graphScrollPane = new JScrollPane(new JPanel());

		graphScrollPane.setBorder(BorderFactory.createEmptyBorder());
		graphScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		graphScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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

		// slider panel for DendroGram
		// /////////////////////////////////////////////
		sliderPanel_Dendo = new JPanel();
		sliderPanel_Dendo.setOpaque(false);
		sliderPanel_Dendo
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sliderPanel_Dendo.setLayout(new BoxLayout(sliderPanel_Dendo,
				BoxLayout.X_AXIS));

		JPanel sliderCutPointUIPanel = new JPanel();
		sliderPanel_Dendo.add(sliderCutPointUIPanel);

		sliderCutPointUIPanel.setOpaque(false);
		sliderCutPointUIPanel.setLayout(new BorderLayout());
		sliderCutPointUIPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
				10, 10));
		JLabel headerLabel5 = new JLabel("No. Clusters");
		headerLabel5.setBackground(bgColor);
		headerLabel5.setForeground(fgColor);
		sliderCutPointUIPanel.add(headerLabel5, BorderLayout.NORTH);
		sliderCutPointUI = new JSlider(JSlider.VERTICAL, 0, noTraces, 2);
		sliderCutPointUI.setOpaque(false);
		sliderCutPointUI.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				cutPoint = (int) ((double) sliderCutPointUI.getValue()
						/ noTraces * (upperMaxBound));
				diameterLabel5.setText(format.format(cutPoint));
				if (sliderCutPointUI.getValueIsAdjusting() == false) {
					updateUI_cutPoint();
				}
			}
		});
		sliderCutPointUIPanel.add(sliderCutPointUI, BorderLayout.CENTER);
		diameterLabel5 = new JLabel(format.format(cutPoint));
		diameterLabel5.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterLabel5.setOpaque(false);
		diameterLabel5.setForeground(fgColor);
		sliderCutPointUIPanel.add(diameterLabel5, BorderLayout.SOUTH);

		ArrayList<String> values = new ArrayList<String>();
		values.add(ST_MinVariance_linkage);
		values.add(ST_Single_linkage);
		values.add(ST_Complete_linkage);
		values.add(ST_Average_linkage);
		values.add(ST_Centroid_linkage);

		clusteringMethod = new GUIPropertyListEnumeration("Clustering Method",
				"", values, null, 150);

		ArrayList<String> values2 = new ArrayList<String>();
		values2.add(ST_DendroGram);
		displayMethod = new GUIPropertyListEnumeration("Display Method", "",
				values2, this, 150);

		startButton = new JButton("cluster");
		startButton.setOpaque(false);
		startButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (rootAHCTree == null
						|| !currentMethod.equals(clusteringMethod.getValue())) {
					currentMethod = (String) clusteringMethod.getValue();
					clusterCriteria = currentMethod;
					Message.add("AH clustering with "
							+ clusteringMethod.getValue() + "...",
							Message.NORMAL);
					cluster();
				}
			}
		});

		lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
		lowerPanel.setOpaque(false);
		lowerPanel.add(Box.createVerticalStrut(5));
		lowerPanel.add(clusteringMethod.getPropertyPanel());
		lowerPanel.add(Box.createVerticalStrut(5));
		lowerPanel.add(displayMethod.getPropertyPanel());
		lowerPanel.add(Box.createVerticalStrut(5));
		lowerPanel.add(startButton);

		menuPanel = new SmoothPanel();
		menuUpperPanel = new SmoothPanel();
		menuUpperPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		menuUpperPanel.setLayout(new BorderLayout());
		menuUpperPanel.add(sliderPanel_Dendo, BorderLayout.CENTER);
		menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		menuPanel.setLayout(new BorderLayout());
		menuPanel.add(menuUpperPanel, BorderLayout.NORTH);
		menuPanel.add(lowerPanel, BorderLayout.SOUTH);
		rootPanel.add(menuPanel, BorderLayout.EAST);
	}

	protected void cluster() {

		// prepare progress display
		progress = new ProgressPanel("Clustering");
		progress.setNote("processing...");
		progress.setMinMax(0, noTraces);
		graphScrollPane.getViewport().setView(progress.getPanel());

		progress.setMinMax(0, input.getLog().numberOfInstances() - 1
				+ (input.getLog().numberOfInstances()) / 100);
		progress.setProgress(0);
		graphScrollPane.getViewport().setView(progress.getPanel());
		Thread clusterThread = new Thread() {
			public void run() {
				// do cluster
				build();
				// update ui
				// updateSliders();
				updateUI();
				startButton.setEnabled(true);
			}
		};
		startButton.setEnabled(false);
		clusterThread.start();

	}

	protected void build() {
		progress.setNote("Calculating distances (might take few minutes)...");
		distances = input.getDistanceMatrix();
		progress.setNote("Clustering...");
		agglomerativeClustering();
		noClusters = cutPoint;
		HashSet<HashSet<Integer>> clusterPartitions = getClusters(noClusters);
		Cluster[] cluster = new Cluster[noClusters];
		int i = 0;
		Iterator<HashSet<Integer>> it = clusterPartitions.iterator();
		int[] traceIndices;
		HashSet<Integer> currentCluster;
		Iterator<Integer> it2;
		while (it.hasNext()) {
			currentCluster = it.next();
			traceIndices = new int[currentCluster.size()];
			int j = 0;
			it2 = currentCluster.iterator();
			while (it2.hasNext())
				traceIndices[j++] = it2.next().intValue() - 1;
			cluster[i] = new Cluster(input.profile.getLog(), "Cluster "
					+ (i + 1), traceIndices);
			i++;
		}
		clusters = new ClusterSet(input.profile.getLog());
		for (i = 0; i < noClusters; i++)
			clusters.addCluster(cluster[i]);
		progress.close();

		// Silhoutte width;

		HashMap<Integer, ArrayList<Integer>> clusterIdTraceIndicesMap = new HashMap<Integer, ArrayList<Integer>>();
		float[] a = new float[noTraces];
		float[] b = new float[noTraces];
		float[] s = new float[noTraces];
		float[] clusterDiameter = new float[noClusters];
		float[] clusterDensity = new float[noClusters];
		float[] avgLength = new float[noClusters];
		float sumAllDistances;
		float avgS;
		ArrayList<Integer> traceIds;
		int clusterId = 0;
		it = clusterPartitions.iterator();
		while (it.hasNext()) {
			currentCluster = it.next();
			traceIds = new ArrayList<Integer>();
			it2 = currentCluster.iterator();
			while (it2.hasNext())
				traceIds.add(it2.next().intValue() - 1);

			clusterIdTraceIndicesMap.put(clusterId, traceIds);
			// System.out.println(clusterId+" @ "+traceIds.size());
			clusterId++;
		}

		int currentTrace;
		float sum;
		double maxDist;
		for (i = 0; i < noClusters; i++) {
			traceIds = clusterIdTraceIndicesMap.get(i);
			int noTracesInClusterI = traceIds.size();
			maxDist = Float.MIN_VALUE;
			sumAllDistances = 0;
			for (int j = 0; j < noTracesInClusterI; j++) {
				currentTrace = traceIds.get(j);
				sum = 0;

				for (int k = j + 1; k < noTracesInClusterI; k++) {
					if (distances.get(currentTrace, traceIds.get(k)) > maxDist)
						maxDist = distances.get(currentTrace, traceIds.get(k));

					sumAllDistances += distances.get(currentTrace, traceIds
							.get(k));
					sum += distances.get(currentTrace, traceIds.get(k));
				}
				a[currentTrace] = sum / noTracesInClusterI;
			}
			clusterDiameter[i] = new Double(maxDist).floatValue();
			avgLength[i] = sumAllDistances
					/ (noTracesInClusterI * (noTracesInClusterI - 1));
			clusterDensity[i] = avgLength[i] / clusterDiameter[i];
		}

		ArrayList<Integer> traceIdsJ;
		int noTracesInClusterI, noTracesInClusterJ;
		float minSum = Float.MAX_VALUE, avgSum;
		for (i = 0; i < noClusters; i++) {
			traceIds = clusterIdTraceIndicesMap.get(i);
			noTracesInClusterI = traceIds.size();
			for (int k = 0; k < noTracesInClusterI; k++) {
				currentTrace = traceIds.get(k);

				minSum = Float.MAX_VALUE;
				for (int j = 0; j < noClusters; j++) {
					if (i != j) {
						traceIdsJ = clusterIdTraceIndicesMap.get(j);
						noTracesInClusterJ = traceIdsJ.size();
						sum = 0;

						for (int l = 0; l < noTracesInClusterJ; l++) {
							sum += distances
									.get(currentTrace, traceIdsJ.get(l));
						}
						avgSum = sum / noTracesInClusterJ;
						if (avgSum < minSum)
							minSum = avgSum;
					}
				}
				b[currentTrace] = minSum;
			}
		}
		avgS = 0;
		for (i = 0; i < noTraces; i++) {
			s[i] = (b[i] - a[i]) / Math.max(b[i], a[i]);
			avgS += s[i];
		}
		System.out.println("Silhoutte Width: " + avgS / noTraces);

		// System.out.println("Cluster Diameter");
		// for(i = 0; i < noClusters; i++)
		// System.out.println(clusterDiameter[i]);

		float avgClusterDensity = 0;
		for (i = 0; i < noClusters; i++)
			avgClusterDensity += clusterDensity[i];

		System.out.println("Avg. Cluster Density: "
				+ (avgClusterDensity / noClusters));
	}

	@Override
	public ClusterSet getClusters() {
		// TODO Auto-generated method stub
		return clusters;
	}

	@Override
	public JComponent getUI() {
		sliderPanel = null;
		setupGui();
		return rootPanel;
	}

	public void updateGUI() {
		if (displayMethod.getValue().equals(ST_DendroGram)) {
			menuUpperPanel.removeAll();
			menuUpperPanel.add(sliderPanel_Dendo);
			sliderPanel_Dendo.updateUI();
			// if(rootAHCTree!=null){
			// dendrogram = new DendroGramUI(rootAHCTree, cutPoint);
			// graphScrollPane.getViewport().setView(dendrogram);
			// makeClustersForDendrogram();
			// }
		} else {
			menuUpperPanel.removeAll();
			menuUpperPanel.add(sliderPanel);
			menuUpperPanel.repaint();
			sliderPanel.updateUI();
			// if(rootAHCTree!=null){
			// try {
			// graphScrollPane.getViewport().setView(getGraphPanel());
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }
		}
	}

	protected void updateUI_cutPoint() {
		if (myClusters != null && getRoot() != null && bDrawing) {
			try {

				newDendroGram = new NewDendroGramUI(getRoot(), cutPoint,
						myClusters[K - cutPoint].level);
				graphScrollPane.getViewport().setView(newDendroGram);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void updateUI() {
		if (getRoot() != null && bDrawing) {
			try {
				if (displayMethod.getValue().equals(ST_DendroGram)) {
					newDendroGram = new NewDendroGramUI(getRoot(), noClusters,
							myClusters[K - noClusters].level);
					graphScrollPane.getViewport().setView(newDendroGram);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// The clustering code starts here

	class MyCluster {
		int ID; // cluster identifier
		int cardinality; // No. of items in the cluster
		double level; // the level of the cluster in the tree
		MyCluster left, right; // the left and right clusters
		double[] distChild; // distances to lower level nodes;

		public MyCluster(int clustId, double[] distChild) {
			// For leaf nodes
			this.ID = clustId + 1;
			this.cardinality = 1;
			this.level = 0;
			this.distChild = distChild;
		}

		public MyCluster(int clustId, MyCluster left, MyCluster right,
				double level, double[] distChild) {
			this.ID = clustId + 1;
			this.left = left;
			this.right = right;
			this.cardinality = left.cardinality + right.cardinality;
			this.level = level;
			this.distChild = distChild;
		}

		public boolean live() {
			return distChild != null;
		}

		public void kill() {
			distChild = null;
		}

		public void print() {
			print(0);
		}

		public void print(int n) {
			if (right != null)
				right.print(n + 6);
			indent(n);
			System.out.println("[" + ID + "] (" + (int) (100 * level) / 100.0
					+ ")");

			if (left != null)
				left.print(n + 6);
		}

		public void indent(int n) {
			for (int i = 0; i < n; i++)
				System.out.print(" ");
		}
	}

	protected void agglomerativeClustering() {

		System.out.println("Clustering Criteria: " + clusterCriteria);
		N = noTraces;

		itemsJoined = new int[N][2];
		myClusters = new MyCluster[2 * N - 1];
		double[] distMatrix;
		for (int i = 0; i < N; i++) {
			// Generate Distance Matrix I
			distMatrix = new double[i + 1];
			for (int j = 0; j < i; j++) {
				distMatrix[j] = distances.get(i, j);
			}
			distMatrix[i] = 0;
			myClusters[i] = new MyCluster(i, distMatrix);
		}
		K = N;
		progress.setProgress(2);
		while (K < 2 * N - 1) {
			// System.out.println("K: "+K);
			progress.setProgress(K - N + 2);
			progress.setNote("clustering step " + (K - N + 2) + " of " + N);
			findAndJoin();
		}

	}

	protected void findAndJoin() {
		int minI = -1, minJ = -1;
		double minDist = Double.POSITIVE_INFINITY;
		for (int i = 0; i < K; i++) {
			if (myClusters[i].live()) {
				for (int j = 0; j < i; j++) {
					if (myClusters[j].live()) {
						double d = dist(i, j);
						if (d < minDist) {
							minDist = d;
							minI = i;
							minJ = j;
						}
					}
				}
			}
		}
		// Logger.println("Items Joined " + (K - N) + ": " + (minI + 1) + " "
		// + (minJ + 1) + " " + minDist);
		itemsJoined[K - N][0] = minI + 1;
		itemsJoined[K - N][1] = minJ + 1;

		if (clusterCriteria.equals(ST_MinVariance_linkage))
			joinMinVariance(minI, minJ);
		else if (clusterCriteria.equals(ST_Single_linkage))
			joinSingleLinkage(minI, minJ);
		else if (clusterCriteria.equals(ST_Complete_linkage))
			joinCompleteLinkage(minI, minJ);
		else if (clusterCriteria.equals(ST_Average_linkage))
			joinAverageLinkage(minI, minJ);
		else if (clusterCriteria.equals(ST_Centroid_linkage))
			joinCentroid(minI, minJ);
	}

	private double dist(int i, int j) {
		return myClusters[Math.max(i, j)].distChild[Math.min(i, j)];
	}

	private void joinAverageLinkage(int i, int j) {
		// Join clusters i and j to form a new cluster K
		double[] distMatrix = new double[K];

		for (int m = 0; m < K; m++) {
			if (myClusters[m].live() && m != j && m != i) {
				distMatrix[m] = (dist(i, m) * myClusters[i].cardinality + dist(
						j, m)
						* myClusters[j].cardinality)
						/ (myClusters[i].cardinality + myClusters[j].cardinality);
			}
		}
		myClusters[K] = new MyCluster(K, myClusters[i], myClusters[j], dist(i,
				j) / 2, distMatrix);
		myClusters[i].kill();
		myClusters[j].kill();
		K++;
	}

	private void joinSingleLinkage(int i, int j) {
		double[] distMatrix = new double[K];
		for (int m = 0; m < K; m++) {
			if (myClusters[m].live() && m != j && m != i) {
				distMatrix[m] = Math.min(dist(i, m), dist(j, m));
			}
		}
		myClusters[K] = new MyCluster(K, myClusters[i], myClusters[j], dist(i,
				j) / 2, distMatrix);
		myClusters[i].kill();
		myClusters[j].kill();
		K++;
	}

	private void joinMinVariance(int i, int j) {
		// System.out.println("In minVariance, joining clusters: "+i+" "+j+" "+K);
		double[] distMatrix = new double[K];
		for (int m = 0; m < K; m++) {
			if (myClusters[m].live() && m != j && m != i) {
				distMatrix[m] = ((float) (myClusters[i].cardinality + myClusters[m].cardinality))
						/ (myClusters[i].cardinality
								+ myClusters[j].cardinality + myClusters[m].cardinality)
						* dist(m, i)
						+ ((float) (myClusters[j].cardinality + myClusters[m].cardinality))
						/ (myClusters[i].cardinality
								+ myClusters[j].cardinality + myClusters[m].cardinality)
						* dist(m, j)
						- ((float) (myClusters[m].cardinality))
						/ (myClusters[i].cardinality
								+ myClusters[j].cardinality + myClusters[m].cardinality)
						* dist(i, j);
			}
		}
		myClusters[K] = new MyCluster(K, myClusters[i], myClusters[j], dist(i,
				j) / 2, distMatrix);
		myClusters[i].kill();
		myClusters[j].kill();
		K++;
	}

	private void joinCompleteLinkage(int i, int j) {
		double[] distMatrix = new double[K];
		for (int m = 0; m < K; m++) {
			if (myClusters[m].live() && m != j && m != i) {
				distMatrix[m] = Math.max(dist(i, m), dist(j, m));
			}
		}
		myClusters[K] = new MyCluster(K, myClusters[i], myClusters[j], dist(i,
				j) / 2, distMatrix);
		myClusters[i].kill();
		myClusters[j].kill();
		K++;
	}

	private void joinCentroid(int i, int j) {
		double[] distMatrix = new double[K];
		for (int m = 0; m < K; m++) {
			if (myClusters[m].live() && m != j && m != i) {
				distMatrix[m] = (myClusters[i].cardinality * dist(m, i)
						+ myClusters[j].cardinality * dist(m, j) - ((myClusters[i].cardinality
						* myClusters[j].cardinality * dist(i, j)) / ((float) (myClusters[i].cardinality + myClusters[j].cardinality))))
						/ (float) (myClusters[i].cardinality + myClusters[j].cardinality);
			}
		}
		myClusters[K] = new MyCluster(K, myClusters[i], myClusters[j], dist(i,
				j) / 2, distMatrix);
		myClusters[i].kill();
		myClusters[j].kill();
		K++;
	}

	public MyCluster getRoot() {
		return myClusters[K - 1];
	}

	public void printTree() {
		getRoot().print();
	}

	public HashSet<HashSet<Integer>> getClusters(int noClusters) {
		// System.out.println("Entering getClusters");

		HashSet<HashSet<Integer>> clusterPartitions = new HashSet<HashSet<Integer>>();
		boolean[] flag = new boolean[N - noClusters];

		for (int i = 0; i < N - noClusters; i++)
			flag[i] = false;
		HashSet<Integer> clusters = new HashSet<Integer>();

		for (int i = 0; i < N; i++) {
			clusters.add(i + 1);
		}

		HashSet<Integer> currentCluster;
		for (int i = N - noClusters; i > 0; i--) {
			if (flag[i - 1] == false) {
				currentCluster = getElements(i - 1, flag);
				clusterPartitions.add(currentCluster);
				clusters.removeAll(currentCluster);
			}
		}
		Iterator<Integer> it = clusters.iterator();
		while (it.hasNext()) {
			currentCluster = new HashSet<Integer>();
			currentCluster.add(it.next());
			clusterPartitions.add(currentCluster);
		}

		// Logger.printReturn("Exiting getClusters");
		return clusterPartitions;
	}

	public HashSet<Integer> getElements(int index, boolean[] flag) {
		HashSet<Integer> elements = new HashSet<Integer>();
		if (flag[index] == false) {
			if (itemsJoined[index][0] > N)
				elements
						.addAll(getElements(itemsJoined[index][0] - N - 1, flag));
			else
				elements.add(itemsJoined[index][0]);

			if (itemsJoined[index][1] > N)
				elements
						.addAll(getElements(itemsJoined[index][1] - N - 1, flag));
			else
				elements.add(itemsJoined[index][1]);

			flag[index] = true;
		}

		return elements;
	}

	public void printClusterPartitionsToFile(String charStreamDir,
			String charStreamListFileName, String outputDir, int noClusters) {
		Vector<String> charStreamFileNames = new Vector<String>();
		HashSet<HashSet<Integer>> clusterPartitions = getClusters(noClusters);
		HashSet<Integer> currentCluster;
		Iterator<HashSet<Integer>> partitionIt;
		Iterator<Integer> it;
		partitionIt = clusterPartitions.iterator();

		FileOutputStream out;
		PrintStream p;
		int i = 1;
		try {
			if (!(new File(outputDir + "\\" + noClusters).exists()))
				new File(outputDir + "\\" + noClusters).mkdirs();

			while (partitionIt.hasNext()) {
				currentCluster = partitionIt.next();
				it = currentCluster.iterator();

				out = new FileOutputStream(outputDir + "\\" + noClusters
						+ "\\cluster" + i + ".txt");
				p = new PrintStream(out);
				while (it.hasNext()) {
					p
							.println(charStreamFileNames.get(it.next()
									.intValue() - 1));
				}
				p.close();
				out.close();
				i++;
			}
		} catch (FileNotFoundException e) {
			System.out
					.println("FileNotFoundException while writing cluster partitions");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("IOException while writing cluster partitions");
			System.exit(0);
		}
		// Logger.printReturn("Entering printClusterPartitionsToFile");
	}

	class NewDendroGramUI extends JPanel {
		protected MyCluster root;
		int clusterCutPoint;
		int noTraces;
		double threshold;

		protected int rasterSize;
		protected int border = 100;
		protected int ratioY;
		protected int ratioX;
		protected int height;
		protected int width;

		protected BufferedImage buffer;
		protected int bufferSizeX;
		protected int bufferSizeY;

		protected ColorReference colorReference;

		public NewDendroGramUI(MyCluster cluster, int noClusters,
				double threshold) {
			this.root = cluster;
			this.clusterCutPoint = noClusters;
			noTraces = root.cardinality;
			colorReference = new ColorReference();
			this.threshold = threshold;
		}

		public Dimension getMaximumSize() {
			int numberOfTraces = root.cardinality;
			int recomSize = Math.max(ratioX = (width - 4 * border)
					/ numberOfTraces, 2);
			int width = numberOfTraces * recomSize;
			int height = 4000;
			return new Dimension(width, height);
		}

		protected void paintComponent(Graphics grx) {
			int numberOfTraces = root.cardinality;
			// double maxValue = rootNode.getDistance();
			double maxValue = numberOfTraces;
			width = this.getWidth();
			height = this.getHeight();
			final Graphics2D g2d = (Graphics2D) grx;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			// paint black background
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, width, height);
			g2d.setColor(Color.white);

			border = 5;
			ratioY = (int) ((height - 7 * border) / (maxValue + 0.005 * maxValue));
			ratioX = (width - 4 * border) / numberOfTraces;

			int recomRasterSize = Math.max(ratioX, 2);

			// if(recomRasterSize != this.rasterSize || this.buffer == null) {
			this.rasterSize = recomRasterSize;
			// re-create buffer
			bufferSizeX = numberOfTraces * recomRasterSize;
			bufferSizeY = height - border * 4;
			buffer = new BufferedImage(bufferSizeX, bufferSizeY,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D gBuf = buffer.createGraphics();

			// draw boxes for instances
			for (int i = 0; i < numberOfTraces; i++) {
				if (i % 2 == 0)
					gBuf.setColor(new Color(240, 240, 220));
				else
					gBuf.setColor(new Color(150, 100, 220));
				gBuf.fillRect(i * rasterSize, bufferSizeY - border * 3,
						rasterSize, border * 3);
			}

			bufferSizeY -= border * 3;
			// draw lines
			gBuf.setColor(Color.white);
			// drawLines(root, 0, gBuf);
			draw(root, gBuf, bufferSizeX, bufferSizeY);

			// write number of clusters
			gBuf.setColor(Color.green);
			gBuf.drawString("Number of Clusters = " + clusterCutPoint, border,
					13);

			// draw cutOff lines
			int tempY = bufferSizeY - 35
					- (int) (threshold * (bufferSizeY - 50) / root.level);
			gBuf.setColor(Color.YELLOW);
			gBuf.drawLine(0, tempY, bufferSizeX, tempY);

			gBuf.dispose();
			g2d.drawImage(buffer, 2 * border, 2 * border, this);
			if (bufferSizeX > width) {
				Dimension dim = new Dimension(bufferSizeX + 4 * border,
						this.height);
				this.setPreferredSize(dim);
				revalidate();
			}

			// draw outer line
			g2d.setColor(Color.white);
			g2d.drawRect(border, border, bufferSizeX + border * 2, this
					.getHeight()
					- border * 2);

			g2d.dispose();
		}

		protected void draw(MyCluster node, Graphics g, int w, int h) {
			int card = node.cardinality;
			draw(node, g, w, h, 0, (double) w / card, (double) (h - 50)
					/ node.level, 0);
		}

		protected int draw(MyCluster node, Graphics g, int w, int h,
				int leftcard, double xsc, double ysc, int fromy) {
			// System.out.println(node.cardinality);
			if (node.left != null && node.right != null) {
				int y = (int) (h - 30 - node.level * ysc);
				int leftx = draw(node.left, g, w, h, leftcard, xsc, ysc, y);
				int rightx = draw(node.right, g, w, h, leftcard
						+ node.left.cardinality, xsc, ysc, y);
				g.drawLine(leftx, y, rightx, y);
				int x = (leftx + rightx) / 2;
				// if(y == fromy)
				// System.out.println("OOP");
				g.drawLine(x, y, x, fromy);
				return x;
			} else {
				// Leaf Node
				int x = (int) ((leftcard + 0.5) * xsc);
				// System.out.println((h-30)+" "+fromy);
				g.drawLine(x, h - 30, x, fromy);
				g.fillOval(x - 4, h - 30 - 4, 8, 8);
				g.drawString(Integer.toString(node.ID), x - 5, h - 10);
				return x;
			}
		}

	}
}
