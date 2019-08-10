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
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.deckfour.slickerbox.components.SlickTabbedPane;
import org.processmining.analysis.traceclustering.distance.EuclideanDistance;
import org.processmining.analysis.traceclustering.model.AHCTreeNode;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.StopWatch;
import org.processmining.analysis.benchmark.metric.TokenFitnessMetric;
import org.processmining.mining.petrinetmining.AlphaProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.mining.regionmining.RegionMiner;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.SmoothPanel;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class ModelDrawAnalyzer extends JPanel {

	// HTML styles
	private static final String H1 = "h1";
	private static final String TR = "tr";
	private static final String TH = "th";
	private static final String TD = "td";

	// for gui
	protected JPanel resultPanel = null;
	protected SlickTabbedPane tabbedPane = null;
	protected JPanel treeViewPanel = null;
	protected JPanel algorithmViewPanel = null;
	protected JPanel evaluationViewPanel = null;
	protected JPanel configurationPanel;

	public static Color colorTextAreaBg = new Color(160, 160, 160);
	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	protected ProgressPanel progressPanel = new ProgressPanel(
			"Evaluate clustering results");

	/**
	 * The data mining classifier to be used for analysis.
	 */
	protected ClusterSet clusters;
	protected AggregateProfile agProfiles;
	protected AHCTreeNode rootACH;

	public JPanel analyse(ClusterSet aClusters) {
		this.clusters = aClusters;
		agProfiles = clusters.getAGProfiles();
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

	protected void startAnalysis() {
		progressPanel.setNote("Evaluate clustering results ...");
		progressPanel.setMinMax(0, clusters.size());
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
				// draw model
				for (Cluster cluster : clusters.getClusters()) {
					try {
						// AlphaProcessMiner miningPlugin = new
						// AlphaProcessMiner();
						RegionMiner miningPlugin = new RegionMiner();
						PetriNetResult result = (PetriNetResult) miningPlugin
								.mine(cluster.getFilteredLog());
						JScrollPane scrollPane = new JScrollPane(result
								.getVisualization());
						tabbedPane.addTab(cluster.getName(), scrollPane);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					progressPanel.setProgress(progressPanel.getValue() + 1);
				}

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
}
