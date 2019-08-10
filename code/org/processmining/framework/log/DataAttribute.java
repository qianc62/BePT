package org.processmining.framework.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataAttribute {

	private final String name;
	private String value;
	private List<String> modelReferences;

	public DataAttribute(String name, String value) {
		this(name, value, null);
	}

	public DataAttribute(String name, String value, List<String> modelReferences) {
		this.name = name;
		this.value = value;
		setModelReferences(modelReferences);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getModelReferences() {
		return modelReferences;
	}

	public String toString() {
		return "Attribute(name='" + name + "', value='" + value
				+ "', modelrefs='" + modelReferences + "')";
	}

	public void setModelReferences(List<String> modelReferences) {
		this.modelReferences = Collections
				.unmodifiableList(modelReferences == null ? new ArrayList<String>()
						: new ArrayList<String>(modelReferences));
	}
}
