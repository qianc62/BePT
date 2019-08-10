package org.processmining.mining.armining;

import java.io.IOException;

import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.util.PluginDocumentationLoader;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

/**
 * <p>
 * Title: ARMiner
 * </p>
 * <p>
 * Description:The mining plug-in "Association Rule Miner" generates association
 * rules for the event log. It also calculates frequent itemsets. The algorithms
 * implemented in this plug-in are the Apriori algorithm and the Predictive
 * Apriori algorithm.
 * </p>
 * 
 * @author Shaifali Gupta (s.gupta@student.tue.nl)/shaifaligupta80@yahoo.com
 *         23rd May 2007
 * @version 1.0
 */

public class ARMiner implements MiningPlugin {

	public ARMiner() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private ARMinerUI armUI = null;

	/**
	 * Returns the HTML description of this plugin in the help system or as
	 * context sensitive help.
	 * 
	 * @return String
	 */
	public String getHtmlDescription() {
		return ("<h1>" + getName() + "</h1>" + PluginDocumentationLoader
				.load(this));
	}

	/**
	 * Returns the name of the plugin in the ProM list for mining plug-ins.
	 * 
	 * @return String
	 */
	public String getName() {
		return "Association Rule Miner";
	}

	/**
	 * Return a JPanel that contains a user interface for setting the options
	 * specific to this plugin.
	 * 
	 * @param logSummary
	 *            LogSummary
	 * @return JPanel
	 */
	public JPanel getOptionsPanel(LogSummary logSummary) {
		if (armUI == null) {
			armUI = new ARMinerUI();
		}
		return armUI;
	}

	/**
	 * Executes the mining (algorithm) plug-in.
	 * 
	 * @param logReader
	 *            LogReader
	 * @return MiningResult
	 */
	public MiningResult mine(LogReader logReader) {
		MiningResult result;
		AssociationAnalyzer myAnalyzer = null;
		myAnalyzer = armUI.getAnalyzerObject();

		// whether the user wants to insert a no name activity (sort of dummy
		// activity)
		Boolean dummyChkBoxValue = myAnalyzer.isCheckBoxDummySelected();

		Boolean eventCareChkBoxValue = myAnalyzer.isCheckBoxECSelected();

		// Insert dummy with event type information
		if (dummyChkBoxValue == true && eventCareChkBoxValue == true) {
			try {
				myAnalyzer.createInputForWeka2(logReader);
			} catch (IndexOutOfBoundsException ex) {
			} catch (IOException ex) {
			}
		} else {
			// Insert dummy without event type information
			if (dummyChkBoxValue == true && eventCareChkBoxValue == false) {
				try { //
					myAnalyzer.createInputForWeka3(logReader);
				} catch (IndexOutOfBoundsException ex) {
				} catch (IOException ex) {
				}
			} else {
				// Do not insert dummy but retain event type information
				if (dummyChkBoxValue == false && eventCareChkBoxValue == true) {
					try { //
						myAnalyzer.createInputForWeka4(logReader);
					} catch (IndexOutOfBoundsException ex) {
					} catch (IOException ex) {
					}
				} else {
					try { // dont do anything, work on original log (without
						// event type information), both are false
						myAnalyzer.createInputForWeka(logReader);
					} catch (IndexOutOfBoundsException ex) {
					} catch (IOException ex) {
					}
				}
			}
		}

		myAnalyzer.initAssociator();
		// myBuildAssociations contains the actual method buildAssociations()
		// available in Weka that actually does the mining work.
		myAnalyzer.myBuildAssociations();
		// The result in form of rules/FIS is given to the ARMinerResult class
		// for display.
		result = new ARMinerResult(myAnalyzer, logReader);
		return result;
	}

	private void jbInit() throws Exception {
	}
}
