/**
 * Project: ProM
 * File: Edge.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jul 5, 2006, 3:17:51 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.mining.graphclustering;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;

import org.processmining.framework.models.DotFileWriter;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class Edges implements DotFileWriter {

	protected HashSet<Edge> edges = null;
	protected double threshold = 0.0;

	public Edges() {
		edges = new HashSet<Edge>();
	}

	public void setThreshold(double aThreshold) {
		threshold = aThreshold;
	}

	public void addEdge(Node from, Node to, double frequency) {
		// no self-referencing edges of cluster nodes
		if (from.equals(to) && (from instanceof ClusterNode)) {
			return;
		}
		// try to find and increase frequency of existing edge
		for (Edge edge : edges) {
			if (edge.getSource().equals(from) && edge.getTarget().equals(to)) {
				edge.increaseFrequency(frequency);
				return;
			}
		}
		// add new edge to set
		edges.add(new Edge(from, to, frequency));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.DotFileWriter#writeToDot(java.io.Writer
	 * )
	 */
	public void writeToDot(Writer bw) throws IOException {
		for (Edge edge : edges) {
			edge.writeToDot(bw);
		}
	}

	protected class Edge {

		protected Node source = null;
		protected Node target = null;
		protected double frequency = 0.0;

		public Edge(Node source, Node target, double frequency) {
			this.source = source;
			this.target = target;
			this.frequency = frequency;
		}

		public Node getSource() {
			return source;
		}

		public Node getTarget() {
			return target;
		}

		public double getFrequency() {
			return frequency;
		}

		public void increaseFrequency(double offset) {
			frequency += offset;
		}

		public boolean equals(Object o) {
			if ((o instanceof Edge) == false) {
				return false;
			} else {
				Edge e = (Edge) o;
				return ((this.source.equals(e.getSource())) && (this.target
						.equals(e.getTarget())));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.processmining.framework.models.DotFileWriter#writeToDot(java.
		 * io.Writer)
		 */
		public void writeToDot(Writer bw) throws IOException {
			bw.write(source.getId() + " -> " + target.getId() + " [label=\""
					+ ClusterGraph.format(frequency) + "\"");
			if (frequency < threshold) {
				int grayValue = 80 - (int) ((frequency / threshold) * 80.0);
				bw.write("color=\"gray" + grayValue + "\"");
			}
			bw.write("];\n");
		}
	}
}
