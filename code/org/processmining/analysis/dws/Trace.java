package org.processmining.analysis.dws;

import java.io.IOException;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.models.heuristics.HeuristicsNet;

/**
 * Defines internal data structures and methods for optimizing the clustering of
 * instances.
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 */

public class Trace implements Comparable {

	private int[] sequence; // sequenza di IDs
	private HeuristicsNet en;

	public Trace(Trace tlen, Trace t2) {
		sequence = new int[tlen.size() + 1];
		for (int i = 0; i < tlen.size(); i++) {
			sequence[i] = tlen.sequence[i];
		}
		sequence[sequence.length - 1] = t2.sequence[1];
		this.en = tlen.en;
	}

	public Trace(int[] a, HeuristicsNet en) {
		sequence = a;
		this.en = en;
	}

	/**
	 * Build a trace
	 * 
	 * @param ates
	 */
	public Trace(AuditTrailEntryList ates, HeuristicsNet en) {
		int dim = ates.size();
		if (dim == 0) {
			return;
		}
		sequence = new int[dim];

		for (int i = 0; i < ates.size(); i++) {
			try {
				int index = getIndexATE(ates.get(i), en);
				sequence[i] = index;
			} catch (IOException ioe) {
				// do nothing.
			}

		}
		this.en = en;
	}

	/*
	 * Restituisce l'indice associato ad un certo AuditTrailEntry FORSE DA
	 * MODIFICARE NEL CASO DI DuplicateTaskHeuristicsNet
	 */
	private int getIndexATE(AuditTrailEntry ate, HeuristicsNet en) {
		LogEvents logEvents = en.getLogEvents();
		return logEvents.findLogEventNumber(ate.getElement(), ate.getType());
	}

	public int size() {
		return sequence.length;
	}

	private int lastElementInTrace() {
		return sequence[sequence.length - 1];
	}

	private int fisrtElementInTrace() {
		return sequence[0];
	}

	public boolean contains(Trace t) {
		int iThis = 0;
		for (int i = 0; i < t.sequence.length; i++) {
			while (iThis < sequence.length && sequence[iThis] != t.sequence[i]) {
				iThis++;
			}
			if (iThis == sequence.length) {
				return false;
			}
		}
		return true;
	}

	public boolean concatenable(Trace t) {
		return lastElementInTrace() == t.fisrtElementInTrace();
	}

	public int overlap(Trace t) {
		int occurrence = 0;
		for (int j = 0; j < t.size(); j++) {
			boolean trovato = false;
			for (int i = 0; i < sequence.length && !trovato; i++) {
				if (sequence[i] == t.sequence[j]) {
					occurrence++;
					trovato = true;
				}
			}
		}
		return occurrence;
	}

	public double overlapDecaying(Trace t) {
		double occurrence = 0;
		for (int j = 0; j < t.size(); j++) {
			boolean trovato = false;
			for (int i = 0; i < sequence.length && !trovato; i++) {
				if (sequence[i] == t.sequence[j]) {
					occurrence += Math.pow(t.size(), t.size() - j + 1);
					trovato = true;
				}
			}
		}
		return occurrence;
	}

	public String toString() {
		String ris = "";
		for (int i = 0; i < sequence.length - 1; i++) {
			ris += en.getLogEvents().getEvent(sequence[i])
					.getModelElementName()
					+ ",";
		}
		ris += en.getLogEvents().getEvent(sequence[sequence.length - 1])
				.getModelElementName();
		return ris;
	}

	public Trace getBodyForFeature() {
		int[] h = new int[size() - 1];
		for (int i = 0; i < size() - 1; i++) {
			h[i] = sequence[i];
		}
		return new Trace(h, en);
	}

	public Trace getHeadForFeature() {
		int[] h = new int[1];
		h[0] = sequence[size() - 1];
		return new Trace(h, en);
	}

	public Trace concat(Trace t) {
		int[] s = new int[sequence.length + t.size()];
		for (int i = 0; i < sequence.length; i++) {
			s[i] = sequence[i];
		}
		for (int i = sequence.length; i < sequence.length + t.size(); i++) {
			s[i] = t.sequence[i - sequence.length];
		}
		return new Trace(s, en);
	}

	public int compareTo(Object o) {
		Trace t = (Trace) o;
		int l1 = this.size();
		int l2 = t.size();
		int ris = 0;
		if (l1 != l2) {
			ris = l1 - l2;
		}
		for (int i = 0; i < this.size() && ris == 0; i++) {
			if (en.getLogEvents().getEvent(sequence[i]).getModelElementName()
					.compareTo(
							en.getLogEvents().getEvent(t.sequence[i])
									.getModelElementName()) < 0) {
				ris = -1;
			}
			if (en.getLogEvents().getEvent(sequence[i]).getModelElementName()
					.compareTo(
							en.getLogEvents().getEvent(t.sequence[i])
									.getModelElementName()) > 0) {
				ris = 1;
			}
		}
		// System.out.println(this+" vs "+t+":"+ris);
		return ris;
	}

	public boolean equals(Object o) {
		Trace t = (Trace) o;
		boolean verify = true;
		if (this.size() != t.size())
			return false;
		for (int i = 0; i < sequence.length; i++)
			if (sequence[i] != t.sequence[i])
				verify = false;
		return verify;
	}

	public boolean extend(Trace t) {
		// TODO ANTO 16-3-2007
		if (t.size() >= this.size())
			return false;
		int inizio;
		for (inizio = 0; inizio < this.size()
				&& (this.sequence[inizio] != t.sequence[0]); inizio++)
			;
		boolean verify = true;
		if (inizio == this.size() || this.size() - inizio < t.size())
			verify = false;
		else {
			for (int i = 0; i < t.size() && verify; i++)
				if (t.sequence[i] != this.sequence[i + inizio])
					verify = false;
		}
		return verify;
	}

	// TODO ANTO
	public int getLastElementInTrace() {
		return sequence[sequence.length - 1];
	}

	public boolean onlyOneOutput(int i) {
		return this.en.getNumberOutputSet(i) == 1;
	}
	// FINE TODO

}
