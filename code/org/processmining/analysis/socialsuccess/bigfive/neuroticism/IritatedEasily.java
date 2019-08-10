/**
 * 
 */
package org.processmining.analysis.socialsuccess.bigfive.neuroticism;

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
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;

/**
 * @author MvanWingerden
 * 
 */
public class IritatedEasily extends Behaviour {

	private HashSet<String> postActions = new HashSet<String>();

	public IritatedEasily(Trait tr) {
		super(tr);
		postActions.add("REGISTER");
		postActions.add("EDIT");
		postActions.add("CREATE");
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		// process 'USER+ACTION' is nodig
		if (trait.getData().isProcessAvailable(PersonalityData.USER_ACTION)) {
			LogReader log = trait.getData().getProcess(
					PersonalityData.USER_ACTION);
			HashMap<String, Double> impatientPerUser = new HashMap<String, Double>();
			HashMap<String, Double> averagePerUser = new HashMap<String, Double>();
			HashMap<String, Double> nodesPerUser = new HashMap<String, Double>();

			Iterator<ProcessInstance> instance = log.instanceIterator();
			while (instance.hasNext()) {
				AuditTrailEntry prevEntry = new AuditTrailEntryImpl();
				prevEntry.setName("");
				ProcessInstance p = instance.next();
				String uid = p.getAttributes().get("userID");
				Iterator<AuditTrailEntry> audits = p.getAuditTrailEntryList()
						.iterator();
				while (audits.hasNext()) {
					AuditTrailEntry au = audits.next();
					if (beginTime.before(au.getTimestamp())
							&& endTime.after(au.getTimestamp())
							&& au.getType().equalsIgnoreCase("complete")
							&& postActions.contains(au.getName())) {
						if (nodesPerUser.containsKey(uid)) {
							nodesPerUser.put(uid, nodesPerUser.get(uid) + 1);
						} else {
							nodesPerUser.put(uid, 1.);
						}
						if (prevEntry != null
								&& prevEntry.getName().equalsIgnoreCase(
										au.getName())
								&& prevEntry.getType().equalsIgnoreCase(
										au.getType())) {
							if (impatientPerUser.containsKey(uid)) {
								impatientPerUser.put(uid, impatientPerUser
										.get(uid) + 1);
							} else {
								impatientPerUser.put(uid, 1.);
							}
						}
						prevEntry = (AuditTrailEntry) au.clone();
					} else {
						// deze actie voldoet niet, dan moet vorige actie
						// variabele
						// dus even op null gezet worden
						prevEntry = null;
					}
				}
			}

			Iterator<String> users = impatientPerUser.keySet().iterator();

			while (users.hasNext()) {
				double count, impatient;
				String u = users.next();
				if (impatientPerUser.containsKey(u))
					impatient = impatientPerUser.get(u);
				else
					impatient = 0.;

				count = nodesPerUser.get(u);
				averagePerUser.put(u, impatient / count);
			}
			// cluster de gemiddelde
			calculateResults(averagePerUser);
		} else {
			// TODO I might throw an exception here
			return;
		}
	}

}
