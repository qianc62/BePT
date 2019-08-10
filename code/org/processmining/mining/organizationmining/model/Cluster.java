/*
 * Copyright (c) 2007 Minseok Song
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
package org.processmining.mining.organizationmining.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author Minseok
 * 
 */
public class Cluster {

	protected String name;
	protected LogReader log;
	protected List<String> originatorList;

	public Cluster(LogReader aLog, String aName) {
		name = aName;
		log = aLog;
		originatorList = new ArrayList<String>();
	}

	public Cluster(Cluster other) {
		name = other.name;
		log = other.log;
		originatorList = new ArrayList<String>(other.originatorList);
	}

	public Cluster(LogReader aLog, String aName, String[] aoriginatorList) {
		this(aLog, aName);
		for (String index : aoriginatorList) {
			originatorList.add(index);
		}
	}

	public Cluster(LogReader aLog, String aName,
			Collection<String> aoriginatorList) {
		this(aLog, aName);
		originatorList.addAll(aoriginatorList);
	}

	public void addOriginator(String index) {
		if (originatorList.contains(index) == false) {
			originatorList.add(index);
		}
	}

	public int size() {
		return (originatorList != null) ? originatorList.size() : 0;
	}

	public void setName(String str) {
		name = str;
	}

	public String getName() {
		return name;
	}

	public boolean containsAll(Cluster other) {
		return (originatorList.containsAll(other.originatorList));
	}

	public List<String> getOriginatorList() {
		return originatorList;
	}

	public Cluster mergeWith(Cluster other) {
		originatorList.addAll(other.originatorList);
		return this;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Cluster(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Cluster) {
			Cluster other = (Cluster) obj;
			return (originatorList.containsAll(other.originatorList) && (other.originatorList
					.containsAll(originatorList)));
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
