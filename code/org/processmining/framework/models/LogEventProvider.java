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

package org.processmining.framework.models;

import org.processmining.framework.log.LogEvent;

/**
 * <p>
 * Title: LogEventProvider
 * </p>
 * 
 * <p>
 * Description: This interface can be used by import plugins to see whether
 * objects can connect with a LogEvent, and thus to a specific log by remapping
 * these LogEvents
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
public interface LogEventProvider {

	/**
	 * This method returns the current LogEvent the LogEventProvider has stored
	 * 
	 * @return LogEvent The stored LogEvent
	 */
	public LogEvent getLogEvent();

	/**
	 * This method sets the LogEvent for the LogEventProvider to store
	 * 
	 * @param le
	 *            LogEvent The LogEvent to Store
	 */
	public void setLogEvent(LogEvent le);

}
