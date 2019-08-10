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

public class PDMResult implements MiningResult, Provider, LogReaderConnection {
	// The loaded YAWL model
	protected PDMModel model;
	protected LogReader log;
	/*
	 * private YawlNetHierarchy hierarchy = new YawlNetHierarchy() { protected
	 * void selectionChanged(final Object newSelection) { ModelGraph graph; if
	 * (newSelection instanceof YAWLDecomposition) { graph = new
	 * ModelGraph("bla") { public void writeToDot(Writer bw) throws IOException
	 * { ((YAWLDecomposition) newSelection).writeToDot(bw, model);
	 * Message.add("ja"); } }; } else { graph = (YAWLModel) newSelection; }
	 * 
	 * ModelGraphPanel gp = graph.getGrappaVisualization();
	 * gp.addGrappaListener(new YAWLGrappaAdapter(YAWLResult.this));
	 * netContainer.setViewportView(gp); netContainer.invalidate();
	 * netContainer.repaint(); } };
	 */
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
	public PDMResult(LogReader log, PDMModel model) {
		this.model = model;
		ModelGraphPanel gp = model.getGrappaVisualization();
		netContainer.setViewportView(gp);
		netContainer.invalidate();
		netContainer.repaint();

		/*
		 * hierarchy.addHierarchyObject(model, null, model.getName()); Iterator
		 * it = model.getDecompositions().iterator(); while (it.hasNext()) {
		 * YAWLDecomposition decomposition = (YAWLDecomposition) it.next();
		 * hierarchy.addHierarchyObject(decomposition, model,
		 * decomposition.getName()); } this.log = log;
		 */
	}

	/**
	 * Provide all objects
	 * 
	 * @return The loaded YAWL model as a ProvidedObject
	 */
	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("PDM model",
				new Object[] { model }) };
	}

	/**
	 * Visualize the YAWL model
	 * 
	 * @return The JPAnel visualizing the YAWL model
	 */
	public JComponent getVisualization() {
		/*
		 * JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		 * true, hierarchy.getTreeVisualization(), netContainer);
		 * splitPane.setOneTouchExpandable(true); mainPanel.add(splitPane,
		 * BorderLayout.CENTER);
		 */
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
		// YAWLDecomposition decomposition;
		ArrayList list = new ArrayList();
		// Iterator it = model.getDecompositions().iterator();
		/*
		 * while (it.hasNext()) { decomposition = (YAWLDecomposition) it.next();
		 * Iterator it2 = decomposition.getNodes().iterator(); while
		 * (it2.hasNext()) { Object object = it2.next(); if (object instanceof
		 * YAWLTask) { list.add(object); } } }
		 */return list;

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
		/*
		 * YAWLDecomposition decomposition; Iterator it =
		 * model.getDecompositions().iterator(); while (it.hasNext()) {
		 * decomposition = (YAWLDecomposition) it.next(); Iterator it2 =
		 * decomposition.getNodes().iterator(); while (it2.hasNext()) { Object
		 * object = it2.next(); if (object instanceof YAWLTask) { Object[]
		 * objects = (Object[]) eventsMapping.get(object); YAWLTask task =
		 * (YAWLTask) object; task.setLogEvent((LogEvent) objects[0]);
		 * task.setIdentifier((String) objects[1]); } } }
		 */
	}

	void selectDecom(String decomName) {
		/*
		 * YAWLDecomposition comp = model.getDecomposition(decomName); if (comp
		 * != null) { hierarchy.setSelectedNode(comp); }
		 */
	}

}

// Copied, perhaps of some use in the future.
class PDMGrappaAdapter extends GrappaAdapter {

	private PDMResult result;

	public PDMGrappaAdapter(PDMResult result) {
		this.result = result;
	}

	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {
		/*
		 * super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
		 * int i = InputEvent.BUTTON1_MASK; int j = InputEvent.SHIFT_MASK; if
		 * ((modifiers & i) == i && (modifiers & j) == j && clickCount == 1 &&
		 * elem != null && elem.object != null && elem.object instanceof
		 * YAWLTask) { YAWLTask t = (YAWLTask) elem.object;
		 * result.selectDecom(t.getDecomposition());
		 * 
		 * }
		 */
	}

}
