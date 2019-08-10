package org.processmining.analysis.clustering.algorithm;

import java.util.TreeMap;
import java.util.Vector;

import org.processmining.analysis.clustering.SimCalculator;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class AHC {

	int numOfProcess;
	int K; // N/A, but if K is given.
	SimCalculator sim;

	String[] proc;
	double[][] totalSimilarity;
	double[][] simOfClusters;
	int[] belongedCluster = null;
	int[] numOfProcInCluster = null;
	public TreeMap phm_index = new TreeMap(); // pi Sequence -> index

	int[][] clusterProcedure = null;
	double[] clusterProcedureSim = null;
	public Vector[] logList;
	int clusteringCount = 0;

	public AHC(SimCalculator sim, int minimumNumOfClusters) {

		this.sim = sim;
		this.proc = sim.getProc();
		this.numOfProcess = proc.length;
		this.totalSimilarity = sim.getTotalSimilarity();

		this.belongedCluster = new int[numOfProcess];
		this.numOfProcInCluster = new int[numOfProcess];

		this.K = minimumNumOfClusters;

		System.out.println("***proc.length=" + numOfProcess);
		clusterProcedure = new int[numOfProcess - 1][2];
		clusterProcedureSim = new double[numOfProcess - 1];
		logList = new Vector[numOfProcess - 1];
	}

	private double findNearestCluster() {

		double maxMeanSim = -1;
		double meanSim = 0;
		int cluster1 = -1;
		int cluster2 = -1;

		if (clusteringCount == 0) {
			simOfClusters = totalSimilarity;
			for (int i = 0; i < numOfProcess - 1; i++) {
				for (int j = i + 1; j < numOfProcess; j++) {
					meanSim = simOfClusters[i][j];
					if (meanSim > maxMeanSim) {
						maxMeanSim = meanSim; // + 0.00000000001; //Orderig
						// Tuning Tip
						cluster1 = i;
						cluster2 = j;
					}
				}
				// System.out.println("maxMeanSim=" + maxMeanSim + " : meanSim="
				// + meanSim);
			}
		} else {
			for (int i = 0; i < numOfProcess - 1; i++) {
				if (numOfProcInCluster[i] == 0) {
					continue;
				}

				for (int j = i + 1; j < numOfProcess; j++) {
					if (numOfProcInCluster[j] == 0) {
						continue;
					}

					meanSim = simOfClusters[i][j];
					if (meanSim > maxMeanSim) {
						maxMeanSim = meanSim; // + 0.00000000001; //Orderig
						// Tuning Tip
						cluster1 = i;
						cluster2 = j;
					}
				}
				// System.out.println("maxMeanSim=" + maxMeanSim + " : meanSim="
				// + meanSim);

			}
		}

		// cluster 2 => cluster 1
		System.out.println("Cluster " + cluster2 + "->" + cluster1 + " : "
				+ maxMeanSim);
		clusterProcedure[clusteringCount][0] = cluster2;
		clusterProcedure[clusteringCount][1] = cluster1;
		clusterProcedureSim[clusteringCount] = maxMeanSim;
		Vector v1 = (Vector) phm_index.get(proc[cluster2]);
		Vector v2 = (Vector) phm_index.get(proc[cluster1]);
		logList[clusteringCount] = new Vector();
		for (int i = 0; i < v1.size(); i++) {
			logList[clusteringCount].add(v1.get(i));
		}
		for (int i = 0; i < v2.size(); i++) {
			logList[clusteringCount].add(v2.get(i));
		}
		v2.addAll(v1);
		clusteringCount++;

		Message.add("The number of clusters = "
				+ (numOfProcess - clusteringCount));

		// update
		int n1 = numOfProcInCluster[cluster1];
		int n2 = numOfProcInCluster[cluster2];
		for (int j = 0; j < numOfProcess; j++) {
			if (j < cluster1) {
				simOfClusters[j][cluster1] = (n1 * simOfClusters[j][cluster1] + n2
						* simOfClusters[j][cluster2])
						/ (n1 + n2);
			} else if (j < cluster2) {
				simOfClusters[cluster1][j] = (n1 * simOfClusters[cluster1][j] + n2
						* simOfClusters[j][cluster2])
						/ (n1 + n2);
			} else {
				simOfClusters[cluster1][j] = (n1 * simOfClusters[cluster1][j] + n2
						* simOfClusters[cluster2][j])
						/ (n1 + n2);
			}
		}
		numOfProcInCluster[cluster1] = n1 + n2;
		numOfProcInCluster[cluster2] = 0;

		for (int j = 0; j < numOfProcess; j++) {
			if (belongedCluster[j] == cluster2) {
				belongedCluster[j] = cluster1;
			}
		}

		return maxMeanSim;
	}

	public int[][] getClusterProcedure() {
		return clusterProcedure;
	}

	public double[] getClusterProcedureSim() {
		return clusterProcedureSim;
	}

	/*
	 * private double calculateSimilarityOfTwoCluster(int cluster1, int
	 * cluster2) {
	 * 
	 * int count = 0; double sumSim = 0;
	 * 
	 * for(int i=0; i<numOfProcess; i++) { if(belongedCluster[i]==cluster1)
	 * 
	 * for(int j=0; j<numOfProcess; j++) { if(belongedCluster[j]==cluster2){
	 * 
	 * sumSim += totalSimilarity[i][j]; count++; } } }
	 * 
	 * double meanSim; if (count==0) meanSim = 0; else meanSim = sumSim / count;
	 * 
	 * return meanSim; }
	 */

	public void AHCAlgorithm() {

		// makeEachCluster
		for (int i = 0; i < numOfProcess; i++) {
			belongedCluster[i] = i;
			numOfProcInCluster[i] = 1;
			Vector v = (Vector) sim.logS.phm_index.get(proc[i]);
			Vector v2 = new Vector();
			for (int j = 0; j < v.size(); j++) {
				v2.add(v.get(j));
			}
			phm_index.put(proc[i], v2);
		}

		// clustering
		double maxMeanSim = findNearestCluster();
		for (int i = 1; i < numOfProcess - K; i++) {
			maxMeanSim = findNearestCluster();
			// System.out.println("-------------->("+i+") maxMeanSim = " +
			// maxMeanSim);
			// if(maxMeanSim==0) {System.out.println("low limit"); break;}
		}

	}
}
