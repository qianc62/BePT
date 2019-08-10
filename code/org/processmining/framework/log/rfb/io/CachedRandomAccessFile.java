/**
 * Project: ProM
 * File: CachedRandomAccessFile.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 25, 2006, 12:36:52 AM
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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class implements the interface of ManagedRandomAccessFile, providing
 * high-level type read and write operations on a managed random access file.
 * <p>
 * On top of such managed random access file, this class provides a transparent
 * caching layer, which is supposed to speed up read operations by limiting
 * actual calls to the low-level I/O layer.
 * <p>
 * Caching is performed read-only, which has, in its current implementation, the
 * following implications:
 * <ul>
 * <li>Sequential read access (in forward direction) is sped up considerably,
 * especially when frequently only small amounts of data are accessed at once
 * (e.g. when reading integers, longs, etc.).</li>
 * <li>This speedup on sequential reads is also retained, if forward seeks, or
 * skips of bytes, are performed within the cached area.</li>
 * <li>Consecutive write access disrupts caching, but has no negative
 * performance implications when compared to the un-cached managed random access
 * file.</li>
 * <li>Interleaving read/write access, or backward seeks between reads, will
 * affect the performance drastically in a negative way. Given a sound
 * block-oriented I/O implementation of operating system and JRE, this should
 * however be barely noticeable (if not performed excessively).</li>
 * <li>Instances of this class consume a static amount of memory for the cache,
 * whose size in bytes can be defined in the constructor. They will also reserve
 * the same amount of memory if the actual buffered file is smaller than the
 * cache size.</li>
 * </ul>
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 * 
 */
public class CachedRandomAccessFile extends ManagedRandomAccessFile {

	/**
	 * Defines the default cache size, which is used when no value is provided
	 * in the constructor.
	 */
	public static int DEFAULT_CACHE_SIZE = 4096;
	/**
	 * Size of the cache, in bytes.
	 */
	protected int cacheSize = 0;
	/**
	 * Describes the remaining valid bytes in the cache.
	 */
	protected int cacheValidSize = 0;
	/**
	 * Describes the offset within the cached file, which is represented by the
	 * first byte currently in the cache.
	 */
	protected long cacheOffset = 0;
	/**
	 * Cache for underlying file data.
	 */
	protected byte cache[] = null;
	/**
	 * Indicates, whether the cache has been invalidated (e.g. by a write
	 * operation to the underlying file).
	 */
	protected boolean cacheDirty = true;
	/**
	 * Byte buffer which abstracts from the cache for read operations of basic
	 * Java types.
	 */
	protected ByteBuffer buffer = null;

	/**
	 * Creates a new instance caching the given amount of bytes in heap memory.
	 * 
	 * @param cacheSize
	 *            Size of the memory cache, in bytes.
	 * @throws IOException
	 */
	public CachedRandomAccessFile(int cacheSize) throws IOException {
		super();
		this.cacheSize = cacheSize;
		cacheValidSize = 0;
		cacheOffset = 0;
		cache = new byte[cacheSize];
		cacheDirty = true;
		buffer = ByteBuffer.wrap(cache);
	}

	/**
	 * Creates a new instance caching the default amount of bytes in heap
	 * memory.
	 * 
	 * @throws IOException
	 */
	public CachedRandomAccessFile() throws IOException {
		this(CachedRandomAccessFile.DEFAULT_CACHE_SIZE);
	}

	/**
	 * Creates a new instance which is a clone of the provided template
	 * instance. The new instance will contain an exact copy of the provided
	 * template from the beginning.
	 * <p>
	 * Changes are defined to be not synchronized between template and clone,
	 * i.e. the created clone will behave as a truly separate and independent
	 * instance after creation.
	 * 
	 * @param template
	 *            Template to create a clone from.
	 * @throws IOException
	 */
	public CachedRandomAccessFile(CachedRandomAccessFile template)
			throws IOException {
		super(template);
		cacheSize = template.cacheSize;
		cacheValidSize = 0;
		cacheOffset = 0;
		cache = new byte[cacheSize];
		cacheDirty = true;
		buffer = ByteBuffer.wrap(cache);
	}

