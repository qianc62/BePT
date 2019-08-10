/***********************************************************
 *      This software is part of the graphviz package      *
 *                http://www.graphviz.org/                 *
 *                                                         *
 *            Copyright (c) 1994-2004 AT&T Corp.           *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *                      by AT&T Corp.                      *
 *                                                         *
 *        Information and Software Systems Research        *
 *              AT&T Research, Florham Park NJ             *
 **********************************************************/

package att.grappa;

/**
 * This class is used whenever a problem is detected during parsing.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public class GraphParserException extends RuntimeException {
	/**
	 * Constructs an <code>GraphParserException</code> with no detail message.
	 */
	public GraphParserException() {
	}

	/**
	 * Constructs an <code>GraphParserException</code> with the specified detail
	 * message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public GraphParserException(String message) {
		super(message);
	}
}
