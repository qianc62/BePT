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

import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.painter.GGNodeShapePainter;
import org.processmining.framework.models.petrinet.Place;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class PetriNetPlace extends GGNode {
	/**
	 * Drawing the place
	 */
	private static Color background = new Color(250, 250, 250);
	private static Color border = new Color(20, 20, 20);
	private static Color text = new Color(10, 10, 10);

	private static GGNodeShapePainter painter = new PetriNetPlacePainter(
			background, border, text);

	private Place original;

	public PetriNetPlace(Place original) {
		super(new String[] { "" }, painter);
		painter.setBorderWidth(2f);
		this.original = original;
	}

	public Place original() {
		return original;
	}

	public Object clone() {
		PetriNetPlace clone = (PetriNetPlace) super.clone();
		clone.original = original;
		return clone;
	}

}
