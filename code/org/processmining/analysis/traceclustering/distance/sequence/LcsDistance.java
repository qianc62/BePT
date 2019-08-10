/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.analysis.traceclustering.distance.sequence;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LcsDistance extends StringDistanceMetric {

	/**
	 * @param name
	 * @param description
	 */
	public LcsDistance() {
		super("LCS distance", "LCS distance");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.traceclustering.distance.sequence.
	 * StringDistanceMetric#getDistance(java.lang.String, java.lang.String)
	 */
	@Override
	public double getDistance(String a, String b) {
		int[][] c = new int[a.length()][b.length()];
		for (int i = 0; i < a.length(); i++) {
			c[i][0] = 0;
		}
		for (int j = 1; j < b.length(); j++) {
			c[0][j] = 0;
		}
		for (int i = 1; i < a.length(); i++) {
			for (int j = 1; j < b.length(); j++) {
				if (a.charAt(i) == b.charAt(j)) {
					c[i][j] = c[i - 1][j - 1] + 1;
				} else {
					c[i][j] = Math.max(c[i][j - 1], c[i - 1][j]);
				}
			}
		}
		int longest = Math.max(a.length(), b.length()) - 1;
		return (double) (longest - c[a.length() - 1][b.length() - 1]);
	}

}
