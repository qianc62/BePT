package org.processmining.analysis.socialsuccess.bigfive.conscientiousness;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Trait;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

public class TidyUp extends Behaviour {

	public TidyUp(Trait tr) {
		super(tr);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		HashMap<String, Double> editsPerUser = new HashMap<String, Double>();
		HashMap<String, Double> lastEditPerUser = new HashMap<String, Double>();
		HashMap<String, Double> nodesPerUser = new HashMap<String, Double>();
		HashMap<String, Double> scorePerUser = new HashMap<String, Double>();
		boolean editFound = false;
		/*
		 * if
		 * (trait.getData().isProcessAvailable(SocialSuccessData.USER_ACTION)) {
		 * // welke users hebben er een tidyUp functionaliteit gebruikt // welke
		 * users hebben hoeveel wijzigingen aan hun posts gemaakt LogReader log
		 * = trait.getData().getProcess( SocialSuccessData.USER_ACTION);
		 * Iterator<ProcessInstance> instance = log.instanceIterator(); while
		 * (instance.hasNext()) { ProcessInstance p = instance.next(); String
		 * uid = p.getAttributes().get("User_ID"); Iterator<AuditTrailEntry>
		 * audits = p.getAuditTrailEntryList() .iterator(); while
		 * (audits.hasNext()) { AuditTrailEntry au = audits.next(); if
		 * (beginTime.before(au.getTimestamp()) &&
		 * endTime.after(au.getTimestamp())) { // tidy - up if
		 * (au.getName().equalsIgnoreCase("TIDY") &&
		 * au.getType().equalsIgnoreCase("complete")) { if
		 * (scorePerUser.containsKey(uid)) { Double v =
		 * scorePerUser.remove(uid); scorePerUser.put(uid, v +
		 * TIDY_CHANGE_RATIO); } else { scorePerUser.put(uid,
		 * TIDY_CHANGE_RATIO); } } if (au.getName().equalsIgnoreCase("FORM") &&
		 * au.getType().equalsIgnoreCase("complete") &&
		 * au.getDataAttributes().containsKey( "form_action") &&
		 * au.getDataAttributes().get("form_action") .equalsIgnoreCase("edit"))
		 * { if (scorePerUser.containsKey(uid)) { Double v =
		 * scorePerUser.remove(uid); scorePerUser.put(uid, v + 1); } else {
		 * scorePerUser.put(uid, 1.); } } } } } } else
		 */if (trait.getData().isProcessAvailable(
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
			Iterator<ProcessInstance> instance = log.instanceIterator();
			while (instance.hasNext()) {
				ProcessInstance p = instance.next();
				Iterator<AuditTrailEntry> audits = p.getAuditTrailEntryList()
						.iterator();
				if (p.getAttributes().containsKey("GenericType")
						&& p.getAttributes().get("GenericType").equals("node")
						&& p.getAttributes().containsKey("CreatedBy")) {
					String creator = p.getAttributes().get("CreatedBy");
					if (editsPerUser.containsKey(creator)) {
						nodesPerUser
								.put(creator, nodesPerUser.get(creator) + 1);
					} else {
						nodesPerUser.put(creator, 1.);
					}
					while (audits.hasNext()) {
						AuditTrailEntry au = audits.next();
						if (beginTime.before(au.getTimestamp())
								&& endTime.after(au.getTimestamp())) {
							if ((au.getName().equalsIgnoreCase("EDIT") || au
									.getName().equalsIgnoreCase("LAST_EDIT"))
									&& au.getType()
											.equalsIgnoreCase("complete")
									&& au.getAttributes().containsKey("Actor")) {
								String editor = au.getAttributes().get("Actor");
								if (creator.equals(editor)) {
									if (au.getName().equalsIgnoreCase(
											"LAST_EDIT")) {
										if (lastEditPerUser.containsKey(editor)) {
											lastEditPerUser
													.put(
															editor,
															lastEditPerUser
																	.get(editor) + 1);
										} else {
											lastEditPerUser.put(editor, 1.);
										}
									} else {
										if (editsPerUser.containsKey(editor)) {
											editsPerUser
													.put(editor, editsPerUser
															.get(editor) + 1);
										} else {
											editsPerUser.put(editor, 1.);
											editFound = true;
										}
									}
								}
							}
						}
					}
				}
			}
			HashSet<String> users;
			if (editFound)
				users = new HashSet<String>(editsPerUser.keySet());
			else
				users = new HashSet<String>(lastEditPerUser.keySet());
			Iterator<String> it = users.iterator();
			while (it.hasNext()) {
				String user = it.next();
				if (editFound) {
					scorePerUser.put(user, editsPerUser.get(user)
							/ nodesPerUser.get(user));
				} else {
					scorePerUser.put(user, lastEditPerUser.get(user)
							/ nodesPerUser.get(user));
				}
			}
		} else {
			// TODO misschien eens een exceptie throwen
			return;
		}
		calculateResults(scorePerUser);
	}
}
