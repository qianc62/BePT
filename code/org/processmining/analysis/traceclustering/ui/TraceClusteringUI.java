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
package org.processmining.analysis.traceclustering.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.SmoothPanel;

import weka.gui.beans.ScatterPlotMatrix;
import weka.gui.beans.AttributeSummarizer;

import org.processmining.analysis.traceclustering.algorithm.AHCAlgorithm;
import org.processmining.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm;
import org.processmining.analysis.traceclustering.algorithm.ClusteringAlgorithm;
import org.processmining.analysis.traceclustering.algorithm.ClusteringInput;
import org.processmining.analysis.traceclustering.algorithm.DensityBasedClustererAlgorithm;
import org.processmining.analysis.traceclustering.algorithm.EMAlgorithm;
import org.processmining.analysis.traceclustering.algorithm.WekaKMeansAlgorithm;
import org.processmining.analysis.traceclustering.algorithm.FarthestFirstAlgorithm;
import org.processmining.analysis.traceclustering.algorithm.ParametricClustering;
import org.processmining.analysis.traceclustering.algorithm.QualityThresholdAlgorithm;
import org.processmining.analysis.traceclustering.algorithm.SOMAlgorithm;
import org.processmining.analysis.traceclustering.distance.CorrelationCoefficientDistance;
import org.processmining.analysis.traceclustering.distance.DistanceMetric;
import org.processmining.analysis.traceclustering.distance.EuclideanDistance;
import org.processmining.analysis.traceclustering.distance.GenericEditDistance;
import org.processmining.analysis.traceclustering.distance.HammingDistance;
import org.processmining.analysis.traceclustering.distance.JaccardIndexDistance;
import org.processmining.analysis.traceclustering.distance.LevenshteinDistance;
import org.processmining.analysis.traceclustering.preprocessor.RPProfile;
import org.processmining.analysis.traceclustering.preprocessor.PCAProfile;
import org.processmining.analysis.traceclustering.preprocessor.SVDProfile;
import org.processmining.analysis.traceclustering.preprocessor.TFIDFProfile;
import org.processmining.analysis.traceclustering.preprocessor.AbstractPreProcessor;
import org.processmining.analysis.traceclustering.profile.ActivityCharStreamProfile;
import org.processmining.analysis.traceclustering.profile.ActivityPatternAlphabetsProfile;
import org.processmining.analysis.traceclustering.profile.ActivityPatternsProfile;
import org.processmining.analysis.traceclustering.profile.ActivityProfile;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.analysis.traceclustering.profile.CaseDataProfile;
import org.processmining.analysis.traceclustering.profile.DataTypeProfile;
import org.processmining.analysis.traceclustering.profile.DataValueProfile;
import org.processmining.analysis.traceclustering.profile.OriginatorProfile;
import org.processmining.analysis.traceclustering.profile.PerformanceProfile;
import org.processmining.analysis.traceclustering.profile.Profile;
import org.processmining.analysis.traceclustering.profile.TransitionProfile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.StopWatch;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;

/**
 * User interface for the trace clustering plugin
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 */
public class TraceClusteringUI extends JPanel implements Provider {

	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	public static Color colorTextAreaBg = new Color(160, 160, 160);

	protected LogReader log;
	protected List<Profile> profiles;
	protected AggregateProfile aggregateProfile;
	protected List<DistanceMetric> distanceMetrics;
	protected List<ClusteringAlgorithm> algorithms;
	protected ClusteringAlgorithm activeAlgorithm;
	protected GUIPropertyBoolean tfIDF;
	protected GUIPropertyBoolean reduction;
	protected List<Profile> reductionMethods;
	protected JPanel clusteringPanel;
	// protected SlickTabbedPane tabbedPane;
	protected JPanel configurationPanel;
	protected JComboBox clusteringAlgorithmsBox;
	protected JComboBox distanceMetricsBox;
	protected JComboBox reductionMethodsBox;
	protected JTextArea algorithmDescription;
	protected JTextArea metricDescription;
	protected GUIPropertyInteger reducedDim;

	private boolean abortProcess;

	public TraceClusteringUI(LogReader aLog) throws IndexOutOfBoundsException,
			IOException {
		this.setLayout(new BorderLayout());
		this.setBackground(colorBg);
		log = aLog;
		buildProfiles();
	}

