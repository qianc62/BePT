package org.processmining.analysis.tracediff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;

/**
 * Class mapping the differences obtained from the diff operation to audit trail
 * entries in the given trace. Interpretation depends on the side of the
 * comparison in diff ('to' or 'from').
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public abstract class TraceDiff {

	protected AuditTrailEntryList ateList;
	protected Diff diff;

	protected int[] mapped; // the original indicees mapped to the global
	// indicees
	protected boolean[] diffed; // the original indicees whether part of diff
	// (true) or not (false)
	protected int[] reverse; // the global indicess mapped to the original
	// indicees (-1 if not present)
	protected ArrayList<Integer> diffs; // the start of diffs in terms of global

	// indices

	public TraceDiff(AuditTrailEntryList list, Diff diffResult) {
		ateList = list;
		diff = diffResult;
		mapped = new int[ateList.size()];
		diffed = new boolean[ateList.size()];
		for (int i = 0; i < diffed.length; i++) { // init needed?
			diffed[i] = false;
		}
		diffs = new ArrayList<Integer>();
		initMapping();
	}

	/**
	 * Returns the diff positions at mapped indicees.
	 * 
	 * @return an array containing the positions of each difference in the
	 *         global index
	 */
	public List<Integer> getDiffPositions() {
		return diffs;
	}

	/**
	 * Retrieves original Audit trail entry given the global index.
	 * 
	 * @param index
	 *            the global position
	 * @return the original audit trail entry belonging to this position
	 */
	public AuditTrailEntry getElementAt(int index) {
		try {
			if (reverse[index] != -1) {
				return ateList.get(reverse[index]);
			} else {
				return null;
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Tests whether the original event at the given index was part of a
	 * difference in the diff.
	 * 
	 * @param index
	 *            the global position to be checked
	 * @return whether the event was part of difference or not
	 */
	public boolean elementDiffed(int index) {
		if (reverse[index] != -1) {
			return diffed[reverse[index]];
		} else {
			return false;
		}
	}

	/**
	 * Retrieves the global indix for the given original index.
	 * 
	 * @param origIndex
	 *            the index in the undiffed trace
	 * @return the index in the diffed view for this element
	 */
	public int getGlobalIndex(int origIndex) {
		return mapped[origIndex];
	}

	/**
	 * Retrieve the original event index for the global position.
	 * 
	 * @param index
	 *            global index
	 * @return the original trace index for the event
	 */
	public int getOriginalIndex(int index) {
		return reverse[index];
	}

	// ///////////////////////////////////////////////////////

	/**
	 * Initializes the mapping from original to global indicees.
	 */
	protected void initMapping() {
		int last = 0; // init
		mapped[0] = 0; // init
		int offset = 0; // diff offset from last difference
		for (Difference difference : diff.getDiffs()) {
			// fill indicees between last and current (no difference there)
			int current = getDiffStart(difference);
			int first = last + 1;
			for (int i = first; i < current; i++) {
				if (i == first) {
					mapped[last + 1] = mapped[last] + 1 + offset;
				} else {
					mapped[i] = mapped[i - 1] + 1;
				}
				last = i;
			}
			// get offset
			offset = getOffset(difference);
			// assign indecees in diffed subsequence
			int end = getDiffEnd(difference);
			for (int i = current; i <= end; i++) {
				if (i != 0) {
					mapped[i] = mapped[i - 1] + 1; // starts with current
				} else {
					mapped[i] = 0; // if very first no previous exists
				}
				diffed[i] = true; // this event is part of a diffed sequence!
				last = i;
			}
			if (end != -1) {
				// store difference
				diffs.add(new Integer(mapped[current]));
			}
		}
		// map the entries after the last difference
		int first = last + 1;
		for (int i = first; i < ateList.size(); i++) {
			if (i == first) {
				mapped[last + 1] = mapped[last] + 1 + offset;
			} else {
				mapped[i] = mapped[i - 1] + 1;
			}
			last = i;
		}
	}

	/**
	 * Builds the reverse mapping from global indecees to orginal trace
	 * indicees.
	 */
	public void buildReversedMapping(int maxIndex) {
		// get mapped index of last ate and add 1 to get the size (since
		// starting from 0)
		int length = maxIndex + 1;
		reverse = new int[length];
		int prevOffset = 0; // hung-over offset
		for (int i = 0; i < mapped.length; i++) {
			reverse[mapped[i]] = i;
			int offset = mapped[i] - i;
			if (offset >= prevOffset) {
				offset = offset - prevOffset;
			}
			for (int j = 1; j <= offset; j++) {
				reverse[mapped[i] - j] = -1;
			}
			prevOffset = prevOffset + offset;
		}
		int lastMapped = mapped[mapped.length - 1];
		for (int i = 1; i < reverse.length - lastMapped; i++) {
			reverse[lastMapped + i] = -1;
		}
	}

	/**
	 * Fetches the start index of the difference in original trace.
	 * 
	 * @param difference
	 *            the difference for which the start is to be determined
	 * @return the index that corresponds to the start of the difference in the
	 *         original sequence
	 */
	protected abstract int getDiffStart(Difference difference);

	/**
	 * Fetches the end index of the difference in original trace.
	 * 
	 * @param difference
	 *            the difference for which the end is to be determined
	 * @return the index that corresponds to the end of the difference in the
	 *         original sequence
	 */
	protected abstract int getDiffEnd(Difference difference);

	/**
	 * Obtains the offset between the changes made in the one original trace and
	 * the other original trace
	 * 
	 * @param difference
	 *            the difference for which the offset is to be determined
	 * @return 0 if equal or less changes are made in this trace compared to the
	 *         other trace, a number > 0 if more elements are affected in this
	 *         tracen than in the other trace.
	 */
	protected abstract int getOffset(Difference difference);

}
