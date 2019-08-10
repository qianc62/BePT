package org.processmining.analysis.rolehierarchy;

import java.util.Set;

public interface NodeLabelFormatter {
	String getLabel(Set<String> nodeNames, boolean useShortLabels);
}
