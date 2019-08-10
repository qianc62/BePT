/*
 * Created on Jun 17, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning.freqstat;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

/**
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class DmcProximitySensing implements MiningPlugin {

	protected FrequencyStatistics statistics = null;

	public DmcProximitySensing() {
		statistics = new FrequencyStatistics();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#getOptionsPanel(org.processmining
	 * .framework.log.LogSummary)
	 */
	public JPanel getOptionsPanel(LogSummary summary) {
		return new JPanel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		statistics.initialize();
		Progress progress = new Progress("Accumulating proximity set...");
		int progCounter = 0;
		progress.setMaximum(log.getLogSummary().getNumberOfAuditTrailEntries());
		while (log.hasNext()) {
			// iterating through process instances
			ProcessInstance instance = log.next();
			AuditTrailEntry last = null;
			AuditTrailEntry current = null;
			long currentProximity = 0;
			AuditTrailEntries ates = instance.getAuditTrailEntries();
			while (ates.hasNext()) {
				current = ates.next();
				if ((last != null) && (last != current)) {
					// Message.add("this: " + current.getTimestamp().getTime() +
					// ", last: " + last.getTimestamp().getTime());
					currentProximity = current.getTimestamp().getTime()
							- last.getTimestamp().getTime();
					statistics.addValue(currentProximity);
					progCounter++;
					progress.setProgress(progCounter);
				}
				last = current;
			}
		}
		Message.add("Done scanning proximity profile. Built "
				+ statistics.getNumberOfEntries() + " entries.");
		Message.add("Distinct values recorded: "
				+ statistics.getNumberOfValues());
		Message.add("Value range is: [" + statistics.getMinValue() + ", "
				+ statistics.getMaxValue() + "].");
		return new ProximitySensingResults(statistics, log);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "DMC proximity sensing";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "no description available yet - bug me if this persists..:)";
	}

}
