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

package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;

import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.att.HLBooleanAttribute;
import org.processmining.framework.models.hlprocess.att.HLBooleanDistribution;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.att.HLNominalDistribution;
import org.processmining.framework.models.hlprocess.att.HLNumericAttribute;

/**
 * This class represents the declaration of a function that can be used in cpn
 * for generating random values for some data attribute. In the case that the
 * data attribute is of a nominal type a ran() function is returned without a
 * function body
 * 
 * @author rmans
 * @author arozinat
 */
public class CpnFunction {

	/**
	 * The name of the function.
	 */
	private String myFunctionName = "";

	/**
	 * the body of the function
	 */
	private String myFunctionBody = "";

	/**
	 * Default constructor
	 * 
	 * @param dataAttr
	 *            HLAttribute The data attribute for which we want to obtain a
	 *            random function.
	 */
	public CpnFunction(HLAttribute dataAttr) {
		myFunctionName = "random"
				+ CpnUtils.getCpnValidName(dataAttr.getName()) + "()";
		if (dataAttr instanceof HLNominalAttribute) {
			myFunctionBody = CpnUtils.getCpnDistributionFunction(
					(HLNominalDistribution) ((HLNominalAttribute) dataAttr)
							.getPossibleValues(), dataAttr.getName());
		} else if (dataAttr instanceof HLNumericAttribute) {
			myFunctionBody = CpnUtils
					.getCpnDistributionFunction(((HLNumericAttribute) dataAttr)
							.getPossibleValuesNumeric());
		} else if (dataAttr instanceof HLBooleanAttribute) {
			myFunctionBody = CpnUtils.getCpnDistributionFunction(
					(HLBooleanDistribution) ((HLBooleanAttribute) dataAttr)
							.getPossibleValues(), dataAttr.getName());
		}
	}

	/**
	 * Generates a cpn function based on a name and a body
	 * 
	 * @param name
	 *            String the name
	 * @param body
	 *            String the body
	 */
	public CpnFunction(String name, String body) {
		myFunctionName = name;
		myFunctionBody = body;
	}

	/**
	 * Retrieves the name of the function
	 * 
	 * @return String the name
	 */
	public String getFunctionName() {
		return myFunctionName;
	}

	/**
	 * Retrieves the body of this function
	 * 
	 * @return String the body
	 */
	public String getFunctionBody() {
		return myFunctionBody;
	}

	/**
	 * Writes this function to the cpn-file (declarations part).
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @throws IOException
	 */
	public void write(BufferedWriter bw) throws IOException {
		// TODO: use xml strip functions from mxml lib instead
		if (myFunctionBody.contains("<")) {
			myFunctionBody = myFunctionBody.replace("<", "&lt;");
		}
		if (myFunctionBody.contains(">")) {
			myFunctionBody = myFunctionBody.replace(">", "&gt;");
		}
		bw.write("\t\t\t\t\t<ml id=\"" + ManagerID.getNewID() + "\">" + "fun "
				+ myFunctionName + " = " + myFunctionBody + ";" + "\n"
				+ "\t\t\t\t\t\t<layout>" + "fun " + myFunctionName + " = "
				+ myFunctionBody + ";" + "</layout>\n" + "\t\t\t\t\t</ml>");
	}

	/**
	 * Returns the complete declaration of the function (cpn-syntax) Only
	 * usefull in the case that the data attribute is of a numeric type
	 * 
	 * @return String
	 * @deprecated
	 */
	public String toString() {
		return "fun " + myFunctionName + " = " + myFunctionBody + ";";
	}
}
