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

package org.processmining.framework.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.processmining.framework.ui.MDIDesktopPane;

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

public abstract class CatchOutOfMemoryAction extends AbstractAction {
	protected MDIDesktopPane desktop;

	public CatchOutOfMemoryAction(String s, MDIDesktopPane desktop) {
		super(s);
		this.desktop = desktop;
	}

	public CatchOutOfMemoryAction(String s, Icon i, MDIDesktopPane desktop) {
		super(s, i);
		this.desktop = desktop;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			execute(e);
		} catch (OutOfMemoryError err) {
			handleOutOfMem();
		}
	}

	protected abstract void execute(ActionEvent e);

	protected abstract void handleOutOfMem();

}
