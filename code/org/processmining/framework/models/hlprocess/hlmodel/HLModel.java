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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.HLTypes.Perspective;
import org.processmining.framework.ui.Message;

/**
 * Allows to associate actual graph nodes in a control-flow process model with
 * high-level process information. <br>
 * Task nodes can be mapped on HLActivities, and choice nodes on HLChoices. <br>
 * To provide this kind of mapping is the main task of the interface and any
 * implementing class. <br>
 * The actual high-level information is encapsulated in the referenced HLProcess
 * object.
 * 
 * @see HLProcess
 * @see HLActivity
 * @see HLChoice
 */
public abstract class HLModel implements Cloneable {

	/** the global high level process information */
	protected HLProcess hlProcess;
	/** the model where this high-level process refers to */
	protected ModelGraph model;

	/**
	 * the mapping from all activity nodes in the model to its belonging high
	 * level activity ID
	 */
	protected HashMap<ModelGraphVertex, HLID> vertexToHLActivityMapping;
	/**
	 * the mapping from all choice nodes in the model to its high level choice
	 * ID
	 */
	protected HashMap<ModelGraphVertex, HLID> vertexToHLChoiceMapping;

	/**
	 * Default constructor.
	 * 
	 * @param aModel
	 *            the control-flow model to which the high level information
	 *            should be mapped
	 */
	protected HLModel(ModelGraph aModel) {
		model = aModel;
	}

	/**
	 * Initializes the mappings between high-level activities and choices and
	 * the nodes in the corresponding nodes in the process model.
	 * <p>
	 * Deriving sub classes should override this method to establish an
	 * appropriate default mapping and call this method in the very beginning.
	 * <p>
	 * Should be called in the constructor of deriving sub classes and will be
	 * used to reset the high-level model to default information.
	 */
	protected void initialize() {
		hlProcess = new HLProcess();
		vertexToHLActivityMapping = new HashMap<ModelGraphVertex, HLID>();
		vertexToHLChoiceMapping = new HashMap<ModelGraphVertex, HLID>();
	}

	/**
	 * Retrieves the high-level information for this process.
	 * 
	 * @return the HLProcess object containing high-level information
	 */
	public HLProcess getHLProcess() {
		return hlProcess;
	}

	/**
	 * Sets the high-level information for this process.
	 * 
	 * @param process
	 *            the HLProcess object containing the high-level information
	 */
	public void setHLProcess(HLProcess process) {
		hlProcess = process;
	}

	/**
	 * Retrieves the underlying control-flow model for the high level model.
	 * 
	 * @return ModelGraph the underlying process model
	 */
	public ModelGraph getProcessModel() {
		return model;
	}

	/**
	 * Retrieves the visualization object for this high level process.
	 * <p>
	 * Per default the plain process model is returned here. If a diagnostic
	 * visualization is desired, implementing classes should return a new object
	 * of type {@link HighLevelVisualization} every time this method is called.
	 * 
	 * @param the
	 *            perspectives that should be visualized for this model
	 * @return the diagnostic visualization object for the current high level
	 *         information
	 */
	public ModelGraph getVisualization(Set<Perspective> perspectivesToShow) {
		return getProcessModel();
	}

	/**
	 * Returns a list of potentially filtered activities.
	 * <p>
	 * Sub classes can determine which activities they want to hide. Per default
	 * all activities are visible.
	 * 
	 * @return
	 */
	public ArrayList<HLActivity> getSelectedActivities() {
		ArrayList<HLActivity> result = new ArrayList<HLActivity>();
		for (HLID actID : vertexToHLActivityMapping.values()) {
			result.add(hlProcess.getActivity(actID));
		}
		return result;
	}

	/**
	 * Retrieves the activity ID stored for the given vertex in the activity
	 * mapping.
	 * 
	 * @param vertex
	 *            the vertex for which the activity ID should be retrieved
	 * @return the activity ID, <code>null</code> if not found
	 */
	public HLID getActivityID(ModelGraphVertex vertex) {
		return vertexToHLActivityMapping.get(vertex);
	}

	/**
	 * Retrieves the choice ID stored for the given vertex in the choice
	 * mapping.
	 * 
	 * @param vertex
	 *            the vertex for which the choice ID should be retrieved
	 * @return the choice ID, <code>null</code> if not found
	 */
	public HLID getChoiceID(ModelGraphVertex vertex) {
		return vertexToHLChoiceMapping.get(vertex);
	}

