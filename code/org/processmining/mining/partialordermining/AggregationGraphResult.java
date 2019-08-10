package org.processmining.mining.partialordermining;

import org.processmining.mining.instancemining.ModelGraphResult;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;

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
public class AggregationGraphResult extends ModelGraphResult {

	private LogReader log;

	public AggregationGraphResult(LogReader log, ModelGraph net) {
		super(net);
		this.log = log;
	}

	public LogReader getLogReader() {
		return log;
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject(net.getIdentifier(),
				new Object[] { net, log }) };
	}

}
