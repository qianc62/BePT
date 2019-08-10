package org.processmining.analysis.performance.advanceddottedchartanalysis.model;

import java.util.HashMap;

public class MinMaxModel {
	protected HashMap<String, Long> minMappings = new HashMap<String, Long>();
	protected HashMap<String, Long> maxMappings = new HashMap<String, Long>();
	protected HashMap<String, Long> durationMappings = new HashMap<String, Long>();

	public void assignValue(String key, long value) {
		if (minMappings.containsKey(key)) {
			if ((long) minMappings.get(key) > value) {
				minMappings.put(key, value);
				durationMappings.put(key, maxMappings.get(key)
						- minMappings.get(key));
			}
		} else {
			minMappings.put(key, value);
			durationMappings.put(key, (long) 0);
		}

		if (maxMappings.containsKey(key)) {
			if ((long) maxMappings.get(key) < value) {
				maxMappings.put(key, value);
				durationMappings.put(key, maxMappings.get(key)
						- minMappings.get(key));
			}
		} else {
			maxMappings.put(key, value);
		}
	}

	public void increaseMaxValue(String key) {
		if (maxMappings.containsKey(key)) {
			maxMappings.put(key, (long) maxMappings.get(key) + 1);
		} else {
			maxMappings.put(key, (long) 1);
		}
	}

	public long getMaxValue(String key) {
		return maxMappings.get(key);
	}

	public long getMinValue(String key) {
		return minMappings.get(key);
	}

	public long getDurationValue(String key) {
		return durationMappings.get(key);
	}
}
