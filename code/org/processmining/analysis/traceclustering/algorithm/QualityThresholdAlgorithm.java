/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 *
 * LICENSE:
 *
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * EXEMPTION:
 *
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 *
 */
package org.processmining.analysis.traceclustering.algorithm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.traceclustering.distance.DistanceMatrix;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.analysis.traceclustering.ui.DisjointClusterView;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.StopWatch;

/**
 * @author christian
 * 
 */
public class QualityThresholdAlgorithm extends ClusteringAlgorithm {

	protected static Color colorBg = new Color(120, 120, 120);
	protected static Color colorFg = new Color(30, 30, 30);

	protected static DecimalFormat format = new DecimalFormat("0.000");

	protected JPanel rootPanel;
	protected JLabel thresholdLabel;
	protected JSlider thresholdSlider;
	// protected JScrollPane view;
	protected JComponent view;
	protected ProgressPanel progress;
	protected double diameter;

	protected ClusterSet clusters;

	/**
	 * @param name
	 * @param description
	 */
	public QualityThresholdAlgorithm() {
		super(
				"Quality threshold clustering",
				"Quality threshold clustering allows the user to specify"
						+ " a maximum diameter of clusters. The algorithm will return"
						+ " a repeatable, optimized set of clusters which meet the"
						+ " given requirement.");
		clusters = null;
	}

