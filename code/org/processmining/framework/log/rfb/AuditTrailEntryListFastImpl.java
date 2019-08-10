/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.framework.log.rfb;

import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.rfb.io.ATERandomFileBuffer;
import org.processmining.framework.ui.menus.LogReaderMenu;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AuditTrailEntryListFastImpl implements AuditTrailEntryList {

	public static int OVERFLOW_LIMIT = 100;

	protected int size = 0;
	protected ATERandomFileBuffer buffer;
	protected BitSet holeFlags;
	protected int[] overflowIndices;
	protected AuditTrailEntry[] overflowEntries;
	protected int overflowSize;

	public AuditTrailEntryListFastImpl() throws IOException {
		this.size = 0;
		this.buffer = new ATERandomFileBuffer(LogReaderMenu.storageProvider);
		this.holeFlags = new BitSet();
		this.overflowIndices = new int[OVERFLOW_LIMIT];
		this.overflowEntries = new AuditTrailEntry[OVERFLOW_LIMIT];
		this.overflowSize = 0;
	}

	public AuditTrailEntryListFastImpl(ATERandomFileBuffer buffer,
			int maxFragment) throws IOException {
		this.size = 0;
		this.buffer = buffer;
		this.holeFlags = new BitSet();
		this.overflowIndices = new int[OVERFLOW_LIMIT];
		this.overflowEntries = new AuditTrailEntry[OVERFLOW_LIMIT];
		this.overflowSize = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#append(org.processmining
	 * .framework.log.AuditTrailEntry)
	 */
	public synchronized int append(AuditTrailEntry ate) throws IOException {
		buffer.append(ate);
		size++;
		return size - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#cleanup()
	 */
	public synchronized void cleanup() throws IOException {
		buffer.cleanup();
		this.holeFlags = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#cloneInstance()
	 */
	public synchronized AuditTrailEntryList cloneInstance() throws IOException {
		this.consolidate();
		AuditTrailEntryListFastImpl clone = new AuditTrailEntryListFastImpl();
		clone.buffer = buffer.cloneInstance();
		clone.size = size;
		return clone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#consolidate()
	 */
	public synchronized boolean consolidate() throws IOException {
		if (isTainted()) {
			// proceed with consolidation
			ATERandomFileBuffer nBuffer = new ATERandomFileBuffer(
					LogReaderMenu.storageProvider);
			int overflowIndex = 0;
			int fileBufferIndex = 0;
			for (int i = 0; i < size; i++) {
				if (overflowIndex < overflowSize
						&& overflowIndices[overflowIndex] == i) {
					nBuffer.append(overflowEntries[overflowIndex]);
					overflowIndex++;
				} else {
					while (holeFlags.get(fileBufferIndex) == true) {
						fileBufferIndex++;
					}
					nBuffer.append(buffer.get(fileBufferIndex));
					fileBufferIndex++;
				}
			}
			buffer.cleanup();
			buffer = nBuffer;
			overflowSize = 0;
			holeFlags.clear();
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#get(int)
	 */
	public synchronized AuditTrailEntry get(int index)
			throws IndexOutOfBoundsException, IOException {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		int bufferIndex = index;
		// correct buffer index from overflow
		for (int i = 0; i < overflowSize; i++) {
			if (overflowIndices[i] == index) {
				return overflowEntries[i];
			} else if (overflowIndices[i] < index) {
				bufferIndex--;
			} else {
				break;
			}
		}
		// determine deleted offset
		// step over flagged indices and adjust buffer index upwards
		// respectively
		for (int hole = holeFlags.nextSetBit(0); hole >= 0
				&& hole <= bufferIndex; hole = holeFlags.nextSetBit(hole + 1)) {
			bufferIndex++;
		}
		// buffer index should now point to the corresponding index
		// within the file buffer, so return it
		return buffer.get(bufferIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#insert(org.processmining
	 * .framework.log.AuditTrailEntry, int)
	 */
	public synchronized void insert(AuditTrailEntry ate, int index)
			throws IndexOutOfBoundsException, IOException {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		// check if we can append
		if (index == size) {
			append(ate);
			return;
		}
		// adjust size and overflow size
		size++;
		overflowSize++;
		// add to overflow set
		for (int i = overflowSize - 2; i >= 0; i--) {
			if (overflowIndices[i] >= index) {
				overflowIndices[i + 1] = overflowIndices[i] + 1;
				overflowEntries[i + 1] = overflowEntries[i];
			} else {
				overflowIndices[i + 1] = index;
				overflowEntries[i + 1] = ate;
				if (overflowSize == overflowIndices.length) {
					consolidate();
				}
				return;
			}
		}
		// if we arrive here, we must insert at zero
		overflowIndices[0] = index;
		overflowEntries[0] = ate;
		if (overflowSize == overflowIndices.length) {
			consolidate();
		}
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
		if (ate.getTimestamp() == null || get(size - 1).getTimestamp() == null
				|| ate.getTimestamp().after(get(size - 1).getTimestamp())) {
			append(ate);
			return size - 1;
		}
		for (int i = 0; i < size; i++) {
			if (get(i).getTimestamp() == null) {
				continue;
			}
			if (get(i).getTimestamp().after(ate.getTimestamp())) {
				insert(ate, i);
				return i;
			}
		}
		throw new IOException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#isTainted()
	 */
	public synchronized boolean isTainted() {
		return (overflowSize > 0) || (holeFlags.cardinality() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#iterator()
	 */
	public Iterator iterator() {
		return new AuditTrailEntryListIterator(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#remove(int)
	 */
	public synchronized AuditTrailEntry remove(int index)
			throws IndexOutOfBoundsException, IOException {
		// check overflow list and adjust indices
		AuditTrailEntry removed = null;
		int smallerOverflow = 0;
		for (int i = 0; i < overflowSize; i++) {
			if (overflowIndices[i] == index) {
				removed = overflowEntries[i];
			} else if (overflowIndices[i] > index) {
				overflowIndices[i] = overflowIndices[i] - 1;
				if (removed != null) {
					// move left
					overflowIndices[i - 1] = overflowIndices[i];
					overflowEntries[i - 1] = overflowEntries[i];
				}
			} else if (overflowIndices[i] < index) {
				smallerOverflow++;
			}
		}
		if (removed != null) {
			// adjust overflow size
			overflowSize--;
			// invalidate entry in overflow set
			overflowIndices[overflowSize] = -1;
			overflowEntries[overflowSize] = null;
		} else {
			int bufferIndex = index - smallerOverflow;
			for (int hole = holeFlags.nextSetBit(0); hole >= 0
					&& hole <= bufferIndex; hole = holeFlags
					.nextSetBit(hole + 1)) {
				bufferIndex++;
			}
			removed = buffer.get(bufferIndex);
			holeFlags.set(bufferIndex, true);
		}
		size--;
		return removed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.AuditTrailEntryList#replace(org.processmining
	 * .framework.log.AuditTrailEntry, int)
	 */
	public synchronized AuditTrailEntry replace(AuditTrailEntry ate, int index)
			throws IndexOutOfBoundsException, IOException {
		// check overflow list and adjust indices
		AuditTrailEntry replaced = null;
		int smallerOverflow = 0;
		for (int i = 0; i < overflowSize; i++) {
			if (overflowIndices[i] == index) {
				replaced = overflowEntries[i];
				overflowEntries[i] = ate;
				return replaced;
			} else if (overflowIndices[i] > index) {
				// done
				break;
			} else if (overflowIndices[i] < index) {
				smallerOverflow++;
			}
		}
		// still here: we must look in file buffer
		int bufferIndex = index - smallerOverflow;
		for (int hole = holeFlags.nextSetBit(0); hole >= 0
				&& hole <= bufferIndex; hole = holeFlags.nextSetBit(hole + 1)) {
			bufferIndex++;
		}
		replaced = buffer.get(bufferIndex);
		if (buffer.replace(ate, bufferIndex) == false) {
			remove(index);
			insert(ate, index);
		}
		return replaced;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.AuditTrailEntryList#size()
	 */
	public synchronized int size() {
		return size;
	}

}
