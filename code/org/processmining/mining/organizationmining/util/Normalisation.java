/*
 * Copyright (c) 2007 Alexander Serebrenik (a.serebrenik@tue.nl)
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
package org.processmining.mining.organizationmining.util;

import java.util.ArrayList;

/**
 * @author alexander
 * 
 */
public class Normalisation {

	public static double getMaximum(ArrayList<Double> original) {
		double maximum = original.get(original.size());
		for (int x = original.size() - 1; x >= 0; x--) {
			if (maximum < original.get(x))
				maximum = original.get(x);
		}
		return maximum;
	}

	public static double getMinimum(ArrayList<Double> original) {
		double minimum = original.get(original.size());
		for (int x = original.size() - 1; x >= 0; x--) {
			if (minimum > original.get(x))
				minimum = original.get(x);
		}
		return minimum;
	}

	public static double getMean(ArrayList<Double> original) {
		double sum = 0.0;
		for (int x = original.size(); x >= 0; x--) {
			sum += original.get(x);
		}
		return sum / original.size();
	}

	public static double getStandardDeviation(ArrayList<Double> original) {
		double mean = getMean(original);
		return getStdDev(original, mean);
	}

	private static double getStdDev(ArrayList<Double> original, double mean) {
		double sum = 0.0;

		for (int x = original.size(); x >= 0; x--) {
			double diff = original.get(x) - mean;
			sum += (diff * diff);
		}
		return Math.sqrt(sum / original.size());
	}

	/**
	 * 
	 * @param original
	 * @return normalised list normalisation = subtract mean and divide by the
	 *         stddev
	 */
	public static ArrayList<Double> normalise(ArrayList<Double> original) {
		double mean = getMean(original);
		double stddev = getStdDev(original, mean);
		ArrayList<Double> normalised = new ArrayList<Double>();

		for (int x = original.size(); x >= 0; x--) {
			normalised.add((original.get(x) - mean) / stddev);
		}
		return normalised;
	}

}
