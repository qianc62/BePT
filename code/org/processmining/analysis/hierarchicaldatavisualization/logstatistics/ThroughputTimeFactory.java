package org.processmining.analysis.hierarchicaldatavisualization.logstatistics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.analysis.hierarchicaldatavisualization.HierarchicalDataElement;
import org.processmining.analysis.logstatistics.LogStatistic;
import org.processmining.framework.models.ModelGraphVertex;

public class ThroughputTimeFactory implements LogStatisticDataElementFactory {
	public void create(String key, LogStatistic value,
			Map<String, ModelGraphVertex> mapping,
			Collection<HierarchicalDataElement> data) {
		data.add(new ThroughputTimeDataElement(key, value, mapping));
	}
}

class ThroughputTimeDataElement implements HierarchicalDataElement {

	private double value;
	private Set<ModelGraphVertex> nodes;

	public ThroughputTimeDataElement(String key, LogStatistic value,
			Map<String, ModelGraphVertex> mapping) {
		this.value = value.getTimeStampStatistics().getMax()
				- value.getTimeStampStatistics().getMin();
		this.nodes = new HashSet<ModelGraphVertex>();
		for (String uri : value.getModelReferences()) {
			this.nodes.add(mapping.get(uri));
		}
	}

	public double getValue() {
		return value;
	}

	public Set<ModelGraphVertex> getNodes() {
		return nodes;
	}
}
