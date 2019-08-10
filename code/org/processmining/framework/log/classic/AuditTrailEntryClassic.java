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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;

/**
 * A single audit trail entry in a workflow log.
 * <p>
 * This class only provides some simple getter methods to get the information in
 * an audit trail entry.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class AuditTrailEntryClassic extends AuditTrailEntry implements
		Cloneable {

	private String element;
	private String type;
	private Date timestamp;
	private String originator;
	private DataSection data;

	/**
	 * 
	 * @param element
	 *            String
	 * @param type
	 *            String
	 * @param timestamp
	 *            Date
	 * @param originator
	 *            String
	 * @param data
	 *            HashMap
	 */
	private void construct(String element, String type, Date timestamp,
			String originator, Map<String, String> data) {
		this.element = (element == null ? "" : element.trim());
		this.type = (type == null ? "" : type.trim());
		this.originator = (originator == null ? "" : originator);
		this.timestamp = timestamp;
		this.data = new DataSection();
		if (data != null) {
			this.data.putAll(data);
		}
	}

	/**
	 * 
	 * @param element
	 *            String
	 * @param type
	 *            String
	 * @param timestamp
	 *            Date
	 * @param originator
	 *            String
	 * @param data
	 *            HashMap
	 */
	public AuditTrailEntryClassic(String element, String type, Date timestamp,
			String originator, Map<String, String> data) {
		construct(element, type, timestamp, originator, data);

	}

	/**
	 * 
	 * @param element
	 *            String
	 * @param type
	 *            String
	 * @param timestamp
	 *            String
	 * @param originator
	 *            String
	 * @param data
	 *            Map
	 */
	public AuditTrailEntryClassic(String element, String type,
			String timestamp, String originator, Map<String, String> data) {

		// SimpleDateFormat dateParser = new
		// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		Date d = null;
		if (timestamp.length() >= "yyyy-MM-ddTHH:mm:ss".length()) {

			String format = "yyyy-MM-dd'T'HH:mm:ss";
			String end = timestamp.substring("yyyy-MM-ddTHH:mm:ss".length());
			timestamp = timestamp.substring(0, "yyyy-MM-ddTHH:mm:ss".length());
			if (!end.equals("")) {
				// end now contains all optional elements, such as:
				// milliseconds and/or
				// timezone
				int ms = end.indexOf(".");
				int tz = end.indexOf("-");
				if (tz == -1) {
					tz = end.indexOf("+");
				}
				if (tz == -1) {
					tz = end.indexOf("Z");

				}
				int mse = 0;

				if (ms != -1) {
					// We have a milliseconds part
					mse = tz;
					if (mse == -1) {
						// no timezone
						mse = end.length();
					}
					format += ".SSS";
					timestamp += ".";
					for (int i = 0; i < (mse - ms - 1); i++) {
						if (i > 2) {
							format += "S";
						}
						timestamp += end.charAt(i + 1);
					}
					for (int i = 0; i < 3 - (mse - ms - 1); i++) {
						timestamp += "0";
					}

				}
				if (tz != -1) {
					// There is a timezone
					// mse is the first index of the timezone part
					String timezone = end.substring(mse);

					if (timezone.length() == 1) {
						// Timezone = 'Z'
						timezone = "+00:00";
					}
					timestamp += "GMT" + timezone;

					format += "z";

				}
			}
			SimpleDateFormat dateParser = new SimpleDateFormat(format);
			d = dateParser.parse(timestamp == null ? "" : timestamp,
					new ParsePosition(0));
		}
		construct(element, type, d, originator, data);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryIF#getElement()
	 */
	public String getElement() {
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryIF#getType()
	 */
	public String getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryIF#getTimestamp()
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryIF#getOriginator()
	 */
	public String getOriginator() {
		return originator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryIF#getData()
	 */
	public DataSection getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryIF#toString()
	 */
	public String toString() {
		return "[ATE: " + element + ", " + type + ", " + timestamp + ", "
				+ originator + ", " + data.toString() + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryIF#equals(java.lang.Object
	 * )
	 */
	public boolean equals(Object o) {
		// check object identity first
		if (this == o) {
			return true;
		}
		// check type (which includes check for null)
		if (o instanceof AuditTrailEntryClassic == false) {
			return false;
		} else if (!element.equals(((AuditTrailEntryClassic) o).element)
				|| !type.equals(((AuditTrailEntryClassic) o).type)
				|| !originator.equals(((AuditTrailEntryClassic) o).originator)
				|| !data.equals(((AuditTrailEntryClassic) o).data)) {
			return false;
		}
		// timestamp can be null!
		else if ((timestamp != null)
				&& (((AuditTrailEntryClassic) o).timestamp != null)) {
			return timestamp.equals(((AuditTrailEntryClassic) o).timestamp);
		} else if (((AuditTrailEntryClassic) o).timestamp == null) {
			// both are null, which is ok
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryIF#hashCode()
	 */
	public int hashCode() {
		// simple recipe for generating hashCode given by
		// Effective Java (Addison-Wesley, 2001)
		int result = 17;
		result = 37 * result + element.hashCode();
		result = 37 * result + type.hashCode();
		result = 37 * result + originator.hashCode();
		// timestamp can be null!
		if (timestamp != null) {
			result = 37 * result + timestamp.hashCode();
		}
		result = 37 * result + data.hashCode();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryIF#clone()
	 */
	public Object clone() {
		AuditTrailEntryClassic o = new AuditTrailEntryClassic(element, type,
				timestamp, originator, data);
		// clone referenced objects to realize deep copy
		if (timestamp != null) {
			o.timestamp = (Date) timestamp.clone();
		}
		if (data != null) {
			/**
			 * @todo: check whether it is safe to assume an underlying HashMap.
			 *        (cast necessary as Map itself does not provide a clone
			 *        method)
			 */
			o.data = (DataSection) data.clone();
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryIF#addDataAttribute(java
	 * .lang.String, java.lang.String)
	 */
	public void addDataAttribute(String key, String value) {
		data.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryIF#setData(java.util.Map)
	 */
	public void setData(Map<String, String> data) {
		this.data = new DataSection();
		if (data != null) {
			this.data.putAll(data);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryIF#setElement(java.lang
	 * .String)
	 */
	public void setElement(String element) {
		this.element = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryIF#setOriginator(java.
	 * lang.String)
	 */
	public void setOriginator(String originator) {
		this.originator = originator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryIF#setTimestamp(java.util
	 * .Date)
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryIF#setType(java.lang.String
	 * )
	 */
	public void setType(String type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryIF#id()
	 */
	public long id() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getAttributes()
	 */
	public DataSection getDataAttributes() {
		return getData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return getData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getName()
	 */
	public String getName() {
		return getElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#removeAttribute(java.lang.String
	 * )
	 */
	public void removeAttribute(String key) {
		data.remove(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttribute(java.lang.String,
	 * java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		data.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#setName(java.lang.String)
	 */
	public void setName(String name) {
		setElement(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogEntity#getDescription()
	 */
	public String getDescription() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setDataAttributes(DataSection)
	 */
	public void setDataAttributes(DataSection map) {
		data = new DataSection(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogEntity#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map<String, String> map) {
		data = DataSection.fromMap(map);
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

	public List<String> getModelReferences() {
		return new ArrayList<String>(0);
	}

	public List<String> getElementModelReferences() {
		return new ArrayList<String>(0);
	}

	public List<String> getOriginatorModelReferences() {
		return new ArrayList<String>(0);
	}

	public List<String> getTypeModelReferences() {
		return new ArrayList<String>(0);
	}

	public void setElementModelReferences(List<String> modelReferences) {
		// empty on purpose
	}

	public void setOriginatorModelReferences(List<String> modelReferences) {
		// empty on purpose
	}

	public void setTypeModelReferences(List<String> modelReferences) {
		// empty on purpose
	}

	public void setModelReferences(List<String> modelReferences) {
		// empty on purpose
	}
}
