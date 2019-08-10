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

package org.processmining.exporting.colorReference;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.analysis.performance.dottedchart.ui.ColorReference;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;

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
 * @author Minseok Song
 * @version 1.0
 */

public class ColorReferenceExport implements ExportPlugin {

	public ColorReferenceExport() {
	}

	public String getName() {
		return "Color Reference file";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof ColorReference) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();
		Message.add("<ColorReferenceExport>", Message.TEST);
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof ColorReference) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));
				((ColorReference) o[i]).writeToXML(bw);
				bw.close();
				Message.add("</ColorReferenceExport>", Message.TEST);
				return;
			}
		}

	}

	public String getFileExtension() {
		return "xml";
	}

	public String getHtmlDescription() {
		String s = "<html>";
		s += "<head><title>ProM Framework: Color Reference Plug-in</title></head>";
		s += "<body><h1>Color Reference Export Plug-in</h1>";
		s += "<p>The Color Reference Export Plug-in make the Dotted Chart analysis plug-in write a Color Reference file.";
		s += "The resulting file can be used in the Dotted Chart analysis plug-in. </p>";
		s += "</body></html>";
		return s;
	}
}
