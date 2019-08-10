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

import org.processmining.converting.ConvertingPlugin;
import org.processmining.converting.ConvertingPluginCollection;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.actions.ConvertAction;
import org.processmining.framework.ui.actions.ConvertInternalAction;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ConversionMenu extends JMenu {

	private static final long serialVersionUID = -3949536262533489010L;

	private boolean onlyContext;
	private MDIDesktopPane desktop;
	private JMenuItem none = new JMenuItem(
			RuntimeUtils
					.stripHtmlForOsx("<html><font color=\"#999999\">No converters available<br>for selected frame</font></html>"));
	private JMenuItem noneshort = new JMenuItem(
			RuntimeUtils
					.stripHtmlForOsx("<html><font color=\"#999999\">No converter available</font></html>"));

	public ConversionMenu(MDIDesktopPane desktop) {
		this(desktop, true);
	}

	public ConversionMenu(final ProvidedObject[] pos) {
		this.onlyContext = true;

		setText("Conversion");
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

	public ConversionMenu(MDIDesktopPane desktop, boolean all) {
		this.onlyContext = !all;
		this.desktop = desktop;
		setText("Conversion");
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

	private void buildChildMenus(ProvidedObject[] objects, JMenuItem noneItem) {
		ConvertingPluginCollection collection = ConvertingPluginCollection
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
			add(noneItem);
		}
		if (!onlyContext) {
			addSeparator();
			add(new ConvertAction(desktop));
		}
	}

	private void buildSubMenus(JMenu superMenu,
			ConvertingPluginCollection collection, ProvidedObject po) {
		for (int i = 0; i < collection.size(); i++) {
			ConvertingPlugin algorithm = (ConvertingPlugin) collection.get(i);

			if (algorithm.accepts(po)) {
				superMenu.add(new JMenuItem(new ConvertInternalAction(
						algorithm, po)));
			}
		}
	}
}
