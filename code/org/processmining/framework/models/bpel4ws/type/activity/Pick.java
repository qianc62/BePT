/*
 * Created on 15-02-2005
 *
 */
package org.processmining.framework.models.bpel4ws.type.activity;

import java.util.LinkedHashMap;
import java.util.Map;

import org.processmining.framework.models.bpel4ws.type.BPEL4WSVisitor;
import org.processmining.framework.models.bpel4ws.unit.Pair;
import org.processmining.framework.models.bpel4ws.type.BPEL4WS;

/**
 * @author Kristian Bisgaard Lassen
 */
public class Pick extends Composed {

	/***/
	public final Map<Activity, String> messages;

	/***/
	public final Map<Activity, Pair<String, String>> alarms;

	/**
	 * @param name
	 * @param messages
	 * @param alarms
	 */
	public Pick(String name, Map<Activity, String> messages,
			Map<Activity, Pair<String, String>> alarms) {
		super(name);
		this.messages = messages;
		this.alarms = alarms;
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitable#acceptVisitor(type.bpel4ws.BPEL4WSVisitor)
	 */
	public void acceptVisitor(BPEL4WSVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * @see type.bpel4ws.activity.Activity#cloneActivity()
	 */
	@Override
	public Activity cloneActivity() {
		Map<Activity, String> clonedMessages = new LinkedHashMap<Activity, String>();
		for (Activity activity : messages.keySet())
			clonedMessages
					.put(activity.cloneActivity(), messages.get(activity));
		Map<Activity, Pair<String, String>> clonedAlarms = new LinkedHashMap<Activity, Pair<String, String>>();
		for (Activity activity : alarms.keySet()) {
			Pair<String, String> pair = alarms.get(activity);
			clonedAlarms.put(activity.cloneActivity(),
					new Pair<String, String>(pair.first, pair.second));
		}
		return new Pick(name, clonedMessages, clonedAlarms);
	}

	public String writeToDot(BPEL4WS model) {
		String s = "";
		for (Activity activity : messages.keySet()) {
			s += "    n" + this + " [label=\"" + name + "];\n";
		}
		return s;
	}
}
