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

package org.processmining.exporting.orgmodel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.orgmodel.OrgModel;
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

public class OrgModelExport implements ExportPlugin {

	public OrgModelExport() {
	}

	public String getName() {
		return "Org Model file";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof OrgModel) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();
		Message.add("<OMMLExport>", Message.TEST);
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof OrgModel) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));
				((OrgModel) o[i]).writeToXML(bw);
				bw.close();
				((OrgModel) o[i]).writeToTestLog();
				Message.add("</OMMLExport>", Message.TEST);
				return;
			}
		}

	}

	public String getFileExtension() {
		return "xml";
	}

	public String getHtmlDescription() {
		String s = "<html>";
		s += "<head><title>ProM Framework: Org Model Export Plug-in</title></head>";
		s += "<body><h1>Org Model Export Plug-in</h1>";
		s += "<p>The Org Model Export Plug-in writes a Org Model model. ";
		s += "The resulting file can be directly imported in the ProM Framework.</p>";
		s += "</body></html>";
		return s;
	}
}
