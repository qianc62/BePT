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
 * $Archive: /ProcessMining/src/org/processmining/analysis/petrinet/WoflanAnalysisUI.java $
 * Last changed by: $Author: Bfvdonge $
 * Last changed at: $Date: 3-02-06 11:45 $
 * Revision number: $Revision: 8 $
 * $NoKeywords: $
 *
 * Copyright (c) 2005 Eindhoven Technical University of Technology
 * All rights reserved.
 */
package org.processmining.analysis.petrinet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.processmining.framework.models.Bag;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.TPNWriter;
import org.processmining.framework.models.petrinet.algorithms.Woflan;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;

import att.grappa.Edge;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * <p>
 * Title: WoflanAnalysisUI
 * </p>
 * <p>
 * Description: Woflan Analysis Plugin User Interface
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005 Eric Verbeek
 * </p>
 * <p>
 * Company: Technische Universiteit Eindhoven
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class WoflanAnalysisUI extends JPanel implements Provider,
		TreeSelectionListener {

	private PetriNet net;
	private DefaultMutableTreeNode root; // Holds the root node in the tree with
	// analysis results.
	private JTree treeContainer; // Holds the tree with analysis results.
	private JScrollPane netContainer; // Holds the net and visualizes selected
	// analysis results.
	private JPanel containersPanel; // Holds both containers
	private JButton nextButton; // Holds the Next button
	private JButton finishButton; // Holds the Finish button
	private JPanel buttonsPanel = new JPanel(); // Holds all buttons

	private DefaultMutableTreeNode netProperties; // Holds the Net Properties
	// node.
	private DefaultMutableTreeNode netDiagnosis; // Holds the Net Diagnosis
	// node.

	private Woflan woflan = new Woflan();
	private int woflanNet;

	private int isSound = 0; // 0 == unknown, 1 == no, 2 == yes.
	private int isBounded = 0;
	private int isLive = 0;
	private int isWFNet = 0;
	private int isSCoverable = 0;
	private int isPICoverable = 0;
	private int isNonDead = 0;

	private JButton nameButton;

	private StateSpace reachability = null;
	private StateSpace coverability = null;
	private ModelGraphPanel gp;
	private HashMap mapping;
	private boolean invariantsDone = false;
	private ArrayList tinv;
	private ArrayList pinv;
	private int currentTInv;

	/**
	 * Creates a GUI for the Woflan analysis.
	 * 
	 * @param net
	 *            The Petri net that is to be analysed by Woflan
	 */
	public WoflanAnalysisUI(PetriNet net) {
		this.net = net;

		root = new DefaultMutableTreeNode("Woflan");
		treeContainer = new JTree(root);
		treeContainer.addTreeSelectionListener(this);
		containersPanel = new JPanel();
		nextButton = new JButton("Next");
		finishButton = new JButton("Finish");
		buttonsPanel = new JPanel();

		netProperties = new DefaultMutableTreeNode("Net Properties");
		netDiagnosis = new DefaultMutableTreeNode("Net Diagnosis");
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Creates a GUI for the Woflan analysis.
	 * 
	 * @throws java.lang.Exception
	 */
	void jbInit() throws Exception {

		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buildNetDiagnosis(1);
				// Update the visible tree accordingly
				treeContainer.updateUI();
			}
		});

		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buildNetDiagnosis(100);
				// Update the visible tree accordingly
				treeContainer.updateUI();

				Message.add("<WoflanAnalysis>", Message.TEST);
				Enumeration dfe = root.depthFirstEnumeration();
				while (dfe.hasMoreElements()) {
					Object object = dfe.nextElement();
					String text = object.toString();
					if (text.startsWith("Place ")
							|| text.startsWith("Transition ")) {
						// nothing
					} else {
						Message.add(text, Message.TEST);
					}
				}
				Message.add("</WoflanAnalysis>", Message.TEST);
			}
		});

		try {
			// Don't know how to get access to the filename of the net.
			// Write the net to a temporary tpn file.
			File tpnFile = File.createTempFile("woflan", ".tpn");
			tpnFile.deleteOnExit();
			BufferedWriter bw = new BufferedWriter(new FileWriter(tpnFile,
					false));
			String export = TPNWriter.write(net);
			bw.write(export);
			bw.close();

			// Have Woflan open the temporary file, if possible.
			woflanNet = woflan.Open(tpnFile.getAbsolutePath());

			buildNetProperties();

			validate();
			repaint();
		} catch (Exception ex) {
			System.err
					.println("Error while running Woflan: " + ex.getMessage());
		}
		if (woflanNet != 0) {
			buttonsPanel.add(nextButton);
			buttonsPanel.add(finishButton);
		}

		gp = net.getGrappaVisualization();
		mapping = new HashMap();
		buildGraphMapping(mapping, gp.getSubgraph());
		netContainer = new JScrollPane(gp);

		containersPanel.setLayout(new GridLayout(1, 1));
		containersPanel.add(netContainer);
		containersPanel.add(new JScrollPane(treeContainer));

		this.setLayout(new BorderLayout());
		this.add(containersPanel, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.SOUTH);
	}

	private String getName(String name) {
		// String can either be "place_99" or "t_99", see TPNWriter.
		int n;
		if (name.startsWith("place_")) {
			n = Integer.parseInt(name.substring(6));
		} else if (name.startsWith("t_")) {
			n = Integer.parseInt(name.substring(2));
		} else {
			return name;
		}
		if (n < net.getPlaces().size()) {
			Place p = (Place) net.getPlaces().get(n);
			return "Place " + p.getIdentifier();
		}
		n -= net.getPlaces().size();
		n--;
		if (n < net.getTransitions().size()) {
			Transition t = (Transition) net.getTransitions().get(n);
			return "Transition " + t.getIdentifier();
		}
		return name;
	}

	private Place getPlace(String name) {
		int n;
		for (n = 0; n < net.getPlaces().size(); n++) {
			Place p = (Place) net.getPlaces().get(n);
			if (p.getIdentifier().equals(name)) {
				return p;
			}
		}
		return null;
	}

	private Transition getTransition(String name) {
		int n;
		for (n = 0; n < net.getTransitions().size(); n++) {
			Transition t = (Transition) net.getTransitions().get(n);
			if (t.getIdentifier().equals(name)) {
				return t;
			}
		}
		return null;
	}

	private void getBasicNetProperty(int info, int subinfo, String label,
			DefaultMutableTreeNode parent) {
		int n, i;
		DefaultMutableTreeNode node;

		n = Integer.parseInt(woflan.Info(woflanNet, info, 0, 0));
		node = new DefaultMutableTreeNode(label + " [" + n + "]");
		parent.add(node);
		for (i = 0; i < n; i++) {
			node.add(new DefaultMutableTreeNode(getName(woflan.Info(woflanNet,
					subinfo, i, 0))));
		}
	}

	/**
	 * Create the "Net Properties" section in the Analysis tree.
	 * 
	 * @param woflan
	 *            Handle to Woflan itself.
	 * @param woflanNet
	 *            The reference to the Petri net at hand.
	 */
	private void buildNetProperties() {
		int nofP, nofT, i;
		String s;

		root.add(netProperties);
		getBasicNetProperty(woflan.InfoNofP, woflan.InfoPName, "Places",
				netProperties);
		getBasicNetProperty(woflan.InfoNofT, woflan.InfoTName, "Transitions",
				netProperties);
	}

	private void buildWFNetDiagnosis() {
		int nofSrcP, nofSnkP, nofSrcT, nofSnkT, nofUncN, nofSncN;
		DefaultMutableTreeNode step;
		nofSrcP = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofSrcP,
				0, 0));
		nofSnkP = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofSnkP,
				0, 0));
		nofSrcT = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofSrcT,
				0, 0));
		nofSnkT = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofSnkT,
				0, 0));
		woflan.Info(woflanNet, woflan.SetSUnc, 0, 0);
		nofUncN = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofUncN,
				0, 0));
		nofSncN = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofSncN,
				0, 0));
		if (nofSrcP == 1 && nofSnkP == 1 && nofSrcT == 0 && nofSnkT == 0
				&& nofUncN == 0 && nofSncN == 0) {
			isWFNet = 2;
			step = new DefaultMutableTreeNode("WF-net: Passed");
			netDiagnosis.add(step);
		} else {
			isWFNet = 1;
			step = new DefaultMutableTreeNode("WF-net: Failed");
			netDiagnosis.add(step);
			if (nofSrcP != 1) {
				getBasicNetProperty(woflan.InfoNofSrcP, woflan.InfoSrcPName,
						"Source places", step);
			}
			if (nofSnkP != 1) {
				getBasicNetProperty(woflan.InfoNofSnkP, woflan.InfoSnkPName,
						"Sink places", step);
			}
			if (nofSrcT != 0) {
				getBasicNetProperty(woflan.InfoNofSrcT, woflan.InfoSrcTName,
						"Source transitions", step);
			}
			if (nofSnkT != 0) {
				getBasicNetProperty(woflan.InfoNofSnkT, woflan.InfoSnkTName,
						"Sink transitions", step);
			}
			if (nofUncN != 0) {
				getBasicNetProperty(woflan.InfoNofUncN, woflan.InfoUncNName,
						"Unconnected nodes", step);
			}
			if (nofSncN != 0) {
				getBasicNetProperty(woflan.InfoNofSncN, woflan.InfoSncNName,
						"Strongly unconnected nodes", step);
			}
		}
	}

	private void buildSCoverableDiagnosis() {
		int nof, nofNot, nofN;
		DefaultMutableTreeNode step;
		woflan.Info(woflanNet, woflan.SetSCom, 0, 0);
		nofNot = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofNotSCom,
				0, 0));
		if (nofNot == 0) {
			isSCoverable = 2;
			step = new DefaultMutableTreeNode("S-coverability: Passed");
			netDiagnosis.add(step);
		} else {
			isSCoverable = 1;
			step = new DefaultMutableTreeNode("S-coverability: Failed");
			netDiagnosis.add(step);
			getBasicNetProperty(woflan.InfoNofNotSCom, woflan.InfoNotSComNName,
					"Uncovered nodes", step);
			DefaultMutableTreeNode node;
			int i, j;
			nof = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofSCom,
					0, 0));
			for (i = 0; i < nof; i++) {
				nofN = Integer.parseInt(woflan.Info(woflanNet,
						woflan.InfoSComNofN, i, 0));
				node = new DefaultMutableTreeNode("S-component " + i + " ["
						+ nofN + "]");
				step.add(node);
				for (j = 0; j < nofN; j++) {
					node.add(new DefaultMutableTreeNode(getName(woflan.Info(
							woflanNet, woflan.InfoSComNName, i, j))));
				}
			}
			int nofC, nofPT, nofTP;
			woflan.Info(woflanNet, woflan.SetNFCC, 0, 0);
			woflan.Info(woflanNet, woflan.SetPTH, 0, 0);
			woflan.Info(woflanNet, woflan.SetTPH, 0, 0);
			nofC = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofNFCC,
					0, 0));
			nofPT = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofPTH,
					0, 0));
			nofTP = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofTPH,
					0, 0));
			if (nofC == 0 || (nofPT == 0 && nofTP == 0)) {
				if (nofC == 0) {
					step.add(new DefaultMutableTreeNode("Free-choice: Passed"));
				}
				if (nofPT == 0 && nofTP == 0) {
					step
							.add(new DefaultMutableTreeNode(
									"Well-handled: Passed"));
				}
				step
						.add(new DefaultMutableTreeNode(
								"Soundness: About to fail"));
			}
		}
	}

	private void buildPIDiagnosis() {
		int nof, nofNot, nofN;
		DefaultMutableTreeNode step;
		woflan.Info(woflanNet, woflan.SetSPIn, 0, 0);
		nofNot = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofNotSPIn,
				0, 0));
		if (nofNot == 0) {
			isPICoverable = 2;
			step = new DefaultMutableTreeNode("PI-coverability: Passed");
			netDiagnosis.add(step);
		} else {
			isPICoverable = 1;
			step = new DefaultMutableTreeNode("PI-coverability: Failed");
			netDiagnosis.add(step);
			getBasicNetProperty(woflan.InfoNofNotSPIn, woflan.InfoNotSPInPName,
					"Uncovered nodes", step);
		}
		DefaultMutableTreeNode node, node2;
		int i, j;
		nof = Integer
				.parseInt(woflan.Info(woflanNet, woflan.InfoNofSPIn, 0, 0));
		for (i = 0; i < nof; i++) {
			nofN = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoSPInNofP,
					i, 0));
			node = new DefaultMutableTreeNode("P-invariant " + i + " [" + nofN
					+ "]");
			step.add(node);
			for (j = 0; j < nofN; j++) {
				node2 = new DefaultMutableTreeNode(getName(woflan.Info(
						woflanNet, woflan.InfoSPInPName, i, j)));
				node.add(node2);
				node2
						.add(new DefaultMutableTreeNode("Weight "
								+ woflan.Info(woflanNet,
										woflan.InfoSPInPWeight, i, j)));
			}
		}
	}

	private void buildClusterDiagnosis(DefaultMutableTreeNode parent) {
		int nofC, nofN;
		int i, j;
		DefaultMutableTreeNode node1, node2;
		woflan.Info(woflanNet, woflan.SetNFCC, 0, 0);
		nofC = Integer.parseInt(woflan
				.Info(woflanNet, woflan.InfoNofNFCC, 0, 0));
		node1 = new DefaultMutableTreeNode("Non-free-choice clusters [" + nofC
				+ "]");
		parent.add(node1);
		for (i = 0; i < nofC; i++) {
			nofN = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNFCCNofN,
					i, 0));
			node2 = new DefaultMutableTreeNode("Non-free-choice cluster" + i
					+ " [" + nofN + "]");
			node1.add(node2);
			for (j = 0; j < nofN; j++) {
				node2.add(new DefaultMutableTreeNode(getName(woflan.Info(
						woflanNet, woflan.InfoNFCCNName, i, j))));
			}
		}
	}

	private void buildHandleDiagnosis(boolean isPT,
			DefaultMutableTreeNode parent) {
		int set, infoNof, infoNofN1, infoNofN2, infoN1Name, infoN2Name;
		int i, j, nof, nof1, nof2;
		String s;
		DefaultMutableTreeNode node1, node2, node3;

		if (isPT) {
			set = woflan.SetPTH;
			infoNof = woflan.InfoNofPTH;
			infoNofN1 = woflan.InfoPTHNofN1;
			infoNofN2 = woflan.InfoPTHNofN2;
			infoN1Name = woflan.InfoPTHN1Name;
			infoN2Name = woflan.InfoPTHN2Name;
			s = "PT";
		} else {
			set = woflan.SetTPH;
			infoNof = woflan.InfoNofTPH;
			infoNofN1 = woflan.InfoTPHNofN1;
			infoNofN2 = woflan.InfoTPHNofN2;
			infoN1Name = woflan.InfoTPHN1Name;
			infoN2Name = woflan.InfoTPHN2Name;
			s = "TP";
		}
		woflan.Info(woflanNet, set, 0, 0);
		nof = Integer.parseInt(woflan.Info(woflanNet, infoNof, 0, 0));
		node1 = new DefaultMutableTreeNode(s + "-handles [" + nof + "]");
		parent.add(node1);
		for (i = 0; i < nof; i++) {
			nof1 = Integer.parseInt(woflan.Info(woflanNet, infoNofN1, i, 0));
			node2 = new DefaultMutableTreeNode(s + "-handle " + i + ": "
					+ getName(woflan.Info(woflanNet, infoN1Name, i, 0)) + " - "
					+ getName(woflan.Info(woflanNet, infoN1Name, i, nof1 - 1)));
			node1.add(node2);
			node3 = new DefaultMutableTreeNode("Path 1 [" + nof1 + "]");
			node2.add(node3);
			for (j = 0; j < nof1; j++) {
				node3.add(new DefaultMutableTreeNode(getName(woflan.Info(
						woflanNet, infoN1Name, i, j))));
			}
			nof2 = Integer.parseInt(woflan.Info(woflanNet, infoNofN2, i, 0));
			node3 = new DefaultMutableTreeNode("Path 2 [" + nof2 + "]");
			node2.add(node3);
			for (j = 0; j < nof2; j++) {
				node3.add(new DefaultMutableTreeNode(getName(woflan.Info(
						woflanNet, infoN2Name, i, j))));
			}
		}
	}

	private void buildSequenceDiagnosis(boolean isUnbounded,
			DefaultMutableTreeNode parent) {
		int nofSeq, nofT;
		int infoNofS, infoNofT, infoName;
		int i, j;
		String s;

		if (isUnbounded) {
			infoNofS = woflan.InfoNofUnbS;
			infoNofT = woflan.InfoUnbSNofT;
			infoName = woflan.InfoUnbSTName;
			s = "Unbounded";
		} else {
			infoNofS = woflan.InfoNofNLiveS;
			infoNofT = woflan.InfoNLiveSNofT;
			infoName = woflan.InfoNLiveSTName;
			s = "Non-live";
		}
		DefaultMutableTreeNode node, node2;
		nofSeq = Integer.parseInt(woflan.Info(woflanNet, infoNofS, 0, 0));
		node = new DefaultMutableTreeNode(s + " sequences [" + nofSeq + "]");
		parent.add(node);
		for (i = 0; i < nofSeq; i++) {
			nofT = Integer.parseInt(woflan.Info(woflanNet, infoNofT, i, 0));
			node2 = new DefaultMutableTreeNode(s + " sequence " + i + " ["
					+ nofT + "]");
			node.add(node2);
			for (j = 0; j < nofT; j++) {
				node2.add(new DefaultMutableTreeNode(getName(woflan.Info(
						woflanNet, infoName, i, j))));
			}
		}

	}

	private void buildBoundedDiagnosis() {
		int nofNot;
		DefaultMutableTreeNode step, node;
		woflan.Info(woflanNet, woflan.SetUnb, 0, 0);
		nofNot = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofUnbP, 0,
				0));
		if (nofNot == 0) {
			isBounded = 2;
			step = new DefaultMutableTreeNode("Boundedness: Passed");
			netDiagnosis.add(step);
		} else {
			isBounded = 1;
			step = new DefaultMutableTreeNode("Boundedness: Failed");
			netDiagnosis.add(step);
			getBasicNetProperty(woflan.InfoNofUnbP, woflan.InfoUnbPName,
					"Unbounded places", step);
			buildSequenceDiagnosis(true, step);
			buildHandleDiagnosis(false, step);
		}
	}

	private void buildNonDeadDiagnosis() {
		int nofNot;
		DefaultMutableTreeNode step, node;
		woflan.Info(woflanNet, woflan.SetNLive, 0, 0);
		nofNot = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofDeadT,
				0, 0));
		if (nofNot == 0) {
			isNonDead = 2;
			step = new DefaultMutableTreeNode("0-Liveness: Passed");
			netDiagnosis.add(step);
		} else {
			isNonDead = 1;
			step = new DefaultMutableTreeNode("0-Liveness: Failed");
			netDiagnosis.add(step);
			getBasicNetProperty(woflan.InfoNofDeadT, woflan.InfoDeadTName,
					"Dead transitions", step);
			buildHandleDiagnosis(true, step);
		}
	}

	private void buildLiveDiagnosis() {
		int nofNot;
		DefaultMutableTreeNode step, node;
		// Pre: The next line has been invoked before.
		// woflan.Info(woflanNet, woflan.SetNLive, 0, 0);
		nofNot = Integer.parseInt(woflan.Info(woflanNet, woflan.InfoNofNLiveT,
				0, 0));
		if (nofNot == 0) {
			isLive = 2;
			step = new DefaultMutableTreeNode("Liveness: Passed");
			netDiagnosis.add(step);
		} else {
			isLive = 1;
			step = new DefaultMutableTreeNode("Liveness: Failed");
			netDiagnosis.add(step);
			getBasicNetProperty(woflan.InfoNofNLiveT, woflan.InfoNLiveTName,
					"Non-live transitions", step);
			buildSequenceDiagnosis(false, step);
			buildHandleDiagnosis(true, step);
		}
	}

	/**
	 * Create the "Net Diagnosis" section in the Analysis tree.
	 * 
	 * @param woflan
	 *            Handle to Woflan itself.
	 * @param woflanNet
	 *            The reference to the Petri net at hand.
	 */
	private void buildNetDiagnosis(int nofSteps) {
		int i = nofSteps;

		while (i > 0 && woflanNet != 0) { // Do something as long as soundness
			// is unknown.
			i--;
			if (isSound != 0 && woflanNet != 0) {
				nextButton.setVisible(false);
				finishButton.setVisible(false);
				woflan.Close(woflanNet);
				woflanNet = 0;
			} else if (isWFNet == 0) { // Unknown whether WF-net.
				root.add(netDiagnosis);
				// Check whether WF-net.
				buildWFNetDiagnosis();
			} else if (isWFNet == 1 && isSound == 0) {
				// No WF-net, hence soundness not defined (for sake of
				// simplicity say it is not sound).
				isSound = 1;
				i++;
			} else if (isWFNet == 2 && isSCoverable == 0) { // WF-net, unknown
				// whether
				// S-Coverable.
				// Check whether S-coverable
				buildSCoverableDiagnosis();
			} else if (isSCoverable == 1 && isPICoverable == 0) { // No S-Cover,
				// check
				// whether
				// PI-cover
				// Check whether BPI-coverable
				buildPIDiagnosis();
			} else if (isPICoverable == 1 && isBounded == 0) { // Check
				// boundedness
				// Check whether bounded
				buildBoundedDiagnosis();
			} else if ((isSCoverable == 2 || isPICoverable == 2)
					&& isBounded == 0) {
				isBounded = 2;
				i++;
			} else if (isBounded == 2 && isNonDead == 0) { // Bounded: check
				// whether dead
				// transitions exist
				// Check whether 0-live
				buildNonDeadDiagnosis();
			} else if (isBounded == 2 && isNonDead == 2 && isLive == 0) { // Bounded,
				// no
				// dead
				// transitions:
				// check
				// liveness
				// Check whether live
				buildLiveDiagnosis();
			} else if (isLive == 2 && isSound == 0) {
				isSound = 2;
				i++;
			} else {
				isSound = 1;
				i++;
			}
		}
	}

	public void valueChanged(TreeSelectionEvent event) {
		TreePath[] selectedPaths = treeContainer.getSelectionPaths();
		Bag selectedPlaces = new Bag();
		Bag selectedTransitions = new Bag();
		selectedPlaces.clear();
		selectedTransitions.clear();
		if (selectedPaths != null) {
			int i;
			for (i = 0; i < selectedPaths.length; i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPaths[i]
						.getLastPathComponent();
				String s = node.toString();
				if (s.startsWith("Place ")) {
					selectedPlaces.add(getPlace(s.substring(6)));
				} else if (s.startsWith("Transition ")) {
					selectedTransitions.add(getTransition(s.substring(11)));
				}
			}
		}
		highlight(selectedPlaces, selectedTransitions);
	}

	private void highlight(Bag selectedPlaces, Bag selectedTransitions) {
		Bag selected = new Bag();
		selected.addAll(selectedPlaces);
		selected.addAll(selectedTransitions);
		gp.unSelectAll();
		gp.selectElements(selected);
		validate();
		repaint();
		// System.err.println(selected.toString());
	}

	private void buildGraphMapping(Map mapping, Subgraph g) {
		Enumeration e = g.nodeElements();
		while (e.hasMoreElements()) {
			Node n = (Node) e.nextElement();
			mapping.put(n.object, n);
		}
		e = g.edgeElements();
		while (e.hasMoreElements()) {
			Edge n = (Edge) e.nextElement();
			mapping.put(n.object, n);
		}
		e = g.subgraphElements();
		while (e.hasMoreElements()) {
			Subgraph n = (Subgraph) e.nextElement();
			buildGraphMapping(mapping, n);
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		ArrayList objects = new ArrayList();
		if (net != null) {
			objects.add(new ProvidedObject("Original Petri Net",
					new Object[] { net }));
		}
		if (reachability != null) {
			objects.add(new ProvidedObject("Reachability graph",
					new Object[] { reachability }));
		}
		ProvidedObject[] res = new ProvidedObject[objects.size()];
		for (int i = 0; i < objects.size(); i++) {
			res[i] = (ProvidedObject) objects.get(i);
		}
		return res;
	}

}
