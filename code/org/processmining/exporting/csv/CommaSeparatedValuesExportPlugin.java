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

package org.processmining.exporting.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.conversion.CaseDataExtractorOptions;

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

public class CommaSeparatedValuesExportPlugin implements ExportPlugin {
	public CommaSeparatedValuesExportPlugin() {
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof CaseDataExtractorOptions) {
				return true;
			}
		}
		return false;
	}

	public String getFileExtension() {
		return "csv";
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		// object.getObjects()[0] is the LogReader
		CaseDataExtractorOptions theOptions = (CaseDataExtractorOptions) object
				.getObjects()[1];
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
		theOptions.printLog(bw, "CommaSeparatedValuesExportPlugin");
		bw.close();
	}

	public String getName() {
		// return "Comma Separated Values";
		return "Standard CSV";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:export:csv";
	}

}
