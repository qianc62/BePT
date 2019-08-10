package org.processmining.framework.models.logabstraction;

import java.util.*;
import org.processmining.framework.log.LogReader;
import org.processmining.analysis.log.scale.ProcessInstanceScale;
import org.processmining.framework.log.ProcessInstance;
import java.util.ArrayList;

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
public class PrefixAbstractionFactory implements LogAbstractionFactory {
	public PrefixAbstractionFactory() {
		super();
	}

	public List<LogAbstraction> getAbstractions(LogReader log,
			ProcessInstanceScale scale) {
		List<LogAbstraction> list = new ArrayList<LogAbstraction>(log
				.getLogSummary().getNumberOfProcessInstances());
		Iterator it = log.instanceIterator();
		while (it.hasNext()) {
			list.add(new PrefixAbstraction(log, (ProcessInstance) it.next(),
					scale));
		}
		return list;
	}

	public LogAbstraction getAbstraction(LogReader log, ProcessInstance pi,
			ProcessInstanceScale scale) {
		return new PrefixAbstraction(log, pi, scale);
	}
}
