package org.processmining.mining.organizationmining.algorithm;

/*
 * @author Minseok Song (m.s.song@tue.nl)
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.mining.organizationmining.distance.DistanceMatrix;
import org.processmining.mining.organizationmining.model.AHCTreeNode;
import org.processmining.mining.organizationmining.model.Cluster;
import org.processmining.mining.organizationmining.model.ClusterSet;
import org.processmining.mining.organizationmining.profile.AggregateProfile;
import org.processmining.mining.organizationmining.ui.hierarchicalui.DendroGramUI;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;
import org.processmining.framework.models.orgmodel.Task;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.Dot;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

import att.grappa.Graph;
import att.grappa.GrappaPanel;

public class AHCAlgorithm extends ClusteringAlgorithm implements
		GuiNotificationTarget {

	private OrgModel orgModel = null;
	private int clusterCounter;
	protected ClusterSet clusters;
	protected AHCTreeNode rootAHCTree = null;
	protected ArrayList<Cluster> listAHCTree = null;
	protected int originatorSize = 0;
	protected DistanceMatrix distances;
	protected double upperBound, lowerBound, upperBoundUI, lowerBoundUI,
			cutPoint, upperMaxBound;
	protected boolean bDrawing = true;
	protected String currentMethod;
	protected AggregateProfile agProfiles;

	// GUI
	protected JPanel rootPanel;
	protected JScrollPane graphScrollPane;
	protected JPanel menuPanel;
	protected JPanel menuUpperPanel;
	protected JPanel lowerPanel;
	protected JPanel sliderPanel;
	protected JPanel sliderPanel_Dendo;
	protected JSlider sliderUpperUI;
	protected JSlider sliderCutPointUI;
	protected JSlider sliderLowerUI;
	protected JSlider sliderUpper;
	protected JSlider sliderLower;
	protected JTextField diameterTextField1;
	protected JTextField diameterTextField2;
	protected JTextField diameterTextField3;
	protected JTextField diameterTextField4;
	protected JTextField diameterTextField5;
	protected DecimalFormat format = new DecimalFormat("0.0000");
	protected Color bgColor = new Color(160, 160, 160);
	protected Color fgColor = new Color(50, 50, 50);
	protected JButton startButton;
	protected GUIPropertyListEnumeration clusteringMethod;
	protected GUIPropertyListEnumeration displayMethod;
	protected ProgressPanel progress;
	protected DendroGramUI dendrogram;

	// STATIC
	static protected String ST_Single_linkage = "Single linkage";
	static protected String ST_Complete_linkage = "Complete linkage";
	static protected String ST_Centroid_linkage = "Centroid linkage";
	static protected String ST_Average_linkage = "Average linkage";

	static protected String ST_DendroGram = "Dendrogram";
	static protected String ST_Tree = "Tree";

	/**
	 * @param name
	 * @param description
	 */
	public AHCAlgorithm() {
		super("AHC Miner", "Agglomerative Hierarchical clustering");
		clusters = null;
		listAHCTree = null;
	}

	public void cluster() {
		// prepare progress display
		progress = new ProgressPanel("Clustering");
		progress.setNote("processing...");
		graphScrollPane.getViewport().setView(progress.getPanel());

		progress.setMinMax(0, input.getLog().numberOfInstances() - 1
				+ (input.getLog().numberOfInstances()) / 100);
		progress.setProgress(0);
		graphScrollPane.getViewport().setView(progress.getPanel());
		Thread clusterThread = new Thread() {
			public void run() {
				// do cluster
				build();
				// update ui
				updateSliders();
				updateUI();
				startButton.setEnabled(true);
			}
		};
		startButton.setEnabled(false);
		clusterThread.start();
	}

	protected void build() {
		int step = 1;
		// initialize clusters
		if (clusters == null)
			clusters = new ClusterSet(input.getLog(), agProfiles);
		else
			clusters.clear();

		// initialize listAHCTree
		if (listAHCTree == null)
			listAHCTree = new ArrayList<Cluster>();
		else
			listAHCTree.clear();

		// initialize variables
		clusterCounter = 0;
		//
		progress.setProgress(step++);
		progress.setNote("calculate distances ...");

		distances = input.getDistanceMatrix();
		originatorSize = input.getLog().getLogSummary().getOriginators().length;

		// make initial Cluster
		for (int i = 0; i < originatorSize; i++) {
			// to do changed
			double temp[] = new double[agProfiles.numberOfItems()];
			for (int j = 0; j < temp.length; j++)
				temp[j] = agProfiles.getValue(i, j);
			AHCTreeNode tempCluster = new AHCTreeNode(input.getLog(),
					"OrgUnit " + clusterCounter++, temp);
			tempCluster.addOriginator(input.getLog().getLogSummary()
					.getOriginators()[i]);
			listAHCTree.add(tempCluster);
			if (i % 100 == 0) {
				progress.setProgress(step++);
				progress.setNote((i / 100) + " of " + (originatorSize / 100)
						+ "steps (initialzing clusters) ...");
			}
		}

		// clustering
		upperMaxBound = 0.0;
		for (int i = 0; i < originatorSize - 1; i++) {
			progress.setProgress(step + i);
			progress.setNote(i + " of " + (originatorSize - 2) + " steps ...");

			if (clusteringMethod.getValue().equals(ST_Single_linkage))
				findNearestCluster_SingleLinkage();
			if (clusteringMethod.getValue().equals(ST_Complete_linkage))
				findNearestCluster_CompleteLinkage();
			if (clusteringMethod.getValue().equals(ST_Centroid_linkage))
				findNearestCluster_CentroidLinkage();
			if (clusteringMethod.getValue().equals(ST_Average_linkage))
				findNearestCluster_AverageLinkage();
		}
		rootAHCTree = (AHCTreeNode) listAHCTree.get(0);
		// todo: should fix it
		if (!clusteringMethod.getValue().equals(ST_Centroid_linkage))
			upperMaxBound = rootAHCTree.getDistance();
	}

	// The Nearest-Neighbor Algorithm (Min (D1, D2))
	private void findNearestCluster_SingleLinkage() {
		double minDistance = Double.MAX_VALUE;
		Cluster aNode = null, bNode = null;
		for (int i = 0; i < listAHCTree.size() - 1; i++) {
			Cluster aTempNode = listAHCTree.get(i);
			for (int j = i + 1; j < listAHCTree.size(); j++) {
				Cluster bTempNode = listAHCTree.get(j);
				double tempMinDistance = Double.MAX_VALUE;
				Iterator itr = aTempNode.getOriginatorList().iterator();
				while (itr.hasNext()) {
					int trace1 = agProfiles.getIndexOfOriginator((String) itr
							.next());
					Iterator itr2 = bTempNode.getOriginatorList().iterator();
					while (itr2.hasNext()) {
						int trace2 = agProfiles
								.getIndexOfOriginator((String) itr2.next());
						if (tempMinDistance > distances.get(trace1, trace2))
							tempMinDistance = distances.get(trace1, trace2);
					}
				}
				if (minDistance > tempMinDistance) {
					minDistance = tempMinDistance;
					aNode = aTempNode;
					bNode = bTempNode;
				}
			}
		}
		AHCTreeNode aAHCTreeNode = new AHCTreeNode(input.getLog(), "OrgUnit "
				+ clusterCounter++, (AHCTreeNode) aNode, (AHCTreeNode) bNode,
				minDistance);
		listAHCTree.remove(aNode);
		listAHCTree.remove(bNode);
		listAHCTree.add(aAHCTreeNode);
	}

	// The Farthest-Neighbor Algorithm (Max (D1, D2))
	private void findNearestCluster_CompleteLinkage() {
		double minDistance = Double.MAX_VALUE;
		Cluster aNode = null, bNode = null;
		for (int i = 0; i < listAHCTree.size() - 1; i++) {
			Cluster aTempNode = listAHCTree.get(i);
			for (int j = i + 1; j < listAHCTree.size(); j++) {
				Cluster bTempNode = listAHCTree.get(j);
				double tempMaxDistance = Double.MIN_VALUE;
				Iterator itr = aTempNode.getOriginatorList().iterator();
				while (itr.hasNext()) {
					int trace1 = agProfiles.getIndexOfOriginator((String) itr
							.next());
					Iterator itr2 = bTempNode.getOriginatorList().iterator();
					while (itr2.hasNext()) {
						int trace2 = agProfiles
								.getIndexOfOriginator((String) itr2.next());
						if (tempMaxDistance < distances.get(trace1, trace2))
							tempMaxDistance = distances.get(trace1, trace2);
					}
				}
				if (minDistance > tempMaxDistance) {
					minDistance = tempMaxDistance;
					aNode = aTempNode;
					bNode = bTempNode;
				}
			}
		}
		AHCTreeNode aAHCTreeNode = new AHCTreeNode(input.getLog(), "OrgUnit "
				+ clusterCounter++, (AHCTreeNode) aNode, (AHCTreeNode) bNode,
				minDistance);
		listAHCTree.remove(aNode);
		listAHCTree.remove(bNode);
		listAHCTree.add(aAHCTreeNode);
	}

	// The Average Linkage Algorithm (Max (D1, D2))
	private void findNearestCluster_AverageLinkage() {
		double minDistance = Double.MAX_VALUE;
		Cluster aNode = null, bNode = null;
		for (int i = 0; i < listAHCTree.size() - 1; i++) {
			Cluster aTempNode = listAHCTree.get(i);
			for (int j = i + 1; j < listAHCTree.size(); j++) {
				Cluster bTempNode = listAHCTree.get(j);
				double tempDistanceSum = 0;
				Iterator itr = aTempNode.getOriginatorList().iterator();
				while (itr.hasNext()) {
					int trace1 = agProfiles.getIndexOfOriginator((String) itr
							.next());
					Iterator itr2 = bTempNode.getOriginatorList().iterator();
					while (itr2.hasNext()) {
						int trace2 = agProfiles
								.getIndexOfOriginator((String) itr2.next());
						tempDistanceSum += distances.get(trace1, trace2);
					}
				}
				tempDistanceSum = tempDistanceSum
						/ (aTempNode.size() * bTempNode.size());
				if (minDistance > tempDistanceSum) {
					minDistance = tempDistanceSum;
					aNode = aTempNode;
					bNode = bTempNode;
				}
			}
		}
		AHCTreeNode aAHCTreeNode = new AHCTreeNode(input.getLog(), "OrgUnit "
				+ clusterCounter++, (AHCTreeNode) aNode, (AHCTreeNode) bNode,
				minDistance);
		listAHCTree.remove(aNode);
		listAHCTree.remove(bNode);
		listAHCTree.add(aAHCTreeNode);
	}

	// The Centroid Linkage Algorithm (Max (D1, D2))
	private void findNearestCluster_CentroidLinkage() {
		double minDistance = Double.MAX_VALUE;
		Cluster aNode = null, bNode = null;
		for (int i = 0; i < listAHCTree.size() - 1; i++) {
			Cluster aTempNode = listAHCTree.get(i);
			double[] firstCenter = ((AHCTreeNode) aTempNode).getCenter();
			for (int j = i + 1; j < listAHCTree.size(); j++) {
				Cluster bTempNode = listAHCTree.get(j);
				double tempDistanceSum = 0;
				double[] secondCenter = ((AHCTreeNode) bTempNode).getCenter();
				for (int k = 0; k < firstCenter.length; k++) {
					double temp = (firstCenter[k] - secondCenter[k]);
					tempDistanceSum += temp * temp;
				}
				tempDistanceSum = Math.sqrt(tempDistanceSum);
				if (minDistance > tempDistanceSum) {
					minDistance = tempDistanceSum;
					aNode = aTempNode;
					bNode = bTempNode;
				}
			}
		}
		if (minDistance > upperMaxBound)
			upperMaxBound = minDistance;
		AHCTreeNode aAHCTreeNode = new AHCTreeNode(input.getLog(), "OrgUnit "
				+ clusterCounter++, (AHCTreeNode) aNode, (AHCTreeNode) bNode,
				minDistance);
		listAHCTree.remove(aNode);
		listAHCTree.remove(bNode);
		listAHCTree.add(aAHCTreeNode);
	}

	// generating organizational model
	private void buildOrgModelDendrogram() {
		orgModel = new OrgModel();
		String users[] = input.getLog().getLogSummary().getOriginators();

		for (int i = 0; i < users.length; i++) {

			Resource resource = new Resource(users[i], users[i]);
			orgModel.addResource(resource);
		}

		int temp = 0;
		HashMap<String, OrgEntity> orgEntityMap = new HashMap<String, OrgEntity>();
		for (Cluster cluster : clusters.getClusters()) {
			OrgEntity tempEntity = new OrgEntity("minedRole" + temp++, cluster
					.getName(), OrgEntity.ORGENTITYTYPE_MININGRESULT);
			orgModel.addOrgEntity(tempEntity);
			orgEntityMap.put(cluster.getName(), tempEntity);
			for (String user : cluster.getOriginatorList()) {
				Resource res = orgModel.getResource(user);
				res.addOrgEntity(tempEntity);
			}
		}

		LogEvents modelElements = input.getLog().getLogSummary().getLogEvents();
		for (int i = 0; i < modelElements.size(); i++) {
			Task tempTask = new Task("minedtask" + i, modelElements.get(i)
					.getModelElementName(), modelElements.get(i).getEventType());
			orgModel.addTask(tempTask);
		}
		LogReader log = input.getLog();

		Iterator it = log.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();
			Iterator ates = pi.getAuditTrailEntryList().iterator();

			while (ates.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ates.next();
				// Which logEvent happened?
				LogEvent le = modelElements.findLogEvent(ate.getElement(), ate
						.getType());
				Resource res = orgModel.getResource(ate.getOriginator());
				if (ate.getOriginator() == null) {
					res = orgModel.getResource("");
				}
				if (res == null) {
					if (ate.getOriginator() != null)
						res = new Resource(ate.getOriginator(), ate
								.getOriginator());
					else
						res = new Resource("", "");
				}
				// add to org model
				for (String entityID : orgModel.getOrgEntityList(res)) {
					orgModel.getTask(le).addOrgEntity(
							orgModel.getOrgEntity(entityID));

				}
			}
		}
	}

	private void buildOrgModelTreeView() {
		orgModel = new OrgModel();
		String users[] = input.getLog().getLogSummary().getOriginators();

		for (int i = 0; i < users.length; i++) {

			Resource resource = new Resource(users[i], users[i]);
			orgModel.addResource(resource);
		}

		int temp = 0;
		HashMap<String, OrgEntity> orgEntityMap = new HashMap<String, OrgEntity>();
		for (Cluster cluster : clusters.getClusters()) {
			OrgEntity tempEntity = new OrgEntity("minedRole" + temp++, cluster
					.getName(), OrgEntity.ORGENTITYTYPE_MININGRESULT);
			orgModel.addOrgEntity(tempEntity);
			orgEntityMap.put(cluster.getName(), tempEntity);
		}

		// hierarchy
		for (Cluster cluster : clusters.getClusters()) {
			OrgEntity superOrgEntity = orgEntityMap.get(cluster.getName());
			AHCTreeNode right = (AHCTreeNode) ((AHCTreeNode) cluster)
					.getRight();
			AHCTreeNode left = (AHCTreeNode) ((AHCTreeNode) cluster).getLeft();
			OrgEntity subRightOrgEntity = null;
			OrgEntity subLeftOrgEntity = null;

			subRightOrgEntity = orgEntityMap.get(right.getName());
			if (right != null && isInRange(right.getDistance())) {
				superOrgEntity.addSubEntityID(subRightOrgEntity.getID());
				subRightOrgEntity.addSuperEntityID(superOrgEntity.getID());
			}

			subLeftOrgEntity = orgEntityMap.get(left.getName());
			if (left != null && isInRange(left.getDistance())) {
				superOrgEntity.addSubEntityID(subLeftOrgEntity.getID());
				subLeftOrgEntity.addSuperEntityID(superOrgEntity.getID());
			}

			for (String user : cluster.getOriginatorList()) {
				Resource res = orgModel.getResource(user);
				if (!((isInRange(right.getDistance()) && right
						.getOriginatorList().contains(user)) || (isInRange(left
						.getDistance()) && left.getOriginatorList().contains(
						user))))
					res.addOrgEntity(superOrgEntity);
			}
		}

		LogEvents modelElements = input.getLog().getLogSummary().getLogEvents();
		for (int i = 0; i < modelElements.size(); i++) {
			Task tempTask = new Task("minedtask" + i, modelElements.get(i)
					.getModelElementName(), modelElements.get(i).getEventType());
			orgModel.addTask(tempTask);
		}
		LogReader log = input.getLog();

		Iterator it = log.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();
			Iterator ates = pi.getAuditTrailEntryList().iterator();

			while (ates.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ates.next();
				// Which logEvent happened?
				LogEvent le = modelElements.findLogEvent(ate.getElement(), ate
						.getType());
				Resource res = orgModel.getResource(ate.getOriginator());
				if (ate.getOriginator() == null) {
					res = orgModel.getResource("");
				}
				if (res == null) {
					if (ate.getOriginator() != null)
						res = new Resource(ate.getOriginator(), ate
								.getOriginator());
					else
						res = new Resource("", "");
				}
				// add to org model
				for (String entityID : orgModel.getOrgEntityList(res)) {
					orgModel.getTask(le).addOrgEntity(
							orgModel.getOrgEntity(entityID));
				}
			}
		}
	}

	private boolean isInRange(double dist) {
		boolean bResult = false;
		if (dist > lowerBoundUI && dist > lowerBound && dist <= upperBound
				&& dist <= upperBoundUI)
			bResult = true;
		return bResult;
	}

	// GUI settings
	protected void setupGui() {
		upperBound = 1.0;
		lowerBound = 0.0;
		upperBoundUI = 1.0;
		lowerBoundUI = 0.0;
		upperMaxBound = 1.0;
		cutPoint = 1.0;
		currentMethod = "";
		agProfiles = (AggregateProfile) input.getProfile();

		rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());

		rootPanel.setBorder(BorderFactory.createEmptyBorder());
		graphScrollPane = new JScrollPane(new JPanel());

		graphScrollPane.setBorder(BorderFactory.createEmptyBorder());
		graphScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		graphScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		rootPanel.add(graphScrollPane, BorderLayout.CENTER);

		JPanel emptyPanel = new JPanel();
		emptyPanel.setBackground(new Color(100, 100, 100));
		emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
		JPanel innerPanel = new JPanel();
		innerPanel.setOpaque(false);
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
		JLabel emptyLabel = new JLabel(
				"click to start clustering on the right panel");
		emptyLabel.setOpaque(false);
		innerPanel.add(Box.createHorizontalGlue());
		innerPanel.add(emptyLabel);
		innerPanel.add(Box.createHorizontalGlue());
		emptyPanel.add(Box.createVerticalGlue());
		emptyPanel.add(innerPanel);
		emptyPanel.add(Box.createVerticalGlue());

		graphScrollPane.getViewport().setView(emptyPanel);

		// slider panel for DendroGram
		// /////////////////////////////////////////////
		sliderPanel_Dendo = new JPanel();
		sliderPanel_Dendo.setOpaque(false);
		sliderPanel_Dendo
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sliderPanel_Dendo.setLayout(new BoxLayout(sliderPanel_Dendo,
				BoxLayout.X_AXIS));

		JPanel sliderCutPointUIPanel = new JPanel();
		sliderPanel_Dendo.add(sliderCutPointUIPanel);

		sliderCutPointUIPanel.setOpaque(false);
		sliderCutPointUIPanel.setLayout(new BorderLayout());
		sliderCutPointUIPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
				10, 10));
		JLabel headerLabel5 = new JLabel("Cut Point");
		headerLabel5.setBackground(bgColor);
		headerLabel5.setForeground(fgColor);
		sliderCutPointUIPanel.add(headerLabel5, BorderLayout.NORTH);
		sliderCutPointUI = new JSlider(JSlider.VERTICAL, 0, 10000, 10000);
		sliderCutPointUI.setOpaque(false);
		sliderCutPointUI.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				cutPoint = (double) sliderCutPointUI.getValue() / 10000.0
						* (upperMaxBound + upperMaxBound * 0.005);
				diameterTextField5.setText(format.format(cutPoint));
				if (sliderCutPointUI.getValueIsAdjusting() == false) {
					updateUI_cutPoint();
				}
			}
		});
		sliderCutPointUIPanel.add(sliderCutPointUI, BorderLayout.CENTER);
		diameterTextField5 = new JTextField(format.format(cutPoint));
		diameterTextField5.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				// not used
			}

			public void keyPressed(KeyEvent e) {
				// not used
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					sliderCutPointUI
							.setValue((int) (Double.valueOf(diameterTextField5
									.getText())
									/ (upperMaxBound + upperMaxBound * 0.005) * 10000.0));
			}
		});
		diameterTextField5.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterTextField5.setOpaque(false);
		diameterTextField5.setForeground(fgColor);
		sliderCutPointUIPanel.add(diameterTextField5, BorderLayout.SOUTH);

		// slider panel for TreeView
		// /////////////////////////////////////////////
		initSliderPanel();

		ArrayList<String> values = new ArrayList<String>();
		values.add(ST_Single_linkage);
		values.add(ST_Complete_linkage);
		// values.add(ST_Centroid_linkage); // blocked Centroid method. It
		// should be improved with distance measures.
		values.add(ST_Average_linkage);
		clusteringMethod = new GUIPropertyListEnumeration("Clustering Method",
				"", values, null, 150);

		ArrayList<String> values2 = new ArrayList<String>();
		values2.add(ST_DendroGram);
		values2.add(ST_Tree);
		displayMethod = new GUIPropertyListEnumeration("Display Method", "",
				values2, this, 150);

		startButton = new JButton("calculate");
		startButton.setOpaque(false);
		startButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (rootAHCTree == null
						|| !currentMethod.equals(clusteringMethod.getValue())) {
					currentMethod = (String) clusteringMethod.getValue();
					Message.add("AH clustering with "
							+ clusteringMethod.getValue() + "...",
							Message.NORMAL);
					cluster();
				}
			}
		});

		lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
		lowerPanel.setOpaque(false);
		lowerPanel.add(Box.createVerticalStrut(5));
		lowerPanel.add(clusteringMethod.getPropertyPanel());
		lowerPanel.add(Box.createVerticalStrut(5));
		lowerPanel.add(displayMethod.getPropertyPanel());
		lowerPanel.add(Box.createVerticalStrut(5));
		lowerPanel.add(startButton);

		menuPanel = new SmoothPanel();
		menuUpperPanel = new SmoothPanel();
		menuUpperPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		menuUpperPanel.setLayout(new BorderLayout());
		menuUpperPanel.add(sliderPanel_Dendo, BorderLayout.CENTER);
		menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		menuPanel.setLayout(new BorderLayout());
		menuPanel.add(menuUpperPanel, BorderLayout.CENTER);
		menuPanel.add(lowerPanel, BorderLayout.SOUTH);
		rootPanel.add(menuPanel, BorderLayout.EAST);
	}

	protected void initSliderPanel() {
		// Slider Panel for Tree ///////////////////////////////////////////////
		sliderPanel = new JPanel();
		sliderPanel.setOpaque(false);
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
		JPanel sliderUpperUIPanel = new JPanel();
		JPanel sliderLowerUIPanel = new JPanel();
		JPanel sliderUpperPanel = new JPanel();
		JPanel sliderLowerPanel = new JPanel();
		sliderPanel.add(sliderUpperUIPanel);
		sliderPanel.add(sliderLowerUIPanel);
		sliderPanel.add(sliderUpperPanel);
		sliderPanel.add(sliderLowerPanel);

		// size
		sliderUpperUIPanel.setPreferredSize(new Dimension(45, 0));
		sliderLowerUIPanel.setPreferredSize(new Dimension(45, 0));
		sliderUpperPanel.setPreferredSize(new Dimension(45, 0));
		sliderLowerPanel.setPreferredSize(new Dimension(45, 0));

		// upper bound UI panel
		sliderUpperUIPanel.setOpaque(false);
		sliderUpperUIPanel.setLayout(new BorderLayout());
		sliderUpperUIPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
				10, 10));
		JLabel headerLabel = new JLabel("UP/UI");
		headerLabel.setBackground(bgColor);
		headerLabel.setForeground(fgColor);
		sliderUpperUIPanel.add(headerLabel, BorderLayout.NORTH);
		sliderUpperUI = new JSlider(JSlider.VERTICAL, 0, 10000, 10000);
		sliderUpperUI.setOpaque(false);
		sliderUpperUI.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				upperBoundUI = (double) sliderUpperUI.getValue() / 10000.0
						* upperMaxBound;
				diameterTextField1.setText(format.format(upperBoundUI));
				if (sliderUpperUI.getValueIsAdjusting() == false) {
					updateUI();
				}
			}
		});
		sliderUpperUIPanel.add(sliderUpperUI, BorderLayout.CENTER);
		diameterTextField1 = new JTextField(format.format(upperBoundUI));
		diameterTextField1.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				// not used
			}

			public void keyPressed(KeyEvent e) {
				// not used
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					sliderUpperUI.setValue((int) (Double
							.valueOf(diameterTextField1.getText())
							/ upperMaxBound * 10000.0));
			}
		});
		diameterTextField1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterTextField1.setOpaque(false);
		diameterTextField1.setForeground(fgColor);
		sliderUpperUIPanel.add(diameterTextField1, BorderLayout.SOUTH);

		// lower bound UI panel
		sliderLowerUIPanel.setOpaque(false);
		sliderLowerUIPanel.setLayout(new BorderLayout());
		sliderLowerUIPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
				10, 10));
		JLabel headerLabel2 = new JLabel("LW/UI");
		headerLabel2.setOpaque(false);
		headerLabel2.setForeground(fgColor);
		sliderLowerUIPanel.add(headerLabel2, BorderLayout.NORTH);

		sliderLowerUI = new JSlider(JSlider.VERTICAL, 0, 10000,
				(int) (lowerBoundUI / upperBoundUI * 10000));
		sliderLowerUI.setOpaque(false);
		sliderLowerUI.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				lowerBoundUI = (double) sliderLowerUI.getValue() / 10000.0
						* upperMaxBound;
				diameterTextField2.setText(format.format(lowerBoundUI));
				if (sliderLowerUI.getValueIsAdjusting() == false) {
					updateUI();
				}
			}
		});
		sliderLowerUIPanel.add(sliderLowerUI, BorderLayout.CENTER);
		diameterTextField2 = new JTextField(format.format(lowerBoundUI));
		diameterTextField2.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				// not used
			}

			public void keyPressed(KeyEvent e) {
				// not used
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					sliderLowerUI.setValue((int) (Double
							.valueOf(diameterTextField2.getText())
							/ upperMaxBound * 10000.0));
			}
		});
		diameterTextField2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterTextField2.setOpaque(false);
		diameterTextField2.setForeground(fgColor);
		sliderLowerUIPanel.add(diameterTextField2, BorderLayout.SOUTH);

		// upper bound panel
		sliderUpperPanel.setOpaque(false);
		sliderUpperPanel.setLayout(new BorderLayout());
		sliderUpperPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));
		JLabel headerLabel3 = new JLabel("UP/CL");
		headerLabel3.setOpaque(false);
		headerLabel3.setForeground(fgColor);
		sliderUpperPanel.add(headerLabel3, BorderLayout.NORTH);
		sliderUpper = new JSlider(JSlider.VERTICAL, 0, 10000, 10000);
		sliderUpper.setOpaque(false);
		sliderUpper.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				upperBound = (double) sliderUpper.getValue() / 10000.0
						* upperMaxBound;
				diameterTextField3.setText(format.format(upperBound));
				if (sliderUpper.getValueIsAdjusting() == false) {
					updateUI();
				}
			}
		});
		sliderUpperPanel.add(sliderUpper, BorderLayout.CENTER);
		diameterTextField3 = new JTextField(format.format(upperBound));
		diameterTextField3.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				// not used
			}

			public void keyPressed(KeyEvent e) {
				// not used
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					sliderUpper.setValue((int) (Double
							.valueOf(diameterTextField3.getText())
							/ upperMaxBound * 10000.0));
			}
		});
		diameterTextField3.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterTextField3.setOpaque(false);
		diameterTextField3.setForeground(fgColor);
		sliderUpperPanel.add(diameterTextField3, BorderLayout.SOUTH);

		// lower bound panel
		sliderLowerPanel.setOpaque(false);
		sliderLowerPanel.setLayout(new BorderLayout());
		sliderLowerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));
		JLabel headerLabel4 = new JLabel("LW/CL");
		headerLabel4.setOpaque(false);
		headerLabel4.setForeground(fgColor);
		sliderLowerPanel.add(headerLabel4, BorderLayout.NORTH);

		sliderLower = new JSlider(JSlider.VERTICAL, 0, 10000, (int) (lowerBound
				/ upperBoundUI * 10000));
		sliderLower.setOpaque(false);
		sliderLower.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				lowerBound = (double) sliderLower.getValue() / 10000.0
						* upperMaxBound;
				diameterTextField4.setText(format.format(lowerBound));
				if (sliderLower.getValueIsAdjusting() == false) {
					updateUI();
				}
			}
		});
		sliderLowerPanel.add(sliderLower, BorderLayout.CENTER);
		diameterTextField4 = new JTextField(format.format(lowerBound));
		diameterTextField4.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				// not used
			}

			public void keyPressed(KeyEvent e) {
				// not used
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					sliderLower.setValue((int) (Double
							.valueOf(diameterTextField4.getText())
							/ upperMaxBound * 10000.0));
			}
		});
		diameterTextField4.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterTextField4.setOpaque(false);
		diameterTextField4.setForeground(fgColor);
		sliderLowerPanel.add(diameterTextField4, BorderLayout.SOUTH);
	}

	protected void updateSliders() {
		bDrawing = false;
		sliderCutPointUI.setValue(10000);
		cutPoint = upperMaxBound + upperMaxBound * 0.005;
		diameterTextField5.setText(format.format(cutPoint));

		sliderUpperUI.setValue(10000);
		diameterTextField1.setText(format.format(upperMaxBound));
		upperBoundUI = upperMaxBound;

		sliderUpper.setValue(10000);
		diameterTextField3.setText(format.format(upperMaxBound));
		upperBound = upperMaxBound;

		sliderLowerUI.setValue(0);
		diameterTextField2.setText(format.format(0));
		lowerBoundUI = 0;

		sliderLower.setValue(0);
		diameterTextField4.setText(format.format(0));
		lowerBound = 0;

		bDrawing = true;
	}

	protected void updateUI() {
		if (rootAHCTree != null && bDrawing) {
			try {
				if (displayMethod.getValue().equals(ST_DendroGram)) {
					dendrogram = new DendroGramUI(rootAHCTree, cutPoint);
					graphScrollPane.getViewport().setView(dendrogram);
					buildOrgModelDendrogram();
				} else {
					graphScrollPane.getViewport().setView(getGraphPanel());
					buildOrgModelTreeView();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void updateUI_cutPoint() {
		if (rootAHCTree != null && bDrawing) {
			try {
				dendrogram = new DendroGramUI(rootAHCTree, cutPoint);
				graphScrollPane.getViewport().setView(dendrogram);
				makeClustersForDendrogram();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		buildOrgModelDendrogram();
	}

	private void makeClustersForDendrogram() {
		clusterCounter = 0;
		clusters.clear();
		findCluster(rootAHCTree);
	}

	private void findCluster(AHCTreeNode temp) {
		if (temp.getDistance() < cutPoint)
			return;
		if (temp.getLeft() == null)
			return;
		if (((AHCTreeNode) temp.getLeft()).getDistance() < cutPoint) {
			temp.getLeft().setName("OrgUnit " + clusterCounter++);
			clusters.addCluster(temp.getLeft());
		} else
			findCluster((AHCTreeNode) temp.getLeft());

		if (temp.getRight() == null)
			return;
		if (((AHCTreeNode) temp.getRight()).getDistance() < cutPoint) {
			temp.getRight().setName("OrgUnit " + clusterCounter++);
			clusters.addCluster(temp.getRight());
		} else
			findCluster((AHCTreeNode) temp.getRight());
	}

	public OrgModel getOrgModel() {
		return orgModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.hierachicalorgminer.algorithm.ClusteringAlgorithm
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
	 * org.processmining.mining.hierachicalorgminer.algorithm.ClusteringAlgorithm
	 * #getUI()
	 */
	@Override
	public JComponent getUI() {
		rootAHCTree = null;
		sliderPanel = null;
		setupGui();
		return rootPanel;
	}

	public void writeToDotEach(Writer bw, AHCTreeNode temp) throws IOException {
		if (temp.getDistance() <= lowerBoundUI)
			return;

		temp.setName("OrgUnit " + clusterCounter++);

		String color = null;
		double dist = temp.getDistance();
		if (dist <= lowerBound || dist > upperBound || dist > upperBoundUI)
			color = "gray86";
		else {
			color = "chartreuse3";
			clusters.addCluster(temp);
		}

		if (dist <= upperBoundUI)
			bw.write(temp.getName().replace(" ", "")
					+ " [shape=\"octagon\",style=\"filled\",fillcolor=\""
					+ color + "\",label=\"" + temp.getName() + "\\n"
					+ temp.size() + "\\n" + temp.getDistance() + "\"];\n");

		if (temp.getLeft() != null)
			writeToDotEach(bw, (AHCTreeNode) temp.getLeft());
		if (temp.getRight() != null)
			writeToDotEach(bw, (AHCTreeNode) temp.getRight());
	}

	public void writeToDotEachArc(Writer bw, AHCTreeNode temp)
			throws IOException {
		if (temp.getLeft() != null) {
			if (((AHCTreeNode) temp.getLeft()).getDistance() > lowerBoundUI
					&& ((AHCTreeNode) temp.getLeft()).getDistance() <= upperBoundUI) {
				if (temp.getDistance() <= upperBoundUI)
					bw.write(temp.getName().replace(" ", "")
							+ " -- "
							+ ((AHCTreeNode) temp.getLeft()).getName().replace(
									" ", "") + " [label=\""
							+ temp.getDistance() + "\"];\n");
				writeToDotEachArc(bw, (AHCTreeNode) temp.getLeft());
			}
		}
		if (temp.getRight() != null) {
			if (((AHCTreeNode) temp.getRight()).getDistance() > lowerBoundUI
					&& ((AHCTreeNode) temp.getRight()).getDistance() <= upperBoundUI) {
				if (temp.getDistance() <= upperBoundUI)
					bw.write(temp.getName().replace(" ", "")
							+ " -- "
							+ ((AHCTreeNode) temp.getRight()).getName()
									.replace(" ", "") + " [label=\""
							+ temp.getDistance() + "\"];\n");
				writeToDotEachArc(bw, (AHCTreeNode) temp.getRight());
			}
		}
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
		clusterCounter = 0;
		clusters.clear();
		writeToDotEach(bw, rootAHCTree);
		writeToDotEachArc(bw, rootAHCTree);
		// close graph
		bw.write("}\n");
	}

	public JPanel getGraphPanel() throws Exception {
		File dotFile = File.createTempFile("pmt", ".dot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(dotFile, false));
		writeToDot(bw);
		bw.close();
		Graph graph = Dot.execute(dotFile.getAbsolutePath());
		dotFile.deleteOnExit();
		graph.setEditable(true);
		graph.setMenuable(true);
		graph.setErrorWriter(new PrintWriter(System.err, true));
		GrappaPanel gp = new GrappaPanel(graph);
		gp.setScaleToFit(true);
		gp.setBackgroundColor(new Color(100, 100, 100));
		return gp;
	}

	public void updateGUI() {
		if (displayMethod.getValue().equals(ST_DendroGram)) {
			menuUpperPanel.removeAll();
			menuUpperPanel.add(sliderPanel_Dendo);
			sliderPanel_Dendo.updateUI();
			if (rootAHCTree != null) {
				dendrogram = new DendroGramUI(rootAHCTree, cutPoint);
				graphScrollPane.getViewport().setView(dendrogram);
				makeClustersForDendrogram();
			}
		} else {
			menuUpperPanel.removeAll();
			menuUpperPanel.add(sliderPanel);
			menuUpperPanel.repaint();
			sliderPanel.updateUI();
			if (rootAHCTree != null) {
				try {
					graphScrollPane.getViewport().setView(getGraphPanel());
					buildOrgModelTreeView();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
