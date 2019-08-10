package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class RelativeRatio5Map extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time) / 5L);
	};

}
