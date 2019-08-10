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

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.ui.Message;

import att.grappa.GrappaPanel;

/**
 * Contains all the results obtained during the structural analysis. Can be used
 * to retrieve values for the implemented metrics.
 * 
 * @see StructuralAnalysisMethod
 * 
 * @author arozinat
 */
public class StructuralAnalysisResult extends AnalysisResult {

	public PetriNet inputPetriNet;
	/** The original Petri net passed to the analysis method. */
	public PetriNet reducedPetriNet;
	/** The Petri net reduced by redundant invisible tasks */
	public DiagnosticPetriNet analyzedPetriNet;
	/** The Petri net enhanced with diagnostic information. */

	// / The current way of visualizing the analysis result.
	private DisplayState currentVisualization = DisplayState.STRUCTURAL;

	// /////////// analysis options (if no configuration is given, all options
	// are treated enabled)
	private boolean structuralAppropriatenessOption = false;
	private boolean improvedStructuralAppropriatenessOption = false;
	/** TEMPLATE: add further options here... */

	// ////////////// result data structures
	private ArrayList<Transition> redundantInvisibleTasks = new ArrayList<Transition>();

	/**
	 * Creates the result object for the structural Analysis Method.
	 * 
	 * @param analysisOptions
	 *            the option chosen by the user
	 * @param net
	 *            the Petri net to be analyzed
	 * @param method
	 *            the corresponding analysis method
	 */
	public StructuralAnalysisResult(AnalysisConfiguration analysisOptions,
			PetriNet net, StructuralAnalysisMethod method) {
		super(analysisOptions);
		inputPetriNet = net;

		// init diagnostic data structures
		analyzedPetriNet = new StrAppropriatenessVisualization(inputPetriNet,
				new ArrayList(), null);

		if (analysisOptions != null) {
			// retrieve the options
			ArrayList configurationOptions = analysisOptions
					.getEnabledOptionsForAnalysisMethod(new ArrayList(), method
							.getIdentifier());
			Iterator allOptions = configurationOptions.iterator();
			while (allOptions.hasNext()) {
				AnalysisConfiguration currentOption = (AnalysisConfiguration) allOptions
						.next();
				if (currentOption.getName() == "saS") {
					structuralAppropriatenessOption = true;
				}
				if (currentOption.getName() == "aaS") {
					improvedStructuralAppropriatenessOption = true;
				}
				/* TEMPLATE: add further options here... */
			}
		}
		// if configuration object is null --> do all
		else {
			structuralAppropriatenessOption = true;
			improvedStructuralAppropriatenessOption = true;
			/* TEMPLATE: add further options here... */
		}
	}

	// ///////// ANALYSIS OPTIONS
	// ///////////////////////////////////////////////

	/**
	 * Indicates whether the "Structural Appropriateness" metric has been
	 * selected by the user.
	 * 
	 * @return <code>true</code> if this option has been chosen,
	 *         <code>false</code> otherwise
	 */
	public boolean calculateStructuralAppropriateness() {
		return structuralAppropriatenessOption;
	}

	/**
	 * Indicates whether the "Advanced Structural Appropriateness" metric has
	 * been selected by the user.
	 * 
	 * @return <code>true</code> if this option has been chosen,
	 *         <code>false</code> otherwise
	 */
	public boolean calculateImprovedStructuralAppropriateness() {
		return improvedStructuralAppropriatenessOption;
	}

	// /////// METRICS /////////////////////////////////////////////////////////

	/**
	 * Retrieves the structural appropriateness measure based on the graph size.
	 * 
	 * @return float The value calculated (0 <= value <= 1).
	 */
	public float getStructuralAppropriatenessMeasure() {
		float numberOfNonDuplicates = inputPetriNet
				.getNumberOfNonDuplicateTasks();
		float numberOfNodes = inputPetriNet.getTransitions().size()
				+ inputPetriNet.getPlaces().size();
		// check for
		if (numberOfNodes > 0) {
			// calculate metric
			return ((numberOfNonDuplicates + 2) / numberOfNodes);

		} else {
			// inform the user about metric conditions
			Message
					.add("The structural appropriateness metric aS cannot be calculated for "
							+ "models without any nodes.");
			return 0;
		}
	}

