package org.processmining.mining.tsmining;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.instancemining.ModelGraphResult;
import org.processmining.framework.models.transitionsystem.TSConstants;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class TSGenerator implements MiningPlugin {
	private TSGeneratorOptionsPanel ui = null;

	public String getName() {
		return "Transition System Generator";
	}

	public String getHtmlDescription() {
		return null;
	}

	public JPanel getOptionsPanel(LogSummary summary) {
		if (ui == null)
			ui = new TSGeneratorOptionsPanel(summary);
		return ui;
	}

	public MiningResult mine(LogReader log) {
		int genFlags = TSConstants.MODEL_ELEMENTS; // options for Transition
		// System Generation
		int visFlags = TransitionSystem.IDENTIFIER; // option for Transition
		// System VIsualization
		int typeOfTS = 0;
		int typeOfStrat = 0;
		// Check the user interface settings
		if (ui.isEventTypes())
			genFlags |= TSConstants.EVENT_TYPES;
		if (ui.isExplicitEnd())
			genFlags |= TSConstants.EXPLICIT_END;
		if (ui.isKillLoops())
			genFlags |= TSConstants.KILL_LOOPS;
		if (ui.isIDNames())
			visFlags = TransitionSystem.ID;

		if (ui.isBasicTransitionSystemSets())
			typeOfTS |= TSConstants.SETS;
		else
			typeOfTS |= TSConstants.BAGS;
		if (ui.useExtendedStrategy())
			typeOfStrat |= TSConstants.EXTENDED;

		GregorianCalendar startCal = new GregorianCalendar();
		Date startTime = startCal.getTime();

		TransitionSystem ts = null;
		TSGAlgorithm algorithm = null;
		TSStrategy modificationStrategy = null;
		// ========== Execute the Generation Algorithms ========== START
		if (ui.haveTimestamps())
			algorithm = new TSGAlgorithmTime(log, typeOfTS, genFlags, visFlags);
		else
			algorithm = new TSGAlgorithmNoTime(log, typeOfTS, genFlags,
					visFlags);
		ts = algorithm.getTransitionSystem();
		// ========== Execute the Generation Algorithms ========== FINISH
		// ========== Execute Modification Strategy ========== START
		if ((typeOfStrat & TSConstants.EXTENDED) == TSConstants.EXTENDED) {
			if (ui.haveTimestamps())
				modificationStrategy = new TSStrategyExtendTime(log, ts,
						genFlags, typeOfTS);
			else
				modificationStrategy = new TSStrategyExtendNoTime(log, ts,
						genFlags, typeOfTS);
			ts = modificationStrategy.getTransitionSystem();
		}
		// ========== Execute Modification Strategy ========== FINISH

		GregorianCalendar endCal = new GregorianCalendar();
		Date endTime = endCal.getTime();
		long executionTime = endTime.getTime() - startTime.getTime();
		Message.add("======================================");
		Message.add("Transition System:");
		Message.add("Number of States: " + ts.getVerticeList().size());
		Message.add("Number of Transitions: " + ts.getEdges().size());
		Message.add("Execution time: " + executionTime + " milliseconds");
		Message.add("======================================");
		if (UISettings.getInstance().getTest() == true)
			this.outputForTests(log, ts, executionTime);
		ModelGraphResult result = new ModelGraphResult(ts);
		return result;
	}

	void outputForTests(LogReader log, TransitionSystem ts, long time) {
		Message.add("<TransitionSystemGenerator file=\""
				+ log.getFile().getShortName() + "\">", Message.TEST);
		Message.add("<NumberOfStates = \"" + ts.getVerticeList().size()
				+ "\"/>", Message.TEST);
		Message.add(
				"<NumberOfTransitions = \"" + ts.getEdges().size() + "\"/>",
				Message.TEST);
		// Message.add("<ExecutionTime = \"" + time + "\"/>", Message.TEST);
		Message.add("<Finished = \"Successfully!!!\"/>", Message.TEST);
		Message.add("</TransitionSystemGenerator>", Message.TEST);
	}
}
