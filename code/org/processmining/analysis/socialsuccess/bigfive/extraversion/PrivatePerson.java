package org.processmining.analysis.socialsuccess.bigfive.extraversion;

import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Trait;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;

public class PrivatePerson extends Behaviour {
	private HashSet<String> publicActions = new HashSet<String>();
	private HashSet<String> privateActions = new HashSet<String>();
	private HashMap<String, HashMap<String, Double>> publicActionsPP = new HashMap<String, HashMap<String, Double>>();
	private HashMap<String, HashMap<String, Double>> privateActionsPP = new HashMap<String, HashMap<String, Double>>();

	public PrivatePerson(Trait tr) {
		super(tr);
		// private actions
		privateActions.add("OVERVIEW");
		privateActions.add("LIST");
		privateActions.add("DETAIL");
		// public actions
		publicActions.add("DELETE");
		publicActions.add("CREATE");
		publicActions.add("EDIT");
		publicActions.add("TIDY_UP");
		publicActions.add("REPORT");
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		// process 'USER+ACTION' is nodig
		if (trait.getData().isProcessAvailable(PersonalityData.USER_ACTION)) {
			HashMap<String, Double> scorePerUser = new HashMap<String, Double>();
			LogReader log = trait.getData().getProcess(
					PersonalityData.USER_ACTION);
			Iterator<ProcessInstance> it = log.instanceIterator();
			while (it.hasNext()) {
				ProcessInstance pi = it.next();
				String user = pi.getName();
				double nPublicActions = 0, nPrivateActions = 0;
				HashMap<String, Double> countsPub = new HashMap<String, Double>();
				HashMap<String, Double> countsPriv = new HashMap<String, Double>();
				Iterator<AuditTrailEntryImpl> auIt = pi
						.getAuditTrailEntryList().iterator();
				while (auIt.hasNext()) {
					AuditTrailEntryImpl entry = auIt.next();
					if (beginTime.before(entry.getTimestamp())
							&& endTime.after(entry.getTimestamp())) {
						String action = entry.getName();
						if (publicActions.contains(action)) {
							nPublicActions++;
							if (countsPub.containsKey(action))
								countsPub
										.put(action, countsPub.get(action) + 1);
							else
								countsPub.put(action, 1.);
						}
						if (privateActions.contains(action)) {
							nPrivateActions++;
							if (countsPriv.containsKey(action))
								countsPriv.put(action,
										countsPriv.get(action) + 1);
							else
								countsPriv.put(action, 1.);
						}
					}
				}
				publicActionsPP.put(user, countsPub);
				privateActionsPP.put(user, countsPriv);
				// TODO deze is wel heel exponentieel
				if (nPrivateActions != 0)
					scorePerUser.put(user, nPublicActions / nPrivateActions);
			}
			calculateResults(scorePerUser);
		} else {
			// TODO I might throw an exception here
			return;
		}
	}
}
