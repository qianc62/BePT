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

package org.processmining.converting;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import mathCollection.HashMultiset;
import mathCollection.Multiset;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.importing.heuristicsnet.HeuristicsNetResultWithLogReader;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * <p>
 * Title: Petri net to Heuristic net
 * </p>
 * 
 * <p>
 * Description: This class converts Petri nets to Heuristic nets. The Petri net
 * should have a single start/end transition because a Heuristic net has a
 * single start/end task
 * </p>
 * . If this is not the case, one of the start/end transitions of the Petri net
 * will be randomly chosen to become the single start/end taks of the converted
 * Heuristic net. Note that this may lead to undesirable side-effects.
 * 
 * <p>
 * <b>IMPORTANT!!!</b> This plug-in depends on the
 * <code>PetriNetReduction</code> plug-in! The reason is that, before the Petri
 * net is converted to a Heuristic net, this Petri net is reduced so that the
 * number of invisible tasks is minimized. Note that this reduction may remove
 * constructs like <i>milestones</i> etc.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */
public class PetriNetToHeuristicNetConverter implements ConvertingPlugin {
	public PetriNetToHeuristicNetConverter() {
	}

	public String getName() {
		return "Petri net to Heuristic net";
	}

	public String getHtmlDescription() {
		return "<p> This plug-in converts Petri nets to Heuristic nets. "
				+ "The Petri net is assumed to: <ul type=\"square\">"
				+ "<li> have all the <i>invisible</i> transitions with no more than one input/output arc. "
				+ "Invisible transitions that violate this constraint are automatically made visible;</li>"
				+ "<li> have only <i>visible</i> transitions as start/end ones. Thus, if there is an invisible "
				+ "transition connected to a start/end place, this invisible transition is automatically turned "
				+ "into a visible one.</li>" + "</ul>";
	}

	public MiningResult convert(ProvidedObject original) {

		PetriNet petriNet = null;
		LogReader log = null;

		for (int i = 0; i < original.getObjects().length; i++) {
			petriNet = (original.getObjects()[i] instanceof PetriNet ? (PetriNet) original
					.getObjects()[i]
					: petriNet);
			log = ((original.getObjects()[i] instanceof LogReader) ? ((LogReader) original
					.getObjects()[i])
					: log);

		}

		// converting the reduced Petri net to a Heuristic net
		HeuristicsNet hn = null;
		try {
			hn = toHeuristicsNet(removeUnnecessaryInvisibleTasksFromPetriNet(petriNet));
		} catch (Exception e) {
			Message.add("Could not convert the Petri net. " + e.getMessage());
		}

		return new HeuristicsNetResultWithLogReader(hn, log);
	}

	/**
	 * Removes the unnecessary invisible tasks of a Petri net.
	 * 
	 * @param petriNet
	 *            Petri net to reduce.
	 * @return PetriNet Reduced Petri net.
	 */

	public static PetriNet removeUnnecessaryInvisibleTasksFromPetriNet(
			PetriNet petriNet) {
		// reducing the imported Petri net
		PetriNetReduction reducedPetriNet = new PetriNetReduction();

		reducedPetriNet.setNonReducableNodes(petriNet.getVisibleTasks());

		return ((PetriNetResult) reducedPetriNet.convert(new ProvidedObject(
				"Petri net", new Object[] { petriNet }))).getPetriNet();
	}

