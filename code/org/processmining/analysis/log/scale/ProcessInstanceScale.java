/**
 *
 */
package org.processmining.analysis.log.scale;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.log.LogReader;

/**
 * <p>
 * This interface specifies a method by which process instances can be
 * 'weighed', i.e. assigned an absolute value of some kind. These weights should
 * infer a total order upon a set of process instances, based on e.g. their
 * runtime, number of events, etc.
 * <p>
 * Weights are supposed to be interpreted like "the bigger the better", i.e. a
 * process instance that has a higher "value" of some kind should also yield a
 * respectively high weight.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public interface ProcessInstanceScale extends Plugin {

	/**
	 * This method assigns a weight on some total scale to the process instance
	 * of the log identified by the id. The returned value must always be the
	 * same for identical process instances (from the same ProcessInstanceScale
	 * implementation).
	 * 
	 * @param instance
	 *            the process instance to be weighed, the process instance can
	 *            be retrieved by calling log.get(i).
	 * @return Weight assigned to this process instance by the respective scale
	 *         implementation. This value should always be <code>>= 0.0</code>.
	 *         <p>
	 *         Weights are supposed to be interpreted like
	 *         "the bigger the better", i.e. a process instance that has a
	 *         higher "value" of some kind should also yield a respectively high
	 *         weight.
	 */
	public double weigh(ProcessInstance instance);

	void initializeScale(LogReader log);

	void updateScale(ProcessInstance instance, LogReader log);

}
