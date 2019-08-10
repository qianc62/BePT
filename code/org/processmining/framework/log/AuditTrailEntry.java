/**
 * Project: ProM
 * File: AuditTrailEntry.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 22, 2006, 12:29:14 AM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
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
 ***********************************************************
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

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This abstract class defines the access methods to an audit trail entry object
 * in the framework.<br/>
 * <b>Notice:</b> Read-only implementations of this interface may throw
 * exceptions when modifying methods are called. It is expected that you handle
 * these according to your intent.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 * 
 */
public abstract class AuditTrailEntry extends LogEntity {

	/**
	 * Returns the workflow model element identifier (event name)
	 * 
	 * @return String
	 */
	public String getElement() {
		return getName();
	}

	/**
	 * Returns the list of model references (concepts in ontologies) for the
	 * element of this entry.
	 */
	public abstract List<String> getElementModelReferences();

	/**
	 * Sets the list of model references (concepts in ontologies) for the
	 * element of this entry.
	 * 
	 * @param modelReferences
	 */
	public abstract void setElementModelReferences(List<String> modelReferences);

	/**
	 * Returns the event type, as defined in the MXML finite state machine.
	 * 
	 * @return String
	 */
	public abstract String getType();

	/**
	 * Returns the list of model references (concepts in ontologies) for the
	 * type of this entry.
	 */
	public abstract List<String> getTypeModelReferences();

	/**
	 * Sets the list of model references (concepts in ontologies) for the type
	 * of this entry.
	 * 
	 * @param modelReferences
	 */
	public abstract void setTypeModelReferences(List<String> modelReferences);

	/**
	 * Returns the exact date and time, at which this event has occurred. Notice
	 * that this is an optional attribute, thus <code>null</code> may be
	 * returned.
	 * 
	 * @return Date
	 */
	public abstract Date getTimestamp();

	/**
	 * Returns the originator, i.e. the name or identifier of the entity which
	 * has triggered the occurrence of the described event.
	 * 
	 * @return String
	 */
	public abstract String getOriginator();

	/**
	 * Returns the list of model references (concepts in ontologies) for the
	 * originator of this entry.
	 */
	public abstract List<String> getOriginatorModelReferences();

	/**
	 * Sets the list of model references (concepts in ontologies) for the
	 * originator of this entry.
	 * 
	 * @param modelReferences
	 */
	public abstract void setOriginatorModelReferences(
			List<String> modelReferences);

	/**
	 * Returns the ID of this audit trail entry.
	 * <p>
	 * An ID for an audit trail entry is defined to be unique within the context
	 * of its containing log file collection.
	 * 
	 * @return
	 */
	public abstract long id();

	/**
	 * Sets the model element of this event.<br/>
	 * <b>Notice:</b> Implementations of this interface may support modification
	 * of instances to varying degrees. It is suggested that you test the
	 * modification support of the underlying implementation by use of the
	 * <code>isMutable()</code> method.
	 * 
	 * @see isMutable
	 * @param element
	 *            identifier of the workflow model element associated with this
	 *            event.
	 */
	public void setElement(String element) {
		setName(element);
	}

	/**
	 * Sets the type of this event, as defined in the MXML finite state machine.<br/>
	 * <b>Notice:</b> Implementations of this interface may support modification
	 * of instances to varying degrees. It is suggested that you test the
	 * modification support of the underlying implementation by use of the
	 * <code>isMutable()</code> method.
	 * 
	 * @param type
	 *            String identifier of the event type to set.
	 */
	public abstract void setType(String type);

	/**
	 * Sets the timestamp of this event.<br/>
	 * <b>Notice:</b> Implementations of this interface may support modification
	 * of instances to varying degrees. It is suggested that you test the
	 * modification support of the underlying implementation by use of the
	 * <code>isMutable()</code> method.
	 * 
	 * @param timestamp
	 *            The exact date and time at which this event has been observed.
	 */
	public abstract void setTimestamp(Date timestamp);

	/**
	 * Sets the name or identifier of the organizational entity which has
	 * triggered the occurrence of this event.<br/>
	 * <b>Notice:</b> Implementations of this interface may support modification
	 * of instances to varying degrees. It is suggested that you test the
	 * modification support of the underlying implementation by use of the
	 * <code>isMutable()</code> method.
	 * 
	 * @param originator
	 *            Name or identifier of the organizational entity having
	 *            triggered this event.
	 */
	public abstract void setOriginator(String originator);

	/**
	 * Overridden to specify when two audit trail entries are considered to be
	 * equal.
	 * 
	 * @param o
	 *            Object The <code>AuditTrailEntry</code> to be compared with.
	 * @return boolean True if all attributes are equal, false otherwise.
	 */
	public abstract boolean equals(Object o);

	/**
	 * Overridden to produce the same hash code for equal objects.
	 * 
	 * @return int The hash code calculated.
	 */
	public abstract int hashCode();

	/**
	 * Make a deep copy of the object. Note that this method needs to be
	 * extended as soon as there are attributes added to the class which are not
	 * primitive or immutable.
	 * 
	 * @return Object The cloned object.
	 */
	public abstract Object clone();

	/*
	 * ------------------------------------------------------------------------
	 * Deprecated methods - do not use anymore! (subject to removal!)
	 */

	/**
	 * Returns a map of key / value pairs describing data available for this
	 * event.
	 * 
	 * @deprecated Use <code>getAttributes()</code> instead!
	 * @return Map
	 */
	public Map<String, String> getData() {
		return getAttributes();
	}

	/**
	 * Sets an identified attribute in the data section of this event.
	 * Attributes are specified as key / value pairs. When the attribute has
	 * already been present in the audit trail entry, its value is replaced by
	 * this method. If the named attribute has not previously existed, it is
	 * newly introduced and created.<br/>
	 * <b>Notice:</b> Implementations of this interface may support modification
	 * of instances to varying degrees. It is suggested that you test the
	 * modification support of the underlying implementation by use of the
	 * <code>isMutable()</code> method.
	 * 
	 * @param key
	 *            Key of the attribute to be set.
	 * @param value
	 *            Value to set the attribute to.
	 * @deprecated Use <code>setAttribute()</code> instead!
	 */
	public void addDataAttribute(String key, String value) {
		setAttribute(key, value);
	}

}
