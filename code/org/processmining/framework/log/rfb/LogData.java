/**
 * Project: ProM HPLR
 * File: LogData.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 9, 2006, 1:45:05 PM
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.processmining.framework.log.DataAttribute;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.ui.Message;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class servers as a simple abstract data type, or container, holding a
 * set of processes and referring instances. No abstract access, or meta
 * information, is provided (in contrast to e.g. a log reader).
 * <p>
 * The purpose is to provide a means to store and exchange event log data in its
 * pure form. It is further equipped with a lightweight log summary, which is
 * transparently adjusted when a log filter is applied to the set.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class LogData {

	// BVD: Constant so signal the LogData object to keep the process if there
	// are no
	// process instances included
	public static final String RETAIN_PROCESS_IF_EMPTY = "ProM: Retain this process if empty";

	/**
	 * Holds references to the stored processes.
	 */
	protected ArrayList<ProcessImpl> processes = null;
	/**
	 * Holds references to the stored process instances.
	 */
	protected ArrayList<ProcessInstanceImpl> instances = null;
	/**
	 * Holds the log summary describing the contained log information.
	 */
	protected LightweightLogSummary summary = null;

	private static final List<String> EMPTY_LIST = Collections
			.unmodifiableList(new ArrayList<String>(0));

	/**
	 * Creates a new instance with empty sets.
	 * 
	 * @return A new empty instance.
	 */
	public static LogData createInstance() {
		System.gc(); // trigger old instances to be cleaned up
		return new LogData(new ArrayList<ProcessImpl>(),
				new ArrayList<ProcessInstanceImpl>(),
				new LightweightLogSummary());
	}

	/**
	 * Creates a new instance with the provided lists as data set.
	 * 
	 * @param processes
	 *            List of processes to include.
	 * @param instances
	 *            List of process instances to include.
	 * @param summary
	 *            Log summary describing the provided data.
	 * @return Newly created instance.
	 */
	public static LogData createInstance(ArrayList<ProcessImpl> processes,
			ArrayList<ProcessInstanceImpl> instances,
			LightweightLogSummary summary) {
		System.gc(); // trigger old instances to be cleaned up
		return new LogData(processes, instances, summary);
	}

	/**
	 * Creates a new instance, which is a filtered, deep copy, i.e. clone, of
	 * the provided template instance.
	 * 
	 * @param template
	 *            Template instance, from which the event log data is used.
	 * @param filter
	 *            Log filter which is applied to the template data.
	 * @return Newly created, filtered clone of the template instance.
	 * @throws IOException
	 */
	public static LogData createInstance(LogData template, LogFilter filter)
			throws IOException {
		System.gc(); // trigger old instances to be cleaned up
		long duration = System.currentTimeMillis();
		LogData virgin = new LogData(new ArrayList<ProcessImpl>(),
				new ArrayList<ProcessInstanceImpl>(),
				new LightweightLogSummary(template.summary.getWorkflowLog()
						.getName(), template.summary.getWorkflowLog()
						.getDescription(), template.summary.getWorkflowLog()
						.getData(), template.summary.getWorkflowLog()
						.getModelReferences(), template.summary.getSource()
						.getName(), template.summary.getSource().getData(),
						template.summary.getSource().getModelReferences()));
		// create new temporary process copy list
		ArrayList<ProcessImpl> procTmpList = new ArrayList<ProcessImpl>();
		// copy list of processes without links to instances
		for (int i = 0; i < template.processes.size(); i++) {
			ProcessImpl pTmp = new ProcessImpl(template.processes.get(i));
			procTmpList.add(pTmp);
			// BVD: Add the process to the virgin, if the
			// RETAIN_PROCESS_IF_EMPTY attribute is set
			if (pTmp.getAttributes().get(RETAIN_PROCESS_IF_EMPTY) != null
					&& pTmp.getAttributes().get(RETAIN_PROCESS_IF_EMPTY)
							.equalsIgnoreCase("true")) {
				if (virgin.processes.contains(pTmp) == false) {
					virgin.processes.add(pTmp);
					virgin.summary.addProcess(pTmp.getName(), pTmp
							.getDescription(), pTmp.getDataAttributes(), pTmp
							.getModelReferences());
				}
			}
			// /BVD
		}
		// copy filtered list of instances
		ProcessInstanceImpl piTmp = null;
		for (int j = 0; j < template.instances.size(); j++) {
			piTmp = new ProcessInstanceImpl(template.instances.get(j));
			// apply filter to current process instance
			if (filter == null || filter.filter(piTmp) == true) {
				// some log filters do not correctly report when a
				// process instance does not pass the filter (i.e. no
				// audit trail entries are left after filtering).
				// additional check is provided as workaround:
				if (piTmp.getAuditTrailEntryList().size() == 0) {
					// raise error to warn filter authors
					Message
							.add(
									"Error: process instance has passed log filter "
											+ "'"
											+ filter.getName()
											+ "', but contains no audit trail entries!",
									Message.ERROR);
					// skip this process instance and continue
					continue;
				}
				// instance has (partly) passed the filter,
				// consolidate list of audit trail entries.
				piTmp.getAuditTrailEntryList().consolidate();
				// add to list of instances
				virgin.instances.add(piTmp);
				// add to respective process
				ProcessImpl pTmp = null;
				for (int k = 0; k < procTmpList.size(); k++) {
					pTmp = procTmpList.get(k);
					if (pTmp.getName().equals(piTmp.getProcess())) {
						pTmp.addProcessInstance(piTmp);
						// add respective process to new log data container
						if (virgin.processes.contains(pTmp) == false) {
							virgin.processes.add(pTmp);
							virgin.summary.addProcess(pTmp.getName(), pTmp
									.getDescription(),
									pTmp.getDataAttributes(), pTmp
											.getModelReferences());
						}
						// add instance to log summary
						virgin.summary.addProcessInstance(piTmp);
						break;
					}
				}
			}
		}
		// explicitly free temporary process list
		procTmpList = null;
		duration = System.currentTimeMillis() - duration;
		Message.add("Filtered "
				+ template.summary.getNumberOfAuditTrailEntries() + " > "
				+ virgin.summary.getNumberOfAuditTrailEntries() + " events in "
				+ duration + " msec. ", Message.DEBUG);
		// return filtered copy
		return virgin;
	}

	// BVD
	public boolean addProcessInstance(
			org.processmining.framework.log.Process p, ProcessInstanceImpl pi) {
		if (!processes.contains(p)) {
			// Problem, since this process is not contained in the LogData.
			return false;
		}
		if (!p.getName().equals(pi.getProcess())) {
			// Problem, process instance does not belong in process
			return false;
		}
		try {
			// add the process instance to the summary
			summary.addProcessInstance(pi);
			// success, so add the process instance to the process
			p.addProcessInstance(pi);
			// and add the process instance to the local reference list
			instances.add(pi);
		} catch (IOException ex) {
			return false;
		} catch (IndexOutOfBoundsException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Creates a new instance, which is a filtered, deep copy, i.e. clone, of
	 * the provided template instance. The clone will only contain the instances
	 * of the template, of which indices are contained in the provided integer
	 * array.
	 * 
	 * @param template
	 *            Template instance, from which the event log data is used.
	 * @param processInstancesToKeep
	 *            Array of integer indices indicating the process instances in
	 *            the template to be retained in the clone.
	 * @return Newly created, filtered clone of the template instance.
	 * @throws IOException
	 */
	public static LogData createInstance(LogData template,
			int[] processInstancesToKeep) throws IOException {
		System.gc(); // trigger old instances to be cleaned up
		LogData virgin = new LogData(new ArrayList<ProcessImpl>(),
				new ArrayList<ProcessInstanceImpl>(),
				new LightweightLogSummary(template.summary.getWorkflowLog()
						.getName(), template.summary.getWorkflowLog()
						.getDescription(), template.summary.getWorkflowLog()
						.getData(), template.summary.getWorkflowLog()
						.getModelReferences(), template.summary.getSource()
						.getName(), template.summary.getSource().getData(),
						template.summary.getSource().getModelReferences()));
		// create new temporary process copy list
		ArrayList<ProcessImpl> procTmpList = new ArrayList<ProcessImpl>();
		// copy list of processes without links to instances
		for (int i = 0; i < template.processes.size(); i++) {
			procTmpList.add(new ProcessImpl(template.processes.get(i)));
		}
		// copy filtered list of instances
		ProcessInstanceImpl piTmp = null;
		for (int j = 0; j < processInstancesToKeep.length; j++) {
			// pick instance referred in the given array of instances to keep
			piTmp = new ProcessInstanceImpl(template.instances
					.get(processInstancesToKeep[j]));
			// add to list of instances
			virgin.instances.add(piTmp);
			// add to respective process
			ProcessImpl pTmp = null;
			for (int k = 0; k < procTmpList.size(); k++) {
				pTmp = procTmpList.get(k);
				if (pTmp.getName().equals(piTmp.getProcess())) {
					pTmp.addProcessInstance(piTmp);
					// add respective process to new log data container
					if (virgin.processes.contains(pTmp) == false) {
						virgin.processes.add(pTmp);
						virgin.summary.addProcess(pTmp.getName(), pTmp
								.getDescription(), pTmp.getDataAttributes(),
								pTmp.getModelReferences());
					}
					// add instance to log summary
					virgin.summary.addProcessInstance(piTmp);
					break;
				}
			}
		}
		// explicitly free temporary process list
		procTmpList = null;
		// return filtered copy
		return virgin;
	}

	/**
	 * This method creates a new log data container instance directly from
	 * parsing a supplied MXML log file. No filtering, or other log-distorting
	 * operations, is performed, so that the resulting log data container
	 * truthfully represents the parsed log file.
	 * 
	 * @param file
	 *            Log file to be parsed in MXML format.
	 * @return The newly created log data container.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static LogData createInstance(LogFile file)
			throws ParserConfigurationException, SAXException, IOException {
		System.gc(); // trigger old instances to be cleaned up
		// create an empty log data container
		LogData virgin = new LogData(new ArrayList<ProcessImpl>(),
				new ArrayList<ProcessInstanceImpl>(),
				new LightweightLogSummary());
		// set up a specialized SAX2 handler to fill the container
		MxmlHandler mxmlHandler = virgin.new MxmlHandler(virgin, file);
		// set up SAX parser and parse provided log file into the container
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(file.getInputStream(), mxmlHandler);
		// return the newly equipped log data container
		return virgin;
	}

	/**
	 * This method creates a new log data container instance directly from
	 * parsing a supplied MXML log file. No filtering, or other log-distorting
	 * operations, is performed, so that the resulting log data container
	 * truthfully represents the parsed log file.
	 * 
	 * @param files
	 *            List of log files to be parsed in MXML format.
	 * @return The newly created log data container.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static LogData createInstance(List<LogFile> files)
			throws ParserConfigurationException, SAXException, IOException {
		// sanity check
		if (files == null || files.size() == 0) {
			throw new IOException("No log files to parse!");
		}
		System.gc(); // trigger old instances to be cleaned up
		// create an empty log data container
		LogData virgin = new LogData(new ArrayList<ProcessImpl>(),
				new ArrayList<ProcessInstanceImpl>(),
				new LightweightLogSummary());
		// set up a specialized SAX2 handler to fill the container
		MxmlHandler mxmlHandler = virgin.new MxmlHandler(virgin, files.get(0));
		// set up SAX parser and parse provided log files into the container
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		for (LogFile file : files) {
			parser.parse(file.getInputStream(), mxmlHandler);
		}
		// return the newly equipped log data container
		return virgin;
	}

	/**
	 * Simple protected constructor.
	 * 
	 * @param processes
	 *            Processes to store.
	 * @param instances
	 *            Process instances to store.
	 */
	protected LogData(ArrayList<ProcessImpl> processes,
			ArrayList<ProcessInstanceImpl> instances,
			LightweightLogSummary summary) {
		this.processes = processes;
		this.instances = instances;
		this.summary = summary;
	}

	/**
	 * Provides direct access to the list of contained Processes.
	 * 
	 * @return The list of contained processes.
	 */
	public synchronized ArrayList<ProcessImpl> processes() {
		return processes;
	}

	/**
	 * Provides direct access to the list of contained process instances.
	 * 
	 * @return The list of contained process instances.
	 */
	public synchronized ArrayList<ProcessInstanceImpl> instances() {
		return instances;
	}

	/**
	 * Provides direct access to the summary describing the contained event log
	 * data.
	 * 
	 * @return
	 */
	public synchronized LightweightLogSummary summary() {
		return summary;
	}

	protected synchronized void ensureIntegrity()
			throws IndexOutOfBoundsException, IOException {
		for (ProcessInstanceImpl pi : instances) {
			System.out.print("Probing instance " + pi.getName() + "...");
			for (int i = 0; i < pi.getAuditTrailEntryList().size(); i++) {
				pi.getAuditTrailEntryList().get(i).getElement();
			}
			System.out.println("done!");
		}
		System.out.println("all set and ready to go!");
	}

	/*
	 * ***********************************************************************
	 * Protected SAX2 handler to allow parsing log data directly from a provided
	 * log file.
	 */

	/**
	 * This class iplements a SAX2 handler for sequential parsing of MXML
	 * documents. It is geared towards directly constructing a log reader's data
	 * structures, of which it is a protected class.
	 * 
	 * @author Christian W. Guenther (christian at deckfour dot org)
	 */
	protected class MxmlHandler extends DefaultHandler {

		/**
		 * Parent log data container, which is used for storing data.
		 */
		protected LogData parent = null;
		/**
		 * Log file, which is parsed to extract the parent container's data.
		 */
		protected LogFile file = null;
		/**
		 * Buffer for characters.
		 */
		protected StringBuffer buffer = null;
		// buffering attributes, to keep data between
		// start and end element events.
		protected AuditTrailEntryImpl entry = null;
		protected String unknownType = null;
		protected String attributeName = null;
		protected List<String> attributeModelRefs = null;
		protected boolean sourceOpen = false;
		protected ProcessImpl currentProcess = null;
		protected ProcessInstanceImpl currentInstance = null;
		protected Date lastTimestamp = null;
		protected int numUnorderedEntries = 0;

		/**
		 * Creates a new SAX2 handler instance.
		 * 
		 * @param aData
		 *            Parent container to store data to.
		 * @param aFile
		 *            LogFile used for parsing.
		 */
		protected MxmlHandler(LogData aData, LogFile aFile) {
			parent = aData;
			file = aFile;
			buffer = new StringBuffer();
			entry = null;
			attributeName = null;
			attributeModelRefs = null;
			sourceOpen = false;
			currentProcess = null;
			currentInstance = null;
			lastTimestamp = null;
			numUnorderedEntries = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
		 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			String tagName = localName;
			if (tagName.equalsIgnoreCase("")) {
				tagName = qName;
			}
			try {
				// probe element type
				if (tagName.equalsIgnoreCase("WorkflowLog")) {
					// start workflow log
					parent.summary.getWorkflowLog().setDescription(
							attributes.getValue(attributes
									.getIndex("description")));
					parent.summary.getWorkflowLog()
							.setName(file.getShortName());
					parent.summary.getWorkflowLog().setModelReferences(
							parseModelReferences(attributes));
				} else if (tagName.equalsIgnoreCase("Source")) {
					// start source
					sourceOpen = true;
					String program = attributes.getValue(attributes
							.getIndex("program"));
					parent.summary.getSource().setName(program);
					parent.summary.getSource().setModelReferences(
							parseModelReferences(attributes));
					parent.summary.getSource().addAttribute("program", program); // backward
					// compatibility
				} else if (tagName.equalsIgnoreCase("Process")) {
					// check if process is already contained
					String procId = attributes.getValue("id");
					String procDescr = attributes.getValue("description");
					currentProcess = null;
					for (ProcessImpl proc : parent.processes()) {
						if (proc.getName().equals(procId)) {
							// process already contained
							currentProcess = proc;
							break;
						}
					}
					if (currentProcess == null) {
						// start process
						currentProcess = new ProcessImpl(procId, procDescr,
								new DataSection(),
								parseModelReferences(attributes));
						parent.processes.add(currentProcess);
						parent.summary.addProcess(currentProcess.getName(),
								currentProcess.getDescription(), currentProcess
										.getDataAttributes(), currentProcess
										.getModelReferences());
					}
				} else if (tagName.equalsIgnoreCase("ProcessInstance")) {
					// start process instance
					currentInstance = new ProcessInstanceImpl(currentProcess
							.getName(), parseModelReferences(attributes));
					currentInstance.setName(attributes.getValue(attributes
							.getIndex("id")));
					currentInstance.setDescription(attributes
							.getValue(attributes.getIndex("description")));
				} else if (tagName.equalsIgnoreCase("AuditTrailEntry")) {
					// start audit trail entry
					entry = new AuditTrailEntryImpl();
				} else if (tagName.equalsIgnoreCase("Attribute")) {
					// set current attribute name
					attributeName = attributes.getValue(
							attributes.getIndex("name")).trim();
					attributeModelRefs = parseModelReferences(attributes);
				} else if (tagName.equalsIgnoreCase("EventType")) {
					// set current unknown event type
					if (attributes.getIndex("unknowntype") >= 0) {
						unknownType = attributes.getValue(attributes
								.getIndex("unknowntype"));
					} else {
						unknownType = null;
					}
					entry
							.setTypeModelReferences(parseModelReferences(attributes));
				} else if (tagName.equalsIgnoreCase("WorkflowModelElement")) {
					// started workflow model element
					entry
							.setElementModelReferences(parseModelReferences(attributes));
				} else if (tagName.equalsIgnoreCase("Originator")) {
					// started originator
					entry
							.setOriginatorModelReferences(parseModelReferences(attributes));
				}
				// sh*t happens here:
			} catch (IOException e) {
				// oops...
				System.err.println("Error in " + this.getClass().toString()
						+ ", while parsing starting element:");
				e.printStackTrace();
			}
		}

		private List<String> parseModelReferences(Attributes attrs) {
			int index = attrs.getIndex("modelReference");

			if (index >= 0) {
				List<String> result = new ArrayList<String>(1);
				String elements = attrs.getValue(index);
				for (String uri : elements.split(" ")) {
					if (uri.length() > 0) {
						result.add(uri);
					}
				}
				return result;
			} else {
				return EMPTY_LIST;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
		 * java.lang.String, java.lang.String)
		 */
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			String tagName = localName;
			if (tagName.equalsIgnoreCase("")) {
				tagName = qName;
			}
			try {
				if (tagName.equalsIgnoreCase("WorkflowLog")) {
					// finished reading
					if (numUnorderedEntries > 0) {
						// let number of unordered entries be shown
						Message.add("LogData: Log contains "
								+ numUnorderedEntries
								+ " audit trail entries in non-natural order!",
								Message.ERROR);
						Message
								.add(
										"LogData: The log file you have loaded is not MXML compliant! (error compensated transparently)",
										Message.ERROR);
					}
				} else if (tagName.equalsIgnoreCase("Process")) {
					// finished process
					// add only, if it contains instances
					// (after filtering, that is)
					// BVD: keep the empty process, if the
					// RETAIN_PROCESS_IF_EMPTY attribute is set
					if ((currentProcess.size() == 0)
							&& (currentProcess.getAttributes().get(
									RETAIN_PROCESS_IF_EMPTY) == null || !currentProcess
									.getAttributes().get(
											RETAIN_PROCESS_IF_EMPTY)
									.equalsIgnoreCase("true"))) {
						parent.processes.remove(currentProcess);
						parent.summary.removeProcess(currentProcess.getName());
					}
					currentProcess = null;
				} else if (tagName.equalsIgnoreCase("Source")) {
					// finished source
					sourceOpen = false;
				} else if (tagName.equalsIgnoreCase("ProcessInstance")) {
					// finished process instance
					if (currentInstance.getAuditTrailEntryList().size() > 0) {
						// only use non-empty instances
						parent.instances.add(currentInstance);
						parent.summary.addProcessInstance(currentInstance);
						currentProcess.addProcessInstance(currentInstance);
					}
					currentInstance = null;
					// reset last timestamp
					lastTimestamp = null;
				} else if (tagName.equalsIgnoreCase("AuditTrailEntry")) {
					// finished audit trail entry
					Date timestamp = entry.getTimestamp();
					if (timestamp == null) {
						// no timestamp defaults to appending
						currentInstance.getAuditTrailEntryList().append(entry);
					} else if (lastTimestamp == null) {
						// no previous timestamp; append and remember timestamp
						currentInstance.getAuditTrailEntryList().append(entry);
						lastTimestamp = timestamp;
					} else {
						// both timestamp and previous timestamp present
						if (timestamp.compareTo(lastTimestamp) >= 0) {
							// last element in list as of timestamp order,
							// append and update reference timestamp
							currentInstance.getAuditTrailEntryList().append(
									entry);
							lastTimestamp = timestamp;
						} else {
							// audit trail entry is located somewhere in the
							// middle of
							// the list; insert ordered, without updating
							// reference timestamp
							currentInstance.getAuditTrailEntryList()
									.insertOrdered(entry);
						}
					}
					entry = null;
				} else if (tagName.equalsIgnoreCase("Attribute")) {
					// check where to put this attribute,
					// proceed bottom-up:
					if (entry != null) {
						entry.getDataAttributes()
								.put(
										new DataAttribute(attributeName, buffer
												.toString().trim(),
												attributeModelRefs));
					} else if (currentInstance != null) {
						currentInstance.getDataAttributes()
								.put(
										new DataAttribute(attributeName, buffer
												.toString().trim(),
												attributeModelRefs));
					} else if (currentProcess != null) {
						currentProcess.getDataAttributes()
								.put(
										new DataAttribute(attributeName, buffer
												.toString().trim(),
												attributeModelRefs));
					} else if (sourceOpen == true) {
						parent.summary.getSource()
								.addAttribute(
										new DataAttribute(attributeName, buffer
												.toString().trim(),
												attributeModelRefs));
					} else {
						parent.summary.getWorkflowLog()
								.addAttribute(
										new DataAttribute(attributeName, buffer
												.toString().trim(),
												attributeModelRefs));
					}
					// reset attribute name
					attributeName = null;
					attributeModelRefs = null;
				} else if (tagName.equalsIgnoreCase("EventType")) {
					// finished event type
					entry.setType(buffer.toString().trim());
					if (unknownType != null) {
						// type was in fact unknown
						entry.setType(unknownType.trim());
						unknownType = null;
					}
				} else if (tagName.equalsIgnoreCase("WorkflowModelElement")) {
					// finished workflow model element
					entry.setElement(buffer.toString().trim());
				} else if (tagName.equalsIgnoreCase("Timestamp")) {
					// finished timestamp
					String tsString = buffer.toString().trim();
					Date timestamp = XmlUtils.parseXsDateTime(tsString);
					if (timestamp != null) {
						entry.setTimestamp(timestamp);
					}
				} else if (tagName.equalsIgnoreCase("Originator")) {
					// finished originator
					String originator = buffer.toString().trim();
					if (originator.length() == 0) {
						originator = null;
					}
					entry.setOriginator(originator);
				}
			} catch (IOException e) {
				// oops...
				System.err.println("Error in " + this.getClass().toString()
						+ ", while parsing ending element:");
				e.printStackTrace();
			}
			// reset character buffer
			buffer.delete(0, buffer.length());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] str, int offset, int len)
				throws SAXException {
			// append characters to buffer
			buffer.append(str, offset, len);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[],
		 * int, int)
		 */
		public void ignorableWhitespace(char[] str, int offset, int len)
				throws SAXException {
			// append whitespace to buffer
			buffer.append(str, offset, len);
		}

	}
}
