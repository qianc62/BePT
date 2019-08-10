package org.processmining.analysis.logclustering.profiles;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;

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
public class OriginatorVectorProfile extends VectorProfile {
	public OriginatorVectorProfile(LogReader log) {
		super("Originator profile", "Profile for originators", log
				.getLogSummary().getNumberOfProcessInstances(), log
				.getLogSummary().getOriginators());
	}

	public void buildProfile(int traceIndex, AuditTrailEntry ate) {
		String originator = ate.getOriginator();
		if (originator != null && originator.length() > 0) {
			super.increaseItemBy(originator, traceIndex, 1.0);
		}
	}

}
