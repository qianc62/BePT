package org.processmining.analysis.socialsuccess.bigfive.extraversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Trait;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

public class StartConversations extends Behaviour {

	public StartConversations(Trait tr) {
		super(tr);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		// process 'USER+ACTION' is nodig
		if (trait.getData().isProcessAvailable(
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

			HashMap<String, Integer> numberOfNodesPerUser = new HashMap<String, Integer>();
			HashMap<String, Integer> numberOfNodesWithRepliesPerUser = new HashMap<String, Integer>();

			Iterator<ProcessInstance> instance = log.instanceIterator();
			while (instance.hasNext()) {
				ProcessInstance p = instance.next();
				String uid = p.getAttributes().get("CreatedBy");
				String createT = p.getAttributes().get("TimestampCreate");
				SimpleDateFormat fd = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				try {
					if (p.getAttributes().containsKey("GenericType")
							&& p.getAttributes().get("GenericType").equals(
									"node")
							&& beginTime.before(fd.parse(createT))
							&& endTime.after(fd.parse(createT))) {
						if (numberOfNodesPerUser.containsKey(uid)) {
							Integer v = numberOfNodesPerUser.get(uid);
							numberOfNodesPerUser.put(uid, v + 1);
						} else {
							numberOfNodesPerUser.put(uid, 1);
						}
						if ((p.getDataAttributes().containsKey("NROfComments") && Integer
								.parseInt(p.getDataAttributes().get(
										"NROfComments")) > 0)
								|| (p.getDataAttributes().containsKey(
										"ContainsComments") && Integer
										.parseInt(p.getDataAttributes().get(
												"ContainsComments")) > 0)) {
							if (numberOfNodesWithRepliesPerUser
									.containsKey(uid)) {
								Integer v = numberOfNodesWithRepliesPerUser
										.get(uid);
								numberOfNodesWithRepliesPerUser.put(uid, v + 1);
							} else {
								numberOfNodesWithRepliesPerUser.put(uid, 1);
							}

						}
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// bereken het gemiddelde
			HashMap<String, Double> averageReactionPerUser = new HashMap<String, Double>();
			Iterator<String> it = numberOfNodesWithRepliesPerUser.keySet()
					.iterator();
			while (it.hasNext()) {
				String uid = it.next();
				Double r;
				if (!numberOfNodesWithRepliesPerUser.containsKey(uid)) {
					r = 0.;
				} else { // it exists
					r = ((double) numberOfNodesWithRepliesPerUser.get(uid) / (double) numberOfNodesPerUser
							.get(uid));
				}
				averageReactionPerUser.put(uid, Math.log1p(r));
			}

			calculateResults(averageReactionPerUser);
		} else {
			// TODO I might throw an exception here
			return;
		}
	}

}
