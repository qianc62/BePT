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

package org.processmining.mining.logabstraction;

import javax.swing.JPanel;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.LogRelationEditor;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.ProgressDummy;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public abstract class LogRelationBasedAlgorithm implements MiningPlugin {

	private LogRelationUI ui = null;

	protected LogRelations logRelations;
	private LogRelationEditor relationsEditor = new LogRelationEditor(
			logRelations);
	private LogReader log;

	abstract public MiningResult mine(LogReader log, LogRelations relations,
			Progress progress);

	public MiningResult mine(LogReader log) {
		MiningResult result;
		Progress progress;

		progress = new Progress("Calculating log relations");
		logRelations = getLogRelations(log, progress);
		if (logRelations == null) {
			return null;
		}

		progress = new Progress("Mining " + log.getFile().getShortName()
				+ " using " + getName());
		result = mine(log, logRelations, progress);

		this.log = log;

		progress.close();
		return result;
	}

	public JPanel getOptionsPanel(LogSummary summary) {
		if (ui == null) {
			ui = new LogRelationUI(summary);
		}
		return ui;
	}

	public LogRelationUI getUI() {
		return ui;
	}

	public LogRelations getLogRelations(LogReader log, Progress progress) {
		boolean useFsm = (ui != null && ui.getFsmLogFilter());
		boolean usePO = (ui != null && ui.usePO());
		LogAbstraction logAbstraction;
		LogRelations relations;
		String[][] intervals;

		Message.add("Starting log filtering...", Message.DEBUG);
		if (progress != null) {
			progress.setMinMax(0, useFsm ? 5 : 4);
		}

		// First layer: abstract from log, make direct succession and model
		// elements
		message(
				"Starting log abstraction: building >                             ",
				1, progress);
		logAbstraction = new LogAbstractionImpl(log, usePO);

		if (progress != null && progress.isCanceled()) {
			return null;
		}

		// Second layer: Abstract from succession and build causal and parallel
		// relations
		message("Starting log abstraction: building -> and ||", 2, progress);
		relations = (new MinValueLogRelationBuilder(logAbstraction, 0, log
				.getLogSummary().getLogEvents())).getLogRelations();

		if (progress != null && progress.isCanceled()) {
			return null;
		}

		// Third layer: Use Finite State Machine to insert causality
		if (useFsm) {
			message("Starting log abstraction: building -> based on FSM", 3,
					progress);
			relations = (new FSMLogRelationBuilder(relations))
					.getLogRelations();
		}

		if (progress != null && progress.isCanceled()) {
			return null;
		}

		// Fourth layer: Use log ordering to determine paralellism and causality
		message("Starting log abstraction: building || based on overlap", 4,
				progress);
		intervals = (ui == null ? new String[0][0] : ui.getIntervals());
		for (int i = 0; i < intervals.length; i++) {
			relations = (new TimeIntervalLogRelationBuilder(relations, log,
					intervals[i][0], intervals[i][1])).getLogRelations();

			if (progress != null && progress.isCanceled()) {
				return null;
			}
		}

		if (progress.isCanceled()) {
			progress.close();
			return null;
		}
		progress.close();
		return relations;
	}

	public boolean showIntermediateUpdates() {
		if (ui == null) {
			return false;
		}
		return ui.showIntResults();
	}

	public MiningResult editRelations(LogEvent event) {
		if (!relationsEditor.edit(logRelations, event)) {
			return null;
		}

		logRelations = relationsEditor.getLogRelations();
		return mine(log, logRelations, new ProgressDummy());
	}

	private void message(String msg, int stage, Progress progress) {
		Message.add(msg, Message.DEBUG);
		if (progress != null) {
			progress.setNote(msg);
			progress.setProgress(stage);
		}
	}
}
