/**
 * Project: ProM
 * File: ChangeMiner.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 18, 2006, 12:10:28 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 ***********************************************************
 *
 * This software is part of the ProM package
 * http://www.processmining.org/
 *
 * Copyright (c) 2003-2006 TU/e Eindhoven
 * and is licensed under the
 * Common Public License, Version 1.0
 * by Eindhoven University of Technology
 * Department of Information Systems
 * http://is.tm.tue.nl
 *
 ***********************************************************/
package org.processmining.mining.change;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.UISettings;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.logabstraction.LogRelations;
import org.processmining.mining.partialordermining.AggregationGraphResult;
import org.processmining.mining.partialordermining.PartialOrderAggregationPlugin;
import org.processmining.mining.partialordermining.PartialOrderGeneratorPlugin;

/**
 * This mining plugin uses the instance / multi-phase mining technique to build
 * and aggregate instance graphs describing change processes. The ordering
 * relation between change operations, i.e. events, is not solely based on
 * occurrence of events in the log, but (partially) replaced by the defined
 * notion of commutativity between change operations.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class ChangeMiner implements MiningPlugin {

	protected ChangeMinerOptionsPanel optionsPanel = null;

	/**
	 *
	 */
	public ChangeMiner() {
		super();
		optionsPanel = new ChangeMinerOptionsPanel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#getOptionsPanel(org.processmining
	 * .framework.log.LogSummary)
	 */
	public JPanel getOptionsPanel(LogSummary summary) {
		return optionsPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		if (isChangeLog(log) == false) {
			JOptionPane
					.showMessageDialog(
							null,
							"The given log does not appear to include change information!\n"
									+ "You can only use the change miner on logs which include context\n"
									+ "information related to a process change event\n"
									+ "Most likely you have tried to apply the change miner to a\n"
									+ "regular enactment log, which cannot be handled by this plugin!\n",
							"Cannot find change information in log!",
							JOptionPane.ERROR_MESSAGE);
			return new MiningResult() {
				public LogReader getLogReader() {
					return null;
				}

				public JComponent getVisualization() {
					JPanel dummyPanel = new JPanel();
					dummyPanel
							.add(
									new JLabel(
											"<html><b>The given log does not appear to include change information!<br>"
													+ "You can only use the change miner on logs which include context<br>"
													+ "information related to a process change event<br>"
													+ "Most likely you have tried to apply the change miner to a<br>"
													+ "regular enactment log, which cannot be handled by this plugin!</b></html>"),
									BorderLayout.CENTER);
					return dummyPanel;
				}
			};
		}
		Progress progress = new Progress("Mining "
				+ log.getFile().getShortName() + " using " + getName());
		// test output start
		if (UISettings.getInstance().getTest() == true) {
			Message.add("<ChangeMiner file=\"" + log.getFile().getShortName()
					+ "\" conflictingCausalityAllowed=\""
					+ optionsPanel.isConflictingCausalityAllowed() + "\">",
					Message.TEST);
		}
		// test output end
		CommutativityLogRelationBuilder relationBuilder = new CommutativityLogRelationBuilder(
				log);
		relationBuilder.setAllowConflictingCausality(optionsPanel
				.isConflictingCausalityAllowed());
		LogRelations relations = relationBuilder.getLogRelations();
		// test output start
		if (UISettings.getInstance().getTest() == true) {
			Message.add("</ChangeMiner>", Message.TEST);
		}
		// test output end
		PartialOrderGeneratorPlugin poGenerator = new PartialOrderGeneratorPlugin();
		MiningResult genResult = poGenerator.mine(log, relations, progress);
		PartialOrderAggregationPlugin poAgg = new PartialOrderAggregationPlugin();
		AggregationGraphResult aggResult = (AggregationGraphResult) poAgg.mine(
				genResult.getLogReader(), progress, false);
		progress.close();
		return aggResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Change mining plugin";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	protected boolean isChangeLog(LogReader log) {
		if (log.numberOfInstances() > 0
				&& log.getInstance(0).getAuditTrailEntryList().size() > 0) {
			try {
				AuditTrailEntry ate = log.getInstance(0)
						.getAuditTrailEntryList().get(0);
				if (ate.getAttributes().get("CHANGE.subject") == null) {
					return false;
				} else {
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

}
