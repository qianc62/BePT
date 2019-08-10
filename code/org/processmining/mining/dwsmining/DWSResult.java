/*
 * Created on 19-set-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.processmining.mining.dwsmining;

import javax.swing.JComponent;

import org.processmining.analysis.dws.Cluster;
import org.processmining.analysis.dws.DWSOutputGUI;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

/**
 * Visualizes the results of DWS mining plugin, including a tree of workflow
 * models.
 * 
 * @author Gianluigi Greco, Antonella Guzzo, Luigi Pontieri.
 * @version 1.0
 */

public class DWSResult implements MiningResult, Provider {

	private Cluster root;
	private DWSOutputGUI outputGui = null;

	public void setRoot(Cluster root) {
		this.root = root;
	}

	public JComponent getVisualization() {
		outputGui = new DWSOutputGUI(root);
		return outputGui;
	}

	public LogReader getLogReader() {
		return null;
	}

	public ProvidedObject[] getProvidedObjects() {
		if (outputGui != null)
			return outputGui.getProvidedObjects();
		else
			return null;
	}

}
