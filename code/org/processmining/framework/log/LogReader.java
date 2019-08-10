/**
 * Project: ProM
 * File: LogReader.java
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
import java.util.Iterator;
import java.util.List;

/**
 * Interface for log reading and modification access to process event logs.
 * 
 * @author christian
 */
public abstract class LogReader {

	/**
	 * Factory method prototype.
	 * <p>
	 * <b>Notice:</b> You <i>have to</i> override this method in your log reader
	 * implementation, as instances will always be created using a static
	 * factory method within ProM.
	 * 
	 * @param reader
	 *            A log reader on whose log the new log reader is based.
	 * @param filter
	 *            The LogFilter to use for the created log reader.
	 * @return
	 */
	public static LogReader createInstance(LogReader reader, LogFilter filter)
			throws Exception {
		throw new Exception("Not implemented!");
	}

	/**
	 * Factory method prototype.
	 * <p>
	 * <b>Notice:</b> You <i>have to</i> override this method in your log reader
	 * implementation, as instances will always be created using a static
	 * factory method within ProM.
	 * 
	 * @param filter
	 *            The LogFilter to use for the created log reader.
	 * @param file
	 *            The file to back the created log reader.
	 * @return
	 */
	public static LogReader createInstance(LogFilter filter, LogFile file)
			throws Exception {
		throw new Exception("Not implemented!");
	}

	/**
	 * Factory method prototype.
	 * <p>
	 * <b>Notice:</b> You <i>have to</i> override this method in your log reader
	 * implementation, as instances will always be created using a static
	 * factory method within ProM.
	 * 
	 * @param reader
	 *            The log reader from which the newly created instance inherits
	 *            its setting and configuration (filter, file, etc.).
	 * @param processInstancesToKeep
	 *            Indices of the process instances, which are to be contained in
	 *            the newly created log reader.
	 * @return
	 */
	public static LogReader createInstance(LogReader reader,
			int processInstancesToKeep[]) throws Exception {
		throw new Exception("Not implemented!");
	}

	/**
	 * Clones this log reader, including all contained processes and process
	 * instances. The contained process instances, whose indices are contained
	 * in the given integer array, are excluded and will not be contained in the
	 * returned clone.
	 * <p>
	 * Notice that this method will yield a deep copy of the cloned instance.
	 * This implies doubling this instance's memory consumption (either in
	 * random access memory or filesystem space consumption, depending on the
	 * implementation) and no synchronization of changes between original and
	 * clone.
	 * 
	 * @param pitk
	 *            The contained process instances, whose indices are contained
	 *            in the given integer array, are excluded and will not be
	 *            contained in the returned clone.
	 * @return A clone of this log reader, excluding the process instances whose
	 *         indices are given in the supplied array.
	 */
	public abstract LogReader clone(int[] pitk);

	/**
	 * Returns a string representation of this log reader instance.
	 * 
	 * @return
	 */
	public abstract String toString();

	/**
	 * Tells whether or not this logReader object represents a selection of a
	 * log, instead of a complete log.
	 * 
	 * @return true if it it a selection
	 */
	public abstract boolean isSelection();

	/**
	 * Returns the log file instance that was given in the constructor.
	 * 
	 * @return the log file instance that was given in the constructor
	 */
	public abstract LogFile getFile();

	/**
	 * Returns the filter instance that was given in the constructor.
	 * 
	 * @return the filter instance that was given in the constructor
	 */
	public abstract LogFilter getLogFilter();

	/**
	 * Returns a <code>LogSummary</code> object with summarized info about the
	 * workflow log. Note that, if a filter is used, any information provided in
	 * this object is based on the filtered log, not on the real contents of the
	 * log file.
	 * 
	 * @return a <code>LogSummary</code> object with summarized info about the
	 *         workflow log
	 */
	public abstract LogSummary getLogSummary();

	/**
	 * Retrieves the indices of process instances which are not excluded from
	 * reading.
	 * 
	 * @return
	 */
	public abstract int[] processInstancesToKeep();

	/**
	 * Retrieves an iterator over the process instances contained in the
	 * encapsulated log file.
	 * 
	 * @return an iterator over the contained process instances.
	 */
	public abstract Iterator instanceIterator();

	public List<ProcessInstance> getInstances() {
		List<ProcessInstance> result = new ArrayList<ProcessInstance>();
		Iterator iterator = instanceIterator();
		while (iterator.hasNext())
			result.add((ProcessInstance) iterator.next());
		return result;
	}

	/**
	 * Retrieves the number of process instances contained in this log file.
	 * 
	 * @return the number of process instances contained in this log file.
	 */
	public abstract int numberOfInstances();

	/**
	 * Retrieves the process instance located at the given index within the
	 * encapsulated log file.
	 * 
	 * @param index
	 *            index of the requested process instance in the log
	 * @return referenced process instance.
	 */
	public abstract ProcessInstance getInstance(int index);

	/**
	 * Retrieves an iterator over the processes contained in the encapsulated
	 * log file.
	 * 
	 * @return an iterator over the contained processes.
	 */
	public abstract Iterator processIterator();

	/**
	 * Retrieves the number of processes contained in this log file.
	 * 
	 * @return the number of processes contained in this log file.
	 */
	public abstract int numberOfProcesses();

	/**
	 * Retrieves the process located at the given index within the encapsulated
	 * log file.
	 * 
	 * @param index
	 *            index of the requested process in the log
	 * @return referenced process.
	 */
	public abstract Process getProcess(int index);

	/*
	 * --------------------------------------------------------------------------
	 * ----- Deprecated methods - do not use anymore! (subject to removal)
	 */

	/**
	 * Iterator interface method.
	 * <p>
	 * Probes whether the log reader has another process instance available for
	 * reading.
	 * 
	 * @return
	 * @deprecated If you prefer to use iterator access to process instances,
	 *             use the <code>instanceIterator()</code> method instead!
	 */
	public abstract boolean hasNext();

	/**
	 * Iterator interface method.
	 * <p>
	 * Retrieves the next process instance from the log.
	 * 
	 * @return The next process instance from the log.
	 * @deprecated If you prefer to use iterator access to process instances,
	 *             use the <code>instanceIterator()</code> method instead!
	 */
	public abstract ProcessInstance next();

	/**
	 * Iterator interface method.
	 * <p>
	 * Resets the main iterator to the first process instance.
	 * 
	 * @deprecated If you prefer to use iterator access to process instances,
	 *             use the <code>instanceIterator()</code> method instead!
	 */
	public abstract void reset();

}