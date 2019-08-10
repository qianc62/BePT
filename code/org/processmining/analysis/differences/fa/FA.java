package org.processmining.analysis.differences.fa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.analysis.differences.relations.Relation;
import org.processmining.analysis.differences.relations.Tuple;
import org.processmining.framework.models.petrinet.Marking;

/**
 * The FA defines an abstract class for finite automatons. This class should be
 * overridden by primarily the deterministic- and non-deterministic
 * implementations. The class wraps the data of the automaton, and provides
 * various methods for manipulating a finite automaton.
 * 
 * @author Marcus Johansson, Remco Dijkman
 * @version 1.0
 */

public abstract class FA {

	/* We declare the automaton data we need */
	private int[] alphabet;
	private int alphabetSize;

	private FATransitions[] s;
	private IntegerSet acceptingStates;
	private IntegerSet allStates;
	private int initialState;

	/* The current top of the transitionlist, i.e., the highest used state */
	private int topState;

	/* Map for translating state names to internal indices */
	private Hashtable stateNameMap;
	private Marking[] stateNames;

	private int freeList[];
	private int freeTop;

	/* The size of the lite with state transitions */
	private int maxStates;

	/* A pointer from a symbol to a set of states from which this symbol leaves */
	private Map<Integer, Set<Integer>> sym2States;

	private Relation<Integer, Integer> adjacencyRelation;

	/**
	 * Constructs an empty FA initialized to a capacity of 16 states. This is
	 * later doubled on need.
	 * 
	 * @param alphabet
	 *            The alphabet that the NFA should accept.
	 * @param alphabetSize
	 *            The size of the alphabet.
	 * 
	 */
	public FA(int[] alphabet, int alphabetSize) {
		this(alphabet, alphabetSize, 16);
	}

	/**
	 * Constructs an empty FA initialized to maxStates states.
	 * 
	 * @param alphabet
	 *            The alphabet that the NFA should accept.
	 * @param alphabetSize
	 *            The size of the alphabet.
	 * @param maxStates
	 *            The maximum number of states in the automaton. This is later
	 *            doubled on need.
	 * 
	 */
	public FA(int[] alphabet, int alphabetSize, int maxStates) {
		this.alphabet = alphabet;
		this.alphabetSize = alphabetSize;
		this.maxStates = maxStates;

		s = new FATransitions[maxStates];
		stateNames = new Marking[maxStates];
		topState = 0;

		allStates = new IntegerSet();
		acceptingStates = new IntegerSet();
		initialState = -1;

		stateNameMap = new Hashtable();

		freeList = new int[maxStates];
		freeTop = -1;

		sym2States = new HashMap();

		adjacencyRelation = null;
	}

	/**
	 * Sets the initial state of the automaton. Does nothing if that state does
	 * not exist
	 * 
	 * @param state
	 *            The name of the initial state.
	 * 
	 */
	public void setInitialState(Marking state) {
		adjacencyRelation = null;
		int i = getStateIndex(state);

		if (i == -1) {
		} else {
			initialState = i;
		}
	}

	/**
	 * Sets the initial state of the automaton.
	 * 
	 * @param stateIndex
	 *            The index of the initial state
	 */
	public void setInitialState(int stateIndex) {
		initialState = stateIndex;
	}

	/**
	 * Method for adding a state to the automaton without giving the state a
	 * name. The state will be given a unique name in the context of the
	 * automaton.
	 * 
	 * @param accepting
	 *            Specifices whether the state is accepting or not.
	 * @return Integer representing the internal state id.
	 * 
	 */

	public int addState(boolean accepting) {
		return addState(nonExistingMarking(), accepting);
	}

	/**
	 * Method for adding a state to the automaton.
	 * 
	 * @param name
	 *            The name of the state to add.
	 * @param accepting
	 *            Specifices whether the state is accepting or not.
	 * @return Integer representing the internal state id.
	 * 
	 */
	public int addState(Marking name, boolean accepting) {
		adjacencyRelation = null;
		int i = (name == null) ? -1 : getStateIndex(name);

		if (i != -1 && name != null) {
			return i;
		} else {
			i = getFreeSlot();

			if (i == maxStates) {
				FATransitions[] tempS = new FATransitions[maxStates * 2];
				int tempFreeList[] = new int[maxStates * 2];
				Marking tempStateNames[] = new Marking[maxStates * 2];

				for (int j = 0; j < maxStates; j++) {
					tempS[j] = s[j];
					tempFreeList[j] = freeList[j];
					tempStateNames[j] = stateNames[j];
				}
				s = tempS;
				freeList = tempFreeList;
				stateNames = tempStateNames;

				maxStates = maxStates * 2;
			}

			if (name == null) {
				name = new Marking();
			}

			stateNameMap.put(name, new Integer(i));
			stateNames[i] = name;
			s[i] = new FATransitions();
		}

		allStates.add(i);

		if (accepting) {
			acceptingStates.add(i);
		}

		return i;
	}

