/*
 * Created on 15-02-2005
 *
 */
package org.processmining.framework.models.bpel4ws.type.activity;

import org.processmining.framework.models.bpel4ws.type.BPEL4WSVisitor;

/**
 * @author Kristian Bisgaard Lassen
 */
public class Reply extends Basic {

	/**
	 * @param name
	 */
	public Reply(String name) {
		super(name);
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
		return new Reply(name);
	}
}
