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

package org.processmining.exporting.bpel;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.bpel.BPEL;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: BPELExport
 * </p>
 * 
 * <p>
 * Description: Export a BPEL object to a BPEL 1.1 file
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
public class BPELExport implements ExportPlugin {

	public BPELExport() {
	}

	public String getName() {
		return "BPEL 1.1 file";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof BPEL) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof BPEL) {
				try {
					// Have the underlying Document (bpel.getDocument) written
					// out.
					BPEL bpel = (BPEL) o[i];
					TransformerFactory xformFactory = TransformerFactory
							.newInstance();
					Transformer idTransform = xformFactory.newTransformer();
					Source input = new DOMSource(bpel.getDocument());
					idTransform.transform(input, new StreamResult(output));
				} catch (Exception ex) {
					Message.add("Unable to export to BPEL: " + ex.toString());
				}
				return;
			}
		}
	}

	public String getFileExtension() {
		return "bpel";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:export:bpel";
	}
}