	/**
	 * Method for adding a transition to the automaton.
	 * 
	 * @param sourceState
	 *            The state from which the transition should start.
	 * @param targetState
	 *            The state to which the transition should go.
	 * @param symbol
	 *            The input symbol for which the transition should occur.
	 * @param nonDeterministic
	 *            Specifies whether the transition may be non-deterministic.
	 * @return false iff the transition cannot be added (i.e. it already exists,
	 *         one of its states does not exist, or it violates determinism).
	 * 
	 */
	public boolean addTransition(Marking sourceState, Marking targetState,
			int symbol, boolean nonDeterministic) {
		adjacencyRelation = null;
		int i = getStateIndex(sourceState);
		int j = getStateIndex(targetState);

		return addTransition(i, j, symbol, nonDeterministic);
	}

	/**
	 * Method for adding a transition to the automaton.
	 * 
	 * @param sourceStateIndex
	 *            The index of the state from which the transition should start.
	 * @param targetStateIndex
	 *            The index of the state to which the transition should go.
	 * @param symbol
	 *            The input symbol for which the transition should occur.
	 * @param nonDeterministic
	 *            Specifies whether the transition may be non-deterministic.
	 * @return false iff the transition cannot be added (i.e. it already exists,
	 *         one of its states does not exist, or it violates determinism).
	 * 
	 */
	public boolean addTransition(int sourceStateIndex, int targetStateIndex,
			int symbol, boolean nonDeterministic) {
		adjacencyRelation = null;
		if (sourceStateIndex == -1 || targetStateIndex == -1) {
			return false;
		} else {
			addSymbolToState(sourceStateIndex, symbol);
			return s[sourceStateIndex].addTransition(targetStateIndex, symbol,
					nonDeterministic);
		}
	}

	private void addSymbolToState(int stateIndex, int symbol) {
		Set<Integer> statesWithSymbol = sym2States.get(symbol);
		if (statesWithSymbol == null) {
			statesWithSymbol = new HashSet<Integer>();
			statesWithSymbol.add(stateIndex);
			sym2States.put(symbol, statesWithSymbol);
		} else {
			statesWithSymbol.add(stateIndex);
		}
	}

	private void removeSymbolFromState(int stateIndex, int symbol) {
		Set<Integer> statesWithSymbol = sym2States.get(symbol);
		if (statesWithSymbol != null) {
			statesWithSymbol.remove(stateIndex);
		}
	}

	public void replaceSymbol(int symbol, int replacement,
			boolean allowNonDeterminism) {
		adjacencyRelation = null;
		int c = 0;
		int symbolFoundAt = -1;
		int replacementFoundAt = -1;
		while ((c < alphabetSize)
				&& ((symbolFoundAt == -1) || (replacementFoundAt == -1))) {
			if (alphabet[c] == symbol) {
				symbolFoundAt = c;
			}
			if (alphabet[c] == replacement) {
				replacementFoundAt = c;
			}
			c++;
		}
		if ((replacement != NFA.EPSILON) && (replacementFoundAt == -1)) {
			if (symbolFoundAt != -1) {
				alphabet[symbolFoundAt] = replacement;
			}
		} else {
			if (symbolFoundAt != -1) {
				alphabet[symbolFoundAt] = alphabet[alphabetSize - 1];
				alphabetSize--;
			}
		}

		Set<Integer> statesWithSymbol = new HashSet<Integer>();
		statesWithSymbol.addAll(sym2States.get(symbol));
		for (Iterator<Integer> i = statesWithSymbol.iterator(); i.hasNext();) {
			Integer state = i.next();
			s[state].replaceSymbol(symbol, replacement, allowNonDeterminism);
			addSymbolToState(state, replacement);
			removeSymbolFromState(state, symbol);
		}
	}

