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
 * An extension of the Enumeration interface specific to enumerations of graph
 * elements.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public interface GraphEnumeration extends java.util.Enumeration {
	/**
	 * Get the root of this enumeration.
	 * 
	 * @return the root subgraph for this enumeration
	 */
	public Subgraph getSubgraphRoot();

	/**
	 * Get the types of elements possibly contained in this enumeration.
	 * 
	 * @return an indication of the types of elements in this enumeration
	 * @see GrappaConstants#NODE
	 * @see GrappaConstants#EDGE
	 * @see GrappaConstants#SUBGRAPH
	 */
	public int getEnumerationTypes();

	/**
	 * A convenience method that should just return a cast of a call to
	 * nextElement()
	 * 
	 * @return the next graph element in the enumeration
	 * @exception java.util.NoSuchElementException
	 *                whenever the enumeration has no more elements.
	 */
	public Element nextGraphElement() throws java.util.NoSuchElementException;
}
