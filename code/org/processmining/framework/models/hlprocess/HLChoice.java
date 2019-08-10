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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.processmining.framework.models.hlprocess.HLTypes.ChoiceEnum;

/**
 * Holds information with respect to a decision point in the process. <br>
 * Note that a choice is defined independently of any concrete choice node in
 * the context of a process model (e.g., an XOR connector in an EPC model). It
 * only specifies a set of target activities that might be enabled by this
 * choice, and enabling conditions for each of them. <br>
 * If the choice is to be connected to a specific node in a process model, then
 * this can be done in the high level process implementation for the
 * corresponding process model type (e.g., a Place for the HighLevelPetriNet).
 * 
 * @see HLCondition
 * @see HighLevelModel#findChoice
 * @see HighLevelModel#findModelGraphVertexForChoice
 * @see HLProcess
 */
public class HLChoice extends HLProcessElement implements Cloneable {

	/**
	 * maps the activities that might be enabled by this choice to their
	 * enabling conditions
	 */
	protected HashMap<HLID, HLCondition> conditions = new HashMap<HLID, HLCondition>();

	// TODO Anne: move the configuration to the cpn export (i.e., out of the
	// high level process)
	/** the choice configuration */
	protected ChoiceEnum myConfiguration;

	/**
	 * Default constructor (automatically registers the new choice for the given
	 * high level process).
	 * 
	 * @param name
	 *            the name for this choice
	 * @param aProc
	 *            the high level process this choice belongs to
	 */
	public HLChoice(String aName, HLProcess aProc) {
		super(aName, aProc);
		process.choices.put(getID(), this);
		myConfiguration = ChoiceEnum.DATA;
	}

	/**
	 * Retrieves the conditions for the given activity.
	 * 
	 * @param actID
	 *            the ID of the activity for which the pre-conditions are
	 *            requested
	 * @return the condition object if activity involved in this choice.
	 *         <code>Null</code> otherwise
	 */
	public HLCondition getCondition(HLID actID) {
		return conditions.get(actID);
	}

	/**
	 * Retrieves the activity that is the target for the given condition.
	 * 
	 * @param cond
	 *            the condition for which the target is requested
	 * @return the activity that is associated to this condition.
	 *         <code>Null</code> if not found
	 */
	public HLActivity getTarget(HLCondition cond) {
		for (Entry<HLID, HLCondition> entry : conditions.entrySet()) {
			if (entry.getValue() == cond) {
				HLID actID = entry.getKey();
				return process.getActivity(actID);
			}
		}
		return null;
	}

	/**
	 * Retrieves all condition objects for this choice.
	 * 
	 * @return the conditions
	 */
	public Collection<HLCondition> getConditions() {
		return conditions.values();
	}

	/**
	 * Retrieves the high-level activities that may be activated by this choice
	 * (activation can range from activating 0 to all, depending on the actual
	 * conditions).
	 * 
	 * @return all activities that may be activated
	 */
	public Collection<HLActivity> getChoiceTargets() {
		ArrayList<HLActivity> result = new ArrayList<HLActivity>();
		for (HLID id : conditions.keySet()) {
			result.add(process.getActivity(id));
		}
		return result;
	}

	/**
	 * Retrieves the IDs of the high-level activities that may be activated by
	 * this choice (activation can range from activating 0 to all, depending on
	 * the actual conditions).
	 * 
	 * @return the IDs of all activities that may be activated
	 */
	public Collection<HLID> getChoiceTargetIDs() {
		return conditions.keySet();
	}

	/**
	 * Replaces the given old target activity ID by the new ID. The condition
	 * information stays the same, and will be from now related to the new high
	 * level activity for this choice. <br>
	 * If the old target activity is not part of this choice, the method has no
	 * effect.
	 * 
	 * @param oldTarget
	 *            the previous condition target
	 * @param newTarget
	 *            the new condition target
	 */
	public void replaceChoiceTarget(HLID oldTarget, HLID newTarget) {
		if (conditions.containsKey(oldTarget)) {
			HLCondition cond = conditions.get(oldTarget);
			conditions.remove(oldTarget);
			conditions.put(newTarget, cond);
		}
	}

	/**
	 * Removes the condition for the specified target activity. <br>
	 * If the specified activity was never a target for this choice, the method
	 * has no effect.
	 * 
	 * @param target
	 *            the ID of the given target activity
	 */
	public void removeCondition(HLID target) {
		conditions.remove(target);
	}

	/**
	 * Adds a new condition for the specified activity. <br>
	 * Note that if the activity is already part of this choice, calling this
	 * method has no effect.
	 * 
	 * @param actID
	 *            the ID of the activity associated with this choice target
	 *            (branch)
	 * @return either the newly created condition object, or the existing one
	 */
	public HLCondition addChoiceTarget(HLID actID) {
		HLCondition result;
		if (conditions.containsKey(actID) == false) {
			result = new HLCondition(this);
			conditions.put(actID, result);
			return result;
		} else {
			return conditions.get(actID);
		}
	}

	// TODO Anne: move the configuration to the cpn export (i.e., out of the
	// high level process)
	/**
	 * Indicates which kind of information should be used in the simulation
	 * model to choose for one of the alternative paths for this choice node.
	 * 
	 * @return the configuration selected for this choice
	 */
	public ChoiceEnum getChoiceConfiguration() {
		return myConfiguration;
	}

	// TODO Anne: move the configuration to the cpn export (i.e., out of the
	// high level process)
	/**
	 * Determines the preferred way to resolve this choice situation.
	 * 
	 * @param aChoice
	 *            the preferred choice configuration
	 */
	public void setChoiceConfiguration(ChoiceEnum aChoice) {
		myConfiguration = aChoice;
	}

	/**
	 * Makes a deep copy of this object while the ID remains the same. <br>
	 * Note that this method needs to be extended as soon as there are
	 * attributes added to the class which are not primitive or immutable.
	 */
	public Object clone() {
		HLChoice o = null;
		try {
			o = (HLChoice) super.clone();
			// make a deep copy of the conditions
			o.conditions = (HashMap<HLID, HLCondition>) conditions.clone();
			for (Entry<HLID, HLCondition> entry : conditions.entrySet()) {
				HLCondition clonedCond = (HLCondition) entry.getValue().clone();
				o.conditions.put(entry.getKey(), clonedCond);
			}
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
