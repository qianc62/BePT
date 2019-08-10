/**
 * Project: ProM
 * File: ClusterGraph.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jul 5, 2006, 2:39:46 PM
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;

import org.processmining.framework.log.LogEvents;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;
import org.processmining.framework.util.TestUtils;

import att.grappa.Graph;
import att.grappa.GrappaPanel;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ClusterGraph implements DotFileWriter {

	protected ArrayList<Node> nodes = null;
	protected ArrayList<Node> primitiveNodes = null;
	protected ArrayList<ClusterNode> clusters = null;
	protected ArrayList<Node> nodeMap = null;
	protected Edges edges = null;
	protected LogEvents events = null;
	protected DoubleMatrix2D followMatrix = null;
	protected double normalizationFactor = 0.0;
	protected double minimalFrequency = 0.0;
	protected boolean attenuateEdges = true;
	protected boolean mergeClusters = true;

	static NumberFormat numberFormat = NumberFormat.getInstance();
	{
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
	}

	public static String format(double number) {
		return numberFormat.format(number);
	}

	public ClusterGraph(LogEvents events, DoubleMatrix2D followMatrix) {
		this.events = events;
		this.followMatrix = followMatrix;
		nodes = new ArrayList<Node>();
		edges = new Edges();
		for (int i = 0; i < events.size(); i++) {
			nodes.add(new Node(this, i));
		}
		calculateNormalizationFactor();
		attenuateEdges = true;
	}

	public Node resolveNode(String name) {
		if (name.startsWith("cluster")) {
			return resolveCluster(name);
		} else if (name.startsWith("node")) {
			for (Node node : nodes) {
				if (node.getId().equals(name)) {
					return node;
				}
			}
			return null;
		} else {
			return null;
		}
	}

	public ClusterNode resolveCluster(String name) {
		for (ClusterNode cluster : clusters) {
			if (cluster.getId().equals(name)) {
				return cluster;
			}
		}
		return null;
	}

	public Node getNode(int index) {
		return nodes.get(index);
	}

	public void setAttenuateEdges(boolean attenuation) {
		attenuateEdges = attenuation;
	}

	public void setMergeClusters(boolean merge) {
		mergeClusters = merge;
	}

	public double getThresholdShowing(int numberOfPrimitives) {
		if (numberOfPrimitives >= events.size()) {
			return 0.0;
		}
		int[] frequencies = new int[events.size()];
		for (int i = 0; i < frequencies.length; i++) {
			frequencies[i] = events.get(i).getOccurrenceCount();
		}
		Arrays.sort(frequencies);
		double threshold = frequencies[frequencies.length - numberOfPrimitives];
		return normalize(threshold);
	}

	protected double calculateNormalizationFactor() {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double cur = 0.0;
		// look for maximum in follower matrix
		/*
		 * skip this, we focus on nodes! TODO: remove later!
		 */
		for (int x = 0; x < followMatrix.columns(); x++) {
			for (int y = 0; y < followMatrix.rows(); y++) {
				cur = followMatrix.get(x, y);
				if (cur > max) {
					max = cur;
				}
			}
		}
		// look for maximum in occurrence counts of events
		for (int i = 0; i < events.size(); i++) {
			cur = events.get(i).getOccurrenceCount();
			if (cur > max) {
				max = cur;
			}
			if (cur < min) {
				min = cur;
			}
		}
		normalizationFactor = 1.0 / max;
		minimalFrequency = min * normalizationFactor;
		return normalizationFactor;
	}

	public double normalize(double frequency) {
		return (frequency * normalizationFactor);
	}

	public double getMinimalNodeFrequency() {
		return minimalFrequency;
	}

	public LogEvents getLogEvents() {
		return events;
	}

	public DoubleMatrix2D getFollowMatrix() {
		return followMatrix;
	}

	public void cluster(double threshold) {
		// reset cluster counter
		ClusterNode.resetInstanceCounter();
		// copy nodes to mapping list and
		// determine clustering victims (create bitmap)
		primitiveNodes = new ArrayList<Node>();
		nodeMap = new ArrayList<Node>(nodes.size());
		boolean[] victimMap = new boolean[nodes.size()];
		Node node = null;
		for (int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);
			nodeMap.add(node);
			if (node.getFrequency() < threshold) {
				victimMap[i] = true;
			} else {
				victimMap[i] = false;
				// remember primitive node (not clustered)
				primitiveNodes.add(node);
			}
		}
		// create clusters
		clusters = new ArrayList<ClusterNode>();
		while (victimLeft(victimMap) == true) {
			boolean keepOn = true;
			ClusterNode cluster = new ClusterNode(this);
			while (keepOn == true) {
				keepOn = false;
				for (int i = 0; i < victimMap.length; i++) {
					if (victimMap[i] == true) {
						if (cluster.addNode(nodes.get(i)) == true) {
							keepOn = true;
							victimMap[i] = false;
							nodeMap.set(i, cluster);
						}
					}
				}
			}
			// new cluster completed; merge or add (setting-dependent)
			if (mergeClusters == true) {
				mergeWithExistingClusters(cluster);
			} else {
				clusters.add(cluster);
			}
		}
	}

	protected void mergeWithExistingClusters(ClusterNode cluster) {
		// try to merge with existing clusters; use first that works.
		for (ClusterNode existing : clusters) {
			if (existing.mergeWith(cluster) == true) {
				// update node mapping to merge victor
				for (Node node : cluster.getPrimitives()) {
					nodeMap.set(node.getIndex(), existing);
				}
				return; // done!
			}
		}
		// merging not possible; add as new cluster
		clusters.add(cluster);
	}

	public void createEdges() {
		edges = new Edges();
		double frequency = 0.0;
		for (int x = 0; x < followMatrix.columns(); x++) {
			for (int y = 0; y < followMatrix.rows(); y++) {
				frequency = normalize(followMatrix.get(x, y));
				if (frequency > 0.0) {
					edges.addEdge(nodeMap.get(x), nodeMap.get(y), frequency);
				}
			}
		}
	}

	protected boolean victimLeft(boolean[] victimMap) {
		for (int i = 0; i < victimMap.length; i++) {
			if (victimMap[i] == true) {
				return true;
			}
		}
		return false;
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
				.write("digraph G { ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Helvetica\";rankdir=\"TB\"; \n");
		bw
				.write("edge [arrowsize=\"0.5\",decorate=false,fontname=\"Helvetica\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Helvetica\",fontsize=\"8\",shape=\"box\",style=\"filled\",fillcolor=\"palegoldenrod\"];\n");
		// write primitive nodes
		for (Node node : primitiveNodes) {
			node.writeToDot(bw);
		}
		// write cluster nodes
		for (ClusterNode cluster : clusters) {
			cluster.writeToDot(bw);
		}
		// write edges
		edges.writeToDot(bw);
		bw.write("}\n");
	}

	public JPanel getGraphPanel(double threshold) throws Exception {
		this.cluster(threshold);
		this.createEdges();
		if (attenuateEdges == true) {
			edges.setThreshold(threshold);
		}
		File dotFile = File.createTempFile("pmt", ".dot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(dotFile, false));
		writeToDot(bw);
		bw.close();
		System.out.println(dotFile.getAbsolutePath());
		Graph graph = Dot.execute(dotFile.getAbsolutePath());
		dotFile.deleteOnExit();
		graph.setEditable(true);
		graph.setMenuable(true);
		graph.setErrorWriter(new PrintWriter(System.err, true));
		GrappaPanel gp = new GrappaPanel(graph);
		gp.addGrappaListener(new ClusterGraphAdapter(this));
		gp.setScaleToFit(true);
		return gp;
	}

	public void testOutput() {
		Message.add("<ClusterGraph>", Message.TEST);
		Message.add("\t<PrimitiveNodes size=\"" + primitiveNodes.size()
				+ "\"/>", Message.TEST);
		Message.add("\t<ClusterNodes size=\"" + clusters.size() + "\"/>",
				Message.TEST);
		Message.add("\t<Edges size=\"" + edges.edges.size() + "\"/>",
				Message.TEST);
		Message.add("\t<FollowerMatrix hash=\""
				+ TestUtils.hash(this.followMatrix) + "\"/>", Message.TEST);
		Message.add("</ClusterGraph>", Message.TEST);
	}

}
