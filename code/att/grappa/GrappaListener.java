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
 * An interface for handling mouse-related activity that occurs on a graph.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public interface GrappaListener {
	/**
	 * The method called when a single mouse click occurs on a displayed
	 * subgraph.
	 * 
	 * @param subg
	 *            displayed subgraph where action occurred
	 * @param elem
	 *            subgraph element in which action occurred
	 * @param pt
	 *            the point where the action occurred (graph coordinates)
	 * @param modifiers
	 *            mouse modifiers in effect
	 * @param panel
	 *            specific panel where the action occurred
	 */
	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel);

	/**
	 * The method called when a mouse press occurs on a displayed subgraph.
	 * 
	 * @param subg
	 *            displayed subgraph where action occurred
	 * @param elem
	 *            subgraph element in which action occurred
	 * @param pt
	 *            the point where the action occurred (graph coordinates)
	 * @param modifiers
	 *            mouse modifiers in effect
	 * @param panel
	 *            specific panel where the action occurred
	 */
	public void grappaPressed(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, GrappaPanel panel);

	/**
	 * The method called when a mouse release occurs on a displayed subgraph.
	 * 
	 * @param subg
	 *            displayed subgraph where action occurred
	 * @param elem
	 *            subgraph element in which action occurred
	 * @param pt
	 *            the point where the action occurred (graph coordinates)
	 * @param modifiers
	 *            mouse modifiers in effect
	 * @param pressedElem
	 *            subgraph element in which the most recent mouse press occurred
	 * @param pressedPt
	 *            the point where the most recent mouse press occurred (graph
	 *            coordinates)
	 * @param pressedModifiers
	 *            mouse modifiers in effect when the most recent mouse press
	 *            occurred
	 * @param outline
	 *            enclosing box specification from the previous drag position
	 *            (for XOR reset purposes)
	 * @param panel
	 *            specific panel where the action occurred
	 */
	public void grappaReleased(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, Element pressedElem, GrappaPoint pressedPt,
			int pressedModifiers, GrappaBox outline, GrappaPanel panel);

	/**
	 * The method called when a mouse drag occurs on a displayed subgraph.
	 * 
	 * @param subg
	 *            displayed subgraph where action occurred
	 * @param currentPt
	 *            the current drag point
	 * @param currentModifiers
	 *            the current drag mouse modifiers
	 * @param pressedElem
	 *            subgraph element in which the most recent mouse press occurred
	 * @param pressedPt
	 *            the point where the most recent mouse press occurred (graph
	 *            coordinates)
	 * @param pressedModifiers
	 *            mouse modifiers in effect when the most recent mouse press
	 *            occurred
	 * @param outline
	 *            enclosing box specification from the previous drag position
	 *            (for XOR reset purposes)
	 * @param panel
	 *            specific panel where the action occurred
	 */
	public void grappaDragged(Subgraph subg, GrappaPoint currentPt,
			int currentModifiers, Element pressedElem, GrappaPoint pressedPt,
			int pressedModifiers, GrappaBox outline, GrappaPanel panel);

	/**
	 * The method called when a element tooltip is needed.
	 * 
	 * @param subg
	 *            displayed subgraph where action occurred
	 * @param elem
	 *            subgraph element in which action occurred
	 * @param pt
	 *            the point where the action occurred (graph coordinates)
	 * @param modifiers
	 *            mouse modifiers in effect
	 * @param panel
	 *            specific panel where the action occurred
	 * 
	 * @return the tip to be displayed or null
	 */
	public String grappaTip(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, GrappaPanel panel);
}
