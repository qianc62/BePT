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
package org.processmining.mining.fuzzymining.metrics.trace;

import java.io.IOException;
import java.util.HashSet;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;

/**
 * @author christian
 * 
 */
public class TraceVarietyMetric extends TraceMetric {

	/**
	 * @param aName
	 * @param aDescription
	 * @param log
	 */
	public TraceVarietyMetric(LogSummary logSummary) {
		super("Trace variety",
				"Measures a trace by the number of contained event classes.",
				logSummary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.TraceMetric#measure(org.
	 * processmining.framework.log.LogReader)
	 */
	public void measure(LogReader log) {
		AuditTrailEntryList ateList;
		AuditTrailEntry ate;
		HashSet<String> eventSet;
		for (int i = 0; i < log.numberOfInstances(); i++) {
			eventSet = new HashSet<String>();
			ateList = log.getInstance(i).getAuditTrailEntryList();
			for (int j = 0; j < ateList.size(); j++) {
				try {
					ate = ateList.get(j);
					eventSet.add(ate.getElement() + ate.getType());
				} catch (IndexOutOfBoundsException e) {
					// no critical error, fail gracefully
					e.printStackTrace();
				} catch (IOException e) {
					// no critical error, fail gracefully
					e.printStackTrace();
				}
			}
			values[i] = eventSet.size();
		}
	}

}
