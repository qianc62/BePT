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

package org.processmining.framework.ui.menus;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.RuntimeUtils;

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
public class HelpMenu extends JMenu {

	private JMenuItem noneshort = new JMenuItem(
			RuntimeUtils
					.stripHtmlForOsx("<html><font color=\"#999999\">No help available</font></html>"));

	public HelpMenu() {
		super("Plugin help");

		addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
			}

			public void menuDeselected(MenuEvent e) {
				removeAll();
			}

			public void menuSelected(MenuEvent e) {
				buildChildMenus(MainUI.getInstance().getDesktop()
						.getPluginSelectedFrame(), noneshort);
			}
		});
		noneshort.setEnabled(false);
	}

	private void buildChildMenus(Plugin plugin, JMenuItem noneFound) {
		if (plugin != null) {
			add(new JMenuItem(new HelpAction(plugin)));
		}
		// iterate the provided objects
		if (getItemCount() == 0) {
			add(noneFound);
		}
	}

	class HelpAction extends AbstractAction {

		private Plugin plugin;

		public HelpAction(Plugin plugin) {
			super(RuntimeUtils.stripHtmlForOsx("<html>" + plugin.getName()
					+ "</html>"));
			this.plugin = plugin;
		}

		public void actionPerformed(ActionEvent e) {
			MainUI.getInstance().showReference(plugin);
		}
	}

}
