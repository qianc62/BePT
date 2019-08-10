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
package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;

/**
 * @author christian
 * 
 */
public class TimestampInjectionFilter extends LogFilter {

	protected long refTime = System.currentTimeMillis();
	protected long interInstanceIncrement = 60000;
	protected long interEventIncrement = 10000;

	/**
	 * @param load
	 * @param name
	 */
	public TimestampInjectionFilter() {
		super(LogFilter.FAST, "Timestamp injection filter");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#doFiltering(org.processmining
	 * .framework.log.ProcessInstance)
	 */
	@Override
	protected boolean doFiltering(ProcessInstance instance) {
		refTime += interInstanceIncrement;
		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		AuditTrailEntry current;
		for (int i = 0; i < ateList.size(); i++) {
			try {
				current = ateList.get(i);
				current.setTimestamp(new Date(refTime));
				ateList.replace(current, i);
				refTime += interEventIncrement;
			} catch (IOException e) {
				// error?
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#getHelpForThisLogFilter()
	 */
	@Override
	protected String getHelpForThisLogFilter() {
		return "Injects timestamps with regular distances into the log";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#getParameterDialog(org.
	 * processmining.framework.log.LogSummary)
	 */
	@Override
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary, this) {
			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				return new TimestampInjectionFilter();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#thisFilterChangesLog()
	 */
	@Override
	protected boolean thisFilterChangesLog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#writeSpecificXML(java.io.
	 * BufferedWriter)
	 */
	@Override
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// ignore for now
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#readSpecificXML(org.w3c.dom
	 * .Node)
	 */
	@Override
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		// ignore for now
	}

}
