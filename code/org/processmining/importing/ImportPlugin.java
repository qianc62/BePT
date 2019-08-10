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

import javax.swing.filechooser.FileFilter;

import org.processmining.framework.plugin.Plugin;
import org.processmining.mining.MiningResult;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public interface ImportPlugin extends Plugin {
	public FileFilter getFileFilter();

	/**
	 * Interface for plugins that import from file.
	 * 
	 * @param input
	 *            The framework will always provide a ProMInputStream as the
	 *            <code>input</code> parameter. To retrieve the filename use:
	 *            <code>((ProMInputStream) input).getFilename();</code>
	 * @return MiningResult A JComponent that is visualized in a frame. If
	 *         <code>null</code> is returned then the framework thinks the call
	 *         to this method was aborted.
	 * @throws IOException
	 *             If an IO exception occurs.
	 */
	public MiningResult importFile(InputStream input) throws IOException;
}