	/**
	 * This method converts a Petri Net to a Heuristic net.
	 * 
	 * @param pn
	 *            PetriNet
	 * @return HeuristicsNet
	 * @throws Exception
	 */
	public HeuristicsNet toHeuristicsNet(PetriNet pn) throws Exception {
		HeuristicsNet hn = null;

		// TODO - Treat for multiple start/end tasks!!

		// auxiliary structure that keeps track of the incidence matrices of the
		// PN
		DoubleMatrix2D incidencenMatrix = pn.getIncidenceMatrix();
		IdentifyElementsIncidenceMatrices matrix = new IdentifyElementsIncidenceMatrices(
				pn.getInputArcsTransitionsIncidenceMatrix(), pn
						.getOutputArcsTransitionsIncidenceMatrix());

		// Checking if there are invisible transitions with multiple
		// input/output places
		// If so, these invisible transitions are converted to visible one
		convertInvisibleTransitionsWithMultipleInputOutputPlacesToVisibleOnes(
				pn, matrix);

		// Getting the set of visiblet tasks in the PN.
		// These tasks are the tasks of the HN.
		ArrayList visibleTasks = pn.getVisibleTasks();

		Multiset ms = new HashMultiset(); // used to build an event log with the
		// elements that are actually linked
		// to a transition in the PN
		for (int i = 0; i < visibleTasks.size(); i++) {
			LogEvent event = ((Transition) visibleTasks.get(i)).getLogEvent();
			ms.add(event);
		}

		// adding the names of the visible transitions as events
		LogEvents events = new LogEvents();
		events.addAll(ms.toSet());

		// building the Heuristic net
		// This procedure works for PNs with and without duplicate transitions!
		int[] duplicatesMapping = new int[visibleTasks.size()];
		Hashtable mappingBetweenTransitionsInPNandHN = new Hashtable();
		int indexDuplicatesMapping = 0;
		for (int i = 0; i < events.size(); i++) {
			ArrayList transitionsLinkedToEvent = pn.findTransitions(events
					.get(i));
			for (int j = 0; j < transitionsLinkedToEvent.size(); j++) {
				mappingBetweenTransitionsInPNandHN.put(
						(Transition) transitionsLinkedToEvent.get(j),
						indexDuplicatesMapping);
				duplicatesMapping[indexDuplicatesMapping++] = i;

			}
		}
		HNSubSet[] reverseDuplicatesMapping = HeuristicsNet
				.buildReverseDuplicatesMapping(duplicatesMapping);
		hn = new HeuristicsNet(events, duplicatesMapping,
				reverseDuplicatesMapping);

		// converting the HN to PN:
		// 1 - start tasks
		// 2 - end tasks
		// 3 - input/output sets

		// setting the start tasks
		HNSubSet startPlaces = matrix.getStartPlaces();
		// getting the output tasks for the start place
		HNSubSet startTransitions = new HNSubSet();
		for (int indexStartPlace = 0; indexStartPlace < startPlaces.size(); indexStartPlace++) {
			HNSubSet outputTransitions = matrix
					.getOutputTransitions(startPlaces.get(indexStartPlace));
			for (int indexStartTransition = 0; indexStartTransition < outputTransitions
					.size(); indexStartTransition++) {
				Transition t = (Transition) pn.getTransitions().get(
						outputTransitions.get(indexStartTransition));
				startTransitions.add(findEquivalentTaskInHN(t,
						mappingBetweenTransitionsInPNandHN));
			}
		}
		// setting the start tasks with the correct codes for the mapped HN
		hn.setStartTasks(startTransitions);

		// setting the end tasks
		HNSubSet endPlaces = matrix.getEndPlaces();
		// getting the input tasks for the end place
		HNSubSet endTransitions = new HNSubSet();
		for (int indexEndPlace = 0; indexEndPlace < endPlaces.size(); indexEndPlace++) {
			HNSubSet inputTransitions = matrix.getInputTransitions(endPlaces
					.get(indexEndPlace));
			for (int indexEndTransition = 0; indexEndTransition < inputTransitions
					.size(); indexEndTransition++) {
				Transition t = (Transition) pn.getTransitions().get(
						inputTransitions.get(indexEndTransition));
				endTransitions.add(findEquivalentTaskInHN(t,
						mappingBetweenTransitionsInPNandHN));
			}
		}
		// setting the start tasks with the correct codes for the mapped HN
		hn.setEndTasks(endTransitions);

		// for every place, get its input/output transitions and add subsets
		// for these transitions. If the transitions are invisible, look further
		// to get
		// the visible transitions that are connected to these invisible
		// transitions.

		HNSubSet[] inputVisibleTransitionsForPlaceInPN = new HNSubSet[incidencenMatrix
				.columns()];
		HNSubSet[] outputVisibleTransitionsForPlaceInPN = new HNSubSet[incidencenMatrix
				.columns()];
		for (int placeInPN = 0; placeInPN < incidencenMatrix.columns(); placeInPN++) {
			inputVisibleTransitionsForPlaceInPN[placeInPN] = toVisibleInputTransitions(
					matrix.getInputTransitions(placeInPN), pn, events, matrix,
					mappingBetweenTransitionsInPNandHN);
			outputVisibleTransitionsForPlaceInPN[placeInPN] = toVisibleOutputTransitions(
					matrix.getOutputTransitions(placeInPN), pn, events, matrix,
					mappingBetweenTransitionsInPNandHN);
		}

		// create the input and output sets of every task in the heuristic net.
		ArrayList transitionsInPN = pn.getTransitions();
		for (int transitionCode = 0; transitionCode < transitionsInPN.size(); transitionCode++) {
			// for every visible transition, get its input places and output
			// places
			Transition transitionInPN = (Transition) transitionsInPN
					.get(transitionCode);
			if (!transitionInPN.isInvisibleTask()) {
				// retrieving the input places of this visible transition...
				HNSubSet inputPlacesTransitionInPN = matrix
						.getInputPlaces(transitionCode);
				// identifying the respective code of this transition in the
				// heuristics net
				int correspondingTaskInHN = findEquivalentTaskInHN(
						transitionInPN, mappingBetweenTransitionsInPNandHN);
				// adding the input elements of every input places of the
				// transition in the PN
				// as input subsets of the corresponding task in the HN
				for (int i = 0; i < inputPlacesTransitionInPN.size(); i++) {
					insertInHeuristicNetSets(
							hn.getInputSets(),
							correspondingTaskInHN,
							inputVisibleTransitionsForPlaceInPN[inputPlacesTransitionInPN
									.get(i)]);
				}

				// retrieving the output places of this visible transition...
				HNSubSet outputPlacesTransitionInPN = matrix
						.getOutputPlaces(transitionCode);
				// adding the input elements of every input places of the
				// transition in the PN
				// as input subsets of the corresponding task in the HN
				for (int i = 0; i < outputPlacesTransitionInPN.size(); i++) {
					insertInHeuristicNetSets(
							hn.getOutputSets(),
							correspondingTaskInHN,
							outputVisibleTransitionsForPlaceInPN[outputPlacesTransitionInPN
									.get(i)]);
				}

			}
		}

		return hn;
	}

