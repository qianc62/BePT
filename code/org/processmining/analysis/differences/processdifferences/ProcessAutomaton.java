package org.processmining.analysis.differences.processdifferences;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.algorithms.EPCToPetriNetConverter;
import org.processmining.framework.models.fsm.FSMState;
import org.processmining.framework.models.fsm.FSMTransition;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.Marking;
import org.processmining.framework.models.petrinet.algorithms.InitialPlaceMarker;
import org.processmining.framework.models.petrinet.algorithms.ReachabilityGraphBuilder;
import org.processmining.analysis.differences.fa.*;
import org.processmining.analysis.differences.relations.*;
import org.processmining.converting.PetriNetReduction;

import att.grappa.Edge;

/**
 * This class defines the abstract (and concrete) business process notation for
 * which we can compute differences (definitions 1, 2 and 3 from 2007 BETA WP.)
 * To make the abstract notation suitable for another concrete notation, adapt
 * the methods: - inFor - visibleTasks - constructAutomaton - acceptedModelType
 * 
 * @author Remco Dijkman
 * 
 */
public class ProcessAutomaton extends NFA {
	protected Map<Integer, Object> symbol2Object;
	protected Map<Object, Integer> object2Symbol;

	protected ModelGraph process; // the process that this is the automaton for

	public ProcessAutomaton(ProcessAutomaton ptnetsem) {
		this((NFA) ptnetsem, ptnetsem.getSymbol2Object(), ptnetsem
				.getObject2Symbol(), ptnetsem.getProcess());
	}

	public ProcessAutomaton(NFA nfa, Map<Integer, Object> symbol2Object,
			Map<Object, Integer> object2Symbol, ModelGraph n) {
		super(nfa);

		this.symbol2Object = new HashMap<Integer, Object>();
		this.symbol2Object.putAll(symbol2Object);
		this.object2Symbol = new HashMap<Object, Integer>();
		this.object2Symbol.putAll(object2Symbol);
		setProcess(n);
	}

	public ProcessAutomaton(ModelGraph n) {
		super(null, 0);

		process = n;

		setAlphabet();

		constructAutomaton();
	}

	public ModelGraph getProcess() {
		return process;
	}

	public void setProcess(ModelGraph process) {
		this.process = process;
	}

	/**
	 * Replaces each occurrence of an object by another object Also removes the
	 * object from the alphabet (and adds the other object if necessary)
	 * 
	 * @param o
	 *            object to replace
	 * @param replacement
	 *            object to replace by
	 */
	public void replace(Object o, Object replacement) {
		/*
		 * If object2Symbol(replacement) already exists in the NFA then
		 * object2Symbol(o) must be replaced by object2Symbol(replacement) in
		 * the NFA (such that o is treated as equivalent to replacement by the
		 * NFA) and o must be removed from object2symbol and symbol2object If
		 * object2Symbol(replacement) does not exist in the NFA then o must be
		 * replaced by replacement in object2symbol and symbol2object
		 */
		Integer oSymbol = object2Symbol.get(o);
		Integer replacementSymbol = object2Symbol.get(replacement);
		if (oSymbol != null) {
			if (replacementSymbol != null) {
				replaceSymbol(oSymbol, replacementSymbol);
				object2Symbol.remove(o);
				symbol2Object.remove(oSymbol);
			} else {
				object2Symbol.remove(o);
				object2Symbol.put(replacement, oSymbol);
				symbol2Object.put(oSymbol, replacement);
			}
		}
	}

	/**
	 * Replaces each occurrence of an object by an occurrence of its equivalence
	 * class in the given relation.
	 * 
	 * @param r
	 *            relation to obtain equivalence classes from.
	 */
	public void replaceObjectsByEquivalenceClass(Relation<Object, Object> r) {
		Set<Object> objectsToReplace = new HashSet<Object>(object2Symbol
				.keySet());
		objectsToReplace.retainAll(r.getDom());

		for (Iterator<Object> i = objectsToReplace.iterator(); i.hasNext();) {
			Object o = i.next();
			replace(o, r.eqClass(o));
		}
	}

	/**
	 * Abstract from all occurrences of objects that are NOT in the given set.
	 * 
	 * @param s
	 *            the set of objects to retain; all other objects are abstracted
	 *            from.
	 */
	public void restrictTo(Set<Object> s) {
		Set<Object> objectsToAbstractFrom = new HashSet<Object>(object2Symbol
				.keySet());
		objectsToAbstractFrom.removeAll(s);
		abstractFromAll(objectsToAbstractFrom);
	}

