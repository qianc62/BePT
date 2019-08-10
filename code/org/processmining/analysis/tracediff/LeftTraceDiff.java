package org.processmining.analysis.tracediff;

import org.processmining.framework.log.AuditTrailEntryList;

/**
 * Implements the left side of the diffed log trace view by interpreting the
 * difference objects correspondingly.
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public class LeftTraceDiff extends TraceDiff {

	/**
	 * Creates an audit trail entry list that can be displayed in the gui,
	 * whereas the original indicees are translated into global indicees
	 * assuming that this trace corresponds to the 'from' side of the
	 * comparison.
	 * 
	 * @param list
	 *            the original list of audit trail entries to be displayed
	 * @param diff
	 *            the diff object performing the 'longest subsequence' analysis
	 *            as in standard diff
	 */
	public LeftTraceDiff(AuditTrailEntryList list, Diff diff) {
		super(list, diff);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.tracediff.DiffedAuditTrailEntryListModel#
	 * getDiffStart(org.processmining.analysis.tracediff.Difference)
	 */
	protected int getDiffStart(Difference difference) {
		return difference.getDeletedStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.tracediff.DiffedAuditTrailEntryListModel#
	 * getDiffEnd(org.processmining.analysis.tracediff.Difference)
	 */
	protected int getDiffEnd(Difference difference) {
		return difference.getDeletedEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.tracediff.DiffedAuditTrailEntryListModel#getOffset
	 * (org.processmining.analysis.tracediff.Difference)
	 */
	protected int getOffset(Difference difference) {
		int offset;
		if (difference.getAddedEnd() != -1) {
			offset = difference.getAddedEnd() - difference.getAddedStart();
			if (difference.getDeletedEnd() != -1) { // subtract deletion if
				// present
				offset = offset
						- (difference.getDeletedEnd() - difference
								.getDeletedStart());
				if (offset < 0) {
					offset = 0;
				}
			} else {
				offset++; // if not equal deletion and addition, then the pure
				// addition needs to be counted
			}
		} else {
			offset = 0; // no offset needed if only deletion
		}
		return offset;
	}

}