	/**
	 * Retrieves the list of redundant invisible tasks that have detected by the
	 * reduction rule analysis in ProM.
	 * 
	 * @return the list of redundant invisible tasks
	 */
	public ArrayList<Transition> getRedundantInvisibleTasks() {
		return redundantInvisibleTasks;
	}

	/**
	 * Retrieves the reduced Petri Net in case there were redundant invisible
	 * tasks found in the model.
	 * 
	 * @return the reduced Petri Net if there were redundant invisible tasks
	 *         found in the model, <code>null</code> otherwise
	 */
	public PetriNet getReducedPetriNet() {
		return reducedPetriNet;
	}

	// ////////////////////////////////////////////////////////////////////////////

	/**
	 * Adds the given invisible transition to the list of redundant invisible
	 * tasks for this petri net.
	 * 
	 * @param trans
	 *            the transition to be added as a redundant invisible one
	 */
	public void addRedundantInvisibleTask(Transition trans) {
		redundantInvisibleTasks.add(trans);
	}

	/**
	 * Provides the Petri net reduced by redundant invisible tasks.
	 * 
	 * @param reducedNet
	 *            the reduced Petri net
	 */
	public void setReducedPetriNet(PetriNet reducedNet) {
		reducedPetriNet = reducedNet;
	}

	// //////////////////////Visualization methods /////////////////////////////

	/**
	 * Get the current display state.
	 * 
	 * @return The current display state.
	 */
	public String getCurrentVisualizationState() {
		return currentVisualization.toString();
	}

	/**
	 * Set the new visualization state, which leads to a replacement of the
	 * diagnostic Petri net that actually delivers the customized visualization.
	 * 
	 * @param newVisualization
	 *            The new visualization state.
	 */
	private void setVisualizationState(DisplayState newVisualization) {
		// exchange the diagnostic Petri net for delivering the correct
		// visualization
		if (newVisualization.toString().equals(
				DisplayState.STRUCTURAL.toString())) {
			// only change if state is new
			if (getCurrentVisualizationState().equals(
					newVisualization.toString()) == false) {
				analyzedPetriNet = new StrAppropriatenessVisualization(
						(DiagnosticPetriNet) analyzedPetriNet);
			}
			// but set always the current options state
			StrAppropriatenessVisualization currentVisualization = (StrAppropriatenessVisualization) analyzedPetriNet;
			currentVisualization.redundantInvisiblesOption = newVisualization.redundantInvisiblesOption;
			currentVisualization.alternativeDuplicatesOption = newVisualization.alternativeDuplicatesOption;
		} else {
			Message
					.add(
							"Correct visualization state could not be determined.\n",
							2);
		}
		currentVisualization = newVisualization;
	}

	/**
	 * Creates a visualization of the conformance check results. Note that a
	 * change of the display state by the user will have no effect before
	 * calling this methods. This is intended to prevent unnecessary cloning of
	 * the diagnostic petri net, which actually delivers the custom
	 * visualization of the conformance analysis results.
	 * 
	 * @param selectedInstances
	 *            The process instances that have been selected for updating the
	 *            visualization.
	 * @param currentVisualization
	 *            The current display state.
	 * @return The visualization wrapped in a GrappaPanel.
	 */
	public GrappaPanel getVisualization(DisplayState currentVisualization) {

		// ensure the display state being up to date
		setVisualizationState(currentVisualization);
		GrappaPanel myResultVisualization;
		myResultVisualization = ((DiagnosticPetriNet) analyzedPetriNet)
				.getGrappaVisualization();
		return myResultVisualization;
	}
}
