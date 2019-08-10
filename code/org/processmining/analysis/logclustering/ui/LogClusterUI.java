package org.processmining.analysis.logclustering.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.logclustering.LogClusterPlugin;
import org.processmining.analysis.logclustering.clusteringalgorithm.AHClustering;
import org.processmining.analysis.logclustering.clusteringalgorithm.ClusteringAlgorithm;
import org.processmining.analysis.logclustering.clusteringalgorithm.DefaultClustering;
import org.processmining.analysis.logclustering.clusteringalgorithm.KMeansClustering;
import org.processmining.analysis.logclustering.profiles.ActivityVectorProfile;
import org.processmining.analysis.logclustering.profiles.AggregateProfiles;
import org.processmining.analysis.logclustering.profiles.OriginatorVectorProfile;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class LogClusterUI extends JPanel implements GuiNotificationTarget {
	protected static Color COLOR_BG = new Color(120, 120, 120);

	final LogClusterPlugin myAlgorithm;
	final AnalysisInputItem[] myInput;
	final LogReader myLog;

	private LogSummary summary = null;
	// profiles
	private ActivityVectorProfile activityProfile;
	private OriginatorVectorProfile originatorProfile;
	private AggregateProfiles agProfiles;

	// clustering algorithm
	GUIPropertyListEnumeration casGuiList;
	ClusteringAlgorithm clusteringAlgorithm;

	// GUI related attributes
	private JPanel buttonsPanel = new JPanel(new BorderLayout()); // lower panel
	// containing
	// the
	// buttons
	private JPanel infoButtonsPanel = new JPanel(); // panel containing the log
	// summary and the
	// documentation button
	private JButton startButton = new JButton("Start Analysis");
	private JButton docsButton = new JButton("Plugin documentation..."); // shows
	// the
	// plugin
	// documentation
	protected JPanel profilesListPanel;
	protected JScrollPane metricsScrollPane;
	protected JSplitPane rootSplitPane;
	protected JPanel rightPanel;

	public LogClusterUI(LogClusterPlugin algorithm, AnalysisInputItem[] input,
			LogReader log) {
		myAlgorithm = algorithm;
		myInput = input;
		myLog = log;
		summary = myLog.getLogSummary();
		// build GUI
		try {
			jbInit();
			registerGuiActionListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Build the performance analysis settings GUI, in which the user can
	 * specify performance settings, before starting the actual analysis.
	 */
	private void jbInit() {
		activityProfile = new ActivityVectorProfile(myLog);
		originatorProfile = new OriginatorVectorProfile(myLog);

		profilesListPanel = new JPanel();
		profilesListPanel.setBorder(BorderFactory.createEmptyBorder());
		profilesListPanel.setBackground(new Color(40, 40, 40));
		profilesListPanel.setLayout(new BoxLayout(profilesListPanel,
				BoxLayout.Y_AXIS));
		profilesListPanel.add(new ProfileConfigurationUI(activityProfile));
		profilesListPanel.add(new ProfileConfigurationUI(originatorProfile));
		metricsScrollPane = new JScrollPane(profilesListPanel);
		metricsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		metricsScrollPane.setOpaque(false);
		metricsScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		metricsScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel metricsPanel = new JPanel();
		metricsPanel.setBackground(COLOR_BG);
		metricsPanel.setLayout(new BorderLayout());
		metricsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JPanel filterPanel = new JPanel();
		filterPanel.setOpaque(false);
		filterPanel.setLayout(new BorderLayout());
		filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		metricsPanel.add(metricsScrollPane, BorderLayout.CENTER);

		rootSplitPane = new JSplitPane();
		rootSplitPane.setBorder(BorderFactory.createEmptyBorder());
		rootSplitPane.setBackground(COLOR_BG);
		rootSplitPane.setForeground(COLOR_BG);
		rootSplitPane.setDividerLocation(500);
		rootSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		rootSplitPane.setLeftComponent(metricsPanel);

		// clustering algotirm
		ArrayList<ClusteringAlgorithm> casList = new ArrayList<ClusteringAlgorithm>();
		casList.add(new DefaultClustering());
		casList.add(new KMeansClustering());
		casList.add(new AHClustering());
		casGuiList = new GUIPropertyListEnumeration("Clustering Algorithm",
				casList, this);

		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(casGuiList.getPropertyPanel());
		rightPanel.add(((ClusteringAlgorithm) casGuiList.getValue())
				.getOptionPanel());
		rootSplitPane.setRightComponent(rightPanel);

		buttonsPanel.add(startButton, BorderLayout.EAST);
		infoButtonsPanel.add(docsButton);
		buttonsPanel.add(infoButtonsPanel, BorderLayout.WEST);
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		// pack
		this.setLayout(new BorderLayout());
		this.add(rootSplitPane, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.SOUTH);
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.validate();
		this.repaint();

	}

	/**
	 * Connects the GUI elements with functionality to create interaction.
	 */
	private void registerGuiActionListener() {
		// show plug-in documentation
		docsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(myAlgorithm);
			}
		});
		// start analysis
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				execute();
			}
		});
	}

	private void execute() {

		// for(int i=0; i<summary.getModelElements().length;i++)
		// System.out.println(summary.getModelElements()[i].toString());
		Iterator it = myLog.instanceIterator();
		int index = 0;
		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();
			Iterator ates = pi.getAuditTrailEntryList().iterator();

			while (ates.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ates.next();
				if (activityProfile.getNormalizationMaximum() != 0)
					activityProfile.buildProfile(index, ate);
				if (originatorProfile.getNormalizationMaximum() != 0)
					originatorProfile.buildProfile(index, ate);
			}
			index++;
		}

		agProfiles = new AggregateProfiles(summary
				.getNumberOfProcessInstances());
		agProfiles.addProfileMetrics(activityProfile);
		agProfiles.addProfileMetrics(originatorProfile);

		clusteringAlgorithm = (ClusteringAlgorithm) (casGuiList.getValue());
		clusteringAlgorithm.setAggregateProfiles(agProfiles);
		LogClusterResultUI result = new LogClusterResultUI(myLog,
				clusteringAlgorithm);
		result.setSize(600, 400);
		result.setVisible(true);
		result.validate();
		result.repaint();
		MainUI.getInstance().createFrame("Result", result);
	}

	public void updateGUI() {
		rightPanel = null;
		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(casGuiList.getPropertyPanel());
		rightPanel.add(((ClusteringAlgorithm) casGuiList.getValue())
				.getOptionPanel());
		rootSplitPane.setRightComponent(rightPanel);
	}
}
