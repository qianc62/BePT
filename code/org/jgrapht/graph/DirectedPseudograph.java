/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (barak_naveh@users.sourceforge.net)
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
/* ----------------
 * DirectedPseudograph.java
 * ----------------
 * (C) Copyright 2004-2008, by Christian Hammer and Contributors.
 *
 * Original Author:  Christian Hammer
 * Contributor(s):   -
 *
 * $Id: DirectedPseudograph.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 11-Mar-2004 : Initial revision: generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;

/**
 * A directed pseudograph. A directed pseudograph is a non-simple directed graph
 * in which both graph loops and multiple edges are permitted. If you're unsure
 * about pseudographs, see: <a
 * href="http://mathworld.wolfram.com/Pseudograph.html">
 * http://mathworld.wolfram.com/Pseudograph.html</a>.
 */
public class DirectedPseudograph<V, E> extends AbstractBaseGraph<V, E>
		implements DirectedGraph<V, E> {
	// ~ Static fields/initializers
	// ---------------------------------------------

	private static final long serialVersionUID = -8300409752893486415L;

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * @see AbstractBaseGraph
	 */
	public DirectedPseudograph(Class<? extends E> edgeClass) {
		this(new ClassBasedEdgeFactory<V, E>(edgeClass));
	}

	/**
	 * @see AbstractBaseGraph
	 */
	public DirectedPseudograph(EdgeFactory<V, E> ef) {
		super(ef, true, true);
	}
}

// End DirectedPseudograph.java
