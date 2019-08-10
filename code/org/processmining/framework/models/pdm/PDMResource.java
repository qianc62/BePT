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

package org.processmining.framework.models.pdm;

import java.io.*;
import javax.xml.parsers.*;
import org.processmining.framework.models.pdm.*;
import org.w3c.dom.*;

/**
 * <p>
 * Title: PDM Resource
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */
public class PDMResource {

	private String resourceID; // the ID of the resource

	/**
	 * Creates the resource with identifier 'id'
	 * 
	 * @param id
	 *            String
	 */
	public PDMResource(String id) {
		this.resourceID = id;
	}

	/**
	 * Returns the identifier of the resource.
	 * 
	 * @return String
	 */
	public String getID() {
		return this.resourceID;
	}

	/**
	 * Writes a Resource node to the PDM file
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 */
	public void writeToPDM(Writer bw) throws IOException {
		bw.write("\t<Resource\n");
		bw.write("\t\t\tResourceID=\"" + resourceID + "\"\n");
		bw.write("\t\t>\n");
	}

}
