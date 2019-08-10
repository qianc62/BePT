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

package org.processmining.framework.models.protos.algorithms;

import java.io.*;
import javax.xml.parsers.*;

import org.processmining.framework.models.*;
import org.processmining.framework.models.protos.*;

import org.w3c.dom.*;

/**
 * <p>
 * Title: Protos XML export reader
 * </p>
 * 
 * <p>
 * Description: Reads a Protos XML export and costructs a Protos model from it.
 * Tested with Prots 7.0
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class ProtosXMLExportReader {

	private ProtosXMLExportReader() {
	}

	/**
	 * Construct a Protos model out of a Protos XML Export stream
	 * 
	 * @param input
	 *            InputStream The Protos XML Export stream, that is, a file
	 *            generated bij Protos' XML Export functionality.
	 * @return ProtosModel The constructed Protos model. Contains everything
	 *         except the ProtosModelOptions (almost no use).
	 * @throws Exception
	 *             Throws an error message (one aerror message per line), if
	 *             errors were detected.
	 */
	public static ProtosModel read(InputStream input) throws Exception {
		ProtosModel protosModel = new ProtosModel();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;

		dbf.setValidating(false);
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);

		// Load the xml file as a tree structure
		doc = dbf.newDocumentBuilder().parse(input);

		String msg = protosModel.readXMLExport(doc);
		if (msg.length() > 0) {
			throw new Exception(msg);
		}

		return protosModel;
	}
}
