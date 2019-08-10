package org.processmining.mining.traceclustering;

import org.processmining.framework.ui.Progress;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * Abstract class defining the interface for a metric to measure the distance
 * between traces, or process instances in a log.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 */
public abstract class TraceDistanceMetric {

	/**
	 * ; The number of traces whose distances are measured.
	 */
	protected int size;
	/**
	 * Matrix holding the actual distance measures as derived by the specific
	 * metric implementation.
	 */
	protected DoubleMatrix2D distances;

	/**
	 * Initializes the data structures of a metric.
	 * 
	 * @param size
	 *            Number of traces to be compared (must be greater than zero).
	 */
	protected TraceDistanceMetric(int size) {
		this.size = size;
		this.distances = DoubleFactory2D.sparse.make(size, size, 0.0);
	}

	/**
	 * This method is called by the framework, when the metric is supposed to
	 * derive its distance measures (e.g., from the log, or from another data
	 * source). The complete measurements (i.e., between all combinations of
	 * traces) should be derived in this method, such that later access is fast.
	 * 
	 * @param progress
	 *            This reference may be used to indicate progress feedback to
	 *            the framework (may be <code>null</code>).
	 */
	public abstract void measure(Progress progress);

	/**
	 * This method is called by clustering algorithms to determine the distance,
	 * as measured by this metric implementation, between two traces, or process
	 * instances. These traces are specified by their index, i.e. their
	 * occurrence rank in the log.
	 * 
	 * @param traceIndexA
	 *            Index of the first trace to be compared (comparison
	 *            reference).
	 * @param traceIndexB
	 *            Index of the second trace to be compared (comparison target).
	 * @return The distance between the two given traces, as a double within the
	 *         range <code>[0, 1]</code>.
	 */
	public abstract double getDistance(int traceIndexA, int traceIndexB);

	protected void normalize(double normalizationMaximum) {
		double currentMax = 0.0;
		double currentValue;
		// determine maximum value in distance matrix first
		for (int x = 0; x < distances.columns(); x++) {
			for (int y = 0; y < distances.rows(); y++) {
				currentValue = distances.get(x, y);
				if (currentValue > currentMax) {
					currentMax = currentValue;
				}
			}
		}
		// calculate normalization factor
		double factor = normalizationMaximum / currentMax;
		// normalize values by multiplication with
		// the normalization factor.
		for (int x = 0; x < distances.columns(); x++) {
			for (int y = 0; y < distances.rows(); y++) {
				distances.set(x, y, distances.get(x, y) * factor);
			}
		}
	}

}
