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
package org.processmining.mining.fuzzymining.graph.transform;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatisticsImpl;
import org.processmining.framework.ui.Message;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;

/**
 * @author christian
 * 
 */
public class StatisticalCleanupTransformer extends FuzzyGraphTransformer {

	SummaryStatistics nodeSignificanceStats;
	SummaryStatistics linkSignificanceStats;
	SummaryStatistics linkCorrelationStats;

	/**
	 * 
	 */
	public StatisticalCleanupTransformer() {
		super("Statistical cleanup transformer");
		nodeSignificanceStats = new SummaryStatisticsImpl();
		linkSignificanceStats = new SummaryStatisticsImpl();
		linkCorrelationStats = new SummaryStatisticsImpl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.transform.FuzzyGraphTransformer
	 * #transform(org.processmining.mining.fuzzymining.graph.FuzzyGraph)
	 */
	public void transform(MutableFuzzyGraph graph) {
		Message.add("Performing statistical cleanup of graph...",
				Message.NORMAL);
		createStatistics(graph);
		int numberOfNodes = graph.getNumberOfInitialNodes();
		// clean up node significance
		double nodeMaxDeviation = nodeSignificanceStats.getStandardDeviation();// *
		// 2.0;
		double maxNodeSig = nodeSignificanceStats.getMean() + nodeMaxDeviation;
		double minNodeSig = nodeSignificanceStats.getMean() - nodeMaxDeviation;
		Node node;
		int attNodeCounter = 0;
		int delNodeCounter = 0;
		for (int i = 0; i < numberOfNodes; i++) {
			node = graph.getNodeMappedTo(i);
			if (node != null) {
				if (node.getSignificance() > maxNodeSig) {
					// attenuate node significance
					node.setSignificance(maxNodeSig);
					attNodeCounter++;
				} else if (node.getSignificance() < minNodeSig) {
					// remove node from graph
					graph.setNodeAliasMapping(i, null);
					delNodeCounter++;
				}
			}
		}
		// clean up link significance
		double linkMaxDeviation = linkSignificanceStats.getStandardDeviation();// *
		// 2.0;
		double minLinkSig = linkSignificanceStats.getMean() - linkMaxDeviation;
		double maxLinkSig = linkSignificanceStats.getMean() + linkMaxDeviation;
		double curSig;
		int attLinkCounter = 0;
		int delLinkCounter = 0;
		for (int x = 0; x < numberOfNodes; x++) {
			for (int y = 0; y < numberOfNodes; y++) {
				curSig = graph.getBinarySignificance(x, y);
				if (curSig > maxLinkSig) {
					// attenuate link significance
					graph.setBinarySignificance(x, y, maxLinkSig);
					attLinkCounter++;
				} else if (curSig < minLinkSig) {
					// remove connection (incl. correlation entry)
					graph.setBinarySignificance(x, y, 0.0);
					graph.setBinaryCorrelation(x, y, 0.0);
					delLinkCounter++;
				}
			}
		}
		Message.add("...done!", Message.NORMAL);
		Message.add("Deleted " + delNodeCounter + " nodes.", Message.NORMAL);
		Message.add("Attenuated " + attNodeCounter + " nodes.", Message.NORMAL);
		Message.add("Deleted " + delLinkCounter + " links.", Message.NORMAL);
		Message.add("Attenuated " + attLinkCounter + " links.", Message.NORMAL);
	}

	protected void createStatistics(FuzzyGraph graph) {
		nodeSignificanceStats.clear();
		linkSignificanceStats.clear();
		linkCorrelationStats.clear();
		double nodeSig, linkSig, linkCor;
		for (int x = 0; x < graph.getNumberOfInitialNodes(); x++) {
			nodeSig = graph.getNodeMappedTo(x).getSignificance();
			if (nodeSig > 0.0) {
				nodeSignificanceStats.addValue(nodeSig);
			}
			for (int y = 0; y < graph.getNumberOfInitialNodes(); y++) {
				linkSig = graph.getBinarySignificance(x, y);
				linkCor = graph.getBinaryCorrelation(x, y);
				if (linkSig > 0.0) {
					linkSignificanceStats.addValue(linkSig);
					linkCorrelationStats.addValue(linkCor);
				}
			}
		}
	}

	public void printStatistics() {
		System.out.println("Node significance statistics: ");
		print(nodeSignificanceStats);
		System.out.println("Link significance statistics: ");
		print(linkSignificanceStats);
		System.out.println("Link Correlation statistics: ");
		print(linkCorrelationStats);
	}

	protected void print(SummaryStatistics stats) {
		System.out.println("min: " + stats.getMin());
		System.out.println("max: " + stats.getMax());
		System.out.println("mean: " + stats.getMean());
		System.out.println("geometr. mean: " + stats.getGeometricMean());
		System.out.println("standard deviation: "
				+ stats.getStandardDeviation());
		System.out.println("variance: " + stats.getVariance());
	}

}
