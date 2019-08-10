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
/* -----------------------
 * EdgeTraversalEvent.java
 * -----------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: EdgeTraversalEvent.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 11-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht.event;

import java.util.*;

/**
 * A traversal event for a graph edge.
 * 
 * @author Barak Naveh
 * @since Aug 11, 2003
 */
public class EdgeTraversalEvent<V, E> extends EventObject {
	// ~ Static fields/initializers
	// ---------------------------------------------

	private static final long serialVersionUID = 4050768173789820979L;

	// ~ Instance fields
	// --------------------------------------------------------

	/**
	 * The traversed edge.
	 */
	protected E edge;

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Creates a new EdgeTraversalEvent.
	 * 
	 * @param eventSource
	 *            the source of the event.
	 * @param edge
	 *            the traversed edge.
	 */
	public EdgeTraversalEvent(Object eventSource, E edge) {
		super(eventSource);
		this.edge = edge;
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Returns the traversed edge.
	 * 
	 * @return the traversed edge.
	 */
	public E getEdge() {
		return edge;
	}
}

// End EdgeTraversalEvent.java
