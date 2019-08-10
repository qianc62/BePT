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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.traceclustering.distance.DistanceMetric;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.analysis.traceclustering.ui.SOMUI;

import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Minseok Song
 */
public class SOMAlgorithm extends ClusteringAlgorithm implements
		GuiNotificationTarget {

	protected Cluster[][] clusterArray;
	protected int clusterXDim;
	protected int clusterYDim;
	protected long randomSeed;
	protected InstancePoint[][] pointArray;
	protected int radius;
	protected boolean bTraining;
	protected ArrayList<Integer> traceList;
	protected int traceSize;
	protected ArrayList<ClusterSet> clustersList;
	protected ArrayList<Integer> frequencyList;
	protected ClusterSet clusters;
	protected ClusterSet clustersforOthers;
	protected AggregateProfile agProfiles;
	protected DistanceMetric distanceMeasures;
	protected DoubleMatrix2D distanceMatrix;
	protected DoubleMatrix2D distances;
	protected double estimatedRadius = 0;

	// GUI
	protected JPanel rootPanel;
	protected JScrollPane graphScrollPane;
	protected GUIPropertyInteger clusterXDimBox = new GUIPropertyInteger(
			"Width = ", 3, 1, 100);
	protected GUIPropertyInteger clusterYDimBox = new GUIPropertyInteger(
			"Height = ", 3, 1, 100);
	protected GUIPropertyInteger randomSeedBox = new GUIPropertyInteger(
			"Random Seed = ", 999, 1, 100000);
	protected GUIPropertyInteger numberOfTrainingBox;
	protected GUIPropertyInteger radiusBox = new GUIPropertyInteger(
			"Radius = ", 2, 0, 50);
	// if we use Tree view, this should be modified.
	protected GUIPropertyBoolean hideNullCluster = new GUIPropertyBoolean(
			"Hide null clusters", true, null);// this);
	protected JSlider scatterSlider;
	protected JPanel sliderPanel_Scatter;
	protected JButton startButton;
	protected ProgressPanel progress;
	protected SOMUI somUI;
	protected GUIPropertyListEnumeration backgroundStyle;
	protected GUIPropertyListEnumeration coloringStyle;
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);

	public SOMAlgorithm() {
		super(
				"SOM Clustering",
				"SOM Clustering allows the user to specify"
						+ " width and height. The algorithm will return"
						+ " the number (width*height) of clusters and show a screen for clusters");
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
		somUI = null;
		setupGUI();
		return rootPanel;
	}

	public void cluster() {
		// prepare progress display
		progress = new ProgressPanel("Clustering");
		progress.setNote("processing...");
		graphScrollPane.getViewport().setView(progress.getPanel());

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
		for (int i = 0; i < traceSize; i++)
			traceList.add(Integer.valueOf(i));
		this.clusterXDim = clusterXDimBox.getValue();
		this.clusterYDim = clusterYDimBox.getValue();
		this.randomSeed = randomSeedBox.getValue();
		this.radius = radiusBox.getValue();
		this.clustersList = new ArrayList<ClusterSet>();
		this.frequencyList = new ArrayList<Integer>();
		clusterArray = new Cluster[clusterXDim][clusterYDim];
		pointArray = new InstancePoint[clusterXDim][clusterYDim];
		// run SOM
		clusters = runSOM();
		// make result GUI
		somUI = new SOMUI(clusters, clusterXDimBox.getValue(), clusterYDimBox
				.getValue(), scatterSlider.getValue(), (String) backgroundStyle
				.getValue(), (String) coloringStyle.getValue(), getUMatrix());

	}

	private ClusterSet runSOM() {
		ClusterSet clusters = new ClusterSet(input.getLog());
		// initial points
		initSOM();

		progress.setNote("processing...");
		int trainingSize = numberOfTrainingBox.getValue();
		progress.setMinMax(0, trainingSize + traceSize);
		// training
		bTraining = true;
		for (int k = 0; k < trainingSize; k++) {
			progress.setProgress(k);
			progress.setNote(k + " of " + trainingSize
					+ " training steps (initialzing cells) ...");
			double weight = (0.001) * (1 - k / Math.pow(10, 2)); // k
			calcualteDistance(k, weight);
		}

		// cluster
		bTraining = false;
		for (int i = 0; i < traceSize; i++) {
			progress.setProgress(trainingSize + i);
			progress.setNote(i + " of " + traceSize
					+ " clustering steps (allocating instances) ...");
			calcualteDistance(i, 0);
		}

		for (int i = 0; i < clusterYDim; i++)
			for (int j = 0; j < clusterXDim; j++) {
				clusters.addCluster(clusterArray[j][i]);
			}
		// rename cluster including # of instances in it.
		for (int i = 0; i < clusterXDim; i++)
			for (int j = 0; j < clusterYDim; j++)
				clusterArray[i][j].setName(clusterArray[i][j].getName() + "("
						+ clusterArray[i][j].size() + ")");
		return clusters;
	}

	private void initSOM() {
		clusterArray = new Cluster[clusterXDim][clusterYDim];
		pointArray = new InstancePoint[clusterXDim][clusterYDim];
		agProfiles.setRandomSeed(randomSeedBox.getValue());

		for (int i = 0; i < clusterXDim; i++)
			for (int j = 0; j < clusterYDim; j++) {
				clusterArray[i][j] = new Cluster(input.getLog(), "Cluster(" + j
						+ "," + i + ")"); // name --> Cluster (y,x)
				pointArray[i][j] = new InstancePoint();
				for (int k = 0; k < agProfiles.numberOfItems(); k++) {
					double t = agProfiles.getRandomValue(k);
					pointArray[i][j].set(agProfiles.getItemKey(k), t);// agProfiles.getRandomValue(k));
				}
			}
		// calcuateEstimatedRadius();
	}

	private double[][] getUMatrix() {
		double[][] uMatrix = new double[clusterXDim][clusterYDim];
		// getting distances between nodes
		for (int i = 0; i < clusterXDim; i++) {
			for (int j = 0; j < clusterYDim; j++) {
				double tempDistance = distanceMeasures.getDistance(
						pointArray[i][j],
						pointArray[i][((j + 1) % clusterYDim)]);
				tempDistance += distanceMeasures.getDistance(pointArray[i][j],
						pointArray[(i + 1) % clusterXDim][j]);
				tempDistance += distanceMeasures
						.getDistance(pointArray[i][j], pointArray[(i + 1)
								% clusterXDim][((j + 1) % clusterYDim)]);
				tempDistance += distanceMeasures
						.getDistance(
								pointArray[i][(j + 1) % clusterYDim],
								pointArray[(i + 1) % clusterXDim][((j + 1) % clusterYDim)]);
				tempDistance += distanceMeasures.getDistance(pointArray[(i + 1)
						% clusterXDim][j],
						pointArray[i][((j + 1) % clusterYDim)]);
				tempDistance += distanceMeasures
						.getDistance(
								pointArray[(i + 1) % clusterXDim][j],
								pointArray[(i + 1) % clusterXDim][((j + 1) % clusterYDim)]);

				uMatrix[i][j] = tempDistance / 6;
			}
		}
		return uMatrix;
	}

	// do not use because of poor performance
	/*
	 * private void calcuateEstimatedRadius() { // calculating real paretoradius
	 * int n= agProfiles.numberOfInstances();
	 * progress.setNote("estmating radius for P-matrix...");
	 * progress.setMinMax(0,n*(n-1)/2); progress.setProgress(0); double
	 * optimalMedian = 0.2013 * n;
	 * 
	 * // calculating distances between every example double[] distances = new
	 * double[n * n]; double[] distances2 = new double[n * n]; int k=0; for (int
	 * i = 0; i < n; i++) { for (int j = i+1; j < n; j++) { distances[i * n + j]
	 * =
	 * distanceMeasures.getDistance(agProfiles.getPoint(i),agProfiles.getPoint(
	 * j)); distances[j * n + i] = distances[i * n + j]; distances2[i * n + j] =
	 * distances[i * n + j]; distances2[j * n + i] = distances[i * n + j]; k++;
	 * progress.setProgress(k); } } Arrays.sort(distances); double
	 * percentilSetDifference = Double.POSITIVE_INFINITY; // finding percentil,
	 * closest to paretoradius double radius; for (int percentil = 0; percentil
	 * < 100; percentil++) { int[] nn = new int[n]; radius = distances[(int)
	 * Math.round(((double) (percentil * n * n)) / 100)]; for (int i = 0; i < n;
	 * i++) { for (int j = 0; j < n; j++) { if (distances2[i * n + j]<= radius)
	 * { nn[i]++; } } } Arrays.sort(nn); int currentMedian = nn[n / 2] - 1;
	 * //point himself is no real neighbour, but always nearest point if
	 * (Math.abs(currentMedian - optimalMedian) <= percentilSetDifference) {
	 * percentilSetDifference = Math.abs(currentMedian - optimalMedian); } else
	 * { estimatedRadius = radius; break; } } }
	 * 
	 * // do not use because of poor performance private double[][] getPMatrix()
	 * { int n= agProfiles.numberOfInstances(); // generating P Matrix
	 * double[][] pMatrix = new double[clusterXDim][clusterYDim]; for (int i =
	 * 0; i < clusterXDim; i++) { for (int j = 0; j < clusterYDim; j++) {
	 * InstancePoint ip = pointArray[i][j]; int neighbours = 0; for (int x = 0;
	 * x < n; x++) { double distance = distanceMeasures.getDistance
	 * (agProfiles.getPoint(x),pointArray[i][j]); if (distance <
	 * estimatedRadius) { neighbours++; } } pMatrix[i][j] = ((double)
	 * neighbours) / n; } } return pMatrix; }
	 */

	private void calcualteDistance(int index, double weight) {
		int rowMinIndex, colMinIndex;
		double minValue;
		rowMinIndex = -1;
		colMinIndex = -1;
		minValue = Double.MAX_VALUE;
		for (int j = 0; j < clusterXDim; j++)
			for (int k = 0; k < clusterYDim; k++) {
				double tempDistance = distanceMeasures.getDistance(agProfiles
						.getPoint(index), pointArray[j][k]);
				if (tempDistance < minValue) {
					rowMinIndex = j;
					colMinIndex = k;
					minValue = tempDistance;
				}
			}
		if (bTraining) {
			// update weights within boundary
			int temp, row1, row2, col1, col2;
			temp = rowMinIndex - radius;
			row1 = (temp > 0) ? temp : 0;
			temp = rowMinIndex + radius;
			row2 = (temp > clusterXDim) ? clusterXDim : temp;
			temp = colMinIndex - radius;
			col1 = (temp > 0) ? temp : 0;
			temp = colMinIndex + radius;
			col2 = (temp > clusterYDim) ? clusterYDim : temp;
			// update vector
			for (int j = row1; j < row2; j++) {
				for (int l = col1; l < col2; l++) {
					for (int k = 0; k < agProfiles.numberOfItems(); k++) {
						double currentValue = pointArray[j][l].get(agProfiles
								.getItemKey(k));
						currentValue += weight
								* (agProfiles.getValue(index, k) - currentValue);
						pointArray[j][l].set(agProfiles.getItemKey(k),
								currentValue);
					}
				}
			}
		} else {
			clusterArray[rowMinIndex][colMinIndex].addTrace(index);
		}
	}

	private void generateClustersForOthers() {
		clustersforOthers = new ClusterSet(input.getLog(), agProfiles); // added
		for (Cluster cluster : clusters.getClusters()) {
			if (hideNullCluster.getValue() == true && cluster.size() == 0)
				continue;
			clustersforOthers.addCluster(cluster);
		}

	}

	// GUI methods
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

		menuPanel.add(clusterXDimBox.getPropertyPanel());
		menuPanel.add(clusterYDimBox.getPropertyPanel());
		menuPanel.add(radiusBox.getPropertyPanel());
		numberOfTrainingBox = new GUIPropertyInteger("Number of training = ",
				Math.round(agProfiles.numberOfInstances() / 10 + 1), 1,
				agProfiles.numberOfInstances());
		menuPanel.add(numberOfTrainingBox.getPropertyPanel());
		menuPanel.add(randomSeedBox.getPropertyPanel());

		// background style
		ArrayList<String> values = new ArrayList<String>();
		values.add(SOMUI.ST_LANDSCALE);
		values.add(SOMUI.ST_BLACK);
		backgroundStyle = new GUIPropertyListEnumeration("Background Style",
				"", values, this, 150);
		menuPanel.add(backgroundStyle.getPropertyPanel());

		// background coloring style
		ArrayList<String> values2 = new ArrayList<String>();
		values2.add(SOMUI.ST_DENSITY);
		values2.add(SOMUI.ST_UMATRIX);
		coloringStyle = new GUIPropertyListEnumeration("Color Style", "",
				values2, this, 150);
		menuPanel.add(coloringStyle.getPropertyPanel());

		// scattering ratio
		sliderPanel_Scatter = new JPanel();
		sliderPanel_Scatter.setOpaque(false);
		sliderPanel_Scatter.setOpaque(false);
		sliderPanel_Scatter.setLayout(new BorderLayout());
		sliderPanel_Scatter.setBorder(BorderFactory.createEmptyBorder(10, 10,
				10, 10));
		JLabel headerLabel = new JLabel("Scattering Ratio : ");
		headerLabel.setBackground(bgColor);
		headerLabel.setForeground(fgColor);
		sliderPanel_Scatter.add(headerLabel, BorderLayout.WEST);
		sliderPanel_Scatter.setPreferredSize(new Dimension(240, 50));
		sliderPanel_Scatter.setMaximumSize(new Dimension(240, 50));
		sliderPanel_Scatter.setMinimumSize(new Dimension(240, 50));
		sliderPanel_Scatter.setSize(new Dimension(240, 50));

		scatterSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
		scatterSlider.setOpaque(false);
		scatterSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (somUI != null) {
					somUI.setScatteredRatio(scatterSlider.getValue());
					somUI.repaint();
				}
			}
		});
		sliderPanel_Scatter.add(scatterSlider, BorderLayout.CENTER);
		menuPanel.add(sliderPanel_Scatter);

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
			generateClustersForOthers();
			graphScrollPane.getViewport().setView(somUI);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateGUI() {
		if (somUI != null)
			somUI.setBackground((String) backgroundStyle.getValue(),
					(String) coloringStyle.getValue());
	}
}
