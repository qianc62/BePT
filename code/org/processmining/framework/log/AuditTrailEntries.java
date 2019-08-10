/**
 * Project: ProM
 * File: AuditTrailEntries.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 22, 2006, 12:29:14 AM
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
package org.processmining.framework.log;

import java.util.ArrayList;

/**
 * This interface provides access to a sequential set of audit trail entries, as
 * found in one process instance of a log file.
 * 
 * @deprecated This is only provided for backward compatibility, you should not
 *             use this class and update your implementations. Use the container
 *             interface of <code>AuditTrailEntryList</code> instead where
 *             possible, which should be practically everywhere!
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public interface AuditTrailEntries extends Cloneable {

	/**
	 * Iterator-style access method.
	 * <p>
	 * Determines whether there is another audit trail entry left. (This relates
	 * to the built-in iterator walking through the elements.)
	 * 
	 * @see next
	 * @return <code>true</code> if there is at least one audit trail entry
	 *         left, <code>false</code> otherwise.
	 */
	public abstract boolean hasNext();

	/**
	 * Iterator-style access method.
	 * <p>
	 * Retrieves the next audit trail entry from the list. (This relates to the
	 * built-in iterator walking through the elements and implies moving it one
	 * poksition further.) Before calling this method one should call
	 * {@link #hasNext hasNext} in order to determine whether there is onother
	 * element left.
	 * 
	 * @return the next <code>AuditTrailEntry</code> from the list
	 */
	public abstract AuditTrailEntry next();

	/**
	 * Iterator-style access method.
	 * <p>
	 * Resets the built-in iterator to the first element of the list.
	 */
	public abstract void reset();

	/**
	 * Iterator-style access method.
	 * <p>
	 * Remove the entry that was returned by the last call to
	 * <code>next()</code>.
	 */
	public abstract void remove();

	/**
	 * Adds an audit trail entry to the end of the list.
	 * 
	 * @param ate
	 *            the entry to add
	 */
	public abstract void add(AuditTrailEntry ate);

	/**
	 * Adds an audit trail entry to the given position in the list.
	 * 
	 * @param ate
	 *            the entry to add
	 * @param pos
	 *            the position of the entry
	 */
	public abstract void add(AuditTrailEntry ate, int pos);

	/**
	 * Returns the list of audit trail entries.
	 * <p>
	 * <b>WARNING:</b> The usage contract for this interface determines, that
	 * any changes that you make for the returned array list will not be
	 * persistent to both this instance and the potentially wrapped higher-level
	 * classes! Use dedicated modification methods for this purpose!
	 * 
	 * @return the list of audit trail entries as an <code>ArrayList</code>
	 * @deprecated Use the random access interface (e.g. <code>get(index)</code>
	 *             ) instead, as this method is subject to proximate deletion!
	 */
	public abstract ArrayList toArrayList();

	/**
	 * Gets the first element out of the list of audit trail entries.
	 * 
	 * @return the first <code>AuditTrailEntry</code> from the list
	 */
	public abstract AuditTrailEntry first();

	/**
	 * Gets the last element out of the list of audit trail entries.
	 * 
	 * @return the last <code>AuditTrailEntry</code> from the list
	 */
	public abstract AuditTrailEntry last();

	/**
	 * Retrieves an audit trail entry based on the given position in the list.
	 * 
	 * @param n
	 *            the index position in the list (starting with 0)
	 * @return the <code>AuditTrailEntry<code> stored at the given position
	 */
	public abstract AuditTrailEntry getEntry(int n);

	/**
	 * Retrieves the list of audit trail entries as a string.
	 * 
	 * @return all entries as a <code>String</code>
	 */
	public abstract String toString();

	/**
	 * Determines the number of audit trail entries stored in the list.
	 * 
	 * @return the number of audit trail entries
	 */
	public abstract int size();

	/**
	 * Makes a shallow copy of this object. This means that the contained list
	 * of {@link AuditTrailEntry AuditTrailEntry} elements will be not cloned.
	 * However, the iterator is duplicated in its current positon.
	 * 
	 * @return the cloned object
	 */
	public abstract AuditTrailEntries cloneIteratorOnly();

	/**
	 * Makes a deep copy of this object. This means that both the contained list
	 * of {@link AuditTrailEntry AuditTrailEntry} elements and the iterator in
	 * its current positon will be duplicated. Overrides {@link Object#clone
	 * clone}.
	 * 
	 * @return the cloned object
	 */
	public abstract Object clone();

}
