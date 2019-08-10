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

import org.processmining.analysis.conformance.BehAppropriatenessAnalysisGUI;
import org.processmining.analysis.conformance.ConformanceLogReplayResult;
import org.processmining.analysis.conformance.ConformanceMeasurer;
import org.processmining.analysis.conformance.MaximumSearchDepthDiagnosis;
import org.processmining.analysis.conformance.StateSpaceExplorationMethod;
import org.processmining.analysis.conformance.StateSpaceExplorationResult;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethodEnum;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.ui.Progress;

/**
 * The behavioral appropriateness metric builds on global successorship and
 * predecessorship relations. It looks for "sometimes" relations (i.e.,
 * alternative or parallel behavior) in the model and the log, and punishes
 * those relations that are in the model but not in the log. <br>
 * Values range from 0 to 1. Note that metric is relative to model flexibility
 * (the less flexible the model is, the more coase-grained the measurement
 * steps). In the case of a purely sequential model, this metric is trivially
 * true. Furthermore, the flower model can yield a value of 1.0 if the log
 * itself also exhibits random behavior. <br>
 * Assumptions: Can deal with duplicate tasks. Big loops (i.e., spanning huge
 * parts of the model) will "blur" the measurements (behavior with loops cannot
 * be analyzed properly).
 * 
 * @author Anne Rozinat
 */
public class BehavioralAppropriatenessMetric implements BenchmarkMetric {

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

		// invoke log replay analysis method
		LogReplayAnalysisMethod logReplayAnalysis = new LogReplayAnalysisMethod(
				model, referenceLog, new ConformanceMeasurer(), progress);
		int maxSearchDepth = MaximumSearchDepthDiagnosis
				.determineMaximumSearchDepth(model);
		logReplayAnalysis.setMaxDepth(maxSearchDepth); // automatically set
		// maximum search depth
		// for log replay
		ConformanceLogReplayResult replayResult = (ConformanceLogReplayResult) logReplayAnalysis
				.analyse(myOptions);
		// invoke state space analysis method
		StateSpaceExplorationMethod stateSpaceAnalysis = new StateSpaceExplorationMethod(
				model, progress);
		StateSpaceExplorationResult stateSpaceResult = (StateSpaceExplorationResult) stateSpaceAnalysis
				.analyse(myOptions);

		// clean up previous results first
		// TODO: check whether needed (might be obsolete as result object only
		// used once)
		BehAppropriatenessAnalysisGUI.cleanLogAndModelRelations(
				stateSpaceResult, replayResult);
		// match the log-derived relations with the model-derived relations and
		// remember result
		// in the diagnostic Petri net
		BehAppropriatenessAnalysisGUI.matchLogAndModelRelations(
				stateSpaceResult, replayResult);

		// return result
		double behavioralAppropriateness = stateSpaceResult
				.getImprovedBehavioralAppropriatenessMeasure();
		return behavioralAppropriateness;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.benchmark.metric.BenchmarkMetric#name()
	 */
	public String name() {
		return "Behavioral Appropriateness aB'";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.benchmark.metric.BenchmarkMetric#description()
	 */
	public String description() {
		return "The <b>behavioral appropriateness</b> metric <b>a<sub>B</sub>'</b> is based on <i>follows</i> and <i>precedes</i> relations among activities "
				+ "(the greater the value the more precisely the behavior observed in the log is captured by the model). Use the <i>Conformance Checker</i> to see diagnostic visualizations.";
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
		AnalysisConfiguration aaB_option = new AnalysisConfiguration();
		aaB_option.setName("aaB");
		aaB_option
				.setToolTip("Advanced behavioral appropriateness based on activity relations that were not observed i the log");
		aaB_option
				.setDescription("The <b>advanced behavioral appropriateness</b> metric <i>aa<sub>B</sub></i> is based on successorship relations among activities with respect the event relations observed  in the log "
						+ "(the greater the value the more precisely the behavior observed in the log is captured).");
		aaB_option.setNewAnalysisMethod(AnalysisMethodEnum.LOG_REPLAY);
		aaB_option.setNewAnalysisMethod(AnalysisMethodEnum.STATE_SPACE);
		// // build behavioral appropriateness section
		AnalysisConfiguration behAppropOptions = new AnalysisConfiguration();
		behAppropOptions.setName("Precision");
		behAppropOptions.setToolTip("Behavioral Appropriateness Analysis");
		behAppropOptions
				.setDescription("Precision, or Behavioral Appropriateness, evaluates <i>how precisely</i> the model describes the observed process.");
		behAppropOptions.addChildConfiguration(aaB_option);
		// indicate the type of analysis method that is needed
		// TODO - check whether this cannot already implicitly be determined
		behAppropOptions.addRequestedMethod(AnalysisMethodEnum.LOG_REPLAY);
		behAppropOptions.addRequestedMethod(AnalysisMethodEnum.STATE_SPACE); // for
		// improved
		// metric!

		AnalysisConfiguration analysisOptions = new AnalysisConfiguration();
		analysisOptions.addChildConfiguration(behAppropOptions);
		return analysisOptions;
	}

}
