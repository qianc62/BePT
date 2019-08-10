package org.processmining.analysis.graphmatching.algos;

import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.processmining.analysis.graphmatching.graph.SimpleGraph;
import org.processmining.analysis.graphmatching.graph.TwoVertices;
import org.processmining.analysis.graphmatching.led.StringEditDistance;

public class GraphEditDistanceAStarSim extends DistanceAlgoAbstr {

	private Set<Integer> partition1;
	private Set<Integer> partition2;

	private double labelSubstitutionCost(Integer v1, Integer v2) {
		if (partition1 != null)
			if ((partition1.contains(v1) && !partition2.contains(v2))
					|| (!partition1.contains(v1) && partition2.contains(v2)))
				return Double.POSITIVE_INFINITY;
		double led = 1.0 - StringEditDistance.similarity(sg1.getLabel(v1), sg2
				.getLabel(v2));
		return led > this.ledcutoff ? Double.POSITIVE_INFINITY : led;
	}

	public double compute(SimpleGraph sg1, SimpleGraph sg2) {
		double accept_threshold = Double.POSITIVE_INFINITY;
		PriorityQueue<Mapping> open = new PriorityQueue<Mapping>();
		PriorityQueue<Mapping> fullMappings = new PriorityQueue<Mapping>();
		boolean matched = false;
		Mapping m;

		init(sg1, sg2);
		Integer v1 = sg1.getVertices().iterator().next();
		for (Integer v2 : sg2.getVertices()) {
			double labelSubs = labelSubstitutionCost(v1, v2);
			if (labelSubs != Double.POSITIVE_INFINITY) {
				m = new Mapping();
				m.step(v1, v2, labelSubs);
				m.updateCost(this);
				open.add(m);
				matched = true;
			}
		}

		if (!matched) {
			m = new Mapping();
			m.step(v1, EPSILON);
			m.updateCost(this);
			open.add(m);
		}

		while (!open.isEmpty()) {
			Mapping p = open.remove();

			if (p.getCost() > accept_threshold)
				break;
			if (p.remaining1.size() == 0 && p.remaining2.size() == 0) {
				fullMappings.add(p);
				accept_threshold = p.getCost();
				continue;
			}
			if (p.remaining1.size() > 0 && p.remaining2.size() > 0) {
				matched = false;
				Integer vk = p.remaining1.get(0);
				for (Integer w : p.remaining2) {
					double labelSubs = labelSubstitutionCost(vk, w);
					if (labelSubs != Double.POSITIVE_INFINITY) {
						m = p.clone();
						m.step(vk, w, labelSubs);
						m.updateCost(this);
						open.add(m);
						matched = true;
					}
				}

				if (!matched) {
					p.step(vk, EPSILON);
					p.updateCost(this);
					open.add(p);
				}
			} else if (p.remaining1.size() > 0) {
				Integer vk = p.remaining1.get(0);
				p.step(vk, EPSILON);
				p.updateCost(this);
				open.add(p);
			} else {
				Integer vk = p.remaining2.get(0);
				p.step(EPSILON, vk);
				p.updateCost(this);
				open.add(p);
			}
		}

		Mapping mapping = fullMappings.remove();

		bestMapping = new HashSet<TwoVertices>();
		for (Map.Entry<Integer, Integer> e : mapping.mappingsFrom1.entrySet()) {
			if (e.getValue() != EPSILON) {
				bestMapping.add(new TwoVertices(e.getKey(), e.getValue()));
			}
		}

		return mapping.cost;
	}

	Set<TwoVertices> bestMapping = null;

	public Set<TwoVertices> bestMapping() {
		return bestMapping;
	}

	public void setPartitions(Set<Integer> functions1, Set<Integer> functions2) {
		this.partition1 = functions1;
		this.partition2 = functions2;
	}
}
