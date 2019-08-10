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
import java.util.StringTokenizer;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.TPNWriter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;

/**
 * @author not attributable
 * @version 1.0
 */

public class TpnExport implements ExportPlugin {

	public TpnExport() {
	}

	public String getName() {
		return "TPN";
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
				String export = TPNWriter.write((PetriNet) o[i]);
				bw.write(export);
				bw.close();
				StringTokenizer lineTokenizer = new StringTokenizer(export,
						"\n");
				int nofLines = lineTokenizer.countTokens();
				int nofChars = export.length();
				Message.add("<TpnExport nofLines=\"" + nofLines
						+ "\" nofChars=\"" + nofChars + "\"/>", Message.TEST);
				return;
			}
		}
	}

	public String getFileExtension() {
		return "tpn";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:export:tpn";
	}
}
