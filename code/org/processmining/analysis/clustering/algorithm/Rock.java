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
public class Rock {

	int numOfProcess;
	int K; // K-means Algorithm������ cluster ��

	String[] proc;
	double[][] totalSimilarity;

	public Rock(SimCalculator sim, int K) {

		this.proc = sim.getProc();
		this.numOfProcess = proc.length;
		this.totalSimilarity = sim.getTotalSimilarity();

		this.K = K;
	}

	/* ROCK �˰?�� */
	int[][] isNeighbor = new int[numOfProcess][numOfProcess]; // �̿� ����
	int[][] link = new int[numOfProcess][numOfProcess]; // ���μ����� ��ũ
	double theta = 0.3; // �Ѱ谪(threshold). theta���� similarity�� ũ��,
	// neighbor�� �����Ѵ�.
	double[][] goodness = new double[numOfProcess][numOfProcess]; // ���μ�����
	// Ŭ�����͸�
	// ���յ�
	double indexOnEstimation = 0.5; // Ŭ�����͸� �򰡰��

	double[][] q = new double[numOfProcess][numOfProcess - 1]; // ����
	// �ִ�����(goodness)
	int[][] q1 = new int[numOfProcess][numOfProcess - 1]; // ����
	// �ִ�����(������μ�����
	// �ε���)
	int[] heapSize = new int[numOfProcess]; // ���� �ִ������� ũ��

	double[] Q = new double[numOfProcess]; // �۷ι� �ִ�����(goodness)
	int[] Q1 = new int[numOfProcess]; // �۷ι� �ִ�����(�ش����μ���)
	int[] Q2 = new int[numOfProcess]; // �۷ι� �ִ�����(������μ���)

	/* ���μ��� Ŭ�����͸� */
	// int[] mergedProcess = new int[numOfProcess]; //merge�Ǹ� 1�� �ٲ�.
	int numOfCluster = numOfProcess; // Ŭ�������� ����(���� ���� ����),
	int[] includingProcess = new int[numOfProcess]; // Ŭ�����Ͱ� �����ϰ� �ִ�
	// ���μ����� ����

	int[] belongedCluster = new int[numOfProcess]; // ���μ����� ���Ե� Ŭ������

	// �ε���{0,,,k-1}

	// ///////////////////////////////
	// ���μ��� �̿� ã�� ��ũ(link) ��� & ���յ�(goodness) ���
	// ///////////////////////////////
	public void findNeighbor() {

		for (int i = 0; i < numOfProcess; i++) {
			for (int j = 0; j < numOfProcess; j++) {

				if (totalSimilarity[i][j] > theta && i != j) {
					isNeighbor[i][j] = 1;
				} else {
					isNeighbor[i][j] = 0;
				}

			}
		}
	}

	public void countLink() {

		for (int i = 0; i < numOfProcess; i++) {
			for (int j = 0; j < numOfProcess; j++) {

				int count = 0;
				for (int m = 0; m < numOfProcess; m++) {
					if (isNeighbor[i][m] == 1 && isNeighbor[j][m] == 1
							&& m != i && m != j && i != j) {
						count++;
					}
				}
				link[i][j] = count;
			}
		}
	}

	public void makeEachCluster() {

		for (int i = 0; i < numOfProcess; i++) {
			belongedCluster[i] = i; // ���� Ŭ������
			includingProcess[i] = 1;
		}
	}

	/*
	 * private int countLinkOfTwoClusters(int cluster1, int cluster2) {
	 * 
	 * int count = 0; int cluster1Size = 0; int cluster2Size = 0;
	 * 
	 * for(int i=0; i<numOfProcess; i++) { if(belongedCluster[i]==cluster1) {
	 * cluster1Size++;
	 * 
	 * cluster2Size = 0; for(int j=0; j<numOfProcess; j++) {
	 * if(belongedCluster[j]==cluster2) { cluster2Size++;
	 * 
	 * count += link[i][j]; } }
	 * 
	 * } }
	 * 
	 * includingProcess[cluster1] = cluster1Size; includingProcess[cluster2] =
	 * cluster2Size;
	 * 
	 * return count; }
	 */

	public void calculateGoodness() {

		for (int i = 0; i < numOfProcess; i++) {
			for (int j = 0; j < numOfProcess; j++) {

				if (i == j) {
					goodness[i][j] = 0;
					continue;
				}

				int iSize = includingProcess[i];
				int jSize = includingProcess[j];

				goodness[i][j] = link[i][j]
						/ (Math.pow(iSize + jSize, 1 + 2 * indexOnEstimation)
								- Math.pow(iSize, 1 + 2 * indexOnEstimation) - Math
								.pow(jSize, 1 + 2 * indexOnEstimation));
			}
		}
	}

