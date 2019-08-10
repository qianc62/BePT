/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

/*
 * Copyright (c) 2006 Eindhoven University of Technology
 * All rights reserved.
 */

package org.processmining.analysis.originator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;
import org.processmining.framework.models.orgmodel.algorithms.OmmlReader;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Utils;
import org.processmining.framework.ui.filters.GenericMultipleExtFilter;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GUIPropertyString;
import org.processmining.framework.util.GuiPropertyStringList;
import org.processmining.mining.snamining.SocialNetworkResults;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

/**
 * Displays the analysis settings frame that precedes the actual analysis. The
 * user can define group and relationship between group and nodes here.
 * 
 * @see GroupingSNAPlugin
 * 
 * @author Minseok Song
 */

public class GroupingSNASettings extends JPanel {

	// final attributes
	final GroupingSNAPlugin myAlgorithm;
	final SocialNetworkMatrix mySnMatrix;
	final AnalysisInputItem[] myInput;

	private GuiPropertyStringList groupNames;
	private ArrayList<String> groupNameforResult; // used to make reslut matrix
	private UiNodesToGroup[] uiNodesToGroup;
	private OrgModel orgModel;

	// GUI related attributes
	private JPanel groupPane = new JPanel();
	private JButton relationGroupButton = new JButton("Make Relationship");

	private JPanel filePane = new JPanel();
	private JLabel filenameLabel = new JLabel();
	private JTextField orgModelFile = new JTextField();
	private JButton chooseOrgModelButton = new JButton();
	private JButton relationFileButton = new JButton("Show Relationship");
	private GUIPropertyListEnumeration selectRoleUnit;
	private JScrollPane rightPane = new JScrollPane();

	private JTabbedPane tabPane = new JTabbedPane();
	private JSplitPane splitPane;

	// lower panel and buttons
	private JPanel buttonsPanel = new JPanel(new BorderLayout()); // lower panel
	// containing
	// the
	// buttons
	private JButton startButton = new JButton(new GroupingSNAAction(this));
	private JButton docsButton = new JButton("Plugin documentation..."); // shows

	// the
	// plugin
	// documentation

	public GroupingSNASettings(GroupingSNAPlugin algorithm,
			AnalysisInputItem[] input, SocialNetworkMatrix snMatrix) {

		myAlgorithm = algorithm;
		mySnMatrix = snMatrix;
		myInput = input;
		uiNodesToGroup = new UiNodesToGroup[mySnMatrix.getNodeNames().length];

		groupNames = new GuiPropertyStringList("Group Name",
				new ArrayList<String>());

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

		// build the GUI
		initGroupPane();
		initFilePane();

		// add tabPanes
		tabPane.add(groupPane, "OrgEntity");
		tabPane.add(filePane, "Org. Model");
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPane,
				rightPane);

