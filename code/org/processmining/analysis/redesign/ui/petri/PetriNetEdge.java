/*
 * Copyright (c) 2009 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.analysis.redesign.ui.petri;

import java.awt.Color;

import org.deckfour.gantzgraf.model.GGEdge;
import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.painter.GGEdgeGenericPainter;
import org.deckfour.gantzgraf.painter.GGEdgePainter;

import att.grappa.Edge;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class PetriNetEdge extends GGEdge {

	private Edge original;
	/**
	 * Drawing the edge
	 */
	private static GGEdgePainter painter = new GGEdgeGenericPainter(new Color(
			20, 20, 20), new Color(60, 60, 60));

	public PetriNetEdge(Edge original, GGNode source, GGNode target) {
		super(source, target, true, null, painter, 0.4f);
		this.original = original;
	}

	public Edge original() {
		return original;
	}

	public Object clone() {
		PetriNetEdge clone = (PetriNetEdge) super.clone();
		clone.original = original;
		return clone;
	}

}
