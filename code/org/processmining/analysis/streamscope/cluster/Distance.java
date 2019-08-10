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
package org.processmining.analysis.streamscope.cluster;

import java.io.IOException;
import java.util.LinkedList;

import org.processmining.analysis.streamscope.EventClassTable;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Progress;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class Distance {

	protected static int MAX_LOOK_BACK = 5;
	protected static double ATTENUATION_FACTOR = 0.666666;

	public static Distance create(LogReader log, Progress progress)
			throws IOException {
		progress.setNote("Creating event class table...");
		progress.setProgress(0);
		EventClassTable ecTable = new EventClassTable(log, progress);
		double[][] distances = new double[ecTable.size()][ecTable.size()];
		double[][] frequencies = new double[ecTable.size()][ecTable.size()];
		// initialize
		for (int n = 0; n < ecTable.size(); n++) {
			for (int m = 0; m < ecTable.size(); m++) {
				distances[n][m] = 0.0;
				frequencies[n][m] = 0.0;
			}
		}
		progress.setNote("Measuring event class distances...");
		int ateCounter = 0;
		int lastProgress = -1;
		for (int i = 0; i < log.numberOfInstances(); i++) {
			AuditTrailEntryList ateList = log.getInstance(i)
					.getAuditTrailEntryList();
			LinkedList<Integer> lookBackIndices = new LinkedList<Integer>();
			LinkedList<AuditTrailEntry> lookBackEntries = new LinkedList<AuditTrailEntry>();
			for (int k = 1; k < ateList.size(); k++) {
				AuditTrailEntry current = ateList.get(k);
				int currentIndex = ecTable.getIndex(current.getElement());
				// look back
				double attenuationFactor = 1.0;
				/*
				 * for(int l=(k-1); l>(k-MAX_LOOK_BACK) && l>=0; l--) {
				 * AuditTrailEntry back = ateList.get(l); int backIndex =
				 * ecTable.getIndex(back.getElement()); double distance =
				 * staticDistance(back, current) * attenuationFactor;
				 * distances[currentIndex][backIndex] += distance;
				 * distances[backIndex][currentIndex] += distance;
				 * frequencies[currentIndex][backIndex] += attenuationFactor;
				 * frequencies[backIndex][currentIndex] += attenuationFactor;
				 * attenuationFactor *= ATTENUATION_FACTOR; }
				 */
				for (int m = 0; m < lookBackIndices.size(); m++) {
					int backIndex = lookBackIndices.get(m);
					AuditTrailEntry backEntry = lookBackEntries.get(m);
					double distance = staticDistance(backEntry, current)
							* attenuationFactor;
					distances[currentIndex][backIndex] += distance;
					distances[backIndex][currentIndex] += distance;
					frequencies[currentIndex][backIndex] += attenuationFactor;
					frequencies[backIndex][currentIndex] += attenuationFactor;
					attenuationFactor *= ATTENUATION_FACTOR;
				}
				// adjust sizes
				lookBackIndices.addFirst(currentIndex);
				lookBackEntries.addFirst(current);
				if (lookBackIndices.size() > MAX_LOOK_BACK) {
					lookBackIndices.removeLast();
					lookBackEntries.removeLast();
				}
				ateCounter++;
				if (ateCounter == 3000) {
					ateCounter = 0;
					progress.inc();
				}
			}
			// progress.inc();
		}
		/*
		 * // remove frequency factor for(int x=0; x<ecTable.size(); x++) {
		 * for(int y=0; y<ecTable.size(); y++) { distances[x][y] /=
		 * frequencies[x][y]; } }
		 */
		// normalization
		/*
		 * for(int i=0; i<ecTable.size(); i++) { for(int k=0; k<ecTable.size();
		 * k++) { distances[i][k] /= maxDistance; } }
		 */
		// here we go!
		return new Distance(ecTable, distances);
	}

	public static double staticDistance(AuditTrailEntry ateA,
			AuditTrailEntry ateB) {
		return 1.0;
	}

	public double[][] distances;
	public EventClassTable ecTable;

	protected Distance(EventClassTable ecTable, double[][] distances) {
		this.ecTable = ecTable;
		this.distances = distances;
	}

	public EventClassTable getEventClassTable() {
		return ecTable;
	}

	public double distance(String elementA, String elementB) {
		return distances[ecTable.getIndex(elementA)][ecTable.getIndex(elementB)];
	}

	public double distance(int x, int y) {
		return distances[x][y];
	}

	public double distanceSingleLinkage(Node nodeA, Node nodeB) {
		double minDistance = 0.0;
		for (int x : nodeA.getIndices()) {
			for (int y : nodeB.getIndices()) {
				if (distances[x][y] > minDistance) {
					minDistance = distances[x][y];
				}
			}
		}
		return minDistance;
	}

	public double distanceAverageLinkage(Node nodeA, Node nodeB) {
		double meanDistance = 0.0;
		int divisor = 0;
		for (int x : nodeA.getIndices()) {
			for (int y : nodeB.getIndices()) {
				meanDistance += distances[x][y];
				divisor++;
			}
		}
		return meanDistance / divisor;
	}

	public double distanceCompleteLinkage(Node nodeA, Node nodeB) {
		double maxDistance = Double.MAX_VALUE;
		for (int x : nodeA.getIndices()) {
			for (int y : nodeB.getIndices()) {
				if (maxDistance > distances[x][y]) {
					maxDistance = distances[x][y];
				}
			}
		}
		return maxDistance;
	}

}
