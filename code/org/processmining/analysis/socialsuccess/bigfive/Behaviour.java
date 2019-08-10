package org.processmining.analysis.socialsuccess.bigfive;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.socialsuccess.BigFive;
import org.processmining.analysis.socialsuccess.clustering.KMeans1D;

public abstract class Behaviour {
	protected Trait trait;
	private Date pStartTime = null;
	private Date pEndTime = null;
	private ArrayList<HashMap<String, Double>> clusters = null;
	private HashMap<String, Double> endScores;
	private final static int LOWER_CLUSTER = 0;
	private final static int MIDDLE_L_CLUSTER = 1;
	private final static int MIDDLE_U_CLUSTER = 2;
	private final static int UPPER_CLUSTER = 3;

	public Behaviour(Trait tr) {
		this.trait = tr;
	}

	/**
	 * Get analysis results for a specific user.
	 * 
	 * @param UserIdentifier
	 * @return the score for this behaviour.
	 */
	final public double getAnalysis(String userIdentifier) {
		return getClusters(userIdentifier);
	}

	final public double getNormalisedScore(String userIdentifier) {
		if (endScores.containsKey(userIdentifier)) {
			return endScores.get(userIdentifier);
		} else {
			return 0;
		}
	}

	final public double getClusters(String userIdentifier) {
		// clusters zijn nog niet gemaakt of er bestaat wel data maar deze
		// is van een ander moment verdeel ze dus alsnog
		if (clusters == null
				|| !trait.getData().getStartTime().equals(pStartTime)
				|| !trait.getData().getEndTime().equals(pEndTime)) {
			analyseAllUsers(trait.getData().getStartTime(), trait.getData()
					.getEndTime());
		}

		if (clusters == null) {
			return -1.;
		} else if (clusters.get(LOWER_CLUSTER).containsKey(userIdentifier)) {
			return BigFive.additionConst;
		} else if (clusters.size() > MIDDLE_L_CLUSTER
				&& clusters.get(MIDDLE_L_CLUSTER).containsKey(userIdentifier)) {
			return 2 * BigFive.additionConst;
		} else if (clusters.size() > MIDDLE_U_CLUSTER
				&& clusters.get(MIDDLE_U_CLUSTER).containsKey(userIdentifier)) {
			return 3 * BigFive.additionConst;
		} else if (clusters.size() > UPPER_CLUSTER
				&& clusters.get(UPPER_CLUSTER).containsKey(userIdentifier)) {
			return 4 * BigFive.additionConst;
		} else {
			return 0.;
		}
	}

	/**
	 * Execute the analysis of all the users for this behaviour.
	 */
	final public void analyseAllUsers() {
		analyseAllUsers(trait.getData().getStartTime(), trait.getData()
				.getEndTime());
	}

	public abstract void analyseAllUsers(Date startTime, Date endTime);

	protected void calculateResultsInv(HashMap<String, Double> scorePerUser) {
		Iterator<String> users = scorePerUser.keySet().iterator();
		while (users.hasNext()) {
			String uid = users.next();
			scorePerUser.put(uid, scorePerUser.get(uid) * -1);
		}
		calculateResults(scorePerUser);
	}

	protected void calculateResults(HashMap<String, Double> score) {
		if (score.size() == 0) {
			return;
		} else {
			// de data is gegenereerd aan de hand van deze datum +/-
			pStartTime = trait.getData().getStartTime();
			pEndTime = trait.getData().getEndTime();
			KMeans1D km = new KMeans1D(4, score);
			clusters = km.getClusters();
			Iterator<HashMap<String, Double>> it = clusters.iterator();
			while (it.hasNext()) {
				trait.addUsers((it.next().keySet()));
			}
		}
	}
}
