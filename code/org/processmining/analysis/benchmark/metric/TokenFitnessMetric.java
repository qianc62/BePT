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

import org.processmining.analysis.conformance.ConformanceLogReplayResult;
import org.processmining.analysis.conformance.ConformanceMeasurer;
import org.processmining.analysis.conformance.MaximumSearchDepthDiagnosis;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethodEnum;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.ui.Progress;

/**
 * Fitness evaluates whether the observed process <i>complies with</i> the
 * control flow specified by the process. One way to investigate the fitness is
 * to replay the log in the Petri net. The log replay is carried out in a
 * non-blocking way, i.e., if there are tokens missing to fire the transition in
 * question they are created artificially and replay proceeds. While doing so,
 * diagnostic data is collected and can be accessed afterwards. The log replay
 * can deal with invisible and duplicate tasks. <br>
 * The token-based <b>fitness</b> metric <i>f</i> relates the amount of missing
 * tokens during log replay with the amount of consumed ones and the amount of
 * remaining tokens with the produced ones. If the log could be replayed
 * correctly, that is, there were no tokens missing nor remaining, it evaluates
 * to 1. Similarly, if every produced and consumed token is remaining or
 * missing, it evaluates to 0. <br>
 * Assumptions: for the log replay it is assumed that the model has a unique
 * start and end place, and that there are no dead tasks (i.e., it is a Workflow
 * Net). Furthermore, it is assumed that the tasks in the given Petri net and
 * the reference log are mapped to each other via their log event type.
 * 
 * @author Anne Rozinat
 * 
 */
public class TokenFitnessMetric implements BenchmarkMetric {

	/**
	 * Configuration object telling the log replay that it has to collect data
	 * needed for the fitness calculation.
	 */
	private AnalysisConfiguration myOptions;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.benchmark.BenchmarkMetric#measure(org.
	 * processmining.framework.models.petrinet.PetriNet,
	 * org.processmining.framework.log.LogReader,
	 * org.processmining.framework.models.petrinet.PetriNet)
	 */
	public double measure(PetriNet model, LogReader referenceLog,
			PetriNet referenceModel, Progress progress) {
		// create analysis configuration
		myOptions = createAnalysisConfiguration();

		// invoke analysis method
		LogReplayAnalysisMethod logReplayAnalysis = new LogReplayAnalysisMethod(
				model, referenceLog, new ConformanceMeasurer(), progress);
		int maxSearchDepth = MaximumSearchDepthDiagnosis
				.determineMaximumSearchDepth(model);
		logReplayAnalysis.setMaxDepth(maxSearchDepth); // automatically set
		// maximum search depth
		// for log replay
		ConformanceLogReplayResult replayResult = (ConformanceLogReplayResult) logReplayAnalysis
				.analyse(myOptions);

		// return result
		double fitness = replayResult.getFitnessMeasure();
		return fitness;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.benchmark.BenchmarkMetric#name()
	 */
	public String name() {
		return "Token-based Fitness f";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.benchmark.metric.BenchmarkMetric#description()
	 */
	public String description() {
		return "The <b>token-based fitness</b> metric <b>f</b> relates the amount of missing tokens during <i>log replay</i> to the amount of consumed tokens, and "
				+ "the amount of remaining tokens to the amount of produced tokens. If the log could be replayed correctly, that is, there were no tokens missing nor remaining, it evaluates to 1. "
				+ "Use the <i>Conformance Checker</i> to see diagnostic visualizations.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.benchmark.BenchmarkMetric#needsReferenceLog()
	 */
	public boolean needsReferenceLog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.benchmark.BenchmarkMetric#needsReferenceModel
	 * ()
	 */
	public boolean needsReferenceModel() {
		return false;
	}

	/**
	 * Creates the configuration object that is used by the log replay to
	 * determine which data should be collected (in order to calculate metric).
	 * 
	 * @return a configuration object that only contains the "f" fitness metric
	 */
	private AnalysisConfiguration createAnalysisConfiguration() {
		AnalysisConfiguration f_option = new AnalysisConfiguration();
		f_option.setName("f");
		f_option
				.setToolTip("Degree of fit based on missing and remaining tokens in the model during log replay");
		f_option
				.setDescription("The token-based <b>fitness</b> metric <i>f</i> relates the amount of missing tokens during log replay with the amount of consumed ones and "
						+ "the amount of remaining tokens with the produced ones. If the log could be replayed correctly, that is, there were no tokens missing nor remaining, it evaluates to 1.");
		f_option.setNewAnalysisMethod(AnalysisMethodEnum.LOG_REPLAY);

		AnalysisConfiguration fitnessOptions = new AnalysisConfiguration();
		fitnessOptions.setName("Fitness");
		fitnessOptions.setToolTip("Fitness Analysis");
		fitnessOptions
				.setDescription("Fitness evaluates whether the observed process <i>complies with</i> the control flow specified by the process. "
						+ "One way to investigate the fitness is to replay the log in the Petri net. The log replay is carried out in a non-blocking way, i.e., if there are tokens missing "
						+ "to fire the transition in question they are created artificially and replay proceeds. While doing so, diagnostic data is collected and can be accessed afterwards.");
		fitnessOptions.addChildConfiguration(f_option);

		AnalysisConfiguration analysisOptions = new AnalysisConfiguration();
		analysisOptions.addChildConfiguration(fitnessOptions);
		return analysisOptions;
	}
}