	@Override
	public RandomAccessStorage copy() throws IOException {
		return new CachedRandomAccessFile(this);
	}

	/**
	 * Ensures that the given amount of bytes from the current file position are
	 * available from the cache. This implies potentially loading data from the
	 * managed file into the cache and ajusting the buffer position.
	 * 
	 * @param size
	 *            Number of bytes to be cached, referred from the current
	 *            (virtual) file pointer position.
	 *            <p>
	 *            <b>Notice:</b> It is assumed that the given number of bytes is
	 *            between zero and the cache size. This property must be ensured
	 *            outside of this method!
	 * @throws IOException
	 */
	protected synchronized void cache(int size) throws IOException {
		/*
		 * One of three conditions triggers reading data from the file system
		 * into the cache buffer: 1) The cache is dirty from a previous write
		 * operation 2) The requested area ends after the cached area 3) The
		 * requested area begins before the cached area
		 */
		if ((cacheDirty == true)
				|| (currentFilePointer + size > cacheOffset + cacheValidSize)
				|| (currentFilePointer < cacheOffset)) {
			// requested data is not (completely) in cache.
			// remember file offset corresponding to first byte in cache
			cacheOffset = currentFilePointer;
			// read directly from file system.
			// (this will modify the currentFilePointer value in super class)
			cacheValidSize = raf().read(cache);
			// reset virtual current file pointer to beginning of cache
			currentFilePointer = cacheOffset;
			// reset byte buffer to point to beginning of cache
			buffer.rewind();
		} else {
			// adjust buffer position on the cache
			// (potentially modified by previous seeks or skips)
			buffer.position((int) (currentFilePointer - cacheOffset));
		}
	}

