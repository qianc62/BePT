/*
 * Created on 15-02-2005
 *
 */
package org.processmining.framework.models.bpel4ws.type.activity;

import org.processmining.framework.models.bpel4ws.type.BPEL4WSVisitable;

/**
 * @author Kristian Bisgaard Lassen
 */
public abstract class Activity implements BPEL4WSVisitable {

	/**
	 * A description of this <code>Activity</code>
	 */
	public final String name;

	/**
	 * @param name
	 */
	public Activity(final String name) {
		if (name == null)
			this.name = this.getClass().getSimpleName();
		else
			this.name = name;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

	/**
	 * @return A clone of this activity
	 */
	public abstract Activity cloneActivity();

}
