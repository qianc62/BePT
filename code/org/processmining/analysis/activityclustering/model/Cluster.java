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
package org.processmining.analysis.activityclustering.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class Cluster implements Comparable<Cluster> {

	protected LogReader log;
	protected int processInstanceIndex;
	protected List<Integer> eventIndices;
	protected Footprint footprint;
	protected Date firstDate;
	protected Date lastDate;
	protected boolean isSorted;

	public Cluster(LogReader log, int instanceIndex,
			FootprintReference footprintReference) {
		this.log = log;
		this.processInstanceIndex = instanceIndex;
		this.eventIndices = new ArrayList<Integer>();
		this.footprint = new Footprint(footprintReference);
		this.isSorted = false;
		this.firstDate = null;
		this.lastDate = null;
	}

	protected void ensureSorted() {
		if (isSorted == false) {
			Collections.sort(eventIndices);
			isSorted = true;
		}
	}

	public Footprint footprint() {
		return footprint;
	}

	public int size() {
		return eventIndices.size();
	}

	public void addEvent(int index) throws IndexOutOfBoundsException,
			IOException {
		eventIndices.add(index);
		footprint.add(log.getInstance(processInstanceIndex)
				.getAuditTrailEntryList().get(index).getElement());
		isSorted = false;
	}

	public AuditTrailEntry getEvent(int index)
			throws IndexOutOfBoundsException, IOException {
		ensureSorted();
		return log.getInstance(processInstanceIndex).getAuditTrailEntryList()
				.get(eventIndices.get(index));
	}

	public List<Integer> getEventIndices() {
		ensureSorted();
		return eventIndices;
	}

	public List<AuditTrailEntry> getEvents() throws IndexOutOfBoundsException,
			IOException {
		ensureSorted();
		ArrayList<AuditTrailEntry> events = new ArrayList<AuditTrailEntry>();
		AuditTrailEntryList ateList = log.getInstance(processInstanceIndex)
				.getAuditTrailEntryList();
		for (int index : eventIndices) {
			events.add(ateList.get(index));
		}
		return events;
	}

	public Date getStart() throws IndexOutOfBoundsException, IOException {
		if (eventIndices.size() > 0) {
			ensureSorted();
			if (firstDate == null) {
				for (int i = 0; i < eventIndices.size(); i++) {
					Date ts = getEvent(i).getTimestamp();
					if (ts != null) {
						firstDate = ts;
						break;
					}
				}
			}
			return firstDate;
		} else {
			return null;
		}
	}

	public Date getEnd() throws IndexOutOfBoundsException, IOException {
		if (eventIndices.size() > 0) {
			ensureSorted();
			if (lastDate == null) {
				for (int i = eventIndices.size() - 1; i >= 0; i--) {
					Date ts = getEvent(i).getTimestamp();
					if (ts != null) {
						lastDate = ts;
						break;
					}
				}
			}
			return lastDate;
		} else {
			return null;
		}
	}

	public int getStartIndex() {
		if (eventIndices.isEmpty()) {
			return -1;
		} else {
			ensureSorted();
			return eventIndices.get(0);
		}
	}

	public int getEndIndex() {
		if (eventIndices.isEmpty()) {
			return -1;
		} else {
			ensureSorted();
			return eventIndices.get(eventIndices.size() - 1);
		}
	}

	public int getInstanceIndex() {
		return processInstanceIndex;
	}

	public Cluster merge(Cluster other) {
		eventIndices.addAll(other.eventIndices);
		footprint.merge(other.footprint);
		isSorted = false;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Cluster other) {
		if (this.processInstanceIndex != other.processInstanceIndex) {
			return this.processInstanceIndex - other.processInstanceIndex;
		} else {
			this.ensureSorted();
			other.ensureSorted();
			return this.eventIndices.get(0) - other.eventIndices.get(0);
		}

	}

}
