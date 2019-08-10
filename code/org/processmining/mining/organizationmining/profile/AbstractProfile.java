/*
 * Copyright (c) 2007 Minseok Song
 *
 * LICENSE:
 *
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * EXEMPTION:
 *
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 *
 */
package org.processmining.mining.organizationmining.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.processmining.mining.organizationmining.distance.DistanceMatrix;
import org.processmining.mining.organizationmining.distance.DistanceMetric;
import org.processmining.mining.organizationmining.util.EfficientSparseDoubleArray;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Progress;

/**
 * @author Minseok Song
 * 
 */
public abstract class AbstractProfile extends Profile {

	/**
	 * Array, which holds for each process instance a sparse array, holding the
	 * values (Double precision) for each item.
	 */
	protected EfficientSparseDoubleArray[] values;// SparseArray<Double>[]
	// values;

	/**
	 * Set, holding all item keys present for any instance.
	 */
	protected List<String> itemKeys;

	/**
	 * Hash map for faster item index access (buffering of indices)
	 */
	protected HashMap<String, Integer> itemIndices;

	/**
	 * Holds the maximal value currently set for any item measurement
	 */
	protected double valueMaximum;

	/**
	 * Creates a new profile
	 * 
	 * @param aName
	 *            name of the profile
	 * @param aDescription
	 *            human-readable, 1-sentence description of the profile
	 * @param log
	 *            LogReader instance used to build the profile
	 */
	protected AbstractProfile(String aName, String aDescription, LogReader aLog) {
		super(aLog, aName, aDescription);
		normalizationMaximum = 1.0;
		valueMaximum = 0.0;
		invert = false;
		itemKeys = new ArrayList<String>();
		itemIndices = new HashMap<String, Integer>();
		values = new EfficientSparseDoubleArray[log.getLogSummary()
				.getOriginators().length];
		// String users[] = summary.getOriginators();
		// initialize profile's data structure
		for (int i = 0; i < log.getLogSummary().getOriginators().length; i++) {
			values[i] = new EfficientSparseDoubleArray(0.0);
		}
	}

	protected double normalizationFactor() {
		if (valueMaximum > 0) {
			return normalizationMaximum / valueMaximum;
		} else {
			return normalizationMaximum;
		}
	}

	protected double normalize(double value) {
		if (valueMaximum > 0) {
			return (value * normalizationMaximum) / valueMaximum;
		} else {
			return 0.0;
		}
	}

	public List<String> getItemKeys() {
		return itemKeys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.traceclustering.profile.Profile#getItemKey
	 * (int)
	 */
	@Override
	public String getItemKey(int index) {
		return itemKeys.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.traceclustering.profile.Profile#numberOfItems
	 * ()
	 */
	@Override
	public int numberOfItems() {
		return itemKeys.size();
	}

	public double getValue(int originatorIndex, int itemIndex) {
		return normalize(values[originatorIndex].get(itemIndex));
	}

	public double getValue(String originatorName, int itemIndex) {
		return normalize(values[originators.indexOf(originatorName)]
				.get(itemIndex));
	}

	public double getValue(String originatorName, String itemId) {
		int itemIndex = itemIndices.get(itemId);
		if (itemIndex < 0) {
			// invalid index given!
			throw new IllegalArgumentException("Invalid item ID given!");
		} else {
			return normalize(values[originators.indexOf(originatorName)]
					.get(itemIndex));
		}
	}

	public double getValue(int originatorIndex, String itemId) {
		int itemIndex = itemIndices.get(itemId);
		if (itemIndex < 0) {
			// invalid index given!
			throw new IllegalArgumentException("Invalid item ID given!");
		} else {
			return normalize(values[originatorIndex].get(itemIndex));
		}
	}

	public double getRandomValue(int itemIndex) {
		double minimum = Double.MAX_VALUE;
		double maximum = Double.MIN_VALUE;
		double value;
		// look for column's minimum and maximum value
		for (int i = getNumberOfOriginators() - 1; i >= 0; i--) {
			value = getValue(originators.get(i), itemIndex);
			if (value > maximum) {
				maximum = value;
			}
			if (value < minimum) {
				minimum = value;
			}
		}
		// generate and return randomly distributed
		// value within the determined column's range
		double rnd = minimum + (random.nextDouble() * (maximum - minimum));
		return normalize(rnd);
	}

	public double getRandomValue(String itemId) {
		return getRandomValue(itemKeys.indexOf(itemId));
	}

	/**
	 * Manually sets the value for a specific process instance / item
	 * combination in this profile
	 * 
	 * @param instanceId
	 * @param itemId
	 * @param value
	 */
	protected void setValue(int instanceIndex, String itemId, double value) {
		// add item id to global set if necessary
		Integer itemIndexObj = itemIndices.get(itemId);
		int itemIndex;
		if (itemIndexObj != null) {
			itemIndex = itemIndexObj;
		} else {
			// not present yet, add
			itemIndex = itemKeys.size();
			itemKeys.add(itemId);
			itemIndices.put(itemId, itemIndex);
		}
		if (value > valueMaximum) {
			// adjust local value maximum to newly set value
			valueMaximum = value;
		}
		values[instanceIndex].set(itemIndex, value);
	}

	/**
	 * Increments the value stored for a specific process instance / item
	 * combination stored in this profile, by the given increment
	 * 
	 * @param originatorName
	 * @param itemId
	 * @param increment
	 */
	protected void incrementValue(String originatorName, String itemId,
			double increment) {
		double currentValue = 0.0;
		// add item id to global set if necessary
		Integer itemIndexObj = itemIndices.get(itemId);
		int itemIndex;
		if (itemIndexObj != null) {
			itemIndex = itemIndexObj;
			currentValue = values[originators.indexOf(originatorName)]
					.get(itemIndex);
		} else {
			// not present yet, add
			itemIndex = itemKeys.size();
			itemKeys.add(itemId);
			itemIndices.put(itemId, itemIndex);
		}
		// increment current value
		currentValue += increment;
		if (currentValue > valueMaximum) {
			// adjust local value maximum to newly set value
			valueMaximum = currentValue;
		}
		values[originators.indexOf(originatorName)]
				.set(itemIndex, currentValue);
	}

	public DistanceMatrix getDistanceMatrix(DistanceMetric metric,
			Progress progress) {
		DistanceMatrix distanceMatrix = metric.getDistanceMatrix(this);
		distanceMatrix.normalizeToMaximum(this.normalizationMaximum);
		if (invert == true) {
			distanceMatrix.invert();
		}
		return distanceMatrix;
	}

}
