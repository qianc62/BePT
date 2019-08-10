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
 * GraphEdgeChangeEvent.java
 * -------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: GraphEdgeChangeEvent.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 10-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht.event;

/**
 * An event which indicates that a graph edge has changed, or is about to
 * change. The event can be used either as an indication <i>after</i> the edge
 * has been added or removed, or <i>before</i> it is added. The type of the
 * event can be tested using the
 * {@link org.jgrapht.event.GraphChangeEvent#getType()} method.
 * 
 * @author Barak Naveh
 * @since Aug 10, 2003
 */
public class GraphEdgeChangeEvent<V, E> extends GraphChangeEvent {
	// ~ Static fields/initializers
	// ---------------------------------------------

	private static final long serialVersionUID = 3618134563335844662L;

	/**
	 * Before edge added event. This event is fired before an edge is added to a
	 * graph.
	 */
	public static final int BEFORE_EDGE_ADDED = 21;

	/**
	 * Before edge removed event. This event is fired before an edge is removed
	 * from a graph.
	 */
	public static final int BEFORE_EDGE_REMOVED = 22;

	/**
	 * Edge added event. This event is fired after an edge is added to a graph.
	 */
	public static final int EDGE_ADDED = 23;

	/**
	 * Edge removed event. This event is fired after an edge is removed from a
	 * graph.
	 */
	public static final int EDGE_REMOVED = 24;

	// ~ Instance fields
	// --------------------------------------------------------

	/**
	 * The edge that this event is related to.
	 */
	protected E edge;

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Constructor for GraphEdgeChangeEvent.
	 * 
	 * @param eventSource
	 *            the source of this event.
	 * @param type
	 *            the event type of this event.
	 * @param e
	 *            the edge that this event is related to.
	 */
	public GraphEdgeChangeEvent(Object eventSource, int type, E e) {
		super(eventSource, type);
		edge = e;
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Returns the edge that this event is related to.
	 * 
	 * @return the edge that this event is related to.
	 */
	public E getEdge() {
		return edge;
	}
}

// End GraphEdgeChangeEvent.java
