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

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * This importstream is used by the framework to load a file. The
 * <code>ImpotrPlugin.importFile(InputStream input)</code> is called with a
 * ProMInputStream. To get the filename, a type-cast is necessary
 * 
 * @author not attributable
 * @version 1.0
 */
public class ProMInputStream extends FileInputStream {

	private String filename;

	/**
	 * Construcs a ProMInpuStream and remembers the filename for later
	 * retrieval.
	 * 
	 * @param name
	 *            String
	 * @throws FileNotFoundException
	 */
	public ProMInputStream(String name) throws FileNotFoundException {
		super(name);
		this.filename = name;
	}

	/**
	 * return the Filename used to initiate this stream;
	 * 
	 * @return String
	 */
	public String getFileName() {
		return filename;
	}
}
