/**
 * Project: ProM
 * File: AuditTrailEntryListImpl.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 23, 2006, 8:20:07 PM
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.rfb.io.ATERandomFileBuffer;
import org.processmining.framework.ui.menus.LogReaderMenu;

/**
 * This class implements the <code>AuditTrailEntryList</code> interface in an
 * efficient way. Efficient in this context may be read as best-effort approach
 * to limit runtime overhead when accessing and modifying a collection of audit
 * trail entries. Runtime overhead has been optimized following an expected
 * typical usage pattern, which makes the following assumptions.
 * <ul>
 * <li>Sequential reading of audit trail entries is the main and most frequent
 * use case.</li>
 * <li>Appending new audit trail entries to the list is the most frequent
 * modification operation and is comparably cheap.</li>
 * <li>Random read access to audit trail entries in this list scales with the
 * distance of the requested object from the last referenced object in this
 * list. Forward reading towards the end of the list is more frequent, and thus
 * faster, than backward seeking.</li>
 * <li>Modification of the list, except appending audit trail entries to the
 * list, is the least frequent, and thus most expensive, use case. The cost of
 * modifying the list scales with the fragmentation of changes (locality
 * improves speed).</li>
 * </ul>
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 * 
 */
public class AuditTrailEntryListImpl implements AuditTrailEntryList {

	/**
	 * Default maximal fragmentation to allow, as sum of removed and inserted
	 * (not appended!) audit trail entries
	 */
	public static int DEFAULT_FRAGMENTATION_LIMIT = 100;

	/**
	 * Maximal fragmentation to allow, as sum of removed and inserted (not
	 * appended!) audit trail entries
	 */
	protected int fragLimit = DEFAULT_FRAGMENTATION_LIMIT;
	/**
	 * Array containing the (virtual) indices of inserted (but not yet
	 * consolidated) audit trail entries. Their position in this array
	 * corresponds to their position in the overflow list.
	 */
	protected int[] overflowIndices = null;
	/**
	 * Array containing the (file buffer based) indices of removed, i.e.
	 * ignored, audit trail entries within the file buffer object
	 */
	protected int[] removedIndices = null;
	/**
	 * Counts the number of removed audit trail entries which are ignored in the
	 * file buffer.
	 */
	protected int removedCounter = 0;
	/**
	 * Ordered list containing the inserted audit trail entries, which have not
	 * yet been consolidated.
	 */
	protected ArrayList<AuditTrailEntryImpl> overflowList = null;
	/**
	 * Underlying file buffer holding the main audit trail entry stock. When
	 * addressing the file buffer, virtual indices (as interpreted by using
	 * classes) need to be adjusted based on the structure of removed, i.e.
	 * ignored, and inserted (but not yet consolidated) audit trail entries.
	 */
	protected ATERandomFileBuffer fileBuffer = null;

	/**
	 * Creates a new, empty list of audit trail entries with the default
	 * fragmentation limit.
	 * 
	 * @throws IOException
	 */
	public AuditTrailEntryListImpl() throws IOException {
		// TODO: make this implementation cleaner - no static reference to menu
		// class member!
		this(new ATERandomFileBuffer(LogReaderMenu.storageProvider),
				DEFAULT_FRAGMENTATION_LIMIT);
	}

	/**
	 * Creates a new list of audit trail entries.
	 * 
	 * @param initialBuffer
	 *            File buffer containing the initial set of audit trail entries.
	 *            This file buffer must be in a consolidated state.
	 * @param maxFragment
	 *            Maximal level of fragmentation for the created instance.
	 *            Fragmentation is the sum of inserted (but not yet
	 *            consolidated) and removed, i.e. ignored, audit trail entries
	 *            in an AuditTrailEntryList.
	 */
	public AuditTrailEntryListImpl(ATERandomFileBuffer initialBuffer,
			int maxFragment) {
		synchronized (AuditTrailEntryListImpl.class) {
			// use provided buffer verbatim
			fileBuffer = initialBuffer;
			removedCounter = 0;
			fragLimit = maxFragment;
			overflowIndices = new int[fragLimit + 2];
			removedIndices = new int[fragLimit + 2];
			Arrays.fill(overflowIndices, Integer.MIN_VALUE);
			Arrays.fill(removedIndices, Integer.MIN_VALUE);
			overflowList = new ArrayList<AuditTrailEntryImpl>(fragLimit + 1);
		}
	}

