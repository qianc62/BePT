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

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

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

public class PNKernelExport implements ExportPlugin {

	public String getName() {
		return "Petri Net Kernel file";
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

				PnmlWriter.write(true, false, (PetriNet) o[i], bw);
				bw.close();
				return;
			}
		}
	}

	public String getFileExtension() {
		return "pnml";
	}

	public String getHtmlDescription() {
		return "";
	}
}
