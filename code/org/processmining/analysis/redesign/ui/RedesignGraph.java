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
package org.processmining.analysis.redesign.ui;

import java.awt.Color;
import java.util.ArrayList;

import org.deckfour.gantzgraf.canvas.GGCanvas;
import org.deckfour.gantzgraf.event.GGGraphListener;
import org.deckfour.gantzgraf.model.GGEdge;
import org.deckfour.gantzgraf.model.GGGraph;
import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.painter.GGEdgeGenericPainter;
import org.deckfour.gantzgraf.painter.GGEdgePainter;
import org.processmining.analysis.redesign.ui.RedesignAnalysisUI.RedesignType;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * @author Mariska Netjes (m.netjes@tue.nl)
 */
public class RedesignGraph extends GGGraph {

	private RedesignAnalysisUI parent;
	private int rank = 1;

	private static Color lineColor = new Color(50, 50, 40);
	private static Color labelColor = new Color(60, 60, 60);
	private static GGEdgePainter edgePainter = new GGEdgeGenericPainter(
			lineColor, labelColor);

	/**
	 * Creates the initial tree to display in the UI
	 * 
	 * @param parent
	 *            RedesignAnalysisUI the UI that displays the tree
	 * @param original
	 *            HLPetriNet the model associated to the only node in the tree
	 */
	public RedesignGraph(RedesignAnalysisUI parent, HLPetriNet original) {
		super(Direction.LEFT_TO_RIGHT);
		this.parent = parent;
		this.add(createNode(original, new String[] { "Original_0" }, 0));
	}

	/**
	 * Determines the parent node for the initial model (=parent) for which an
	 * alternative model (=model) has been created with a given redesign type.
	 * Decides on the labeling and id of the node that needs to be created for
	 * the alternative model
	 * 
	 * @param parent
	 *            HLPetriNet the model that has been redesigned
	 * @param model
	 *            HLPetriNet the alternative model, i.e., the result of the
	 *            redesign
	 * @param type
	 *            RedesignType the type of redesign
	 * @return
	 */
	public RedesignNode addRedesign(HLPetriNet parent, HLPetriNet model,
			RedesignType type) {
		RedesignNode parentNode = null;
		for (GGNode node : nodes()) {
			if (((RedesignNode) node).getModel().equals(parent)) {
				parentNode = (RedesignNode) node;
				break;
			}
		}
		if (parentNode == null) {
			throw new AssertionError("no parent node found!");
		}
		int id = rank;
		rank++;
		String[] label = { type.name() + "_" + id };
		return addRedesign(parentNode, model, label, id);
	}

	/**
	 * Creates a new node associated with the alternative model with the given
	 * label and id. The node is connected to the parent node, i.e., the node
	 * where the redesign that resulted in the alternative model has been
	 * started.
	 * 
	 * @param parent
	 *            RedesignNode the parent node which model is redesigned
	 * @param model
	 *            HLPetriNet the alternative model, i.e., the result of the
	 *            redesign
	 * @param label
	 *            String[] the label of the new node
	 * @param id
	 *            int the id of the new node
	 * @return RedesignNode the created node that is associated to the
	 *         alternative model
	 */
	public RedesignNode addRedesign(RedesignNode parent, HLPetriNet model,
			String[] label, int id) {
		RedesignNode node = createNode(model, label, id);
		this.add(node);
		this.add(new GGEdge(parent, node, true, null, edgePainter, 0.3f));
		return node;
	}

