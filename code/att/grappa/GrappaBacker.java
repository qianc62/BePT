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
 * An interface for defining an image drawing method to be used for painting the
 * background of the graph.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public interface GrappaBacker {
	/**
	 * The method for drawing the background image.
	 * 
	 * @param g2d
	 *            the graphics context.
	 * @param graph
	 *            the graph being drawn in the foreground.
	 * @param bbox
	 *            the bounding box of the graph.
	 * @param clip
	 *            the clipping shape defining the limits of the area to be
	 *            drawn.
	 */
	public void drawBackground(java.awt.Graphics2D g2d, Graph graph,
			java.awt.geom.Rectangle2D bbox, java.awt.Shape clip);
}
