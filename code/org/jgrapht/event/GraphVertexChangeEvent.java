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
/* ---------------------------
 * GraphVertexChangeEvent.java
 * ---------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: GraphVertexChangeEvent.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 10-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht.event;

/**
 * An event which indicates that a graph vertex has changed, or is about to
 * change. The event can be used either as an indication <i>after</i> the vertex
 * has been added or removed, or <i>before</i> it is added. The type of the
 * event can be tested using the
 * {@link org.jgrapht.event.GraphChangeEvent#getType()} method.
 * 
 * @author Barak Naveh
 * @since Aug 10, 2003
 */
public class GraphVertexChangeEvent<V> extends GraphChangeEvent {
	// ~ Static fields/initializers
	// ---------------------------------------------

	private static final long serialVersionUID = 3690189962679104053L;

	/**
	 * Before vertex added event. This event is fired before a vertex is added
	 * to a graph.
	 */
	public static final int BEFORE_VERTEX_ADDED = 11;

	/**
	 * Before vertex removed event. This event is fired before a vertex is
	 * removed from a graph.
	 */
	public static final int BEFORE_VERTEX_REMOVED = 12;

	/**
	 * Vertex added event. This event is fired after a vertex is added to a
	 * graph.
	 */
	public static final int VERTEX_ADDED = 13;

	/**
	 * Vertex removed event. This event is fired after a vertex is removed from
	 * a graph.
	 */
	public static final int VERTEX_REMOVED = 14;

	// ~ Instance fields
	// --------------------------------------------------------

	/**
	 * The vertex that this event is related to.
	 */
	protected V vertex;

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Creates a new GraphVertexChangeEvent object.
	 * 
	 * @param eventSource
	 *            the source of the event.
	 * @param type
	 *            the type of the event.
	 * @param vertex
	 *            the vertex that the event is related to.
	 */
	public GraphVertexChangeEvent(Object eventSource, int type, V vertex) {
		super(eventSource, type);
		this.vertex = vertex;
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Returns the vertex that this event is related to.
	 * 
	 * @return the vertex that this event is related to.
	 */
	public V getVertex() {
		return vertex;
	}
}

// End GraphVertexChangeEvent.java
