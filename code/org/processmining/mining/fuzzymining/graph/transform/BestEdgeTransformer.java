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

import java.util.BitSet;

import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;

/**
 * @author cgunther
 * 
 */
public class BestEdgeTransformer extends FuzzyGraphTransformer {

	protected int numberOfInitialNodes;
	protected MutableFuzzyGraph graph;
	protected BitSet preserveMask;

	public BestEdgeTransformer() {
		super("Best edge transformer");
		graph = null;
		preserveMask = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.transform.FuzzyGraphTransformer
	 * #transform(org.processmining.mining.fuzzymining.graph.FuzzyGraph)
	 */
	public void transform(MutableFuzzyGraph graph) {
		this.graph = graph;
		numberOfInitialNodes = graph.getNumberOfInitialNodes();
		preserveMask = new BitSet(numberOfInitialNodes * numberOfInitialNodes);
		buildBitMask();
		filterEdges();
	}

	protected void buildBitMask() {
		for (int i = 0; i < numberOfInitialNodes; i++) {
			setBitMask(graph.getPrimitiveNode(i));
		}
	}

	protected void filterEdges() {
		for (int x = 0; x < numberOfInitialNodes; x++) {
			for (int y = 0; y < numberOfInitialNodes; y++) {
				if (x == y) {
					// no self-loops handled here..
					continue;
				} else if (getBitMask(x, y) == false) {
					graph.setBinarySignificance(x, y, 0.0);
					graph.setBinaryCorrelation(x, y, 0.0);
				}
			}
		}
	}

	protected void setBitMask(Node node) {
		int nodeIndex = node.getIndex();
		// find best predecessor and successor
		int bestX = -1;
		int bestY = -1;
		double bestXSig = 0.0;
		double bestYSig = 0.0;
		double sigX, sigY;
		for (int x = 0; x < numberOfInitialNodes; x++) {
			if (x == nodeIndex) {
				continue;
			} // skip self
			sigX = graph.getBinarySignificance(x, nodeIndex);
			if (sigX > bestXSig) {
				bestXSig = sigX;
				bestX = x;
			}
			sigY = graph.getBinarySignificance(nodeIndex, x);
			if (sigY > bestYSig) {
				bestYSig = sigY;
				bestY = x;
			}
		}
		// flag best predecessor and successor, if any
		if (bestX >= 0) {
			setBitMask(bestX, nodeIndex, true);
		}
		if (bestY >= 0) {
			setBitMask(nodeIndex, bestY, true);
		}
	}

	protected void setBitMask(int x, int y, boolean value) {
		preserveMask.set(translateIndex(x, y), value);
	}

	protected boolean getBitMask(int x, int y) {
		return preserveMask.get(translateIndex(x, y));
	}

	protected int translateIndex(int x, int y) {
		return ((x * numberOfInitialNodes) + y);
	}

}
