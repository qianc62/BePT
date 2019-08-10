package org.processmining.analysis.socialsuccess.clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * Implements the k-means algorithm, based on the algorithm by Manas Somaiya
 * 
 * @author Manas Somaiya mhs@cise.ufl.edu
 * @author Martin van Wingerden martinvw@mtin.nl
 */
abstract public class KMeansnD {
	/** not a user point but a mean point */
	protected static final String MEAN_POINT = "_reserved";
	protected static final String SUM_POINT = "_reserved";
	protected static final int MAX_ITERATIONS = 250;
	public static final int MAX_CLUSTERS = 50;

	/** Has the kMeans algorithm finished */
	protected boolean isFinished = false;

	/** Number of clusters */
	protected int k;

	/** Number of clusters */
	protected int realK;

	/** Array of clusters */
	protected Cluster[] clusters;

	/** Number of iterations */
	protected int nIterations;

	/** Vector of data points */
	protected ArrayList<kMeansPointnD> kMeansPoints;

	/**
	 * Runs the k-means algorithm over the data set
	 */
	final public void runKMeans() {

		try {
			initialise();

			do {
				// Form k clusters
				Iterator<kMeansPointnD> i = this.kMeansPoints.iterator();
				while (i.hasNext())
					this.assignToCluster(i.next());

				this.nIterations++;
			}
			// Repeat while centroids do not change
			while (this.updateMeans() && this.nIterations < MAX_ITERATIONS);

			// System.out.println("Clusters found after " + this.nIterations +
			// " iterations.");

			isFinished = true;
		} catch (InvalidDimensionsException e) {
			e.printStackTrace();
			System.out.println("Mismatch in dimensions!");
		}
	} // end of runKMeans()

	/**
	 * initialise the clusters
	 * 
	 * @throws InvalidDimensionsException
	 */
	abstract void initialise() throws InvalidDimensionsException;

	/**
	 * Assigns a data point to one of the k clusters based on its distance from
	 * the means of the clusters
	 * 
	 * @param dp
	 *            data point to be assigned
	 * @throws InvalidDimensionsException
	 */
	final protected void assignToCluster(kMeansPointnD dp)
			throws InvalidDimensionsException {
		int oldSize;
		double minDistance;
		int optimalCluster = dp.getClusterNumber();
		if (optimalCluster >= 0) {
			minDistance = dp.distance(this.clusters[optimalCluster].getMean());
			// Verlaag het aantal items in een cluster
			oldSize = clusters[optimalCluster].getSize();
			clusters[optimalCluster].setSize(oldSize - 1);
		} else {
			minDistance = Double.MAX_VALUE;
		}

		for (int i = 0; i < this.k; i++) {
			// if there is a more optimal cluster, save it
			if (dp.distance(this.clusters[i].getMean()) < minDistance) {
				minDistance = dp.distance(this.clusters[i].getMean());
				optimalCluster = i;
			}
		}

		// Verhoog het aantal items in een cluster
		oldSize = clusters[optimalCluster].getSize();
		clusters[optimalCluster].setSize(oldSize + 1);
		dp.assignToCluster(optimalCluster);
	} // end of assignToCluster

	/**
	 * Updates the means of all k clusters, and returns if they have changed or
	 * not
	 * 
	 * @return have the updated means of the clusters changed or not
	 */
	protected boolean updateMeans() {

		boolean reply = false;
		kMeansPointnD[] clusterSum = new kMeansPointnD[this.k];
		int[] clusterSize = new int[this.k];
		kMeansPointnD[] pastMeans = new kMeansPointnD[this.k];

		for (int i = 0; i < this.k; i++) {
			clusterSum[i] = new kMeansPointnD(SUM_POINT, getDimension());
			clusterSize[i] = 0;
			pastMeans[i] = this.clusters[i].getMean();
		}

		try {
			Iterator<kMeansPointnD> i = this.kMeansPoints.iterator();
			while (i.hasNext()) {
				kMeansPointnD dp = i.next();
				if (!dp.user.equals(MEAN_POINT)) {
					clusterSum[dp.getClusterNumber()].add(dp);
					clusterSize[dp.getClusterNumber()]++;
				}
			}

			realK = 0;
			for (int j = 0; j < this.k; j++) {
				if (clusterSize[j] != 0) {
					realK++; // count the real number of clusters
					// Divide the vector by the cluster size
					clusterSum[j].divide(clusterSize[j]);
					kMeansPointnD newMeanPoint = new kMeansPointnD(MEAN_POINT,
							clusterSum[j].getValue());
					newMeanPoint.assignToCluster(j);
					this.clusters[j].setMean(newMeanPoint);
					if (!pastMeans[j].equals(this.clusters[j].getMean()))
						reply = true;
				}
			}
		} catch (InvalidDimensionsException e) {
			System.out.println("Mismatch in dimensions!");
		}
		return reply;
	} // end of updateMeans()

	abstract protected int getDimension();

	final protected double getDistortion() throws InvalidDimensionsException {
		double sum = 0.;
		for (int i = 0; i < k; i++) {
			sum += clusters[i].getDistortion(kMeansPoints);
		}
		return sum;
	}

	final protected double getSchwarzCriterion()
			throws InvalidDimensionsException {
		// assuming that the model is normally distributed
		// Distortion + mk log R [waar: m=#dimensions; k=#Centers; R=#Records]
		int R = kMeansPoints.size();
		int m = getDimension();
		return R * Math.log(getDistortion() / R) + (m * k * Math.log(R));
	}

