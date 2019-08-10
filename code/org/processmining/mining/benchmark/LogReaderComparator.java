/**
 * Project: ProM
 * File: LogReaderComparator.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jun 2, 2006, 2:43:32 PM
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
package org.processmining.mining.benchmark;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.InfoItem;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.classic.LogReaderClassic;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.logabstraction.LogAbstraction;
import org.processmining.mining.logabstraction.LogAbstractionImpl;

/**
 * @author christian
 * 
 */
public class LogReaderComparator implements MiningPlugin {

	protected String messages = "";
	protected Progress progress = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#getOptionsPanel(org.processmining
	 * .framework.log.LogSummary)
	 */
	public JPanel getOptionsPanel(LogSummary summary) {
		return new JPanel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		messages = "";
		progress = new Progress("comparing logs..", 0, log.numberOfInstances());
		try {
			LogFile file = log.getFile();
			LogFilter filter = log.getLogFilter();
			LogReader classic = LogReaderClassic.createInstance(filter, file);
			LogReader nike = BufferedLogReader.createInstance(log, filter);
			compare(classic, nike);
			LogAbstraction laClassic = new LogAbstractionImpl(classic);
			LogAbstraction laNike = new LogAbstractionImpl(nike);
			if (laClassic.getEndInfo().equals(laNike.getEndInfo()) == false) {
				error("LogAbstraction.getEndInfo() returned conflicting results!");
			}
			if (laClassic.getCloseInInfo(2).equals(laNike.getCloseInInfo(2)) == false) {
				error("LogAbstraction.getCloseInInfo(2) returned conflicting results!");
			}
			if (laClassic.getFollowerInfo(1).equals(laNike.getFollowerInfo(1)) == false) {
				error("LogAbstraction.getFollowerInfo(1) returned conflicting results!");
			}
			if (laClassic.getStartInfo().equals(laNike.getStartInfo()) == false) {
				error("LogAbstraction.getStartInfo() returned conflicting results!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		progress.close();
		return new ComparatorResultPanel(log, messages);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "LogReader comparison driver";
	}

	public void compare(LogReader a, LogReader b) {
		// compare the number of contained process instances
		if (a.getLogSummary().getNumberOfProcessInstances() != b
				.getLogSummary().getNumberOfProcessInstances()) {
			error("Process instance count mismatch (" + a.numberOfInstances()
					+ " <-> " + b.numberOfInstances() + ")!");
		} else {
			message("Process instance count: MATCH.");
		}
		// compare log summary
		progress.setNote("comparing log summaries...");
		compare(a.getLogSummary(), b.getLogSummary());
		// compare process instances
		progress.setNote("comparing process instances...");
		for (int i = 0; i < a.numberOfInstances(); i++) {
			progress.setProgress(i);
			if (compare(a.getInstance(i), b.getInstance(i)) == false) {
				error("Process instance mismatch: # " + i);
			} else {
				message("Process instance # " + i + ": MATCH.");
			}
		}
	}

	protected boolean compare(ProcessInstance a, ProcessInstance b) {
		boolean result = true;
		AuditTrailEntries entriesA = a.getAuditTrailEntries();
		AuditTrailEntries entriesB = b.getAuditTrailEntries();
		if (entriesA.size() != entriesB.size()) {
			error("Process instance size mismatch: " + entriesA.size()
					+ " <-> " + entriesB.size());
			return false;
		}
		for (int i = 0; i < entriesA.size(); i++) {
			if (compare(entriesA.next(), entriesB.next()) == false) {
				error("Audit trail entry mismatch (#" + i + ")");
				result = false;
			}
		}
		// message("First: (a) " + a.getAuditTrailEntries().first() + "  (b) " +
		// b.getAuditTrailEntries().first());
		// message("Last: (a) " + a.getAuditTrailEntries().last() + "  (b) " +
		// b.getAuditTrailEntries().last());
		return result;
	}

	protected boolean compare(AuditTrailEntry a, AuditTrailEntry b) {
		boolean result = true;
		if (a.getElement().equals(b.getElement()) == false) {
			error("Element mismatch! (" + a.getElement() + " <-> "
					+ b.getElement() + ")");
			result = false;
		}
		if (a.getOriginator() != null
				&& a.getOriginator().equals(b.getOriginator()) == false) {
			error("Originator mismatch! (" + a.getOriginator() + " <-> "
					+ b.getOriginator() + ")");
			result = false;
		}
		if (a.getType().equals(b.getType()) == false) {
			error("Type mismatch! (" + a.getType() + " <-> " + b.getType()
					+ ")");
			result = false;
		}
		Date tsA = a.getTimestamp();
		Date tsB = b.getTimestamp();
		if (tsA != null && tsB == null) {
			error("Timestamp missing in (b) reader, present in (a)!");
			result = false;
		} else if (tsB != null && tsA == null) {
			error("Timestamp missing in (a) reader, present in (b)!");
			result = false;
		} else if (tsA != null && tsB != null && tsA.equals(tsB) == false) {
			error("Timestamp mismatch!: " + tsA + " <-> " + tsB);
			result = false;
		}
		Map<String, String> data = a.getAttributes();
		String key = null;
		for (Iterator it = data.keySet().iterator(); it.hasNext();) {
			key = (String) it.next();
			if (a.getAttributes().get(key).equals(b.getAttributes().get(key)) == false) {
				error("Attribute mismatch (" + key + ": "
						+ a.getAttributes().get(key) + " <-> "
						+ b.getAttributes().get(key) + ")!");
				result = false;
			}
		}
		return result;
	}

	protected void compare(LogSummary a, LogSummary b) {
		InfoItem[] processesA = a.getProcesses();
		InfoItem[] processesB = b.getProcesses();
		compareArrays(processesA, processesB);
		compareArrays(a.getEventTypes(), b.getEventTypes());
		compareArrays(a.getModelElements(), b.getModelElements());
		compareArrays(a.getOriginators(), b.getOriginators());
	}

	protected void compareArrays(Object[] a, Object[] b) {
		if (a.length != b.length) {
			error("Length mismatch: " + a.length + " <-> " + b.length + "!");
		}
		for (int i = 0; i < a.length; i++) {
			if (arrayContains(b, a[i]) == false) {
				error("Element" + a[i] + " missing from other! (a->b)");
			}
		}
		for (int i = 0; i < b.length; i++) {
			if (arrayContains(a, b[i]) == false) {
				error("Element" + b[i] + " missing from other! (b->a)");
			}
		}
	}

	protected boolean arrayContains(Object arr[], Object probe) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(probe)) {
				return true;
			}
		}
		return false;
	}

	protected void message(String msg) {
		messages += msg + "\n";
	}

	protected void error(String err) {
		messages += "ERROR: " + err + "\n";
	}

}
