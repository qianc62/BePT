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

package org.processmining.importing.protos;

import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.models.protos.ProtosModel;
import org.processmining.framework.models.protos.algorithms.ProtosXMLExportReader;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.protosmining.ProtosResult;

/**
 * <p>
 * Title: Protos XML Export Import
 * </p>
 * 
 * <p>
 * Description: Import plug-in for Protos XML Export files
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
public class ProtosImport implements LogReaderConnectionImportPlugin {
	public ProtosImport() {
	}

	/**
	 * Get the name of the plug-in
	 * 
	 * @return "Protos XML Export file"
	 */
	public String getName() {
		return "Protos XML Export file";
	}

	/**
	 * Protos XML Export files have xml extensions
	 * 
	 * @return File filter for xml files
	 */
	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("xml");
	}

	/**
	 * Import the Protos XML Export file which is waiting in hte given stream
	 * 
	 * @param input
	 *            The given stream
	 * @return The Protos model as a MiningResult
	 * @throws IOException
	 *             If reading fails
	 */
	public MiningResult importFile(InputStream input) throws IOException {
		try {
			ProtosModel model = ProtosXMLExportReader.read(input);

			if (model == null) {
				return null;
			}

			return new ProtosResult(null, model);

		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	/**
	 * Provide help
	 * 
	 * @return Help text
	 */
	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:import:protos";
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
