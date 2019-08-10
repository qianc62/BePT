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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.framework.models.hlprocess.expr.HLDataExpression;

/**
 * Coordinates the high level elements for a process, whereas <i>high level</i>
 * is interpreted as being everything beyond the <i>pure control flow</i>. <br>
 * The purpose is to capture, e.g., performance, data, and organizational
 * characteristics of a process in a way that is independent of the concrete
 * process modeling language. <br>
 * Note that there may be many orthogonal, potentially conflicting, views on all
 * these characteristics (e.g., sojourn time of an activity includes execution
 * time). <br>
 * If the high level process information is to be related to elements in a
 * concrete process model then this can be done in the high level process
 * implementation for the corresponding process model type (e.g., HLPetriNet). <br>
 * Maintains consistency and enables cloning of the high level structure.
 * 
 * @see HighLevelModel
 * @see HLGlobal
 * @see HLActivity
 * @see HLChoice
 * @see HLAttribute
 * @see HLGroup
 * @see HLResource
 */
public class HLProcess implements Cloneable {

	/** Maps IDs onto activities, choices etc. */
	protected HashMap<HLID, HLActivity> activities = new HashMap<HLID, HLActivity>();
	protected HashMap<HLID, HLChoice> choices = new HashMap<HLID, HLChoice>();
	protected HashMap<HLID, HLAttribute> attributes = new HashMap<HLID, HLAttribute>();
	protected HashMap<HLID, HLGroup> groups = new HashMap<HLID, HLGroup>();
	protected HashMap<HLID, HLResource> resources = new HashMap<HLID, HLResource>();
	protected HLID anybodyHLID, nobodyHLID;
	/** Global information about the process */
	protected HLGlobal global = new HLGlobal(this);

	public static final String GROUP_ANYBODY = "Anybody";
	public static final String GROUP_NOBODY = "Nobody";

	/**
	 * Creates a new HLProcess structure.
	 * 
	 * @param name
	 *            the name of this HLProcess
	 */
	public HLProcess() {
		this("");
	}

	/**
	 * Creates a new HLProcess structure.
	 * 
	 * @param name
	 *            the name of this HLProcess
	 */
	public HLProcess(String name) {
		global.setName(name);
		anybodyHLID = (new HLGroup(GROUP_ANYBODY, this)).getID();
		nobodyHLID = (new HLGroup(GROUP_NOBODY, this)).getID();
	}

	// //////////////////////// Retrieval Methods ////////////////////////

	/**
	 * Retrieves the HLActivity object for the given ID.
	 * 
	 * @param id
	 *            the ID for the requested activity
	 */
	public HLActivity getActivity(HLID id) {
		return activities.get(id);
	}

	/**
	 * Retrieves all activities that belong to this process.
	 * 
	 * @return the activities
	 */
	public List<HLActivity> getActivities() {
		return new ArrayList<HLActivity>(activities.values());
	}

	/**
	 * Retrieves the HLChoice object for the given ID.
	 * 
	 * @param id
	 *            the ID for the requested choice
	 */
	public HLChoice getChoice(HLID id) {
		return choices.get(id);
	}

	/**
	 * Retrieves all choices that belong to this process.
	 * 
	 * @return the choices
	 */
	public List<HLChoice> getChoices() {
		return new ArrayList<HLChoice>(choices.values());
	}

	/**
	 * Retrieves the HLAttribute object for the given ID.
	 * 
	 * @param id
	 *            the ID for the requested attribute
	 */
	public HLAttribute getAttribute(HLID id) {
		return attributes.get(id);
	}

	/**
	 * Retrieves all attributes that belong to this process.
	 * 
	 * @return the attributes
	 */
	public Set<HLAttribute> getAttributes() {
		return new HashSet<HLAttribute>(attributes.values());
	}

	/**
	 * Retrieves the HLGroup object for the given ID.
	 * 
	 * @param id
	 *            the ID for the requested group
	 */
	public HLGroup getGroup(HLID id) {
		if (groups.containsKey(id)) {
			return groups.get(id);
		} else {
			return null;
		}
	}

	/**
	 * Retrieves the HLID of NobodyGroup.
	 */
	public HLID getNobodyGroupID() {
		return nobodyHLID;
	}

