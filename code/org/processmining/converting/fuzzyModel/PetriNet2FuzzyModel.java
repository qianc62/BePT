/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.converting.fuzzyModel;

import java.util.ArrayList;
import java.util.List;

import org.processmining.converting.ConvertingPlugin;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.fuzzymining.ui.FuzzyModelViewResult;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class PetriNet2FuzzyModel implements ConvertingPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.converting.ConvertingPlugin#accepts(org.processmining
	 * .framework.plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject original) {
		for (Object o : original.getObjects()) {
			if (o instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.converting.ConvertingPlugin#convert(org.processmining
	 * .framework.plugin.ProvidedObject)
	 */
	public MiningResult convert(ProvidedObject original) {
		PetriNet petriNet = null;
		for (Object o : original.getObjects()) {
			if (o instanceof PetriNet) {
				petriNet = (PetriNet) o;
				break;
			}
		}
		// assemble fuzzy graph from petri net
		FuzzyModelCreator kreator = new FuzzyModelCreator();
		// add all transitions as nodes
		for (Transition transition : petriNet.getTransitions()) {
			if (transition.getLogEvent() != null) {
				kreator.addNode(transition.getLogEvent());
			}
		}
		// add links for each transition to their potential followers
		for (Transition transition : petriNet.getTransitions()) {
			if (transition.getLogEvent() != null) {
				for (Transition follower : getFollowers(transition)) {
					kreator.addEdge(transition.getLogEvent(), follower
							.getLogEvent(), 1.0, 1.0);
				}
			}
		}
		// return created fuzzy graph in result view
		return new FuzzyModelViewResult(kreator.generateGraph());
	}

	protected List<Transition> getFollowers(Transition transition) {
		ArrayList<Transition> followers = new ArrayList<Transition>();
		for (Object p : transition.getSuccessors()) {
			Place place = (Place) p;
			for (Object t : place.getSuccessors()) {
				Transition folwr = (Transition) t;
				if (folwr.getLogEvent() != null) {
					followers.add(folwr);
				} else {
					followers.addAll(getFollowers(folwr));
				}
			}
		}
		return followers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "Translates a Petri net to a Fuzzy Model";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Petri net to Fuzzy Model";
	}

}
