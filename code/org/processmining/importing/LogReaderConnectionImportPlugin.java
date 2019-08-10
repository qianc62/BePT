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

package org.processmining.importing;

import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.ui.ComboBoxLogEvent;
import org.processmining.mining.MiningResult;

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
public interface LogReaderConnectionImportPlugin extends ImportPlugin {

	public static final LogEvent MAKE_VISIBLE = new LogEvent(
			ComboBoxLogEvent.VISIBLE, ComboBoxLogEvent.VISIBLE);
	public static final LogEvent MAKE_INVISIBLE = new LogEvent(
			ComboBoxLogEvent.INVISIBLE, ComboBoxLogEvent.INVISIBLE);

	/**
	 * Interface for plugins that import from file. This MiningResult
	 * <i>MUST</i> implement the <code>LogReaderConnection</code> interface.
	 * 
	 * @param input
	 *            The framework will always provide a ProMInputStream as the
	 *            <code>input</code> parameter. To retrieve the filename use:
	 *            <code>((ProMInputStream) input).getFilename();</code>
	 * @return MiningResult A JComponent that is visualized in a frame. If
	 *         <code>null</code> is returned then the framework thinks the call
	 *         to this method was aborted. This MiningResult <i>MUST</i>
	 *         implement the <code>LogReaderConnection</code> interface.
	 * @throws IOException
	 *             If an IO exception occurs.
	 */
	public MiningResult importFile(InputStream input) throws IOException;

	/**
	 * Tells the log reader connection to find matches to events in the log
	 * based on string labels. If false, no matching is done, except if the
	 * imported model elements (e.g. transitions, EPCfunctions) have explicit
	 * links to log events. If true, matching is done based on string distance
	 * if there is no explicit link.
	 * 
	 * @return all connectable objects of the underlying model
	 */
	public boolean shouldFindFuzzyMatch();

}
