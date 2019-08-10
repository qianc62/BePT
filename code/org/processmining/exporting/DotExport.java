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

package org.processmining.exporting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author not attributable
 * @version 1.0
 */

public class DotExport implements ExportPlugin {

	public DotExport() {
	}

	public String getName() {
		return "DOT file";
	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean isDotWriter = false;
		while ((i < original.getObjects().length)) {
			isDotWriter = isDotWriter
					|| (original.getObjects()[i] instanceof DotFileWriter);
			i++;
		}
		return isDotWriter;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof DotFileWriter) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));
				((DotFileWriter) o[i]).writeToDot(bw);
				bw.close();
				return;
			}
		}
	}

	public String getFileExtension() {
		return "dot";
	}

	public String getHtmlDescription() {
		return "<p>This plugin accepts any graph that can be visualized in ProM. The export "
				+ "writes a dot file, which can be used to generate the figures seen in ProM "
				+ "outside of the framework. Note that some custom figures, such as the "
				+ "elements of a YAWL model will not be visualized correctly. Instead, they are "
				+ "substituted by a box."
				+ "<p> For more information about DOT, click <a href=\"http://www.graphviz.org/\">here</a>.";
	}
}
