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
package org.processmining.exporting.fuzzyModel;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyModelExport implements ExportPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.exporting.ExportPlugin#accepts(org.processmining.framework
	 * .plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject object) {
		for (Object obj : object.getObjects()) {
			if (obj instanceof FuzzyGraph) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.exporting.ExportPlugin#export(org.processmining.framework
	 * .plugin.ProvidedObject, java.io.OutputStream)
	 */
	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		FuzzyGraph graph = null;
		for (Object obj : object.getObjects()) {
			if (obj instanceof FuzzyGraph) {
				graph = (FuzzyGraph) obj;
				break;
			}
		}
		if (graph == null) {
			throw new AssertionError(
					"No valid fuzzy graph found for exporting!");
		}
		output = new BufferedOutputStream(new GZIPOutputStream(output));
		FuzzyModelSerializer.serialize(graph, output);
		output.flush();
		output.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.exporting.ExportPlugin#getFileExtension()
	 */
	public String getFileExtension() {
		return "fmz";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Fuzzy Model Export";
	}

}
