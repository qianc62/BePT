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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.UISettings;
import org.processmining.importing.ImportPluginCollection;
import org.processmining.importing.LogReaderConnectionImportPlugin;

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
public class RecentFileMenu extends JMenu {
	private MDIDesktopPane desktop;

	public RecentFileMenu(MDIDesktopPane desktop) {
		super("Recent files");
		this.desktop = desktop;
		addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
			}

			public void menuDeselected(MenuEvent e) {
				removeAll();
			}

			public void menuSelected(MenuEvent e) {
				buildChildMenus();
			}
		});
	}

	private void buildChildMenus() {
		ArrayList files = UISettings.getInstance().getRecentFiles();
		ImportPluginCollection collection = ImportPluginCollection
				.getInstance();

		ArrayList logReaders = new ArrayList();
		ProvidedObject[] objs = MainUI.getInstance().getProvidedObjects();
		for (int j = 0; j < objs.length; j++) {
			for (int k = 0; k < objs[j].getObjects().length; k++) {
				Object o = objs[j].getObjects()[k];
				if (o instanceof LogReader) {
					logReaders.add(objs[j].getName());
					logReaders.add(o);
				}
			}
		}

		removeAll();
		for (int i = 0; i < files.size() && i < 20; i++) {
			String[] file = (String[]) files.get(i);

			Plugin plugin = collection.get(file[1]);
			if ((plugin != null)
					&& (plugin instanceof LogReaderConnectionImportPlugin)) {
				JMenu subMenu = new JMenu((new File(file[0])).getName() + " - "
						+ file[1]);
				subMenu.add(new OpenRecentFileAction(file[0],
						"Without Log file", file[1], desktop, null));
				Iterator it = logReaders.iterator();
				if (it.hasNext()) {
					subMenu.addSeparator();
				}
				while (it.hasNext()) {
					subMenu.add(new OpenRecentFileAction(file[0], "With: "
							+ (String) it.next(), file[1], desktop,
							(LogReader) it.next()));
				}
				add(subMenu);
			} else {
				add(new OpenRecentFileAction(file[0], (new File(file[0]))
						.getName(), file[1], desktop));
			}
		}
		setEnabled(getItemCount() > 0);
	}
}
