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
package org.processmining.framework.ui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class DesktopBackgroundMenu extends JMenu implements ActionListener {

	protected File[] backgrounds;
	protected ButtonGroup buttonGroup;

	public DesktopBackgroundMenu() {
		super("Desktop background");
		buttonGroup = new ButtonGroup();
		String selectedBgPath = UISettings.getInstance()
				.getPreferredDesktopBackground();
		File selectedBg = new File(selectedBgPath);
		backgrounds = new File[0];
		// collect background directory candidates
		File bgDir = new File(UISettings.getProMDirectoryPath()
				+ "images/desktop/");
		try {
			bgDir = bgDir.getCanonicalFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (bgDir.exists() == true) {
			ArrayList<File> bgList = new ArrayList<File>();
			File[] protoBg = bgDir.listFiles();
			for (int i = 0; i < protoBg.length; i++) {
				if (protoBg[i].isFile()
						&& (protoBg[i].getName().startsWith(".") == false)) {
					bgList.add(protoBg[i]);
					Message.add("Desktop background found at "
							+ protoBg[i].getAbsolutePath(), Message.DEBUG);
				} else {
					Message.add(
							"Skipping file " + protoBg[i].getAbsolutePath(),
							Message.DEBUG);
				}
			}
			backgrounds = bgList.toArray(backgrounds);
		} else {
			Message.add("Desktop background directory not found! ("
					+ bgDir.getAbsolutePath() + ")", Message.ERROR);
		}
		// build menu
		JRadioButtonMenuItem item;
		for (int i = 0; i < backgrounds.length; i++) {
			String bgName = backgrounds[i].getName();
			if (bgName.indexOf(".") > 0) {
				bgName = bgName.substring(0, bgName.lastIndexOf("."));
			}
			item = new JRadioButtonMenuItem(bgName);
			item.setActionCommand(backgrounds[i].getAbsolutePath());
			item.addActionListener(this);
			buttonGroup.add(item);
			if (backgrounds[i].equals(selectedBg)) {
				item.setSelected(true);
			} else {
				item.setSelected(false);
			}
			this.add(item);
		}
		// add item for no background pattern
		item = new JRadioButtonMenuItem("No background pattern");
		item.setActionCommand("NONE");
		item.addActionListener(this);
		buttonGroup.add(item);
		if (selectedBgPath.equals("NONE")) {
			item.setSelected(true);
		} else {
			item.setSelected(false);
		}
		this.add(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("NONE")) {
			UISettings.getInstance().setPreferredDesktopBackground("NONE");
			MainUI.getInstance().getDesktop().setBackground("NONE");
		} else {
			for (int i = 0; i < backgrounds.length; i++) {
				if (command.equals(backgrounds[i].getAbsolutePath())) {
					UISettings.getInstance().setPreferredDesktopBackground(
							command);
					MainUI.getInstance().getDesktop().setBackground(command);
					break;
				}
			}
		}
	}

}
