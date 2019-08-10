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
package org.processmining.exporting.fuzzyModel;

import java.io.IOException;
import java.io.OutputStream;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.lib.xml.Document;
import org.processmining.lib.xml.Tag;
import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryMetric;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryMetric;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyModelSerializer {

	protected static final int VERSION = 1;

	public static void serialize(FuzzyGraph model, OutputStream out)
			throws IOException {
		int size = model.getNumberOfInitialNodes();
		Document doc = new Document(out);
		// create root node with size attribute
		Tag root = doc.addNode("fuzzyModel");
		root.addAttribute("size", Integer.toString(model
				.getNumberOfInitialNodes()));
		root.addAttribute("version", Integer.toString(VERSION));
		// add attributes section
		if (model.getAttributeKeys().size() > 0) {
			Tag attributes = root.addChildNode("attributes");
			attributes.addAttribute("size", Integer.toString(model
					.getAttributeKeys().size()));
			for (String key : model.getAttributeKeys()) {
				Tag attribute = attributes.addChildNode("attribute");
				attribute.addAttribute("key", key);
				attribute.addAttribute("value", model.getAttribute(key));
			}
		}
		// add log events section
		Tag logEvents = root.addChildNode("logEvents");
		LogEvents events = model.getLogEvents();
		logEvents.addAttribute("size", Integer.toString(events.size()));
		for (int i = 0; i < events.size(); i++) {
			LogEvent event = events.get(i);
			Tag logEvent = logEvents.addChildNode("logEvent");
			logEvent.addAttribute("element", event.getModelElementName());
			logEvent.addAttribute("type", event.getEventType());
			logEvent.addAttribute("occurrence", Integer.toString(event
					.getOccurrenceCount()));
		}
		// add metrics section
		Tag metrics = root.addChildNode("metrics");
		Tag unarySig = metrics.addChildNode("unarySignificance");
		unarySig.addAttribute("size", Integer.toString(model
				.getNodeSignificanceMetric().size()));
		unarySig.addTextNode(serializeUnaryMetricValues(model
				.getNodeSignificanceMetric()));
		Tag binarySig = metrics.addChildNode("binarySignificance");
		binarySig.addAttribute("size", Integer.toString(model
				.getEdgeSignificanceMetric().size()));
		binarySig.addTextNode(serializeBinaryMetricValues(model
				.getEdgeSignificanceMetric()));
		Tag binaryCorr = metrics.addChildNode("binaryCorrelation");
		binaryCorr.addAttribute("size", Integer.toString(model
				.getEdgeCorrelationMetric().size()));
		binaryCorr.addTextNode(serializeBinaryMetricValues(model
				.getEdgeCorrelationMetric()));
		// add transformed metrics section
		Tag transformedMetrics = root.addChildNode("transformedMetrics");
		// transformed significance
		Tag sigTransformed = transformedMetrics
				.addChildNode("transformedBinarySignificance");
		sigTransformed.addAttribute("size", Integer.toString(model
				.getNumberOfInitialNodes()));
		StringBuilder builder = new StringBuilder();
		builder.append(model.getBinarySignificance(0, 0));
		for (int y = 1; y < size; y++) {
			builder.append(";");
			builder.append(model.getBinarySignificance(0, y));
		}
		for (int x = 1; x < size; x++) {
			for (int y = 0; y < size; y++) {
				builder.append(";");
				builder.append(model.getBinarySignificance(x, y));
			}
		}
		sigTransformed.addTextNode(builder.toString());
		// transformed correlation
		Tag corrTransformed = transformedMetrics
				.addChildNode("transformedBinaryCorrelation");
		corrTransformed.addAttribute("size", Integer.toString(model
				.getNumberOfInitialNodes()));
		builder = new StringBuilder();
		builder.append(model.getBinaryCorrelation(0, 0));
		for (int y = 1; y < size; y++) {
			builder.append(";");
			builder.append(model.getBinaryCorrelation(0, y));
		}
		for (int x = 1; x < size; x++) {
			for (int y = 0; y < size; y++) {
				builder.append(";");
				builder.append(model.getBinaryCorrelation(x, y));
			}
		}
		corrTransformed.addTextNode(builder.toString());
		// add removed nodes section
		Tag abstractedNodes = root.addChildNode("abstractedNodes");
		for (int i = 0; i < size; i++) {
			if (model.getNodeMappedTo(i) == null) {
				// abstracted node
				Tag abstracted = abstractedNodes.addChildNode("abstractedNode");
				abstracted.addAttribute("index", Integer.toString(i));
			}
		}
		// add cluster section
		Tag clusters = root.addChildNode("clusters");
		clusters.addAttribute("size", Integer.toString(model.getClusterNodes()
				.size()));
		for (ClusterNode clusterNode : model.getClusterNodes()) {
			Tag cluster = clusters.addChildNode("cluster");
			cluster.addAttribute("index", Integer.toString(clusterNode
					.getIndex()));
			cluster.addAttribute("name", clusterNode.getElementName());
			Node[] primitives = clusterNode.getPrimitives().toArray(
					new Node[clusterNode.getPrimitives().size()]);
			builder = new StringBuilder();
			builder.append(primitives[0].getIndex());
			for (int i = 1; i < primitives.length; i++) {
				builder.append(";");
				builder.append(primitives[i].getIndex());
			}
			cluster.addAttribute("size", Integer.toString(primitives.length));
			cluster.addTextNode(builder.toString());
		}
		// close document to flush output stream
		doc.close();
	}

	protected static String serializeUnaryMetricValues(UnaryMetric metric) {
		StringBuilder builder = new StringBuilder();
		int size = metric.size();
		builder.append(metric.getMeasure(0));
		for (int i = 1; i < size; i++) {
			builder.append(";");
			builder.append(metric.getMeasure(i));
		}
		return builder.toString();
	}

	protected static String serializeBinaryMetricValues(BinaryMetric metric) {
		StringBuilder builder = new StringBuilder();
		int size = metric.size();
		builder.append(metric.getMeasure(0, 0));
		for (int y = 1; y < size; y++) {
			builder.append(";");
			builder.append(metric.getMeasure(0, y));
		}
		for (int x = 1; x < size; x++) {
			for (int y = 0; y < size; y++) {
				builder.append(";");
				builder.append(metric.getMeasure(x, y));
			}
		}
		return builder.toString();
	}

}
