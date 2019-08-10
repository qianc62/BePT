package org.processmining.analysis.ltlchecker;

import java.util.ArrayList;
import java.util.List;

public class SetsSetItem {
	private List<String> modelReferences;
	private String name;

	public SetsSetItem(String name) {
		this.name = name;
		this.modelReferences = new ArrayList<String>(0);
	}

	public SetsSetItem(String name, List<String> modelReferences) {
		this.name = name;
		this.modelReferences = modelReferences;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SetsSetItem)) {
			return false;
		}
		SetsSetItem other = (SetsSetItem) o;
		return name.equals(other.name)
				&& modelReferences.equals(other.modelReferences);
	}

	@Override
	public int hashCode() {
		return name.hashCode() + modelReferences.hashCode();
	}

	public List<String> getModelReferences() {
		return modelReferences;
	}
}