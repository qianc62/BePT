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

import java.util.Iterator;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.mining.petrinetmining.PetriNetResult;

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
public class PetriNetLogReaderConnectionPlugin implements AnalysisPlugin {

	public PetriNetLogReaderConnectionPlugin() {
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
		AnalysisInputItem pnet = new AnalysisInputItem("Petri net") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasPetrinet = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof PetriNet) {
						hasPetrinet = true;
					}
					;
				}
				;
				return hasPetrinet;
			}
		};
		return new AnalysisInputItem[] { logReader, pnet };
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		LogReader log = null;
		PetriNet pnet = null;
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
			if (inputs[j].getCaption().equals("Petri net")) {
				Object[] o = (inputs[j].getProvidedObjects())[0].getObjects();

				for (int i = 0; pnet == null; i++) {
					if (o[i] instanceof PetriNet) {
						pnet = (PetriNet) o[i];
					}
					;
				}
				;
			}
		}
		if ((pnet == null) || (log == null)) {
			return null;
		}

		Message.add("<ConnectPetriNetWithLog>", Message.TEST);
		PetriNetResult result = new PetriNetResult(pnet);
		boolean fuzzy = true;
		Iterator it = pnet.getTransitions().iterator();
		while (fuzzy && it.hasNext()) {
			fuzzy = (((Transition) it.next()).getLogEvent() == null);
		}

		if (MainUI.getInstance().connectResultWithLog(result, log, this, fuzzy,
				false)) {
			Message.add("</ConnectPetriNetWithLog>", Message.TEST);
			return new MiningResultComponent(result);
		} else {
			Message.add("</ConnectPetriNetWithLog>", Message.TEST);
			return null;
		}
	}

	public String getName() {
		return "Connect Petri net to Log file";
	}

	public String getHtmlDescription() {
		return "This plugin connects a Petri net to a new log file, without the need for "
				+ "writing the Petri net to file first.";
	}
}
