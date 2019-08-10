package org.processmining.analysis.logclustering.profiles;

import java.util.Arrays;

import org.processmining.analysis.logclustering.distancemeasure.DistanceMeasure;
import org.processmining.framework.log.AuditTrailEntry;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import java.util.HashSet;

/**
 * Abstract class defining the interface for a log profile that contains profile
 * of instances in a log
 * 
 * @author Minseok Song (m.s.song@tue.nl)
 */
public abstract class VectorProfile extends Profile {

	/**
	 * ; The item of the profile (e.g. task name, originator name, etc.)
	 */
	protected String[] profileItem;

	/**
	 * ; The number of profiles (e.g. number of tasks, number of originators,
	 * etc.)
	 */
	protected int profileSize;

	/**
	 * Matrix holding the actual profile as derived by the specific profile
	 * implementation.
	 */
	protected DoubleMatrix2D profile;

	/**
	 * Initializes the data structures of a profile.
	 * 
	 * @param size
	 *            Number of traces to be compared
	 * @param size
	 *            Profile (must be greater than zero).
	 */
	protected VectorProfile(String aName, String aDescription, int traceSize,
			String[] profileItem) {
		super(aName, aDescription, traceSize);
		this.profileSize = profileItem.length;
		this.profileItem = profileItem;
		this.profile = DoubleFactory2D.sparse.make(traceSize, profileSize, 0.0);
	}

	protected abstract void buildProfile(int traceIndex, AuditTrailEntry ate);

	protected void normalize(double normalizationMaximum) {
		double currentMax = 0.0;
		double currentValue;
		// determine maximum value in distance matrix first
		for (int x = 0; x < profile.columns(); x++) {
			for (int y = 0; y < profile.rows(); y++) {
				currentValue = profile.get(x, y);
				if (currentValue > currentMax) {
					currentMax = currentValue;
				}
			}
		}
		// calculate normalization factor
		double factor = normalizationMaximum / currentMax;
		// normalize values by multiplication with
		// the normalization factor.
		for (int x = 0; x < profile.columns(); x++) {
			for (int y = 0; y < profile.rows(); y++) {
				profile.set(x, y, profile.get(x, y) * factor);
			}
		}
	}

	protected DoubleMatrix2D normalizeDistance(DoubleMatrix2D distance) {
		double currentMax = 0.0;
		double currentValue;
		// determine maximum value in distance matrix first
		for (int x = 0; x < distance.rows(); x++) {
			for (int y = 0; y < distance.columns(); y++) {
				currentValue = distance.get(x, y);
				if (currentValue > currentMax) {
					currentMax = currentValue;
				}
			}
		}
		// calculate normalization factor
		double factor = normalizationMaximum / currentMax;
		// normalize values by multiplication with
		// the normalization factor.
		for (int x = 0; x < distance.rows(); x++) {
			for (int y = 0; y < distance.columns(); y++) {
				distance.set(x, y, distance.get(x, y) * factor);
			}
		}
		return distance;
	}

	public DoubleMatrix2D calculateDistance(DistanceMeasure dm) {
		return this.normalizeDistance(dm.calculateDistance(profile));
	}

	public DoubleMatrix2D calculateDistance(DistanceMeasure dm,
			DoubleMatrix2D center) {
		return this.normalizeDistance(dm.calculateDistance(profile, center));
	}

	/**
	 * return random points (each value is in between min and max of a column)
	 */
	public DoubleMatrix1D generateRandomPoint() {
		DoubleMatrix1D point = DoubleFactory1D.dense.make(profileSize);

		// initialize minimum and maximum
		double min[], max[];
		min = new double[profileSize];
		max = new double[profileSize];

		for (int i = 0; i < profileSize; i++) {
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}

		for (int i = 0; i < traceSize; i++) {
			for (int j = 0; j < profileSize; j++) {
				if (min[j] > profile.get(i, j))
					min[j] = profile.get(i, j);
				if (max[j] < profile.get(i, j))
					min[j] = profile.get(i, j);
			}
		}

		for (int i = 0; i < profileSize; i++) {
			point.set(i, Math.random() * (max[i] - min[i]) + min[i]);
		}

		return point;
	}

	public DoubleMatrix1D calcuateCenter(HashSet<Integer> traceSet) {
		DoubleMatrix1D center = DoubleFactory1D.dense.make(profileSize);

		for (int i = 0; i < traceSize; i++) {
			if (traceSet.contains((Integer) i))
				for (int j = 0; j < profileSize; j++) {
					center.set(j, center.get(j) + profile.get(i, j));
				}
		}

		for (int i = 0; i < profileSize; i++) {
			center.set(i, center.get(i) / traceSet.size());
		}

		return center;
	}

	/**
	 * This method returns the number of columns.
	 */
	public int getProfileSize() {
		return profileSize;
	}

	/**
	 * Helper method; increases the counter for a given item (class), trace, by
	 * a defined increment.
	 * 
	 * @param item
	 * @param traceIndex
	 * @param increment
	 */
	protected void increaseItemBy(String item, int traceIndex, double increment) {
		int column = Arrays.binarySearch(profileItem, item);
		profile.set(traceIndex, column, profile.get(traceIndex, column)
				+ increment);
	}
}
