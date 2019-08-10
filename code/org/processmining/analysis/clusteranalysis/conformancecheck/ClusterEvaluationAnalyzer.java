/**
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Copyright (c) 2003-2006 TU/e Eindhoven
 *    by Eindhoven University of Technology
 *    Department of Information Systems
 *    http://is.tm.tue.nl
 *
 ************************************************************************/
package org.processmining.analysis.clusteranalysis.conformancecheck;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.deckfour.slickerbox.components.SlickTabbedPane;
import org.processmining.analysis.traceclustering.distance.CorrelationCoefficientDistance;
import org.processmining.analysis.traceclustering.distance.DistanceMatrix;
import org.processmining.analysis.traceclustering.distance.DistanceMetric;
import org.processmining.analysis.traceclustering.distance.EuclideanDistance;
import org.processmining.analysis.traceclustering.distance.HammingDistance;
import org.processmining.analysis.traceclustering.distance.JaccardIndexDistance;
import org.processmining.analysis.traceclustering.distance.sequence.GZipCompressionDistance;
import org.processmining.analysis.traceclustering.distance.sequence.LcsDistance;
import org.processmining.analysis.traceclustering.distance.sequence.LevenshteinEditDistance;
import org.processmining.analysis.traceclustering.distance.sequence.StringDistanceMetric;
import org.processmining.analysis.traceclustering.model.AHCTreeNode;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.ProgressDummy;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.StopWatch;
import org.processmining.analysis.benchmark.metric.TokenFitnessMetric;
import org.processmining.mining.petrinetmining.AlphaProcessMiner; //import org.processmining.mining.petrinetmining.AlphaPPProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.mining.regionmining.RegionMiner;
import org.processmining.mining.geneticmining.fitness.Fitness;
import org.processmining.mining.geneticmining.fitness.FitnessFactory;
import org.processmining.mining.heuristicsmining.HeuristicsMiner;
import org.processmining.mining.heuristicsmining.HeuristicsNetResult;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.SmoothPanel;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class ClusterEvaluationAnalyzer extends JPanel {

	// HTML styles
	private static final String H1 = "h1";
	private static final String TR = "tr";
	private static final String TH = "th";
	private static final String TD = "td";
	private static final String BR = "br";

	private static final String PETRI = "Alpha Miner";
	private static final String REGION = "Regin Miner";
	// private static final String ALPHA = "Alpha++ Miner";
	private static final String HEUMINER = "Heuristics Miner";

	// for gui
	protected JPanel resultPanel = null;
	protected SlickTabbedPane tabbedPane = null;
	protected JPanel treeViewPanel = null;
	protected JPanel algorithmViewPanel = null;
	protected JPanel evaluationViewPanel = null;
	protected JPanel configurationPanel;
	protected List<String> minerList;
	protected List<DistanceMetric> distanceMetrics;
	protected GUIPropertyListEnumeration distanceMetricsBox;
	protected GUIPropertyListEnumeration miningBox;
	protected DistanceMetric metric;

	public static Color colorTextAreaBg = new Color(160, 160, 160);
	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	protected ProgressPanel progressPanel = new ProgressPanel(
			"Evaluate clustering results");
	private ArrayList result = new ArrayList();

	/**
	 * The data mining classifier to be used for analysis.
	 */
	protected ClusterSet clusters;
	protected AggregateProfile agProfiles;
	protected AHCTreeNode rootACH;

	public JPanel analyse(ClusterSet aClusters) {
		this.clusters = aClusters;
		agProfiles = clusters.getAGProfiles();
		// startAnalysistemp();
		initGUI();
		return this;
	}

	protected void initGUI() {

		JPanel confLowerPanel = new JPanel();
		confLowerPanel.setBackground(colorBg);
		confLowerPanel.setLayout(new BorderLayout());
		confLowerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		configurationPanel = new JPanel();
		configurationPanel.setBackground(colorTextAreaBg);
		configurationPanel.setForeground(colorFg);
		configurationPanel.setLayout(new BoxLayout(configurationPanel,
				BoxLayout.Y_AXIS));
		configurationPanel.setBorder(BorderFactory.createEmptyBorder());
		distanceMetrics = new ArrayList<DistanceMetric>();
		distanceMetrics.add(new EuclideanDistance());
		distanceMetrics.add(new JaccardIndexDistance());
		distanceMetrics.add(new HammingDistance());
		distanceMetrics.add(new CorrelationCoefficientDistance());
		distanceMetrics.add(new GZipCompressionDistance());
		distanceMetrics.add(new LcsDistance());
		distanceMetrics.add(new LevenshteinEditDistance());
		distanceMetricsBox = new GUIPropertyListEnumeration("Chart", "",
				distanceMetrics, null, 200);
		configurationPanel.add(distanceMetricsBox.getPropertyPanel());

		minerList = new ArrayList<String>();
		minerList.add(PETRI);
		minerList.add(REGION);
		// minerList.add(ALPHA);
		minerList.add(HEUMINER);

		miningBox = new GUIPropertyListEnumeration("Mining ", "", minerList,
				null, 200);
		configurationPanel.add(miningBox.getPropertyPanel());
		// add scroll pane to the left of the configuration panel
		JPanel configSuperPanel = new JPanel();
		configSuperPanel.setBorder(BorderFactory
				.createEmptyBorder(5, 5, 10, 10));
		configSuperPanel.setBackground(colorBg);
		configSuperPanel.setMinimumSize(new Dimension(400, 2000));
		configSuperPanel.setMaximumSize(new Dimension(450, 2000));
		configSuperPanel.setPreferredSize(new Dimension(440, 2000));
		configSuperPanel.setLayout(new BorderLayout());
		JLabel configSuperLabel = new JLabel("Configuration");
		configSuperLabel.setBorder(BorderFactory
				.createEmptyBorder(0, 0, 10, 10));
		configSuperLabel.setForeground(colorFg);
		configSuperLabel.setOpaque(false);
		configSuperLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		configSuperLabel.setFont(configSuperLabel.getFont().deriveFont(16.0f));
		configSuperPanel.add(configSuperLabel, BorderLayout.NORTH);
		configSuperPanel.add(configurationPanel, BorderLayout.CENTER);
		confLowerPanel.add(configSuperPanel, BorderLayout.CENTER);

		SmoothPanel rightPanel = new SmoothPanel();
		rightPanel.setBackground(new Color(140, 140, 140));
		rightPanel.setHighlight(new Color(160, 160, 160));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		rightPanel.add(Box.createHorizontalStrut(5));
		// add right side to configuration panel at center position
		JButton startButton = new AutoFocusButton("start calculation");
		startButton.setOpaque(false);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startAnalysis();
			}
		});
		rightPanel.add(startButton);
		confLowerPanel.add(rightPanel, BorderLayout.EAST);

		// add header
		configurationPanel = new JPanel();
		configurationPanel.setBorder(BorderFactory.createEmptyBorder());
		configurationPanel.setLayout(new BorderLayout());
		configurationPanel.add(confLowerPanel, BorderLayout.CENTER);

		HeaderBar header = new HeaderBar("Clustering Result Analysis");
		header.setHeight(40);
		configurationPanel.add(header, BorderLayout.NORTH);
		// set configuration panel as displayed
		configurationPanel.revalidate();

		this.removeAll();
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.add(configurationPanel, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	protected void startAnalysisTemp() {
		// fitness
		JTextPane myTextPane = new JTextPane();
		myTextPane.setContentType("text/html");
		myTextPane.setText(getHtmlRepresentationFitness());
		myTextPane.setEditable(false);
		myTextPane.setCaretPosition(0);
		myTextPane.setBackground(colorTextAreaBg);
		JScrollPane scrollPane = new JScrollPane(myTextPane);
		this.removeAll();
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
	}

	protected JPanel drawProcesses() {
		SlickTabbedPane tabbedPane = new SlickTabbedPane();
		// draw model
		int i = 0;
		for (Cluster cluster : clusters.getClusters()) {
			try {
				JScrollPane scrollPane;
				if (miningBox.getValue().equals(HEUMINER)) {
					scrollPane = new JScrollPane(((HeuristicsNetResult) result
							.get(i)).getVisualization());
				} else {
					scrollPane = new JScrollPane(((PetriNetResult) result
							.get(i)).getVisualization());
				}
				tabbedPane.addTab(cluster.getName(), scrollPane);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			i++;
		}
		return tabbedPane;
	}

	protected void startAnalysis() {

		metric = (DistanceMetric) distanceMetricsBox.getValue();
		progressPanel.setNote("Evaluate clustering results ...");
		progressPanel.setMinMax(0, clusters.size() * 3);
		this.removeAll();
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.add(progressPanel.getPanel(), BorderLayout.CENTER);
		Thread buildProfileThread = new Thread() {
			public void run() {
				StopWatch watch = new StopWatch();
				watch.start();
				resultPanel = new JPanel();
				resultPanel.setBorder(BorderFactory.createEmptyBorder());
				resultPanel.setLayout(new BorderLayout());
				tabbedPane = new SlickTabbedPane();
				progressPanel.setProgress(0);

				deriveModel();

				// fitness
				JTextPane myTextPane = new JTextPane();
				myTextPane.setContentType("text/html");
				myTextPane.setText(getHtmlRepresentationFitness());
				myTextPane.setEditable(false);
				myTextPane.setCaretPosition(0);
				myTextPane.setBackground(colorTextAreaBg);
				JScrollPane scrollPane = new JScrollPane(myTextPane);
				tabbedPane.addTab("fitness values", scrollPane);

				// draw model
				tabbedPane.addTab("process models", drawProcesses());

				// distance between clusters
				progressPanel.setProgress(2);
				JTextPane myTextPane2 = new JTextPane();
				myTextPane2.setContentType("text/html");
				myTextPane2.setText(getHtmlRepresentationDistance());
				myTextPane2.setEditable(false);
				myTextPane2.setCaretPosition(0);
				myTextPane2.setBackground(colorTextAreaBg);
				JScrollPane scrollPane2 = new JScrollPane(myTextPane2);
				tabbedPane.addTab("distance values", scrollPane2);

				progressPanel.setProgress(4);
				HeaderBar header = new HeaderBar("Clustering Result Analysis");
				header.setHeight(40);
				resultPanel.add(header, BorderLayout.NORTH);
				resultPanel.add(tabbedPane, BorderLayout.CENTER);

				watch.stop();
				Message.add("Calcuate conformance values"
						+ watch.formatDuration());
				updateGUI();
			}
		};
		buildProfileThread.start();
	}

	public void updateGUI() {
		this.removeAll();
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.add(resultPanel, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	public void deriveModel() {
		for (Cluster cluster : clusters.getClusters()) {
			try {
				if (miningBox.getValue().equals(PETRI)) {
					AlphaProcessMiner miningPlugin = new AlphaProcessMiner();
					result.add((PetriNetResult) miningPlugin.mine(cluster
							.getFilteredLog()));
				} else if (miningBox.getValue().equals(REGION)) {
					RegionMiner miningPlugin = new RegionMiner();
					miningPlugin.getOptionsPanel(cluster.getFilteredLog()
							.getLogSummary());
					result.add((PetriNetResult) miningPlugin.mine(cluster
							.getFilteredLog()));
					// } else if (miningBox.getValue().equals(ALPHA)) {
					// AlphaPPProcessMiner miningPlugin = new
					// AlphaPPProcessMiner();
					// result.add((PetriNetResult)
					// miningPlugin.mine(cluster.getFilteredLog()));
				} else {
					HeuristicsMiner miningPlugin = new HeuristicsMiner();
					result.add((HeuristicsNetResult) miningPlugin.mine(cluster
							.getFilteredLog()));
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	/**
	 * Delivers a HTML representation of the statistic results. The reason for
	 * displaying HTML is that it can easily be copied and pasted from the
	 * framework to, e.g., a word processing program.
	 * 
	 * @param performance
	 *            model
	 * @return the String containing a HTML representation of the results
	 */
	private String getHtmlRepresentationFitness() {

		// distance statistics
		StringBuffer sb = new StringBuffer("<html><body>");
		sb.append(tag("Table view ", H1));
		sb.append("<table border=\"1\">");

		// write duration statistics table header
		StringBuffer tableHeader = new StringBuffer();
		tableHeader.append(tag("Cluster", TH));
		tableHeader.append(tag("size", TH));
		tableHeader.append(tag("fitness", TH));
		sb.append(tag(tableHeader.toString(), TR));

		int i = 0;
		double sum = 0.0;
		for (Cluster cluster : clusters.getClusters()) {
			StringBuffer tempBuffer = new StringBuffer();
			tempBuffer.append(tag(cluster.getName(), TD));
			tempBuffer.append(tag(String.valueOf(cluster.size()), TD));
			try {
				double fitness = 0.0;
				if (miningBox.getValue().equals(HEUMINER)) {
					HeuristicsNet net = ((HeuristicsNetResult) result.get(i))
							.getHeuriticsNet();
					Fitness hnfitness = FitnessFactory.getFitness(0, cluster
							.getFilteredLog(), null);
					fitness = hnfitness.calculate(new HeuristicsNet[] { net })[0]
							.getFitness();
				} else {
					PetriNet petriNet = ((PetriNetResult) result.get(i))
							.getPetriNet();
					TokenFitnessMetric tm = new TokenFitnessMetric();
					fitness = tm.measure(petriNet, cluster.getFilteredLog(),
							null, new ProgressDummy());
				}
				tempBuffer.append(tag(String.valueOf(fitness), TD));
				sum += fitness * cluster.size();
				i++;
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			sb.append(tag(tempBuffer.toString(), TR));
			progressPanel.setProgress(progressPanel.getValue() + 1);
		}
		sb.append("</table>");
		sb.append("average = "
				+ String.valueOf(sum / clusters.getLog().numberOfInstances()));
		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Delivers a HTML representation of the statistic results. The reason for
	 * displaying HTML is that it can easily be copied and pasted from the
	 * framework to, e.g., a word processing program.
	 * 
	 * @param performance
	 *            model
	 * @return the String containing a HTML representation of the results
	 */
	private String getHtmlRepresentationDistance() {
		// distance statistics
		StringBuffer sb = new StringBuffer("<html><body>");
		sb.append(tag("Table view ", H1));
		sb.append("<table border=\"1\">");

		// write duration statistics table header
		StringBuffer tableHeader = new StringBuffer();
		tableHeader.append(tag("Cluster", TH));
		tableHeader.append(tag("size", TH));
		tableHeader.append(tag("intra distance (avg)", TH));
		sb.append(tag(tableHeader.toString(), TR));
		double[] distnace = calculateIntraClusterDistance();
		double sum = 0.0;
		int i = 0;
		for (Cluster cluster : clusters.getClusters()) {
			StringBuffer tempBuffer = new StringBuffer();
			tempBuffer.append(tag(tag(cluster.getName(), BR), TD));
			tempBuffer.append(tag(String.valueOf(cluster.size()), TD));
			tempBuffer.append(tag(String.valueOf(distnace[i++]), TD));
			sb.append(tag(tempBuffer.toString(), TR));
			if (!String.valueOf(distnace[i - 1]).equals("NaN"))
				sum += distnace[i - 1];
		}
		sb.append("</table>");
		sb.append("average = " + String.valueOf(sum / distnace.length));
		sb.append("<br>");

		sb.append(tag("Distance between centroids", H1));
		sb.append("<table border=\"1\">");

		// write duration statistics table header
		tableHeader = new StringBuffer();

		tableHeader.append(tag("", TH));
		for (Cluster cluster : clusters.getClusters()) {
			tableHeader.append(tag(cluster.getName(), TH));
		}

		sb.append(tag(tableHeader.toString(), TR));
		DoubleMatrix2D matrix = calculateInterClusterDistanceAgregated();
		i = 0;
		sum = 0.0;
		for (Cluster cluster : clusters.getClusters()) {
			StringBuffer tempBuffer = new StringBuffer();
			tempBuffer.append(tag(tag(cluster.getName(), BR), TD));
			for (int j = 0; j < clusters.size(); j++) {
				tempBuffer.append(tag(String.valueOf(matrix.get(i, j)), TD));
				sum += matrix.get(i, j);
			}
			sb.append(tag(tempBuffer.toString(), TR));
			i++;
		}
		sb.append("</table>");
		sb.append("average = "
				+ String.valueOf(sum
						/ (clusters.size() * (clusters.size() - 1))));
		sb.append("<br>");
		// sb.append(getHtmlRepresentationDistanceInstance());
		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Delivers a HTML representation of the statistic results. The reason for
	 * displaying HTML is that it can easily be copied and pasted from the
	 * framework to, e.g., a word processing program.
	 * 
	 * @param performance
	 *            model
	 * @return the String containing a HTML representation of the results
	 */
	private String getHtmlRepresentationDistanceInstance() {
		// distance statistics
		StringBuffer sb = new StringBuffer();

		// write duration statistics table header
		StringBuffer tableHeader = new StringBuffer();

		DistanceMatrix dm;
		if (metric instanceof StringDistanceMetric) {
			dm = ((StringDistanceMetric) metric).getDistanceMatrix(clusters
					.getLog());
			dm.normalizeToMaximum(1.0);
		} else {
			dm = agProfiles.getDistanceMatrix(metric, new ProgressDummy());
		}

		sb.append(tag("Distance between instances", H1));
		sb.append("<table border=\"1\">");
		// write duration statistics table header
		tableHeader = new StringBuffer();

		tableHeader.append(tag("", TH));
		for (int k = 0; k < clusters.getLog().numberOfInstances(); k++) {
			tableHeader.append(tag(clusters.getLog().getInstance(k).getName(),
					TH));
		}

		sb.append(tag(tableHeader.toString(), TR));
		int i = 0;
		double sum = 0.0;
		for (int k = 0; k < clusters.getLog().numberOfInstances(); k++) {
			StringBuffer tempBuffer = new StringBuffer();
			tempBuffer.append(tag(tag(clusters.getLog().getInstance(k)
					.getName(), BR), TD));
			for (int l = 0; l < clusters.getLog().numberOfInstances(); l++) {
				tempBuffer.append(tag(String.valueOf(dm.get(k, l)), TD));
				sum += dm.get(k, l);
				if (k != l)
					i++;
			}
			sb.append(tag(tempBuffer.toString(), TR));
		}
		sb.append("</table>");
		sb.append("average = " + String.valueOf(sum / i));
		sb.append("<br>");
		return sb.toString();
	}

	/**
	 * calculate inter-cluster distance
	 * 
	 * @return
	 */
	protected DoubleMatrix2D calculateInterClusterDistanceCentroid_() {
		InstancePoint[] instancePoints;
		instancePoints = new InstancePoint[clusters.size()];
		DistanceMatrix dm = getDistanceMatrix();
		for (int i = 0; i < clusters.size(); i++) {
			instancePoints[i] = new InstancePoint();
			Cluster tempCluster = (Cluster) clusters.getClusters().get(i);
			if (tempCluster.size() == 0)
				continue;
			instancePoints[i] = new InstancePoint();
			Iterator itr = tempCluster.getTraceIndices().iterator();
			while (itr.hasNext()) {
				int index = (Integer) itr.next();
				InstancePoint iPoint = agProfiles.getPoint(index);
				Iterator itr2 = iPoint.getItemKeys().iterator();
				while (itr2.hasNext()) {
					String key = (String) itr2.next();
					if (instancePoints[i].getItemKeys().contains(key))
						instancePoints[i].set(key, instancePoints[i].get(key)
								+ iPoint.get(key));
					else
						instancePoints[i].set(key, iPoint.get(key));
				}
			}
			itr = instancePoints[i].getItemKeys().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				instancePoints[i].set(key, instancePoints[i].get(key)
						/ tempCluster.size());
			}
			progressPanel.setProgress(progressPanel.getValue() + 1);
		}

		DoubleMatrix2D D = DoubleFactory2D.dense.make(instancePoints.length,
				instancePoints.length, 0);
		for (int i = 0; i < instancePoints.length - 1; i++) {
			for (int j = i + 1; j < instancePoints.length; j++) {
				double temp = metric.getDistance(instancePoints[i],
						instancePoints[j]);
				D.set(i, j, temp);
				D.set(j, i, temp);
			}
		}
		return D;
	}

	/**
	 * calculate inter-cluster distance
	 * 
	 * @return
	 */
	protected DoubleMatrix2D calculateInterClusterDistanceAgregated() {
		DistanceMatrix dm = getDistanceMatrix();
		DoubleMatrix2D D = DoubleFactory2D.dense.make(clusters.size(), clusters
				.size(), 0);

		for (int i = 0; i < clusters.size() - 1; i++) {
			Cluster tempCluster = (Cluster) clusters.getClusters().get(i);
			for (int j = i + 1; j < clusters.size(); j++) {
				Cluster tempCluster2 = (Cluster) clusters.getClusters().get(j);
				double temp = 0.0;
				double divider = 0.0;
				Iterator<Integer> itr = tempCluster.getTraceIndices()
						.iterator();
				while (itr.hasNext()) {
					int inst1 = itr.next();
					Iterator<Integer> itr2 = tempCluster2.getTraceIndices()
							.iterator();
					while (itr2.hasNext()) {
						int inst2 = itr2.next();
						temp += dm.get(inst1, inst2);
						divider++;
					}
				}
				divider = Math.max(divider, 1.0);
				temp = temp / divider;
				D.set(i, j, temp);
				D.set(j, i, temp);
			}
			progressPanel.setProgress(progressPanel.getValue() + 1);
		}

		return D;
	}

	/**
	 * calculate intra-cluster distance
	 * 
	 * @return
	 */
	protected double[] calculateIntraClusterDistance() {
		double[] centers = new double[clusters.size()];
		DistanceMatrix dm = getDistanceMatrix();

		for (int i = 0; i < clusters.size(); i++) {
			Cluster tempCluster = (Cluster) clusters.getClusters().get(i);
			if (tempCluster.size() == 0)
				continue;
			double temp = 0.0;
			double divider = 0;
			for (int j = 0; j < tempCluster.getTraceIndices().size() - 1; j++) {
				for (int k = j + 1; k < tempCluster.getTraceIndices().size(); k++) {
					temp += dm.get(tempCluster.getTraceIndices().get(j),
							tempCluster.getTraceIndices().get(k));
					divider++;
				}
			}
			divider = Math.max(divider, 1.0);

			centers[i] = temp / divider;
			progressPanel.setProgress(progressPanel.getValue() + 1);
		}
		return centers;
	}

	// /////////////////////// PRIVATE HELPER METHODS
	// //////////////////////////////
	// helper method from the extended log summary - functionality will be moved
	// to the log summary at a later point in time
	private String tag(String s, String tag) {
		return "<" + tag + ">" + s + "</" + tag + ">";
	}

	public DistanceMatrix getDistanceMatrix() {
		agProfiles.resetDistanceMatrix();
		if (metric instanceof StringDistanceMetric) {
			DistanceMatrix dm = ((StringDistanceMetric) metric)
					.getDistanceMatrix(clusters.getLog());
			dm.normalizeToMaximum(1.0);
			return dm;
		}
		return agProfiles.getDistanceMatrix(metric, new ProgressDummy());
	}
}
