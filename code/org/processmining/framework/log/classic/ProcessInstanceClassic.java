/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.log.classic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.ModelElements;
import org.processmining.framework.log.ProcessInstance;

/**
 * A single process instance.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class ProcessInstanceClassic extends ProcessInstance {

	private String process;
	// private AuditTrailEntries entries;
	private ArrayList entries;
	private ModelElementsClassic elements = null;
	protected String name = null;
	protected String description = null;
	protected DataSection attributes = null;

	public ProcessInstanceClassic(String process, String name,
			String description, Map<String, String> data, ArrayList entries) {
		this.process = (process == null ? "" : process.trim());
		this.name = (name == null ? "" : name.trim());
		this.attributes = new DataSection();
		this.entries = entries;
		this.description = description;
		if (data != null) {
			this.attributes.putAll(data);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.classic.ProcessInstance#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ProcessInstance#getProcess()
	 */
	public String getProcess() {
		return process;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ProcessInstance#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.classic.ProcessInstance#isEmpty()
	 */
	public boolean isEmpty() {
		return entries.size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.ProcessInstance#getAuditTrailEntries()
	 */
	public AuditTrailEntries getAuditTrailEntries() {
		return new AuditTrailEntriesClassic(entries);
		// return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#getModelElements()
	 */
	public ModelElements getModelElements() {
		if (elements == null) {
			elements = new ModelElementsClassic(this);
		}
		return elements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#getData()
	 */
	public Map getData() {
		return attributes;
	}

	public DataSection getDataAttributes() {
		return attributes;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = "";
		AuditTrailEntries ate = getAuditTrailEntries();

		while (ate.hasNext()) {
			s += "\t" + ate.next().toString() + "\n";
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
		// if new methods are used with classic log reader, interface must be
		// mediated
		AuditTrailEntries ate = getAuditTrailEntries();
		AuditTrailEntryListProxy proxy = new AuditTrailEntryListProxy(ate);
		return proxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.ProcessInstance#cloneInstance()
	 */
	public ProcessInstance cloneInstance() {
		// if new methods are used with classic log reader, interface must be
		// mediated
		ProcessInstanceClassic o = null;
		try {
			o = (ProcessInstanceClassic) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// clone non-primitive types
		if (attributes != null) {
			// TODO check whether holds - strings are expected to be copied
			// anyway (as they are treated
			// as basic data types)
			o.attributes = (DataSection) attributes.clone();
		}
		if (entries != null) {
			o.entries = (ArrayList) ((AuditTrailEntries) getAuditTrailEntries()
					.clone()).toArrayList();
		}
		if (elements != null) {
			o.elements = (ModelElementsClassic) elements.clone();
		}
		return o;
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
	 * @see org.processmining.framework.log.LogEntity#setAttributes(DataSection)
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

	public List<String> getModelReferences() {
		return new ArrayList<String>();
	}

	public void setModelReferences(List<String> modelReferences) {
		// empty on purpose
	}
}
