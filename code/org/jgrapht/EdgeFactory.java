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
/* ----------------
 * EdgeFactory.java
 * ----------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: EdgeFactory.java 645 2008-09-30 19:44:48Z perfecthash $
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht;

/**
 * An edge factory used by graphs for creating new edges.
 * 
 * @author Barak Naveh
 * @since Jul 14, 2003
 */
public interface EdgeFactory<V, E> {
	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Creates a new edge whose endpoints are the specified source and target
	 * vertices.
	 * 
	 * @param sourceVertex
	 *            the source vertex.
	 * @param targetVertex
	 *            the target vertex.
	 * 
	 * @return a new edge whose endpoints are the specified source and target
	 *         vertices.
	 */
	public E createEdge(V sourceVertex, V targetVertex);
}

// End EdgeFactory.java
