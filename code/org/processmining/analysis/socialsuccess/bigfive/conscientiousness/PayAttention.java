package org.processmining.analysis.socialsuccess.bigfive.conscientiousness;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Trait;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

public class PayAttention extends Behaviour {
	HashMap<String, Double> scorePerUser = null;

	public PayAttention(Trait tr) {
		super(tr);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		HashMap<String, ArrayList<Double>> postTimesPerUser = new HashMap<String, ArrayList<Double>>();
		if (trait.getData().isProcessAvailable(PersonalityData.USER_ACTION)) {
			LogReader log = trait.getData().getProcess(
					PersonalityData.USER_ACTION);
			Iterator<ProcessInstance> instance = log.instanceIterator();
			while (instance.hasNext()) {
				Date formStart = null;
				ProcessInstance p = instance.next();
				String uid = p.getAttributes().get("userID");
				String prevName = "";
				Iterator<AuditTrailEntry> audits = p.getAuditTrailEntryList()
						.iterator();
				while (audits.hasNext()) {
					AuditTrailEntry au = audits.next();
					if (beginTime.before(au.getTimestamp())
							&& endTime.after(au.getTimestamp())) {
						if (au.getName().equalsIgnoreCase("CREATE")
								|| au.getName().equalsIgnoreCase("EDIT")
								|| au.getName().equalsIgnoreCase("SIGNUP")) {
							if (au.getType().equalsIgnoreCase("schedule")) {
								formStart = au.getTimestamp();
								prevName = au.getName();
							}
							if (au.getType().equalsIgnoreCase("complete")
									&& formStart != null
									&& prevName.equals(au.getName())) {
								ArrayList<Double> times;
								if (postTimesPerUser.containsKey(uid))
									times = postTimesPerUser.remove(uid);
								else
									times = new ArrayList<Double>();
								times
										.add((double) (au.getTimestamp()
												.getTime() - formStart
												.getTime()));
								postTimesPerUser.put(uid, times);
								formStart = null;
							}
						}
					}

				}
			}
			scorePerUser = new HashMap<String, Double>();
			Iterator<String> userIt = postTimesPerUser.keySet().iterator();
			while (userIt.hasNext()) {
				String uid = userIt.next();
				ArrayList<Double> times = postTimesPerUser.get(uid);
				// minder dan 3 nodes is niet relevant
				if (times.size() > 3) {
					Collections.sort(times);
					double median;
					// size is even
					if (times.size() % 2 == 0) {
						double median_l = times.get((times.size() / 2) - 1);
						double median_u = times.get(times.size() / 2);
						median = Math.round((median_l + median_u) / 2);
					} else { // size is odd
						median = times.get((times.size() + 1) / 2 - 1);
					}
					scorePerUser.put(uid, median);
				}
			}

			calculateResults(scorePerUser);
		} else {
			// TODO throw misschien eens een exception
		}
	}
}
