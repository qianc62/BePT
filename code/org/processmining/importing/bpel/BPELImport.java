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

package org.processmining.importing.bpel;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.models.bpel.BPEL;
import org.processmining.framework.models.bpel.BPELProcess;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.bpel.BPELResult;

import org.w3c.dom.Document;
import org.processmining.framework.ui.Message;
import org.processmining.framework.models.bpel.BPELConstants;

/**
 * <p>
 * Title: BPELImport
 * </p>
 * 
 * <p>
 * Description: Import a BPEL 1.1 file
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
public class BPELImport implements LogReaderConnectionImportPlugin {
	public BPELImport() {
	}

	public String getName() {
		return "BPEL 1.1 file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("bpel");
	}

	public MiningResult importFile(InputStream input) throws IOException {
		try {
			// Read the BPEL file as an XML document
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			Document doc = dbf.newDocumentBuilder().parse(input);
			if (doc == null || doc.getDocumentElement() == null) {
				return null;
			}

			// Hook the BPEL object to the XML document.
			BPEL model = new BPEL(doc);
			BPELProcess process = new BPELProcess(doc.getDocumentElement());
			model.setProcess(process);
			process.hookupActivities();

			Message.add("<BPELImport nofInvokes=\""
					+ process.countActivities(BPELConstants.stringInvoke)
					+ "\" nofReceives=\""
					+ process.countActivities(BPELConstants.stringReceive)
					+ "\" nofReplies=\""
					+ process.countActivities(BPELConstants.stringReply)
					+ "\"/>", Message.TEST);

			return new BPELResult(null, model);

		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:import:bpel";
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
