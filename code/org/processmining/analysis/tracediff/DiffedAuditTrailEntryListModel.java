package org.processmining.analysis.tracediff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractListModel;

import org.processmining.framework.log.AuditTrailEntryList;

/**
 * List model building up the diff of two traces in the same list. Each element
 * in the list consists of two aligned audit trail entries and their type of
 * difference at this position.
 * 
 * @see TraceDiffCellRenderer
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public class DiffedAuditTrailEntryListModel extends AbstractListModel {

	protected AuditTrailEntryList leftTrace;
	protected AuditTrailEntryList rightTrace;
	protected Diff diff;

	protected TraceDiff leftDiff;
	protected TraceDiff rightDiff;
	protected int maxIndex;

	/**
	 * Constructor taking additional attributes relating to diffed traces.
	 * 
	 * @param left
	 *            the 'from' trace in the comparison
	 * @param right
	 *            the 'to' trace in the comparison
	 * @param aDiff
	 *            the diff object from the longest subsequence analysis
	 */
	public DiffedAuditTrailEntryListModel(AuditTrailEntryList left,
			AuditTrailEntryList right, Diff aDiff) {
		leftTrace = left;
		rightTrace = right;
		diff = aDiff;
		leftDiff = new LeftTraceDiff(leftTrace, diff);
		rightDiff = new RightTraceDiff(rightTrace, diff);
		maxIndex = Math.max(leftDiff.getGlobalIndex(leftTrace.size() - 1),
				rightDiff.getGlobalIndex(rightTrace.size() - 1));
		leftDiff.buildReversedMapping(maxIndex);
		rightDiff.buildReversedMapping(maxIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		return new EventDiff(leftDiff.getElementAt(index), rightDiff
				.getElementAt(index), leftDiff.elementDiffed(index), rightDiff
				.elementDiffed(index), leftDiff.getOriginalIndex(index),
				rightDiff.getOriginalIndex(index));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return maxIndex + 1;
	}

	/**
	 * Retrieves the global positions at which a difference starts.
	 * 
	 * @return the list of change start positions
	 */
	public List<Integer> getDiffIndicees() {
		HashSet<Integer> result = new HashSet<Integer>();
		result.addAll(leftDiff.getDiffPositions());
		result.addAll(rightDiff.getDiffPositions());
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(result);
		Collections.sort(list);
		return list;
	}

}