	/**
	 * Retrieves the HLID of NobodyGroup.
	 */
	public HLID getAnybodyGroupID() {
		return anybodyHLID;
	}

	/**
	 * Retrieves all groups that belong to this process. Note that this will not
	 * contain the "Nobody" or "Anybody" group.
	 * 
	 * @see #getAllGroups()
	 * @return the groups defined for this process
	 */
	public List<HLGroup> getGroups() {
		ArrayList<HLGroup> result = new ArrayList<HLGroup>();
		for (HLGroup group : groups.values()) {
			if (anybodyHLID.equals(group.getID())
					|| nobodyHLID.equals(group.getID())) {
				continue;
			}
			result.add(group);
		}
		return result;
	}

	/**
	 * Retrieves all groups that belong to this process, including "Anybody" and
	 * "Nobody" groups.
	 * 
	 * @see #getGroups()
	 * @return the groups defined for this process including "Anybody" and
	 *         "Nobody" groups
	 */
	public List<HLGroup> getAllGroups() {
		ArrayList<HLGroup> result = new ArrayList<HLGroup>();
		// put nobody and anybody on the first part of the list
		// (used for, e.g., displaying available groups in GUI)
		result.add(groups.get(nobodyHLID));
		result.add(groups.get(anybodyHLID));
		for (HLGroup group : groups.values()) {
			if (anybodyHLID.equals(group.getID())
					|| nobodyHLID.equals(group.getID())) {
				continue;
			}
			result.add(group);
		}
		return result;
	}

	/**
	 * Retrieves the HLResource object for the given ID.
	 * 
	 * @param id
	 *            the ID for the requested activity
	 */
	public HLResource getResource(HLID id) {
		return resources.get(id);
	}

	/**
	 * Retrieves all resources that belong to this process.
	 * 
	 * @return the resources
	 */
	public List<HLResource> getResources() {
		return new ArrayList<HLResource>(resources.values());
	}

	/**
	 * Returns the global information for this process.
	 * 
	 * @return the HLGlobal object for this HLProcess
	 */
	public HLGlobal getGlobalInfo() {
		return global;
	}

	// //////////////////////////// Add Methods //////////////////////////////

	/**
	 * Adds the given high level activity to this process and sets the reference
	 * to this high level process. <br>
	 * If an object with the same ID is already contained in the process, its
	 * reference to this high level process will be removed, and it will be
	 * replaced.
	 * 
	 * @param act
	 *            the new activity
	 */
	public void addOrReplace(HLActivity act) {
		if (activities.containsKey(act.getID())) {
			HLActivity old = activities.get(act.getID());
			old.process = null;
			act.process = this;
			activities.put(act.getID(), act);
		} else {
			act.process = this;
			activities.put(act.getID(), act);
		}
	}

	/**
	 * Adds the given high level activity to this process and sets the reference
	 * to this high level process. <br>
	 * If an object with the same name is already contained in the process, its
	 * reference to this high level process will be removed, and it will be
	 * replaced.
	 * 
	 * @param act
	 *            the new activity
	 */
	public void addOrReplaceByName(HLActivity newAct) {
		for (HLActivity oldAct : getActivities()) {
			if (oldAct.getName().equalsIgnoreCase(newAct.getName())) {
				activities.remove(oldAct.getID());
				activities.put(newAct.getID(), newAct);
				newAct.process = this;
			} else {
				newAct.process = this;
				activities.put(newAct.getID(), newAct);
			}
		}
	}

	/**
	 * Adds the given high level choice to this process and sets the reference
	 * to this high level process. <br>
	 * If an object with the same ID is already contained in the process, its
	 * reference to this high level process will be removed, and it will be
	 * replaced.
	 * 
	 * @param choice
	 *            the new choice
	 */
	public void addOrReplace(HLChoice choice) {
		if (choices.containsKey(choice.getID())) {
			HLChoice old = choices.get(choice.getID());
			old.process = null;
			choice.process = this;
			choices.put(choice.getID(), choice);
		} else {
			choice.process = this;
			choices.put(choice.getID(), choice);
		}
	}

