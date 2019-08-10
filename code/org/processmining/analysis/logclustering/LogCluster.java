package org.processmining.analysis.logclustering;

import java.util.HashSet;
import java.util.Iterator;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class LogCluster {

	protected HashSet<Integer> traces;

	// protected double[] center;

	public LogCluster() {
		traces = new HashSet<Integer>();
	}

	public void addTraceStat(int stat) {
		traces.add(Integer.valueOf(stat));
	}

	public void addTraceStat(Integer stat) {
		traces.add(stat);
	}

	public void removeTraceStat(int stat) {
		traces.remove(Integer.valueOf(stat));
	}

	public HashSet<Integer> getTraces() {
		return traces;
	}

	public boolean containTrace(int a) {
		boolean bResult = false;
		bResult = traces.contains(Integer.valueOf(a));
		return bResult;
	}

	public boolean containTrace(Integer a) {
		boolean bResult = false;
		bResult = traces.contains(a);
		return bResult;
	}

	public int size() {
		return traces.size();
	}

	/*
	 * public void getCetner(double[] center) { this.center = center; }
	 * 
	 * 
	 * public double[] getCetner() { return center; }
	 */
	public Object clone() {
		LogCluster obj = new LogCluster();
		Iterator itr = traces.iterator();
		while (itr.hasNext()) {
			obj.addTraceStat((Integer) itr.next());
		}
		return obj;
	}

}
