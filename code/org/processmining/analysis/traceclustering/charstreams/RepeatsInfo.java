package org.processmining.analysis.traceclustering.charstreams;

/**
 * @author R. P. Jagadeesh Chandra Bose
 * 
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
