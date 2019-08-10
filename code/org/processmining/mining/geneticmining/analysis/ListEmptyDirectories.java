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

package org.processmining.mining.geneticmining.analysis;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.util.MethodsForFiles;
import org.processmining.framework.util.Parameters;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: This class lists all empty subdirectories in a directory.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class ListEmptyDirectories {

	public static void listEmptyDir(String root) {
		HashSet subDirs = null;
		Iterator iterator = null;
		File f = null;
		int total = 0;

		subDirs = MethodsForFiles.getAllSubDirectories(new File(root));

		System.out.println("Root = " + root);
		iterator = subDirs.iterator();
		System.out.println("The empty directories are:");
		while (iterator.hasNext()) {
			f = (File) iterator.next();
			if (f.isDirectory()) {
				if (f.list().length == 0) {
					System.out.println(f.getAbsolutePath());
					total++;
				}
			}
		}
		System.out.println("TOTAL = " + total);
	}

	public static void main(String[] args) {
		Parameters param = null;

		param = new Parameters(args[0]);
		listEmptyDir(param.getParameter("DIR_NETS_TO_COMPARE"));
	}

}