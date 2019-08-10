/**
 * Project: ProM
 * File: AuditTrailEntryImpl.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 22, 2006, 11:14:04 PM
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
package org.processmining.framework.log.rfb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;

/**
 * This class implements an audit trail entry.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class AuditTrailEntryImpl extends AuditTrailEntry {

	// serial version id for Java serialization
	private static final long serialVersionUID = -8259601214466810886L;

	protected String element;
	protected String eventType;
	protected Date timestamp;
	protected String originator;
	protected DataSection attributes;
	protected long instanceNumber;

	private List<String> elementModelReferences;
	private List<String> originatorModelReferences;
	private List<String> typeModelReferences;

	private static final List<String> EMPTY_LIST = Collections
			.unmodifiableList(new ArrayList<String>(0));

	// static instance number handling; for ordering purposes (c.f. interface
	// Comparable)
	protected static long instanceNumberCounter = Long.MIN_VALUE;

	synchronized protected static long nextInstanceNumber() {
		long result = AuditTrailEntryImpl.instanceNumberCounter;
		if (result == Long.MAX_VALUE) {
			// reset instance counter
			AuditTrailEntryImpl.instanceNumberCounter = Long.MIN_VALUE;
		}
		AuditTrailEntryImpl.instanceNumberCounter++;
		return result;
	}

	/**
	 * creates a new audit trail entry with initialized data hashtable
	 */
	public AuditTrailEntryImpl() {
		element = null;
		eventType = null;
		timestamp = null;
		originator = null;
		attributes = new DataSection();
		instanceNumber = nextInstanceNumber();
		elementModelReferences = null;
		typeModelReferences = null;
		originatorModelReferences = null;
	}

	/**
	 * Internal constructor. Should only be used within parts of the framework
	 * whose core responsibility is managing and reading / writing logs!
	 * 
	 * @param aAttributes
	 *            hash table containing data attributes (may be
	 *            <code>null</code>)
	 * @param aWFMElement
	 *            workflow model element
	 * @param anEventType
	 *            event type
	 * @param anUnknownType
	 *            unknown event type (<code>null</code> or empty string if
	 *            well-known)
	 * @param aTimestamp
	 *            timestamp of audit trail entry (may be <code>null</code>)
	 * @param anOriginator
	 *            originator string (may be <code>null</code>)
	 * @param elementModelReferences
	 *            list of element model references (concept URIs) (may be
	 *            <code>null</code>)
	 * @param typeModelReferences
	 *            list of type model references (concept URIs) (may be
	 *            <code>null</code>)
	 * @param originatorModelReferences
	 *            list of originator model references (concept URIs) (may be
	 *            <code>null</code>)
	 */
	public AuditTrailEntryImpl(DataSection aAttributes, String aWFMElement,
			String anEventType, Date aTimestamp, String anOriginator,
			List<String> elementModelReferences,
			List<String> typeModelReferences,
			List<String> originatorModelReferences) {
		this(aAttributes, aWFMElement, anEventType, aTimestamp, anOriginator,
				nextInstanceNumber(), elementModelReferences,
				typeModelReferences, originatorModelReferences);
	}

	/**
	 * Internal constructor. Should only be used within parts of the framework
	 * whose core responsibility is managing and reading / writing logs!
	 * 
	 * @param aAttributes
	 *            hash table containing data attributes (may be
	 *            <code>null</code>)
	 * @param aWFMElement
	 *            workflow model element
	 * @param anEventType
	 *            event type
	 * @param anUnknownType
	 *            unknown event type (<code>null</code> or empty string if
	 *            well-known)
	 * @param aTimestamp
	 *            timestamp of audit trail entry (may be <code>null</code>)
	 * @param anOriginator
	 *            originator string (may be <code>null</code>)
	 * @param anID
	 *            ID of the audit trail entry.
	 *            <p>
	 *            <b>Warning:</b> Only use this constructor if you wish
	 * @param elementModelReferences
	 *            list of element model references (concept URIs) (may be
	 *            <code>null</code>)
	 * @param typeModelReferences
	 *            list of type model references (concept URIs) (may be
	 *            <code>null</code>)
	 * @param originatorModelReferences
	 *            list of originator model references (concept URIs) (may be
	 *            <code>null</code>) to reconstruct a properly saved ATE,
	 *            otherwise you're up for serious identity crisis!
	 */
	public AuditTrailEntryImpl(DataSection aAttributes, String aWFMElement,
			String anEventType, Date aTimestamp, String anOriginator,
			long anID, List<String> elementModelReferences,
			List<String> typeModelReferences,
			List<String> originatorModelReferences) {
		// 'attributes' can be null
		if (aAttributes != null) {
			attributes = new DataSection(aAttributes);
		} else {
			attributes = new DataSection();
		}
		element = aWFMElement;
		eventType = anEventType;
		timestamp = aTimestamp;
		originator = anOriginator;
		instanceNumber = anID;
		this.elementModelReferences = elementModelReferences;
		this.typeModelReferences = typeModelReferences;
		this.originatorModelReferences = originatorModelReferences;
	}

	/**
	 * clones the parameter object
	 * 
	 * @param toBeCloned
	 */
	public AuditTrailEntryImpl(AuditTrailEntry toBeCloned) {
		this.setDataAttributes(toBeCloned.getDataAttributes());
		this.setElement(toBeCloned.getElement());
		this.setType(toBeCloned.getType());
		if (toBeCloned.getTimestamp() != null) {
			// optional attribute!
			this.setTimestamp(toBeCloned.getTimestamp());
		}
		this.setOriginator(toBeCloned.getOriginator());
		this.setElementModelReferences(toBeCloned.getElementModelReferences());
		this.setTypeModelReferences(toBeCloned.getTypeModelReferences());
		this.setOriginatorModelReferences(toBeCloned
				.getOriginatorModelReferences());
		this.instanceNumber = nextInstanceNumber();
	}

	/**
	 * overriding Object's 'clone' method
	 */
	public Object clone() {
		return new AuditTrailEntryImpl(this);
	}

	/**
	 * Retrieves the type of this event
	 */
	public String getType() {
		return eventType;
	}

	/**
	 * Retrieves the log-wide unique ID of this event
	 */
	public long id() {
		return instanceNumber;
	}

	/**
	 * Sets this event's type
	 */
	public void setType(String type) {
		eventType = type;
	}

	/**
	 * @return Returns the originator.
	 */
	public String getOriginator() {
		return originator;
	}

	/**
	 * @param originator
	 *            The originator to set.
	 */
	public void setOriginator(String originator) {
		this.originator = originator;
	}

	/**
	 * @return Returns the timestamp.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            The timestamp to set.
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = new Date(timestamp.getTime());
	}

	/**
	 * set timestamp using a Date object
	 * 
	 * @param aDate
	 */
	public void setTimestamp(Date aDate) {
		this.timestamp = new Date(aDate.getTime());
	}

	/**
	 * set the timestamp using a date in milliseconds UN*X time
	 * 
	 * @param aDateInMilliseconds
	 */
	public void setTimestamp(long aDateInMilliseconds) {
		this.timestamp = new Date(aDateInMilliseconds);
	}

	/**
	 * implements the 'Comparable' interface
	 */
	public int compareTo(Object arg0) {
		if ((arg0 instanceof AuditTrailEntryImpl) == false) {
			return Integer.MIN_VALUE;
		}
		AuditTrailEntryImpl other = (AuditTrailEntryImpl) arg0;
		// notice, timestamps are not mandatory attributes!
		if (this.getTimestamp() != null && other.getTimestamp() != null) {
			int tsCompare = this.getTimestamp().compareTo(other.getTimestamp());
			if (tsCompare != 0) {
				return tsCompare;
			}
		}
		// fall back to instance number comparison (i.e. creation time
		// comparison)
		return (int) (instanceNumber - other.id());
	}

	/**
	 * implements the 'Comparable' interface
	 */
	public boolean equals(Object arg0) {
		if (arg0 instanceof AuditTrailEntryImpl) {
			AuditTrailEntryImpl other = (AuditTrailEntryImpl) arg0;
			return (other.id() == this.id());
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hashCode = element.hashCode();
		if (originator != null) {
			hashCode = (hashCode * 32) + originator.hashCode();
		}
		hashCode = (hashCode * 32) + eventType.hashCode();
		if (timestamp != null) {
			hashCode = (hashCode * 32) + timestamp.hashCode();
		}
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[ATE: " + element + ", " + eventType + ", " + timestamp + ", "
				+ originator + ", " + attributes.toString() + "]";
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
		return null; // not applicable!
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getName()
	 */
	public String getName() {
		return element;
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
	public void setDescription(String description) {
		// ignore!
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#setName(java.lang.String)
	 */
	public void setName(String name) {
		element = name;
	}

	public void setOriginatorModelReferences(
			List<String> originatorModelReferences) {
		this.originatorModelReferences = originatorModelReferences;
	}

	public void setTypeModelReferences(List<String> typeModelReferences) {
		this.typeModelReferences = typeModelReferences;
	}

	public void setElementModelReferences(List<String> elementModelReferences) {
		this.elementModelReferences = elementModelReferences;
	}

	public List<String> getModelReferences() {
		return EMPTY_LIST;
	}

	public List<String> getElementModelReferences() {
		return elementModelReferences == null ? EMPTY_LIST : Collections
				.unmodifiableList(elementModelReferences);
	}

	public List<String> getOriginatorModelReferences() {
		return originatorModelReferences == null ? EMPTY_LIST : Collections
				.unmodifiableList(originatorModelReferences);
	}

	public List<String> getTypeModelReferences() {
		return typeModelReferences == null ? EMPTY_LIST : Collections
				.unmodifiableList(typeModelReferences);
	}

	public void setModelReferences(List<String> modelReferences) {
		// empty on purpose
	}
}
