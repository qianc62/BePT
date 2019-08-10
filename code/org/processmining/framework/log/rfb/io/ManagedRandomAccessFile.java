/**
 * Project: ProM
 * File: ManagedRandomAccessFile.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 24, 2006, 8:53:04 PM
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * This class represents the typical interface of a random access file, i.e. the
 * <code>DataInput</code> and <code>DataOutput</code> interfaces plus navigation
 * and closing functionality.
 * <p>
 * Within all instances of this class, a given limit of open files is ensured.
 * The lightweight wrapper instances automatically care for restoring the
 * underlying instance's state when it had been closed intermediately.
 * <p>
 * Notice that the wrapper's <code>seek()</code> method has been implemented in
 * a lazy fashion, i.e. it will only access I/O when necessary.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 * 
 */
public class ManagedRandomAccessFile implements RandomAccessStorage {

	/*
	 * This installs the shutdown hook, which will clear all files backing
	 * instances from this class when the virtual machine shuts down. The
	 * shutdown hook is installed at the moment, at which this class is first
	 * loaded by the JVM.
	 */
	static {
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		System.out.println("shutdown hook installed");
	}

	/**
	 * Prefix for temporary buffer files.
	 */
	protected static final String TEMP_FILE_PREFIX = "PROM_ATERND";
	/**
	 * Suffix for temporary buffer files.
	 */
	protected static final String TEMP_FILE_SUFFIX = ".atedb";

	/*
	 * Static open file handles management facilities
	 */
	/**
	 * Counts the global number of open random access files held by instances of
	 * this class
	 */
	protected static int openFilesCounter = 0;
	/**
	 * Holds references of instances of this class, which currently hold an open
	 * random access file
	 */
	protected static ArrayList<ManagedRandomAccessFile> openFilesList = new ArrayList<ManagedRandomAccessFile>();
	/**
	 * Defines the maximal number of open random access files concurrently held
	 * by instances of this class
	 */
	protected static int maxOpenFiles = 250;

	/**
	 * Acquires an open file slot for the calling instance and registers it
	 * accordingly. Transparently closes the last opened file of another
	 * instance, if limit is to be exceeded.
	 * 
	 * @param customer
	 *            Calling instance requiring an open file slot(<code>this</code>
	 *            reference)
	 * @throws IOException
	 */
	protected synchronized static void retrieveOpenFileSlot(
			ManagedRandomAccessFile customer) throws IOException {
		// probe, whether maximum open files limit has been exceeded
		if (openFilesCounter >= maxOpenFiles) {
			// close the last opened file
			ManagedRandomAccessFile victim = (ManagedRandomAccessFile) openFilesList
					.remove(0);
			victim.closeHandle();
			// adjust counter
			openFilesCounter--;
		}
		// provide slot and save reference
		openFilesList.add(customer);
		// adjust counter
		openFilesCounter++;
	}

	/**
	 * Called by instances of this class to signal release of a currently held
	 * open file handle (when closing the instance).
	 * 
	 * @param customer
	 *            Calling instance freeing an open file slot(<code>this</code>
	 *            reference)
	 * @throws IOException
	 */
	protected synchronized static void releaseOpenFileSlot(
			ManagedRandomAccessFile customer) throws IOException {
		if (openFilesList.remove(customer) == true) {
			openFilesCounter--;
		}
	}

	/*
	 * Instance declarations
	 */

	protected File file = null;
	protected RandomAccessFile raf = null;
	protected long currentFilePointer = 0;
	protected boolean isOpen = false;

	/**
	 * Creates a new managed instance, which is based on the given file.
	 * 
	 * @param aFile
	 *            The file which to access in a random fashion.
	 * @throws IOException
	 */
	public ManagedRandomAccessFile() throws IOException {
		// initialize
		file = ManagedRandomAccessFile.createTempFile();
		raf = null;
		currentFilePointer = 0;
		isOpen = false;
	}

	/**
	 * Creates a new managed instance, which is based on the given file. The
	 * created instance will be an exact copy, or clone, of the given template
	 * instance. Modifications are not synchronized between template and clone,
	 * i.e. they behave as truly separate instances that are only exactly the
	 * same after construction.
	 * 
	 * @param template
	 *            The template to create a clone from.
	 * @param aFile
	 *            File which is to be backing the clone to be created.
	 * @throws IOException
	 */
	public ManagedRandomAccessFile(ManagedRandomAccessFile template)
			throws IOException {
		file = ManagedRandomAccessFile.createTempFile();
		raf = null;
		currentFilePointer = template.currentFilePointer;
		isOpen = false;
		copyFile(template.file, file);
	}

	/**
	 * Deletes the underlying file from the file system.
	 * <p>
	 * <b>Warning:</b> The usage contract is, that after calling this method no
	 * reading or modifying method may be called. Otherwise, unexpected results
	 * may be produced.
	 * 
	 * @return Whether deleting the underlying file was successful
	 * @throws IOException
	 */
	public synchronized boolean delete() throws IOException {
		close();
		return file.delete();
	}

