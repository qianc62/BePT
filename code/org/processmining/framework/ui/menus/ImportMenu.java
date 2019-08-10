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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.actions.CatchOutOfMemoryAction;
import org.processmining.framework.ui.actions.ImportAnyFileAction;
import org.processmining.framework.ui.actions.ImportFileAction;
import org.processmining.framework.ui.actions.OpenLogAction;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.importing.ImportPlugin;
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
public class ImportMenu extends JMenu {

	private JMenu recentFilesMenu;
	private MDIDesktopPane desktop;
	private ExitAction exitAction;
	private OpenLogAction openAction;
	protected ImportAnyFileAction anyFileAction;

	public ImportMenu(MDIDesktopPane desktop) {
		super("File");
		recentFilesMenu = new RecentFileMenu(desktop);
		exitAction = new ExitAction(desktop);
		openAction = new OpenLogAction(desktop, "Open MXML Log file");
		anyFileAction = new ImportAnyFileAction(desktop, !RuntimeUtils
				.isRunningMacOsX());
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
		ImportPluginCollection collection = ImportPluginCollection
				.getInstance();
		ArrayList logReaders = new ArrayList();
		ProvidedObject[] objs = MainUI.getInstance().getProvidedObjects();
		for (int j = 0; j < objs.length; j++) {
			for (int k = 0; k < objs[j].getObjects().length; k++) {
				Object o = objs[j].getObjects()[k];
				if (o instanceof LogReader) {
					logReaders.add(o);
					logReaders.add(objs[j].getName());
				}
			}
		}

		removeAll();
		add(anyFileAction);
		addSeparator();
		add(openAction);

		for (int i = 0; i < collection.size(); i++) {
			ImportPlugin algorithm = (ImportPlugin) collection.get(i);
			if (algorithm instanceof LogReaderConnectionImportPlugin) {
				JMenu subMenu = new JMenu("Open " + algorithm.getName());
				subMenu.add(new ImportFileAction(algorithm, null, desktop,
						"Without Log file"));
				Iterator it = logReaders.iterator();
				if (it.hasNext()) {
					subMenu.addSeparator();
				}
				while (it.hasNext()) {
					subMenu.add(new ImportFileAction(algorithm, (LogReader) it
							.next(), desktop, "With: " + (String) it.next()));
				}
				add(subMenu);
			} else {
				add(new ImportFileAction(algorithm, desktop));
			}
		}
		addSeparator();

		add(recentFilesMenu);
		addSeparator();

		add(exitAction);

		setEnabled(true);

	}
}

class ExitAction extends CatchOutOfMemoryAction {
	public ExitAction(MDIDesktopPane desktop) {
		super("Exit", desktop);
		putValue(SHORT_DESCRIPTION, "Exit the program");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
	}

	public void execute(ActionEvent e) {
		MainUI.getInstance().quit();
	}

	public void handleOutOfMem() {

	}
}
