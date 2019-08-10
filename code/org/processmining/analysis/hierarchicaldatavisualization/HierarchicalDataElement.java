package org.processmining.analysis.hierarchicaldatavisualization;

import java.util.Set;

import org.processmining.framework.models.ModelGraphVertex;

public interface HierarchicalDataElement {
	Set<ModelGraphVertex> getNodes();

	double getValue();
}
