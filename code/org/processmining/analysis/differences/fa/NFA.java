package org.processmining.analysis.differences.fa;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Marking;

/**
 * The NFA defines a non-deterministic finite automaton by extending the finite
 * automaton class FA.
 * 
 * @author Marcus Johansson, Remco Dijkman
 * @version 1.0
 */

public class NFA extends FA {
	/**
	 * The epsilon transition symbol.
	 */
	public static final int EPSILON = 0;

	/**
	 * Constructs an empty NFA initialized to a capacity of 16 states. This is
	 * later doubled on need.
	 * 
	 * @param alphabet
	 *            The alphabet that the NFA should accept.
	 * @param alphabetSize
	 *            The size of the alphabet.
	 * 
	 */
	public NFA(int[] alphabet, int alphabetSize) {
		this(alphabet, alphabetSize, 16);
	}

	/**
	 * Constructs an empty NFA initialized to maxStates states.
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
	public NFA(int[] alphabet, int alphabetSize, int maxStates) {
		super(alphabet, alphabetSize, maxStates);
	}

	/**
	 * Constructs a NFA identical to the FA passed as parameter.
	 * 
	 * @param fa
	 *            The FA to copy.
	 * 
	 */
	public NFA(FA fa) {
		super(fa.getAlphabet(), fa.getAlphabetSize(), fa.getMaxStates());

		// copy the alphabet, otherwise errors can occur
		int newAlphabet[] = new int[fa.getAlphabetSize()];
		int oldAlphabet[] = fa.getAlphabet();
		for (int i = 0; i < fa.getAlphabetSize(); i++) {
			newAlphabet[i] = oldAlphabet[i];
		}
		this.setAlphabet(newAlphabet, fa.getAlphabetSize());

		FATransitions ms[] = fa.getStates();
		int size = fa.getTopStateIndex();
		int initial = fa.getInitialState();
		IntegerSet accepting = fa.acceptingStates();
		int stateMap[] = new int[size];

		/* Copy states */
		for (int i = 0; i < size; i++) {
			stateMap[i] = addState(fa.getStateName(i), accepting.contains(i));

			if (i == initial) {
				this.setInitialState(getStateName(i));
			}
		}

		/* Copy transitions */
		for (int i = 0; i < size; i++) {
			int tSize = ms[i].getSize();
			int symbol[] = ms[i].getSymbolArray();
			int target[] = ms[i].getTargetArray();

			for (int j = 0; j < tSize; j++) {
				addTransition(getStateName(stateMap[i]),
						getStateName(stateMap[target[j]]), symbol[j]);
			}
		}

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
	 * @return false iff the transition could not be added (i.e. it already
	 *         exists, or one of its states does not exist).
	 * 
	 */
	public boolean addTransition(Marking sourceState, Marking targetState,
			int symbol) {
		return addTransition(sourceState, targetState, symbol, true);
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
	 * @return false iff the transition could not be added (i.e. it already
	 *         exists, or one of its states does not exist).
	 * 
	 */
	public boolean addTransition(int sourceStateIndex, int targetStateIndex,
			int symbol) {
		return addTransition(sourceStateIndex, targetStateIndex, symbol, true);
	}

	/**
	 * Converts the NFA to a DFA using the subset construction technique.
	 * 
	 * @return A DFA accepting the same language as the NFA.
	 */
	public DFA getDFA() {
		IntegerSet acceptingStates = acceptingStates();

		int alphabetSize = getAlphabetSize();
		int[] alphabet = getAlphabet();

		DFA dfa = new DFA(alphabet, alphabetSize);

		IntegerPartition unmarkedStates = new IntegerPartition();
		IntegerPartition dStates = new IntegerPartition();

		IntegerSet temp = new IntegerSet();
		temp.add(getInitialState());
		temp = epsilonClosure(temp);
		int tStateNum = dStates.addGroup(temp);
		unmarkedStates.addGroup(temp);

		// We are not supposed to use markings in this way, but we will.
		PetriNet p = new PetriNet(); // added by marian
		Marking initialMarking = new Marking();
		initialMarking.addPlace(new Place(Integer.toString(tStateNum), p), 0); // p
		// added
		// by
		// marian,
		// addmark
		// changed
		// to
		// add
		// place
		dfa.addState(initialMarking, !(temp.intersection(acceptingStates))
				.isEmpty());
		dfa.setInitialState(initialMarking);

		while (tStateNum != -1) {
			IntegerSet tState = unmarkedStates.popFirstGroup();

			if (tState == null) {
				break;
			}
			tStateNum = dStates.getGroupNumber(tState);

			for (int i = 0; i < alphabetSize; i++) {
				IntegerSet reachable = symbolTransition(tState, alphabet[i]);

				reachable = epsilonClosure(reachable);

				if (reachable.isEmpty()) {
					continue;
				}

				int uStateNum = dStates.getGroupNumber(reachable);

				if (uStateNum == -1) {
					uStateNum = dStates.addGroup(reachable);
					unmarkedStates.addGroup(reachable);

					boolean accepting = !(reachable
							.intersection(acceptingStates)).isEmpty();

					Marking newMarking = new Marking();
					newMarking.addPlace(new Place(Integer.toString(uStateNum),
							p), 0);// p added by marian, addmark changed to add
					// place
					dfa.addState(newMarking, accepting);
				}

				Marking fromMarking = new Marking();
				fromMarking.addPlace(new Place(Integer.toString(tStateNum), p),
						0);// p added by marian, addmark changed to add place
				Marking toMarking = new Marking();
				toMarking
						.addPlace(new Place(Integer.toString(uStateNum), p), 0);// p
				// added
				// by
				// marian,
				// addmark
				// changed
				// to
				// add
				// place
				dfa.addTransition(fromMarking, toMarking, alphabet[i]);
			}

		}
		return dfa;
	}

	/**
	 * Merges a given NFA into the NFA.
	 * 
	 * @param merger
	 *            The NFA to merge with.
	 * @param startStateName
	 *            The name of the new initial state in the NFA.
	 */

	public void merge(FA merger, Marking startStateName) {
		int startState = getStateIndex(startStateName);

		FATransitions ms[] = merger.getStates();
		int size = merger.getTopStateIndex();
		int initial = merger.getInitialState();
		IntegerSet accepting = merger.acceptingStates();
		int stateMap[] = new int[size];

		/* Copy states */
		for (int i = 0; i < size; i++) {
			if (i == initial) {
				stateMap[i] = startState;
			} else {
				stateMap[i] = addState(nonExistingMarking(), accepting
						.contains(i));
			}
		}

		/* Copy transitions */
		for (int i = 0; i < size; i++) {
			int tSize = ms[i].getSize();
			int symbol[] = ms[i].getSymbolArray();
			int target[] = ms[i].getTargetArray();

			for (int j = 0; j < tSize; j++) {
				addTransition(stateMap[i], stateMap[target[j]], symbol[j]);
			}
		}

	}

	/**
	 * Returns an NFA representing a|b, given NFA's a and b.
	 * 
	 * @param alphabet
	 *            The alphabet of the created NFA.
	 * @param alphabetSize
	 *            The size of the alphabet.
	 * @param a
	 *            NFA a.
	 * @param b
	 *            NFA b.
	 * @return NFA N(a|b).
	 * 
	 */
	public static NFA or(int[] alphabet, int alphabetSize, NFA a, NFA b) {
		NFA orNFA = new NFA(alphabet, alphabetSize);

		int iState = orNFA.addState(orNFA.nonExistingMarking(), false);
		int or1StartState = orNFA.addState(orNFA.nonExistingMarking(), a
				.acceptingStates().contains(a.getInitialState()));
		int or2StartState = orNFA.addState(orNFA.nonExistingMarking(), b
				.acceptingStates().contains(b.getInitialState()));

		orNFA.setInitialState(orNFA.getStateName(iState));

		orNFA.addTransition(iState, or1StartState, EPSILON);
		orNFA.addTransition(iState, or2StartState, EPSILON);

		orNFA.merge(a, orNFA.getStateName(or1StartState));
		orNFA.merge(b, orNFA.getStateName(or2StartState));

		return orNFA;
	}

	private IntegerSet symbolTransition(IntegerSet states, int c) {
		FATransitions[] s = getStates();
		IntegerSet newM = new IntegerSet();

		int[] statesArray = states.getArray();
		int statesArrayLen = states.getCardinality();

		for (int i = 0; i < statesArrayLen; i++) {
			int elem = statesArray[i];

			newM = newM.union(s[elem].getSymbolTargetStates(c));
		}

		return newM;
	}

	private IntegerSet epsilonClosure(IntegerSet states) {
		FATransitions[] s = getStates();
		IntegerSet lastStateSet;

		do {

			int[] statesArray = states.getArray();
			int statesArrayLen = states.getCardinality();

			lastStateSet = new IntegerSet(states);
			for (int i = 0; i < statesArrayLen; i++) {
				int elem = statesArray[i];

				IntegerSet epsClosure = s[elem].getSymbolTargetStates(EPSILON);

				states = states.union(epsClosure);
			}

		} while (!lastStateSet.equals(states));
		return states;
	}

	public void replaceSymbol(int symbol, int replacement) {
		replaceSymbol(symbol, replacement, true);
	}
}
