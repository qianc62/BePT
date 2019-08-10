package org.processmining.analysis.performance.componentstate;

import javax.swing.*;

import org.processmining.analysis.*;
import org.processmining.framework.log.*;
import org.processmining.framework.plugin.*;
import org.processmining.framework.util.*;

/**
 * The plugin provides a dotted chart
 * 
 * @author Gabriela Alina Chipaila (g.a.chipaila@student.tue.nl)
 * 
 */

public class ComponentStatePlugin implements AnalysisPlugin {

	public ComponentStatePlugin() {
		super();
	}

	/**
	 * Specify the name of the plugin.
	 * 
	 * @return The name of the plugin.
	 */
	public String getName() {
		return "Element State Analysis";
	}

	/**
	 * Provide user documentation for the plugin.
	 * 
	 * @return The Html body of the documentation page.
	 */
	public String getHtmlDescription() {
		return null;
	}

	/**
	 * Define the input items necessary for the application of the plugin to
	 * offer its functionality to the user only in the right context.
	 * 
	 * @return An array with an AnalysisInputItem that accepts a ProvidedObject
	 *         having a LogReader and a PetriNet.
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
		/* newly define an analysis input item .. */
		new AnalysisInputItem("Log") {
			// .. including the accepts method, which actually evaluates
			// the validity of the context provided
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLogReader = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLogReader = true;
					}
				}
				// context needs to provide both
				return hasLogReader;
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
		// since the plugin can only be invoked by the ProM tool, if the accepts
		// method
		// of its AnalysisInputItem (deliverable by getInputItems) evaluates to
		// true,
		// we can be sure that all the required arguments are passed over
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				logReader = (LogReader) o[i];
			}
		}
		// construct the initial settings screen
		return new ComponentStateAnalysis(this, analysisInputItemArray,
				logReader);
	}

}
