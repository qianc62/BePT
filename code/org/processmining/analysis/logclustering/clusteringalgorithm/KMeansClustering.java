package org.processmining.analysis.logclustering.clusteringalgorithm;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.processmining.analysis.logclustering.LogCluster;
import org.processmining.analysis.logclustering.LogClusterSet;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.util.Dot;

import att.grappa.Graph;
import att.grappa.GrappaPanel;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import org.processmining.analysis.logclustering.distancemeasure.EuclidianDistanceMeasure;
import java.util.Iterator;
import org.processmining.analysis.logclustering.distancemeasure.DistanceMeasure;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.analysis.logclustering.distancemeasure.CorrelationCoefficientDistanceMeasure;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import org.processmining.framework.util.GUIPropertyInteger;

/**
 * @author Minseok Song
 * 
 */
public class KMeansClustering extends ClusteringAlgorithm implements
		DotFileWriter {

	protected ArrayList<Integer> traceList;
	protected int clusterSize;
	protected DoubleMatrix2D centers;
	protected LogClusterSet clusterSet = new LogClusterSet();
	private int profileSize;
	private EuclidianDistanceMeasure dm = null;
	// GUI
	protected JScrollPane graphScrollPane;
	protected GUIPropertyInteger clusterSizeBox = new GUIPropertyInteger(
			"Number of Clusters = ", 3, 1, 100);
	protected GUIPropertyInteger maxIterationBox = new GUIPropertyInteger(
			"Number of Clusters = ", 50, 1, 100);

	public KMeansClustering() {
		super("KMeans Clustering");
		ArrayList<DistanceMeasure> values = new ArrayList<DistanceMeasure>();
		values.add(new CorrelationCoefficientDistanceMeasure());
		values.add(new EuclidianDistanceMeasure());

		distanceMeasures = new GUIPropertyListEnumeration("Distance Measure",
				values);

	}

	public void build() {
		int traceSize = agProfiles.getTraceSize();
		traceList = new ArrayList<Integer>();
		for (int i = 0; i < traceSize; i++)
			traceList.add(Integer.valueOf(i));

		this.clusterSize = clusterSizeBox.getValue();
		DoubleMatrix2D distances;
		dm = new EuclidianDistanceMeasure();

		// initial points
		initCenters();

		// initialize clusters
		double temp[];
		double temp_old[] = new double[clusterSize];
		LogClusterSet oldClusterSet = null;
		;// = new LogClusterSet();

		for (int k = 0; k < maxIterationBox.getValue(); k++) {
			System.out.println(k + "th iteration ===============");
			clusterSet.clear();
			for (int i = 0; i < clusterSize; i++)
				clusterSet.addLogCluster(new LogCluster());

			System.out.println("========== distances ===============");
			// distances = agProfiles.calcuateDistance(dm, centers);
			distances = agProfiles.calcuateDistance(getDistanceMeasure(),
					centers);
			temp = new double[clusterSize];

			for (int i = 0; i < distances.rows(); i++) {

				System.out.print(i + "th element : ");
				int index = -1;
				double value = Double.MAX_VALUE;
				for (int j = 0; j < distances.columns(); j++) {
					System.out.print(j + " = " + distances.get(i, j) + ", ");
					if (value > distances.get(i, j)) {
						index = j;
						value = distances.get(i, j);
					}
				}
				// assign cluster
				System.out.println("index = " + index);
				((LogCluster) clusterSet.getClusters().get(index))
						.addTraceStat(traceList.get(i));
				temp[index] += value;
				// calcualte mean
			}

			// println cluster
			tempPrint();

			// recalculate center
			if (k == 0) {
				for (int i = 0; i < clusterSize; i++)
					temp_old[i] = temp[i];
				oldClusterSet = (LogClusterSet) clusterSet.clone();
			} else {
				if (clusterSet.equals(oldClusterSet)) {
					break;
				} else {
					oldClusterSet = (LogClusterSet) clusterSet.clone();
				}
			}
			calculateCenter();
		}
	}

	private void initCenters() {
		profileSize = agProfiles.getProfileSize();
		centers = DoubleFactory2D.dense.make(clusterSize, profileSize, 0);
		for (int i = 0; i < clusterSize; i++) {
			DoubleMatrix1D tempPoint = agProfiles.generateRandomPoint();
			System.out.println("point = " + tempPoint.toString());
			for (int j = 0; j < profileSize; j++)
				centers.set(i, j, tempPoint.get(j));
		}
	}

	private void calculateCenter() {
		for (int i = 0; i < clusterSize; i++) {
			if (clusterSet.getClusters().get(i).size() == 0)
				continue;
			DoubleMatrix1D tempPoint = agProfiles
					.calcuateCenter(((LogCluster) clusterSet.getClusters().get(
							i)).getTraces());
			for (int j = 0; j < profileSize; j++)
				centers.set(i, j, tempPoint.get(j));
		}

	}

	private void tempPrint() {
		for (int i = 0; i < clusterSize; i++) {
			LogCluster temp = clusterSet.getClusters().get(i);
			System.out.print("cluster = ");
			Iterator itr = temp.getTraces().iterator();
			while (itr.hasNext()) {
				System.out.print(", " + itr.next());
			}
			System.out.println();
		}
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
		optionsPanel.add(maxIterationBox.getPropertyPanel());
		return optionsPanel;
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
		for (LogCluster cluster : clusterSet.getClusters()) {
			if (cluster.size() == 0)
				continue;
			bw
					.write("cluster"
							+ clusterCounter
							+ " [shape=\"octagon\",style=\"filled\",fillcolor=\"chartreuse3\",label=\"Cluster "
							+ clusterCounter + "\\n" + cluster.size()
							+ "\"];\n");
			clusterCounter++;
		}
		DoubleMatrix2D length = dm.calculateDistance(centers);
		// write edges
		clusterCounter = 0;
		for (int i = 0; i < clusterSet.size() - 1; i++) {
			if (clusterSet.getClusters().get(i).size() == 0)
				continue;
			int clusterCounter2 = clusterCounter + 1;
			for (int k = i + 1; k < clusterSet.size(); k++) {
				if (i == k || clusterSet.getClusters().get(k).size() == 0)
					continue;
				bw.write("cluster" + clusterCounter + " -- cluster"
						+ clusterCounter2 + " [label=\"" + length.get(i, k)
						+ "\"];\n");
				clusterCounter2++;
			}
			clusterCounter++;
		}
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

		try {
			graphScrollPane.getViewport().setView(getGraphPanel());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}

}
