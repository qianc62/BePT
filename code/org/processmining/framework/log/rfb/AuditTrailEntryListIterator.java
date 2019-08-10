/**
 * Project: ProM
 * File: AuditTrailEntryListIterator.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 23, 2006, 10:38:33 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
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
 ***********************************************************
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
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;

/**
 * Implements an iterator over an instance of AuditTrailEntryList.
 * <p>
 * <b>Note:</b> Potential I/O errors are not thrown by this iterator in order to
 * retain conformance with the java.util.Iterator interface. However, I/O errors
 * are reported to STDERR when occurring.
 * <p>
 * <b>WARNING:</b> If you retrieve more than one iterator from one audit trail
 * entry list, consistency between iterators is no longer guaranteed. Further,
 * the iterator is implemented accessing the underlying list through its random
 * access interface. This implies that, if the list is modified during the
 * lifetime of an iterator on it (be it using another iterator or the direct
 * access methods), it is not guaranteed that iterators on this list will retain
 * consistency and return correct results from that point onwards.
 * 
 * @see org.processmining.framework.log.AuditTrailEntryList
 * @see java.util.Iterator
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class AuditTrailEntryListIterator implements Iterator {

	protected AuditTrailEntryList list = null;
	protected int position = 0;

	/**
	 * Constructs a new iterator on the specified
	 * <code>AuditTrailEntryList</code>.
	 * 
	 * @param anATEList
	 *            Audit trail entry list, over which the created iterator
	 *            iterates.
	 */
	public AuditTrailEntryListIterator(AuditTrailEntryList anATEList) {
		list = anATEList;
		position = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return (position < list.size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		AuditTrailEntry result = null;
		try {
			result = list.get(position);
			position++;
		} catch (Exception e) {
			// IO Errors are printed to STDERR
			e.printStackTrace();
			System.err.println("I/O error in AuditTrailEntryListIterator");
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		try {
			position--;
			list.remove(position);
		} catch (IOException e) {
			// IO Errors are printed to STDERR
			e.printStackTrace();
			System.err.println("I/O error in AuditTrailEntryListIterator");
		}
	}

}