	protected void buildProfiles() throws IndexOutOfBoundsException,
			IOException {
		this.removeAll();
		final ProgressPanel progressPanel = new ProgressPanel(
				"Building profiles");
		progressPanel.setNote("Building set of profiles from log...");
		this.add(progressPanel.getPanel(), BorderLayout.CENTER);
		progressPanel.setMinMax(0, 9);
		profiles = new ArrayList<Profile>();
		reductionMethods = new ArrayList<Profile>();
		Thread buildProfileThread = new Thread() {
			public void run() {
				try {
					StopWatch watch = new StopWatch();
					watch.start();
					progressPanel.setNote("Deriving activity profile...");
					progressPanel.setProgress(1);
					profiles.add(new ActivityProfile(log));
					/* JC's Addition Begin */
					progressPanel.setNote("Deriving activity profile...");
					progressPanel.setProgress(1);
					profiles.add(new ActivityCharStreamProfile(log));
					progressPanel
							.setNote("Deriving activity patterns profile...");
					progressPanel.setProgress(1);
					profiles.add(new ActivityPatternsProfile(log));
					progressPanel
							.setNote("Deriving activity pattern alphabets profile...");
					progressPanel.setProgress(1);
					profiles.add(new ActivityPatternAlphabetsProfile(log));
					/* JC's Addition End */
					progressPanel.setNote("Deriving data type profile...");
					progressPanel.setProgress(2);
					profiles.add(new DataTypeProfile(log));
					progressPanel.setNote("Deriving data value profile...");
					progressPanel.setProgress(3);
					profiles.add(new DataValueProfile(log));
					progressPanel.setNote("Deriving originator profile...");
					progressPanel.setProgress(4);
					profiles.add(new OriginatorProfile(log));
					progressPanel.setNote("Deriving performance profile...");
					progressPanel.setProgress(5);
					profiles.add(new PerformanceProfile(log));
					progressPanel.setNote("Deriving transition profile...");
					progressPanel.setProgress(6);
					profiles.add(new TransitionProfile(log));
					// progressPanel.setNote("Deriving indirect transition profile...");
					// progressPanel.setProgress(7);
					// profiles.add(new IndirectTransitionProfile(log));
					progressPanel.setNote("Deriving case data profile...");
					progressPanel.setProgress(8);
					profiles.add(new CaseDataProfile(log));
					progressPanel.setNote("done!");
					progressPanel.setProgress(9);
					watch.stop();
					reductionMethods.add(new SVDProfile(log));
					reductionMethods.add(new RPProfile(log));
					reductionMethods.add(new PCAProfile(log));
					Message.add("Built profiles in " + watch.formatDuration());
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				buildGui();
			}
		};
		buildProfileThread.start();
	}

	protected void buildGui() {
		// assemble list of available distance metrics
		distanceMetrics = new ArrayList<DistanceMetric>();
		distanceMetrics.add(new EuclideanDistance());
		distanceMetrics.add(new JaccardIndexDistance());
		distanceMetrics.add(new HammingDistance());
		distanceMetrics.add(new CorrelationCoefficientDistance());
		distanceMetrics.add(new LevenshteinDistance());
		distanceMetrics.add(new GenericEditDistance());
		// assemble list of available clustering algorithms
		algorithms = new ArrayList<ClusteringAlgorithm>();
		algorithms.add(new QualityThresholdAlgorithm());
		/* JC's Addition Start */
		algorithms.add(new AgglomerativeHierarchicalAlgorithm());
		/* JC's Addition End */
		algorithms.add(new AHCAlgorithm());
		algorithms.add(new WekaKMeansAlgorithm());
		algorithms.add(new FarthestFirstAlgorithm());
		algorithms.add(new DensityBasedClustererAlgorithm());
		algorithms.add(new EMAlgorithm());
		algorithms.add(new SOMAlgorithm());
		algorithms.add(new ParametricClustering());
		// clear currently used algorithm marker
		activeAlgorithm = null;
		// create configuration panel
		JPanel confLowerPanel = new JPanel();
		confLowerPanel.setBackground(colorBg);
		confLowerPanel.setLayout(new BorderLayout());
		confLowerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		// create list of profiles panel
		JPanel profilesPanel = new JPanel();
		profilesPanel.setBackground(colorBg);
		profilesPanel.setLayout(new BoxLayout(profilesPanel, BoxLayout.Y_AXIS));
		profilesPanel.setBorder(BorderFactory.createEmptyBorder());
		// add a configuration component to list for each profile
		for (Profile profile : profiles) {
			profilesPanel.add(new ProfileConfigurationComponent(profile));
		}
		// enclose list of profiles in scroll pane
		JScrollPane profilesScrollPane = new JScrollPane(profilesPanel);
		profilesScrollPane.setBorder(BorderFactory.createLineBorder(new Color(
				90, 90, 90)));
		profilesScrollPane.setBackground(colorBg);
		profilesScrollPane.setMinimumSize(new Dimension(400, 2000));
		profilesScrollPane.setMaximumSize(new Dimension(430, 2000));
		profilesScrollPane.setPreferredSize(new Dimension(420, 2000));
		profilesScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		profilesScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		profilesScrollPane.getVerticalScrollBar().setBlockIncrement(25);
		profilesScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		// add scroll pane to the left of the configuration panel
		JPanel profilesSuperPanel = new JPanel();
		profilesSuperPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10,
				10));
		profilesSuperPanel.setBackground(colorBg);
		profilesSuperPanel.setMinimumSize(new Dimension(400, 2000));
		profilesSuperPanel.setMaximumSize(new Dimension(450, 2000));
		profilesSuperPanel.setPreferredSize(new Dimension(440, 2000));
		profilesSuperPanel.setLayout(new BorderLayout());
		JLabel profilesSuperLabel = new JLabel("Profiles configuration");
		profilesSuperLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10,
				10));
		profilesSuperLabel.setForeground(colorFg);
		profilesSuperLabel.setOpaque(false);
		profilesSuperLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		profilesSuperLabel.setFont(profilesSuperLabel.getFont().deriveFont(
				16.0f));
		profilesSuperPanel.add(profilesSuperLabel, BorderLayout.NORTH);
		profilesSuperPanel.add(profilesScrollPane, BorderLayout.CENTER);
		confLowerPanel.add(profilesSuperPanel, BorderLayout.WEST);
		// create right side, i.e. distance metric and clustering algorithm
		// choice
		SmoothPanel rightPanel = new SmoothPanel();
		rightPanel.setBackground(new Color(140, 140, 140));
		rightPanel.setHighlight(new Color(160, 160, 160));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		// prepare comboboxes
		clusteringAlgorithmsBox = new JComboBox(algorithms.toArray());
		clusteringAlgorithmsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ClusteringAlgorithm alg = (ClusteringAlgorithm) clusteringAlgorithmsBox
						.getSelectedItem();
				algorithmDescription.setText(alg.getDescription());
			}
		});
		clusteringAlgorithmsBox.setOpaque(false);
		distanceMetricsBox = new JComboBox(distanceMetrics.toArray());
		distanceMetricsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DistanceMetric metric = (DistanceMetric) distanceMetricsBox
						.getSelectedItem();
				metricDescription.setText(metric.getDescription());
			}
		});
		distanceMetricsBox.setOpaque(false);

		reductionMethodsBox = new JComboBox(reductionMethods.toArray());
		reductionMethodsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Profile profile =
				// (Profile)reductionMethodsBox.getSelectedItem();
			}
		});
		reductionMethodsBox.setOpaque(false);

		JLabel metricsLabel = new JLabel("Distance metric");
		metricsLabel.setOpaque(false);
		metricsLabel.setForeground(colorFg);
		metricsLabel.setFont(metricsLabel.getFont().deriveFont(16.0f));
		JLabel algorithmsLabel = new JLabel("Clustering algorithm");
		algorithmsLabel.setOpaque(false);
		algorithmsLabel.setForeground(colorFg);
		algorithmsLabel.setFont(algorithmsLabel.getFont().deriveFont(16.0f));
		JLabel preprocessingLabel = new JLabel("Preprocessing");
		preprocessingLabel.setOpaque(false);
		preprocessingLabel.setForeground(colorFg);
		preprocessingLabel.setFont(preprocessingLabel.getFont().deriveFont(
				16.0f));
		metricDescription = new JTextArea();// 3, 20);
		metricDescription.setWrapStyleWord(true);
		metricDescription.setFont(metricDescription.getFont().deriveFont(11f));
		metricDescription
				.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		metricDescription.setBackground(colorTextAreaBg);
		metricDescription.setForeground(colorFg);
		metricDescription.setMaximumSize(new Dimension(400, 50));
		metricDescription.setMinimumSize(new Dimension(400, 50));
		metricDescription.setPreferredSize(new Dimension(400, 50));
		metricDescription.setText(((DistanceMetric) distanceMetricsBox
				.getSelectedItem()).getDescription());
		algorithmDescription = new JTextArea();// 3, 30);
		algorithmDescription.setWrapStyleWord(true);
		algorithmDescription.setFont(algorithmDescription.getFont().deriveFont(
				11f));
		algorithmDescription.setBorder(BorderFactory.createEmptyBorder(3, 3, 3,
				3));
		algorithmDescription.setBackground(colorTextAreaBg);
		algorithmDescription.setForeground(colorFg);
		algorithmDescription.setMaximumSize(new Dimension(400, 50));
		algorithmDescription.setMinimumSize(new Dimension(400, 50));
		algorithmDescription.setPreferredSize(new Dimension(400, 50));
		algorithmDescription
				.setText(((ClusteringAlgorithm) clusteringAlgorithmsBox
						.getSelectedItem()).getDescription());
		tfIDF = new GUIPropertyBoolean("TF.IDF", false);
		reduction = new GUIPropertyBoolean("Preprocessing", false);
		reducedDim = new GUIPropertyInteger("dim:", 3, 0, 50);
		rightPanel.add(packHorizontallyLeftAligned(metricsLabel, 5));
		rightPanel.add(Box.createVerticalStrut(5));
		rightPanel.add(packHorizontallyLeftAligned(distanceMetricsBox, 25));
		rightPanel.add(Box.createVerticalStrut(5));
		rightPanel.add(packHorizontallyLeftAligned(metricDescription, 25));
		rightPanel.add(Box.createVerticalStrut(10));
		rightPanel.add(packHorizontallyLeftAligned(algorithmsLabel, 5));
		rightPanel.add(Box.createVerticalStrut(5));
		rightPanel
				.add(packHorizontallyLeftAligned(clusteringAlgorithmsBox, 25));
		rightPanel.add(Box.createVerticalStrut(5));
		rightPanel.add(packHorizontallyLeftAligned(algorithmDescription, 25));
		rightPanel.add(Box.createVerticalStrut(10));
		rightPanel.add(packHorizontallyLeftAligned(preprocessingLabel, 5));
		rightPanel.add(Box.createVerticalStrut(5));
		rightPanel
				.add(packHorizontallyLeftAligned(tfIDF.getPropertyPanel(), 25));
		rightPanel.add(Box.createVerticalStrut(5));
		rightPanel.add(packHorizontallyLeftAligned(
				reduction.getPropertyPanel(), 25));
		rightPanel.add(Box.createVerticalStrut(5));
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.LINE_AXIS));
		tempPanel.add(reductionMethodsBox);
		tempPanel.add(reducedDim.getPropertyPanel());
		tempPanel.setBackground(colorInnerBg);
		rightPanel.add(packHorizontallyLeftAligned(tempPanel, 25));
		rightPanel.add(Box.createVerticalGlue());
		JButton startButton = new AutoFocusButton("start clustering");
		startButton.setOpaque(false);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DistanceMetric metric = (DistanceMetric) distanceMetricsBox
						.getSelectedItem();
				if (metric.getName().equals("Generic Edit Distance")
						|| metric.getName().equals("Levenshtein Edit Distance")) {
					int noActiveProfiles = 0;
					boolean isCharStreamProfile = true;
					for (Profile profile : profiles) {
						if (profile.getNormalizationMaximum() > 0.0)
							noActiveProfiles++;
						if (profile.getNormalizationMaximum() > 0.0
								&& !profile.getName().equals(
										"Activity Char Streams")) {
							isCharStreamProfile = false;
							break;
						}
					}
					if (noActiveProfiles > 1 || !isCharStreamProfile) {
						MainUI.getInstance().showGlassDialog(
								"Edit Distance Undefined",
								"Choose only Activity Char Streams Profile");
						abortProcess = true;
					} else {
						abortProcess = false;
					}
				}
				if (!abortProcess)
					startClustering();
			}
		});
		rightPanel.add(packHorizontallyRightAligned(startButton, 0));
		// add right side to configuration panel at center position
		confLowerPanel.add(rightPanel, BorderLayout.CENTER);

		// add header
		configurationPanel = new JPanel();
		configurationPanel.setBorder(BorderFactory.createEmptyBorder());
		configurationPanel.setLayout(new BorderLayout());
		configurationPanel.add(confLowerPanel, BorderLayout.CENTER);
		HeaderBar header = new HeaderBar("Trace clustering");
		header.setHeight(40);
		configurationPanel.add(header, BorderLayout.NORTH);
		// set configuration panel as displayed
		configurationPanel.revalidate();
		this.removeAll();
		this.add(configurationPanel, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	protected void startClustering() {
		final ProgressPanel progressPanel = new ProgressPanel(
				"Aggregating profiles");
		progressPanel.setNote("Aggregating profiles...");
		this.removeAll();
		this.add(progressPanel.getPanel(), BorderLayout.CENTER);
		aggregateProfile = new AggregateProfile(log);
		for (Profile profile : profiles) {
			if (profile.getNormalizationMaximum() > 0.0) {
				aggregateProfile.addProfile(profile);
			}
		}
		if (tfIDF.getValue()) {
			getIFIDF();
		}
		if (reduction.getValue()) {
			AbstractPreProcessor preprocessor = (AbstractPreProcessor) reductionMethodsBox
					.getSelectedItem();
			try {
				if (preprocessor instanceof RPProfile)
					preprocessor = new RPProfile(log);
				if (preprocessor instanceof SVDProfile)
					preprocessor = new SVDProfile(log);
				if (preprocessor instanceof PCAProfile)
					preprocessor = new PCAProfile(log);
			} catch (Exception ce) {
			}
			;
			preprocessor.buildProfile(aggregateProfile, reducedDim.getValue());
			aggregateProfile = null;
			aggregateProfile = new AggregateProfile(log);
			aggregateProfile.addProfile(preprocessor);
		}
		DistanceMetric metric = (DistanceMetric) distanceMetricsBox
				.getSelectedItem();
		ClusteringInput input = new ClusteringInput(aggregateProfile, metric);
		ClusteringAlgorithm algorithm = (ClusteringAlgorithm) clusteringAlgorithmsBox
				.getSelectedItem();
		algorithm.setInput(input);
		// added
		// tabbedPane = new SlickTabbedPane();
		// tabbedPane.addTab("Trace Clustering", algorithm.getUI());
		// tabbedPane.addTab("Profile view", getScatterPoltMatrix());
		// tabbedPane.addTab("Profile Summary", getAttributeSummarizer());

		JComponent clusteringUI = algorithm.getUI();
		clusteringPanel = new JPanel();
		clusteringPanel.setBorder(BorderFactory.createEmptyBorder());
		clusteringPanel.setLayout(new BorderLayout());
		HeaderBar header = new HeaderBar(algorithm.getName() + " ("
				+ metric.getName() + ")");
		header.setHeight(40);
		header.setCloseActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// reset to configuration view
				activeAlgorithm = null;
				removeAll();
				add(configurationPanel, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		});
		clusteringPanel.add(header, BorderLayout.NORTH);
		// clusteringPanel.add(tabbedPane, BorderLayout.CENTER);
		clusteringPanel.add(clusteringUI, BorderLayout.CENTER);
		removeAll();
		add(clusteringPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
		activeAlgorithm = algorithm;
	}

	protected void getIFIDF() {
		TFIDFProfile newProfile = null;
		try {
			newProfile = new TFIDFProfile(log);
			newProfile.buildProfile(aggregateProfile);
		} catch (Exception ce) {
		}
		aggregateProfile = null;
		aggregateProfile = new AggregateProfile(log);
		aggregateProfile.addProfile(newProfile);
	}

	protected JPanel getScatterPoltMatrix() {
		ScatterPlotMatrix dv = new ScatterPlotMatrix();
		try {
			dv.setInstances(aggregateProfile.getWekaData());
			dv.setPreferredSize(new Dimension(350, 350));
		} catch (Exception ce) {
		}
		return dv;
	}

	protected JPanel getAttributeSummarizer() {
		AttributeSummarizer as = new AttributeSummarizer();
		try {
			as.setInstances(aggregateProfile.getWekaData());
			as.setPreferredSize(new Dimension(350, 350));
		} catch (Exception ce) {
		}
		return as;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		if (activeAlgorithm != null && activeAlgorithm.getClusters() != null) {
			return activeAlgorithm.getClusters().getProvidedObjects();
		} else {
			// just return the complete log (no clusters currently available)
			ProvidedObject[] objects = new ProvidedObject[1];
			// add complete log
			objects[0] = new ProvidedObject("Complete log",
					new Object[] { log });
			return objects;
		}
	}

	protected static JPanel packHorizontallyLeftAligned(JComponent comp,
			int leftOffset) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		if (leftOffset > 0) {
			packed.add(Box.createHorizontalStrut(leftOffset));
		}
		packed.add(comp);
		packed.add(Box.createHorizontalGlue());
		int height = (int) comp.getMinimumSize().getHeight();
		packed.setMinimumSize(comp.getMinimumSize());
		packed.setMaximumSize(new Dimension(4000, (int) height));
		packed.setPreferredSize(new Dimension(4000, (int) height));
		return packed;
	}

	protected static JPanel packHorizontallyRightAligned(JComponent comp,
			int rightOffset) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalGlue());
		packed.add(comp);
		if (rightOffset > 0) {
			packed.add(Box.createHorizontalStrut(rightOffset));
		}
		int height = (int) comp.getMinimumSize().getHeight();
		packed.setMinimumSize(comp.getMinimumSize());
		packed.setMaximumSize(new Dimension(4000, (int) height));
		packed.setPreferredSize(new Dimension(4000, (int) height));
		return packed;
	}

}
