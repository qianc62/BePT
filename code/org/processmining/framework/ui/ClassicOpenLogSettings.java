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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.analysis.summary.LogSummaryUI;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.mining.DummyMiningPlugin;
import org.processmining.mining.DummyMiningResult;
import org.processmining.mining.MiningPlugin;
import java.util.Iterator;
import org.processmining.framework.log.rfb.LogData;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ClassicOpenLogSettings extends OpenLogSettings {

	protected LogFile file;
	protected LogSummary summary;
	protected JPanel algorithmPanel;
	protected DummyMiningResult result = null;
	protected JComponent dummyVis;

	protected JTabbedPane logUI;

	protected AdvancedLogFilterSettingsComponent advancedPanel = new AdvancedLogFilterSettingsComponent(
			ClassicOpenLogSettings.this);

	protected LogReader currentLog;

	protected JScrollPane logUIScrollPane;
	protected JSplitPane jSplitPane1 = new JSplitPane();
	protected JButton playButton;
	protected BorderLayout borderLayout1 = new BorderLayout();
	protected JPanel filterButtonPanel = new JPanel();
	protected BorderLayout borderLayout2 = new BorderLayout();
	protected DefaultLogFilterUI simpleUI;

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] res = new ProvidedObject[0];
		if (result != null) {
			res = result.getProvidedObjects();
		}
		ProvidedObject[] objects = new ProvidedObject[1 + res.length];
		String s = file.getShortName();

		if (s.indexOf(File.pathSeparator) > 0) {
			s = s.substring(s.lastIndexOf(File.pathSeparator) + 1);
		}
		if (s.indexOf(File.separator) > 0) {
			s = s.substring(s.lastIndexOf(File.separator) + 1);
		}
		objects[0] = new ProvidedObject("Filtered " + s,
				new Object[] { getSelectedLogReader() });

		for (int i = 0; i < res.length; i++) {
			objects[i + 1] = new ProvidedObject(
					"Previewed " + res[i].getName(), res[i].getObjects());
		}
		return objects;
	}

	/**
	 * Constructs a panel with settings for instantiating a LogReader object
	 * 
	 * @param file
	 *            LogFile
	 * @param summary
	 *            LogSummary
	 * @param algorithm
	 *            MiningPlugin
	 */
	public ClassicOpenLogSettings(LogFile file) {
		super(file);
		this.file = file;
		setupGui();
	}

	public ClassicOpenLogSettings(LogReader log) {
		super(log.getFile());
		this.file = log.getFile();
		this.currentLog = log;
		this.summary = log.getLogSummary();
		completeGui();
	}

	public void setupGui() {
		// decorate empty window with 'wait please..' teaser message
		ProgressPanel progress = new ProgressPanel("Loading");
		progress.setNote("Please wait while ProM is loading the log...");
		progress.setMinMax(0, 1000);
		progress.setProgress(0);
		getContentPane().setLayout(new BorderLayout());
		file.setProgressBar(progress.getProgressBar());
		// getContentPane().add(waitPanel);
		getContentPane().add(progress.getPanel(), BorderLayout.CENTER);
		pack();
		// load log file asynchronously in helper thread
		SwingWorker loadWorker = new SwingWorker() {

			protected LogReader reader = null;
			protected Exception exception = null;

			public Object construct() {
				try {
					reader = LogReaderFactory.createInstance(
							new DefaultLogFilter(DefaultLogFilter.INCLUDE),
							file);
					currentLog = reader;
					summary = currentLog.getLogSummary();
				} catch (Exception e) {
					// this usually signals that something has gone wrong while
					// reading the log;
					// we expect a malformed or otherwise erroneous input file,
					// so we abort here
					// and inform the user later (set flag).
					currentLog = null;
					exception = e;
					e.printStackTrace();
				}
				return null;
			}

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

	protected void errorReadingLog(Exception e) {
		Message.add("Error opening log file " + file.toString(), Message.ERROR);
		Message.add("Exception raised:\n" + e.toString(), Message.ERROR);
		JOptionPane
				.showMessageDialog(null, "Error reading log file:\n"
						+ e.toString(), "Error reading log!",
						JOptionPane.ERROR_MESSAGE);
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
			// Check if maybe for all processes, the option to keep that process
			// is set
			boolean set = true;
			Iterator it = currentLog.processIterator();
			while (set && it.hasNext()) {
				org.processmining.framework.log.Process p = (org.processmining.framework.log.Process) it
						.next();
				set = (p.getAttributes().containsKey(
						LogData.RETAIN_PROCESS_IF_EMPTY) && p.getAttributes()
						.get(LogData.RETAIN_PROCESS_IF_EMPTY).equalsIgnoreCase(
								"true"));
			}
			if (!set
					&& (showMalformedLogWarning("No process instances contained in log!") == JOptionPane.YES_OPTION)) {
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
		completeGui();
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

	protected void completeGui() {
		getContentPane().removeAll();
		// remember original size
		Dimension dim = this.getSize();
		algorithmPanel = new JPanel(new BorderLayout());
		logUI = new JTabbedPane();
		simpleUI = new DefaultLogFilterUI(summary);
		advancedPanel = new AdvancedLogFilterSettingsComponent(this);
		logUI.addTab("simple", simpleUI);
		logUI.addTab("advanced", advancedPanel);
		logUI.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (logUI.getSelectedComponent() == advancedPanel) {
					advancedPanel.buildFilterCombo(ClassicOpenLogSettings.this);
				}
			}
		});

		playButton = new JButton(new BuildLogReaderAction(
				ClassicOpenLogSettings.this));
		// Now read the whole log again!
		// playButton.doClick();

		JPanel panel = new JPanel(new BorderLayout());
		JPanel filterPanel = new JPanel(new BorderLayout());
		JPanel lowerPanel = new JPanel();
		JButton logInfoButton = new JButton("Summary entire log");

		logInfoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().createFrame(
						"Summary of entire " + getTitle(),
						new LogSummaryUI(summary));
			}
		});
		jSplitPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jSplitPane1.setResizeWeight(1.0);
		jSplitPane1.setOneTouchExpandable(true);
		filterPanel.setLayout(borderLayout1);
		lowerPanel.setMaximumSize(new Dimension(32767, 32767));
		lowerPanel.setLayout(borderLayout2);
		lowerPanel.add(filterButtonPanel, BorderLayout.WEST);

		filterButtonPanel.add(logInfoButton, null);
		filterButtonPanel.add(playButton);

		filterPanel.add(jSplitPane1, BorderLayout.CENTER);

		logUIScrollPane = new JScrollPane(logUI);
		logUIScrollPane.setMinimumSize(logUI.getMinimumSize());
		logUIScrollPane.setPreferredSize(logUI.getMinimumSize());
		jSplitPane1.add(logUIScrollPane, JSplitPane.LEFT);
		jSplitPane1.setDividerLocation((int) logUI.getPreferredSize()
				.getWidth() + 24);
		panel.add(lowerPanel, BorderLayout.SOUTH);

		getContentPane().setLayout(new BorderLayout());

		JLabel l = new JLabel(
				"<html>Please click \"Preview log settings\" to inspect <br>"
						+ "the current settings. This requires the log to be <br>"
						+ "read in total and might take some time.<br><br>It is "
						+ "<i>not necessary</i> to do this before you start mining!</html>");
		JPanel p = new JPanel();
		p.add(l);
		algorithmPanel.add(p, BorderLayout.CENTER);
		jSplitPane1.add(algorithmPanel, JSplitPane.RIGHT);

		panel.add(filterPanel, BorderLayout.CENTER);

		getContentPane().add(panel, BorderLayout.CENTER);

		this.pack();
		// restore original size
		this.setSize(dim);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.ui.OpenLogSettings#getSelectedLogReader()
	 */
	public LogReader getSelectedLogReader() {
		try {
			currentLog = LogReaderFactory.createInstance(getLogFilter(),
					currentLog);
			return currentLog;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.OpenLogSettings#getFile()
	 */
	public LogFile getFile() {
		return file;
	}

	public LogFilter getLogFilter() {
		if (logUI == null) {
			return null;
		}
		if (logUI.getSelectedComponent() == simpleUI) {
			return simpleUI.getLogFilter();
		} else {
			return advancedPanel.getLogFilter();
		}
	}

	/**
	 * Sets the process to be mined to the given one
	 * 
	 * @param mineProcess
	 *            String
	 */
	public void setProcess(String process) {
		simpleUI.selectProcess(process);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.OpenLogSettings#getLogSummary()
	 */
	public LogSummary getLogSummary() {
		return summary;
	}

}

class BuildLogReaderAction extends AbstractAction {
	private ClassicOpenLogSettings frame;
	private static final MiningPlugin algorithm = new DummyMiningPlugin();

	public BuildLogReaderAction(ClassicOpenLogSettings frame) {
		super("Preview log settings");
		putValue(SHORT_DESCRIPTION, "Apply current settings for visualization");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			execute(e);
		} catch (OutOfMemoryError err) {
			handleOutOfMem();
		}
	}

	public void execute(ActionEvent e) {

		setEnabled(false);

		final LogReader log = frame.getSelectedLogReader();

		/*
		 * Invoking start() on the SwingWorker causes a new Thread to be created
		 * that will call construct(), and then finished(). Note that finished()
		 * is called even if the worker is interrupted because we catch the
		 * InterruptedException in doWork().
		 */
		SwingWorker worker = new SwingWorker() {

			public Object construct() {
				Message.add("Applying settings for preview");
				try {
					MainUI.getInstance().addAction(algorithm,
							LogStateMachine.START, new Object[] { log });
					synchronized (log) {
						frame.result = (DummyMiningResult) algorithm.mine(log);
					}
					return frame.result;
				} catch (OutOfMemoryError err) {
					Message.add("Out of memory while mining");
					frame.result = null;
					return null;
				}
			}

			public void finished() {
				if (frame.result != null) {
					frame.algorithmPanel.removeAll();
					frame.dummyVis = frame.result.getVisualization();
					frame.algorithmPanel.add(frame.dummyVis);
					frame.algorithmPanel.validate();
					frame.algorithmPanel.repaint();
				}
				setEnabled(true);
			}
		};
		worker.start();
	}

	public void handleOutOfMem() {
		Message.add("Out of memory while mining");
	}

}
