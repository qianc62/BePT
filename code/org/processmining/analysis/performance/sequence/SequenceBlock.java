package org.processmining.analysis.performance.sequence;

import java.util.Date;

/**
 * Represents a period of activity of a data-element instance in a sequence.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class SequenceBlock extends DataElementBlock {
	private Date beginTimestamp;
	private Date endTimestamp;

	/**
	 * Constructor that initializes the block
	 * 
	 * @param beginTimestamp
	 *            Date
	 * @param endTimestamp
	 *            Date
	 * @param dataElement
	 *            String
	 */
	public SequenceBlock(Date beginTimestamp, Date endTimestamp,
			String dataElement) {
		super(dataElement);
		this.beginTimestamp = beginTimestamp;
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Returns the duration of the period of activity
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
	 * Sets the begin timestamp. Needed to be able to combine blocks/periods
	 * that overlap
	 * 
	 * @param beginTimestamp
	 *            Date
	 */
	public void setBeginTimestamp(Date beginTimestamp) {
		this.beginTimestamp = beginTimestamp;
	}

	/**
	 * Returns the end timestamp. Needed to be able to combine blocks/periods
	 * that overlap
	 * 
	 * @return Date
	 */
	public Date getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * Sets the end timestamp
	 * 
	 * @param endTimestamp
	 *            Date
	 */
	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
}
