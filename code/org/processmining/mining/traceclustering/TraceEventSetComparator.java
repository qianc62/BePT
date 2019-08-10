/**
 * 
 */
package org.processmining.mining.traceclustering;

import java.util.HashSet;
import java.util.Set;

import org.processmining.framework.log.LogEvent;

/**
 * @author christian
 * 
 */
public class TraceEventSetComparator extends TraceStatsComparator {

	/**
	 * @param traceStats
	 */
	public TraceEventSetComparator(TraceStats traceStats) {
		super(traceStats);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.traceclustering.TraceStatsComparator#compare
	 * (int, int)
	 */
	protected double compare(int x, int y) {
		Set<LogEvent> eventsX = stats.getStatsForTrace(x).getEvents();
		Set<LogEvent> eventsY = stats.getStatsForTrace(y).getEvents();
		HashSet<LogEvent> union = new HashSet<LogEvent>(eventsX);
		union.addAll(eventsY);
		HashSet<LogEvent> cut = new HashSet<LogEvent>(eventsX);
		cut.retainAll(eventsY);
		return 1.0 - ((double) cut.size() / (double) union.size());
	}

}
