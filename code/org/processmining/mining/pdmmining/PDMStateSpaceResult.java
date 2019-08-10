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

package org.processmining.mining.pdmmining;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.pdm.PDMModel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningResult;
import org.processmining.framework.models.pdm.PDMStateSpace;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

/**
 * <p>
 * Title: PDM Result
 * </p>
 * <p>
 * Description: MiningResult for PDM
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */

public class PDMStateSpaceResult implements MiningResult, Provider,
		LogReaderConnection {
	// The loaded YAWL model
	protected PDMStateSpace statespace;
	protected LogReader log;
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JScrollPane netContainer = new JScrollPane();

	/**
	 * Set the loaded YAWL model
	 * 
	 * @param log
	 *            The corresponding log
	 * @param model
	 *            The loaded model
	 */
	public PDMStateSpaceResult(LogReader log, PDMStateSpace statespace) {
		this.statespace = statespace;
		ModelGraphPanel gp = statespace.getGrappaVisualization();
		netContainer.setViewportView(gp);
		netContainer.invalidate();
		netContainer.repaint();

	}

	/**
	 * Provide all objects
	 * 
	 * @return The loaded YAWL model as a ProvidedObject
	 */
	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("PDM Statespace",
				new Object[] { statespace }) };
	}

	/**
	 * Visualize the YAWL model
	 * 
	 * @return The JPAnel visualizing the YAWL model
	 */
	public JComponent getVisualization() {
		mainPanel.add(netContainer, BorderLayout.CENTER);
		return mainPanel;
	}

	/**
	 * No log reader
	 * 
	 * @return null
	 */
	public LogReader getLogReader() {
		return log;
	}

	/**
	 * Returns all connectable objects of the underlying model.
	 * 
	 * @return all connectable objects of the underlying model
	 */
	public ArrayList getConnectableObjects() {
		ArrayList list = new ArrayList();
		return list;

	}

	/**
	 * Connects a <code>LogReader</code> to the object. The
	 * <code>eventsMapping</code> variable is a <code>HashMap</code> that has a
	 * key for every <code>LogEvent</code> returned by the
	 * <code>getLogEvents</code> method. Each key is a <code>LogEvent</code> and
	 * it is associated with a <code>Object[2]</code> object. This array
	 * contains two objects. The first object is again a <code>LogEvent</code>
	 * object, to which the original should be mapped. The second is a
	 * <code>String</code> object, representing the label that should be used
	 * for the identifier of the underlying graphical object. All
	 * <code>LogEvent</code>s in the underlying model will be translated using
	 * this mapping. The <code>eventsMapping</code> parameter may be
	 * <code>null</code>.
	 * 
	 * @param newLog
	 *            the log reader to connect
	 * @param eventsMapping
	 *            the events to map
	 */
	public void connectWith(LogReader newLog, HashMap eventsMapping) {
	}

	void selectDecom(String decomName) {

	}

}
