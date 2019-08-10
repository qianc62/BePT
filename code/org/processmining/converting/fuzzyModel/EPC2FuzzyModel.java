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
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.fuzzymining.ui.FuzzyModelViewResult;

import att.grappa.Edge;
import att.grappa.Node;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class EPC2FuzzyModel implements ConvertingPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.converting.ConvertingPlugin#accepts(org.processmining
	 * .framework.plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject original) {
		for (Object o : original.getObjects()) {
			if (o instanceof ConfigurableEPC) {
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
		ConfigurableEPC epc = null;
		for (Object o : original.getObjects()) {
			if (o instanceof ConfigurableEPC) {
				epc = (ConfigurableEPC) o;
				break;
			}
		}
		// assemble fuzzy graph from EPC
		FuzzyModelCreator kreator = new FuzzyModelCreator();
		// add nodes from EPC functions
		for (Object o : epc.getFunctions()) {
			EPCFunction func = (EPCFunction) o;
			if (func.getLogEvent() != null) {
				kreator.addNode(func.getLogEvent());
			}
		}
		// add edges to successor of each node / function
		for (Object o : epc.getFunctions()) {
			EPCFunction func = (EPCFunction) o;
			if (func.getLogEvent() != null) {
				addAllSuccessorEdges(epc, kreator, func, func, true);
			}
		}
		// return created fuzzy graph in result view
		return new FuzzyModelViewResult(kreator.generateGraph());
	}

	protected void addAllSuccessorEdges(ConfigurableEPC epc,
			FuzzyModelCreator kreator, EPCFunction source, Node start,
			boolean forceFollow) {
		if (start.getOutEdges() != null
				&& (forceFollow || source.equals(start) == false)) {
			for (Edge outEdge : start.getOutEdges()) {
				Node target = outEdge.getHead();
				if (target == null) {
					continue;
				} else if (target instanceof EPCFunction
						&& ((EPCFunction) target).getLogEvent() != null) {
					kreator.addEdge(source.getLogEvent(),
							((EPCFunction) target).getLogEvent(), 1.0, 1.0);
				} else {
					addAllSuccessorEdges(epc, kreator, source, target, false);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "Translates an EPC to a Fuzzy Model";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "EPC to Fuzzy Model";
	}

}
