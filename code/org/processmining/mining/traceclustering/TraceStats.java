/**
 * 
 */
package org.processmining.mining.traceclustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author christian
 * 
 */
public class TraceStats {

	protected LogReader log;
	protected ArrayList<SingleTraceStat> stats;

	public TraceStats(LogReader logReader) {
		log = logReader;
		stats = new ArrayList<SingleTraceStat>(log.numberOfInstances());
		for (int i = 0; i < log.numberOfInstances(); i++) {
			SingleTraceStat sts = new SingleTraceStat(log.getInstance(i), i);
			stats.add(sts);
		}
	}

	public int size() {
		return stats.size();
	}

	public SingleTraceStat getStatsForTrace(int index) {
		return stats.get(index);
	}

	public List<SingleTraceStat> getTraceStats() {
		return new ArrayList<SingleTraceStat>(stats);
	}

	public class SingleTraceStat {

		protected int index;
		protected ProcessInstance trace;
		protected HashSet<LogEvent> events;
		protected DoubleMatrix2D followerMatrix;

		public SingleTraceStat(ProcessInstance pi, int index) {
			trace = pi;
			this.index = index;
			events = new HashSet<LogEvent>();
			int size = log.getLogSummary().getLogEvents().size();
			followerMatrix = DoubleFactory2D.sparse.make(size, size, 0.0);
			try {
				parseInstance();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public ProcessInstance getTrace() {
			return trace;
		}

		public int getTraceIndex() {
			return index;
		}

		public Set<LogEvent> getEvents() {
			return events;
		}

		public int getFollowerMatrixSize() {
			return followerMatrix.columns();
		}

		public double getFollowerRate(int sourceIndex, int targetIndex) {
			return followerMatrix.get(sourceIndex, targetIndex);
		}

		protected void parseInstance() throws IndexOutOfBoundsException,
				IOException {
			LogEvents logEvents = log.getLogSummary().getLogEvents();
			AuditTrailEntryList ateList = trace.getAuditTrailEntryList();
			AuditTrailEntry lastAte = ateList.get(0);
			int lastEvent = logEvents.findLogEventNumber(lastAte.getElement(),
					lastAte.getType());
			events.add(logEvents.get(lastEvent));
			AuditTrailEntry currentAte;
			int currentEvent;
			for (int i = 1; i < ateList.size(); i++) {
				currentAte = ateList.get(i);
				currentEvent = logEvents.findLogEventNumber(currentAte
						.getElement(), currentAte.getType());
				events.add(logEvents.get(currentEvent));
				followerMatrix.set(lastEvent, currentEvent, followerMatrix.get(
						lastEvent, currentEvent) + 1);
				lastAte = currentAte;
				lastEvent = currentEvent;
			}
		}

	}

}
