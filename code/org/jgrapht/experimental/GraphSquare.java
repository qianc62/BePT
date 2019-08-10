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
/* ----------------------
 * GraphSquare.java
 * ----------------------
 * (C) Copyright 2004-2008, by Michael Behrisch and Contributors.
 *
 * Original Author:  Michael Behrisch
 * Contributor(s):   -
 *
 * $Id: GraphSquare.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 14-Sep-2004 : Initial revision (MB);
 *
 */
package org.jgrapht.experimental;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.event.*;
import org.jgrapht.graph.*;

/**
 * DOCUMENT ME!
 * 
 * @author Michael Behrisch
 * @since Sep 14, 2004
 */
public class GraphSquare<V, E> extends AbstractBaseGraph<V, E> {
	// ~ Static fields/initializers
	// ---------------------------------------------

	private static final long serialVersionUID = -2642034600395594304L;
	private static final String UNMODIFIABLE = "this graph is unmodifiable";

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Constructor for GraphSquare.
	 * 
	 * @param g
	 *            the graph of which a square is to be created.
	 * @param createLoops
	 */
	public GraphSquare(final Graph<V, E> g, final boolean createLoops) {
		super(g.getEdgeFactory(), false, createLoops);
		Graphs.addAllVertices(this, g.vertexSet());
		addSquareEdges(g, createLoops);

		if (g instanceof ListenableGraph) {
			((ListenableGraph<V, E>) g)
					.addGraphListener(new GraphListener<V, E>() {
						public void edgeAdded(GraphEdgeChangeEvent<V, E> e) {
							E edge = e.getEdge();
							addEdgesStartingAt(g, g.getEdgeSource(edge), g
									.getEdgeTarget(edge), createLoops);
							addEdgesStartingAt(g, g.getEdgeTarget(edge), g
									.getEdgeSource(edge), createLoops);
						}

						public void edgeRemoved(GraphEdgeChangeEvent<V, E> e) { // this
																				// is
																				// not
																				// a
																				// very
																				// performant
																				// implementation
							GraphSquare.super.removeAllEdges(edgeSet());
							addSquareEdges(g, createLoops);
						}

						public void vertexAdded(GraphVertexChangeEvent<V> e) {
						}

						public void vertexRemoved(GraphVertexChangeEvent<V> e) {
						}
					});
		}
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * @see Graph#addEdge(Object, Object)
	 */
	public E addEdge(V sourceVertex, V targetVertex) {
		throw new UnsupportedOperationException(UNMODIFIABLE);
	}

	/**
	 * @see Graph#addEdge(Object, Object, E)
	 */
	public boolean addEdge(V sourceVertex, V targetVertex, E e) {
		throw new UnsupportedOperationException(UNMODIFIABLE);
	}

	/**
	 * @see Graph#addVertex(Object)
	 */
	public boolean addVertex(V v) {
		throw new UnsupportedOperationException(UNMODIFIABLE);
	}

	/**
	 * @see Graph#removeAllEdges(Collection)
	 */
	public boolean removeAllEdges(Collection<? extends E> edges) {
		throw new UnsupportedOperationException(UNMODIFIABLE);
	}

	/**
	 * @see Graph#removeAllEdges(V, V)
	 */
	public Set<E> removeAllEdges(V sourceVertex, V targetVertex) {
		throw new UnsupportedOperationException(UNMODIFIABLE);
	}

	/**
	 * @see Graph#removeAllVertices(Collection)
	 */
	public boolean removeAllVertices(Collection<? extends V> vertices) {
		throw new UnsupportedOperationException(UNMODIFIABLE);
	}

	/**
	 * @see Graph#removeEdge(E)
	 */
	public boolean removeEdge(E e) {
		throw new UnsupportedOperationException(UNMODIFIABLE);
	}

	/**
	 * @see Graph#removeEdge(V, V)
	 */
	public E removeEdge(V sourceVertex, V targetVertex) {
		throw new UnsupportedOperationException(UNMODIFIABLE);
	}

	/**
	 * @see Graph#removeVertex(V)
	 */
	public boolean removeVertex(V v) {
		throw new UnsupportedOperationException(UNMODIFIABLE);
	}

	private void addEdgesStartingAt(final Graph<V, E> g, final V v, final V u,
			boolean createLoops) {
		if (!g.containsEdge(v, u)) {
			return;
		}

		final List<V> adjVertices = Graphs.neighborListOf(g, u);

		for (int i = 0; i < adjVertices.size(); i++) {
			final V w = adjVertices.get(i);

			if (g.containsEdge(u, w) && ((v != w) || createLoops)) {
				super.addEdge(v, w);
			}
		}
	}

	private void addSquareEdges(Graph<V, E> g, boolean createLoops) {
		for (V v : g.vertexSet()) {
			List<V> adjVertices = Graphs.neighborListOf(g, v);

			for (int i = 0; i < adjVertices.size(); i++) {
				addEdgesStartingAt(g, v, adjVertices.get(i), createLoops);
			}
		}
	}
}

// End GraphSquare.java
