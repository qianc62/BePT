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

package org.processmining.analysis.summary;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class LogSummaryPlugin implements AnalysisPlugin {

	public LogSummaryPlugin() {
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		LogReader log = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
			}
		}

		return new LogSummaryUI(log);
	}

	public String getHtmlDescription() {
		return "<p> This plug-in returns statistical information about a log.<p> ";
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Log") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLog = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLog = true;
					}
				}
				return hasLog;
			}
		} };
		return items;

	}

	public String getName() {
		return "Log Summary";
	}
}
