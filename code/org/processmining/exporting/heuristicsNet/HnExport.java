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

package org.processmining.exporting.heuristicsNet;

import java.io.IOException;
import java.io.OutputStream;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title: Heuristics net's exporter
 * </p>
 * <p>
 * Description: This class exports heuristics net to a text file whose
 * termination is ".ind".
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class HnExport implements ExportPlugin {

	public static final String FILE_TERMINATION = "hn";
	public static final String FILE_TYPE = "HN file";

	public HnExport() {
	}

	public String getName() {
		return FILE_TYPE;
	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		HeuristicsNet hNet = null;
		while ((i < original.getObjects().length)) {
			hNet = (original.getObjects()[i] instanceof HeuristicsNet ? (HeuristicsNet) (original
					.getObjects()[i])
					: hNet);
			i++;
		}
		return (hNet != null);
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof HeuristicsNet) {
				((HeuristicsNet) o[i]).toFile(output);
				return;
			}
		}
	}

	public String getFileExtension() {
		return FILE_TERMINATION;
	}

	public String getHtmlDescription() {
		return "This plug-in exports a <i>heuristics net</i> to a file.";
	}
}
