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

package org.processmining.exporting.protos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.protos.ProtosModel;
import org.processmining.framework.models.protos.ProtosString;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title: Protos XML Export export
 * </p>
 * 
 * <p>
 * Description: Export plug-in for Protos XML Export
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class ProtosExport implements ExportPlugin {

	public ProtosExport() {
	}

	public String getName() {
		return "Protos XML Export file";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof ProtosModel) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof ProtosModel) {
				String xml = ((ProtosModel) o[i])
						.writeXMLExport(ProtosString.ProtosModel);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));
				bw.write(xml);
				bw.close();
				return;
			}
		}
	}

	public String getFileExtension() {
		return "xml";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:export:protos";
	}
}
