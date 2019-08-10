/**
 * Project: ProM HPLR
 * File: LogEntity.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 27, 2006, 2:25:08 AM
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
package org.processmining.framework.log;

import java.util.List;
import java.util.Map;

/**
 * This abstract class contains the basic attributes of any log entity, such as
 * and audit trail entry, process, or process instance, and corresponding
 * retrieval and modification methods.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public abstract class LogEntity {

	/**
	 * Returns the name of this entity.
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Returns the description of this entity.
	 * 
	 * @return
	 */
	public abstract String getDescription();

	/**
	 * Returns the meta-data associated with this entity.
	 * 
	 * @return
	 */
	public abstract DataSection getDataAttributes();

	/**
	 * Returns the meta-data associated with this entity.
	 * 
	 * @return
	 */
	public abstract Map<String, String> getAttributes();

	/**
	 * Sets the name, or ID, of this entity
	 * 
	 * @param name
	 */
	public abstract void setName(String name);

	/**
	 * Sets the description of this entity
	 * 
	 * @param description
	 */
	public abstract void setDescription(String description);

	/**
	 * Sets the set of attributes, given as DataAttributes, for this entity.
	 * Replaces the previously contained set of attributes.
	 * 
	 * @param data
	 */
	public abstract void setDataAttributes(DataSection data);

	/**
	 * Sets the set of attributes, given as key-value pairs, for this entity.
	 * Replaces the previously contained set of attributes.
	 * 
	 * @param data
	 */
	public abstract void setAttributes(Map<String, String> data);

	/**
	 * Sets an attribute of this entity as a key-value pair. If a previous
	 * mapping for the given key exists, it will be removed. Otherwise a new
	 * mapping will be added.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setAttribute(String key, String value);

	/**
	 * Removes the attribute with the given key from the set of attributes of
	 * this entity.
	 * 
	 * @param key
	 */
	public abstract void removeAttribute(String key);

	/**
	 * Returns the list of model elements (concepts in ontologies) of this
	 * entity
	 */
	public abstract List<String> getModelReferences();

	/**
	 * Sets the list of model references (concepts in ontologies)
	 * 
	 * @param modelReferences
	 *            the new list of model references
	 */
	public abstract void setModelReferences(List<String> modelReferences);

	/**
	 * Returns a string representation of this entity.
	 * 
	 * @return
	 */
	public String toString() {
		return getName();
	}

}
