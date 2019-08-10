package org.processmining.analysis.differences.processdifferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.processmining.analysis.differences.relations.*;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;

public class ProcessDifferences {

	private ProcessAutomaton ptnetsem1;
	private ProcessAutomaton ptnetsem2;

	private Relation<ModelGraphVertex, ModelGraphVertex> r;

	private Map<Set<ModelGraphVertex>, String> nameForEqClassIn1;
	private Map<Set<ModelGraphVertex>, String> nameForEqClassIn2;

	public ProcessDifferences(ModelGraph ptnet1, ModelGraph ptnet2,
			Relation<ModelGraphVertex, ModelGraphVertex> r) {
		ptnetsem1 = new ProcessAutomaton(ptnet1);
		ptnetsem2 = new ProcessAutomaton(ptnet2);

		this.r = r;
		this.r.clos();

		nameForEqClassIn1 = new HashMap<Set<ModelGraphVertex>, String>();
		nameForEqClassIn2 = new HashMap<Set<ModelGraphVertex>, String>();

		for (Iterator<ModelGraphVertex> i = ptnetsem1.visibleTasks().iterator(); i
				.hasNext();) {
			ModelGraphVertex t = i.next();
			if (r.getDom().contains(t)) {
				Set<ModelGraphVertex> eqClassOfT = r.eqClass(t);
				nameForEqClassIn1.put(eqClassOfT, t.getIdentifier());// changed
				// to
				// getIdentifier
				// Marian
				// //TODO:
				// This
				// should
				// not
				// just
				// be
				// this
				// transition's
				// name,
				// but
				// also
				// that
				// of
				// other
				// transitions
				// in
				// the
				// equivalence
				// class
			}
		}
		for (Iterator<ModelGraphVertex> i = ptnetsem2.visibleTasks().iterator(); i
				.hasNext();) {
			ModelGraphVertex t = i.next();
			if (r.getDom().contains(t)) {
				Set<ModelGraphVertex> eqClassOfT = r.eqClass(t);
				nameForEqClassIn2.put(eqClassOfT, t.getIdentifier());// changed
				// to
				// getIdentifier
				// Marian
				// //TODO:
				// This
				// should
				// not
				// just
				// be
				// this
				// transition's
				// name,
				// but
				// also
				// that
				// of
				// other
				// transitions
				// in
				// the
				// equivalence
				// class
			}
		}
	}

	/**
	 * Gives meaningful names to equivalence classes. This is achieved by
	 * replacing each equivalence class by a task from the original process.
	 */
	private Set<ModelGraphVertex> useNamingScheme(
			Set<Set<ModelGraphVertex>> inputdeps, int net) {
		Set<ModelGraphVertex> result = new HashSet<ModelGraphVertex>();

		for (Iterator<Set<ModelGraphVertex>> i = inputdeps.iterator(); i
				.hasNext();) {
			result.addAll(i.next());
		}
		result.removeAll((net == 1) ? ptnetsem2.visibleTasks() : ptnetsem1
				.visibleTasks());
		return result;
	}

	private List<String> useNamingScheme(List<Set<ModelGraphVertex>> inputdeps,
			int net) {
		List<String> result = new LinkedList<String>();

		Map<Set<ModelGraphVertex>, String> namingScheme = (net == 1) ? nameForEqClassIn1
				: nameForEqClassIn2;

		for (Iterator<Set<ModelGraphVertex>> i = inputdeps.listIterator(); i
				.hasNext();) {
			result.add(namingScheme.get(i.next()));
		}
		return result;
	}

	private ProcessAutomaton useNamingScheme(ProcessAutomaton ptnetsem, int net) {
		Map<Set<ModelGraphVertex>, String> namingScheme = (net == 1) ? nameForEqClassIn1
				: nameForEqClassIn2;
		for (Iterator<Entry<Set<ModelGraphVertex>, String>> i = namingScheme
				.entrySet().iterator(); i.hasNext();) {
			Entry<Set<ModelGraphVertex>, String> e = i.next();
			Set<ModelGraphVertex> eqClass = e.getKey();
			String name = e.getValue();
			ptnetsem.replace(eqClass, name);
		}
		return ptnetsem;
	}