	/**
	 * convertInvisibleTransitionsWithMultipleInputOutputPlacesToVisibleOnes
	 * 
	 * @param pn
	 *            PetriNet
	 */
	private void convertInvisibleTransitionsWithMultipleInputOutputPlacesToVisibleOnes(
			PetriNet pn, IdentifyElementsIncidenceMatrices matrix) {

		// invisible transitions connected to start places are made visible
		boolean hasInvisibleTransitionsConnectedToStartPlaces = false;
		HNSubSet startPlaces = matrix.getStartPlaces();
		if (startPlaces.size() > 0) {
			// there are starting tasks

			for (int i = 0; i < startPlaces.size(); i++) {
				HNSubSet outputTransitionsPlace = matrix
						.getOutputTransitions(startPlaces.get(i));
				for (int j = 0; j < outputTransitionsPlace.size(); j++) {
					Transition outputTransition = pn.getTransitions().get(
							(outputTransitionsPlace.get(j)));
					if (outputTransition.isInvisibleTask()) {
						hasInvisibleTransitionsConnectedToStartPlaces = true;
						outputTransition.setIdentifier(outputTransition
								.getIdentifier());
						outputTransition.setLogEvent(new LogEvent(
								outputTransition.getIdentifier(),
								LogStateMachine.COMPLETE));
					}
				}
			}
		} else {

			String message = "No starting tasks have been detected! \n"
					+ "Note that this plug-in reduces the Petri net before converting it to a Heuristic net and \n"
					+ "invisible starting transitions are automatically removed from the Petri net. \n"
					+ "Please read the 'Help' for more details.";

			Message.add(message, Message.WARNING);

		}

		// invisible transitions connected to end places are made visible
		boolean hasInvisibleTransitionsConnectedToEndPlaces = false;
		HNSubSet endPlaces = matrix.getEndPlaces();
		for (int i = 0; i < endPlaces.size(); i++) {
			HNSubSet inputTransitionsPlace = matrix
					.getInputTransitions(endPlaces.get(i));
			for (int j = 0; j < inputTransitionsPlace.size(); j++) {
				Transition inputTransition = pn.getTransitions().get(
						(inputTransitionsPlace.get(j)));
				if (inputTransition.isInvisibleTask()) {
					hasInvisibleTransitionsConnectedToEndPlaces = true;
					inputTransition.setIdentifier(inputTransition
							.getIdentifier());
					inputTransition.setLogEvent(new LogEvent(inputTransition
							.getIdentifier(), LogStateMachine.COMPLETE));
				}
			}
		}

		// checking the remaining invisible transitions
		// invisible transitions with multiple input/output places are going to
		// be made visible
		boolean hasInvisibleTransitionsWithMultipleInputOutputArcs = false;
		ArrayList transitions = pn.getTransitions();
		for (int i = 0; i < transitions.size(); i++) {
			Transition t = (Transition) transitions.get(i);
			if (t.isInvisibleTask()) {
				// checking number input/output places
				if (t.inDegree() > 1 || t.outDegree() > 1) {
					hasInvisibleTransitionsWithMultipleInputOutputArcs = true;
					t.setIdentifier(t.getIdentifier());
					t.setLogEvent(new LogEvent(t.getIdentifier(),
							LogStateMachine.COMPLETE));
				}
			}
		}

		if (hasInvisibleTransitionsConnectedToStartPlaces
				|| hasInvisibleTransitionsConnectedToEndPlaces
				|| hasInvisibleTransitionsWithMultipleInputOutputArcs) {
			String message = "Some of the invisible transitions in this Petri net are going to \n"
					+ "be automatically made visible in the conversion process because \n"
					+ "this Petri net does not satisfy the constraints required by this plug-in. \n"
					+ "Please read the 'Help' for more details.";

			Message.add(message, Message.WARNING);
			// showWarningMessageDialog(message);
		}
	}

