/*
 * Created on 15-02-2005
 *
 */
package org.processmining.framework.models.bpel4ws.type.activity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.processmining.framework.models.bpel4ws.type.BPEL4WSVisitor;
import org.processmining.framework.models.bpel4ws.unit.Pair;

import java.io.*;
import org.processmining.framework.models.bpel4ws.type.*;

/**
 * @author Kristian Bisgaard Lassen
 */
public class Flow extends Composed {

	/**
	 * <code>Activity</code> array to be executed concurrently
	 */
	public final Vector<Activity> activities;

	/***/
	public final Map<Pair<Activity, Activity>, String> links;

	/***/
	public final Map<Activity, String> jc;

	/**
	 * @param name
	 * @param activities
	 * @param links
	 * @param jc
	 */
	public Flow(String name, Vector<Activity> activities,
			Map<Pair<Activity, Activity>, String> links,
			Map<Activity, String> jc) {
		super(name);
		this.activities = activities;
		this.links = links;
		this.jc = jc;
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
		Map<Activity, Activity> clonedActivities = new LinkedHashMap<Activity, Activity>();
		for (Activity activity : activities)
			clonedActivities.put(activity, activity.cloneActivity());
		Map<Pair<Activity, Activity>, String> clonedLinks = new LinkedHashMap<Pair<Activity, Activity>, String>();
		for (Pair<Activity, Activity> key : links.keySet())
			clonedLinks.put(new Pair<Activity, Activity>(clonedActivities
					.get(key.first), clonedActivities.get(key.second)), links
					.get(key));
		Map<Activity, String> newJc = new LinkedHashMap<Activity, String>();
		for (Activity activity : jc.keySet())
			newJc.put(clonedActivities.get(activity), jc.get(activity));
		return new Flow(name, new Vector<Activity>(clonedActivities.values()),
				clonedLinks, newJc);
	}

	public String writeToDot(BPEL4WS model) {
		String s = "";
		for (Activity activity : activities) {
			s += "    n" + this + " [label=\"" + name + "];\n";
		}
		return s;
	}
}
