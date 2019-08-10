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
/* -------------------------
 * DefaultDirectedGraph.java
 * -------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: DefaultDirectedGraph.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 05-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;

/**
 * A directed graph. A default directed graph is a non-simple directed graph in
 * which multiple edges between any two vertices are <i>not</i> permitted, but
 * loops are.
 * 
 * <p>
 * prefixed 'Default' to avoid name collision with the DirectedGraph interface.
 * </p>
 */
public class DefaultDirectedGraph<V, E> extends AbstractBaseGraph<V, E>
		implements DirectedGraph<V, E> {
	// ~ Static fields/initializers
	// ---------------------------------------------

	private static final long serialVersionUID = 3544953246956466230L;

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Creates a new directed graph.
	 * 
	 * @param edgeClass
	 *            class on which to base factory for edges
	 */
	public DefaultDirectedGraph(Class<? extends E> edgeClass) {
		this(new ClassBasedEdgeFactory<V, E>(edgeClass));
	}

	/**
	 * Creates a new directed graph with the specified edge factory.
	 * 
	 * @param ef
	 *            the edge factory of the new graph.
	 */
	public DefaultDirectedGraph(EdgeFactory<V, E> ef) {
		super(ef, false, true);
	}
}

// End DefaultDirectedGraph.java
