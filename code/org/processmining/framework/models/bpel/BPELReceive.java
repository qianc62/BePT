/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.bpel;

import org.w3c.dom.Element;

import org.processmining.framework.models.bpel.visit.*;

/**
 * <p>
 * Title: BPELReceive
 * </p>
 * 
 * <p>
 * Description: Class for a BPEL receive activity.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class BPELReceive extends BPELEvent {

	public BPELReceive(Element element) {
		super(element);
	}

	public BPELReceive(String name) {
		super(BPELConstants.stringReceive, name);
	}

	public BPELReceive cloneActivity() {
		BPELReceive clone = new BPELReceive(element
				.getAttribute(BPELConstants.stringName));
		clone.cloneLinks(this);
		return clone;
	}

	public void acceptVisitor(BPELVisitor visitor) {
		visitor.visit(this);
	}
}
