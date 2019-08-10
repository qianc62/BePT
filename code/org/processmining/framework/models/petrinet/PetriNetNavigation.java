package org.processmining.framework.models.petrinet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import att.grappa.Edge;
import att.grappa.Node;

/**
 * <p>
 * Title: PetriNetNavigation
 * </p>
 * 
 * <p>
 * Description: A toolbox for navigating a Petri net.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: University of Aarhus
 * </p>
 * 
 * @author Kristian Bisgaard Lassen (<a
 *         href="mailto:K.B.Lassen@daimi.au.dk">mailto
 *         :K.B.Lassen@daimi.au.dk</a>)
 * @version 1.0
 */
public class PetriNetNavigation {

	/**
	 * Returns all nodes that has an arc to the input node
	 * 
	 * @param node
	 *            - A node
	 * @return A collection of the incoming nodes
	 */
	public static LinkedList<Node> getIncomingNodes(Node node) {
		Set<Node> seen = new LinkedHashSet<Node>();
		LinkedList<Node> nodes = new LinkedList<Node>();
		Collection<Edge> inEdges = node.getInEdges();
		if (inEdges != null)
			for (Edge arc : inEdges) {
				if (seen.add((Node) arc.getTail()))
					nodes.add(arc.getTail());
			}
		return nodes;
	}

	/**
	 * Returns all nodes that can reach the input node in two steps
	 * 
	 * @param aNode
	 *            - A node
	 * @return A collection of nodes that can reach the input node in two steps
	 */
	public static LinkedList<Node> getIncomingNodesOfIncomingNodes(Node aNode) {
		LinkedList<Node> nodes = new LinkedList<Node>();
		for (Node node : getIncomingNodes(aNode))
			for (Node nextNode : getIncomingNodes(node))
				nodes.add(nextNode);
		return nodes;
	}

	/**
	 * Returns all nodes that has an arc from the output node
	 * 
	 * @param node
	 *            - A node
	 * @return A collection of outgoing nodes
	 */
	public static LinkedList<Node> getOutgoingNodes(Node node) {
		Set<Node> seen = new LinkedHashSet<Node>();
		LinkedList<Node> nodes = new LinkedList<Node>();
		Collection<Edge> outEdges = node.getOutEdges();
		if (outEdges != null)
			for (Edge arc : outEdges) {
				if (seen.add((Node) arc.getHead()))
					nodes.add((Node) arc.getHead());
			}
		return nodes;
	}

	/**
	 * Returns the all nodes that can be reached in two steps from the input
	 * node
	 * 
	 * @param aNode
	 *            - A node
	 * @return A collection of nodes that can be reached in two steps from this
	 *         node
	 */
	public static LinkedList<Node> getOutgoingNodesOfOutgoingNodes(Node aNode) {
		LinkedList<Node> nodes = new LinkedList<Node>();
		for (Node node : getOutgoingNodes(aNode))
			for (Node nextNode : getOutgoingNodes(node))
				nodes.add(nextNode);
		return nodes;
	}

	/**
	 * @see PetriNetNavigation#getOutgoingNodes(Node)
	 */
	public static Vector<Place> getOutgoingPlaces(Node n) {
		Vector<Place> places = new Vector<Place>();
		if (n instanceof Transition)
			for (Node node : getOutgoingNodes(n))
				places.add((Place) node);
		else
			for (Node node : getOutgoingNodesOfOutgoingNodes(n))
				places.add((Place) node);
		return places;
	}

	/**
	 * @see PetriNetNavigation#getIncomingNodes(Node)
	 */
	public static Vector<Place> getIncomingPlaces(Node n) {
		Vector<Place> places = new Vector<Place>();
		if (n instanceof Transition)
			for (Node node : getIncomingNodes(n))
				places.add((Place) node);
		else
			for (Node node : getIncomingNodesOfIncomingNodes(n))
				places.add((Place) node);
		return places;
	}

	/**
	 * @see PetriNetNavigation#getOutgoingNodes(Node)
	 */
	public static List<Transition> getOutgoingTransitions(Node n) {
		Set<Transition> transitions = new LinkedHashSet<Transition>();
		if (n instanceof Place)
			for (Node node : getOutgoingNodes(n))
				transitions.add((Transition) node);
		else
			for (Node node : getOutgoingNodesOfOutgoingNodes(n))
				transitions.add((Transition) node);
		return new ArrayList<Transition>(transitions);
	}

	/**
	 * @see PetriNetNavigation#getIncomingNodes(Node)
	 */
	public static List<Transition> getIncomingTransitions(Node n) {
		Set<Transition> transitions = new LinkedHashSet<Transition>();
		if (n instanceof Place)
			for (Node node : getIncomingNodes(n))
				transitions.add((Transition) node);
		else
			for (Node node : getIncomingNodesOfIncomingNodes(n))
				transitions.add((Transition) node);
		return new ArrayList<Transition>(transitions);
	}

	/**
	 * Returns the transition end of the input arc
	 * 
	 * @param arc
	 *            - An arc
	 * @return A transition of the input arc
	 */
	public static Transition getTransition(Edge arc) {
		if (arc.getHead() instanceof Transition)
			return (Transition) arc.getHead();
		return (Transition) arc.getTail();
	}

}
