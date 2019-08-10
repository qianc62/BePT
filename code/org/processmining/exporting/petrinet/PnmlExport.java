/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.exporting.petrinet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.PnmlWriter;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class PnmlExport implements ExportPlugin {

	public PnmlExport() {
	}

	public String getName() {
		return "PNML 1.3.2 file";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));

				PnmlWriter.write(false, true, (PetriNet) o[i], bw);
				bw.close();
				return;
			}
		}
	}

	public String getFileExtension() {
		return "pnml";
	}

	public String getHtmlDescription() {
		return "<p> <b>Plug-in: PNML 1.3.2 export</b>"
				+ "<p>This Plug-in allows the user export a Petri net to a version of "
				+ "<a href=\"http://www2.informatik.hu-berlin.de/top/pnml/about.html\">PNML</a>  "
				+ "which can be read by <a href=\"http://www.yasper.org/\">YASPER</a>.";
	}
}
