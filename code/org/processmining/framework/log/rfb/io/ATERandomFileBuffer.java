/**
 * Project: ProM
 * File: ATERandomFileBuffer.java
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
package org.processmining.framework.log.rfb.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataAttribute;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;

/**
 * This class provides a random-access interface to a sequential set of audit
 * trail entries. These audit trail entries are buffered not in heap space, but
 * in a binary buffer file, whose encoding is implemented in this class as well.
 * <p>
 * The structure of a buffer file is a sequence of binary records, one for each
 * audit trail entry, where records are encoded as follows:
 * <ul>
 * <li>Offset in bytes to next record / size of record (4-byte integer)</li>
 * <li>Offset in bytes to previous record (4-byte integer)</li>
 * <li>ID of the audit trail entry (8-byte long)</li>
 * <li>Workflow model element (UTF-8 encoded string)</li>
 * <li>Event type (UTF-8 encoded string)</li>
 * <li>Originator (UTF-8 encoded string)</li>
 * <li>Timestamp (8-byte long)</li>
 * <li>Size of data section / number of key-value pairs (4-byte integer)</li>
 * </ul>
 * This fixed part is followed by a number of string pairs describing the audit
 * trail entry's attributes. The number of subsequent strings is obviously the
 * value for size of the data section (see above) multiplied with 2.
 * <ul>
 * <li>Attribute key (UTF-8 encoded String)</li>
 * <li>Attribute value (UTF-8 encoded String)</li>
 * </ul>
 * 
 * @author christian
 * 
 */
public class ATERandomFileBuffer {

	/**
	 * Encoding used for storing strings in buffer storages.
	 */
	protected static final String STRING_ENCODING = "UTF-8";

	/**
	 * The number of audit trail entries contained in a buffer.
	 */
	protected int size = 0;
	/**
	 * The current logical index (in number of audit trail entries) in the
	 * buffer.
	 */
	protected int index = 0;
	/**
	 * The current actual position in the backing buffer storage, in bytes
	 * offset from the beginning of the storage.
	 */
	protected long position = 0;
	/**
	 * The position at which the last audit trail entry was inserted into the
	 * backing buffer storage. Initialized with -1.
	 */
	protected long lastInsertPosition = -1;
	/**
	 * The random access storage to back the buffer of audit trail entries.
	 */
	protected RandomAccessStorage storage = null;
	/**
	 * Storage provider which is used to allocate new buffer storages.
	 */
	protected StorageProvider provider = null;

	/**
	 * Separator to use when writing a list of model references to persistent
	 * storage.
	 */
	private static final String MODEL_REFERENCE_SEPARATOR = " ";
	/**
	 * The model reference separator as a compiled regex, so splitting a list of
	 * references is fast.
	 */
	private static final Pattern MODEL_REFERENCE_SEPARATOR_PATTERN = Pattern
			.compile(MODEL_REFERENCE_SEPARATOR);

	/**
	 * Creates and initializes a new instance of this class.
	 * 
	 * @param aProvider
	 *            storage provider used for backing this buffer.
	 * @throws IOException
	 */
	public ATERandomFileBuffer(StorageProvider aProvider) throws IOException {
		provider = aProvider;
		size = 0;
		index = 0;
		position = 0;
		lastInsertPosition = -1;
		storage = provider.createStorage();
	}

	/**
	 * Creates a new instance of this class, which is an exact copy, or clone,
	 * of the provided template instance. The created clone will be independent,
	 * i.e. backed by a distinct persistent storage, and changes will not be
	 * synchronized between template and clone.
	 * 
	 * @param template
	 *            The template instance to be cloned.
	 * @throws IOException
	 */
	public ATERandomFileBuffer(ATERandomFileBuffer template) throws IOException {
		// class-exclusive access
		synchronized (ATERandomFileBuffer.class) {
			// clone exact state of template
			provider = template.provider;
			size = template.size;
			index = template.index;
			position = template.position;
			lastInsertPosition = template.lastInsertPosition;
			// create a distinct storage to back the clone
			storage = template.storage.copy();
		}
	}

	protected ATERandomFileBuffer() {
		// implicit constructor reserved for derived classes
	}

	/**
	 * Returns the storage provider used by this instance
	 * 
	 * @return
	 */
	public StorageProvider getProvider() {
		return provider;
	}

	/**
	 * Returns the current position of this instance
	 * 
	 * @return
	 */
	public long position() {
		return position;
	}

