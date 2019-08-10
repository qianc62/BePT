package org.processmining.analysis.abstractions;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.abstractions.ui.RepeatAbstractionUI;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;

public class RepeatAbstractionPlugin implements AnalysisPlugin {
	protected RepeatAbstractionUI ui;

	public RepeatAbstractionPlugin() {

	}

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
		ui = new RepeatAbstractionUI(log);
		return ui;
	}

	public AnalysisInputItem[] getInputItems() {
		// needs any instance of LogReader to work
		AnalysisInputItem[] items = { new AnalysisInputItem("Low-level Log") {
			public boolean accepts(ProvidedObject object) {
				for (Object obj : object.getObjects()) {
					if (obj instanceof LogReader) {
						return true;
					}
				}
				return false;
			}
		} };
		return items;
	}

	/*
	 * HV: Line removed due to error in nightly build: [javac]
	 * D:/Hudson_home/jobs/ProM5 Nightly
	 * build/workspace/trunk/src/plugins/org/processmining
	 * /analysis/abstractions/RepeatAbstractionPlugin.java:49: method does not
	 * override a method from its superclass [javac] @Override [javac] ^
	 * 
	 * @Override
	 */
	public String getHtmlDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Repeat Abstractions";
	}

}
