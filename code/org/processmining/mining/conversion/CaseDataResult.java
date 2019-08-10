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

package org.processmining.mining.conversion;

import javax.swing.JComponent;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class CaseDataResult implements MiningResult, Provider {
	private CaseDataExtractorOptions theResult;
	private LogReader theLog;

	public CaseDataResult(CaseDataExtractorOptions theResult, LogReader log) {
		this.theResult = theResult;
		theLog = log;
	}

	public JComponent getVisualization() {
		return theResult;
	}

	public LogReader getLogReader() {
		return theLog;
	}

	public ProvidedObject[] getProvidedObjects() {
		return (theResult == null ? new ProvidedObject[0]
				: new ProvidedObject[] { new ProvidedObject("Case data",
						new Object[] { theLog, theResult }) });
	}
}