	/**
	 * Returns the last insert position of this instance
	 * 
	 * @return
	 */
	public long lastInsert() {
		return lastInsertPosition;
	}

	/**
	 * Returns the random access storage this instance is based on
	 * 
	 * @return
	 */
	public RandomAccessStorage getStorage() {
		return storage;
	}

	/**
	 * Retrieves the number of audit trail entries recorded in this instance.
	 * 
	 * @return number of audit trail entries recorded in this instance
	 */
	public synchronized int size() {
		return size;
	}

	/**
	 * Retrieves the current internal, logical position of this collection.
	 * 
	 * @return
	 */
	public synchronized int index() {
		return index;
	}

	/**
	 * Appends a new audit trail entry to the end of this collection.
	 * <p>
	 * Notice that a call to this method does not affect the current position
	 * audit trail entries are read from.
	 * 
	 * @param ate
	 *            The audit trail entry to append
	 * @throws IOException
	 */
	public synchronized void append(AuditTrailEntry ate) throws IOException {
		// remember insert position
		long insertPosition = storage.length();
		// position storage pointer at end of storage
		storage.seek(insertPosition);
		// encode audit trail entry to byte array
		byte ateEnc[] = encode(ate);
		// compute segment length: add some slack to accomodate for later,
		// larger versions of this entry
		int segmentPaddingSize = ateEnc.length / 4;
		int segmentSize = ateEnc.length + segmentPaddingSize;
		byte segmentPadding[] = new byte[segmentPaddingSize];
		Arrays.fill(segmentPadding, (byte) 0);
		// record offset to subsequent audit trail entry for forward
		// skips; as size of ATE encoding + 12 bytes (for forward and
		// backward offset marker and payload size)
		storage.writeInt(segmentSize + 12);
		// record offset to previous ATE (for backward skips)
		storage.writeInt((int) (insertPosition - lastInsertPosition));
		// record actual payload size
		storage.writeInt(ateEnc.length);
		// record ATE encoding data
		storage.write(ateEnc);
		// record padding data
		storage.write(segmentPadding);
		// update last position pointer to this entry
		lastInsertPosition = insertPosition;
		// update collection size
		size++;
	}

	public synchronized boolean replace(AuditTrailEntry ate, int index)
			throws IOException {
		// check for index sanity
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		// determine and set appropriate file pointer position
		navigateToIndex(index);
		storage.seek(position);
		long atePosition = position;
		// read navigation data
		int fwd = storage.readInt();
		// skip backwards pointer and payload size, not relevant
		storage.skipBytes(8);
		int segmentSize = fwd - 12;
		// encode event
		byte[] ateEnc = encode(ate);
		boolean success = false;
		if (ateEnc.length <= segmentSize) {
			// overwrite event
			storage.seek(atePosition + 8);
			storage.writeInt(ateEnc.length);
			// insert new padding
			byte segmentPadding[] = new byte[segmentSize - ateEnc.length];
			Arrays.fill(segmentPadding, (byte) 0);
			storage.write(ateEnc);
			storage.write(segmentPadding);
			success = true;
		} else {
			success = false;
		}
		// return to prior position
		this.position = atePosition;
		storage.seek(this.position);
		return success;
	}

	/**
	 * Retrieves the audit trail entry recorded at the specified position
	 * 
	 * @param ateIndex
	 *            Position of the requested audit trail entry, defined to be
	 *            within <code>[0, size()-1]</code>.
	 * @return The requested audit trail entry object.
	 * @throws IOException
	 * @throws IndexOutOfBoundsException
	 */
	public synchronized AuditTrailEntry get(int ateIndex) throws IOException,
			IndexOutOfBoundsException {
		// check for index sanity
		if (ateIndex < 0 || ateIndex >= size) {
			throw new IndexOutOfBoundsException();
		}
		// determine and set appropriate file pointer position
		navigateToIndex(ateIndex);
		// read and return requested audit trail entry
		return read();
	}

	/**
	 * Returns an iterator on the audit trail entries buffered in this
	 * collection.
	 * 
	 * @return an iterator interface to the managed audit trail entries.
	 */
	public synchronized Iterator iterator() {
		return new ATERandomFileBufferIterator(this);
	}

	/**
	 * Cleans up any non-volatile resources (e.g. temporary files) associated
	 * with this instance and resets the instance to an initial state.
	 * 
	 * @throws IOException
	 */
	public synchronized void cleanup() throws IOException {
		// close and delete the underlying storage
		storage.close();
		size = 0;
		index = 0;
		position = 0;
		lastInsertPosition = -1;
	}

