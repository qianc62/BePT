package org.processmining.analysis.benchmark.metric;

import java.util.HashSet;
import java.util.Iterator;

import org.processmining.analysis.conformance.ConformanceLogReplayResult;
import org.processmining.analysis.conformance.ConformanceMeasurer;
import org.processmining.analysis.conformance.DiagnosticPetriNet;
import org.processmining.analysis.conformance.DiagnosticTransition;
import org.processmining.analysis.conformance.MaximumSearchDepthDiagnosis;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.algorithms.CoverabilityGraphBuilder;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethodEnum;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedLogTrace;
import org.processmining.framework.models.petrinet.algorithms.logReplay.ReplayedTransition;
import org.processmining.framework.ui.Progress;

/**
 * Precision and Structure metric based on the Minimal Description Length
 * principle. Advocates a balance of the cost of encoding the model and encoding
 * the data with the model.
 * 
 * @author Anne Rozinat
 */
public class MinimalDescriptionLengthMetric implements BenchmarkMetric {

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
		// myOptions = createAnalysisConfiguration();

		// invoke structural analysis --> "Compactness" measure part
		// StructuralAnalysisMethod structuralAnalysis = new
		// StructuralAnalysisMethod(model);
		// StructuralAnalysisResult structuralResult =
		// (StructuralAnalysisResult) structuralAnalysis.analyse(myOptions);
		// float compactnessMeasure =
		// structuralResult.getStructuralAppropriatenessMeasure();

		int compactnessMeasure = model.getEdges().size();

		// invoke log replay analysis method --> "Preciseness" measure part
		LogReplayAnalysisMethod logReplayAnalysis = new LogReplayAnalysisMethod(
				model, referenceLog, new MDLMeasurer(), progress);
		int maxSearchDepth = MaximumSearchDepthDiagnosis
				.determineMaximumSearchDepth(model);
		logReplayAnalysis.setMaxDepth(maxSearchDepth); // automatically set
		// maximum search depth
		// for log replay
		ConformanceLogReplayResult replayResult = (ConformanceLogReplayResult) logReplayAnalysis
				.analyse(null);
		// TODO: get "preciseness" part from replay result (to be implemented)

