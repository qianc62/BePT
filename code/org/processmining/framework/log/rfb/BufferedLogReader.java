/**
 * Project: ProM HPLR
 * File: BufferedLogReader.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 3, 2006, 6:57:39 PM
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
package org.processmining.framework.log.rfb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.xml.sax.SAXException;

/**
 * This class implements a fast, SAX-based, log reader buffering log data partly
 * on binary files residing in persistent storage. The purpose of using this log
 * reader is to reduce the memory footprint of log data management, speed up
 * general access and in particular sequential read access, and to allow random
 * access to all data, including write access.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class BufferedLogReader extends LogReader {

	/**
	 * The log file from which event data is read.
	 */
	protected LogFile file = null;
	/**
	 * LogFilter instance applied to this log.
	 */
	protected LogFilter filter = null;
	/**
	 * Marks the indices of process instances in the parent's collection which
	 * this instance inherits. This is only relevant for log readers, that are
	 * created as a selection of other log readers, and can be <code>null</code>
	 * otherwise.
	 */
	protected int[] processInstancesToKeep = null;
	/**
	 * Parent log reader, from whose data and settings the respective instance
	 * has been derived. Can be <code>null</code>, if the instance has been
	 * derived directly from an MXML log file.
	 */
	protected BufferedLogReader parent = null;
	/**
	 * This thread is created and configured in the respective constructor. Its
	 * purpose is to derive the instance's data set from either the original
	 * log, or a parent log reader. Invocation of the thread is suspended until
	 * the first access is attempted on the instance's data set.
	 */
	protected Thread processingThread = null;
	/**
	 * Internal marker. Remebers, whether the internal processing thread has
	 * already been started. Serves as synchronization when concurrent access is
	 * attempted, but still blocked by data acquisition.
	 */
	protected boolean processingThreadStarted = false;
	/**
	 * Contains the currently valid processes, process instances, and log
	 * summary of the encapsulated log.
	 */
	protected LogData data = null;
	/**
	 * Contains the processes, process instances, and log summary as contained
	 * in the original log this reader is derived from.
	 */
	protected LogData originalData = null;
	/**
	 * The index for the built-in iterator over the contained process instances.
	 */
	protected int instanceIteratorIndex = 0;
	/**
	 * Marker for thread synchronization; only after this attribute has been set
	 * to <code>true</code>, the data stored in this collection can be used.
	 */
	protected boolean isValid = false;
	/**
	 * Exception that occurred while reading the log in a separate thread.
	 */
	private Exception exceptionDuringReading = null;

	/**
	 * Creates a new instance, based on the original log data used by the
	 * provided parameter instance, but filtered with the provided log filter.
	 * This factory method helps to speed up re-filtering of event log data, as
	 * the MXML parsing step can be omitted.
	 * 
	 * @param reader
	 *            Buffered log reader from which to obtain the event log data.
	 * @param filter
	 *            Log filter to be applied to the obtained event log data.
	 * @throws Exception
	 */
	public static LogReader createInstance(LogReader reader, LogFilter filter)
			throws Exception {
		Message.add("Buffered log reader created from reader " + reader
				+ ", filter: " + filter, Message.DEBUG);
		if (reader instanceof BufferedLogReader) {
			return new BufferedLogReader((BufferedLogReader) reader, filter);
		} else {
			throw new Exception(
					"New instances can only be based on instances of the same class!");
		}
	}

	/**
	 * Creates a new instance, parses the given log file and builds the log's
	 * data structures as random access swap files.
	 * 
	 * @param aFile
	 *            LogFile from which to read event data.
	 * @param aFilter
	 *            LogFilter, which is to be applied to the contained log data.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParserConfigurationException
	 */
	public static LogReader createInstance(LogFilter aFilter, LogFile aFile)
			throws Exception {
		Message.add("Buffered log reader created from file " + aFile
				+ ", using filter " + aFilter, Message.DEBUG);
		return new BufferedLogReader(aFile, aFilter);
	}

	/**
	 * Creates a new instance, parses the given list of log files and builds the
	 * log's data structures as random access swap files.
	 * 
	 * @param files
	 *            List of LogFiles from which to read event data.
	 * @param aFilter
	 *            LogFilter, which is to be applied to the contained log data.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParserConfigurationException
	 */
	public static LogReader createInstance(LogFilter aFilter,
			List<LogFile> files) throws Exception {
		Message.add("Buffered log reader created from files: ", Message.DEBUG);
		for (LogFile file : files) {
			Message.add(" - " + file, Message.DEBUG);
		}
		Message.add("using filter " + aFilter, Message.DEBUG);
		return new BufferedLogReader(files, aFilter);
	}

	/**
	 * Create a deep clone of the provided buffered log reader, which only has
	 * the instances indicated in the provided integer array as indices.
	 * 
	 * @param reader
	 *            Template buffered log reader.
	 * @param pitk
	 *            Process instance indices to keep.
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	public static LogReader createInstance(LogReader reader, int[] pitk)
			throws Exception {
		Message.add("Buffered log reader created from reader " + reader
				+ ", pitk.: " + pitk, Message.DEBUG);
		if (reader instanceof BufferedLogReader) {
			return new BufferedLogReader((BufferedLogReader) reader, pitk);
		} else {
			throw new Exception(
					"New instances can only be based on instances of the same class!");
		}
	}

	/**
	 * Creates a new instance, parses the given log file and builds the log's
	 * data structures as random access swap files.
	 * 
	 * @param aFile
	 *            LogFile from which to read event data.
	 * @param aFilter
	 *            LogFilter, which is to be applied to the contained log data.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParserConfigurationException
	 */
	protected BufferedLogReader(LogFile aFile, LogFilter aFilter)
			throws IOException, SAXException, ParserConfigurationException {
		synchronized (this) {
			isValid = false;
			file = aFile;
			filter = aFilter;
			processInstancesToKeep = null;
			parent = null;
			instanceIteratorIndex = 0;
			originalData = null;
			data = null;
			// prepare asynchronous parsing of log data in thread.
			processingThreadStarted = false;
			processingThread = new Thread() {
				public void run() {
					try {
						originalData = LogData.createInstance(file);
						data = LogData.createInstance(originalData, filter);
						flagDataValid();
					} catch (Exception e) {
						// pass exception on to main thread
						setExceptionDuringReading(e);

						originalData = null;

						// call flagDataValid();
						// strictly speaking, this breaks the semantics of this
						// method, but we need it to wake up accessor methods.
						// setting the data attributes to null will raise
						// exceptions
						// at the calling party, where they need to be handled!
						// TODO: look for a cleaner solution!
						flagDataValid();
					}
				}
			};
		}
	}

	/**
	 * Creates a new instance, parses the given log files and builds the log's
	 * data structures as random access swap files.
	 * 
	 * @param files
	 *            List of LogFile instances, from which to read event data.
	 * @param aFilter
	 *            LogFilter, which is to be applied to the contained log data.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParserConfigurationException
	 */
	protected BufferedLogReader(List<LogFile> files, LogFilter aFilter)
			throws IOException, SAXException, ParserConfigurationException {
		// TODO: merge with previous method into one (code duplication!)
		synchronized (this) {
			isValid = false;
			file = files.get(0);
			filter = aFilter;
			processInstancesToKeep = null;
			parent = null;
			instanceIteratorIndex = 0;
			originalData = null;
			data = null;
			final List<LogFile> fileList = new ArrayList<LogFile>(files);
			// prepare asynchronous parsing of log data in thread.
			processingThreadStarted = false;
			processingThread = new Thread() {
				public void run() {
					try {
						originalData = LogData.createInstance(fileList);
						data = LogData.createInstance(originalData, filter);
						flagDataValid();
					} catch (Exception e) {
						// pass exception on to main thread
						setExceptionDuringReading(e);

						originalData = null;

						// call flagDataValid();
						// strictly speaking, this breaks the semantics of this
						// method, but we need it to wake up accessor methods.
						// setting the data attributes to null will raise
						// exceptions
						// at the calling party, where they need to be handled!
						// TODO: look for a cleaner solution!
						flagDataValid();
					}
				}
			};
		}
	}

	protected synchronized void setExceptionDuringReading(Exception exception) {
		exceptionDuringReading = exception;
		data = null;
	}

	private synchronized Exception getExceptionDuringReading() {
		return exceptionDuringReading;
	}

	/**
	 * Creates a new instance, based on the original log data used by the
	 * provided parameter instance, but filtered with the provided log filter.
	 * This constructor helps to speed up re-filtering of event log data, as the
	 * MXML parsing step can be omitted.
	 * 
	 * @param aReader
	 *            Buffered log reader from which to obtain the event log data.
	 * @param aFilter
	 *            Log filter to be applied to the obtained event log data.
	 * @throws IOException
	 */
	protected BufferedLogReader(BufferedLogReader aReader, LogFilter aFilter)
			throws IOException {
		synchronized (this) {
			// initialization
			isValid = false;
			file = aReader.file;
			filter = aFilter;
			processInstancesToKeep = null;
			parent = aReader;
			instanceIteratorIndex = 0;
			originalData = parent.getOriginalData();
			data = null;
			// prepare asynchronous derivation of data in internal thread.
			processingThreadStarted = false;
			processingThread = new Thread() {
				public void run() {
					try {
						// filter the new instance's data from the original set
						// (original behavior of the classic log reader
						// implementation)
						data = LogData.createInstance(originalData, filter);
						flagDataValid();
					} catch (IOException e) {
						// pass exception on to main thread
						setExceptionDuringReading(e);
					}
				}
			};
		}
	}

	/**
	 * Create a deep clone of the provided buffered log reader, which only has
	 * the instances indicated in the provided integer array as indices.
	 * 
	 * @param reader
	 *            Template buffered log reader.
	 * @param pitk
	 *            Process instance indices to keep.
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	protected BufferedLogReader(BufferedLogReader reader, int[] pitk)
			throws IndexOutOfBoundsException, IOException {
		// initialization
		isValid = false;
		file = reader.file;
		filter = reader.filter;
		processInstancesToKeep = pitk;
		parent = reader;
		instanceIteratorIndex = 0;
		originalData = parent.getOriginalData();
		// prepare asynchronous derivation of log data in internal thread.
		processingThreadStarted = false;
		processingThread = new Thread() {
			public void run() {
				try {
					// derive data as selection of the provided reader's
					// current set (as opposed to the original set).
					data = LogData.createInstance(parent.getLogData(),
							processInstancesToKeep);
					flagDataValid();
				} catch (IOException e) {
					// pass exception on to main thread
					setExceptionDuringReading(e);
				}
			}
		};
	}

	/**
	 * Inquires, whether the respective instance is valid, i.e. its data
	 * structures have been set up and processed, and its data has become
	 * readable.
	 * 
	 * @return Validity of the instance.
	 */
	public synchronized boolean isValid() {
		return isValid;
	}

	/**
	 * Helper method, which flags this reader's data valid and notifies, i.e.
	 * wakes up, all waiting threads that want to access this data.
	 */
	protected synchronized void flagDataValid() {
		isValid = true;
		notifyAll();
		Message.add("Processing data for buffered log reader completed.",
				Message.DEBUG);
	}

	/**
	 * Helper method, which blocks passively until the log data container's data
	 * becomes available for reading.
	 */
	protected void ensureValidData() {
		// return quick, if there is nothing to wait for!
		if (isValid == true) {
			return;
		}
		/*
		 * check whether processing thread is already running (synchronized, as
		 * this method is the only one to modify the 'processingThreadStarted'
		 * flag's value).
		 */
		synchronized (this) {
			if (processingThreadStarted == false) {
				// set flag to prevent starting the thread twice
				processingThreadStarted = true;
				// intiate processing of log data
				processingThread.start();
				Message.add(
						"Processing data for buffered log reader initiated...",
						Message.DEBUG);
			}
		}
		// passively wait, until data becomes available.
		while (isValid == false) {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		/*
		 * data is available; return and stop blocking.
		 */
		if (getExceptionDuringReading() != null) {
			throw new Error("Could not load log file",
					getExceptionDuringReading());
		}
		if (data == null) {
			throw new Error(
					"Internal error while reading log: no data available, but no other error detected.");
		}
	}

	/**
	 * Retrieves the set of log data underlying this log reader's current state.
	 * 
	 * @return a LogData container.
	 */
	public LogData getLogData() {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		return data;
	}

	/**
	 * Retrieves the set of log data contained in the original log, from which
	 * this log reader has been derived.
	 * 
	 * @return a LogData container.
	 */
	public LogData getOriginalData() {
		/*
		 * If this instance does not have a parent, that means that it needs to
		 * first parse its original data from the MXML log file supplied. In
		 * this case, we wait in a blocking (passive) manner. Otherwise, the
		 * original data has been set in the constructor already, and we can
		 * skip data processing task on this one. Notice that this 'lazy data
		 * processing' tactics does also help to limit memory consumption. Log
		 * reader specific buffer files are only called, when the data
		 * processing thread is invoked (which we can skip here).
		 */
		if (parent == null) {
			ensureValidData();
		}
		return originalData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#clone(int[])
	 */
	public synchronized LogReader clone(int[] pitk) {
		try {
			return new BufferedLogReader(this, pitk);
		} catch (Exception e) {
			// Fail and tell about it
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getFile()
	 */
	public LogFile getFile() {
		return file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getInstance(int)
	 */
	public synchronized ProcessInstance getInstance(int index) {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		return (ProcessInstance) data.instances().get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getLogFilter()
	 */
	public LogFilter getLogFilter() {
		return filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getLogSummary()
	 */
	public synchronized LogSummary getLogSummary() {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		return data.summary();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getProcess(int)
	 */
	public synchronized Process getProcess(int index) {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		return (Process) data.processes().get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#hasNext()
	 */
	public synchronized boolean hasNext() {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		return (instanceIteratorIndex < data.instances().size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#instanceIterator()
	 */
	public synchronized Iterator instanceIterator() {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		return data.instances().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#isSelection()
	 */
	public synchronized boolean isSelection() {
		return (processInstancesToKeep != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#next()
	 */
	public synchronized ProcessInstance next() {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		ProcessInstance result = (ProcessInstance) data.instances().get(
				instanceIteratorIndex);
		instanceIteratorIndex++;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#numberOfInstances()
	 */
	public synchronized int numberOfInstances() {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		return data.instances().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#numberOfProcesses()
	 */
	public synchronized int numberOfProcesses() {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		return data.processes().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#processInstancesToKeep()
	 */
	public synchronized int[] processInstancesToKeep() {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		// a buffered log reader, by definition, only contains
		// the process instances it is supposed to.
		// thus, all contained instances are to keep!
		int pitk[] = new int[data.instances().size()];
		for (int i = 0; i < data.instances().size(); i++) {
			pitk[i] = i;
		}
		return pitk;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#processIterator()
	 */
	public synchronized Iterator processIterator() {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		return data.processes().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#reset()
	 */
	public synchronized void reset() {
		instanceIteratorIndex = 0;
	}

	/**
	 * Cleans up the log collection, removing all traces from the file system
	 * (buffers etc.)
	 * <p>
	 * <b>Warning:</b> After calling this method, the respective log reader
	 * instance becomes unusable! All method calls subsequent to this one will
	 * return undefined results!
	 * 
	 * @throws IOException
	 */
	public synchronized void cleanup() throws IOException {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		for (int i = 0; i < data.instances().size(); i++) {
			((ProcessInstanceImpl) data.instances().get(i)).cleanup();
		}
		data = null;
		originalData = null;
	}

	/**
	 * Applies the contained filter to the log data. The log will be altered,
	 * and the sets of processes and instances, and the log summary will be
	 * changed.
	 * 
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	protected synchronized void filter() throws IndexOutOfBoundsException,
			IOException {
		/*
		 * block until this instance's data becomes valid, before returning
		 * anything.
		 */
		ensureValidData();
		// derive new log data from original data,
		// and apply respective filter to it.
		data = LogData.createInstance(originalData, filter);
	}

	/**
	 * Returns a string representation of this log reader.
	 */
	public String toString() {
		return "BufferedLogReader: "
				+ originalData.summary.numberOfProcessInstances
				+ " Process Instances and "
				+ originalData.summary.numberOfAuditTrailEntries
				+ " Audit Trail Entries from \"" + file.toString() + "\"";
	}

	/**
	 * Returns the hash code of this log reader.
	 */
	public int hashCode() {
		return file.getShortName().hashCode();
	}

}
