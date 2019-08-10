/*
 * Created on 15-02-2005
 *
 */
package org.processmining.framework.models.bpel4ws.type;

import org.processmining.framework.models.bpel4ws.type.activity.Activity;

/**
 * @author Kristian Bisgaard Lassen
 */
public class BPEL4WSProcess implements BPEL4WSVisitable {

	/**
     *
     */
	public final Activity activity;

	/**
     *
     */
	public final String name;

	/**
	 * @param activity
	 * @param name
	 */
	public BPEL4WSProcess(final Activity activity, final String name) {
		this.activity = activity;
		this.name = name;
	}

	/**
	 * @see type.bpel4ws.BPEL4WSVisitable#acceptVisitor(type.bpel4ws.BPEL4WSVisitor)
	 */
	public void acceptVisitor(BPEL4WSVisitor visitor) {
		visitor.visit(this);
	}

}
