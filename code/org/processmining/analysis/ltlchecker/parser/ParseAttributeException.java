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

package org.processmining.analysis.ltlchecker.parser;

/**
 * ParseAttributeException is generated when a string value of an attribute is
 * parsed to the type of the attribute and there is going something wrong with
 * the parsing.
 *
 * @version 0.1
 * @author HT de Beer
 */
public class ParseAttributeException extends Exception {

	public ParseAttributeException(String stringValue, Attribute attr) {
		super(
				"Error by parsing '" + stringValue + "' as a " +
				attr.toString() + ".");
	}

}
