package org.processmining.analysis.socialsuccess.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Implements the k-means algorithm, based on the algorithm by Manas Somaiya
 * 
 * @author Manas Somaiya mhs@cise.ufl.edu
 * @author Martin van Wingerden martinvw@mtin.nl
 */
public class KMeans1D extends KMeansnD {
	protected static final int RANDOM = 0;
	protected static final int PICK_VALUES = 1;
	protected static final int USE_METHOD = PICK_VALUES;

	private TreeSet<Double> uniqueValues;

	/**
	 * Returns a new instance of kMeans algorithm
	 * 
	 * @param k
	 *            number of clusters
	 * @param kMeansPoints
	 *            List containing objects of type kMeansPoint
	 */
	public KMeans1D(int _k, HashMap<String, Double> scores) {
		try {
			this.nIterations = 0;
			this.kMeansPoints = new ArrayList<kMeansPointnD>();
			this.uniqueValues = new TreeSet<Double>();
			Iterator<String> users = scores.keySet().iterator();
			while (users.hasNext()) {
				String user = users.next();
				Vector<Double> s = new Vector<Double>();
				s.add(0, scores.get(user));
				this.kMeansPoints.add(new kMeansPoint1D(user, s));
				this.uniqueValues.add(scores.get(user));
			}
			this.k = Math.min(_k, this.uniqueValues.size());
			this.clusters = new Cluster[this.k];
		} catch (InvalidDimensionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // end of kMeans()

	/**
	 * Get the clusters
	 */
	public ArrayList<HashMap<String, Double>> getClusters() {
		ArrayList<HashMap<String, Double>> returnClusters = new ArrayList<HashMap<String, Double>>(
				this.k);

		if (!isFinished)
			runKMeans();

		TreeMap<Double, Integer> t = new TreeMap<Double, Integer>();
		for (int i = 0; i < this.k; i++) {
			if (this.clusters[i].getSize() > 0)
				t.put(this.clusters[i].getMean().getValue().get(0), i);
			returnClusters.add(i, new HashMap<String, Double>());
		}

		HashMap<Integer, Integer> mapClusterToCluster = new HashMap<Integer, Integer>();
		Iterator<Double> means = t.keySet().iterator();
		int n = 0;
		while (means.hasNext()) {
			Double mean = means.next();
			mapClusterToCluster.put(t.get(mean), n);
			n++;
		}

		Iterator<kMeansPointnD> it = kMeansPoints.iterator();
		while (it.hasNext()) {
			kMeansPointnD k = it.next();
			if (!k.getUser().equals(MEAN_POINT)) {
				int c = mapClusterToCluster.get(k.getClusterNumber());
				returnClusters.get(c).put(k.getUser(), k.getValue().get(0));
			}
		}

		return returnClusters;
	}

	/**
	 * initialise the clusters
	 * 
	 * @throws InvalidDimensionsException
	 */
	protected void initialise() throws InvalidDimensionsException {
		if (USE_METHOD == RANDOM) {
			initializeRandom();
		} else if (USE_METHOD == PICK_VALUES) {
			initializePickValues();
		}
	}

	/**
	 * Pick the values
	 * 
	 * @throws InvalidDimensionsException
	 */
	private void initializePickValues() throws InvalidDimensionsException {

		Object[] temp = uniqueValues.toArray();
		for (int i = 0; i < k; i++) {
			this.clusters[i] = new Cluster(i);
			// set the mean to a good value
			int index = (int) Math.floor(((double) i)
					* (((double) uniqueValues.size() - 1) / (this.k - 1)));
			Vector<Double> t = new Vector<Double>();
			t.add(0, (Double) temp[index]);
			this.clusters[i].setMean(new kMeansPoint1D(MEAN_POINT, t));
		}
		// we hebben de unieke waarden niet meer nodig
		uniqueValues.clear();
	}

	/**
	 * Initialize the points in a random way
	 * 
	 * @throws InvalidDimensionsException
	 */
	private void initializeRandom() throws InvalidDimensionsException {
		// Select k points as initial means
		for (int i = 0; i < k; i++) {
			this.clusters[i] = new Cluster(i);
			int index = (int) (Math.random() * this.kMeansPoints.size());
			Vector<Double> t = new Vector<Double>();
			t.add(0, this.kMeansPoints.get(index).getValue().get(0));
			this.clusters[i].setMean(new kMeansPoint1D(MEAN_POINT, t));
		}
	}

	@Override
	protected int getDimension() {
		return 1;
	}

	@Override
	protected int resetK(int _k) {
		this.k = Math.min(_k, this.uniqueValues.size());
		this.clusters = new Cluster[this.k];
		return this.k;
	}
}

class kMeansPoint1D extends kMeansPointnD {
	/** Stores the value of this point */

	protected int requiredDimensions = 1;

	/* Construct a kMeansPoint */
	public kMeansPoint1D(String _user, Vector<Double> _value)
			throws InvalidDimensionsException {
		super(_user, 1);
		if (checkDimensions(_value))
			this.value = _value;
		else
			throw new InvalidDimensionsException(requiredDimensions);
	}

	private boolean checkDimensions(Vector<Double> _value) {
		return (_value.size() == requiredDimensions);
	}

	public kMeansPoint1D(String _user) {
		super(_user, 1);
	}

	/* Does the point p equal this point */
	public boolean equals(kMeansPointnD _p) {
		if (_p.getClass().getName().equals(this.getClass().getName())) {
			kMeansPoint1D p = (kMeansPoint1D) _p;
			return (p.getValue() == this.getValue());
		} else {
			return false;
		}
	}
}
