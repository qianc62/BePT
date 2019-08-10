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

public class GraphEditDistanceAllOptions extends DistanceAlgoAbstr implements
		DistanceAlgo {

	class UnfinishedMapping {
		public Set<Integer> freeVertices1;
		public Set<Integer> freeVertices2;
		public Set<TwoVertices> mapping;

		public UnfinishedMapping() {
			this.freeVertices1 = new HashSet<Integer>();
			this.freeVertices2 = new HashSet<Integer>();
			this.mapping = new HashSet<TwoVertices>();
		}

		public UnfinishedMapping(Set<Integer> freeVertices1,
				Set<Integer> freeVertices2, Set<TwoVertices> mapping) {
			this.freeVertices1 = new HashSet<Integer>(freeVertices1);
			this.freeVertices2 = new HashSet<Integer>(freeVertices2);
			this.mapping = new HashSet<TwoVertices>(mapping);
		}

		public UnfinishedMapping(Set<Integer> freeVertices1,
				Set<Integer> freeVertices2) {
			this.freeVertices1 = new HashSet<Integer>(freeVertices1);
			this.freeVertices2 = new HashSet<Integer>(freeVertices2);
			this.mapping = new HashSet<TwoVertices>();
		}

		public boolean equals(Object o) {
			if (o instanceof UnfinishedMapping) {
				UnfinishedMapping op = (UnfinishedMapping) o;
				return op.freeVertices1.equals(this.freeVertices1)
						&& op.freeVertices2.equals(this.freeVertices2)
						&& op.mapping.equals(this.mapping);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return this.freeVertices1.hashCode()
					+ this.freeVertices2.hashCode() + this.mapping.hashCode();
		}
	}

	Set<Set<TwoVertices>> finalMappings;
	Set<TwoVertices> mappingWithMinimalDistance = null;

	public Set<TwoVertices> bestMapping() {
		return this.mappingWithMinimalDistance;
	}

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

	private void computeAllMappings() {
		Set<UnfinishedMapping> unfinishedMappings = new HashSet<UnfinishedMapping>();
		finalMappings = new HashSet<Set<TwoVertices>>();

		unfinishedMappings.add(new UnfinishedMapping(sg1.getVertices(), sg2
				.getVertices()));

		do {
			unfinishedMappings = prune(unfinishedMappings);
			unfinishedMappings = step(unfinishedMappings);
		} while (!unfinishedMappings.isEmpty());
	}

	private Set<UnfinishedMapping> prune(Set<UnfinishedMapping> ufs) {
		if ((prunewhen == 0) || (ufs.size() < prunewhen)) {
			return ufs;
		}

		SortedMap<Double, UnfinishedMapping> mappingsByEd = new TreeMap<Double, UnfinishedMapping>();
		for (UnfinishedMapping uf : ufs) {
			mappingsByEd.put(editDistance(uf.mapping), uf);
		}

		// Return only the first 'pruneTo' members of that SortedMap.
		Set<UnfinishedMapping> newMappings = new HashSet<UnfinishedMapping>();
		Iterator<Map.Entry<Double, UnfinishedMapping>> ocms = mappingsByEd
				.entrySet().iterator();
		for (int i = 0; (i < pruneto) && ocms.hasNext(); i++) {
			newMappings.add(ocms.next().getValue());
		}
		return newMappings;

	}

	private Set<UnfinishedMapping> step(Set<UnfinishedMapping> ufs) {
		Set<UnfinishedMapping> newUfs = new HashSet<UnfinishedMapping>();

		for (UnfinishedMapping uf : ufs) {
			if (uf.freeVertices1.isEmpty() || uf.freeVertices2.isEmpty()) {
				finalMappings.add(uf.mapping);
			} else {
				for (Integer f1 : uf.freeVertices1) {
					for (Integer f2 : uf.freeVertices2) {
						if (StringEditDistance.similarity(
								this.sg1.getLabel(f1), this.sg2.getLabel(f2)) >= ledcutoff) {
							UnfinishedMapping newMapping = new UnfinishedMapping(
									uf.freeVertices1, uf.freeVertices2,
									uf.mapping);
							newMapping.freeVertices1.remove(f1);
							newMapping.freeVertices2.remove(f2);
							newMapping.mapping.add(new TwoVertices(f1, f2));

							newUfs.add(newMapping);
						}
					}
					// Must also map f1 to 'nothing'
					UnfinishedMapping newMapping = new UnfinishedMapping(
							uf.freeVertices1, uf.freeVertices2, uf.mapping);
					newMapping.freeVertices1.remove(f1);
					newUfs.add(newMapping);
				}
			}
		}

		return newUfs;
	}
}
