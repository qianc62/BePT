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

public class ShowGratitude extends Behaviour {

	private HashMap<String, Double> repliesPerUser;
	private HashMap<String, Double> votesPerUser;
	private final static int VOTE_REPLY_RATIO = 2; // een vote telt

	// {votesRepliesRatio}
	// zwaarder mee dan een
	// reply

	public ShowGratitude(Trait tr) {
		super(tr);
	}

	@Override
	public void analyseAllUsers(Date startTime, Date endTime) {
		Message.add("SSA: Show Gratitude");
		repliesPerUser = new HashMap<String, Double>();
		votesPerUser = new HashMap<String, Double>();
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

		HashMap<String, Double> scorePerUser = bundleResults();

		calculateResults(scorePerUser);
	}

	@SuppressWarnings("unchecked")
	private void analayseAllNodes(LogReader log, Date beginTime, Date endTime) {
		Iterator<ProcessInstance> instance = log.instanceIterator();
		while (instance.hasNext()) {
			ProcessInstance p = instance.next();
			String nodeCreator = p.getAttributes().get("CreatedBy");
			Iterator<AuditTrailEntry> audits = p.getAuditTrailEntryList()
					.iterator();
			while (audits.hasNext()) {
				AuditTrailEntry au = audits.next();
				if (beginTime.before(au.getTimestamp())
						&& endTime.after(au.getTimestamp())) {
					// Tel het aantal reacties
					if (au.getName().equalsIgnoreCase("REACT")
							&& au.getType().equalsIgnoreCase("complete")) {
						// die door iemand gedaan zijn
						if (au.getDataAttributes().containsKey("Actor")) {
							String uid = au.getDataAttributes().get("Actor");
							// die niet de auteur van de node is
							if (!uid.equals(nodeCreator)) {
								if (repliesPerUser.containsKey(uid)) {
									Double v = repliesPerUser.remove(uid);
									repliesPerUser.put(uid, v + 1);
								} else {
									repliesPerUser.put(uid, new Double(1));
								}
							}
						}
					}
					// Tel het aantal stemmen
					if (au.getName().equalsIgnoreCase("VOTE")
							&& au.getType().equalsIgnoreCase("complete")) {
						// die door iemand zijn gedaan
						if (au.getDataAttributes().containsKey("Actor")
								&& au.getDataAttributes().containsKey("Actor")) {
							String uid = au.getDataAttributes().get("Actor");
							Integer vote = Integer.parseInt(au
									.getDataAttributes().get("Vote"));
							// die niet de auteur van de node is
							// && vote is > 50%
							if (!uid.equals(nodeCreator)
									&& vote.compareTo(50) == 1) {
								if (votesPerUser.containsKey(uid)) {
									Double v = votesPerUser.remove(uid);
									votesPerUser.put(uid, v + 1);
								} else {
									votesPerUser.put(uid, new Double(1));
								}
							}
						}
					}
				}
			}
		}
	}

	/*
	 * @SuppressWarnings("unchecked") private void analayseAllUsersExt(LogReader
	 * log, Date beginTime, Date endTime) { Iterator<ProcessInstance> instance =
	 * log.instanceIterator(); while (instance.hasNext()) { ProcessInstance p =
	 * instance.next(); String nodeCreator = p.getAttributes().get("CreatedBy");
	 * Iterator<AuditTrailEntry> audits = p.getAuditTrailEntryList()
	 * .iterator(); while (audits.hasNext()) { AuditTrailEntry au =
	 * audits.next(); if (beginTime.before(au.getTimestamp()) &&
	 * endTime.after(au.getTimestamp())) { // Tel het aantal reacties if
	 * (au.getName().equalsIgnoreCase("CREATE") &&
	 * au.getType().equalsIgnoreCase("complete")) { // die door iemand gedaan
	 * zijn if (au.getDataAttributes().containsKey("GenericType") &&
	 * au.getDataAttributes().get("GenericType").equals("comments")) { String
	 * uid = au.getOriginator(); if (repliesPerUser.containsKey(uid)) { Double r
	 * = repliesPerUser.remove(uid); repliesPerUser.put(uid, r + 1); } else {
	 * repliesPerUser.put(uid, 1.); } } } // Tel het aantal stemmen if
	 * (au.getName().equalsIgnoreCase("VOTE") &&
	 * au.getType().equalsIgnoreCase("complete")) { // die door iemand zijn
	 * gedaan if (au.getDataAttributes().containsKey("Node_By")) { String uid =
	 * au.getDataAttributes().get("Node_By"); Integer vote =
	 * Integer.parseInt(au.getDataAttributes().get("Vote")); // die niet de
	 * auteur van de node is // && vote is > 50% if (!uid.equals(nodeCreator) &&
	 * vote.compareTo(50) == 1) { if (votesPerUser.containsKey(uid)) { Double v
	 * = votesPerUser.remove(uid); votesPerUser.put(uid, v + 1); } else {
	 * votesPerUser.put(uid, 1.); } } } } } } } }
	 */
	private HashMap<String, Double> bundleResults() {
		Iterator<String> userIt;
		HashMap<String, Double> scorePerUser = new HashMap<String, Double>();

		userIt = repliesPerUser.keySet().iterator();
		while (userIt.hasNext()) {
			String uid = userIt.next();
			Double numberOf = repliesPerUser.get(uid);
			if (scorePerUser.containsKey(uid)) {
				Double v = scorePerUser.remove(uid);
				scorePerUser.put(uid, v + numberOf);
			} else {
				scorePerUser.put(uid, numberOf);
			}
		}

		userIt = votesPerUser.keySet().iterator();
		while (userIt.hasNext()) {
			String uid = userIt.next();
			Double numberOf = votesPerUser.get(uid);
			if (scorePerUser.containsKey(uid)) {
				Double v = scorePerUser.remove(uid);
				scorePerUser.put(uid, v + (VOTE_REPLY_RATIO * numberOf));
			} else {
				scorePerUser.put(uid, VOTE_REPLY_RATIO * numberOf);
			}
		}

		return scorePerUser;
	}
}
