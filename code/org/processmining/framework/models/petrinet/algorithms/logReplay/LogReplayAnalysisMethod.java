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

package org.processmining.framework.models.petrinet.algorithms.logReplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Token;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.InitialPlaceMarker;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;

/**
 * Replays the given log in the given Petri net model in a non-blocking way. It
 * replays the given log in the given Petri net model in a non-blocking way
 * while taking care of invisible tasks that might transparently enable visible
 * tasks, and duplicate tasks, which lead to a conflict that needs to be
 * resolved. <br>
 * Taking measurements that are needed to calculate any metrics based on the log
 * replay method is only done in the {@link Measurer Measurer} object. That is,
 * if an additional measurement step is to be introduced, call a newly defined
 * <code>protected</code> "takeAdditionalmeasurement()" method (that does
 * nothing per default but can be overridden in your specific subclass of the
 * measurer during the appropriate phase of log replay. This way, side effects
 * on other log-replay using applications can be avoided. <br>
 * All the diagnostic information is stored in an object of the class
 * {@link LogReplayAnalysisResult LogReplayAnalysisResult}. Deriving subclasses
 * should create their special-purpose subclass from it in
 * {@link Measurer#initLogReplayAnalysisResult initLogReplayAnalysisResult}.
 * 
 * @author arozinat
 */
public class LogReplayAnalysisMethod implements AnalysisMethod {

	protected PetriNet myPetriNet;
	protected LogReader myLogReader;
	protected LogReplayAnalysisResult myResult;
	protected Measurer myMeasurer;

	protected Progress myProgress; // the progress bar object that needs to be
	// updated
	protected int progressCounter; // the counter of currently replayed audit
	// trail entries

	// temporal storage for the currently replayed log trace (for exploring
	// future in ambigious cases)
	protected AuditTrailEntryList ates;
	protected int atesPos;

	/**
	 * Integer value to represent the option to build only partial coverability
	 * graphs when searching for shortest invisible task sequences. <br>
	 * Note that in the case of a restricted search possible enabling sequences
	 * exceeding the specified lenght will not be found. <br>
	 * Per default the whole state space will be constructed. Use
	 * {@link #setMaxDepth(int)} in order to restrict the search depth during
	 * log replay.
	 */
	private int saveTroubles = -1;

	/**
	 * Option determining whether to look into the future for choosing among
	 * multiple shortest sequences of invisible tasks.
	 * <p>
	 * Needed to replay EPC OR splits after conversion to Petri nets.
	 * <p>
	 * Switched off per default for performance reasons.
	 */
	public boolean findBestShortestSequence = false;

	/**
	 * Creates the log replay analysis method object.
	 * 
	 * @param inputPetriNet
	 *            the PetriNet passed to the conformance check plugin
	 * @param inputLog
	 *            the LogReader passed to the conformance check plugin
	 */
	public LogReplayAnalysisMethod(PetriNet inputPetriNet, LogReader inputLog,
			Measurer measurer, Progress progress) {
		myPetriNet = inputPetriNet;
		myLogReader = inputLog;
		myMeasurer = measurer;
		myProgress = progress;
	}

