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

import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.processmining.exporting.ExportPlugin;
import org.processmining.exporting.ExportPluginCollection;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.actions.ExportAction;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ExportMenu extends JMenu {

	private static final long serialVersionUID = 760277658078005760L;

	private boolean onlyContext;
	private JMenuItem none = new JMenuItem(
			RuntimeUtils
					.stripHtmlForOsx("<html><font color=\"#999999\">No export available <br>for selected frame</font></html>"));
	private JMenuItem noneshort = new JMenuItem(
			RuntimeUtils
					.stripHtmlForOsx("<html><font color=\"#999999\">No export available</font></html>"));

	public ExportMenu(JDesktopPane desktop) {
		this.onlyContext = false;

		setText("Exports");
		addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
			}

			public void menuDeselected(MenuEvent e) {
				removeAll();
			}

			public void menuSelected(MenuEvent e) {
				buildChildMenus(MainUI.getInstance().getProvidedObjects(), none);
			}
		});
		none.setEnabled(false);
	}

	public ExportMenu(final ProvidedObject[] pos) {
		this.onlyContext = true;

		setText("Export");
		addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
			}

			public void menuDeselected(MenuEvent e) {
				removeAll();
			}

			public void menuSelected(MenuEvent e) {
				buildChildMenus(pos, noneshort);
			}
		});
		noneshort.setEnabled(false);
		none.setEnabled(false);
	}

	private void buildChildMenus(ProvidedObject[] objects, JMenuItem noneFound) {
		ExportPluginCollection collection = ExportPluginCollection
				.getInstance();
		boolean addToSelf = onlyContext && objects.length == 1;

		for (int j = 0; j < objects.length; j++) {
			if (objects[j] != null) {
				JMenu poMenu = addToSelf ? this : new JMenu(objects[j]
						.getName());

				buildSubMenus(poMenu, collection, objects[j]);

				if (!addToSelf && poMenu.getItemCount() > 0) {
					add(poMenu);
				}
			}
		}
		if (getItemCount() == 0) {
			add(noneFound);
		}
	}

	private void buildSubMenus(JMenu superMenu,
			ExportPluginCollection collection, ProvidedObject po) {
		for (int i = 0; i < collection.size(); i++) {
			ExportPlugin algorithm = (ExportPlugin) collection.get(i);
			if (algorithm.accepts(po)) {
				superMenu.add(new JMenuItem(new ExportAction(algorithm, po)));
			}
		}
	}
}
