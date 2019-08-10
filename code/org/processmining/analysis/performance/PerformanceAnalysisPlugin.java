/**
 * Project: ProM
 * File: PerformanceAnalysisPlugin.java
 * Author: Peter T.G. Hornix (P.T.G.Hornix@student.tue.nl)
 * Created: Feb 9, 2006, 2:33:02 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the
 *      names of its contributors may be used to endorse or promote
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.analysis.performance;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.util.PluginDocumentationLoader;

/**
 * The plugin provides some performance metrics and displays these with help of
 * the input Petri net
 * 
 * @author Peter T.G. Hornix (P.T.G.Hornix@student.tue.nl)
 * 
 */
public class PerformanceAnalysisPlugin implements AnalysisPlugin {

	/**
         *
         */
	public PerformanceAnalysisPlugin() {
		super();
	}

	/**
	 * Specify the name of the plugin.
	 * 
	 * @return The name of the plugin.
	 */
	public String getName() {
		return "Performance Analysis with Petri net";
	}

	/**
	 * Provide user documentation for the plugin.
	 * 
	 * @return The Html body of the documentation page.
	 */
	public String getHtmlDescription() {
		return ("<h1>" + getName() + ": Users Guide </h1>" + PluginDocumentationLoader
				.load(this));
	}

	/**
	 * Define the input items necessary for the application of the plugin to
	 * offer its functionality to the user only in the right context. The
	 * Performance analysis (with Petri net) plugin requires a Petri net and a
	 * log file to evaluate their correspondence.
	 * 
	 * @return An array with an AnalysisInputItem that accepts a ProvidedObject
	 *         having a LogReader and a PetriNet.
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

	/**
	 * Define the procedure that is called automatically as soon as the plugin
	 * is invoked by the ProM tool.
	 * 
	 * @param analysisInputItemArray
	 *            The list of input items necessary to carry out the analysis
	 *            provided by the plugin.
	 * @return The JComponent to be displayed to the user within the plugin
	 *         window.
	 */
	public JComponent analyse(AnalysisInputItem[] analysisInputItemArray) {
		AnalysisInputItem PNLog = analysisInputItemArray[0];

		Object[] o = PNLog.getProvidedObjects()[0].getObjects();
		LogReader logReader = null;
		PetriNet petriNet = null;
		// since the plugin can only be invoked by the ProM tool, if the accepts
		// method
		// of its AnalysisInputItem (deliverable by getInputItems) evaluates to
		// true,
		// we can be sure that all the required arguments are passed over
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				logReader = (LogReader) o[i];
			} else if (o[i] instanceof PetriNet) {
				petriNet = (PetriNet) o[i];
			}
		}
		// construct the initial settings screen
		return new PerformanceAnalysisSettings(this, analysisInputItemArray,
				petriNet, logReader);
	}

}