	/**
	 * Creates a new list of audit trail entries, which is a clone of the
	 * provided template instance. The clone will hold the exactly same state as
	 * the template, but further changes will not be synchronized between
	 * template and clone.
	 * <p>
	 * Clones will be non-tainted, which is achieved by consolidating the cloned
	 * template instance in advance. Be aware, that your template is
	 * consolidated after using it as template in this constructor.
	 * 
	 * @param template
	 *            The template instance to be cloned.
	 * @throws IOException
	 */
	public AuditTrailEntryListImpl(AuditTrailEntryListImpl template)
			throws IOException {
		this(template, template.fragLimit);
	}

	/**
	 * Creates a new list of audit trail entries, which is a clone of the
	 * provided template instance. The clone will hold the exactly same state as
	 * the template, but further changes will not be synchronized between
	 * template and clone.
	 * <p>
	 * Clones will be non-tainted, which is achieved by consolidating the cloned
	 * template instance in advance. Be aware, that your template is
	 * consolidated after using it as template in this constructor.
	 * 
	 * @param template
	 *            The template instance to be cloned.
	 * @param maxFragment
	 *            Maximal level of fragmentation for the created instance.
	 *            Fragmentation is the sum of inserted (but not yet
	 *            consolidated) and removed, i.e. ignored, audit trail entries
	 *            in an AuditTrailEntryList.
	 * @throws IOException
	 */
	public AuditTrailEntryListImpl(AuditTrailEntryListImpl template,
			int maxFragment) throws IOException {
		synchronized (AuditTrailEntryListImpl.class) {
			template.consolidate(true);
			fileBuffer = new ATERandomFileBuffer(template.fileBuffer);
			removedCounter = 0;
			fragLimit = maxFragment;
			overflowIndices = new int[fragLimit + 1];
			removedIndices = new int[fragLimit + 1];
			overflowList = new ArrayList<AuditTrailEntryImpl>(fragLimit + 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.AuditTrailEntryList#size()
	 */
	public synchronized int size() {
		return fileBuffer.size() + overflowList.size() - removedCounter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.AuditTrailEntryList#get(int)
	 */
	public synchronized AuditTrailEntry get(int index)
			throws IndexOutOfBoundsException, IOException {
		// index sanity check
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		// probe overflow list first
		AuditTrailEntry result = getFromOverflowVirt(index);
		if (result == null) {
			// not contained in overflow list; adjust index and retrieve from
			// file buffer
			int fbIndex = translateVirtualIndexToFileBuffer(index);
			result = fileBuffer.get(fbIndex);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.AuditTrailEntryList#iterator()
	 */
	public synchronized Iterator iterator() {
		return new AuditTrailEntryListIterator(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.rfb.AuditTrailEntryList#append(org.
	 * processmining.framework.log.AuditTrailEntry)
	 */
	public synchronized int append(AuditTrailEntry ate) throws IOException {
		// last position is in file buffer by default,
		// delegate appending
		int position = size();
		fileBuffer.append(ate);
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.rfb.AuditTrailEntryList#insert(org.
	 * processmining.framework.log.AuditTrailEntry, int)
	 */
	public synchronized void insert(AuditTrailEntry ate, int index)
			throws IndexOutOfBoundsException, IOException {
		// index sanity checks
		if (index < 0 || index > size()) {
			// position is erroneous!
			throw new IndexOutOfBoundsException(
					"Attempting to insert entry at index beyond size!");
		}
		// delegate inserting, with consolidation checks enabled
		insert(ate, index, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#insertOrdered(org
	 * .processmining.framework.log.AuditTrailEntry)
	 */
	public synchronized int insertOrdered(AuditTrailEntry ate)
			throws IOException {
		Date ts = ate.getTimestamp();
		if (ts == null) {
			throw new IOException(
					"Date is 'null'! - ordered insertion not possible! (ts)");
		}
		Date curTs = null;
		// traverse the list of audit trail entries from beginning to end
		for (int i = 0; i < this.size(); i++) {
			curTs = this.get(i).getTimestamp();
			if (curTs == null) {
				throw new IOException(
						"Date is 'null'! - ordered insertion not possible! (curTs)");
			}
			if (ts.compareTo(curTs) < 0) {
				// replace current position with new audit trail entry,
				// shift all following towards the end
				this.insert(ate, i);
				return i;
			}
		}
		// no later audit trail entry in list; append
		this.append(ate);
		return this.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.AuditTrailEntryList#remove(int)
	 */
	public synchronized AuditTrailEntry remove(int index)
			throws IndexOutOfBoundsException, IOException {
		// check validity of index
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		// delegate, with consolidation checks enabled
		return remove(index, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.rfb.AuditTrailEntryList#replace(org.
	 * processmining.framework.log.AuditTrailEntry, int)
	 */
	public synchronized AuditTrailEntry replace(AuditTrailEntry ate, int index)
			throws IndexOutOfBoundsException, IOException {
		// check index for sanity
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		AuditTrailEntry result = get(index);
		// try to replace within random file buffer
		int fbIndex = translateVirtualIndexToFileBuffer(index);
		if (this.fileBuffer.replace(ate, fbIndex) == true) {
			// could replace within file buffer
			return result;
		}
		// remove and insert replacement without consolidation checks
		result = remove(index, false);
		insert(ate, index, false);
		// checked consolidation when finished
		consolidate(false);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.AuditTrailEntryList
	 */
	public synchronized boolean isTainted() {
		return (removedCounter > 0 || overflowList.size() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.AuditTrailEntryList
	 */
	public synchronized boolean consolidate() throws IOException {
		// enforced consolidation
		return consolidate(true);
	}

	/**
	 * @return the fragmentation degree of this instance, as sum of the number
	 *         of removed, i.e. ignored, audit trail entries and the number of
	 *         newly inserted audit trail entries, which have not yet been
	 *         consolidated.
	 */
	public synchronized int fragmentation() {
		return removedCounter + overflowList.size();
	}

	/**
	 * Returns a deep copy of this list of audit trail entries, which is backed
	 * by exact copies of the underlying swap file. Changes between this
	 * instance and the created clone are not synchronized!
	 * 
	 * @return A deep copy of this list.
	 * @throws IOException
	 */
	public synchronized AuditTrailEntryList cloneInstance() throws IOException {
		return new AuditTrailEntryListImpl(this);
	}

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
	public synchronized void cleanup() throws IOException {
		fileBuffer.cleanup();
		overflowList = null;
	}

	protected synchronized AuditTrailEntry remove(int index, boolean consolidate)
			throws IndexOutOfBoundsException, IOException {
		// probe overflow list first (cheap, nont-tainting case)
		AuditTrailEntry result = getFromOverflowVirt(index);
		if (result != null) {
			// audit trail entry to be removed was in overflow list
			// remove entry from indices array and decrement greater indices
			// (to reflect removal)
			int pos = -1;
			for (int i = 0; i < overflowList.size(); i++) {
				if (overflowIndices[i] > index) {
					overflowIndices[i - 1] = overflowIndices[i] - 1;
				} else if (overflowIndices[i] == index) {
					// remember position of element to be removed (in overflow
					// list)
					pos = i;
				}
			}
			// extra validity check
			// TODO: remove later, if proven, tried, and tested
			if (pos < 0) {
				throw new IndexOutOfBoundsException(
						"Implementation fault in AuditTrailEntryListImpl.remove(int, boolean)");
			}
			// remove auddit trail entry from overflow list
			overflowList.remove(pos);
			// No adjusting of removed counter in this case, as operations like
			// this case do actually defragment the collection!
		} else {
			// audit trail entry is in the file buffer
			int fbIndex = translateVirtualIndexToFileBuffer(index);
			// correct larger overflow entries' indices
			// (decrement, as we removed a predecessor)
			for (int i = 0; i < overflowList.size(); i++) {
				if (overflowIndices[i] > index) {
					overflowIndices[i] = overflowIndices[i] - 1;
				}
			}
			// retrieve and remove filebuffer-stored element
			result = fileBuffer.get(fbIndex);
			addEntryToRemovedListFb(fbIndex);
			removedCounter++;
			if (consolidate == true) {
				// perform consolidation of list, if necessary (non-forcing)
				consolidate(false);
			}
		}
		return result;
	}

	protected synchronized void insert(AuditTrailEntry ate, int index,
			boolean consolidate) throws IOException {
		if (index == size()) {
			// to be inserted at last position; append
			fileBuffer.append(ate);
		} else if (index < size()) {
			// insert entry to overflow index array
			int overflowPos = addEntryToOverflowIndicesVirtual(index);
			// insert audit trail entry at correct overflow list position
			overflowList.add(overflowPos, (AuditTrailEntryImpl) ate);
			if (consolidate == true) {
				// consolidate, if necessary
				consolidate(false);
			}
		}
	}

	/**
	 * Retrieves the audit trail entry with the given virtual index from the
	 * overflow list.
	 * 
	 * @param index
	 *            Index of the requested audit trail entry.
	 * @return the requested audit trail entry, or <code>null</code> if the
	 *         requested index is not contained in the overflow list.
	 */
	protected synchronized AuditTrailEntry getFromOverflowVirt(int index) {
		for (int i = 0; i < overflowList.size(); i++) {
			if (overflowIndices[i] == index) {
				return (AuditTrailEntry) overflowList.get(i);
			} else if (overflowIndices[i] > index) {
				// array of overflow indices is ordered!
				return null;
			}
		}
		return null;
	}

	/**
	 * Translates a given virtual index, as used by using classes, into the
	 * actual index of the requested audit trail entry within the file buffer.
	 * <p>
	 * <b>Warning:</b> This method assumes that the requested audit trail entry
	 * is indeed located within the file buffer. Failure to comply to this
	 * assumption will yield wrong results.
	 * 
	 * @param virtualIndex
	 *            virtual index
	 * @return actual file buffer based index
	 */
	protected synchronized int translateVirtualIndexToFileBuffer(
			int virtualIndex) throws IndexOutOfBoundsException {
		int fbIndex = virtualIndex;
		// decrement index by number of ATEs in overflow list
		// with smaller indices than the given virtual index
		for (int i = 0; i < overflowList.size(); i++) {
			if (overflowIndices[i] < virtualIndex) {
				fbIndex--;
			} else {
				break; // array of overflow indices is ordered
			}
		}
		// increment index by number of removed indices smaller
		// or equal than requested; adjust index on the way!
		for (int i = 0; i < removedCounter; i++) {
			if (removedIndices[i] <= fbIndex) {
				fbIndex++;
			} else {
				break; // array of removed indices is ordered
			}
		}
		return fbIndex;
	}

	/**
	 * Translates a given index within the file buffer to the virtual index the
	 * referenced audit trail entry represents.
	 * 
	 * @param fbIndex
	 *            actual index of the ATE entry within the file buffer.
	 * @return virtual index of the referenced ATE entry.
	 */
	protected synchronized int translateFileBufferIndexToVirtual(int fbIndex) {
		int virtualIndex = fbIndex;
		// decrement virtual index with the number of audit trail
		// entries in the file, before the provided file-buffer-based
		// index, which have been marked as deleted.
		for (int i = 0; i < removedCounter; i++) {
			if (removedIndices[i] < fbIndex) {
				virtualIndex--;
			} else {
				break; // array of removed indices is ordered!
			}
		}
		// increment virtual index with the number of audit trail
		// entries in the overflow list, which have a smaller index
		// than itself.
		for (int i = 0; i < overflowList.size(); i++) {
			if (overflowIndices[i] <= virtualIndex) {
				virtualIndex++;
			} else {
				break; // array of overflow indices is ordered!
			}
		}
		return virtualIndex;
	}

	/**
	 * Inserts a given virtual index into the list of overflow entry positions;
	 * Implies incrementing the subsequent entries by 1 each.
	 * 
	 * @param index
	 *            The requested virtual index of the ATE to be inserted.
	 * @return the index of the given index entry within the overflow array.
	 */
	protected synchronized int addEntryToOverflowIndicesVirtual(int index) {
		// iterate through overflow indices array backwards
		for (int i = overflowList.size(); i > 0; i--) {
			if (overflowIndices[i - 1] >= index) {
				// shift larger/equal index towards the end and
				// increment it (as we squeeze in before it!)
				overflowIndices[i] = overflowIndices[i - 1] + 1;
			} else {
				// correct position found! insert and return position!
				overflowIndices[i] = index;
				return i;
			}
		}
		// Smallest index to be inserted yet (can also be the first one!).
		overflowIndices[0] = index;
		return 0;
	}

	/**
	 * Inserts a given file buffer based index into the list of removed entry
	 * positions;
	 * 
	 * @param index
	 *            The requested virtual index of the ATE to be removed.
	 * @return the index of the given index entry within the overflow array.
	 * @throws IndexOutOfBoundsException
	 */
	protected synchronized int addEntryToRemovedListFb(int index)
			throws IndexOutOfBoundsException {
		// iterate backwards through the removed indices array.
		// keep the array of removed file buffer indices sorted in ascending
		// manner.
		// remember: removed indices are referring to positions in the file
		// buffer!
		for (int i = removedCounter; i > 0; i--) {
			if (removedIndices[i - 1] > index) {
				// shift larger indices towards the end of the array
				removedIndices[i] = removedIndices[i - 1];
			} else {
				// correct insert position found:
				// insert and return position
				removedIndices[i] = index;
				return i;
			}
		}
		// the index to be inserted is the smallest in set: position is zero.
		removedIndices[0] = index;
		return 0;
	}

	/**
	 * Performs a consolidation of the internal data structures. Overflow and
	 * removal lists will be cleared and merged with the file buffer into a new
	 * file buffer. This operation is pretty expensive, but can gain speed
	 * improvements when the instance becomes too fragmented between file buffer
	 * and overflow / removal structures.
	 * 
	 * @param force
	 *            If this parameter is set to <code>true</code>, consolidation
	 *            is performed also when the specified fragmentation limit has
	 *            not been exceeded yet. When set to <code>false</code>, the
	 *            fragmentation limit is respected for runtime optimization.
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	protected synchronized boolean consolidate(boolean force)
			throws IndexOutOfBoundsException, IOException {
		// only consolidate, if one of two conditions holds:
		// - consolidation enforced, and modifications have been performed
		// - fragmentation limit has been exceeded
		if ((force == true && isTainted())
				|| ((overflowList.size() + removedCounter) >= fragLimit)) {
			// perform consolidation; create new consolidated file buffer
			ATERandomFileBuffer consolidationBuffer = new ATERandomFileBuffer(
					LogReaderMenu.storageProvider);
			for (int i = 0; i < size(); i++) {
				consolidationBuffer.append(get(i));
			}
			// remove and clean up old file buffer, replace by consolidation
			// buffer
			fileBuffer.cleanup();
			fileBuffer = consolidationBuffer;
			// initialize overflow and removal structures
			overflowList.clear();
			Arrays.fill(overflowIndices, Integer.MIN_VALUE);
			Arrays.fill(removedIndices, Integer.MIN_VALUE);
			removedCounter = 0;
			return true;
		} else {
			return false;
		}
	}

}
