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

package org.processmining.framework.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A general information item from a workflow log.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 * @author Peter van den Brand
 * @version 2.0
 */
public class InfoItem {

	/**
	 * Name of the info item
	 */
	protected String name;
	/**
	 * Description string
	 */
	protected String description;
	/**
	 * Map containing a set of attributes, stored as key-value pairs.
	 */
	protected DataSection data;

	/**
	 * List of model references (concepts in ontologies)
	 */
	protected List<String> modelReferences;

	private static final List<String> EMPTY_LIST = Collections
			.unmodifiableList(new ArrayList<String>(0));

	/**
	 * Creates a new information item
	 * 
	 * @param name
	 *            Name of the information item
	 * @param description
	 *            Description string
	 * @param data
	 *            Map containing a set of attributes,
	 * @param modelReferences
	 *            List containing a the model references stored as key-value
	 *            pairs.
	 */
	public InfoItem(String name, String description, DataSection data,
			List<String> modelReferences) {
		this.name = (name == null ? "" : name.trim());
		this.description = (description == null ? "" : description.trim());
		this.data = (data == null ? new DataSection() : new DataSection(data));
		setModelReferences(modelReferences);
	}

	/**
	 * @return The name of the information item
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The description of the information item
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return The attributes, stored as key-value pairs.
	 */
	public DataSection getData() {
		return data;
	}

	/**
	 * @return The model references as a list of URIs.
	 */
	public List<String> getModelReferences() {
		return modelReferences;
	}

	/**
	 * Sets the information item's name.
	 * 
	 * @param name
	 *            The information item's name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the information item's description.
	 * 
	 * @param description
	 *            The information item's description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the information item's attributes.
	 * 
	 * @param data
	 *            The information item's attributes.
	 */
	public void setData(DataSection data) {
		this.data = new DataSection(data);
	}

	/**
	 * Sets the information item's list of model references.
	 * 
	 * @param modelReferences
	 *            The information item's model references.
	 */
	public void setModelReferences(List<String> modelReferences) {
		this.modelReferences = (modelReferences == null ? EMPTY_LIST
				: modelReferences);
	}

	/**
	 * Adds an attribute as key-value pair to this information item.
	 * 
	 * @param key
	 *            Key of the given attribute.
	 * @param value
	 *            Value of the given attribute.
	 */
	public void addAttribute(String key, String value) {
		this.data.put(key, value);
	}

	/**
	 * Adds an attribute as key-value pair to this information item.
	 * 
	 * @param key
	 *            Key of the given attribute.
	 * @param value
	 *            Value of the given attribute.
	 */
	public void addAttribute(DataAttribute attr) {
		this.data.put(attr);
	}

	/**
	 * Returns a string representation of this information item.
	 */
	public String toString() {
		return "name: " + name + ", desc: " + description + ", data: "
				+ data.toString();
	}

	public boolean equals(Object other) {
		if ((other instanceof InfoItem) == false) {
			return false;
		}
		InfoItem otherItem = (InfoItem) other;
		return (this.name.equals(otherItem.name)
				&& (this.description.equals(otherItem.description)) && (this.data
				.equals(otherItem.data)));
	}

}
