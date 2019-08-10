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
package org.processmining.analysis.traceclustering.profile;

import java.io.IOException;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;

/**
 * @author christian
 * 
 */
public class TransitionProfile extends AbstractProfile {

	/**
	 * @param log
	 * @throws IOException
	 * @throws IndexOutOfBoundsException
	 */
	public TransitionProfile(LogReader log) throws IndexOutOfBoundsException,
			IOException {
		super("Transition", "Compares the event transitions in cases", log);
		buildProfile(log);
	}

	protected void buildProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		for (int c = 0; c < log.numberOfInstances(); c++) {
			AuditTrailEntryList ateList = log.getInstance(c)
					.getAuditTrailEntryList();
			AuditTrailEntry previous = ateList.get(0);
			AuditTrailEntry current = null;
			for (int i = 1; i < ateList.size(); i++) {
				current = ateList.get(i);
				incrementValue(c, encodeTransition(previous, current), 1.0);
				previous = current;
			}
			// disable if no more than one transition type present in log
			if (this.getItemKeys().size() <= 1) {
				this.setNormalizationMaximum(0.0);
			}
		}
	}

	protected static String encodeTransition(AuditTrailEntry ateA,
			AuditTrailEntry ateB) {
		return ateA.getElement() + "--" + ateA.getType() + "-->"
				+ ateB.getElement() + "--" + ateB.getType();
	}

}
