/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.framework.models.recommendation.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.writing.LogSetSequential;
import org.processmining.lib.mxml.writing.impl.LogSetSequentialImpl;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RecommendationCompletedTraceMarshal {

	public String marshal(ProcessInstance trace) throws Exception {
		// set up MXMLib serialization environment
		StringLogPersistency sPersistency = new StringLogPersistency();
		LogSetSequential seqSet = new LogSetSequentialImpl(sPersistency,
				"ProM recommendation servide", "void");
		// write boilerplate process and process instance header
		seqSet.startProcess("TraceContainer", "Recommendation service created",
				null);
		seqSet.startProcessInstance(trace.getName(), trace.getDescription(),
				trace.getAttributes());
		// write events
		AuditTrailEntryList ateList = trace.getAuditTrailEntryList();
		org.processmining.framework.log.AuditTrailEntry promAte;
		org.processmining.lib.mxml.AuditTrailEntry mxmlibAte;
		for (int i = 0; i < ateList.size(); i++) {
			promAte = ateList.get(i);
			mxmlibAte = promAteToMxmlibAte(promAte);
			seqSet.addAuditTrailEntry(mxmlibAte);
		}
		// close instance and process and finish up
		seqSet.endProcessInstance();
		seqSet.endProcess();
		seqSet.finish();
		// return string output from persistency
		return sPersistency.getOutputString();
	}

	public ProcessInstance unmarshal(String traceXml) throws Exception {
		StringLogFile sLogFile = new StringLogFile(traceXml);
		LogReader log = LogReaderFactory.createInstance(new DefaultLogFilter(
				DefaultLogFilter.INCLUDE), sLogFile);
		return log.getInstance(0);
	}

	protected org.processmining.lib.mxml.AuditTrailEntry promAteToMxmlibAte(
			org.processmining.framework.log.AuditTrailEntry promAte) {
		org.processmining.lib.mxml.AuditTrailEntry mxmlibAte = new org.processmining.lib.mxml.AuditTrailEntry();
		mxmlibAte.setWorkflowModelElement(promAte.getElement());
		mxmlibAte.setEventType(EventType.getType(promAte.getType()));
		mxmlibAte.setTimestamp(promAte.getTimestamp());
		mxmlibAte.setOriginator(promAte.getOriginator());
		mxmlibAte.setAttributes(promAte.getAttributes());
		return mxmlibAte;
	}

	/**
	 * Internal class to implement a string-based logfile for the ProM log
	 * reader to read from.
	 * 
	 * @author Christian W. Guenther (christian@deckfour.org)
	 * 
	 */
	protected class StringLogFile extends LogFile {

		String data;

		public StringLogFile(String data) {
			this.data = data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.log.LogFile#getInputStream()
		 */
		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(data.getBytes());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.log.LogFile#getShortName()
		 */
		@Override
		public String getShortName() {
			return "string-based log file (recommendation service)";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.log.LogFile#toString()
		 */
		@Override
		public String toString() {
			return getShortName();
		}

	}

}