	/**
	 * Method for deleting a state from the automaton. If the given state does
	 * not exist in the automaton, it remains unchanged.
	 * 
	 * @param name
	 *            The name of the state to delete. I
	 * 
	 */
	public void delState(Marking name) {
		adjacencyRelation = null;
		int i = getStateIndex(name);

		int[] symbolsfromi = s[i].getSymbolArray();
		for (int k = 0; k < symbolsfromi.length; k++) {
			removeSymbolFromState(i, symbolsfromi[k]);
		}

		if (i == -1) {
			return;
		}

		for (int j = 0; j < topState; j++) {
			if (i == j) {
				continue;
			}

			if (s[j] != null) {
				int[] symbolsfromj = s[j].getSymbolArray();
				for (int k = 0; k < symbolsfromj.length; k++) {
					IntegerSet onlyStateI = new IntegerSet();
					onlyStateI.add(i);
					if (s[j].getSymbolTargetStates(symbolsfromj[k]).equals(
							onlyStateI)) {
						removeSymbolFromState(j, symbolsfromj[k]);
					}
				}
				s[j].removeTransitionsToState(i);
			}
		}

		s[i] = null;
		stateNames[i] = null;
		allStates.remove(i);
		acceptingStates.remove(i);

		stateNameMap.remove(name);
		freeList[++freeTop] = i;
	}

	/**
	 * Method for deleting a transition from the automaton. If the given
	 * transition does not exist in the automaton, it remains unchanged.
	 * 
	 * @param sourceState
	 *            The source state of the transition.
	 * @param targetState
	 *            The target state of the transition.
	 * @param symbol
	 *            The symbol of the transition.
	 * 
	 */
	public void delTransition(Marking sourceState, Marking targetState,
			int symbol) {
		adjacencyRelation = null;
		int i = getStateIndex(sourceState);
		int j = getStateIndex(targetState);

		if (i != -1 && j != -1) {
			IntegerSet onlyStateJ = new IntegerSet();
			onlyStateJ.add(j);
			if (s[i].getSymbolTargetStates(symbol).equals(onlyStateJ)) {
				removeSymbolFromState(i, symbol);
			}
			s[i].removeTransition(j, symbol);
		}
	}

	/**
	 * Returns a string representation of the automaton.
	 * 
	 * @return A string representing the automaton.
	 * 
	 */
	public String toString() {
		String str = "";

		for (int i = 0; i < topState; i++) {
			str += stateNames[i] + "(" + i + "): " + s[i] + "\n";
		}

		return str;
	}

	/**
	 * Returns the unique internal state id of a given state name.
	 * 
	 * @param stateName
	 *            The name of the state.
	 * @return Integer representing the internal state id.
	 * 
	 */
	protected int getStateIndex(Marking stateName) {
		Integer iIndex = (Integer) stateNameMap.get(stateName);
		// TODO this has to be solved much better!! Marian
		if (iIndex == null) {
			Enumeration e = stateNameMap.keys();
			while (e.hasMoreElements()) {
				Marking m = (Marking) e.nextElement();
				if (m.toString().equals(stateName.toString()))
					iIndex = (Integer) stateNameMap.get(m);
			}
			if (iIndex == null) {
				return -1;
			}
		}
		return iIndex.intValue();
	}

	/**
	 * Returns an array with the characters that are part of the alphabet
	 * (EPSILON excluded).
	 * 
	 * @return The alphabet.
	 * 
	 */

	public int[] getAlphabet() {
		return alphabet;
	}

	/**
	 * Returns the size of the alphabet.
	 * 
	 * @return The size of the alphabet.
	 * 
	 */
	public int getAlphabetSize() {
		return alphabetSize;
	}

	/**
	 * Returns the size of the array of state transitions.
	 * 
	 * @return The maximum number of states.
	 * 
	 */
	protected int getMaxStates() {
		return maxStates;
	}

	/**
	 * Returns the array of state transitions of the NFA. Each index in the
	 * array holds the transitions object of the state with internal id index.
	 * 
	 * @return Array of state transitions.
	 * 
	 */
	protected FATransitions[] getStates() {
		return s;
	}

	/**
	 * Returns index of the top state. This is used in conjunction with
	 * getStates in order to know the utilized length of the returned state
	 * transitions array.
	 * 
	 * @return The index of the top state.
	 * 
	 */
	protected int getTopStateIndex() {
		return topState;
	}

	/**
	 * Return the index of the initial state.
	 * 
	 * @return Index of the initial state.
	 * 
	 */
	protected int getInitialState() {
		return initialState;
	}

