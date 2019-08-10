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
 **********************************************************/

package org.processmining.framework.log.classic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.processmining.analysis.summary.ExtendedLogSummary;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.InfoItem;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderException;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Reads a workflow log file.
 * <p>
 * This class allows a workflow log to be read by reading one process instance
 * at a time. A log is explicitly not read into memory completely, to allow very
 * large logs to be read.
 * <p>
 * A log file will be opened when it is first needed, i.e. when
 * <code>next()</code>, <code>hasNext()</code>, <code>reset()</code> or
 * <code>getLogSummary()</code> is first called. The log file is automatically
 * closed after the last process instance in the log is read.
 * <p>
 * When the log is first opened, a summary will be read first. This means that
 * the whole log will be read once before actually returning any process
 * instances. This summary information is available by calling the
 * <code>getLogSummary()</code> method.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class LogReaderClassic extends LogReader {

	private LogFilter filter;
	protected int[] processInstancesToKeep = null;
	private LogFile inputFile;
	private ExtendedLogSummary summary;
	private InputStream in;
	private XmlPullParser parser;
	private boolean initialized;

	private ProcessInstance current;
	private int currentIndex;
	private int nextIndexToKeep;
	private int indexInKeepList;

	private String curProcessName;

	/**
	 * Derive a new log reader, based on the given log reader and filtered by
	 * the given filter. If the filter parameter is null, no filtering is done.
	 * Note that the log file is not actually opened for reading yet.
	 * 
	 * @param filter
	 *            the filter to use for filtering the log file
	 * @param inputFile
	 *            the log file to read
	 */
	public static LogReader createInstance(LogReader reader, LogFilter filter)
			throws Exception {
		return new LogReaderClassic(filter, reader.getFile());
	}

	/**
	 * Read the given log file, filtered by the given filter. If the filter
	 * parameter is null, no filtering is done. Note that the log file is not
	 * actually opened for reading yet.
	 * 
	 * @param filter
	 *            the filter to use for filtering the log file
	 * @param inputFile
	 *            the log file to read
	 */
	public static LogReader createInstance(LogFilter filter, LogFile inputFile) {
		return new LogReaderClassic(filter, inputFile);
	}

	/**
	 * Read the given log file, filtered by the filter of the original reader,
	 * but only give the instances with the given indices. Note that the log
	 * file is not actually opened for reading yet. Note also that the int[]
	 * processInstancesToKeep should be sorted.
	 * 
	 * @param oldLogReader
	 *            the original log reader
	 * @param pitk
	 *            the process instances to keep
	 * 
	 * @deprecated use <code>clone(int[]) instead</code>.
	 */
	public static LogReader createInstance(LogReader oldLogReader, int[] pitk)
			throws Exception {
		if (oldLogReader instanceof LogReaderClassic) {
			return new LogReaderClassic((LogReaderClassic) oldLogReader, pitk);
		} else {
			throw new Exception(
					"New instances can only be based on instances of the same class!");
		}
	}

	/**
	 * Read the given log file, filtered by the given filter. If the filter
	 * parameter is null, no filtering is done. Note that the log file is not
	 * actually opened for reading yet.
	 * 
	 * @param filter
	 *            the filter to use for filtering the log file
	 * @param inputFile
	 *            the log file to read
	 */
	protected LogReaderClassic(LogFilter filter, LogFile inputFile) {
		this.filter = filter;
		this.inputFile = inputFile;
		initialized = false;
		processInstancesToKeep = null;
	}

	/**
	 * Read the given log file, filtered by the filter of the original reader,
	 * but only give the instances with the given indices. Note that the log
	 * file is not actually opened for reading yet. Note also that the int[]
	 * processInstancesToKeep should be sorted.
	 * 
	 * @param oldLogReader
	 *            the original log reader
	 * @param pitk
	 *            the process instances to keep
	 * 
	 * @deprecated use <code>clone(int[]) instead</code>.
	 */
	protected LogReaderClassic(LogReader oldLogReader, int[] pitk) {
		this(oldLogReader.getLogFilter(), oldLogReader.getFile());

		int[] oldInstancesToKeep = oldLogReader.processInstancesToKeep();

		processInstancesToKeep = new int[pitk.length];

		// Now, we need to update the list of indices we need to keep, with the
		// old list we have
		// from the old LogReader
		// This means that we need to get, from the oldInstancesToKeep list
		// all the int's stored on the indices of the new list.
		for (int i = 0; i < processInstancesToKeep.length; i++) {
			processInstancesToKeep[i] = (oldInstancesToKeep == null ? pitk[i]
					: oldInstancesToKeep[pitk[i]]);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#toString()
	 */
	public String toString() {
		return "LogReader: " + getLogSummary().getNumberOfProcessInstances()
				+ " instances from " + inputFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#isSelectionOf()
	 */
	public boolean isSelection() {
		return processInstancesToKeep != null;
	}

	private void initialize() {
		if (!initialized) {
			Message.add("Please wait while initializing the Log....");

			currentIndex = -1;
			indexInKeepList = 0;
			nextIndexToKeep = (processInstancesToKeep == null ? 0
					: processInstancesToKeep[0]);

			initialized = true;
			open();
			try {
				parseDocument();
			} catch (IOException ex) {
				summary = new ExtendedLogSummary();
				summary.setWorkflowLog(new InfoItem("IO Exception occurred", ex
						.getMessage(), null, null));
			} catch (XmlPullParserException ex) {
				summary.setWorkflowLog(new InfoItem("XML Exception occurred",
						ex.getMessage(), null, null));
			}
			reset();
			Message.add("finished initializing.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#getFile()
	 */
	public LogFile getFile() {
		return inputFile;
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
	public LogSummary getLogSummary() {
		initialize();
		return summary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#hasNext()
	 */
	public boolean hasNext() {
		initialize();
		return current != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#next()
	 */
	public ProcessInstance next() {
		ProcessInstance pi;

		initialize();
		pi = current;
		fetchNext();
		return pi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogReader#reset()
	 */
	public void reset() {
		initialize();
		close();
		open();
		currentIndex = -1;
		indexInKeepList = 0;
		nextIndexToKeep = (processInstancesToKeep == null ? 0
				: processInstancesToKeep[0]);
		fetchNext();
	}

	private void fetchNext() {
		if (nextIndexToKeep < 0) {
			current = null;
			return;
		}

		boolean filterResult;

		do {
			do {
				current = parseNextProcessInstance();
				filterResult = current != null ? (filter == null ? true
						: filter.filter(current)) : false;
			} while (current != null
					&& (filterResult == false || current.isEmpty()));
			currentIndex++;
		} while ((currentIndex != nextIndexToKeep));
		if (processInstancesToKeep == null) {
			nextIndexToKeep++;
		} else {
			indexInKeepList++;
			if (indexInKeepList < processInstancesToKeep.length) {
				nextIndexToKeep = processInstancesToKeep[indexInKeepList];
			} else {
				nextIndexToKeep = -1;
			}
		}
	}

	private void open() {
		try {
			in = inputFile.getInputStream();
			parser = XmlPullParserFactory.newInstance(
					System.getProperty(XmlPullParserFactory.PROPERTY_NAME,
							"org.kxml2.io.KXmlParser"), null).newPullParser();
			parser.setInput(in, null);
		} catch (IOException ex) {
			throw new LogReaderException(ex);
		} catch (XmlPullParserException ex) {
			throw new LogReaderException(ex);
		}
	}

	private void close() {
		if (in != null) {
			try {
				in.close();
			} catch (IOException ex) {
				throw new LogReaderException(ex);
			}
			in = null;
		}
	}

	private boolean atEndTag(String name) throws XmlPullParserException {
		return parser.getEventType() == XmlPullParser.END_TAG
				&& name.equals(parser.getName());
	}

	private boolean atStartTag(String name) throws XmlPullParserException {
		return parser.getEventType() == XmlPullParser.START_TAG
				&& name.equals(parser.getName());
	}

	// will close input stream if there are no process instances left
	private ProcessInstance parseNextProcessInstance() {
		int eventType;

		if (parser == null || in == null) {
			return null;
		}

		try {
			eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (atStartTag("ProcessInstance")) {
					return parseProcessInstance();
				} else if (atStartTag("Process")) {
					curProcessName = parser.getAttributeValue(null, "id");
				}
				eventType = parser.next();
			}
			close();
			return null;
		} catch (IOException ex) {
			close();
			throw new LogReaderException(ex);
		} catch (XmlPullParserException ex) {
			close();
			throw new LogReaderException(ex);
		}
	}

	private void parseDocument() throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();

		summary = new ExtendedLogSummary();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (atStartTag("WorkflowLog")) {
				parseWorkflowLog();
				return;
			}
			eventType = parser.next();
		}
		throw new LogReaderException(
				"invalid workflow log: could not find WorkflowLog tag");
	}

	private void parseWorkflowLog() throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		String curWorkflowLog = parser.getAttributeValue(null, "description");
		DataSection curWorkflowLogData = new DataSection();
		while (!(eventType == XmlPullParser.END_DOCUMENT)
				&& !atEndTag("WorkflowLog")) {
			if (atStartTag("Data")) {
				parseData(curWorkflowLogData);
			} else if (atStartTag("Source")) {
				parseSource();
			} else if (atStartTag("Process")) {
				parseProcess();
			}
			eventType = parser.next();
		}
		summary.setWorkflowLog(new InfoItem("", curWorkflowLog,
				curWorkflowLogData, null));
	}

	private void parseSource() throws XmlPullParserException, IOException {
		DataSection curSourceData = new DataSection();
		String program = parser.getAttributeValue(null, "program");

		curSourceData.put("program", program == null ? "" : program.trim());
		while (!atEndTag("Source")) {
			if (atStartTag("Data")) {
				parseData(curSourceData);
			}
			parser.next();
		}
		summary.setSource(new InfoItem("", "", curSourceData, null));
	}

	private void parseProcess() throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		String curProcessDesc = parser.getAttributeValue(null, "description");
		DataSection curProcessData = new DataSection();

		boolean processAdded = false;
		curProcessName = parser.getAttributeValue(null, "id");
		while (!(eventType == XmlPullParser.END_DOCUMENT)
				&& !atEndTag("Process")) {
			if (atStartTag("Data")) {
				parseData(curProcessData);
			} else if (atStartTag("ProcessInstance")) {
				if (!processAdded) {
					summary.addProcess(new InfoItem(curProcessName,
							curProcessDesc, curProcessData, null));
					processAdded = true;
				}
				ProcessInstance pi = parseProcessInstance();

				if ((pi != null) && (filter == null || filter.filter(pi))
						&& (!pi.isEmpty())) {
					currentIndex++;

					if ((processInstancesToKeep == null)
							|| (currentIndex == nextIndexToKeep)) {
						summary.addProcessInstance(pi);
					}
					if ((processInstancesToKeep != null)
							&& (currentIndex == nextIndexToKeep)) {
						indexInKeepList++;
						if (indexInKeepList < processInstancesToKeep.length) {
							nextIndexToKeep = processInstancesToKeep[indexInKeepList];
						} else {
							nextIndexToKeep = -1;
						}
					}
				}

			}
			eventType = parser.next();
		}
		// if (hasEntries) {
		// }
	}

	private ProcessInstance parseProcessInstance()
			throws XmlPullParserException, IOException {
		String name = parser.getAttributeValue(null, "id");
		String descr = parser.getAttributeValue(null, "description");
		HashMap data = new HashMap();
		ArrayList ate = new ArrayList();

		while (!atEndTag("ProcessInstance")) {
			if (atStartTag("Data")) {
				parseData(data);
			} else if (atStartTag("AuditTrailEntry")) {
				insertSorted(ate, parseAuditTrailEntry());
			}
			parser.next();
		}
		return new ProcessInstanceClassic(curProcessName, name, descr, data,
				ate);
	}

	private void parseData(Map data) throws XmlPullParserException, IOException {
		while (!atEndTag("Data")) {
			if (atStartTag("Attribute")) {
				String name = parser.getAttributeValue(null, "name");
				String value = parser.nextText();

				data.put(name == null ? "" : name.trim(), value == null ? ""
						: value.trim());
			}
			parser.next();
		}
	}

	private AuditTrailEntry parseAuditTrailEntry()
			throws XmlPullParserException, IOException {
		String element = "", type = "", timestamp = "", originator = "";
		HashMap data = new HashMap();

		while (!atEndTag("AuditTrailEntry")) {
			if (atStartTag("Data")) {
				parseData(data);
			} else if (atStartTag("WorkflowModelElement")) {
				element = parser.nextText();
			} else if (atStartTag("EventType")) {
				String unknownType = parser.getAttributeValue(null,
						"unknowntype");

				type = parser.nextText();
				type = (type == null ? "" : type.trim());
				if (type.equals("unknown")) {
					if (unknownType == null) {
						throw new RuntimeException(
								"Event type 'unknown' not specified");
					}
					type = type + ":" + unknownType.trim();
				}
			} else if (atStartTag("Timestamp")) {
				timestamp = parser.nextText();
			} else if (atStartTag("Originator")) {
				originator = parser.nextText();
			}
			parser.next();
		}
		return new AuditTrailEntryClassic(element, type, timestamp, originator,
				data);
	}

	private void insertSorted(ArrayList ate, AuditTrailEntry entry) {
		if (entry.getTimestamp() == null) {
			ate.add(entry);
			return;
		}
		for (int i = 0; i < ate.size(); i++) {
			AuditTrailEntry a = (AuditTrailEntry) ate.get(i);
			if (a.getTimestamp() == null) {
				continue;
			}

			if (a.getTimestamp().compareTo(entry.getTimestamp()) > 0) {
				ate.add(i, entry);
				return;
			}
		}
		ate.add(entry);
	}

	public int[] processInstancesToKeep() {
		return processInstancesToKeep;
	}

	public int numberOfInstances() {
		return summary.getNumberOfProcessInstances();
	}

	public ProcessInstance getInstance(int index) {
		// TODO: dirty implementation; rework or remove.
		ProcessInstance requested = null;
		if (0 <= index && index < numberOfInstances()) {
			this.reset();
			for (int i = 0; i <= index; i++) {
				requested = this.next();
			}
			return requested;
		}
		return null;
	}

	public LogReader clone(int[] pitk) {
		return new LogReaderClassic(this, pitk);
	}

	public Process getProcess(int index) {
		// Not implemented!
		return null;
	}

	public Iterator instanceIterator() {
		return new LogReaderClassicIterator(this);
	}

	public int numberOfProcesses() {
		// Not implemented!
		return 0;
	}

	public Iterator processIterator() {
		// Not implemented!
		return null;
	}

	public class LogReaderClassicIterator implements Iterator {

		protected LogReaderClassic parent = null;

		public LogReaderClassicIterator(LogReaderClassic aParent) {
			parent = aParent;
			parent.reset();//
		}

		public boolean hasNext() {
			return parent.hasNext();
		}

		public Object next() {
			return parent.next();
		}

		public void remove() {
			// TODO: Not implemented;
		}

	}
}