	private Set<List<Set<ModelGraphVertex>>> normalizeCycles(
			Set<List<Object>> cycles) {
		Set<List<Set<ModelGraphVertex>>> result = new HashSet<List<Set<ModelGraphVertex>>>();

		for (Iterator<List<Object>> i = cycles.iterator(); i.hasNext();) {
			List<Object> cycle = i.next();
			List<Set<ModelGraphVertex>> normalizedCycle = new LinkedList<Set<ModelGraphVertex>>();
			for (Iterator<Object> j = cycle.listIterator(); j.hasNext();) {
				Object tInCycle = j.next();
				if ((tInCycle != null) && (tInCycle instanceof Set)) {
					normalizedCycle.add((Set<ModelGraphVertex>) tInCycle);
				}
			}
			result.add(normalizedCycle);
		}
		return result;
	}

	private boolean cycleContainedIn(List<Set<ModelGraphVertex>> cycle,
			Set<List<Set<ModelGraphVertex>>> cycles) {
		if (cycles.contains(cycle)) {
			return true;
		}

		List<Set<ModelGraphVertex>> c = new LinkedList<Set<ModelGraphVertex>>();
		c.addAll(cycle);
		for (int i = 1; i < c.size(); i++) {
			// rotate cycle
			Set<ModelGraphVertex> firstElt = c.remove(0);
			c.add(firstElt);
			if (cycles.contains(c)) {
				return true;
			}
		}
		return false;
	}

	private Set<Set<ModelGraphVertex>> mapEqClass(Set<ModelGraphVertex> sv) {
		Set<Set<ModelGraphVertex>> result = new HashSet<Set<ModelGraphVertex>>();

		for (Iterator<ModelGraphVertex> i = sv.iterator(); i.hasNext();) {
			result.add(r.eqClass(i.next()));
		}

		return result;
	}