	/**
	 * Adds the given high level attribute to this process and sets the
	 * reference to this high level process. <br>
	 * If an object with the same ID is already contained in the process, its
	 * reference to this high level process will be removed, and it will be
	 * replaced.
	 * 
	 * @param att
	 *            the new attribute
	 */
	public void addOrReplace(HLAttribute att) {
		if (attributes.containsKey(att.getID())) {
			HLAttribute old = attributes.get(att.getID());
			old.process = null;
			att.process = this;
			attributes.put(att.getID(), att);
		} else {
			att.process = this;
			attributes.put(att.getID(), att);
		}
	}

	/**
	 * Adds the given high level resource to this process and sets the reference
	 * to this high level process. <br>
	 * If an object with the same ID is already contained in the process, its
	 * reference to this high level process will be removed, and it will be
	 * replaced.
	 * 
	 * @param res
	 *            the new resource
	 */
	public void addOrReplace(HLResource res) {
		if (resources.containsKey(res.getID())) {
			HLResource old = resources.get(res.getID());
			old.process = null;
			res.process = this;
			resources.put(res.getID(), res);
		} else {
			res.process = this;
			resources.put(res.getID(), res);
			groups.get(anybodyHLID).addResource(res.getID());
		}
	}

	/**
	 * Adds a new high level group to this process process and sets the
	 * reference to this high level process. <br>
	 * If an object with the same ID is already contained in the process, its
	 * reference to this high level process will be removed, and it will be
	 * replaced.
	 * 
	 * @param grp
	 *            the new group
	 */
	public void addOrReplace(HLGroup grp) {
		if (groups.containsKey(grp.getID())) {
			HLGroup old = groups.get(grp.getID());
			old.process = null;
			grp.process = this;
			groups.put(grp.getID(), grp);
		} else {
			grp.process = this;
			groups.put(grp.getID(), grp);
		}
	}

	// //////////////////////////// Remove Methods ///////////////////////////

