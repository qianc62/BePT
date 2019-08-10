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

package org.processmining.analysis.petrinet.cpnexport;

/**
 * An ID manager for generating new cpnIDs that can be used for creating a valid
 * CPN file.
 * 
 * @author Ronny Mans
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 * @version 1.0
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
	 * Returns a new, unique, cpnID.
	 * 
	 * @return a string representation of a cpn-ID.
	 */
	public static String getNewID() {
		int result = myIDCounter;
		myIDCounter = myIDCounter + 1;
		return "ID" + result;
	}

	public static void reset() {
		myIDCounter = 0;
	}

}
