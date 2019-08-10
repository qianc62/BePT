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

public class InstanceEPC extends EPC {
	public InstanceEPC() {
		this(true);
	}

	public InstanceEPC(boolean enforceValidity) {
		super(enforceValidity);
		this.setName("Instance EPC");
	}

	public EPCConnector addConnector(EPCConnector c) {
		if (enforceValidity) {
			// Only allow AND connectors
			if (c.getType() != EPCConnector.AND) {
				return null;
			}
		}
		return super.addConnector(c);
	}

}
