/**
 * Project: ProM HPLR
 * File: AuditTrailEntriesProxy.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 27, 2006, 9:38:59 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the
 *      names of its contributors may be used to endorse or promote
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.rfb;

import java.io.IOException;
import java.util.ArrayList;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;

/**
 * This class implements a lightweight proxy, which transparently maps the
 * legacy <code>AuditTrailEntries</code> interface to the actual
 * <code>AuditTrailEntryList</code> interface.
 * 
 * @author Christian W. Guenther (christian at deckfour dot com)
 * 
 */
public class AuditTrailEntriesProxy implements AuditTrailEntries {

	/**
	 * The proxied container
	 */
	protected AuditTrailEntryList ates = null;
	/**
	 * Memory index for iterator functionality
	 */
	protected int position = 0;
	/**
	 * Flag to ensure at most one removal per invocation of <code>next()</code>.
	 */
	protected boolean canRemove = false;

	/**
	 * Creates a new proxy instance, backed by the provided audit trail entry
	 * list and with its iterator set to the specified position in the list.
	 * 
	 * @param ateList
	 * @param startPosition
	 */
	public AuditTrailEntriesProxy(AuditTrailEntryList ateList, int startPosition) {
		ates = ateList;
		position = startPosition;
		if (position > ateList.size()) {
			System.err
					.println("AuditTrailEntriesProxy instantiated with erroneous start position (>size)!");
		}
		canRemove = (startPosition > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntries#add(org.processmining
	 * .framework.log.AuditTrailEntry)
	 */
	public void add(AuditTrailEntry ate) {
		try {
			ates.append(ate);
		} catch (IOException e) {
			System.err.println("Serious error in AuditTrailEntriesProxy:");
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntries#add(org.processmining
	 * .framework.log.AuditTrailEntry, int)
	 */
	public void add(AuditTrailEntry ate, int pos) {
		try {
			ates.insert(ate, pos);
		} catch (IOException e) {
			System.err.println("Serious error in AuditTrailEntriesProxy:");
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntries#cloneIteratorOnly()
	 */
	public AuditTrailEntries cloneIteratorOnly() {
		return new AuditTrailEntriesProxy(ates, position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntries#first()
	 */
	public AuditTrailEntry first() {
		if (ates.size() == 0) {
			return null;
		} else {
			try {
				return ates.get(0);
			} catch (IOException e) {
				System.err.println("Serious error in AuditTrailEntriesProxy:");
				e.printStackTrace();
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntries#getEntry(int)
	 */
	public AuditTrailEntry getEntry(int n) {
		try {
			if (n <= ates.size()) {
				return ates.get(n);
			} else {
				System.err.println("Serious error in AuditTrailEntriesProxy:"
						+ " getEntry() called with invalid index!");
				return null;
			}
		} catch (IOException e) {
			System.err.println("Serious error in AuditTrailEntriesProxy:");
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntries#hasNext()
	 */
	public boolean hasNext() {
		return (position < ates.size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntries#last()
	 */
	public AuditTrailEntry last() {
		if (ates.size() == 0) {
			return null;
		} else {
			try {
				return ates.get(ates.size() - 1);
			} catch (IOException e) {
				System.err.println("Serious error in AuditTrailEntriesProxy:");
				e.printStackTrace();
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntries#next()
	 */
	public AuditTrailEntry next() {
		AuditTrailEntry result = null;
		try {
			if (position < ates.size()) {
				result = ates.get(position);
				position++;
			} else {
				System.err.println("Serious error in AuditTrailEntriesProxy:"
						+ " next() called in invalid state!");
				return null;
			}
		} catch (IOException e) {
			System.err.println("Serious error in AuditTrailEntriesProxy:");
			e.printStackTrace();
		}
		canRemove = true;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntries#remove()
	 */
	public void remove() {
		if (canRemove == false) {
			System.err
					.println("ERROR: Only one call of 'remove' permitted per call of 'next'!");
			return;
		}
		try {
			// remove the last retrieved audit trail entry
			position--;
			ates.remove(position);
			canRemove = false;
			// safety check: removing the first element
			if (position < 0) {
				System.err.println("Serious error in AuditTrailEntriesProxy:"
						+ " remove() called with no prior call to next()!");
				position = Integer.MAX_VALUE; // this will break!
			}
		} catch (IOException e) {
			System.err.println("Serious error in AuditTrailEntriesProxy:");
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntries#reset()
	 */
	public void reset() {
		position = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntries#size()
	 */
	public int size() {
		return ates.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntries#toArrayList()
	 */
	public ArrayList toArrayList() {
		return new ATEArrayListProxy(ates);
	}

	public Object clone() {
		return new AuditTrailEntriesProxy(ates, position);
	}

}
