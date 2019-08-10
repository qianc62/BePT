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
package org.processmining.analysis.mdl;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * Precision and Structure metrics based on the Minimal Description Length
 * principle. Advocates a balance of the cost of encoding the model and encoding
 * the data with the model. <br>
 * Different encodings are possible in the case that the event log does not fit,
 * e.g., the given process model.
 * 
 * @author Anne Rozinat, Christian Guenther
 */
public class MDLAnalysisPlugin implements AnalysisPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {
		AnalysisInputItem PNLog = inputs[0];

		Object[] o = PNLog.getProvidedObjects()[0].getObjects();
		LogReader logReader = null;
		ModelGraph model = null;
		// since the plugin can only be invoked by the ProM tool, if the accepts
		// method
		// of its AnalysisInputItem (deliverable by getInputItems) evaluates to
		// true,
		// we can be sure that all the required arguments are passed over
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				logReader = (LogReader) o[i];
			} else if (o[i] instanceof PetriNet) {
				model = (ModelGraph) o[i];
			}
		}
		// the construction of the plugin`s graphical user interface has been
		// seperately implemented in the class DecisionPointAnalysisSettings
		// return new DecisionPointAnalysisSettings(model, logReader);
		return new MDLAnalysisResult((PetriNet) model, logReader, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.AnalysisPlugin#getInputItems()
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
		/* newly define an analysis input item .. */
		new AnalysisInputItem("Petrinet with associated Log") {
			// .. including the accepts method, which actually evaluates
			// the validity of the context provided
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLogReader = false;
				boolean hasPetriNet = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLogReader = true;
					} else if (o[i] instanceof PetriNet) {
						hasPetriNet = true;
					}
				}
				// context needs to provide both
				return hasLogReader && hasPetriNet;
			}
		} };
		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/mdl";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Minimum Description Length";
	}

}
