/* jahmm package - v0.6.1 *
 *  Copyright (c) 2004-2006, Jean-Marc Francois.
 *
 *  This file is part of Jahmm.
 *  Jahmm is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Jahmm is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jahmm; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */

/* 
 * Adapted by Anne Rozinat for tailored visualization.
 */
package org.processmining.analysis.hmm.jahmmext;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.processmining.framework.log.LogEvent;

import be.ac.ulg.montefiore.run.jahmm.Hmm;

/**
 * An HMM to <i>dot</i> file converter. See
 * <url>http://www.research.att.com/sw/tools/graphviz/</url> for more
 * information on the <i>dot</i> tool.
 * <p>
 * The command <tt>dot -Tps -o &lt;outputfile&gt; &lt;inputfile&gt;</tt> should
 * produce a Postscript file describing an HMM.
 * 
 * Adapted for Hmm experiment package by Anne Rozinat.
 */
class HmmDrawerDot<H extends Hmm<?>> {
	protected double minimumAij = 0.0001;
	protected double minimumPi = 0.0001;
	protected NumberFormat probabilityFormat;

	/**
	 * This class converts an HMM to a dot file.
	 */
	public HmmDrawerDot() {
		probabilityFormat = NumberFormat.getInstance();
		probabilityFormat.setMaximumFractionDigits(3);
	}

	protected String convert(H hmm, ArrayList<LogEvent> observationMapping) {
		String s = beginning();

		s += transitions(hmm);
		s += states(hmm, observationMapping);

		return s + ending();
	}

	protected String beginning() {
		return "digraph G {\n graph [rankdir=LR]; \n";
	}

	protected String transitions(Hmm<?> hmm) {
		String s = "";

		for (int i = 0; i < hmm.nbStates(); i++)
			for (int j = 0; j < hmm.nbStates(); j++)
				if (hmm.getAij(i, j) >= minimumAij)
					s += "\t" + i + " -> " + j + " [label="
							+ probabilityFormat.format(hmm.getAij(i, j))
							+ "];\n";

		return s;
	}

	protected String states(H hmm, ArrayList<LogEvent> observationMapping) {
		String s = "";

		for (int i = 0; i < hmm.nbStates(); i++) {
			String logEventString = "";
			if (i < observationMapping.size()) {
				logEventString = " ("
						+ observationMapping.get(i).getModelElementName() + ")";
			}

			s += "\t" + i + " [";

			if (hmm.getPi(i) >= minimumPi) {
				s += "shape=doublecircle, label=\"" + i + logEventString
						+ "  Pi= " + probabilityFormat.format(hmm.getPi(i));
			} else {
				s += "shape=circle, label=\"" + i + logEventString;
			}

			s += "\\n" + opdfLabel(hmm, i) + "\"";

			s += "];\n";
		}

		return s;
	}

	protected String opdfLabel(H hmm, int stateNb) {
		String s = hmm.getOpdf(stateNb).toString();
		s = s.replaceAll("Integer distribution --- (.*)", "$1");
		return "[ " + s + " ]";
	}

	protected String ending() {
		return "}\n";
	}

	/**
	 * Writes a dot file depicting the given HMM.
	 * 
	 * @param hmm
	 *            The HMM to depict.
	 * @param filename
	 *            The resulting 'dot' file filename.
	 */
	public void write(H hmm, String filename,
			ArrayList<LogEvent> observationMapping) throws IOException {
		FileWriter fw = new FileWriter(filename);
		fw.write(convert(hmm, observationMapping));
		fw.close();
	}
}