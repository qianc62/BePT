/**
 * Copyright (c) 2009 Christian W. Guenther (christian@deckfour.org)
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

package org.processmining.analysis.redesign.ui.petri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.deckfour.gantzgraf.canvas.GGCanvas;
import org.deckfour.gantzgraf.event.GGGraphListener;
import org.deckfour.gantzgraf.model.GGEdge;
import org.deckfour.gantzgraf.model.GGGraph;
import org.deckfour.gantzgraf.model.GGNode;
import org.processmining.analysis.redesign.ui.RedesignAnalysisUI;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.PNNode;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import att.grappa.Edge;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * @author mnetjes
 * 
 */

public class PetriNetGraph extends GGGraph {
	private RedesignAnalysisUI parent;
	private HLPetriNet model;

	/**
	 * Drawing the graph
	 */
	public PetriNetGraph(final RedesignAnalysisUI parent, final HLPetriNet model) {
		super(Direction.LEFT_TO_RIGHT);
		this.parent = parent;
		this.model = model;
		/**
		 * assemble shadow graph
		 */
		HashMap<PNNode, GGNode> nodeMap = new HashMap<PNNode, GGNode>();
		for (Transition t : model.getPNModel().getTransitions()) {
			PetriNetTransition nt = new PetriNetTransition(t);
			nodeMap.put(t, nt);
			this.add(nt);
			/**
			 * set a graph listener to be able to detect the selection of the
			 * node
			 */
			nt.setGraphListener(new GGGraphListener() {
				public void edgeMouseOff(GGCanvas canvas, GGGraph graph,
						GGEdge edge, float x, float y) {
					// TODO Auto-generated method stub
				}

				public void edgeMouseOn(GGCanvas canvas, GGGraph graph,
						GGEdge edge, float x, float y) {
					// TODO Auto-generated method stub
				}

				public void edgeSelected(GGCanvas canvas, GGGraph graph,
						GGEdge edge, float x, float y) {
				}

				public void edgeSelectionReset(GGCanvas canvas, GGGraph graph,
						GGEdge edge, float x, float y) {
					// TODO Auto-generated method stub
				}

				public void nodeMouseOff(GGCanvas canvas, GGGraph graph,
						GGNode node, float x, float y) {
					// TODO Auto-generated method stub
				}

				public void nodeMouseOn(GGCanvas canvas, GGGraph graph,
						GGNode node, float x, float y) {
					// TODO Auto-generated method stub
				}

				/**
				 * at selection of the node, the associated model displays the
				 * nodes, node colors may have been updated.
				 */
				public void nodeSelected(GGCanvas canvas, GGGraph graph,
						GGNode node, float x, float y) {
					for (GGNode n : nodes()) {
						if (n.equals(node)) {
							n.setSelected(true);
						}
					}
					updateView();
					display();
				}

				/**
				 * at selection reset of the node, the associated model displays
				 * the nodes, node colors may have been updated.
				 */
				public void nodeSelectionReset(GGCanvas canvas, GGGraph graph,
						GGNode node, float x, float y) {
					for (GGNode n : nodes()) {
						if (n.equals(node)) {
							n.setSelected(false);
						}
					}
					updateView();
					display();
				}
			});
		}
		for (Place p : model.getPNModel().getPlaces()) {
			PetriNetPlace np = new PetriNetPlace(p);
			nodeMap.put(p, np);
			this.add(np);
		}
		for (Object o : model.getPNModel().getEdges()) {
			Edge e = (Edge) o;
			GGNode source = nodeMap.get(e.getTail());
			GGNode target = nodeMap.get(e.getHead());
			PetriNetEdge edge = new PetriNetEdge(e, source, target);
			this.add(edge);
		}
	}

	/**
	 * Returns the original Petri net
	 * 
	 * @return original PetriNet the original Petri net
	 */
	public PetriNet getOriginal() {
		return model.getPNModel();
	}

	public void display() {
		parent.displaySelectedComponents(model, this);
	}

	/**
	 * Returns a specific transition contained in this graph by the first
	 * element of its label. This is its identifier.
	 */
	public synchronized PetriNetTransition getNodeByIdentifier(String identifier) {
		for (GGNode node : nodes) {
			if (node.label()[0].equals(identifier)) {
				return (PetriNetTransition) node;
			}
		}
		return null;
	}

	/**
	 * Returns the petri net nodes which are currently selected in the GUI of
	 * this graph. If no nodes are selected, all nodes of the petri net are
	 * returned.
	 * 
	 * @return ArrayList<PNNode> selectedNodeList the selected nodes or
	 *         ArrayList<PNNode> AllNodeList all nodes in the petrinet
	 */
	public List<PNNode> getSelectedPetriNetNodes() {
		ArrayList<PNNode> selectedNodeList = new ArrayList<PNNode>();
		ArrayList<PNNode> allNodeList = new ArrayList<PNNode>();
		for (GGNode node : this.nodes()) {
			if (node instanceof PetriNetTransition) {
				PetriNetTransition pnt = (PetriNetTransition) node;
				if (node.isSelected()) {
					selectedNodeList.add(pnt.original());
				}
				allNodeList.add(pnt.original());
			} else if (node instanceof PetriNetPlace) {
				PetriNetPlace pnp = (PetriNetPlace) node;
				if (node.isSelected()) {
					selectedNodeList.add(pnp.original());
				}
				allNodeList.add(pnp.original());
			}
		}
		if (selectedNodeList.size() > 0) {
			return selectedNodeList;
		} else {
			return allNodeList;
		}
	}

	/**
	 * Returns the petri net transitions which are currently selected in the GUI
	 * of this graph. If no transitions are selected, all transitions of the
	 * petri net are returned.
	 * 
	 * @return ArrayList<Transition> selectedTransitionList the selected
	 *         transitions or ArrayList<Transition> AllTransitionList all
	 *         transitions in the petrinet
	 */
	public List<Transition> getSelectedPetriNetTransitions() {
		ArrayList<Transition> selectedTransitionList = new ArrayList<Transition>();
		ArrayList<Transition> allTransitionList = new ArrayList<Transition>();
		for (GGNode node : this.nodes()) {
			if (node instanceof PetriNetTransition) {
				PetriNetTransition pnt = (PetriNetTransition) node;
				if (node.isSelected()) {
					selectedTransitionList.add(pnt.original());
				}
				allTransitionList.add(pnt.original());
			}
		}
		if (selectedTransitionList.size() > 0) {
			return selectedTransitionList;
		} else {
			return allTransitionList;
		}
	}
}
