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
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.processmining.framework.ui.AnalysisUI;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.Utils;

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

public class AnalyseAction extends CatchOutOfMemoryAction {

	public static Icon getIcon() {
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/toolbar_analyze.png";
		if ((new File(customIconPath).exists())) {
			return new ImageIcon(customIconPath);
		} else {
			return Utils.getStandardIcon("general/Find24");
		}
	}

	public AnalyseAction(MDIDesktopPane desktop) {
		super("More analysis...", AnalyseAction.getIcon(), desktop);
		putValue(SHORT_DESCRIPTION, "Open analysis frame");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
	}

	public void execute(ActionEvent e) {
		AnalysisUI frame = new AnalysisUI(desktop);

		frame.setVisible(true);
		desktop.add(frame);
		try {
			frame.setSelected(true);
		} catch (PropertyVetoException ex) {
		}
	}

	public void handleOutOfMem() {
		Message.add("Out of memory while analysing");
	}
}
