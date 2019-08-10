/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.analysis.streamscope;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.HeaderBar;
import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.slicker.ProgressPanel;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class StreamAnalysisPlugin extends JPanel implements AnalysisPlugin,
		Provider {

	protected LogReader log;
	protected JComponent view;
	protected StreamLogView streamLogView;
	protected ProgressPanel progressPanel;

	protected Color labelColor = new Color(40, 40, 40);

	public StreamAnalysisPlugin() {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		HeaderBar headerBar = new HeaderBar("Stream Scope");
		headerBar.setHeight(40);
		progressPanel = new ProgressPanel("Generating stream view...", true);
		progressPanel.setNote("ordering and clustering event classes on score");
		progressPanel.getProgressBar().setIndeterminate(true);
		this.view = progressPanel.getPanel();
		this.add(headerBar, BorderLayout.NORTH);
		this.add(this.view, BorderLayout.CENTER);
	}

	public void setup(final LogReader log) {
		Thread setupThread = new Thread() {
			public void run() {
				progressPanel.setMinMax(0, (log.getLogSummary()
						.getNumberOfAuditTrailEntries() / 3000));// +
				// log.getLogSummary().getLogEvents().size());
				progressPanel.setProgress(0);
				Thread.yield();
				streamLogView = new StreamLogView(log, progressPanel);
				remove(view);
				view = streamLogView;
				add(view, BorderLayout.CENTER);
				revalidate();
			}
		};
		setupThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {
		// look for LogReader instance to open GUI
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		log = null;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
				break;
			}
		}
		// open GUI
		if (log != null) {
			setup(log);
			return this;
		} else {
			// error!
			throw new AssertionError(
					"analysis input items do not contain a log reader instance!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.AnalysisPlugin#getInputItems()
	 */
	public AnalysisInputItem[] getInputItems() {
		// needs any instance of LogReader to work
		AnalysisInputItem[] items = { new AnalysisInputItem("Log") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean hasLog = false;
				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLog = true;
						break;
					}
				}
				return hasLog;
			}
		} };
		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Stream Scope";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		if (streamLogView == null) {
			return null;
		}
		ProvidedObject filteredLogObject = new ProvidedObject(
				"Filtered log (projected on stream scope)",
				new Object[] { streamLogView.getFilteredLog() });
		return new ProvidedObject[] { filteredLogObject };
	}

}
