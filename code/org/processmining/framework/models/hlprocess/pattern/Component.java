package org.processmining.framework.models.hlprocess.pattern;

/**
 * Author: Mariska Netjes
 * (c) 2008 Technische Universiteit Eindhoven and STW
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.processmining.framework.models.petrinet.*;

import att.grappa.Node;
import org.processmining.framework.models.hlprocess.hlmodel.*;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLAttribute;
import java.util.Iterator;
import org.processmining.framework.ui.Message;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLTypes;

/**
 * Defines the process part that is selected from a HLPetriNet for redesign. For
 * details see BETA report WP240. The component is a sub net of the HLPetriNet
 * control flow. Different type of components are used as input for the various
 * redesigns.
 */

public class Component {

	protected HLPetriNet hlNet = null;
	protected Collection<PNNode> nodes = new HashSet<PNNode>();

	/**
	 * Basic constructor
	 * 
	 * @param inputModel
	 *            HLPetriNet Model that should be redesigned.
	 * @param selectedNodes
	 *            Collection<PNNode> The nodes (places and transitions) that are
	 *            selected, i.e., the process part that is changed. Note:
	 *            Collection can be implemented as Set or List.
	 */
	public Component(HLPetriNet inputModel, Collection<PNNode> selectedNodes) {
		hlNet = inputModel;
		for (PNNode node : selectedNodes) {
			nodes.add(node);
		}
	}

	/**
	 * Retrieves the collection of nodes, can be implemented as set or list
	 * 
	 * @return Collection<PNNode> The included nodes
	 */
	public Collection<PNNode> getNodes() {
		return nodes;
	}

	/**
	 * Retrieves the list of nodes
	 * 
	 * @return List<PNNode> The included nodes
	 */
	public List<PNNode> getNodeList() {
		List<PNNode> nodeList = new ArrayList<PNNode>();
		for (PNNode node : nodes) {
			nodeList.add(node);
		}
		return nodeList;
	}

	/**
	 * Retrieves the list of transitions
	 * 
	 * @return List<Transition> The included transitions
	 */
	public List<Transition> getTransitions() {
		List<Transition> list = new ArrayList<Transition>();
		for (PNNode node : nodes) {
			if (node instanceof Transition) {
				list.add((Transition) node);
			}
		}
		return list;
	}

