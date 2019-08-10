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
/* -------------------
 * StarGraphGenerator.java
 * -------------------
 * (C) Copyright 2008-2008, by Andrew Newell and Contributors.
 *
 * Original Author:  Andrew Newell
 * Contributor(s):   -
 *
 * $Id: StarGraphGenerator.java 651 2008-12-24 21:13:41Z perfecthash $
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.generate;

import java.util.*;

import org.jgrapht.*;

/**
 * Generates a <a href="http://mathworld.wolfram.com/StarGraph.html">star
 * graph</a> of any size. This is a graph where every vertex has exactly one
 * edge with a center vertex.
 * 
 * @author Andrew Newell
 * @since Dec 21, 2008
 */
public class StarGraphGenerator<V, E> implements GraphGenerator<V, E, V> {
	// ~ Static fields/initializers
	// ---------------------------------------------

	public static final String CENTER_VERTEX = "Center Vertex";

	// ~ Instance fields
	// --------------------------------------------------------

	private int order;

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Creates a new StarGraphGenerator object.
	 * 
	 * @param order
	 *            number of total vertices including the center vertex
	 */
	public StarGraphGenerator(int order) {
		this.order = order;
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Generates a star graph with the designated order from the constructor
	 */
	public void generateGraph(Graph<V, E> target,
			final VertexFactory<V> vertexFactory, Map<String, V> resultMap) {
		if (order < 1) {
			return;
		}

		// Create center vertex
		V centerVertex = vertexFactory.createVertex();
		target.addVertex(centerVertex);
		if (resultMap != null) {
			resultMap.put(CENTER_VERTEX, centerVertex);
		}

		// Create other vertices
		for (int i = 0; i < (order - 1); i++) {
			V newVertex = vertexFactory.createVertex();
			target.addVertex(newVertex);
		}

		// Add one edge between the center vertex and every other vertex
		Iterator<V> iter = target.vertexSet().iterator();
		while (iter.hasNext()) {
			V v = iter.next();
			if (v != centerVertex) {
				target.addEdge(v, centerVertex);
			}
		}
	}
}

// End StarGraphGenerator.java
