package org.processmining.analysis.logclustering.clusteringalgorithm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.analysis.logclustering.LogCluster;
import org.processmining.analysis.logclustering.LogClusterSet;
import org.processmining.analysis.logclustering.distancemeasure.CorrelationCoefficientDistanceMeasure;
import org.processmining.analysis.logclustering.distancemeasure.DistanceMeasure;
import org.processmining.analysis.logclustering.distancemeasure.EuclidianDistanceMeasure;
import org.processmining.analysis.logclustering.distancemeasure.HammingDistanceMeasure;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;
import org.processmining.framework.util.GUIPropertyListEnumeration;

import att.grappa.Graph;
import att.grappa.GrappaPanel;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Minseok Song
 * 
 */
public class DefaultClustering extends ClusteringAlgorithm {

	protected static NumberFormat numberFormat = NumberFormat.getInstance();
	{
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
	}

	protected double maxDiameter;
	protected DoubleMatrix2D distances;

	// GUI
	protected JScrollPane graphScrollPane;
	protected JSlider slider;
	protected JLabel diameterLabel;
	protected DecimalFormat format = new DecimalFormat("0.0000");
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);

	public DefaultClustering() {
		super("Default Clustering");
		ArrayList<DistanceMeasure> values = new ArrayList<DistanceMeasure>();
		values.add(new CorrelationCoefficientDistanceMeasure());
		values.add(new EuclidianDistanceMeasure());
		values.add(new HammingDistanceMeasure());

		distanceMeasures = new GUIPropertyListEnumeration("Distance Measure",
				values);

	}

	public void build() {
		traceList = new ArrayList<Integer>();

		for (int i = 0; i < agProfiles.getTraceSize(); i++)
			traceList.add(Integer.valueOf(i));
		this.maxDiameter = 1.0;

		distances = agProfiles.calcuateDistance(getDistanceMeasure());
		clusterSet = new LogClusterSet();
	}

	public int size() {
		return clusterSet.size();
	}

	public List<LogCluster> getClusters() {
		return clusterSet.getClusters();
	}

	public void cluster(double maxDiameter) {
		this.maxDiameter = maxDiameter;
		clusterSet.clear();
		for (int i = 0; i < agProfiles.getTraceSize(); i++)
			traceList.add(Integer.valueOf(i));
		while (traceList.size() > 0) {
			Message.add("Building next generation from " + traceList.size()
					+ " traces...", Message.NORMAL);
			LogCluster nextCluster = deriveNextCluster(traceList, maxDiameter);
			clusterSet.addLogCluster(nextCluster);
			traceList.removeAll(nextCluster.getTraces());
			Message.add(
					"...found winner with size " + nextCluster.size() + ".",
					Message.NORMAL);
		}
	}

	public LogCluster deriveNextCluster(List<Integer> traces, double maxDiameter) {
		ArrayList<LogCluster> nextClusters = new ArrayList<LogCluster>(traces
				.size());
		// build current generation of clusters
		for (Integer trace : traces) {
			nextClusters.add(buildCluster(trace, traces, maxDiameter));
		}
		// determine largest in current generation
		LogCluster largestCluster = nextClusters.get(0);
		LogCluster currentCluster;
		for (int i = 1; i < nextClusters.size(); i++) {
			currentCluster = nextClusters.get(i);
			if (currentCluster.size() > largestCluster.size()) {
				largestCluster = currentCluster;
			}
		}
		return largestCluster;
	}

	public LogCluster buildCluster(Integer initialTrace, List<Integer> traces,
			double maxDiameter) {
		LogCluster cluster = new LogCluster();
		cluster.addTraceStat(initialTrace);
		List<Integer> allStats = new ArrayList<Integer>(traces);
		for (int i = 0; i < allStats.size(); i++) {
			Integer stat = allStats.get(i);
			if (stat == initialTrace) {
				continue;
			}
			cluster.addTraceStat(stat);
			if (getMaxDiameter(cluster) > maxDiameter) {
				cluster.removeTraceStat(stat); // TODO: is this okay?
				return cluster;
			}
		}
		return cluster;
	}

	public double getMaxDiameter(LogCluster cluster) {
		double maxDiameter = Double.MIN_VALUE;
		double distance;
		for (int source : cluster.getTraces()) {
			for (int target : cluster.getTraces()) {
				distance = distances.get(source, target);
				if (distance > maxDiameter) {
					maxDiameter = distance;
				}
			}
		}
		return maxDiameter;
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
		for (LogCluster cluster : clusterSet.getClusters()) {
			bw
					.write("cluster"
							+ clusterCounter
							+ " [shape=\"octagon\",style=\"filled\",fillcolor=\"chartreuse3\",label=\"Cluster "
							+ clusterCounter + "\\n" + cluster.size()
							+ " instances\\n\u00F8 "
							+ format.format(getMaxDiameter(cluster)) + "\"];\n");
			clusterCounter++;
		}
		// write edges
		for (int i = 1; i < clusterSet.size(); i++) {
			double maxClusterRelation = 0.0;
			int relationIndex = 0;
			for (int k = 0; k < i; k++) {
				double maxRelation = distances.get(i, k);// agProfiles.getDistance(i,k);//comparator.getRelation(i,
				// k);
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

	public JPanel getPanel() {
		JPanel temp = new JPanel();
		temp.setLayout(new BorderLayout());
		graphScrollPane = new JScrollPane(new JPanel());
		temp.add(graphScrollPane, BorderLayout.CENTER);
		JPanel sliderPanel = new JPanel();
		sliderPanel.setBackground(bgColor);
		sliderPanel.setLayout(new BorderLayout());
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		temp.add(sliderPanel, BorderLayout.EAST);
		JLabel headerLabel = new JLabel("Diameter limit");
		headerLabel.setBackground(bgColor);
		headerLabel.setForeground(fgColor);
		sliderPanel.add(headerLabel, BorderLayout.NORTH);
		slider = new JSlider(JSlider.VERTICAL, 0, 10000, 8000);
		slider.setBackground(bgColor);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				double threshold = (double) slider.getValue() / 10000.0
						* agProfiles.getSumOfNormalizationMaximum();
				diameterLabel.setText("  < " + format.format(threshold) + "  ");
				if (slider.getValueIsAdjusting() == false) {
					Message.add("Re-clustering with threshold " + threshold
							+ "...", Message.NORMAL);
					cluster(threshold);
					try {
						graphScrollPane.getViewport().setView(getGraphPanel());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		sliderPanel.add(slider, BorderLayout.CENTER);
		diameterLabel = new JLabel(format.format(0.8 * agProfiles
				.getSumOfNormalizationMaximum()));
		diameterLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterLabel.setBackground(bgColor);
		diameterLabel.setForeground(fgColor);
		sliderPanel.add(diameterLabel, BorderLayout.SOUTH);
		try {
			cluster(0.8);
			graphScrollPane.getViewport().setView(getGraphPanel());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}

}