	/**
	 * Deletes the underlying file from the file system when the virtual machine
	 * is shut down.
	 * <p>
	 * <b>Warning:</b> The usage contract is, that after calling this method no
	 * reading or modifying method may be called. Otherwise, unexpected results
	 * may be produced.
	 * 
	 * @return Whether deleting the underlying file was successful
	 * @throws IOException
	 */
	public synchronized void deleteOnExit() throws IOException {
		close();
		file.deleteOnExit();
	}

	/**
	 * Closes this instance virtually (flushes and releases the managed file)
	 * 
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {
		if (isOpen) {
			raf().close();
			ManagedRandomAccessFile.releaseOpenFileSlot(this);
			isOpen = false;
		}
	}

	/**
	 * Retrieves the current offset within the managed file, from which the next
	 * read or write occurs.
	 * 
	 * @return current offset within the managed file
	 */
	public synchronized long getFilePointer() {
		return currentFilePointer;
	}

	/**
	 * Retrieves the current size of the managed file
	 * 
	 * @return current underlying file size in bytes
	 * @throws IOException
	 */
	public synchronized long length() throws IOException {
		return raf().length();
	}

	/**
	 * Sets the file pointer offset to the given position.
	 * <p>
	 * Notice that this method behaves different from the identical method in
	 * <code>RandomAccessFile</code>, in that it does not necessarily trigger a
	 * low-level I/O call, as this class implements lazy seeking (seeking is
	 * only performed at the point in time, at which it becomes necessary for
	 * access).
	 * 
	 * @param position
	 *            The offset in bytes from the beginning of the managed file, at
	 *            which to read or write the next data.
	 */
	public synchronized void seek(long position) {
		currentFilePointer = position;
	}

	/*
	 * Protected helper and management methods
	 */

	/**
	 * Called by the static open file management facility, when the underlying
	 * file has to be closed due to open file limit restrictions.
	 * <p>
	 * Usage contract: This method must only be called on instances which are
	 * guaranteed to hold an open file reference at the point of calling!
	 */
	protected synchronized void closeHandle() throws IOException {
		// it is ensured by the usage contract, that
		// this instance's file is currently open.
		// close file and reset (to not be error tolerant!)
		raf.close();
		raf = null;
		isOpen = false;
	}

	/**
	 * Re-opens the managed random access file, implies retrieving an open file
	 * slot from the static management facilities.
	 */
	protected synchronized void reOpen() throws IOException {
		// retrieve open file slot
		ManagedRandomAccessFile.retrieveOpenFileSlot(this);
		// adjust internal state
		isOpen = true;
		// restore random access file
		raf = new RandomAccessFile(file, "rw");
	}

