package org.processmining.analysis.sequenceclustering;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.slicker.logdialog.SlickerOpenLogSettings;

/**
 * @author Gabriel Veiga, IST - Technical University of Lisbon
 * @author Supervisor: Prof. Diogo Ferreira
 */
public class SequenceClusteringPlugin implements AnalysisPlugin {

	public SequenceClusteringPlugin() {
	}

	public String getName() {
		return "Sequence Clustering";
	}

	// protected ClassicOpenLogSettings settings;
	protected PreProcessingUI preProcessingUI;
	protected SlickerOpenLogSettings logSettings;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {
		// look for LogReader instance to open GUI
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		LogReader log = null;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
				break;
			}
		}
		logSettings = new SlickerOpenLogSettings(log);
		preProcessingUI = new PreProcessingUI(logSettings);
		// open GUI
		if (log != null) {
			return preProcessingUI.getInstances();
		} else {
			// error!
			throw new AssertionError(
					"analysis input items do not contain a log reader instance!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.AnalysisPlugin#getInputItems()
	 */
	public AnalysisInputItem[] getInputItems() {
		// needs any instance of LogReader to work
		AnalysisInputItem[] items = { new AnalysisInputItem("Log") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLog = false;
				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLog = true;
						break;
					}
				}
				return hasLog;
			}
		} };
		return items;
	}

	public String getHtmlDescription() {
		// TODO Auto-generated method stub
		return null;
	}
}
