package org.processmining.analysis.hmm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

import att.grappa.Edge;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.ViterbiCalculator;

/**
 * Class realizing the mapping from Simple Petri nets to HMMs. After creation
 * the HMM-based metrics can be requested via {@link #getMetrics()}.
 */
public class HmmAnalyzer {

	protected LogReader inputLog;
	protected PetriNet inputModel;
	protected Hmm<ObservationInteger> inputHmm;
	protected Hmm<ObservationInteger> adjustedHmm;
	protected List<List<ObservationInteger>> observationSequences; // 
	protected List<List<ObservationInteger>> nonDuplicateSequences; // traces
	// without
	// frequencies
	protected HashSet<LogEvent> alreadyProcessed;

	protected int successorFrequencies[]; // total number of observations
	// following i
	protected HmmAnalyzerMetrics data = new HmmAnalyzerMetrics();

	public HmmAnalyzer(PetriNet net, LogReader log) {
		inputLog = log;
		inputModel = net;
		inputHmm = createHmm();
		if (log != null) {
			createObservationSequences();
			adjustHmm();
			collectPreMeasurements();
			data.inputHmm = inputHmm;
			data.inputLog = log;
		}
	}

	public HmmAnalyzerMetrics getMetrics() {
		return data;
	}

	public void printMetrics() {
		double traceFitness = data.getTraceFitness();
		double modelLevelFitness = data.getModelLevelFitness();
		double logLevelFitness = data.getLogLevelFitness();
		double averageTraceFitness = data.getAverageTraceFitness();
		double modelPrecision = data.getModelPrecision();
		double totalPrecision = data.getTotalPrecision();
		double logCompleteness = data.getAbsoluteLogCompletenessPrecision();
		// write output
		System.out.println("Trace Fitness: " + traceFitness);
		System.out.println("Model-level Fitness: " + modelLevelFitness);
		System.out.println("Log-level Fitness: " + logLevelFitness);
		System.out.println("Averge Trace Fitness: " + averageTraceFitness);
		System.out.println("Model-level Precision: " + modelPrecision);
		System.out.println("Total Model Precision: " + totalPrecision);
		System.out.println("Absolute Log Completeness: " + logCompleteness
				+ "\n");
	}

	/**
	 * Returns the adjusted Hmm.
	 * 
	 * @return the Hmm after adjustment based on the log
	 */
	public Hmm<ObservationInteger> getAdjustedHmm() {
		return adjustedHmm;
	}

	/**
	 * Returns the input Hmm.
	 * 
	 * @return the model-based Hmm
	 */
	public Hmm<ObservationInteger> getInputHmm() {
		return inputHmm;
	}

	/**
	 * Returns the mapping of observation code symbols to actual log events
	 * (manifested by their natural position in the list).
	 * 
	 * @return the list of log events while position reflects integer code
	 */
	public ArrayList<LogEvent> getObservationMapping() {
		return data.observed;
	}

