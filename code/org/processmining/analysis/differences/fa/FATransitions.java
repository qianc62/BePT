package org.processmining.analysis.differences.fa;

/**
 * The FATransitions class represents a list of transitions from a given state.
 * 
 * @author Marcus Johansson, Remco Dijkman
 * @version 1.0
 */

public class FATransitions {
	private class Transition {
		private int state;
		private int symbol;
		private Transition nextTransition;

		public int getTransitionState() {
			return state;
		}

		public int getTransitionSymbol() {
			return symbol;
		}

		public void setTransitionSymbol(int symbol) {
			this.symbol = symbol;
		}

		public Transition nextTransition() {
			return nextTransition;
		}

		public void setNextTransition(Transition t) {
			nextTransition = t;
		}

		public Transition(int state, int symbol, Transition nextTransition) {
			this.state = state;
			this.symbol = symbol;
			this.nextTransition = nextTransition;
		}
	}

	private Transition root;
	private Transition top;

	private int currSize;

	/**
	 * Constructs an empty Transition set.
	 * 
	 */
	public FATransitions() {
		root = top = null;
		currSize = 0;
	}

	/**
	 * Checks whether a given transition (targetstate, symbol-pair) is contained
	 * in the set.
	 * 
	 * @param state
	 *            The target state of the transition to look for.
	 * @param symbol
	 *            The symbol of the transition to look for.
	 * @return true if the transition is contained within the list, otherwise
	 *         false.
	 * 
	 */
	public boolean contains(int state, int symbol) {
		Transition next = root;

		while (next != null) {
			if (next.getTransitionState() == state
					&& next.getTransitionSymbol() == symbol) {
				return true;
			}

			next = next.nextTransition();
		}

		return false;
	}

	/**
	 * Adds a transition to the list.
	 * 
	 * @param targetState
	 *            The target state of the transition.
	 * @param symbol
	 *            The symbol of the transition.
	 * @param allowNonDeterministic
	 *            Indicates whether non deterministic transitions are allowed.
	 * @return false iff the given transition already exists in the list, or a
	 *         violation of the integrity of the automaton occurs.
	 * 
	 */
	public boolean addTransition(int targetState, int sym,
			boolean allowNonDeterministic) {
		if (contains(targetState, sym)) {
			return false;
		}

		if (!allowNonDeterministic && getSymbolTargetState(sym) != -1) {
			return false;
		}

		if (root == null && top == null) {
			root = new Transition(targetState, sym, null);
			top = root;
			currSize++;
		} else {
			top.setNextTransition(new Transition(targetState, sym, null));
			top = top.nextTransition();
			currSize++;
		}
		return true;
	}

	/**
	 * Replaces transitions with symbol sym by transitions with symbol
	 * replacement Does nothing if a transition already exists or if an addition
	 * would violate deterministic constraints
	 * 
	 * @param sym
	 *            symbol to replace
	 * @param replacement
	 *            symbol to replace by
	 * @param allowNonDeterministic
	 *            true iff determinism must be maintained
	 */
	public void replaceSymbol(int sym, int replacement,
			boolean allowNonDeterministic) {
		if (allowNonDeterministic) {
			int[] targetStates = getSymbolTargetStates(sym).getArray();
			for (int i = 0; i < targetStates.length; i++) {
				removeTransition(targetStates[i], sym);
				this.addTransition(targetStates[i], replacement,
						allowNonDeterministic);
			}
		} else {
			int targetState = getSymbolTargetState(sym);
			if (targetState != -1) {
				removeTransition(targetState, sym);
				addTransition(targetState, replacement, allowNonDeterministic);
			}
		}
	}