	/**
	 * Abstracts from each occurrence of an object by labeling it silent Also
	 * removes the object from the alphabet
	 * 
	 * @param o
	 *            object to abstract from
	 */
	public void abstractFrom(Object o) {
		Integer oSymbol = object2Symbol.get(o);
		if ((oSymbol != null) && (oSymbol != NFA.EPSILON)) {
			replaceSymbol(oSymbol, NFA.EPSILON);
			object2Symbol.remove(o);
			symbol2Object.remove(oSymbol);
		}
	}

	/**
	 * Abstracts from each occurrence of an object from the argument set by
	 * labeling it silent. Also removes the objects from the alphabet.
	 * 
	 * @param os
	 *            the objects to abstract from
	 */
	public void abstractFromAll(Set<Object> os) {
		for (Iterator<Object> i = os.iterator(); i.hasNext();) {
			abstractFrom(i.next());
		}
	}

	/**
	 * Returns the automaton that accepts both the set of traces accepted by pt1
	 * and the set of traces accepted by pt2. Warning - postcondition:
	 * getProcess()==null.
	 * 
	 * @param ptnet1
	 *            automaton 1 to compute the disjunction of
	 * @param ptnet2
	 *            automaton 2 to compute the disjunction of
	 * @return the disjunction of ptnet1 and ptnet2
	 */
	public static ProcessAutomaton or(ProcessAutomaton ptnet1,
			ProcessAutomaton ptnet2) {
		ProcessAutomaton pt1 = new ProcessAutomaton(ptnet1); // TODO: This copy
		// should not be
		// necessary,
		// but somewhere
		// the procedure
		// below has
		// undesirable
		// side-effects.
		ProcessAutomaton pt2 = new ProcessAutomaton(ptnet2); // TODO: This copy
		// should not be
		// necessary,
		// but somewhere
		// the procedure
		// below has
		// undesirable
		// side-effects.

		int maxSymbol = 0;
		Map<Object, Integer> jointObject2Symbol = new HashMap<Object, Integer>();
		Map<Integer, Object> jointSymbol2Object = new HashMap<Integer, Object>();
		Set<Integer> jointAlphabet = new HashSet<Integer>();

		// Initially just copy all info from pt1
		for (Iterator<Entry<Integer, Object>> i = pt1.getSymbol2Object()
				.entrySet().iterator(); i.hasNext();) {
			Entry<Integer, Object> ent = i.next();
			int sym = ent.getKey();
			Object obj = ent.getValue();
			jointAlphabet.add(sym);
			jointObject2Symbol.put(obj, sym);
			jointSymbol2Object.put(sym, obj);
			if (sym > maxSymbol) {
				maxSymbol = sym;
			}
		}

		// Replace all symbols in pt2 by symbols that do not exist in pt1 nor in
		// pt2
		int pt2AlphabetSize = pt2.getAlphabetSize();
		int[] newPt2Alphabet = new int[pt2AlphabetSize];
		int[] oldPt2Alphabet = pt2.getAlphabet();
		for (int i = 0; i < pt2AlphabetSize; i++) {
			if (oldPt2Alphabet[i] > maxSymbol) {
				maxSymbol = oldPt2Alphabet[i];
			}
		}
		for (int i = 0; i < pt2AlphabetSize; i++) {
			int oldSymbol = oldPt2Alphabet[i];
			int newSymbol = oldSymbol + maxSymbol;
			newPt2Alphabet[i] = newSymbol;
			Object obj = pt2.getSymbol2Object().get(oldSymbol);
			pt2.getSymbol2Object().remove(oldSymbol);
			pt2.getSymbol2Object().put(newSymbol, obj);
			pt2.getObject2Symbol().put(obj, newSymbol);
			pt2.replaceSymbol(oldSymbol, newSymbol);
		}

		// For all objects in pt2
		for (Iterator<Entry<Integer, Object>> i = pt2.getSymbol2Object()
				.entrySet().iterator(); i.hasNext();) {
			Entry<Integer, Object> ent = i.next();
			int sym = ent.getKey();
			Object obj = ent.getValue();
			// if the object exists in pt1
			// then replace the symbol in pt2 with the symbol in pt1
			// else add je sym,obj combination to the joint alphabet
			Integer symForObjInPt1 = pt1.getObject2Symbol().get(obj);
			if (symForObjInPt1 != null) {
				pt2.replaceSymbol(sym, symForObjInPt1);
			} else {
				jointAlphabet.add(sym);
				jointSymbol2Object.put(sym, obj);
				jointObject2Symbol.put(obj, sym);
			}
		}

		// Construct joint NFA
		int[] alphabet = new int[jointAlphabet.size()];
		int c = 0;
		for (Iterator<Integer> i = jointAlphabet.iterator(); i.hasNext();) {
			alphabet[c] = i.next();
			c++;
		}
		NFA jointNFA = NFA.or(alphabet, jointAlphabet.size(), (NFA) pt1,
				(NFA) pt2);

		return new ProcessAutomaton(jointNFA, jointSymbol2Object,
				jointObject2Symbol, null);
	}

