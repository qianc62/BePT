package org.processmining.analysis.graphmatching.algos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.analysis.graphmatching.graph.SimpleGraph;
import org.processmining.analysis.graphmatching.graph.TwoVertices;
import org.processmining.analysis.graphmatching.led.StringEditDistance;

public abstract class DistanceAlgoAbstr implements DistanceAlgo {

	public final static int EPSILON = -1; // means: 'no mapping'
	public final static double VERTEX_INSERTION_COST = 0.1; // Only for
	// reproducing
	// Luciano's results
	public final static double VERTEX_DELETION_COST = 0.9; // Only for
	// reproducing
	// Luciano's results

	protected SimpleGraph sg1;
	protected SimpleGraph sg2;
	protected int totalNrVertices;
	protected int totalNrEdges;

	double weightGroupedVertex;
	double weightSkippedVertex;
	double weightSkippedEdge;
	double weightSubstitutedVertex;
	double ledcutoff;
	boolean usepuredistance;
	int prunewhen;
	int pruneto;
	boolean useepsilon;
	boolean dogrouping;
	boolean useevents;

	class Mapping implements Comparable<Mapping> {
		protected double cost;
		protected double vertexMappingCost;
		protected double vertexMappingCount;
		protected Map<Integer, Integer> mappingsFrom1;
		protected Map<Integer, Integer> mappingsFrom2;
		protected Set<Integer> addedVertices;
		protected Set<Integer> deletedVertices;
		protected Set<TwoVertices> matchedEdges;
		protected Set<TwoVertices> addedEdges;
		protected Set<TwoVertices> deletedEdges;
		protected List<Integer> remaining1;
		protected List<Integer> remaining2;

		public Mapping() {
			cost = 0.0;
			vertexMappingCost = 0.0;
			vertexMappingCount = 0.0;
			mappingsFrom1 = new HashMap<Integer, Integer>();
			mappingsFrom2 = new HashMap<Integer, Integer>();
			addedVertices = new HashSet<Integer>();
			deletedVertices = new HashSet<Integer>();
			matchedEdges = new HashSet<TwoVertices>();
			addedEdges = new HashSet<TwoVertices>();
			deletedEdges = new HashSet<TwoVertices>();
			remaining1 = new LinkedList<Integer>(sg1.getVertices());
			remaining2 = new LinkedList<Integer>(sg2.getVertices());
		}

		public Mapping clone() {
			Mapping m = new Mapping();
			m.remaining1.clear();
			m.remaining2.clear();
			m.cost = cost;
			m.vertexMappingCost = vertexMappingCost;
			m.vertexMappingCount = vertexMappingCount;

			m.mappingsFrom1.putAll(mappingsFrom1);
			m.mappingsFrom2.putAll(mappingsFrom2);
			m.addedVertices.addAll(addedVertices);
			m.deletedVertices.addAll(deletedVertices);
			m.matchedEdges.addAll(matchedEdges);
			m.addedEdges.addAll(addedEdges);
			m.deletedEdges.addAll(deletedEdges);
			m.remaining1.addAll(remaining1);
			m.remaining2.addAll(remaining2);
			return m;
		}

		public int compareTo(Mapping o) {
			if (cost < o.cost)
				return -1;
			else if (cost > o.cost)
				return 1;
			else
				return 0;
		}

		public double getCost() {
			return cost;
		}

		public void updateCost(GraphEditDistanceAStarSim edCalculator) {
			cost = edCalculator.editDistance(this);
		}

		public void step(Integer v1, Integer v2) {
			step(v1, v2, 0.0);
		}

