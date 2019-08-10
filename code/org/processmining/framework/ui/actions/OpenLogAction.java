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
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.OpenMXMLLogDialog;
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

public class OpenLogAction extends CatchOutOfMemoryAction {

	private static final long serialVersionUID = -6649600120991420475L;

	public static Icon getIcon() {
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/toolbar_open.png";
		if ((new File(customIconPath).exists())) {
			return new ImageIcon(customIconPath);
		} else {
			return Utils.getStandardIcon("general/Open24");
		}
	}

	public OpenLogAction(MDIDesktopPane desktop, String label) {
		super(label, desktop);
		putValue(SHORT_DESCRIPTION, "Open a XML workflow log");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
	}

	public OpenLogAction(MDIDesktopPane desktop) {
		super("Open new log...", OpenLogAction.getIcon(), desktop);
		putValue(SHORT_DESCRIPTION, "Open a XML workflow log");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
	}

	public void execute(ActionEvent e) {
		OpenMXMLLogDialog dialog = new OpenMXMLLogDialog(UISettings
				.getInstance().getLastOpenedLogFile());

		if (dialog.showDialog(false)) {
			MainUI.getInstance().addAction("import : MXML Log File",
					LogStateMachine.START, null);
			MainUI.getInstance().createOpenLogFrame(dialog.getFile());
			UISettings.getInstance().addRecentFile(dialog.getFile().toString(),
					null);
			UISettings.getInstance()
					.setLastOpenedLogFile(
							dialog.getFile() == null ? "" : dialog.getFile()
									.toString());
		}
	}

	public void handleOutOfMem() {

	}
}
