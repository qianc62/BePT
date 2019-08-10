/**
 * Project: ProM
 * File: ProxyProcess.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Sep 28, 2006, 5:01:53 PM
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.ModelElements;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ProxyProcess extends Process {

	protected Process parent = null;

	public ProxyProcess(Process aParent) {
		parent = aParent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.Process#addProcessInstance(org.processmining
	 * .framework.log.ProcessInstance)
	 */
	public void addProcessInstance(ProcessInstance instance) {
		parent.addProcessInstance(instance);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#clear()
	 */
	public int clear() {
		return parent.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#getInstance(int)
	 */
	public ProcessInstance getInstance(int index) {
		return new ProxyProcessInstance(parent.getInstance(index));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.Process#getInstance(java.lang.String)
	 */
	public ProcessInstance getInstance(String name) {
		return new ProxyProcessInstance(parent.getInstance(name));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#getInstanceNames()
	 */
	public String[] getInstanceNames() {
		return parent.getInstanceNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#getModelElements()
	 */
	public ModelElements getModelElements() {
		return parent.getModelElements();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#iterator()
	 */
	public Iterator iterator() {
		return new ProxyProcessIterator(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.Process#removeProcessInstance(java.lang
	 * .String)
	 */
	public ProcessInstance removeProcessInstance(String name) {
		return new ProxyProcessInstance(parent.removeProcessInstance(name));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#removeProcessInstance(int)
	 */
	public ProcessInstance removeProcessInstance(int index) {
		return new ProxyProcessInstance(parent.removeProcessInstance(index));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#size()
	 */
	public int size() {
		return parent.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getDataAttributes()
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

	protected class ProxyProcessIterator implements Iterator {

		protected ProxyProcess process = null;
		protected int position = 0;

		public ProxyProcessIterator(ProxyProcess aParent) {
			process = aParent;
			position = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return (position < process.size());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			return process.getInstance(position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			System.err.println("Modification of proxy not allowed!");
		}

	}

	public List<String> getModelReferences() {
		return parent.getModelReferences();
	}

	public void setModelReferences(List<String> modelReferences) {
		parent.setModelReferences(modelReferences);
	}
}
