/**
 * 
 */
package org.processmining.mining.traceclustering;

import org.processmining.mining.traceclustering.TraceStats.SingleTraceStat;

/**
 * @author cgunther
 * 
 */
public class TraceFollowerMatrixComparator extends TraceStatsComparator {

	/**
	 * @param traceStats
	 */
	public TraceFollowerMatrixComparator(TraceStats traceStats) {
		super(traceStats);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.traceclustering.TraceStatsComparator#compare
	 * (int, int)
	 */
	protected double compare(int x, int y) {
		SingleTraceStat statX = stats.getStatsForTrace(x);
		SingleTraceStat statY = stats.getStatsForTrace(y);
		int eventsSize = statX.getFollowerMatrixSize();
		double aggregation = 0.0;
		double valX, valY, tmp;
		for (int a = 0; a < eventsSize; a++) {
			for (int b = 0; b < eventsSize; b++) {
				valX = statX.getFollowerRate(a, b);
				valY = statY.getFollowerRate(a, b);
				// tmp = Math.pow(valX - valY, 2);
				tmp = valX - valY;
				tmp = tmp * tmp;
				aggregation += tmp;
			}
		}
		return Math.pow(aggregation, 0.5);
	}

}
