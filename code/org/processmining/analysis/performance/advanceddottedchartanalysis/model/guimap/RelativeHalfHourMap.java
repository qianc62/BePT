package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class RelativeHalfHourMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time) / 1800000L);
	};

}
