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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;

/**
 * @author christian
 * 
 */
public class PerformanceProfile extends AbstractProfile {

	/**
	 * @param log
	 * @throws IOException
	 * @throws IndexOutOfBoundsException
	 */
	public PerformanceProfile(LogReader log) throws IndexOutOfBoundsException,
			IOException {
		super("Performance", "Compares the performance of cases", log);
		buildProfile(log);
	}

	protected void buildProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		for (int c = 0; c < log.numberOfInstances(); c++) {
			AuditTrailEntryList ateList = log.getInstance(c)
					.getAuditTrailEntryList();
			// calculate global metrics
			setValue(c, "numberOfEvents", ateList.size());
			// calculate transition metrics
			double minTransitionTime = 0.0;
			double maxTransitionTime = 0.0;
			double meanTransitionTime = 0.0;
			ArrayList<Double> transitionList = new ArrayList<Double>();
			int transitionCounter = 0;
			Date current = null, last = null;
			// walk the process instance
			for (int i = 0; i < ateList.size(); i++) {
				current = ateList.get(i).getTimestamp();
				// check for valid timestamp
				if (current != null) {
					// check for valid predecessor timestamp
					if (last != null) {
						// calculate transition time
						double time = new Double(current.getTime()
								- last.getTime());
						time /= 1000.0;
						// adjust mean
						meanTransitionTime += time;
						transitionCounter++;
						// adjust min
						if (time < minTransitionTime) {
							minTransitionTime = time;
						}
						// adjust max
						if (time > maxTransitionTime) {
							maxTransitionTime = time;
						}
						// add to transition list (for median)
						transitionList.add(time);
					}
					// swap predecessor timestamp
					last = current;
				} else {
					// current event has no timestamp; void last one remembered
					last = null;
				}
			}
			if (transitionCounter > 0 && maxTransitionTime > 0.0) {
				setValue(c, "caseDuration", measureDuration(ateList));
				// set transition measures for instance
				setValue(c, "minTransitionTime", minTransitionTime);
				setValue(c, "maxTransitionTime", maxTransitionTime);
				meanTransitionTime /= transitionCounter;
				setValue(c, "meanTransitionTime", meanTransitionTime);
				if (transitionList.size() > 0) {
					Collections.sort(transitionList);
					int medianIndex = transitionList.size() / 2;
					double medianTransitionTime = transitionList
							.get(medianIndex);
					setValue(c, "medianTransitionTime", medianTransitionTime);
				}
			}
		}
	}

	/**
	 * helper method: measures the total duration of a case (zero if no
	 * timestamps present) in milliseconds
	 * 
	 * @param ateList
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	protected double measureDuration(AuditTrailEntryList ateList)
			throws IndexOutOfBoundsException, IOException {
		Date first = null;
		Date last = null;
		for (int i = 0; i < ateList.size(); i++) {
			first = ateList.get(i).getTimestamp();
			if (first != null) {
				break;
			}
		}
		for (int i = ateList.size() - 1; i >= 0; i--) {
			last = ateList.get(i).getTimestamp();
			if (last != null) {
				break;
			}
		}
		if (first != null && last != null && first != last) {
			return (double) (last.getTime() - first.getTime()) / 1000.0;
		} else {
			return 0.0;
		}
	}

}
