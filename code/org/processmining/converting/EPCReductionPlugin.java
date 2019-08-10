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

package org.processmining.converting;

import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.algorithms.ConnectorStructureExtractor;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;

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
 * @author not attributable
 * @version 1.0
 */

public class EPCReductionPlugin implements ConvertingPlugin {

	public String getName() {
		return "EPC reduction plugin";
	}

	public String getHtmlDescription() {
		return "<p> <b>Plug-in: EPC reduction</b>"
				+ "<p>This Plug-in is used by the EPC verification plugin. For more "
				+ "details, see "
				+ org.processmining.framework.util.Constants.get_BVD_URLString(
						"EPC_verification", "this paper")
				+ " for a description of the verification approach, which includes the reduction."
				+ "<p>An application of this plugin to a real life dataset can "
				+ "be found "
				+ org.processmining.framework.util.Constants.get_BVD_URLString(
						"SAP_reduction", "here");
	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof EPC);
			b |= (original.getObjects()[i] instanceof ConfigurableEPC);
			i++;
		}
		return b;
	}

	public MiningResult convert(ProvidedObject original) {
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof EPC);
			b |= (original.getObjects()[i] instanceof ConfigurableEPC);
			i++;
		}

		Object o = original.getObjects()[i - 1];
		ConfigurableEPC org;
		org = ((ConfigurableEPC) o);
		ConfigurableEPC reducedEPC = ConnectorStructureExtractor.extract(org);
		reducedEPC.Test("EPCreducedTo");
		return new EPCResult(null, reducedEPC);

	}

}
