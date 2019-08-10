package org.processmining.mining.pdmmining;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.pdm.PDMPetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningResult;

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
public class PDMPetriNetResult implements MiningResult, Provider,
		LogReaderConnection {

	protected LogReader log;
	private PDMPetriNet net = new PDMPetriNet();
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JScrollPane netContainer = new JScrollPane();

	public PDMPetriNetResult() {
	}

	public PDMPetriNetResult(PDMPetriNet net) {
		this.net = net;
		ModelGraphPanel gp = net.getGrappaVisualization();
		netContainer.setViewportView(gp);
		netContainer.invalidate();
		netContainer.repaint();

	}

	/**
	 * Returns a component that contains the visualization of this mining
	 * result. This function should return <code>null</code> if this result
	 * cannot be visualized.
	 * 
	 * @return a component that contains the visualization of this mining result
	 *         or <code>null</code> if this result cannot be visualized
	 */
	public JComponent getVisualization() {
		mainPanel.add(netContainer, BorderLayout.CENTER);
		return mainPanel;
	}

	/**
	 * Returns the <code>LogReader</code> object that was used to generate this
	 * mining result. This function may return null.
	 * 
	 * @return the <code>LogReader</code> object that was used to generate this
	 *         mining result or null.
	 */
	public LogReader getLogReader() {
		return log;
	}

	/**
	 * Provide all objects
	 * 
	 * @return The loaded YAWL model as a ProvidedObject
	 */
	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("PDM petri net",
				new Object[] { net }) };
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
		 */}

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

}
