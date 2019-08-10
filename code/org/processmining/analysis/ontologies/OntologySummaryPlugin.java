package org.processmining.analysis.ontologies;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author Ana Karla A. de Medeiros
 * @author Peter van den Brand
 */
public class OntologySummaryPlugin implements AnalysisPlugin {
	public OntologySummaryPlugin() {
	}

	/**
	 * analyse
	 * 
	 * @param analysisInputItemArray
	 *            AnalysisInputItem[]
	 * @return JComponent
	 * @todo Implement this org.processmining.analysis.AnalysisPlugin method
	 */
	public JComponent analyse(AnalysisInputItem[] analysisInputItemArray) {
		Object[] o = (analysisInputItemArray[0].getProvidedObjects())[0]
				.getObjects();
		LogReader log = null;

		for (int i = 0; i < o.length; i++) {
			// TODO select SemanticLogReader objects instead of LogReader
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
			}
		}

		return new OntologySummaryResults(log.getLogSummary().getOntologies(),
				log);

	}

	/**
	 * getHtmlDescription
	 * 
	 * @return String
	 * @todo Implement this org.processmining.framework.plugin.Plugin method
	 */
	public String getHtmlDescription() {
		return null;
	}

	/**
	 * getInputItems
	 * 
	 * @return AnalysisInputItem[]
	 * @todo Implement this org.processmining.analysis.AnalysisPlugin method
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Log") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLog = false;

				for (int i = 0; i < o.length; i++) {
					// TODO select SemanticLogReader objects instead of
					// LogReader
					if (o[i] instanceof LogReader) {
						hasLog = true;
					}
				}
				return hasLog;
			}
		} };
		return items;

	}

	/**
	 * getName
	 * 
	 * @return String
	 * @todo Implement this org.processmining.framework.plugin.Plugin method
	 */
	public String getName() {
		return "Ontology Summary";
	}
}
