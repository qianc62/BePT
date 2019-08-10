package org.processmining.framework.models.causality;

import org.processmining.framework.models.ModelGraphVertex;
import java.util.HashSet;
import java.io.Serializable;
import java.io.IOException;
import java.io.BufferedWriter;
import org.w3c.dom.Node;
import java.util.HashMap;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class ForwardEdge {
	public ModelGraphVertex source;
	public HashSet<ModelGraphVertex> destinations;
	private CausalFootprint footprint;

	private ForwardEdge() {

	}

	public ForwardEdge(ModelGraphVertex source,
			HashSet<ModelGraphVertex> destinations, CausalFootprint footprint) {
		this.source = source;
		this.destinations = destinations;
		this.footprint = footprint;
	}

	public boolean equals(ModelGraphVertex source,
			HashSet<ModelGraphVertex> destinations) {
		return this.source.equals(source)
				&& this.destinations.equals(destinations);
	}

	/**
	 * Write the inside of the <backwardedge> tag in the XML export file to the
	 * OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	public void writeXML(BufferedWriter output) throws IOException {
		output.write("<source>\n");
		footprint.writeVertexXML(source, output);
		output.write("</source>\n");
		output.write("<destinations>\n");
		for (ModelGraphVertex v : destinations) {
			footprint.writeVertexXML(v, output);
		}
		output.write("</destinations>\n");

	}

	/**
	 * Read the inside of the <CausalFootprint> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	public static ForwardEdge readXML(Node edgeNode,
			HashMap<Long, ModelGraphVertex> stored2retrieved,
			CausalFootprint footprint) throws IOException,
			ClassNotFoundException, IllegalAccessException,
			InstantiationException {

		ForwardEdge edge = new ForwardEdge();
		edge.footprint = footprint;

		for (int i = 0; i < edgeNode.getChildNodes().getLength(); i++) {
			Node node = edgeNode.getChildNodes().item(i);
			if (node.getNodeName().equals("source")) {
				// Read the nodeSpecific part of this LogFilter
				for (int j = 0; j < node.getChildNodes().getLength(); j++) {
					if (node.getChildNodes().item(j).getNodeName().equals(
							"vertex")) {
						edge.source = footprint.readVertexXML(node
								.getChildNodes().item(j), footprint,
								stored2retrieved);
					}
				}
			} else if (node.getNodeName().equals("destinations")) {
				edge.destinations = new HashSet<ModelGraphVertex>(node
						.getChildNodes().getLength());
				for (int j = 0; j < node.getChildNodes().getLength(); j++) {
					if (node.getChildNodes().item(j).getNodeName().equals(
							"vertex")) {
						edge.destinations.add(footprint.readVertexXML(node
								.getChildNodes().item(j), footprint,
								stored2retrieved));
					}
				}
			}
		}

		return edge;
	}

}
