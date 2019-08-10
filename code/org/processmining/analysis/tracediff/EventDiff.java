package org.processmining.analysis.tracediff;

import org.processmining.framework.log.AuditTrailEntry;

/**
 * Data class encapsulating the type of a difference between events at a given
 * global position.
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public class EventDiff {

	public enum DiffType {
		none, // no difference for this index
		both, // both different
		left, // additional event on left side
		right
		// additional event on right side
	}

	protected AuditTrailEntry left;
	protected AuditTrailEntry right;
	protected int leftIndex;
	protected int rightIndex;
	protected DiffType type;

	/**
	 * Creates an event difference object based on the given traces and diff
	 * info.
	 * 
	 * @param leftTrace
	 *            the event in the 'from' trace in the comparison (may be null)
	 * @param rightTrace
	 *            the event in the 'to' trace in the comparison (may be null)
	 * @param leftDiffed
	 *            whether there is an original event in the left trace that is
	 *            part of a difference (if
	 *            <code>false</> then either no difference
	 * or right-diffed at this position)
	 * @param rightDiffed
	 *            whether there is an original event on the right side that is
	 *            part of a difference (if <code>false</> then either no
	 *            difference or left-diffed at this position)
	 * @param leftInd
	 *            the index of the audit trail entry in the original left trace
	 *            (-1 if no original event is mapped to this position)
	 * @param rightInd
	 *            the index of the audit trail entry in the original right trace
	 *            (-1 if no original event is mapped to this position)
	 */
	public EventDiff(AuditTrailEntry leftTrace, AuditTrailEntry rightTrace,
			boolean leftDiffed, boolean rightDiffed, int leftInd, int rightInd) {
		left = leftTrace;
		right = rightTrace;
		// determine kinf of diff at this global index
		if (leftDiffed == false && rightDiffed == false) {
			type = DiffType.none;
		} else if (leftDiffed == false && rightDiffed == true) {
			type = DiffType.right;
		} else if (leftDiffed == true && rightDiffed == false) {
			type = DiffType.left;
		} else {
			type = DiffType.both;
		}
		// original indicees
		leftIndex = leftInd;
		rightIndex = rightInd;
	}

	/**
	 * Returns the event in the 'from' trace in the comparison (may be null).
	 * 
	 * @return the left audit trail entry at this global position, if it exists
	 */
	public AuditTrailEntry getLeft() {
		return left;
	}

	/**
	 * Returns the event in the 'to' trace in the comparison (may be null).
	 * 
	 * @return the right audit trail entry at this global position, if it exists
	 */
	public AuditTrailEntry getRight() {
		return right;
	}

	/**
	 * Determines whether difference is 'left', 'right', 'both', or 'none'.
	 * 
	 * @return type of difference at this global position.
	 */
	public DiffType getType() {
		return type;
	}

	/**
	 * The original index of the left audit trail entry.
	 * 
	 * @return the position in the original left trace, -1 if not exists
	 */
	public int getLeftIndex() {
		return leftIndex;
	}

	/**
	 * The original index of the right audit trail entry.
	 * 
	 * @return the position in the original right trace, -1 if not exists
	 */
	public int getRightIndex() {
		return rightIndex;
	}

}
