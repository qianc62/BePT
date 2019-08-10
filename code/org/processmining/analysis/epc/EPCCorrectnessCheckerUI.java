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

package org.processmining.analysis.epc;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.epcpack.algorithms.EPCToPetriNetConverter;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Token;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.ReachabilityGraphBuilder;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class EPCCorrectnessCheckerUI extends JPanel implements Provider {

	public final static String COUNTER = "$$$Counter$$$ ";
	public final static String TRIVCORRECT = "The EPC is trivially correct, regardless of initial states.";
	public final static String CORRECT = "The EPC is correct, with respect to the chosen initial states.";
	public final static String INCORRECT = "The EPC contains structural errors.";
	public final static String RELCORRECT = "The EPC can be correct, but allows for undesired behaviour.";

	private ConfigurableEPC epc;
	private ConfigurableEPC reducedEPC;
	private PetriNet pnet;
	private StateSpace coverability;
	private HashMap initialEvents;

	protected HashSet initialEventSets;
	protected JPanel tab0Panel;
	protected JPanel tab1Panel;

	protected ArrayList finalStatesToKeep;
	protected ArrayList finalStatesToRemove;
	private ArrayList notColoredStates;
	private HashSet coloredTrans;
	private HashSet notColoredTrans;
	private HashMap connectorMapping;
	private HashSet extraPlaces;

	private ModelGraphPanel epcVis = null;
	private ModelGraphPanel reducedEPCVis = null;
	private ModelGraphPanel pnetVis = null;
	private ModelGraphPanel coverabilityVis = null;

	JButton tab0NextButton;
	SettingsPanel settingsPanel = new SettingsPanel();

	JSplitPane jSplitPane1 = new JSplitPane();
	JTabbedPane rightTabPane = new JTabbedPane();

	JPanel rightPanel = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	GridBagLayout gridBagLayout2 = new GridBagLayout();

	FlowLayout flowLayout1 = new FlowLayout();
	FlowLayout flowLayout2 = new FlowLayout();

	JTabbedPane tabbedPane = new JTabbedPane();

	public EPCCorrectnessCheckerUI(ConfigurableEPC orgEPC) {
		this.epc = orgEPC;
		this.epc.setShowObjects(false, false, false);
		this.epcVis = epc.getGrappaVisualization();

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		rightPanel.setLayout(flowLayout2);
		rightPanel.add(rightTabPane);

		rightTabPane.addTab("Main EPC", new JScrollPane(epcVis));
		rightTabPane.addTab("Reduced EPC", null);
		rightTabPane.addTab("Petri net", null);
		rightTabPane.addTab("Coverability graph", null);
		rightTabPane.addTab("Plugin Settings", settingsPanel);

		rightTabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (rightTabPane.getSelectedIndex() == 1
						&& (reducedEPC != null)
						&& (rightTabPane.getSelectedComponent() == null)) {
					if (reducedEPCVis == null) {
						reducedEPCVis = reducedEPC.getGrappaVisualization();
					}
					rightTabPane.setComponentAt(1, new JScrollPane(
							reducedEPCVis));
					;
				} else if (rightTabPane.getSelectedIndex() == 2
						&& (pnet != null)
						&& (rightTabPane.getSelectedComponent() == null)) {
					if (pnetVis == null) {
						pnetVis = pnet.getGrappaVisualization();
					}
					rightTabPane.setComponentAt(2, new JScrollPane(pnetVis));
					;
				} else if (rightTabPane.getSelectedIndex() == 3
						&& (coverability != null)
						&& (rightTabPane.getSelectedComponent() == null)) {
					if (coverabilityVis == null) {
						coverabilityVis = coverability.getGrappaVisualization();
					}
					rightTabPane.setComponentAt(3, new JScrollPane(
							coverabilityVis));
					;
				}
			}
		});

		this.setLayout(borderLayout1);

		flowLayout1.setAlignment(FlowLayout.LEFT);

		jSplitPane1.add(rightTabPane, JSplitPane.RIGHT);

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(tabbedPane, BorderLayout.CENTER);

		jSplitPane1.add(leftPanel, JSplitPane.LEFT);

		this.add(jSplitPane1, BorderLayout.CENTER);
		rightTabPane.setEnabledAt(1, false);
		rightTabPane.setEnabledAt(2, false);
		rightTabPane.setEnabledAt(3, false);

		initialEvents = new HashMap();
		Iterator it = epc.getEvents().iterator();
		while (it.hasNext()) {
			EPCEvent e = (EPCEvent) it.next();
			if (e.inDegree() == 0) {
				initialEvents.put(e, null);
			}
		}

		JPanel tab0 = new JPanel(new BorderLayout());
		JPanel tab0ButPanel = new JPanel(new FlowLayout());
		tab0NextButton = new JButton("Next step");

		tab0NextButton
				.addActionListener(new NavActionListener(tabbedPane, this));
		tab0ButPanel.add(tab0NextButton);
		tab0.add(tab0ButPanel, BorderLayout.SOUTH);
		tab0Panel = new InitialEventChoserPanel(initialEvents, epcVis);
		tab0.add(tab0Panel, BorderLayout.CENTER);

		tabbedPane.addTab("Initial event-sets", tab0);

		// This step fills the PetriNet in "pnet"
		// and provides all initial events in "initialEvents"

	}

	protected void convertToPetrinet() {

		// Convert the EPC to a petrinet
		connectorMapping = new HashMap();
		pnet = EPCToPetriNetConverter.convert(reducedEPC, connectorMapping);
		pnet.Test("Petrinet");

		rightTabPane.setEnabledAt(2, true);

		// Check the user for initial markings
		initialEvents = new HashMap();
		extraPlaces = new HashSet();
		ArrayList places = pnet.getPlaces();
		int i = 0;
		int up = places.size();
		while (i < up) {
			Place p = (Place) places.get(i);
			i++;
			Place p2 = pnet.addPlace(COUNTER + p.getIdentifier());
			extraPlaces.add(p2);
			Iterator it2 = p.getPredecessors().iterator();
			while (it2.hasNext()) {
				pnet.addEdge(p2, (Transition) it2.next());
			}
			it2 = p.getSuccessors().iterator();
			while (it2.hasNext()) {
				pnet.addEdge((Transition) it2.next(), p2);
			}
			if (p.inDegree() > 0) {
				continue;
			}
			EPCEvent e = (EPCEvent) p.object;
			if (e != null) {
				initialEvents.put(((HashSet) e.object2).iterator().next(), p);
			}
		}
		// Message.add("Initial events: " + initialEvents.keySet().toString());
		// Message.add("Initial places: " + initialEvents.values().toString());
	}

	protected void makeCoverabilityGraph() {
		Iterator it;

		// Check for the initialStates list
		Place start = pnet.addPlace("Fictive start");

		it = initialEventSets.iterator();
		while (it.hasNext()) {
			HashSet initialSet = (HashSet) it.next();
			Transition t = pnet.addTransition(new Transition(pnet));
			pnet.addEdge(start, t);
			Iterator it2 = initialSet.iterator();
			while (it2.hasNext()) {
				EPCEvent e = (EPCEvent) it2.next();
				Place p = (Place) initialEvents.get(e);
				if (p != null) {
					pnet.addEdge(t, p);
					pnet
							.addEdge(pnet
									.findPlace(COUNTER + p.getIdentifier()), t);
				} // if p==null then we found an event that is not in the
				// reduced EPC
			}
		}

		// and edit the petrinet accordingly

		// Mark the initial places
		it = pnet.getPlaces().iterator();
		while (it.hasNext()) {
			Place p = (Place) it.next();
			p.removeAllTokens();
			if (extraPlaces.contains(p)) {
				p.addToken(new Token());
			}
		}
		start.addToken(new Token());

		// Build coverabilitygraph
		coverability = ReachabilityGraphBuilder.build(pnet);
		coverability.Test("coverabilityGraph");
		rightTabPane.setEnabledAt(3, true);

		// Find the final states
		// and check with the user for correct final states
		finalStatesToKeep = new ArrayList();
		finalStatesToRemove = new ArrayList();
		int i = -1;
		while (i + 1 < coverability.getVerticeList().size()) {
			i++;

			State s = (State) coverability.getVerticeList().get(i);

			String id = "[";
			it = s.iterator();
			while (it.hasNext()) {
				Place p = (Place) it.next();
				int o = s.getOccurances(p);
				if (p.getIdentifier().startsWith(COUNTER)) {
					continue;
				}
				if (!id.equals("[")) {
					id += ",\\n";
				}
				id += "(" + p.getIdentifier() + "," + o + ")";

			}
			s.setIdentifier(id + "]");

			if (s.outDegree() > 0) {
				continue;
			}

			// The state s is an end state
			boolean keep = true;
			Iterator it2 = s.iterator();
			while (keep && it2.hasNext()) {
				Place p = (Place) it2.next();

				if (p.getIdentifier().startsWith(COUNTER)) {
					continue;
				}
				// Keep if: there's only one token in p
				// p belongs to an event
				// The event is an end event
				keep = keep && (s.getOccurances(p) == 1)
						&& (p.object instanceof EPCEvent)
						&& (((EPCEvent) p.object).outDegree() == 0);
			}
			if (!keep) {
				finalStatesToRemove.add(s);
				continue;
			}
			// This state is a possible final state.
			finalStatesToKeep.add(s);

		}

		if (tabbedPane.getTabCount() != 1) {
			tabbedPane.remove(1);
		}

		JPanel tab1 = new JPanel(new BorderLayout());
		JPanel tab1ButPanel = new JPanel(new FlowLayout());
		JButton tab1nextButton = new JButton("Show result");
		tab1nextButton
				.addActionListener(new NavActionListener(tabbedPane, this));
		tab1ButPanel.add(tab1nextButton);
		tab1.add(tab1ButPanel, BorderLayout.SOUTH);
		tab1Panel = new FinalEventSetSelectorPanel(finalStatesToKeep,
				finalStatesToRemove, epcVis);
		tab1.add(tab1Panel, BorderLayout.CENTER);

		tabbedPane.add(tab1, 1);
		tabbedPane.setTitleAt(1, "Final event-sets");

	}

	protected void reduceEPC() {
		// Reduce the EPC to the minimal one
		reducedEPC = settingsPanel.reduce(epc);
		reducedEPC.setShowObjects(false, false, false);
		rightTabPane.setEnabledAt(1, true);
		// Check for Trivial correct
		if ((reducedEPC.getEvents().size() == 2)
				&& (reducedEPC.getEdges().size() == 1)
				&& (((EPCEvent) reducedEPC.getEvents().get(0)).getSuccessors()
						.contains(reducedEPC.getEvents().get(1)) || ((EPCEvent) reducedEPC
						.getEvents().get(1)).getSuccessors().contains(
						reducedEPC.getEvents().get(0)))) {
			tab0NextButton.setEnabled(false);
			reportResult(TRIVCORRECT);
		}
		reducedEPC.Test("reducedEPC");

	}

	protected void colorCoverabilityGraph() {
		// Color the coverabilitygraph from the correct final states
		// back to the initial state.

		// We know now, al possible final states, including the ones to keep

		// Then, we color the graph. We add all nodes to a hashmap that have an
		// outgoing arc
		// to a node already in the map.
		ArrayList colored = new ArrayList();
		colored.addAll(finalStatesToKeep);

		notColoredStates = new ArrayList();
		notColoredStates.addAll(coverability.getVerticeList());
		notColoredStates.removeAll(colored);

		extendColorMap(notColoredStates, colored);

		// We now have the colored states,
		// get the colored edges
		// and find the transitions that are not colored

		coloredTrans = new HashSet();
		notColoredTrans = new HashSet();

		Iterator it = coverability.getEdges().iterator();
		while (it.hasNext()) {
			ModelGraphEdge e = (ModelGraphEdge) it.next();
			if (colored.contains(e.getSource())
					&& colored.contains(e.getDest())) {
				coloredTrans.add(e.object);
			}
		}
		notColoredTrans.addAll(pnet.getTransitions());
		notColoredTrans.removeAll(coloredTrans);

		// this.add(jPanel2, BorderLayout.SOUTH);

	}

	protected void checkORCovering() {
		// In this step, we check whether all OR-joins are covered. This means,
		// that in case of a split, for all possible outgoing edges, there
		// should be at least one transition belonging to the connector that
		// follows that direction
		boolean del = false;
		Iterator it = notColoredTrans.iterator();
		while (it.hasNext()) {
			Transition t = (Transition) it.next();
			if (!(t.object instanceof EPCConnector)) {
				continue;
			}
			EPCConnector c = (EPCConnector) t.object;
			if (c.getType() != EPCConnector.OR) {
				continue;
			}
			// Now, c is an OR connector of which one transition is not colored
			// Check if this transition is necessary
			ArrayList transitions = (ArrayList) connectorMapping.get(c);

			ArrayList placesToCover = new ArrayList();
			placesToCover.addAll(t.getPredecessors());
			placesToCover.addAll(t.getSuccessors());

			Iterator it2 = transitions.iterator();
			while (it2.hasNext()) {
				Transition t2 = (Transition) it2.next();
				if (t == t2) {
					continue;
				}
				if (notColoredTrans.contains(t2)) {
					continue;
				}
				placesToCover.removeAll(t2.getPredecessors());
				placesToCover.removeAll(t2.getSuccessors());
			}

			if (placesToCover.isEmpty()) {
				pnet.delTransition(t);
				del = true;
			}

		}
		if (del) {
			colorCoverabilityGraph();
		}

	}

	private void extendColorMap(ArrayList notColored, ArrayList colored) {
		boolean addedSomething = false;
		Iterator it = notColored.iterator();
		while (it.hasNext()) {
			State s = (State) it.next();
			boolean col = false;
			Iterator it2 = s.getSuccessors().iterator();
			while (!col && it2.hasNext()) {
				col = colored.contains((State) it2.next());
			}
			if (col) {
				colored.add(s);
				it.remove();
				addedSomething = true;
			}
		}
		if (addedSomething) {
			extendColorMap(notColored, colored);
		}
	}

	protected void showResult() {
		if (reducedEPCVis == null) {
			reducedEPCVis = reducedEPC.getGrappaVisualization();
		}
		if (pnetVis == null) {
			pnetVis = pnet.getGrappaVisualization();
		}
		if (coverabilityVis == null) {
			coverabilityVis = coverability.getGrappaVisualization();
		}

		epcVis.unSelectAll();
		if (reducedEPCVis != null) {
			reducedEPCVis.unSelectAll();
		}
		if (pnetVis != null) {
			pnetVis.unSelectAll();
		}
		if (coverabilityVis != null) {
			coverabilityVis.unSelectAll();
		}

		if (notColoredStates.size() == 0) {
			reportResult(CORRECT);
		} else if (notColoredTrans.size() == 0) {
			reportResult(RELCORRECT);
		} else {
			reportResult(INCORRECT);
		}

		if (coverabilityVis != null) {
			coverabilityVis.selectElements(notColoredStates);
		}

		Iterator it = notColoredTrans.iterator();
		while (it.hasNext()) {
			// t is not colored
			Transition t = (Transition) it.next();
			ArrayList a = new ArrayList();
			a.add(t);
			if (pnetVis != null) {
				pnetVis.selectElements(a);
			}
		}
		it = pnet.getPlaces().iterator();

		while (it.hasNext()) {
			Place p = (Place) it.next();
			if (notColoredTrans.containsAll(p.getPredecessors())
					&& notColoredTrans.containsAll(p.getSuccessors())
					&& p.inDegree() * p.outDegree() > 0) {
				ArrayList a = new ArrayList();
				a.add(p);
				if (pnetVis != null) {
					pnetVis.selectElements(a);
				}
			}
		}

		HashSet selectedEPCObjects = new HashSet();

		it = notColoredTrans.iterator();
		while (it.hasNext()) {
			Transition t = (Transition) it.next();
			// o is not colored
			EPCObject o = (EPCObject) t.object;
			if (o == null) {
				continue;
			}
			ArrayList a = new ArrayList();
			a.add(o);
			if (reducedEPCVis != null) {
				reducedEPCVis.selectElements(a);
			}
			if (o.object2 != null) {
				selectedEPCObjects.addAll((Collection) o.object2);
			}
		}

		if (reducedEPCVis != null) {
			HashSet s = reducedEPCVis.selectEdges(false);
			it = s.iterator();
			while (it.hasNext()) {
				ModelGraphEdge e = (ModelGraphEdge) it.next();
				selectedEPCObjects.addAll((HashSet) e.object2);
			}
		}

		if (pnetVis != null) {
			pnetVis.selectEdges(true);
		}
		if (coverabilityVis != null) {
			coverabilityVis.selectEdges(true);
		}

		epcVis.selectElements(selectedEPCObjects);

	}

	public ProvidedObject[] getProvidedObjects() {
		ArrayList objects = new ArrayList();
		if (epc != null) {
			objects
					.add(new ProvidedObject("Original EPC",
							new Object[] { epc }));
		}
		if (reducedEPC != null) {
			objects.add(new ProvidedObject("Reduced EPC",
					new Object[] { reducedEPC }));
		}
		if (pnet != null) {
			objects.add(new ProvidedObject("Petri net", new Object[] { pnet }));
		}
		if (coverability != null) {
			objects.add(new ProvidedObject("Coverability graph",
					new Object[] { coverability }));
		}
		ProvidedObject[] res = new ProvidedObject[objects.size()];
		for (int i = 0; i < objects.size(); i++) {
			res[i] = (ProvidedObject) objects.get(i);
		}
		return res;
	}

	private void reportResult(String result) {
		Message.add("<verificationresult " + result + ">", Message.TEST);
		JOptionPane.showMessageDialog(MainUI.getInstance(), result,
				"Verification result", JOptionPane.INFORMATION_MESSAGE);
		Message.add(result);

	}

	public void invalidateGraphVis() {
		reducedEPCVis = null;
		if (rightTabPane.getComponentAt(1) != null) {
			rightTabPane.remove(rightTabPane.getComponentAt(1));
			rightTabPane.setEnabledAt(1, false);
			rightTabPane.insertTab("Reduced EPC", null, null, "", 1);
		}
		pnetVis = null;
		if (rightTabPane.getComponentAt(2) != null) {
			rightTabPane.remove(rightTabPane.getComponentAt(2));
			rightTabPane.setEnabledAt(2, false);
			rightTabPane.insertTab("Petri net", null, null, "", 2);
		}
		coverabilityVis = null;
		if (rightTabPane.getComponentAt(3) != null) {
			rightTabPane.remove(rightTabPane.getComponentAt(3));
			rightTabPane.setEnabledAt(3, false);
			rightTabPane.insertTab("Coverability graph", null, null, "", 3);
		}
	}

}

