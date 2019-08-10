package org.processmining.analysis.socialsuccess.bigfive.openness;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.*;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

public class FullOfIdeas extends Behaviour {
	// Openeness +
	protected HashMap<String, Double> nodesPerCreator = null;

	public FullOfIdeas(Openness openness) {
		super(openness);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		// process 'NODES+TAGS', 'ACTION-USER_ORIG' of 'ACTION-REFER_ORIG' is
		// nodig
		LogReader log = null;
		PersonalityData dt = trait.getData();
		if (dt.isProcessAvailable(PersonalityData.NODES_TAGS)) {
			log = dt.getProcess(PersonalityData.NODES_TAGS);
		} else if (dt
				.isProcessAvailable(PersonalityData.NODE_ACTION_REFER_ORIG)) {
			log = dt.getProcess(PersonalityData.NODE_ACTION_REFER_ORIG);
		} else {
			log = dt.getProcess(PersonalityData.NODE_ACTION_USER_ORIG);
		}

		if (log != null) {
			nodesPerCreator = new HashMap<String, Double>();

			Iterator<ProcessInstance> instance = log.instanceIterator();
			SimpleDateFormat fd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			while (instance.hasNext()) {
				ProcessInstance p = instance.next();
				DataSection d = p.getDataAttributes();
				try {
					if (d.containsKey("GenericType")
							&& d.get("GenericType").equals("node")
							&& d.containsKey("TimestampCreate")
							&& beginTime.before(fd.parse(d
									.get("TimestampCreate")))
							&& endTime
									.after(fd.parse(d.get("TimestampCreate")))
							&& d.containsKey("CreatedBy")) {
						String author = d.get("CreatedBy");
						if (nodesPerCreator.containsKey(author)) {
							Double prev = nodesPerCreator.remove(author);
							nodesPerCreator.put(author, prev + 1);
						} else {
							nodesPerCreator.put(author, 1.);
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			calculateResults(nodesPerCreator);
		} else {
			// TODO I might throw an exception here
			return;
		}
	}

}
