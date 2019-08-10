package org.processmining.analysis.socialsuccess.bigfive.openness;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.*;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;

public class HaveRichVocubalary extends Behaviour {
	// Openness

	public HaveRichVocubalary(Openness openness) {
		super(openness);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		Message.add("SSA: Have A Rich Vocubalary");
		// process 'NODES+TAGS' is nodig
		if (trait.getData().isProcessAvailable(PersonalityData.NODES_TAGS)) {
			LogReader log = trait.getData().getProcess(
					PersonalityData.NODES_TAGS);
			HashMap<String, HashSet<String>> tagsPerUser = new HashMap<String, HashSet<String>>();
			HashMap<String, Integer> nodesPerUser = new HashMap<String, Integer>();

			Iterator<ProcessInstance> instance = log.instanceIterator();
			while (instance.hasNext()) {
				ProcessInstance p = instance.next();
				// sla het aantal nodes per creator op
				String creater = p.getAttributes().get("CreatedBy");
				if (nodesPerUser.containsKey(creater)) {
					nodesPerUser.put(creater, nodesPerUser.get(creater) + 1);
				} else {
					nodesPerUser.put(creater, 1);
				}
				// maak entrylist iterator aan
				Iterator<AuditTrailEntry> audits = p.getAuditTrailEntryList()
						.iterator();
				while (audits.hasNext()) {
					AuditTrailEntry au = audits.next();
					if (beginTime.before(au.getTimestamp())
							&& endTime.after(au.getTimestamp())) {
						String uid = au.getAttributes().get("tagged_by");

						String tagDigest = trait.calculateDigest(au
								.getOriginator());
						if (tagsPerUser.containsKey(uid)) {
							HashSet<String> v = tagsPerUser.get(uid);
							if (!v.contains(tagDigest)) {
								v.add(tagDigest);
								tagsPerUser.put(uid, v);
							}
						} else {
							HashSet<String> v = new HashSet<String>();
							v.add(tagDigest);
							tagsPerUser.put(uid, v);
						}
					}
				}
			}
			// relatief ten opzichte van wat?

			HashMap<String, Double> scores = new HashMap<String, Double>();

			Iterator<String> it = tagsPerUser.keySet().iterator();
			while (it.hasNext()) {
				String user = it.next();
				if (tagsPerUser.get(user).size() > 1
						&& nodesPerUser.get(user) > 1) {
					Double s;
					if (nodesPerUser.get(user) == null) {
						s = 0.;
					} else {
						s = (double) tagsPerUser.get(user).size()
								/ nodesPerUser.get(user);
					}
					scores.put(user, s);
				}
			}
			calculateResults(scores);
		} else {
			// TODO I might throw an exception here
			return;
		}
	}
}
