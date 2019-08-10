package org.processmining.analysis.differences.fa;

import java.util.LinkedList;

import org.processmining.framework.models.petrinet.Marking;

/**
 * The DFA defines a deterministic finite automaton, by extending the finite
 * automaton class FA.
 * 
 * Methods are provided to minimize a DFA, and simulate it on a given input.
 * 
 * @author Marcus Johansson, Remco Dijkman
 * @version 1.0
 */

public class DFA extends FA {

	/**
	 * Constructs an empty DFA initialized to a capacity of 16 states. This is
	 * later doubled on need.
	 * 
	 * @param alphabet
	 *            The alphabet that the NFA should accept.
	 * @param alphabetSize
	 *            The size of the alphabet.
	 * 
	 */
	public DFA(int[] alphabet, int alphabetSize) {
		super(alphabet, alphabetSize, 16);
	}

	/**
	 * Constructs an empty DFA initialized to maxStates states.
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
	public DFA(int[] alphabet, int alphabetSize, int maxStates) {
		super(alphabet, alphabetSize, 16);
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
	 *         exists, one of its states does not exist, or it violates the
	 *         determinism).
	 * 
	 */
	public boolean addTransition(Marking sourceState, Marking targetState,
			int symbol) {
		return addTransition(sourceState, targetState, symbol, false);
	}

	public void replaceSymbol(int symbol, int replacement) {
		replaceSymbol(symbol, replacement, false);
	}

	/**
	 * Changes the DFA such that it accepts the language that is the complement
	 * of the language accepted by this DFA.
	 */
	public void complement() {
		closeUnderAlphabet();
		setAcceptingStates(allStates().removeSet(acceptingStates()));
	}

	/**
	 * Returns the NFA that accepts the same language as this DFA.
	 * 
	 * @return An NFA that accepts the same language as this DFA
	 */
	public NFA getNFA() {
		return new NFA(this);
	}

	/**
	 * Minimizes the DFA.
	 * 
	 * @return The minimized DFA.
	 * @throws FAException
	 *             if something goes wrong during minization.
	 * 
	 */
	public DFA minimize() {
		FATransitions[] s = getStates();
		IntegerSet acceptingStates = acceptingStates();
		IntegerSet allStates = allStates();

		IntegerPartition p = new IntegerPartition();
		p.addGroup(acceptingStates);
		p.addGroup(allStates.removeSet(acceptingStates));

		IntegerPartition pNew = p;

		do {
			p = pNew;

			pNew = refineStatePartition(p);
		} while (!p.equals(pNew));

		/*
		 * We create a new DFA, and chooses a representing state from each
		 * subgroup, which will be a state in the new DFA
		 */

		DFA minDFA = new DFA(getAlphabet(), getAlphabetSize());

		IntegerSet[] stateGroups = pNew.getGroups();
		int nrStateGroups = pNew.getSize();

		for (int i = 0; i < nrStateGroups; i++) {
			int sState = stateGroups[i].firstElement(); // The first element of
			// each group is the
			// rep. state
			boolean accepting = acceptingStates.contains(sState);
			minDFA.addState(getStateName(sState), accepting);
		}

		/* Next, we add transitions */
		int[] alphabet = getAlphabet();
		int alphabetSize = getAlphabetSize();

		for (int i = 0; i < nrStateGroups; i++) {
			int sState = stateGroups[i].firstElement();

			for (int k = 0; k < alphabetSize; k++) {
				int t = s[sState].getSymbolTargetState(alphabet[k]);

				if (t != -1) {
					int r = representativeState(pNew, t);

					minDFA.addTransition(getStateName(sState), getStateName(r),
							alphabet[k]);
				}

			}
		}

		minDFA.setInitialState(getStateName(representativeState(pNew,
				getInitialState())));

		/* Removing dead states */
		minDFA.removeDeadStates();

		return minDFA;
	}

