/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.log.classic;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;

/**
 * A list of <code>AuditTrailEntry</code> objects connected to an iterator in
 * order to walk through them. This usually corresponds to one audit trail, or
 * trace, in the log (that is, an ordered sequence of activities that took place
 * for one process instance.
 * 
 * @author Peter van den Brand
 */
public class AuditTrailEntriesClassic implements AuditTrailEntries {

	private ArrayList entries; // the list of stored audit trail entries
	private Iterator iterator; // the iterator stepping through the list
	private int currentPosition = 0; // position stored in order to provide a

	// clone() method

	/**
	 * Constructor for an empty list of audit trail entries.
	 */
	public AuditTrailEntriesClassic() {
		this(null);
	}

	/**
	 * Constructor for a given initial list of audit trail entries.
	 * 
	 * @param entries
	 *            the initial list of entries
	 */
	public AuditTrailEntriesClassic(ArrayList entries) {
		this.entries = (entries == null ? new ArrayList() : entries);
		reset();
	}

	// ////////////////// ITERATOR-RELATED METHODS
	// //////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#hasNext()
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#next()
	 */
	public AuditTrailEntry next() {
		// keep position for clone method
		currentPosition++;
		return (AuditTrailEntry) iterator.next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#reset()
	 */
	public void reset() {
		iterator = entries.iterator();
		// keep position for clone method
		currentPosition = 0;
	}

	// ////////////////// LIST-RELATED METHODS
	// //////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#remove()
	 */
	public void remove() {
		iterator.remove();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntriesIF#add(org.processmining
	 * .framework.log.AuditTrailEntry)
	 */
	public void add(AuditTrailEntry ate) {
		entries.add(ate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntriesIF#add(org.processmining
	 * .framework.log.AuditTrailEntry, int)
	 */
	public void add(AuditTrailEntry ate, int pos) {
		entries.add(pos, ate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#toArrayList()
	 */
	public ArrayList toArrayList() {
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#first()
	 */
	public AuditTrailEntry first() {
		return (AuditTrailEntry) entries.get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#last()
	 */
	public AuditTrailEntry last() {
		return (AuditTrailEntry) entries.get(entries.size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#getEntry(int)
	 */
	public AuditTrailEntry getEntry(int n) {
		return (AuditTrailEntry) entries.get(n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#toString()
	 */
	public String toString() {
		return entries.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#size()
	 */
	public int size() {
		return entries.size();
	}

	// ///////////////////// CLONE METHODS
	// //////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntriesIF#cloneIteratorOnly()
	 */
	public AuditTrailEntries cloneIteratorOnly() {
		AuditTrailEntriesClassic newObject = new AuditTrailEntriesClassic(
				entries);
		// re-establish position of the iterator
		while (newObject.currentPosition < currentPosition) {
			newObject.next();
		}
		return newObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntriesIF#clone()
	 */
	public Object clone() {
		AuditTrailEntriesClassic newObject = null;
		try {
			newObject = (AuditTrailEntriesClassic) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// make deep copy of the elements in the list
		// TODO - check whether deep copy is indeed created (probably not)
		newObject.entries = (ArrayList) entries.clone();
		// make the iterator belong to the cloned list
		newObject.iterator = newObject.entries.iterator();
		// re-establish position of the iterator
		while (newObject.currentPosition < currentPosition) {
			newObject.next();
		}
		return newObject;
	}
}
