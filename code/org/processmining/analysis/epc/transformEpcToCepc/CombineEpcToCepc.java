package org.processmining.analysis.epc.transformEpcToCepc;

/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2008 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

import javax.swing.JComponent;

import org.processmining.analysis.Analyzer;
import org.processmining.framework.models.epcpack.ConfigurableEPC;

/**
 * <p>
 * Title: TransformEpcToCepc
 * </p>
 * 
 * <p>
 * Description: Analysis Plugin that combines two EPC process models into an EPC
 * process model.
 * </p>
 * 
 * @author Marijn Nagelkerke (mnagelkerke)
 * @version 1.0
 */
public class CombineEpcToCepc {

	@Analyzer(name = "Transform EPCs to a combined EPC", help = "<h1>Transform two EPC's into one EPC</h1><p>Combines the two specified EPC models into one new EPC. Focus in combining processes is on <ol><li>adding as little extra behavior as possible,</li><li>try to combine the models with low number of OR-connectors</li></ol>There is no attention on the complexity of the model (in terms of number of connectors, edges, etc)</p><br><p>Other documentation can be found in my Masters Thesis 'Combining Process Models into an Integrated Process Model'.</p>", names = {
			"EPC Model 1", "EPC Model 2" }, connected = false)
	public JComponent analyze(ConfigurableEPC epc1, ConfigurableEPC epc2) {
		return new CombineEpcToCepcResult(epc1, epc2);
	}
}
