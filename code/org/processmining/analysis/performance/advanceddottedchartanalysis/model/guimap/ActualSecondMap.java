package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class ActualSecondMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time + timeOffset) / 1000L);
	};

}
