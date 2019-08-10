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
package org.processmining.mining.fuzzymining.replay;

import java.util.ArrayList;
import java.util.List;

import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;
import org.processmining.mining.fuzzymining.replay.DetailNodeAnalysis.SimplificationType;

/**
 * @author christian
 * 
 */
public class FuzzyDetailAnalysis {

	protected FuzzyGraph graph;
	protected double detail;
	protected List<DetailNodeAnalysis> nodeAnalysis;

	public FuzzyDetailAnalysis(FuzzyGraph aGraph) {
		graph = aGraph;
		detail = calculate();
	}

	public double getDetail() {
		return detail;
	}

	public List<DetailNodeAnalysis> getNodeAnalysis() {
		return nodeAnalysis;
	}

	public double calculate() {
		nodeAnalysis = new ArrayList<DetailNodeAnalysis>();
		int visible = 0;
		double allSignificance = 0.0;
		double visibleSignificance = 0.0;
		Node node;
		for (int i = 0; i < graph.getNumberOfInitialNodes(); i++) {
			node = graph.getNodeMappedTo(i);
			if (node == null) {
				nodeAnalysis.add(new DetailNodeAnalysis(graph
						.getPrimitiveNode(i), graph.getLogEvents().get(i),
						SimplificationType.REMOVED));
			} else if (node instanceof ClusterNode) {
				nodeAnalysis.add(new DetailNodeAnalysis(graph
						.getPrimitiveNode(i), graph.getLogEvents().get(i),
						SimplificationType.CLUSTERED));
			} else {
				nodeAnalysis.add(new DetailNodeAnalysis(graph
						.getPrimitiveNode(i), graph.getLogEvents().get(i),
						SimplificationType.PRESERVED));
			}
			if (node != null && (node instanceof ClusterNode) == false) {
				visible++;
				visibleSignificance += node.getSignificance();
			}
			allSignificance += graph.getPrimitiveNode(i).getSignificance();
		}
		return visibleSignificance / allSignificance;
	}
}
