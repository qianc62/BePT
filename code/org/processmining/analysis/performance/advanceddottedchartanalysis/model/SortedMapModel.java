package org.processmining.analysis.performance.advanceddottedchartanalysis.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.processmining.analysis.performance.advanceddottedchartanalysis.ui.DottedChartPanel;

public class SortedMapModel {

	protected MinMaxModel minMaxModel = new MinMaxModel();
	protected DottedChartModel dcModel;
	protected HashMap<String, ArrayList<String>> sortedStringMap = new HashMap<String, ArrayList<String>>();
	protected HashMap<String, int[]> sortedCodeMap = new HashMap<String, int[]>();
	protected HashMap<String, HashMap<Integer, Integer>> codeMatchMapMap = new HashMap<String, HashMap<Integer, Integer>>();
	protected long[] dataArray;
	protected ArrayList<String> originalStrList;

	public SortedMapModel(DottedChartModel aDottedChartModel) {
		dcModel = aDottedChartModel;
	}

	protected void updateMinMaxValue(String time, String instanceid,
			String task, String originator, String event, long value) {
		minMaxModel.assignValue(DottedChartModel.ST_INST + time + instanceid,
				value);
		minMaxModel.assignValue(DottedChartModel.ST_ORIG + time + task, value);
		minMaxModel.assignValue(DottedChartModel.ST_EVEN + time + originator,
				value);
		minMaxModel.assignValue(DottedChartModel.ST_TASK + time + event, value);
	}

	public long[] countNumbers(String componentType) {
		int[] codes = dcModel.getCode(componentType);
		ArrayList<String> itemList = dcModel.getItemArrayList(componentType);
		long[] values = new long[itemList.size()];
		for (int i = 0; i < codes.length; i++) {
			values[codes[i]]++;
		}
		return values;
	}

	public long[] getStartTimes(String timeOption, String componentType) {
		ArrayList<String> itemList = dcModel.getItemArrayList(componentType);
		long[] values = new long[itemList.size()];
		for (int i = 0; i < itemList.size(); i++) {
			values[i] = minMaxModel.getMinValue(componentType + timeOption + i);
		}
		return values;
	}

	public long[] getStartTimes(String timeOption, String componentType,
			ArrayList<String> strList) {
		ArrayList<String> itemList = dcModel.getItemArrayList(componentType);

		long[] values = new long[strList.size()];
		int i = 0;
		for (String str : strList) {
			values[i++] = minMaxModel.getMinValue(componentType + timeOption
					+ itemList.indexOf(str));
		}
		return values;
	}

	public long[] getEndTimes(String timeOption, String componentType) {
		ArrayList<String> itemList = dcModel.getItemArrayList(componentType);

		long[] values = new long[itemList.size()];
		for (int i = 0; i < itemList.size(); i++) {
			values[i] = minMaxModel.getMaxValue(componentType + timeOption + i);
		}

		return values;
	}

	public long[] getEndTimes(String timeOption, String componentType,
			ArrayList<String> strList) {
		ArrayList<String> itemList = dcModel.getItemArrayList(componentType);

		long[] values = new long[strList.size()];
		int i = 0;
		for (String str : strList) {
			values[i++] = minMaxModel.getMaxValue(componentType + timeOption
					+ itemList.indexOf(str));
		}
		return values;
	}

	public long[] getDurations(String timeOption, String componentType,
			ArrayList<String> strList) {
		ArrayList<String> itemList = dcModel.getItemArrayList(componentType);

		long[] values = new long[strList.size()];
		int i = 0;
		for (String str : strList) {
			values[i++] = minMaxModel.getDurationValue(componentType
					+ timeOption + itemList.indexOf(str));
		}
		return values;
	}

	public long[] getDurations(String timeOption, String componentType) {
		ArrayList<String> itemList = dcModel.getItemArrayList(componentType);

		long[] values = new long[itemList.size()];
		for (int i = 0; i < itemList.size(); i++) {
			values[i] = minMaxModel.getDurationValue(componentType + timeOption
					+ i);
		}

		return values;
	}