	public void makeLocalHeap() {

		for (int i = 0; i < numOfProcess; i++) {

			int qSize = 0;
			for (int j = 0; j < numOfProcess; j++) {

				if (i != j) {
					insertLocalHeap(i, qSize, goodness[i][j], j);
					qSize++;
				}
			}
			heapSize[i] = qSize; // (numOfProcess -1)
		}

	}

	public void makeGlobalHeap() {

		int qSize = 0;

		for (int i = 0; i < numOfProcess; i++) {

			insertGlobalHeap(qSize, q[i][0], i, q1[i][0]); // (golbalQSize,
			// goodness, process
			// i, process j)
			qSize++;
		}

	}

	public void RockAlgorithm() {

		findNeighbor(); // ���缺�� ��Ÿ���� ū ���� Neighbor �̴�.
		// Simulation.printArrayInt("isNeighbor", isNeighbor[0]);

		countLink(); // �� ���μ����� ����� Neighbor ������ Link�̴�.
		makeEachCluster(); // ó������ ������ ������ Ŭ�������̴�.
		calculateGoodness(); // Goodness�� ����Ѵ�.

		makeLocalHeap();
		makeGlobalHeap();

		for (int i = 0; numOfCluster > K; i++) {
			mergeCluster();
		}
	}

	public void mergeCluster() {

		int u = Q1[0];
		int v = Q2[0];

		// u<v�� �����..
		if (u > v) {
			int k = u;
			u = v;
			v = k;
		}

		for (int j = 0; j < numOfProcess; j++) {
			if (belongedCluster[j] == v) {
				belongedCluster[j] = u;
			}
		}
		numOfCluster--;
		System.out.println("Cluster " + v + "->" + u + " : " + Q[0]);
		System.out.println("Cluster " + numOfCluster);

		// �� ��ũ�� ���Ͽ� ���� �μ��� ��ũ�� ���ִ´�.
		for (int i = 0; i < numOfProcess; i++) {

			link[i][u] = link[i][u] + link[i][v];
			link[u][i] = link[u][i] + link[v][i];
			link[i][i] = 0;
		}

		includingProcess[u] = includingProcess[u] + includingProcess[v];
		includingProcess[v] = 0;

		// ������ ���μ����� ��õ� goodness�� local heap�鿡�� �����Ѵ�.
		for (int i = 0; i < numOfProcess; i++) {

			if (i == u) { // u�� ���������� ���� ��, makeLocalHeap()�Լ��� ����.
				int qSize = 0;
				for (int j = 0; j < numOfProcess; j++) { // u�� �ٸ� ���
					// ���μ����鿡 ���Ͽ�
					// �ٽ� ���� ������
					// ��

					if (j != i && j != v) {
						int n1 = includingProcess[i];
						int n2 = includingProcess[j];
						if (n1 * n2 == 0) {
							goodness[i][j] = 0.0;
						} else {
							goodness[i][j] = link[i][j]
									/ (Math.pow(n1 + n2,
											1 + 2 * indexOnEstimation)
											- Math.pow(n1,
													1 + 2 * indexOnEstimation) - Math
											.pow(n2, 1 + 2 * indexOnEstimation));
						}

						insertLocalHeap(i, qSize, goodness[i][j], j);
						qSize++;
					}
				}
				heapSize[i] = qSize;
			} else { // �ٸ� �ѵ��� ���������� ��� ����.
				for (int j = 0; j < heapSize[i]; j++) { // ���� ��������
					// ���������� �����͵���
					// ��� �f���.

					if (q1[i][j] == v) { // v�� ��õ� �����Ͱ� ������, ����
						deleteLocalHeap(i, heapSize[i], j); // deleteLocalHeap(int
						// i /*qNumber*/,
						// int arraySize,
						// int del /*deleted
						// Index*/)
						heapSize[i]--;
					}
				}
				for (int j = 0; j < heapSize[i]; j++) { // ���� ��������
					// ���������� �����͵���
					// ��� �f���.

					if (q1[i][j] == u) { // u�� ��õ� �����Ͱ� ������, ������Ʈ
						int n1 = includingProcess[i];
						int n2 = includingProcess[u]; // �̹� ������ ������
						if (n1 * n2 == 0) {
							goodness[i][u] = 0.0;
						} else {
							goodness[i][u] = link[i][u]
									/ (Math.pow(n1 + n2,
											1 + 2 * indexOnEstimation)
											- Math.pow(n1,
													1 + 2 * indexOnEstimation) - Math
											.pow(n2, 1 + 2 * indexOnEstimation));
						}

						deleteLocalHeap(i, heapSize[i], j); // deleteLocalHeap(int
						// i /*qNumber*/,
						// int arraySize,
						// int del /*deleted
						// Index*/)
						heapSize[i]--;
						insertLocalHeap(i, heapSize[i], goodness[i][j], j);
						heapSize[i]++;
					}
				}
			}
		}
		makeGlobalHeap();

	}