	/**
	 * Returns the automaton that accepts the complement of the set of traces
	 * accepted by the argument automaton.
	 * 
	 * @param ptnetsem
	 *            the automaton to return the complement of.
	 * @return the complement of the ptnetsem.
	 */
	public static ProcessAutomaton complement(ProcessAutomaton ptnetsem) {
		DFA complementSem = ((NFA) ptnetsem).getDFA();
		complementSem.complement();

		return new ProcessAutomaton(complementSem.getNFA(), ptnetsem
				.getSymbol2Object(), ptnetsem.getObject2Symbol(), ptnetsem
				.getProcess());
	}

	public Map<Object, Integer> getObject2Symbol() {
		return object2Symbol;
	}

	public Map<Integer, Object> getSymbol2Object() {
		return symbol2Object;
	}

	/**
	 * Returns the regular expression that represents the traces accepted by
	 * this automaton.
	 * 
	 * @return the resular expression that represents the traces accepted by
	 *         this sutomaton.
	 */
	public String deriveRegExp() {
		DFA myDFA = this.getDFA();
		myDFA.minimize();
		String regexp = myDFA.deriveRegExp();
		for (Iterator<Entry<Integer, Object>> i = getSymbol2Object().entrySet()
				.iterator(); i.hasNext();) {
			Entry<Integer, Object> e = i.next();
			regexp = regexp.replaceAll("\\[" + e.getKey() + "\\]", "'"
					+ e.getValue().toString() + "'");
		}
		return regexp;
	}

	/**
	 * Returns the set of cycles that contain o.
	 * 
	 * @return the set of cycles that contain o. Cycles are specified as lists
	 *         starting with o.
	 */
	public Set<List<Object>> cyclesContaining(Object o) {
		Set<List<Integer>> superResult = super.cyclesContaining(object2Symbol
				.get(o));
		Set<List<Object>> result = new HashSet<List<Object>>();

		for (Iterator<List<Integer>> i = superResult.iterator(); i.hasNext();) {
			List<Integer> superPartialResult = i.next();
			List<Object> partialResult = new LinkedList<Object>();
			for (Iterator<Integer> j = superPartialResult.iterator(); j
					.hasNext();) {
				partialResult.add(symbol2Object.get(j.next()));
			}
			result.add(partialResult);
		}

		return result;
	}

	/**
	 * Returns true iff the automaton accepts a trace that consists of one step.
	 * 
	 * @param step
	 *            the step
	 * @return true iff the trace 'step' is accepted
	 */
	public boolean acceptsTraceWithSingleStepIn(Object step) {
		DFA dfa = getDFA();
		int symbol = object2Symbol.get(step);
		return dfa.acceptsFromStart(symbol);
	}

	/**
	 * Returns the symbols that precede the given symbol in some execution
	 * trace.
	 * 
	 * @param o
	 *            the given symbol.
	 * @return the symbols that precede o in some execution trace.
	 */
	public Set<Object> symbolsPreceding(Object o) {
		Set<Object> result = new HashSet<Object>();
		DFA dfa = getDFA();

		// Get states from which eqT leaves
		IntegerSet statesEqTFrom = new IntegerSet(); // TODO: We should actually
		// get rid of the stupid
		// IntegerSet
		Iterator<Integer> it = dfa.statesInWhichSOriginates(
				getObject2Symbol().get(o)).iterator();
		while (it.hasNext()) {
			statesEqTFrom.add(it.next());
		}
		// Get symbols that point to one of these states
		for (int i = 0; i < dfa.getAlphabetSize(); i++) {
			Integer sym = dfa.getAlphabet()[i];
			if (!dfa.statesToWhichSPoints(sym).intersection(statesEqTFrom)
					.isEmpty()) {
				// add the object represented by this symbol
				result.add(getSymbol2Object().get(sym));
			}
		}

		return result;
	}

