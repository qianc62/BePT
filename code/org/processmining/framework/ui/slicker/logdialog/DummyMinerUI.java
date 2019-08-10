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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.mining.DummyMiningPlugin;
import org.processmining.mining.DummyMiningResult;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class DummyMinerUI extends RoundedPanel {

	protected Color colorBg = new Color(160, 160, 160);
	protected LogReader log = null;
	protected SlickerOpenLogSettings parent = null;
	protected JComponent view = null;
	protected DummyMiningResult result = null;

	public DummyMinerUI(SlickerOpenLogSettings parent) {
		super(10, 0, 5);
		this.parent = parent;
		this.setBackground(colorBg);
		this.setLayout(new BorderLayout());
		view = new JPanel();
		view.setOpaque(false);
		this.add(view, BorderLayout.CENTER);
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

	public LogReader getResultReader() {
		if (this.result != null) {
			return this.result.getLogReader();
		} else {
			return null;
		}
	}

	public ActionListener getActivationListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateView();
			}
		};
	}

	protected void updateView() {
		LogReader uLog = parent.getLog();
		if (log == null || uLog.equals(log) == false) {
			log = uLog;
			SwingWorker worker = new SwingWorker() {

				public Object construct() {
					Message.add("Applying settings for preview");
					try {
						synchronized (log) {
							result = (DummyMiningResult) (new DummyMiningPlugin())
									.mine(log);
						}
						return result;
					} catch (OutOfMemoryError err) {
						Message.add("Out of memory while mining");
						result = null;
						return null;
					}
				}

				public void finished() {
					if (result != null) {
						removeAll();
						view = result.getVisualization();
						SlickerSwingUtils.injectBackgroundColor(view, colorBg);
						add(view, BorderLayout.CENTER);
						revalidate();
						repaint();
					}
				}
			};
			worker.start();
		}
	}

}
