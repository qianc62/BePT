package org.processmining.analysis.causality;

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

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.causality.CausalFootprint;
import org.processmining.framework.models.causality.CausalityFootprintFactory;
import org.processmining.framework.plugin.ProvidedObject;

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
public class CausalityStructureAnalysisPlugin implements AnalysisPlugin {
	public CausalityStructureAnalysisPlugin() {
	}

	public String getName() {
		return "Causal footprint analyzer";
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Causal footprint") {
			public boolean accepts(ProvidedObject object) {
				int i = 0;
				boolean b = false;
				while (!b && (i < object.getObjects().length)) {
					b |= (object.getObjects()[i] instanceof CausalFootprint);
					b |= CausalityFootprintFactory.canConvert(object
							.getObjects()[i]);
					i++;
				}
				return b;
			}
		} };
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		int i = 0;
		boolean b = false;
		while (!b && (i < o.length)) {
			b |= (o[i] instanceof CausalFootprint);
			b |= CausalityFootprintFactory.canConvert(o[i]);
			i++;
		}

		CausalFootprint cs;

		Object o2 = o[i - 1];
		if (!(o2 instanceof CausalFootprint)) {
			cs = CausalityFootprintFactory.make(o2);
			if (cs != null) {
				cs.Test("DerivedFootprint");
				return new CausalFootprintAnalysisResult(cs, ((ModelGraph) o2),
						(inputs[0].getProvidedObjects())[0]);
			} else {
				return null;
			}
		} else {
			cs = (CausalFootprint) o2;
			cs.Test("ReceivedFootprint");
			return new CausalFootprintAnalysisResult(cs, null, null);
		}

	}

	public String getHtmlDescription() {
		return "Analyses a causality structure for one of the erroneous patterns."
				+ "It accepts EPCs, Petri nets and causality structures.";
	}
}