	/**
	 * Creates an HMM based on the given Petri net model. Assumes that there is
	 * no parallelism, no duplicate tasks, and a clear start place.
	 */
	protected Hmm<ObservationInteger> createHmm() {
		alreadyProcessed = new HashSet<LogEvent>();
		// create HMM (one state for each type of log event)
		data.observed = new ArrayList<LogEvent>();
		for (Transition trans : inputModel.getTransitions()) {
			if (trans.isInvisibleTask() == false) {
				if (data.observed.contains(trans.getLogEvent()) == false) {
					data.observed.add(trans.getLogEvent());
				}
			}
		}
		OpdfIntegerFactory factory = new OpdfIntegerFactory(data.observed
				.size() + 1);
		Hmm<ObservationInteger> resultHmm = new Hmm<ObservationInteger>(
				data.observed.size() + 1, factory); // plus final state
		// assign transition probabilities
		Place initialPlace = null;
		for (Place place : inputModel.getPlaces()) {
			if (place.inDegree() == 0 && place.outDegree() > 0) {
				initialPlace = place;
				continue;
			}
		}
		ArrayList<Transition> initialStates = new ArrayList<Transition>();
		if (initialPlace != null) {
			for (Edge edge : initialPlace.getOutEdges()) {
				Transition startTask = (Transition) edge.getHead();
				if (startTask.isInvisibleTask() == false) {
					initialStates.add(startTask);
				} else {
					initialStates.addAll(getNextVisibleTransitions(startTask));
				}
			}
		}
		// assign initial state probabilities
		for (int i = 0; i < data.observed.size() + 1; i++) {
			resultHmm.setPi(i, 0.0);
		}
		for (Transition trans : initialStates) {
			int index = data.observed.indexOf(trans.getLogEvent());
			resultHmm.setPi(index, (1.0 / (double) initialStates.size()));
			traceTransition(trans, resultHmm);
		}
		// assign observation element probabilities per state
		for (int i = 0; i < data.observed.size() + 1; i++) { // final state has
			// dummy
			// observation
			double[] prob = new double[data.observed.size() + 1];
			for (int j = 0; j < data.observed.size() + 1; j++) {
				if (i == j) {
					prob[j] = 1.0;
				} else {
					prob[j] = 0.0;
				}
			}
			resultHmm.setOpdf(i, new OpdfInteger(prob));
		}
		// find last place (assume one single final place)
		Place finalPlace = null;
		for (Place place : inputModel.getPlaces()) {
			if (place.outDegree() == 0 && place.inDegree() > 0) {
				finalPlace = place;
				continue;
			}
		}
		ArrayList<Transition> finalStates = new ArrayList<Transition>();
		if (finalPlace != null) {
			for (Edge edge : finalPlace.getInEdges()) {
				Transition endTask = (Transition) edge.getTail();
				if (endTask.isInvisibleTask() == false) {
					finalStates.add(endTask);
				} else {
					finalStates.addAll(getPreviousVisibleTransitions(endTask));
				}
			}
		}
		// assign final state probabilities
		for (Transition fin : finalStates) {
			int finIndex = data.observed.indexOf(fin.getLogEvent());
			ArrayList<Transition> allFinSuccessors = getNextVisibleTransitions(fin);
			// adjust the transition probabilities to the successors (lower as
			// also possible to end)
			for (Transition finSuc : allFinSuccessors) {
				int finSucIndex = data.observed.indexOf(finSuc.getLogEvent());
				resultHmm.setAij(finIndex, finSucIndex,
						(1.0 / (double) (allFinSuccessors.size() + 1)));
			}
			resultHmm.setAij(finIndex, data.observed.size(),
					(1.0 / (double) (allFinSuccessors.size() + 1)));
		}
		resultHmm.setAij(data.observed.size(), data.observed.size(), 1.0);
		for (int i = 0; i < data.observed.size(); i++) {
			resultHmm.setAij(data.observed.size(), i, 0.0);
		}
		return resultHmm;
	}

	protected ArrayList<Transition> getNextVisibleTransitions(Transition trans) {
		ArrayList<Transition> resultList = new ArrayList<Transition>();
		if (trans.outDegree() > 0) {
			Edge outEdge = trans.getOutEdges().get(0); // assume sequential
			// models
			Place outPlace = (Place) outEdge.getHead();
			if (outPlace.outDegree() > 0) {
				for (Edge nextEdge : outPlace.getOutEdges()) {
					Transition nextTrans = (Transition) nextEdge.getHead();
					if (nextTrans.isInvisibleTask() == false) {
						resultList.add(nextTrans);
					} else {
						resultList.addAll(getNextVisibleTransitions(nextTrans));
					}
				}
			}
		}
		return resultList;
	}

	protected ArrayList<Transition> getPreviousVisibleTransitions(
			Transition trans) {
		ArrayList<Transition> resultList = new ArrayList<Transition>();
		if (trans.inDegree() > 0) {
			Edge inEdge = trans.getInEdges().get(0); // assume sequential models
			Place inPlace = (Place) inEdge.getTail();
			if (inPlace.inDegree() > 0) {
				for (Edge prevEdge : inPlace.getInEdges()) {
					Transition prevTrans = (Transition) prevEdge.getTail();
					if (prevTrans.isInvisibleTask() == false) {
						resultList.add(prevTrans);
					} else {
						resultList
								.addAll(getPreviousVisibleTransitions(prevTrans));
					}
				}
			}
		}
		return resultList;
	}

