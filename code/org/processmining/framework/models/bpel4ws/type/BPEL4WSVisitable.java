/*
 * Created on 16-02-2005
 *
 */
package org.processmining.framework.models.bpel4ws.type;

/**
 * @author Kristian Bisgaard Lassen
 */
public interface BPEL4WSVisitable {

	/**
	 * @param visitor
	 */
	public void acceptVisitor(BPEL4WSVisitor visitor);

}
