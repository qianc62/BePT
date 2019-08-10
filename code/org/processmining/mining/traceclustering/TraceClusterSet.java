/**
 * 
 */
package org.processmining.mining.traceclustering;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;
import org.processmining.mining.traceclustering.TraceStats.SingleTraceStat;

import att.grappa.Graph;
import att.grappa.GrappaPanel;

/**
 * @author christian
 * 
 */
public class TraceClusterSet implements DotFileWriter {

	protected static NumberFormat numberFormat = NumberFormat.getInstance();
	{
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
	}

	protected TraceStats stats;
	protected TraceStatsComparator comparator;
	protected double maxDiameter;
	protected ArrayList<TraceCluster> clusters;

	public TraceClusterSet(TraceStats stats, TraceStatsComparator comparator) {
		this.stats = stats;
		this.comparator = comparator;
		this.maxDiameter = 1.0;
		clusters = new ArrayList<TraceCluster>();
	}

	public int size() {
		return clusters.size();
	}

	public List<TraceCluster> getClusters() {
		return clusters;
	}

	public void cluster(double maxDiameter) {
		this.maxDiameter = maxDiameter;
		clusters.clear();
		List<SingleTraceStat> traces = stats.getTraceStats();
		while (traces.size() > 0) {
			Message.add("Building next generation from " + traces.size()
					+ " traces...", Message.NORMAL);
			TraceCluster nextCluster = deriveNextCluster(traces, maxDiameter);
			clusters.add(nextCluster);
			traces.removeAll(nextCluster.getTraces());
			Message.add(
					"...found winner with size " + nextCluster.size() + ".",
					Message.NORMAL);
		}
	}

	public TraceCluster deriveNextCluster(List<SingleTraceStat> traces,
			double maxDiameter) {
		ArrayList<TraceCluster> nextClusters = new ArrayList<TraceCluster>(
				traces.size());
		// build current generation of clusters
		for (SingleTraceStat trace : traces) {
			nextClusters.add(buildCluster(trace, traces, maxDiameter));
		}
		// determine largest in current generation
		TraceCluster largestCluster = nextClusters.get(0);
		TraceCluster currentCluster;
		for (int i = 1; i < nextClusters.size(); i++) {
			currentCluster = nextClusters.get(i);
			if (currentCluster.size() > largestCluster.size()) {
				largestCluster = currentCluster;
			}
		}
		return largestCluster;
	}

	public TraceCluster buildCluster(SingleTraceStat initialTrace,
			List<SingleTraceStat> traces, double maxDiameter) {
		TraceCluster cluster = new TraceCluster();
		cluster.addTraceStat(initialTrace);
		List<SingleTraceStat> allStats = new ArrayList<SingleTraceStat>(traces);
		Collections.sort(allStats, comparator.getComparator(initialTrace));
		for (int i = 0; i < allStats.size(); i++) {
			SingleTraceStat stat = allStats.get(i);
			if (stat == initialTrace) {
				continue;
			}
			cluster.addTraceStat(stat);
			if (cluster.getMaxDiameter() > maxDiameter) {
				cluster.removeTraceStat(stat); // TODO: is this okay?
				return cluster;
			}
		}
		return cluster;
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
				.write("graph G { ranksep=\".3\"; compound=true; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Helvetica\";rankdir=\"TB\"; \n");
		bw
				.write("edge [decorate=false,fontname=\"Helvetica\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Helvetica\",fontsize=\"8\",shape=\"box\",style=\"filled\",fillcolor=\"palegoldenrod\"];\n");
		int clusterCounter = 0;
		for (TraceCluster cluster : clusters) {
			bw
					.write("cluster"
							+ clusterCounter
							+ " [shape=\"octagon\",style=\"filled\",fillcolor=\"chartreuse3\",label=\"Cluster "
							+ clusterCounter + "\\n" + cluster.size()
							+ " instances\\n� "
							+ numberFormat.format(cluster.getMaxDiameter())
							+ "\"];\n");
			clusterCounter++;
		}
		// write edges
		for (int i = 1; i < clusters.size(); i++) {
			double maxClusterRelation = 0.0;
			int relationIndex = 0;
			for (int k = 0; k < i; k++) {
				double maxRelation = comparator.getRelation(i, k);
				if (maxRelation > maxClusterRelation) {
					maxClusterRelation = maxRelation;
					relationIndex = k;
				}
			}
			bw.write("cluster" + i + " -- cluster" + relationIndex
					+ " [label=\"" + maxClusterRelation + "\"];\n");
		}
		// close graph
		bw.write("}\n");
	}

	public JPanel getGraphPanel() throws Exception {
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
		gp.setBackground(new Color(220, 220, 210));
		gp.setScaleToFit(true);
		return gp;
	}

	/*
	 * *********************************** cluster class *********************
	 */

	public class TraceCluster {

		protected HashSet<SingleTraceStat> traces;

		public TraceCluster() {
			traces = new HashSet<SingleTraceStat>();
		}

		public void addTraceStat(SingleTraceStat stat) {
			traces.add(stat);
		}

		public void removeTraceStat(SingleTraceStat stat) {
			traces.remove(stat);
		}

		public Set<SingleTraceStat> getTraces() {
			return traces;
		}

		public int size() {
			return traces.size();
		}

		public double getMaxDiameter() {
			double maxDiameter = Double.MIN_VALUE;
			double distance;
			for (SingleTraceStat source : traces) {
				for (SingleTraceStat target : traces) {
					distance = comparator.getRelation(source.getTraceIndex(),
							target.getTraceIndex());
					if (distance > maxDiameter) {
						maxDiameter = distance;
					}
				}
			}
			return maxDiameter;
		}

		public double getMinDiameter() {
			double minDiameter = Double.MAX_VALUE;
			double distance;
			for (SingleTraceStat source : traces) {
				for (SingleTraceStat target : traces) {
					distance = comparator.getRelation(source.getTraceIndex(),
							target.getTraceIndex());
					if (distance < minDiameter) {
						minDiameter = distance;
					}
				}
			}
			return minDiameter;
		}
	}

}
