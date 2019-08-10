package org.processmining.analysis.log.scale;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.LogReader;

public class MinimumCycleTimeScale implements ProcessInstanceScale {

	public static final String MIN_CYCLE_TIME = "minCycleTime";
	public static final String MAX_CYCLE_TIME = "maxCycleTime";
	public static final String CYCLE_TIME = "cycleTime";

	public static final String LAST_UPDATED_INSTANCE = "MinimumCycleTimeScale.lastupdatedtime";

	private int lastSeenInstance = -1;

	public void initializeScale(LogReader log) {
		for (int i = lastSeenInstance + 1; i < log.getInstances().size(); i++) {
			ProcessInstance pi = log.getInstance(i);
			updateScale(pi, log);
		}
		lastSeenInstance = log.getInstances().size() - 1;
	}

	public void updateScale(ProcessInstance pi, LogReader log) {
		// Nothing to do here, since there is no global information kept in the
		// scale
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.log.scale.ProcessInstanceScale#weigh(org.
	 * processmining.framework.log.LogReader, int)
	 */
	public double weigh(ProcessInstance instance) {
		if (instance.getAuditTrailEntryList().size() == 0) {
			return 0.0;
		}

		String cycleTimeString = null;
		cycleTimeString = instance.getDataAttributes().get(CYCLE_TIME);

		if (cycleTimeString == null) {
			// no weighing possible (first and last events have no timestamp)
			return 0.0;
		}

		double cycleTimeMinutes = Integer.parseInt(cycleTimeString);
		if (cycleTimeMinutes == 0) {
			return 0;
		}

		return cycleTimeMinutes;
	}

	public String getHtmlDescription() {
		return "Calculates the cycle time.";
	}

	public String getName() {
		return "Minimum Cycle Time";
	}

}
