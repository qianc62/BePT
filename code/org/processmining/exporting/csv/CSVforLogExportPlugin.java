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

package org.processmining.exporting.csv;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import org.processmining.exporting.*;
import org.processmining.framework.plugin.*;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */

public class CSVforLogExportPlugin implements ExportPlugin {
	public CSVforLogExportPlugin() {
	}

	protected LogReader log;
	protected ArrayList<String> instanceDataSet = new ArrayList<String>();
	protected ArrayList<String> eventDataSet = new ArrayList<String>();

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();
		boolean logr = false;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				logr = true;
			}
		}
		return logr;
	}

	public String getFileExtension() {
		return "csv";
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		// object.getObjects()[0] is the LogReader
		Object[] o = object.getObjects();
		log = null;
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
			}
		}

		// make instance/event data list
		for (int c = 0; c < log.numberOfInstances(); c++) {
			AuditTrailEntryList ateList = log.getInstance(c)
					.getAuditTrailEntryList();
			Map<String, String> attributes = log.getInstance(c).getAttributes();
			for (String key : attributes.keySet()) {
				if (!instanceDataSet.contains(key))
					instanceDataSet.add(key);
			}
			for (int i = 0; i < ateList.size(); i++) {
				for (String key : ateList.get(i).getAttributes().keySet()) {
					if (!eventDataSet.contains(key))
						eventDataSet.add(key);
				}
			}
		}

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
		// write first line
		bw.write("caseID;taskID;originator;eventtype;timestamp;");
		for (String str : instanceDataSet) {
			bw.write(str + ";");
		}
		for (String str : eventDataSet) {
			bw.write(str + ";");
		}
		bw.write("\n");

		for (int c = 0; c < log.numberOfInstances(); c++) {
			AuditTrailEntryList ateList = log.getInstance(c)
					.getAuditTrailEntryList();
			Map<String, String> attributes = log.getInstance(c).getAttributes();
			String temp = "";
			for (String str : instanceDataSet) {
				if (attributes.containsKey(str)) {
					temp += attributes.get(str) + ";";
				} else {
					temp += ";";
				}
			}
			for (int i = 0; i < ateList.size(); i++) {
				AuditTrailEntry ate = ateList.get(i);
				bw.write(log.getInstance(c).getName() + ";");
				bw.write(ate.getName() + ";");
				if (ate.getOriginator() == null) {
					bw.write(";");
				} else {
					bw.write(ate.getOriginator() + ";");
				}
				if (ate.getType() == null) {
					bw.write(";");
				} else {
					bw.write(ate.getType() + ";");
				}
				if (ate.getTimestamp() == null) {
					bw.write(";");
				} else {
					bw.write(format.format(ate.getTimestamp()) + ";");
				}
				bw.write(temp);
				for (String str : eventDataSet) {
					if (ate.getAttributes().containsKey(str)) {
						bw.write(ate.getAttributes().get(str) + ";");
					} else {
						bw.write(";");
					}
				}
				bw.write("\n");
			}
		}
		bw.close();
	}

	public String getName() {
		// return "Comma Separated Values";
		return "CSV for log Exporter";
	}

	public String getHtmlDescription() {
		String s = "<html>";
		s += "<head><title>ProM Framework: CSV for log Export Plug-in</title></head>";
		s += "<body><h1>CSV for Log Export Plug</h1>";
		s += "<p>The CSV for log Export Plug-in writes a log to a CSV file.</p>";
		s += "</body></html>";
		return s;
	}

}
