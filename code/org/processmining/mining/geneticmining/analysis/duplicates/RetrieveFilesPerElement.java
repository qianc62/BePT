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

package org.processmining.mining.geneticmining.analysis.duplicates;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * <p>
 * Identifies the files that have a given termination and groups them per
 * element. The startint point of this search is a root directory.
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */
public class RetrieveFilesPerElement {

	private File[][] files;
	private String[] elements;

	public RetrieveFilesPerElement(String[] elements, String rootDirFiles,
			String fileTermination, boolean includeSubDirectories)
			throws FileNotFoundException {

		// checking if the directories exist...
		File root = new File(rootDirFiles);

		RetrieveFiles.checkIfDirectoryExists(root);

		retrieveAllFilesForElementsInDirectory(elements, rootDirFiles,
				fileTermination, includeSubDirectories);
		this.elements = elements;
	}

	private void retrieveAllFilesForElementsInDirectory(String[] elements,
			String rootDir, String fileTermination,
			boolean includeSubDirectories) {

		files = new File[elements.length][];
		for (int i = 0; i < elements.length; i++) {
			try {
				files[i] = new RetrieveFiles(rootDir.trim() + File.separator
						+ elements[i].trim(), fileTermination,
						includeSubDirectories).getFiles();
			} catch (FileNotFoundException fne) {
				files[i] = new File[0];
			}
		}

	}

	/**
	 * Lists all the found files per element provided.
	 */
	public File[][] getFiles() {
		return files;
	}

	/**
	 * List the names of the provided elements.
	 */
	public String[] getElements() {
		return elements;
	}

}
