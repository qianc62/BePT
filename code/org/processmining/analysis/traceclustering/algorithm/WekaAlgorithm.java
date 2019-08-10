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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.analysis.traceclustering.distance.DistanceMetric;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.Dot;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GuiNotificationTarget;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import att.grappa.Graph;
import att.grappa.GrappaPanel;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Minseok Song
 */
public abstract class WekaAlgorithm extends ClusteringAlgorithm implements
		DotFileWriter, GuiNotificationTarget {

	protected Random random = new Random();
	protected ArrayList<Integer> traceList;
	protected int clusterSize;
	protected int traceSize;
	protected double currentDistanceSum;
	protected ArrayList<ClusterSet> clustersList;
	protected ClusterSet clusters;
	protected ClusterSet clustersforOthers;
	protected AggregateProfile agProfiles;
	protected InstancePoint[] instancePoints;
	protected DistanceMetric distanceMeasures;
	protected DoubleMatrix2D distanceMatrix;
	protected DoubleMatrix2D distances;
	protected static String ST_FREQUENCY = "Frequency";
	protected static String ST_DISTANCE = "Distance";
	protected Clusterer clusterer = null;
	protected Instances data = null;

	// GUI
	protected JPanel rootPanel;
	protected JScrollPane graphScrollPane;
	protected GUIPropertyInteger clusterSizeBox = new GUIPropertyInteger(
			"Number of Clusters = ", 3, 1, 100);
	protected GUIPropertyInteger maxIterationBox = new GUIPropertyInteger(
			"Number of Iterations = ", 50, 1, 1000);
	protected GUIPropertyInteger randomSeedBox = new GUIPropertyInteger(
			"Random Seed = ", 999, 1, 100000);
	protected JButton startButton;
	protected ProgressPanel progress;
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);

	public WekaAlgorithm() {
		super("Weka Clustering", "Weka Clustering allows the user to specify"
				+ " the number of clusters. The algorithm will return"
				+ " the number of clusters which users want.");
		clusters = null;
	}

	public WekaAlgorithm(String name, String desc) {
		super(name, desc);
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
		progress.setMinMax(0, 3);
		progress.setProgress(0);
		graphScrollPane.getViewport().setView(progress.getPanel());

		makeWekaData();
		random.setSeed(randomSeedBox.getValue());

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

	protected void build() {
		initClusterer();
		doCluster();
	}

	protected void initClusterer() {
		// initialize variables
		progress.setProgress(1);
		progress.setNote("init clusterer...");

		clusters = new ClusterSet(input.getLog());
		traceList = new ArrayList<Integer>();
		for (int i = 0; i < traceSize; i++)
			traceList.add(Integer.valueOf(i));
		this.clusterSize = clusterSizeBox.getValue();
		this.clustersList = new ArrayList<ClusterSet>();
		clusters.clear();
		for (int i = 0; i < clusterSize; i++)
			clusters.addCluster(new Cluster(input.getLog(), "Cluster" + i));
	}

	protected abstract void doCluster();

	protected void assignInstace() {
		progress.setProgress(3);
		progress.setNote("assign instance to cluster...");

		try {
			for (int i = 0; i < data.numInstances(); i++) {
				int k = clusterer.clusterInstance(data.instance(i));
				((Cluster) clusters.getClusters().get(k)).addTrace(traceList
						.get(i));
			}

		} catch (Exception c) {
			Message.add("Weka Error: " + c.toString(), Message.ERROR);
		}
	}

	private void makeWekaData() {
		progress.setProgress(2);
		progress.setNote("make weka data...");

		// create attribute information
		FastVector attributeInfo = new FastVector();
		// make attribute
		// clean the relevant attribute list and re-fill based on new selection
		// scope
		for (int i = 0; i < agProfiles.numberOfItems(); i++) {
			String name = CpnUtils.replaceSpecialCharacters(agProfiles
					.getItemKey(i));
			Attribute wekaAtt = new Attribute(name);
			attributeInfo.addElement(wekaAtt);
		}
		attributeInfo.trimToSize();
		data = new Instances("Clustering", attributeInfo, 0);
		try {
			for (int i = 0; i < traceSize; i++) {
				Instance instance0 = new Instance(attributeInfo.size());
				for (int j = 0; j < agProfiles.numberOfItems(); j++) {
					String name = CpnUtils.replaceSpecialCharacters(agProfiles
							.getItemKey(j));
					Attribute wekaAtt = data.attribute(name);
					if (wekaAtt != null) {
						double doubleAttValue = (new Double(agProfiles
								.getValue(i, j))).doubleValue();
						instance0.setValue(wekaAtt, doubleAttValue);
					} else {
						Message.add("Weka Error: fail to add", Message.ERROR);
					}
				}
				instance0.setDataset(data);
				data.add(instance0);
			}
		} catch (Exception c) {
			Message.add("Weka Error: " + c.toString(), Message.ERROR);
		}
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
		rootPanel.add(getMenuPanel(), BorderLayout.EAST);
	}

	protected abstract SmoothPanel getMenuPanel();

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
			if (cluster.size() == 0)
				continue;
			cluster.setName("Cluster " + clusterCounter);
			clustersforOthers.addCluster(cluster);
			bw
					.write(cluster.getName().replace(" ", "")
							+ " [shape=\"octagon\",style=\"filled\",fillcolor=\"chartreuse3\",label=\""
							+ cluster.getName() + "\\n" + cluster.size()
							+ "\"];\n");
			cluster.setName("Cluster " + clusterCounter + " (" + cluster.size()
					+ ")");
			clusterCounter++;
		}
		// close graph
		bw.write("}\n");
	}

}
