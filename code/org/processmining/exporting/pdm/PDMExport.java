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

package org.processmining.exporting.pdm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.pdm.PDMModel;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class PDMExport implements ExportPlugin {

	public PDMExport() {
	}

	public String getName() {
		return "PDM file";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PDMModel) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PDMModel) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));
				((PDMModel) o[i]).writeToPDM(bw);
				bw.close();
				return;
			}
		}
	}

	public String getFileExtension() {
		return "xml";
	}

	public String getHtmlDescription() {
		String s = "<html>";
		s += "<head><title>ProM Framework: PDM Export Plug-in</title></head>";
		s += "<body><h1>PDM Export Plug-in</h1>";
		s += "<p>The PDM Export Plug-in writes a PDM model to a an XML file ";
		s += "such that is corresponds to the XML-scheme of a Product Data Model</p>";
		s += "</body></html>";
		return s;
	}
}
