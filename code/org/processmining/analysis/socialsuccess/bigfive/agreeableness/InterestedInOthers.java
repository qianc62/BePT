/**
 * 
 */
package org.processmining.analysis.socialsuccess.bigfive.agreeableness;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Trait;
import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;

/**
 * @author MvanWingerden
 * 
 */
public class InterestedInOthers extends Behaviour {
	protected HashMap<String, Double> viewedProfilesPerUser = null;

	/**
	 * @param tr
	 */
	public InterestedInOthers(Trait tr) {
		super(tr);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date startTime, Date endTime) {
		Message.add("SSA: Interested In Others");
		// process 'USER+ACTION' is nodig
		if (trait.getData().isProcessAvailable(PersonalityData.USER_ACTION)) {
			LogReader log = trait.getData().getProcess(
					PersonalityData.USER_ACTION);
			viewedProfilesPerUser = new HashMap<String, Double>();

			Iterator<ProcessInstance> instance = log.instanceIterator();
			while (instance.hasNext()) {
				ProcessInstance p = instance.next();
				String uid = p.getAttributes().get("userID");
				Iterator<AuditTrailEntry> audits = p.getAuditTrailEntryList()
						.iterator();
				while (audits.hasNext()) {
					AuditTrailEntry au = audits.next();
					if (startTime.before(au.getTimestamp())
							&& endTime.after(au.getTimestamp())
							&& au.getName().equalsIgnoreCase("PROFILE")
							&& au.getType().equalsIgnoreCase("complete")
							&& au.getDataAttributes().containsKey("userID")) {
						int profileID = getIntFromUID(au.getDataAttributes()
								.get("userID"));
						int userID = getIntFromUID(uid);
						if (userID != profileID) {
							if (viewedProfilesPerUser.containsKey(uid)) {
								viewedProfilesPerUser.put(uid,
										viewedProfilesPerUser.get(uid) + 1);
							} else {
								viewedProfilesPerUser.put(uid, 1.);
							}
						}
					}
				}
			}
			// Spreiding is exponentieel
			/*
			 * HashMap<String, Double> scorePerUser = new HashMap<String,
			 * Double>(); Iterator<String> it =
			 * viewedProfilesPerUser.keySet().iterator(); while (it.hasNext()){
			 * String user = it.next(); scorePerUser.put(user,
			 * Math.log1p(viewedProfilesPerUser.get(user))); }
			 */
			calculateResults(viewedProfilesPerUser);
		} else {
			// TODO I might throw an exception here
			return;
		}
	}

	public int getIntFromUID(String uid) {
		if (uid.contains("-")) {
			// bijvoorbeeld "2351-martinvw"
			return Integer.parseInt(uid.subSequence(0, uid.indexOf("-"))
					.toString());
		} else if (uid.contains("U_")) {
			// bijvoorbeeld "U_2351"
			return Integer.parseInt(uid.substring(2));
		}
		return 0;
	}
}
