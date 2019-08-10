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

package org.processmining.exporting.epcs;

import java.io.IOException;
import java.io.OutputStream;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
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

public class PPMGraphFormatExportPlugin implements ExportPlugin {
	public PPMGraphFormatExportPlugin() {
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof ConfigurableEPC) {
				return true;
			}
		}
		return false;
	}

	public String getFileExtension() {
		return "xml";
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof ConfigurableEPC) {
				write(output, (ConfigurableEPC) o[i]);
				output.close();
				return;

			}
		}

	}

	public String getName() {
		return "Aris graph format";
	}

	public String getHtmlDescription() {
		return "This plugin writes a given EPC to an XML-based file. This file is made up according to "
				+ "the Aris Graph format, used in Aris PPM.";
	}

	public void write(final OutputStream output, ConfigurableEPC epc)
			throws IOException {

		// Write the header to the stream.
		writeLn("<?xml version=\"1.0\"?>", output);
		writeLn("<!DOCTYPE graphlist SYSTEM \"graph.dtd\">", output);
		writeLn("<graphlist>", output);

		writeLn("<graph id=\"" + epc.getIdentifier() + "\" xml:lang=\"en\">",
				output);

		// Now write the real information for this instance
		epc.writePPMImport(output);

		writeLn("</graph>", output);

		writeLn("</graphlist>", output);

	}

	private void writeLn(String s, OutputStream out) {
		try {
			if (s.length() != 0) {
				out.write(s.getBytes());
			}
			out.write("\n".getBytes());
		} catch (IOException ex) {
		}
	}

}
