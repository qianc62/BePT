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

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.yawl.YAWLCondition;
import org.processmining.framework.models.yawl.YAWLDecomposition;
import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.yawl.YAWLNode;
import org.processmining.framework.models.yawl.YAWLTask;

import att.grappa.Edge;

/**
 * High-level process model implementation for YAWL models. <br>
 * Maps YAWLTasks onto HLActivities and both YAWLConditions with more than one
 * outgoing arc as well as YAWLTasks with XOR split semantics to HLChoices.
 */
public class HLYAWL extends HLModel {

	/**
	 * Creates a high-level YAWL model with default high-level information.
	 * 
	 * @param aYawlModel
	 *            the underlying (low-level) YAWL model
	 */
	public HLYAWL(YAWLModel aYawlModel) {
		super(aYawlModel);
		initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.hlmodel.HLModel#initialize()
	 */
	protected void initialize() {
		super.initialize();
		YAWLModel yawlModel = (YAWLModel) model;
		for (YAWLDecomposition decomposition : yawlModel.getDecompositions()) {
			if (decomposition.isRoot() == true) {
				// TODO Anne: check how to handle hierarchical models. For now
				// only handle the nodes from the
				// the root net
				hlProcess.getGlobalInfo().setName(decomposition.getID());
				initActivities(decomposition);
				initChoices(decomposition);
			}
		}
	}

	/*
	 * Maps each YAWL task to a high-level activity.
	 */
	private void initActivities(YAWLDecomposition decomposition) {
		for (YAWLNode node : decomposition.getNodes()) {
			// every YAWL task gets a high-level activity
			if (node instanceof YAWLTask) {
				HLActivity hlActivity = new HLActivity(node.getIdentifier(),
						hlProcess);
				vertexToHLActivityMapping.put(node, hlActivity.getID());
			}
		}
	}

	/*
	 * Maps conditions with more than one outgoing arc and XOR split tasks onto
	 * high-level choices.
	 */
	private void initChoices(YAWLDecomposition decomposition) {
		// nodes need to be in place when creating the choices as they are
		// condition targes
		for (YAWLNode node : decomposition.getNodes()) {
			// only handle XOR splits for now as not clear how OR joins would
			// work in the simulation part
			if ((node instanceof YAWLTask && ((YAWLTask) node).getSplitType() == EPCConnector.XOR)
					|| ((node instanceof YAWLCondition) && (node.outDegree() > 1))) {
				HLChoice hlChoice = new HLChoice(node.getIdentifier(),
						hlProcess);
				// get condition targets
				for (Edge edge : node.getOutEdges()) {
					ModelGraphVertex outNode = (ModelGraphVertex) edge
							.getHead();
					HLActivity act = null;
					if (outNode instanceof YAWLTask) {
						act = findActivity(outNode);
						if (act != null) {
							hlChoice.addChoiceTarget(act.getID());
						}
					}
					// in case of condition being the target, add all following
					// tasks as condition targets
					else if (outNode instanceof YAWLCondition) {
						if (outNode.getOutEdges() != null) {
							for (Edge outCond : outNode.getOutEdges()) {
								ModelGraphVertex outTask = (ModelGraphVertex) outCond
										.getHead();
								act = findActivity(outTask);
								if (act != null) {
									hlChoice.addChoiceTarget(act.getID());
								}
							}
						}
					}
				}
				// add choice
				vertexToHLChoiceMapping.put(node, hlChoice.getID());
			}
		}
	}

	/**
	 * Get the underlying (low-level) YAWL model.
	 * 
	 * @return the actual YAWL model of this high-level YAWL model
	 */
	public YAWLModel getYAWLModel() {
		return (YAWLModel) model;
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
		for (YAWLDecomposition decomposition : ((YAWLModel) model)
				.getDecompositions()) {
			// check only for the nodes of the root net
			if (decomposition.isRoot() == true) {
				returnNodes.addAll(decomposition.getNodes());
			}
		}
		return returnNodes;
	}

	public String toString() {
		return "YAWL model: " + hlProcess.getGlobalInfo().getName();
	}

}
