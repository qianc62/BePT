package org.processmining.analysis.performance.advanceddottedchartanalysis.model.guimap;

import java.util.HashMap;

import org.processmining.analysis.performance.advanceddottedchartanalysis.model.DottedChartModel;

public class GuiMapModel {

	// actual map
	protected HashMap<String, ActualSecondMap> actualSecondMap = new HashMap<String, ActualSecondMap>();
	protected HashMap<String, ActualMinuteMap> actualMinuteMap = new HashMap<String, ActualMinuteMap>();
	protected HashMap<String, ActualHalfHourMap> actualHalfHourMap = new HashMap<String, ActualHalfHourMap>();
	protected HashMap<String, ActualHourMap> hourMap = new HashMap<String, ActualHourMap>();
	protected HashMap<String, ActualMonthMap> monthMap = new HashMap<String, ActualMonthMap>();
	protected HashMap<String, ActualWeekMap> weekMap = new HashMap<String, ActualWeekMap>();
	protected HashMap<String, ActualDayMap> dayMap = new HashMap<String, ActualDayMap>();

	// relative map
	protected HashMap<String, RelativeSecondMap> relativeSecondMap = new HashMap<String, RelativeSecondMap>();
	protected HashMap<String, RelativeMinuteMap> relativeMinuteMap = new HashMap<String, RelativeMinuteMap>();
	protected HashMap<String, RelativeHalfHourMap> relativeHalfHourMap = new HashMap<String, RelativeHalfHourMap>();
	protected HashMap<String, RelativeHourMap> relativeHourMap = new HashMap<String, RelativeHourMap>();
	protected HashMap<String, RelativeDayMap> relativeDayMap = new HashMap<String, RelativeDayMap>();

	// relative ratio map
	protected HashMap<String, RelativeRatio1Map> relativeRatio1Map = new HashMap<String, RelativeRatio1Map>();
	protected HashMap<String, RelativeRatio5Map> relativeRatio5Map = new HashMap<String, RelativeRatio5Map>();
	protected HashMap<String, RelativeRatio10Map> relativeRatio10Map = new HashMap<String, RelativeRatio10Map>();
	protected HashMap<String, RelativeRatio50Map> relativeRatio50Map = new HashMap<String, RelativeRatio50Map>();
	protected HashMap<String, RelativeRatio100Map> relativeRatio100Map = new HashMap<String, RelativeRatio100Map>();

	// logical map
	protected HashMap<String, LogicalMap> logicalMap = new HashMap<String, LogicalMap>();

	public void makeActualMap(String key, long time, String id, int num) {
		if (monthMap.get(key) == null) {
			monthMap.put(key, new ActualMonthMap());
		}
		monthMap.get(key).put(time, id, num);

		if (weekMap.get(key) == null) {
			weekMap.put(key, new ActualWeekMap());
		}
		weekMap.get(key).put(time, id, num);

		if (dayMap.get(key) == null) {
			dayMap.put(key, new ActualDayMap());
		}
		dayMap.get(key).put(time, id, num);

		if (hourMap.get(key) == null) {
			hourMap.put(key, new ActualHourMap());
		}
		hourMap.get(key).put(time, id, num);

		if (actualHalfHourMap.get(key) == null) {
			actualHalfHourMap.put(key, new ActualHalfHourMap());
		}
		actualHalfHourMap.get(key).put(time, id, num);

		if (actualMinuteMap.get(key) == null) {
			actualMinuteMap.put(key, new ActualMinuteMap());
		}
		actualMinuteMap.get(key).put(time, id, num);

		if (actualSecondMap.get(key) == null) {
			actualSecondMap.put(key, new ActualSecondMap());
		}
		actualSecondMap.get(key).put(time, id, num);
	}

