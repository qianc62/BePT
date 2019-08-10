package org.processmining.converting.wfnet2bpel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.processmining.converting.wfnet2bpel.pattern.BPELLibraryComponent;
import org.processmining.converting.wfnet2bpel.pattern.FlowComponent;
import org.processmining.converting.wfnet2bpel.pattern.PickComponent;
import org.processmining.converting.wfnet2bpel.pattern.SwitchComponent;
import org.processmining.framework.models.bpel.BPELActivity;
import org.processmining.framework.models.bpel.BPELEmpty;
import org.processmining.framework.models.bpel.BPELFlow;
import org.processmining.framework.models.bpel.BPELPick;
import org.processmining.framework.models.bpel.BPELSequence;
import org.processmining.framework.models.bpel.BPELSwitch;
import org.processmining.framework.models.bpel.BPELWhile;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.PetriNetNavigation;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.pattern.Component;
import org.processmining.framework.models.petrinet.pattern.SequenceComponent;
import org.processmining.framework.models.petrinet.pattern.WhileComponent;

import att.grappa.Edge;
import att.grappa.Node;

/**
 * <p>
 * Title: BPELTranslator
 * </p>
 * 
 * <p>
 * Description: Translates a component into a BPELActivity
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
public class BPELTranslator {

	/**
	 * A counter to insure unique names for created fragment transitions
	 */
	protected static int fragmentCount = 0;

	private static int linkCount;

	/**
	 * Translate a match into an activity.
	 * 
	 * @param match
	 *            - describes a WF-net and what kind of component it is
	 * @param annotation
	 *            - transition annotations
	 * @return An activity that corresponds to the WF-net of the match
	 */
	public static BPELActivity translate(Component match,
			Map<String, BPELActivity> annotations) {
		linkCount = 0;
		BPELActivity annotation = null;
		if (match instanceof SequenceComponent)
			annotation = translateSequence(match.getWfnet(), annotations);
		else if (match instanceof SwitchComponent)
			annotation = translateSwitch(match.getWfnet(), annotations);
		else if (match instanceof PickComponent)
			annotation = translatePick(match.getWfnet(), annotations);
		else if (match instanceof FlowComponent)
			annotation = translateFlow(match.getWfnet(), annotations);
		else if (match instanceof WhileComponent)
			annotation = translateWhile(match.getWfnet(), annotations);
		else if (match instanceof BPELLibraryComponent) {
			annotation = ((BPELLibraryComponent) match).getActivity();
			fragmentCount++;
		}
		return annotation;
	}

	/**
	 * Translates a PICK-component.
	 * 
	 * @param wfnet
	 *            - the PICK-component
	 * @param annotations
	 *            - transition annotations
	 * @return A translation of the PICK-component
	 */
	private static BPELPick translatePick(PetriNet wfnet,
			Map<String, BPELActivity> annotations) {
		fragmentCount++;
		BPELPick pickActivity = new BPELPick("Pick_F" + fragmentCount);
		for (Edge arc : wfnet.getSource().getOutEdges()) {
			pickActivity.getMessages().put(
					annotations.get(((PNEdge) arc).getTransition().getName()),
					"");
		}
		return pickActivity;
	}

	/**
	 * Translates a FLOW-component.
	 * 
	 * @param wfnet
	 *            - the FLOW-component
	 * @param annotations
	 *            - transition annotations
	 * @return A translation of the FLOW-component
	 */
	private static BPELFlow translateFlow(PetriNet wfnet,
			Map<String, BPELActivity> annotations) {
		fragmentCount++;
		BPELFlow flowActivity = new BPELFlow("Flow_F" + fragmentCount);
		Map<String, BPELActivity> clonedNodes = new LinkedHashMap<String, BPELActivity>();

		for (Transition transition : wfnet.getTransitions()) {
			BPELActivity activity = annotations.get(transition.getName());
			BPELActivity clonedActivity = activity.cloneActivity();
			flowActivity.appendChildActivity(clonedActivity);
			clonedNodes.put(transition.getName(), clonedActivity);
		}

		for (Place place : wfnet.getPlaces()) {
			if (place.getOutEdges() != null && place.getOutEdges().size() > 1) {
				BPELEmpty empty = new BPELEmpty(place.getIdentifier());
				flowActivity.appendChildActivity(empty);
				clonedNodes.put(place.getName(), empty);
			}
		}

		// for (Node node : wfnet.getPlaces()) {
		// BPELActivity nodeTranslation = clonedNodes.get(node.getName());
		// // Map<Pair<BPELActivity,BPELActivity>,String> linkNames = new
		// // LinkedHashMap<Pair<BPELActivity,BPELActivity>, String>();
		// String joinCondition = null;
		// if (node instanceof Place && node.getOutEdges() != null
		// && node.getOutEdges().size() > 1) {
		// for (Node inNode : PetriNetNavigation.getIncomingNodes(node)) {
		// BPELActivity from = clonedNodes.get(inNode.getName());
		// String linkName = "link" + linkCount++;
		// from.appendSource(linkName);
		// nodeTranslation.appendTarget(linkName);
		// // linkNames.put(Pair.create(from,nodeTranslation),
		// // linkName);
		// if (joinCondition == null)
		// joinCondition = "bpws:getLinkStatus('" + linkName
		// + "')";
		// else
		// joinCondition += "AND bpws:getLinkStatus('" + linkName
		// + "')";
		// }
		// } else {
		//
		// }
		//
		// if (joinCondition != null) {
		// String name = node.getName();
		// BPELActivity activity = annotations.get(name);
		// activity.setJoinCondition(joinCondition);
		// } else {
		//
		// }
		// }
		//
		// for (BPELActivity activity : new ArrayList<BPELActivity>(flowActivity
		// .getActivities())) {
		// if (activity instanceof BPELEmpty
		// && activity.getAllSources().size() == 1) {
		//
		// }
		// }

		for (Transition transition : wfnet.getTransitions()) {
			for (Transition outTransition : PetriNetNavigation
					.getOutgoingTransitions(transition)) {
				BPELActivity fromActivity = clonedNodes.get(transition
						.getName());
				BPELActivity toActivity = clonedNodes.get(outTransition
						.getName());
				linkCount++;
				fromActivity.appendSource("link" + linkCount);
				toActivity.appendTarget("link" + linkCount);
			}
			String type = null;
			if (transition.inDegree() > 1)
				type = "and";
			else if (transition.inDegree() == 1
					&& PetriNetNavigation.getIncomingPlaces(transition)
							.firstElement().outDegree() > 1)
				type = "or";
			if (type != null) {
				String condition = new String();
				for (Transition incomingTransition : PetriNetNavigation
						.getIncomingTransitions(transition))
					condition += "bpws:getLinkStatus('"
							+ incomingTransition.getIdentifier() + "_"
							+ transition.getIdentifier() + "') " + type + " ";
				if (condition.length() > 0) {
					condition = condition.substring(0, condition.length()
							- type.length() - 2);
					annotations.get(transition.getName()).setJoinCondition(
							condition);
				}
			}
		}
		return flowActivity;
	}

	/**
	 * Translates a SWITCH-component.
	 * 
	 * @param wfnet
	 *            - the SWITCH-component
	 * @param annotations
	 *            - transition annotations
	 * @return A translation of the SWITCH-component
	 */
	private static BPELSwitch translateSwitch(PetriNet wfnet,
			Map<String, BPELActivity> annotations) {
		fragmentCount++;
		BPELSwitch newSwitch = new BPELSwitch("Switch_F" + fragmentCount);
		int n = wfnet.getSource().getOutEdges().size();
		for (Edge arc : wfnet.getSource().getOutEdges()) {
			n--;
			newSwitch.appendChildActivity(n > 0 ? "?" : null, annotations.get(
					PetriNetNavigation.getTransition(arc).getName())
					.cloneActivity());
		}
		return newSwitch;
	}

	/**
	 * Translates a SEQUENCE-component.
	 * 
	 * @param wfnet
	 *            - the SEQUENCE-component
	 * @param annotations
	 *            - transition annotations
	 * @return A translation of the SEQUENCE-component
	 */
	private static BPELSequence translateSequence(PetriNet wfnet,
			Map<String, BPELActivity> annotations) {
		fragmentCount++;
		BPELSequence newSequence = new BPELSequence("Sequence_F"
				+ fragmentCount);
		Node source = wfnet.getSource();
		Transition transition;
		if (source instanceof Transition)
			transition = (Transition) source;
		else
			transition = PetriNetNavigation.getOutgoingTransitions(source).get(
					0);
		while (transition != null) {
			newSequence.appendChildActivity(annotations.get(
					transition.getName()).cloneActivity());
			List<Transition> outTransitions = PetriNetNavigation
					.getOutgoingTransitions(transition);
			if (!outTransitions.isEmpty())
				transition = outTransitions.get(0);
			else
				transition = null;
		}
		return newSequence;
	}

	/**
	 * Translates a WHILE-component.
	 * 
	 * @param wfnet
	 *            - the WHILE-component
	 * @param annotations
	 *            - transition annotations
	 * @return A translation of the WHILE-component
	 */
	private static BPELActivity translateWhile(PetriNet wfnet,
			Map<String, BPELActivity> annotations) {
		Transition source = (Transition) wfnet.getSource();
		Transition sink = (Transition) wfnet.getSink();
		Transition whileNode = null;
		for (Transition transition : wfnet.getTransitions()) {
			if (transition != source && transition != sink) {
				whileNode = transition;
				break;
			}
		}

		fragmentCount++;
		BPELSequence newSequence = new BPELSequence("Sequence_F"
				+ fragmentCount);
		BPELActivity annotation = annotations.get(source.getName());
		newSequence.appendChildActivity(annotation);
		fragmentCount++;
		BPELWhile newWhile = new BPELWhile("While_F" + fragmentCount++);
		newWhile.appendChildActivity(annotations.get(whileNode.getName()));
		newSequence.appendChildActivity(newWhile);
		newSequence.appendChildActivity(annotations.get(sink.getName()));
		return newSequence;
	}

}
