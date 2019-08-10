/**
 * Project: ProM HPLR
 * File: LogReaderFactory.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 5, 2006, 8:59:48 PM
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
package org.processmining.framework.log;

import org.processmining.framework.log.classic.LogReaderClassic;
import org.processmining.framework.log.rfb.BufferedLogReader;

/**
 * This class provides an abstraction layer to the creation and derivation of
 * LogReader instances. It provides static facilities for setting a specific log
 * reader implementation, which is then transparently used when requesting log
 * reader creation by the provided static methods.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class LogReaderFactory {

	protected static Class logReaderClass = BufferedLogReader.class;

	/**
	 * Creates a new instance of the currently configured log reader
	 * implementation. This method creates a log reader based on a specified log
	 * filter and provided log reader's original data.
	 * <p>
	 * Use this method preferably to the one based on a filter and log file. It
	 * generally performs faster, as the original log file does not have to be
	 * parsed and interpreted over again.
	 * 
	 * @param filter
	 *            The filter which which the log reader is equipped.
	 * @param reader
	 *            Log reader from which to copy original event data.
	 * @return The newly created log reader.
	 * @throws Exception
	 */
	public static LogReader createInstance(LogFilter filter, LogReader reader)
			throws Exception {
		// TODO: Reflection API don't work here, because we have an integer
		// array in one of the methods (which is not derived from Object).
		// Research, whether there are other ways to make this smoother.
		if (logReaderClass.equals(BufferedLogReader.class)) {
			return BufferedLogReader.createInstance(reader, filter);
		} else if (logReaderClass.equals(LogReaderClassic.class)) {
			return LogReaderClassic.createInstance(reader, filter);
		} else {
			throw new Exception(
					"Currently set factory class is not functional!");
		}
	}

	/**
	 * Creates a new instance of the currently configured log reader
	 * implementation. This method creates a log reader based on a specified log
	 * filter and log file.
	 * 
	 * @param filter
	 *            The filter which which the log reader is equipped.
	 * @param file
	 *            Log file from which to read event data.
	 * @return The newly created log reader.
	 * @throws Exception
	 */
	public static LogReader createInstance(LogFilter filter, LogFile file)
			throws Exception {
		// TODO: Reflection API don't work here, because we have an integer
		// array in one of the methods (which is not derived from Object).
		// Research, whether there are other ways to make this smoother.
		if (logReaderClass.equals(BufferedLogReader.class)) {
			return BufferedLogReader.createInstance(filter, file);
		} else if (logReaderClass.equals(LogReaderClassic.class)) {
			return LogReaderClassic.createInstance(filter, file);
		} else {
			throw new Exception(
					"Currently set factory class is not functional!");
		}
	}

	/**
	 * Creates a new instance of the currently configured log reader
	 * implementation. This method creates a log reader based on a parent log
	 * reader (whose settings are copied to the newly created instance) and an
	 * array containing the indices of process instances from the old log
	 * reader, which are to be contained in the newly created one.
	 * 
	 * @param reader
	 *            Parent log reader on which to base the new instance.
	 * @param processInstancesToKeep
	 *            Array containing the indices of process instances in the
	 *            parent log reader, which are to be used as well in the newly
	 *            created instance.
	 * @return The derived log reader which is newly created.
	 * @throws Exception
	 */
	public static LogReader createInstance(LogReader reader,
			int processInstancesToKeep[]) throws Exception {
		// TODO: Reflection API don't work here, because we have an integer
		// array in one of the methods (which is not derived from Object).
		// Research, whether there are other ways to make this smoother.
		if (logReaderClass.equals(BufferedLogReader.class)) {
			return BufferedLogReader.createInstance(reader,
					processInstancesToKeep);
		} else if (logReaderClass.equals(LogReaderClassic.class)) {
			return LogReaderClassic.createInstance(reader,
					processInstancesToKeep);
		} else {
			throw new Exception(
					"Currently set factory class is not functional!");
		}
	}

	/**
	 * Sets the LogReader implementation class, which is to be used for future
	 * creation and derivation of log readers.
	 * 
	 * @param readerClass
	 *            A class derived from LogReader
	 * @throws Exception
	 */
	public static void setLogReaderClass(Class readerClass) throws Exception {
		logReaderClass = readerClass;
	}

	/**
	 * Gets the LogReader implementation class, which is to be used for future
	 * creation and derivation of log readers.
	 * 
	 * @throws Exception
	 */
	public static Class getLogReaderClass() {
		return logReaderClass;
	}

}
