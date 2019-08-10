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

import java.io.*;
import java.text.*;
import java.util.*;

import org.processmining.framework.log.*;

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

public class EPC extends ConfigurableEPC {
	public EPC() {
		this(true);
	}

	public EPC(boolean enforceValidity) {
		super(enforceValidity);
		this.setName("EPC");
	}

	public EPCFunction addFunction(EPCFunction f) {
		if (f.isConfigurable()) {
			return null;
		}
		super.addFunction(f);
		return f;
	}

	public EPCConnector addConnector(EPCConnector c) {
		if (c.isConfigurable()) {
			return null;
		}
		super.addConnector(c);
		return c;
	}

}
