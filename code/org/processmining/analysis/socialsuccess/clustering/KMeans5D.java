package org.processmining.analysis.socialsuccess.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import org.processmining.analysis.socialsuccess.BigFive;

/**
 * Implements the k-means algorithm, based on the algorithm by Manas Somaiya
 * 
 * @author Manas Somaiya mhs@cise.ufl.edu
 * @author Martin van Wingerden martinvw@mtin.nl
 */
public class KMeans5D extends KMeansnD {

	private boolean normalizeScores = true;

	/**
	 * Returns a new instance of kMeans algorithm
	 * 
	 * @param k
	 *            number of clusters
	 * @param kMeansPoints
	 *            List containing objects of type kMeansPoint
	 */
	public KMeans5D(int _k, HashMap<String, Vector<Double>> scores) {
		try {
			this.nIterations = 0;
			this.kMeansPoints = new ArrayList<kMeansPointnD>();
			Iterator<String> users = scores.keySet().iterator();
			while (users.hasNext()) {
				String user = users.next();
				if (normalizeScores) {
					Vector<Double> nScores = new Vector<Double>();
					// bepaal maximum
					double max = 0;
					Iterator<Double> it = scores.get(user).iterator();
					while (it.hasNext()) {
						max = Math.max(max, it.next());
					}
					double multiplier = (BigFive.additionConst * 5.) / max;
					it = scores.get(user).iterator();
					// normalize the scores
					while (it.hasNext()) {
						nScores.add(it.next() * multiplier);
					}
					scores.put(user, nScores);
				}
				this.kMeansPoints
						.add(new kMeansPoint5D(user, scores.get(user)));
			}
			this.k = Math.min(_k, scores.size());
			this.clusters = new Cluster[this.k];
		} catch (InvalidDimensionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // end of kMeans()

	/**
	 * Get the clusters
	 */
	public ArrayList<TreeMap<String, Vector<Double>>> getClusters() {
		ArrayList<TreeMap<String, Vector<Double>>> returnClusters = new ArrayList<TreeMap<String, Vector<Double>>>(
				this.k);

		if (!isFinished)
			runKMeans();

		for (int i = 0; i < this.k; i++) {
			returnClusters.add(i, new TreeMap<String, Vector<Double>>());
			returnClusters.get(i).put(MEAN_POINT,
					clusters[i].getMean().getValue());
		}

		Iterator<kMeansPointnD> it = kMeansPoints.iterator();
		while (it.hasNext()) {
			kMeansPointnD k = it.next();
			int c = k.getClusterNumber();
			returnClusters.get(c).put(k.getUser(), k.getValue());
		}

		return returnClusters;
	}

	/**
	 * initialise the clusters
	 * 
	 * @throws InvalidDimensionsException
	 */
	protected void initialise() throws InvalidDimensionsException {
		// Select k points as initial means
		for (int i = 0; i < k; i++) {
			this.clusters[i] = new Cluster(i);
			int index = (int) (Math.random() * this.kMeansPoints.size());
			Vector<Double> t = new Vector<Double>();
			t = kMeansPoints.get(index).getValue();
			this.clusters[i].setMean(new kMeansPoint5D(MEAN_POINT, t));
		}
	}

	@Override
	protected int getDimension() {
		return 5;
	}

	@Override
	protected int resetK(int _k) {
		this.k = Math.min(_k, kMeansPoints.size());
		this.clusters = new Cluster[this.k];
		return this.k;
	}
}

class kMeansPoint5D extends kMeansPointnD {
	/** Stores the value of this point */

	protected int requiredDimensions = 5;

	/* Construct a kMeansPoint */
	public kMeansPoint5D(String _user, Vector<Double> _value)
			throws InvalidDimensionsException {
		super(_user, 5);
		if (checkDimensions(_value))
			this.value = _value;
		else
			throw new InvalidDimensionsException(requiredDimensions);
	}

	private boolean checkDimensions(Vector<Double> _value) {
		return (_value.size() == requiredDimensions);
	}

	public kMeansPoint5D(String _user) {
		super(_user, 5);
	}
}
