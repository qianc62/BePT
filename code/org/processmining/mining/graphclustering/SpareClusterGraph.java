/**
 * Project: ProM
 * File: SpareClusterGraph.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jul 10, 2006, 2:51:59 PM
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

import javax.swing.JPanel;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.util.Dot;

import att.grappa.Graph;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class SpareClusterGraph {

	protected static int clusterCounter = 0;

	protected static NumberFormat numberFormat = NumberFormat.getInstance();
	{
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
	}

	public static String format(double number) {
		return numberFormat.format(number);
	}

	protected LogEvents logEvents = null;
	protected DoubleMatrix2D followMatrix = null;
	protected double threshold = 0.0;
	protected double normalizationFactor = 0.0;

	protected ArrayList<SpareCluster> clusters = null;
	protected SpareCluster[] clusterMap = null;

	public SpareClusterGraph(LogEvents aLogEvents,
			DoubleMatrix2D aFollowMatrix, double aThreshold) {
		logEvents = aLogEvents;
		followMatrix = aFollowMatrix;
		threshold = aThreshold;
		normalizationFactor = calculateNormalizationFactor();
		clusters = new ArrayList<SpareCluster>();
	}

	protected double calculateNormalizationFactor() {
		// determine maximal value in follower matrix
		double max = 0.0;
		for (int x = 0; x < followMatrix.rows(); x++) {
			for (int y = 0; y < followMatrix.columns(); y++) {
				double current = followMatrix.get(x, y);
				if (current > max) {
					max = current;
				}
			}
		}
		for (int i = 0; i < logEvents.size(); i++) {
			int current = logEvents.get(i).getOccurrenceCount();
			if (current > max) {
				max = current;
			}
		}
		// return normalization factor
		return (1.0 / max);
	}

	protected SpareCluster getClusterForIndex(int index) {
		return clusterMap[index];
	}

	public void setThreshold(double aThreshold) {
		threshold = aThreshold;
	}

	public JPanel getGraphPanel(double threshold) throws Exception {
		setThreshold(threshold);
		File dotFile = File.createTempFile("pmt", ".dot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(dotFile, false));
		writeToDot(bw);
		bw.close();
		System.out.println(dotFile.getAbsolutePath());
		Graph graph = Dot.execute(dotFile.getAbsolutePath());
		graph.setEditable(true);
		graph.setMenuable(true);
		graph.setErrorWriter(new PrintWriter(System.err, true));
		GrappaPanel gp = new GrappaPanel(graph);
		gp.addGrappaListener(new GrappaAdapter());
		gp.setScaleToFit(true);
		return gp;
	}

	public void writeToDot(Writer bw) throws IOException {
		clusterGraph();
		bw
				.write("digraph G { ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Helvetica\";rankdir=\"TB\"; \n");
		bw
				.write("edge [arrowsize=\"0.5\",decorate=true,fontname=\"Helvetica\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Helvetica\",fontsize=\"8\",shape=\"box\",style=\"filled\",fillcolor=\"palegoldenrod\"];\n");
		// write regular nodes
		for (int i = 0; i < clusterMap.length; i++) {
			if (clusterMap[i] == null) {
				// write regular node with given index
				LogEvent e = logEvents.get(i);
				bw.write(getNodeId(i) + " [label=\"" + e.getModelElementName()
						+ "\\n" + e.getEventType() + "\\n"
						+ (e.getOccurrenceCount() * normalizationFactor)
						+ "\"];\n");
			}
		}
		// write cluster nodes
		for (SpareCluster cluster : clusters) {
			bw
					.write("cluster"
							+ cluster.getId()
							+ " [shape=\"octagon\",style=\"filled\",fillcolor=\"chartreuse3\",label=\"Cluster "
							+ cluster.getId() + "\"];\n");
		}
		// write edges
		for (int x = 0; x < followMatrix.columns(); x++) {
			for (int y = 0; y < followMatrix.rows(); y++) {
				if (followMatrix.get(x, y) > 0.0) {
					bw.write(getNodeId(x) + " -> " + getNodeId(y)
							+ " [label=\""
							+ (followMatrix.get(x, y) * normalizationFactor)
							+ "\"];\n");
				}
			}
		}
		bw.write("}\n");
	}

	protected String getNodeId(int index) {
		if (clusterMap[index] == null) {
			return ("node" + index);
		} else {
			return ("cluster" + clusterMap[index].getId());
		}
	}

	protected void clusterGraph() {
		clusters = new ArrayList<SpareCluster>();
		// create and initialize cluster mapping table
		clusterMap = new SpareCluster[logEvents.size()];
		for (int i = 0; i < logEvents.size(); i++) {
			clusterMap[i] = null;
		}
		// create bitmap with 'true' for every log event's index which
		// will have to be clustered
		boolean[] victimMap = new boolean[logEvents.size()];
		for (int i = 0; i < logEvents.size(); i++) {
			if ((logEvents.get(i).getOccurrenceCount() * normalizationFactor) < threshold) {
				victimMap[i] = true;
			} else {
				victimMap[i] = false;
			}
		}
		// create clusters
		while (victimLeft(victimMap) == true) {
			boolean keepOn = true;
			SpareCluster cluster = new SpareCluster(logEvents.size());
			while (keepOn == true) {
				keepOn = false;
				for (int i = 0; i < victimMap.length; i++) {
					if (victimMap[i] == true) {
						if (cluster.addEvent(i) == true) {
							keepOn = true;
							victimMap[i] = false;
						}
					}
				}
			}
			clusters.add(cluster);
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
	 * **************************************************************************
	 * ********* protected class to encode a cluster
	 */

	protected class SpareCluster {

		protected boolean[] bitmap = null;
		protected int clusterNumber = 0;

		public SpareCluster(int numberOfElements) {
			bitmap = new boolean[logEvents.size()];
			synchronized (SpareClusterGraph.class) {
				clusterNumber = SpareClusterGraph.clusterCounter;
				SpareClusterGraph.clusterCounter++;
			}
		}

		public int getId() {
			return clusterNumber;
		}

		public boolean addEvent(LogEvent e) {
			return addEvent(logEvents.indexOf(e));
		}

		public boolean addEvent(int index) {
			boolean clusterEmpty = true;
			for (int i = 0; i < bitmap.length; i++) {
				if (bitmap[i] == true) {
					clusterEmpty = false;
					// check if connected to cluster member node
					if ((followMatrix.get(i, index) > 0.0)
							|| (followMatrix.get(index, i) > 0.0)) {
						// connected; add new index to cluster
						bitmap[index] = true;
						// register index to this cluster
						clusterMap[index] = this;
						return true;
					}
				}
			}
			// add event to empty cluster, if so
			if (clusterEmpty == true) {
				bitmap[index] = true;
				// register index to this cluster
				clusterMap[index] = this;
				return true;
			} else {
				return false;
			}
		}

		public boolean containsIndex(int index) {
			return bitmap[index];
		}

		public boolean containsEvent(LogEvent e) {
			return containsIndex(logEvents.indexOf(e));
		}

		public ArrayList<LogEvent> getEvents() {
			ArrayList<LogEvent> members = new ArrayList<LogEvent>();
			for (int i = 0; i < bitmap.length; i++) {
				if (bitmap[i] == true) {
					members.add(logEvents.get(i));
				}
			}
			return members;
		}
	}

}
