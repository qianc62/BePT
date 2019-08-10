package org.processmining.framework.models.causality;

import org.processmining.framework.models.ModelGraphVertex;
import java.util.HashSet;
import java.io.Serializable;
import java.io.IOException;
import java.io.BufferedWriter;
import org.w3c.dom.Node;
import org.processmining.framework.models.ModelGraph;
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
public class BackwardEdge {
	public ModelGraphVertex destination;
	public HashSet<ModelGraphVertex> sources;
	private CausalFootprint footprint;

	private BackwardEdge() {

	}

	public BackwardEdge(HashSet<ModelGraphVertex> sources,
			ModelGraphVertex destination, CausalFootprint footprint) {
		this.sources = sources;
		this.destination = destination;
		this.footprint = footprint;
	}

	public boolean equals(HashSet<ModelGraphVertex> sources,
			ModelGraphVertex destination) {
		return this.sources.equals(sources)
				&& this.destination.equals(destination);
	}

	/**
	 * Write the inside of the <backwardedge> tag in the XML export file to the
	 * OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	public void writeXML(BufferedWriter output) throws IOException {
		output.write("<destination>\n");
		footprint.writeVertexXML(destination, output);
		output.write("</destination>\n");
		output.write("<sources>\n");
		for (ModelGraphVertex v : sources) {
			footprint.writeVertexXML(v, output);
		}
		output.write("</sources>\n");

	}

	/**
	 * Read the inside of the <CausalFootprint> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	public static BackwardEdge readXML(Node edgeNode,
			HashMap<Long, ModelGraphVertex> stored2retrieved,
			CausalFootprint footprint) throws IOException,
			ClassNotFoundException, IllegalAccessException,
			InstantiationException {

		BackwardEdge edge = new BackwardEdge();
		edge.footprint = footprint;

		for (int i = 0; i < edgeNode.getChildNodes().getLength(); i++) {
			Node node = edgeNode.getChildNodes().item(i);
			if (node.getNodeName().equals("destination")) {
				// Read the nodeSpecific part of this LogFilter
				for (int j = 0; j < node.getChildNodes().getLength(); j++) {
					if (node.getChildNodes().item(j).getNodeName().equals(
							"vertex")) {
						edge.destination = footprint.readVertexXML(node
								.getChildNodes().item(j), footprint,
								stored2retrieved);
					}
				}
			} else if (node.getNodeName().equals("sources")) {
				edge.sources = new HashSet<ModelGraphVertex>(node
						.getChildNodes().getLength());
				for (int j = 0; j < node.getChildNodes().getLength(); j++) {
					if (node.getChildNodes().item(j).getNodeName().equals(
							"vertex")) {
						edge.sources.add(footprint.readVertexXML(node
								.getChildNodes().item(j), footprint,
								stored2retrieved));
					}
				}
			}
		}

		return edge;
	}
}
