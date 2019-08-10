package org.processmining.framework.log.classic;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;

/**
 * This class implements a lightweight proxy, which transparently maps the
 * actual <code>AuditTrailEntryList</code> interface to the legacy
 * <code>AuditTrailEntries</code> interface.
 * 
 * @see AuditTrailEntries
 * 
 * @author arozinat
 */

public class AuditTrailEntryListProxy implements AuditTrailEntryList {

	/**
	 * The proxied container
	 */
	private AuditTrailEntries myAtes = null;

	/**
	 * Creates a new proxy instance, backed by the provided audit trail entry
	 * list and with its iterator set to the specified position in the list.
	 * 
	 * @param ateList
	 * @param startPosition
	 */
	public AuditTrailEntryListProxy(AuditTrailEntries ateList) {
		myAtes = ateList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#size()
	 */
	public int size() {
		return myAtes.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#get(int)
	 */
	public AuditTrailEntry get(int index) throws IndexOutOfBoundsException,
			IOException {
		return myAtes.getEntry(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#iterator()
	 */
	public Iterator iterator() {
		return myAtes.toArrayList().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#append(org.processmining
	 * .framework.log.AuditTrailEntry)
	 */
	public int append(AuditTrailEntry ate) throws IOException {
		myAtes.add(ate);
		return myAtes.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#insert(org.processmining
	 * .framework.log.AuditTrailEntry, int)
	 */
	public void insert(AuditTrailEntry ate, int index)
			throws IndexOutOfBoundsException, IOException {
		myAtes.add(ate, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#insertOrdered(org
	 * .processmining.framework.log.AuditTrailEntry)
	 */
	public int insertOrdered(AuditTrailEntry ate) throws IOException {
		Date ts = ate.getTimestamp();
		if (ts == null) {
			// fall back to appending
			myAtes.add(ate, myAtes.size());
			return (myAtes.size() - 1);
		}
		Date curTs = null;
		for (int i = 0; i < myAtes.size(); i++) {
			curTs = myAtes.getEntry(i).getTimestamp();
			if (curTs == null) {
				// fall back to appending
				myAtes.add(ate, myAtes.size());
				return (myAtes.size() - 1);
			} else if (ts.compareTo(curTs) < 0) {
				// correct insert position found
				myAtes.add(ate, i);
				return i;
			}
		}
		// default to appending
		myAtes.add(ate, myAtes.size());
		return (myAtes.size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#remove(int)
	 */
	public AuditTrailEntry remove(int index) throws IndexOutOfBoundsException,
			IOException {
		// move iterator to index position
		myAtes.reset();
		int i = 0;
		while (myAtes.hasNext()) {
			AuditTrailEntry ate = myAtes.next();
			if (i == index) {
				myAtes.remove();
				return ate;
			}
			i = i + 1;
		}
		// should not happen if index is within bounds!
		throw new IndexOutOfBoundsException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#replace(org.processmining
	 * .framework.log.AuditTrailEntry, int)
	 */
	public AuditTrailEntry replace(AuditTrailEntry newAte, int index)
			throws IndexOutOfBoundsException, IOException {
		// move iterator to index position
		myAtes.reset();
		int i = 0;
		while (myAtes.hasNext()) {
			AuditTrailEntry oldAte = myAtes.next();
			if (i == index) {
				myAtes.remove(); // remove old entry
				myAtes.add(newAte, index); // add new entry
				return oldAte;
			}
			i = i + 1;
		}
		// should not happen if index is within bounds!
		throw new IndexOutOfBoundsException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#isTainted()
	 */
	public boolean isTainted() {
		return false; // classic ate list is not organized in such a way that it
		// could be tainted
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#consolidate()
	 */
	public boolean consolidate() throws IOException {
		return true; // classic ate list is not organized in such a way that it
		// could be consolidated
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#cloneInstance()
	 */
	public AuditTrailEntryList cloneInstance() throws IOException {
		return new AuditTrailEntryListProxy((AuditTrailEntries) myAtes.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#cleanup()
	 */
	public void cleanup() throws IOException {
		// classic ate list is not organized in such a way that it could be
		// cleaned up
	}
}
