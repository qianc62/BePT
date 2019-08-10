/*
 * Created on Jun 17, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning.freqstat;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * Class for keeping statistics of and performing analysis on the frequency in
 * which (low-level) events occur.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class FrequencyStatistics {

	protected TreeMap distances = null;
	protected long entryCounter = 0;
	protected long valueCounter = 0;

	/**
	 * constructor
	 */
	public FrequencyStatistics() {
		initialize();
	}

	/**
	 * resets the object to its initial state
	 */
	public void initialize() {
		distances = new TreeMap();
		entryCounter = 0;
		valueCounter = 0;
	}

	/**
	 * adds a newly discovered value to the set
	 * 
	 * @param distance
	 */
	public void addValue(long distance) {
		Long distObj = new Long(distance);
		Long countObj = null;
		if (distances.containsKey(distObj)) {
			countObj = (Long) distances.get(distObj);
		} else {
			countObj = new Long(0);
			valueCounter++;
		}
		countObj = new Long(countObj.longValue() + 1);
		distances.put(distObj, countObj);
		entryCounter++;
	}

	/**
	 * @return the total number of represented values
	 */
	public long getNumberOfEntries() {
		return entryCounter;
	}

	/**
	 * @return the number of distinct represented values
	 */
	public long getNumberOfValues() {
		return valueCounter;
	}

	/**
	 * @return the smallest value stored
	 */
	public long getMinValue() {
		return ((Long) distances.get(distances.lastKey())).longValue();
	}

	/**
	 * @return the largest value stored
	 */
	public long getMaxValue() {
		return ((Long) distances.get(distances.firstKey())).longValue();
	}

	/**
	 * Retrieves, for a given value, the number of entries that have been stored
	 * with that value (e.g., how many times '5' has been stored).
	 * 
	 * @param value
	 * @return
	 */
	public long getNumberOfEntriesForValue(long value) {
		Long valObj = new Long(value);
		if (distances.containsKey(valObj)) {
			return ((Long) distances.get(valObj)).longValue();
		} else {
			return 0;
		}
	}

	/**
	 * Retrieves the number of recorded values within a given interval. The
	 * interval is interpreted as: [minVal, maxVal] (boundary-inlcusive).
	 * 
	 * @param minVal
	 * @param maxVal
	 * @return
	 */
	public long getNumberOfEntriesBetweenValues(long minVal, long maxVal) {
		Long keyObj = null;
		long result = 0;
		for (Iterator it = distances.keySet().iterator(); it.hasNext();) {
			keyObj = (Long) it.next();
			if ((keyObj.longValue() >= minVal)
					&& (keyObj.longValue() <= maxVal)) {
				result += ((Long) distances.get(keyObj)).longValue();
			}
		}
		return result;
	}

	/**
	 * Creates an array of distribution values for e.g. displaying results in a
	 * plotted graph.
	 * 
	 * @param maxValue
	 *            the value the highest number of occurrences should be mapped
	 *            to (e.g., display graph height)
	 * @param granularity
	 *            the number of values to normalize to
	 * @return an integer array of size 'granularity', with values between [0,
	 *         maxValue].
	 */
	public int[] getNormalizedDistribution(int maxValue, int granularity) {
		long values[] = new long[granularity];
		long maxOccurred = 0;
		int normalized[] = new int[granularity];
		long leftBoundary = getMinValue();
		long rightBoundary = getMaxValue();
		if ((rightBoundary - leftBoundary) <= 0) {
			return null;
		}
		long step = ((rightBoundary - leftBoundary) / granularity);
		// build initial array
		for (int i = 0; i < granularity; i++) {
			values[i] = getNumberOfEntriesBetweenValues((i * step),
					((i + 1) * step));
			if (values[i] > maxOccurred) {
				maxOccurred = values[i];
			}
		}
		// normalize array
		double normalizationFactor = (double) maxValue / (double) maxOccurred;
		for (int i = 0; i < granularity; i++) {
			normalized[i] = (int) (values[i] * normalizationFactor);
		}
		return normalized;
	}

}
