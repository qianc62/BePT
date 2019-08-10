/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.analysis.traceclustering.distance.sequence;

import org.processmining.analysis.traceclustering.distance.DistanceMatrix;
import org.processmining.analysis.traceclustering.distance.DistanceMetric;
import org.processmining.analysis.traceclustering.distance.FloatDistanceMatrix;
import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.analysis.traceclustering.profile.Profile;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public abstract class StringDistanceMetric extends DistanceMetric {

	/**
	 * @param name
	 * @param description
	 */
	protected StringDistanceMetric(String name, String description) {
		super(name, description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.traceclustering.distance.DistanceMetric#
	 * getDistance
	 * (org.processmining.analysis.traceclustering.model.InstancePoint,
	 * org.processmining.analysis.traceclustering.model.InstancePoint)
	 */
	@Override
	public double getDistance(InstancePoint pointA, InstancePoint pointB) {
		// TODO Auto-generated method stub
		return 0;
	}

	public abstract double getDistance(String a, String b);

	public double getDistance(LogEvents events, AuditTrailEntryList ateListA,
			AuditTrailEntryList ateListB) {
		String a = TraceStringEncoder.encode(ateListA, events);
		String b = TraceStringEncoder.encode(ateListB, events);
		/*
		 * System.out.println("Encoding of process instances:");
		 * System.out.println("A: '" + a + "'"); System.out.println("B: '" + b +
		 * "'"); System.out.println("--> Distance: " + getDistance(a, b) +
		 * "\n");
		 */
		return getDistance(a, b);
	}

	public DistanceMatrix getDistanceMatrix(LogReader log) {
		LogEvents events = log.getLogSummary().getLogEvents();
		FloatDistanceMatrix matrix = new FloatDistanceMatrix(log
				.numberOfInstances());
		for (int x = 0; x < log.numberOfInstances() - 1; x++) {
			// matrix.set(x, x, 0);
			AuditTrailEntryList ateListA = log.getInstance(x)
					.getAuditTrailEntryList();
			for (int y = x + 1; y < log.numberOfInstances(); y++) {
				// System.out.println("computing distance " +
				// log.getInstance(x).getName() + " <-> " +
				// log.getInstance(y).getName());
				AuditTrailEntryList ateListB = log.getInstance(y)
						.getAuditTrailEntryList();
				double distance = getDistance(events, ateListA, ateListB);
				matrix.set(x, y, distance);
				matrix.set(y, x, distance);
			}
		}
		return matrix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.traceclustering.distance.DistanceMetric#
	 * getDistanceMatrix
	 * (org.processmining.analysis.traceclustering.profile.Profile)
	 */
	@Override
	public DistanceMatrix getDistanceMatrix(Profile profile) {
		return null;
	}

}
