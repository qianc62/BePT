/**
 * Project: ProM
 * File: ProxyProcessInstance.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Sep 28, 2006, 5:02:54 PM
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

import java.util.List;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.ModelElements;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.AuditTrailEntriesProxy;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ProxyProcessInstance extends ProcessInstance {

	protected ProcessInstance parent = null;

	public ProxyProcessInstance(ProcessInstance aParent) {
		parent = aParent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#cloneInstance()
	 */
	public ProcessInstance cloneInstance() {
		return new ProxyProcessInstance(parent.cloneInstance());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.ProcessInstance#getAuditTrailEntries()
	 */
	public AuditTrailEntries getAuditTrailEntries() {
		return new AuditTrailEntriesProxy(getAuditTrailEntryList(), 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.ProcessInstance#getAuditTrailEntryList()
	 */
	public AuditTrailEntryList getAuditTrailEntryList() {
		return new ProxyAuditTrailEntryList(parent.getAuditTrailEntryList());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#getModelElements()
	 */
	public ModelElements getModelElements() {
		return parent.getModelElements();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#getProcess()
	 */
	public String getProcess() {
		return parent.getProcess();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#isEmpty()
	 */
	public boolean isEmpty() {
		return parent.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getAttributes()
	 */
	public DataSection getDataAttributes() {
		return parent.getDataAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return parent.getAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getDescription()
	 */
	public String getDescription() {
		return parent.getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getName()
	 */
	public String getName() {
		return parent.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#removeAttribute(java.lang.String
	 * )
	 */
	public void removeAttribute(String key) {
		parent.removeAttribute(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttribute(java.lang.String,
	 * java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		parent.setAttribute(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttributes(java.util.Map)
	 */
	public void setDataAttributes(DataSection data) {
		parent.setDataAttributes(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map<String, String> data) {
		parent.setAttributes(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setDescription(java.lang.String
	 * )
	 */
	public void setDescription(String description) {
		parent.setDescription(description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#setName(java.lang.String)
	 */
	public void setName(String name) {
		parent.setName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#toString()
	 */
	public String toString() {
		return parent.toString();
	}

	public List<String> getModelReferences() {
		return parent.getModelReferences();
	}

	public void setModelReferences(List<String> modelReferences) {
		parent.setModelReferences(modelReferences);
	}
}
