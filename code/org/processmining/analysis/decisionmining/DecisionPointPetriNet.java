/**
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Copyright (c) 2003-2006 TU/e Eindhoven
 *    by Eindhoven University of Technology
 *    Department of Information Systems
 *    http://is.tm.tue.nl
 *
 ************************************************************************/

package org.processmining.analysis.decisionmining;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;

/**
 * A decision point that works with Petri net models.
 * 
 * @see DecisionPointContextPetriNet
 * @see DecisionPointBuilderPetriNet
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public class DecisionPointPetriNet extends DecisionPoint {

	/** The associated place in the Petri net model */
	private Place myPlace;

	/** The associated place in the Petri net model */
	private PetriNet myPetriNetModel;

	/**
	 * {@inheritDoc}
	 */
	public DecisionPointPetriNet(String name,
			DecisionPointAnalysisResult parent, PetriNet petriNetModel,
			Place place) {
		super(name, parent);
		myNode = place;
		myPlace = place;
		myPetriNetModel = petriNetModel;
		myContext = buildContext();
	}

	/**
	 * Initializes the decision point context as a
	 * {@link DecisionPointContextPetriNet DecisionPointContextPetriNet}.
	 */
	private DecisionPointContext buildContext() {
		return new DecisionPointContextPetriNet(myPetriNetModel, myPlace, this);
	}
}
