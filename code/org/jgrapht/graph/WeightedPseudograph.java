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
/* ------------------------
 * WeightedPseudograph.java
 * ------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: WeightedPseudograph.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 05-Aug-2003 : Initial revision (BN);
 * 06-Aug-2005 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;

/**
 * A weighted pseudograph. A weighted pseudograph is a non-simple undirected
 * graph in which both graph loops and multiple edges are permitted. The edges
 * of a weighted pseudograph have weights. If you're unsure about pseudographs,
 * see: <a href="http://mathworld.wolfram.com/Pseudograph.html">
 * http://mathworld.wolfram.com/Pseudograph.html</a>.
 */
public class WeightedPseudograph<V, E> extends Pseudograph<V, E> implements
		WeightedGraph<V, E> {
	// ~ Static fields/initializers
	// ---------------------------------------------

	private static final long serialVersionUID = 3257290244524356152L;

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Creates a new weighted pseudograph with the specified edge factory.
	 * 
	 * @param ef
	 *            the edge factory of the new graph.
	 */
	public WeightedPseudograph(EdgeFactory<V, E> ef) {
		super(ef);
	}

	/**
	 * Creates a new weighted pseudograph.
	 * 
	 * @param edgeClass
	 *            class on which to base factory for edges
	 */
	public WeightedPseudograph(Class<? extends E> edgeClass) {
		this(new ClassBasedEdgeFactory<V, E>(edgeClass));
	}
}

// End WeightedPseudograph.java
