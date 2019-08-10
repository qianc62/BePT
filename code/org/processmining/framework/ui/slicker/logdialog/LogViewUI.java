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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.deckfour.slickerbox.components.MouseOverLabel;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.slicker.LogView;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LogViewUI extends RoundedPanel {

	protected LogReader log = null;
	protected JComponent view = null;
	protected SlickerOpenLogSettings parent;

	public LogViewUI(SlickerOpenLogSettings parent) {
		super(15, 0, 10);
		this.setBackground(new Color(60, 60, 60));
		this.setLayout(new BorderLayout());
		MouseOverLabel info = new MouseOverLabel(
				"<html>Process instances are arranged vertically, "
						+ "shown as streams of triangular events. The color of events "
						+ "describes their frequency (green is highly frequent, red is low-frequent). "
						+ "Hover the mouse over events to view more information.</html>",
				11f, new Color(180, 180, 180));
		JPanel headerPanel = new JPanel();
		headerPanel.setOpaque(false);
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		headerPanel.add(info);
		headerPanel.add(Box.createHorizontalGlue());
		this.add(headerPanel, BorderLayout.SOUTH);
		this.parent = parent;
		this.view = new JPanel();
		this.view.setOpaque(false);
		this.add(this.view, BorderLayout.CENTER);
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

	protected void updateView() {
		LogReader uLog = parent.getLog();
		if (log == null || uLog.equals(log) == false) {
			log = uLog;
			if (this.view != null) {
				this.remove(this.view);
			}
			view = new LogView(log);
			this.add(view, BorderLayout.CENTER);
			revalidate();
			repaint();
		}
	}

}
