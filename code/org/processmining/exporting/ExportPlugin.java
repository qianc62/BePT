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

package org.processmining.exporting;

import java.io.IOException;
import java.io.OutputStream;

import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public interface ExportPlugin extends Plugin {
	public boolean accepts(ProvidedObject object);

	public String getFileExtension();

	public void export(ProvidedObject object, OutputStream output)
			throws IOException;
}
