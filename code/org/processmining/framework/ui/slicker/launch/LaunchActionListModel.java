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
package org.processmining.framework.ui.slicker.launch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LaunchActionListModel implements ListModel {

	protected ArrayList<ListDataListener> listDataListeners;
	protected LaunchActionList actionList;
	protected List<AbstractAction> currentActions;
	protected ActionFilter lastFilter = null;;

	public LaunchActionListModel(LaunchActionList actionList) {
		this.actionList = actionList;
		this.listDataListeners = new ArrayList<ListDataListener>();
		filter(null);
		this.actionList.addUpdateListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filter(lastFilter);
			}
		});
	}

	public void setActionList(LaunchActionList actionList, ActionFilter filter) {
		lastFilter = filter;
		this.actionList = actionList;
		filter(filter);
	}

	public void filter(ActionFilter filter) {
		lastFilter = filter;
		if (filter != null) {
			this.currentActions = actionList.filter(filter);
		} else {
			this.currentActions = actionList.getAllActions();
		}
		// notify listeners
		ListDataEvent event = new ListDataEvent(this,
				ListDataEvent.CONTENTS_CHANGED, 0, currentActions.size());
		for (ListDataListener listener : listDataListeners) {
			listener.contentsChanged(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener
	 * )
	 */
	public void addListDataListener(ListDataListener listener) {
		listDataListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		return this.currentActions.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return this.currentActions.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.swing.ListModel#removeListDataListener(javax.swing.event.
	 * ListDataListener)
	 */
	public void removeListDataListener(ListDataListener listener) {
		listDataListeners.remove(listener);
	}

}
