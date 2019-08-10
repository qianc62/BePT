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
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.slicker.ProgressPanel;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class InspectorUI extends JPanel {

	protected SlickerOpenLogSettings parent;
	protected CloudChamberUI cloudChamberUI;
	protected LogViewUI logViewUI;
	protected LogPreviewUI logPreviewUI;
	protected LogReader log = null;

	protected ProgressPanel progress;
	protected FlatTabbedPane tabPane;

	/**
	 * @param title
	 * @param fgColor
	 * @param bgColor
	 * @param titleColor
	 */
	public InspectorUI(SlickerOpenLogSettings parent) {
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		progress = new ProgressPanel("Updating log...", false);
		this.add(progress.getPanel(), BorderLayout.CENTER);
		tabPane = new FlatTabbedPane("Log inspector", new Color(240, 240, 240,
				230), new Color(180, 180, 180, 120), new Color(220, 220, 220,
				150));
		this.parent = parent;
		this.logPreviewUI = new LogPreviewUI(parent);
		tabPane.addTab("Browser", logPreviewUI);
		this.logViewUI = new LogViewUI(parent);
		tabPane.addTab("Explorer", logViewUI);
		this.cloudChamberUI = new CloudChamberUI(parent);
		tabPane.addTab("Dotplot", cloudChamberUI);
	}

	public LogReader getBrowserSelectionLog() {
		return this.logPreviewUI.getResultReader();
	}

	public LogReader getCloudChamberPreviewedLog() {
		return this.cloudChamberUI.getPreviewedLog();
	}

	public ActionListener getActivationListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateLog();
			}
		};
	}

	protected void updateLog() {
		removeAll();
		progress.setNote("Filtering log using "
				+ parent.getActiveLogFilterName());
		add(progress.getPanel(), BorderLayout.CENTER);
		revalidate();
		repaint();
		Thread updateThread = new Thread() {
			public void run() {
				Thread.yield();
				parent.getLog();
				removeAll();
				add(tabPane, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		};
		updateThread.start();
		/*
		 * SwingWorker worker = new SwingWorker() {
		 * 
		 * public Object construct() { progress.setNote("Filtering log using " +
		 * parent.getActiveLogFilterName()); add(progress.getPanel(),
		 * BorderLayout.CENTER); //progress.setProgress(0); revalidate();
		 * repaint(); return parent.getLog(); }
		 * 
		 * public void finished() { removeAll(); add(tabPane,
		 * BorderLayout.CENTER); revalidate(); repaint(); } }; worker.start();
		 */
	}

}
