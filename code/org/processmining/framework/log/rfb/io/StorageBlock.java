/**
 * Project: ProM
 * File: StorageBlock.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 13, 2006, 4:57:31 PM
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
package org.processmining.framework.log.rfb.io;

import java.io.IOException;

/**
 * This class represents a block of bytes, as provided by a block data storage.
 * These blocks can be used to constitute and back a virtual file.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 * 
 */
public class StorageBlock implements RandomAccessStorage {

	/**
	 * The block data storage of which this block is a part.
	 */
	protected BlockDataStorage parent = null;
	/**
	 * The logical index of this block in the encompassing block data storage.
	 */
	protected int blockNumber = 0;
	/**
	 * The current maximal number of bytes which can be allocated in this block.
	 */
	protected int maxSize = 0;
	/**
	 * The current number of bytes allocated in this block at the moment.
	 */
	protected int size = 0;
	/**
	 * Current offset of bytes in this block, from which the next read or write
	 * operation will occur.
	 */
	protected int pointer = 0;

	/**
	 * Creates and initializes a new storage block object.
	 * 
	 * @param aParent
	 *            Block data storage of which this block is a part.
	 * @param aBlockNumber
	 *            Index of this block in the encompassing storage.
	 * @param aMaxSize
	 *            Maximal number of bytes contained in this block.
	 */
	public StorageBlock(BlockDataStorage aParent, int aBlockNumber, int aMaxSize) {
		// prevent race conditions in construction
		synchronized (this) {
			parent = aParent;
			blockNumber = aBlockNumber;
			maxSize = aMaxSize;
			size = 0;
			pointer = 0;
		}
	}

	/**
	 * Internal method for automatically growing the current size of this block
	 * with each write operation.
	 */
	protected synchronized void adjustSize() {
		if (pointer > maxSize) {
			System.err.println("Invalid pointer to be set: " + pointer + " > "
					+ maxSize + " !");
		}
		if (pointer > size) {
			size = pointer;
		}
	}

	/**
	 * Adjusts the maximal allocated number of bytes in this block, used by the
	 * encompassing block data storage.
	 * 
	 * @param aMaxSize
	 *            Maximal size in bytes to be available.
	 */
	public synchronized void setMaxSize(int aMaxSize) {
		if (aMaxSize < size) {
			System.err.println("Invalid maximal size to be set: " + aMaxSize
					+ " > " + size + " !");
		}
		maxSize = aMaxSize;
	}

	/**
	 * Returns the maximal number of bytes that can be allocated by and from
	 * this block.
	 * 
	 * @return Maximal size in bytes available.
	 */
	public synchronized int getMaxSize() {
		return maxSize;
	}

	/**
	 * Returns the free number of bytes, i.e. which can still be allocated, from
	 * this block.
	 * 
	 * @return
	 */
	public synchronized int getFreeBytes() {
		return maxSize - size;
	}

	/**
	 * Returns the fill ratio of this block.
	 * 
	 * @return The percentage of this block which is currently filled with data,
	 *         as value within <code>[0.0, 1.0]</code>
	 */
	public synchronized double fillRatio() {
		return ((double) size / (double) maxSize);
	}

	/**
	 * Returns the logical index of this block in its providing block data
	 * storage.
	 * 
	 * @return
	 */
	public synchronized int getBlockNumber() {
		return blockNumber;
	}

