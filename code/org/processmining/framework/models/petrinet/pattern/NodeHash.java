package org.processmining.framework.models.petrinet.pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.processmining.framework.models.bpel.util.Quadruple;
import org.processmining.framework.models.bpel.util.Triple;
import org.processmining.framework.models.petrinet.Choice;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNetNavigation;
import org.processmining.framework.models.petrinet.Place;

import att.grappa.Edge;
import att.grappa.Node;

/**
 * <p>
 * Title: NodeHash
 * </p>
 * 
 * <p>
 * Description: A short description of properties of a node.
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
public class NodeHash
		extends
		Quadruple<List<Triple<Integer, Integer, Integer>>, Integer, Integer, Integer> {

	private NodeHash(List<Triple<Integer, Integer, Integer>> first,
			Integer second, Integer third, Integer fourth) {
		super(first, second, third, fourth);
	}

	public static NodeHash create(Node node) {
		List<Triple<Integer, Integer, Integer>> neighborhood = new ArrayList<Triple<Integer, Integer, Integer>>();
		Set<Node> nodes = new LinkedHashSet<Node>();
		nodes.addAll(PetriNetNavigation.getIncomingNodes(node));
		nodes.addAll(PetriNetNavigation.getOutgoingNodes(node));
		for (Node aNode : nodes) {
			neighborhood.add(getHash(aNode));
		}
		Collections.sort(neighborhood,
				new Comparator<Triple<Integer, Integer, Integer>>() {
					public int compare(Triple<Integer, Integer, Integer> arg0,
							Triple<Integer, Integer, Integer> arg1) {
						int c = arg0.first.compareTo(arg1.first);
						if (c == 0) {
							int cc = arg0.second.compareTo(arg1.second);
							if (cc == 0)
								return arg0.third.compareTo(arg1.third);
							else
								return cc;
						} else
							return c;
					}
				});
		Triple<Integer, Integer, Integer> hash = getHash(node);
		return new NodeHash(neighborhood, hash.first, hash.second, hash.third);
	}

	private static Triple<Integer, Integer, Integer> getHash(Node node) {
		if (node instanceof Place) {
			Choice choice = getChoice((Place) node);
			Collection<Edge> outEdges = node.getOutEdges();
			if (outEdges != null && !outEdges.isEmpty())
				return Triple.create(node.inDegree(), node.outDegree(), 0);
			else if (choice == Choice.EXPLICIT)
				return Triple.create(node.inDegree(), node.outDegree(), 1);
			else if (choice == Choice.IMPLICIT)
				return Triple.create(node.inDegree(), node.outDegree(), 2);
			return Triple.create(node.inDegree(), node.outDegree(), 3);
		}
		return Triple.create(node.inDegree(), node.outDegree(), 4);
	}

	/**
	 * Calculates the choice of a place
	 * 
	 * @param place
	 *            - A place
	 * @return The choice type of that place
	 */
	public static Choice getChoice(Place place) {
		if (place.outDegree() == 0)
			return Choice.NOT_CHOICE;
		Collection<Node> nodes = PetriNetNavigation.getIncomingNodes(place);
		nodes.retainAll(PetriNetNavigation.getOutgoingNodes(place));
		if (!nodes.isEmpty())
			return Choice.NOT_CHOICE;
		boolean explicit = true, implicit = true;
		for (Edge edge : place.getOutEdges()) {
			PNEdge pnedge = (PNEdge) edge;
			if (pnedge.getTransition().isInvisibleTask())
				explicit = false;
			else
				implicit = false;
		}
		if (explicit)
			return Choice.EXPLICIT;
		else if (implicit)
			return Choice.IMPLICIT;
		else
			return Choice.NOT_DEFINED;
	}

}