	/**
	 * Returns the first ModelGraphVertex that is mapped to the given high level
	 * activity.
	 * 
	 * @param actID
	 *            the ID of the high level activity for which we want to have
	 *            the corresponding modelgraphVertex in the graph
	 * @return one vertex that is mapped to the given high level activity
	 */
	public ModelGraphVertex findModelGraphVertexForActivity(HLID actID) {
		for (ModelGraphVertex key : vertexToHLActivityMapping.keySet()) {
			if (vertexToHLActivityMapping.get(key).equals(actID)) {
				return key;
			}
		}
		return null;
	}

	/**
	 * Returns the first ModelGraphVertex that is mapped to the given high level
	 * choice.
	 * 
	 * @param choiceID
	 *            the ID of the high level choice for which we want to have the
	 *            corresponding modelgraphVertex in the graph
	 * @return one vertex that is mapped to to the given high level choice
	 */
	public ModelGraphVertex findModelGraphVertexForChoice(HLID choiceID) {
		for (ModelGraphVertex key : vertexToHLChoiceMapping.keySet()) {
			if (vertexToHLChoiceMapping.get(key).equals(choiceID)) {
				return key;
			}
		}
		return null;
	}

	/**
	 * Replaces the old vertex in by the given new value in the activity
	 * mapping. If it does not exist, the method has no effect
	 * 
	 * @param oldVertex
	 *            the vertex to be replaced
	 * @param newVertex
	 *            the new vertex for the given activity
	 */
	public void replaceModelGraphVertexForActivity(ModelGraphVertex oldVertex,
			ModelGraphVertex newVertex) {
		if (vertexToHLActivityMapping.containsKey(oldVertex)) {
			HLID actID = vertexToHLActivityMapping.get(oldVertex);
			vertexToHLActivityMapping.remove(oldVertex);
			vertexToHLActivityMapping.put(newVertex, actID);
		}
	}

	/**
	 * Replaces the old vertex in by the given new value in the choice mapping.
	 * If it does not exist, the method has no effect
	 * 
	 * @param oldVertex
	 *            the vertex to be replaced
	 * @param newVertex
	 *            the new vertex for the given choice
	 */
	public void replaceModelGraphVertexForChoice(ModelGraphVertex oldVertex,
			ModelGraphVertex newVertex) {
		if (vertexToHLChoiceMapping.containsKey(oldVertex)) {
			HLID chID = vertexToHLChoiceMapping.get(oldVertex);
			vertexToHLChoiceMapping.remove(oldVertex);
			vertexToHLChoiceMapping.put(newVertex, chID);
		}
	}

	/**
	 * Sets the corresponding highlevelactivity for a node of the process model
	 * that belongs to the highlevelprocess.
	 * <p>
	 * Note that if there was a previous HLActivity associated to this vertex,
	 * it will be replaced in the high-level process by the new one.
	 * <p>
	 * This has the side effect that if other task nodes in the process sharing
	 * the same high-level activity would point to an HLActivity that is not
	 * anymore in the process. They need to be updated as well.
	 * 
	 * @param vertex
	 *            ModelGraphVertex a node in the process model that belongs to
	 *            the highlevelprocess
	 * @param activity
	 *            HLActivity the highlevelactivity that has to be assigned to
	 *            the node in the process model
	 */
	public void setActivity(ModelGraphVertex vertex, HLActivity activity) {
		HashMap<ModelGraphVertex, HLID> modifiedMapping = (HashMap<ModelGraphVertex, HLID>) vertexToHLActivityMapping
				.clone();
		if (vertexToHLActivityMapping.containsKey(vertex)) {
			HLID oldActID = vertexToHLActivityMapping.get(vertex);
			// remove old mapping and add new one
			modifiedMapping.remove(oldActID);
			modifiedMapping.put(vertex, activity.getID());
			hlProcess.replaceActivity(oldActID, activity);
			vertexToHLActivityMapping = modifiedMapping;
		} else {
			vertexToHLActivityMapping.put(vertex, activity.getID());
		}
	}

	/**
	 * Sets the corresponding highlevelchoice for a node of the process model
	 * that belongs to the highlevelprocess.
	 * <p>
	 * Note that if there was a previous HLChoice associated to this vertex, it
	 * will be replaced in the high-level process by the new one.
	 * <p>
	 * This has the side effect that if other nodes in the process sharing the
	 * same high-level choice would point to an HLChoice that is not anymore in
	 * the process. They need to be updated as well.
	 * 
	 * @param vertex
	 *            ModelGraphVertex a node in the process model that belongs to
	 *            the highlevelprocess
	 * @param choice
	 *            HLChoice the highlevelactivity that has to be assigned to the
	 *            node in the process model.
	 */
	public void setChoice(ModelGraphVertex vertex, HLChoice choice) {
		HashMap<ModelGraphVertex, HLID> modifiedMapping = (HashMap<ModelGraphVertex, HLID>) vertexToHLChoiceMapping
				.clone();
		if (vertexToHLChoiceMapping.containsKey(vertex)) {
			HLID oldChID = vertexToHLChoiceMapping.get(vertex);
			// remove old mapping and add new one
			modifiedMapping.remove(oldChID);
			modifiedMapping.put(vertex, choice.getID());
			hlProcess.replaceChoice(oldChID, choice);
			vertexToHLChoiceMapping = modifiedMapping;
		} else {
			vertexToHLChoiceMapping.put(vertex, choice.getID());
		}

	}