	protected void setupGui() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createEmptyBorder());
		rootPanel.setBackground(colorBg);
		JPanel controlPanel = new SmoothPanel();
		controlPanel.setMinimumSize(new Dimension(160, 200));
		controlPanel.setMaximumSize(new Dimension(160, 2000));
		controlPanel.setPreferredSize(new Dimension(160, 500));
		controlPanel.setBackground(colorBg);
		controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		controlPanel.setLayout(new BorderLayout());
		JLabel header = new JLabel("Max. diameter");
		header.setOpaque(false);
		header.setForeground(colorFg);
		header.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		thresholdLabel = new JLabel(format.format(0.5));
		thresholdLabel.setOpaque(false);
		thresholdLabel.setForeground(colorFg);
		thresholdLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		thresholdSlider = new JSlider();
		thresholdSlider.setAlignmentX(JSlider.CENTER_ALIGNMENT);
		thresholdSlider.setMinimum(0);
		thresholdSlider.setMaximum(1000);
		thresholdSlider.setValue(500);
		thresholdSlider.setOrientation(JSlider.VERTICAL);
		thresholdSlider.setOpaque(false);
		thresholdSlider.setEnabled(false);
		thresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double value = (double) thresholdSlider.getValue() / 1000.0;
				thresholdLabel.setText(format.format(value));
				if (thresholdSlider.getValueIsAdjusting() == false) {
					// slider released - cluster with updated values
					cluster(value);
				}
			}
		});
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
		lowerPanel.setOpaque(false);
		lowerPanel.add(thresholdLabel);
		controlPanel.add(header, BorderLayout.NORTH);
		controlPanel.add(thresholdSlider, BorderLayout.CENTER);
		controlPanel.add(lowerPanel, BorderLayout.SOUTH);
		rootPanel.add(controlPanel, BorderLayout.EAST);
		// view = new JScrollPane();
		// view.setBorder(BorderFactory.createEmptyBorder());
		// view.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// view.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// rootPanel.add(view, BorderLayout.CENTER);
		progress = new ProgressPanel("Calculating distance matrix");
		progress.setNote("Calculating trace distance matrix...");
		view = progress.getPanel();
		rootPanel.add(view, BorderLayout.CENTER);
		try {
			rootPanel.revalidate();
			rootPanel.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// view.getViewport().setView(progress.getPanel());
		Thread setupThread = new Thread() {
			public void run() {
				// pre-calculate distance matrix
				StopWatch watch = new StopWatch();
				watch.start();
				input.getDistanceMatrix(progress);
				watch.stop();
				Message.add("Calculated distance matrix in "
						+ watch.formatDuration());
				watch.start();
				// start clustering with 0.5
				cluster(0.5);
				watch.stop();
				Message
						.add("Clustering performed in "
								+ watch.formatDuration());
			}
		};
		setupThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.traceclustering.algorithm.ClusteringAlgorithm
	 * #getClusters()
	 */
	@Override
	public ClusterSet getClusters() {
		return clusters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.traceclustering.algorithm.ClusteringAlgorithm
	 * #getUI()
	 */
	@Override
	public JComponent getUI() {
		setupGui();
		return rootPanel;
	}

	public void cluster(double maxDiameter) {
		// prepare progress display
		diameter = maxDiameter;
		progress = new ProgressPanel("Clustering");
		progress.setNote("clustering with target diameter of " + diameter);
		progress.setMinMax(0, input.getLog().numberOfInstances());
		progress.setProgress(0);
		// view.getViewport().setView(progress.getPanel());
		rootPanel.remove(view);
		view = progress.getPanel();
		rootPanel.add(view, BorderLayout.CENTER);
		try {
			rootPanel.revalidate();
			rootPanel.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Thread clusterThread = new Thread() {
			public void run() {
				// initialize cluster sets
				ClusterSet tmpClusters = new ClusterSet(input.getLog());
				tmpClusters.initializeClusterSet();
				clusters = new ClusterSet(input.getLog(),
						(AggregateProfile) input.getProfile());
				while (tmpClusters.size() > 1) {
					Cluster generated = createNextCluster(tmpClusters, diameter);
					ArrayList<Cluster> removeList = new ArrayList<Cluster>();
					for (Cluster tmpCluster : tmpClusters.getClusters()) {
						if (generated.containsAll(tmpCluster) == true) {
							removeList.add(tmpCluster);
						}
					}
					tmpClusters.getClusters().removeAll(removeList);
					clusters.getClusters().add(generated);
					int clustered = input.getLog().numberOfInstances()
							- tmpClusters.size();
					progress.setProgress(clustered);
					progress.setNote(clustered + " of "
							+ input.getLog().numberOfInstances()
							+ " traces clustered...");
				}
				clusters.getClusters().addAll(tmpClusters.getClusters());
				// view.getViewport().setView(new DisjointClusterSetUI(clusters,
				// input));
				DisjointClusterView clusterView = new DisjointClusterView(
						clusters, input);
				rootPanel.remove(view);
				view = clusterView;
				rootPanel.add(view, BorderLayout.CENTER);
				try {
					rootPanel.revalidate();
					rootPanel.repaint();
				} catch (Exception e) {
					e.printStackTrace();
				}
				thresholdSlider.setEnabled(true);
			}
		};
		thresholdSlider.setEnabled(false);
		clusterThread.start();
	}

	protected Cluster createNextCluster(ClusterSet clusters, double maxDiameter) {
		Cluster candidate = null;
		int maxSize = 0;
		// find largest cluster from provided set within max. diameter
		for (Cluster cluster : clusters.getClusters()) {
			Cluster current = generateCluster(cluster, clusters, maxDiameter);
			int size = current.getTraceIndices().size();
			if (size > maxSize) {
				candidate = current;
				maxSize = size;
			}
		}
		return candidate;
	}

	protected Cluster generateCluster(Cluster seed, ClusterSet clusters,
			double diameter) {
		Cluster generated = new Cluster(seed);
		// generate list of comparable pointers to non-seed clusters
		DistanceMatrix distanceMatrix = input.getDistanceMatrix();
		List<ClusterPointer> pointers = new ArrayList<ClusterPointer>();
		for (Cluster cluster : clusters.getClusters()) {
			if (cluster != seed) {
				pointers.add(new ClusterPointer(cluster, seed, distanceMatrix));
			}
		}
		// sort pointers in descending order of distance
		Collections.sort(pointers);
		// build new cluster
		ClusterPointer pointer;
		for (int i = 0; i < pointers.size(); i++) {
			pointer = pointers.get(i);
			int pointerIndex = pointer.getTraceIndex();
			for (int clIndex : generated.getTraceIndices()) {
				if (distanceMatrix.get(pointerIndex, clIndex) > diameter) {
					return generated;
				}
			}
			// new cluster can be added diameter-safely
			generated.mergeWith(pointer.getCluster());
		}
		return generated;
	}

	protected class ClusterPointer implements Comparable<ClusterPointer> {

		protected Cluster cluster;
		double distanceToReference;

		protected ClusterPointer(Cluster aCluster, Cluster aReferenceCluster,
				DistanceMatrix aDistanceMatrix) {
			cluster = aCluster;
			int clusterIndex = cluster.getTraceIndices().get(0);
			int referenceIndex = aReferenceCluster.getTraceIndices().get(0);
			distanceToReference = aDistanceMatrix.get(clusterIndex,
					referenceIndex);
		}

		public double getDistanceToReference() {
			return distanceToReference;
		}

		public Cluster getCluster() {
			return cluster;
		}

		public int getTraceIndex() {
			return cluster.getTraceIndices().get(0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(ClusterPointer other) {
			if (distanceToReference > other.distanceToReference) {
				return 1;
			} else if (distanceToReference < other.distanceToReference) {
				return -1;
			} else {
				return 0;
			}
		}

	}

}
