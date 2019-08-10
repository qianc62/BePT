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
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.IconVerticalTabbedPane;
import org.deckfour.slickerbox.components.SlickTabbedPane;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.OpenLogSettings;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.StopWatch;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class SlickerOpenLogSettings extends OpenLogSettings {

	private static final long serialVersionUID = 4258730373866637365L;

	protected enum ActiveFilter {
		INITIAL, SIMPLE, ADVANCED;
	}

	protected ActiveFilter activeFilter = ActiveFilter.INITIAL;
	protected SlickerSimpleFilterSettings simpleFilterSettings;
	protected SlickerAdvancedLogFilterConfiguration advancedFilterSettings;

	protected InspectorUI inspectorUI;

	// icons
	protected Image dashboardIcon = (new ImageIcon(
			"images/slicker/dashboard48.png")).getImage();
	protected Image filterIcon = (new ImageIcon("images/slicker/filter48.png"))
			.getImage();
	protected Image inspectorIcon = (new ImageIcon(
			"images/slicker/inspector48.png")).getImage();
	protected Image summaryIcon = (new ImageIcon(
			"images/slicker/summary48-2.png")).getImage();

	// data attributes
	protected LogFile logFile;
	protected LogReader log;
	protected LogReader currentLog;
	protected LogFilter currentFilter;
	protected LogSummary summary;

	// UI elements
	protected JPanel root;
	protected HeaderBar header;
	protected JComponent view;
	protected ProgressPanel progress;
	protected SlickTabbedPane tabbedPane;

	/**
	 * Creates a new open log settings with slicker look
	 * 
	 * @param logFile
	 *            Log file to wrap around
	 */
	public SlickerOpenLogSettings(LogFile logFile) {
		super(logFile);
		this.logFile = logFile;
		initialize();
		loadLogFromFile();
	}

	public SlickerOpenLogSettings(LogReader log) {
		super(log.getFile());
		this.logFile = log.getFile();
		this.log = log;
		this.currentLog = log;
		this.summary = log.getLogSummary();
		initialize();
		// completeGui();
	}

	/**
	 * loads the log and initializes this component
	 */
	protected void initialize() {
		String promDir = System.getProperty("user.dir");
		if (promDir.endsWith(".")) {
			promDir = promDir.substring(0, promDir.length() - 1);
		}
		if (promDir.endsWith(System.getProperty("file.separator")) == false) {
			promDir += System.getProperty("file.separator");
		}
		dashboardIcon = (new ImageIcon(promDir
				+ "images/slicker/dashboard48.png")).getImage();
		filterIcon = (new ImageIcon(promDir + "images/slicker/filter48.png"))
				.getImage();
		inspectorIcon = (new ImageIcon(promDir
				+ "images/slicker/inspector48.png")).getImage();
		summaryIcon = (new ImageIcon(promDir + "images/slicker/summary48-2.png"))
				.getImage();
		// shorten title if necessary
		String title = logFile.getShortName();
		this.setBackground(new Color(40, 40, 40));
		// setup header bar and progress panel for loading
		header = new HeaderBar(title);
		header.setHeight(40);
		// compile layout
		root = new JPanel();
		root.setBorder(BorderFactory.createEmptyBorder());
		root.setLayout(new BorderLayout());
		root.add(header, BorderLayout.NORTH);
		view = new JPanel();
		view.setOpaque(false);
		root.add(view, BorderLayout.CENTER);
		this.setContentPane(root);
		pack();
	}

	protected void loadLogFromFile() {
		progress = new ProgressPanel("Loading");
		progress.setNote("Please wait while ProM is loading the log...");
		progress.setMinMax(0, 1000);
		progress.setProgress(0);
		// this is necessary to get log reading feedback
		logFile.setProgressBar(progress.getProgressBar());
		root.remove(view);
		view = progress.getPanel();
		root.add(view, BorderLayout.CENTER);
		revalidate();
		pack();
		// load log file asynchronously in helper thread
		SwingWorker loadWorker = new SwingWorker() {
			protected Throwable exception = null;

			// called on thread starting
			public Object construct() {
				try {
					log = LogReaderFactory.createInstance(new DefaultLogFilter(
							DefaultLogFilter.INCLUDE), logFile);
					currentLog = log;
					summary = log.getLogSummary();
				} catch (Throwable e) {
					// this usually signals that something has gone wrong while
					// reading the log;
					// we expect a malformed or otherwise erroneous input file,
					// so we abort here
					// and inform the user later (set flag).
					log = null;
					exception = e;
					e.printStackTrace();
				}
				return null;
			}

			// called after thread has finished
			public void finished() {
				if (exception == null) {
					logReadingFinished();
				} else {
					errorReadingLog(exception);
				}
			}

		};
		loadWorker.start();
	}

	protected void errorReadingLog(Throwable e) {
		String msg = e.getCause() != null ? e.getCause().getMessage() : e
				.getMessage();

		// close the progress bar window
		this.setVisible(false);

		Message.add("Error opening log file " + logFile.toString(),
				Message.ERROR);
		Message.add("Exception raised:\n" + msg, Message.ERROR);
		JOptionPane.showMessageDialog(null, "Error reading log file:\n" + msg,
				"Error reading log!", JOptionPane.ERROR_MESSAGE);
	}

	protected void logReadingFinished() {
		// log sanity checks;
		// notify user if the log is awkward / does miss crucial information
		if (summary.getProcesses().length == 0) {
			if (showMalformedLogWarning("No processes contained in log!") == JOptionPane.YES_OPTION) {
				dispose();
				return;
			}
		} else if (summary.getNumberOfProcessInstances() == 0) {
			if (showMalformedLogWarning("No process instances contained in log!") == JOptionPane.YES_OPTION) {
				dispose();
				return;
			}
		} else if (summary.getNumberOfAuditTrailEntries() == 0) {
			if (showMalformedLogWarning("No audit trail entries contained in log!") == JOptionPane.YES_OPTION) {
				dispose();
				return;
			}
		}
		// if we arrive here, the log shall be displayed
		// completeGui();
	}

	protected void completeGui() {
		GradientPanel backPanel = new GradientPanel(new Color(130, 130, 130),
				new Color(40, 40, 40));
		backPanel.setLayout(new BorderLayout());
		// compile filter panel
		FlatTabbedPane filterPane = new FlatTabbedPane("Log filter", new Color(
				240, 240, 240, 230), new Color(180, 180, 180, 120), new Color(
				220, 220, 220, 150));
		simpleFilterSettings = new SlickerSimpleFilterSettings(log,
				new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						activeFilter = ActiveFilter.SIMPLE;
						currentFilter = simpleFilterSettings.getLogFilter();
					}
				});
		filterPane.addTab("Simple", simpleFilterSettings);
		advancedFilterSettings = new SlickerAdvancedLogFilterConfiguration(log,
				new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						LogFilter aFilter = advancedFilterSettings
								.getLogFilter();
						// advanced filter has a real setting
						activeFilter = ActiveFilter.ADVANCED;
						currentFilter = aFilter;
						// delete current log (no safe comparison for advanced
						// log filters)
						currentLog = null;
					}
				});
		filterPane.addTab("Advanced", advancedFilterSettings);
		// initialize
		activeFilter = ActiveFilter.INITIAL;
		currentLog = log;
		// compile summary panel
		LogSummaryUI summaryPane = new LogSummaryUI(this);
		// compile inspector panel
		inspectorUI = new InspectorUI(this);
		// compile root panel
		IconVerticalTabbedPane iconTabs = new IconVerticalTabbedPane(new Color(
				230, 230, 230, 210), new Color(20, 20, 20, 160));
		iconTabs.addTab("Dashboard", dashboardIcon, new LogDashboardUI(this));
		iconTabs.addTab("Filter", filterIcon, filterPane);
		iconTabs.addTab("Inspector", inspectorIcon, inspectorUI, inspectorUI
				.getActivationListener());
		iconTabs.addTab("Summary", summaryIcon, summaryPane);
		backPanel.add(iconTabs, BorderLayout.CENTER);
		root.remove(view);
		view = backPanel;
		root.add(view, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	protected int showMalformedLogWarning(String error) {
		String options[] = { "Discard log", "Open log anyway" };
		String message = "\nThe log file you have tried to open appears to have\n"
				+ "the following problem:\n\n"
				+ error
				+ "\n\n"
				+ "ProM may not be able to use this file, and you may\n"
				+ "experience awkward behavior and results using it.\n"
				+ "Do you want to discard this file, or open it anyway?\n\n";
		return JOptionPane.showOptionDialog(null, message,
				"Input log appears to be invalid!", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);
	}

	public LogReader getLog() {
		try {
			if (this.currentLog == null) {
				// no filtered log set
				if (this.currentFilter == null) {
					// no filter set, use original
					this.currentLog = this.log;
				} else {
					// filter original
					this.currentLog = LogReaderFactory.createInstance(
							this.currentFilter, this.log);
				}
			} else {
				// check if current log has the current filter, otherwise create
				// new filtered logs
				if (this.currentFilter != null
						&& this.currentLog.getLogFilter().equals(
								this.currentFilter) == false) {
					this.currentFilter.setLowLevelFilter(this.log
							.getLogFilter());
					this.currentLog = LogReaderFactory.createInstance(
							this.currentFilter, this.log);
				}
			}
			// safety check: is the log empty
			if (this.currentLog.numberOfInstances() == 0) {
				this.currentLog = log;
			}
			return this.currentLog;
		} catch (Throwable e) {
			// oops
			e.printStackTrace();
			return null;
		}
	}

	public String getActiveLogFilterName() {
		if (this.activeFilter == ActiveFilter.INITIAL
				|| this.currentFilter == null) {
			return "unfiltered log";
		} else if (this.activeFilter == ActiveFilter.SIMPLE) {
			return "simple log filter";
		} else if (this.activeFilter == ActiveFilter.ADVANCED) {
			return "advanced log filter";
		} else {
			return "unknown";
		}
	}

	public LogReader getOriginalLog() {
		return this.log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		StopWatch timer = new StopWatch();
		timer.start();
		if (log == null || inspectorUI == null) {
			// loading in progress or failed
			return new ProvidedObject[] {};
		}
		// trigger current log update
		getLog();
		String logName = logFile.getShortName();
		ArrayList<ProvidedObject> providedObjects = new ArrayList<ProvidedObject>();
		if (currentLog != log) {
			String filterName = "Simple filter";
			if (activeFilter.equals(ActiveFilter.ADVANCED)) {
				filterName = "Advanced filter";
			}
			providedObjects.add(new ProvidedObject("Filtered " + logName + " ("
					+ filterName + ")", new Object[] { currentLog }));
		}
		LogReader browserSelection = inspectorUI.getBrowserSelectionLog();
		if (browserSelection != null) {
			providedObjects
					.add(new ProvidedObject("Previewed " + logName
							+ " (browser inspector)",
							new Object[] { browserSelection }));
		}
		LogReader cloudchamber = inspectorUI.getCloudChamberPreviewedLog();
		if (cloudchamber != null) {
			providedObjects.add(new ProvidedObject("Previewed " + logName
					+ " (dotplot inspector)", new Object[] { cloudchamber }));
		}
		providedObjects.add(new ProvidedObject("Raw " + logName
				+ " (unfiltered)", new Object[] { log }));
		timer.stop();
		Message.add("Filtered log in " + timer.formatDuration());
		return providedObjects.toArray(new ProvidedObject[providedObjects
				.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.OpenLogSettings#getFile()
	 */
	@Override
	public LogFile getFile() {
		return this.logFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.OpenLogSettings#getLogFilter()
	 */
	@Override
	public LogFilter getLogFilter() {
		return this.currentFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.OpenLogSettings#getLogSummary()
	 */
	@Override
	public LogSummary getLogSummary() {
		return this.summary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.ui.OpenLogSettings#getSelectedLogReader()
	 */
	@Override
	public LogReader getSelectedLogReader() {
		return this.getLog();
	}

}