	public ArrayList<String> getSortedItemArrayList(String timeOption,
			String componentType, String sortStardard, boolean desc) {
		if (sortStardard.equals(DottedChartPanel.ST_START_TIME)
				|| sortStardard.equals(DottedChartPanel.ST_END_TIME)
				|| sortStardard.equals(DottedChartPanel.ST_DURATION)) {
			if (!sortedStringMap.containsKey(componentType + sortStardard
					+ desc)) {
				originalStrList = dcModel.getItemArrayList(componentType);
				ArrayList<String> origianlSortedKeys = new ArrayList<String>(
						originalStrList);
				if (sortStardard.equals(DottedChartPanel.ST_START_TIME)) {
					dataArray = getStartTimes(DottedChartModel.TIME_ACTUAL,
							componentType);
				} else if (sortStardard.equals(DottedChartPanel.ST_END_TIME)) {
					dataArray = getEndTimes(DottedChartModel.TIME_ACTUAL,
							componentType);
				} else if (sortStardard.equals(DottedChartPanel.ST_DURATION)) {
					dataArray = getDurations(DottedChartModel.TIME_ACTUAL,
							componentType);
				}
				quicksort(origianlSortedKeys, 0,
						(origianlSortedKeys.size() - 1), sortStardard);

				sortedStringMap.put(componentType + sortStardard + false,
						origianlSortedKeys);

				ArrayList<String> inversedKeys = new ArrayList<String>();
				for (int k = origianlSortedKeys.size() - 1; k >= 0; k--)
					inversedKeys.add(origianlSortedKeys.get(k));
				sortedStringMap.put(componentType + sortStardard + true,
						inversedKeys);
			}
			return sortedStringMap.get(componentType + sortStardard + desc);
		} else if (!sortedStringMap.containsKey(timeOption + componentType
				+ sortStardard + desc)) {
			originalStrList = dcModel.getItemArrayList(componentType);
			ArrayList<String> origianlSortedKeys = new ArrayList<String>(
					originalStrList);
			if (!sortStardard.equals(DottedChartPanel.STR_NONE)) {
				if (sortStardard.equals(DottedChartPanel.ST_SIZE)) {
					dataArray = countNumbers(componentType);
				} else if (sortStardard.equals(DottedChartPanel.ST_FIRST_EVENT)) {
					dataArray = getStartTimes(timeOption, componentType);
				} else if (sortStardard.equals(DottedChartPanel.ST_LAST_EVENT)) {
					dataArray = getEndTimes(timeOption, componentType);
				} else if (sortStardard.equals(DottedChartPanel.ST_SPAN)) {
					dataArray = getDurations(timeOption, componentType);
				}
				quicksort(origianlSortedKeys, 0,
						(origianlSortedKeys.size() - 1), sortStardard);
			}
			sortedStringMap.put(timeOption + componentType + sortStardard
					+ false, origianlSortedKeys);

			ArrayList<String> inversedKeys = new ArrayList<String>();
			for (int k = origianlSortedKeys.size() - 1; k >= 0; k--)
				inversedKeys.add(origianlSortedKeys.get(k));
			sortedStringMap.put(timeOption + componentType + sortStardard
					+ true, inversedKeys);
		}
		return sortedStringMap.get(timeOption + componentType + sortStardard
				+ desc);
	}