	/**
	 * Returns the HLActivity object belonging to the given node. <br>
	 * Note that the given modelgraphvertex only needs to be equal to the
	 * modelgraphvertex of the belonging graph but not the same object.
	 * 
	 * @param v
	 *            ModelGraphVertex the vertex for which the corresponding
	 *            simulation information object is to be found
	 * @return the highlevel activity object for the given modelgraph vertex
	 *         <code>Null</code> otherwise
	 */
	public HLActivity findActivity(ModelGraphVertex v) {
		for (Entry<ModelGraphVertex, HLID> entry : vertexToHLActivityMapping
				.entrySet()) {
			if (entry.getKey() != null && entry.getKey().equals(v)) {
				return hlProcess.getActivity(entry.getValue());
			}
		}
		// not found
		Message.add("Finding the high-level activity for model graph vertex "
				+ v.getIdentifier() + " has failed.", Message.DEBUG);
		return null;
	}

	/**
	 * Returns the HLChoice object belonging to the given node. <br>
	 * Note that the given modelgraphvertex only needs to be equal to the
	 * modelgraphvertex of the belonging graph but not the same object.
	 * 
	 * @param v
	 *            ModelGraphVertex the vertex for which the corresponding
	 *            simulation information object is to be found
	 * @return the highlevel activity object for the given modelgraph vertex
	 *         <code>Null</code> otherwise
	 */
	public HLChoice findChoice(ModelGraphVertex v) {
		for (Entry<ModelGraphVertex, HLID> entry : vertexToHLChoiceMapping
				.entrySet()) {
			if (entry.getKey() != null && entry.getKey().equals(v)) {
				return hlProcess.getChoice(entry.getValue());
			}
		}
		// not found
		Message.add("Finding the high-level choice for model graph vertex "
				+ v.getIdentifier() + " has failed.", Message.DEBUG);
		return null;
	}

	/**
	 * Retrieves all nodes of the process model that could be mapped to elements
	 * of the high-level process (such as activities or choices).
	 * 
	 * @return the nodes of the process model that can be mapped to the
	 *         high-level process elements. The list can be empty
	 */
	public List<ModelGraphVertex> getGraphNodes() {
		ArrayList<ModelGraphVertex> returnNodes = new ArrayList<ModelGraphVertex>();
		returnNodes.addAll(model.getVerticeList());
		return returnNodes;
	}

	/**
	 * Retrieves all nodes of the process model that are currently mapped to
	 * high level activities.
	 * 
	 * @return the activity graph nodes
	 */
	public List<ModelGraphVertex> getActivityNodes() {
		return new ArrayList<ModelGraphVertex>(vertexToHLActivityMapping
				.keySet());
	}

	/**
	 * Retrieves all nodes of the process model that are currently mapped to
	 * high level choices.
	 * 
	 * @return the choice graph nodes
	 */
	public List<ModelGraphVertex> getChoiceNodes() {
		return new ArrayList<ModelGraphVertex>(vertexToHLChoiceMapping.keySet());
	}

	/**
	 * Makes a deep copy of the object, but does not clone the underlying
	 * process model. <br>
	 * Clones the attached high level process, and the high-level transition and
	 * the high-level choice mappings. <br>
	 * The underlying Process model must not cloned as object identity of graph
	 * nodes is needed to retrieve the related high-level objects after cloning.
	 * 
	 * @return the cloned object
	 */
	public Object clone() {
		HLModel o = null;
		try {
			o = (HLModel) super.clone();
			// clone the high-level process data
			o.hlProcess = (HLProcess) hlProcess.clone();
			// clone the lists (no deep copy needed?)
			o.vertexToHLActivityMapping = (HashMap<ModelGraphVertex, HLID>) vertexToHLActivityMapping
					.clone();
			o.vertexToHLChoiceMapping = (HashMap<ModelGraphVertex, HLID>) vertexToHLChoiceMapping
					.clone();
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Initializes this high level model with default information.
	 */
	public void reset() {
		// call initialize() method of sub classes
		this.initialize();
	}

	/**
	 * The name of the type of model should be provided here.
	 */
	public abstract String toString();
}
