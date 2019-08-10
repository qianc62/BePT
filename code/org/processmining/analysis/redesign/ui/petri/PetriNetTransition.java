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
import org.processmining.framework.models.petrinet.Transition;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class PetriNetTransition extends GGNode {

	/**
	 * Drawing the transition
	 */
	// Reminder: Color(255,255,255) = white and Color(0,0,0) = black
	private static Color background = new Color(255, 255, 255);
	private static Color border = new Color(40, 50, 40);
	private static Color text = new Color(20, 20, 20);
	private static GGNodeShapePainter painter = new PetriNetTransitionPainter(
			background, border, text);

	private Transition original;

	/**
	 * NB. indicating whether a node is selected: isSelected(), is handled in
	 * super.
	 */

	/**
	 * Flag for indicating whether the selected node is included in a component.
	 */
	protected boolean isSelectedInComponent = false;

	/**
	 * Flag for indicating whether the node could be included in the same
	 * component as the selected node(s) is(are).
	 */
	protected boolean toBeSelectedInComponent = false;

	/**
	 * Flag for indicating that the selected node cannot be included in the same
	 * component as the selected node(s) is(are).
	 */
	protected boolean notToBeSelectedInComponent = false;

	public PetriNetTransition(Transition original) {
		super(original.getIdentifier().split("\\n"), painter);
		painter.setBorderWidth(2f);
		this.original = original;
	}

	public Transition original() {
		return original;
	}

	/**
	 * Probes, whether this node is currently selected and included in a
	 * component.
	 */
	public boolean isSelectedInComponent() {
		return isSelectedInComponent;
	}

	/**
	 * Sets whether this node is currently selected and included in a component.
	 * <p>
	 * Used by event listeners, not for end user consideration.
	 */
	public void setIsSelectedInComponent(boolean sel) {
		this.isSelectedInComponent = sel;
		updateView();
	}

	/**
	 * Probes, whether the node could be included in the same component as the
	 * selected node(s) is(are).
	 */
	public boolean toBeSelectedInComponent() {
		return toBeSelectedInComponent;
	}

	/**
	 * Sets whether the node could be included in the same component as the
	 * selected node(s) is(are).
	 * <p>
	 * Used by event listeners, not for end user consideration.
	 */
	public void setToBeSelectedInComponent(boolean sel) {
		this.toBeSelectedInComponent = sel;
		updateView();
	}

	/**
	 * Probes, whether the node cannot be included in the same component as the
	 * selected node(s) is(are).
	 */
	public boolean notToBeSelectedInComponent() {
		return notToBeSelectedInComponent;
	}

	/**
	 * Sets whether the node cannot be included in the same component as the
	 * selected node(s) is(are).
	 * <p>
	 * Used by event listeners, not for end user consideration.
	 */
	public void setNotToBeSelectedInComponent(boolean sel) {
		this.notToBeSelectedInComponent = sel;
		updateView();
	}

	public Object clone() {
		PetriNetTransition clone = (PetriNetTransition) super.clone();
		clone.original = original;
		return clone;
	}

}