	/**
	 * Removes the specified attribute from this process and removes the
	 * attribute from activities if they refer to it.
	 * 
	 * @param id
	 *            the ID of the attribute that should be removed
	 * @return whether attribute was found or not
	 */
	public boolean removeAttribute(HLID id) {
		if (attributes.containsKey(id)) {
			attributes.remove(id);
			// also remove potential references from activities
			for (HLActivity act : activities.values()) {
				act.removeOutputDataAttribute(id);
				act.removeInputDataAttribute(id);
			}
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Removes the specified activity from this process and removes the activity
	 * from choices if they refer to it.
	 * 
	 * @param id
	 *            the ID of the activity that should be removed
	 * @return whether activity was found or not
	 */
	public boolean removeActivity(HLID id) {
		if (activities.containsKey(id)) {
			activities.remove(id);
			// also remove from choices in this process
			for (HLChoice choice : choices.values()) {
				choice.removeCondition(id);
			}
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Removes the specified choice from this process.
	 * 
	 * @param id
	 *            the ID of the choice that should be removed
	 * @return whether choice was found or not
	 */
	public boolean removeChoice(HLID id) {
		if (choices.containsKey(id)) {
			choices.remove(id);
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Removes the specified group from this process and removes the group from
	 * activities if they refer to it.
	 * 
	 * @see #removeGroupWithoutAffectingAssigmnets(HLID)
	 * @param id
	 *            the ID of the group that should be removed
	 * @return whether group was found or not
	 */
	public boolean removeGroup(HLID id) {
		if (groups.containsKey(id)) {
			groups.remove(id);
			// also remove potential references from activities
			for (HLActivity act : activities.values()) {
				if (act.getGroupID().equals(id)) {
					act.setGroup(nobodyHLID);
				}
			}
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Removes the specified group from this process but does not remove the
	 * group ID from activities if they refer to it.
	 * 
	 * @see #removeGroup(HLID)
	 * @param id
	 *            the ID of the group that should be removed
	 * @return whether group was found or not
	 */
	public boolean removeGroupWithoutAffectingAssigmnets(HLID id) {
		if (groups.containsKey(id)) {
			groups.remove(id);
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Removes the specified resource from this process and removes the resource
	 * from groups if they refer to it.
	 * 
	 * @param id
	 *            the ID of the resource that should be removed
	 * @return whether resource was found or not
	 */
	public boolean removeResource(HLID id) {
		if (resources.containsKey(id)) {
			resources.remove(id);
			// also remove potential references from groups
			for (HLGroup grp : groups.values()) {
				grp.removeResource(id);
			}
			groups.get(anybodyHLID).removeResource(id);
			return true;
		} else {
			return false; // not found
		}
	}

	// //////////////////////////// Replace Methods //////////////////////////

	/**
	 * Replaces the specified attribute by the given new attribute. <br>
	 * All the references at activities and data expressions will be updated.
	 * 
	 * @param oldAttID
	 *            the ID of the attribute that should be replaced
	 * @param newAtt
	 *            the new HLAttribute object
	 * @return whether specified attribute was found or not
	 */
	public boolean replaceAttribute(HLID oldAttID, HLAttribute newAtt) {
		if (attributes.containsKey(oldAttID)) {
			// update references at activities
			for (HLActivity act : activities.values()) {
				for (HLAttribute att : act.getOutputDataAttributes()) {
					// if attribute was registered with this activity
					if (att.getID() == oldAttID) {
						act.removeOutputDataAttribute(oldAttID);
						act.addOutputDataAttribute(newAtt.getID());
						continue;
					}
				}
				for (HLAttribute att : act.getInputDataAttributes()) {
					// if attribute was registered with this activity
					if (att.getID() == oldAttID) {
						act.removeInputDataAttribute(oldAttID);
						act.addInputDataAttribute(newAtt.getID());
						continue;
					}
				}

			}
			// update references at expressions
			for (HLChoice choice : choices.values()) {
				for (HLCondition cond : choice.getConditions()) {
					HLDataExpression condExpr = cond.getExpression();
					condExpr.replaceAttribute(oldAttID, newAtt.getID());
				}
			}
			attributes.remove(oldAttID);
			attributes.put(newAtt.getID(), newAtt);
			newAtt.process = this;
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Replaces the specified activity by the given new activity. <br>
	 * All the references at choices will be updated.
	 * 
	 * @param oldActID
	 *            the ID of the activity that should be replaced
	 * @param newAct
	 *            the new HLActivity object
	 * @return whether activity was found or not
	 */
	public boolean replaceActivity(HLID oldActID, HLActivity newAct) {
		if (activities.containsKey(oldActID)) {
			for (HLChoice ch : choices.values()) {
				ch.replaceChoiceTarget(oldActID, newAct.getID());
			}
			activities.remove(oldActID);
			activities.put(newAct.getID(), newAct);
			newAct.process = this;
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Replaces the specified choice by the given new choice.
	 * 
	 * @param oldChoiceID
	 *            the ID of the choice that should be replaced
	 * @param newChoice
	 *            the new HLChoice object
	 * @return whether choice was found or not
	 */
	public boolean replaceChoice(HLID oldChoiceID, HLChoice newChoice) {
		if (choices.containsKey(oldChoiceID)) {
			choices.remove(oldChoiceID);
			choices.put(newChoice.getID(), newChoice);
			newChoice.process = this;
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Replaces the specified group by the given new group. <br>
	 * All the references at activities will be updated.
	 * 
	 * @param oldGroupID
	 *            the ID of the group that should be replaced
	 * @param newGroup
	 *            the new HLGroup object
	 * @return whether group was found or not
	 */
	public boolean replaceGroup(HLID oldGroupID, HLGroup newGroup) {
		if (groups.containsKey(oldGroupID)) {
			for (HLActivity act : activities.values()) {
				if (act.getGroupID().equals(oldGroupID)) {
					act.setGroup(newGroup.getID());
				}
			}
			groups.remove(oldGroupID);
			groups.put(newGroup.getID(), newGroup);
			newGroup.process = this;
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Replaces the specified resource by the given new resource. <br>
	 * All the references at groups will be updated.
	 * 
	 * @param oldResID
	 *            the ID of the group that should be replaced
	 * @param newResource
	 *            the new HLResource object
	 * @return whether resource was found or not
	 */
	public boolean replaceResource(HLID oldResID, HLResource newResource) {
		if (resources.containsKey(oldResID)) {
			for (HLGroup grp : groups.values()) {
				for (HLResource res : grp.getResources()) {
					if (res.getID().equals(oldResID)) {
						grp.removeResource(oldResID);
						grp.addResource(newResource.getID());
					}
				}
			}
			resources.remove(oldResID);
			resources.put(newResource.getID(), newResource);
			newResource.process = this;
			return true;
		} else {
			return false; // not found
		}
	}

	/**
	 * Updates the HLID of the NobodyGroup. <br>
	 * NOTE: Do not call this method in other situations than when converting
	 * one type of high-level process to another. In this situation, the
	 * high-level process information of the source hlModel (including nobodyID)
	 * needs to be re-constructed in the target hlModel. However, normally the
	 * ID management should not be touched as this can have unexpected side
	 * effects.
	 */
	public void setNobodyGroupID(HLID id) {
		nobodyHLID = id;
	}

	/**
	 * Updates the HLID of the AnybodyGroup. <br>
	 * NOTE: Do not call this method in other situations than when converting
	 * one type of high-level process to another. In this situation, the
	 * high-level process information of the source hlModel (including
	 * anybodyID) needs to be re-constructed in the target hlModel. However,
	 * normally the ID management should not be touched as this can have
	 * unexpected side effects.
	 */
	public void setAnybodyGroupID(HLID id) {
		anybodyHLID = id;
	}

	// //////////////////////////// Convenience Methods
	// /////////////////////////

	/**
	 * Retrieves all the choices that are specified for the given target
	 * activity. <br>
	 * There may be more than one if the activity is involved in more than one
	 * choice.
	 * 
	 * @param actID
	 *            the ID of the activity for which the choices are requested
	 * @return the list of choice objects related to this activity. The list can
	 *         be empty
	 */
	public ArrayList<HLChoice> getChoicesForTargetActivity(HLID actID) {
		ArrayList<HLChoice> result = new ArrayList<HLChoice>();
		for (HLChoice choice : getChoices()) {
			HLCondition cond = choice.getCondition(actID);
			if (cond != null) {
				result.add(choice);
			}
		}
		return result;
	}

	/**
	 * Retrieves all those activities that use the given attribute.
	 * 
	 * @param attID
	 *            the ID of the attribute for which the activities are requested
	 * @return the list of activity objects using this data attribute. The list
	 *         can be empty
	 */
	public ArrayList<HLActivity> getActivitiesForAttribute(HLID attID) {
		ArrayList<HLActivity> result = new ArrayList<HLActivity>();
		for (HLActivity act : getActivities()) {
			if (act.hasInputDataAttribute(attID) == true) {
				result.add(act);
			}
			if (act.hasOutputDataAttribute(attID) == true) {
				result.add(act);
			}
		}
		return result;
	}

	/**
	 * Retrieves all those activities that use the given attribute as input.
	 * 
	 * @param attID
	 *            the ID of the attribute for which the activities are requested
	 * @return the list of activity objects using this data attribute as input.
	 *         The list can be empty
	 */
	public ArrayList<HLActivity> getActivitiesForInputAttribute(HLID attID) {
		ArrayList<HLActivity> result = new ArrayList<HLActivity>();
		for (HLActivity act : getActivities()) {
			if (act.hasInputDataAttribute(attID) == true) {
				result.add(act);
			}
		}
		return result;
	}

	/**
	 * Retrieves all those activities that use the given attribute as output.
	 * 
	 * @param attID
	 *            the ID of the attribute for which the activities are requested
	 * @return the list of activity objects using this data attribute as output.
	 *         The list can be empty
	 */
	public ArrayList<HLActivity> getActivitiesForOutputAttribute(HLID attID) {
		ArrayList<HLActivity> result = new ArrayList<HLActivity>();
		for (HLActivity act : getActivities()) {
			if (act.hasOutputDataAttribute(attID) == true) {
				result.add(act);
			}
		}
		return result;
	}

	/*
	 * Returns the first found group matching the given name. <p>Keep in mind
	 * that this is not deterministic as soon as there are more than one group
	 * with this name in the process
	 * 
	 * @param name the name to match
	 * 
	 * @return the first group with the same name if found, <code>null</code>
	 * otherwise
	 */
	public HLGroup findGroupByName(String name) {
		for (HLGroup result : getGroups()) {
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/*
	 * Returns the first found attribute matching the given name. <p>Keep in
	 * mind that this is not deterministic as soon as there are more than one
	 * attribute with this name in the process
	 * 
	 * @param name the name to match
	 * 
	 * @return the first attribute with the same name if found,
	 * <code>null</code> otherwise
	 */
	public HLAttribute findAttributeByName(String name) {
		for (HLAttribute result : getAttributes()) {
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the first found activity matching the given name.
	 * <p>
	 * Keep in mind that this is not deterministic as soon as there are more
	 * than one activity with this name in the process
	 * 
	 * @param name
	 *            the name to match
	 * @return the first activity with the same name if found, <code>null</code>
	 *         otherwise
	 */
	public HLActivity findActivityByName(String name) {
		for (HLActivity result : getActivities()) {
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the first found choice matching the given name.
	 * <p>
	 * Keep in mind that this is not deterministic as soon as there are more
	 * than one choice with this name in the process
	 * 
	 * @param name
	 *            the name to match
	 * @return the first choice with the same name if found, <code>null</code>
	 *         otherwise
	 */
	public HLChoice findChoiceByName(String name) {
		for (HLChoice result : getChoices()) {
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Removes all groups that are never assigned to any activity in the process
	 * and all resources that do not belong to any group.
	 */
	public void removeUnassignedGroupsAndResources() {
		// first check for unused groups
		for (HLGroup hlGrp : getGroups()) {
			boolean usedSomewhere = false;
			for (HLActivity hlAct : getActivities()) {
				if (hlAct.getGroupID().equals(hlGrp.getID())) {
					usedSomewhere = true;
				}
			}
			if (usedSomewhere == false) {
				// assignments do not need to be checked again
				removeGroupWithoutAffectingAssigmnets(hlGrp.getID());
			}
		}
		// now remove all resources that do not belong to a group
		for (HLResource hlRes : getResources()) {
			boolean usedSomewhere = false;
			for (HLGroup hlGrp : getGroups()) {
				if (hlGrp.getResourceIDs().contains(hlRes.getID())) {
					usedSomewhere = true;
				}
			}
			if (usedSomewhere == false) {
				removeResource(hlRes.getID());
			}
		}
	}

	// //////////////////////////// General Purpose Methods //////////////////

	/**
	 * Makes a deep copy of this object. <br>
	 * Note that this method needs to be extended as soon as there are
	 * attributes added to the class which are not primitive or immutable.
	 */
	public Object clone() {
		HLProcess o = null;
		try {
			o = (HLProcess) super.clone();
			// clone global information
			o.global = (HLGlobal) global.clone();
			// make deep copy of activity objects
			o.activities = (HashMap<HLID, HLActivity>) activities.clone();
			for (HLActivity act : activities.values()) {
				HLActivity clonedAct = (HLActivity) act.clone();
				clonedAct.process = o;
				o.activities.put(clonedAct.getID(), clonedAct);
			}
			// make deep copy of choice objects
			o.choices = (HashMap<HLID, HLChoice>) choices.clone();
			for (HLChoice choice : choices.values()) {
				HLChoice clonedChoice = (HLChoice) choice.clone();
				clonedChoice.process = o;
				o.choices.put(clonedChoice.getID(), clonedChoice);
			}
			// make deep copy of attribute objects
			o.attributes = (HashMap<HLID, HLAttribute>) attributes.clone();
			for (HLAttribute att : attributes.values()) {
				HLAttribute clonedAtt = (HLAttribute) att.clone();
				clonedAtt.process = o;
				o.attributes.put(clonedAtt.getID(), clonedAtt);
			}
			// make deep copy of resource objects
			o.resources = (HashMap<HLID, HLResource>) resources.clone();
			for (HLResource res : resources.values()) {
				HLResource clonedRes = (HLResource) res.clone();
				clonedRes.process = o;
				o.resources.put(clonedRes.getID(), clonedRes);
			}
			// make deep copy of group objects
			o.groups = (HashMap<HLID, HLGroup>) groups.clone();
			for (HLGroup grp : groups.values()) {
				HLGroup clonedGrp = (HLGroup) grp.clone();
				clonedGrp.process = o;
				o.groups.put(clonedGrp.getID(), clonedGrp);
			}
			return o;
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
