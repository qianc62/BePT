/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.importing.bpmn;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.models.bpmn.BpmnGraph;
import org.processmining.framework.models.bpmn.BpmnUtils;
import org.processmining.framework.ui.filters.GenericFileFilter; //import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.importing.ImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.bpmnmining.BpmnResult;
import org.w3c.dom.Document;

/**
 * Plugin to import BPMN file
 * 
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * @version 1.0
 */
public class BpmnImport implements ImportPlugin /* LogReaderConnectionImportPlugin */{

	// Use default constructor

	/**
	 * Gets the name of this plugin.
	 * 
	 * @return
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Ilog BPMN file";
	}

	public String getHtmlDescription() {
		return "Import a BPMN file to ProM";
	}

	public FileFilter getFileFilter() {
		return new GenericFileFilter("ibp");
	}

	public MiningResult importFile(InputStream input) throws IOException {
		BpmnResult result = null;
		Document doc = null;

		try {
			// Read the BPMN file as an XML document
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			doc = dbf.newDocumentBuilder().parse(input);
		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}

		if (doc != null) {
			BpmnGraph graph = BpmnUtils.createBpmnGraph(doc);
			graph.Test("BPMNImport");
			result = new BpmnResult(null, graph);
		}
		return result;
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}

}
