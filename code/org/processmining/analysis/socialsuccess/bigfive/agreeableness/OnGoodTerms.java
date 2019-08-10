/**
 * 
 */
package org.processmining.analysis.socialsuccess.bigfive.agreeableness;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Trait;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;

/**
 * @author MvanWingerden
 * 
 */
public class OnGoodTerms extends Behaviour {

	private HashMap<String, Double> repliedToPerUser;

	/**
	 * @param tr
	 */
	public OnGoodTerms(Trait tr) {
		super(tr);
	}

	@Override
	public void analyseAllUsers(Date startTime, Date endTime) {
		Message.add("SSA: On Good Terms");
		repliedToPerUser = new HashMap<String, Double>();
		LogReader log;
		if (trait.getData().isProcessAvailable(
				PersonalityData.NODE_ACTION_REFER_ORIG)) {
			log = trait.getData().getProcess(
					PersonalityData.NODE_ACTION_REFER_ORIG);
			analayseAllNodes(log, startTime, endTime);
		} else if (trait.getData().isProcessAvailable(
				PersonalityData.NODE_ACTION_USER_ORIG)) {
			log = trait.getData().getProcess(
					PersonalityData.NODE_ACTION_USER_ORIG);
			analayseAllNodes(log, startTime, endTime);
		} else {
			return;
		}

		calculateResults(repliedToPerUser);
	}

	@SuppressWarnings("unchecked")
	private void analayseAllNodes(LogReader log, Date startTime, Date endTime) {
		Iterator<ProcessInstance> instance = log.instanceIterator();
		HashMap<String, HashMap<String, Integer>> repliedTo = new HashMap<String, HashMap<String, Integer>>();
		while (instance.hasNext()) {
			ProcessInstance p = instance.next();
			String nodeCreator = p.getAttributes().get("CreatedBy");
			Iterator<AuditTrailEntry> audits = p.getAuditTrailEntryList()
					.iterator();
			while (audits.hasNext()) {
				AuditTrailEntry au = audits.next();
				if (startTime.before(au.getTimestamp())
						&& endTime.after(au.getTimestamp())
						&& au.getName().equalsIgnoreCase("REACT")
						&& au.getType().equalsIgnoreCase("complete")) {
					if (au.getDataAttributes().containsKey("Actor")
							&& !au.getDataAttributes().get("Actor").equals(
									nodeCreator)) {
						String uid = au.getDataAttributes().get("Actor");
						if (repliedTo.containsKey(uid)) {
							HashMap<String, Integer> v = repliedTo.remove(uid);
							if (v.containsKey(nodeCreator)) {
								Integer r = v.remove(nodeCreator);
								v.put(nodeCreator, r + 1);
							} else {
								v.put(nodeCreator, 1);
							}
							repliedTo.put(uid, v);
						} else {
							HashMap<String, Integer> v = new HashMap<String, Integer>();
							v.put(nodeCreator, 1);
							repliedTo.put(uid, v);
						}
					}
				}
			}
		}
		Iterator<String> keys = repliedTo.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			repliedToPerUser.put(key, (double) repliedTo.get(key).size());
		}
	}
}
