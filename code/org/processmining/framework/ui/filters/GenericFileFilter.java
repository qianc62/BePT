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

package org.processmining.framework.ui.filters;

import java.io.File;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class GenericFileFilter extends javax.swing.filechooser.FileFilter {

	private String description;
	private String ext;

	public GenericFileFilter(String ext) {
		if (ext.length() > 0 && ext.charAt(0) != '.') {
			ext = "." + ext;
		}
		this.ext = ext.toLowerCase();
		description = ext.toUpperCase().substring(1) + " files (*."
				+ ext.substring(1) + ")";
	}

	public GenericFileFilter(String ext, String description) {
		if (ext.length() > 0 && ext.charAt(0) != '.') {
			ext = "." + ext;
		}
		this.ext = ext.toLowerCase();
		this.description = description;
	}

	public String addExtension(String name) {
		int i = name.lastIndexOf('.');

		return i >= 0 && name.substring(i).toLowerCase().equals(ext) ? name
				: name + ext;
	}

	public boolean accept(File f) {
		String name = f.getName();
		int i = name.lastIndexOf('.');

		return f.isDirectory()
				|| (i >= 0 && name.substring(i).toLowerCase().equals(ext));
	}

	public String getDescription() {
		return description;
	}
}
