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

import org.processmining.framework.ui.Message;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;

/**
 * @author christian
 * 
 */
public class ConcurrencyEdgeTransformer extends FuzzyGraphTransformer {

	protected MutableFuzzyGraph graph;
	protected double preserveThreshold;
	protected double ratioThreshold;
	protected int counterParallelized;
	protected int counterResolved;

	public ConcurrencyEdgeTransformer() {
		super("Concurrency edge transformer");
		this.graph = null;
		this.preserveThreshold = 0.4;
		this.ratioThreshold = 0.7;
	}

	public void setPreserveThreshold(double threshold) {
		preserveThreshold = threshold;
	}

	public void setRatioThreshold(double threshold) {
		ratioThreshold = threshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.graph.transform.FuzzyGraphTransformer
	 * #transform(org.processmining.mining.fuzzymining.graph.FuzzyGraph)
	 */
	public void transform(MutableFuzzyGraph graph) {
		counterParallelized = 0;
		counterResolved = 0;
		this.graph = graph;
		int numberOfNodes = graph.getNumberOfInitialNodes();
		for (int x = 0; x < numberOfNodes; x++) {
			for (int y = 0; y < x; y++) {
				processRelationPair(x, y);
			}
		}
		Message.add("    Concurrency edge transformer: Resolved "
				+ counterResolved + " conflicts, parallelized "
				+ counterParallelized + " node pairs.", Message.NORMAL);
	}

	protected void processRelationPair(int indexA, int indexB) {
		double sigFwd = graph.getBinarySignificance(indexA, indexB);
		double sigRwd = graph.getBinarySignificance(indexB, indexA);
		if (sigFwd > 0.0 && sigRwd > 0.0) {
			// conflict situation
			double relImpAB = getRelativeImportanceForEndNodes(indexA, indexB);
			double relImpBA = getRelativeImportanceForEndNodes(indexB, indexA);
			if (relImpAB > preserveThreshold && relImpBA > preserveThreshold) {
				// preserve both links, as they are sufficiently locally
				// important
			} else {
				// investigate ratio of local importance between both relations
				double ratio = Math.min(relImpAB, relImpBA)
						/ Math.max(relImpAB, relImpBA);
				if (ratio < ratioThreshold) {
					// preserve locally more important relation
					if (relImpAB > relImpBA) {
						// erase B -> A
						graph.setBinarySignificance(indexB, indexA, 0.0);
						graph.setBinaryCorrelation(indexB, indexA, 0.0);
					} else {
						// erase A -> B
						graph.setBinarySignificance(indexA, indexB, 0.0);
						graph.setBinaryCorrelation(indexA, indexB, 0.0);
					}
					counterResolved++;
				} else {
					// erase both.
					// erase A -> B
					graph.setBinarySignificance(indexA, indexB, 0.0);
					graph.setBinaryCorrelation(indexA, indexB, 0.0);
					// erase B -> A
					graph.setBinarySignificance(indexB, indexA, 0.0);
					graph.setBinaryCorrelation(indexB, indexA, 0.0);
					counterParallelized++;
				}
			}
		}
		// else: no conflict to be resolved
	}

	protected double getRelativeImportanceForEndNodes(int fromIndex, int toIndex) {
		double sigRef = graph.getBinarySignificance(fromIndex, toIndex);
		// accumulate all outgoing significances of source node, and
		// all incoming significances of target node, respectively.
		double sigSourceOutAcc = 0.0;
		double sigTargetInAcc = 0.0;
		for (int i = graph.getNumberOfInitialNodes() - 1; i >= 0; i--) {
			if (i != fromIndex) { // ignore self-loops in calculation
				sigSourceOutAcc += graph.getBinarySignificance(fromIndex, i);
			}
			if (i != toIndex) { // ignore self-loops in calculation
				sigTargetInAcc += graph.getBinarySignificance(i, toIndex);
			}
		}
		// relative importance is the product of the relative significances
		// within the source's outgoing and the target's incoming links
		double relativeImportance = ((sigRef / sigSourceOutAcc) + (sigRef / sigTargetInAcc));
		return relativeImportance;
	}

}
