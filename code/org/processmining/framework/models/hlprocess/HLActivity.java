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

package org.processmining.framework.models.hlprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.processmining.framework.models.hlprocess.HLTypes.TransformationType;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution;

/**
 * Holds information with respect to an activity in the process. <br>
 * Note that an activity is defined independently of any concrete task node in
 * the context of a process model (e.g., a function in an EPC model). <br>
 * If the activity is to be related to a specific node in a process model, then
 * this can be done in the high level process implementation for the
 * corresponding process model type (e.g., a Place for the HLPetriNet).
 * 
 * @see HighLevelModel#findActivity
 * @see HighLevelModel#findModelGraphVertexForActivity
 * @see HLProcess
 */
public class HLActivity extends HLProcessElement implements Cloneable {

	/** the execution time for this activity */
	protected HLDistribution executionTime = new HLGeneralDistribution();
	/** the waiting time for this activity */
	protected HLDistribution waitingTime = new HLGeneralDistribution();
	/** the sojourn time for this activity */
	protected HLDistribution sojournTime = new HLGeneralDistribution();
	/** the ID of the group that is defined for this activity */
	protected HLID groupID;
	// /** the data attributes that are available for this transition and their
	// modification type*/
	// private HashMap<HLID, TransformationType> dataAttributes = new
	// HashMap<HLID, TransformationType>();
	/**
	 * Added by Mariska Netjes, the input data attributes that are available for
	 * this transition and their modification type
	 */
	private HashMap<HLID, TransformationType> inputDataAttributes = new HashMap<HLID, TransformationType>();
	/**
	 * Added by Mariska Netjes, the output data attributes that are available
	 * for this transition and their modification type In case there is no
	 * distinction between input and output, all data attributes are output data
	 * attributes
	 */
	private HashMap<HLID, TransformationType> outputDataAttributes = new HashMap<HLID, TransformationType>();

	/**
	 * Default constructor (automatically registers the new activity for the
	 * given high level process).
	 * 
	 * @param name
	 *            the name of this high-level activity
	 * @param aProc
	 *            the high level process this activity belongs to
	 */
	public HLActivity(String aName, HLProcess aProc) {
		super(aName, aProc);
		process.activities.put(getID(), this);
		groupID = process.nobodyHLID;
	}

	/**
	 * Adds a new data attribute reference to this activity (with default
	 * transformation type "Resampled"). <br>
	 * Note that if this data attribute had already been added to this activity
	 * before, the method has no effect. It is assumed that the attribute is
	 * already part of the high level process.
	 * 
	 * @param attID
	 *            the ID of the attribute to be added
	 */
	// public void addDataAttribute(HLID attID) {
	// if (!outputDataAttributes.keySet().contains(attID)) {
	// outputDataAttributes.put(attID, TransformationType.Resample);
	// }
	// if (!inputDataAttributes.keySet().contains(attID)) {
	// inputDataAttributes.put(attID, TransformationType.Resample);
	// }
	//
	// }
	/**
	 * Adds a new input data attribute reference to this activity (with default
	 * transformation type "Resampled"). <br>
	 * Note that if this input data attribute had already been added to this
	 * activity before, the method has no effect. It is assumed that the
	 * attribute is already part of the high level process.
	 * 
	 * @param attID
	 *            the ID of the attribute to be added
	 */
	public void addInputDataAttribute(HLID attID) {
		if (!inputDataAttributes.keySet().contains(attID)) {
			inputDataAttributes.put(attID, TransformationType.Resample);
		}
	}

	/**
	 * Adds a new output data attribute reference to this activity (with default
	 * transformation type "Resampled"). In case there is no distinction between
	 * input and output data attributes, a data attribute has to be added to the
	 * outputDataAttributes. <br>
	 * Note that if this output data attribute had already been added to this
	 * activity before, the method has no effect. It is assumed that the
	 * attribute is already part of the high level process.
	 * 
	 * @param attID
	 *            the ID of the attribute to be added
	 */
	public void addOutputDataAttribute(HLID attID) {
		if (!outputDataAttributes.keySet().contains(attID)) {
			outputDataAttributes.put(attID, TransformationType.Resample);
		}
	}

	// /**
	// * Checks whether the given data attribute is used for this
	// * activity.
	// * @param attID the ID of the attribute for which we want to check
	// * @return <code>true</code> if the data attribute is referenced,
	// * <code>false</code> otherwise
	// */
	// public boolean hasDataAttribute(HLID attID) {
	// if (inputDataAttributes.keySet().contains(attID) == true ||
	// outputDataAttributes.keySet().contains(attID) == true) {
	// return true;
	// } else {
	// return false;
	// }
	// }

