/**
 * Project: ProM HPLR
 * File: ProcessInstanceImpl.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 27, 2006, 9:23:29 PM
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ModelElements;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.io.ATERandomFileBuffer;

/**
 * Implements the interface of a process instance. Semi-lighweight container,
 * keeping the process instance's meta-information in memory while swapping the
 * actual audit trail entries contained to persistent memory. Uses the cached
 * managed random-access file implementation provided by AuditTrailEntryListImpl
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ProcessInstanceImpl extends ProcessInstance {

	protected static boolean USE_FAST_LIST = true;

	protected static AuditTrailEntryList clone(AuditTrailEntryList list) {
		AuditTrailEntryList clone = null;
		if (list instanceof AuditTrailEntryListImpl) {
			try {
				clone = ((AuditTrailEntryListImpl) list).cloneInstance();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (list instanceof AuditTrailEntryListFastImpl) {
			try {
				clone = ((AuditTrailEntryListFastImpl) list).cloneInstance();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return clone;
	}

	protected static AuditTrailEntryList createList() {
		AuditTrailEntryList virgin = null;
		if (USE_FAST_LIST == true) {
			try {
				virgin = new AuditTrailEntryListFastImpl();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				virgin = new AuditTrailEntryListImpl();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return virgin;
	}

	protected static AuditTrailEntryList createList(ATERandomFileBuffer buffer,
			int maxFragment) {
		AuditTrailEntryList virgin = null;
		if (USE_FAST_LIST == true) {
			try {
				virgin = new AuditTrailEntryListFastImpl(buffer, maxFragment);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			virgin = new AuditTrailEntryListImpl(buffer, maxFragment);
		}
		return virgin;
	}

	/**
	 * ID of the process instance
	 */
	protected String name = null;
	/**
	 * Human-readable description string
	 */
	protected String description = null;
	/**
	 * Map of string-based data attributes (key-value pairs)
	 */
	protected DataSection attributes = null;
	/**
	 * Holds the name, or ID, of this process instance. Replicated, because
	 * process instances must not be managed by, nor requested from, a
	 * <code>Process</code> container.
	 */
	protected String process = null;
	/**
	 * List of audit trail entries contained in this process instance.
	 */
	protected AuditTrailEntryList ates = null;
	/**
	 * Lightweight model elements container.
	 */
	protected LightweightModelElements modelElements = null;
	/**
	 * Proxy providing the <code>AuditTrailEntries</code> legacy interface to
	 * plugins that have not been updated yet. <b>This field is subject to
	 * removal!</b>
	 */
	protected AuditTrailEntriesProxy atesProxy = null;

	/**
	 * The list of model references (concepts in ontologies)
	 */
	private List<String> modelReferences;

	private static final List<String> EMPTY_LIST = Collections
			.unmodifiableList(new ArrayList<String>(0));

	/**
	 * Creates a new process instance.
	 * 
	 * @param processName
	 *            Name of the process of which this is to be an instance.
	 * @param ateList
	 *            List of audit trail entries contained in this process
	 *            instance.
	 */
	public ProcessInstanceImpl(String processName, AuditTrailEntryList ateList,
			final List<String> modelReferences) {
		name = null;
		description = null;
		attributes = new DataSection();
		process = processName;
		ates = ateList;
		atesProxy = new AuditTrailEntriesProxy(ates, 0);
		this.modelReferences = modelReferences;
	}

	/**
	 * Creates a new process instance.
	 * 
	 * @param processName
	 *            Name of the process of which this is to be an instance.
	 * @param ateBuffer
	 *            Random File Buffer containing the audit trail entries of this
	 *            instance.
	 * @param maxFragment
	 *            Maximal fragmentation count of the transparently created
	 *            AuditTrailEntryList managing read/write access to the set of
	 *            audit trail entries.
	 */
	public ProcessInstanceImpl(String processName,
			ATERandomFileBuffer ateBuffer, int maxFragment, LogReader log,
			final List<String> modelReferences) {
		this(processName, createList(ateBuffer, maxFragment), modelReferences);
	}

	/**
	 * Creates a new empty process instance.
	 * 
	 * @param processName
	 *            Name of the process of which this is to be an instance.
	 * @throws IOException
	 */
	public ProcessInstanceImpl(String processName,
			final List<String> modelReferences) throws IOException {
		this(processName, createList(), modelReferences);
	}

	/**
	 * Creates a clone of the given process instance. Changes are not
	 * synchronized between template and clone.
	 * 
	 * @param template
	 *            The instance to be cloned.
	 * @throws IOException
	 */
	public ProcessInstanceImpl(ProcessInstanceImpl template) throws IOException {
		name = template.name;
		description = template.description;
		attributes = new DataSection(template.attributes);
		process = template.process;
		ates = clone(template.ates);
		atesProxy = new AuditTrailEntriesProxy(ates, 0);
		modelReferences = template.modelReferences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#getProcess()
	 */
	public String getProcess() {
		return process;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#isEmpty()
	 */
	public boolean isEmpty() {
		return (ates.size() == 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#toString()
	 */
	public String toString() {
		String s = "";
		Iterator it = getAuditTrailEntryList().iterator();
		while (it.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) it.next();
			s += "\t" + ate.toString() + "\n";
		}
		return "[PI: " + process + ", " + name + ", data: "
				+ attributes.toString() + ",\n" + s + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.ProcessInstance#getAuditTrailEntryList()
	 */
	public AuditTrailEntryList getAuditTrailEntryList() {
		return ates;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#getModelElements()
	 */
	public ModelElements getModelElements() {
		if (modelElements == null || ates.isTainted()) {
			// first access to model elements, or the process instance's set
			// of audit trail entries has been modified since the last
			// invocation.
			// reconstruct the set of model elements from scratch.
			HashSet<ProcessInstance> instanceSet = new HashSet<ProcessInstance>();
			instanceSet.add(this);
			modelElements = new LightweightModelElements(instanceSet);
			for (int i = 0; i < ates.size(); i++) {
				try {
					modelElements.addElement(ates.get(i).getElement());
				} catch (IOException e) {
					// I/O problems, abort and print message to STDERR.
					System.err.println("Serious error in class "
							+ this.getClass().toString() + ":");
					e.printStackTrace();
					return null;
				}
			}
		}
		return modelElements;
	}

	/**
	 * Returns a deep copy of this instance. Changes are not synchronized
	 * between instance and clone.
	 * <p>
	 * <b>Warning:</b> Can return <code>null</code> if a serious error has been
	 * encountered! Check for this return value in your using code!
	 */
	public ProcessInstance cloneInstance() {
		try {
			return new ProcessInstanceImpl(this);
		} catch (IOException e) {
			// print error and return null
			e.printStackTrace();
			return null;
		}
	}

	public void cleanup() throws IOException {
		ates.cleanup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.ProcessInstance#getAuditTrailEntries()
	 */
	public AuditTrailEntries getAuditTrailEntries() {
		return new AuditTrailEntriesProxy(ates, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getAttributes()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#setName(java.lang.String)
	 */
	public void setName(String nName) {
		name = nName;
	}

	/*
	 * The method sets the process this processinstance belongs to. If the
	 * process instance is already part of a process and the name is changed, it
	 * will be thrown out by the logReader automatically. Therefore it is wise
	 * only to use this method on instances that do have not been added to a
	 * Process object yet.
	 */
	public void setProcess(String process) {
		this.process = process;
	}

	public List<String> getModelReferences() {
		return modelReferences == null ? EMPTY_LIST : Collections
				.unmodifiableList(modelReferences);
	}

	public void setModelReferences(List<String> modelReferences) {
		this.modelReferences = modelReferences == null ? null
				: new ArrayList<String>(modelReferences);
	}

}
