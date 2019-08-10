package org.processmining.analysis.tracediff;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * Plugin visualizing the differences of two traces.
 * <p>
 * The first trace is the left (from) and the second the right (to) trace in the
 * comparison.
 * 
 * @author Anne Rozinat (a.rozinat@tue.nl)
 */
public class TraceDiffPlugin implements AnalysisPlugin {

	public JComponent analyse(AnalysisInputItem[] inputs) {
		AnalysisInputItem PNLog = inputs[0];
		Object[] o = PNLog.getProvidedObjects()[0].getObjects();
		LogReader log = null;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
				break;
			}
		}
		return new TraceDiffPluginUI(log);
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = {
		/* newly define an analysis input item .. */
		new AnalysisInputItem("Two Log Traces") {
			// .. including the accepts method, which actually evaluates
			// the validity of the context provided
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						return true;
					}
				}
				// context needs to provide two traces
				return false;
			}
		} };
		return items;
	}

	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/onlinedoc/tracediff";
	}

	public String getName() {
		return "Trace Comparison";
	}

}