	final public int getOptimalNrOfClusters() {
		double minValue = Double.MAX_VALUE;
		int optK = 0;
		try {
			for (int i = 2; i < MAX_CLUSTERS; i++) {
				int r = this.resetK(i);
				if (r == i) {
					this.runKMeans();
					double schwCrit;
					schwCrit = this.getSchwarzCriterion();
					if (schwCrit < minValue) {
						minValue = schwCrit;
						optK = this.realK;
					}
				} else {
					// dataset is kleiner dan i
					break;
				}
			}
		} catch (InvalidDimensionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return optK;
	}

	abstract protected int resetK(int k);
}

/**
 * Based on the work of Manas Somaiya, but I use a 1D version
 * 
 * @author Manas Somaiya mhs@cise.ufl.edu
 * @author Martin van Wingerden martinvw@mtin.nl
 */
class Cluster {

	/** Cluster Number */
	protected int clusterNumber;

	/** Size */
	protected int size = 0;

	/** Mean data point of this cluster */
	protected kMeansPointnD mean = null;

	/**
	 * Returns a new instance of cluster
	 * 
	 * @param _clusterNumber
	 *            the cluster number of this cluster
	 */
	public Cluster(int _clusterNumber) {

		this.clusterNumber = _clusterNumber;

	} // end of cluster()

	/**
	 * Sets the mean data point of this cluster
	 * 
	 * @param meanDataPoint
	 *            the new mean data point for this cluster
	 */
	public void setMean(kMeansPointnD meanDataPoint) {

		this.mean = meanDataPoint;

	} // end of setMean()

	/**
	 * Returns the mean data point of this cluster
	 * 
	 * @return the mean data point of this cluster
	 */
	public kMeansPointnD getMean() {

		return this.mean;

	} // end of getMean()

	/**
	 * Returns the cluster number of this cluster
	 * 
	 * @return the cluster number of this cluster
	 */
	final public int getClusterNumber() {

		return this.clusterNumber;

	} // end of getClusterNumber()

	final public int getSize() {
		return size;
	}

	final public void setSize(int _size) {
		this.size = _size;
	}

	final public double getDistortion(ArrayList<kMeansPointnD> points)
			throws InvalidDimensionsException {
		double csum = 0.;
		Iterator<kMeansPointnD> it = points.iterator();
		while (it.hasNext()) {
			kMeansPointnD p = it.next();
			if (p.getClusterNumber() == this.clusterNumber) {
				csum += Math.pow(mean.distance(p), 2.);
			}
		}
		return csum;
	}
} // end of class

class kMeansPointnD {
	/** Stores the cluster to which this point belongs */
	protected int cluster;

	/** Stores the user who belongs to this point. */
	protected String user;

	protected Vector<Double> value;

	protected int requiredDimensions = 0;

	/* Construct a kMeansPoint */
	public kMeansPointnD(String _user, int dimensions) {
		this.cluster = -1;
		this.user = _user;
		this.value = new Vector<Double>(dimensions);
		for (int i = 0; i < dimensions; i++) {
			this.value.add(i, 0.);
		}
	}

	public kMeansPointnD(String _user, Vector<Double> _value) {
		this.cluster = -1;
		this.user = _user;
		this.value = _value;
	}

	/* return the user belonging to this point */
	final public String getUser() {
		return user;
	}

	/* return the value of this point */
	final public Vector<Double> getValue() {
		return value;
	}

	/* return the distance between this point and another point */
	public double distance(kMeansPointnD _p) throws InvalidDimensionsException {
		if (_p.getDimensions() == this.getDimensions()) {
			Vector<Double> v1 = this.getValue();
			Vector<Double> v2 = _p.getValue();
			Double sqDistance = 0.;
			Iterator<Double> it1 = v1.iterator();
			Iterator<Double> it2 = v2.iterator();
			while (it1.hasNext() && it2.hasNext()) {
				sqDistance += Math.pow((it1.next() - it2.next()), 2);
			}
			return Math.sqrt(sqDistance);
		} else {
			throw new InvalidDimensionsException(getDimensions());
		}
	}

	public void add(kMeansPointnD _p) throws InvalidDimensionsException {
		if (_p.getDimensions() == this.getDimensions()) {
			Vector<Double> newValue = _p.getValue();
			for (int i = 0; i < value.size(); i++) {
				if (newValue.get(i) != null && this.value.get(i) != null) {
					this.value.add(i, newValue.get(i) + this.value.remove(i));
				} else {
					throw new InvalidDimensionsException(getDimensions());
				}
			}
		} else {
			throw new InvalidDimensionsException(getDimensions());
		}
	}

	public void divide(int d) throws InvalidDimensionsException {
		for (int i = 0; i < value.size(); i++) {
			if (this.value.get(i) != null) {
				this.value.add(i, this.value.remove(i) / d);
			} else {
				throw new InvalidDimensionsException(getDimensions());
			}
		}
	}

	protected int getDimensions() {
		return value.size();
	}

	/* Assign to cluster */
	final public void assignToCluster(int newCluster) {
		this.cluster = newCluster;
	}

	/* Get the cluster number */
	final public int getClusterNumber() {
		return cluster;
	}

	/* Does the point p equal this point */
	public boolean equals(kMeansPointnD p) {
		return p.getValue().equals(this.value);
	}

}