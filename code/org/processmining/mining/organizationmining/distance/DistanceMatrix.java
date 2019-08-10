/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.mining.organizationmining.distance;

/**
 * @author Christian W. Guenther
 * 
 */
public abstract class DistanceMatrix {

	public abstract double get(int x, int y);

	public abstract void set(int x, int y, double value);

	public abstract double getMinValue();

	public abstract double getMaxValue();

	public abstract void multiplyAllFieldsWith(double factor);

	public abstract void normalizeToMaximum(double normalizationMaximum);

	public abstract void invert();

	public abstract int itemSize();

	public abstract void add(DistanceMatrix other);

	protected int sumSmallerEqual(int value) {
		// sum of all values <= value
		// (value + (value - 1) + ... + 1)
		return (value * (value + 1)) / 2;
	}

	protected int translateAddress(int x, int y) {
		if (x >= y) {
			if (x == 0) {
				return y;
			}
			int offset = sumSmallerEqual(x - 1);
			return offset + y;
		} else {
			if (y == 0) {
				return x;
			}
			int offset = sumSmallerEqual(y - 1);
			return offset + x;
		}
	}

}