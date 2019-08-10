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

import java.util.List;
import java.util.ArrayList;

import org.processmining.mining.organizationmining.distance.DistanceMatrix;
import org.processmining.mining.organizationmining.distance.DistanceMetric;
import org.processmining.mining.organizationmining.model.InstancePoint;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Progress;
import java.util.Random;

/**
 * This class defines the interface for a profile, in which for a number of
 * process instances a number of values for specific measured items is stored.
 * These items can be used to compare the process instances by a distance
 * metric.
 * 
 * @author Minseok Song
 */
public abstract class Profile {

	/**
	 * static random object
	 */
	protected static Random random = new Random();

	/**
	 * Set, holding all originators.
	 */
	protected List<String> originators;

	/**
	 * The log profiled by this profile.
	 */
	protected LogReader log;
	/**
	 * the name of this profile
	 */
	protected String name;
	/**
	 * human-readable description (1 sentence) of what is measured
	 */
	protected String description;
	/**
	 * normalization maximum. this parameter controls the weight of the profile
	 * in relation to other profiles.
	 */
	protected double normalizationMaximum;
	/**
	 * if set to <code>true</code>, this parameter will cause all measurements
	 * to be reported in an inverted fashion. I.e., the largest value will
	 * become smallest, and vice versa.
	 */
	protected boolean invert;

	/**
	 * Creates a new profile with the given name and description
	 */
	public Profile(LogReader aLog, String aName, String aDescription) {
		log = aLog;
		name = aName;
		description = aDescription;
		originators = new ArrayList<String>();
		for (int i = 0; i < log.getLogSummary().getOriginators().length; i++) {
			originators.add(log.getLogSummary().getOriginators()[i]);
		}
	}

	public LogReader getLog() {
		return log;
	}

	/**
	 * Returns the name of the profile
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the name of the profile
	 */
	public String toString() {
		return name;
	}

	/**
	 * Returns a short description of the profile
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the normalization maximum of this profile
	 * 
	 * @return
	 */
	public double getNormalizationMaximum() {
		return normalizationMaximum;
	}

	/**
	 * Sets the normalization maximum of this profile. The normalization maximum
	 * defines the largest possible value of the range, which can be returned by
	 * the profile.
	 * 
	 * @param maximum
	 */
	public void setNormalizationMaximum(double maximum) {
		normalizationMaximum = maximum;
	}

	/**
	 * Probes, whether this profile is inverted. If a profile is inverted, large
	 * values will be returned as low, and vice versa. I.e., inversion may be
	 * used to reverse the usual effect of a profile or metric.
	 * 
	 * @return
	 */
	public boolean isInverted() {
		return invert;
	}

	/**
	 * Configures, whether this profile is inverted. If a profile is inverted,
	 * large values will be returned as low, and vice versa. I.e., inversion may
	 * be used to reverse the usual effect of a profile or metric.
	 * 
	 * @param isInverted
	 */
	public void setInvert(boolean isInverted) {
		invert = isInverted;
	}

	/**
	 * Returns the index of Originator in List which are described by the
	 * profile
	 * 
	 * @param isInverted
	 * @return
	 */
	public int getIndexOfOriginator(String ori) {
		return originators.indexOf(ori);
	}

	/**
	 * Returns the number of process instances / cases which are described by
	 * the profile
	 * 
	 * @return
	 */
	public int getNumberOfOriginators() {
		return originators.size();
	}

	public InstancePoint getPoint(String originatorName) {
		InstancePoint point = new InstancePoint();
		for (String key : getItemKeys()) {
			point.set(key, this.getValue(originatorName, key));
		}
		return point;
	}

	public abstract int numberOfItems();

	public abstract String getItemKey(int index);

	/**
	 * Returns the list of item keys, which are used to compare the process
	 * instances in this profile
	 * 
	 * @return
	 */
	public abstract List<String> getItemKeys();

	/**
	 * Returns the value for a given process instance and item contained in this
	 * profile.
	 * 
	 * @param originatorName
	 * @param itemId
	 * @return
	 */
	public abstract double getValue(String originatorName, int itemIndex);

	/**
	 * Returns the value for a given process instance and item contained in this
	 * profile.
	 * 
	 * @param originatorIndex
	 * @param itemId
	 * @return
	 */
	public abstract double getValue(int originatorIndex, int itemIndex);

	/**
	 * Returns the value for a given process instance and item contained in this
	 * profile.
	 * 
	 * @param originatorName
	 * @param itemId
	 * @return
	 */
	public abstract double getValue(String originatorName, String itemId);

	/**
	 * Returns the value for a given process instance and item contained in this
	 * profile.
	 * 
	 * @param originatorIndex
	 * @param itemId
	 * @return
	 */
	public abstract double getValue(int originatorIndex, String itemId);

	/**
	 * Returns a random value of the indexed item (i.e., a randomly distributed
	 * value within the range of recorded settings)
	 * 
	 * @param itemIndex
	 * @return
	 */
	public abstract double getRandomValue(int itemIndex);

	/**
	 * Returns a random value of the given item (i.e., a randomly distributed
	 * value within the range of recorded settings)
	 * 
	 * @param itemId
	 * @return
	 */
	public abstract double getRandomValue(String itemId);

	/**
	 * Initialize the seed number
	 * 
	 * @param seed
	 * @return
	 */
	public void setRandomSeed(long seed) {
		random.setSeed(seed);
	}

	/**
	 * Calculates the trace distance matrix from this profile, using the
	 * provided distance metric implementation.
	 * 
	 * @param metric
	 * @param progress
	 *            Used to provide feedback information, may be <code>null</code>
	 *            .
	 * @return
	 */
	public abstract DistanceMatrix getDistanceMatrix(DistanceMetric metric,
			Progress progress);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Profile) {
			return name.equals(((Profile) obj).getName());
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
