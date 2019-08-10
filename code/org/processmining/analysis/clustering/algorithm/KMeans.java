package org.processmining.analysis.clustering.algorithm;

import org.processmining.analysis.clustering.SimCalculator;

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
/* K-means �˰?�� */
/*
 * select K centroid from N process for(no change) { for (N process){ calculate
 * mean similarities b/w the process and K clusters cluster the process to the
 * most similar cluster } }
 */

public class KMeans {

	int numOfProcess;
	int K;

	String[] proc;
	double[][] totalSimilarity;
	int[] belongedCluster = new int[numOfProcess];

	public KMeans(SimCalculator sim, int K) {

		this.proc = sim.getProc();
		this.numOfProcess = proc.length;
		this.totalSimilarity = sim.getTotalSimilarity();

		this.K = K;
	}

	private void makeSead() {
		for (int i = 0; i < K; i++) {
			belongedCluster[i] = i;
		}
		for (int i = K; i < numOfProcess; i++) {
			belongedCluster[i] = -1;
		}
	}

	private int mergeToCluster(int indexP) {
		int indexC = -1;
		double maxMeanSim = 0.0;

		for (int i = 0; i < K; i++) {
			int count = 0;
			double sumSim = 0;
			for (int j = 0; j < numOfProcess; j++) {
				if (belongedCluster[j] == i) {
					sumSim += totalSimilarity[indexP][j];
					count++;
				}
			}
			double meanSim = sumSim / count;
			if (meanSim > maxMeanSim) {
				indexC = i;
				maxMeanSim = meanSim;
			}
		}
		return indexC;
	}

	public void kmeansAlgorithm() {

		makeSead();

		int changeCount = 0;
		int loofCount = 0;
		do {
			changeCount = 0;
			for (int i = 0; i < numOfProcess; i++) {
				int tmpBelongedCluster = belongedCluster[i];
				belongedCluster[i] = mergeToCluster(i); // cluster the process
				// to most similar
				// cluster
				if (tmpBelongedCluster != belongedCluster[i]) {
					changeCount++;
				}
			}
			System.out.println(loofCount++);
		} while (changeCount > 0);

	}

}