	/**
	 * Returns the set of accepting state indices.
	 * 
	 * @return Set of accepting state indices.
	 * 
	 */
	protected IntegerSet acceptingStates() {
		return acceptingStates;
	}

	/**
	 * Sets the set of accepting state indices.
	 */
	protected void setAcceptingStates(IntegerSet acceptingStates) {
		adjacencyRelation = null;
		this.acceptingStates = acceptingStates;
	}

	/**
	 * Returns the set of all state indicies.
	 * 
	 * @return Set of all state indices.
	 * 
	 */
	protected IntegerSet allStates() {
		return allStates;
	}

	/**
	 * Return the name of the state with the given index i.
	 * 
	 * @param i
	 *            The index of the state.
	 * @return The name of the state.
	 * 
	 */
	protected Marking getStateName(int i) {
		return stateNames[i];
	}

	private int getFreeSlot() {
		if (freeTop >= 0) {
			return freeList[freeTop--];
		}

		return topState++;
	}

	/**
	 * Sets the alphabet of the FA.
	 * 
	 * @param alphabet
	 *            The alphabet that the NFA should accept.
	 * @param alphabetSize
	 *            The size of the alphabet.
	 * 
	 */
	public void setAlphabet(int[] alphabet, int alphabetSize) {
		adjacencyRelation = null;
		this.alphabet = alphabet;
		this.alphabetSize = alphabetSize;
	}

	private void computeReachabilityMatrix() {
		adjacencyRelation = new Relation<Integer, Integer>();
		FATransitions[] allStates = getStates();
		for (int c = 0; c < topState; c++) {
			int targetStates[] = allStates[c].getTargetArray();
			for (int targeti = 0; targeti < targetStates.length; targeti++) {
				adjacencyRelation.addR(new Tuple<Integer, Integer>(c,
						targetStates[targeti]));
			}
		}
	}

	public boolean path(Marking fromState, Marking toState) {
		int from = getStateIndex(fromState);
		int to = getStateIndex(toState);
		if ((from == -1) || (to == -1)) {
			return false;
		} else {
			return path(from, to);
		}
	}

	public boolean path(int fromState, int toState) {
		if (adjacencyRelation == null) {
			this.computeReachabilityMatrix();
		}
		return adjacencyRelation.path(fromState, toState);
	}

	public Set<Marking> reachableFrom(Marking fromState) {
		Set<Marking> result = new HashSet<Marking>();
		int from = getStateIndex(fromState);
		if (from != -1) {
			Set<Integer> intermediateResult = reachableFrom(from);
			for (Iterator<Integer> i = intermediateResult.iterator(); i
					.hasNext();) {
				result.add(getStateName(i.next()));
			}
		}
		return result;
	}

	public Set<Integer> reachableFrom(int fromState) {
		if (adjacencyRelation == null) {
			this.computeReachabilityMatrix();
		}
		return adjacencyRelation.reachableFrom(fromState);
	}

	public boolean isEmptyLanguage() {
		IntegerSet reachableFromStart = new IntegerSet(
				reachableFrom(getInitialState()));
		return reachableFromStart.intersection(acceptingStates).isEmpty();
	}

	protected FATransitions[] getFATransitions() {
		return s;
	}

	/*
	 * This returns minimal cycles, but we are not looking for minimal cycles,
	 * we are looking for all cycles private Set<List<Integer>> cyclesHelper(int
	 * startingAt, int from, Set<Integer> visited, List<Integer> listSoFar){
	 * Set<List<Integer>> result = new HashSet<List<Integer>>();
	 * 
	 * int symbols[] = s[from].getSymbolArray(); for (int i = 0; i <
	 * symbols.length; i++){ int symbol = symbols[i]; int toStates[] =
	 * s[from].getSymbolTargetStates(symbol).getArray(); for (int j = 0; j <
	 * toStates.length; j++){ int toState = toStates[j]; if (toState ==
	 * startingAt){ List<Integer> listSoFarPrime = new LinkedList<Integer>();
	 * listSoFarPrime.addAll(listSoFar); listSoFarPrime.add(symbol);
	 * result.add(listSoFarPrime); }else if (visited.contains(toState)){ }else{
	 * Set<Integer> visitedPrime = new HashSet<Integer>();
	 * visitedPrime.addAll(visited); visitedPrime.add(toState); List<Integer>
	 * listSoFarPrime = new LinkedList<Integer>();
	 * listSoFarPrime.addAll(listSoFar); listSoFarPrime.add(symbol);
	 * result.addAll
	 * (cyclesHelper(startingAt,toState,visitedPrime,listSoFarPrime)); } } }
	 * return result; }
	 * 
	 * protected Set<List<Integer>> cyclesContaining(int symbol){
	 * Set<List<Integer>> result = new HashSet<List<Integer>>(); Set<Integer>
	 * fromStates = sym2States.get(symbol); for (Iterator<Integer> i =
	 * fromStates.iterator(); i.hasNext();){ int fromState = i.next(); int
	 * toStates[] = s[fromState].getSymbolTargetStates(symbol).getArray(); for
	 * (int j = 0; j < toStates.length; j++){ int toState = toStates[j];
	 * Set<Integer> visited = new HashSet<Integer>(); visited.add(fromState);
	 * visited.add(toState); List<Integer> listSoFar = new
	 * LinkedList<Integer>(); listSoFar.add(symbol); if (fromState == toState){
	 * List<Integer> listSoFarPrime = new LinkedList<Integer>();
	 * listSoFarPrime.addAll(listSoFar); result.add(listSoFar); }else{
	 * result.addAll(cyclesHelper(fromState, toState, visited, listSoFar)); } }
	 * } return result; }
	 */

