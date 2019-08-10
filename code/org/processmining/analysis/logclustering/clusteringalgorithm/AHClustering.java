package org.processmining.analysis.logclustering.clusteringalgorithm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.analysis.logclustering.LogCluster;
import org.processmining.analysis.logclustering.LogClusterSet;
import org.processmining.analysis.logclustering.distancemeasure.CorrelationCoefficientDistanceMeasure;
import org.processmining.analysis.logclustering.distancemeasure.DistanceMeasure;
import org.processmining.analysis.logclustering.distancemeasure.EuclidianDistanceMeasure;
import org.processmining.analysis.logclustering.distancemeasure.HammingDistanceMeasure;
import org.processmining.analysis.logclustering.model.AHCTreeNode;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;
import org.processmining.framework.util.GUIPropertyDoubleTextField;
import org.processmining.framework.util.GUIPropertyListEnumeration;

import att.grappa.Graph;
import att.grappa.GrappaPanel;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Minseok Song
 * 
 */
public class AHClustering extends ClusteringAlgorithm implements DotFileWriter {

	protected ArrayList<Integer> traceList;
	protected int clusterSize;
	protected LogClusterSet clusterSet = new LogClusterSet();

	private AHCTreeNode rootAHCTree = null;
	private ArrayList<LogCluster> listAHCTree = null;
	// GUI
	protected JScrollPane graphScrollPane;
	protected JSlider sliderUpperUI;
	protected JSlider sliderLowerUI;
	protected JSlider sliderUpper;
	protected JSlider sliderLower;
	protected JLabel diameterLabel;
	protected JLabel diameterLabel2;
	protected JLabel diameterLabel3;
	protected JLabel diameterLabel4;
	protected DecimalFormat format = new DecimalFormat("0.0000");
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);

	// /
	// protected JScrollPane graphScrollPane;
	protected GUIPropertyDoubleTextField clusterSizeBox = new GUIPropertyDoubleTextField(
			"Min distance", 0);
	private int clusterCounter;

	private int traceSize = 0;
	private DoubleMatrix2D distances;

	private double upperBound, lowerBound = 0.0;
	private double upperBoundUI, lowerBoundUI = 0.0;

	public AHClustering() {
		super("AH Clustering");
		listAHCTree = new ArrayList<LogCluster>();
		ArrayList<DistanceMeasure> values = new ArrayList<DistanceMeasure>();
		values.add(new CorrelationCoefficientDistanceMeasure());
		values.add(new EuclidianDistanceMeasure());
		values.add(new HammingDistanceMeasure());
		distanceMeasures = new GUIPropertyListEnumeration("Distance Measure",
				values);
	}

	public void build() {
		// initialize distance method
		clusterSet.clear();
		listAHCTree.clear();

		distances = agProfiles.calcuateDistance(getDistanceMeasure());

		traceSize = agProfiles.getTraceSize();
		traceList = new ArrayList<Integer>();
		for (int i = 0; i < traceSize; i++)
			traceList.add(Integer.valueOf(i));

		// make initial Cluster
		for (int i = 0; i < traceSize; i++) {
			LogCluster tempCluster = new LogCluster();
			tempCluster.addTraceStat(i);
			listAHCTree.add(tempCluster);
		}

		// clustering
		for (int i = 0; i < traceSize - 1; i++) {
			findNearestCluster();
		}
		rootAHCTree = (AHCTreeNode) listAHCTree.get(0);
		upperBound = rootAHCTree.getDistance();
		lowerBound = clusterSizeBox.getValue();
		upperBoundUI = upperBound;
		lowerBoundUI = clusterSizeBox.getValue();
	}

	// The Nearest-Neighbor Algorithm (Min (D1, D2))
	private void findNearestCluster() {
		double minDistance = Double.MAX_VALUE;
		LogCluster aNode = null, bNode = null;
		for (int i = 0; i < listAHCTree.size(); i++) {
			LogCluster aTempNode = listAHCTree.get(i);
			for (int j = i + 1; j < listAHCTree.size(); j++) {
				LogCluster bTempNode = listAHCTree.get(j);
				double tempMinDistance = Double.MAX_VALUE;
				Iterator itr = aTempNode.getTraces().iterator();
				while (itr.hasNext()) {
					int trace1 = ((Integer) itr.next()).intValue();
					Iterator itr2 = bTempNode.getTraces().iterator();
					while (itr2.hasNext()) {
						int trace2 = ((Integer) itr2.next()).intValue();
						if (tempMinDistance > distances.get(trace1, trace2))
							tempMinDistance = distances.get(trace1, trace2);
					}
				}
				if (minDistance > tempMinDistance) {
					minDistance = tempMinDistance;
					aNode = aTempNode;
					bNode = bTempNode;
				}
			}
		}
		AHCTreeNode aAHCTreeNode = new AHCTreeNode(aNode, bNode, minDistance);
		// aAHCTreeNode.setName("node"+numberOfAHCTreeNode++);
		// clusterSet.addLogCluster(aAHCTreeNode);
		listAHCTree.remove(aNode);
		listAHCTree.remove(bNode);
		listAHCTree.add(aAHCTreeNode);
	}

	public int size() {
		int size = 0;

		for (LogCluster aCluster : clusterSet.getClusters()) {
			if (aCluster.size() != 0)
				size++;
		}
		return size;
	}

	public List<LogCluster> getClusters() {
		return clusterSet.getClusters();
	}

	public JPanel getOptionPanel() {
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBorder(BorderFactory.createLineBorder(new Color(150,
				150, 150), 1));
		optionsPanel.add(distanceMeasures.getPropertyPanel());
		optionsPanel.setBorder(BorderFactory.createLineBorder(new Color(150,
				150, 150), 1));
		optionsPanel.add(clusterSizeBox.getPropertyPanel());

		return optionsPanel;
	}

	public void writeToDotEach(Writer bw, AHCTreeNode temp) throws IOException {
		if (temp.getDistance() <= lowerBoundUI)
			return;

		temp.setName(String.valueOf(clusterCounter));
		clusterCounter++;
		String color = null;
		double dist = temp.getDistance();
		if (dist <= lowerBound || dist > upperBound || dist > upperBoundUI)
			color = "gray86";
		else {
			color = "chartreuse3";
			clusterSet.addLogCluster(temp);
		}

		if (dist <= upperBoundUI)
			bw.write("cluster" + temp.getName()
					+ " [shape=\"octagon\",style=\"filled\",fillcolor=\""
					+ color + "\",label=\"Cluster " + temp.getName() + "\\n"
					+ temp.size() + "\\n" + temp.getDistance() + "\"];\n");

		if (temp.getLeft() != null && temp.getLeft() instanceof AHCTreeNode)
			writeToDotEach(bw, (AHCTreeNode) temp.getLeft());
		if (temp.getRight() != null && temp.getRight() instanceof AHCTreeNode)
			writeToDotEach(bw, (AHCTreeNode) temp.getRight());
	}

	public void writeToDotEachArc(Writer bw, AHCTreeNode temp)
			throws IOException {
		if (temp.getLeft() != null && temp.getLeft() instanceof AHCTreeNode) {
			if (((AHCTreeNode) temp.getLeft()).getDistance() > lowerBoundUI
					&& ((AHCTreeNode) temp.getLeft()).getDistance() <= upperBoundUI) {
				if (temp.getDistance() <= upperBoundUI)
					bw.write("cluster" + temp.getName() + " -- cluster"
							+ ((AHCTreeNode) temp.getLeft()).getName()
							+ " [label=\"" + temp.getDistance() + "\"];\n");
				writeToDotEachArc(bw, (AHCTreeNode) temp.getLeft());
			}
		}
		if (temp.getRight() != null && temp.getRight() instanceof AHCTreeNode) {
			if (((AHCTreeNode) temp.getRight()).getDistance() > lowerBoundUI
					&& ((AHCTreeNode) temp.getRight()).getDistance() <= upperBoundUI) {
				if (temp.getDistance() <= upperBoundUI)
					bw.write("cluster" + temp.getName() + " -- cluster"
							+ ((AHCTreeNode) temp.getRight()).getName()
							+ " [label=\"" + temp.getDistance() + "\"];\n");
				writeToDotEachArc(bw, (AHCTreeNode) temp.getRight());
			}
		}
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
		clusterCounter = 0;
		clusterSet.clear();
		// temp display
		writeToDotEach(bw, rootAHCTree);
		writeToDotEachArc(bw, rootAHCTree);
		// close graph
		bw.write("}\n");
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
		gp.setBackground(new Color(220, 220, 210));
		gp.setScaleToFit(true);
		return gp;
	}

	public JPanel getPanel() {
		JPanel temp = new JPanel();
		temp.setLayout(new BorderLayout());
		graphScrollPane = new JScrollPane(new JPanel());
		temp.add(graphScrollPane, BorderLayout.CENTER);

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
		JPanel sliderUpperUIPanel = new JPanel();
		JPanel sliderLowerUIPanel = new JPanel();
		JPanel sliderUpperPanel = new JPanel();
		JPanel sliderLowerPanel = new JPanel();
		sliderPanel.add(sliderUpperUIPanel);
		sliderPanel.add(sliderLowerUIPanel);
		sliderPanel.add(sliderUpperPanel);
		sliderPanel.add(sliderLowerPanel);

		// upper bound panel
		sliderUpperUIPanel.setBackground(bgColor);
		sliderUpperUIPanel.setLayout(new BorderLayout());
		sliderUpperUIPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
				10, 10));
		JLabel headerLabel = new JLabel("Upper UI");
		headerLabel.setBackground(bgColor);
		headerLabel.setForeground(fgColor);
		sliderUpperUIPanel.add(headerLabel, BorderLayout.NORTH);
		sliderUpperUI = new JSlider(JSlider.VERTICAL, 0, 10000, 10000);
		sliderUpperUI.setBackground(bgColor);
		sliderUpperUI.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				upperBoundUI = (double) sliderUpperUI.getValue() / 10000.0
						* rootAHCTree.getDistance();
				diameterLabel.setText("  < " + format.format(upperBoundUI)
						+ "  ");
				if (sliderUpperUI.getValueIsAdjusting() == false) {
					Message.add("Re-clustering with threshold " + upperBoundUI
							+ "...", Message.NORMAL);
					// cluster(threshold);
					try {
						graphScrollPane.getViewport().setView(getGraphPanel());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		sliderUpperUIPanel.add(sliderUpperUI, BorderLayout.CENTER);
		diameterLabel = new JLabel(format.format(rootAHCTree.getDistance()));
		diameterLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterLabel.setBackground(bgColor);
		diameterLabel.setForeground(fgColor);
		sliderUpperUIPanel.add(diameterLabel, BorderLayout.SOUTH);

		// lower bound pannel
		sliderLowerUIPanel.setBackground(bgColor);
		sliderLowerUIPanel.setLayout(new BorderLayout());
		sliderLowerUIPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
				10, 10));
		JLabel headerLabel2 = new JLabel("Lower UI");
		headerLabel2.setBackground(bgColor);
		headerLabel2.setForeground(fgColor);
		sliderLowerUIPanel.add(headerLabel2, BorderLayout.NORTH);

		sliderLowerUI = new JSlider(JSlider.VERTICAL, 0, 10000,
				(int) (lowerBoundUI / rootAHCTree.getDistance() * 10000));
		sliderLowerUI.setBackground(bgColor);
		sliderLowerUI.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				lowerBoundUI = (double) sliderLowerUI.getValue() / 10000.0
						* rootAHCTree.getDistance();
				diameterLabel2.setText("  < " + format.format(lowerBoundUI)
						+ "  ");
				if (sliderLowerUI.getValueIsAdjusting() == false) {
					Message.add("Re-clustering with threshold " + lowerBoundUI
							+ "...", Message.NORMAL);
					// cluster(threshold);
					try {
						graphScrollPane.getViewport().setView(getGraphPanel());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		sliderLowerUIPanel.add(sliderLowerUI, BorderLayout.CENTER);
		diameterLabel2 = new JLabel(format.format(lowerBoundUI));
		diameterLabel2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterLabel2.setBackground(bgColor);
		diameterLabel2.setForeground(fgColor);
		sliderLowerUIPanel.add(diameterLabel2, BorderLayout.SOUTH);

		// added
		// upper bound panel
		sliderUpperPanel.setBackground(bgColor);
		sliderUpperPanel.setLayout(new BorderLayout());
		sliderUpperPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));
		JLabel headerLabel3 = new JLabel("Upper");
		headerLabel3.setBackground(bgColor);
		headerLabel3.setForeground(fgColor);
		sliderUpperPanel.add(headerLabel3, BorderLayout.NORTH);
		sliderUpper = new JSlider(JSlider.VERTICAL, 0, 10000, 10000);
		sliderUpper.setBackground(bgColor);
		sliderUpper.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				upperBound = (double) sliderUpper.getValue() / 10000.0
						* rootAHCTree.getDistance();
				diameterLabel3.setText("  < " + format.format(upperBound)
						+ "  ");
				if (sliderUpper.getValueIsAdjusting() == false) {
					Message.add("Re-clustering with threshold " + upperBound
							+ "...", Message.NORMAL);
					// cluster(threshold);
					try {
						graphScrollPane.getViewport().setView(getGraphPanel());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		sliderUpperPanel.add(sliderUpper, BorderLayout.CENTER);
		diameterLabel3 = new JLabel(format.format(rootAHCTree.getDistance()));
		diameterLabel3.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterLabel3.setBackground(bgColor);
		diameterLabel3.setForeground(fgColor);
		sliderUpperPanel.add(diameterLabel3, BorderLayout.SOUTH);

		// lower bound pannel
		sliderLowerPanel.setBackground(bgColor);
		sliderLowerPanel.setLayout(new BorderLayout());
		sliderLowerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));
		JLabel headerLabel4 = new JLabel("Lower");
		headerLabel4.setBackground(bgColor);
		headerLabel4.setForeground(fgColor);
		sliderLowerPanel.add(headerLabel4, BorderLayout.NORTH);

		sliderLower = new JSlider(JSlider.VERTICAL, 0, 10000, (int) (lowerBound
				/ rootAHCTree.getDistance() * 10000));
		sliderLower.setBackground(bgColor);
		sliderLower.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				lowerBound = (double) sliderLower.getValue() / 10000.0
						* rootAHCTree.getDistance();
				diameterLabel4.setText("  < " + format.format(lowerBound)
						+ "  ");
				if (sliderLower.getValueIsAdjusting() == false) {
					Message.add("Re-clustering with threshold " + lowerBound
							+ "...", Message.NORMAL);
					// cluster(threshold);
					try {
						graphScrollPane.getViewport().setView(getGraphPanel());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		sliderLowerPanel.add(sliderLower, BorderLayout.CENTER);
		diameterLabel4 = new JLabel(format.format(lowerBound));
		diameterLabel4.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterLabel4.setBackground(bgColor);
		diameterLabel4.setForeground(fgColor);
		sliderLowerPanel.add(diameterLabel4, BorderLayout.SOUTH);

		//

		temp.add(sliderPanel, BorderLayout.EAST);

		try {
			// cluster(0.8);
			graphScrollPane.getViewport().setView(getGraphPanel());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;

		/*
		 * JPanel temp = new JPanel(); temp.setLayout(new BorderLayout());
		 * graphScrollPane = new JScrollPane(new JPanel());
		 * temp.add(graphScrollPane, BorderLayout.CENTER);
		 * 
		 * try { graphScrollPane.getViewport().setView(getGraphPanel()); } catch
		 * (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } return temp;
		 */
	}

}
