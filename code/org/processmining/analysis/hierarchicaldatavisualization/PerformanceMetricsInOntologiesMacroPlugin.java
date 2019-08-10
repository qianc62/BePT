package org.processmining.analysis.hierarchicaldatavisualization;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.logstatistics.LogStatisticsResult;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.util.PluginDocumentationLoader;

public class PerformanceMetricsInOntologiesMacroPlugin implements
		AnalysisPlugin {

	/**
	 * Specify the name of the plugin.
	 * 
	 * @return The name of the plugin.
	 */
	public String getName() {
		return "Performance Metrics in Ontologies";
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
	 * offer its functionality to the user only in the right context. The
	 * Decision Point analysis plugin requires a Petri net to derive the
	 * decision points and a log file to anlyse the data elements.
	 * 
	 * @return An array with an AnalysisInputItem that accepts a ProvidedObject
	 *         having a LogReader and a PetriNet.
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("SAMXML Log") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						return true;
					}
				}
				return false;
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
		AnalysisInputItem inputItem = analysisInputItemArray[0];

		Object[] o = inputItem.getProvidedObjects()[0].getObjects();
		LogReader log = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
			}
		}
		return new PerformanceMetricsInOntologiesResult(LogStatisticsResult
				.getProcessingTimes(log), LogStatisticsResult
				.getThroughputTimes(log));
	}
}
