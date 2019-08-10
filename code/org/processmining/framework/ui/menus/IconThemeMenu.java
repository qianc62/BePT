/**
 * Project: ProM
 * File: IconThemeMenu.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Nov 15, 2006, 2:02:53 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.ui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class IconThemeMenu extends JMenu implements ActionListener {

	protected File[] themes;
	protected ButtonGroup buttonGroup;

	public IconThemeMenu() {
		super("Icon theme");
		buttonGroup = new ButtonGroup();
		String selectedTheme = UISettings.getInstance().getPreferredIconTheme();
		if (selectedTheme.trim().length() == 0) {
			// no theme set (initial startup)
			try {
				selectedTheme = (new File("images/icons/default/"))
						.getCanonicalPath();
				UISettings.getInstance().setPreferredIconTheme(selectedTheme);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		themes = new File[0];
		// collect theme directory candidates
		File themeDir = new File("images/icons/");
		try {
			themeDir = themeDir.getCanonicalFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (themeDir.exists()) {
			ArrayList<File> themeList = new ArrayList<File>();
			File[] protoThemes = themeDir.listFiles();
			for (int i = 0; i < protoThemes.length; i++) {
				if (protoThemes[i].isDirectory()
						&& (protoThemes[i].getName().startsWith(".") == false)) {
					themeList.add(protoThemes[i]);
					Message.add("Icon theme found at "
							+ protoThemes[i].getAbsolutePath(), Message.DEBUG);
				} else {
					Message.add("Skipping directory "
							+ protoThemes[i].getAbsolutePath(), Message.DEBUG);
				}
			}
			themes = themeList.toArray(themes);
		} else {
			Message.add("Icon theme directory not found! ("
					+ themeDir.getAbsolutePath() + ")", Message.ERROR);
		}
		// build menu
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(
				"Classic (ProM 3.x)");
		item.setSelected(true);
		item.setActionCommand("NO_THEME");
		item.addActionListener(this);
		buttonGroup.add(item);
		this.add(item);
		for (int i = 0; i < themes.length; i++) {
			item = new JRadioButtonMenuItem(themes[i].getName());
			item.setActionCommand(themes[i].getAbsolutePath());
			item.addActionListener(this);
			buttonGroup.add(item);
			if (themes[i].getAbsolutePath().equals(selectedTheme)) {
				item.setSelected(true);
			} else {
				item.setSelected(false);
			}
			this.add(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("NO_THEME")) {
			UISettings.getInstance().setPreferredIconTheme("NO_THEME");
		} else {
			for (int i = 0; i < themes.length; i++) {
				if (command.equals(themes[i].getAbsolutePath())) {
					UISettings.getInstance().setPreferredIconTheme(command);
					break;
				} else {
				}
			}
		}
		JOptionPane.showMessageDialog(MainUI.getInstance(),
				"Theme changes will only be visible after\n"
						+ "a restart of the application.");
	}

}
