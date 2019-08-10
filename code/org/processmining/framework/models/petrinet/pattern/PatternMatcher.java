package org.processmining.framework.models.petrinet.pattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.bpel.util.Triple;
import org.processmining.framework.models.petrinet.Choice;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.PetriNetNavigation;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.PnmlReader;
import org.semanticweb.kaon2.server.rmi.a;

import att.grappa.Node;

/**
 * <p>
 * Title: PatternMatcher
 * </p>
 * 
 * <p>
 * Description: A toolbox for finding different patterns in a Petri net.
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
public class PatternMatcher {

	private final static Map<Integer, PetriNet> cachedLibraryComponents = new LinkedHashMap<Integer, PetriNet>();

	private static int reduceCount = 0;

	/**
	 * Finds a maximal sequence in a given WF-net
	 * 
	 * @param wfnet
	 *            - The net that is matched in
	 * @return A WF-net that is a marked graph and a state machine, or null if
	 *         no such exist
	 */
	public static Component getMaximalSequence(PetriNet wfnet) {
		Set<Node> sequence = new LinkedHashSet<Node>();
		TreeSet<Set<Node>> sequences = new TreeSet<Set<Node>>(
				new Comparator<Set<Node>>() {
					public int compare(Set<Node> o1, Set<Node> o2) {
						return new Integer(o1.size()).compareTo(new Integer(o2
								.size()));
					}
				});

		for (Node node : wfnet.getNodes()) {
			if (node.outDegree() == 1) {
				Node next = node;
				do {
					sequence.add(next);
					next = PetriNetNavigation.getOutgoingNodes(next).element();
				} while (next.inDegree() == 1 && next.outDegree() == 1);
				if (next.inDegree() == 1)
					sequence.add(next);
			}
			if (countTransitions(sequence) > 1) {
				sequences.add(new LinkedHashSet<Node>(sequence));
			}
			sequence.clear();
		}

		if (sequences.size() == 0)
			return null;

		PetriNet sequenceNet = wfnet.extractNet(sequences.last());

		return new SequenceComponent(sequenceNet);
	}

	public static List<WellStructuredGraphComponent> getWellStructuredGraphComponents(
			PetriNet wfnet, Set<PetriNet> components) {
		List<WellStructuredGraphComponent> result = new ArrayList<WellStructuredGraphComponent>();
		TreeSet<PetriNet> theComponents = new TreeSet<PetriNet>(
				new Comparator<PetriNet>() {
					public int compare(PetriNet arg0, PetriNet arg1) {
						return arg1.numberOfNodes() - arg0.numberOfNodes();
					}
				});
		theComponents.addAll(components);
		for (PetriNet component : theComponents) {
			if (PatternMatcher.isWellStructured(component)) {
				result.add(new WellStructuredGraphComponent(component));
			}
		}

		return result;
	}

	/**
	 * Counts the number of transitions in a set of nodes.
	 * 
	 * @param nodes
	 *            - A set of nodes
	 * @return The number of transitions
	 */
	private static int countTransitions(Set<Node> nodes) {
		int count = 0;
		for (Node node : nodes)
			if (node instanceof Transition)
				count++;
		return count;
	}

	/**
	 * Tests if a WF-net is well-structured. See WF-net to BPEL paper for a
	 * formal definition
	 * 
	 * @param wfnet
	 *            - A WF-net
	 * @param choices
	 *            - A map over defined on the places in the WF-net that gives
	 *            the type of choice that place represent
	 * @return Answers if the Petri net is well-structured
	 */
	private static boolean isWellStructured(PetriNet wfnet) {
		for (Place place : wfnet.getPlaces()) {
			if (place.outDegree() > 1
					&& (!hasInDegreeLessThanOrEqualToOne(PetriNetNavigation
							.getIncomingNodes(place))))
				return false;
		}
		for (Transition transition : wfnet.getTransitions()) {
			if (transition.inDegree() > 1
					&& !hasInDegreeLessThanOrEqualToOne(PetriNetNavigation
							.getIncomingNodes(transition)))
				return false;
		}
		if (!wfnet.isAcyclic())
			return false;

		return true;
	}

	/**
	 * Answers if source and sink borders a component with the nodes in it
	 * 
	 * @param source
	 *            - A node
	 * @param sink
	 *            - A sink
	 * @param nodes
	 *            - A set of nodes
	 * @return The answer
	 */
	private static boolean isSelfContaining(Node source, Node sink,
			Set<Node> nodes) {
		for (Node node : nodes) {
			if (!node.equals(source))
				for (Node in : PetriNetNavigation.getIncomingNodes(node))
					if (!nodes.contains(in))
						return false;
			if (!node.equals(sink))
				for (Node out : PetriNetNavigation.getOutgoingNodes(node))
					if (!nodes.contains(out))
						return false;
		}
		return true;
	}

	/**
	 * Answers whether or not nodes in a list all have indegree equals to or
	 * less than one
	 * 
	 * @param nodes
	 *            - A list of nodes
	 * @return The answer
	 */
	private static boolean hasInDegreeLessThanOrEqualToOne(List<Node> nodes) {
		if (nodes.isEmpty())
			return true;
		for (Node node : nodes)
			if (node.inDegree() != 1)
				return false;
		return true;
	}

	/**
	 * Answers if a nodes in a list all have indegree and outdegree being
	 * exactly one
	 * 
	 * @param nodes
	 *            - A list of nodes
	 * @return The answer
	 */
	private static boolean hasInAndOutDegreeOne(List<Node> nodes) {
		for (Node node : nodes)
			if (node.inDegree() != 1 || node.outDegree() != 1)
				return false;
		return true;
	}

	private static List<PetriNet> getChoiceComponents(PetriNet wfnet,
			Choice choice, Map<String, Choice> choices) {
		List<PetriNet> result = new ArrayList<PetriNet>();
		for (Place place : wfnet.getPlaces()) {
			LinkedList<Node> outNodes = PetriNetNavigation
					.getOutgoingNodes(place);
			if (place.outDegree() > 1
					&& ((choices == null && NodeHash.getChoice(place) == choice) || choices
							.get(place.getName()) == choice)
					&& hasInAndOutDegreeOne(outNodes)
					&& hasSameOutgoingNode(outNodes)
					&& PetriNetNavigation.getOutgoingNodes(outNodes.element())
							.element().inDegree() == place.outDegree()) {
				Set<Node> nodes = new LinkedHashSet<Node>();
				nodes.add(place);
				nodes.addAll(PetriNetNavigation.getOutgoingTransitions(place));
				nodes.addAll(PetriNetNavigation.getOutgoingPlaces(place));
				result.add(wfnet.extractNet(nodes));
			}
		}
		return result;
	}

	public static List<ExplicitChoiceComponent> getExplicitChoiceComponents(
			PetriNet wfnet, Map<String, Choice> choices) {
		List<ExplicitChoiceComponent> result = new ArrayList<ExplicitChoiceComponent>();
		for (PetriNet explicitChoice : getChoiceComponents(wfnet,
				Choice.EXPLICIT, choices)) {
			result.add(new ExplicitChoiceComponent(explicitChoice));
		}
		return result;
	}

	public static List<ImplicitChoiceComponent> getImplicitChoiceComponents(
			PetriNet wfnet, Map<String, Choice> choices) {
		List<ImplicitChoiceComponent> result = new ArrayList<ImplicitChoiceComponent>();
		for (PetriNet implicitChoice : getChoiceComponents(wfnet,
				Choice.IMPLICIT, choices)) {
			result.add(new ImplicitChoiceComponent(implicitChoice));
		}
		return result;
	}

	/**
	 * Tests whether a list of nodes all have the same outgoing node
	 * 
	 * @param nodes
	 *            - A list of nodes
	 * @return The answer
	 */
	private static boolean hasSameOutgoingNode(List<Node> nodes) {
		Set<Node> outgoingNodes = new LinkedHashSet<Node>();
		for (Node node : nodes) {
			outgoingNodes.addAll(PetriNetNavigation.getOutgoingNodes(node));
			if (outgoingNodes.size() > 1)
				return false;
		}
		return true;
	}

	/**
	 * Finds a WHILE-component in a WF-net
	 * 
	 * @param wfnet
	 *            - A WF-net
	 * @param choices
	 *            - A map over all places in the WF-net to their choice type
	 * @return A workflow net that represent a while loop, or null if no such is
	 *         found
	 */
	public static Component getWhile(PetriNet wfnet) {
		for (Transition source : wfnet.getTransitions())
			if (source.outDegree() == 1) {
				Place place = PetriNetNavigation.getOutgoingPlaces(source)
						.firstElement();
				if (place.inDegree() == 2
						&& place.outDegree() == 2
						&& !PetriNetNavigation.getOutgoingTransitions(place)
								.contains(source)) {
					List<Transition> transitions = PetriNetNavigation
							.getOutgoingTransitions(place);
					Transition t1 = transitions.get(0);
					Transition t2 = transitions.get(transitions.size() - 1);
					Transition iter = null, sink = null;
					boolean isIter = false;
					if (PetriNetNavigation.getOutgoingPlaces(t1)
							.contains(place)) {
						iter = t1;
						sink = t2;
						isIter = true;
					} else if (PetriNetNavigation.getOutgoingPlaces(t2)
							.contains(place)) {
						iter = t2;
						sink = t1;
						isIter = true;
					}
					if (isIter && iter.outDegree() == 1 && sink.inDegree() == 1) {
						Set<Node> nodes = new LinkedHashSet<Node>();
						nodes.add(source);
						nodes.add(place);
						nodes.add(t1);
						nodes.add(t2);
						return new WhileComponent(wfnet.extractNet(nodes));
					}
				}
			}
		return null;
	}

	/**
	 * This method finds all components that exists in the WF-net. The
	 * components are order by node size, so the the smallest WF-nets are first
	 * in an iteration over the answer
	 * 
	 * @param wfnet
	 *            - The WF-net
	 * @return A tree set of components in the WF-net
	 */
	public static TreeSet<PetriNet> getComponents(PetriNet wfnet) {
		TreeSet<PetriNet> components = new TreeSet<PetriNet>(
				new Comparator<PetriNet>() {
					public int compare(PetriNet arg0, PetriNet arg1) {
						return arg0.numberOfNodes() - arg1.numberOfNodes();
					}
				});
		components.add(wfnet);
		Set<Pair<Node, Node>> borderCandidates = new LinkedHashSet<Pair<Node, Node>>();
		Node source = wfnet.getSource();
		Node sink = wfnet.getSink();
		for (Node a : wfnet.getNodes())
			for (Node b : wfnet.getNodes())
				if (a != b
						&& !((a == source && b == sink) || (a == sink && b == source)))
					borderCandidates.add(Pair.create(a, b));
		Set<Node> sinks = new LinkedHashSet<Node>();
		for (Pair<Node, Node> borderCandidate : borderCandidates) {
			try {
				Set<Node> nodes = new LinkedHashSet<Node>();
				getNodesInComponent(borderCandidate.first,
						borderCandidate.first, borderCandidate.second, nodes,
						wfnet);
				if (countTransitions(nodes) > 1
						&& isSelfContaining(borderCandidate.first,
								borderCandidate.second, nodes)) {
					components.add(wfnet.extractNet(nodes));
					sinks.add(borderCandidate.second);
				}
			} catch (Exception e) {
				// not a component
			}
		}
		return components;
	}

	/**
	 * Finds all nodes in a WF-net that are between a source and a sink and add
	 * them to the argument nodes
	 * 
	 * @param current
	 *            - The current node being processed
	 * @param source
	 *            - The source of the component
	 * @param sink
	 *            - The sink of the component
	 * @param nodes
	 *            - Nodes found so far
	 * @param wfnet
	 *            - A WF-net
	 * @throws Exception
	 *             - If source and sink does not define a proper component
	 */
	public static void getNodesInComponent(Node current, Node source,
			Node sink, Set<Node> nodes, PetriNet wfnet) throws Exception {
		if ((current == source && nodes.size() > 1)
				|| (current == wfnet.getSink() && sink != wfnet.getSink()))
			throw new Exception();
		if (nodes.add(current) && current != sink) {
			for (Node out : PetriNetNavigation.getOutgoingNodes(current))
				getNodesInComponent(out, source, sink, nodes, wfnet);
		}
	}

	/**
	 * Finds an isomorphism between two Petri nets
	 * 
	 * @param a
	 *            - The first Petri net
	 * @param b
	 *            - The second Petri net
	 * @param choices
	 *            - A map over choices in both from places to their particular
	 *            place type
	 * @return An isomorphism between the nodes in pn1 and pn2, or null if no
	 *         such exists
	 */
	public static Map<Node, Node> getIsomorph(PetriNet a, PetriNet b) {
		// Simple check to see whether two Petri nets have equally many places
		// and equally many transitions.
		if (a.numberOfPlaces() != b.numberOfPlaces()
				|| a.numberOfTransitions() != b.numberOfTransitions()
				|| a.getEdges().size() != b.getEdges().size())
			return null;

		// All nodes are undecided to begin with, before we try build the
		// isomorphism
		Map<NodeHash, Set<Node>> equivalenceClassesInA = hashNodes(a.getNodes());
		Map<NodeHash, Set<Node>> equivalenceClassesInB = hashNodes(b.getNodes());

		// No nodes are initially decided for the isomorphism
		Map<Node, Node> isomorphism = new TreeMap<Node, Node>(
				new Comparator<Node>() {
					public int compare(Node arg0, Node arg1) {
						return arg0.getName().toLowerCase().compareTo(
								arg1.getName().toLowerCase());
					}
				});

		// For each equivalence class hash representation, see if there is an
		// equivalent equivalence class in each net that only have one member.
		for (NodeHash hash : new ArrayList<NodeHash>(equivalenceClassesInA
				.keySet())) {
			Set<Node> nodesInA = equivalenceClassesInA.get(hash);
			Set<Node> nodesInB = equivalenceClassesInB.get(hash);
			if (nodesInB == null || nodesInA.size() != nodesInB.size())
				return null;

			// if there is an actutal mach add them to the isomorphism
			if (nodesInA.size() == 1) {
				Node na = nodesInA.iterator().next();
				Node nb = nodesInB.iterator().next();
				isomorphism.put(na, nb);
				equivalenceClassesInA.remove(hash);
				equivalenceClassesInB.remove(hash);
			}
		}

		if (equivalenceClassesInA.isEmpty() && equivalenceClassesInB.isEmpty())
			return isomorphism;

		// Propagate the information of the partial isomorphism through the rest
		// of the nets
		return propagateKnownNodeInformation(equivalenceClassesInA,
				equivalenceClassesInB, isomorphism, a.numberOfNodes());
	}

	/**
	 * This is used by the findIsomorph method. This propagate information about
	 * known nodes to find a better information about how the nets being looked
	 * at compares.
	 * 
	 * @param equivalenceClassesInA
	 *            - Nodes in A that is not fixed yet
	 * @param equivalenceClassesInB
	 *            - Nodes in B that is not fixed yet
	 * @param isomorphism
	 *            - Nodes found that must map to each other for an isomorphism
	 *            to exist
	 * @param wfnetSize
	 *            - The size of the components being matched up
	 * @param choices
	 *            - A map from places to their particular choice type
	 * @return A possible isomorphism between the two components, or null if
	 *         none is found
	 */
	private static Map<Node, Node> propagateKnownNodeInformation(
			Map<NodeHash, Set<Node>> equivalenceClassesInA,
			Map<NodeHash, Set<Node>> equivalenceClassesInB,
			Map<Node, Node> isomorphism, int wfnetSize) {
		// Each node in the isomorphism is to unprocessed
		Queue<Node> unprocessedNodes = new LinkedList<Node>(isomorphism
				.keySet());

		List<Triple<NodeHash, Node, Node>> addedNodes = new ArrayList<Triple<NodeHash, Node, Node>>();

		// While more information from the isomorphism can be propagated
		// continue to build the isomorphism. When the iteration stops no more
		// nodes can be added to the isomophism, since we there are no more
		// unique matches left in the two nets
		while (!unprocessedNodes.isEmpty() && isomorphism.size() != wfnetSize) {
			Node nodeA = unprocessedNodes.remove();
			Node nodeB = isomorphism.get(nodeA);

			if (!updateNeighbors(isomorphism, PetriNetNavigation
					.getOutgoingNodes(nodeA), PetriNetNavigation
					.getOutgoingNodes(nodeB), equivalenceClassesInA,
					equivalenceClassesInB, unprocessedNodes, addedNodes)
					|| !updateNeighbors(isomorphism, PetriNetNavigation
							.getIncomingNodes(nodeA), PetriNetNavigation
							.getIncomingNodes(nodeB), equivalenceClassesInA,
							equivalenceClassesInB, unprocessedNodes, addedNodes)) {
				for (Triple<NodeHash, Node, Node> triple : addedNodes) {
					isomorphism.remove(triple.second);
					isomorphism.remove(triple.third);
					equivalenceClassesInA.get(triple.first).add(triple.second);
					equivalenceClassesInB.get(triple.first).add(triple.third);
				}
				return null;
			}
			if (unprocessedNodes.isEmpty() && isomorphism.size() < wfnetSize) {
				for (NodeHash hash : new ArrayList<NodeHash>(
						equivalenceClassesInA.keySet())) {
					Set<Node> klass = equivalenceClassesInA.get(hash);
					if (klass.size() == 1) {
						Node na = klass.iterator().next();
						Node nb = equivalenceClassesInB.get(hash).iterator()
								.next();
						isomorphism.put(na, nb);
						unprocessedNodes.add(na);
						equivalenceClassesInA.remove(hash);
						equivalenceClassesInB.remove(hash);
						addedNodes.add(Triple.create(hash, na, nb));
					}
				}
			}
		}

		// All nodes are matched and mapped by the isomorphism from nodes in A
		// to nodes B
		if (equivalenceClassesInA.isEmpty())
			return isomorphism;

		// After this point we start to guess for a possible match and then test
		// it by trying to find an isomorphism with that pair

		List<Node> allNodesInEqA = new ArrayList<Node>();
		for (Set<Node> nodes : equivalenceClassesInA.values())
			allNodesInEqA.addAll(nodes);

		// Even though we cannot add any more nodes to the isomphism, we know
		// that the equivalence classes are equal in the remaining classes of
		// nets A and B
		for (Node nodeToFixInA : allNodesInEqA) {
			NodeHash nodeToFixHash = NodeHash.create(nodeToFixInA);
			equivalenceClassesInA.get(nodeToFixHash).remove(nodeToFixInA);
			Set<Node> equivalenceClassInB = equivalenceClassesInB
					.get(nodeToFixHash);
			// One for one of the unfixed nodes in A, we try to match them
			// to a node in B of the same equivalens class. We then try to
			// propagate the know information of that isomorphism. If the
			// construction of this new new isomorphism fails then we try
			// with the next in the equivalences class
			for (Node nodeToFixInB : new ArrayList<Node>(equivalenceClassInB)) {
				equivalenceClassInB.remove(nodeToFixInB);
				isomorphism.put(nodeToFixInA, nodeToFixInB);
				Map<Node, Node> newIsomorphism = propagateKnownNodeInformation(
						equivalenceClassesInA, equivalenceClassesInB,
						isomorphism, wfnetSize);
				if (newIsomorphism != null)
					return newIsomorphism;
				isomorphism.remove(nodeToFixInA);
				equivalenceClassInB.add(nodeToFixInB);
				if (!equivalenceClassesInB.containsKey(nodeToFixHash))
					equivalenceClassesInB.put(nodeToFixHash,
							new LinkedHashSet<Node>());
			}

			// If no match can be made why try with another of the nodes in
			// the equivalence class of A
			if (equivalenceClassesInA.get(nodeToFixHash) == null)
				equivalenceClassesInA.put(nodeToFixHash,
						new LinkedHashSet<Node>());
			equivalenceClassesInA.get(nodeToFixHash).add(nodeToFixInA);
		}

		if (isomorphism.size() != wfnetSize)
			return null;

		return isomorphism;
	}

	/**
	 * This is used by propagateKnownNodeInformation. It is used when a node has
	 * been decided to fit in the isomorphism, to update the neighbors of it,
	 * thereby getting more information about a possible isomorphism
	 * 
	 * @param isomorphism
	 *            - The set of nodes that has been decided that they map to each
	 *            other
	 * @param nodesA
	 *            - The nodes in A
	 * @param nodesB
	 *            - The nodes in B
	 * @param equivalenceClassesInA
	 *            - The undecided nodes in A
	 * @param equivalenceClassesInB
	 *            - The undecided nodes in B
	 * @param unprocessedNodes
	 *            - The nodes that has not yet been processed
	 * @param choices
	 *            - A map from places to their choice type
	 * @param addedNodes
	 * @return Answers, whether or not the update of the neighbors went well. If
	 *         true the neighbors where updated and no conflicts where found. If
	 *         false the propagation of the new information about a node yield
	 *         that no isomorphism can exist
	 */
	private static boolean updateNeighbors(Map<Node, Node> isomorphism,
			Collection<Node> nodesA, Collection<Node> nodesB,
			Map<NodeHash, Set<Node>> equivalenceClassesInA,
			Map<NodeHash, Set<Node>> equivalenceClassesInB,
			Queue<Node> unprocessedNodes,
			List<Triple<NodeHash, Node, Node>> addedNodes) {
		Map<NodeHash, Node> uniquesA = getUniqueNodes(nodesA,
				hashNodes(nodesA), isomorphism, true);
		Map<NodeHash, Node> uniquesB = getUniqueNodes(nodesB,
				hashNodes(nodesB), isomorphism, false);

		// If there is not the same amount of equivalence classes with one
		// member in the neighborhood of the isomorphism, no isomorphism can
		// be found
		if (uniquesA.size() != uniquesB.size()
				|| !uniquesA.keySet().containsAll(uniquesB.keySet()))
			return false;

		// Add each pair of members to the isomorphism. Then add neighbors
		// of each new pair in the isomorphism to the set of nodes that needs to
		// be checked for addition to the isomorphism
		for (NodeHash hash : uniquesA.keySet()) {
			Node nodeInA = uniquesA.get(hash);
			Node nodeInB = uniquesB.get(hash);
			isomorphism.put(nodeInA, nodeInB);
			for (Node node : equivalenceClassesInA.get(hash)) {
				Set<Node> newUnprocessed = new LinkedHashSet<Node>();
				newUnprocessed
						.addAll(PetriNetNavigation.getIncomingNodes(node));
				newUnprocessed
						.addAll(PetriNetNavigation.getOutgoingNodes(node));
				newUnprocessed.retainAll(isomorphism.keySet());
				unprocessedNodes.removeAll(newUnprocessed);
				unprocessedNodes.addAll(newUnprocessed);
			}
			equivalenceClassesInA.get(hash).remove(nodeInA);
			equivalenceClassesInB.get(hash).remove(nodeInB);
			addedNodes.add(Triple.create(hash, nodeInA, nodeInB));
			unprocessedNodes.add(nodeInA);
		}
		return true;
	}

	/**
	 * Finds all nodes that is not decided yet, but is uniquely found in the set
	 * of undecided nodes by its hash value
	 * 
	 * @param nodes
	 *            - A collection of nodes
	 * @param name
	 *            - A map from node hashes to their equivalence classes
	 * @param isomorphism
	 *            - Nodes that must map to each other
	 * @param choices
	 *            - A map from places to their choice type
	 * @return A map on a uniquely decidable of nodes
	 */
	private static Map<NodeHash, Node> getUniqueNodes(Collection<Node> nodes,
			Map<NodeHash, Set<Node>> name, Map<Node, Node> isomorphism,
			boolean isInA) {
		// Nodes in the isomorphsim is not considered
		for (Node node : new LinkedList<Node>(nodes))
			if ((isInA && isomorphism.containsKey(node))
					|| (!isInA && isomorphism.containsValue(node))) {
				nodes.remove(node);
				name.get(NodeHash.create(node)).remove(node);
			}

		// Find all neighbours that are in a equivalence class with one member
		Map<NodeHash, Node> unique = new LinkedHashMap<NodeHash, Node>();
		for (Node node : nodes) {
			NodeHash hash = NodeHash.create(node);
			if (name.get(hash).size() == 1)
				unique.put(hash, node);
		}
		return unique;
	}

	/**
	 * Generates equivalence classes from the input nodes
	 * 
	 * @param nodes
	 *            - Nodes to be generated equivalence classes from
	 * @param choices
	 *            - A map from places to their choice type
	 * @return A map from a node hash to the nodes in its equivalence class
	 */
	private static Map<NodeHash, Set<Node>> hashNodes(
			Collection<? extends Node> nodes) {
		// The result of the method
		Map<NodeHash, Set<Node>> hashedNodes = new LinkedHashMap<NodeHash, Set<Node>>();

		// Build the equivalences classes by, hashing all nodes and adding them
		// to their equivalence class
		for (Node node : nodes) {
			NodeHash hash = NodeHash.create(node);
			Set<Node> equivalenceClass = hashedNodes.get(hash);
			if (equivalenceClass == null) {
				equivalenceClass = new LinkedHashSet<Node>();
				hashedNodes.put(hash, equivalenceClass);
			}
			equivalenceClass.add(node);
		}

		return hashedNodes;
	}

	/**
	 * Finds a component from a the user defined LIBRARY-components
	 * 
	 * @param wfnet
	 *            - A WF-net
	 * @param libraryComponent
	 *            - A library component
	 * @param components
	 *            - A set of components
	 * @return A component matched by a LIBRARY-component
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	public static LibraryComponent getComponentFromLibrary(PetriNet wfnet,
			String componentPath, String libraryPath, Set<PetriNet> components)
			throws FileNotFoundException, Exception {
		for (PetriNet component : components) {
			PetriNet libraryComponent = loadLibraryComponent(componentPath,
					libraryPath);
			Map<Node, Node> isomorphism = getIsomorph(component,
					libraryComponent);
			if (isomorphism != null) {
				return new LibraryComponent(wfnet
						.extractNet(new LinkedHashSet<Node>(component
								.getNodes())), componentPath, isomorphism);
			}
		}
		return null;
	}

	private synchronized static PetriNet loadLibraryComponent(
			String componentPath, String libraryPath)
			throws FileNotFoundException, Exception {
		File componentFile = new File(libraryPath + "/" + componentPath
				+ ".pnml");
		PetriNet result = cachedLibraryComponents.get(componentFile.hashCode());
		if (result == null) {
			result = new PnmlReader().read(new FileInputStream(componentFile));
			cachedLibraryComponents.put(componentFile.hashCode(), result);
		}
		return result;
	}

	public synchronized static Pair<Transition, Set<Node>> reduce(
			PetriNet wfnet, PetriNet component) {
		Node source = getNode(wfnet.getNodes(), component.getSource());
		Node sink = getNode(wfnet.getNodes(), component.getSink());
		List<Node> incomingNodes = new LinkedList<Node>();
		List<Node> outgoingNodes = new LinkedList<Node>();

		Set<Node> nodesToBeRemoved = new LinkedHashSet<Node>(component
				.getNodes());

		if (source instanceof Place) {
			incomingNodes.add(source);
			nodesToBeRemoved.remove(component.getSource());
		} else
			incomingNodes.addAll(PetriNetNavigation.getIncomingNodes(source));
		if (sink instanceof Place) {
			outgoingNodes.add(sink);
			nodesToBeRemoved.remove(component.getSink());
		} else
			outgoingNodes.addAll(PetriNetNavigation.getOutgoingNodes(sink));

		Transition tc = new Transition(new LogEvent("Fragment" + reduceCount++,
				"complete"), wfnet);
		wfnet.addTransition(tc);

		for (Node node : incomingNodes)
			wfnet
					.addAndLinkEdge(new PNEdge((Place) node, tc), (Place) node,
							tc);
		for (Node node : outgoingNodes)
			wfnet
					.addAndLinkEdge(new PNEdge(tc, (Place) node), tc,
							(Place) node);

		for (Node node : nodesToBeRemoved) {
			wfnet.removeVertex((ModelGraphVertex) getNode(wfnet.getNodes(),
					node));
		}
		return Pair.create(tc, nodesToBeRemoved);
	}

	public static Node getNode(Collection<? extends Node> nodes, Node node) {
		for (Node aNode : nodes)
			if (aNode.getName().equals(node.getName()))
				return aNode;
		return null;
	}

	public static List<StateMachineComponent> getStateMachineComponents(
			PetriNet wfnet, TreeSet<PetriNet> components) {
		List<StateMachineComponent> result = new ArrayList<StateMachineComponent>();
		TreeSet<PetriNet> theComponents = new TreeSet<PetriNet>(
				new Comparator<PetriNet>() {
					public int compare(PetriNet arg0, PetriNet arg1) {
						return arg1.numberOfNodes() - arg0.numberOfNodes();
					}
				});
		theComponents.addAll(components);
		for (PetriNet component : theComponents)
			if (isStateMachine(component))
				result.add(new StateMachineComponent(component));
		return result;
	}

	private static boolean isStateMachine(PetriNet component) {
		for (Transition transition : component.getTransitions())
			if (transition.inDegree() > 1 || transition.outDegree() > 1)
				return false;
		return true;
	}

	public static List<MarkedGraphComponent> getMarkedGraphComponents(
			PetriNet wfnet, TreeSet<PetriNet> components) {
		List<MarkedGraphComponent> result = new ArrayList<MarkedGraphComponent>();
		TreeSet<PetriNet> theComponents = new TreeSet<PetriNet>(
				new Comparator<PetriNet>() {
					public int compare(PetriNet arg0, PetriNet arg1) {
						return arg1.numberOfNodes() - arg0.numberOfNodes();
					}
				});
		theComponents.addAll(components);
		for (PetriNet component : theComponents)
			if (isMarkedGraph(component))
				result.add(new MarkedGraphComponent(component));
		return result;
	}

	private static boolean isMarkedGraph(PetriNet component) {
		for (Place place : component.getPlaces()) {
			if (place.inDegree() > 1 || place.outDegree() > 1)
				return false;
		}
		return true;
	}

	// public static <A> A reduce(PetriNetReducer<A> reducer, PetriNet pn)
	// throws UnreduciblePetriNetException {
	// PetriNet clone = (PetriNet) pn.clone();
	// Map<Transition, A> annotations = new LinkedHashMap<Transition, A>();
	// for (Transition t : pn.getTransitions())
	// annotations.put(t, reducer.createAnnotation(t));
	// while (annotations.size() > 1) {
	// Component component = null;
	// TreeSet<PetriNet> components = null;
	// for (Pattern pattern : reducer.getPatterns()) {
	// if (!pattern.useAdHocPatternRecognition() && components == null)
	// components = getComponents(clone);
	// component = pattern.recognize(components);
	// if (component != null)
	// break;
	// }
	// if (component == null)
	// throw new UnreduciblePetriNetException(reducer, clone, annotations);
	// Pair<Transition, Set<Transition>> pattern =
	// pattern.reduce(clone,component);
	//			
	// }
	// if (annotations.size() == 1)
	// return annotations.get(clone.getTransitions().get(0));
	// throw new UnreduciblePetriNetException(reducer, clone, annotations);
	// }

}