	/**
	 * Sets a new logical index of this bock in its providing block data
	 * storage.<br/>
	 * <b>Warning:</b> Do not use this method unless you know what you are
	 * doing! The internal functionality of a storage block depends on its
	 * alignment with the encompassing block data storage.
	 * 
	 * @param aBlockNumber
	 *            The new logical index of this bock in its providing block data
	 *            storage.
	 */
	public synchronized void setBlockNumber(int aBlockNumber) {
		blockNumber = aBlockNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.io.RandomAccessStorage#close()
	 */
	public synchronized void close() throws IOException {
		size = 0;
		pointer = 0;
		parent.freeBlock(blockNumber);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.RandomAccessStorage#getFilePointer
	 * ()
	 */
	public synchronized long getFilePointer() throws IOException {
		return pointer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.io.RandomAccessStorage#length()
	 */
	public synchronized long length() throws IOException {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.RandomAccessStorage#seek(long)
	 */
	public synchronized void seek(long pos) throws IOException {
		if (pos < 0 || pos >= maxSize) {
			throw new IOException(
					"Required position not valid for this block! (" + pos
							+ " not in [0, " + maxSize + "])");
		} else {
			pointer = (int) pos;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.RandomAccessStorage#skipBytes(int)
	 */
	public synchronized int skipBytes(int n) throws IOException {
		long updatedPointer = pointer + n;
		if (updatedPointer >= size) {
			throw new IOException(
					"Attempting to skip invalid number of bytes for block!");
		} else {
			pointer += n;
			adjustSize();
			return n;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#write(int)
	 */
	public synchronized void write(int arg0) throws IOException {
		parent.writeByte(blockNumber, pointer, arg0);
		pointer++;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#write(byte[])
	 */
	public synchronized void write(byte[] arg0) throws IOException {
		parent.write(blockNumber, pointer, arg0);
		pointer += arg0.length;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#write(byte[], int, int)
	 */
	public synchronized void write(byte[] arg0, int arg1, int arg2)
			throws IOException {
		parent.write(blockNumber, pointer, arg0, arg1, arg2);
		pointer += arg2;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeBoolean(boolean)
	 */
	public synchronized void writeBoolean(boolean arg0) throws IOException {
		parent.writeBoolean(blockNumber, pointer, arg0);
		pointer += 1;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeByte(int)
	 */
	public synchronized void writeByte(int arg0) throws IOException {
		parent.writeByte(blockNumber, pointer, arg0);
		pointer += 1;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeBytes(java.lang.String)
	 */
	public synchronized void writeBytes(String arg0) throws IOException {
		parent.writeBytes(blockNumber, pointer, arg0);
		pointer += arg0.getBytes().length;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeChar(int)
	 */
	public synchronized void writeChar(int arg0) throws IOException {
		parent.writeChar(blockNumber, pointer, arg0);
		pointer += 2;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeChars(java.lang.String)
	 */
	public synchronized void writeChars(String arg0) throws IOException {
		parent.writeChars(blockNumber, pointer, arg0);
		pointer += (arg0.length() * 2);
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeDouble(double)
	 */
	public synchronized void writeDouble(double arg0) throws IOException {
		parent.writeDouble(blockNumber, pointer, arg0);
		pointer += 8;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeFloat(float)
	 */
	public synchronized void writeFloat(float arg0) throws IOException {
		parent.writeFloat(blockNumber, pointer, arg0);
		pointer += 4;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeInt(int)
	 */
	public synchronized void writeInt(int arg0) throws IOException {
		parent.writeInt(blockNumber, pointer, arg0);
		pointer += 4;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeLong(long)
	 */
	public synchronized void writeLong(long arg0) throws IOException {
		parent.writeLong(blockNumber, pointer, arg0);
		pointer += 8;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeShort(int)
	 */
	public synchronized void writeShort(int arg0) throws IOException {
		parent.writeShort(blockNumber, pointer, arg0);
		pointer += 2;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeUTF(java.lang.String)
	 */
	public synchronized void writeUTF(String arg0) throws IOException {
		throw new IOException("Not implemented on this level!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readBoolean()
	 */
	public synchronized boolean readBoolean() throws IOException {
		boolean result = parent.readBoolean(blockNumber, pointer);
		pointer++;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readByte()
	 */
	public synchronized byte readByte() throws IOException {
		byte result = parent.readByte(blockNumber, pointer);
		pointer++;
		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readChar()
	 */
	public synchronized char readChar() throws IOException {
		char result = parent.readChar(blockNumber, pointer);
		pointer += 2;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readDouble()
	 */
	public synchronized double readDouble() throws IOException {
		double result = parent.readDouble(blockNumber, pointer);
		pointer += 8;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFloat()
	 */
	public synchronized float readFloat() throws IOException {
		float result = parent.readFloat(blockNumber, pointer);
		pointer += 4;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[])
	 */
	public synchronized void readFully(byte[] arg0) throws IOException {
		parent.readFully(blockNumber, pointer, arg0);
		pointer += arg0.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	public synchronized void readFully(byte[] arg0, int arg1, int arg2)
			throws IOException {
		parent.readFully(blockNumber, pointer, arg0, arg1, arg2);
		pointer += arg2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readInt()
	 */
	public synchronized int readInt() throws IOException {
		int result = parent.readInt(blockNumber, pointer);
		pointer += 4;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLine()
	 */
	public synchronized String readLine() throws IOException {
		throw new IOException("Not implemented on this level!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLong()
	 */
	public synchronized long readLong() throws IOException {
		long result = parent.readLong(blockNumber, pointer);
		pointer += 8;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readShort()
	 */
	public synchronized short readShort() throws IOException {
		short result = parent.readShort(blockNumber, pointer);
		pointer += 2;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUTF()
	 */
	public synchronized String readUTF() throws IOException {
		throw new IOException("Not implemented on this level!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	public synchronized int readUnsignedByte() throws IOException {
		throw new IOException("Not implemented on this level!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	public synchronized int readUnsignedShort() throws IOException {
		throw new IOException("Not implemented on this level!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.io.RandomAccessStorage#copy()
	 */
	public RandomAccessStorage copy() throws IOException {
		throw new IOException("Not implemented on this level!");
	}

}
