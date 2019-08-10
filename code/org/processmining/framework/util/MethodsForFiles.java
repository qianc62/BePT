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

package org.processmining.framework.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class MethodsForFiles {

	public static String[] listOfFilesToMine(String directory, String endsWith)
			throws IOException {

		String[] filesToMine = null;
		Vector vector = null;
		String[] potentialFilesToMine = null;

		directory = directory.trim();
		potentialFilesToMine = new File(directory).list();
		vector = new Vector();
		for (int i = 0; i < potentialFilesToMine.length; i++) {
			if (potentialFilesToMine[i].endsWith(endsWith)) {
				filesToMine = extractFiles(directory + File.separator
						+ potentialFilesToMine[i]);
				for (int j = 0; j < filesToMine.length; j++) {
					vector.add(filesToMine[j]);
				}
			}
		}

		filesToMine = new String[vector.size()];
		vector.copyInto(filesToMine);

		return filesToMine;

	}

	public static String[] extractFiles(String file) throws IOException {
		Vector vector = new Vector();
		String[] files = null;
		String zipName = null;
		ZipFile zipFile = null;
		ZipEntry zipEntry = null;
		Enumeration enu = null;

		if (file.toLowerCase().endsWith(".zip")) {
			zipFile = new ZipFile(file);
			// get all of the zip entries
			enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				zipEntry = (ZipEntry) enu.nextElement();
				zipName = "zip://" + file + "#" + zipEntry.getName();
				vector.add(zipName);
			}

		} else {
			vector.add(file);
		}

		files = new String[vector.size()];
		vector.copyInto(files);

		return files;
	}

	/**
	 * Returns all the subdirectories of a given directory. The directory given
	 * as parameter is also included in the returned HashSet.
	 * 
	 * @param file
	 *            directory to look for subdirectories.
	 * @return a HashSet of Files. Every string is a subdirectory. The directory
	 *         given as input is also included in the returned HashSet.
	 */
	public static HashSet getAllSubDirectories(File file) {

		String fullPathFile = null;
		File listedFile = null;
		String[] f = file.list();
		HashSet dir = new HashSet();

		dir.add(file);
		if (f != null) {
			for (int i = 0; i < f.length; i++) {
				fullPathFile = file.getPath() + File.separator + f[i];
				listedFile = new File(fullPathFile);
				if (listedFile.isDirectory()) {
					dir.add(listedFile);
					dir.addAll(getAllSubDirectories(new File(fullPathFile)));
				}
			}
		}

		return dir;
	}

	/**
	 * Returns all the subdirectories of a given directory. The directory given
	 * as parameter is also included if it ends in "dirTermination".
	 * 
	 * @param dir
	 *            directory to look for subdirectories.
	 * @param dirSuffix
	 *            suffix name of the directory to list
	 * @return a HashSet of Files. Every string is a subdirectory.
	 */
	public static HashSet getAllSubDirectoriesInHash(File dir,
			String dirTermination) {

		String[] f = dir.list();

		HashSet dirHash = new HashSet();

		if (f != null) {

			for (int i = 0; i < f.length; i++) {
				String fullPathFile = dir.getPath() + File.separator + f[i];
				File listedFile = new File(fullPathFile);
				if (listedFile.isDirectory()) {
					if (fullPathFile.endsWith(dirTermination)) {
						dirHash.add(listedFile);
					}
					dirHash.addAll(getAllSubDirectoriesInHash(listedFile,
							dirTermination));
				}
			}
		}
		return dirHash;
	}

	/**
	 * Returns all the subdirectories of a given directory. The directory given
	 * as parameter is also included if it ends in "dirTermination".
	 * 
	 * @param dir
	 *            directory to look for subdirectories.
	 * @param dirSuffix
	 *            suffix name of the directory to list
	 * @return an array of Files. Every string is a subdirectory.
	 */
	public static File[] getAllSubDirectories(File dir, String dirTermination) {

		HashSet files = getAllSubDirectoriesInHash(dir, dirTermination);

		File[] foundDirectories = new File[files.size()];
		Iterator it = files.iterator();
		for (int i = 0; i < foundDirectories.length; i++) {
			foundDirectories[i] = (File) it.next();

		}

		return foundDirectories;
	}

	/**
	 * Returns all the files of a given directory and its subdirectories. The
	 * search is recursive.
	 * 
	 * @param file
	 *            directory to look for files.
	 * @return a HashSet of Files. Every string is a file.
	 */
	public static HashSet getAllSubFiles(File file) {

		String fullPathFile = null;
		File listedFile = null;
		String[] f = file.list();
		HashSet files = new HashSet();

		if (f != null) {
			for (int i = 0; i < f.length; i++) {
				fullPathFile = file.getPath() + File.separator + f[i];
				listedFile = new File(fullPathFile);
				if (!listedFile.isDirectory()) {
					files.add(listedFile);
				} else {
					files.addAll(getAllSubFiles(new File(fullPathFile)));
				}
			}
		}

		return files;
	}

	/**
	 * Returns all the files of a given directory and its subdirectories. The
	 * search is recursive.
	 * 
	 * @param file
	 *            directory to look for files.
	 * @param fileTermination
	 *            String the file termination of the retrieved files. This
	 *            termination is case sensitive.
	 * @return a HashSet of Files. Every string is a file.
	 */
	public static HashSet getAllSubFilesInHash(File file, String fileTermination) {

		String[] f = file.list();
		HashSet files = new HashSet();

		if (f != null) {
			for (int i = 0; i < f.length; i++) {
				String fullPathFile = file.getPath() + File.separator + f[i];
				File listedFile = new File(fullPathFile);
				if (!listedFile.isDirectory()) {
					if (listedFile.getAbsolutePath().endsWith(fileTermination)) {
						files.add(listedFile);
					}
				} else {
					files.addAll(getAllSubFilesInHash(listedFile,
							fileTermination));
				}
			}
		}

		return files;
	}

	/**
	 * Returns all the files of a given directory and its subdirectories. The
	 * search is recursive.
	 * 
	 * @param file
	 *            directory to look for files.
	 * @param fileTermination
	 *            String the file termination of the retrieved files. This
	 *            termination is case sensitive.
	 * @return File[] array of Files. Every string is a file.
	 */

	public static File[] getAllSubFiles(File file, String fileTermination) {

		HashSet files = getAllSubFilesInHash(file, fileTermination);

		File[] foundFiles = new File[files.size()];
		Iterator it = files.iterator();
		for (int i = 0; i < foundFiles.length; i++) {
			foundFiles[i] = (File) it.next();

		}

		return foundFiles;
	}

	/**
	 * Returns all the files of a given directory that have a given termination.
	 * 
	 * @param file
	 *            directory to look for files.
	 * @param fileTermination
	 *            String the file termination of the retrieved files. This
	 *            termination is case sensitive.
	 * @return File[] array of Files. Every string is a file.
	 */

	public static File[] getSubFiles(File file, String fileTermination) {

		HashSet files = getSubFilesInHash(file, fileTermination);

		File[] foundFiles = new File[files.size()];
		Iterator it = files.iterator();
		for (int i = 0; i < foundFiles.length; i++) {
			foundFiles[i] = (File) it.next();

		}

		return foundFiles;
	}

	/**
	 * Returns all the files of a given directory that end with a given
	 * termination.
	 * 
	 * @param file
	 *            directory to look for files.
	 * @param fileTermination
	 *            String the file termination of the retrieved files. This
	 *            termination is case sensitive.
	 * @return a HashSet of Files. Every string is a file.
	 */
	public static HashSet getSubFilesInHash(File file, String fileTermination) {

		String[] f = file.list();
		HashSet files = new HashSet();

		if (f != null) {
			for (int i = 0; i < f.length; i++) {
				String fullPathFile = file.getPath() + File.separator + f[i];
				File listedFile = new File(fullPathFile);
				if (!listedFile.isDirectory()) {
					if (listedFile.getAbsolutePath().endsWith(fileTermination)) {
						files.add(listedFile);
					}
				}
			}
		}

		return files;
	}

	/**
	 * This methods lists the direct subdirectories of a given directory. No
	 * recursion is done. Only the first-level sudirectories are reported.
	 * 
	 * @param inputDirectory
	 *            directory to search for subdirectories.
	 * @return File[] array contains the found subdirectories.
	 */
	public static File[] getDirectories(File inputDirectory) {
		File[] foundDirectories = new File[0];
		if (inputDirectory.isDirectory()) {
			// getting all files in directory
			File[] tempFoundDirectories = inputDirectory.listFiles();

			// checking how many are actual directories
			int numActualDirectories = 0;
			for (int i = 0; i < tempFoundDirectories.length; i++) {
				if (tempFoundDirectories[i].isDirectory()) {
					numActualDirectories++;
				} else {
					tempFoundDirectories[i] = null;
				}
			}
			// copying the directories to "foundDirectories"
			foundDirectories = new File[numActualDirectories];
			for (int i = 0, j = 0; i < tempFoundDirectories.length; i++) {
				if (tempFoundDirectories[i] != null) {
					foundDirectories[j++] = tempFoundDirectories[i];
				}
			}

		}

		return foundDirectories;

	}

}
