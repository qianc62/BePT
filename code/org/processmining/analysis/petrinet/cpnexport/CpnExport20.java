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

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * Exports a given low-level Petri net or a ColoredPetriNet to a coloured Petri
 * net representation that can be read by CPN Tools.
 * 
 * @see ColoredPetriNet
 * @see CpnExportSettings
 * 
 * @author Anne Rozinat (a.rozinat@tue.nl)
 * @author Ronny Mans
 * @author Mariska Netjes (adding possibility to remove user save for
 *         CpnExportSettings for redesign mode)
 */
public class CpnExport20 implements AnalysisPlugin {

	/**
	 * Specifies the name of the plug-in. This is used for, e.g., labelling the
	 * corresponding menu item or the user documentation page.
	 * 
	 * @return the name (and the supported version) of the exported file format
	 */
	public String getName() {
		return "Export to CPN Tools 2.0";
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
			/**
			 * Determines whether a given object can be exported as a CPN.
			 * 
			 * @param object
			 *            the <code>ProvidedObject</code> which shall be tested
			 *            for being a valid input to this export plug-in
			 * @return <code>true</code> if the given object is a
			 *         <code>PetriNet</code>, <code>false</code> otherwise
			 */
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof PetriNet || o[i] instanceof HLPetriNet) {
						return true;
					}
				}
				return false;
			}
		} };
		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {
		AnalysisInputItem input = inputs[0];
		Object[] o = input.getProvidedObjects()[0].getObjects();
		ColoredPetriNet netToExport = null;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet || o[i] instanceof HLPetriNet) {
				if ((o[i] instanceof PetriNet) == true) {
					// if the provided object is a low-level Petri net..
					// .. turn it into a coloured Petri net to be exported
					// (low-level net will be only used as template and
					// default simulation information will be created)
					netToExport = new ColoredPetriNet((PetriNet) o[i]);
				} else {
					// if the the provided object is a (PetriNet-based)
					// simulation model..
					// ... clone the passed simulation model (i.e., so that the
					// provided object remains
					// in the framework in the state it was before calling this
					// export plugin)
					HLPetriNet cloned = (HLPetriNet) ((HLPetriNet) o[i])
							.clone();
					netToExport = new ColoredPetriNet(cloned);
				}
			}
		}

		CpnExportSettings exportCPN = new CpnExportSettings(this, netToExport);

		return exportCPN;
	}

	/**
	 * Provides user documentation for the plug-in.
	 * 
	 * @return The Html body of the documentation page.
	 */
	public String getHtmlDescription() {
		return ("http://prom.win.tue.nl/research/wiki/online/cpnexport");
	}

}
