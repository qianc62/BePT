package org.processmining.framework.models.logabstraction;

import java.util.*;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.analysis.log.scale.ProcessInstanceScale;
import java.util.ArrayList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.AuditTrailEntry;

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
public class MultiSetAbstractionFactory implements LogAbstractionFactory {

	private boolean multiset;

	public MultiSetAbstractionFactory(boolean multiset) {
		this.multiset = multiset;
	}

	public List<LogAbstraction> getAbstractions(LogReader log,
			ProcessInstanceScale scale) {
		List<LogAbstraction> aggregatedLog = new ArrayList<LogAbstraction>();

		for (int instance = 0; instance < log.numberOfInstances(); instance++) {
			MultiSetAbstraction msa = (MultiSetAbstraction) getAbstraction(log,
					log.getInstance(instance), scale);
			// not necessary, done by constructor
			// msa.addInstance(instance);
			aggregatedLog.add(msa);
		}
		return aggregatedLog;
	}

	public LogAbstraction getAbstraction(LogReader log, ProcessInstance pi,
			ProcessInstanceScale scale) {
		return new MultiSetAbstraction(log, pi, scale, multiset);
	}
}