	private void showWarningMessageDialog(String message) {

		JOptionPane.showMessageDialog(MainUI.getInstance(), message,
				"Warning in conversion plug-in '" + getName() + "'",
				JOptionPane.WARNING_MESSAGE);

	}

	private void insertInHeuristicNetSets(HNSet[] hnetSets,
			int taskToModifySets, HNSubSet tasksToAddToSets) {

		if (hnetSets[taskToModifySets] == null) {
			hnetSets[taskToModifySets] = new HNSet();
		}
		if (tasksToAddToSets.size() > 0) {
			hnetSets[taskToModifySets].add(tasksToAddToSets);

		}
	}

	private HNSubSet toVisibleInputTransitions(HNSubSet setTransitionCodes,
			PetriNet pn, LogEvents events,
			IdentifyElementsIncidenceMatrices matrix,
			Hashtable mappingBetweenTransitionsInPNandHN) throws Exception {

		HNSubSet visibleTransitions = new HNSubSet();
		for (int i = 0; i < setTransitionCodes.size(); i++) {
			int transitionCode = setTransitionCodes.get(i);
			Transition t = (Transition) pn.getTransitions().get(transitionCode);
			if (t.isInvisibleTask()) {
				// transition is invisible, check its input place
				HNSubSet inputPlacesInvisibleTransition = matrix
						.getInputPlaces(transitionCode);

				for (int j = 0; j < inputPlacesInvisibleTransition.size(); j++) {
					visibleTransitions.addAll(toVisibleInputTransitions(matrix
							.getInputTransitions(inputPlacesInvisibleTransition
									.get(j)), pn, events, matrix,
							mappingBetweenTransitionsInPNandHN));
				}

			} else { // transition is visible task. Add to set of visible
				// transitions
				visibleTransitions.add(findEquivalentTaskInHN(t,
						mappingBetweenTransitionsInPNandHN));
			}
		}

		return visibleTransitions;

	}

	private HNSubSet toVisibleOutputTransitions(HNSubSet setTransitionCodes,
			PetriNet pn, LogEvents events,
			IdentifyElementsIncidenceMatrices matrix,
			Hashtable mappingBetweenTransitionsInPNandHN) throws Exception {

		HNSubSet visibleTransitions = new HNSubSet();
		for (int i = 0; i < setTransitionCodes.size(); i++) {
			int transitionCode = setTransitionCodes.get(i);
			Transition t = (Transition) pn.getTransitions().get(transitionCode);
			if (t.isInvisibleTask()) {
				// transition is invisible, check its output place
				HNSubSet outputPlacesInvisibleTransition = matrix
						.getOutputPlaces(transitionCode);

				for (int j = 0; j < outputPlacesInvisibleTransition.size(); j++) {
					visibleTransitions
							.addAll(toVisibleOutputTransitions(
									matrix
											.getOutputTransitions(outputPlacesInvisibleTransition
													.get(j)), pn, events,
									matrix, mappingBetweenTransitionsInPNandHN));
				}
			} else { // transition is visible task. Add to set of visible
				// transitions
				visibleTransitions.add(findEquivalentTaskInHN(t,
						mappingBetweenTransitionsInPNandHN));
			}
		}

		return visibleTransitions;

	}

