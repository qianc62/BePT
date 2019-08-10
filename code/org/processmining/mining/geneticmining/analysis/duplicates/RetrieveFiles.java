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

import org.processmining.framework.util.MethodsForFiles;

/**
 * <p>
 * This class retrieves all the files that have a certain termination in a
 * directory and, if required, all of its subdirectories.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class RetrieveFiles {

	private File[] files;
	private String[] fileNames;

	public RetrieveFiles(String rootDirFiles, String fileTermination,
			boolean includeSubdirectories) throws FileNotFoundException {

		// checking if the directories exist...
		File root = new File(rootDirFiles);

		RetrieveFiles.checkIfDirectoryExists(root);

		retrieveAllFileInDirectory(root, fileTermination, includeSubdirectories);
	}

	private void retrieveAllFileInDirectory(File rootDirFiles,
			String fileTermination, boolean includeSubdirectories) {
		if (includeSubdirectories) {
			files = MethodsForFiles.getAllSubFiles(rootDirFiles,
					fileTermination);
		} else {
			files = MethodsForFiles.getSubFiles(rootDirFiles, fileTermination);
		}

		fileNames = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			fileNames[i] = files[i].getName().substring(0,
					files[i].getName().lastIndexOf(fileTermination));
		}

	}

	/**
	 * Lists all the found files.
	 */
	public File[] getFiles() {
		return files;
	}

	/**
	 * List the names of the found files. The names exclude the file termination
	 * that was provided for the constructor method.
	 */
	public String[] getNames() {
		return fileNames;
	}

	/**
	 * Checks if a given directory exists.
	 * 
	 * @throws FileNotFoundException
	 *             if the provided "directory" does not exists or is not a
	 *             directory.
	 */
	public static void checkIfDirectoryExists(File directory)
			throws FileNotFoundException {

		if (!directory.exists() || !directory.isDirectory()) {
			throw new FileNotFoundException("The directory \"" + directory
					+ "\" does not exist!");
		}

	}

	public static void main(String args[]) throws FileNotFoundException {

		RetrieveFiles rf = new RetrieveFiles(
				"C:\\AKThesisExperiments\\DGAnoiseFree\\setup\\desiredNets",
				".hn", true);

		for (int i = 0; i < rf.getFiles().length; i++) {
			System.out.println(rf.getNames()[i] + " => " + rf.getFiles()[i]);
		}

	}

}
