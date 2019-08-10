package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class ActualHourMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time + timeOffset) / 3600000L);
	};

}
