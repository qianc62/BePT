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

package org.processmining.framework.models;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.GrappaBacker;
import att.grappa.GrappaConstants;
import att.grappa.GrappaPanel;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class ModelGraphPanel extends GrappaPanel {

	private Object originalObject;

	/**
	 * Constructs a new canvas associated with a particular subgraph. Keep in
	 * mind that Graph is a sub-class of Subgraph so that usually a Graph object
	 * is passed to the constructor.
	 * 
	 * @param subgraph
	 *            the subgraph to be rendered on the canvas
	 */
	public ModelGraphPanel(Subgraph subgraph, ModelGraph original) {
		this(subgraph, null, original);
	}

	/**
	 * Constructs a new canvas associated with a particular subgraph.
	 * 
	 * @param subgraph
	 *            the subgraph to be rendered on the canvas.
	 * @param backer
	 *            used to draw a background for the graph.
	 */
	public ModelGraphPanel(Subgraph subgraph, GrappaBacker backer,
			ModelGraph original) {
		super(subgraph, backer);
		this.originalObject = original;
	}

	public void clearModelGraphPanel() {
		this.originalObject = null;
	}

	public void unSelectAll() {
		unSelectAll(getSubgraph());
		validate();
	}

	private void unSelectAll(Subgraph g) {
		Enumeration e = g.nodeElements();
		while (e.hasMoreElements()) {
			((Element) e.nextElement()).highlight = GrappaConstants._NO_TYPE;
		}
		e = g.edgeElements();
		while (e.hasMoreElements()) {
			((Element) e.nextElement()).highlight = GrappaConstants._NO_TYPE;
		}
		e = g.subgraphElements();
		while (e.hasMoreElements()) {
			unSelectAll((Subgraph) e.nextElement());
		}
	}

	public void selectElements(Collection a) {
		Iterator it = a.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof ModelGraphVertex) {
				Node n = ((ModelGraphVertex) o).visualObject;
				if (n != null) {
					n.highlight = GrappaConstants.SELECTION_MASK;
				}
			}
			if (o instanceof ModelGraphEdge) {
				Edge e = ((ModelGraphEdge) o).visualObject;
				if (e != null) {
					e.highlight = GrappaConstants.SELECTION_MASK;
				}
			}
		}
		repaint();
		validate();
	}

	public HashSet selectEdges(boolean oneEnd) {
		HashSet s = new HashSet();
		selectEdges(getSubgraph(), s, oneEnd);
		repaint();
		validate();
		return s;
	}

	private void selectEdges(Subgraph g, HashSet selectedObjects, boolean oneEnd) {
		Enumeration e = g.edgeElements();
		while (e.hasMoreElements()) {
			Edge edge = (Edge) e.nextElement();
			if ((edge.getHead().highlight == GrappaConstants.SELECTION_MASK)
					&& (edge.getTail().highlight == GrappaConstants.SELECTION_MASK)) {
				if (edge != null) {
					edge.highlight = GrappaConstants.SELECTION_MASK;
					selectedObjects.add(edge.object);
				}
			} else if (oneEnd
					&& ((edge.getHead().highlight == GrappaConstants.SELECTION_MASK) || (edge
							.getTail().highlight == GrappaConstants.SELECTION_MASK))) {
				if (edge != null) {
					edge.highlight = GrappaConstants.SELECTION_MASK;
					selectedObjects.add(edge.object);
				}
			}

		}
		e = g.subgraphElements();
		while (e.hasMoreElements()) {
			unSelectAll((Subgraph) e.nextElement());
		}
	}

	public void setOriginalObject(Object o) {
		originalObject = o;
	}

	public Object getOriginalObject() {
		return originalObject;
	}

}
