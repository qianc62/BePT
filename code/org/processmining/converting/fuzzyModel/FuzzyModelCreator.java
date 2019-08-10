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
package org.processmining.converting.fuzzyModel;

import java.util.ArrayList;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryMetric;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryMetric;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyModelCreator {

	protected LogEvents events;
	protected ArrayList<Double> eventSignificances;

	protected UnaryMetric nodeSignificance = null;
	protected BinaryMetric edgeSignificance = null;
	protected BinaryMetric edgeCorrelation = null;

	public FuzzyModelCreator(LogEvents events) {
		this.events = events;
		eventSignificances = new ArrayList<Double>();
		for (LogEvent event : events) {
			eventSignificances.add(1.0);
		}
	}

	public FuzzyModelCreator() {
		this(new LogEvents());
	}

	public void addNode(LogEvent event) {
		addNode(event.getModelElementName(), event.getEventType(), 1.0);
	}

	public void addNode(String name, String type, double significance) {
		assert (nodeSignificance == null);
		events.add(events.size(), new LogEvent(name, type));
		eventSignificances.add(significance);
	}

	public void addNode(String name, String type) {
		addNode(name, type, 1.0);
	}

	public void addNode(String name) {
		addNode(name, "complete", 1.0);
	}

	public void addEdge(String sourceName, String sourceType,
			String targetName, String targetType, double significance,
			double correlation) {
		ensureMetricsCreated();
		int sourceIndex = events.findLogEventNumber(sourceName, sourceType);
		int targetIndex = events.findLogEventNumber(targetName, targetType);
		edgeSignificance.setMeasure(sourceIndex, targetIndex, significance);
		edgeCorrelation.setMeasure(sourceIndex, targetIndex, correlation);
	}

	public void addEdge(LogEvent source, LogEvent target, double significance,
			double correlation) {
		addEdge(source.getModelElementName(), source.getEventType(), target
				.getModelElementName(), target.getEventType(), significance,
				correlation);
	}

	public void addEdge(String sourceName, String targetName,
			double significance, double correlation) {
		addEdge(sourceName, "complete", targetName, "complete", significance,
				correlation);
	}

	public void addEdge(String sourceName, String targetName) {
		addEdge(sourceName, targetName, 1.0, 1.0);
	}

	public MutableFuzzyGraph generateGraph() {
		ensureMetricsCreated();
		return new MutableFuzzyGraph(nodeSignificance, edgeSignificance,
				edgeCorrelation, events);
	}

	protected void ensureMetricsCreated() {
		if (nodeSignificance == null) {
			// create metrics
			int size = events.size();
			nodeSignificance = new UnaryMetric("Unary significance",
					"Node significance", size);
			for (int i = 0; i < size; i++) {
				nodeSignificance.setMeasure(i, eventSignificances.get(i));
			}
			edgeSignificance = new BinaryMetric("Binary significance",
					"Edge significance", size);
			edgeCorrelation = new BinaryMetric("Binary correlation",
					"Edge correlation", size);
		}
	}

}