	protected void traceTransition(Transition trans, Hmm<ObservationInteger> hmm) {
		if (alreadyProcessed.contains(trans.getLogEvent()) == false) {
			alreadyProcessed.add(trans.getLogEvent());
			int transIndex = data.observed.indexOf(trans.getLogEvent());
			// initialize transition probabilities with 0.0
			for (int i = 0; i < data.observed.size() + 1; i++) {
				hmm.setAij(transIndex, i, 0.0);
			}
			ArrayList<Transition> nextTransitions = getNextVisibleTransitions(trans);
			for (Transition nextTrans : nextTransitions) {
				int nextIndex = data.observed.indexOf(nextTrans.getLogEvent());
				hmm.setAij(transIndex, nextIndex,
						1.0 / ((double) nextTransitions.size()));
				traceTransition(nextTrans, hmm);
			}
		}
	}

	protected void createObservationSequences() {
		try {
			data.eventTraceFrequencies = new int[inputLog.getLogSummary()
					.getNumberOfUniqueProcessInstances()][data.observed.size()][data.observed
					.size()];
			successorFrequencies = new int[data.observed.size()]; // how often i
			// was
			// succeeded
			// by
			// anything
			// (needed
			// to
			// normalize
			// transition
			// probabilities
			// in hmm
			// later)
			data.traceFrequencies = new int[inputLog.getLogSummary()
					.getNumberOfUniqueProcessInstances()];
			// initialize
			for (int i = 0; i < data.observed.size(); i++) {
				successorFrequencies[i] = 0;
				for (int j = 0; j < data.observed.size(); j++) {
					for (int k = 0; k < inputLog.getLogSummary()
							.getNumberOfUniqueProcessInstances(); k++) {
						data.eventTraceFrequencies[k][i][j] = 0;
					}
				}
			}
			observationSequences = new ArrayList<List<ObservationInteger>>();
			nonDuplicateSequences = new ArrayList<List<ObservationInteger>>();
			Process process;
			ProcessInstance instance;
			AuditTrailEntryList ateList;
			int noGroupedInstances;
			int m = 0;
			for (int i = 0; i < inputLog.numberOfProcesses(); i++) {
				process = inputLog.getProcess(i);
				for (int j = 0; j < process.size(); j++) {
					instance = process.getInstance(j);
					int previousIndex = -1;
					noGroupedInstances = MethodsForWorkflowLogDataStructures
							.getNumberSimilarProcessInstances(instance);
					List<ObservationInteger> currentSequence = new ArrayList<ObservationInteger>();
					ateList = instance.getAuditTrailEntryList();
					for (int k = 0; k < ateList.size(); k++) {
						AuditTrailEntry current = ateList.get(k);
						LogEvent le = inputLog.getLogSummary().getLogEvents()
								.findLogEvent(current.getElement(),
										current.getType());
						int leIndex = data.observed.indexOf(le);
						currentSequence.add(new ObservationInteger(leIndex));
						// also store actual frequencies to adjust hmm
						// parameters
						if (previousIndex != -1) {
							data.eventTraceFrequencies[j][previousIndex][leIndex]++;
							successorFrequencies[previousIndex] = successorFrequencies[previousIndex]
									+ noGroupedInstances;
						}
						previousIndex = leIndex;
					}
					for (int k = 0; k < noGroupedInstances; k++) {
						observationSequences.add(currentSequence);
					}
					nonDuplicateSequences.add(currentSequence);
					data.traceFrequencies[m] = noGroupedInstances;
					m++;
				}
			}
		} catch (Exception ex) {
			System.out.println("Error reading Log");
			ex.printStackTrace();
		}
	}

	protected void evaluateObservationSequences(Hmm<ObservationInteger> hmm) {
		// calculate plain observation sequence probability for given hmm
		double sumProb = 0;
		int i = 0;
		for (List<ObservationInteger> seq : nonDuplicateSequences) {
			double currentProb = hmm.probability(seq);
			System.out
					.println("Sequence probability " + i + ": " + currentProb);
			sumProb = sumProb + currentProb;
			i++;
		}
		System.out
				.println("Total sum of observation probabilities: " + sumProb);
		// calculate most likely state sequence according to viterbi
		i = 0;
		for (List<ObservationInteger> seq : nonDuplicateSequences) {
			ViterbiCalculator viterbi = new ViterbiCalculator(seq, hmm);
			System.out
					.println("Most likely states for observation " + i + ": ");
			int[] stateSeq = viterbi.stateSequence();
			for (int j = 0; j < stateSeq.length; j++) {
				System.out.println(stateSeq[j] + " ");
			}
			i++;
		}
	}

