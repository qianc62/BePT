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
package org.processmining.mining.fuzzymining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.fuzzymining.attenuation.Attenuation;
import org.processmining.mining.fuzzymining.metrics.MetricsRepository;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryDerivateMetric;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryLogMetric;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryDerivateMetric;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryLogMetric;

/**
 * @author christian
 * 
 */
public class LogScanner {

	protected int maxLookBack = 5;
	protected ArrayList<AuditTrailEntry> lookBack;
	protected ArrayList<Integer> lookBackIndices;

	public void scan(LogReader log, MetricsRepository metrics,
			Attenuation attenuation, int maxLookBack, Progress progress,
			int initialProgressValue) throws IndexOutOfBoundsException,
			IOException {
		if (progress != null) {
			progress.setNote("Scanning log based metrics...");
		}
		this.maxLookBack = maxLookBack;
		LogEvents logEvents = log.getLogSummary().getLogEvents();
		ProcessInstance pi = null;
		AuditTrailEntryList ateList = null;
		AuditTrailEntry referenceAte, followerAte;
		int referenceIndex, followerIndex;
		double att;
		for (int i = 0; i < log.numberOfInstances(); i++) {
			lookBack = new ArrayList<AuditTrailEntry>(maxLookBack);
			lookBackIndices = new ArrayList<Integer>(maxLookBack);
			pi = log.getInstance(i);
			ateList = pi.getAuditTrailEntryList();
			for (int j = 0; j < ateList.size(); j++) {
				// update progress, if available
				if (progress != null) {
					initialProgressValue++;
					if (initialProgressValue % 200 == 0) {
						progress.setProgress(initialProgressValue);
					}
				}
				// update look back buffer with next audit trail entry
				followerAte = ateList.get(j);
				followerIndex = logEvents.findLogEventNumber(followerAte
						.getElement(), followerAte.getType());
				lookBack.add(0, followerAte);
				lookBackIndices.add(0, followerIndex);
				if (lookBack.size() > (maxLookBack + 1)) {
					// trim look back buffer
					lookBack.remove((maxLookBack + 1));
					lookBackIndices.remove((maxLookBack + 1));
				}
				// transmit event to unary metrics
				for (UnaryLogMetric metric : metrics.getUnaryLogMetrics()) {
					metric.measure(followerAte, followerIndex);
				}
				// iterate over multi-step relations
				for (int k = 1; k < lookBack.size(); k++) {
					referenceAte = lookBack.get(k);
					referenceIndex = lookBackIndices.get(k);
					att = attenuation.getAttenuationFactor(k);
					// transmit relation to all registered metrics
					for (BinaryLogMetric metric : metrics.getBinaryLogMetrics()) {
						metric.measure(referenceAte, followerAte,
								referenceIndex, followerIndex, att);
					}
				}
			}
		}
		// calculate derivate metrics
		List<UnaryDerivateMetric> unaryDerivateMetrics = metrics
				.getUnaryDerivateMetrics();
		for (UnaryDerivateMetric metric : unaryDerivateMetrics) {
			metric.measure();
		}
		List<BinaryDerivateMetric> binaryDerivateMetrics = metrics
				.getBinaryDerivateMetrics();
		for (BinaryDerivateMetric metric : binaryDerivateMetrics) {
			metric.measure();
		}
	}
}
