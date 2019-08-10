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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class MessageConsole {
	private ActionListener messageListener = new ActionListener() {
		public String[] prefix = { "     ", "WAR: ", "ERR: ", "dbg: " };

		public void actionPerformed(ActionEvent e) {
			if (e.getModifiers() == Message.CLEAR_MESSAGES) {
				System.out
						.println("\n--------------------------------------------------------------------------\n");
			} else {
				System.out.println(prefix[e.getModifiers()]
						+ e.getActionCommand());
			}
		}
	};

	public MessageConsole() {
		Message.addActionListener(messageListener);
	}

}
