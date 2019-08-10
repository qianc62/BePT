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

package org.processmining.importing.yawl;

import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.yawl.algorithms.YAWLReader;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.yawlmining.YAWLResult;

/**
 * <p>
 * Title: YAWL import plug-in
 * </p>
 * <p>
 * Description: Plug-in for YAWL engine files.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class YAWLImport implements LogReaderConnectionImportPlugin {
	/**
	 * Create the import plug-in
	 */
	public YAWLImport() {
	}

	/**
	 * Ge tthe name of the plug-in
	 * 
	 * @return "YAWL file"
	 */
	public String getName() {
		return "YAWL file";
	}

	/**
	 * YAWL engine files have xml extensions
	 * 
	 * @return File filter for xml files
	 */
	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("xml");
	}

	/**
	 * Import the YAWL file which is waiting in hte given stream
	 * 
	 * @param input
	 *            The given stream
	 * @return The YAWL model as a MiningResult
	 * @throws IOException
	 *             If reading fails
	 */
	public MiningResult importFile(InputStream input) throws IOException {
		try {
			YAWLModel model = YAWLReader.read(input);

			if (model == null) {
				return null;
			}
			model.Test("YAWLImport");
			return new YAWLResult(null, model);

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
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:import:yawl";
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
