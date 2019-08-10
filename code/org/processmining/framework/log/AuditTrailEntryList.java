/**
 * Project: ProM
 * File: AuditTrailEntryList.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 23, 2006, 8:16:46 PM
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

import java.io.IOException;
import java.util.Iterator;

/**
 * Defines abstract access and modification methods to an ordered set of audit
 * trail entries.
 * <p>
 * The order of the list is defined by given indices, a <b>natural order</b>
 * based on timestamps or creation time of the contained audit trail entries
 * <b>is not enforced</b>. <b>Note:</b> The performance and optimized usage
 * pattern of the represented instances is dependent on the respective
 * implementation of this interface. The interface makes no assumption about the
 * efficiency or cost of provided methods. You are strongly advised to consult
 * the documentation for the implementation of this list in your context, if you
 * want or need to optimize your application for performance.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public interface AuditTrailEntryList {

	/**
	 * Returns the current size of this audit trail entry list.
	 * 
	 * @return The number of (valid) audit trail entries contained in this list.
	 */
	public abstract int size();

	/**
	 * Retrieves the audit trail entry located at the given position, i.e.
	 * index, in the list.
	 * <p>
	 * The usage contract for indices in this context is, that the given value
	 * must reside within <code>[0, size() - 1]</code>.
	 * 
	 * @param index
	 *            Position of the requested audit trail entry (within
	 *            <code>[0, size() - 1]</code>)
	 * @return The requested audit trail entry object
	 * @throws IndexOutOfBoundsException
	 *             Thrown when given index is invalid
	 * @throws IOException
	 *             Thrown on general I/O failures
	 */
	public abstract AuditTrailEntry get(int index)
			throws IndexOutOfBoundsException, IOException;

	/**
	 * Retrieves an iterator on this list.
	 * <p>
	 * <b>Warning:</b> Iterators on audit trail entry lists are not required to
	 * be consistent. When the list is modified during the lifetime of an
	 * iterator, this iterator becomes inconsistent and may return wrong results
	 * or perform in unexpected manner. You are advised to use modifying
	 * iterators exclusively, and not perform modifying operations on the list
	 * while using the iterator. Multiple read-only iterators on the same list
	 * will perform independently, each as expected.
	 * 
	 * @return An iterator over the contained audit trail entries.
	 */
	public abstract Iterator iterator();

	/**
	 * Appends a given audit trail entry to the end of the list.
	 * 
	 * @param ate
	 *            The audit trail entry to append to the list.
	 * @return The index, or position, within the list at which the audit trail
	 *         entry has been inserted.
	 * @throws IOException
	 *             Thrown on I/O failure
	 */
	public abstract int append(AuditTrailEntry ate) throws IOException;

	/**
	 * Inserts the given audit trail entry at the given logical position, or
	 * index, within the list.
	 * 
	 * @param ate
	 *            The audit trail entry to be inserted.
	 * @param index
	 *            Index, or position within the list, at which to insert the
	 *            audit trail entry. Must be within <code>[0, size()]</code>.
	 * @throws IndexOutOfBoundsException
	 *             Thrown if the given index is not within
	 *             <code>[0, size()]</code>.
	 * @throws IOException
	 *             Thrown on I/O failure.
	 */
	public abstract void insert(AuditTrailEntry ate, int index)
			throws IndexOutOfBoundsException, IOException;

	/**
	 * Inserts the given audit trail entry at the correct order position,
	 * depending on its timestamp.
	 * 
	 * @param ate
	 *            The audit trail entry to be inserted
	 * @return Index, at which the audit trail entry has been inserted in the
	 *         list
	 * @throws IOException
	 */
	public abstract int insertOrdered(AuditTrailEntry ate) throws IOException;

	/**
	 * Removes the audit trail entry at the given position, or index, from the
	 * list
	 * 
	 * @param index
	 *            Position of the audit trail entry to be removed (within
	 *            <code>[0, size() - 1]</code>).
	 * @return The removed audit trail entry.
	 * @throws IndexOutOfBoundsException
	 *             Thrown if the given index is not within
	 *             <code>[0, size() - 1]</code>.
	 * @throws IOException
	 */
	public abstract AuditTrailEntry remove(int index)
			throws IndexOutOfBoundsException, IOException;

	/**
	 * Replaces the audit trail entry at the given position by another provided
	 * audit trail entry
	 * 
	 * @param ate
	 *            Audit trail entry to be inserted at the specified index.
	 * @param index
	 *            Index at which the current audit trail entry is removed, and
	 *            the provided one is inserted. Must be within
	 *            <code>[0, size() - 1]</code>.
	 * @return The previous audit trail entry which was replaced and removed
	 *         from the log.
	 * @throws IndexOutOfBoundsException
	 *             Thrown if the index is not within
	 *             <code>[0, size() - 1]</code>.
	 * @throws IOException
	 *             Thrown on I/O failure.
	 */
	public abstract AuditTrailEntry replace(AuditTrailEntry ate, int index)
			throws IndexOutOfBoundsException, IOException;

	/**
	 * Probes whether the implementing structure is in a tainted state.
	 * <p>
	 * Tainted, in this context, is defined as not being structured in an
	 * optimal fashion. Tainted instances can be identified using their method
	 * <code>isTainted()</code>. They may be transformed into an optimized state
	 * by calling their <code>consolidate()</code> method.
	 * <p>
	 * In general, consolidation is supposed to be expensive. Implementations of
	 * this interface are expected to consolidate themselves at specified events
	 * or intervals. You should not manually consolidate an instance other than
	 * if you know what you are doing, and what for.
	 * <p>
	 * One situation where manual consolidation is suggested is, when you stop
	 * modifying a list of audit trail entries and want to start excessive read
	 * operations on it (which are not interleaved with modifying operations).
	 * For details to tainting and consolidation, refer to the documentation of
	 * the implementing classes.
	 * 
	 * @return Whether this object is tainted.
	 */
	public abstract boolean isTainted();

	/**
	 * Consolidates this object, and transforms it into an optimized state.
	 * <p>
	 * Tainted, in this context, is defined as not being structured in an
	 * optimal fashion. Tainted instances can be identified using their method
	 * <code>isTainted()</code>. They may be transformed into an optimized state
	 * by calling their <code>consolidate()</code> method.
	 * <p>
	 * In general, consolidation is supposed to be expensive. Implementations of
	 * this interface are expected to consolidate themselves at specified events
	 * or intervals. You should not manually consolidate an instance other than
	 * if you know what you are doing, and what for.
	 * <p>
	 * One situation where manual consolidation is suggested is, when you stop
	 * modifying a list of audit trail entries and want to start excessive read
	 * operations on it (which are not interleaved with modifying operations).
	 * For details to tainting and consolidation, refer to the documentation of
	 * the implementing classes.
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract boolean consolidate() throws IOException;

	/**
	 * Returns a deep copy of this list of audit trail entries.
	 * <p>
	 * Modifications are not synchronized between original and clone.
	 * 
	 * @return A deep copy of this list of audit trail entries.
	 * @throws IOException
	 */
	public abstract AuditTrailEntryList cloneInstance() throws IOException;

	/**
	 * Frees all resources associated with this instance.
	 * <p>
	 * This method should be invoked, when it is foreseeable that this instance
	 * will no longer be accessed in any way. Do not attempt to access this
	 * instance after this method has been invoked, as things are about to get
	 * ugly otherwise!
	 * 
	 * @throws IOException
	 */
	public abstract void cleanup() throws IOException;
}
