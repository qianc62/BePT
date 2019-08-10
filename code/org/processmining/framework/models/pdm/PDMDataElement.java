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
import java.util.*;
import javax.xml.parsers.*;
import org.processmining.framework.models.*;
import org.w3c.dom.*;

/**
 * <p>
 * Title: PDM Data Element
 * </p>
 * <p>
 * Description: An object that represents a Data Element in a Product Data Model
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

public class PDMDataElement extends ModelGraphVertex {

	private String dataElementID; // the ID of the data element
	private Integer dataElementNR; // the number of the data element, needed for
	// Declare
	private String description; // the description of the data element
	private Object Type; // the type of the data element
	private Object initial; // the initial value of the data element

	/**
	 * Creates a Data Element with identifier "id"
	 * 
	 * @param id
	 *            String
	 */
	public PDMDataElement(PDMModel model, String id) {
		super(model);
		this.dataElementID = id;
		this.description = null;
		setDataElementNR(dataElementID.hashCode());
	}

	/**
	 * Creates a data element with identifier "id" and description "descr"
	 * within the PDM model "model"
	 * 
	 * @param model
	 *            PDMModel
	 * @param id
	 *            String
	 * @param descr
	 *            String
	 */
	public PDMDataElement(PDMModel model, String id, String descr) {
		super(model);
		this.dataElementID = id;
		this.description = descr;
		setDataElementNR(dataElementID.hashCode());
	}

	/**
	 * Creates a data element with identifier "id" within a PDMPetriNet "m"
	 * 
	 * @param m
	 *            PDMPetriNet
	 * @param id
	 *            String
	 */
	public PDMDataElement(PDMPetriNet m, String id) {
		super(m);
		this.dataElementID = id;
		setDataElementNR(dataElementID.hashCode());
	}

	/**
	 * Sets the integer number for the data element based on the ID. To be able
	 * to generate a Declare model from the PDM, we need a numrical reference to
	 * each data element instead of the string identifier. This numerical
	 * reference is generated from the string id by using the hashCode() of the
	 * identifier
	 * 
	 * @param in
	 *            Integer
	 */
	public void setDataElementNR(Integer in) {
		this.dataElementNR = in;
	}

	/**
	 * Returns the numerical identifier of a data element. This numerical
	 * reference is generated from the string identifier bij using the
	 * hashCode() of the string identifier. A numerical reference is needed to
	 * be able to generate a Declare model from the PDM model
	 * 
	 * @return Integer
	 */
	public Integer getDataElementNR() {
		return dataElementNR;
	}

	/**
	 * Returns the identifier of the Data Element
	 * 
	 * @return String
	 */
	public String getID() {
		return dataElementID;
	}

	/**
	 * Returns true if the data element is one of the input or output elements
	 * of the operation. Returns false otherwise
	 * 
	 * @param operation
	 *            PDMOperation
	 * @return Boolean
	 */
	public Boolean intersectsWith(PDMOperation operation) {
		Boolean result = false;
		if (operation.input.containsKey(this.dataElementID)) {
			result = true;
		} else if (operation.output.containsKey(this.dataElementID)) {
			result = true;
		}
		return result;
	}

	/**
	 * Writes a Data Element node to the PDM file
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 */
	public void writeToPDM(Writer bw) throws IOException {
		bw.write("\t<DataElement\n");
		bw.write("\t\t\tDataElementID=\"" + dataElementID + "\"\n");
		bw.write("\t\t>\n");
	}

	/**
	 * Writes the PDM model to a Declare XML file such that the model can be
	 * used by the recommendation service and worklist of Declare. The Declare
	 * model contains all operations as activities with input data elements
	 * (type 0) and output data elements (type 1).
	 * 
	 * @param bw
	 *            Writer
	 * @param str
	 *            String
	 * @throws IOException
	 */
	public void writePDMToDeclare(Writer bw, String str) throws IOException {
		if (str == "input") {
			bw.write("<data element=\"" + dataElementNR + "\" type=\"0\"/>\n");
		} else if (str == "output") {
			bw.write("<data element=\"" + dataElementNR + "\" type=\"1\"/>\n");
		}
	}

	/**
	 * Writes the PDM model to a Declare XML file such taht the model can be
	 * used by the recommendation service ans worklist of Declare. This method
	 * writes the information of a data element: the numerical reference
	 * "dataElementNR", the initial value, the name "dataElementID" and the type
	 * of the data element.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 */
	public void writePDMToDeclare(Writer bw) throws IOException {
		bw.write("<dataelement id=\"" + dataElementNR
				+ "\" initial=\"\" name=\"" + dataElementID
				+ "\" type=\"string\"/>\n");
	}

	/**
	 * Writes a data element to DOT as a circle with the label 'dataElementID'.
	 * 
	 * @param bw
	 *            Writer
	 * @param model
	 *            PDMModel
	 * @throws IOException
	 */
	public void writeToDot(Writer bw, PDMModel model) throws IOException {
		if (!(description == null)) {
			bw.write(getID() + " [shape=circle, height=\".3\", label=\""
					+ dataElementID + "_" + description + "\"];\n");
		} else
			bw.write(getID() + " [shape=circle, height=\".3\", label=\""
					+ dataElementID + "\"];\n");
		// map.put(new String("n" + getId()), this);
	}
}
