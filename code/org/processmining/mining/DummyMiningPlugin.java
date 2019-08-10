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

package org.processmining.mining;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;

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
 * @author not attributable
 * @version 1.0
 */

public class DummyMiningPlugin implements MiningPlugin {

	public DummyMiningPlugin() {
	}

	public JPanel getOptionsPanel(LogSummary summary) {
		return null;
	}

	public MiningResult mine(LogReader log) {
		ArrayList a = new ArrayList();
		Progress p = new Progress("Reading process instance:", 0, log
				.getLogSummary().getNumberOfProcessInstances());
		int j = 0;
		while (j < log.numberOfInstances()) {
			ProcessInstance pi = log.getInstance(j);
			p.setNote(pi.getName());
			p.setProgress(j++);
			a.add(pi.getName());
		}
		p.close();
		Message.add("<Dummyminer loadedPIs=" + a.size() + "/>", Message.TEST);
		return new DummyMiningResult(log, a);
	}

	public String getName() {
		return "Process Instance Inspector";
	}

	public String getHtmlDescription() {
		return "This plugin only returns the process instances, without doing anything.";
	}

}
