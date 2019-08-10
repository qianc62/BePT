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
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;

/**
 * @author christian
 * 
 */
public class DataValueProfile extends AbstractProfile {

	/**
	 * @param log
	 * @throws IOException
	 * @throws IndexOutOfBoundsException
	 */
	public DataValueProfile(LogReader log) throws IndexOutOfBoundsException,
			IOException {
		super("Data value", "Compares the attribute values in events", log);
		buildProfile(log);
	}

	protected void buildProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		for (int c = 0; c < log.numberOfInstances(); c++) {
			AuditTrailEntryList ateList = log.getInstance(c)
					.getAuditTrailEntryList();
			for (int i = 0; i < ateList.size(); i++) {
				Map<String, String> attributes = ateList.get(i).getAttributes();
				for (String key : attributes.keySet()) {
					String item = key + "==" + attributes.get(key);
					incrementValue(c, item, 1.0);
				}
			}
		}
		// disable if no more than one attributes present in log
		if (this.getItemKeys().size() <= 1) {
			this.setNormalizationMaximum(0.0);
		}
	}

}