	public void makeRelativeTimeMap(String key, long time, String id, int num) {
		if (relativeDayMap.get(key) == null) {
			relativeDayMap.put(key, new RelativeDayMap());
		}
		relativeDayMap.get(key).put(time, id, num);

		if (relativeHourMap.get(key) == null) {
			relativeHourMap.put(key, new RelativeHourMap());
		}
		relativeHourMap.get(key).put(time, id, num);

		if (relativeHalfHourMap.get(key) == null) {
			relativeHalfHourMap.put(key, new RelativeHalfHourMap());
		}
		relativeHalfHourMap.get(key).put(time, id, num);

		if (relativeMinuteMap.get(key) == null) {
			relativeMinuteMap.put(key, new RelativeMinuteMap());
		}
		relativeMinuteMap.get(key).put(time, id, num);

		if (relativeSecondMap.get(key) == null) {
			relativeSecondMap.put(key, new RelativeSecondMap());
		}
		relativeSecondMap.get(key).put(time, id, num);

	}

	public void makeRelativeRatioMap(String key, long time, String id, int num) {

		if (relativeRatio1Map.get(key) == null) {
			relativeRatio1Map.put(key, new RelativeRatio1Map());
		}
		relativeRatio1Map.get(key).put(time, id, num);

		if (relativeRatio5Map.get(key) == null) {
			relativeRatio5Map.put(key, new RelativeRatio5Map());
		}
		relativeRatio5Map.get(key).put(time, id, num);

		if (relativeRatio10Map.get(key) == null) {
			relativeRatio10Map.put(key, new RelativeRatio10Map());
		}
		relativeRatio10Map.get(key).put(time, id, num);

		if (relativeRatio50Map.get(key) == null) {
			relativeRatio50Map.put(key, new RelativeRatio50Map());
		}
		relativeRatio50Map.get(key).put(time, id, num);

		if (relativeRatio100Map.get(key) == null) {
			relativeRatio100Map.put(key, new RelativeRatio100Map());
		}
		relativeRatio100Map.get(key).put(time, id, num);
	}

	public void makeLogicalMap(String key, long time, String id, int num) {
		if (logicalMap.get(key) == null) {
			logicalMap.put(key, new LogicalMap());
		}
		logicalMap.get(key).put(time, id, num);
	}

	public GuiMap getMap(String key, String type) {
		if (key.startsWith(DottedChartModel.TIME_ACTUAL)) {
			if (type.equals("month")) {
				return monthMap.get(key);
			} else if (type.equals("week")) {
				return weekMap.get(key);
			} else if (type.equals("day")) {
				return dayMap.get(key);
			} else if (type.equals("hour")) {
				return hourMap.get(key);
			} else if (type.equals("halfhour")) {
				return actualHalfHourMap.get(key);
			} else if (type.equals("minute")) {
				return actualMinuteMap.get(key);
			} else if (type.equals("second")) {
				return actualSecondMap.get(key);
			}
		} else if (key.startsWith(DottedChartModel.TIME_RELATIVE_TIME)) {
			if (type.equals("day")) {
				return relativeDayMap.get(key);
			} else if (type.equals("hour")) {
				return relativeHourMap.get(key);
			} else if (type.equals("halfhour")) {
				return relativeHalfHourMap.get(key);
			} else if (type.equals("minute")) {
				return relativeMinuteMap.get(key);
			} else if (type.equals("second")) {
				return relativeMinuteMap.get(key);
			}
		} else if (key.startsWith(DottedChartModel.TIME_RELATIVE_RATIO)) {
			if (type.equals("1")) {
				return relativeRatio1Map.get(key);
			} else if (type.equals("5")) {
				return relativeRatio5Map.get(key);
			} else if (type.equals("10")) {
				return relativeRatio10Map.get(key);
			} else if (type.equals("50")) {
				return relativeRatio50Map.get(key);
			} else if (type.equals("100")) {
				return relativeRatio100Map.get(key);
			}
		} else if (key.startsWith(DottedChartModel.TIME_LOGICAL_RELATIVE)) {
			return logicalMap.get(key);
		}
		return null;
	}
}
