/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.mining.instancemining;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

import att.grappa.GrappaAdapter;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ModelGraphResult implements MiningResult, Provider {

	protected ModelGraph net;

	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JScrollPane netContainer;

	public ModelGraphResult(ModelGraph net) {
		this.net = net;
	}

	public LogReader getLogReader() {
		return null;
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject(net.getIdentifier(),
				new Object[] { net }) };
	}

	public JComponent getVisualization() {
		if (net == null) {
			netContainer = new JScrollPane();
		} else {
			ModelGraphPanel gp = net.getGrappaVisualization();
			if (gp == null) {
				netContainer = new JScrollPane();
			} else {
				gp.addGrappaListener(new GrappaAdapter());
				netContainer = new JScrollPane(gp);
			}
		}
		mainPanel.add(netContainer, BorderLayout.CENTER);

		return mainPanel;
	}

}
