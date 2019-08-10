/*
 * Created on May 19, 2005
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
package org.processmining.mining.dmcscanning;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.UISettings;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.dmcscanning.aggregation.AggregationMethod;
import org.processmining.mining.dmcscanning.aggregation.GreedyAggregation;
import org.processmining.mining.dmcscanning.aggregation.SimpleAggregation;
import org.processmining.mining.dmcscanning.aggregation.TolerantAggregation;
import org.processmining.mining.dmcscanning.equivalence.FlowerEquivalence;
import org.processmining.mining.dmcscanning.equivalence.ObjectEquivalence;
import org.processmining.mining.dmcscanning.equivalence.StrictEquivalence;
import org.processmining.mining.dmcscanning.ui.DmcOptionsPanel;

/**
 * DMCMiner. This ProM mining plugin implements the concept of Modification
 * Cluster Scanning. Applicable for any sort of event logs, preferrably for sc.
 * data modification logs.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class DmcMiner implements MiningPlugin {

	/* data structures to store (intermediate) results */
	protected DmcSet initialDmcs = null;
	protected AdmcSet admcs = null;
	protected MdmcSet mdmcs = null;

	/* settings */
	protected long maxProximity = 0; // scan window size in milliseconds
	protected long maxNoEvents = 0; // scan window size in number of events
	protected boolean useBreakingProximity = false; // interpret proximity as
	// split-on-break
	protected boolean enforceOriginator = true; // require originators to be
	// uniform within DMCs
	protected boolean enforceEventType = true; // require event types to be
	// uniform within DMCs
	protected boolean consolidateADMC = false; // whether ADMC shall be
	// consolidated
	protected AggregationMethod aggregator = null; // method used for ADMC
	// aggregation
	protected double mdmcSelectionBalance = 0.5; // balance for MDMC derivation
	protected int mdmcSelectionIterations = 1; // number of iterations in MDMC
	// derivation
	protected boolean checkDmcConsistency = false; // whether to periodically
	// check consistency of the
	// DMC set

	/* aggregation methods */
	protected static ArrayList aggregationMethods = null;

	/* object equivalence relation implementations */
	protected ObjectEquivalence equivalence = null;
	protected static ArrayList equivalenceRelations = null;

	/* options panel */
	protected DmcOptionsPanel optionsPanel = null;

	/**
	 * @return ArrayList of all available Aggregation Methods
	 */
	public static ArrayList aggregationMethods() {
		if (aggregationMethods == null) {
			aggregationMethods = new ArrayList();
			// add all available methods here
			aggregationMethods.add(new SimpleAggregation());
			aggregationMethods.add(new TolerantAggregation());
			aggregationMethods.add(new GreedyAggregation());
		}
		return aggregationMethods;
	}

	public static ArrayList equivalenceRelations() {
		if (equivalenceRelations == null) {
			equivalenceRelations = new ArrayList();
			// add all available equivalence relations here
			equivalenceRelations.add(new StrictEquivalence());
			equivalenceRelations.add(new FlowerEquivalence());
		}
		return equivalenceRelations;
	}

	/**
	 * constructor
	 */
	public DmcMiner() {
		aggregator = new SimpleAggregation(); // default aggregation method
		initialize();
	}

	/**
	 * initializes all scan-dependent data structures and settings
	 */
	protected void initialize() {
		initialDmcs = null;
		admcs = null;
		mdmcs = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#getOptionsPanel(org.processmining
	 * .framework.log.LogSummary)
	 */
	public JPanel getOptionsPanel(LogSummary summary) {
		if (optionsPanel == null) {
			optionsPanel = new DmcOptionsPanel(this, summary);
		}
		return optionsPanel;
	}

	protected void displayErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, message,
				"Unable to start activity clustering miner!",
				JOptionPane.ERROR_MESSAGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		// check if the log has timestamps (requirement!)
		try {
			if (log.getInstance(0) != null
					&& log.getInstance(0).getAuditTrailEntryList().size() > 0
					&& log.getInstance(0).getAuditTrailEntryList().get(0)
							.getTimestamp() == null) {
				displayErrorMessage("The activity clustering miner depends on the\n"
						+ "availability of timestamp information for all\n"
						+ "events in the log to be analyzed!\n\n"
						+ "The log you specified does not seem to satisfy\n"
						+ "this requirement, thus mining has been aborted!\n");
				return null;
			}
		} catch (IOException e) {
			return null;
		}
		// log = new ProxyLogReader(log);
		initialize();
		// update values from options panel
		updateConfiguration();
		// start work
		long timer = System.currentTimeMillis();
		Progress progress = new Progress("Scanning for clusters");
		double consolidationRatio = 1.0;
		// extract initial DMCs first
		Message.add("Maximal proximity set to " + maxProximity + " msec.");
		Message.add("Scanning initial clusters from log...");
		if (useBreakingProximity == true) {
			// interpret proximity as split-on-break threshold
			initialDmcs = SplitOnBreakDmcScanner.scanInitialDmcs(log, progress,
					maxProximity, maxNoEvents, equivalence);
		} else {
			// interpret proximity as regular scan window size
			initialDmcs = BufferedDmcScanner.scanInitialDmcs(log, progress,
					maxProximity, maxNoEvents, enforceOriginator,
					enforceEventType, equivalence);
		}
		Message.add("Initial cluster scan done. Extracted "
				+ initialDmcs.size() + " initial clusters in "
				+ ((System.currentTimeMillis() - timer) / 1000.0) + " sec.");
		timer = System.currentTimeMillis();
		progress.close();
		progress = new Progress("Aggregating clusters...");
		if (checkDmcConsistency) {
			Message.add("[Initial cluster set consistency ratio: "
					+ initialDmcs.checkConsistency(equivalence) + "]");
		}
		// aggregate to ADMC
		Message.add("Aggregating initial clusters to aggregated set...");
		admcs = AdmcSet.buildAdmcSet(initialDmcs, aggregator, progress);
		if (checkDmcConsistency) {
			Message.add("[Initial cluster set consistency ratio: "
					+ initialDmcs.checkConsistency(equivalence) + "]");
		}
		Message.add("...done. Derived " + admcs.size()
				+ " aggregated clusters in "
				+ ((System.currentTimeMillis() - timer) / 1000.0) + " sec.");
		timer = System.currentTimeMillis();
		if (consolidateADMC == true) {
			progress.setNote("Consolidating aggregated set of clusters...");
			Message.add("Consolidating aggregated set of clusters...");
			progress.setMinMax(0, 1);
			progress.setProgress(0);
			consolidationRatio = admcs.consolidate();
			progress.setProgress(1);
			Message
					.add("...done. Consolidation ratio: " + consolidationRatio
							+ " (" + admcs.consolidationVictimsSize()
							+ " victims), "
							+ ((System.currentTimeMillis() - timer) / 1000.0)
							+ " sec.");
			timer = System.currentTimeMillis();
		}
		progress.close();
		progress = new Progress("Deriving minimal conflict-free set...");
		Message
				.add("Deriving minimal conflict-free set of clusters from aggregated set...");
		mdmcs = BalancedMdmcSet.buildMDMC(admcs, mdmcSelectionBalance,
				mdmcSelectionIterations, progress);
		if (checkDmcConsistency) {
			Message.add("[Initial cluster set consistency ratio: "
					+ initialDmcs.checkConsistency(equivalence) + "]");
		}
		Message.add("...done. Determined " + mdmcs.size()
				+ " ADMCs in MDMC, in "
				+ ((System.currentTimeMillis() - timer) / 1000.0) + " sec.");
		Message.add("Pre-processing passes finished.");
		progress.close();
		if (UISettings.getInstance().getTest() == true) {
			testOutput();
		}
		return new DmcScanningResult(log, initialDmcs, admcs, mdmcs);
	}

	/**
	 * retrieves the current configuration data from the options panel
	 */
	protected void updateConfiguration() {
		maxProximity = optionsPanel.getMaxProximityTime();
		maxNoEvents = optionsPanel.getMaxNumberOfEvents();
		useBreakingProximity = optionsPanel.isUsingBreakingProximity();
		enforceOriginator = optionsPanel.isEnforceOriginator();
		enforceEventType = optionsPanel.isEnforceEventType();
		checkDmcConsistency = optionsPanel.isMonitorDmcConsistency();
		consolidateADMC = optionsPanel.isConsolidateAdmc();
		aggregator = optionsPanel.getAggregationMethod();
		mdmcSelectionBalance = optionsPanel.getMdmcSelectionBalance();
		mdmcSelectionIterations = optionsPanel.getMdmcSelectionIterations();
		equivalence = optionsPanel.getObjectEquivalence();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Activity Clustering Miner";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
		/*
		 * return "<h1>"+getName()+"</h1>" + "<p>" +
		 * "This plugin implements the concept of <b>Modification Cluster Scanning</b>.<br>"
		 * +
		 * "An initial pass scans a number of Data Modification Clusters (DMCs) from the "
		 * +
		 * "log. In a second pass, these initial DMCs are aggregated to ADMCs (aggregated DMCs), "
		 * +
		 * "according to their similarity. In the third pass, a minimal set of DMCs (MDMC) "
		 * +
		 * "is determined. MDMC is defined as the conflict-free subset of ADMC.<br><br>"
		 * + "<b>Options:</b><br><ul>" +
		 * "<li><b>Maximal proximity</b><br>The maximal proximity between the first and last "
		 * + "event of a DMC, in milliseconds (temporal scan window size).</li>"
		 * +
		 * "<li><b>Maximal number of events</b><br>The maximal proximity between the first and last "
		 * +
		 * "event of a DMC, in number of events (nominal scan window size).<br>"
		 * +
		 * "<i><b>Notice:</b> In case maximal proximity and maximal number of events are in conflict, "
		 * +
		 * "the smallest applicable scan window boundary will be used.</i></li>"
		 * +
		 * "<li><b>Consolidate ADMC</b><br>If this option is enabled, after aggregating to ADMC the "
		 * +
		 * "aggregation method's algorithm for ADMC consolidation will try and limit the ADMC set to "
		 * + "a reasonable subset better suited for deriving MDMC. " +
		 * "<i>Result dependent on aggregation method!<i></li>" +
		 * "<li><b>Aggregation method</b><br>The choice between multiple aggregation methods enables "
		 * +
		 * "influencing the aggregation and consolidation pass. These routines are implemented in a "
		 * +
		 * "pluggable method class, enabling them to be easily extended and exchanged.</li>"
		 * + "</ul><br><br><i>More concise information is to be added soon</i>"
		 * + "</p>";
		 */
	}

	// -------- getters and setters --------------------------------------------

	/**
	 * @return the currently active aggregation method for ADMC generation
	 */
	public AggregationMethod getAggregationMethod() {
		return aggregator;
	}

	/**
	 * sets the aggregation method for ADMC generation
	 * 
	 * @param method
	 */
	public void setAggregationMethod(AggregationMethod method) {
		aggregator = method;
	}

	/**
	 * @return Returns whether ADMC is to be consolidated after derivation.
	 */
	public boolean isConsolidateADMC() {
		return consolidateADMC;
	}

	/**
	 * @param consolidateADMC
	 *            Whether ADMC is to be consolidated after derivation.
	 */
	public void setConsolidateADMC(boolean consolidateADMC) {
		this.consolidateADMC = consolidateADMC;
	}

	/**
	 * @return Returns the maximal number of events within a DMC.
	 */
	public long getMaxNoEvents() {
		return maxNoEvents;
	}

	/**
	 * @param maxNoEvents
	 *            The maximal number of events within a DMC.
	 */
	public void setMaxNoEvents(long maxNoEvents) {
		this.maxNoEvents = maxNoEvents;
	}

	/**
	 * @return Returns the maximal proximity (in milliseconds) between the first
	 *         and last events within a DMC.
	 */
	public long getMaxProximity() {
		return maxProximity;
	}

	/**
	 * @param maxProximity
	 *            The maximal proximity (in milliseconds) between the first and
	 *            last events within a DMC.
	 */
	public void setMaxProximity(long maxProximity) {
		this.maxProximity = maxProximity;
	}

	/**
	 * @return whether all events within a DMC are enforced to have the same
	 *         event type
	 */
	public boolean isEnforceEventType() {
		return enforceEventType;
	}

	/**
	 * @param enforceEventType
	 *            whether all events within a DMC are enforced to have the same
	 *            event type
	 */
	public void setEnforceEventType(boolean enforceEventType) {
		this.enforceEventType = enforceEventType;
	}

	/**
	 * @return whether all events within a DMC are enforced to have the same
	 *         originator
	 */
	public boolean isEnforceOriginator() {
		return enforceOriginator;
	}

	/**
	 * @param enforceOriginator
	 *            whether all events within a DMC are enforced to have the same
	 *            originator
	 */
	public void setEnforceOriginator(boolean enforceOriginator) {
		this.enforceOriginator = enforceOriginator;
	}

	/**
	 * @return Returns the mdmcSelectionBalance.
	 */
	public double getMdmcSelectionBalance() {
		return mdmcSelectionBalance;
	}

	/**
	 * @param mdmcSelectionBalance
	 *            The mdmcSelectionBalance to set.
	 */
	public void setMdmcSelectionBalance(double mdmcSelectionBalance) {
		this.mdmcSelectionBalance = mdmcSelectionBalance;
	}

	/**
	 * @return Returns the mdmcSelectionIterations.
	 */
	public int getMdmcSelectionIterations() {
		return mdmcSelectionIterations;
	}

	/**
	 * @param mdmcSelectionIterations
	 *            The mdmcSelectionIterations to set.
	 */
	public void setMdmcSelectionIterations(int mdmcSelectionIterations) {
		this.mdmcSelectionIterations = mdmcSelectionIterations;
	}

	/**
	 * @return Returns the equivalence.
	 */
	public ObjectEquivalence getEquivalence() {
		return equivalence;
	}

	/**
	 * @param equivalence
	 *            The equivalence to set.
	 */
	public void setEquivalence(ObjectEquivalence equivalence) {
		this.equivalence = equivalence;
	}

	/**
	 * @return Returns the checkDmcConsistency.
	 */
	public boolean isCheckDmcConsistency() {
		return checkDmcConsistency;
	}

	/**
	 * @param checkDmcConsistency
	 *            The checkDmcConsistency to set.
	 */
	public void setCheckDmcConsistency(boolean checkDmcConsistency) {
		this.checkDmcConsistency = checkDmcConsistency;
	}

	/**
	 * Whether proximity is interpreted as split-on-break clustering threshold
	 * 
	 * @return
	 */
	public boolean isUsingBreakingProximity() {
		return useBreakingProximity;
	}

	/**
	 * Sets whether proximity is interpreted as split-on-break clustering
	 * threshold
	 * 
	 * @param isUsed
	 */
	public void setUseBreakingProximity(boolean isUsed) {
		useBreakingProximity = isUsed;
	}

	/**
	 * creates the required Message.TEST output
	 */
	public void testOutput() {
		Message.add("<DmcMiner>", Message.TEST);
		Message.add("\t<Options>", Message.TEST);
		Message.add("\t\t<Option name=\"maxProximityTime\" value=\""
				+ optionsPanel.getMaxProximityTime() + "\"/>", Message.TEST);
		Message.add("\t\t<Option name=\"maxNumberOfEvents\" value=\""
				+ optionsPanel.getMaxNumberOfEvents() + "\"/>", Message.TEST);
		Message.add("\t\t<Option name=\"isUsingBreakingProximity\" value=\""
				+ optionsPanel.isUsingBreakingProximity() + "\"/>",
				Message.TEST);
		Message.add("\t\t<Option name=\"isEnforceOriginator\" value=\""
				+ optionsPanel.isEnforceOriginator() + "\"/>", Message.TEST);
		Message.add("\t\t<Option name=\"isEnforceEventType\" value=\""
				+ optionsPanel.isEnforceEventType() + "\"/>", Message.TEST);
		Message
				.add("\t\t<Option name=\"isMonitorDmcConsistency\" value=\""
						+ optionsPanel.isMonitorDmcConsistency() + "\"/>",
						Message.TEST);
		Message.add("\t\t<Option name=\"isConsolidateAdmc\" value=\""
				+ optionsPanel.isConsolidateAdmc() + "\">", Message.TEST);
		Message.add("\t\t<Option name=\"aggregationMethod\" value=\""
				+ optionsPanel.getAggregationMethod().toString() + "\"/>",
				Message.TEST);
		Message
				.add("\t\t<Option name=\"mdmcSelectionBalance\" value=\""
						+ optionsPanel.getMdmcSelectionBalance() + "\"/>",
						Message.TEST);
		Message.add("\t\t<Option name=\"mdmcSelectionIterations\" value=\""
				+ optionsPanel.getMdmcSelectionIterations() + "\"/>",
				Message.TEST);
		Message.add("\t\t<Option name=\"objectEquivalence\" value=\""
				+ optionsPanel.getObjectEquivalence().toString() + "\"/>",
				Message.TEST);
		Message.add("\t</Options>", Message.TEST);
		this.initialDmcs.testOutput();
		this.admcs.testOutput();
		this.mdmcs.testOutput();
		Message.add("</DmcMiner>", Message.TEST);
	}
}
