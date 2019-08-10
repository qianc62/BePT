package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class RelativeRatio100Map extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time) / 100L);
	};

}
