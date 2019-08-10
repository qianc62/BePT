package org.processmining.analysis.clustering.model;

import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ModelElements;
import org.processmining.framework.log.ProcessInstance;

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
public class LogSequence {

	private LogSummary summary = null;
	private LogReader log = null;
	private int numOfModelElements = 0;
	private int totalNumOfModelElements = 0;

	public TreeMap hm = new TreeMap(); // Model Elements -> index
	public String[] actName; // index -> Activity Name
	public String[] actChar; // index -> Coding Char
	public TreeMap phm = new TreeMap(); // pi Sequence -> frequency
	public TreeMap phm_index = new TreeMap(); // pi Sequence -> index
	// public String[] sampleLogId; //index -> Coding Char

	public String[] piSeq = null; // all pi Sequence

	public String[] proc;
	public int[] proc_freq;
	public int numOfProcess;

	public LogSequence(LogReader log) {
		this.log = log;
		this.summary = log.getLogSummary();

		String[] str = summary.getModelElements();
		numOfModelElements = str.length;

		encodeActivities();
		makeDepGraphs();

		Set s = phm.keySet();
		Object[] keyA = s.toArray();
		numOfProcess = keyA.length;

		proc = new String[numOfProcess];
		for (int i = 0; i < keyA.length; i++) {
			proc[i] = (String) keyA[i];
		}

		proc_freq = new int[numOfProcess];
		for (int i = 0; i < numOfProcess; i++) {
			proc_freq[i] = ((Integer) phm.get(proc[i])).intValue();
		}
	}

	// Activity names are mapped to integers and characters.
	private void encodeActivities() {
		String[] strA = summary.getModelElements();
		actName = new String[strA.length];
		actChar = new String[strA.length];
		for (int i = 0; i < strA.length; i++) {
			hm.put(strA[i].trim(), new Integer(i));
			actName[i] = strA[i].trim();
			actChar[i] = "" + (char) (i + 33);
		}
	}

	private void makeDepGraphs() {

		int piIndex = 0;
		piSeq = new String[summary.getNumberOfProcessInstances()];

		while (log.hasNext()) {
			ProcessInstance pi = log.next();
			ModelElements mo = pi.getModelElements();

			int cur;
			while (mo.hasNext()) {
				cur = ((Integer) hm.get(mo.next().getName().trim())).intValue(); // Model
				// Elements
				// ->
				// index
				if (piSeq[piIndex] == null) {
					piSeq[piIndex] = "" + actChar[cur]; // pi Sequence encoding
				} else {
					piSeq[piIndex] = piSeq[piIndex] + actChar[cur];
				}
			}
			putPItoTreeMap(piIndex); // pi encoding => map
			piIndex++;
		}

	}

	private void putPItoTreeMap(int piIndex) {
		String seq = piSeq[piIndex].trim();
		Integer i = (Integer) phm.get(seq);

		Vector v = new Vector();
		if (i == null) {
			phm.put(seq, new Integer(1)); // piSeq -> frequency
		} else {
			phm.put(seq, new Integer(i.intValue() + 1));
			v = (Vector) phm_index.get(seq);
		}
		v.add(new Integer(piIndex));
		phm_index.put(seq, v); // piSeq -> index
	}

	public int getFrequencyOfProcSequence(String seq) {
		Integer i = (Integer) phm.get(seq.trim());

		if (i == null) {
			return 0;
		}
		return i.intValue();
	}

	public String[][] getProcSeqeunceTable() {
		String[][] table = new String[proc.length][4];
		for (int i = 0; i < proc.length; i++) {
			table[i][0] = "S" + i;
			table[i][1] = proc[i];
			table[i][2] = "" + proc_freq[i];
			/*
			 * Vector v = (Vector) phm_index.get(proc[i]); String s = "";
			 * for(int j=0; j<v.size(); j++){ s += v.get(j).toString();
			 * if(j!=v.size()) s+=", "; }
			 */table[i][3] = "" + phm_index.get(proc[i]);
		}
		return table;
	}

	public String[][] getActSeqeunceTable() {
		String[][] table = new String[actName.length][2];
		for (int i = 0; i < actName.length; i++) {
			table[i][0] = actChar[i];
			table[i][1] = actName[i];
		}
		return table;
	}

	private String printMap(TreeMap m) {
		String str = "";

		Set s = m.keySet();
		Object[] keyA = s.toArray();
		for (int i = 0; i < m.size(); i++) {
			String keyStr = keyA[i].toString();
			int f = ((Integer) m.get(keyStr)).intValue();
			str = str + keyStr + " = " + f + "<br>";
		}
		return str;
	}
}
