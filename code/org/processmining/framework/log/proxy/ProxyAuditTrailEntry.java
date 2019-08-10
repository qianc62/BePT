/**
 * Project: ProM
 * File: ProxyAuditTrailEntry.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Sep 28, 2006, 5:03:18 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.proxy;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ProxyAuditTrailEntry extends AuditTrailEntry {

	protected AuditTrailEntryList parentList = null;
	protected int parentPosition = 0;

	public ProxyAuditTrailEntry(AuditTrailEntryList aParentList, int aPosition) {
		parentList = aParentList;
		parentPosition = aPosition;
	}

	protected AuditTrailEntry parent() {
		AuditTrailEntry result = null;
		try {
			result = parentList.get(parentPosition);
		} catch (IOException e) {
			System.err.println("Error resolving proxy parent!");
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#clone()
	 */
	public Object clone() {
		return parent().clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof ProxyAuditTrailEntry) {
			ProxyAuditTrailEntry other = (ProxyAuditTrailEntry) o;
			return (parentList == other.parentList && parentPosition == other.parentPosition);
		} else {
			return parent().equals(o);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#getOriginator()
	 */
	public String getOriginator() {
		return parent().getOriginator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#getTimestamp()
	 */
	public Date getTimestamp() {
		return parent().getTimestamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#getType()
	 */
	public String getType() {
		return parent().getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#hashCode()
	 */
	public int hashCode() {
		return parent().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntry#id()
	 */
	public long id() {
		return parent().id();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#setOriginator(java.lang
	 * .String)
	 */
	public void setOriginator(String originator) {
		parent().setOriginator(originator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#setTimestamp(java.util
	 * .Date)
	 */
	public void setTimestamp(Date timestamp) {
		parent().setTimestamp(timestamp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntry#setType(java.lang.String)
	 */
	public void setType(String type) {
		parent().setType(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getDataAttributes()
	 */
	public DataSection getDataAttributes() {
		return parent().getDataAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return parent().getAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getDescription()
	 */
	public String getDescription() {
		return parent().getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getName()
	 */
	public String getName() {
		return parent().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#removeAttribute(java.lang.String
	 * )
	 */
	public void removeAttribute(String key) {
		parent().removeAttribute(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttribute(java.lang.String,
	 * java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		parent().setAttribute(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttributes(java.util.Map)
	 */
	public void setDataAttributes(DataSection data) {
		parent().setDataAttributes(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map<String, String> data) {
		parent().setAttributes(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setDescription(java.lang.String
	 * )
	 */
	public void setDescription(String description) {
		parent().setDescription(description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#setName(java.lang.String)
	 */
	public void setName(String name) {
		parent().setName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#toString()
	 */
	public String toString() {
		return parent().toString();
	}

	public List<String> getModelReferences() {
		return parent().getModelReferences();
	}

	public List<String> getElementModelReferences() {
		return parent().getElementModelReferences();
	}

	public List<String> getOriginatorModelReferences() {
		return parent().getOriginatorModelReferences();
	}

	public List<String> getTypeModelReferences() {
		return parent().getTypeModelReferences();
	}

	public void setElementModelReferences(List<String> modelReferences) {
		parent().setElementModelReferences(modelReferences);
	}

	public void setOriginatorModelReferences(List<String> modelReferences) {
		parent().setOriginatorModelReferences(modelReferences);
	}

	public void setTypeModelReferences(List<String> modelReferences) {
		parent().setTypeModelReferences(modelReferences);
	}

	public void setModelReferences(List<String> modelReferences) {
		parent().setModelReferences(modelReferences);
	}
}
