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

package org.processmining.framework.models.epcpack;

import java.io.*;

import org.processmining.framework.log.*;
import java.util.HashMap;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
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
 * @author not attributable
 * @version 1.0
 */
public class EPCSubstFunction extends EPCFunction {

	private ConfigurableEPC substitutedEPC = null;

	public EPCSubstFunction(LogEvent logModelElement, ConfigurableEPC ownerEpc,
			ConfigurableEPC substitutedEPC) {
		super(logModelElement, ownerEpc);
		this.substitutedEPC = substitutedEPC;
	}

	public EPCSubstFunction(EPCFunction f, ConfigurableEPC substitutedEPC) {
		super(f);
		this.substitutedEPC = substitutedEPC;
	}

	public EPCSubstFunction(LogEvent logModelElement, boolean configurable,
			ConfigurableEPC epc, ConfigurableEPC substitutedEPC) {
		super(logModelElement, configurable, epc);
		this.substitutedEPC = substitutedEPC;
	}

	/**
	 * Sets the EPC this function substitutes
	 * 
	 * @param newSubstEPC
	 *            The new EPC this fuction substitutes
	 * @return ConfigurableEPC the old EPC that this function substituted
	 */
	public ConfigurableEPC setSubstitutedEPC(ConfigurableEPC newSubstEPC) {
		ConfigurableEPC epc = substitutedEPC;
		substitutedEPC = newSubstEPC;
		return epc;
	}

	/**
	 * Returns the EPC this function substitutes
	 * 
	 * @return ConfigurableEPC
	 */
	public ConfigurableEPC getSubstitutedEPC() {
		return substitutedEPC;
	}

	public void writeDOTCode(Writer bw, HashMap nodeMapping) throws IOException {
		bw
				.write("node"
						+ getId()
						+ " [shapefile=\"att.grappa.epc.EPCSubstFunctionShape\",shape =\"custom\",style=\"filled"
						+ (isConfigurable() ? ",bold" : "")
						+ "\",fillcolor=\"palegreen1\",label=\"");
		bw.write(getIdentifier());
		bw.write("\"];\n");
		nodeMapping.put(new String("node" + getId()), this);
		writeAdditionalObjectsDOTCode(bw, nodeMapping);
	}

}
