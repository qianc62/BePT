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

package org.processmining.framework.models;

/**
 * This class should be used in the ModelHierarchies to support storing
 * directories.
 * 
 * @version 1.0
 */
public class ModelHierarchyDirectory {

	private String identifier;

	private String directory;

	/**
	 * Constructor.
	 * 
	 * @param identifier
	 *            String identifying the object
	 * @param directory
	 *            String the label of the object
	 */
	public ModelHierarchyDirectory(String identifier, String directory) {
		this.identifier = identifier;
		this.directory = directory;
	}

	/**
	 * Check for equality of the identifier, nothing else!
	 * 
	 * @param o
	 *            Object
	 * @return boolean
	 */
	public boolean equals(Object o) {
		if (o instanceof ModelHierarchyDirectory) {
			return identifier.equals(((ModelHierarchyDirectory) o).identifier);
		} else {
			return false;
		}
	}

	/**
	 * return the hashCode of the identifier, nothing else.
	 * 
	 * @return int
	 */
	public int hashCode() {
		return identifier.hashCode();
	}

	/**
	 * return the directory.
	 * 
	 * @return String
	 */
	public String toString() {
		return directory;

	}

	/**
	 * return the identifier.
	 * 
	 * @return String
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * return the directory.
	 * 
	 * @return String
	 */
	public String getDirectory() {
		return directory;
	}
}
