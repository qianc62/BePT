/*
 * Created on 15-02-2005
 *
 */
package org.processmining.framework.models.bpel4ws.type.activity;

import java.util.List;
import java.util.Vector;

import org.processmining.framework.models.bpel4ws.type.BPEL4WSVisitor;
import org.processmining.framework.models.bpel4ws.type.BPEL4WS;

/**
 * @author Kristian Bisgaard Lassen
 */
public class Sequence extends Composed {

	/**
	 * <code>Activity</code> array to be executed in sequence
	 */
	public final Vector<Activity> activities;

	/**
	 * @param name
	 * @param activities
	 */
	public Sequence(String name, Vector<Activity> activities) {
		super(name);
		if (activities == null)
			this.activities = new Vector<Activity>();
		else
			this.activities = activities;
	}

	/**
	 * @param activities
	 */
	public Sequence(Activity... activities) {
		super(null);
		this.activities = new Vector<Activity>();
		for (Activity activity : activities)
			this.activities.add(activity);
	}

	/**
	 * @param activities
	 */
	public Sequence(Vector<Activity> activities) {
		super(null);
		if (activities == null)
			this.activities = new Vector<Activity>();
		else
			this.activities = activities;
	}

	/**
	 * @param activities
	 */
	public Sequence(List<Activity> activities) {
		super(null);
		this.activities = new Vector<Activity>(activities);
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
		Vector<Activity> clonedActivities = new Vector<Activity>();
		for (Activity activity : activities)
			clonedActivities.add(activity.cloneActivity());
		return new Sequence(name, clonedActivities);
	}

	public String writeToDot(BPEL4WS model) {
		String s = "";
		for (Activity activity : activities) {
			s += "    n" + this + " [label=\"" + name + "];\n";
		}
		return s;
	}
}
