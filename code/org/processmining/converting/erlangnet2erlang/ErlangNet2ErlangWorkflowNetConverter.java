package org.processmining.converting.erlangnet2erlang;

import java.util.LinkedHashMap;
import java.util.Map;

import org.processmining.framework.models.erlang.Function;
import org.processmining.framework.models.erlang.ReceiveStatement;
import org.processmining.framework.models.erlangnet.ErlangNet;
import org.processmining.framework.models.erlangnet.ErlangNetPlaceType;
import org.processmining.framework.models.erlangnet.inscription.RouteInscription;
import org.processmining.framework.models.erlangnet.statement.AssignmentStatement;
import org.processmining.framework.models.erlangnet.statement.NoOperationStatement;
import org.processmining.framework.models.erlangnet.statement.ReceiveSenderAndValueStatement;
import org.processmining.framework.models.erlangnet.statement.Statement;
import org.processmining.framework.models.petrinet.Choice;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNetNavigation;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;

public class ErlangNet2ErlangWorkflowNetConverter {

	private static int placeId = 0;
	private static int transitionId = 0;

	public ErlangWorkflowNet convert(ErlangNet en) {
		ErlangWorkflowNet wfnet = new ErlangWorkflowNet();
		Map<Place, Place> clonedPlaces = new LinkedHashMap<Place, Place>();
		Map<Transition, Transition> clonedTransitions = new LinkedHashMap<Transition, Transition>();
		mapWorkflow(en, wfnet, clonedPlaces, clonedTransitions);
		generatePlaceAnnotations(en, wfnet, clonedPlaces, clonedTransitions);
		generateTransitionAnnotations(en, wfnet, clonedPlaces,
				clonedTransitions);
		return wfnet;
	}

	private void generatePlaceAnnotations(ErlangNet en,
			ErlangWorkflowNet wfnet, Map<Place, Place> clonedPlaces,
			Map<Transition, Transition> clonedTransitions) {
		for (Place place : en.getPlaces()) {
			Choice choice = Choice.EXPLICIT;
			for (Transition transition : PetriNetNavigation
					.getOutgoingTransitions(place)) {
				if (doesReceiveMessage(en, transition)) {
					choice = Choice.IMPLICIT;
					break;
				}
			}
			wfnet.setAnnotation(place, choice);
		}
	}

	private void generateTransitionAnnotations(ErlangNet en,
			ErlangWorkflowNet wfnet, Map<Place, Place> clonedPlaces,
			Map<Transition, Transition> clonedTransitions) {
		for (Transition transition : en.getTransitions()) {
			if (doesReceiveMessage(en, transition)) {
				Place newPlace = generatePlace(wfnet);
				Transition newTransition = generateTransition(wfnet);
				wfnet.addAndLinkEdge(transition, newPlace);
				wfnet.addAndLinkEdge(newPlace, newTransition);
				RouteInscription inscription = ((RouteInscription) en
						.getAnnotation((PNEdge) transition.outEdgeElements()
								.nextElement()));
				Function function = translate(newTransition.getIdentifier(),
						inscription.statement);
				wfnet.setAnnotation(newTransition, function);
			}
		}
	}

	private Function translate(String name, Statement statement) {
		if (statement instanceof AssignmentStatement) {
			AssignmentStatement as = (AssignmentStatement) statement;
			return new Function(
					name,
					new org.processmining.framework.models.erlang.AssignmentStatement(
							as.variable, as.expression));
		} else if (statement instanceof NoOperationStatement) {
			NoOperationStatement nos = (NoOperationStatement) statement;
			return new Function(
					name,
					new org.processmining.framework.models.erlang.NoOperationStatement(
							nos.envId));
		} else if (statement instanceof ReceiveSenderAndValueStatement) {
			// ReceiveSenderAndValueStatement rsavs =
			// (ReceiveSenderAndValueStatement) statement;
			// return new Function(name,
			// new ReceiveStatement(rsavs.));
		}
		return null;
	}

	private Transition generateTransition(ErlangWorkflowNet wfnet) {
		return new Transition("et_" + (++transitionId), wfnet);
	}

	private Place generatePlace(ErlangWorkflowNet wfnet) {
		return new Place("ep_" + (++placeId), wfnet);
	}

	private boolean doesReceiveMessage(ErlangNet en, Transition transition) {
		for (Place inputPlace : PetriNetNavigation
				.getIncomingPlaces(transition)) {
			if (en.getAnnotation(inputPlace) == ErlangNetPlaceType.MSG) {
				return true;
			}
		}
		return false;
	}

	private boolean doesSendMessage(ErlangNet en, Transition transition) {
		for (Place outputPlace : PetriNetNavigation
				.getOutgoingPlaces(transition)) {
			if (en.getAnnotation(outputPlace) == ErlangNetPlaceType.MSG) {
				return true;
			}
		}
		return false;
	}

	private void mapWorkflow(ErlangNet en, ErlangWorkflowNet wfnet,
			Map<Place, Place> clonedPlaces,
			Map<Transition, Transition> clonedTransitions) {
		for (Place place : en.getPlaces()) {
			convert(en, wfnet, clonedPlaces, place);
		}
		for (Transition transition : en.getTransitions()) {
			convert(en, wfnet, clonedTransitions, transition);
		}
		for (Object obj : en.getEdges()) {
			PNEdge edge = (PNEdge) obj;
			convert(wfnet, clonedPlaces, clonedTransitions, edge);
		}
	}

	private void convert(ErlangWorkflowNet wfnet,
			Map<Place, Place> clonedPlaces,
			Map<Transition, Transition> clonedTransitions, PNEdge edge) {
		if (edge.isPT()) {
			Place place = clonedPlaces.get(edge.getTail());
			if (place != null) {
				Transition transition = clonedTransitions.get(edge.getHead());
				wfnet.addAndLinkEdge(new PNEdge(place, transition), place,
						transition);
			}
		} else {
			Place place = clonedPlaces.get(edge.getHead());
			if (place != null) {
				Transition transition = clonedTransitions.get(edge.getTail());
				wfnet.addAndLinkEdge(new PNEdge(transition, place), transition,
						place);
			}
		}
	}

	private void convert(ErlangNet en, ErlangWorkflowNet wfnet,
			Map<Transition, Transition> clonedTransitions, Transition transition) {
		Transition clone = new Transition(transition.getIdentifier(), wfnet);
		wfnet.addTransition(clone);
		clonedTransitions.put(transition, clone);
	}

	private void convert(ErlangNet en, ErlangWorkflowNet wfnet,
			Map<Place, Place> clonedPlaces, Place place) {
		if (en.getAnnotation(place) != ErlangNetPlaceType.MSG) {
			Place clone = new Place(place.getIdentifier(), wfnet);
			wfnet.addPlace(clone);
			clonedPlaces.put(place, clone);
			Choice choice = Choice.EXPLICIT;
			for (Transition transition : PetriNetNavigation
					.getOutgoingTransitions(place))
				for (Place inPlace : PetriNetNavigation
						.getIncomingPlaces(transition))
					if (en.getAnnotation(inPlace) == ErlangNetPlaceType.MSG) {
						choice = Choice.IMPLICIT;
						clonedPlaces.get(place)
								.setAttribute("dotcolor", "blue");
						break;
					}
			wfnet.setAnnotation(place, choice);
		}
	}

}
