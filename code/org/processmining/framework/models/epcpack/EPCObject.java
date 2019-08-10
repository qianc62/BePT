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

package org.processmining.framework.models.epcpack;

import org.processmining.framework.models.*;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public abstract class EPCObject extends ModelGraphVertex {
	private int number;

	public EPCObject(ConfigurableEPC epc) {
		super(epc);
	}

	public void setNumber(int i) {
		number = i;
	}

	public int getNumber() {
		return number;
	}

	public ConfigurableEPC getEPC() {
		return (ConfigurableEPC) getSubgraph();
	}

}