	/**
	 * Removes all transitions to a particular state.
	 * 
	 * @param state
	 *            The target state to which transitions should be removed.
	 * 
	 */
	public void removeTransitionsToState(int state) {
		Transition next = root;
		Transition prev = null;
		boolean removed = false;

		while (next != null) {
			if (next.getTransitionState() == state) {
				if (prev == null) {
					if (top == root) {
						top = next.nextTransition();
					}
					root = next.nextTransition();
				} else {
					if (next == top) {
						top = prev;
					}

					prev.setNextTransition(next.nextTransition());
				}
				currSize--;
				removed = true;
				break;
			}

			prev = next;
			next = next.nextTransition();
		}

		if (removed) {
			removeTransitionsToState(state);
		}
	}

	/**
	 * Removes a given transition from the list.
	 * 
	 * @param state
	 *            The target state of the transition to remove.
	 * @param symbol
	 *            The symbol of the transition to remove.
	 * 
	 */
	public void removeTransition(int state, int sym) {
		Transition next = root;
		Transition prev = null;

		while (next != null) {
			if (next.getTransitionSymbol() == sym
					&& next.getTransitionState() == state) {
				if (prev == null) {
					if (top == root) {
						top = next.nextTransition();
					}
					root = next.nextTransition();
				} else {

					if (next == top) {
						top = prev;
					}

					prev.setNextTransition(next.nextTransition());
				}
				currSize--;
				break;
			}

			prev = next;
			next = next.nextTransition();
		}
	}

	/**
	 * Returns the target state of the a symbol. This should only be used if the
	 * transition list i deterministic.
	 * 
	 * @param sym
	 *            The symbol which associated target state should be returned.
	 * @return The target state of the given symbol.
	 * 
	 */
	public int getSymbolTargetState(int sym) {
		Transition next = root;

		while (next != null) {
			if (next.getTransitionSymbol() == sym) {
				return next.getTransitionState();
			}

			next = next.nextTransition();
		}

		return -1;
	}

	/**
	 * Returns the states reachable through a given symbol. Note that this
	 * should only be used when the transitions are non-deterministic. Otherwise
	 * the set of states will contain at most one element, and the
	 * getSymbolTargetState(int sym)-method should be used instead.
	 * 
	 * @param sym
	 *            The symbol.
	 * @return The set of states reachable through transitions on the given
	 *         symbol.
	 * 
	 */
	public IntegerSet getSymbolTargetStates(int sym) {
		IntegerSet epsStates = new IntegerSet();

		Transition next = root;

		while (next != null) {
			if (next.getTransitionSymbol() == sym) {
				epsStates.add(next.getTransitionState());
			}

			next = next.nextTransition();
		}

		return epsStates;
	}

	/**
	 * Returns an array representation of the symbols in the list.
	 * 
	 * @return Array containing the symbols of the transition list.
	 * 
	 */
	public int[] getSymbolArray() {
		int[] symbols = new int[currSize];
		Transition next = root;

		int i = 0;
		while (next != null) {
			symbols[i++] = next.getTransitionSymbol();

			next = next.nextTransition();
		}

		return symbols;
	}

	/**
	 * Returns an array representation of the target states in the list.
	 * 
	 * @return Array of integers containing the target states of the transition
	 *         list.
	 * 
	 */
	public int[] getTargetArray() {
		int[] targets = new int[currSize];
		Transition next = root;

		int i = 0;
		while (next != null) {
			targets[i++] = next.getTransitionState();

			next = next.nextTransition();
		}

		return targets;
	}

	/**
	 * Returns the number of transitions in the list.
	 * 
	 * @return The number of transitions in the list.
	 * 
	 */
	public int getSize() {
		return currSize;
	}

	/**
	 * Returns a string representation of the transition list.
	 * 
	 * @return String respresenting the transition list.
	 * 
	 */
	public String toString() {
		String str = "{";

		Transition next = root;

		while (next != null) {
			if (next != root) {
				str += ", ";
			}

			str += (next.getTransitionSymbol() + "|" + next
					.getTransitionState());
			next = next.nextTransition();
		}

		return str + "}";
	}
}
