/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.analysis.logmetrics;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LogMetrics {

	public static String getMetricsString(LogReader log) {
		LogMetrics metrics = new LogMetrics(log);
		return metrics.formatMetrics();
	}

	protected int magnitude;
	protected int support;
	protected int[] traceVariety;
	protected int minTraceVariety;
	protected int maxTraceVariety;
	protected int logVariety;
	protected double levelOfDetail;
	protected long[] traceTimeGranularity;
	protected long minTraceTimeGranularity;
	protected long maxTraceTimeGranularity;
	protected double[] traceStructure;
	protected double minTraceStructure;
	protected double maxTraceStructure;
	protected double logStructure;
	protected FollowRelations[] traceBinRelations;
	protected FollowRelations logBinRelations;
	protected double[][] traceAffinity;
	protected double minTraceAffinity;
	protected double maxTraceAffinity;
	protected double logAffinity;

	protected LogReader log;

	public LogMetrics(LogReader log) {
		this.log = log;
		try {
			scanMetrics(log);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String formatMetrics() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nMetrics for log " + log.getFile().getShortName() + "\n\n");
		sb.append("Magnitude: " + magnitude + "\n\n");
		sb.append("Support: " + support + "\n\n");
		sb.append("Variety: " + logVariety + "\n");
		sb.append("-- min: " + minTraceVariety + "\n");
		sb.append("-- max: " + maxTraceVariety + "\n\n");
		sb.append("Level Of Detail: " + levelOfDetail + "\n\n");
		sb.append("Time Granularity:\n");
		sb.append("-- min: " + minTraceTimeGranularity
				+ " (log time granularity)\n");
		sb.append("-- max: " + maxTraceTimeGranularity + "\n\n");
		sb.append("Structure: " + logStructure + "\n");
		sb.append("-- min: " + minTraceStructure + "\n");
		sb.append("-- max: " + maxTraceStructure + "\n\n");
		sb.append("Affinity: " + logAffinity + "\n");
		sb.append("-- min: " + minTraceAffinity + "\n");
		sb.append("-- max: " + maxTraceAffinity + "\n\n");
		return sb.toString();
	}

	protected void scanMetrics(LogReader log) throws IOException {
		magnitude = 0;
		support = 0;
		traceVariety = new int[log.numberOfInstances()];
		minTraceVariety = Integer.MAX_VALUE;
		maxTraceVariety = Integer.MIN_VALUE;
		logVariety = log.getLogSummary().getLogEvents().size();
		levelOfDetail = 0.0;
		traceTimeGranularity = new long[log.numberOfInstances()];
		minTraceTimeGranularity = Long.MAX_VALUE;
		maxTraceTimeGranularity = Long.MIN_VALUE;
		LogEvents logEvents = log.getLogSummary().getLogEvents();
		logBinRelations = new FollowRelations();
		traceBinRelations = new FollowRelations[log.numberOfInstances()];
		traceStructure = new double[log.numberOfInstances()];
		minTraceStructure = Double.MAX_VALUE;
		maxTraceStructure = Double.MIN_VALUE;
		for (int i = 0; i < log.numberOfInstances(); i++) {
			HashSet<LogEvent> traceEventClasses = new HashSet<LogEvent>();
			traceBinRelations[i] = new FollowRelations();
			support++;
			long lastTimestamp = -1;
			long minGranularity = Long.MAX_VALUE;
			AuditTrailEntryList ateList = log.getInstance(i)
					.getAuditTrailEntryList();
			AuditTrailEntry ate = null, lastAte = null;
			for (int e = 0; e < ateList.size(); e++) {
				magnitude++;
				ate = ateList.get(e);
				traceEventClasses.add(logEvents.findLogEvent(ate.getElement(),
						ate.getType()));
				if (ate.getTimestamp() != null && traceTimeGranularity != null) {
					long timestamp = ate.getTimestamp().getTime();
					if (lastTimestamp >= 0) {
						long granularity = timestamp - lastTimestamp;
						if (granularity < minGranularity) {
							minGranularity = granularity;
						}
					}
					lastTimestamp = timestamp;
				} else {
					traceTimeGranularity = null;
				}
				if (lastAte != null) {
					traceBinRelations[i].register(lastAte, ate);
					logBinRelations.register(lastAte, ate);
				}
				lastAte = ate;
			}
			traceVariety[i] = traceEventClasses.size();
			if (traceVariety[i] > maxTraceVariety) {
				maxTraceVariety = traceVariety[i];
			}
			if (traceVariety[i] < minTraceVariety) {
				minTraceVariety = traceVariety[i];
			}
			levelOfDetail += traceEventClasses.size();
			if (traceTimeGranularity != null) {
				traceTimeGranularity[i] = minGranularity;
				if (traceTimeGranularity[i] > maxTraceTimeGranularity) {
					maxTraceTimeGranularity = traceTimeGranularity[i];
				}
				if (traceTimeGranularity[i] < minTraceTimeGranularity) {
					minTraceTimeGranularity = traceTimeGranularity[i];
				}
			}
			traceStructure[i] = 1.0 - (traceBinRelations[i].size() / (double) (traceEventClasses
					.size() * traceEventClasses.size()));
			if (traceStructure[i] > maxTraceStructure) {
				maxTraceStructure = traceStructure[i];
			}
			if (traceStructure[i] < minTraceStructure) {
				minTraceStructure = traceStructure[i];
			}
		}
		levelOfDetail /= log.numberOfInstances();
		logStructure = 1.0 - (logBinRelations.size() / (double) (logEvents
				.size() * logEvents.size()));
		// calculate affinities
		logAffinity = 0.0;
		traceAffinity = new double[log.numberOfInstances()][log
				.numberOfInstances()];
		minTraceAffinity = Double.MAX_VALUE;
		maxTraceAffinity = Double.MIN_VALUE;
		for (int x = 0; x < log.numberOfInstances(); x++) {
			for (int y = x + 1; y < log.numberOfInstances(); y++) {
				double affinity = calculateAffinity(x, y);
				traceAffinity[x][y] = affinity;
				traceAffinity[y][x] = affinity;
				logAffinity += affinity;
				logAffinity += affinity; // 2 x, because we save one comparison
				// each!
				if (affinity > maxTraceAffinity) {
					maxTraceAffinity = affinity;
				}
				if (affinity < minTraceAffinity) {
					minTraceAffinity = affinity;
				}
			}
		}
		logAffinity /= (double) (log.numberOfInstances() * (log
				.numberOfInstances() - 1));
	}

	protected int cardinality(double[] array) {
		int cardinality = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != 0) {
				cardinality++;
			}
		}
		return cardinality;
	}

	protected int cardinality(double[][] array) {
		int cardinality = 0;
		for (int i = 0; i < array.length; i++) {
			cardinality += cardinality(array[i]);
		}
		return cardinality;
	}

	protected int cardinality(double[][][] array) {
		int cardinality = 0;
		for (int i = 0; i < array.length; i++) {
			cardinality += cardinality(array[i]);
		}
		return cardinality;
	}

	protected double calculateAffinity(int traceIndexA, int traceIndexB) {
		double union = traceBinRelations[traceIndexA]
				.union(traceBinRelations[traceIndexB]);
		double overlap = traceBinRelations[traceIndexA]
				.overlap(traceBinRelations[traceIndexB]);
		if (union == 0) {
			return 0;
		} else {
			return overlap / union;
		}
	}

}