	/**
	 * Retrieves a ByteBuffer, which can read the given number of bytes from the
	 * current (virtual) file pointer position. This method will ensure that
	 * caching will be performed where necessary. Where caching is not possible,
	 * due to the size of the requested area exceeding the cache size, the given
	 * number of bytes are directly fetched and wrapped in a byte buffer. Notice
	 * that this will affect performance significantly and should not happen too
	 * frequently (as it invalidates the cache).
	 * 
	 * @param size
	 *            Number of bytes, that are to be read from the provided byte
	 *            buffer.
	 *            <p>
	 *            <b>Warning:</b> This method relies on the correctness of this
	 *            value, which must be absolute and correct; i.e. this is not an
	 *            upper bound, but must be exactly the number of bytes actually
	 *            read from the byte buffer. If this is not ensured, the
	 *            internal state of this instance will become inconsistend,
	 *            rendering future read operation results unpredictable.
	 * @return A byte buffer to read the requested number of bytes from.
	 * @throws IOException
	 */
	protected synchronized ByteBuffer buffer(int size) throws IOException {
		if (size <= cacheSize) {
			// cache can hold the requested number of bytes. cache the
			// requested data and return the byte buffer
			cache(size);
			// advance virtual file pointer trusting number of bytes given
			// as parameter
			currentFilePointer += size;
			// decrease valid cache size accordingly
			cacheValidSize -= size;
			return buffer;
		} else {
			// cache cannot hold the requested number of bytes. create a
			// new intermediate cache and return a new byte buffer over this
			// array.
			// NOTICE: This is an expensive thing to do, and should be evaded
			// by setting the cache size appropriately for the expected maximal
			// data size to be read at once.
			// first invalidate cache
			cacheValidSize = 0;
			cacheDirty = true;
			byte overflow[] = new byte[size];
			super.readFully(overflow);
			return ByteBuffer.wrap(overflow);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.ManagedRandomAccessFile#close()
	 */
	public synchronized void close() throws IOException {
		// free cache buffer for garbage collection
		cache = null;
		super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readBoolean()
	 */
	public synchronized boolean readBoolean() throws IOException {
		return (buffer(1).get() != 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readByte()
	 */
	public synchronized byte readByte() throws IOException {
		return buffer(1).get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readChar()
	 */
	public synchronized char readChar() throws IOException {
		return buffer(2).getChar();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readDouble()
	 */
	public synchronized double readDouble() throws IOException {
		return buffer(8).getDouble();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readFloat()
	 */
	public synchronized float readFloat() throws IOException {
		return buffer(4).getFloat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readFully
	 * (byte[], int, int)
	 */
	public synchronized void readFully(byte[] arg0, int offset, int length)
			throws IOException {
		buffer(offset + length).get(arg0, offset, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readFully
	 * (byte[])
	 */
	public synchronized void readFully(byte[] arr) throws IOException {
		buffer(arr.length).get(arr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readInt()
	 */
	public synchronized int readInt() throws IOException {
		return buffer(4).getInt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readLine()
	 */
	public synchronized String readLine() throws IOException {
		return readUTF();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readLong()
	 */
	public synchronized long readLong() throws IOException {
		return buffer(8).getLong();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readShort()
	 */
	public synchronized short readShort() throws IOException {
		return buffer(2).getShort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readUnsignedByte
	 * ()
	 */
	public synchronized int readUnsignedByte() throws IOException {
		// read short and normalize to positive value
		return (int) buffer(1).get() + 128;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readUnsignedShort
	 * ()
	 */
	public synchronized int readUnsignedShort() throws IOException {
		// read short and normalize to positive value
		return readShort() + 32768;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#readUTF()
	 */
	public synchronized String readUTF() throws IOException {
		// read length of UTF-8 encoded string in bytes
		byte content[] = new byte[readInt()];
		// read bytes of UTF-8 encoded string
		this.readFully(content);
		// assemble and return string
		return new String(content, "UTF-8");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#skipBytes
	 * (int)
	 */
	public synchronized int skipBytes(int arg0) throws IOException {
		// optimize: if file pointer would be within or before the
		// current cache, skipping is possible for sure, and can thus
		// be performed virtually.
		if ((currentFilePointer + arg0) < (cacheOffset + cacheValidSize)) {
			currentFilePointer += arg0;
			return arg0;
		} else {
			// skip result not known for sure; make call to super class.
			return super.skipBytes(arg0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#write(byte[],
	 * int, int)
	 */
	public synchronized void write(byte[] arg, int arg1, int arg2)
			throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.write(arg, arg1, arg2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#write(byte[])
	 */
	public synchronized void write(byte[] arg) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.write(arg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#write(int)
	 */
	public synchronized void write(int val) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.write(val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeBoolean
	 * (boolean)
	 */
	public synchronized void writeBoolean(boolean bool) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeBoolean(bool);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeByte
	 * (int)
	 */
	public synchronized void writeByte(int arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeByte(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeBytes
	 * (java.lang.String)
	 */
	public synchronized void writeBytes(String arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeBytes(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeChar
	 * (int)
	 */
	public synchronized void writeChar(int arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeChar(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeChars
	 * (java.lang.String)
	 */
	public synchronized void writeChars(String arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeChars(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeDouble
	 * (double)
	 */
	public synchronized void writeDouble(double arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeDouble(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeFloat
	 * (float)
	 */
	public synchronized void writeFloat(float arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeFloat(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeInt(int)
	 */
	public synchronized void writeInt(int arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeInt(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeLong
	 * (long)
	 */
	public synchronized void writeLong(long arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeLong(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeShort
	 * (int)
	 */
	public synchronized void writeShort(int arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeShort(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.ManagedRandomAccessFile#writeUTF(
	 * java.lang.String)
	 */
	public synchronized void writeUTF(String arg0) throws IOException {
		// writing is not cached.
		// invalidate cache and delegate to super class.
		cacheDirty = true;
		super.writeUTF(arg0);
	}

}
