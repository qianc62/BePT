package org.processmining.framework.models.logabstraction;

import java.util.List;

import org.processmining.analysis.log.scale.ProcessInstanceScale;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

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
public interface LogAbstractionFactory {

	public List<LogAbstraction> getAbstractions(LogReader log,
			ProcessInstanceScale scale);

	public LogAbstraction getAbstraction(LogReader log, ProcessInstance pi,
			ProcessInstanceScale scale);

}
