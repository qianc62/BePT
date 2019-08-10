/**
 * Project: ProM
 * File: ClusterNode.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jul 5, 2006, 2:41:02 PM
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import org.processmining.framework.util.Dot;

import att.grappa.Graph;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ClusterNode extends Node {

	protected static int instanceCounter = 0;

	public static synchronized void resetInstanceCounter() {
		instanceCounter = 0;
	}

	protected HashSet<Node> primitives = null;
	protected int instanceNumber = 0;

	public ClusterNode(ClusterGraph graph) {
		super(graph, (-1 - instanceCounter));
		synchronized (ClusterNode.class) {
			instanceNumber = instanceCounter;
			instanceCounter++;
			primitives = new HashSet<Node>();
		}
	}

	public String getId() {
		return "cluster" + instanceNumber;
	}

	public int getInstanceNumber() {
		return instanceNumber;
	}

	public synchronized boolean addNode(Node node) {
		if (primitives.size() == 0) {
			primitives.add(node);
			return true;
		}
		for (Node n : primitives) {
			if (n.isDirectlyConnectedTo(node)) {
				// add node to set
				primitives.add(node);
				return true;
			}
		}
		return false;
	}

	public Set<Node> getPrimitives() {
		return primitives;
	}

	public boolean containsNode(Node node) {
		return primitives.contains(node);
	}

	public boolean mergeWith(ClusterNode other) {
		if (canMergeWith(other) == true) {
			primitives.addAll(other.getPrimitives());
			return true;
		} else {
			return false;
		}
	}

	public boolean canMergeWith(ClusterNode other) {
		Set<Node> successors = getSuccessors();
		if (successors.size() > 0
				&& successors.containsAll(other.getSuccessors())) {
			return true;
		} else {
			Set<Node> predecessors = getPredecessors();
			if (predecessors.size() > 0
					&& predecessors.containsAll(other.getPredecessors())) {
				return true;
			} else {
				return false;
			}
		}
	}

	public Set<Node> getPredecessors() {
		HashSet<Node> predecessors = new HashSet<Node>();
		for (Node node : primitives) {
			predecessors.addAll(node.getPredecessors());
		}
		predecessors.removeAll(primitives);
		return predecessors;
	}

	public Set<Node> getSuccessors() {
		HashSet<Node> successors = new HashSet<Node>();
		for (Node node : primitives) {
			successors.addAll(node.getSuccessors());
		}
		successors.removeAll(primitives);
		return successors;
	}

	public double getFrequency() {
		double frequency = 0.0;
		for (Node node : primitives) {
			frequency += node.getFrequency();
		}
		frequency /= (double) primitives.size();
		return frequency;
	}

	public boolean equals(Object o) {
		if ((o instanceof ClusterNode) == false) {
			return false;
		} else {
			ClusterNode c = (ClusterNode) o;
			return (this.instanceNumber == c.instanceNumber);
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
		bw
				.write(getId()
						+ " [shape=\"octagon\",style=\"filled\",fillcolor=\"chartreuse3\",label=\"Cluster "
						+ instanceNumber + "\\n" + primitives.size()
						+ " primitives\\n"
						+ ClusterGraph.format(getFrequency()) + "\"];\n");
	}

	public JPanel getClusterGraphPanel() throws Exception {
		File dotFile = File.createTempFile("pmt", ".dot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(dotFile, false));
		writeClusterSubgraphToDot(bw);
		bw.close();
		System.out.println(dotFile.getAbsolutePath());
		Graph graph = Dot.execute(dotFile.getAbsolutePath());
		dotFile.deleteOnExit();
		graph.setEditable(true);
		graph.setMenuable(true);
		graph.setErrorWriter(new PrintWriter(System.err, true));
		GrappaPanel gp = new GrappaPanel(graph);
		gp.addGrappaListener(new GrappaAdapter());
		gp.setScaleToFit(true);
		return gp;
	}

	public void writeClusterSubgraphToDot(Writer bw) throws IOException {
		// get reference to follow matrix (for edge derivation)
		DoubleMatrix2D followMatrix = graph.getFollowMatrix();
		// write DOT prelude
		bw
				.write("digraph CLG"
						+ instanceNumber
						+ " { ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Helvetica\";rankdir=\"TB\"; \n");
		bw
				.write("edge [arrowsize=\"0.5\",decorate=true,fontname=\"Helvetica\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Helvetica\",fontsize=\"8\",shape=\"box\",style=\"filled\",fillcolor=\"palegoldenrod\"];\n");
		// write adjacent predecessor and successor nodes
		Set<Node> predecessors = getPredecessors();
		Set<Node> successors = getSuccessors();
		// unified set, to prevent duplicate nodes (both predecessor and
		// successor)
		HashSet<Node> adjacentNodes = new HashSet<Node>();
		adjacentNodes.addAll(predecessors);
		adjacentNodes.addAll(successors);
		for (Node node : adjacentNodes) {
			node.writeToDot(bw);
		}
		// write cluster nodes
		bw.write("subgraph " + getId() + " {\n");
		bw.write("node [fillcolor=\"lightgoldenrod1\"];\n");
		bw.write("style=\"filled\";\ncolor=\"chartreuse3\";\n,label=\"Cluster "
				+ instanceNumber + "\";\n");
		for (Node node : primitives) {
			node.writeToDot(bw);
		}
		// write edges within clusters
		writeEdgesBetweenSetsToDot(primitives, primitives, followMatrix, bw);
		// close cluster subgraph
		bw.write("}\n");
		// write edges from predecessors to cluster nodes
		writeEdgesBetweenSetsToDot(predecessors, primitives, followMatrix, bw);
		// write edges from cluster nodes to successors
		writeEdgesBetweenSetsToDot(primitives, successors, followMatrix, bw);
		// finish graph
		bw.write("}\n");
	}

	protected void writeEdgesBetweenSetsToDot(Set<Node> sourceSet,
			Set<Node> targetSet, DoubleMatrix2D followMatrix, Writer bw)
			throws IOException {
		for (Node source : sourceSet) {
			for (Node target : targetSet) {
				if (source.equals(target)) {
					continue;
				}
				double frequency = followMatrix.get(source.getIndex(), target
						.getIndex());
				if (frequency > 0.0) {
					frequency = graph.normalize(frequency);
					// write inner edge
					bw.write(source.getId() + " -> " + target.getId()
							+ " [label=\"" + ClusterGraph.format(frequency)
							+ "\"];\n");
				}
			}
		}
	}

	public String getToolTipText() {
		return "<html><table><tr colspan=\"2\"><td>Cluster " + instanceNumber
				+ "</td></tr>" + "<tr><td>Number of primitives:</td><td>"
				+ primitives.size() + "</td></tr>"
				+ "<tr><td>Mean frequency:</td><td>"
				+ ClusterGraph.format(getFrequency()) + "</td></tr>";
	}
}