	/**
	 * Restricts the depth of search for a sequence of invisible tasks that
	 * might enable the task currently to be replayed. <br>
	 * Per default the whole state space is built each time a transition is not
	 * enabled in order to check for a potential enabling sequence of invisible
	 * tasks only. If this leads to performance bottlenecks, you might want to
	 * restrict the maximum lenght of invisible task sequences that can be found
	 * by the log replay method.
	 * 
	 * @param maxDepth
	 *            indicates the maximum length for an enabling sequence of
	 *            invisible tasks that can be found during log replay. If it is
	 *            0, no invisible task will be considered for whether it might
	 *            enable the currently replayed task. If it is 1, enabling
	 *            sequences of length 1 can be found (if they exist) etc. Note
	 *            that the full state space will be constructed, if the provided
	 *            maxDepth < 0
	 */
	public void setMaxDepth(int maxDepth) {
		this.saveTroubles = maxDepth;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return the {@link AnalysisMethodEnum#LOG_REPLAY LOG_REPLAY}
	 */
	public AnalysisMethodEnum getIdentifier() {
		return AnalysisMethodEnum.LOG_REPLAY;
	}

	/**
	 * Invokes the log replay analysis. <br>
	 * Method replaying the log in a non-blocking way. This means that for each
	 * event in the log the associated task in the model is looked up and fired.
	 * In the case of a transition not being enabled, the missing tokens are
	 * created artificially and the transition is fired anyway. <br>
	 * Furthermore, the Petri net is allowed to contain invisible tasks, i.e.,
	 * which get not logged and therefore are not visible in the log. <br>
	 * Note that the diagnostic log is replayed in the diagnostic petri net,
	 * which both are created by initializing the corresponding
	 * {@link LogReplayResult LogReplayResult} object. This means that parts of
	 * the diagnostic data collection are carried out within the diagnostic
	 * structures itself, e.g., incrementing a counter for firing a transition.
	 * <p>
	 * If you simply want to use some of the metrics calculated by the
	 * conformance checker in your own code it might be better to use the
	 * benchmark metrics. For example, you can find the fitness metric f in the
	 * class org.processmining.analysis.benchmark.metric.TokenFitnessMetric. The
	 * advantage there is that only the particular metric that you want to use
	 * is calculated (sometimes some other metric may take too much time if you
	 * calculate all), and that it is easy to use. <br>
	 * From your code you could simply call it like this:
	 * 
	 * <code>
	 * <br>TokenFitnessMetric fitness = new TokenFitnessMetric();
	 * <br>double fitnessResult = fitness.measure(inputModel, inputLog, null, new Progress(""));
	 * <br>System.out.println("Fitness: " + fitnessResult);
	 * </code>
	 * 
	 * @param analysisOptions
	 *            the configuration object determining which parts of the
	 *            analysis should be carried out (can be <code>null</code>, then
	 *            all options are interpreted as enabled)
	 * @return the {@link LogReplayAnalysisResult LogReplayAnalysisResult}
	 *         object containing all diagnostic results if the log replay could
	 *         be finished successfully, <code>null</code> otherwise
	 */
	public AnalysisResult analyse(AnalysisConfiguration analysisOptions) {

		// initialize the result object and the analysis options
		myResult = myMeasurer.initLogReplayAnalysisResult(analysisOptions,
				myPetriNet, myLogReader, this);

		// inject measurement step
		myMeasurer.initLogReplay();

		// only perform log reply if at least one of the metrics has been
		// selected
		if (myResult.performLogReplay() == true) {
			try {
				ReplayedLogReader diagnosticLog = myResult.replayedLog;

				// determine no. of instances and ates in log
				int noInstancesInLog = myLogReader.getLogSummary()
						.getNumberOfProcessInstances();
				int noATEsInLog = myLogReader.getLogSummary()
						.getNumberOfAuditTrailEntries();
				progressCounter = 1; // will count totally processed ates
				int piCounter = 0; // declare process instance counter (for
				// progress bar text note)
				// start monitoring progress of log replay
				myProgress.setMaximum(noATEsInLog + 1);
				myProgress.setMinimum(0);
				myProgress
						.setNote("Replaying Log Trace                        "); // make
				// wide
				// enough
				// for
				// 1000er
				// range
				myProgress.setProgress(progressCounter);

				// now step through the diagnostic log and replay each process
				// instance
				diagnosticLog.reset();
				while (diagnosticLog.hasNext()) {
					// replay this logical trace or process instance (pi)
					ReplayedLogTrace pi = diagnosticLog.next();
					if (myProgress.isCanceled() == true) {
						// abort if user cancelled the log replay
						Message
								.add("Log replay has been cancelled by the user.");
						return null;
					} else {
						// update GUI progress bar text note
						piCounter = piCounter + 1;
						myProgress.setNote("Replaying Log Trace " + (piCounter)
								+ " of " + noInstancesInLog);
						myProgress.setProgress(progressCounter); // is not
						// necessary
						// but might
						// make
						// appear
						// dialog
						// earlier?
					}
					if (replayTrace(pi, myLogReader) == false) {
						return null;
					}
					// finish replay of that trace
					postTraceReplayAnalysis(pi);
				}

				// finish GUI progress bar (should actually not be necessary)
				myProgress.setProgress(noATEsInLog + 1);

				// replay has finished
				myMeasurer.takePostReplayMeasurement(myResult);
				return myResult;

			} catch (Exception ex) {
				Message.add("Log replay has failed.\n" + ex.toString(), 2);
				ex.printStackTrace();
				return null;
			}
		} else {
			return myResult;
		}
	}

	/**
	 * Replay the given log trace and store diagnostic results while doing so.
	 * 
	 * @param diagnosticNet
	 *            The enhanced Petri net model in which the trace is replayed.
	 * 
	 * @param pi
	 *            the log trace to be replayed
	 * @param log
	 *            the log reader accessing the log to be replayed
	 * @return <code>true</code> if could be finished successfully,
	 *         <code>false</code> otherwise.
	 */
	private boolean replayTrace(ReplayedLogTrace pi, LogReader log) {
		try {
			// inject measurement step
			myMeasurer.initTraceReplay(pi, myResult);

			// remove all tokens and initialize the petri net with one token in
			// start place
			InitialPlaceMarker.mark(myResult.replayedPetriNet, 1);
			ates = pi.getProcessInstance().getAuditTrailEntryList();
			Iterator atesIt = ates.iterator();
			// keep the iterator position for the case that the log replay must
			// be simulated ahead
			// (should be faster than manually cloning iterators all the time)
			atesPos = 0;
			// step through the trace / audit trail of the diagnostic log trace
			while (atesIt.hasNext()) {
				if (myProgress.isCanceled() == true) {
					// abort if user cancelled the log replay
					Message.add("Log replay has been cancelled by the user.");
					return false;
				} else {
					// update progress bar
					myProgress.setProgress(progressCounter);
				}
				AuditTrailEntry ate = (AuditTrailEntry) atesIt.next();
				// injected measurement steps
				myMeasurer.takePreStepExecutionMeasurement(myResult, pi,
						saveTroubles);
				myMeasurer.takeLogEventRecordingMeasurement(myResult, pi, ate);
				// Which logEvent happened?
				LogEvent le = log.getLogSummary().getLogEvents().findLogEvent(
						ate.getElement(), ate.getType());
				// get a list of all transitions that are associated to that log
				// event
				ArrayList<Transition> allAssociatedTasks = myResult.replayedPetriNet
						.findTransitions(le);

				// 0:1 mapping (no task belonging to that log event - nothing to
				// do)
				// (usually does not happen because of log being pre-processed)
				if (allAssociatedTasks.size() == 0) {
					break;
				}

				// 1:1 mapping (exactly one task belongs to that log event -
				// easy case)
				else if (allAssociatedTasks.size() == 1) {
					ReplayedTransition t = (ReplayedTransition) allAssociatedTasks
							.get(0);
					// if transition can be enabled via executing invisible
					// tasks, this is
					// done transparently here and results in true
					if (t.isEnabled(pi, ate.getTimestamp(), saveTroubles,
							myMeasurer, this, atesPos) == false) {
						// if transition belonging to the log event is not ready
						// to fire,
						// enable it by creating the missing tokens
						// (non-blocking log replay)
						artificiallyEnableFailedTask(pi, t, ate);
					}
					// it has been made sure manually that all input places are
					// marked,
					// so now firing without check (ie. quickly) is ok
					t.fireQuick(pi, ate.getTimestamp(), myMeasurer);
				}

				// n:1 mapping (duplicate task - need to find the best one to
				// fire)
				else {
					ReplayedTransition chosenTask = chooseEnabledDuplicateTask(
							allAssociatedTasks, myResult.replayedPetriNet,
							atesPos, pi, ate.getTimestamp());
					if (chosenTask == null) {
						// none was enabled -> fire arbitrary one
						chosenTask = (ReplayedTransition) allAssociatedTasks
								.get(0);
						artificiallyEnableFailedTask(pi, chosenTask, ate);
					}
					chosenTask.fireQuick(pi, ate.getTimestamp(), myMeasurer);
				}
				// keep the iterator position for the case that the log replay
				// must be simulated ahead
				// (should be faster than manually cloning iterators all the
				// time)
				atesPos++; // is ate position within trace
				progressCounter++; // is absolute ate counter for progess bar
			}
			return true;
		} catch (Exception ex) {
			Message.add("Replay failed for trace " + pi.getName() + ".\n"
					+ ex.toString(), 2);
			ex.printStackTrace();
			return false;
		}
	}

	// /////// SECTION DEALING WITH DUPLICATE TASKS //////////

	/**
	 * Determines the best of all enabled duplicate tasks.
	 * 
	 * @param duplicates
	 *            the list of all duplicate tasks
	 * @param replayedNet
	 *            the replayed Petri net Note that due to recursive calls on
	 *            cloned replay scenarios it is necessary to explicitly pass
	 *            this parameter (instead of retrieving it from the
	 *            <code>myResult</code> object as usually)
	 * @param atesPos
	 *            the position within the list of audit trail entries (current
	 *            replay state)
	 * @param ates
	 *            the currently replayed trace as a list of audit trail entries
	 * @param currentTrace
	 *            the log trace for which the potential firings will be noted
	 *            (can be <code>null</code> if no diagnostic information should
	 *            be collected)
	 * @param timeStamp
	 *            the time at which the transition should be enabled
	 * @return the best enabled duplicate task. <code>Null</code>, if none of
	 *         the given transitions is enabled
	 * @exception IndexOutOfBoundsException
	 *                indicates that the log replay tried to read outside of the
	 *                available range of audit trail entries (needs to be
	 *                handled on higher level as returning null is an expected
	 *                result within this algorithm)
	 * @exception IOException
	 *                indicates that something went wrong while reading the
	 *                underlying random access file (only applies if the
	 *                high-performance log reader is used)
	 */
	private ReplayedTransition chooseEnabledDuplicateTask(
			List<Transition> duplicates, ReplayedPetriNet replayedNet,
			int atesPos, ReplayedLogTrace currentTrace, Date timeStamp)
			throws IndexOutOfBoundsException, IOException {

		ReplayedTransition chosenTransition = null;
		List<Transition> enablingSequence = null;
		HashMap<Transition, List<Transition>> possibleCandidates = selectEnabledCandidates(
				duplicates, replayedNet, atesPos);

		// no enabled transition found
		if (possibleCandidates.size() == 0) {
			return chosenTransition;
		}
		// multiple enabled transitions found
		else if (possibleCandidates.size() > 1) {
			// choose for the best candidate
			possibleCandidates = chooseBestCandidate(possibleCandidates,
					atesPos, replayedNet);
		}

		// retrieve the only solution left
		Entry<Transition, List<Transition>> singleEntry = (Entry<Transition, List<Transition>>) possibleCandidates
				.entrySet().iterator().next();
		chosenTransition = (ReplayedTransition) singleEntry.getKey();
		enablingSequence = singleEntry.getValue();

		// execute enabling sequence of invisible tasks (might have length 0)
		chosenTransition.fireSequenceOfTasks(enablingSequence, replayedNet,
				currentTrace, timeStamp, myMeasurer);
		return chosenTransition;
	}

	/**
	 * Sorts out all those given duplicate tasks that can be enabled via firing
	 * a number of invisible tasks within the given net. If a transition is
	 * already enabled the corresponding list is of length 0.
	 * 
	 * @param duplicates
	 *            the list of duplicate tasks to be checked
	 * @param replayedNet
	 *            the replayed Petri net
	 * @return map containing the <code>ReplayedTransition</code> as key and the
	 *         enabling firing sequence in an <code>ArrayList</code> as value
	 */
	private HashMap<Transition, List<Transition>> selectEnabledCandidates(
			List<Transition> duplicates, ReplayedPetriNet replayedNet,
			int atesPos) {
		HashMap<Transition, List<Transition>> possibleCandidates = new HashMap<Transition, List<Transition>>();
		for (Transition currentDuplicate : duplicates) {
			List<Transition> enablingSequence = ((ReplayedTransition) currentDuplicate)
					.getShortestSequenceOfInvisibleTasks(replayedNet,
							saveTroubles, this, atesPos);
			// for a transition which is already enabled,
			// the result sequence is of length 0 (but not null)
			if (enablingSequence != null) {
				possibleCandidates.put(currentDuplicate, enablingSequence);
			}
		}
		return possibleCandidates;
	}

	/**
	 * Chooses the best candidate out of a set of enabled duplicate tasks by
	 * looking into the future (of the currently replayed trace).
	 * 
	 * @param enabledCandidates
	 *            the given set of enabled duplicate tasks as keys with their
	 *            corresponding enabling sequences (which might have length 0)
	 *            as values
	 * @param replayedNet
	 *            the replayed Petri net
	 * @param atesPos
	 *            the replay position within the list of audit trail entries
	 * @param ates
	 *            the currently replayed trace as a list of audit trail entries
	 * @return map containing only one entry with the best candidate as key and
	 *         its potential enabling sequence of invisible tasks as value
	 * @exception IndexOutOfBoundsException
	 *                indicates that the log replay tried to read outside of the
	 *                available range of audit trail entries (needs to be
	 *                handled on higher level as returning null is an expected
	 *                result within this algorithm)
	 * @exception IOException
	 *                indicates that something went wrong while reading the
	 *                underlying random access file (only applies if the
	 *                high-performance log reader is used)
	 */
	public HashMap<Transition, List<Transition>> chooseBestCandidate(
			HashMap<Transition, List<Transition>> enabledCandidates,
			int atesPos, ReplayedPetriNet replayedNet)
			throws IndexOutOfBoundsException, IOException {
		HashMap<ReplayedTransition, ReplayedPetriNet> scenarios = new HashMap<ReplayedTransition, ReplayedPetriNet>();
		for (Entry<Transition, List<Transition>> currentEntry : enabledCandidates
				.entrySet()) {
			ReplayedTransition currentCandidate = (ReplayedTransition) currentEntry
					.getKey();
			List<Transition> enablingSequence = currentEntry.getValue();
			// clone Petri net to preserve replay status
			ReplayedPetriNet clonedNet = (ReplayedPetriNet) replayedNet.clone();
			ReplayedTransition currentCandidateInClonedNet = (ReplayedTransition) clonedNet
					.findTransition(currentCandidate);
			// pretend having executed the candidate to be ready for looking
			// into the future
			// null time stamp is passed as no transitions in the original net
			// will be fired anyway
			currentCandidateInClonedNet.fireSequenceOfTasks(enablingSequence,
					clonedNet, null, null, new Measurer()); // produced and
			// consumed tokens
			// etc. should not
			// be actually
			// counted!
			currentCandidateInClonedNet.fireQuick();
			scenarios.put(currentCandidate, clonedNet);
		}

		// start looking into the future
		scenarios = traceBestCandidate(scenarios, atesPos);
		// returns single scenario, which can be mapped back to the initial list
		ReplayedTransition bestCandidate = (ReplayedTransition) scenarios
				.entrySet().iterator().next().getKey();
		ArrayList<Transition> enablingSequenceBestCandidate = (ArrayList<Transition>) enabledCandidates
				.get(bestCandidate);
		HashMap<Transition, List<Transition>> solution = new HashMap<Transition, List<Transition>>();
		solution.put(bestCandidate, enablingSequenceBestCandidate);
		return solution;
	}

	/**
	 * Recursive helper method to determine the best candidate out of a set of
	 * enabled duplicate tasks by looking into the future (of the currently
	 * replayed trace).
	 * 
	 * @param replayScenarios
	 *            the set of remaining candidates as keys and their
	 *            corresponding replay scenarios as values
	 * @param ates
	 *            the currently replayed trace as a list of audit trail entries
	 * @param atesPos
	 *            the replay position within the list of audit trail entries
	 * @return map containing only one entry with the best candidate as key (the
	 *         corresponding value might be <code>null</code>)
	 * @exception IndexOutOfBoundsException
	 *                indicates that the log replay tried to read outside of the
	 *                available range of audit trail entries (needs to be
	 *                handled on higher level as returning null is an expected
	 *                result within this algorithm)
	 * @exception IOException
	 *                indicates that something went wrong while reading the
	 *                underlying random access file (only applies if the
	 *                high-performance log reader is used)
	 */
	private HashMap<ReplayedTransition, ReplayedPetriNet> traceBestCandidate(
			HashMap<ReplayedTransition, ReplayedPetriNet> replayScenarios,
			int atesPos) throws IndexOutOfBoundsException, IOException {
		HashMap<ReplayedTransition, ReplayedPetriNet> remainingScenarios = new HashMap<ReplayedTransition, ReplayedPetriNet>();
		// check whether there are events remaining in the log
		atesPos++;
		if (atesPos < ates.size()) {
			AuditTrailEntry ate = ates.get(atesPos);
			// since the Petri nets are cloned (i.e., structurally identical),
			// one of them can serve for all
			ReplayedPetriNet oneNet = (ReplayedPetriNet) replayScenarios
					.values().iterator().next();
			LogEvent le = myResult.replayedLog.getLogSummary().getLogEvents()
					.findLogEvent(ate.getElement(), ate.getType());
			ArrayList<Transition> allAssociatedTasks = oneNet
					.findTransitions(le);
			// 0:1 mapping -> skip this event (usually does not happen because
			// of log being pre-processed)
			if (allAssociatedTasks.size() == 0) {
				remainingScenarios = traceBestCandidate(replayScenarios,
						atesPos);
			}
			// 1:1 mapping -> check for all scenarios whether the belonging
			// transition is enabled
			else if (allAssociatedTasks.size() == 1) {
				for (Entry<ReplayedTransition, ReplayedPetriNet> currentCandidateEntry : replayScenarios
						.entrySet()) {
					ReplayedTransition currentCandidate = currentCandidateEntry
							.getKey();
					ReplayedPetriNet currentScenario = currentCandidateEntry
							.getValue();
					// there is only one belonging transition in the net ->
					// findRandomTransition() can be used
					ReplayedTransition belongingTransition = (ReplayedTransition) currentScenario
							.findRandomTransition(le);
					List<Transition> enablingSequence = belongingTransition
							.getShortestSequenceOfInvisibleTasks(
									currentScenario, saveTroubles, this,
									atesPos);
					// for a transition which is already enabled,
					// the result sequence is of length 0 (but not null)
					if (enablingSequence != null) {
						belongingTransition.fireSequenceOfTasks(
								enablingSequence, currentScenario, null, null,
								new Measurer()); // produced and consumed tokens
						// etc. should not be
						// actually counted!
						belongingTransition.fireQuick();
						remainingScenarios.put(currentCandidate,
								currentScenario);
					}
				}
			}
			// n:1 mapping -> check for all scenarios whether one of the
			// belonging transitions is enabled
			else if (allAssociatedTasks.size() > 1) {
				for (Entry<ReplayedTransition, ReplayedPetriNet> currentCandidateEntry : replayScenarios
						.entrySet()) {
					ReplayedTransition currentCandidate = currentCandidateEntry
							.getKey();
					ReplayedPetriNet currentScenario = currentCandidateEntry
							.getValue();
					ArrayList<Transition> belongingTransitions = (ArrayList<Transition>) currentScenario
							.findTransitions(le);
					// copy audit trail with current iterator position (in order
					// to keep the actual replay status)
					ReplayedTransition chosenTransition = chooseEnabledDuplicateTask(
							belongingTransitions, currentScenario, atesPos,
							null, null);
					// if none of them is enabled, do not proceed with this
					// candidate
					if (chosenTransition != null) {
						List<Transition> enablingSequence = chosenTransition
								.getShortestSequenceOfInvisibleTasks(
										currentScenario, saveTroubles, this,
										atesPos);
						chosenTransition.fireSequenceOfTasks(enablingSequence,
								currentScenario, null, null, new Measurer()); // produced
						// and
						// consumed
						// tokens
						// etc.
						// should
						// not
						// be
						// actually
						// counted!
						chosenTransition.fireQuick();
						remainingScenarios.put(currentCandidate,
								currentScenario);
					}
				}
			}
			// just choose any of the remaining candidates
			if (remainingScenarios.size() == 0) {
				Entry<ReplayedTransition, ReplayedPetriNet> chosenEntry = replayScenarios
						.entrySet().iterator().next();
				ReplayedTransition chosenDuplicate = chosenEntry.getKey();
				// the actual replay scenario is not needed anymore since the
				// decision is made
				remainingScenarios.put(chosenDuplicate, null);
				return remainingScenarios;
			}
			// recursively continue the selection
			else if (remainingScenarios.size() > 1) {
				remainingScenarios = traceBestCandidate(remainingScenarios,
						atesPos);
			}
			// return the only candidate left
			return remainingScenarios;
		}
		// just choose any of the remaining candidates
		else {
			Entry<ReplayedTransition, ReplayedPetriNet> chosenEntry = replayScenarios
					.entrySet().iterator().next();
			ReplayedTransition chosenDuplicate = chosenEntry.getKey();
			// the actual replay scenario is not needed anymore since the
			// decision is made
			remainingScenarios.put(chosenDuplicate, null);
			return remainingScenarios;
		}
	}

	// /////// SECTION DEALING WITH EQUALLY LONG SEQUENCES OF INVISIBLE TASKS
	// //////////

	/**
	 * Looks into the future for deciding which of the candidate sequences
	 * should be fired (to optimize replay towards future actions).
	 * <p>
	 * Optional as increases complexity of replay. Mainly introduced to be able
	 * to deal with constructs emerging from OR split conversion.
	 * 
	 * @param enabledCandidates
	 *            the pairs of (a) the list of invisible transitions and the (b)
	 *            transition they are enabling
	 * @param petriNet
	 *            the net to be replayed in the current marking
	 * @param atesPos
	 *            the currently replayed (or imitated) log event
	 */
	public List<Transition> chooseBestSequenceOfInvisibleTasks(
			HashMap<List<Transition>, Transition> enabledCandidates,
			ReplayedPetriNet petriNet, int atesPos)
			throws IndexOutOfBoundsException, IOException {
		HashMap<ReplayedPetriNet, ReplayedTransition> scenarios = new HashMap<ReplayedPetriNet, ReplayedTransition>();
		HashMap<ReplayedPetriNet, List<Transition>> lookup = new HashMap<ReplayedPetriNet, List<Transition>>();
		for (Entry<List<Transition>, Transition> currentEntry : enabledCandidates
				.entrySet()) {
			ReplayedTransition currentCandidate = (ReplayedTransition) currentEntry
					.getValue();
			List<Transition> enablingSequence = currentEntry.getKey();
			// clone Petri net to preserve replay status
			ReplayedPetriNet clonedNet = (ReplayedPetriNet) petriNet.clone();
			ReplayedTransition currentCandidateInClonedNet = (ReplayedTransition) clonedNet
					.findTransition(currentCandidate);
			// pretend having executed the candidate to be ready for looking
			// into the future
			// null time stamp is passed as no transitions in the original net
			// will be fired anyway
			currentCandidateInClonedNet.fireSequenceOfTasks(enablingSequence,
					clonedNet, null, null, new Measurer()); // produced and
			// consumed tokens
			// etc. should not
			// be actually
			// counted!
			currentCandidateInClonedNet.fireQuick();
			scenarios.put(clonedNet, currentCandidate);
			lookup.put(clonedNet, enablingSequence);
		}

		// start looking into the future
		scenarios = traceBestSequenceOfInvisibleTasks(scenarios, atesPos);
		// returns single scenario, which can be mapped back to the initial list
		ReplayedPetriNet bestScenario = (ReplayedPetriNet) scenarios.entrySet()
				.iterator().next().getKey();
		List<Transition> solution = lookup.get(bestScenario);
		return solution;
	}

	/**
	 * Recursively traces scenarios of invisible tasks into the future.
	 */
	private HashMap<ReplayedPetriNet, ReplayedTransition> traceBestSequenceOfInvisibleTasks(
			HashMap<ReplayedPetriNet, ReplayedTransition> replayScenarios,
			int atesPos) throws IndexOutOfBoundsException, IOException {
		HashMap<ReplayedPetriNet, ReplayedTransition> remainingScenarios = new HashMap<ReplayedPetriNet, ReplayedTransition>();
		// check whether there are events remaining in the log
		atesPos++;
		if (atesPos < ates.size()) {
			AuditTrailEntry ate = ates.get(atesPos);
			// since the Petri nets are cloned (i.e., structurally identical),
			// one of them can serve for all
			ReplayedPetriNet oneNet = (ReplayedPetriNet) replayScenarios
					.keySet().iterator().next();
			LogEvent le = myResult.replayedLog.getLogSummary().getLogEvents()
					.findLogEvent(ate.getElement(), ate.getType());
			ArrayList<Transition> allAssociatedTasks = oneNet
					.findTransitions(le);

			// 0:1 mapping -> skip this event (usually does not happen because
			// of log being pre-processed)
			if (allAssociatedTasks.size() == 0) {
				remainingScenarios = traceBestSequenceOfInvisibleTasks(
						replayScenarios, atesPos);
			}

			// 1:1 mapping -> check for all scenarios whether the belonging
			// transition is enabled
			else if (allAssociatedTasks.size() == 1) {
				for (Entry<ReplayedPetriNet, ReplayedTransition> currentCandidateEntry : replayScenarios
						.entrySet()) {
					ReplayedTransition currentCandidate = currentCandidateEntry
							.getValue();
					ReplayedPetriNet currentScenario = currentCandidateEntry
							.getKey();
					// there is only one belonging transition in the net ->
					// findRandomTransition() can be used
					ReplayedTransition belongingTransition = (ReplayedTransition) currentScenario
							.findRandomTransition(le);
					List<Transition> enablingSequence = belongingTransition
							.getShortestSequenceOfInvisibleTasks(
									currentScenario, saveTroubles, this,
									atesPos);
					// for a transition which is already enabled,
					// the result sequence is of length 0 (but not null)
					if (enablingSequence != null) {
						belongingTransition.fireSequenceOfTasks(
								enablingSequence, currentScenario, null, null,
								new Measurer()); // produced and consumed tokens
						// etc. should not be
						// actually counted!
						belongingTransition.fireQuick();
						remainingScenarios.put(currentScenario,
								currentCandidate);
					}
				}
			}

			// n:1 mapping -> check for all scenarios whether one of the
			// belonging transitions is enabled
			else if (allAssociatedTasks.size() > 1) {
				for (Entry<ReplayedPetriNet, ReplayedTransition> currentCandidateEntry : replayScenarios
						.entrySet()) {
					ReplayedTransition currentCandidate = currentCandidateEntry
							.getValue();
					ReplayedPetriNet currentScenario = currentCandidateEntry
							.getKey();
					ArrayList<Transition> belongingTransitions = (ArrayList<Transition>) currentScenario
							.findTransitions(le);
					// copy audit trail with current iterator position (in order
					// to keep the actual replay status)
					ReplayedTransition chosenTransition = chooseEnabledDuplicateTask(
							belongingTransitions, currentScenario, atesPos,
							null, null);
					// if none of them is enabled, do not proceed with this
					// candidate
					if (chosenTransition != null) {
						List<Transition> enablingSequence = chosenTransition
								.getShortestSequenceOfInvisibleTasks(
										currentScenario, saveTroubles, this,
										atesPos);
						chosenTransition.fireSequenceOfTasks(enablingSequence,
								currentScenario, null, null, new Measurer()); // produced
						// and
						// consumed
						// tokens
						// etc.
						// should
						// not
						// be
						// actually
						// counted!
						chosenTransition.fireQuick();
						remainingScenarios.put(currentScenario,
								currentCandidate);
					}
				}
			}

			// just choose any of the remaining candidates
			if (remainingScenarios.size() == 0) {
				Entry<ReplayedPetriNet, ReplayedTransition> chosenEntry = replayScenarios
						.entrySet().iterator().next();
				ReplayedTransition transition = chosenEntry.getValue();
				ReplayedPetriNet scenario = chosenEntry.getKey();
				remainingScenarios.put(scenario, transition);
				return remainingScenarios;
			}
			// recursively continue the selection
			else if (remainingScenarios.size() > 1) {
				remainingScenarios = traceBestSequenceOfInvisibleTasks(
						remainingScenarios, atesPos);
			}
			// return the only candidate left
			return remainingScenarios;
		}
		// just choose any of the remaining candidates
		else {
			Entry<ReplayedPetriNet, ReplayedTransition> chosenEntry = replayScenarios
					.entrySet().iterator().next();
			ReplayedTransition transition = chosenEntry.getValue();
			ReplayedPetriNet scenario = chosenEntry.getKey();
			// the actual replay scenario is not needed anymore since the
			// decision is made
			remainingScenarios.put(scenario, transition);
			return remainingScenarios;
		}
	}

	// ///////////////////////////////// Other log replay methods
	// ///////////////////////////////////

	/**
	 * Enables the transition that has failed successful execution (since for
	 * non-blocking log replay it will be fired anyway).
	 * 
	 * @param pi
	 *            the process instance currently replayed
	 * @param t
	 *            the transition having failed successful execution
	 * @param ate
	 *            the audit trail entry having failed successful replay
	 */
	private void artificiallyEnableFailedTask(ReplayedLogTrace pi,
			ReplayedTransition t, AuditTrailEntry ate) {
		// inject measurement step
		myMeasurer.takeFailedTaskMeasurement(pi, t, ate);
		// for enabling, we need to check the number of edges between every
		// predecessor place and the
		// transition for determination of the amount of tokens needed to enable
		// it
		Iterator it = t.getInEdgesIterator();
		while (it.hasNext()) {
			ReplayedPlace p = (ReplayedPlace) ((ModelGraphEdge) it.next())
					.getSource();
			// add token to place as long as necessary and update missingTokens
			// map
			while (p.getNumberOfTokens() < myResult.replayedPetriNet
					.getEdgesBetween(p, t).size()) {
				// create missing token
				Token newToken = new Token();
				p.addToken(newToken);
				// inject measurement
				myMeasurer.takeMissingTokenMeasurement(p, pi);
			}
		}
		// inject measurement step
		myMeasurer.takeArtificiallyEnabledMeasurement(myResult, pi,
				saveTroubles);
	}

	/**
	 * Finishes evaluation of the given trace by potentially firing invisible
	 * transitions that lead to the final state.
	 * 
	 * @param pi
	 *            the log trace that was replayed last
	 * @return <code>true</code> if could be finished successfully,
	 *         <code>false</code> otherwise
	 */
	private void postTraceReplayAnalysis(ReplayedLogTrace pi) {
		// check first the final place for incoming invisible tasks
		Iterator finalPlaceChecker = myResult.replayedPetriNet.getPlaces()
				.iterator();
		while (finalPlaceChecker.hasNext()) {
			ReplayedPlace currentPlace = (ReplayedPlace) finalPlaceChecker
					.next();
			if (currentPlace.outDegree() == 0) {
				// temporarily add a fake final transition to fire all invisible
				// transitions leading to the final place
				Transition fakeTransition = new Transition("fakeTransition",
						myResult.replayedPetriNet);
				ReplayedTransition fakeDTransition = new ReplayedTransition(
						fakeTransition, myResult.replayedPetriNet,
						new ArrayList());
				myResult.replayedPetriNet.addTransition(fakeDTransition);
				// connect final place with fake transition
				myResult.replayedPetriNet.addEdge(new PNEdge(currentPlace,
						fakeDTransition));
				// only interested in the side effect of firing invisible
				// transitions, if possible
				fakeDTransition.isEnabled(pi, null, saveTroubles, myMeasurer,
						this, atesPos);
				// clean up again
				myResult.replayedPetriNet.delTransition(fakeDTransition);
			}
		}
		// inject measurement
		myMeasurer.takePostTraceReplayMeasurement(myResult, pi);
	}
}
