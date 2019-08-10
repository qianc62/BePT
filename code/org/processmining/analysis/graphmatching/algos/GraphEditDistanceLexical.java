package org.processmining.analysis.graphmatching.algos;

import java.util.HashSet;
import java.util.Set;

import org.processmining.analysis.graphmatching.graph.SimpleGraph;
import org.processmining.analysis.graphmatching.graph.TwoVertices;
import org.processmining.analysis.graphmatching.led.StringEditDistance;

public class GraphEditDistanceLexical extends DistanceAlgoAbstr implements
		DistanceAlgo {

	Set<TwoVertices> solutionMappings = null;

	public double compute(SimpleGraph sg1, SimpleGraph sg2) {
		init(sg1, sg2);
		solutionMappings = new HashSet<TwoVertices>();

		// function mapping score
		for (Integer g1Func : sg1.getVertices()) {
			for (Integer g2Func : sg2.getVertices()) {

				// find the score using edit distance
				double edScore = StringEditDistance.similarity(sg1
						.getLabel(g1Func), sg2.getLabel(g2Func));

				// add all the result that has the ed >= threshold
				if (edScore >= this.ledcutoff) {
					solutionMappings.add(new TwoVertices(g1Func, g2Func));
				}
			}
		}

		return editDistance(solutionMappings);
	}

	public Set<TwoVertices> bestMapping() {
		return solutionMappings;
	}
}