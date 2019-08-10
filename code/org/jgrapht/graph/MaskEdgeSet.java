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
 * MaskEdgeSet.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 *
 * $Id: MaskEdgeSet.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 05-Jun-2007 : Initial revision (GB);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;
import org.jgrapht.util.PrefetchIterator.*;

/**
 * Helper for {@link MaskSubgraph}.
 * 
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
class MaskEdgeSet<V, E> extends AbstractSet<E> {
	// ~ Instance fields
	// --------------------------------------------------------

	private Set<E> edgeSet;

	private Graph<V, E> graph;

	private MaskFunctor<V, E> mask;

	private transient TypeUtil<E> edgeTypeDecl = null;

	private int size;

	// ~ Constructors
	// -----------------------------------------------------------

	public MaskEdgeSet(Graph<V, E> graph, Set<E> edgeSet, MaskFunctor<V, E> mask) {
		this.graph = graph;
		this.edgeSet = edgeSet;
		this.mask = mask;
		this.size = -1;
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return this.edgeSet.contains(o)
				&& !this.mask.isEdgeMasked(TypeUtil.uncheckedCast(o,
						edgeTypeDecl));
	}

	/**
	 * @see java.util.Set#iterator()
	 */
	public Iterator<E> iterator() {
		return new PrefetchIterator<E>(new MaskEdgeSetNextElementFunctor());
	}

	/**
	 * @see java.util.Set#size()
	 */
	public int size() {
		if (this.size == -1) {
			this.size = 0;
			for (Iterator<E> iter = iterator(); iter.hasNext();) {
				iter.next();
				this.size++;
			}
		}
		return this.size;
	}

	// ~ Inner Classes
	// ----------------------------------------------------------

	private class MaskEdgeSetNextElementFunctor implements
			NextElementFunctor<E> {
		private Iterator<E> iter;

		public MaskEdgeSetNextElementFunctor() {
			this.iter = MaskEdgeSet.this.edgeSet.iterator();
		}

		public E nextElement() throws NoSuchElementException {
			E edge = this.iter.next();
			while (isMasked(edge)) {
				edge = this.iter.next();
			}
			return edge;
		}

		private boolean isMasked(E edge) {
			return MaskEdgeSet.this.mask.isEdgeMasked(edge)
					|| MaskEdgeSet.this.mask
							.isVertexMasked(MaskEdgeSet.this.graph
									.getEdgeSource(edge))
					|| MaskEdgeSet.this.mask
							.isVertexMasked(MaskEdgeSet.this.graph
									.getEdgeTarget(edge));
		}
	}
}

// End MaskEdgeSet.java
