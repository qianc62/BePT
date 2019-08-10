/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 *
 * LICENSE:
 *
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * EXEMPTION:
 *
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 *
 */
package org.processmining.framework.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.Utils;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author christian
 * 
 */
public class ShowExposeAction extends CatchOutOfMemoryAction {

	public static Icon getIcon() {
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/toolbar_expose.png";
		if ((new File(customIconPath).exists())) {
			return new ImageIcon(customIconPath);
		} else {
			return Utils.getStandardIcon("general/Open24");
		}
	}

	public ShowExposeAction(MDIDesktopPane desktop, String label) {
		super(label, desktop);
		putValue(SHORT_DESCRIPTION, "Expos\u00E9 (fast window switching)  "
				+ getShortcut());
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
	}

	public ShowExposeAction(MDIDesktopPane desktop) {
		super("Expos\u00E9", ShowExposeAction.getIcon(), desktop);
		putValue(SHORT_DESCRIPTION, "Expos\u00E9 (fast window switching)  "
				+ getShortcut());
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
	}

	public String getShortcut() {
		// get shortcut according to platform
		String shortCut = null;
		if (RuntimeUtils.isRunningMacOsX() == true) {
			shortCut = "Command-E";
		} else {
			shortCut = "Ctrl+E";
		}
		return shortCut;
	}

	@Override
	protected void execute(ActionEvent e) {
		if (MainUI.getInstance().getDesktop().getAllFrames().length > 1) {
			MainUI.getInstance().showExpose();
		} else {
			MainUI
					.getInstance()
					.showGlassDialog(
							"Expos\u00E9 not applicable",
							"The Expos\u00E9 feature has the purpose of helping you switch between a number of "
									+ "internal windows rapidly. You need to have more than one internal window "
									+ "open to make use of this functionality.");
		}
	}

	@Override
	protected void handleOutOfMem() {
		// TODO Auto-generated method stub

	}

}