	/**
	 * Checks whether the given input data attribute is used for this activity.
	 * 
	 * @param attID
	 *            the ID of the attribute for which we want to check
	 * @return <code>true</code> if the data attribute is referenced,
	 *         <code>false</code> otherwise
	 */
	public boolean hasInputDataAttribute(HLID attID) {
		if (inputDataAttributes.keySet().contains(attID) == true) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks whether the given output data attribute is used for this activity.
	 * 
	 * @param attID
	 *            the ID of the attribute for which we want to check
	 * @return <code>true</code> if the data attribute is referenced,
	 *         <code>false</code> otherwise
	 */
	public boolean hasOutputDataAttribute(HLID attID) {
		if (outputDataAttributes.keySet().contains(attID) == true) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Retrieves the list of data attributes available for this activity.
	 * 
	 * @return the list of HLAttribute objects representing the attributes
	 *         provided by this activity. If no data attributes have been set an
	 *         empty list will be returned
	 */
	// public List<HLID> getDataAttributeIDs() {
	// // return new ArrayList<HLID>(DataAttributes.keySet());
	// ArrayList<HLID> result = new ArrayList<HLID>();
	// Iterator<HLID> attIt = outputDataAttributes.keySet().iterator();
	// while (attIt.hasNext()) {
	// HLID id = process.getAttribute(attIt.next()).getID();
	// result.add(id);
	// }
	// Iterator<HLID> inAttIt = inputDataAttributes.keySet().iterator();
	// while (inAttIt.hasNext()) {
	// HLID id = process.getAttribute(attIt.next()).getID();
	// result.add(id);
	// }
	//
	// return result;
	//
	// }
	/**
	 * Retrieves the list of input data attributes available for this activity.
	 * 
	 * @return the list of HLAttribute objects representing the input attributes
	 *         provided by this activity. If no input data attributes have been
	 *         set an empty list will be returned
	 */
	public List<HLID> getInputDataAttributeIDs() {
		return new ArrayList<HLID>(inputDataAttributes.keySet());

	}

	/**
	 * Retrieves the list of output data attributes available for this activity.
	 * 
	 * @return the list of HLAttribute objects representing the output
	 *         attributes provided by this activity. If no output data
	 *         attributes have been set an empty list will be returned
	 */
	public List<HLID> getOutputDataAttributeIDs() {
		return new ArrayList<HLID>(outputDataAttributes.keySet());

	}

	/**
	 * Retrieves the list of data attributes available for this activity.
	 * 
	 * @return the list of HLAttribute objects representing the attributes
	 *         provided by this activity. If no data attributes have been set an
	 *         empty list will be returned
	 */
	// public List<HLAttribute> getDataAttributes() {
	// ArrayList<HLAttribute> result = new ArrayList<HLAttribute>();
	// Iterator<HLID> attIt = outputDataAttributes.keySet().iterator();
	// while (attIt.hasNext()) {
	// HLAttribute att = process.getAttribute(attIt.next());
	// result.add(att);
	// }
	// Iterator<HLID> inAttIt = inputDataAttributes.keySet().iterator();
	// while (inAttIt.hasNext()) {
	// HLAttribute att = process.getAttribute(attIt.next());
	// result.add(att);
	// }
	//
	// return result;
	// }
	/**
	 * Added by Mariska Netjes Retrieves the list of input data attributes
	 * available for this activity.
	 * 
	 * @return the list of HLAttribute objects representing the input attributes
	 *         provided by this activity. If no data attributes have been set an
	 *         empty list will be returned
	 */
	public List<HLAttribute> getInputDataAttributes() {
		ArrayList<HLAttribute> input = new ArrayList<HLAttribute>();
		Iterator<HLID> attIt = inputDataAttributes.keySet().iterator();
		while (attIt.hasNext()) {
			HLAttribute att = process.getAttribute(attIt.next());
			input.add(att);
		}
		return input;
	}

	/**
	 * Added by Mariska Netjes Retrieves the list of output data attributes
	 * available for this activity.
	 * 
	 * @return the list of HLAttribute objects representing the output
	 *         attributes provided by this activity. If no data attributes have
	 *         been set an empty list will be returned
	 */
	public List<HLAttribute> getOutputDataAttributes() {
		ArrayList<HLAttribute> output = new ArrayList<HLAttribute>();
		Iterator<HLID> attIt = outputDataAttributes.keySet().iterator();
		while (attIt.hasNext()) {
			HLAttribute att = process.getAttribute(attIt.next());
			output.add(att);
		}
		return output;
	}

	/**
	 * Specifies a set of input data attributes for this activity. <br>
	 * 
	 * @param map
	 *            the map with the ID of the attribute and its transformation
	 *            type
	 */
	public void setInputDataAttributes(HashMap<HLID, TransformationType> map) {
		inputDataAttributes = map;
	}

	/**
	 * Specifies a set of output data attributes for this activity. <br>
	 * 
	 * @param map
	 *            the map with the ID of the attribute and its transformation
	 *            type
	 */
	public void setOutputDataAttributes(HashMap<HLID, TransformationType> map) {
		outputDataAttributes = map;
	}

	/**
	 * Retrieves the transformation type for the given data attribute.
	 * 
	 * @param attID
	 *            the ID of the attribute for which the transformatin type is
	 *            requested
	 * @return the transformation type of the requested attribute. Can be
	 *         <code>null</code> if attribute not provided by this activity
	 */
	public TransformationType getTransformationType(HLID attID) {
		// return dataAttributes.get(attID);
		if (outputDataAttributes.containsKey(attID)) {
			return outputDataAttributes.get(attID);
		} else {
			return inputDataAttributes.get(attID);
		}
	}

	/**
	 * Specifies the transformation type for the given data attribute. <br>
	 * Adds the referenced attribute if not yet contained.
	 * 
	 * @param attID
	 *            the ID of the data attribute for which the new modification
	 *            type is provided
	 * @param type
	 *            the new transformation type of this data attribute
	 */
	public void setTransformationType(HLID attID, TransformationType modType) {
		if (outputDataAttributes.containsKey(attID)) {
			outputDataAttributes.put(attID, modType);
		}
		if (inputDataAttributes.containsKey(attID)) {
			inputDataAttributes.put(attID, modType);
		}
	}

	/**
	 * Retrieves the distribution of the execution time for this activity. <br>
	 * The execution time is defined as the time from the starting an activity
	 * until its completion.
	 * 
	 * @see #getWaitingTime()
	 * @see #getSojournTime()
	 * @see HLGlobal#getTimeUnit()
	 * 
	 * @return the distribution of the execution time. In the case that no
	 *         execution time has been set a default distribution will be
	 *         returned
	 */
	public HLDistribution getExecutionTime() {
		return executionTime;
	}

	/**
	 * Provides a distribution of the execution time for this activity. <br>
	 * The execution time is defined as the time from the starting an activity
	 * until its completion.
	 * 
	 * @see #setWaitingTime(HLDistribution)
	 * @see #setSojournTime(HLDistribution)
	 * @see HLGlobal#setTimeUnit()
	 * 
	 * @param dist
	 *            the new distribution of the execution time
	 */
	public void setExecutionTime(HLDistribution dist) {
		executionTime = dist;
	}

	/**
	 * Retrieves the distribution of the waiting time for this activity. <br>
	 * The waiting time is defined as the time from the point where an activity
	 * is enabled (that is, ready to be started) until its actual start.
	 * 
	 * @see #getExecutionTime()
	 * @see #getSojournTime()
	 * @see HLGlobal#getTimeUnit()
	 * 
	 * @return the distribution of the waiting time. In the case that no waiting
	 *         time has been set a default distribution will be returned
	 */
	public HLDistribution getWaitingTime() {
		return waitingTime;
	}

	/**
	 * Provides a distribution of the waiting time for this activity. <br>
	 * The waiting time is defined as the time from the point where an activity
	 * is enabled (that is, ready to be started) until its actual start.
	 * 
	 * @see #setExecutionTime(HLDistribution)
	 * @see #setSojournTime(HLDistribution)
	 * @see HLGlobal#setTimeUnit()
	 * 
	 * @param dist
	 *            the new distribution of the waiting time
	 */
	public void setWaitingTime(HLDistribution dist) {
		waitingTime = dist;
	}

	/**
	 * Retrieves the distribution of the sojourn time for this activity. <br>
	 * The sojourn time is defined as the waiting time + the execution time.
	 * 
	 * @see #getExecutionTime()
	 * @see #getWaitingTime()
	 * @see HLGlobal#getTimeUnit()
	 * 
	 * @return the distribution of the sojourn time. In the case that no sojourn
	 *         time has been set a default distribution will be returned
	 */
	public HLDistribution getSojournTime() {
		return sojournTime;
	}

	/**
	 * Provides a distribution of the sojourn time for this activity. <br>
	 * The sojourn time is defined as the waiting time + the execution time.
	 * 
	 * @see #setExecutionTime(HLDistribution)
	 * @see #setWaitingTime(HLDistribution)
	 * @see HLGlobal#setTimeUnit()
	 * 
	 * @param dist
	 *            the new distribution of the sojourn time
	 */
	public void setSojournTime(HLDistribution dist) {
		sojournTime = dist;
	}

	/**
	 * Retrieves the group for this activity. <br>
	 * Note that it is currently not possible to assign multiple groups to an
	 * activity.
	 * 
	 * @return the group of resources that may execute this activity if
	 *         specified. <code>Null</code> otherwise
	 */
	public HLGroup getGroup() {
		return process.getGroup(groupID);
	}

	/**
	 * Retrieves the ID of the group that is associated to this activity.
	 * 
	 * @return the group ID for the associated group of resources
	 */
	public HLID getGroupID() {
		return groupID;
	}

	/**
	 * Specifies a group for this activity. <br>
	 * Note that it is currently not possible to assign multiple groups to an
	 * activity.
	 * 
	 * @param groupID
	 *            the ID of the group of resources that may execute this
	 *            activity
	 */
	public void setGroup(HLID aGroupID) {
		groupID = aGroupID;
	}

	/**
	 * Removes the specified attribute from this activity. <br>
	 * Does nothing if not found.
	 * 
	 * @param id
	 *            the id of the attribute to be removed
	 * @return whether was found or not
	 */
	// public boolean removeDataAttribute(HLID id) {
	// if (outputDataAttributes.keySet().contains(id)) {
	// outputDataAttributes.remove(id);
	// return true;
	// }
	// if (inputDataAttributes.keySet().contains(id)) {
	// inputDataAttributes.remove(id);
	// return true;
	// } else {
	// return false; // attribute not found
	// }
	// }
	/**
	 * Removes the specified input attribute from this activity. <br>
	 * Does nothing if not found.
	 * 
	 * @param id
	 *            the id of the attribute to be removed
	 * @return whether was found or not
	 */
	public boolean removeInputDataAttribute(HLID id) {
		if (inputDataAttributes.keySet().contains(id)) {
			inputDataAttributes.remove(id);
			return true;
		} else {
			return false; // attribute not found
		}
	}

	/**
	 * Removes the specified output attribute from this activity. <br>
	 * Does nothing if not found.
	 * 
	 * @param id
	 *            the id of the attribute to be removed
	 * @return whether was found or not
	 */
	public boolean removeOutputDataAttribute(HLID id) {
		if (outputDataAttributes.keySet().contains(id)) {
			outputDataAttributes.remove(id);
			return true;
		} else {
			return false; // attribute not found
		}
	}

	/**
	 * Removes the given group from this activity. If the given group was not
	 * assigned to this activity in the first place, calling the method has no
	 * effect.
	 * 
	 * @param groupID
	 *            the ID of the group to be removed
	 * @return boolean <code>true</code> if the group was found and removed,
	 *         <code>false</code> otherwise.
	 */
	public boolean removeGroup(HLID aGroupID) {
		if (groupID != null && groupID.equals(aGroupID)) {
			groupID = process.nobodyHLID;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Indicates whether this activity is automatic. An activity is automatic
	 * when the nobody group has been attached to it.
	 * 
	 * @return boolean <code>true</code> if this activity is automatic,
	 *         <code>false</code> otherwise.
	 */
	public boolean isAutomatic() {
		return getGroupID() == process.getNobodyGroupID();
	}

	/**
	 * Makes a deep copy of this object while the ID remains the same. <br>
	 * Note that this method needs to be extended as soon as there are
	 * attributes added to the class which are not primitive or immutable.
	 */
	public Object clone() {
		HLActivity o = null;
		try {
			o = (HLActivity) super.clone();
			// clone the timing information
			o.executionTime = (HLDistribution) this.executionTime.clone();
			o.waitingTime = (HLDistribution) this.waitingTime.clone();
			o.sojournTime = (HLDistribution) this.sojournTime.clone();
			// clone the input data attributes
			o.inputDataAttributes = (HashMap<HLID, TransformationType>) inputDataAttributes
					.clone();
			// clone the output data attributes
			o.outputDataAttributes = (HashMap<HLID, TransformationType>) outputDataAttributes
					.clone();
			return o;
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