		public void step(Integer v1, Integer v2, double subsCost) {
			if (v1 == EPSILON) {
				remaining2.remove(v2);
				addedVertices.add(v2);
				mappingsFrom2.put(v2, EPSILON);
			} else if (v2 == EPSILON) {
				remaining1.remove(v1);
				deletedVertices.add(v1);
				mappingsFrom1.put(v1, EPSILON);
			} else {
				remaining1.remove(v1);
				remaining2.remove(v2);
				vertexMappingCost += subsCost;
				vertexMappingCount += 1.0;
				mappingsFrom1.put(v1, v2);
				mappingsFrom2.put(v2, v1);
			}

			if (v1 != EPSILON) {
				for (Integer v : sg1.preSet(v1)) {
					if (mappingsFrom1.containsKey(v)) {
						if (v2 != EPSILON
								&& sg2.preSet(v2)
										.contains(mappingsFrom1.get(v))) {
							matchedEdges.add(new TwoVertices(v, v1));
						} else {
							deletedEdges.add(new TwoVertices(v, v1));
						}
					}
				}
				for (Integer v : sg1.postSet(v1)) {
					if (mappingsFrom1.containsKey(v)) {
						if (v2 != EPSILON
								&& sg2.postSet(v2).contains(
										mappingsFrom1.get(v))) {
							matchedEdges.add(new TwoVertices(v1, v));
						} else {
							deletedEdges.add(new TwoVertices(v1, v));
						}
					}
				}
			}

			if (v2 != EPSILON) {
				for (Integer v : sg2.preSet(v2)) {
					if (mappingsFrom2.containsKey(v)) {
						if (v1 != EPSILON
								&& sg1.preSet(v1)
										.contains(mappingsFrom2.get(v))) {
							// Edge substitution set is handled over the SG1
							// edges
						} else {
							addedEdges.add(new TwoVertices(v, v2));
						}
					}
				}
				for (Integer v : sg2.postSet(v2)) {
					if (mappingsFrom2.containsKey(v)) {
						if (v1 != EPSILON
								&& sg1.postSet(v1).contains(
										mappingsFrom2.get(v))) {
							// Edge substitution set is handled over the SG1
							// edges
						} else {
							addedEdges.add(new TwoVertices(v, v2));
						}
					}
				}
			}
		}
	}

	/**
	 * Sets the weights for: - skipping vertices (vweight) - substituting
	 * vertices (sweight) - skipping edges (eweight) - string edit similarity
	 * cutoff (ledcutoff) - use pure edit distance/ use weighted average
	 * distance (usepuredistance) Ad usepuredistance: weight is a boolean. If
	 * 1.0: uses the pure edit distance, if 0.0: uses weighted average of the
	 * fractions of skipped vertices, skipped edges and substitution score. -
	 * prune when recursion reaches this depth, 0.0 means no pruning (prunewhen)
	 * - prune to recursion of this depth (pruneto)
	 * 
	 * The argument is an array of objects, interchangably a String ("vweight",
	 * "sweight", or "eweight") and a 0.0 <= Double <= 1.0 that is the value
	 * that should be set for the given weight. All other weights are set to
	 * 0.0.
	 * 
	 * @param weights
	 *            Pre: for i mod 2 = 0: weights[i] instanceof String /\
	 *            weights[i] \in {"vweight", "sweight", or "eweight"} for i mod
	 *            2 = 1: weights[i] instanceof Double /\ 0.0 <= weights[i] <=
	 *            1.0 for i: if i < weights.length(), then i+1 <
	 *            weights.length() Post: weight identified by weights[i] is set
	 *            to weights[i+1] all other weights are set to 0.0
	 */
	public void setWeight(Object weights[]) {
		this.weightGroupedVertex = 0.0;
		this.weightSkippedVertex = 0.0;
		this.weightSubstitutedVertex = 0.0;
		this.weightSkippedEdge = 0.0;
		this.ledcutoff = 0.0;
		this.usepuredistance = false;
		this.prunewhen = 0;
		this.pruneto = 0;
		this.useepsilon = false;
		this.dogrouping = false;
		this.useevents = false;

		for (int i = 0; i < weights.length; i = i + 2) {
			String wname = (String) weights[i];
			Double wvalue = (Double) weights[i + 1];
			if (wname.equals("vweight")) {
				this.weightSkippedVertex = wvalue;
			} else if (wname.equals("sweight")) {
				this.weightSubstitutedVertex = wvalue;
			} else if (wname.equals("gweight")) {
				this.weightGroupedVertex = wvalue;
			} else if (wname.equals("eweight")) {
				this.weightSkippedEdge = wvalue;
			} else if (wname.equals("ledcutoff")) {
				this.ledcutoff = wvalue;
			} else if (wname.equals("usepuredistance")) {
				if (wvalue == 0.0) {
					this.usepuredistance = false;
				} else {
					this.usepuredistance = true;
				}
			} else if (wname.equals("useepsilon")) {
				if (wvalue == 0.0) {
					this.useepsilon = false;
				} else {
					this.useepsilon = true;
				}
			} else if (wname.equals("useevents")) {
				if (wvalue == 0.0) {
					this.useevents = false;
				} else {
					this.useevents = true;
				}
			} else if (wname.equals("dogrouping")) {
				if (wvalue == 0.0) {
					this.dogrouping = false;
				} else {
					this.dogrouping = true;
				}
			} else if (wname.equals("prunewhen")) {
				this.prunewhen = wvalue.intValue();
			} else if (wname.equals("pruneto")) {
				this.pruneto = wvalue.intValue();
			} else {
				System.err
						.println("ERROR: Invalid weight identifier: " + wname);
			}
		}
	}