	private Set<List<Integer>> cyclesHelper(Tuple<Integer, Integer> startEdge,
			Tuple<Integer, Integer> fromEdge,
			Set<Tuple<Integer, Integer>> visited, List<Integer> listSoFar) {
		Set<List<Integer>> result = new HashSet<List<Integer>>();

		int fromStates[] = s[fromEdge.e1].getSymbolTargetStates(fromEdge.e2)
				.getArray();
		for (int i = 0; i < fromStates.length; i++) {
			int symbols[] = s[fromStates[i]].getSymbolArray();
			for (int j = 0; j < symbols.length; j++) {
				Tuple<Integer, Integer> edgeExploring = new Tuple<Integer, Integer>(
						fromStates[i], symbols[j]);
				if (edgeExploring.equals(startEdge)) {
					result.add(listSoFar);
				} else if (visited.contains(edgeExploring)) {
				} else {
					Set<Tuple<Integer, Integer>> visitedPrime = new HashSet<Tuple<Integer, Integer>>();
					visitedPrime.addAll(visited);
					visitedPrime.add(edgeExploring);
					List<Integer> listSoFarPrime = new LinkedList<Integer>();
					listSoFarPrime.addAll(listSoFar);
					listSoFarPrime.add(symbols[j]);
					result.addAll(cyclesHelper(startEdge, edgeExploring,
							visitedPrime, listSoFarPrime));
				}
			}
		}
		return result;
	}

	protected Set<List<Integer>> cyclesContaining(int symbol) {
		Set<List<Integer>> result = new HashSet<List<Integer>>();
		Set<Integer> fromStates = sym2States.get(symbol);
		for (Iterator<Integer> i = fromStates.iterator(); i.hasNext();) {
			int fromState = i.next();
			Tuple<Integer, Integer> edgeExploring = new Tuple<Integer, Integer>(
					fromState, symbol);
			Set<Tuple<Integer, Integer>> visited = new HashSet<Tuple<Integer, Integer>>();
			visited.add(edgeExploring);
			List<Integer> listSoFar = new LinkedList<Integer>();
			listSoFar.add(symbol);
			result.addAll(cyclesHelper(edgeExploring, edgeExploring, visited,
					listSoFar));
		}
		return result;
	}

	protected Marking nonExistingMarking() {
		Marking m;
		PetriNet p = new PetriNet(); // Added by Marian maybe causes
		// problems!!!!
		Integer uniqueNumber = 0;
		do {
			uniqueNumber++;
			m = new Marking();
			m.addPlace(new Place("a", p), uniqueNumber); // p is added by Marian
			// maybe causes
			// problem
		} while (this.getStateIndex(m) != -1);
		return m;
	}

	public Set<Integer> statesInWhichSOriginates(int sym) {
		return sym2States.get(sym);
	}

	public IntegerSet statesToWhichSPoints(int sym) {
		Set<Integer> statesInWhichSOriginates = statesInWhichSOriginates(sym);
		IntegerSet result = new IntegerSet();

		for (Iterator<Integer> i = statesInWhichSOriginates.iterator(); i
				.hasNext();) {
			int origin = i.next();
			int targets[] = s[origin].getSymbolTargetStates(sym).getArray();
			for (int j = 0; j < targets.length; j++) {
				result.add(targets[j]);
			}
		}
		return result;
	}
}
