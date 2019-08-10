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
 * Title: BPELAssign
 * </p>
 * 
 * <p>
 * Description: Class for a BPEL assign activity.
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
public class BPELAssign extends BPELActivity {

	public BPELAssign(Element element) {
		super(element);
	}

	public BPELAssign(String name) {
		super(BPELConstants.stringAssign, name);
	}

	public BPELAssign cloneActivity() {
		BPELAssign clone = new BPELAssign(element
				.getAttribute(BPELConstants.stringName));
		clone.cloneLinks(this);
		return clone;
	}

	public void acceptVisitor(BPELVisitor visitor) {
		visitor.visit(this);
	}
}