class NavActionListener implements ActionListener {
	private JTabbedPane tabbedPane;
	private EPCCorrectnessCheckerUI ui;

	public NavActionListener(JTabbedPane tabbedPane, EPCCorrectnessCheckerUI ui) {
		this.tabbedPane = tabbedPane;
		this.ui = ui;
	}

	public void actionPerformed(ActionEvent e) {
		if (tabbedPane.getSelectedIndex() == 0) {
			ui.invalidateGraphVis();
			ui.reduceEPC();

			ui.initialEventSets = ((InitialEventChoserPanel) ui.tab0Panel)
					.getInitialEventSets();
			// Now start the algorithm:
			ui.convertToPetrinet();
			// This step has edited the PetriNet and built the coverabilitygraph
			// in "coverability"
			// Furthermore, the possible end-markings are known in
			// "finalStatesToKeep" and "finalStatesToRemove"
			ui.makeCoverabilityGraph();
			tabbedPane.setSelectedIndex(1);
			return;
		}
		if (tabbedPane.getSelectedIndex() == 1) {
			ui.finalStatesToRemove.addAll(ui.finalStatesToKeep);
			ui.finalStatesToKeep = ((FinalEventSetSelectorPanel) ui.tab1Panel)
					.getCorrectFinalStates();
			ui.finalStatesToRemove.removeAll(ui.finalStatesToKeep);

			ui.colorCoverabilityGraph();
			// Finally, the transitions are known that are not covered by a path
			// from an initial marking to a final marking in
			// "coloredTransitions".
			// all other transitions are present in "notColoredTransitions"

			ui.checkORCovering();
			// The final step should check whether all OR-connectors are
			// covered.
			// and repeat the third step;

			// The decision is as follows:
			// 1) if notColoredStates.size == 0 then the EPC is SOUND
			// 2) if notColoredStates.size > 0 and
			// notColoredTrans.size == 0 then the EPC is RELAXED SOUND
			// 3) if notColoredTrans.size > 0 then the EPC is not Relaxed sound
			// Based on uncolored transitions, we can make the decision
			ui.showResult();
			return;
		}
	}
}
