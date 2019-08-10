package org.processmining.analysis.traceclustering.algorithm;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.analysis.traceclustering.distance.DistanceMetric;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.Dot;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.FarthestFirst;
import weka.clusterers.MakeDensityBasedClusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.clusterers.SimpleKMeans;

import att.grappa.Graph;
import att.grappa.GrappaPanel;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Minseok Song
 */
public class DensityBasedClustererAlgorithm extends WekaAlgorithm {

	protected GUIPropertyListEnumeration clusterBox;

	public DensityBasedClustererAlgorithm() {
		super("Density Based Clustering",
				"Density Based Clustering allows the user to specify"
						+ " the number of clusters. The algorithm will return"
						+ " the number of clusters which users want.");
		clusters = null;
		clusterer = new MakeDensityBasedClusterer();

	}

	protected void doCluster() {
		try {

			Clusterer clusterer2 = null;
			if (clusterBox.getValue().equals("K-means")) {
				clusterer2 = new SimpleKMeans();
				((SimpleKMeans) clusterer2).setSeed(randomSeedBox.getValue());
				((MakeDensityBasedClusterer) clusterer)
						.setClusterer(clusterer2);
				((MakeDensityBasedClusterer) clusterer)
						.setNumClusters(clusterSizeBox.getValue());
			} else if (clusterBox.getValue().equals("Farthest First")) {
				clusterer2 = new FarthestFirst();
				((FarthestFirst) clusterer2).setSeed(randomSeedBox.getValue());
				((MakeDensityBasedClusterer) clusterer)
						.setClusterer(clusterer2);
			} else {
				clusterer2 = new EM();
				((EM) clusterer2).setSeed(randomSeedBox.getValue());
				((MakeDensityBasedClusterer) clusterer)
						.setClusterer(clusterer2);
				((MakeDensityBasedClusterer) clusterer)
						.setNumClusters(clusterSizeBox.getValue());
			}

			clusterer.buildClusterer(data);
			assignInstace();

		} catch (Exception c) {
			Message.add("Weka Error: " + c.toString(), Message.ERROR);
		}
	}

	protected SmoothPanel getMenuPanel() {
		SmoothPanel menuPanel = new SmoothPanel();
		menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.PAGE_AXIS));

		menuPanel.add(clusterSizeBox.getPropertyPanel());
		menuPanel.add(randomSeedBox.getPropertyPanel());
		ArrayList<String> values = new ArrayList<String>();
		values.add("K-means");
		values.add("Farthest First");
		values.add("EM");
		clusterBox = new GUIPropertyListEnumeration("Clusterer to wrap =",
				null, values, null, 110);
		menuPanel.add(clusterBox.getPropertyPanel());

		startButton = new JButton("cluster");
		startButton.setOpaque(false);
		startButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clusterSize = clusterSizeBox.getValue();
				cluster();
			}
		});
		menuPanel.add(startButton);
		return menuPanel;
	}
}