	/**
	 * Removes "dead" states from the automaton. A state is dead if it is no
	 * accepting and has transitions to itself on all input symbols.
	 * 
	 */
	public void removeDeadStates() {
		FATransitions[] s = getStates();
		IntegerSet acceptingStates = acceptingStates();
		int topState = getTopStateIndex();

		for (int i = 0; i < topState; i++) {
			if (s[i] == null || acceptingStates.contains(i)) {
				continue;
			}

			boolean deadState = true;

			int alphabetSize = getAlphabetSize();
			int[] alphabet = getAlphabet();
			for (int k = 0; k < alphabetSize; k++) {
				int j = s[i].getSymbolTargetState(alphabet[k]);

				if (j != -1 && j != i) {
					deadState = false;
					break;
				}
			}

			if (deadState) {
				delState(getStateName(i));
			}
		}
	}

	private IntegerPartition refineStatePartition(IntegerPartition p) {
		FATransitions[] s = getStates();
		int alphabetSize = getAlphabetSize();
		int[] alphabet = getAlphabet();

		IntegerSet[] pgr = p.getGroups();
		int len = p.getSize();

		IntegerPartition newP = new IntegerPartition();

		/* Iterating once for each group of the partition */
		for (int i = 0; i < len; i++) {
			IntegerSet G = pgr[i];

			int[] Garray = G.getArray();
			int Glen = G.getCardinality();

			int seenTransitionLists[][] = new int[Glen][alphabetSize];
			int nrSeen = 0;

			IntegerSet[] subGroups = new IntegerSet[Glen];

			/* We iterate once for each state in the group */
			for (int j = 0; j < Glen; j++) {
				int state = Garray[j];

				int groupb[] = new int[alphabetSize];

				/*
				 * We put together the list of the transitions for this state on
				 * all alphabet symbols
				 */
				for (int k = 0; k < alphabetSize; k++) {
					int a = alphabet[k];

					int target = s[state].getSymbolTargetState(a);

					if (target != -1) {
						groupb[k] = p.getElementGroupNumber(target);
					} else {
						groupb[k] = -1;
					}
				}

				/*
				 * Have we seen this exact list of transitions of the
				 * alphabetsymbols before?
				 */
				int listNum = getIndexOfArray(seenTransitionLists, nrSeen,
						groupb, alphabetSize);

				if (listNum == -1) {
					/*
					 * If not, we add this list of transition to the list of
					 * seen ones
					 */

					listNum = nrSeen++;
					seenTransitionLists[listNum] = groupb;
					subGroups[listNum] = new IntegerSet();
				}

				/* We add this state to the group it belongs in */
				subGroups[listNum].add(state);
			}

			/* Finally, the group is replaced with its found subgroups */
			// newP.removeGroup(i);
			newP.addGroups(subGroups, nrSeen);
		}

		return newP;
	}

	private int getIndexOfArray(int[][] lists, int nrLists, int[] array,
			int arraySize) {

		for (int i = 0; i < nrLists; i++) {
			boolean equals = true;

			for (int j = 0; j < arraySize; j++) {
				if (lists[i][j] != array[j]) {
					equals = false;
					break;
				}
			}

			if (equals) {
				return i;
			}
		}

		return -1;
	}

	private int representativeState(IntegerPartition p, int state) {
		IntegerSet t = p.getGroup(p.getElementGroupNumber(state));

		return t.firstElement();
	}

	private String braceIfUnbracedU(String toBrace) {
		boolean hasUnbracedU = false;
		int openBraces = 0;
		int nextIndexOfBraceOrU = -1;

		do {
			nextIndexOfBraceOrU++;

			int nextIndexOfOpenBrace = toBrace
					.indexOf('(', nextIndexOfBraceOrU);
			nextIndexOfOpenBrace = (nextIndexOfOpenBrace == -1) ? toBrace
					.length() : nextIndexOfOpenBrace;
			int nextIndexOfCloseBrace = toBrace.indexOf(')',
					nextIndexOfBraceOrU);
			nextIndexOfCloseBrace = (nextIndexOfCloseBrace == -1) ? toBrace
					.length() : nextIndexOfCloseBrace;
			int nextIndexOfU = toBrace.indexOf('U', nextIndexOfBraceOrU);
			nextIndexOfU = (nextIndexOfU == -1) ? toBrace.length()
					: nextIndexOfU;

			nextIndexOfBraceOrU = Math.min(nextIndexOfU, Math.min(
					nextIndexOfOpenBrace, nextIndexOfCloseBrace));

			if (nextIndexOfBraceOrU < toBrace.length()) {
				if (toBrace.charAt(nextIndexOfBraceOrU) == '(') {
					openBraces++;
				} else if (toBrace.charAt(nextIndexOfBraceOrU) == ')') {
					openBraces--;
				} else {
					hasUnbracedU = hasUnbracedU || (openBraces == 0);
				}
			}

		} while ((nextIndexOfBraceOrU < toBrace.length()) && !hasUnbracedU);

		return (hasUnbracedU) ? ("(" + toBrace + ")") : toBrace;
	}

