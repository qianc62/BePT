package org.processmining.mining.regionmining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.JPanel;

import cern.colt.matrix.DoubleMatrix2D;
import lpsolve.AbortListener;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Token;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.CancelationComponent;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.util.ProMLpSolve;
import org.processmining.framework.util.ProMLpSolveException;
import org.processmining.mining.MiningResult;
import org.processmining.mining.logabstraction.LogRelationBasedAlgorithm;
import org.processmining.mining.logabstraction.LogRelations;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class ParikhLanguageRegionMiner extends LogRelationBasedAlgorithm {

	private static int BOUND = 1;

	public String getName() {
		return "Parikh Language-based Region miner";
	}

	public String getHtmlDescription() {
		return "This plugin uses the theory of regions for mining, without building an intermediate transition system, "
				+ "i.e. region theory is used directly onto the log, assuming that the log is a natural language.";
	}

	public ParikhLanguageRegionMiner() {
	}

	// used by the analysisplugin version of this plugin. If set, the petri net
	// is extended with places.
	protected PetriNet petriNet = null;

	public MiningResult mine(LogReader log, final LogRelations relations,
			final Progress progress) {
		ui.updateOptions();
		LogEvents events = log.getLogSummary().getLogEvents();
		PetriNet net;
		Transition[] trans;
		if (petriNet == null) {
			net = new PetriNet();
			trans = new Transition[events.size()];
			for (int i = 0; i < events.size(); i++) {
				trans[i] = new Transition(events.get(i), net);
				trans[i].setNumber(i);
				net.addTransition(trans[i]);
			}
		} else {
			net = (PetriNet) petriNet.clone();
			// petriNet = null;
			trans = new Transition[events.size()];
			for (Transition t : net.getTransitions()) {
				t.setNumber(-2);
			}
			for (int i = 0; i < events.size(); i++) {
				trans[i] = net.findRandomTransition(events.get(i));

				trans[i].setNumber(i);

			}
		}
		PetriNetResult result = new PetriNetResult(log, net, this);
		int caus;
		{
			int c = 0;
			for (int t1 = 0; t1 < events.size(); t1++) {
				for (int t2 = 0; t2 < events.size(); t2++) {
					if (relations.getCausalFollowerMatrix().get(t1, t2) > 0) {
						c++;
					}
				}
				if (relations.getOneLengthLoopsInfo().get(t1) > 0) {
					c++;
				}
				if (relations.getStartInfo().get(t1) > 0) {
					c++;
				}
			}
			caus = c;
		}
		chunk = log.getLogSummary().getNumberOfAuditTrailEntries() / caus;

		// ****************************************************************************************
		// Setup-phase, the ILP base problem is set up.
		progress.setNote("Constructing constraints from log");
		progress.setMinMax(0, 2 * caus + 2 + net.getPlaces().size());
		progress.setProgress(0);

		int[] colSum_x = new int[events.size()];
		int[] colSum_y = new int[events.size()];

		ProMLpSolve problem = setUpProblem(log, progress, trans, relations
				.getCausalFollowerMatrix(), colSum_x, colSum_y);
		if (problem == null) {
			Message
					.add(
							"Initial set of constraints is too complex, i.e. no possible solution exists.",
							Message.ERROR);
			return null;
		}
		// ****************************************************************************************

		// ****************************************************************************************
		// Place removal phase. Each place violating the base ILP is removed
		// from the net.
		progress
				.setNote("Renaming and removing existing places violating base ILP");
		renameAndRemovePlaces(net, problem, ui.getOptions()
				.getRemoveExistingPlaces(), progress);
		Message.add("Retained " + net.getPlaces().size() + " places",
				Message.DEBUG);
		// ****************************************************************************************

		if (!ui.getOptions().getRestrictions().equals(
				ParikhLanguageRegionMinerOptions.NO_PLACES)) {
			// ****************************************************************************************
			// Causal dependencies not expressed by the reduced net are
			// collected and the ILP problem is
			TreeSet<int[]> stack;
			stack = getNotExpressedCausalDependencies(net, relations, progress,
					ui.getOptions().getRestrictToDisconnectedTrans(), trans);
			// ****************************************************************************************

			// ****************************************************************************************
			// Discovery phase. The ILP is solved many times and places might be
			// added in each iteration.
			findPlaces(progress, relations, events, result, net, trans,
					colSum_x, colSum_y, problem, stack);
			// ****************************************************************************************
		}

		// make clusters of transitions referring to the same activity
		net.makeClusters();

		// cleanup the base problem.
		problem.deleteLp();
		problem = null;
		return result;
	}

	/**
	 * removePlaces
	 * 
	 * @param net
	 *            PetriNet
	 * @param problem
	 *            ProMLpSolve
	 * @param progress
	 *            Progress
	 */
	private void renameAndRemovePlaces(PetriNet net, ProMLpSolve problem,
			boolean remove, Progress progress) {
		int size = net.getTransitions().size();
		for (int i = 0; i < net.getPlaces().size() && !progress.isCanceled(); i++) {
			progress.inc();
			Place p = net.getPlaces().get(i);
			double[] vector = new double[2 * size + 1];
			Arrays.fill(vector, 0);
			for (Transition in : (HashSet<Transition>) p.getPredecessors()) {
				vector[in.getNumber()] = 1;
			}
			for (Transition out : (HashSet<Transition>) p.getSuccessors()) {
				vector[size + out.getNumber()] = 1;
			}
			vector[vector.length - 1] = p.getNumberOfTokens();
			try {
				p.setIdentifier(Arrays.toString(vector));
				if (remove && !problem.isFeasible(vector)) {
					// remove place
					Message.add("Removed place: "
							+ p.getPredecessors().toString() + " -> "
							+ p.getSuccessors().toString(), Message.DEBUG);
					net.delPlace(p);
					i--;
				}
			} catch (ProMLpSolveException ex) {
				// LP problem.
				Message.add(ex.getMessage(), Message.ERROR);
				return;
			}
		}
	}

	private int chunk;

	private ProMLpSolve setUpProblem(LogReader log, Progress progress,
			Transition[] trans, DoubleMatrix2D causalDependencies,
			int[] colSum_x, int[] colSum_y) {

		final LogEvents events = log.getLogSummary().getLogEvents();

		final ProMLpSolve problem;

		Arrays.fill(colSum_x, 0);
		Arrays.fill(colSum_y, 0);

		try {
			// Give ProM time to show progress dialog
			Thread.currentThread().sleep(10);
		} catch (InterruptedException ex1) {
			// don't care
			Message.add("Progress probably not shown", Message.ERROR);
		}
		try {
			problem = new ProMLpSolve(0, 2 * events.size() + 1);

			problem.putAbortListener(new AbortListener() {
				public boolean abortfunc(LpSolve lpSolve, Object object)
						throws LpSolveException {
					return ((CancelationComponent) object).isCanceled();
				}
			}, progress);

		} catch (ProMLpSolveException ex2) {
			Message.add(ex2.getMessage(), Message.ERROR);
			return null;
		}

		for (int i = 0; i < events.size() && !progress.isCanceled(); i++) {

			try {
				problem.setColName(i + 1, "x_" + i);
				problem.setBinary(i + 1, ui.getOptions().getIntVars());
				problem.setLowbo(i + 1, 0);
				problem.setUpbo(i + 1, BOUND);

				problem.setColName(i + events.size() + 1, "y_" + i);
				problem.setBinary(i + events.size() + 1, ui.getOptions()
						.getIntVars());
				problem.setLowbo(i + events.size() + 1, 0);
				problem.setUpbo(i + events.size() + 1, BOUND);
			} catch (ProMLpSolveException ex) {
				Message.add(ex.getMessage(), Message.ERROR);
				return null;
			}

		}
		try {

			problem.setColName(problem.getNcolumns() - 1 + 1, "i");
			problem.setBinary(problem.getNcolumns() - 1 + 1, ui.getOptions()
					.getIntVars());
			problem.setLowbo(problem.getNcolumns() - 1 + 1, 0);
			problem.setUpbo(problem.getNcolumns() - 1 + 1, 1);
		} catch (ProMLpSolveException ex) {
			Message.add(ex.getMessage(), Message.ERROR);
			return null;
		}

		try {

			// init + A'.x - A.y >= 0
			TreeSet<int[]> completed = (ui.getOptions()
					.getEmptyAfterCompleteCase() ? new TreeSet<int[]>(
					intArrayComp) : null);
			TreeSet<int[]> eqs = getEquations(events, log, progress, chunk,
					colSum_x, colSum_y, completed, ui.getOptions()
							.getEmptyAfterCompleteCase());
			int[] target = getTargetFunction(colSum_x, colSum_y, 1, 1);

			problem.setAddRowmode(true);
			problem.addTarget(target);
			int bound = ui.getOptions().getSimulationBoundedness();
			Message.add("Adding constraints to enable firing of prefix",
					Message.DEBUG);
			if (bound > 0) {
				Message.add("Adding constraints to bound tokens during replay",
						Message.DEBUG);
			}
			for (int[] eq : eqs) {
				if (progress.isCanceled()) {
					problem.setAddRowmode(false);
					return problem;
				}
				// tokens remaining after executing this sequence
				problem.addConstraint(eq, ProMLpSolve.GE, 0);
				if (bound > 0) {
					for (int i = 0; i < events.size(); i++) {
						// after firing the last transition, there should not be
						// more than bound tokens
						eq[i] = -eq[i + events.size()];
					}
					problem.addConstraint(eq, ProMLpSolve.LE, bound);
				}
			}

			if (completed != null) {
				Message.add(
						"Adding constraints for empty net after complete case",
						Message.DEBUG);
				for (int[] eq : completed) {
					if (progress.isCanceled()) {
						problem.setAddRowmode(false);
						return problem;
					}
					// NO tokens remaining after executing this complete case
					problem.addConstraint(eq, ProMLpSolve.EQ, 0);
				}
			}
			if (ui.getOptions().getDenySelfLoops()) {
				Message.add("Adding constraints preventing self-loops",
						Message.DEBUG);
				int[] eq = new int[2 * events.size() + 1];
				Arrays.fill(eq, 0);
				for (int i = 0; i < events.size(); i++) {
					eq[i] = 1;
					eq[i + events.size()] = 1;
					// x_i + y_i <= 1 (no self loops)
					problem.addConstraint(eq, ProMLpSolve.LE, 1);
					eq[i] = 0;
					eq[i + events.size()] = 0;
				}
			}

			// Marked Graph implies that x.(1) <= 1 and y.(1) <= 1;
			String s_1 = "";
			String s_0 = "";
			for (int i = 0; i < events.size(); i++) {
				s_1 += "1 ";
				s_0 += "0 ";
			}
			int maxIn = ui.getOptions().getMaxInEdges();
			if (maxIn < events.size()) {
				Message.add(
						"Adding constraints limiting incoming edges per place",
						Message.DEBUG);
				// x.(1) <= maxIn (provided by user)
				problem
						.strAddConstraint(s_1 + s_0 + "0", ProMLpSolve.LE,
								maxIn);
			}
			int maxOut = ui.getOptions().getMaxOutEdges();
			if (maxOut < events.size()) {
				Message.add(
						"Adding constraints limiting outgoing edges per place",
						Message.DEBUG);
				// y.(1) <= maxOut (provided by user)
				problem.strAddConstraint(s_0 + s_1 + "0", ProMLpSolve.LE,
						maxOut);
			}
			int total = ui.getOptions().getMaxTotalEdges();
			if (total < maxIn + maxOut) {
				Message.add(
						"Adding constraints limiting total edges per place",
						Message.DEBUG);
				// y.(1) + x.(1) <= total (provided by user)
				problem
						.strAddConstraint(s_1 + s_1 + "0", ProMLpSolve.LE,
								total);
			}
			// x.(1) + y.(1) >= 1
			problem.strAddConstraint(s_1 + s_1 + "0", ProMLpSolve.GE, 1);

			if (ui.getOptions().getRestrictToCausalDependencies()) {
				Message
						.add(
								"Adding constraints to restrict places to causal dependencies",
								Message.DEBUG);
				// places can only express causal dependencies:
				for (int i = 0; i < events.size(); i++) {
					for (int j = 0; j < events.size(); j++) {
						if (progress.isCanceled()) {
							problem.setAddRowmode(false);
							return problem;
						}
						// causal dependencies not expressed yet
						// are recorded in the stack
						if (causalDependencies.get(i, j) > 0) {
							continue;
						}
						// this i to j should not be allowed
						String s = "";
						for (int k = 0; k < 2 * events.size() + 1; k++) {
							s += ((k == i || k == j + events.size()) ? "1 "
									: "0 ");
						}
						// the incoming arrows from i +
						// the outgoing to j should be <= 1 (i.e. only one of
						// them is present)
						problem.strAddConstraint(s, ProMLpSolve.LE, 1);
					}
				}
			}

			problem.setAddRowmode(false);
			problem.addTarget(getTargetFunction(colSum_x, colSum_y, 1, problem
					.getNrows() + 1));

		} catch (ProMLpSolveException ex) {
			Message.add(ex.getMessage(), Message.ERROR);
			return null;
		}

		if (progress.isCanceled()) {
			return null;
		}

		progress.setNote("Initializing solver. This might take some time.");

		// preSolve(problem);

		Message.add("Solver setup with " + problem.getNrows()
				+ " constraints on " + problem.getNcolumns() + " variables.");

		problem.setVerbose(ProMLpSolve.IMPORTANT);
		return problem;
	}

	/*
	 * PreSolving causes problems with the iterative nature of our algorithm.
	 * Each iteration adds bounds/constraints, while the presolve might have
	 * removed the corresponding variables.
	 * 
	 * private void preSolve(ProMLpSolve problem) {
	 * Message.add("Starting to simplify " + problem.getNrows() +
	 * " constraints on " + problem.getNcolumns() + " variables.");
	 * 
	 * problem.setPresolve( ProMLpSolve.PRESOLVE_ROWDOMINATE |
	 * ProMLpSolve.PRESOLVE_ROWS | ProMLpSolve.PRESOLVE_COLS |
	 * ProMLpSolve.PRESOLVE_LINDEP | ProMLpSolve.PRESOLVE_ELIMEQ2 |
	 * ProMLpSolve.PRESOLVE_MERGEROWS | ProMLpSolve.PRESOLVE_DUALS |
	 * ProMLpSolve.PRESOLVE_PROBEFIX | ProMLpSolve.PRESOLVE_PROBEREDUCE |
	 * ProMLpSolve.PRESOLVE_MERGEROWS | ProMLpSolve.PRESOLVE_IMPLIEDSLK |
	 * ProMLpSolve.PRESOLVE_REDUCEMIP, problem.getPresolveloops()); try { if
	 * (problem.isErrorSolveCode(problem.solve())) { // error found in presolve
	 * return; } } catch (ProMLpSolveException ex) {
	 * Message.add(ex.getMessage(), Message.ERROR); return; }
	 * 
	 * problem.setPresolve(ProMLpSolve.PRESOLVE_NONE,
	 * problem.getPresolveloops()); }
	 */

	private void findPlaces(Progress progress, LogRelations relations,
			LogEvents events, PetriNetResult result, PetriNet net,
			Transition[] trans, int[] colSum_x, int[] colSum_y,
			ProMLpSolve problem, TreeSet<int[]> stack) {

		progress.setNote("click cancel to continue with current net.");
		while (!stack.isEmpty()
				&& net.getPlaces().size() < ui.getOptions().getMaxPlaces()
				&& !progress.isCanceled()) {
			int[] dependency = stack.first();
			stack.remove(dependency);
			int t1 = dependency[0];
			int t2 = dependency[1];

			progress.inc();

			// if (foundSets.contains(new int[] {t1, t2})) {
			// continue;
			// // done already
			// }

			if ((t1 != INITIAL)) {
				// We look explicitly for a solution between t1 and t2, hence:
				TreeSet<int[]> bounds = new TreeSet(intArrayComp);
				int added = 0;
				try {
					// this is a causal dependency and not a start-event
					// situation

					progress.setNote("Place for relation: "
							+ (t1 == UNKNOWN ? "?" : events.getEvent(t1))
							+ " --> "
							+ (t2 == UNKNOWN ? "?" : events.getEvent(t2)));

					// init=0 (no initial tokens)
					problem.setBounds(2 * events.size() + 1, 0, 0);

					if (t1 != UNKNOWN) {
						if (Math.round(problem.getUpbo(t1 + 1)) < 1) {
							// there is no possible solution, since t1 cannot
							// have any more
							// outgoing arcs

							continue;
						}
					}

					if (t2 != UNKNOWN) {
						if (Math.round(problem.getUpbo(t2 + events.size() + 1)) < 1) {
							// there is no possible solution, since t2 cannot
							// have any more
							// incoming arcs
							continue;
						}
					}

					int[] constr = new int[2 * events.size() + 1];
					Arrays.fill(constr, 0);

					if (t1 != UNKNOWN) {
						// x(t1) > 0
						problem.setBounds(t1 + 1, 1, BOUND);
						constr[t1] = 1;
						problem.addConstraint(constr, ProMLpSolve.EQ, 1.0);
						constr[t1] = 0;
						added++;
					}

					if (t2 != UNKNOWN) {
						// y(t2) > 0
						problem.setBounds(t2 + events.size() + 1, 1, BOUND);
						constr[t2 + events.size()] = 1;
						problem.addConstraint(constr, ProMLpSolve.EQ, 1.0);
						constr[t2 + events.size()] = 0;
						added++;
					}

					constr[2 * events.size()] = 1;
					problem.addConstraint(constr, ProMLpSolve.EQ, 0);
					constr[2 * events.size()] = 0;
					added++;

					// System.out.println(trans[t1]+" -> "+trans[t2]);
					Place p = solve(events, net, trans, stack, relations,
							bounds, colSum_x, colSum_y, problem);

					if (showIntermediateUpdates() && p != null) {
						result.repaintNet();

					}

				} catch (ProMLpSolveException ex) {
					Message.add(ex.getMessage(), Message.ERROR);
				}
				try {
					// restore bounds
					for (int i = 0; i < added; i++) {
						problem.delConstraint(problem.getNrows());
					}

					problem.setBounds(2 * events.size() + 1, 0, 1);
					if (t1 != UNKNOWN) {
						problem.setBounds(t1 + 1, 0, BOUND);
					}
					if (t2 != UNKNOWN) {
						problem.setBounds(t2 + events.size() + 1, 0, BOUND);
					}
					problem.updateBounds(bounds);
				} catch (ProMLpSolveException ex) {
					Message.add(ex.getMessage(), Message.ERROR);
				}

			} else {
				t1 = t2;
				if (ui.getOptions().getSearchInitialMarking()
						&& relations.getStartInfo().get(t1) > 0) {
					// t1 is a start-event, hence
					progress.inc();
					progress.setNote("Place for inital event: "
							+ events.getEvent(t1));

					// x.(1) == 0
					String c = "";
					for (int i = 0; i < 2 * events.size() + 1; i++) {
						if (i < events.size()) {
							c += "1 ";
						} else {
							c += "0 ";
						}
					}
					TreeSet<int[]> bounds = new TreeSet(intArrayComp);
					try {
						problem.strAddConstraint(c, ProMLpSolve.EQ, 0);

						// y(t1) = 1 (output to t1)
						// cannot be more than 1, since there can only be 1
						// initial token
						if (Math.round(problem.getUpbo(t1 + events.size() + 1)) < 1) {
							// t1 cannot have any incoming arcs anymore.
							continue;
						}
						problem.setBounds(t1 + events.size() + 1, 1, 1);

						int[] constr = new int[2 * events.size() + 1];
						Arrays.fill(constr, 0);

						constr[t1 + events.size()] = 1;
						problem.addConstraint(constr, ProMLpSolve.EQ, 1.0);
						constr[t1 + events.size()] = 0;

						Place p = solve(events, net, trans, stack, relations,
								bounds, colSum_x, colSum_y, problem);
						if (showIntermediateUpdates() && p != null) {
							result.repaintNet();

						}

					} catch (ProMLpSolveException ex) {
						Message.add(ex.getMessage(), Message.ERROR);
					}
					try {
						// restore bounds
						problem.delConstraint(problem.getNrows());
						problem.delConstraint(problem.getNrows());

						problem.setBounds(t1 + events.size() + 1, 0, BOUND);

						problem.updateBounds(bounds);
					} catch (ProMLpSolveException ex) {
						Message.add(ex.getMessage(), Message.ERROR);
					}

				}
			}
		}
	}

	private final static int INITIAL = -1;
	private final static int UNKNOWN = -2;

	private TreeSet<int[]> getNotExpressedCausalDependencies(PetriNet net,
			LogRelations relations, CancelationComponent cancel,
			boolean restrictToDisconnectedTrans, Transition[] trans) {
		TreeSet<int[]> notDone = new TreeSet<int[]>(new IntArrayCausalComp(
				relations.getCausalFollowerMatrix()));

		for (int t1 = 0; t1 < relations.getCausalFollowerMatrix().rows()
				&& !cancel.isCanceled(); t1++) {
			if (trans[t1] == null) {
				continue;
			}
			for (int t2 = 0; t2 < relations.getCausalFollowerMatrix().columns()
					&& !cancel.isCanceled(); t2++) {
				if (trans[t2] == null) {
					continue;
				}
				// Only proceed if
				// the causal dependencies should be used, or
				// (t1 -> t2) or
				// ( t1 = t2 and t1 is a one-loop )
				if (ui.getOptions().getUseCausalDependencies()
						&& (!((relations.getCausalFollowerMatrix().get(t1, t2) > 0) || (t1 == t2 && relations
								.getOneLengthLoopsInfo().get(t1) > 0)))) {
					continue;
				}
				// If the begin transition has output or the end transition
				// has input, then check if this combination should be
				// considered
				// at all.
				int i = t1;
				int j = t2;
				if (restrictToDisconnectedTrans) {
					if (trans[t1].outDegree() > 0) {
						i = UNKNOWN;
					}
					if (trans[t2].inDegree() < 0) {
						j = UNKNOWN;
					}
					if ((i + 1) * (j + 1) > 0) {
						// i*j >0 implies that they are both UNKNOWN (-2 * -2 =
						// 4, or
						// that they are both not unknown
						continue;
					}
				}
				notDone.add(new int[] { i, j });
			}
			// for initial marking
			if (ui.getOptions().getUseCausalDependencies()
					&& relations.getStartInfo().get(t1) > 0) {
				notDone.add(new int[] { INITIAL, t1 });
			}
		}

		if (net == null || cancel.isCanceled()) {
			return notDone;
		}
		// Only connect to transitions without pre- and post-sets

		Iterator it = net.getPlaces().iterator();
		while (it.hasNext() && !cancel.isCanceled()) {
			Place p = (Place) it.next();
			for (Transition post : (HashSet<Transition>) p.getSuccessors()) {
				for (Transition pre : (HashSet<Transition>) p.getPredecessors()) {
					notDone.remove(new int[] { pre.getNumber(),
							post.getNumber() });
				}
				if (p.inDegree() == 0) {
					notDone.remove(new int[] { -1, post.getNumber() });
				}
			}
		}

		return notDone;
	}

	private Place solve(LogEvents events, PetriNet net, Transition[] trans,
			TreeSet<int[]> stack, LogRelations relations,
			TreeSet<int[]> bounds, int[] colSum_x, int[] colSum_y,
			ProMLpSolve problem) throws ProMLpSolveException {

		int code = problem.solve();
		if (!problem.isErrorSolveCode(code)) {
			double[] solution = problem.getColValuesSolution();
			String id = Arrays.toString(solution);
			if (net.findPlace(id) == null) {
				// The place is new.
				Place p = new Place(id, net);
				net.addPlace(p);
				for (int i = 0; i < events.size(); i++) {
					for (int x = 0; x < solution[i]; x++) {
						net.addEdge(trans[i], p);
					}
					for (int x = 0; x < solution[i + events.size()]; x++) {
						net.addEdge(p, trans[i]);
					}
				}
				for (int x = 0; x < solution[solution.length - 1]; x++) {
					p.addToken(new Token());
				}
				if (ui.getOptions().getRestrictions().equals(
						ParikhLanguageRegionMinerOptions.EXT_FREE_CHOICE)
						&& p.outDegree() > 1) {
					// for all outgoing transitions of this place,
					// there might be more incoming places.
					// However, for these places holds:
					// outDegree()==1, or getSuccessors() == p.getSuccessors(),
					// the latter of which is guaranteed by this procedure.

					for (Transition t1 : (HashSet<Transition>) p
							.getSuccessors()) {
						for (Place q : (HashSet<Place>) t1.getPredecessors()) {
							if (q.outDegree() < p.outDegree()) {
								// these places q can be removed, but the
								// causal dependencies it expresses need
								// to be checked again
								for (Transition t2 : (HashSet<Transition>) q
										.getPredecessors()) {
									// it expresses t2 -> t1
									// hence
									// foundSets.remove(new int[]
									// {t2.getNumber(), t1.getNumber()});
									if (!ui.getOptions()
											.getUseCausalDependencies()
											|| (relations
													.getCausalFollowerMatrix()
													.get(t2.getNumber(),
															t1.getNumber()) > 0)) {
										// a causal dependency between t2 and t1
										// was also
										// detected in the log
										stack.add(new int[] { t2.getNumber(),
												t1.getNumber() });
									}
								}
								net.delPlace(q);
							}
						}
					}

					// This is a place with multiple outgoing arcs.
					// from now on, the output's of this place should all be
					// there, or none of them is.
					for (int i = events.size(); i < 2 * events.size(); i++) {
						if (solution[i] == 0) {
							continue;
						}
						for (int j = i + 1; j < 2 * events.size(); j++) {
							if (solution[j] == 0) {
								continue;
							}
							// i and j are outputs.
							int[] constr = new int[2 * events.size() + 1];
							Arrays.fill(constr, 0);
							constr[i] = 1;
							constr[j] = -1;
							problem.addConstraint(constr, ProMLpSolve.EQ, 0);

							colSum_x[i - events.size()] += 1;
							colSum_y[j - events.size()] += -1;
						}
					}
					problem.addTarget(getTargetFunction(colSum_x, colSum_y, 1,
							problem.getNrows() + 1));

				}
				for (int i = 0; i < events.size(); i++) {
					if (solution[i] > 0) {
						for (int j = 0; j < events.size(); j++) {
							if (solution[j + events.size()] > 0) {
								// this place connects transition i to
								// transition j
								if (ui
										.getOptions()
										.getRestrictions()
										.equals(
												ParikhLanguageRegionMinerOptions.STATEMACHINE)) {
									// transition i has an outgoing arc,
									// transition j has an incoming arc,
									// hence from now on,
									// x_i == 0 and y_j == 0
									bounds.add(new int[] { i + 1, 0, 0 });
									bounds.add(new int[] {
											j + events.size() + 1, 0, 0 });
								}
								stack.remove(new int[] { i, j });
							}
						}
					}
				}
				return p;
			}

		}
		return null;
	}

	public static int[] getTargetFunction(int[] colSum_x, int[] colSum_y,
			int weight_i, int rows) {
		int[] target = new int[2 * colSum_x.length + 1];
		int k = 0;
		for (int i = 0; i < colSum_y.length; i++) {
			target[k++] = BOUND * colSum_y[i];
		}
		for (int i = 0; i < colSum_x.length; i++) {
			target[k++] = -BOUND * colSum_y[i];
		}
		// init counts for 1
		target[k] = weight_i * rows;
		return target;

	}

	public TreeSet<int[]> getEquations(LogEvents events, LogReader log,
			Progress progress, int chunk, int[] colSum_x, int[] colSum_y) {
		return getEquations(events, log, progress, chunk, colSum_x, colSum_y,
				null, false);
	}

	public TreeSet<int[]> getEquations(LogEvents events, LogReader log,
			Progress progress, int chunk, int[] colSum_x, int[] colSum_y,
			TreeSet<int[]> completeCases, boolean emptyAfterCaseCompletion) {

		HashMap<LogEvent, Integer> map = new HashMap<LogEvent, Integer>(events
				.size());
		for (int i = 0; i < events.size(); i++) {
			map.put(events.getEvent(i), new Integer(i));
		}

		TreeSet<int[]> result = new TreeSet<int[]>(intArrayComp);

		Iterator it = log.instanceIterator();
		int pref = 0;
		while (it.hasNext() && (progress == null || !progress.isCanceled())) {
			ProcessInstance pi = (ProcessInstance) it.next();
			AuditTrailEntryList ateList = pi.getAuditTrailEntryList();
			int[] row_x = new int[events.size()];
			int[] row_y = new int[events.size()];
			HashMap<String, AuditTrailEntry> id2node = new HashMap();
			boolean po = false;
			if (pi.getAttributes().containsKey(ProcessInstance.ATT_PI_PO)
					&& pi.getAttributes().get(ProcessInstance.ATT_PI_PO)
							.equals("true")) {
				// this is a partial order, hence fill the id2Node array
				// First increase the occurences of ates
				Iterator<AuditTrailEntry> ateIt = pi.getAuditTrailEntryList()
						.iterator();
				while (ateIt.hasNext()) {
					AuditTrailEntry ate = ateIt.next();
					id2node.put(ate.getAttributes().get(
							ProcessInstance.ATT_ATE_ID), ate);
				}
				po = true;
			}
			for (int i = 0; i < ateList.size(); i++) {
				if (progress != null && pref % chunk == 0) {
					progress.inc();
				}
				AuditTrailEntry ate = null;
				try {
					ate = ateList.get(i);
					id2node.put(ate.getAttributes().get(
							ProcessInstance.ATT_ATE_ID), ate);
				} catch (IOException ex) {
					Message.add("Error while reading Log: " + ex.getMessage(),
							Message.ERROR);
					return null;
				} catch (IndexOutOfBoundsException ex) {
					Message.add("Error while reading Log: " + ex.getMessage(),
							Message.ERROR);
					return null;
				}
				LogEvent thisEvent = new LogEvent(ate.getElement(), ate
						.getType());
				int thisIndex = map.get(thisEvent).intValue();
				if (!po) {
					// process instance pi is not a partial order.
					// simple, we update row_y
					row_y[thisIndex] += 1;
				} else {
					// process instance pi is a partial order.
					// we know now that all predecessors of this node have been
					// fired,
					// together with all previously fired ates.
					ArrayList<LogEvent> preEvents = new ArrayList();
					fillWithPreLogEvents(ate, preEvents, id2node);
					Arrays.fill(row_x, 0);
					Arrays.fill(row_y, 0);
					Iterator<LogEvent> it2 = preEvents.iterator();
					while (it2.hasNext()) {
						int index = map.get(it2.next()).intValue();
						row_y[index] += 1;
						row_x[index] += 1;
					}
					row_y[thisIndex] += 1;

				}
				// Build constraint
				int[] constraint = getConstraint(row_x, row_y, 1);

				if (result.add(constraint.clone())) {
					for (int j = 0; j < events.size(); j++) {
						colSum_x[j] += row_x[j];
						colSum_y[j] += row_y[j];
					}
				}

				row_x[thisIndex] += 1;

				if (emptyAfterCaseCompletion && i == ateList.size() - 1) {
					// full case, so the last element of the trace counts.
					constraint = getConstraint(row_x, row_y, 1);
					completeCases.add(constraint);
				}
				pref++;
			}

		}
		return result;
	}

	private void fillWithPreLogEvents(AuditTrailEntry ate,
			final ArrayList<LogEvent> preEvents,
			final HashMap<String, AuditTrailEntry> id2Node) {
		String preSet = ate.getAttributes().get(ProcessInstance.ATT_ATE_PRE);
		StringTokenizer tokenizer = new StringTokenizer(preSet, ",", false);
		while (tokenizer.hasMoreTokens()) {
			AuditTrailEntry preAte = id2Node.get(tokenizer.nextToken());
			LogEvent preEvent = new LogEvent(preAte.getElement(), preAte
					.getType());
			preEvents.add(preEvent);
			fillWithPreLogEvents(preAte, preEvents, id2Node);
		}
	}

	private static int[] getConstraint(int[] row_x, int[] row_y, int init) {
		int[] constraint = new int[row_x.length + row_y.length + 1];
		int k = 0;
		for (int j = 0; j < row_x.length; j++) {
			constraint[k++] = row_x[j];
		}
		for (int j = 0; j < row_y.length; j++) {
			constraint[k++] = -row_y[j];
		}
		// add initial marking to count for 1
		constraint[k] = init;
		return constraint;

	}

	static private final Comparator intArrayComp = new Comparator() {
		public int compare(Object o1, Object o2) {
			if (!(o1 instanceof int[])) {
				return 0;
			}
			if (!(o2 instanceof int[])) {
				return 0;
			}
			int[] v1 = (int[]) o1;
			int[] v2 = (int[]) o2;
			for (int i = 0; i < v1.length; i++) {
				if (v1[i] > v2[i]) {
					return 1;
				}
				if (v1[i] < v2[i]) {
					return -1;
				}
			}
			return 0;
		}

		public boolean equals(Object obj) {
			return false;
		}

	};

	private ParikhLanguageRegionMinerUI ui;

	public JPanel getOptionsPanel(LogSummary summary) {
		ui = new ParikhLanguageRegionMinerUI(summary, super
				.getOptionsPanel(summary), this);
		return ui;
	}

}

class IntArrayCausalComp implements Comparator {

	private DoubleMatrix2D m = null;

	public IntArrayCausalComp(DoubleMatrix2D m) {
		this.m = m;
	}

	public void setCausalMatrix(DoubleMatrix2D m) {
		this.m = m;
	}

	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof int[])) {
			return 0;
		}
		if (!(o2 instanceof int[])) {
			return 0;
		}
		int[] v1 = (int[]) o1;
		int[] v2 = (int[]) o2;
		for (int i = 0; i < v1.length; i++) {
			if (v1[i] > v2[i]) {
				return 1;
			}
			if (v1[i] < v2[i]) {
				return -1;
			}
		}
		// so far, they are equal
		if (v1[0] < 0 || v1[1] < 0) {
			// these are the largest
			return 0;
		}
		if (m.get(v1[0], v1[1]) > m.get(v2[0], v2[1])) {
			return 1;
		} else if (m.get(v1[0], v1[1]) < m.get(v2[0], v2[1])) {
			return -1;
		}
		return 0;
	}

	public boolean equals(Object obj) {
		return false;
	}

};
