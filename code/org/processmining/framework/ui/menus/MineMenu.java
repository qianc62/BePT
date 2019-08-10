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

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.actions.MineAction;
import org.processmining.framework.ui.actions.OpenLogAction;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningPluginCollection;

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
 * @author Peter van den Brandt
 * @author Christian W. Guenther (christian at deckfour dot org)
 * @version 1.0
 */

public class MineMenu extends JMenu {
	private static final long serialVersionUID = -1040790895781307812L;
	private boolean onlyContext;
	private MDIDesktopPane desktop;
	private JMenuItem none = new JMenuItem(
			RuntimeUtils
					.stripHtmlForOsx("<html><font color=\"#999999\">No partial log available<br>in selected frame</font></html>"));

	public MineMenu(MDIDesktopPane desktop) {
		this(desktop, true);
	}

	public MineMenu(MDIDesktopPane desktop, boolean all) {
		this.onlyContext = !all;
		this.desktop = desktop;

		setText("Mining");
		addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
			}

			public void menuDeselected(MenuEvent e) {
				removeAll();
			}

			public void menuSelected(MenuEvent e) {
				buildChildMenus(MainUI.getInstance().getProvidedObjects());
			}
		});
		none.setEnabled(false);
	}

	public MineMenu(final ProvidedObject[] pos) {
		this.onlyContext = true;

		setText("Mining");
		addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
			}

			public void menuDeselected(MenuEvent e) {
				removeAll();
			}

			public void menuSelected(MenuEvent e) {
				buildChildMenus(pos);
			}
		});
		none.setEnabled(false);
	}

	private void buildChildMenus(ProvidedObject[] po) {
		MiningPluginCollection collection = MiningPluginCollection
				.getInstance();
		boolean addToSelf = onlyContext && po.length == 1;

		for (int j = 0; j < po.length; j++) {
			boolean hasReader = false;

			for (int k = 0; !hasReader && k < po[j].getObjects().length; k++) {
				if ((po[j].getObjects()[k] instanceof LogReader)) {
					hasReader = true;
				}
			}
			if (!hasReader) {
				continue;
			}

			JMenu poMenu = addToSelf ? this : new JMenu(po[j].getName());

			for (int i = 0; i < collection.size(); i++) {
				MiningPlugin algorithm = (MiningPlugin) collection.get(i);
				if (algorithm == null) {
					poMenu.addSeparator();
				} else {
					poMenu.add(new JMenuItem(new MineAction(algorithm, po[j])));
				}
			}

			if (!addToSelf && poMenu.getItemCount() > 0) {
				add(poMenu);
			}
		}
		if (getItemCount() == 0) {
			add(none);
		}
		if (!onlyContext) {
			addSeparator();
			add(new OpenLogAction(desktop));
		}
	}
}