	/**
	 * Repositions the low-level layer to read from the specified index.
	 * 
	 * @param reqIndex
	 *            Index to position the file pointer to.
	 * @throws IOException
	 */
	protected synchronized void navigateToIndex(int reqIndex)
			throws IOException {
		// determine if navigation is necessary
		if (reqIndex != index) {
			// ensure that the requested index is valid
			if (reqIndex < 0 || reqIndex >= size) {
				throw new IndexOutOfBoundsException();
			}
			// navigate to requested index in file
			if (reqIndex > index) {
				// forward navigation
				skipForward(reqIndex - index);
			} else {
				// backward navigation
				int backSkips = index - reqIndex;
				if (backSkips < (index / 2)) {
					// check if current index is beyond valid list
					if (index == size) {
						// reset current position to last element in
						// set and adjust index and skip counter.
						index = (size - 1);
						position = lastInsertPosition;
						backSkips = index - reqIndex;
					}
					// move in backward direction
					skipBackward(backSkips);
				} else {
					// it is faster to reset position to the beginning
					// of the file and move forward from there to the
					// requested index
					resetPosition();
					skipForward(reqIndex);
				}
			}
		}
		if (reqIndex != index) {
			throw new IOException("navigation fault! (required: " + reqIndex
					+ ", yielded: " + index + ")");
		}
	}

	/**
	 * Resets the position of the data access layer to read the next audit trail
	 * entry from the first position.
	 */
	protected synchronized void resetPosition() {
		index = 0;
		position = 0;
	}

	/**
	 * Repositions the position of the data access layer to skip the specified
	 * number of records towards the end of the file.
	 * 
	 * @param atesToSkip
	 *            Number of records to be skipped.
	 * @throws IOException
	 */
	protected synchronized void skipForward(int atesToSkip) throws IOException {
		int offset = 0;
		for (int i = 0; i < atesToSkip; i++) {
			// adjust position for reading offset
			storage.seek(position);
			// read forward skip offset
			offset = storage.readInt();
			// set file pointer to next audit trail entry position
			position += offset;
			// adjust index
			index++;
		}
	}

	/**
	 * Repositions the position of the data access layer to skip the specified
	 * number of records towards the beginning of the file.
	 * 
	 * @param atesToSkip
	 *            Number of records to be skipped.
	 * @throws IOException
	 */
	protected synchronized void skipBackward(int atesToSkip) throws IOException {
		int offset = 0;
		for (int i = 0; i < atesToSkip; i++) {
			// position file pointer at current backward offset marker
			storage.seek(position + 4);
			// read backward offset to previous entry
			offset = storage.readInt();
			// adjust file pointer position
			position -= offset;
			// adjust index
			index--;
		}
	}

