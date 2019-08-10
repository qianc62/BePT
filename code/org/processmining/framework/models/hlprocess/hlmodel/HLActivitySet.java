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

package org.processmining.framework.models.hlprocess.hlmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.activitygraph.ActivityGraph;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLTypes.Perspective;
import org.processmining.framework.models.hlprocess.visualization.HLActivitySetVisualization;

/**
 * High-level process model implementation for models without a concrete
 * control-flow model. <br>
 * Provides a means to create a high-level process model without having a
 * process model at hand. Choices only refer to target activities and,
 * therefore, can also be specified for a high- level activity set. <br>
 * Creates and maps graph nodes onto HLActivities and HLChoices. The default
 * visualization for the underlyzing ActivityGraph is a set of rectangular
 * shapes representing the tasks in the model.
 */
public class HLActivitySet extends HLModel {

	/**
	 * Default constructor.
	 */
	public HLActivitySet(ActivityGraph actGraph) {
		super(actGraph);
		initialize();
	}

	/**
	 * Initializes the high-level model by creating high-level activities per
	 * task node in the activity model, and establishing the mapping between
	 * them.
	 */
	protected void initialize() {
		super.initialize();
		ActivityGraph activityGraph = (ActivityGraph) model;
		// initialize high level activities
		for (ModelGraphVertex vert : activityGraph.getActivityVertices()) {
			HLActivity hlAct = new HLActivity(vert.getIdentifier(), hlProcess);
			vertexToHLActivityMapping.put(vert, hlAct.getID());
		}
	}

	/**
	 * Adds the given HLActivity to this HLActivitySet. <br>
	 * Also creates a new graph node in the underlying ActivityGraph.
	 * 
	 * @param act
	 *            the HLActivity to be added
	 */
	public void addActivity(HLActivity act) {
		ModelGraphVertex actNode = new ModelGraphVertex(model);
		((ActivityGraph) model).addActivityVertex(actNode);
		actNode.setIdentifier(act.getName());
		this.setActivity(actNode, act);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.hlmodel.HLModel#getVisualization
	 * ()
	 */
	public ModelGraph getVisualization(Set<Perspective> perspectivesToShow) {
		return new HLActivitySetVisualization(this, perspectivesToShow);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.hlmodel.HLModel#getGraphNodes
	 * ()
	 */
	public List<ModelGraphVertex> getGraphNodes() {
		ArrayList<ModelGraphVertex> returnNodes = new ArrayList<ModelGraphVertex>();
		returnNodes.addAll(((ActivityGraph) model).getActivityVertices());
		return returnNodes;
	}

	public String toString() {
		return "Activity set: " + hlProcess.getGlobalInfo().getName();
	}

}