	/**
	 * Returns the input of a given activity in the process from getProcess().
	 * Definition 2 from 2007 BETA WP. Ignores the 'skipped activities' while
	 * doing that.
	 * 
	 * @param t
	 *            the activity to determine the input for.
	 * @param toSkip
	 *            the skipped activities.
	 * @return the input of the given activity, which is the set of all
	 *         activities from which there is a path to t with no activity on
	 *         that path other than a silent or skipped activity.
	 */
	public Set<ModelGraphVertex> inFor(ModelGraphVertex t,
			Set<ModelGraphVertex> toSkip) {
		Set<ModelGraphVertex> visited = new HashSet<ModelGraphVertex>();
		visited.add(t);
		if (process instanceof PetriNet) {
			toSkip.addAll(((PetriNet) process).getPlaces());
			toSkip.addAll(((PetriNet) process).getInvisibleTasks());
		}
		if ((process instanceof EPC) || (process instanceof ConfigurableEPC)) {
			toSkip.addAll(((ConfigurableEPC) process).getEvents());
			toSkip.addAll(((ConfigurableEPC) process).getConnectors());
		}
		Set<ModelGraphVertex> result = inForHelper(t, visited, toSkip);
		result.add(t);
		return result;
	}

	private Set<ModelGraphVertex> inForHelper(ModelGraphVertex t,
			Set<ModelGraphVertex> visited, Set<ModelGraphVertex> toSkip) {
		Set<ModelGraphVertex> result = new HashSet<ModelGraphVertex>();

		for (Iterator<ModelGraphVertex> i = t.getPredecessors().iterator(); i
				.hasNext();) {
			ModelGraphVertex tPre = i.next();
			if (visited.contains(tPre)) { // if it is visited do nothing
			} else if (toSkip.contains(tPre)) { // if it is skipped add its
				// pre's
				Set<ModelGraphVertex> visitedPrime = new HashSet<ModelGraphVertex>();
				visitedPrime.addAll(visited);
				visitedPrime.add(tPre);
				result.addAll(inForHelper(tPre, visitedPrime, toSkip));
			} else { // if it is not skipped add it to the result set
				result.add(tPre);
			}
		}
		return result;
	}

	private void setAlphabet() {
		symbol2Object = new HashMap<Integer, Object>();
		object2Symbol = new HashMap<Object, Integer>();
		int alphabetSize = visibleTasks().size();
		int[] alphabet = new int[alphabetSize];
		int alphabetCounter = 0;
		for (Iterator<ModelGraphVertex> i = visibleTasks().iterator(); i
				.hasNext();) {
			ModelGraphVertex t = i.next();
			alphabet[alphabetCounter] = NFA.EPSILON + 1 + alphabetCounter;
			symbol2Object.put(NFA.EPSILON + 1 + alphabetCounter, t);
			object2Symbol.put(t, NFA.EPSILON + 1 + alphabetCounter);
			alphabetCounter++;
		}
		setAlphabet(alphabet, alphabetCounter);
	}

	/**
	 * Returns the activities that are not silent.
	 * 
	 * @return the activities that are not silent.
	 */
	public Set<ModelGraphVertex> visibleTasks() {
		return ProcessAutomaton.visibleTasks(process);
	}

	/**
	 * Returns the activities that are not silent.
	 * 
	 * @return the activities that are not silent.
	 */
	public static Set<ModelGraphVertex> visibleTasks(ModelGraph model) {
		Set<ModelGraphVertex> result = new HashSet<ModelGraphVertex>();

		if (model instanceof PetriNet) {
			result.addAll(((PetriNet) model).getVisibleTasks());
		}
		if ((model instanceof EPC) || (model instanceof ConfigurableEPC)) {
			result.addAll(((ConfigurableEPC) model).getFunctions());
		}

		return result;
	}

	/**
	 * Returns true if and only if the model is of a type that can be processed.
	 * Currently Petri nets, EPCs and Configurable EPCs are supported.
	 * 
	 * @param m
	 *            the model to check support for.
	 * @return true if and only if the model is of a type that can be processed.
	 */
	public static boolean acceptedModelType(Object m) {
		return (m instanceof PetriNet) || (m instanceof EPC)
				|| (m instanceof ConfigurableEPC);
	}