	private int findEquivalentTaskInHN(Transition t, Hashtable mappingToHNTasks) {
		return (Integer) mappingToHNTasks.get(t);

	}

	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;

	}
}

class IdentifyElementsIncidenceMatrices {

	DoubleMatrix2D incidenceMatrixForOutputArcsTransitions = null;

	DoubleMatrix2D incidenceMatrixForInputArcsTransitions = null;

	/**
	 * This class assumes that the incidence matrix is Transitions x Places.
	 */

	public IdentifyElementsIncidenceMatrices(
			DoubleMatrix2D incidenceMatrixForInputArcsTransitions,
			DoubleMatrix2D incidenceMatrixForOutputArcsTransitions) {
		this.incidenceMatrixForInputArcsTransitions = incidenceMatrixForInputArcsTransitions;
		this.incidenceMatrixForOutputArcsTransitions = incidenceMatrixForOutputArcsTransitions;
	}

	/**
	 * This method detects the places that do not have input transitions.
	 * 
	 * @return HNSubSet with the places that do not have input transitions.
	 */

	public HNSubSet getStartPlaces() {
		HNSubSet startPlaces = new HNSubSet();
		boolean isStartPlace = true;
		for (int place = 0; place < incidenceMatrixForOutputArcsTransitions
				.columns(); place++) {
			isStartPlace = true;
			for (int transition = 0; transition < incidenceMatrixForOutputArcsTransitions
					.rows(); transition++) {
				if (incidenceMatrixForOutputArcsTransitions.get(transition,
						place) > 0) {
					// the place has at least one input transition...
					isStartPlace = false;
					break;
				}
			}
			// the places does not have input transitions.
			if (isStartPlace) {
				startPlaces.add(place);
			}
		}
		return startPlaces;
	}

	/**
	 * This method detects the places that do not have output transitions.
	 * 
	 * @return HNSubSet with the places that do not have output transitions.
	 */

	public HNSubSet getEndPlaces() {
		HNSubSet endPlaces = new HNSubSet();
		boolean isEndPlace = true;
		for (int place = 0; place < incidenceMatrixForInputArcsTransitions
				.columns(); place++) {
			isEndPlace = true;
			for (int transition = 0; transition < incidenceMatrixForInputArcsTransitions
					.rows(); transition++) {
				if (incidenceMatrixForInputArcsTransitions.get(transition,
						place) < 0) {
					// the place has at least one output transition...
					isEndPlace = false;
					break;
				}
			}
			// the places does not have output transitions.
			if (isEndPlace) {
				endPlaces.add(place);
			}
		}
		return endPlaces;
	}

	public HNSubSet getInputTransitions(int place) {
		HNSubSet inputTransitions = new HNSubSet();
		for (int transition = 0; transition < incidenceMatrixForOutputArcsTransitions
				.rows(); transition++) {
			if (incidenceMatrixForOutputArcsTransitions.get(transition, place) > 0) {
				// transition puts tokens in this place
				inputTransitions.add(transition);
			}
		}
		return inputTransitions;
	}

	public HNSubSet getOutputTransitions(int place) {
		HNSubSet outputTransitions = new HNSubSet();
		for (int transition = 0; transition < incidenceMatrixForInputArcsTransitions
				.rows(); transition++) {
			if (incidenceMatrixForInputArcsTransitions.get(transition, place) < 0) {
				// transition removes tokens from this place
				outputTransitions.add(transition);
			}
		}
		return outputTransitions;
	}

	public HNSubSet getInputPlaces(int transition) {
		HNSubSet inputPlaces = new HNSubSet();
		for (int place = 0; place < incidenceMatrixForInputArcsTransitions
				.columns(); place++) {
			if (incidenceMatrixForInputArcsTransitions.get(transition, place) < 0) {
				// transition puts tokens in this place
				inputPlaces.add(place);
			}
		}
		return inputPlaces;
	}

	public HNSubSet getOutputPlaces(int transition) {
		HNSubSet outputPlaces = new HNSubSet();
		for (int place = 0; place < incidenceMatrixForOutputArcsTransitions
				.columns(); place++) {
			if (incidenceMatrixForOutputArcsTransitions.get(transition, place) > 0) {
				// transition removes tokens from this place
				outputPlaces.add(place);
			}
		}
		return outputPlaces;
	}

}
