package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class ActualWeekMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time + timeOffset) / 604800000L);
	};

}
