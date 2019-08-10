package org.processmining.analysis.performance.sequence;

import java.util.Date;

/**
 * Represents the transfer of work between two data-element instances in a
 * sequence.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class SequenceArrow extends Arrow {
	/**
	 * Timestamp at which the transfer of work begins
	 */
	private Date beginTimestamp;
	/**
	 * Timestamp at which the transfer of work ends
	 */
	private Date endTimestamp;

	/**
	 * Constructor
	 * 
	 * @param beginTimestamp
	 *            Date
	 * @param endTimestamp
	 *            Date
	 * @param source
	 *            String
	 * @param destination
	 *            String
	 */
	public SequenceArrow(Date beginTimestamp, Date endTimestamp, String source,
			String destination) {
		super(source, destination);
		this.beginTimestamp = beginTimestamp;
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Returns the duration of the transfer of work
	 * 
	 * @return long
	 */
	public long getTimeIn() {
		if (beginTimestamp != null && endTimestamp != null) {
			return (endTimestamp.getTime() - beginTimestamp.getTime());
		} else {
			return 0;
		}
	}

	/**
	 * Returns the begin timestamp of this block
	 * 
	 * @return Date
	 */
	public Date getBeginTimestamp() {
		return beginTimestamp;
	}

	/**
	 * @param beginTimestamp
	 *            Date
	 */
	public void setBeginTimestamp(Date beginTimestamp) {
		this.beginTimestamp = beginTimestamp;
	}

	/**
	 * Returns the end timestamp of this block
	 * 
	 * @return Date
	 */
	public Date getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * @param endTimestamp
	 *            Date
	 */
	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Checks whether an arrow is equal to another one
	 * 
	 * @param obj
	 *            Object
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		if (obj instanceof SequenceArrow) {
			SequenceArrow other = (SequenceArrow) obj;
			try {
				if (other.getBeginTimestamp() == this.getBeginTimestamp()
						&& other.getEndTimestamp() == this.getEndTimestamp()
						&& other.getSource().equals(this.getSource())
						&& other.getDestination().equals(this.getDestination())) {
					return true;
				} else {
					return false;
				}
			} catch (NullPointerException ex) {
				return false;
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do
	}
}
