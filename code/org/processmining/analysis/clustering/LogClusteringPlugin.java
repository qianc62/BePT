package org.processmining.analysis.clustering;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.util.PluginDocumentationLoader;

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
public class LogClusteringPlugin implements AnalysisPlugin {

	public LogClusteringPlugin() {

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Start this analysis algorithm.
	 * 
	 * @param inputs
	 *            the inputs chosen by the user
	 * @return user interface to the result of the analysis algorithm
	 * @todo Implement this org.processmining.analysis.AnalysisPlugin method
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {

		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		LogReader log = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
			}
		}
		return new LogClusteringUI(log, this);
	}

	/**
	 * Gets a description of this plugin in HTML.
	 * 
	 * @return a description of this plugin in HTML
	 * @todo Implement this org.processmining.framework.plugin.Plugin method
	 */
	public String getHtmlDescription() {

		return PluginDocumentationLoader.load(this);
	}

	/**
	 * Returns the input items needed by this analysis algorithm.
	 * 
	 * @return the input items accepted by this analysis algorithm
	 * @todo Implement this org.processmining.analysis.AnalysisPlugin method
	 */
	public AnalysisInputItem[] getInputItems() {

		AnalysisInputItem[] items = { new AnalysisInputItem("Log") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLog = false;

				for (int i = 0; i < o.length; i++) {
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
	 * Gets the name of this plugin.
	 * 
	 * @return the name of this plugin
	 * @todo Implement this org.processmining.framework.plugin.Plugin method
	 */
	public String getName() {

		return "Log Clustering";
	}

	private void jbInit() throws Exception {
		getHtmlDescription();
	}
}
