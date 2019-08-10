package org.processmining.mining.partialorderminingTimeUnit;

import org.processmining.framework.log.AuditTrailEntry;

public class AtePositionPiTuple {

	public AuditTrailEntry myAte;
	public int myPositionPi;

	public AtePositionPiTuple() {
		myAte = null;
		// first position in ATElist of pi = 0, so -1
		myPositionPi = -1;
	}

	public AtePositionPiTuple(AuditTrailEntry ate, int pos) {
		this();
		myAte = ate;
		if (pos > -1) {
			myPositionPi = pos;
		}
	}

	public void setAte(AuditTrailEntry ate) {
		myAte = ate;
	}

	public void setPosition(int pos) {
		myPositionPi = pos;
	}

	public AuditTrailEntry getAte() {
		return myAte;
	}

	public int getPositionPi() {
		return myPositionPi;
	}
}
