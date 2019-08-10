package org.processmining.analysis.logstatistics;

import java.util.Collection;
import java.util.HashSet;

public class ValueWithModelReferences {
	private long value;
	private Collection<String> modelReferences;

	public ValueWithModelReferences(long value,
			Collection<String> elementModelReferences,
			Collection<String> originatorModelReferences) {
		this.value = value;
		this.modelReferences = new HashSet<String>();
		if (elementModelReferences != null) {
			this.modelReferences.addAll(elementModelReferences);
		}
		if (originatorModelReferences != null) {
			this.modelReferences.addAll(originatorModelReferences);
		}
	}

	public long getValue() {
		return value;
	}

	public Collection<String> getModelReferences() {
		return modelReferences;
	}
}