	protected void collectPreMeasurements() {
		data.inputHmmProb = new double[nonDuplicateSequences.size()];
		data.adjustedHmmProb = new double[nonDuplicateSequences.size()];
		// calculate plain observation sequence probability for given hmm
		int i = 0;
		for (List<ObservationInteger> seq : nonDuplicateSequences) {
			data.inputHmmProb[i] = inputHmm.probability(seq);
			data.adjustedHmmProb[i] = adjustedHmm.probability(seq);
			i++;
		}
	}

	protected void adjustHmm() {
		adjustedHmm = createHmm();
		data.falseNegatives = new boolean[data.observed.size()][data.observed
				.size()];
		data.falsePositives = new boolean[data.observed.size()][data.observed
				.size()];
		for (int i = 0; i < data.observed.size(); i++) {
			for (int j = 0; j < data.observed.size(); j++) {
				double modelProb = adjustedHmm.getAij(i, j);
				double actualProb = 0.0;
				if (successorFrequencies[i] != 0) {
					// actualProb = (double) data.actualFrequencies[i][j] /
					// (double) successorFrequencies[i];
					for (int k = 0; k < inputLog.getLogSummary()
							.getNumberOfUniqueProcessInstances(); k++) {
						actualProb = actualProb
								+ ((double) data.eventTraceFrequencies[k][i][j] * data.traceFrequencies[k]);
					}
					actualProb = actualProb / (double) successorFrequencies[i];
				} else {
					actualProb = 0.0;
				}
				// detect false negatives
				if (modelProb == 0.0 && actualProb > 0.0) {
					data.falseNegatives[i][j] = true;
				} else {
					data.falseNegatives[i][j] = false;
				}
				// detect false positives
				if (actualProb == 0.0 && modelProb > 0.0) {
					data.falsePositives[i][j] = true;
				} else {
					data.falsePositives[i][j] = false;
				}
				// adjust a_ij in hmm
				adjustedHmm.setAij(i, j, actualProb);
			}
		}
	}

	public List<List<ObservationInteger>> getObservationSequences() {
		return observationSequences;
	}

	public List<List<ObservationInteger>> getNonDuplicateSequences() {
		return nonDuplicateSequences;
	}

	public Hmm<ObservationInteger> createRandomHmm() {
		alreadyProcessed = new HashSet<LogEvent>();
		// create HMM (one state for each type of log event)
		data.observed = new ArrayList<LogEvent>();
		for (Transition trans : inputModel.getTransitions()) {
			if (trans.isInvisibleTask() == false) {
				if (data.observed.contains(trans.getLogEvent()) == false) {
					data.observed.add(trans.getLogEvent());
				}
			}
		}
		OpdfIntegerFactory factory = new OpdfIntegerFactory(data.observed
				.size() + 1);
		Hmm<ObservationInteger> resultHmm = new Hmm<ObservationInteger>(
				data.observed.size() + 1, factory); // plus final state
		// assign initial state probabilities
		for (int i = 0; i < data.observed.size(); i++) {
			resultHmm.setPi(i, (1.0 / (double) data.observed.size()));
		}
		// assign observation element probabilities per state
		for (int i = 0; i < data.observed.size() + 1; i++) { // final state has
			// dummy
			// observation
			double[] prob = new double[data.observed.size() + 1];
			for (int j = 0; j < data.observed.size() + 1; j++) {
				if (i == j) {
					prob[j] = 1.0;
				} else {
					prob[j] = 0.0;
				}
			}
			resultHmm.setOpdf(i, new OpdfInteger(prob));
		}
		// assign transition probabilities
		for (int i = 0; i < data.observed.size(); i++) {
			for (int j = 0; j < data.observed.size() + 1; j++) {
				resultHmm.setAij(i, j,
						(1.0 / ((double) (data.observed.size() + 1))));
			}
		}
		// assign transition probabilities from final state
		resultHmm.setAij(data.observed.size(), data.observed.size(), 1.0);
		resultHmm.setPi(data.observed.size(), 0.0);
		for (int i = 0; i < data.observed.size(); i++) {
			resultHmm.setAij(data.observed.size(), i, 0.0);
		}
		return resultHmm;
	}

}
