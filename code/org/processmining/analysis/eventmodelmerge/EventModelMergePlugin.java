package org.processmining.analysis.eventmodelmerge;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * This analysis plugin merges a simulation model that is based on events into
 * an simulation model that is only based on activities. Therefore, the events
 * that refer to the same activity are merged into one activity. For the timing,
 * organizational and data perspective the user can decide how this merging
 * needs to take place. For the high level choices no user interaction is needed
 * and therefore they are automatically copied from the simulation model that is
 * based on events to the simulation model that is based on activities.
 * 
 * @author rmans
 */
public class EventModelMergePlugin implements AnalysisPlugin {

	/**
	 * Specify the name of the plugin.
	 * 
	 * @return The name of the plugin.
	 */
	public String getName() {
		return "Combine Low-level Activities";
	}

	/**
	 * Provide user documentation for the plugin.
	 * 
	 * @return The Html body of the documentation page.
	 */
	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/eventmodelmerge";
	}

	/**
	 * Define the input items necessary for the application of the plugin to
	 * offer its functionality to the user only in the right context. The
	 * EventModelMerge analysis plugin requires a HighLevelProcess that needs to
	 * be merged into an activity model.
	 * 
	 * @return An array with an AnalysisInputItem that accepts a ProvidedObject
	 *         having a HighLevelProcess.
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
		/* newly define an analysis input item .. */
		new AnalysisInputItem("HighLevel process") {
			// .. including the accepts method, which actually evaluates
			// the validity of the context provided
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasHighLevelProcess = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof HLModel) {
						hasHighLevelProcess = true;
					}
				}

				return hasHighLevelProcess;
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
		AnalysisInputItem highLevelProcessModel = analysisInputItemArray[0];

		Object[] o = highLevelProcessModel.getProvidedObjects()[0].getObjects();
		HLModel highLevelProcess = null;
		// since the plugin can only be invoked by the ProM tool, if the accepts
		// method
		// of its AnalysisInputItem (deliverable by getInputItems) evaluates to
		// true,
		// we can be sure that all the required arguments are passed over
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof HLModel) {
				highLevelProcess = (HLModel) o[i];
			}
		}
		// the construction of the plugin`s graphical user interface has been
		// seperately implemented in the class ConformanceCheckerUI
		JComponent result = new EventModelMergeUI(highLevelProcess);
		return result;
	}

}