	public RedesignNode getNode(int id) {
		for (GGNode gNode : this.nodes()) {
			RedesignNode node = (RedesignNode) gNode;
			if (node.getModelID() == id) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Returns the redesign nodes of this graph.
	 * 
	 * @return ArrayList<RedesignNode> redesignNodes all redesign nodes
	 */
	public ArrayList<RedesignNode> getRedesignNodes() {
		ArrayList<RedesignNode> redesignNodes = new ArrayList<RedesignNode>();
		for (GGNode node : this.nodes()) {
			RedesignNode redesignNode = (RedesignNode) node;
			redesignNodes.add(redesignNode);
		}
		return redesignNodes;
	}

	/**
	 * Returns the redesign nodes which are currently selected in the GUI of
	 * this graph.
	 * 
	 * @return ArrayList<RedesignNode> selectedRedesignNodes the selected
	 *         redesign nodes
	 */
	public ArrayList<RedesignNode> getSelectedRedesignNodes() {
		ArrayList<RedesignNode> selectedRedesignNodes = new ArrayList<RedesignNode>();
		for (GGNode node : this.nodes()) {
			RedesignNode redesignNode = (RedesignNode) node;
			if (node.isSelected()) {
				selectedRedesignNodes.add(redesignNode);
			}
		}
		return selectedRedesignNodes;
	}

	/**
	 * Returns the redesign nodes which are currently selected in the GUI of
	 * this graph.
	 * 
	 * @return ArrayList<RedesignNode> simRedesignNodes the redesign nodes
	 *         selected for simulation
	 */
	public ArrayList<RedesignNode> getRedesignNodesForSimulation() {
		ArrayList<RedesignNode> simRedesignNodes = new ArrayList<RedesignNode>();
		for (GGNode node : this.nodes()) {
			RedesignNode redesignNode = (RedesignNode) node;
			if (redesignNode.isSelectedForSimulation()) {
				simRedesignNodes.add(redesignNode);
			}
		}
		return simRedesignNodes;
	}

	/**
	 * Returns the redesign nodes which are currently selected in the GUI of
	 * this graph.
	 * 
	 * @return ArrayList<RedesignNode> simRedesignNodes the redesign nodes
	 *         selected for simulation
	 */
	public ArrayList<RedesignNode> getSimulatedRedesignNodes() {
		ArrayList<RedesignNode> simRedesignNodes = new ArrayList<RedesignNode>();
		for (GGNode node : this.nodes()) {
			RedesignNode redesignNode = (RedesignNode) node;
			if (redesignNode.isSimulated()) {
				simRedesignNodes.add(redesignNode);
			}
		}
		return simRedesignNodes;
	}

	/**
	 * Returns the redesign nodes which are a direct or indirect successor of a
	 * particular node
	 * 
	 * @return ArrayList<RedesignNode> allSuccessors the succeeding redesign
	 *         nodes NB. Dit gaat ook toe naar het vinden van de transitive
	 *         closure
	 */
	public ArrayList<RedesignNode> getAllSuccessors(RedesignNode firstNode) {
		ArrayList<RedesignNode> allSuccessors = new ArrayList<RedesignNode>();
		if (firstNode.getOutgoingEdges().size() > 0) {
			getDirectSuccessors(firstNode, allSuccessors);
		}
		return allSuccessors;
	}

	public void getDirectSuccessors(RedesignNode node,
			ArrayList<RedesignNode> allSuccessors) {
		for (GGNode sucNode : node.getSuccessors()) {
			RedesignNode succ = (RedesignNode) sucNode;
			allSuccessors.add(succ);
			getDirectSuccessors(succ, allSuccessors);
		}
	}

	/**
	 * Removes all direct and indirect successors of a particular node in the
	 * graph
	 */
	public void removeAllSuccessors(RedesignNode firstNode) {

		if (firstNode.getOutgoingEdges().size() > 0) {
			for (GGNode sucNode : firstNode.getSuccessors()) {
				RedesignNode suc = (RedesignNode) sucNode;
				removeAllSuccessors(suc);
				remove(suc);
			}
		}
	}

	/**
	 * Creates an additional node in a tree and displays the associated model
	 * 
	 * @param model
	 *            HLPetriNet the model to display
	 * @param label
	 *            String[] the label of the new node
	 * @param id
	 *            int the id of the new node
	 * @return node RedesignNode the created node
	 */
	private RedesignNode createNode(HLPetriNet model, String[] label, int id) {
		final RedesignNode node = new RedesignNode(model, label, id);

		/**
		 * set the original node to selectedForSimulation, because the node
		 * should always be simulated for comparison with other nodes
		 */
		if (node.getModelID() == 0) {
			node.setSelectedForSimulation(true);
		}

		/**
		 * set a graph listener to be able to detect the selection of the node
		 */
		node.setGraphListener(new GGGraphListener() {

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
			 * at selection of the node, the associated model is displayed.
			 */
			public void nodeSelected(GGCanvas canvas, GGGraph graph,
					GGNode node, float x, float y) {
				for (GGNode n : nodes()) {
					if (n.equals(node)) {
						n.setSelected(true);
					} else {
						n.setSelected(false);
					}
				}
				updateView();
				parent.showModel(((RedesignNode) node).getModel());
			}

			public void nodeSelectionReset(GGCanvas canvas, GGGraph graph,
					GGNode node, float x, float y) {
				// TODO Auto-generated method stub

			}

		});
		return node;
	}

	/**
	 * Cloning of the graph / tree
	 */
	public Object clone() {
		RedesignGraph clone = (RedesignGraph) super.clone();
		clone.parent = parent;
		return clone;
	}

}
