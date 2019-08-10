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
package org.processmining.importing.pdm;

import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.models.pdm.PDMModel;
import org.processmining.framework.models.pdm.algorithms.PDMReader;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.pdmmining.PDMResult;

/**
 * <p>
 * Title: PDM import plug-in
 * </p>
 * <p>
 * Description: Plug-in for PDM files, containing the XML structure of a Product
 * Data Model.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */

public class PDMImport implements LogReaderConnectionImportPlugin {
	/**
	 * Create the import plug-in
	 */
	public PDMImport() {
	}

	/**
	 * Get the name of the plug-in
	 * 
	 * @return "PDM file"
	 */
	public String getName() {
		return "PDM file";
	}

	/**
	 * PDM files have xml extensions
	 * 
	 * @return File filter for xml files
	 */
	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("xml");
	}

	/**
	 * Import the PDM file which is waiting in the given stream
	 * 
	 * @param input
	 *            The given stream
	 * @return The YAWL model as a MiningResult
	 * @throws IOException
	 *             If reading fails
	 */
	public MiningResult importFile(InputStream input) throws IOException {
		try {
			PDMModel model = PDMReader.read(input);

			if (model == null) {
				return null;
			}

			return new PDMResult(null, model);

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
		return "http://is.tm.tue.nl/staff/ivanderfeesten/ProM/documentation/PDMImport.htm";
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
