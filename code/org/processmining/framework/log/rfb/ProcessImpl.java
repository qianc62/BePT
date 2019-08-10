/**
 * Project: ProM HPLR
 * File: ProcessImpl.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 27, 2006, 1:59:47 AM
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
package org.processmining.framework.log.rfb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.ModelElements;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;

/**
 * This class provides a complete implementation for a process, which is
 * essentially a container for process instances with additional attributes.
 * <p>
 * <b>Notice:</b> This implementation is optimized for random access using
 * indices for addressing specific process instances. Referencing instances by
 * their name is implemented in a rather expensive manner to this date.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class ProcessImpl extends Process {

	/**
	 * Contains the instances of the process in an ordered fashion.
	 */
	protected ArrayList<ProcessInstance> instances = null;
	/**
	 * The process ID
	 */
	protected String name = null;
	/**
	 * The process description
	 */
	protected String description = null;
	/**
	 * Data attributes associated with a process
	 */
	protected DataSection attributes = null;

	/**
	 * The list of model references (concepts in ontologies)
	 */
	private List<String> modelReferences;

	private static final List<String> EMPTY_LIST = Collections
			.unmodifiableList(new ArrayList<String>(0));

	/**
	 * Creates a new process.
	 * 
	 * @param name
	 *            Name / ID of the process.
	 * @param description
	 *            Description string.
	 * @param attributes
	 *            Set of attributes as key-value pairs.
	 * @param modelReferences
	 *            List of model references (concepts in ontologies)
	 */
	public ProcessImpl(String name, String description, DataSection attributes,
			final List<String> modelReferences) {
		this.name = name;
		this.description = description;
		this.attributes = new DataSection(attributes);
		this.instances = new ArrayList<ProcessInstance>();
		this.modelReferences = modelReferences;
	}

	/**
	 * Creates a clone of the provided process. This clone is super-shallow, in
	 * the sense that only the meta-information of the process is copied (in a
	 * deep manner), but no instances are transferred from template to clone.
	 * <p>
	 * This is derived from the practice of cloning, or deriving, log readers,
	 * where not always all instances are transferred to the new clone. It is
	 * thus up to the implementation of the cloning class to take care of
	 * transferring instances (in the depth required).
	 * 
	 * @param template
	 *            The process to be cloned in a super-shallow manner.
	 */
	public ProcessImpl(ProcessImpl template) {
		this.name = template.name;
		this.description = template.description;
		this.attributes = new DataSection(template.attributes);
		this.instances = new ArrayList<ProcessInstance>();
		this.modelReferences = template.modelReferences;
	}

	/**
	 * Adds a new process instance to this process (appended to the end of the
	 * already contained set).
	 */
	public void addProcessInstance(ProcessInstance instance) {
		instances.add(instance);
	}

	/**
	 * Removes the given process instance from the process.
	 * 
	 * @param instance
	 *            The process instance to be removed.
	 * @return Whether the instance was indeed removed.
	 */
	public boolean removeProcessInstance(ProcessInstance instance) {
		return instances.remove(instance);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#getInstance(int)
	 */
	public ProcessInstance getInstance(int index) {
		return (ProcessInstance) instances.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.Process#getInstance(java.lang.String)
	 */
	public ProcessInstance getInstance(String name) {
		ProcessInstance pi = null;
		for (int i = 0; i < instances.size(); i++) {
			pi = (ProcessInstance) instances.get(i);
			if (pi.getName().equals(name)) {
				return pi;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#getInstanceNames()
	 */
	public String[] getInstanceNames() {
		ProcessInstance pi = null;
		ArrayList<String> names = new ArrayList<String>();
		for (int i = 0; i < instances.size(); i++) {
			pi = (ProcessInstance) instances.get(i);
			names.add(pi.getName());
		}
		return names.toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#getModelElements()
	 */
	public ModelElements getModelElements() {
		LightweightModelElements elements = new LightweightModelElements(
				new HashSet<ProcessInstance>(instances));
		ProcessInstance pi = null;
		/*
		 * Model elements of a process are implemented as the union of model
		 * elements of all contained instances. It is implicitly assumed, that
		 * all process instances contain their model elements as lightweight
		 * model elements. TODO: If this method gains popularity, it is advised
		 * to buffer the set of model elements over consecutive calls.
		 * Therefore, the validity of the set must be ensured, as instances can
		 * be removed or added in the meantime. This implementation, though more
		 * expensive, is modification-safe in the above respect.
		 */
		for (Iterator it = instances.iterator(); it.hasNext();) {
			pi = (ProcessInstance) it.next();
			elements.merge((LightweightModelElements) pi.getModelElements());
		}
		return elements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#iterator()
	 */
	public Iterator iterator() {
		return instances.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.Process#size()
	 */
	public int size() {
		return instances.size();
	}

	public int clear() {
		int result = instances.size();
		instances.clear();
		return result;
	}

	public ProcessInstance removeProcessInstance(String name) {
		ProcessInstance pi = null;
		for (int i = 0; i < instances.size(); i++) {
			pi = (ProcessInstance) instances.get(i);
			if (pi.getName().equals(name)) {
				instances.remove(i);
				return pi;
			}
		}
		return null;
	}

	public ProcessInstance removeProcessInstance(int index) {
		return (ProcessInstance) instances.remove(index);
	}

	public String toString() {
		return "Process '" + name + "' (" + size() + " instances)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getDataAttributes()
	 */
	public DataSection getDataAttributes() {
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#removeAttribute(java.lang.String
	 * )
	 */
	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttribute(java.lang.String,
	 * java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttributes(java.util.Map)
	 */
	public void setDataAttributes(DataSection data) {
		attributes = new DataSection(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map<String, String> data) {
		attributes = DataSection.fromMap(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setDescription(java.lang.String
	 * )
	 */
	public void setDescription(String nDescription) {
		description = nDescription;
	}

	public List<String> getModelReferences() {
		return modelReferences == null ? EMPTY_LIST : Collections
				.unmodifiableList(modelReferences);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#setName(java.lang.String)
	 */
	public void setName(String nName) {
		name = nName;
	}

	public void setModelReferences(List<String> modelReferences) {
		this.modelReferences = modelReferences == null ? null
				: new ArrayList<String>(modelReferences);
	}
}
