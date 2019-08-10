package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class GuiMap {

	protected long timeOffset = 1 * 3600000L;
	protected HashMap<String, HashMap<String, ArrayList<Integer>>> map = new HashMap<String, HashMap<String, ArrayList<Integer>>>();

	public abstract String getKey(long time);

	public void put(long time, String id, int num) {
		String str = getKey(time);
		if (map.get(str) == null) {
			map.put(str, new HashMap<String, ArrayList<Integer>>());
		}

		if (map.get(str).get(id) == null) {
			map.get(str).put(id, new ArrayList<Integer>());
		}

		// put data to a map
		map.get(str).get(id).add(Integer.valueOf(num));
	}

	public HashMap<String, HashMap<String, ArrayList<Integer>>> getMap() {
		return map;
	}
}
