/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/
package org.processmining.framework.models.hlprocess.visualization;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.util.GuiDisplayable;

import att.grappa.Edge;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * Visualizes an arbitrary model graph in the context of a high level process. <br>
 * Note that if specific perspectives should be visualized, the passed model
 * needs to override the writeToDot method accordingly.
 * 
 * @see HLModel#getVisualization(perspectives)
 */
public class HLVisualization implements GuiDisplayable {

	/** The process model to be visualized */
	protected ModelGraph processModel;

	/**
	 * the model graph of the current visualization is kept in order to enable
	 * the highlighting of certain nodes in it
	 */
	protected ModelGraphPanel myModelGraphPanel;
	/**
	 * the mapping between Grappa nodes and model nodes will be kept in order to
	 * highlight, e.g., a node in the model
	 */
	protected HashMap myNodeMapping;

	/**
	 * Creates a visualization object that be used to create a panel containing
	 * the model visualization.
	 * 
	 * @param aProcessModel
	 *            the model to be visualized
	 */
	public HLVisualization(ModelGraph aProcessModel) {
		processModel = aProcessModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
	 */
	public JPanel getPanel() {
		JPanel returnPanel = new JPanel(new BorderLayout());
		myModelGraphPanel = processModel.getGrappaVisualization();
		// remember the mapping between grappa nodes and real nodes in order to
		// find, e.g., transitions back
		myNodeMapping = new HashMap();
		if (myModelGraphPanel != null) {
			Subgraph g = myModelGraphPanel.getSubgraph();
			buildGraphMapping(myNodeMapping, g);
			// actually create the result panel containing the visualization
			JScrollPane modelContainer = new JScrollPane(myModelGraphPanel);
			returnPanel.add(modelContainer);
			return returnPanel;
		} else {
			return new JPanel();
		}
	}

	/**
	 * Helper method building the mapping between Grappa nodes and model
	 * elements.
	 * 
	 * @param mapping
	 *            the target map for the mapped elements
	 * @param g
	 *            the subgraph to be mapped
	 */
	private void buildGraphMapping(Map mapping, Subgraph g) {
		Enumeration e = g.nodeElements();
		while (e.hasMoreElements()) {
			Node n = (Node) e.nextElement();
			mapping.put(n.object, n);
		}
		e = g.edgeElements();
		while (e.hasMoreElements()) {
			Edge n = (Edge) e.nextElement();
			mapping.put(n.object, n);
		}
		e = g.subgraphElements();
		while (e.hasMoreElements()) {
			Subgraph n = (Subgraph) e.nextElement();
			buildGraphMapping(mapping, n);
		}
	}

	/**
	 * Highlights the given nodes in the current visualization.
	 * 
	 * @param vertices
	 *            the graph vertices to be highlighted
	 */
	public void highLightNodesInVisualization(
			ArrayList<ModelGraphVertex> vertices) {
		ArrayList<ModelGraphVertex> myVertices = new ArrayList<ModelGraphVertex>();
		for (ModelGraphVertex vert : vertices) {
			for (Object mappedVert : this.myNodeMapping.keySet()) {
				if (mappedVert != null && mappedVert.equals(vert)) {
					myVertices.add((ModelGraphVertex) mappedVert);
				}
			}
		}
		if (myModelGraphPanel != null) {
			myModelGraphPanel.unSelectAll();
			myModelGraphPanel.selectElements(myVertices);
		}
	}
}
