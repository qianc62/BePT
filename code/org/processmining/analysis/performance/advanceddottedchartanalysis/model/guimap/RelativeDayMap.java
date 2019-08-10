package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class RelativeDayMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((int) ((time) / 86400000L));
	};

}
