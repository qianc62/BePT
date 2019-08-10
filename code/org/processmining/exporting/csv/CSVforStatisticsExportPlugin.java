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
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.processmining.exporting.*;
import org.processmining.framework.plugin.*;
import org.processmining.analysis.performance.fsmanalysis.FSMStatistics;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */

public class CSVforStatisticsExportPlugin implements ExportPlugin {
	public CSVforStatisticsExportPlugin() {
	}

	protected AggregateProfile agProfiles;
	protected HashMap<String, DescriptiveStatistics> suMap;
	protected FSMStatistics fsmStatistics;

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (checkDSmap(o[i]))
				return true;
		}
		return false;
	}

	protected boolean checkDSmap(Object o) {
		if (o instanceof HashMap) {
			HashMap tempMap = ((HashMap) o);
			if (tempMap.get(tempMap.keySet().iterator().next()) instanceof DescriptiveStatistics)
				;
			return true;
		}
		return false;
	}

	public String getFileExtension() {
		return "csv";
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		// object.getObjects()[0] is the LogReader
		Object[] o = object.getObjects();
		suMap = null;

		for (int i = 0; suMap == null && i < o.length; i++) {
			if (checkDSmap(o[i])) {
				suMap = (HashMap<String, DescriptiveStatistics>) o[i];
			}
		}

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));

		for (String key : suMap.keySet()) {
			bw.write(key.replace(',', ';') + ", ");
			double[] data = suMap.get(key).getValues();

			for (int i = 0; i < data.length; i++) {
				bw.write(data[i] + ", ");
			}
			bw.write("\n");
		}

		bw.close();
	}

	public String getName() {
		// return "Comma Separated Values";
		return "Descriptive Statistics CSV";
	}

	public String getHtmlDescription() {
		String s = "<html>";
		s += "<head><title>ProM Framework: Standard CSV Export Plug-in</title></head>";
		s += "<body><h1>Standard CSV Export Plug-in</h1>";
		s += "<p>The Standard CSV Export Plug-in writes the output of the Case Data Extractor Mining plug-in to a CSV file.</p>";
		s += "</body></html>";
		return s;
	}

}
