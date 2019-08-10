package org.processmining.analysis.socialsuccess.bigfive.openness;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Openness;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

public class ExcellentIdeas extends Behaviour {
	// Openness

	public ExcellentIdeas(Openness openness) {
		super(openness);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		if (trait.getData().isProcessAvailable(
				PersonalityData.USER_USER_RELATION)) {
			LogReader log = trait.getData().getProcess(
					PersonalityData.USER_USER_RELATION);
			HashMap<String, HashSet<String>> subscribersPerUser = new HashMap<String, HashSet<String>>();
			Iterator<ProcessInstance> it = log.getInstances().iterator();
			while (it.hasNext()) {
				ProcessInstance p = it.next();
				String user = p.getDataAttributes().get("userID");
				Iterator<AuditTrailEntry> audits = p.getAuditTrailEntryList()
						.iterator();
				while (audits.hasNext()) {
					AuditTrailEntry au = audits.next();
					if (endTime.after(au.getTimestamp())
							&& au.getName().equalsIgnoreCase("SUBSCRIBED_BY")) {
						String subscriber = au.getDataAttributes().get(
								"subscriber");
						if (subscribersPerUser.containsKey(user)) {
							subscribersPerUser.get(user).add(subscriber);
						} else {
							HashSet<String> t = new HashSet<String>();
							t.add(subscriber);
							subscribersPerUser.put(user, t);
						}
					}
				}
			}

			HashMap<String, Double> scorePerUser = new HashMap<String, Double>();
			Iterator<String> userIt = subscribersPerUser.keySet().iterator();
			while (userIt.hasNext()) {
				String uid = userIt.next();
				scorePerUser.put(uid, (double) subscribersPerUser.get(uid)
						.size());
			}
			calculateResults(scorePerUser);
		} else if (trait.getData().isProcessAvailable(
				PersonalityData.NODE_ACTION_REFER_ORIG)
				|| trait.getData().isProcessAvailable(
						PersonalityData.NODE_ACTION_USER_ORIG)) {
			LogReader log;
			if (trait.getData().isProcessAvailable(
					PersonalityData.NODE_ACTION_REFER_ORIG)) {
				log = trait.getData().getProcess(
						PersonalityData.NODE_ACTION_REFER_ORIG);
			} else {
				log = trait.getData().getProcess(
						PersonalityData.NODE_ACTION_USER_ORIG);
			}

			HashMap<String, Double> viewedPerCreator = new HashMap<String, Double>();
			Iterator<ProcessInstance> it = log.getInstances().iterator();
			while (it.hasNext()) {
				ProcessInstance p = it.next();
				DataSection d = p.getDataAttributes();
				if (d.containsKey("GenericType")
						&& d.get("GenericType").equals("node")) {
					if (d.containsKey("CreatedBy") && d.containsKey("Views")) {
						String author = d.get("CreatedBy");
						Double views = Double.parseDouble(d.get("Views"));
						if (viewedPerCreator.containsKey(author)) {
							Double prev = viewedPerCreator.remove(author);
							viewedPerCreator.put(author, prev + views);
						} else {
							viewedPerCreator.put(author, views);
						}
					}
				}
			}
			calculateResults(viewedPerCreator);
		}
	}
}
