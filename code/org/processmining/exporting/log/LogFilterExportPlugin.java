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

package org.processmining.exporting.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
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
 * @author not attributable
 * @version 1.0
 */
public class LogFilterExportPlugin implements ExportPlugin {
	public LogFilterExportPlugin() {
	}

	/**
	 * accepts
	 * 
	 * @param object
	 *            ProvidedObject
	 * @return boolean
	 * @todo Implement this org.processmining.exporting.ExportPlugin method
	 */
	public boolean accepts(ProvidedObject object) {
		boolean filterFound = false;
		for (int k = object.getObjects().length - 1; (k >= 0) && !filterFound; k--) {
			if (object.getObjects()[k] instanceof LogFilter) {
				filterFound = true;
			} else if (object.getObjects()[k] instanceof LogReader) {
				LogReader r = (LogReader) object.getObjects()[k];
				if (r.getLogFilter() != null) {
					filterFound = true;
				}
			}
		}
		return filterFound;
	}

	/**
	 * export a LogFilter using the java.io.Serializable interface
	 * 
	 * @param object
	 *            ProvidedObject
	 * @param output
	 *            OutputStream
	 * @throws IOException
	 * @todo Implement this org.processmining.exporting.ExportPlugin method
	 */
	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		LogFilter f = null;
		for (int k = object.getObjects().length - 1; (k >= 0) && (f == null); k--) {
			if (object.getObjects()[k] instanceof LogFilter) {
				f = (LogFilter) object.getObjects()[k];
			} else if (object.getObjects()[k] instanceof LogReader) {
				LogReader r = (LogReader) object.getObjects()[k];
				if (r.getLogFilter() != null) {
					f = r.getLogFilter();
				}
			}
		}
		if (f == null) {
			return;
		}

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bw.write("<ProMLogFilter>\n");
		f.writeXML(bw);
		bw.write("</ProMLogFilter>\n");
		bw.close();

		// Write object with ObjectOutputStream
		// ObjectOutputStream obj_out = new ObjectOutputStream(output);

		// Write object out to disk
		// obj_out.writeObject(f);

	}

	/**
	 * getFileExtension
	 * 
	 * @return String
	 * @todo Implement this org.processmining.exporting.ExportPlugin method
	 */
	public String getFileExtension() {
		return "xml";
	}

	/**
	 * Gets a description of this plugin in HTML.
	 * 
	 * @return a description of this plugin in HTML
	 * @todo Implement this org.processmining.framework.plugin.Plugin method
	 */
	public String getHtmlDescription() {
		return "exports a LogFilter to file using an XML file. The LogFilter can be read again by the framework.";
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
}
