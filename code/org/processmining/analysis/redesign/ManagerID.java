package org.processmining.analysis.redesign;

/**
 * An ID manager for generating new IDs that can be used for creating unique
 * names for nodes
 * 
 * @author: Mariska Netjes
 */
public class ManagerID {

	/**
	 * A counter for saving which IDs already have been given.
	 */
	private static int myIDCounter = 0;

	private ManagerID() {
		// disallow instantiation - force usage of static members
	}

	/**
	 * Returns a new, unique, ID.
	 * 
	 * @return a string representation of a ID.
	 */
	public static String getNewID() {
		int result = myIDCounter;
		myIDCounter = myIDCounter + 1;
		return "" + result;
	}

	public static void reset() {
		myIDCounter = 0;
	}

}