	public boolean useEvents() {
		return useevents;
	}

	protected void init(SimpleGraph sg1, SimpleGraph sg2) {
		this.sg1 = sg1;
		this.sg2 = sg2;
		totalNrVertices = sg1.getVertices().size() + sg2.getVertices().size();
		totalNrEdges = 0;
		for (Integer i : sg1.getVertices()) {
			totalNrEdges += sg1.postSet(i).size();
		}
		for (Integer i : sg2.getVertices()) {
			totalNrEdges += sg2.postSet(i).size();
		}
	}

	protected double computeScore(double skippedEdges, double skippedVertices,
			double substitutedVertices, double nrSubstitutions,
			double insertedVertices, double deletedVertices) {
		if (usepuredistance) {
			if (useepsilon) {
				return weightSkippedVertex
						* (VERTEX_DELETION_COST * deletedVertices + VERTEX_INSERTION_COST
								* insertedVertices) + weightSkippedEdge
						* skippedEdges + weightSubstitutedVertex * 2.0
						* substitutedVertices;
			} else {
				return weightSkippedVertex * skippedVertices
						+ weightSkippedEdge * skippedEdges
						+ weightSubstitutedVertex * 2.0 * substitutedVertices;
			}
		} else {
			// Return the total edit distance. Multiply each element with its
			// weight.
			double vskip = skippedVertices / (1.0 * totalNrVertices);
			double vsubs = (2.0 * substitutedVertices)
					/ (1.0 * nrSubstitutions);
			double editDistance;
			if (totalNrEdges == 0) {
				editDistance = ((weightSkippedVertex * vskip) + (weightSubstitutedVertex * vsubs))
						/ (weightSkippedVertex + weightSubstitutedVertex);
			} else {
				double eskip = (skippedEdges / (1.0 * totalNrEdges));
				editDistance = ((weightSkippedVertex * vskip)
						+ (weightSubstitutedVertex * vsubs) + (weightSkippedEdge * eskip))
						/ (weightSkippedVertex + weightSubstitutedVertex + weightSkippedEdge);
			}
			return ((editDistance >= 0.0) && (editDistance <= 1.0)) ? editDistance
					: 1.0;
		}
	}

	protected double editDistance(Mapping m) {
		double skippedEdges;
		double skippedVertices;
		if (useepsilon) {
			skippedEdges = (double) m.addedEdges.size()
					+ (double) m.deletedEdges.size();
			skippedVertices = (double) m.addedVertices.size()
					+ (double) m.deletedVertices.size();
		} else {
			skippedEdges = (double) totalNrEdges - 2.0
					* (double) m.matchedEdges.size();
			skippedVertices = (double) totalNrVertices - 2.0
					* (double) m.vertexMappingCount;
		}
		double substitutedVertices = m.vertexMappingCost;
		double deletedVertices = (double) m.deletedVertices.size();
		double insertedVertices = (double) m.addedVertices.size();
		double nrSubstitutions = ((double) totalNrVertices) - skippedVertices;

		return computeScore(skippedEdges, skippedVertices, substitutedVertices,
				nrSubstitutions, insertedVertices, deletedVertices);
	}

