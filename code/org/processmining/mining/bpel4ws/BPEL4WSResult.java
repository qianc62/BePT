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

package org.processmining.mining.bpel4ws;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.bpel4ws.type.BPEL4WS;
import org.processmining.framework.models.bpel4ws.type.BPEL4WSProcess;
import org.processmining.framework.models.bpel4ws.type.activity.Composed;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningResult;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

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
public class BPEL4WSResult implements MiningResult, Provider,
		LogReaderConnection {
	protected BPEL4WS model;
	protected LogReader log;
	private BPEL4WSHierarchy hierarchy = new BPEL4WSHierarchy() {
		protected void selectionChanged(final Object newSelection) {
			Object selection = newSelection;
			ModelGraph graph;
			if (selection instanceof BPEL4WS) {
				selection = (Object) ((BPEL4WS) selection).process;
			}
			if (selection instanceof BPEL4WSProcess) {
				selection = (Object) ((BPEL4WSProcess) selection).activity;
			}
			if (selection instanceof Composed) {
				final Composed composed = (Composed) selection;
				graph = new ModelGraph("bla") {
					public void writeToDot(Writer bw) throws IOException {
						bw.write(composed.writeToDot(model));
						Message.add("ja");
					}
				};
			} else {
				return;
			}

			ModelGraphPanel gp = graph.getGrappaVisualization();
			gp.addGrappaListener(new BPEL4WSGrappaAdapter(BPEL4WSResult.this));
			netContainer.setViewportView(gp);
			netContainer.invalidate();
			netContainer.repaint();
		}
	};

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
	public BPEL4WSResult(LogReader log, BPEL4WS model) {
		this.model = model;
		if (model != null) {
			BPEL4WSHierarchyVisitor.Build(this.model, hierarchy);
		}
		this.log = log;
	}

	/**
	 * Provide all objects
	 * 
	 * @return The loaded YAWL model as a ProvidedObject
	 */
	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("BPEL2WS model",
				new Object[] { model }) };
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
		ArrayList list = new ArrayList();
		BPEL4WSConnectablesVisitor.Build(model, list);
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
		BPEL4WSConnectVisitor.Build(model, newLog, eventsMapping);
	}

	void selectComposed(Composed composed) {
		hierarchy.setSelectedNode(composed);
	}
}

// Copied, perhaps of some use in the future.
class BPEL4WSGrappaAdapter extends GrappaAdapter {

	private BPEL4WSResult result;

	public BPEL4WSGrappaAdapter(BPEL4WSResult result) {
		this.result = result;
	}

	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {
		super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
		int i = InputEvent.BUTTON1_MASK;
		int j = InputEvent.SHIFT_MASK;
		if ((modifiers & i) == i && (modifiers & j) == j && clickCount == 1
				&& elem != null && elem.object != null
				&& elem.object instanceof Composed) {
			Composed composed = (Composed) elem.object;
			result.selectComposed(composed);
		}

	}
}