		// lower button panel
		buttonsPanel.add(startButton, BorderLayout.EAST);
		buttonsPanel.add(docsButton, BorderLayout.WEST);
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		// pack
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.SOUTH);
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.validate();
		this.repaint();
	}

	private void initGroupPane() {

		groupPane.setLayout(new BorderLayout());
		groupPane.add(groupNames.getPropertyPanel(), BorderLayout.CENTER);
		groupPane.add(relationGroupButton, BorderLayout.SOUTH);
		relationGroupButton.addActionListener((new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makeRelationship();
			}
		}));
		drawRightPane();
	}

	private void drawRightPane() {
		// draw upper right panel
		JPanel rightInnerPane = new JPanel();
		JPanel imsiPane = new JPanel();
		imsiPane.setLayout(new GridLayout(0, 4));
		for (int i = 0; i < mySnMatrix.getNodeNames().length; i++) {
			uiNodesToGroup[i] = new UiNodesToGroup(new GUIPropertyString(
					"name", mySnMatrix.getNodeNames()[i]),
					new GUIPropertyListEnumeration("group", groupNames
							.getAllValues()));

			imsiPane.add(uiNodesToGroup[i].getNodeName().getPropertyPanel());
			imsiPane.add(uiNodesToGroup[i].getGroup().getPropertyPanel());
		}
		rightInnerPane.add(imsiPane);
		rightPane = new JScrollPane(rightInnerPane);
		rightPane.setBorder(BorderFactory.createLineBorder(new Color(150, 150,
				150), 1));
	}

	private void initFilePane() {
		JPanel filePanel = new JPanel();

		filenameLabel.setText("Org Model file:");
		orgModelFile.setMinimumSize(new Dimension(150, 21));
		orgModelFile.setPreferredSize(new Dimension(200, 21));
		orgModelFile.setEditable(false);

		chooseOrgModelButton.setText("Browse...");
		chooseOrgModelButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						chooseOrgModelButton_actionPerformed(e);
					}
				});

		selectRoleUnit = new GUIPropertyListEnumeration("Grouping property",
				new ArrayList(Arrays.asList(new String[] { "role", "org unit",
						"mined group" })));

		filePanel.setLayout(new GridLayout(0, 1));
		filePanel.add(filenameLabel);
		filePanel.add(orgModelFile);
		filePanel.add(chooseOrgModelButton);
		filePanel.add(selectRoleUnit.getPropertyPanel());
		filePanel.add(relationFileButton);
		filePane.add(filePanel, BorderLayout.CENTER);

		relationFileButton.addActionListener((new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makeRelationship2();
			}
		}));
	}

	void chooseOrgModelButton_actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();

		chooser.setFileFilter(new GenericMultipleExtFilter(
				new String[] { "xml" }, "XML file (*.xml)"));
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getPath();
			setChosenXMLFile(name);
		}
	}

	private void setChosenXMLFile(String logFileName) {
		orgModelFile.setText(logFileName);
	}

	public String getOrgModelFileName() {
		return orgModelFile.getText();
	}

	private void makeRelationship() {
		groupNameforResult = null;
		groupNameforResult = (ArrayList<String>) groupNames.getAllValues();
		rightPane = null;

		drawRightPane();

		splitPane.setRightComponent(rightPane);
		splitPane.validate();
		splitPane.repaint();
	}

	// resource --> originator
	private void makeRelationship2() {
		rightPane = null;

		JPanel rightInnerPane = new JPanel();
		JPanel imsiPane = new JPanel();
		imsiPane.setLayout(new GridLayout(0, 4));

		String selected = selectRoleUnit.getValue().toString();

		orgModel = OmmlReader.read(orgModelFile.getText());

		if (selected.equals("role"))
			groupNameforResult = orgModel
					.getOrgEntityStringList(OrgEntity.ORGENTITYTYPE_ROLE);
		else if (selected.equals("mined group"))
			groupNameforResult = orgModel
					.getOrgEntityStringList(OrgEntity.ORGENTITYTYPE_MININGRESULT);
		else
			groupNameforResult = orgModel
					.getOrgEntityStringList(OrgEntity.ORGENTITYTYPE_ORGUNIT);

		for (int i = 0; i < mySnMatrix.getNodeNames().length; i++) {
			Resource res = orgModel.getResource(mySnMatrix.getNodeNames()[i]);

			if (selected.equals("role")) {
				uiNodesToGroup[i] = new UiNodesToGroup(new GUIPropertyString(
						"name", mySnMatrix.getNodeNames()[i]),
						new GUIPropertyListEnumeration("group", orgModel
								.getOrgEntityList(res,
										OrgEntity.ORGENTITYTYPE_ROLE)));
			} else if (selected.equals("mined group")) {
				uiNodesToGroup[i] = new UiNodesToGroup(new GUIPropertyString(
						"name", mySnMatrix.getNodeNames()[i]),
						new GUIPropertyListEnumeration("group", orgModel
								.getOrgEntityList(res,
										OrgEntity.ORGENTITYTYPE_MININGRESULT)));
			} else {
				uiNodesToGroup[i] = new UiNodesToGroup(new GUIPropertyString(
						"name", mySnMatrix.getNodeNames()[i]),
						new GUIPropertyListEnumeration("group", orgModel
								.getOrgEntityList(res,
										OrgEntity.ORGENTITYTYPE_ORGUNIT)));
			}
			imsiPane.add(uiNodesToGroup[i].getNodeName().getPropertyPanel());
			imsiPane.add(uiNodesToGroup[i].getGroup().getPropertyPanel());

		}

		rightInnerPane.add(imsiPane);
		rightPane = new JScrollPane(rightInnerPane);
		rightPane.setBorder(BorderFactory.createLineBorder(new Color(150, 150,
				150), 1));

		splitPane.setRightComponent(rightPane);
		splitPane.validate();
		splitPane.repaint();
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
	}

	/**
	 * Creates a LogReplayAnalysisMethod-object, using the input log, the input
	 * Petri net and a newly created PerformanceMeasurer-object, and afterwards
	 * calls on the method buildResultsFrame() to create the PerformanceGUI
	 */
	public void startAnalysis() {

		int size = mySnMatrix.getNodeNames().length;
		String[] assignment = new String[size];
		if (uiNodesToGroup[0].getGroup().getValue() != null) {
			for (int i = 0; i < size; i++) {
				assignment[i] = uiNodesToGroup[i].getGroup().getValue()
						.toString();
			}

			// spawn result window
			MainUI.getInstance().createAnalysisResultFrame(
					myAlgorithm,
					myInput,
					new SocialNetworkResults(null, mySnMatrix.groupOriginators(
							groupNameforResult, assignment), null));
		}
	}

	class GroupingSNAAction extends AbstractAction {
		/**
		 * field containing the corresponding performance analysis settings
		 */
		private GroupingSNASettings settings;

		/**
		 * Builds this listener object.
		 * 
		 * @param frame
		 *            the calling settings frame containing the referenced
		 *            objects needed to carry out the analysis
		 */
		public GroupingSNAAction(GroupingSNASettings frame) {
			super("<html><B>Start analysis<html>", Utils
					.getStandardIcon("media/Play24"));
			putValue(SHORT_DESCRIPTION, "Start analysis");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
			settings = frame;
		}

		public void actionPerformed(ActionEvent e) {
			settings.startAnalysis();
		}

	}

	class UiNodesToGroup {
		private GUIPropertyString nodename = null;
		private GUIPropertyListEnumeration assignedGroup = null;

		public UiNodesToGroup(GUIPropertyString nodename,
				GUIPropertyListEnumeration group) {
			this.nodename = nodename;
			this.assignedGroup = group;
		}

		public GUIPropertyString getNodeName() {
			return nodename;
		}

		public GUIPropertyListEnumeration getGroup() {
			return assignedGroup;
		}
	}

}
