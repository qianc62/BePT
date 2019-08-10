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

package org.processmining.analysis.petrinet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.models.Bag;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.algorithms.CoverabilityGraphBuilder;
import org.processmining.framework.models.petrinet.algorithms.InitialPlaceMarker;
import org.processmining.framework.models.petrinet.algorithms.PlaceInvariantCalculator;
import org.processmining.framework.models.petrinet.algorithms.ReachabilityGraphBuilder;
import org.processmining.framework.models.petrinet.algorithms.TransitionInvariantCalculator;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;

import att.grappa.Edge;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * @author not attributable
 * @version 1.0
 */

public class PetriNetAnalysisUI extends JPanel implements Provider {

	private PetriNet net;
	private JButton showNetButton = new JButton("Show Petri Net");
	private JButton calculateInvariantsButton = new JButton(
			"Calculate Invariants");
	private JButton calculateReachabilityButton = new JButton(
			"Show reachability graph");
	private JButton calculateCoverabilityButton = new JButton(
			"Show coverability graph");
	private JButton calculateRestrictedCoverabilityButton = new JButton(
			"Show restricted coverability graph");
	private JScrollPane netContainer;
	private JPanel buttonsPanel = new JPanel();
	private StateSpace reachability = null;
	private StateSpace coverability = null;
	private StateSpace restrictedcoverability = null;
	private ModelGraphPanel gp;
	private HashMap mapping;
	private boolean invariantsDone = false;
	private ArrayList tinv;
	private ArrayList pinv;
	private int currentTInv;

	public PetriNetAnalysisUI(PetriNet net) {
		this.net = net;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {

		calculateReachabilityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InitialPlaceMarker.mark(net, 1);
				remove(netContainer);
				reachability = ReachabilityGraphBuilder.build(net);
				netContainer = new JScrollPane(reachability
						.getGrappaVisualization());
				InitialPlaceMarker.mark(net, 0);
				add(netContainer, BorderLayout.CENTER);
				buttonsPanel.remove(calculateInvariantsButton);
				buttonsPanel.remove(calculateCoverabilityButton);
				buttonsPanel.remove(calculateRestrictedCoverabilityButton);
				buttonsPanel.remove(calculateReachabilityButton);
				validate();
				repaint();
				Message.add("<PetriNetAnalysis nofReachableStates=\""
						+ reachability.getVerticeList().size()
						+ "\" nofEdges=\"" + reachability.getNumberOfEdges()
						+ "\">", Message.TEST);
			}
		});
		calculateCoverabilityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InitialPlaceMarker.mark(net, 1);
				remove(netContainer);
				coverability = CoverabilityGraphBuilder.build(net, -1, true,
						true);
				netContainer = new JScrollPane(coverability
						.getGrappaVisualization());
				InitialPlaceMarker.mark(net, 0);
				buttonsPanel.remove(calculateInvariantsButton);
				buttonsPanel.remove(calculateReachabilityButton);
				buttonsPanel.remove(calculateCoverabilityButton);
				buttonsPanel.remove(calculateRestrictedCoverabilityButton);
				add(netContainer, BorderLayout.CENTER);
				validate();
				repaint();
				Message.add("<PetriNetAnalysis nofCoverableStates=\""
						+ coverability.getVerticeList().size()
						+ "\" nofEdges=\"" + coverability.getNumberOfEdges()
						+ "\">", Message.TEST);
			}
		});
		calculateRestrictedCoverabilityButton
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						InitialPlaceMarker.mark(net, 1);
						remove(netContainer);
						restrictedcoverability = CoverabilityGraphBuilder
								.build(net, -1, true, false);
						netContainer = new JScrollPane(restrictedcoverability
								.getGrappaVisualization());
						InitialPlaceMarker.mark(net, 0);
						buttonsPanel.remove(calculateInvariantsButton);
						buttonsPanel.remove(calculateReachabilityButton);
						buttonsPanel.remove(calculateCoverabilityButton);
						buttonsPanel
								.remove(calculateRestrictedCoverabilityButton);
						add(netContainer, BorderLayout.CENTER);
						validate();
						repaint();
						Message.add("<PetriNetAnalysis nofCoverableStates=\""
								+ restrictedcoverability.getVerticeList()
										.size() + "\" nofEdges=\""
								+ restrictedcoverability.getNumberOfEdges()
								+ "\">", Message.TEST);
					}
				});
		calculateInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!invariantsDone) {
					tinv = TransitionInvariantCalculator.calculate(net, 1);
					Message.add("Semi-positive T-invariants: " + tinv.size());
					Message.add(tinv.toString(), Message.DEBUG);
					pinv = PlaceInvariantCalculator.calculate(net, 1);
					Message.add("Semi-positive P-invariants: " + pinv.size());
					Message.add(pinv.toString(), Message.DEBUG);
					currentTInv = -1;
				}
				invariantsDone = true;
				if (tinv.size() > 0) {
					currentTInv = (currentTInv + 1) % tinv.size();
					Bag b = (Bag) tinv.get(currentTInv);
					gp.unSelectAll();
					gp.selectElements(b);
					gp.selectEdges(false);
				}
				Message.add("<PetriNetAnalysis nofTInvariants=\"" + tinv.size()
						+ "\" nofPInvariants=\"" + pinv.size() + "\">",
						Message.TEST);
			}
		});
		showNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gp.unSelectAll();
				remove(netContainer);
				netContainer = new JScrollPane(gp);
				add(netContainer, BorderLayout.CENTER);
				buttonsPanel.add(calculateReachabilityButton);
				buttonsPanel.add(calculateCoverabilityButton);
				buttonsPanel.add(calculateRestrictedCoverabilityButton);
				buttonsPanel.add(calculateInvariantsButton);
				validate();
				repaint();
			}
		});

		buttonsPanel.add(showNetButton);
		buttonsPanel.add(calculateReachabilityButton);
		buttonsPanel.add(calculateCoverabilityButton);
		buttonsPanel.add(calculateRestrictedCoverabilityButton);
		buttonsPanel.add(calculateInvariantsButton);

		gp = net.getGrappaVisualization();
		mapping = new HashMap();
		buildGraphMapping(mapping, gp.getSubgraph());
		netContainer = new JScrollPane(gp);

		this.setLayout(new BorderLayout());
		this.add(netContainer, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.SOUTH);
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
		if (coverability != null) {
			objects.add(new ProvidedObject("Coverability graph",
					new Object[] { coverability }));
		}
		if (restrictedcoverability != null) {
			objects.add(new ProvidedObject("Restricted coverability graph",
					new Object[] { restrictedcoverability }));
		}
		ProvidedObject[] res = new ProvidedObject[objects.size()];
		for (int i = 0; i < objects.size(); i++) {
			res[i] = (ProvidedObject) objects.get(i);
		}
		return res;
	}
}