	/**
	 * Reads an audit trail entry from the current position of the data access
	 * layer. Calling this method implies the advancement of the data access
	 * layer, so that the next call will yield the subsequent audit trail entry.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected synchronized AuditTrailEntry read() throws IOException {
		// reset file pointer position
		storage.seek(position);
		// compute next position from forward offset
		long nextPosition = position + storage.readInt();
		// skip backward offset (4 bytes)
		storage.skipBytes(4);
		// read payload size
		int ateSize = storage.readInt();
		// buffered implementation: reads the byte array representing the
		// audit trail entry and interprets it from that buffer subsequently.
		byte[] ateData = new byte[ateSize];
		storage.readFully(ateData);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				ateData));
		// read audit trail entry's attributes in specified order from file
		long id = dis.readLong();
		String element = dis.readUTF();
		String type = dis.readUTF();
		String originator = dis.readUTF();
		// originator is not mandatory;
		// set to null, if emtpy string is read
		if (originator.length() == 0) {
			originator = null;
		}
		// timestamp is not mandatory;
		// set to null, if negative value is read
		Date timestamp = null;
		long tsLong = dis.readLong();
		if (tsLong != Long.MIN_VALUE) {
			// encode null value as most negative value possible
			timestamp = new Date(tsLong);
		}
		List<String> elementModelRefs = parseModelReferences(dis.readUTF());
		List<String> typeModelRefs = parseModelReferences(dis.readUTF());
		List<String> originatorModelRefs = parseModelReferences(dis.readUTF());
		// create preliminary result event
		AuditTrailEntryImpl ate = new AuditTrailEntryImpl(null, element, type,
				timestamp, originator, id, elementModelRefs, typeModelRefs,
				originatorModelRefs);
		// read attributes
		int dataSize = dis.readInt();
		String key = null, value = null;
		List<String> refs = null;
		for (int i = 0; i < dataSize; i++) {
			key = dis.readUTF();
			value = dis.readUTF();
			refs = parseModelReferences(dis.readUTF());
			ate.getDataAttributes().put(new DataAttribute(key, value, refs));
		}
		// adjust position of data acess layer
		position = nextPosition;
		index++;
		return ate;
	}

	private List<String> parseModelReferences(String string) {
		if (string.length() == 0) {
			return null;
		}

		List<String> result = new ArrayList<String>(1);
		for (String uri : MODEL_REFERENCE_SEPARATOR_PATTERN.split(string)) {
			if (uri.length() > 0) {
				result.add(uri);
			}
		}
		return result;
	}

	private String encodeModelReferences(List<String> refs) {
		if (refs == null || refs.size() == 0) {
			return "";
		} else {
			StringBuffer result = new StringBuffer();
			for (String uri : refs) {
				result.append(uri);
				result.append(MODEL_REFERENCE_SEPARATOR);
			}
			return result.toString();
		}
	}

	/**
	 * Encodes the given audit trail entry object into a sequence of bytes. This
	 * byte array corresponds to the structure of an audit trail entry record,
	 * as specified in the beginning of this document, excluding the
	 * back-/forward offsets used for navigation.
	 * 
	 * @param ate
	 *            The audit trail entry to be encoded.
	 * @return byte array representing the audit trail entry without navigation
	 *         offsets.
	 * @throws IOException
	 */
	protected byte[] encode(AuditTrailEntry ate) throws IOException {
		// prepare output stream for encoding
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		// write data to output stream
		dos.writeLong(ate.id());
		dos.writeUTF(ate.getElement());
		dos.writeUTF(ate.getType());
		// originator is not mandatory;
		// encode null value as empty string
		String originator = ate.getOriginator();
		if (originator == null) {
			dos.writeUTF("");
		} else {
			dos.writeUTF(originator);
		}
		// timestamp is not mandatory;
		// encode null value as most negative value possible
		Date timestamp = ate.getTimestamp();
		if (timestamp == null) {
			dos.writeLong(Long.MIN_VALUE);
		} else {
			dos.writeLong(ate.getTimestamp().getTime());
		}
		dos.writeUTF(encodeModelReferences(ate.getElementModelReferences()));
		dos.writeUTF(encodeModelReferences(ate.getTypeModelReferences()));
		dos.writeUTF(encodeModelReferences(ate.getOriginatorModelReferences()));
		DataSection data = ate.getDataAttributes();
		dos.writeInt(data.size());
		// write attributes as key-value pairs of UTF-8 strings
		for (String key : data.keySet()) {
			dos.writeUTF(key);
			dos.writeUTF((String) data.get(key));
			dos.writeUTF(encodeModelReferences(data.getModelReferences(key)));
		}
		// flush and serialize output stream result
		dos.flush();
		return baos.toByteArray();
	}

	/**
	 * Clones this instance.
	 * 
	 * @return a clone of this instance
	 * @throws IOException
	 */
	public synchronized ATERandomFileBuffer cloneInstance() throws IOException {
		return new ATERandomFileBuffer(this);
	}

	/**
	 * Remove buffer file when this instance is garbage collected.
	 */
	protected void finalize() throws Throwable {
		// clean buffer file from disk
		cleanup();
	}

	/**
	 * This class implements a lightweight iterator on a file-buffered random
	 * access collection of audit trail entries, as represented by the enclosing
	 * class.
	 * 
	 * @author christian
	 * 
	 */
	protected class ATERandomFileBufferIterator implements Iterator {

		protected ATERandomFileBuffer parent = null;
		protected int position = 0;

		/**
		 * Constructs a new iterator wrapper
		 * 
		 * @param aParent
		 */
		protected ATERandomFileBufferIterator(ATERandomFileBuffer aParent) {
			parent = aParent;
		}

		/**
		 * Probes for the availability of further audit trail entries.
		 */
		public boolean hasNext() {
			return (position < parent.size());
		}

		/**
		 * Retrieves another audit trail entry from the wrapped collection.
		 */
		public Object next() {
			int requestedIndex = position;
			position++;
			try {
				return parent.get(requestedIndex);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		/**
		 * This method is not implemented on this level
		 */
		public void remove() {
			// TODO Not implemented!
		}

	}

}
