package org.processmining.framework.util;

/**
 * This class stores the repeat information viz., the starting position of the
 * repeat, ending position and the repeat itself
 * 
 * @author jcbose (R. P. Jagadeesh Chandra 'JC' Bose)
 */
public class RepeatsInfo {
	int startPos;
	int endPos;
	String repeat;

	public RepeatsInfo(int start, int end, String repeat) {
		this.startPos = start;
		this.endPos = end;
		this.repeat = repeat;
	}

	public int getStartPos() {
		return startPos;
	}

	public int getEndPos() {
		return endPos;
	}

	public String getRepeat() {
		return repeat;
	}
}
