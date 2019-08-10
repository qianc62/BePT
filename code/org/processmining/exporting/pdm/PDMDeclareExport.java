package org.processmining.exporting.pdm;

import java.io.*;

import org.processmining.exporting.*;
import org.processmining.framework.models.pdm.*;
import org.processmining.framework.plugin.*;

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

public class PDMDeclareExport implements ExportPlugin {

	public PDMDeclareExport() {
	}

	public String getName() {
		return "PDM to Declare XML file";
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
				((PDMModel) o[i]).writePDMToDeclare(bw);
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
