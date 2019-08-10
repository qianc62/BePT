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

package org.processmining.importing.orgmodel;

import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.algorithms.OmmlReader;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.organizationmining.OrgMiningResult;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class OrgModelImport implements LogReaderConnectionImportPlugin {

	public OrgModelImport() {
	}

	public String getName() {
		return "Org Model file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("xml");
	}

	public MiningResult importFile(InputStream input) throws IOException {

		try {
			Message.add("<openOMML>", Message.TEST);
			OrgModel orgmodel = OmmlReader.read(input);
			orgmodel.writeToTestLog();
			Message.add("</openOMML>", Message.TEST);
			return new OrgMiningResult(null, orgmodel);
		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/orgmodelimport";
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
