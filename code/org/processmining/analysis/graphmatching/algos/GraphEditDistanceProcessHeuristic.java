package org.processmining.analysis.graphmatching.algos;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.processmining.analysis.graphmatching.graph.SimpleGraph;
import org.processmining.analysis.graphmatching.graph.TwoVertices;
import org.processmining.analysis.graphmatching.led.StringEditDistance;

/**
 * Class that implements the algorithm to compute the edit distance between two
 * SimpleGraph instances. Use the algorithm by calling the constructor with the
 * two SimpleGraph instances between which you want to compute the edit
 * distance. Then call compute(), which will return the edit distance.
 */
public class GraphEditDistanceProcessHeuristic extends DistanceAlgoAbstr
		implements DistanceAlgo {

	/**
	 * A tuple that contains a mapping that is being evaluated by the algorithm
	 * in computeAllMappings(). See cmoputeAllMappings for details.
	 */
	class CurrentMapping {
		public Set<Integer> currVerticesSG1;
		public Set<Integer> currVerticesSG2;
		public Set<TwoVertices> mapping;
		public Set<Integer> verticesSG1Used;
		public Set<Integer> verticesSG2Used;

		public CurrentMapping(Set<Integer> currVerticesSG1,
				Set<Integer> currVerticesSG2) {
			this.currVerticesSG1 = currVerticesSG1;
			this.currVerticesSG2 = currVerticesSG2;
			this.mapping = new HashSet<TwoVertices>();
			this.verticesSG1Used = new HashSet<Integer>();
			this.verticesSG2Used = new HashSet<Integer>();
		}

		public CurrentMapping(Set<Integer> currVerticesSG1,
				Set<Integer> currVerticesSG2, Set<TwoVertices> mapping,
				Set<Integer> verticesSG1Used, Set<Integer> verticesSG2Used) {
			this.currVerticesSG1 = currVerticesSG1;
			this.currVerticesSG2 = currVerticesSG2;
			this.mapping = mapping;
			this.verticesSG1Used = verticesSG1Used;
			this.verticesSG2Used = verticesSG2Used;
		}

		public boolean equals(Object o) {
			if (o instanceof CurrentMapping) {
				CurrentMapping cm = (CurrentMapping) o;
				return (currVerticesSG1.equals(cm.currVerticesSG1))
						&& (currVerticesSG2.equals(cm.currVerticesSG2))
						&& (mapping.equals(cm.mapping))
						&& (verticesSG1Used.equals(cm.verticesSG1Used))
						&& (verticesSG2Used.equals(cm.verticesSG2Used));
			} else {
				return false;
			}
		}

		public int hashCode() {
			return currVerticesSG1.hashCode() + currVerticesSG2.hashCode()
					+ mapping.hashCode() + verticesSG1Used.hashCode()
					+ verticesSG2Used.hashCode();
		}
	}

	Set<Set<TwoVertices>> finalMappings;

	/**
	 * Computes all possible mappings between the two graphs. Stores them in
	 * 'finalMappings'. (Actually it does not compute >all< mappings; it only
	 * computes mappings that are expected to be >optimal<. So the naming of the
	 * method is not entirely accurate.)
	 * 
	 * The algorithm works as follows: - initial step: set <current vertices> to
	 * the source vertices of the two graphs - Recursive step: 1. prune the set
	 * of current mappings if necessary 2. take all current mappings up to
	 * <current vertices> and extend them
	 */
	private void computeAllMappings() {
		CurrentMapping currentMapping = new CurrentMapping(
				sg1.sourceVertices(), sg2.sourceVertices());

		Set<CurrentMapping> currentMappings = new HashSet<CurrentMapping>();
		currentMappings.add(currentMapping);

		do {
			currentMappings = prune(currentMappings);
			currentMappings = step(currentMappings);
		} while (!currentMappings.isEmpty());
	}

	/**
	 * Extend each current mapping
	 */
	private Set<CurrentMapping> step(Set<CurrentMapping> currentMappings) {
		Set<CurrentMapping> newMappings = new HashSet<CurrentMapping>();

		for (CurrentMapping cm : currentMappings) {
			// For each currentMapping
			if (cm.currVerticesSG1.isEmpty() || cm.currVerticesSG2.isEmpty()) {
				// If mapping is final, then store it
				finalMappings.add(cm.mapping);
			} else {
				// If the mapping is not final,
				for (Integer i : cm.currVerticesSG1) {
					for (Integer j : cm.currVerticesSG2) {
						// take a possible pair (i,j) from the <current
						// vertices>
						// for that pair:
						Set<Integer> newVerticesSG1Used;
						Set<Integer> newVerticesSG2Used;
						Set<Integer> newCurrVerticesSG1;
						Set<Integer> newCurrVerticesSG2;
						Set<TwoVertices> newMapping;
						CurrentMapping nm;
						// 1. Create a mapping in which i is substituted for j
						if (StringEditDistance.similarity(sg1.getLabel(i), sg2
								.getLabel(j)) >= this.ledcutoff) {
							newVerticesSG1Used = new HashSet<Integer>(
									cm.verticesSG1Used);
							newVerticesSG1Used.add(i);
							newVerticesSG2Used = new HashSet<Integer>(
									cm.verticesSG2Used);
							newVerticesSG2Used.add(j);
							newCurrVerticesSG1 = new HashSet<Integer>(
									cm.currVerticesSG1);
							newCurrVerticesSG1.addAll(sg1.postSet(i));
							newCurrVerticesSG1.removeAll(newVerticesSG1Used);
							newCurrVerticesSG2 = new HashSet<Integer>(
									cm.currVerticesSG2);
							newCurrVerticesSG2.addAll(sg2.postSet(j));
							newCurrVerticesSG2.removeAll(newVerticesSG2Used);
							newMapping = new HashSet<TwoVertices>(cm.mapping);
							newMapping.add(new TwoVertices(i, j));
							nm = new CurrentMapping(newCurrVerticesSG1,
									newCurrVerticesSG2, newMapping,
									newVerticesSG1Used, newVerticesSG2Used);
							newMappings.add(nm);
						}

						// 2. Create a mapping in which i is skipped
						newVerticesSG1Used = new HashSet<Integer>(
								cm.verticesSG1Used);
						newVerticesSG1Used.add(i);
						newVerticesSG2Used = new HashSet<Integer>(
								cm.verticesSG2Used);
						newCurrVerticesSG1 = new HashSet<Integer>(
								cm.currVerticesSG1);
						newCurrVerticesSG1.addAll(sg1.postSet(i));
						newCurrVerticesSG1.removeAll(newVerticesSG1Used);
						newCurrVerticesSG2 = new HashSet<Integer>(
								cm.currVerticesSG2);
						newMapping = new HashSet<TwoVertices>(cm.mapping);
						nm = new CurrentMapping(newCurrVerticesSG1,
								newCurrVerticesSG2, newMapping,
								newVerticesSG1Used, newVerticesSG2Used);
						newMappings.add(nm);

						// 2. Create a mapping in which j is skipped
						newVerticesSG1Used = new HashSet<Integer>(
								cm.verticesSG1Used);
						newVerticesSG2Used = new HashSet<Integer>(
								cm.verticesSG2Used);
						newVerticesSG2Used.add(j);
						newCurrVerticesSG1 = new HashSet<Integer>(
								cm.currVerticesSG1);
						newCurrVerticesSG2 = new HashSet<Integer>(
								cm.currVerticesSG2);
						newCurrVerticesSG2.addAll(sg2.postSet(j));
						newCurrVerticesSG2.removeAll(newVerticesSG2Used);
						newMapping = new HashSet<TwoVertices>(cm.mapping);
						nm = new CurrentMapping(newCurrVerticesSG1,
								newCurrVerticesSG2, newMapping,
								newVerticesSG1Used, newVerticesSG2Used);
						newMappings.add(nm);
					}
				}
			}
		}

		return newMappings;
	}

	/**
	 * Prune the set of current mappings into a smaller set of current mappings,
	 * when the size of the set exceeds 'pruneWhen'. Prune by keeping only the
	 * 'pruneTo' most viable mappings. (I.e. the 'pruneTo' mappings with the
	 * smallest edit-distance.
	 */
	private Set<CurrentMapping> prune(Set<CurrentMapping> currentMappings) {
		if ((prunewhen == 0) || (currentMappings.size() < prunewhen)) {
			return currentMappings;
		}

		// Prune when size of currentMappings >= pruneWhen

		// Create a SortedMap that couples each currentMapping to its
		// edit-distance and
		// that is sorted by edit-distance.
		SortedMap<Double, CurrentMapping> mappingsByEd = new TreeMap<Double, CurrentMapping>();
		for (CurrentMapping cm : currentMappings) {
			mappingsByEd.put(editDistance(cm.mapping), cm);
		}

		// Return only the first 'pruneTo' members of that SortedMap.
		Set<CurrentMapping> newMappings = new HashSet<CurrentMapping>();
		Iterator<Map.Entry<Double, CurrentMapping>> ocms = mappingsByEd
				.entrySet().iterator();
		for (int i = 0; (i < pruneto) && ocms.hasNext(); i++) {
			newMappings.add(ocms.next().getValue());
		}
		return newMappings;
	}

	Set<TwoVertices> mappingWithMinimalDistance = null;

	/**
	 * Computes the edit distance between the two SimpleGraph instances with
	 * which the object was instantiated.
	 * 
	 * Precondition: the algorithm only returns a useful edit distance if the
	 * graphs both have at least one 'source node' (a node with no incoming
	 * arcs.) Otherwise, Double.MAX_VALUE is returned.
	 * 
	 * @return edit distance
	 */
	public double compute(SimpleGraph sg1, SimpleGraph sg2) {
		init(sg1, sg2);
		finalMappings = new HashSet<Set<TwoVertices>>();

		// Compute all mappings
		computeAllMappings();

		mappingWithMinimalDistance = null;
		double minimalDistance = Double.MAX_VALUE;

		// Find the mapping with the smallest edit distance
		for (Set<TwoVertices> mapping : finalMappings) {
			double editDistance = editDistance(mapping);
			if (editDistance < minimalDistance) {
				minimalDistance = editDistance;
				mappingWithMinimalDistance = mapping;
			}
		}
		/*
		 * for (TwoVertices pair: mappingWithMinimalDistance){
		 * System.out.println("\"" + sg1.getLabel(pair.v1) + "\" - \"" +
		 * sg2.getLabel(pair.v2) + "\""); }
		 */
		// Return the smallest edit distance
		return minimalDistance;
	}

	public Set<TwoVertices> bestMapping() {
		return this.mappingWithMinimalDistance;
	}
}
