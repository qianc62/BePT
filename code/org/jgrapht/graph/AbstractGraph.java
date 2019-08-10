/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ------------------
 * AbstractGraph.java
 * ------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: AbstractGraph.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;

/**
 * A skeletal implementation of the <tt>Graph</tt> interface, to minimize the
 * effort required to implement graph interfaces. This implementation is
 * applicable to both: directed graphs and undirected graphs.
 * 
 * @author Barak Naveh
 * @see Graph
 * @see DirectedGraph
 * @see UndirectedGraph
 */
public abstract class AbstractGraph<V, E> implements Graph<V, E> {
	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Construct a new empty graph object.
	 */
	public AbstractGraph() {
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * @see Graph#containsEdge(Object, Object)
	 */
	public boolean containsEdge(V sourceVertex, V targetVertex) {
		return getEdge(sourceVertex, targetVertex) != null;
	}

	/**
	 * @see Graph#removeAllEdges(Collection)
	 */
	public boolean removeAllEdges(Collection<? extends E> edges) {
		boolean modified = false;

		for (E e : edges) {
			modified |= removeEdge(e);
		}

		return modified;
	}

	/**
	 * @see Graph#removeAllEdges(Object, Object)
	 */
	public Set<E> removeAllEdges(V sourceVertex, V targetVertex) {
		Set<E> removed = getAllEdges(sourceVertex, targetVertex);
		removeAllEdges(removed);

		return removed;
	}

	/**
	 * @see Graph#removeAllVertices(Collection)
	 */
	public boolean removeAllVertices(Collection<? extends V> vertices) {
		boolean modified = false;

		for (V v : vertices) {
			modified |= removeVertex(v);
		}

		return modified;
	}

	/**
	 * Returns a string of the parenthesized pair (V, E) representing this
	 * G=(V,E) graph. 'V' is the string representation of the vertex set, and
	 * 'E' is the string representation of the edge set.
	 * 
	 * @return a string representation of this graph.
	 */
	public String toString() {
		return toStringFromSets(vertexSet(), edgeSet(),
				(this instanceof DirectedGraph));
	}

	/**
	 * Ensures that the specified vertex exists in this graph, or else throws
	 * exception.
	 * 
	 * @param v
	 *            vertex
	 * 
	 * @return <code>true</code> if this assertion holds.
	 * 
	 * @throws NullPointerException
	 *             if specified vertex is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if specified vertex does not exist in this graph.
	 */
	protected boolean assertVertexExist(V v) {
		if (containsVertex(v)) {
			return true;
		} else if (v == null) {
			throw new NullPointerException();
		} else {
			throw new IllegalArgumentException("no such vertex in graph");
		}
	}

	/**
	 * Removes all the edges in this graph that are also contained in the
	 * specified edge array. After this call returns, this graph will contain no
	 * edges in common with the specified edges. This method will invoke the
	 * {@link Graph#removeEdge(Object)} method.
	 * 
	 * @param edges
	 *            edges to be removed from this graph.
	 * 
	 * @return <tt>true</tt> if this graph changed as a result of the call.
	 * 
	 * @see Graph#removeEdge(Object)
	 * @see Graph#containsEdge(Object)
	 */
	protected boolean removeAllEdges(E[] edges) {
		boolean modified = false;

		for (int i = 0; i < edges.length; i++) {
			modified |= removeEdge(edges[i]);
		}

		return modified;
	}

	/**
	 * Helper for subclass implementations of toString( ).
	 * 
	 * @param vertexSet
	 *            the vertex set V to be printed
	 * @param edgeSet
	 *            the edge set E to be printed
	 * @param directed
	 *            true to use parens for each edge (representing directed);
	 *            false to use curly braces (representing undirected)
	 * 
	 * @return a string representation of (V,E)
	 */
	protected String toStringFromSets(Collection<? extends V> vertexSet,
			Collection<? extends E> edgeSet, boolean directed) {
		List<String> renderedEdges = new ArrayList<String>();

		StringBuffer sb = new StringBuffer();
		for (E e : edgeSet) {
			if ((e.getClass() != DefaultEdge.class)
					&& (e.getClass() != DefaultWeightedEdge.class)) {
				sb.append(e.toString());
				sb.append("=");
			}
			if (directed) {
				sb.append("(");
			} else {
				sb.append("{");
			}
			sb.append(getEdgeSource(e));
			sb.append(",");
			sb.append(getEdgeTarget(e));
			if (directed) {
				sb.append(")");
			} else {
				sb.append("}");
			}

			// REVIEW jvs 29-May-2006: dump weight somewhere?
			renderedEdges.add(sb.toString());
			sb.setLength(0);
		}

		return "(" + vertexSet + ", " + renderedEdges + ")";
	}
}

// End AbstractGraph.java
