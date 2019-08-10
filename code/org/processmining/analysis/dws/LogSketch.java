package org.processmining.analysis.dws;

import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

/**
 * The sketch of the log storing associations between traces Name and ID.
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 */

public class LogSketch {

	/**
	 * Key: Name of the instance; Value: ID of the instance
	 */
	public HashMap m = new HashMap();

	/**
	 * @param log
	 *            Log to be sketched.
	 */
	public LogSketch(LogReader log) {
		int i = 0;
		Iterator logInstanceIterator = log.instanceIterator();
		while (logInstanceIterator.hasNext()) {
			ProcessInstance pi = (ProcessInstance) logInstanceIterator.next();
			// usa il metodo di raggruppamento
			// int numSimilarPis =
			// MethodsForWorkflowLogDataStructures.getNumberSimilarProcessInstances(pi);
			m.put(pi.getName(), new Integer(i));
			i++;
		}
	}
}
