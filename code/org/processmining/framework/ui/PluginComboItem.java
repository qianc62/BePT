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

package org.processmining.framework.ui;

import org.processmining.framework.plugin.Plugin;

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
public class PluginComboItem {

	private Plugin plugin;

	public PluginComboItem(Plugin pl) {
		this.plugin = pl;
	}

	public String toString() {
		return plugin.getName();
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof PluginComboItem) {
			// CHeck if the provided objects are the same, as well as the label.
			return plugin.equals(((PluginComboItem) o).plugin);
		} else {
			return false;
		}
		// return o != null && o.toString().equals(toString());
	}

	public Plugin getPlugin() {
		try {
			return plugin.getClass().newInstance();
		} catch (IllegalAccessException ex1) {
			Message
					.add(
							"Cannot create new instance of Contributor, using the old one.",
							Message.ERROR);
			return plugin;
		} catch (InstantiationException ex1) {
			Message
					.add(
							"Cannot create new instance of Contributor, using the old one.",
							Message.ERROR);
			return plugin;
		}
	}
}
