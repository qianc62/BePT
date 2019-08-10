package org.processmining.analysis.traceclustering.model;

import java.util.ArrayList;
import java.util.List;

import org.processmining.analysis.traceclustering.util.EfficientSparseDoubleArray;

public class InstancePoint {

	protected List<String> itemKeys;
	protected EfficientSparseDoubleArray itemValues;

	public InstancePoint() {
		itemKeys = new ArrayList<String>();
		itemValues = new EfficientSparseDoubleArray(0.0);
	}

	public List<String> getItemKeys() {
		return itemKeys;
	}

	public int size() {
		return itemKeys.size();
	}

	public void set(String itemKey, double value) {
		int index = itemKeys.indexOf(itemKey);
		if (index < 0) {
			index = itemKeys.size();
			itemKeys.add(itemKey);
		}
		itemValues.set(index, value);
	}

	public double get(String itemKey) {
		int index = itemKeys.indexOf(itemKey);
		if (index < 0) {
			throw new AssertionError(
					"Item key not contained in this instance point!");
		} else {
			return itemValues.get(index);
		}
	}

}
