package org.processmining.analysis.hierarchicaldatavisualization.logstatistics;

import java.util.Collection;
import java.util.Map;

import org.processmining.analysis.hierarchicaldatavisualization.HierarchicalDataElement;
import org.processmining.analysis.logstatistics.LogStatistic;
import org.processmining.framework.models.ModelGraphVertex;

public interface LogStatisticDataElementFactory {
	void create(String key, LogStatistic value,
			Map<String, ModelGraphVertex> vertexMapping,
			Collection<HierarchicalDataElement> data);
}
