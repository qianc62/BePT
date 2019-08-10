/**
 * Project: ProM
 * File: Node.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jul 5, 2006, 2:40:40 PM
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
import java.util.Set;

import org.processmining.framework.models.DotFileWriter;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class Node implements DotFileWriter {

	protected int index = 0;
	protected ClusterGraph graph = null;

	public Node(ClusterGraph graph, int index) {
		this.graph = graph;
		this.index = index;
	}

	public String getId() {
		return "node" + index;
	}

	public int getIndex() {
		return index;
	}

	public ClusterGraph getGraph() {
		return graph;
	}

	public String getElement() {
		return graph.getLogEvents().get(index).getModelElementName();
	}

	public String getType() {
		return graph.getLogEvents().get(index).getEventType();
	}

	public double getFrequency() {
		return graph.normalize((double) graph.getLogEvents().get(index)
				.getOccurrenceCount());
	}

	public boolean isDirectlyConnectedTo(Node other) {
		if (other instanceof ClusterNode) {
			System.err.println("error!");
			return false;
		}
		if (this.equals(other)) {
			return false;
		}
		DoubleMatrix2D followMatrix = graph.getFollowMatrix();
		return ((followMatrix.get(index, other.getIndex()) > 0.0) || (followMatrix
				.get(other.getIndex(), index) > 0.0));
	}

	public Set<Node> getPredecessors() {
		HashSet<Node> predecessors = new HashSet<Node>();
		DoubleMatrix2D followMatrix = graph.getFollowMatrix();
		for (int x = 0; x < followMatrix.columns(); x++) {
			if (x == index) {
				continue; // skip self
			}
			if (followMatrix.get(x, index) > 0.0) {
				predecessors.add(graph.getNode(x));
			}
		}
		return predecessors;
	}

	public Set<Node> getSuccessors() {
		HashSet<Node> successors = new HashSet<Node>();
		DoubleMatrix2D followMatrix = graph.getFollowMatrix();
		for (int y = 0; y < followMatrix.rows(); y++) {
			if (y == index) {
				continue; // skip self
			}
			if (followMatrix.get(index, y) > 0.0) {
				successors.add(graph.getNode(y));
			}
		}
		return successors;
	}

	public boolean equals(Object o) {
		if ((o instanceof Node) == false) {
			return false;
		} else {
			Node n = (Node) o;
			return (this.graph.equals(n.getGraph()) && this.getIndex() == n
					.getIndex());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.DotFileWriter#writeToDot(java.io.Writer
	 * )
	 */
	public void writeToDot(Writer bw) throws IOException {
		bw.write(getId() + " [label=\"" + getElement() + "\\n" + getType()
				+ "\\n" + ClusterGraph.format(getFrequency()) + "\"];\n");
	}

	public String getToolTipText() {
		return "<html><table><tr colspan=\"2\"><td>" + getElement()
				+ "</td></tr>" + "<tr><td>Event type:</td><td>" + getType()
				+ "</td></tr>" + "<tr><td>Frequency:</td><td>"
				+ ClusterGraph.format(getFrequency()) + "</td></tr>";
	}

}
