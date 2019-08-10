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
package org.processmining.framework.ui.slicker.logdialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.processmining.framework.log.LogReader;
import org.processmining.mining.cloudchamber.CloudChamberMiner;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class CloudChamberUI extends JPanel {

	protected LogReader log = null;
	protected SlickerOpenLogSettings parent;
	protected CloudChamberMiner miner;

	public CloudChamberUI(SlickerOpenLogSettings parent) {
		this.parent = parent;
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 3, 5));
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				updateView();
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});
	}

	public ActionListener getActivationListener() {
		ActionListener activationListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateView();
			}
		};
		return activationListener;
	}

	public LogReader getPreviewedLog() {
		if (miner != null) {
			return miner.getPreviewedLog();
		} else {
			return null;
		}
	}

	protected void updateView() {
		LogReader uLog = parent.getLog();
		if (log == null || uLog.equals(log) == false) {
			log = uLog;
			this.removeAll();
			miner = new CloudChamberMiner();
			miner.mine(parent.getLog()).getVisualization();
			this.add(miner.getCloudChamberPanel(false), BorderLayout.CENTER);
			revalidate();
			repaint();
		}
	}

}
