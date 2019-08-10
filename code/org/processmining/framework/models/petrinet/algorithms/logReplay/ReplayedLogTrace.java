/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.petrinet.algorithms.logReplay;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

/**
 * A log trace involved in the log replay analysis method. Internally, the
 * original process instance is referenced, but in addition to that diagnostic
 * data may be stored in deriving subclasses.
 * 
 * @see ReplayedLogReader
 * @see ReplayedPetriNet
 * @see LogReplayAnalysisMethod
 * 
 * @author arozinat
 */
public class ReplayedLogTrace {

	/**
	 * Keeps a link to the corresponding process instance. Note that this does
	 * not necessarily mean that the instance is held in memory as the new log
	 * reader manages its instances via random access files.
	 */
	protected ProcessInstance processInstance;

	/**
	 * Constructor.
	 * 
	 * @param the
	 *            process instance related to this trace
	 */
	public ReplayedLogTrace(ProcessInstance pi) {
		processInstance = pi;
	}

	/**
	 * Returns the process instance related to this trace.
	 * 
	 * @return the process instance related to this trace
	 */
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	/**
	 * Retrieves the name of the wrapped process instance. This is a convenience
	 * method as the name of the process instance is used at many places (and
	 * otherwise {@link #getProcessInstance()} must be called before every
	 * time).
	 * 
	 * @return the name of the wrapped instance
	 */
	public String getName() {
		return processInstance.getName();
	}

	/**
	 * Returns the number of similar process instances represented logically by
	 * this diagnostic log trace. This needs to be taken into account for
	 * calculating the conformance analysis measures.
	 * 
	 * @return the number of represented instances
	 */
	public int getNumberOfProcessInstances() {
		return MethodsForWorkflowLogDataStructures
				.getNumberSimilarProcessInstances(processInstance);
	}
}