	/**
	 * Provides wrapped access to the managed random access file. Use only this
	 * method, and not the attribute reference directly. Calling this method
	 * will transparently ensure, that the returned random access file
	 * references an open instance which is intelligently reset to the previous
	 * file pointer position if necessary (lazy seeking)
	 * 
	 * @return the managed random access file handle for internal use
	 * @throws IOException
	 */
	protected synchronized RandomAccessFile raf() throws IOException {
		if (isOpen == false) {
			reOpen();
			// restore previous file pointer position
			raf.seek(currentFilePointer);
		} else {
			// file was open; check if file pointer has been modified,
			// i.e. if a seek is necessary
			if (raf.getFilePointer() != currentFilePointer) {
				raf.seek(currentFilePointer);
			}
		}
		return raf;
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void write(int val) throws IOException {
		raf().write(val);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void write(byte[] arg) throws IOException {
		raf().write(arg);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void write(byte[] arg, int arg1, int arg2)
			throws IOException {
		raf().write(arg, arg1, arg2);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeBoolean(boolean bool) throws IOException {
		raf().writeBoolean(bool);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeByte(int arg0) throws IOException {
		raf().writeByte(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeShort(int arg0) throws IOException {
		raf().writeShort(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeChar(int arg0) throws IOException {
		raf().writeChar(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeInt(int arg0) throws IOException {
		raf().writeInt(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeLong(long arg0) throws IOException {
		raf().writeLong(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeFloat(float arg0) throws IOException {
		raf().writeFloat(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeDouble(double arg0) throws IOException {
		raf().writeDouble(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeBytes(String arg0) throws IOException {
		raf().writeBytes(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataOutput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeChars(String arg0) throws IOException {
		raf().writeChars(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Warning:</b> This method uses a custom format to encode UTF-8 strings
	 * to bytes than specified in DataOutput.
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataOutput
	 */
	public synchronized void writeUTF(String arg0) throws IOException {
		// convert string to UTF-8 encoded bytes
		byte[] content = arg0.getBytes("UTF-8");
		// record resulting array length first
		raf().writeInt(content.length);
		// record string content bytes
		raf().write(content);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized void readFully(byte[] arg0) throws IOException {
		raf().readFully(arg0);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized void readFully(byte[] arg0, int arg1, int arg2)
			throws IOException {
		raf().readFully(arg0, arg1, arg2);
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized int skipBytes(int arg0) throws IOException {
		// low-level I/O call can not be evaded (without keeping
		// the current file size, which is currently not implemented),
		// so we can perform this on the underlying file directly.
		int skipped = raf().skipBytes(arg0);
		currentFilePointer += skipped;
		return skipped;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized boolean readBoolean() throws IOException {
		boolean result = raf().readBoolean();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized byte readByte() throws IOException {
		byte result = raf().readByte();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized int readUnsignedByte() throws IOException {
		int result = raf().readUnsignedByte();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized short readShort() throws IOException {
		short result = raf().readShort();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized int readUnsignedShort() throws IOException {
		int result = raf().readUnsignedShort();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized char readChar() throws IOException {
		char result = raf().readChar();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized int readInt() throws IOException {
		int result = raf().readInt();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized long readLong() throws IOException {
		long result = raf().readLong();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized float readFloat() throws IOException {
		float result = raf().readFloat();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized double readDouble() throws IOException {
		double result = raf().readDouble();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Wrapped method from <code>DataInput</code> interface.</b>
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized String readLine() throws IOException {
		String result = raf().readLine();
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * <b>Warning:</b> custom implementation will expect different low-level
	 * byte encoding than e.g. the identical method from
	 * <code>RandomAccessFile</code>!
	 * <p>
	 * Preserves the internal state of the managed proxy instance.
	 * 
	 * @see java.io.DataInput
	 */
	public synchronized String readUTF() throws IOException {
		byte[] content = new byte[raf().readInt()];
		raf().readFully(content);
		String result = new String(content, "UTF-8");
		// retrieve current file pointer directly from underlying
		// file.
		// WARNING: referencing the file via the raf() method
		// in this context would distort the actual current
		// file pointer!
		currentFilePointer = raf.getFilePointer();
		return result;
	}

	/**
	 * Cleans the actual file which has been wrapped from disk.
	 */
	protected void finalize() throws Throwable {
		if (delete() == false) {
			deleteOnExit();
		}
	}

	/**
	 * Copies the contents of the source file verbatim to the provided
	 * destination file.
	 * <p>
	 * This implementation uses the Java NIO API to realize a most efficient
	 * copy procedure. The NIO API should automatically be mapped to highly
	 * efficient operating system level functionality available in the
	 * respective host operating system.
	 * 
	 * @param source
	 *            File to be copied.
	 * @param destination
	 *            Destination of copy.
	 * @throws IOException
	 */
	public static void copyFile(File source, File destination)
			throws IOException {
		FileInputStream fis = new FileInputStream(source);
		FileOutputStream fos = new FileOutputStream(destination);
		FileChannel inCh = fis.getChannel();
		FileChannel outCh = fos.getChannel();
		inCh.transferTo(0, inCh.size(), outCh);
		inCh.close();
		fis.close();
		outCh.close();
		fos.flush();
		fos.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.io.RandomAccessStorage#copy()
	 */
	public RandomAccessStorage copy() throws IOException {
		return new ManagedRandomAccessFile(this);
	}

	/**
	 * Creates a new temporary file with the configured pre- and suffix in the
	 * system-wide temporary file directory.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected static File createTempFile() throws IOException {
		return File.createTempFile(ManagedRandomAccessFile.TEMP_FILE_PREFIX,
				ManagedRandomAccessFile.TEMP_FILE_SUFFIX);
	}

	/**
	 * This class implements a shutdown hook, which will clear all files backing
	 * instances of the enclosing class from the file system at the moment, at
	 * which the JVM shuts down.
	 * 
	 * @author Christian W. Guenther (christian at deckfour dot org)
	 */
	protected static class ShutdownHook extends Thread {

		/**
		 * Regular expression to match temporary file names.
		 */
		protected String tmpFileRegEx = ManagedRandomAccessFile.TEMP_FILE_PREFIX
				+ "(.*)" + ManagedRandomAccessFile.TEMP_FILE_SUFFIX;

		/**
		 * This method is invoked in a dedicated thread by the virtual machine,
		 * before it attempts shutdown. It will attempt to delete all temporary
		 * files used by ATERandomFileBuffer from the file system.
		 */
		public void run() {
			System.out.print("ManagedRandomAccessFile.ShutdownHook invoked.. ");
			int cleaned = 0;
			File[] tmpFiles = (new File(System.getProperty("java.io.tmpdir")))
					.listFiles();
			for (int i = 0; i < tmpFiles.length; i++) {
				if (tmpFiles[i].getName().matches(tmpFileRegEx)) {
					if (tmpFiles[i].delete() == false) {
						tmpFiles[i].deleteOnExit();
					}
					cleaned++;
				}
			}
			System.out.print("cleaned " + cleaned + " stale files.");
		}
	}

}
