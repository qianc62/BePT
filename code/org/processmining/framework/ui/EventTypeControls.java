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

package org.processmining.framework.ui;

import javax.swing.JRadioButton;

import org.processmining.framework.log.filter.DefaultLogFilter;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class EventTypeControls {

	private String name;
	private JRadioButton discard;
	private JRadioButton include;

	public EventTypeControls(String name, JRadioButton discard,
			JRadioButton include) {
		this.name = name;
		this.discard = discard;
		this.include = include;
	}

	public String getName() {
		return name;
	}

	public int getState() {
		return discard.getModel().isSelected() ? DefaultLogFilter.DISCARD
				: include.getModel().isSelected() ? DefaultLogFilter.INCLUDE
						: DefaultLogFilter.DISCARD_INSTANCE;
	}
}
