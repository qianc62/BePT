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

package org.processmining.analysis.ltlchecker.parser;

import org.processmining.framework.log.ProcessInstance;

/**
 * AttributeNoValueException is generated when a attribute does not exists in
 * a process instance or audit trail entry.
 *
 * @version 0.1
 * @author HT de Beer
 */
public class AttributeNoValueException extends Exception {

	public AttributeNoValueException(ProcessInstance pi, int ateNr, Attribute attr) {
		super(
				"No element " + attr.toString() + " in pi" + pi.getName() +
				" - " + pi.getProcess() + " ( " + ateNr + " ).");
	}

}