	public HashMap<Integer, Integer> getSortedMapping(String timeOption,
			String str, String sort, boolean desc) {
		ArrayList<String> strList = dcModel.getItemArrayList(str);
		if (sort.equals(DottedChartPanel.ST_START_TIME)
				|| sort.equals(DottedChartPanel.ST_END_TIME)
				|| sort.equals(DottedChartPanel.ST_DURATION)) {
			if (!codeMatchMapMap.containsKey(str + sort + desc)) {
				HashMap<Integer, Integer> codeMatchMap = new HashMap<Integer, Integer>();
				ArrayList<String> tempsortedKeys = getSortedItemArrayList(
						timeOption, str, sort, desc);
				for (int k = 0; k < strList.size(); k++) {
					codeMatchMap.put(k, tempsortedKeys.indexOf(strList.get(k)));
				}
				codeMatchMapMap.put(str + sort + desc, codeMatchMap);
			}
			return codeMatchMapMap.get(str + sort + desc);
		} else if (!codeMatchMapMap.containsKey(timeOption + str + sort + desc)) {
			HashMap<Integer, Integer> codeMatchMap = new HashMap<Integer, Integer>();
			ArrayList<String> tempsortedKeys = getSortedItemArrayList(
					timeOption, str, sort, desc);
			for (int k = 0; k < strList.size(); k++) {
				codeMatchMap.put(k, tempsortedKeys.indexOf(strList.get(k)));
			}
			codeMatchMapMap.put(timeOption + str + sort + desc, codeMatchMap);
		}
		return codeMatchMapMap.get(timeOption + str + sort + desc);
	}

	public int[] getSortedCode(String timeOption, String str, String sort,
			boolean desc) {
		if (sort.equals(DottedChartPanel.STR_NONE)) {
			return dcModel.getCode(str);
		}
		if (sort.equals(DottedChartPanel.ST_START_TIME)
				|| sort.equals(DottedChartPanel.ST_END_TIME)
				|| sort.equals(DottedChartPanel.ST_DURATION)) {
			if (!sortedCodeMap.containsKey(str + sort + desc)) {
				int[] codes = dcModel.getCode(str);
				int[] sortedCode = new int[codes.length];
				HashMap<Integer, Integer> codeMatchMap = getSortedMapping(
						timeOption, str, sort, desc);

				for (int k = 0; k < codes.length; k++) {
					sortedCode[k] = codeMatchMap.get(codes[k]);
				}
				sortedCodeMap.put(str + sort + desc, sortedCode);
			}

			return sortedCodeMap.get(str + sort + desc);

		} else if (!sortedCodeMap.containsKey(timeOption + str + sort + desc)) {
			int[] codes = dcModel.getCode(str);
			int[] sortedCode = new int[codes.length];
			HashMap<Integer, Integer> codeMatchMap = getSortedMapping(
					timeOption, str, sort, desc);

			for (int k = 0; k < codes.length; k++) {
				sortedCode[k] = codeMatchMap.get(codes[k]);
			}
			sortedCodeMap.put(timeOption + str + sort + desc, sortedCode);
		}

		return sortedCodeMap.get(timeOption + str + sort + desc);
	}

	// ////////quick sort
	private void quicksort(ArrayList<String> key, int left, int right,
			String type) {
		if (right <= left)
			return;
		int i = partition(key, left, right, type);
		quicksort(key, left, i - 1, type);
		quicksort(key, i + 1, right, type);
	}

	private int partition(ArrayList<String> key, int left, int right,
			String type) {
		int i = left - 1;
		int j = right;
		while (true) {
			while (less(key, (++i), right, type))
				// find item on left to swap
				; // a[right] acts as sentinel
			while (less(key, right, (--j), type))
				// find item on right to swap
				if (j == left)
					break; // don't go out-of-bounds
			if (i >= j)
				break; // check if pointers cross
			exch(key, i, j); // swap two elements into place
		}
		exch(key, i, right); // swap with partition element
		return i;
	}

	// is x < y ?
	private boolean less(ArrayList<String> keys, int i, int j, String type) {
		if (type.equals(DottedChartPanel.ST_NAME)) {
			return (keys.get(i).compareTo(keys.get(j)) < 0);
		} else { // if(type.equals(DottedChartPanel.ST_SIZE)||type.equals(DottedChartPanel.ST_START_TIME)||
			// type.equals(DottedChartPanel.ST_END_TIME)||type.equals(DottedChartPanel.ST_DURATION)){
			return (dataArray[originalStrList.indexOf(keys.get(i))] < dataArray[originalStrList
					.indexOf(keys.get(j))]);
		}
	}

	// exchange a[i] and a[j]
	private void exch(ArrayList<String> key, int i, int j) {
		String swap = key.get(i);
		key.set(i, key.get(j));
		key.set(j, swap);
	}
}
