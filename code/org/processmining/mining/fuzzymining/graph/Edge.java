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
package org.processmining.mining.fuzzymining.graph;

import java.io.IOException;
import java.io.Writer;

import org.processmining.framework.models.DotFileWriter;

/**
 * @author christian
 * 
 */
public class Edge implements DotFileWriter {

	protected Node source;
	protected Node target;
	protected double significance;
	protected double correlation;
	protected double attenuationThreshold;

	public Edge(Node source, Node target, double significance,
			double correlation) {
		this.source = source;
		this.target = target;
		this.significance = significance;
		this.correlation = correlation;
	}

	public Node getSource() {
		return source;
	}

	public Node getTarget() {
		return target;
	}

	public double getSignificance() {
		return significance;
	}

	public double getCorrelation() {
		return correlation;
	}

	public void setSignificance(double significance) {
		this.significance = significance;
	}

	public void setCorrelation(double correlation) {
		this.correlation = correlation;
	}

	public boolean equals(Object other) {
		if (other instanceof Edge) {
			Edge oE = (Edge) other;
			return (source.equals(oE.source) && target.equals(oE.target));
		} else {
			return false;
		}
	}

	public int hashCode() {
		return (source.hashCode() << 2) + target.hashCode();
	}

	public String toString() {
		return "Edge " + source.id() + " -> " + target.id();
	}

	public void setAttenuationThreshold(double attenuationThreshold) {
		this.attenuationThreshold = attenuationThreshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.DotFileWriter#writeToDot(java.io.Writer
	 * )
	 */
	public void writeToDot(Writer bw) throws IOException {
		bw.write(source.id() + " -> " + target.id() + " [label=\""
				+ MutableFuzzyGraph.format(significance) + "\\n"
				+ MutableFuzzyGraph.format(correlation) + "\"");
		if (significance < attenuationThreshold) {
			int grayValue = 80 - (int) ((significance / attenuationThreshold) * 80.0);
			bw.write("color=\"gray" + grayValue + "\"");
		}
		bw.write("];\n");
	}

};
