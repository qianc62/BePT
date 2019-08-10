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

package org.processmining.mining.bpmnmining;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.bpmn.BpmnGraph;
import org.processmining.framework.models.bpmn.BpmnNetHierarchy;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningResult;

/**
 * <p>
 * Title: BPMN Result
 * </p>
 * <p>
 * Description: MiningResult for BPMN
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * @version 1.0
 */

public class BpmnResult implements MiningResult, Provider/* , LogReaderConnection */{
	// The loaded BPMN model
	protected BpmnGraph graph;
	protected LogReader log;
	private BpmnNetHierarchy hierarchy = new BpmnNetHierarchy() {
		protected void selectionChanged(final Object newSelection) {
			ModelGraph graph = (BpmnGraph) newSelection;
			ModelGraphPanel gp = graph.getGrappaVisualization();

			netContainer.setViewportView(gp);
			netContainer.invalidate();
			netContainer.repaint();
		}
	};

	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JScrollPane netContainer = new JScrollPane();

	/**
	 * Set the loaded BPMN model
	 * 
	 * @param log
	 *            The corresponding log
	 * @param graph
	 *            The loaded graph
	 */
	public BpmnResult(LogReader log, BpmnGraph graph) {
		this.graph = graph;
		hierarchy.addHierarchyObject(graph, null, graph.getName());
		this.log = log;
	}

	/**
	 * Provide all objects
	 * 
	 * @return The loaded BPMN model as a ProvidedObject
	 */
	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("BPMN model",
				new Object[] { graph }) };
	}

	/**
	 * Visualize the BPMN model
	 * 
	 * @return The JPAnel visualizing the BPMN model
	 */
	public JComponent getVisualization() {
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				true, hierarchy.getTreeVisualization(), netContainer);
		splitPane.setOneTouchExpandable(true);
		mainPanel.add(splitPane, BorderLayout.CENTER);

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
	 * @return all connectable objects of the underlying model public ArrayList
	 *         getConnectableObjects() { ArrayList list = new ArrayList();
	 *         return list; }
	 */

	/**
	 * 
	 * @param newLog
	 *            the log reader to connect
	 * @param eventsMapping
	 *            the events to map public void connectWith(LogReader newLog,
	 *            HashMap eventsMapping) { }
	 */

	void selectDecom(String decomName) {
	}
}