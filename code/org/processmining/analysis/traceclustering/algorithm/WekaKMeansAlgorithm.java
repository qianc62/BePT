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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Minseok Song
 */
public class WekaKMeansAlgorithm extends WekaAlgorithm implements
		DotFileWriter, GuiNotificationTarget {

	protected GUIPropertyInteger numberOfRunsBox = new GUIPropertyInteger(
			"Number of Runs = ", 50, 1, 100);
	protected GUIPropertyListEnumeration selectResultBox;
	protected ArrayList<Integer> frequencyList;

	// GUI
	public WekaKMeansAlgorithm() {
		super("K-Means Clustering",
				"K-Means Clustering allows the user to specify"
						+ " the number of clusters. The algorithm will return"
						+ " the number of clusters which users want.");
		clusters = null;
		clusterer = new SimpleKMeans();
		frequencyList = new ArrayList<Integer>();
	}

	protected void doCluster() {
		progress.setNote("run...");
		progress.setMinMax(0, numberOfRunsBox.getValue() + 1);
		progress.setProgress(0);

		double temp_sum = Double.MAX_VALUE;
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
		System.out.println("sdfasd =" + frequencyList.size());
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
		setCenters();
	}

	private ClusterSet runKmean() {
		// initialize variables
		ClusterSet clusters = new ClusterSet(input.getLog(), agProfiles);
		traceList = new ArrayList<Integer>();
		for (int i = 0; i < traceSize; i++)
			traceList.add(Integer.valueOf(i));
		this.clusterSize = clusterSizeBox.getValue();
		this.clustersList = new ArrayList<ClusterSet>();

		clusters.clear();
		for (int i = 0; i < clusterSize; i++)
			clusters.addCluster(new Cluster(input.getLog(), "Cluster" + i));

		try {
			((SimpleKMeans) clusterer)
					.setNumClusters(clusterSizeBox.getValue());
			((SimpleKMeans) clusterer).setSeed(random.nextInt());
			((SimpleKMeans) clusterer).buildClusterer(data);

			for (int i = 0; i < data.numInstances(); i++) {
				int k = clusterer.clusterInstance(data.instance(i));
				((Cluster) clusters.getClusters().get(k)).addTrace(traceList
						.get(i));
			}

		} catch (Exception c) {
			Message.add("Weka Error: " + c.toString(), Message.ERROR);
		}
		currentDistanceSum = ((SimpleKMeans) clusterer).getSquaredError();
		return clusters;
	}

	private void setCenters() {
		Instances data2 = ((SimpleKMeans) clusterer).getClusterCentroids();
		try {
			instancePoints = new InstancePoint[clusterer.numberOfClusters()];
			for (int i = 0; i < clusterer.numberOfClusters(); i++) {
				instancePoints[i] = new InstancePoint();
			}

			for (int i = 0; i < data2.numAttributes(); i++) {
				double db[] = data2.attributeToDoubleArray(i);
				for (int c = 0; c < clusterer.numberOfClusters(); c++) {
					instancePoints[c].set(agProfiles.getItemKey(i), db[c]);
				}
			}
		} catch (Exception c) {
			Message.add("Weka Error: " + c.toString(), Message.ERROR);
		}
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

	protected SmoothPanel getMenuPanel() {
		SmoothPanel menuPanel = new SmoothPanel();
		menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.PAGE_AXIS));

		menuPanel.add(clusterSizeBox.getPropertyPanel());
		menuPanel.add(numberOfRunsBox.getPropertyPanel());

		ArrayList<String> values2 = new ArrayList<String>();
		values2.add(ST_DISTANCE);
		values2.add(ST_FREQUENCY);
		selectResultBox = new GUIPropertyListEnumeration(
				"Choose a cluster based on =", null, values2, null, 230);
		menuPanel.add(randomSeedBox.getPropertyPanel());

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
		return menuPanel;
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
			if (cluster.size() == 0)
				continue;
			cluster.setName("Cluster " + clusterCounter++);
			clustersforOthers.addCluster(cluster);
			bw
					.write(cluster.getName().replace(" ", "")
							+ " [shape=\"octagon\",style=\"filled\",fillcolor=\"chartreuse3\",label=\""
							+ cluster.getName() + "\\n" + cluster.size()
							+ "\"];\n");// + centers[clusterCounter-1]+
			// "\"];\n");
		}
		// todo
		/*
		 * DoubleMatrix2D length =
		 * calculateInterClusterDistance(clustersforOthers); // write edges for
		 * (int i = 0; i < clusters.size() - 1; i++) { Cluster cluster1 =
		 * clusters.getClusters().get(i); if
		 * (clusters.getClusters().get(i).size() == 0) continue; for (int k = i
		 * + 1; k < clusters.size(); k++) { if
		 * (clusters.getClusters().get(k).size() == 0) continue; Cluster
		 * cluster2 = clusters.getClusters().get(k);
		 * bw.write(cluster1.getName().replace(" ", "") + " -- " +
		 * cluster2.getName().replace(" ", "") + " [label=\"" + length.get(i, k)
		 * + "\"];\n"); } }
		 */
		// close graph
		bw.write("}\n");
	}

}
