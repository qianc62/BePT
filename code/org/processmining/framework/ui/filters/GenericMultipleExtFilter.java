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

public class GenericMultipleExtFilter extends
		javax.swing.filechooser.FileFilter {

	private String description;
	private String[] ext;

	public GenericMultipleExtFilter(String[] ext, String description) {
		this.ext = new String[ext.length];
		for (int i = 0; i < ext.length; i++) {
			if (ext[i].length() > 0 && ext[i].charAt(0) != '.') {
				ext[i] = "." + ext[i];
			}
			this.ext[i] = ext[i].toLowerCase();
		}
		this.description = description;
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String name = f.getName().toLowerCase();
		for (int i = 0; i < ext.length; i++) {
			if (name.endsWith(ext[i])) {
				return true;
			}
		}
		return false;
	}

	public String getDescription() {
		return description;
	}
}
