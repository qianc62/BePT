package org.processmining.analysis.socialnetwork;

import java.util.Iterator;
import java.util.Set;

import edu.uci.ics.jung.algorithms.importance.BaryCenter;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.importance.HITS;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.StringLabeller;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class SNAlgorithmJung {
	private edu.uci.ics.jung.graph.Graph g = null;
	private NumberEdgeValue edge_weight = null;

	// private NumberVertexValue vertex_weight = null;

	public SNAlgorithmJung(Graph g, NumberEdgeValue edge_weight) {
		this.g = g;
		this.edge_weight = edge_weight;
	}

	public String calculateDegree(boolean[] selectedDegreeOptions) {
		StringBuffer stringBuffer = new StringBuffer();
		// header
		stringBuffer.append("Degree Centrality\nNode Name\t");
		if (selectedDegreeOptions[0])
			stringBuffer.append("In Degree\t");
		if (selectedDegreeOptions[1])
			stringBuffer.append("Out Degree\t");
		stringBuffer.append("\n");

		StringLabeller sl = StringLabeller.getLabeller(g);
		Set vertices = g.getVertices();
		Iterator it = vertices.iterator();
		float inDegree_sum = 0;
		float outDegree_sum = 0;

		while (it.hasNext()) {
			Vertex v = (Vertex) it.next();
			stringBuffer.append(sl.getLabel(v) + "\t");
			if (selectedDegreeOptions[0]) {
				float inDegree = 0;

				if (selectedDegreeOptions[3]) {
					Set inEdges = v.getInEdges();
					Iterator it2 = inEdges.iterator();
					while (it2.hasNext()) {
						Edge ed = (Edge) it2.next();
						inDegree += edge_weight.getNumber(ed).floatValue();
					}
				} else if (selectedDegreeOptions[2])
					inDegree = (float) v.inDegree() / (float) vertices.size();
				else
					inDegree = v.inDegree();

				stringBuffer.append(String.valueOf(inDegree) + "\t");
				inDegree_sum += inDegree;
			}

			if (selectedDegreeOptions[1]) {
				float outDegree = 0;
				if (selectedDegreeOptions[3]) {
					Set outEdges = v.getOutEdges();
					Iterator it2 = outEdges.iterator();
					while (it2.hasNext()) {
						Edge ed = (Edge) it2.next();
						outDegree += edge_weight.getNumber(ed).doubleValue();
					}
				} else if (selectedDegreeOptions[2])
					outDegree = (float) v.outDegree() / (float) vertices.size();
				else
					outDegree = v.outDegree();
				stringBuffer.append(String.valueOf(outDegree) + "\t");
				outDegree_sum += outDegree;
			}
			stringBuffer.append("\n");
		}
		if (selectedDegreeOptions[0])
			stringBuffer.append("average in Degree = "
					+ String.valueOf(inDegree_sum / vertices.size()) + "\n");
		if (selectedDegreeOptions[1])
			stringBuffer.append("average out Degree = "
					+ String.valueOf(outDegree_sum / vertices.size())
					+ "\n\n\n");
		return stringBuffer.toString();
	}

	public String calculateBetweenness() {

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Betweeness Centrality\nNode Name\tBetweenness\n");

		StringLabeller sl = StringLabeller.getLabeller(g);

		BetweennessCentrality ranker = new BetweennessCentrality(g);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();

		Set vertices = g.getVertices();

		Iterator it = vertices.iterator();
		while (it.hasNext()) {
			Vertex v = (Vertex) it.next();
			Double t = ranker.getRankScore(v);
			stringBuffer.append(sl.getLabel(v) + "\t" + t + "\n");
		}
		stringBuffer.append("\n\n");
		return stringBuffer.toString();
	}

	public String calculateBaryRanker() {

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("BaryRanker\nNode Name\tBaryRanker\n");

		StringLabeller sl = StringLabeller.getLabeller(g);

		BaryCenter ranker = new BaryCenter(g);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();

		Set vertices = g.getVertices();

		Iterator it = vertices.iterator();
		while (it.hasNext()) {
			Vertex v = (Vertex) it.next();
			Double t = ranker.getRankScore(v);
			stringBuffer.append(sl.getLabel(v) + "\t" + t + "\n");
		}
		stringBuffer.append("\n\n");
		return stringBuffer.toString();
	}

	public String calculateHITS() {
		// hubs-and-authorities
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("hubs-and-authorities\nNode Name\tHITS\n");

		StringLabeller sl = StringLabeller.getLabeller(g);

		HITS ranker = new HITS(g);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();

		Set vertices = g.getVertices();

		Iterator it = vertices.iterator();
		while (it.hasNext()) {
			Vertex v = (Vertex) it.next();
			Double t = ranker.getRankScore(v);
			stringBuffer.append(sl.getLabel(v) + "\t" + t + "\n");
		}
		stringBuffer.append("\n\n");
		return stringBuffer.toString();
	}
}
