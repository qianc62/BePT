package org.processmining.analysis.eventmodelmerge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.processmining.framework.models.activitygraph.ActivityGraph;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLTypes.EventType;
import org.processmining.framework.models.hlprocess.hlmodel.HLActivitySet;

/**
 * For the provided activityset which is based on events, a new empy activityset
 * will be generated which will be based on activities. That means that the
 * events that refer to the same activity are merged into one activity. Also a
 * mapping will be generated for which highlevelactivities, that refer to an
 * event, are merged into which highlevelactivity that refers to an activity.
 * 
 * @author rmans
 */
public class MergeActivitySetIntoActivityModel {

	/**
	 * The activity set that contains events
	 */
	private HLActivitySet activitySetWithEvents;

	/**
	 * the mapping from activities in the real activity model to the activities
	 * in the activityset that refers to events and that are merged into that
	 * activity in the real activity model
	 */
	private HashMap<HLID, ArrayList<HLID>> mapping = new HashMap<HLID, ArrayList<HLID>>();

	/**
	 * Basic constructor
	 * 
	 * @param activitySet
	 *            HLActivitySet the activity set that needs to be merged into an
	 *            activity set with only activities.
	 */
	public MergeActivitySetIntoActivityModel(HLActivitySet activitySet) {
		activitySetWithEvents = activitySet;
	}

	/**
	 * Merges the provided activity set into an activityset that only refers to
	 * activities.
	 * 
	 * @return HLActivitySet the activity set that only refers to activities.
	 *         The provided activityset is a newly created object which only
	 *         contains default information. So no information is transferred
	 *         from the activity set that refers to events to the activity set
	 *         that refers to activities.
	 */
	public HLActivitySet mergeActivitySetIntoActivityModel() {
		HLActivitySet activitySetWithActivities = new HLActivitySet(
				new ActivityGraph());
		Iterator hlActs = activitySetWithEvents.getHLProcess().getActivities()
				.iterator();
		while (hlActs.hasNext()) {
			HLActivity hlAct = (HLActivity) hlActs.next();
			String nameHLAct = hlAct.getName();
			// check whether the name of the hlactivity ends with start,
			// schedule or complete
			EventType[] evtTypes = EventType.values();
			boolean evtTypeExists = false;
			int lastIndex = 0;
			for (int i = 0; i < evtTypes.length; i++) {
				if (nameHLAct.endsWith(evtTypes[i].toString())) {
					evtTypeExists = true;
					// get the prefix with trailing whitespaces
					lastIndex = nameHLAct.lastIndexOf(evtTypes[i].toString());
					String activityName = (nameHLAct
							.substring(0, lastIndex - 1)).trim();
					// look whether already a hlactivity exists with this name
					boolean activityExists = false;
					Iterator<Entry<HLID, ArrayList<HLID>>> it = mapping
							.entrySet().iterator();
					while (it.hasNext()) {
						Entry<HLID, ArrayList<HLID>> entry = it.next();

						// TODO Anne: check whether should not be obtained from
						// 'activitySetWithActivities'
						// --> don't understand why this is added here at all
						// Minseok changed the code. (activitySetWithEvents -->
						// activitySetWithActivities
						HLActivity key = activitySetWithActivities
								.getHLProcess().getActivity(entry.getKey());

						if (key != null && key.getName().equals(activityName)) {
							// already an activity exists with this name
							// add the hlAct to the values of this entry
							entry.getValue().add(hlAct.getID());
							activityExists = true;
						}
					}

					if (!activityExists) {
						// create a hlactivity with the activity name and add it
						// to the model
						HLActivity newAct = new HLActivity(activityName,
								activitySetWithActivities.getHLProcess());
						// add this to the mapping
						ArrayList<HLID> mergedActivities = new ArrayList<HLID>();
						mergedActivities.add(hlAct.getID());
						mapping.put(newAct.getID(), mergedActivities);
					}
					break;
				}
			}

			if (!evtTypeExists) {
				// this is an activity on its own
				// create a new activity for this with the same name and add it
				// to the model
				HLActivity newAct = new HLActivity(nameHLAct,
						activitySetWithActivities.getHLProcess());
				// add this to the mapping
				ArrayList<HLID> mergedActivities = new ArrayList<HLID>();
				mergedActivities.add(hlAct.getID());
				mapping.put(newAct.getID(), mergedActivities);
			}
		}
		Iterator itr = activitySetWithActivities.getHLProcess().getGroups()
				.iterator();
		while (itr.hasNext()) {
			System.out.println("name_____ = "
					+ ((HLGroup) itr.next()).getName());
		}

		return activitySetWithActivities;
	}

	/**
	 * Retrieves the mapping of which highlevelactivities that refer to events
	 * are merged into a highlevelactivity that refer to an activity.
	 * 
	 * @return HashMap the mapping from the highlevelactivities of the
	 *         activityset and that refer to activities, that is merged into an
	 *         activitymodel, to the highlevel activities in the provided
	 *         activityset, and that refer to an event, that needed to be merged
	 *         into an activity model.
	 */
	public HashMap<HLID, ArrayList<HLID>> getMapping() {
		return mapping;
		// TODO Anne: check why copied??
		// HashMap<String, ArrayList<String>> returnHashMap = new
		// HashMap<String, ArrayList<String>>();
		// Iterator<Entry<String, ArrayList<String>>> it =
		// mapping.entrySet().iterator();
		// while (it.hasNext()) {
		// Entry entry = it.next();
		// HLActivity castKey = (HLActivity) entry.getKey();
		// ArrayList<String> castValue = (ArrayList<String>) entry.getValue();
		// returnHashMap.put(castKey.getID(), castValue);
		// }
		// return returnHashMap;
	}
}
