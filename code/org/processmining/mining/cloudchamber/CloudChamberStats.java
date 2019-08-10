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
package org.processmining.mining.cloudchamber;

import java.io.IOException;
import java.text.DecimalFormat;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;

/**
 * @author christian
 * 
 */
public class CloudChamberStats {

	protected static DecimalFormat decFormat = new DecimalFormat("##0.0## %");

	protected double[] occurrences;
	protected int[] indices;
	protected int[] instanceBoundaries;
	protected LogEvents events;

	public CloudChamberStats(LogReader log) {
		events = log.getLogSummary().getLogEvents();
		initializeData(log);
	}

	public double getValueAt(int x, int y) {
		if (indices[x] == indices[y]) {
			// matching event class; return relative frequency
			return occurrences[indices[x]];
		} else {
			// different events; empty field
			return 0.0;
		}
	}

	public String[] getDescriptionAt(int x, int y) {
		if (indices[x] == indices[y]) {
			// matching event class; return description
			LogEvent event = events.get(indices[x]);
			String result[] = new String[2];
			result[0] = event.getModelElementName() + " ("
					+ event.getEventType() + ")";
			result[1] = decFormat.format(occurrences[indices[x]]);
			return result;
		} else {
			// different events; empty field
			return null;
		}
	}

	public int[] getInstanceBoundaries() {
		return instanceBoundaries;
	}

	public int size() {
		return indices.length;
	}

	public void initializeData(LogReader log) {
		LogEvents events = log.getLogSummary().getLogEvents();
		indices = new int[getNumberOfAuditTrailEntries(log)];
		occurrences = new double[events.size()];
		instanceBoundaries = new int[log.numberOfInstances()];
		AuditTrailEntryList ateList = null;
		AuditTrailEntry ate = null;
		int index;
		int counter = 0;
		int max = 0;
		// elicitation loop
		for (int i = 0; i < log.numberOfInstances(); i++) {
			instanceBoundaries[i] = counter;
			ateList = log.getInstance(i).getAuditTrailEntryList();
			for (int k = 0; k < ateList.size(); k++) {
				try {
					ate = ateList.get(k);
					index = events.findLogEventNumber(ate.getElement(), ate
							.getType());
					occurrences[index]++;
					max = Math.max(max, (int) occurrences[index]);
					indices[counter] = index;
					counter++;
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		// normalization loop
		for (int i = 0; i < occurrences.length; i++) {
			occurrences[i] = (occurrences[i] / max);
		}
	}

	protected static int getNumberOfAuditTrailEntries(LogReader log) {
		int counter = 0;
		for (int i = log.numberOfInstances() - 1; i >= 0; i--) {
			counter += log.getInstance(i).getAuditTrailEntryList().size();
		}
		return counter;
	}

}