	// ///////////////////////////////
	// �������� ó�� �Լ�
	// ///////////////////////////////
	// ���μ��� i�� ���� ������ �ڷ� �߰�
	private void insertLocalHeap(int i, int arraySize, double data, int j) {

		// if(arraySize == q[i].length)
		// {System.out.println("Local Heap is Full~~~~"); System.exit(0);}
		int k = 0;
		for (k = arraySize; true;) {
			if (k == 0) {
				break;
			}
			if (data <= q[i][(k + 1) / 2 - 1]) {
				break;
			}
			q[i][k] = q[i][(k + 1) / 2 - 1];
			q1[i][k] = q1[i][(k + 1) / 2 - 1];
			k = (k + 1) / 2 - 1;
		}
		// arraySize++;

		q[i][k] = data;
		q1[i][k] = j;
	}

	// �۷ι� ���� �ڷᱸ�� �߰�
	private void insertGlobalHeap(int arraySize, double data, int i, int j) {
		// if(arraySize == Q.length)
		// {System.out.println("Global Heap is Full~~~~"); System.exit(0);}
		int k = 0;
		for (k = arraySize; true;) {
			if (k == 0) {
				break;
			}
			if (data <= Q[(k + 1) / 2 - 1]) {
				break;
			}
			Q[k] = Q[(k + 1) / 2 - 1];
			Q1[k] = Q1[(k + 1) / 2 - 1];
			Q2[k] = Q2[(k + 1) / 2 - 1];
			k = (k + 1) / 2 - 1;
		}
		// arraySize++;

		Q[k] = data;
		Q1[k] = i;
		Q2[k] = j;
	}

	// ���� �ڷᱸ�� ����
	private void deleteLocalHeap(int i /* qNumber */, int arraySize, int del /*
																			 * deleted
																			 * Index
																			 */) {
		if (del >= arraySize) {
			System.out.println("Heap is too small~~~~" + del + "~" + arraySize);
			System.exit(0);
		}
		int k = del;
		double data = q[i][arraySize - 1];
		int data1 = q1[i][arraySize - 1];

		for (int j = (del + 1) * 2 - 1; j < arraySize;) {
			if (j < arraySize - 1) {
				if (q[i][j] < q[i][j + 1]) {
					j++;
				}
			}

			if (data >= q[i][j]) {
				break;
			}
			q[i][k] = q[i][j];
			q1[k] = q1[j];

			k = j;
			j = (j + 1) * 2 - 1;
		}

		q[i][k] = data;
		q1[i][k] = data1;
		q[i][arraySize - 1] = 0.0;
		q1[i][arraySize - 1] = 0;

		// return q[i];
	}

	// ���� �ڷᱸ�� ����
	private void deleteMaxGlobalHeap(int arraySize) {
		if (arraySize == 0) {
			System.out.println("Heap is Empty~~~~");
			System.exit(0);
		}
		int k = 0;
		double data = Q[arraySize - 1];
		int data1 = Q1[arraySize - 1];
		int data2 = Q2[arraySize - 1];

		for (int j = 1; j < arraySize;) {
			if (j < arraySize - 1) {
				if (Q[j] < Q[j + 1]) {
					j++;
				}
			}

			if (data >= Q[j]) {
				break;
			}
			Q[k] = Q[j];
			Q1[k] = Q1[j];
			Q2[k] = Q2[j];

			k = j;
			j = (j + 1) * 2 - 1;
		}

		Q[k] = data;
		Q1[k] = data1;
		Q2[k] = data2;
		Q[arraySize - 1] = 0.0;
		Q[arraySize - 1] = 0;
		Q[arraySize - 1] = 0;

		// return Q;
	}

}
