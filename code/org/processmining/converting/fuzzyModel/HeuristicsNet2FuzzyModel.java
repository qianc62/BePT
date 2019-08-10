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

import org.processmining.converting.ConvertingPlugin;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.fuzzymining.ui.FuzzyModelViewResult;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class HeuristicsNet2FuzzyModel implements ConvertingPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.converting.ConvertingPlugin#accepts(org.processmining
	 * .framework.plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject original) {
		for (Object o : original.getObjects()) {
			if (o instanceof HeuristicsNet) {
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
		HeuristicsNet net = null;
		for (Object o : original.getObjects()) {
			if (o instanceof HeuristicsNet) {
				net = (HeuristicsNet) o;
				break;
			}
		}
		// assemble fuzzy graph from heuristics net
		FuzzyModelCreator kreator = new FuzzyModelCreator();
		// add a node for each log event found
		for (LogEvent event : net.getLogEvents()) {
			kreator.addNode(event);
		}
		// for each node, find and add edges to each possible successor
		for (int i = 0; i < net.size(); i++) {
			addSuccessorEdgesForIndex(net, kreator, i);
		}

		// return created fuzzy graph in result view
		return new FuzzyModelViewResult(kreator.generateGraph());
	}

	protected void addSuccessorEdgesForIndex(HeuristicsNet net,
			FuzzyModelCreator kreator, int index) {
		int eventIndex = net.getDuplicatesMapping()[index];
		LogEvent refEvent = net.getLogEvents().get(eventIndex);
		HNSubSet outSetElements = net.getAllElementsOutputSet(index);
		for (int i = 0; i < net.size(); i++) {
			if (outSetElements.contains(i)) {
				LogEvent successorEvent = net.getLogEvents().get(
						net.getDuplicatesMapping()[i]);
				kreator.addEdge(refEvent, successorEvent, 1.0, 1.0);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "Translates a Heuristics net to a Fuzzy Model";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Heuristics net to Fuzzy Model";
	}

}
