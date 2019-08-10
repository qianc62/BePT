package org.processmining.analysis.petrinet;

import org.processmining.mining.regionmining.ParikhLanguageRegionMiner;
import org.processmining.analysis.AnalysisPlugin;
import javax.swing.JComponent;
import org.processmining.analysis.AnalysisInputItem;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.MiningSettings;

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
public class ParikhLanguageNetExtender extends ParikhLanguageRegionMiner
		implements AnalysisPlugin {

	public String getName() {
		return "Parikh Language-based Region net extender";
	}

	public String getHtmlDescription() {
		return "This plugin uses the theory of regions for mining, without building an intermediate transition system, "
				+ "i.e. region theory is used directly onto the log, assuming that the log is a natural language."
				+ "The given net is extended with places to express causal dependencies in the log "
				+ "not yet expressed by places in the Petri net.";
	}

	public ParikhLanguageNetExtender() {
	}

	/**
	 * Define the input items necessary for the application of the plugin to
	 * offer its functionality to the user only in the right context. The
	 * ConformanceChecker analysis plugin requires a Petri net and a log file to
	 * evaluate their correspondence.
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
						hasPetriNet = true; // ((PetriNet)
						// o[i]).findRandomInvisibleTransition()
						// == null;;
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
		return (JComponent) new MiningSettings(logReader,
				"Settings for parikhMining Analysis plugin", this)
				.getContentPane();

	}

}