	/**
	 * Checks whether the input model includes the selected nodes Comparison is
	 * based on the identifiers of the nodes
	 */
	public boolean netIncludesNodes() {
		List<PNNode> list = new ArrayList<PNNode>();
		for (PNNode selNode : nodes) {
			for (PNNode inputNode : hlNet.getPNModel().getNodes()) {
				if (inputNode.getIdentifier().equalsIgnoreCase(
						selNode.getIdentifier())) {
					list.add(selNode);
				}
			}
		}
		if (list.size() == nodes.size()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks whether the complete input model is covered by the selected nodes,
	 * that is, the complete input model is selected. Comparison is based on the
	 * number of nodes in the model.
	 */
	public boolean netEqualsNodes() {
		if (hlNet.getPNModel().getNodes().size() == nodes.size()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks whether the input model includes the selected nodes
	 */
	public boolean isTwoTransitions() {
		List<PNNode> list = new ArrayList<PNNode>();
		for (PNNode selNode : nodes) {
			if (selNode instanceof Transition) {
				list.add(selNode);
			}
		}
		if (list.size() == 2) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Retrieves the list of node identifiers minus the source node identifier
	 * 
	 * @return List<String> The identifiers
	 */
	public List<String> getNodesMinusSourceIDs() {
		List<String> nodesMinusSource = new ArrayList<String>();
		for (PNNode node : getNodes()) {
			if (!node.getIdentifier().equalsIgnoreCase(
					getProjection().getPNModel().getSource().getIdentifier())) {
				nodesMinusSource.add(node.getIdentifier());
			}
		}
		return nodesMinusSource;
	}

	/**
	 * Retrieves the predecessors of the list of nodes minus the source node and
	 * returns their identifiers
	 * 
	 * @return List<String> The predecessor node identifiers
	 */
	public Set<String> getPredecessorsOfNodesMinusSourceIDs() {
		Set<String> predecessors = new HashSet<String>();
		for (PNNode node : getNodes()) {
			if (!node.getIdentifier().equalsIgnoreCase(
					getProjection().getPNModel().getSource().getIdentifier())) {
				for (Object pred : node.getPredecessors()) {
					PNNode pre = (PNNode) pred;
					predecessors.add(pre.getIdentifier());
				}
			}
		}
		return predecessors;
	}

	/**
	 * Retrieves the list of node identifiers minus the sink node identifier
	 * 
	 * @return List<String> The identifiers
	 */
	public Set<String> getNodesMinusSinkIDs() {
		Set<String> nodesMinusSink = new HashSet<String>();
		for (PNNode node : getNodes()) {
			if (!node.getIdentifier().equalsIgnoreCase(
					getProjection().getPNModel().getSink().getIdentifier())) {
				nodesMinusSink.add(node.getIdentifier());
			}
		}
		return nodesMinusSink;
	}

	/**
	 * Retrieves the successors of the list of nodes minus the sink node and
	 * returns their identifiers
	 * 
	 * @return List<String> The successor node identifiers
	 */
	public Set<String> getSuccessorsOfNodesMinusSinkIDs() {
		Set<String> successors = new HashSet<String>();
		for (PNNode node : getNodes()) {
			if (!node.getIdentifier().equalsIgnoreCase(
					getProjection().getPNModel().getSink().getIdentifier())) {
				for (Object o : node.getSuccessors()) {
					PNNode suc = (PNNode) o;
					successors.add(suc.getIdentifier());
				}
			}
		}
		return successors;
	}

	/**
	 * Determines whether a set of nodes fulfills the requirements of a
	 * component.
	 * 
	 * @return true requirements are fulfilled
	 * @return false requirements are not fulfilled
	 */
	public boolean isComponent() {
		/**
		 * the requirements for the component are:
		 */
		/**
		 * 1. the nodes, places and transitions, are included in the net
		 */
		if (netIncludesNodes()
				&&
				/**
				 * 2. there is more than one transition included
				 */
				getProjection().getPNModel().getTransitions().size() > 1
				&&
				/**
				 * 3. there is only one source node and only one sink node
				 * (getProjection() may result in several sub nets, this is not
				 * OK)
				 */
				getProjection().getPNModel().getStartNodes().size() == 1
				&& getProjection().getPNModel().getEndNodes().size() == 1
				&&
				/**
				 * 4. the source node is not the sink node
				 */
				(!getProjection().getPNModel().getSource().getIdentifier()
						.equalsIgnoreCase(
								getProjection().getPNModel().getSink()
										.getIdentifier()))
				&&
				/**
				 * 5. the set of predecessors of the component nodes minus the
				 * source node is a sub set of the set of component nodes minus
				 * the sink node
				 */
				getNodesMinusSinkIDs().containsAll(
						getPredecessorsOfNodesMinusSourceIDs())
				&&
				/**
				 * 6. the set of successors of the component nodes minus the
				 * sink node is a sub set of the set of component nodes minus de
				 * source node
				 */
				getNodesMinusSourceIDs().containsAll(
						getSuccessorsOfNodesMinusSinkIDs())
				&&
				/**
				 * 7. the net does not contain a connection between the sink
				 * node and the source node
				 */
				hlNet.getPNModel().findEdge(
						(PNNode) getProjection().getPNModel().getSink(),
						(PNNode) getProjection().getPNModel().getSource()) == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determines whether a set of nodes fulfills the requirements of an acyclic
	 * marked graph component, see BETA working paper 240 for its definition.
	 * 
	 * @return true requirements are fulfilled
	 * @return false requirements are not fulfilled
	 */
	public boolean isAcyclicMGComponent() {
		/**
		 * the requirements for the acyclic marked graph component are:
		 */
		/**
		 * 1. it is a component
		 */
		if (isComponent() &&
		/**
		 * 2. it is acyclic
		 */
		getProjection().getPNModel().isAcyclic() &&
		/**
		 * 3. it is a marked graph
		 */
		getProjection().getPNModel().isMarkedGraph() &&
		/**
		 * 4. there are places in the component because isMarkedGraph is true
		 * for zero places and this can not be.
		 */
		getProjection().getPNModel().numberOfPlaces() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determines whether a set of nodes fulfills the requirements of a proper
	 * component. A component is a proper component if the component is acyclic
	 * and a marked graph and if the data attributes are properly distributed
	 * over the process, that is, data elements necessary for the execution of a
	 * task (its input) are available when the task becomes enabled. A task that
	 * outputs a certain data element has to be placed before the task(s) that
	 * require this element as an input. A data element can only be the output
	 * of one task.
	 * 
	 * @return true requirements are fulfilled
	 * @return false requirements are not fulfilled
	 */
	public boolean isProperComponent() {
		/**
		 * the requirements for the proper component are:
		 */
		/**
		 * 1. it is an acyclic marked graph component. note that this enforces
		 * the execution of tasks according to the data requirements.
		 */
		if (isAcyclicMGComponent()
		/**
		 * 1. data elements are distributed such that elements necessary for the
		 * execution of a task are available when the task becomes enabled.
		 */

		/**
		 * 2. Each data element is only once an output data element.
		 */

		/**
		 * 3. All input data elements for a task are produced by preceding
		 * tasks.
		 */
		) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the projection of a set of nodes on a given HLPetriNet
	 * 
	 * @return subHLNet HLPetriNet the (sub)net resulting from the projection
	 *         Note: if requirements are not fulfilled, the return is null. This
	 *         exception should be handled in code using the getProjection()
	 *         method.
	 */
	public HLPetriNet getProjection() {
		/**
		 * First, create a low-level sub net including the selected nodes
		 */
		if (netIncludesNodes()) {
			Set<Node> nodeSet = new HashSet<Node>();
			for (Node node : nodes) {
				nodeSet.add(node);
			}
			/**
			 * @see petrinet Please note that extractNet(nodeSet) requires
			 *      unique naming of the nodes.
			 */
			PetriNet subNet = hlNet.getPNModel().extractNet(nodeSet);
			/**
			 * Then, create a HL sub net.
			 */
			HLPetriNet subHLNet = makeHLPetriNet(hlNet, subNet);
			copyHighLevelInformation(hlNet, subHLNet);

			return subHLNet;
		} else {
			/**
			 * Provide user with a message and return null.
			 */
			Message
					.add(
							"Failure while creating the projection: the selected nodes"
									+ " are not a sub set of the nodes in the input net",
							Message.ERROR);
			return null;
		}
	}

	/**
	 * Create high-level Petri net and re-link activities and choices based on
	 * the mapping.
	 * 
	 * @param hlInputNet
	 *            the source HLPetriNet model (unique naming is assumed)
	 * @param petriNetResult
	 *            the low-level sub net taken from the source model
	 * @return the created target HLPetriNet model
	 * @see HLProtosToHLPetriNet
	 */
	private HLPetriNet makeHLPetriNet(HLPetriNet hlInputNet,
			PetriNet petriNetResult) {
		HLPetriNet hlPetriNet = new HLPetriNet(petriNetResult);
		/**
		 * re-establish the activity mapping
		 */
		for (ModelGraphVertex graphNodeInput : hlInputNet.getActivityNodes()) {
			for (ModelGraphVertex graphNodePN : petriNetResult.getVerticeList()) {
				if (graphNodeInput.getIdentifier().equalsIgnoreCase(
						graphNodePN.getIdentifier())) {
					HLActivity clonedAct = (HLActivity) hlInputNet
							.findActivity(graphNodeInput).clone();
					hlPetriNet.setActivity(graphNodePN, clonedAct);
					break;
				}
			}
		}

		/**
		 * re-establisch the choices
		 */
		for (ModelGraphVertex graphNodeInput : hlInputNet.getChoiceNodes()) {
			for (ModelGraphVertex graphNodePN : petriNetResult.getVerticeList()) {
				if (graphNodeInput.getIdentifier().equalsIgnoreCase(
						graphNodePN.getIdentifier())) {
					HLChoice clonedCh = (HLChoice) hlInputNet.findChoice(
							graphNodeInput).clone();
					hlPetriNet.setChoice(graphNodePN, clonedCh); // also
					// replaces
					// the
					// choice in
					// hlprocess
					break;
				}
			}
		}
		return hlPetriNet;
	}

	/**
	 * Copies the high-level information from the source HLPetriNet to the
	 * target HLPetriNet.
	 * 
	 * @param hlInput
	 *            the source of the high-level information to be copied
	 * @param hlPetriNet
	 *            the target of the high-level information to be copied
	 * @see HLProtosToHLPetriNet
	 */
	private void copyHighLevelInformation(HLPetriNet hlInput,
			HLPetriNet hlPetriNet) {
		hlPetriNet.getHLProcess().getGlobalInfo().setName(
				hlInput.getHLProcess().getGlobalInfo().getName());
		hlPetriNet.getHLProcess().getGlobalInfo().setTimeUnit(
				hlInput.getHLProcess().getGlobalInfo().getTimeUnit());
		hlPetriNet.getHLProcess().getGlobalInfo().setCaseGenerationScheme(
				hlInput.getHLProcess().getGlobalInfo()
						.getCaseGenerationScheme());
		Iterator<HLAttribute> attributes = hlInput.getHLProcess()
				.getAttributes().iterator();
		while (attributes.hasNext()) {
			HLAttribute att = attributes.next();
			HLAttribute clonedAtt = (HLAttribute) att.clone();
			hlPetriNet.getHLProcess().addOrReplace(clonedAtt);
		}
		Iterator<HLResource> resources = hlInput.getHLProcess().getResources()
				.iterator();
		while (resources.hasNext()) {
			HLResource res = resources.next();
			HLResource clonedRes = (HLResource) res.clone();
			hlPetriNet.getHLProcess().addOrReplace(clonedRes);
		}
		Iterator<HLGroup> groups = hlInput.getHLProcess().getGroups()
				.iterator();
		while (groups.hasNext()) {
			HLGroup grp = groups.next();
			HLGroup clonedGrp = (HLGroup) grp.clone();
			hlPetriNet.getHLProcess().addOrReplace(clonedGrp);
		}
		Iterator<HLTypes.Perspective> perspectives = hlInput.getHLProcess()
				.getGlobalInfo().getPerspectives().iterator();
		while (perspectives.hasNext()) {
			HLTypes.Perspective per = perspectives.next();
			hlPetriNet.getHLProcess().getGlobalInfo().addPerspective(per);
		}
	}

}
