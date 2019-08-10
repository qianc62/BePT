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

package org.processmining.mining.protosmining;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.protos.ProtosFlowElement;
import org.processmining.framework.models.protos.ProtosHierarchy;
import org.processmining.framework.models.protos.ProtosModel;
import org.processmining.framework.models.protos.ProtosSubprocess;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningResult;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

/**
 * <p>
 * Title: Protos mining result
 * </p>
 * 
 * <p>
 * Description: Holds a Protos mining result
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
 * @author Eric Verbeek
 * @version 1.0
 */
public class ProtosResult implements MiningResult, Provider,
		LogReaderConnection {
	// The loaded Protos model
	protected ProtosModel model;
	protected LogReader log;
	protected ProtosSubprocess subprocess = null; // Selected subprocess, if
	// null then model is
	// selected.
	private ProtosHierarchy hierarchy = new ProtosHierarchy() {
		protected void selectionChanged(final Object newSelection) {
			ModelGraph graph;
			if (newSelection instanceof ProtosSubprocess) {
				subprocess = (ProtosSubprocess) newSelection;
			} else {
				subprocess = null;
			}
			graph = (ModelGraph) newSelection;

			ModelGraphPanel gp = graph.getGrappaVisualization();
			gp.addGrappaListener(new ProtosGrappaAdapter(ProtosResult.this));
			netContainer.setViewportView(gp);
			netContainer.invalidate();
			netContainer.repaint();
		}
	};

	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JScrollPane netContainer = new JScrollPane();

	/**
	 * Set the loaded Protos model
	 * 
	 * @param log
	 *            The corresponding log
	 * @param model
	 *            The loaded model
	 */
	public ProtosResult(LogReader log, ProtosModel model) {
		this.model = model;
		hierarchy.addHierarchyObject(model, null, model.getName());
		Iterator it = model.getSubprocesses().iterator();
		while (it.hasNext()) {
			ProtosSubprocess subprocess = (ProtosSubprocess) it.next();
			hierarchy.addHierarchyObject(subprocess, model, subprocess
					.getName());
		}
		this.log = log;
	}

	/**
	 * Provide all objects
	 * 
	 * @return The loaded YAWL model as a ProvidedObject
	 */
	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject(
				subprocess == null ? "Protos model" : "Protos subprocess",
				new Object[] { subprocess == null ? model : subprocess }) };
	}

	/**
	 * Visualize the YAWL model
	 * 
	 * @return The JPAnel visualizing the YAWL model
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
	 * @return all connectable objects of the underlying model
	 */
	public ArrayList getConnectableObjects() {
		ProtosSubprocess subprocess;
		ArrayList list = new ArrayList();
		Iterator it = model.getSubprocesses().iterator();
		while (it.hasNext()) {
			subprocess = (ProtosSubprocess) it.next();
			Iterator it2 = subprocess.getActivities().iterator();
			while (it2.hasNext()) {
				Object object = it2.next();
				list.add(object);
			}
		}
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
		ProtosSubprocess decomposition;
		Iterator it = model.getSubprocesses().iterator();
		while (it.hasNext()) {
			decomposition = (ProtosSubprocess) it.next();
			Iterator it2 = decomposition.getActivities().iterator();
			while (it2.hasNext()) {
				ProtosFlowElement flowElement = (ProtosFlowElement) it2.next();
				Object[] objects = (Object[]) eventsMapping.get(flowElement);
				flowElement.setLogEvent((LogEvent) objects[0]);
				flowElement.getVertex().setIdentifier((String) objects[1]);
			}
		}
	}

	void selectDecom(ProtosSubprocess subprocess) {
		hierarchy.setSelectedNode(subprocess);
	}
}

class ProtosGrappaAdapter extends GrappaAdapter {

	private ProtosResult result;

	public ProtosGrappaAdapter(ProtosResult result) {
		this.result = result;
	}

	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {
		super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
		int i = InputEvent.BUTTON1_MASK;
		int j = InputEvent.SHIFT_MASK;
		if ((modifiers & i) == i && (modifiers & j) == j && clickCount == 1
				&& elem != null && elem.object != null
				&& elem.object instanceof ProtosFlowElement) {
			ProtosFlowElement flowElement = (ProtosFlowElement) elem.object;
			result.selectDecom(flowElement.getSubprocess());
		}

	}
}
