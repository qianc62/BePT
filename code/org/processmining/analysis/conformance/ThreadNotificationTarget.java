package org.processmining.analysis.conformance;

import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;

/**
 * An object which has to be notified after the thread is done.
 * 
 * @author anne
 */
public interface ThreadNotificationTarget {

	/**
	 * Called by the specified thread after it has finished.
	 * 
	 * @param thread
	 *            the thread that just finished
	 */
	public void threadDone(AnalysisMethodExecutionThread thread);

	/**
	 * Retrieves the copy of the current analysis configuration in order to
	 * carry out the analysis based on the chosen options.
	 * 
	 * @return the current AnalysisConfiguration
	 */
	public AnalysisConfiguration getAnalysisConfiguration();
}