	private void constructEPCAutomaton(ConfigurableEPC epc) {
		PetriNet n = EPCToPetriNetConverter.convert(epc, new HashMap());
		;
		simplifyNet(n);

		do {
			n = reduceNet(n);
		} while (simplifyNet(n));
		simplifyNet(n);

		constructPetriNetAutomaton(n, true);
	}

	/**
	 * @param forEPC
	 *            the automaton is constructed for a Petri net that was derived
	 *            from an EPC
	 */
	private void constructPetriNetAutomaton(PetriNet ptnet, boolean forEPC) {
		InitialPlaceMarker.mark(ptnet, 1);
		StateSpace rgraph = ReachabilityGraphBuilder.build(ptnet);
		FSMState startState = rgraph.getStartState();

		for (ModelGraphVertex v : rgraph.getVerticeList()) {
			if (v instanceof State) {
				State s = (State) v;
				boolean isAccepting = (s.getOutEdges() == null) ? true : (s
						.getOutEdges().size() == 0);
				addState(s.getMarking(), isAccepting);
				if (startState.equals(s)) {
					setInitialState(s.getMarking());
				}
			}
		}
		for (Object e : rgraph.getEdges()) {
			if (e instanceof FSMTransition) {
				FSMTransition t = (FSMTransition) e;
				Transition trigger = (Transition) t.object; // Should be
				// instanceof
				// Transition
				Marking targetMarking = ((State) t.getHead()).getMarking();
				Marking sourceMarking = ((State) t.getTail()).getMarking();
				if (trigger.isInvisibleTask()) {
					this.addTransition(sourceMarking, targetMarking,
							NFA.EPSILON);
				} else {
					Object o = null;
					if (forEPC) {
						// If the transition is derived from an EPC function the
						// EPC function should be on
						// in the transitionsystem rather than the PetriNet
						// transition.
						// Assumption: (toFire.isVisibleTask()) iff
						// (toFire.object instanceof EPCFunction)
						o = trigger.object;
					} else {
						o = trigger;
					}
					this.addTransition(sourceMarking, targetMarking,
							object2Symbol.get(o));
				}
			}
		}
	}

	private void constructAutomaton() {
		if ((process instanceof EPC) || (process instanceof ConfigurableEPC)) {
			ConfigurableEPC epc = (ConfigurableEPC) process;
			constructEPCAutomaton(epc);
		}
		if (process instanceof PetriNet) {
			PetriNet n = (PetriNet) process;
			constructPetriNetAutomaton(n, false);
		}
	}

	private PetriNet reduceNet(PetriNet petrinet) {
		HashSet visible = new HashSet();
		Iterator it = petrinet.getTransitions().iterator();
		// Since the reduction removes all transition.object, we need to keep
		// track of those.
		Map<String, EPCFunction> m = new HashMap<String, EPCFunction>();
		while (it.hasNext()) {
			Transition t = (Transition) it.next();
			if (t.object instanceof EPCFunction) {
				visible.add(t);
				m.put(t.getIdentifier(), (EPCFunction) t.object);
			}
		}

		it = petrinet.getPlaces().iterator();
		while (it.hasNext()) {
			Place p = (Place) it.next();
			if (p.inDegree() * p.outDegree() == 0) {
				visible.add(p);
			}
		}

		// Reduce the PetriNet with Murata rules, while keeping the visible ones
		PetriNetReduction pnred = new PetriNetReduction();
		pnred.setNonReducableNodes(visible);

		PetriNet reduced = pnred.reduce(petrinet);
		reduced.makeClusters();

		// Now add the transition.object again
		for (Transition t : reduced.getTransitions()) {
			t.object = m.get(t.getIdentifier());
		}

		return reduced;
	}

	private boolean simplifyNet(PetriNet p) {
		ArrayList<Place> ap = new ArrayList<Place>();
		ap.addAll(p.getPlaces());

		boolean changed = false;

		for (Place place : ap) {
			if (place.outDegree() == 0) {
				p.delPlace(place);
				changed = true;
			}

			ArrayList<Transition> at = new ArrayList<Transition>();
			if (place.getOutEdges() != null) {
				for (Edge mge : place.getOutEdges()) {
					if (mge.getHead() instanceof Transition) {
						if ((mge.getHead().outDegree() == 0)
								&& ((Transition) mge.getHead())
										.isInvisibleTask()) {
							at.add((Transition) mge.getHead());
						}
					}
				}
			}
			if (at.size() > 1) {
				at.remove(0);
				for (Transition t : at) {
					p.delTransition(t);
					changed = true;
				}
			}
		}
		return changed;
	}

}