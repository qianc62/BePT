/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.analysis.activityclustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.processmining.analysis.activityclustering.metrics.CorrelationMetric;
import org.processmining.analysis.activityclustering.model.Cluster;
import org.processmining.analysis.activityclustering.model.FootprintReference;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ClusterScanner {

	protected LogReader log;
	protected FootprintReference footprintReference;
	protected CorrelationMetric metric;
	protected int lookahead;
	protected double threshold;

	public ClusterScanner(LogReader log, CorrelationMetric metric,
			int lookahead, double threshold) {
		this.log = log;
		this.metric = metric;
		this.lookahead = lookahead;
		this.threshold = threshold;
		this.footprintReference = new FootprintReference(log);
	}

	public List<Cluster> scanInstance(int index)
			throws IndexOutOfBoundsException, IOException {
		AuditTrailEntryList ateList = log.getInstance(index)
				.getAuditTrailEntryList();
		// create and initialize best lookahead fellows map
		int bestLookaheadFellows[] = new int[ateList.size()];
		Arrays.fill(bestLookaheadFellows, -1);
		// scan fill best lookahead fellows map
		for (int i = 0; i < ateList.size() - 1; i++) {
			bestLookaheadFellows[i] = findBestLookaheadFellow(ateList, i);
		}
		// create and initialize cluster data structures
		Cluster clusterMap[] = new Cluster[ateList.size()];
		Arrays.fill(clusterMap, null);
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		// scan forward for all events in this process instance
		for (int i = 0; i < ateList.size(); i++) {
			scanForwardChained(i, index, bestLookaheadFellows, clusterMap,
					clusters);
		}
		return clusters;
	}

	protected void scanForwardChained(int init, int index,
			int[] bestLookaheadFellows, Cluster[] clusterMap,
			List<Cluster> clusters) throws IndexOutOfBoundsException,
			IOException {
		// create new cluster and add initial event
		Cluster cluster = new Cluster(log, index, footprintReference);
		cluster.addEvent(init);
		clusterMap[init] = cluster;
		// scan forward
		int current = init;
		while (bestLookaheadFellows[current] > 0) {
			current = bestLookaheadFellows[current];
			if (clusterMap[current] == null) {
				// not clustered yet, add to cluster
				cluster.addEvent(current);
				clusterMap[current] = cluster;
			} else {
				// already previously clustered:
				// roll back and transfer all previously clustered events
				// to the previously created cluster found now
				Cluster previous = clusterMap[current];
				previous.merge(cluster);
				for (int rollbackVictim : cluster.getEventIndices()) {
					clusterMap[rollbackVictim] = previous;
				}
				// return (rest of the chain has already been added to
				// previous cluster)
				return;
			}
		}
		// we have reached the end of the chain without discovering a previous
		// cluster underway, so we can safely add this cluster to the list
		clusters.add(cluster);
	}

	protected int findBestLookaheadFellow(AuditTrailEntryList ateList,
			int eventIndex) throws IndexOutOfBoundsException, IOException {
		AuditTrailEntry refAte = ateList.get(eventIndex);
		AuditTrailEntry compAte = null;
		int bestLookaheadFellow = -1;
		double bestCorrelation = 0.0;
		double correlation;
		for (int i = eventIndex + 1; (i < ateList.size() && i < (eventIndex + lookahead)); i++) {
			compAte = ateList.get(i);
			correlation = metric.measureCorrelation(refAte, compAte);
			if (correlation > threshold && correlation > bestCorrelation) {
				// highest correlation above threshold so far, set new
				// best lookahead fellow.
				bestLookaheadFellow = i;
				bestCorrelation = correlation;
			}
		}
		return bestLookaheadFellow;
	}

}
