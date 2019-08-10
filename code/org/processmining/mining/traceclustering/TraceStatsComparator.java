/**
 * 
 */
package org.processmining.mining.traceclustering;

import java.util.Comparator;

import org.processmining.mining.traceclustering.TraceStats.SingleTraceStat;

/**
 * @author christian
 * 
 */
public abstract class TraceStatsComparator {

	protected TraceStats stats;
	// protected DoubleMatrix2D relations;
	protected double[][] relations;

	public TraceStatsComparator(TraceStats traceStats) {
		stats = traceStats;
		int size = stats.size();
		// relations = DoubleFactory2D.dense.make(size, size, 0.0);
		relations = new double[size][size];
		double max = Double.MIN_VALUE;
		double val;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				val = compare(x, y);
				// relations.set(x, y, val);
				relations[x][y] = val;
				if (val > max) {
					max = val;
				}
			}
		}
		// normalize
		double normalizationFactor = 1.0 / max;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				// relations.set(x, y, (relations.get(x, y) *
				// normalizationFactor));
				relations[x][y] *= normalizationFactor;
			}
		}
	}

	public double getRelation(int x, int y) {
		// return relations.get(x, y);
		return relations[x][y];
	}

	protected abstract double compare(int x, int y);

	public TraceComparator getComparator(SingleTraceStat trace) {
		return new TraceComparator(this, trace);
	}

	protected class TraceComparator implements Comparator<SingleTraceStat> {

		protected SingleTraceStat reference;
		protected TraceStatsComparator comparator;

		public TraceComparator(TraceStatsComparator comp, SingleTraceStat ref) {
			comparator = comp;
			reference = ref;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(SingleTraceStat one, SingleTraceStat two) {
			double toOne = comparator.getRelation(reference.getTraceIndex(),
					one.getTraceIndex());
			double toTwo = comparator.getRelation(reference.getTraceIndex(),
					two.getTraceIndex());
			if (toOne == toTwo) {
				return 0;
			} else if (toOne > toTwo) {
				return 1;
			} else {
				return -1;
			}
		}

	}

}
