package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class ActualHalfHourMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time + timeOffset) / 1800000L);
	};

}
