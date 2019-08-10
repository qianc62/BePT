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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;

/**
 * @author christian
 * 
 */
public class AteReference extends AuditTrailEntry {

	protected LogReader log;
	protected int processInstanceIndex;
	protected int eventIndex;

	public AteReference(LogReader log, int processInstanceIndex, int eventIndex) {
		this.log = log;
		this.processInstanceIndex = processInstanceIndex;
		this.eventIndex = eventIndex;
	}

	protected AuditTrailEntry dereference() {
		try {
			return log.getInstance(processInstanceIndex)
					.getAuditTrailEntryList().get(eventIndex);
		} catch (IOException e) {
			// not much to do here...
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#clone()
	 */
	@Override
	public Object clone() {
		return dereference().clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof AteReference) {
			AteReference other = (AteReference) o;
			return (log == other.log
					&& processInstanceIndex == other.processInstanceIndex && eventIndex == other.eventIndex);
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#getElementModelReferences
	 * ()
	 */
	@Override
	public List<String> getElementModelReferences() {
		return dereference().getElementModelReferences();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#getOriginator()
	 */
	@Override
	public String getOriginator() {
		return dereference().getOriginator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#getOriginatorModelReferences
	 * ()
	 */
	@Override
	public List<String> getOriginatorModelReferences() {
		return dereference().getOriginatorModelReferences();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#getTimestamp()
	 */
	@Override
	public Date getTimestamp() {
		return dereference().getTimestamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#getType()
	 */
	@Override
	public String getType() {
		return dereference().getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#getTypeModelReferences()
	 */
	@Override
	public List<String> getTypeModelReferences() {
		return dereference().getTypeModelReferences();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#hashCode()
	 */
	@Override
	public int hashCode() {
		return (processInstanceIndex * 100) + eventIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#id()
	 */
	@Override
	public long id() {
		return dereference().id();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#setElementModelReferences
	 * (java.util.List)
	 */
	@Override
	public void setElementModelReferences(List<String> modelReferences) {
		dereference().setElementModelReferences(modelReferences);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#setOriginator(java.lang
	 * .String)
	 */
	@Override
	public void setOriginator(String originator) {
		dereference().setOriginator(originator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#setOriginatorModelReferences
	 * (java.util.List)
	 */
	@Override
	public void setOriginatorModelReferences(List<String> modelReferences) {
		dereference().setOriginatorModelReferences(modelReferences);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#setTimestamp(java.util
	 * .Date)
	 */
	@Override
	public void setTimestamp(Date timestamp) {
		dereference().setTimestamp(timestamp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#setType(java.lang.String)
	 */
	@Override
	public void setType(String type) {
		dereference().setType(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#setTypeModelReferences
	 * (java.util.List)
	 */
	@Override
	public void setTypeModelReferences(List<String> modelReferences) {
		dereference().setTypeModelReferences(modelReferences);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getAttributes()
	 */
	@Override
	public Map<String, String> getAttributes() {
		return dereference().getAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getDataAttributes()
	 */
	@Override
	public DataSection getDataAttributes() {
		return dereference().getDataAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getDescription()
	 */
	@Override
	public String getDescription() {
		return dereference().getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getModelReferences()
	 */
	@Override
	public List<String> getModelReferences() {
		return dereference().getModelReferences();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getName()
	 */
	@Override
	public String getName() {
		return dereference().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#removeAttribute(java.lang.String
	 * )
	 */
	@Override
	public void removeAttribute(String key) {
		dereference().removeAttribute(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttribute(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setAttribute(String key, String value) {
		dereference().setAttribute(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttributes(java.util.Map)
	 */
	@Override
	public void setAttributes(Map<String, String> data) {
		dereference().setAttributes(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setDataAttributes(org.processmining
	 * .framework.log.DataSection)
	 */
	@Override
	public void setDataAttributes(DataSection data) {
		dereference().setDataAttributes(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setDescription(java.lang.String
	 * )
	 */
	@Override
	public void setDescription(String description) {
		dereference().setDescription(description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setModelReferences(java.util
	 * .List)
	 */
	@Override
	public void setModelReferences(List<String> modelReferences) {
		dereference().setModelReferences(modelReferences);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		dereference().setName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#toString()
	 */
	@Override
	public String toString() {
		return dereference().toString();
	}

}
