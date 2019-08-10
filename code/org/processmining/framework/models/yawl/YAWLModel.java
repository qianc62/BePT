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

package org.processmining.framework.models.yawl;

import java.io.*;
import java.util.*;

import org.processmining.framework.models.*;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: YAWL Model
 * </p>
 * <p>
 * Description: Holds an imported YAWL model
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */

public class YAWLModel extends ModelGraph {

	private String uri; // The uri of the YAWL model
	private HashMap<String, YAWLDecomposition> decompositions = new HashMap(); // All

	// decompositions
	// of
	// the
	// YAWL
	// model

	/**
	 * Create a new YAWL mode, given its uri.
	 * 
	 * @param uri
	 *            The given uri.
	 */
	public YAWLModel(String uri) {
		super("YAWL model");
		init(uri);
	}

	public YAWLModel(YAWLModel model) {
		super("YAWL model");
		init(model.uri);
	}

	private void init(String uri) {
		this.uri = uri.replaceAll(" ", "."); // spaces are not allowed in uri's.
	}

	/**
	 * Adds a given decomposition with a given name to the YAWL model.
	 * 
	 * @param id
	 *            The given name
	 * @param decomposition
	 *            THe given decomposition
	 */
	public void addDecomposition(String id, YAWLDecomposition decomposition) {
		decompositions.put(id, decomposition);
	}

	public Collection<YAWLDecomposition> getDecompositions() {
		return decompositions.values();
	}

	public YAWLDecomposition getDecomposition(String id) {
		return decompositions.get(id);
	}

	/**
	 * Writes the model to dot. For the time being, the first decompositionis
	 * written.
	 * 
	 * @param bw
	 *            The writer
	 * @throws IOException
	 *             If writing fails
	 */
	public void writeToDot(Writer bw) throws IOException {
		Iterator it = decompositions.values().iterator();
		YAWLDecomposition decomposition;
		while (it.hasNext()) {
			decomposition = (YAWLDecomposition) it.next();
			if (decomposition.isRoot()) {
				decomposition.writeToDot(bw, this);
				return;
			}
		}
	}

	/**
	 * Return whether the given name corresponds to a non-empty decomposition
	 * 
	 * @param name
	 *            The given name
	 * @return Whether this name corresponds to a non-empty decomposition
	 */
	public boolean isComposite(String name) {
		YAWLDecomposition decomposition = decompositions.get(name);
		if (decomposition == null) {
			return false;
		}
		return !decomposition.getVerticeList().isEmpty();
	}

	/**
	 * Export to YAWL file.
	 * 
	 * @param bw
	 *            Writer
	 * @return String The string to export for this YAWLDecompositon.
	 */
	public String writeToYAWL(Writer bw) {
		String s = "";
		s += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		s += "<specificationSet\n";
		s += "\txmlns=\"http://www.citi.qut.edu.au/yawl\"\n";
		s += "\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
		s += "\tversion=\"Beta 4\"\n";
		s += "\txsi:schemaLocation=\"http://www.citi.qut.edu.au/yawl d:/yawl/schema/YAWL_SchemaBeta4.xsd\"\n";
		s += ">\n";
		s += "\t<specification uri=\"" + uri + "\">\n";
		s += "\t\t<name>" + uri + "</name>\n";
		s += "\t\t<documentation>" + uri + "</documentation>\n";
		s += "\t\t<metaData/>\n";
		for (YAWLDecomposition decomposition : decompositions.values()) {
			s += decomposition.writeToYAWL();
		}
		s += "\t</specification>\n";
		s += "</specificationSet>\n";

		return s;
	}

	/**
	 * Print key indicators of the YAWL model to the Test tab.
	 * 
	 * @param tag
	 *            String The tag to use for the indicators.
	 */
	public void Test(String tag) {
		Message.add("<" + tag + ">", Message.TEST);
		for (YAWLDecomposition decomposition : getDecompositions()) {
			decomposition.Test();
		}
		Message.add("</" + tag + ">", Message.TEST);
	}

}
