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

/*
 * Copyright (c) 2005 Eindhoven Technical University of Technology
 * All rights reserved.
 */

package org.processmining.analysis.conformance;

import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.converting.PetriNetReduction;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethodEnum;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.ui.Progress;

import att.grappa.Node;

/**
 * Invokes all structural analysis methods (i.e., for which only a process model
 * is necessary).
 * 
 * @author arozinat
 */
public class StructuralAnalysisMethod implements AnalysisMethod {

	private PetriNet myPetriNet;
	private StructuralAnalysisResult myResult;

	/**
	 * Creates the structural analysis method object. Note that the structural
	 * analysis only depends on the model (and the mapping established between
	 * the log and the model) but not on the log.
	 * 
	 * @param inputPetriNet
	 *            the PetriNet passed to the conformance check plugin
	 */
	public StructuralAnalysisMethod(PetriNet inputPetriNet) {
		myPetriNet = inputPetriNet;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return the {@link AnalysisMethodEnum#STRUCTURAL STRUCTURAL}
	 */
	public AnalysisMethodEnum getIdentifier() {
		return AnalysisMethodEnum.STRUCTURAL;
	}

	/**
	 * Invoke Structural analysis method.
	 * 
	 * @param analysisOptions
	 *            the given analysis configuration
	 * @return a result object of type {@link StructuralAnalysisResult
	 *         StructuralAnalysisResult}
	 */
	public AnalysisResult analyse(AnalysisConfiguration analysisOptions) {
		// initialize the result object and the analysis options
		myResult = new StructuralAnalysisResult(analysisOptions, myPetriNet,
				this);

		// determine redundant invisible task using the Petri Net reduction in
		// ProM
		if (myResult.calculateImprovedStructuralAppropriateness() == true) { // configurated

			// / start monitoring progress of log replay
			Progress progress = new Progress("Checking for redundant tasks...",
					0, 2);
			progress.setProgress(0);

			// TODO - check whether identifier can be assumed to be equal
			// instead (numbers don't work
			// as it is not preserved during reduction technique!
			// give unique number for each transition in order to find them back
			// (after reduction equals method might yield different result for
			// formerly equal tasks)
			// Iterator<Transition> it = myPetriNet.getTransitions().iterator();
			// int i = 0;
			// while (it.hasNext()) {
			// Transition t = it.next();
			// t.setNumber(i);
			// i++;
			// }

			// remember all invisible tasks to check for their presence in the
			// reduced net later on
			ArrayList<Transition> allInvisibles = myPetriNet
					.getInvisibleTasks();

			// only if there are invisible tasks at all
			if (allInvisibles.size() > 0) {

				progress.setProgress(1); // otherwise first time it is not
				// visible!

				// create reduction facility object
				PetriNetReduction reducer = new PetriNetReduction();
				// get all visible tasks in order to prevent reduction of those
				ArrayList nonReducibleNodes = myPetriNet.getVisibleTasks();

				// prevent also the reduction of the start and end place
				ArrayList<Node> startEndPlaces = new ArrayList<Node>();
				Iterator<Place> allPlaces = myPetriNet.getPlaces().iterator();
				while (allPlaces.hasNext()) {
					Place current = allPlaces.next();
					if (current.inDegree() == 0 || current.outDegree() == 0) {
						startEndPlaces.add(current);
					}
				}
				nonReducibleNodes.addAll(startEndPlaces);
				reducer.setNonReducableNodes(nonReducibleNodes);

				// clone the input Petri net in order to keep the structure of
				// it
				PetriNet clonedNet = (PetriNet) myPetriNet.clone();
				PetriNet reducedNet = reducer.reduce(clonedNet);

				// now check which of the invisible tasks have been reduced
				Iterator<Transition> invisiblesInReducedNet = reducedNet
						.getInvisibleTasks().iterator();
				while (invisiblesInReducedNet.hasNext()) {
					Transition currentNonRedundant = invisiblesInReducedNet
							.next();
					Iterator<Transition> potentiallyRedundantInvisibles = allInvisibles
							.iterator();
					while (potentiallyRedundantInvisibles.hasNext()) {
						Transition currentCandidate = potentiallyRedundantInvisibles
								.next();
						if (currentCandidate.getIdentifier().equals(
								currentNonRedundant.getIdentifier()) == true) {
							// not redundant --> remove!
							allInvisibles.remove(currentCandidate);
							break;
						}
					}
				}
				// add all the remaining invisibles to the result object as they
				// are redundant
				// (have been removed in reduction!)
				Iterator<Transition> remainingRedundants = allInvisibles
						.iterator();
				while (remainingRedundants.hasNext()) {
					myResult.addRedundantInvisibleTask(remainingRedundants
							.next());
				}
				// remember the reduced net for potential user export
				myResult.setReducedPetriNet(reducedNet);
			}

			// finish GUI progress bar
			progress.setProgress(2);
		}
		return myResult;
	}
}
