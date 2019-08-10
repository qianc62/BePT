package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class RelativeMinuteMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time) / 60000L);
	};

}
