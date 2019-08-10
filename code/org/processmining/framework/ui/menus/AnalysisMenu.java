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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.AnalysisPluginCollection;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.actions.AnalyseAction;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class AnalysisMenu extends JMenu {

	private static final long serialVersionUID = -108506382383699893L;

	private MDIDesktopPane desktop;
	private JMenuItem none = new JMenuItem(
			RuntimeUtils
					.stripHtmlForOsx("<html><font color=\"#999999\">No analysis available<br>for selected frame</font></html>"));
	private JMenuItem noneshort = new JMenuItem(
			RuntimeUtils
					.stripHtmlForOsx("<html><font color=\"#999999\">No analysis available</font></html>"));
	private boolean onlyContext;

	public AnalysisMenu(MDIDesktopPane desktop) {
		this(desktop, true);
	}

	public AnalysisMenu(final ProvidedObject[] pos) {
		this.onlyContext = true;

		setText("Analysis");
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
		none.setEnabled(false);
		noneshort.setEnabled(false);
	}

	public AnalysisMenu(MDIDesktopPane desktop, boolean all) {
		onlyContext = !all;
		this.desktop = desktop;

		setText("Analysis");
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

	private void buildChildMenus(ProvidedObject[] objects, JMenuItem noneFound) {
		AnalysisPluginCollection collection = AnalysisPluginCollection
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
		if (!onlyContext) {
			addSeparator();
			add(new AnalyseAction(desktop));
		}
	}

	private void buildSubMenus(JMenu superMenu,
			AnalysisPluginCollection collection, ProvidedObject po) {
		boolean addSeparatorFirst = false;

		for (int i = 0; i < collection.size(); i++) {
			AnalysisPlugin algorithm = (AnalysisPlugin) collection.get(i);

			if (algorithm == null) {
				if (superMenu.getItemCount() > 0) {
					addSeparatorFirst = true;
				}
			} else {
				AnalysisInputItem[] items = algorithm.getInputItems();

				// if the analysisInputItem requires one or more objects and
				// this item accepts our object, add a menu item.
				if (items.length == 1
						&& (items[0].getMinimum() == 1 || items[0].getMaximum() == 1)
						&& items[0].accepts(po)) {
					if (addSeparatorFirst) {
						// we never add a separator as the first item or the
						// last item in a menu
						// we also never insert two seperators right after each
						// other
						superMenu.addSeparator();
						addSeparatorFirst = false;
					}
					superMenu.add(new JMenuItem(new AnalysisAction(algorithm,
							po)));
				}
			}
		}
	}
}
