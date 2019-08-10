package org.processmining.analysis.causality;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
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
public class CausalFootprintSimilarity implements AnalysisPlugin {
	public CausalFootprintSimilarity() {
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem(
				"Causal footprints", 2, 2) {
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

	public JComponent analyse(AnalysisInputItem[] analysisInputItemArray) {

		return new CausalFootprintSimilarityResult(analysisInputItemArray[0]
				.getProvidedObjects()[0], analysisInputItemArray[0]
				.getProvidedObjects()[1]);
	}

	public String getName() {
		return "Footprint Similarity";
	}

	public String getHtmlDescription() {
		return "Calculates the similarity of two footprints";
	}

}