		// TODO: calculate based on "compactness" part and "preciseness" part
		// return result
		int mdlValue = -1;
		return mdlValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.benchmark.metric.BenchmarkMetric#name()
	 */
	public String name() {
		return "Minimal Description Length";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.benchmark.metric.BenchmarkMetric#description()
	 */
	public String description() {
		return "TODO";
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

	// /////////////////////////////////// Helper methods MDL metric
	// //////////////////////////////////////////

	// /**
	// * Creates the configuration object that is used by the structural
	// analysis to determine
	// * which data should be collected (in order to calculate metric).
	// * @return a configuration object that only contains the "saS" fitness
	// metric
	// */
	// private AnalysisConfiguration createAnalysisConfiguration() {
	// // TODO: think about how to avoid all this repliated configuration
	// (currently easiest solution)
	// // saS
	// AnalysisConfiguration aS_option = new AnalysisConfiguration();
	// aS_option.setSelected(false);
	// aS_option.setName("saS");
	// aS_option.setToolTip(
	// "Simple structural appropriateness based on the size of the process model");
	// aS_option.setDescription("The <b>simple structural appropriateness</b> metric <i>sa<sub>S</sub></i> is a simple metric based on the graph size of the model "
	// +
	// "(the greater the value the more compact is the model). " +
	// "Note that this metric should only be used as a comparative means for models allowing for the same amount of behavior.");
	// aS_option.setNewAnalysisMethod(AnalysisMethodEnum.STRUCTURAL);
	// //// build structural appropriatness section
	// AnalysisConfiguration structAppropOptions = new AnalysisConfiguration();
	// structAppropOptions.setName("Structure");
	// structAppropOptions.setToolTip("Structural Appropriateness Analysis");
	// structAppropOptions.setDescription("Structural Appropriateness evaluates whether the model describes the observed process in a <i>structurally suitable</i> way.");
	// structAppropOptions.addChildConfiguration(aS_option);
	// // indicate the type of analysis method that is needed
	// structAppropOptions.addRequestedMethod(AnalysisMethodEnum.STRUCTURAL);
	//
	// AnalysisConfiguration analysisOptions = new AnalysisConfiguration();
	// analysisOptions.addChildConfiguration(structAppropOptions);
	// return analysisOptions;
	// }

	// ////////////////////////// MDL measurer (log replay) for MDL metric
	// ////////////////////////////////////

	/**
	 * Class taking all the measurements that are needed to calculate the MDL
	 * metric for the given log and model (cost to encode the log in model)
	 * during log replay.
	 * 
	 * @author Anne Rozinat
	 */
	public class MDLMeasurer extends ConformanceMeasurer {

		@Override
		protected void takeFailedTaskMeasurement(ReplayedLogTrace pi,
				ReplayedTransition t, AuditTrailEntry ate) {
			// TODO Remember that current ate has failed execution (will be
			// punished by MDL metric for lack of fitness)
			// super.takeFailedTaskMeasurement(pi, t, ate);
		}

		@Override
		protected void takePreStepExecutionMeasurement(
				LogReplayAnalysisResult result, ReplayedLogTrace pi,
				int maxDepth) {
			// determine Number of enabled tasks

			// build (partial) coverability graph from the current marking,
			// note that the net must be cloned before to preserve its replay
			// state
			DiagnosticPetriNet clonedNet = (DiagnosticPetriNet) result.replayedPetriNet
					.clone();
			StateSpace coverabilityGraph = CoverabilityGraphBuilder.build(
					clonedNet, maxDepth);
			// find current replay state in state space (i.e., the start state)
			State replayState = (State) coverabilityGraph.getStartState();

			// hash set containing the all the visible tasks that have already
			// been reached in the state space (i.e., were already counted being
			// enabled)
			HashSet<DiagnosticTransition> alreadyCounted = new HashSet<DiagnosticTransition>();

			// get upper limit
			int maxEnabled = result.replayedPetriNet.getNumberOfVisibleTasks();
			// contains the states that have been visited already
			HashSet<State> visitedStates = new HashSet<State>();

			// start recursive procedure
			alreadyCounted = traceEnabledTasks(alreadyCounted,
					coverabilityGraph, replayState, maxEnabled, visitedStates);

			// record measurement for current ate
			// this.addMeanNumberEnabledMeasurement(alreadyCounted.size());
		}

		/**
		 * TODO: avoid duplicating code from Conformance Measurer!
		 * 
		 * Recursively trace the given state space for enabled transitions while
		 * adding newly found (visible) tasks to the alreadyCounted set.
		 * 
		 * @param alreadyCounted
		 *            Contains the visible (transparently) enabled transitions
		 *            so far encountered.
		 * @param coverabilityGraph
		 *            The coverability graph to be traversed.
		 * @param currentState
		 *            The current traversal state.
		 * @return The transitions so far encountered.
		 */
		private HashSet<DiagnosticTransition> traceEnabledTasks(
				HashSet<DiagnosticTransition> alreadyCounted,
				StateSpace coverabilityGraph, State currentState,
				int maxEnabled, HashSet<State> visitedStates) {

			if ((visitedStates.contains(currentState) == true)
					|| (alreadyCounted.size() == maxEnabled)) {
				// abort to prevent infinite cycles and
				// stop tracing paths if all possible tasks are already
				// contained
				return alreadyCounted;
			}

			HashSet<DiagnosticTransition> mergedAlreadyCounted = new HashSet<DiagnosticTransition>();
			if (currentState.getOutEdges() != null) {
				Iterator outgoingEdges = currentState.getOutEdges().iterator();
				while (outgoingEdges.hasNext()) {
					ModelGraphEdge currentEdge = (ModelGraphEdge) outgoingEdges
							.next();
					DiagnosticTransition associatedTransition = (DiagnosticTransition) currentEdge.object;
					// spawn new path for every newly found invisible task
					if (associatedTransition.isInvisibleTask()) {
						// remember to prevent infinite loops
						visitedStates.add(currentState);
						State nextState = (State) currentEdge.getDest();
						// recursive call (merge sets)
						mergedAlreadyCounted.addAll(traceEnabledTasks(
								alreadyCounted, coverabilityGraph, nextState,
								maxEnabled, visitedStates));
					}
					// check whether transition was not counted yet
					else if (alreadyCounted.contains(associatedTransition) == false) {
						mergedAlreadyCounted.add(associatedTransition);
					}
				}
			}
			return mergedAlreadyCounted;
		}
	}
}
