/**
 * Project: ProM
 * File: VirtFsRandomAccessStorage.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 15, 2006, 10:58:49 PM
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
import java.util.ArrayList;

import org.processmining.framework.ui.Message;

/**
 * This class implements a file metaphor, i.e. an ordered set of bytes which can
 * be randomly accessed and modified. As it is dynamically backed by a virtual
 * file system, the VFS/NIKEFS restrictions apply:
 * <p>
 * <ul>
 * <li>Created files will persist during runtime of the JVM.</li>
 * <li>After closing them, their resources will be freed by the virtual file
 * system.</li>
 * <li>Modification of file metaphors, other than initial appending of data, is
 * not encouraged. If you want to modify a virtual file, make sure you use the
 * same block width (number of bytes written in bulk) that you used when first
 * writing to the respective section of the file.</li>
 * <li>Performance is backed by NIO memory-mapped files in the virtual file
 * system. In practice, this can be very fast, but also suffer from
 * ill-partitioned swap files or an overloaded / fragmented virtual file system.
 * </li>
 * </ul>
 * <p>
 * This file metaphor has the restriction, that the file must either be written
 * in exactly the same granularity on re-writing specific address spaces, or
 * preferably no modification other than appending data does occur.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class VirtFsRandomAccessStorage implements RandomAccessStorage {

	/**
	 * Static instance counter, used to provide short names to instances of this
	 * class.
	 */
	protected static int INSTANCE_COUNTER = 0;
	/**
	 * This constant holds the maximal number of blocks that a virtual file /
	 * random access storage from this class can be distributed over. If this
	 * number of blocks is exceeded, the contents of the 'file' will be
	 * transferred to a newly allocated block which can accomodate the complete
	 * size of the 'file'.
	 */
	public static final int MAX_BLOCKS_PER_STORAGE = 4;

	/**
	 * Instance number, for short reference
	 */
	protected int instanceNumber = 0;
	/**
	 * Virtual file system from which blocks are allocated
	 */
	protected VirtualFileSystem fs = null;
	/**
	 * List of blocks representing this file's contents
	 */
	protected ArrayList<StorageBlock> blocks = null;
	/**
	 * Current virtual file pointer
	 */
	protected long pointer = 0;

	/**
	 * Creates a new virtual file metaphor, based on the given virtual file
	 * system object for allocating blocks.
	 * 
	 * @param vfs
	 *            Virtual file system providing blocks for this file.
	 */
	public VirtFsRandomAccessStorage(VirtualFileSystem vfs) {
		fs = vfs;
		blocks = new ArrayList<StorageBlock>();
		pointer = 0;
		synchronized (VirtFsRandomAccessStorage.class) {
			instanceNumber = VirtFsRandomAccessStorage.INSTANCE_COUNTER++;
		}
	}

	/**
	 * Returns a block, to which the specified number of bytes can be safely
	 * written, starting from the current pointer. The block is preconfigured to
	 * point to the given virtual offset internally before it is returned.
	 * 
	 * @param bytes
	 *            Number of bytes to be written.
	 * @return A storage block, to which the specified number of bytes can be
	 *         written at the specified position.
	 * @throws IOException
	 */
	protected synchronized StorageBlock getBlockForWriting(int bytes)
			throws IOException {
		// sanity check to spot implementation errors
		if (bytes <= 0) {
			throw new IOException(
					"Requested block for writing invalid number of bytes ("
							+ bytes + ")!");
		}
		// set pointer to updated position, the correct position
		// as *after* the requested write operation.
		pointer += bytes;
		// safety check: present implementation has a maximal file size limit of
		// 2GB (int_max)
		if (pointer > Integer.MAX_VALUE) {
			throw new IOException(
					"NIKEFS: Current implementation has a maximal virtual "
							+ " file size limit of " + Integer.MAX_VALUE
							+ " bytes!");
		}
		long nPointer = pointer; // marks righter limit pointer
		StorageBlock block = null;
		int lastBlockIndex = blocks.size() - 1;
		// iterate over current blocks in set
		for (int i = 0; i <= lastBlockIndex; i++) {
			if (nPointer < bytes) {
				// signal obvious contract violation or implementation fault:
				// this point should not be reached in regular operation!
				throw new IOException(
						"Invalid pointer offset: cannot determine"
								+ " correct block for write access.");
			}
			block = blocks.get(i);
			// case 1: this is the last block and can accomodate the
			// requested bytes; or, this is a regular block comprising
			// the byte range to be written
			// -> adjust block pointer and return.
			if ((i == lastBlockIndex && block.getMaxSize() >= nPointer)
					|| (block.length() >= nPointer)) {
				long offset = nPointer - bytes;
				block.seek(offset);
				return block;
				// case 2: this the last block and cannot accomodate the
				// requested number of bytes (excluded by last test)
				// -> allocate new block, add to list, and return it.
			} else if (i == lastBlockIndex) {
				// check first, if the maximal number of blocks has been reached
				if (blocks.size() >= VirtFsRandomAccessStorage.MAX_BLOCKS_PER_STORAGE) {
					// consolidate the virtual file
					block = consolidateBlocks((int) pointer);
					// update internal pointer of new block
					block.seek(pointer - bytes);
				} else {
					// allocate new block
					// System.out.println("NIKEFS: Growing vFile " +
					// instanceNumber + " to " + (blocks.size() + 1) +
					// " blocks.");
					block = fs.allocateBlock(bytes);
					blocks.add(block);
				}
				return block;
				// decrement right offset pointer by length of current block
				// and test next block (iteration).
			} else {
				nPointer -= block.length();
			}
		}
		// this point can be reached, if no block has been allocated yet
		if (blocks.size() == 0) {
			block = fs.allocateBlock(bytes);
			blocks.add(block);
			return block;
		} else {
			// signal obvious contract violation or implementation fault:
			// this point should not be reached in regular operation!
			throw new IOException("Serious iteration error: cannot determine"
					+ " correct block for write access.");
		}

	}

	/**
	 * Consolidates the virtual file, i.e.: allocates a new block that can
	 * accomodate at least the size of the currently contained blocks, copy the
	 * contents of these to the new block, and finally replace the previous
	 * blocks with the new block.<br/>
	 * This prevents scattering of virtual files over too many blocks, thus
	 * increasing R/W performance of the virtual file system. Notice, however,
	 * that consolidating virtual files usually goes hand in hand with increased
	 * swap disk space consumption, as the virtual file system can not manage
	 * the available real disk space as efficiently as before.
	 * 
	 * @param minNewSize
	 *            Minimal size, in bytes, the newly allocated block should be
	 *            able to accomodate.
	 * @return The newly allocated block (already added to the list of blocks)
	 * @throws IOException
	 */
	protected synchronized StorageBlock consolidateBlocks(int minNewSize)
			throws IOException {
		// measure performance
		long time = System.currentTimeMillis();
		// allocate new block with minimal required size from
		// the virtual file system
		StorageBlock nBlock = fs.allocateBlock(minNewSize);
		long oldSize = this.length();
		// copy the contents of all contained blocks sequentially
		// to the newly allocated block
		byte buffer[] = new byte[4096];
		int bufSize = 0;
		for (StorageBlock block : blocks) {
			block.seek(0);
			bufSize = (int) block.length();
			// read block in large chunks (full buffer size)
			while (bufSize >= buffer.length) {
				block.readFully(buffer);
				nBlock.write(buffer);
				bufSize -= buffer.length;
			}
			// if one chunk < bufSize remains, read these remains and finish up
			if (bufSize > 0) {
				block.readFully(buffer, 0, bufSize);
				nBlock.write(buffer, 0, bufSize);
			}
			// close block, while we're at it
			block.close();
		}
		// remove all previous blocks, replace them
		// with the newly allocated, consolidated block
		blocks.clear();
		if (nBlock.length() != oldSize || blocks.size() != 0) {
			System.err.println("CONSOLIDATION MALFUNCTION!!!");
		}
		blocks.add(nBlock);
		// print information and return
		time = System.currentTimeMillis() - time;
		Message.add("NikeFS: Consolidated vFile.#" + instanceNumber
				+ ": new block w/ " + nBlock.getMaxSize() + " bytes ("
				+ minNewSize + " req'd) in " + time + " msec.", Message.DEBUG);
		return nBlock;
	}

	/**
	 * Returns a block for safely reading the specified number of bytes from the
	 * current file pointer offset. The block is preconfigured to point to the
	 * given virtual offset internally before it is returned.
	 * 
	 * @param bytes
	 *            Number of bytes to be read.
	 * @return Block for reading the given number of bytes safely.
	 * @throws IOException
	 */
	protected synchronized StorageBlock getBlockForReading(int bytes)
			throws IOException {
		// sanity check to spot implementation errors
		if (bytes <= 0) {
			throw new IOException(
					"Requested block for writing invalid number of bytes ("
							+ bytes + ")!");
		}
		// adjust pointer to new position (as if after reading the
		// specified number of bytes).
		pointer += bytes;
		long nPointer = pointer;
		StorageBlock block = null;
		int lastBlockIndex = blocks.size() - 1;
		// iterate over current blocks in set
		for (int i = 0; i <= lastBlockIndex; i++) {
			block = blocks.get(i);
			if (nPointer < bytes) {
				// signal obvious contract violation or implementation fault:
				// this point should not be reached in regular operation!
				throw new IOException(
						"Invalid pointer offset: cannot determine"
								+ " correct block for read access. (2)");
				// case 1: current block comprises the byte range to be read
				// -> adjust internal block offset and return it.
			} else if (block.length() >= nPointer) {
				// adjust block's internal offset
				block.seek(nPointer - bytes);
				return block;
				// else: adjust right delimiter pointer by subtracting current
				// block's length, and turn to next block (iteration).
			} else {
				nPointer -= block.length();
				if (nPointer < bytes) {
					// signal obvious contract violation or implementation
					// fault:
					// this point should not be reached in regular operation!
					throw new IOException(
							"Invalid pointer offset: cannot determine"
									+ " correct block for read access. (1)");
				}
			}
		}
		// signal obvious contract violation or implementation fault:
		// this point should not be reached in regular operation!
		throw new IOException(
				"Serious iteration error: cannot determine correct"
						+ " block for read access.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.io.RandomAccessStorage#close()
	 */
	public synchronized void close() throws IOException {
		for (StorageBlock block : blocks) {
			block.close();
		}
		blocks.clear();
		fs = null;
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
		long result = 0;
		for (StorageBlock block : blocks) {
			result += block.length();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.RandomAccessStorage#seek(long)
	 */
	public synchronized void seek(long pos) throws IOException {
		pointer = pos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.RandomAccessStorage#skipBytes(int)
	 */
	public synchronized int skipBytes(int n) throws IOException {
		pointer += n;
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#write(int)
	 */
	public synchronized void write(int b) throws IOException {
		getBlockForWriting(1).write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#write(byte[])
	 */
	public synchronized void write(byte[] b) throws IOException {
		getBlockForWriting(b.length).write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#write(byte[], int, int)
	 */
	public synchronized void write(byte[] b, int off, int len)
			throws IOException {
		getBlockForWriting(len).write(b, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeBoolean(boolean)
	 */
	public synchronized void writeBoolean(boolean v) throws IOException {
		getBlockForWriting(1).writeBoolean(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeByte(int)
	 */
	public synchronized void writeByte(int v) throws IOException {
		getBlockForWriting(1).writeByte(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeBytes(java.lang.String)
	 */
	public synchronized void writeBytes(String s) throws IOException {
		getBlockForWriting(s.getBytes().length).writeBytes(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeChar(int)
	 */
	public synchronized void writeChar(int v) throws IOException {
		getBlockForWriting(2).writeChar(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeChars(java.lang.String)
	 */
	public synchronized void writeChars(String s) throws IOException {
		getBlockForWriting(s.length() * 2).writeChars(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeDouble(double)
	 */
	public synchronized void writeDouble(double v) throws IOException {
		getBlockForWriting(8).writeDouble(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeFloat(float)
	 */
	public synchronized void writeFloat(float v) throws IOException {
		getBlockForWriting(4).writeFloat(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeInt(int)
	 */
	public synchronized void writeInt(int v) throws IOException {
		getBlockForWriting(4).writeInt(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeLong(long)
	 */
	public synchronized void writeLong(long v) throws IOException {
		getBlockForWriting(8).writeLong(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeShort(int)
	 */
	public synchronized void writeShort(int v) throws IOException {
		getBlockForWriting(2).writeShort(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeUTF(java.lang.String)
	 */
	public synchronized void writeUTF(String str) throws IOException {
		throw new IOException("Not supported on this file metaphor!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readBoolean()
	 */
	public synchronized boolean readBoolean() throws IOException {
		return getBlockForReading(1).readBoolean();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readByte()
	 */
	public synchronized byte readByte() throws IOException {
		return getBlockForReading(1).readByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readChar()
	 */
	public synchronized char readChar() throws IOException {
		return getBlockForReading(2).readChar();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readDouble()
	 */
	public synchronized double readDouble() throws IOException {
		return getBlockForReading(8).readDouble();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFloat()
	 */
	public synchronized float readFloat() throws IOException {
		return getBlockForReading(4).readFloat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[])
	 */
	public synchronized void readFully(byte[] b) throws IOException {
		getBlockForReading(b.length).readFully(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	public synchronized void readFully(byte[] b, int off, int len)
			throws IOException {
		getBlockForReading(len).readFully(b, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readInt()
	 */
	public synchronized int readInt() throws IOException {
		return getBlockForReading(4).readInt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLine()
	 */
	public synchronized String readLine() throws IOException {
		throw new IOException("Not supported on this file metaphor!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLong()
	 */
	public synchronized long readLong() throws IOException {
		return getBlockForReading(8).readLong();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readShort()
	 */
	public synchronized short readShort() throws IOException {
		return getBlockForReading(2).readShort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUTF()
	 */
	public synchronized String readUTF() throws IOException {
		throw new IOException("Not supported on this file metaphor!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	public synchronized int readUnsignedByte() throws IOException {
		return getBlockForReading(1).readUnsignedByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	public synchronized int readUnsignedShort() throws IOException {
		return getBlockForReading(1).readUnsignedShort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.io.RandomAccessStorage#copy()
	 */
	public synchronized RandomAccessStorage copy() throws IOException {
		// create a copy of this virtual random access storage / file.
		// Use NIO methods to copy each block over.
		// It is important that each block is copied in one chunk, i.e. at
		// once. If not, this will interfere with the way ATERandomFileBuffer
		// writes to blocks in a sparse way (leaving potentially small chunks
		// of a block free at the end if they cannot hold the requested size).
		// For example, an integer could have its two bytes distributed over
		// two different blocks, thus this integer cannot be read correctly
		// as a consequence.
		// The copied file will hold the exact same contents
		// as this instance, while being newly distributed over available
		// blocks (Do not use this method for brute-force defragmentation,
		// though).
		VirtFsRandomAccessStorage clone = new VirtFsRandomAccessStorage(fs);
		// determine size of largest block in storage
		int maxBlockSize = 0;
		for (StorageBlock block : blocks) {
			maxBlockSize = Math.max(maxBlockSize, (int) block.length());
		}
		// create upper-bounded heap buffer
		byte buffer[] = new byte[maxBlockSize];
		// copy all blocks at once
		int curBlockSize = 0;
		for (StorageBlock block : blocks) {
			block.seek(0);
			curBlockSize = (int) block.length();
			block.readFully(buffer, 0, curBlockSize);
			clone.write(buffer, 0, curBlockSize);
		}
		// safety check
		if (this.length() != clone.length()) {
			throw new IOException("ERROR!");
		}
		return clone;
	}

}
