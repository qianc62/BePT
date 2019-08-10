package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class RelativeHourMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time) / 3600000L);
	};

}
