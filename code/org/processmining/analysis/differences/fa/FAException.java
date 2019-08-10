package org.processmining.analysis.differences.fa;

/**
 * The FAException defines the exception class used by the NFA and DFA finite
 * automaton classes.
 * 
 * @author Marcus Johansson, Remco Dijkman
 * @version 1.0
 */

public class FAException extends Exception {
	/**
	 * Specifies that an error occured due to that a state already existed in
	 * the automaton.
	 */
	public static final int STATE_EXISTS = 10;

	/**
	 * Specifies that an error occured due to that a state is missing in the
	 * automaton.
	 */
	public static final int MISSING_STATE = 20;

	/**
	 * Specifies that an error occured due to that a transition of the given
	 * symbol already exists (Used by DFA).
	 */
	public static final int TRANSITION_ON_SYMBOL_EXISTS = 30;

	/**
	 * Specifies that an error occured due to that accepting state(s) are not
	 * specified.
	 */
	public static final int MISSING_ACCEPTING_STATE = 40;

	/**
	 * Specifies that an error occured due to that an initial state is not
	 * specified.
	 */
	public static final int MISSING_INITIAL_STATE = 50;

	/**
	 * Specifies that an error occured due to that that transition already
	 * existed.
	 */
	public static final int TRANSITION_EXISTS = 60;

	private int code;

	/**
	 * Constructs a FAException with the given exception code.
	 * 
	 * @param code
	 *            The exception code.
	 * 
	 */
	public FAException(int code) {
		super();
		this.code = code;
	}

	/**
	 * Returns a string representation of the exception, with a description of
	 * the exception code.
	 * 
	 * @return A string representing the exception.
	 * 
	 */
	public String toString() {
		switch (code) {
		case 0:
			return "No error";
		case 10:
			return "State exists";
		case 20:
			return "Missing state";
		case 30:
			return "Transition on symbol exists";
		case 40:
			return "Missing accepting state";
		case 50:
			return "Missing initial state";
		case 60:
			return "Transition exists";
		default:
			return "Unknown code";
		}
	}
}