	protected double editDistance(Set<TwoVertices> m) {
		Set<Integer> verticesFrom1Used = new HashSet<Integer>();
		Set<Integer> verticesFrom2Used = new HashSet<Integer>();

		double epsilonSkippedVertices = 0.0;
		double epsilonInsertedVertices = 0.0;
		double epsilonDeletedVertices = 0.0;
		double epsilonSkippedEdges = 0.0;

		// vid1tovid2 = m, but it is a mapping, so we can more efficiently find
		// the
		// counterpart of a node in SimpleGraph1.
		Map<Integer, Integer> vid1tovid2 = new HashMap<Integer, Integer>();
		Map<Integer, Integer> vid2tovid1 = new HashMap<Integer, Integer>();

		// Substituted vertices are vertices that >are< mapped.
		// Their distance is 1.0 - string-edit similarity of their labels.
		double substitutedVertices = 0.0;
		double nrSubstitutions = 0.0;
		for (TwoVertices pair : m) {
			if ((pair.v1 != EPSILON) && (pair.v2 != EPSILON)) {
				double substitutionDistance;
				String label1 = sg1.getLabel(pair.v1);
				String label2 = sg2.getLabel(pair.v2);
				verticesFrom1Used.add(pair.v1);
				verticesFrom2Used.add(pair.v2);
				if (((label1.length() == 0) && (label2.length() != 0))
						|| ((label1.length() != 0) && (label2.length() == 0))) {
					// Do not tolerate mapping of tasks to control nodes
					substitutionDistance = Double.MAX_VALUE;
				} else {
					// Score the substitution
					substitutionDistance = 1.0 - StringEditDistance.similarity(
							sg1.getLabel(pair.v1), sg2.getLabel(pair.v2));
				}
				nrSubstitutions += 1.0;
				substitutedVertices += substitutionDistance;
			} else {
				if (pair.v1 == EPSILON) {
					epsilonInsertedVertices += 1.0;
				} else {
					epsilonDeletedVertices += 1.0;
				}
				epsilonSkippedVertices += 1.0;
			}

			// make each pair \in m also a pair \in vid1tovid2,
			// such that in the end vid1tovid2 = m.
			vid1tovid2.put(pair.v1, pair.v2);
			vid2tovid1.put(pair.v2, pair.v1);
		}

		// Substituted edges are edges that are not mapped.
		// First, create the set of all edges in SimpleGraph 2.
		Set<TwoVertices> edgesIn1 = sg1.getEdges();
		Set<TwoVertices> edgesIn2 = sg2.getEdges();

		// Second, create the set of all edges in SimpleGraph 1,
		// but translate it into an edge on vertices from SimpleGraph 2.
		// I.e.: if (v1,v2) \in <Edges from SimpleGraph 1> and
		// v1 is mapped onto v1' and v2 is mapped onto v2', then
		// (v1',v2') \in <Translated edges from SimpleGraph 1>.
		Set<TwoVertices> translatedEdgesIn1 = new HashSet<TwoVertices>();
		for (Integer i : sg1.getVertices()) {
			for (Integer j : sg1.postSet(i)) {
				Integer srcMap = vid1tovid2.get(i);
				Integer tgtMap = vid1tovid2.get(j);
				if ((srcMap != null) && (tgtMap != null)) {
					if ((srcMap != EPSILON) && (tgtMap != EPSILON)) {
						translatedEdgesIn1.add(new TwoVertices(srcMap, tgtMap));
					} else {
						epsilonSkippedEdges += 1.0;
					}
				}
			}
		}
		edgesIn2.removeAll(translatedEdgesIn1); // Edges that are skipped remain
		Set<TwoVertices> translatedEdgesIn2 = new HashSet<TwoVertices>();
		for (Integer i : sg2.getVertices()) {
			for (Integer j : sg2.postSet(i)) {
				Integer srcMap = vid2tovid1.get(i);
				Integer tgtMap = vid2tovid1.get(j);
				if ((srcMap != null) && (tgtMap != null)) {
					if ((srcMap != EPSILON) && (tgtMap != EPSILON)) {
						translatedEdgesIn2.add(new TwoVertices(srcMap, tgtMap));
					} else {
						epsilonSkippedEdges += 1.0;
					}
				}
			}
		}
		edgesIn1.removeAll(translatedEdgesIn2); // Edges that are skipped remain
		double skippedEdges = 1.0 * edgesIn1.size() + 1.0 * edgesIn2.size();
		Set<Integer> skippedVerticesIn1 = new HashSet<Integer>(sg1
				.getVertices());
		skippedVerticesIn1.removeAll(verticesFrom1Used);
		Set<Integer> skippedVerticesIn2 = new HashSet<Integer>(sg2
				.getVertices());
		skippedVerticesIn2.removeAll(verticesFrom2Used);
		double skippedVertices = skippedVerticesIn1.size()
				+ skippedVerticesIn2.size();

		if (useepsilon) {
			skippedEdges = epsilonSkippedEdges;
			skippedVertices = epsilonSkippedVertices;
		}

		return computeScore(skippedEdges, skippedVertices, substitutedVertices,
				nrSubstitutions, 0.0, 0.0);
	}
}