	private String braceIfUnbracedPart(String toBrace) {
		boolean hasUnbracedPart = false;
		int openBraces = 0;
		int nextIndexOfBrace = -1;

		do {
			nextIndexOfBrace++;

			int nextIndexOfOpenBrace = toBrace.indexOf('(', nextIndexOfBrace);
			nextIndexOfOpenBrace = (nextIndexOfOpenBrace == -1) ? toBrace
					.length() : nextIndexOfOpenBrace;
			int nextIndexOfCloseBrace = toBrace.indexOf(')', nextIndexOfBrace);
			nextIndexOfCloseBrace = (nextIndexOfCloseBrace == -1) ? toBrace
					.length() : nextIndexOfCloseBrace;
			int nextIndexStraightCloseBrace = toBrace.indexOf(']',
					nextIndexOfBrace);
			nextIndexStraightCloseBrace = (nextIndexStraightCloseBrace == -1) ? toBrace
					.length()
					: nextIndexStraightCloseBrace;

			nextIndexOfBrace = Math.min(nextIndexStraightCloseBrace, Math.min(
					nextIndexOfOpenBrace, nextIndexOfCloseBrace));

			if (nextIndexOfBrace < toBrace.length()) {
				if (toBrace.charAt(nextIndexOfBrace) == '(') {
					openBraces++;
				} else if (toBrace.charAt(nextIndexOfBrace) == ')') {
					openBraces--;
					if ((openBraces == 0)
							&& (nextIndexOfBrace != toBrace.length() - 1)) {
						hasUnbracedPart = true;
					}
				} else if (nextIndexOfBrace != toBrace.length() - 1) {
					hasUnbracedPart = hasUnbracedPart || (openBraces == 0);
				}
			}

		} while ((nextIndexOfBrace < toBrace.length()) && !hasUnbracedPart);

		return (hasUnbracedPart) ? ("(" + toBrace + ")") : toBrace;
	}

