package org.processmining.analysis.dws;

import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.heuristics.HeuristicsNet;

/**
 * A feature for the clustering of the form head: body, where both body and head
 * are trace
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 * @see org.processmining.analysis.dws.Trace
 */

public class Feature implements Comparable {

	private Trace body;
	private Trace head;

	public Feature(Trace t) {
		body = t.getBodyForFeature();
		head = t.getHeadForFeature();
	}

	/**
	 * Check the occurrences in the log.
	 * 
	 * @param log
	 *            Log reader
	 * @param en
	 *            A HeursticsNet
	 * @return number of time features occurs in the log
	 */
	public int occurrences(LogReader log, HeuristicsNet en) {
		int occurrence = 0;

		Trace t = body.concat(head);
		Iterator logInstanceIterator = log.instanceIterator();
		while (logInstanceIterator.hasNext()) {
			ProcessInstance pi = (ProcessInstance) logInstanceIterator.next();
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			Trace tLog = new Trace(ates, en);
			if (tLog.contains(t)) {
				occurrence++;
			}
		}
		return occurrence;
	}

	/**
	 * @return Body of the feature.
	 */
	public Trace getBody() {
		return body;
	}

	/**
	 * @return Head of the feature.
	 */
	public Trace getHead() {
		return head;
	}

	public String toString() {
		return body + " -/-> " + head + "\t";
		// return head + " : "+ body+ " ;";
	}

	public int compareTo(Object o) {
		Feature f = (Feature) o;
		// System.out.println(this+"vs"+f);
		int h = head.compareTo(f.head);
		int b = body.compareTo(f.body);
		if (b != 0) {
			return b;
		}
		return h;
	}

	public boolean equals(Object o) {
		Feature f = (Feature) o;
		boolean h = head.equals(f.head);
		boolean b = body.equals(f.body);
		return h && b;
	}

	// TODO ANTO
	public boolean ridond(Feature t) {
		Trace conc = this.getBody().concat(this.getHead());
		int index = this.getBody().getLastElementInTrace();
		if (t.getBody().contains(conc) && this.getBody().onlyOneOutput(index))
			return true;
		return false;
	}

}
