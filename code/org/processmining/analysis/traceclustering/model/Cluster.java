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
package org.processmining.analysis.traceclustering.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author christian
 * 
 */
public class Cluster {

	protected String name;
	protected LogReader log;
	protected List<Integer> traceIndices;

	public Cluster(LogReader aLog, String aName) {
		name = aName;
		log = aLog;
		traceIndices = new ArrayList<Integer>();
	}

	public Cluster(Cluster other) {
		name = other.name;
		log = other.log;
		traceIndices = new ArrayList<Integer>(other.traceIndices);
	}

	public Cluster(LogReader aLog, String aName, int[] aTraceIndices) {
		this(aLog, aName);
		for (int index : aTraceIndices) {
			traceIndices.add(index);
		}
	}

	public Cluster(LogReader aLog, String aName,
			Collection<Integer> aTraceIndices) {
		this(aLog, aName);
		traceIndices.addAll(aTraceIndices);
	}

	public void addTrace(int index) {
		if (traceIndices.contains(index) == false) {
			traceIndices.add(index);
		}
	}

	public int size() {
		return (traceIndices != null) ? traceIndices.size() : 0;
	}

	public void setName(String str) {
		name = str;
	}

	public String getName() {
		return name;
	}

	public boolean containsAll(Cluster other) {
		return (traceIndices.containsAll(other.traceIndices));
	}

	public List<Integer> getTraceIndices() {
		return traceIndices;
	}

	public Cluster mergeWith(Cluster other) {
		traceIndices.addAll(other.traceIndices);
		return this;
	}

	public ProvidedObject getProvidedObject() throws Exception {
		return new ProvidedObject(name, new Object[] { getFilteredLog() });
	}

	public LogReader getFilteredLog() throws Exception {
		int[] filter = new int[traceIndices.size()];
		int i = 0;
		for (int index : traceIndices) {
			filter[i] = index;
			i++;
		}
		return LogReaderFactory.createInstance(log, filter);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Cluster(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Cluster) {
			Cluster other = (Cluster) obj;
			return (traceIndices.containsAll(other.traceIndices) && (other.traceIndices
					.containsAll(traceIndices)));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

}
