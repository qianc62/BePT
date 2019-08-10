package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

public class RelativeRatio10Map extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time) / 10L);
	};

}
