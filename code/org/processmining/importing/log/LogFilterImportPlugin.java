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

package org.processmining.importing.log;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.filter.AbstractLogFilter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.ImportPlugin;
import org.processmining.importing.ProMInputStream;
import org.processmining.mining.MiningResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Imports a LogFilter object using the standards as describes in the
 * Serializable interface
 * 
 * @author not attributable
 * @version 1.0
 */
public class LogFilterImportPlugin implements ImportPlugin {
	public LogFilterImportPlugin() {
	}

	/**
	 * getFileFilter
	 * 
	 * @return FileFilter
	 * @todo Implement this org.processmining.importing.ImportPlugin method
	 */
	public FileFilter getFileFilter() {
		return new GenericFileFilter("xml");
	}

	/**
	 * Gets a description of this plugin in HTML.
	 * 
	 * @return a description of this plugin in HTML
	 * @todo Implement this org.processmining.framework.plugin.Plugin method
	 */
	public String getHtmlDescription() {
		return "Imports a LogFilter using an XML file. After importing, it adds the log Filter to the"
				+ " LogFilterCollection of the framework.";
	}

	/**
	 * Gets the name of this plugin.
	 * 
	 * @return the name of this plugin
	 * @todo Implement this org.processmining.framework.plugin.Plugin method
	 */
	public String getName() {
		return "Log Filter (advanced)";
	}

	public MiningResult importFile(InputStream input) throws IOException {
		try {
			return importNow(input);
		} catch (Throwable t) {
			throw new IOException(t.toString());
		}
	}

	/**
	 * importFile
	 * 
	 * @param input
	 *            InputStream
	 * @return MiningResult
	 * @throws IOException
	 * @todo Implement this org.processmining.importing.ImportPlugin method
	 */
	private MiningResult importNow(InputStream input) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;

		dbf.setValidating(false);
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);

		doc = dbf.newDocumentBuilder().parse(input);
		NodeList netNodes;

		// check if root element is a <pnml> tag
		if (!doc.getDocumentElement().getTagName().equals("ProMLogFilter")) {
			throw new IOException("ProMLogFilter tag not found");
		}

		netNodes = doc.getDocumentElement().getElementsByTagName("LogFilter");
		LogFilter f = null;
		if (netNodes.getLength() > 0) {
			f = LogFilter.readXML(netNodes.item(0));
			f = new AbstractLogFilter(((ProMInputStream) input).getFileName(),
					f);
			MainUI.getInstance().addGlobalProvidedObject(
					new ProvidedObject(f.getName(), new Object[] { f }));
		}
		if (f != null) {
			Message.add("Import of LogFilter succesfull.");
			Message
					.add("The imported LogFilter is now available to the framework.");
		} else {
			Message.add("Import of LogFilter NOT succesfull.");
		}
		if (f != null) {
			Message.add("<ImportLogFilter succesfull>", Message.TEST);
		} else {
			Message.add("<ImportLogFilter NOTsuccesfull>", Message.TEST);
		}
		return new LogFilterImportResult();
	}
}

class LogFilterImportResult implements MiningResult {

	/**
	 * @return null
	 */
	public JComponent getVisualization() {
		return null;
	}

	/**
	 * @return null
	 */
	public LogReader getLogReader() {
		return null;
	}
}
