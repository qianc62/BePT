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
package org.processmining.mining.organizationmining.profile;

import java.io.IOException;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;

/**
 * @author christian
 * 
 */
public class ActivityProfile extends AbstractProfile {

	/**
	 * @param log
	 *            The log file to be profiled
	 * @throws IOException
	 * @throws IndexOutOfBoundsException
	 */
	public ActivityProfile(LogReader log) throws IndexOutOfBoundsException,
			IOException {
		super("Activity", "Compares the occurrences of activities", log);
		buildProfile(log);
	}

	protected void buildProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		for (int i = 0; i < log.numberOfInstances(); i++) {
			AuditTrailEntryList ateList = log.getInstance(i)
					.getAuditTrailEntryList();
			for (int a = 0; a < ateList.size(); a++) {
				incrementValue(ateList.get(a).getOriginator(),
						encodeActivity(ateList.get(a)), 1.0);
			}
		}
	}

	public static String encodeActivity(AuditTrailEntry ate) {
		return ate.getElement() + "--" + ate.getType();
	}

}
