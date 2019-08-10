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

package org.processmining.analysis.logreaderconnection;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.importing.heuristicsnet.HeuristicsNetResultWithLogReader;

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
public class HNetLogReaderConnectionPlugin implements AnalysisPlugin {

	public HNetLogReaderConnectionPlugin() {
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem logReader = new AnalysisInputItem("Log File") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLogReader = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLogReader = true;
					}
					;
				}
				;
				return hasLogReader;
			}
		};
		AnalysisInputItem pnet = new AnalysisInputItem("Heuristics net") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasNet = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof HeuristicsNet) {
						hasNet = true;
					}
					;
				}
				;
				return hasNet;
			}
		};
		return new AnalysisInputItem[] { logReader, pnet };
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		LogReader log = null;
		HeuristicsNet hnet = null;
		for (int j = 0; j < inputs.length; j++) {
			if (inputs[j].getCaption().equals("Log File")) {
				Object[] o = (inputs[j].getProvidedObjects())[0].getObjects();

				for (int i = 0; log == null; i++) {
					if (o[i] instanceof LogReader) {
						log = (LogReader) o[i];
					}
					;
				}
				;
			}
			if (inputs[j].getCaption().equals("Heuristics net")) {
				Object[] o = (inputs[j].getProvidedObjects())[0].getObjects();

				for (int i = 0; hnet == null; i++) {
					if (o[i] instanceof HeuristicsNet) {
						hnet = (HeuristicsNet) o[i];
					}
					;
				}
				;
			}
		}
		if ((hnet == null) || (log == null)) {
			return null;
		}
		Message.add("<ConnectHeuristicNetWithLog>", Message.TEST);

		HeuristicsNetResultWithLogReader result = new HeuristicsNetResultWithLogReader(
				hnet, null);

		if (MainUI.getInstance().connectResultWithLog(result, log, this, true,
				false)) {
			Message.add("</ConnectHeuristicNetWithLog>", Message.TEST);
			return new MiningResultComponent(result);
		} else {
			Message.add("</ConnectHeuristicNetWithLog>", Message.TEST);
			return null;
		}
	}

	public String getName() {
		return "Connect Heuristics net to Log file";
	}

	public String getHtmlDescription() {
		return "This plugin connects a Heuristics net to a new log file, without the need for "
				+ "writing the Heuritics net to file first.";
	}
}
