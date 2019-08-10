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
 * ChromaticNumber.java
 * -------------------
 * (C) Copyright 2008-2008, by Andrew Newell and Contributors.
 *
 * Original Author:  Andrew Newell
 * Contributor(s):   -
 *
 * $Id: ChromaticNumber.java 680 2009-05-25 05:55:31Z perfecthash $
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

/**
 * Allows the <a href="http://mathworld.wolfram.com/ChromaticNumber.html">
 * chromatic number</a> of a graph to be calculated. This is the minimal number
 * of colors needed to color each vertex such that no two adjacent vertices
 * share the same color. This algorithm will not find the true chromatic number,
 * since this is an NP-complete problem. So, a greedy algorithm will find an
 * approximate chromatic number.
 * 
 * @author Andrew Newell
 * @since Dec 21, 2008
 */
public abstract class ChromaticNumber {
	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Finds the number of colors required for a greedy coloring of the graph.
	 * 
	 * @param g
	 *            an undirected graph to find the chromatic number of
	 * 
	 * @return integer the approximate chromatic number from the greedy
	 *         algorithm
	 */
	public static <V, E> int findGreedyChromaticNumber(UndirectedGraph<V, E> g) {
		// A copy of the graph is made, so that elements of the graph may be
		// removed to carry out the algorithm
		UndirectedGraph<V, E> sg = new UndirectedSubgraph<V, E>(g, null, null);

		// The Vertices will be sorted in decreasing order by degree, so that
		// higher degree vertices have priority to be colored first
		VertexDegreeComparator<V, E> comp = new VertexDegreeComparator<V, E>(sg);
		List<V> sortedVertices = new LinkedList<V>(sg.vertexSet());
		Collections.sort(sortedVertices, comp);
		Collections.reverse(sortedVertices);

		int color;

		// Each vertex will attempted to be colored with a single color each
		// iteration, and these vertices will be removed from the graph at the
		// end of each iteration
		for (color = 0; sg.vertexSet().size() > 0; color++) {
			// This set will contain vertices that are colored with the
			// current color of this iteration
			Set<V> currentColor = new HashSet<V>();
			for (Iterator<V> iter = sortedVertices.iterator(); iter.hasNext();) {
				V v = iter.next();

				// Add new vertices to be colored as long as they are not
				// adjacent with any other vertex that has already been colored
				// with the current color
				boolean flag = true;
				for (Iterator<V> innerIter = currentColor.iterator(); innerIter
						.hasNext();) {
					V temp = innerIter.next();
					if (sg.containsEdge(temp, v)) {
						flag = false;
						break;
					}
				}
				if (flag) {
					currentColor.add(v);
				}
			}

			// Remove vertices from the graph and then repeat the process for
			// the next iteration
			sg.removeAllVertices(currentColor);
		}
		return color;
	}
}

// End ChromaticNumber.java
