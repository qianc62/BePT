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
package org.processmining.analysis.benchmark.metric;

import java.util.ArrayList;

import org.processmining.analysis.conformance.StateSpaceExplorationMethod;
import org.processmining.analysis.conformance.StateSpaceExplorationResult;
import org.processmining.analysis.conformance.StructuralAnalysisMethod;
import org.processmining.analysis.conformance.StructuralAnalysisResult;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethodEnum;
import org.processmining.framework.ui.Progress;

/**
 * The structural appropriateness metric evaluates the state space of the model
 * and checks for both redundant invisible and alternative duplicate tasks. <br>
 * Values range from 0 (all task in model are either alternative duplicate or
 * redundant invisible) to 1 (none of them is). <br>
 * Assumptions: Alternative duplicate tasks in loops are not detected.
 * Furthermore, invisible tasks that are redundant only with lazy semantics are
 * also not detected yet.
 * 
 * @author Anne Rozinat
 */
public class StructuralAppropriatenessMetric implements BenchmarkMetric {

	/**
	 * Configuration object telling the log replay that it has to collect data
	 * needed for the fitness calculation.
	 */
	private AnalysisConfiguration myOptions;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.benchmark.metric.BenchmarkMetric#measure(org
	 * .processmining.framework.models.petrinet.PetriNet,
	 * org.processmining.framework.log.LogReader,
	 * org.processmining.framework.models.petrinet.PetriNet,
	 * org.processmining.framework.ui.Progress)
	 */
	public double measure(PetriNet model, LogReader referenceLog,
			PetriNet referenceModel, Progress progress) {
		// create analysis configuration
		myOptions = createAnalysisConfiguration();

		// invoke state space analysis method
		StateSpaceExplorationMethod stateSpaceAnalysis = new StateSpaceExplorationMethod(
				model, progress);
		StateSpaceExplorationResult stateSpaceResult = (StateSpaceExplorationResult) stateSpaceAnalysis
				.analyse(myOptions);

		// invoke structural analysis method
		StructuralAnalysisMethod structuralAnalysis = new StructuralAnalysisMethod(
				model);
		StructuralAnalysisResult structuralResult = (StructuralAnalysisResult) structuralAnalysis
				.analyse(myOptions);

		// add the information about redundant invisibles to the diagnostic
		// petri net
		ArrayList<Transition> redundantInv = structuralResult
				.getRedundantInvisibleTasks();
		// record number of redundant invisibles for aaS metric
		stateSpaceResult.updateNumberOfRedundantInvisibles(redundantInv.size());

		// return result
		double structuralAppropriateness = stateSpaceResult
				.getImprovedStructuralAppropriatenessMeasure();
		return structuralAppropriateness;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.benchmark.metric.BenchmarkMetric#name()
	 */
	public String name() {
		return "Structural Appropriateness aS'";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.benchmark.metric.BenchmarkMetric#description()
	 */
	public String description() {
		return "The <b>structural appropriateness</b> metric <b>a<sub>S</sub>'</b> is based on the detection of <i>redundant invisible tasks</i> (can be removed without changing the behavior of the model) "
				+ "and <i>alternative duplicate tasks</i> (list alternative behavior rather than expressing it in a meaningful way). Use the <i>Conformance Checker</i> to see diagnostic visualizations.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.benchmark.metric.BenchmarkMetric#needsReferenceLog
	 * ()
	 */
	public boolean needsReferenceLog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.benchmark.metric.BenchmarkMetric#
	 * needsReferenceModel()
	 */
	public boolean needsReferenceModel() {
		return false;
	}

	/**
	 * Creates the configuration object that is used by the log replay and state
	 * space exploration to determine which data should be collected (in order
	 * to calculate metric).
	 * 
	 * @return a configuration object that only contains the "f" fitness metric
	 */
	private AnalysisConfiguration createAnalysisConfiguration() {
		AnalysisConfiguration aaS_option = new AnalysisConfiguration();
		aaS_option.setName("aaS");
		aaS_option
				.setToolTip("Advanced structural appropriateness based on the punishement of redundant invisible and alternative duplicate tasks.");
		aaS_option
				.setDescription("The <b>advanced structural appropriateness</b> metric <i>aa<sub>S</sub></i> is based on the detection of redundant invisible tasks (simply superfluous) "
						+ "and alternative duplicate tasks (list alternative behavior rather than expressing it in a meaningful way).");
		aaS_option.setNewAnalysisMethod(AnalysisMethodEnum.STATE_SPACE);
		aaS_option.setNewAnalysisMethod(AnalysisMethodEnum.STRUCTURAL);
		// // build structural appropriatness section
		AnalysisConfiguration structAppropOptions = new AnalysisConfiguration();
		structAppropOptions.setName("Structure");
		structAppropOptions.setToolTip("Structural Appropriateness Analysis");
		structAppropOptions
				.setDescription("Structural Appropriateness evaluates whether the model describes the observed process in a <i>structurally suitable</i> way.");
		structAppropOptions.addChildConfiguration(aaS_option);
		// indicate the type of analysis method that is needed
		// TODO - check whether this cannot already implicitly be determined
		structAppropOptions.addRequestedMethod(AnalysisMethodEnum.STATE_SPACE); // for
		// improved
		// metric!

		AnalysisConfiguration analysisOptions = new AnalysisConfiguration();
		analysisOptions.addChildConfiguration(structAppropOptions);
		return analysisOptions;
	}
}
