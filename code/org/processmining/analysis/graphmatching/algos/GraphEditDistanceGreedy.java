package org.processmining.analysis.graphmatching.algos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.processmining.analysis.graphmatching.graph.SimpleGraph;
import org.processmining.analysis.graphmatching.graph.TwoVertices;
import org.processmining.analysis.graphmatching.led.StringEditDistance;

/**
 * Class that implements the algorithm to compute the edit distance between two
 * SimpleGraph instances. Use the algorithm by calling the constructor with the
 * two SimpleGraph instances between which you want to compute the edit
 * distance. Then call compute(), which will return the edit distance.
 */
public class GraphEditDistanceGreedy extends DistanceAlgoAbstr implements
		DistanceAlgo {

	private Set<TwoVertices> times(Set<Integer> a, Set<Integer> b) {
		Set<TwoVertices> result = new HashSet<TwoVertices>();
		for (Integer ea : a) {
			for (Integer eb : b) {
				if (StringEditDistance.similarity(sg1.getLabel(ea), sg2
						.getLabel(eb)) >= this.ledcutoff) {
					result.add(new TwoVertices(ea, eb));
				}
			}
		}
		return result;
	}

	Set<TwoVertices> mapping = null;

	public double compute(SimpleGraph sg1, SimpleGraph sg2) {
		init(sg1, sg2);

		// INIT
		mapping = new HashSet<TwoVertices>();
		Set<TwoVertices> openCouples = times(sg1.getVertices(), sg2
				.getVertices());
		double shortestEditDistance = Double.MAX_VALUE;
		Random randomized = new Random();

		// STEP
		boolean doStep = true;
		while (doStep) {
			doStep = false;
			Vector<TwoVertices> bestCandidates = new Vector<TwoVertices>();
			double newShortestEditDistance = shortestEditDistance;

			for (TwoVertices couple : openCouples) {
				Set<TwoVertices> newMapping = new HashSet<TwoVertices>(mapping);
				newMapping.add(couple);
				double newEditDistance = dogrouping ? this
						.groupedEditDistance(newMapping) : this
						.editDistance(newMapping);

				if (newEditDistance < newShortestEditDistance) {
					bestCandidates = new Vector<TwoVertices>();
					bestCandidates.add(couple);
					newShortestEditDistance = newEditDistance;
				} else if (newEditDistance == newShortestEditDistance) {
					bestCandidates.add(couple);
				}
			}

			if (bestCandidates.size() > 0) {
				// Choose a random candidate
				TwoVertices couple = bestCandidates.get(randomized
						.nextInt(bestCandidates.size()));

				if (dogrouping) {
					openCouples.remove(couple);
				} else {
					Set<TwoVertices> newOpenCouples = new HashSet<TwoVertices>();
					for (TwoVertices p : openCouples) {
						if (!p.v1.equals(couple.v1) && !p.v2.equals(couple.v2)) {
							newOpenCouples.add(p);
						}
					}
					openCouples = newOpenCouples;
				}

				mapping.add(couple);
				shortestEditDistance = newShortestEditDistance;
				doStep = true;
			}
		}

		// Return the smallest edit distance
		return shortestEditDistance;
	}

	public Set<TwoVertices> bestMapping() {
		return mapping;
	}

	protected double groupedEditDistance(Set<TwoVertices> m) {
		Set<Integer> verticesFrom1Used = new HashSet<Integer>();
		Set<Integer> verticesFrom2Used = new HashSet<Integer>();

		// Relate each vertex to a group
		Map<Integer, Integer> vid1togid = new HashMap<Integer, Integer>();
		Map<Integer, Integer> vid2togid = new HashMap<Integer, Integer>();
		Map<Integer, Set<Integer>> gidtovid1 = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Set<Integer>> gidtovid2 = new HashMap<Integer, Set<Integer>>();
		int gid = 1;

		double substitutedVertices = 0.0;
		for (TwoVertices pair : m) {
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
				substitutionDistance = 1.0 - StringEditDistance.similarity(sg1
						.getLabel(pair.v1), sg2.getLabel(pair.v2));
			}
			substitutedVertices += substitutionDistance;

			Integer groupOf1 = vid1togid.get(pair.v1);
			Integer groupOf2 = vid2togid.get(pair.v2);
			if ((groupOf1 == null) && (groupOf2 == null)) {
				vid1togid.put(pair.v1, gid);
				vid2togid.put(pair.v2, gid);
				Set<Integer> elts1ingroup = new HashSet<Integer>();
				elts1ingroup.add(pair.v1);
				gidtovid1.put(gid, elts1ingroup);
				Set<Integer> elts2ingroup = new HashSet<Integer>();
				elts2ingroup.add(pair.v2);
				gidtovid2.put(gid, elts2ingroup);
				gid++;
			} else if ((groupOf1 != null) && (groupOf2 == null)) {
				vid2togid.put(pair.v2, groupOf1);
				Set<Integer> elts2ingroup = new HashSet<Integer>();
				elts2ingroup.add(pair.v2);
				gidtovid2.put(groupOf1, elts2ingroup);
			} else if ((groupOf1 == null) && (groupOf2 != null)) {
				vid1togid.put(pair.v1, groupOf2);
				Set<Integer> elts1ingroup = new HashSet<Integer>();
				elts1ingroup.add(pair.v1);
				gidtovid1.put(groupOf2, elts1ingroup);
			} else if (!groupOf1.equals(groupOf2)) { // Both elements are in a
				// different group
				Integer usegroup = Math.min(groupOf1, groupOf2);
				Set<Integer> elts1ingroup = new HashSet<Integer>();
				Set<Integer> elts2ingroup = new HashSet<Integer>();
				for (Integer elt1 : gidtovid1.get(groupOf1)) {
					vid1togid.put(elt1, usegroup);
					elts1ingroup.add(elt1);
				}
				for (Integer elt1 : gidtovid1.get(groupOf2)) {
					vid1togid.put(elt1, usegroup);
					elts1ingroup.add(elt1);
				}
				for (Integer elt2 : gidtovid2.get(groupOf1)) {
					vid2togid.put(elt2, usegroup);
					elts2ingroup.add(elt2);
				}
				for (Integer elt2 : gidtovid2.get(groupOf2)) {
					vid2togid.put(elt2, usegroup);
					elts2ingroup.add(elt2);
				}
				gidtovid1.put(usegroup, elts1ingroup);
				gidtovid2.put(usegroup, elts2ingroup);
			}
		}
		double skippedVertices = (double) totalNrVertices
				- (double) verticesFrom1Used.size()
				- (double) verticesFrom2Used.size();

		// Substituted edges are edges that are not mapped.
		// First, create the set of all edges in SimpleGraph 2.
		Set<TwoVertices> edgesIn1 = sg1.getEdges();
		Set<TwoVertices> edgesIn2 = sg2.getEdges();

		Set<TwoVertices> translatedEdgesIn1 = new HashSet<TwoVertices>();
		Set<TwoVertices> translatedEdgesIn2 = new HashSet<TwoVertices>();

		double groupedEdges = 0.0;
		for (TwoVertices e1 : edgesIn1) {
			Integer gsrc = vid1togid.get(e1.v1);
			Integer gtgt = vid1togid.get(e1.v2);
			if ((gsrc != null) && (gtgt != null)) {
				if (gsrc.equals(gtgt)) {
					groupedEdges += 1.0;
				} else {
					translatedEdgesIn1.add(new TwoVertices(gsrc, gtgt));
				}
			}
		}
		for (TwoVertices e2 : edgesIn2) {
			Integer gsrc = vid2togid.get(e2.v1);
			Integer gtgt = vid2togid.get(e2.v2);
			if ((gsrc != null) && (gtgt != null)) {
				if (gsrc.equals(gtgt)) {
					groupedEdges += 1.0;
				} else {
					translatedEdgesIn2.add(new TwoVertices(gsrc, gtgt));
				}
			}
		}

		// Grouped vertices
		Set<Integer> groups = new HashSet<Integer>();
		double groupedVertices = 0.0;
		for (Integer groupid : vid1togid.values())
			groups.add(groupid);
		for (Integer groupid : vid2togid.values())
			groups.add(groupid);
		for (Integer groupid : groups) {
			double groupsize1 = (double) gidtovid1.get(groupid).size();
			if (groupsize1 > 1.0) {
				groupedVertices += groupsize1;
			}
			double groupsize2 = (double) gidtovid2.get(groupid).size();
			if (groupsize2 > 1.0) {
				groupedVertices += groupsize2;
			}
		}

		translatedEdgesIn1.retainAll(translatedEdgesIn2); // These are mapped
		// edges
		double mappedEdges = (double) translatedEdgesIn1.size();
		double skippedEdges = (double) edgesIn1.size()
				+ (double) edgesIn2.size() - groupedEdges - mappedEdges;

		double vskip = skippedVertices / (1.0 * totalNrVertices);
		double vgroup = groupedVertices / (1.0 * totalNrVertices);
		double vsubs = (2.0 * substitutedVertices)
				/ (1.0 * totalNrVertices - skippedVertices);
		double editDistance;
		if (totalNrEdges == 0) {
			editDistance = ((weightSkippedVertex * vskip)
					+ (weightGroupedVertex * vgroup) + (weightSubstitutedVertex * vsubs))
					/ (weightSkippedVertex + weightSubstitutedVertex + weightGroupedVertex);
		} else {
			double eskip = (skippedEdges / (1.0 * totalNrEdges));
			editDistance = ((weightSkippedVertex * vskip)
					+ (weightGroupedVertex * vgroup)
					+ (weightSubstitutedVertex * vsubs) + (weightSkippedEdge * eskip))
					/ (weightSkippedVertex + weightSubstitutedVertex
							+ weightSkippedEdge + weightGroupedVertex);
		}
		return ((editDistance >= 0.0) && (editDistance <= 1.0)) ? editDistance
				: 1.0;
	}
}