	public Set<ProcessDifference> proceduralDifferences() {
		// These two lines will make the algorithm perform (much) better.
		// However, they are not part of the official theory
		ProcessAutomaton ptnetsem1temp = new ProcessAutomaton(this.ptnetsem1
				.getDFA().getNFA(), this.ptnetsem1.getSymbol2Object(),
				this.ptnetsem1.getObject2Symbol(), this.ptnetsem1.getProcess());
		ProcessAutomaton ptnetsem2temp = new ProcessAutomaton(this.ptnetsem2
				.getDFA().getNFA(), this.ptnetsem2.getSymbol2Object(),
				this.ptnetsem2.getObject2Symbol(), this.ptnetsem2.getProcess());
		;

		Set<ProcessDifference> result = new HashSet<ProcessDifference>();

		// 'skipped activities' in behavior 1
		Set<ModelGraphVertex> skippedTasksIn1 = new HashSet<ModelGraphVertex>(
				ptnetsem1temp.visibleTasks());
		skippedTasksIn1.removeAll(r.getDom());
		for (Iterator<ModelGraphVertex> i = skippedTasksIn1.iterator(); i
				.hasNext();) {
			ModelGraphVertex skippedT = i.next();
			Set<ModelGraphVertex> ts = new HashSet<ModelGraphVertex>();
			ts.add(skippedT);
			result.add(new ProcessDifference(ts, null, null, null, null, null,
					ProcessDifference.TYPE_SKIPPED_ACTIVITY,
					ProcessDifference.DIRECTION_PROVMORE));
		}
		// 'skipped activities' in behavior 2
		Set<ModelGraphVertex> skippedTasksIn2 = new HashSet<ModelGraphVertex>(
				ptnetsem2temp.visibleTasks());
		skippedTasksIn2.removeAll(r.getDom());
		for (Iterator<ModelGraphVertex> i = skippedTasksIn2.iterator(); i
				.hasNext();) {
			ModelGraphVertex skippedT = i.next();
			Set<ModelGraphVertex> ts = new HashSet<ModelGraphVertex>();
			ts.add(skippedT);
			result.add(new ProcessDifference(null, ts, null, null, null, null,
					ProcessDifference.TYPE_SKIPPED_ACTIVITY,
					ProcessDifference.DIRECTION_REQMORE));
		}

		// DFA for process 1 in which all tasks are replaced by their
		// equivalence class.
		// This DFA is used to compute cycles in the process.
		ProcessAutomaton dfaEq1 = new ProcessAutomaton(ptnetsem1temp);
		dfaEq1.replaceObjectsByEquivalenceClass((Relation) r);
		dfaEq1 = new ProcessAutomaton(dfaEq1.getDFA().getNFA(), dfaEq1
				.getSymbol2Object(), dfaEq1.getObject2Symbol(), dfaEq1
				.getProcess());
		// Compute a similar DFA for process 2.
		ProcessAutomaton dfaEq2 = new ProcessAutomaton(ptnetsem2temp);
		dfaEq2.replaceObjectsByEquivalenceClass((Relation) r);
		dfaEq2 = new ProcessAutomaton(dfaEq2.getDFA().getNFA(), dfaEq2
				.getSymbol2Object(), dfaEq2.getObject2Symbol(), dfaEq2
				.getProcess());

		Set<List<Set<ModelGraphVertex>>> cyclesFound = new HashSet<List<Set<ModelGraphVertex>>>();
		// The approach deals with all equivalents of an activity at once, so
		// keep track of the activities and equivalent that have been processed
		Set<ModelGraphVertex> dealtWith = new HashSet<ModelGraphVertex>();
		// For each non-skipped activity
		for (Iterator<ModelGraphVertex> i = r.getDom().iterator(); i.hasNext();) {
			ModelGraphVertex a = i.next();

			if (!dealtWith.contains(a)) {
				Set<ModelGraphVertex> eqClassOfA = r.eqClass(a);
				dealtWith.addAll(eqClassOfA);

				Set<ModelGraphVertex> equivalentsOfAIn1 = r.eqClass(a);
				equivalentsOfAIn1.retainAll(ptnetsem1temp.visibleTasks());

				Set<ModelGraphVertex> equivalentsOfAIn2 = r.eqClass(a);
				equivalentsOfAIn2.retainAll(ptnetsem2temp.visibleTasks());

				// Compute the input dependencies for a in 1 (definition 13 in
				// 2007 BETA WP)
				Set<Set<ModelGraphVertex>> inPaEq1 = new HashSet<Set<ModelGraphVertex>>();
				for (ModelGraphVertex aIn1 : equivalentsOfAIn1) {
					Set<ModelGraphVertex> inPa1 = ptnetsem1temp.inFor(aIn1,
							skippedTasksIn1); // in_P(a)
					inPaEq1.addAll(mapEqClass(inPa1)); // [in_P(a)]~
				}
				ProcessAutomaton aEq1 = new ProcessAutomaton(ptnetsem1temp);
				aEq1.replaceObjectsByEquivalenceClass((Relation) r); // [Tr(P)]~
				aEq1.restrictTo((Set) inPaEq1); // [Tr(P)]~ / [in_P(a)]~
				Set<Set<ModelGraphVertex>> inputDependenciesOfAIn1 = (Set) aEq1
						.symbolsPreceding(eqClassOfA); // x such that \sigma x
				// [a]~ sigma' \in
				// [Tr(P)]~ / [in_P(a)]~

				// Compute the input dependencies for a in 2 (definition 13 in
				// 2007 BETA WP)
				Set<Set<ModelGraphVertex>> inPaEq2 = new HashSet<Set<ModelGraphVertex>>();
				for (ModelGraphVertex aIn2 : equivalentsOfAIn2) {
					Set<ModelGraphVertex> inPa2 = ptnetsem2temp.inFor(aIn2,
							skippedTasksIn2); // in_Q(a)
					inPaEq2.addAll(mapEqClass(inPa2)); // [in_Q(a)]~
				}
				ProcessAutomaton aEq2 = new ProcessAutomaton(ptnetsem2temp);
				aEq2.replaceObjectsByEquivalenceClass((Relation) r); // [Tr(Q)]~
				aEq2.restrictTo((Set) inPaEq2); // [Tr(Q)]~ / [in_Q(a)]~
				Set<Set<ModelGraphVertex>> inputDependenciesOfAIn2 = (Set) aEq2
						.symbolsPreceding(eqClassOfA); // x such that \sigma x
				// [a]~ sigma' \in
				// [Tr(Q)]~ / [in_Q(a)]~

				// Compute differences concerning input dependencies
				boolean eqInputDeps = false;
				Set<Set<ModelGraphVertex>> id1CapId2 = new HashSet<Set<ModelGraphVertex>>();
				id1CapId2.addAll(inputDependenciesOfAIn1);
				id1CapId2.retainAll(inputDependenciesOfAIn2);
				if (inputDependenciesOfAIn1
						.containsAll(inputDependenciesOfAIn2)) {
					if (inputDependenciesOfAIn2
							.containsAll(inputDependenciesOfAIn1)) {
						// xs = ys (because (ys \subseteq xs) \land (xs
						// \subseteq ys))
						eqInputDeps = true;
					} else {
						// ys \subset xs (because (ys \subseteq xs) \land \lnot
						// (xs \subseteq ys))
						inputDependenciesOfAIn1
								.removeAll(inputDependenciesOfAIn2);
						result.add(new ProcessDifference(equivalentsOfAIn1,
								equivalentsOfAIn2, useNamingScheme(
										inputDependenciesOfAIn1, 1), null,
								null, null,
								ProcessDifference.TYPE_ADD_DEPENDENCIES,
								ProcessDifference.DIRECTION_PROVMORE));
					}
				} else if (inputDependenciesOfAIn2
						.containsAll(inputDependenciesOfAIn1)) {
					// xs \subset ys (because \lnot (ys \subseteq xs) \land (xs
					// \subseteq ys))
					inputDependenciesOfAIn2.removeAll(inputDependenciesOfAIn1);
					result.add(new ProcessDifference(equivalentsOfAIn1,
							equivalentsOfAIn2, null, useNamingScheme(
									inputDependenciesOfAIn2, 2), null, null,
							ProcessDifference.TYPE_ADD_DEPENDENCIES,
							ProcessDifference.DIRECTION_REQMORE));
				} else if (id1CapId2.isEmpty()) {
					// xs \cap ys = \emptyset
					result.add(new ProcessDifference(equivalentsOfAIn1,
							equivalentsOfAIn2, null, null, null, null,
							ProcessDifference.TYPE_DIFF_MOMENTS,
							ProcessDifference.DIRECTION_UNI));
				} else {
					// xs \neq ys (because \lnot (ys \subseteq xs) \land \lnot
					// (xs \subseteq ys))
					Set<Set<ModelGraphVertex>> temp = new HashSet<Set<ModelGraphVertex>>();
					temp.addAll(inputDependenciesOfAIn2);
					inputDependenciesOfAIn2.removeAll(inputDependenciesOfAIn1);
					inputDependenciesOfAIn1.removeAll(temp);
					result.add(new ProcessDifference(equivalentsOfAIn1,
							equivalentsOfAIn2, useNamingScheme(
									inputDependenciesOfAIn1, 1),
							useNamingScheme(inputDependenciesOfAIn2, 2), null,
							null, ProcessDifference.TYPE_DIFF_DEPENDENCIES,
							ProcessDifference.DIRECTION_UNI));
				}

				boolean inAdditionalCycle = false;
				Set<List<Set<ModelGraphVertex>>> cyclesIn1 = normalizeCycles(dfaEq1
						.cyclesContaining(eqClassOfA));
				Set<List<Set<ModelGraphVertex>>> cyclesIn2 = normalizeCycles(dfaEq2
						.cyclesContaining(eqClassOfA));
				// Compute differences concerning cycles
				if (!cyclesIn1.equals(cyclesIn2)) {
					inAdditionalCycle = true;

					Set<List<Set<ModelGraphVertex>>> additionalCyclesIn1 = new HashSet<List<Set<ModelGraphVertex>>>();
					additionalCyclesIn1.addAll(cyclesIn1);
					additionalCyclesIn1.removeAll(cyclesIn2);
					Set<List<Set<ModelGraphVertex>>> additionalCyclesIn2 = cyclesIn2;
					additionalCyclesIn2.removeAll(cyclesIn1);

					for (Iterator<List<Set<ModelGraphVertex>>> cycleI = additionalCyclesIn1
							.iterator(); cycleI.hasNext();) {
						List<Set<ModelGraphVertex>> cycle = cycleI.next();
						if (!cycleContainedIn(cycle, cyclesFound)) {
							cyclesFound.add(cycle);
							Set<Set<ModelGraphVertex>> cycleAsSet = new HashSet<Set<ModelGraphVertex>>(
									cycle);
							result.add(new ProcessDifference(useNamingScheme(
									cycleAsSet, 1), null, null, null,
									useNamingScheme(cycle, 1).toString(), null,
									ProcessDifference.TYPE_ITERATIVE,
									ProcessDifference.DIRECTION_PROVMORE));
						}
					}
					for (Iterator<List<Set<ModelGraphVertex>>> cycleI = additionalCyclesIn2
							.iterator(); cycleI.hasNext();) {
						List<Set<ModelGraphVertex>> cycle = cycleI.next();
						if (!cycleContainedIn(cycle, cyclesFound)) {
							cyclesFound.add(cycle);
							Set<Set<ModelGraphVertex>> cycleAsSet = new HashSet<Set<ModelGraphVertex>>(
									cycle);
							result.add(new ProcessDifference(null,
									useNamingScheme(cycleAsSet, 2), null, null,
									null, useNamingScheme(cycle, 2).toString(),
									ProcessDifference.TYPE_ITERATIVE,
									ProcessDifference.DIRECTION_REQMORE));
						}
					}
				}

				// Compute Differences concerning conditions
				if (eqInputDeps && !inAdditionalCycle) {
					ProcessAutomaton inputConditionOfTIn1 = aEq1; // [Tr(P)]~ /
					// [in_P(a)]~
					ProcessAutomaton inputConditionOfTIn2 = aEq2; // [Tr(Q)]~ /
					// [in_Q(a)]~

					boolean acceptsTin1 = inputConditionOfTIn1
							.acceptsTraceWithSingleStepIn(eqClassOfA);
					boolean acceptsTin2 = inputConditionOfTIn2
							.acceptsTraceWithSingleStepIn(eqClassOfA);

					ProcessAutomaton xsC = ProcessAutomaton
							.complement(inputConditionOfTIn1);
					ProcessAutomaton ysC = ProcessAutomaton
							.complement(inputConditionOfTIn2);
					ProcessAutomaton xsMinYs = ProcessAutomaton
							.complement(ProcessAutomaton.or(xsC,
									inputConditionOfTIn2));
					ProcessAutomaton ysMinXs = ProcessAutomaton
							.complement(ProcessAutomaton.or(
									inputConditionOfTIn1, ysC));

					if (acceptsTin1 && !acceptsTin2) {
						result.add(new ProcessDifference(equivalentsOfAIn1,
								equivalentsOfAIn2, null, null, null, null,
								ProcessDifference.TYPE_DIFF_START,
								ProcessDifference.DIRECTION_PROVMORE));
					} else if (!acceptsTin1 && acceptsTin2) {
						result.add(new ProcessDifference(equivalentsOfAIn1,
								equivalentsOfAIn2, null, null, null, null,
								ProcessDifference.TYPE_DIFF_START,
								ProcessDifference.DIRECTION_REQMORE));
					}

					if (xsMinYs.isEmptyLanguage()) {
						if (ysMinXs.isEmptyLanguage()) {
							// xs = ys
						} else {
							// (xs \subseteq ys) \land \lnot (ys \subseteq xs)
							result.add(new ProcessDifference(equivalentsOfAIn1,
									equivalentsOfAIn2, null, null, null,
									useNamingScheme(ysMinXs, 2).deriveRegExp(),
									ProcessDifference.TYPE_ADD_CONDITIONS,
									ProcessDifference.DIRECTION_REQMORE));
						}
					} else if (ysMinXs.isEmptyLanguage()) {
						// \lnot (xs \subseteq ys) \land (ys \subseteq xs)
						result.add(new ProcessDifference(equivalentsOfAIn1,
								equivalentsOfAIn2, null, null, useNamingScheme(
										xsMinYs, 1).deriveRegExp(), null,
								ProcessDifference.TYPE_ADD_CONDITIONS,
								ProcessDifference.DIRECTION_PROVMORE));
					} else {
						// \lnot (xs \subseteq ys) \land \lnot (ys \subseteq xs)
						result.add(new ProcessDifference(equivalentsOfAIn1,
								equivalentsOfAIn2, null, null, useNamingScheme(
										xsMinYs, 1).deriveRegExp(),
								useNamingScheme(ysMinXs, 2).deriveRegExp(),
								ProcessDifference.TYPE_DIFF_CONDITIONS,
								ProcessDifference.DIRECTION_UNI));
					}
				}
			}
		}

		return result;
	}
}