	/**
	 * Returns a regular expression for the language accepted by the DFA
	 */
	public String deriveRegExp() {
		/*
		 * The matrix used here represents a 'regular expression automaton',
		 * such that: regaut[i][j] = r represents that the automaton can
		 * transition from state i into state j by processing a string that is
		 * accepted by regexp r.
		 */

		String result = "";

		int finalStates[] = acceptingStates().getArray();
		int initialState = getInitialState();
		FATransitions states[] = getFATransitions();
		int statesLength = getTopStateIndex();

		// for each final state
		for (int fi = 0; fi < finalStates.length; fi++) {
			int f = finalStates[fi];
			LinkedList<Integer> nonIntFinStates = new LinkedList<Integer>(); // The
			// set
			// of
			// states
			// that
			// is
			// nether
			// the
			// initial
			// nor
			// the
			// final
			// construct a 'regular expression automaton'
			String regaut[][] = new String[statesLength][statesLength];
			for (int fromState = 0; fromState < statesLength; fromState++) {
				if ((fromState != initialState) && (fromState != f)) {
					nonIntFinStates.add(fromState);
				}
				int symbols[] = states[fromState].getSymbolArray();
				for (int i = 0; i < symbols.length; i++) {
					int symbol = symbols[i];
					int toState = states[fromState]
							.getSymbolTargetState(symbol);
					if (regaut[fromState][toState] == null) {
						regaut[fromState][toState] = "["
								+ Integer.toString(symbol) + "]";
					} else {
						String existing = regaut[fromState][toState]
								.startsWith("(") ? regaut[fromState][toState]
								.substring(1, regaut[fromState][toState]
										.length()) : regaut[fromState][toState]; // to
						// remove
						// brackets
						regaut[fromState][toState] = "(" + existing + " U ["
								+ Integer.toString(symbol) + "])";
					}
				}
			}
			// choose a state that is neither the initial nor the final state
			while (!nonIntFinStates.isEmpty()) {
				int state = nonIntFinStates.removeFirst();
				for (int j = 0; j < statesLength; j++) {
					for (int k = 0; k < statesLength; k++) {
						if ((j != state) && (k != state)
								&& (regaut[j][state] != null)
								&& (regaut[state][k] != null)) {
							regaut[j][k] = (regaut[j][k] != null) ? (regaut[j][k] + " U ")
									: "";
							if (regaut[state][state] == null) {
								regaut[j][k] += braceIfUnbracedU(regaut[j][state])
										+ braceIfUnbracedU(regaut[state][k]);
							} else {
								regaut[j][k] += braceIfUnbracedU(regaut[j][state])
										+ braceIfUnbracedPart(regaut[state][state])
										+ "*"
										+ braceIfUnbracedU(regaut[state][k]);
							}
						}
					}
				}
			}
			// determine expression accepted at f
			String aexp = "";
			if (initialState == f) {
				aexp = (regaut[initialState][f] == null) ? "EMPTY"
						: braceIfUnbracedPart(regaut[initialState][f]) + "*";
			} else {
				String w1star = (regaut[initialState][initialState] == null) ? ""
						: braceIfUnbracedPart(regaut[initialState][initialState])
								+ "*";
				String w2 = (regaut[initialState][f] == null) ? ""
						: regaut[initialState][f];
				String w3 = (regaut[f][f] == null) ? "" : regaut[f][f];
				String w4 = (regaut[f][initialState] == null) ? ""
						: regaut[f][initialState];
				if (w2.length() == 0) {
					aexp = w1star;
				} else {
					if ((w3.length() != 0) && (w4.length() != 0)) {
						aexp = w1star + braceIfUnbracedU(w2) + "("
								+ braceIfUnbracedU(w3) + " U "
								+ braceIfUnbracedU(w4) + w1star
								+ braceIfUnbracedU(w2) + ")*";
					} else if (w3.length() != 0) {
						aexp = w1star + braceIfUnbracedU(w2)
								+ braceIfUnbracedPart(w3) + "*";
					} else if (w4.length() != 0) {
						aexp = w1star + braceIfUnbracedU(w2) + "("
								+ braceIfUnbracedU(w4) + w1star
								+ braceIfUnbracedU(w2) + ")*";
					} else {
						aexp = w1star + braceIfUnbracedU(w2);
					}
				}
			}

			result = ((result.length() == 0) ? "" : result + " U ") + aexp;
		}

		return result;
	}

	/**
	 * Ensures that each state has a transition for each letter in the alphabet
	 */
	public void closeUnderAlphabet() {
		Marking sink = null;
		int alphabetsize = getAlphabetSize();
		int alphabet[] = getAlphabet();
		FATransitions states[] = getFATransitions();
		int statesLength = getTopStateIndex();

		for (int statei = 0; statei < statesLength; statei++) {
			FATransitions state = states[statei];
			for (int alphabeti = 0; alphabeti < alphabetsize; alphabeti++) {
				if (state.getSymbolTargetState(alphabet[alphabeti]) == -1) {
					if (sink == null) {
						sink = addSink();
					}
					addTransition(getStateName(statei), sink,
							alphabet[alphabeti]);
				}
			}
		}
	}

	private Marking addSink() {
		Marking sink = nonExistingMarking();

		addState(sink, false);
		int alphabetsize = getAlphabetSize();
		int alphabet[] = getAlphabet();
		for (int alphabeti = 0; alphabeti < alphabetsize; alphabeti++) {
			this.addTransition(sink, sink, alphabet[alphabeti]);
		}

		return sink;
	}

	/**
	 * Returns true if the automaton accepts a trace consisting of only the
	 * specified symbol.
	 * 
	 * @param symbol
	 *            a symbol from the alphabet
	 * @return true iff the trace 'symbol' is accepted by the automaton
	 */
	public boolean acceptsFromStart(int symbol) {
		int targetState = getFATransitions()[getInitialState()]
				.getSymbolTargetState(symbol);
		if (targetState == -1) {
			return false;
		} else {
			return acceptingStates().contains(targetState);
		}
	}
}
