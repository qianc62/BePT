/*
 * Created on Jun 4, 2005
 *
 * (c) 2005 Christian W. Guenther, all rights reserved.
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning.logutils;

/**
 * Bookmark Intended to provide a convenient means for temporarily saving an
 * iterator's current state, in order to reset it to that state later on.
 * 
 * @author christian
 * 
 *         Christian W. Guenther (christian@deckfour.com)
 * 
 */
public class Bookmark {

	protected int position = 0;

	/**
	 * constructor
	 */
	public Bookmark(int pos) {
		position = pos;
	}

	public int getPosition() {
		return position;
	}

}
