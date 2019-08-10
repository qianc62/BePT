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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.framework.util.StopWatch;
import org.processmining.mining.FileResult;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.NewStyleMiningPlugin;

/**
 * @author Peter van den Brand
 * @author Christian W. Guenther (christian at deckfour dot org)
 * @version 2
 */

public class MiningSettings extends JInternalFrame {

	private static final long serialVersionUID = -5017991258084616093L;

	protected static Color COLOR_OUTER_BG = new Color(130, 130, 130);
	protected static Color COLOR_INNER_BG = new Color(170, 170, 170);

	protected LogFile file;
	protected LogSummary summary;
	protected MiningPlugin algorithm;
	protected String objectName;
	protected JPanel algorithmPanel;
	protected LogReader sourceLog;
	protected JButton playButton;

	/**
	 * Constructs a panel with settings for executing a MiningPlugin. It assumes
	 * the given LogSummary is a summary of the log, so it does not read the
	 * log. The right hand side is given by algorithm.getOptionsPanel(). Note
	 * that the log is assumed to be open through the LogReader object. Hence no
	 * changes can be made to the log.
	 * 
	 * @param log
	 *            LogReader
	 * @param name
	 *            String
	 * @param algorithm
	 *            MiningPlugin
	 */
	public MiningSettings(LogReader log, String name,
			final MiningPlugin algorithm) {
		super("Settings for mining " + name + " using " + algorithm.getName(),
				true, true, true, true);
		this.objectName = name;
		this.file = null;
		this.summary = log.getLogSummary();
		this.algorithm = algorithm;
		this.algorithmPanel = algorithm.getOptionsPanel(summary);
		this.sourceLog = log;

		if (algorithmPanel == null) {
			setVisible(false);
			mine();
		} else {
			initialize();
			setVisible(true);
		}
	}

	public LogReader getLogReader() {
		if (sourceLog != null) {
			return sourceLog;
		}
		try {
			sourceLog = LogReaderFactory.createInstance(getLogFilter(),
					sourceLog);
			return sourceLog;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void initialize() {

		MainUI.getInstance().addAction(algorithm, LogStateMachine.SCHEDULE,
				null);

		playButton = new AutoFocusButton("start mining");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				playButton.setEnabled(false);
				mine();
			}
		});

		JButton helpButton = new SlickerButton("Help");
		helpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(algorithm);
			}
		});

		JPanel rootPanel = new JPanel();
		rootPanel.setBorder(BorderFactory.createEmptyBorder());
		rootPanel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		rootPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		southPanel.setBackground(COLOR_OUTER_BG);
		southPanel.add(helpButton);
		southPanel.add(Box.createHorizontalGlue());
		southPanel.add(playButton);
		rootPanel.add(southPanel, BorderLayout.SOUTH);

		Color defaultBackground = (new JPanel()).getBackground();
		// look and feel injection
		if (algorithmPanel.isOpaque() == false
				|| algorithmPanel.getBackground().equals(defaultBackground)) {
			// assimilate algorithm configuration panel to look and feel
			SlickerSwingUtils.injectBackgroundColor(algorithmPanel,
					COLOR_INNER_BG);
			RoundedPanel enclosurePanel = new RoundedPanel(14, 8, 3);
			enclosurePanel.setLayout(new BorderLayout());
			enclosurePanel.setBackground(COLOR_INNER_BG);
			enclosurePanel.add(algorithmPanel, BorderLayout.CENTER);
			JPanel backdropPanel = new JPanel();
			backdropPanel.setBorder(BorderFactory.createEmptyBorder());
			backdropPanel.setBackground(COLOR_OUTER_BG);
			backdropPanel.setLayout(new BorderLayout());
			backdropPanel.add(enclosurePanel, BorderLayout.CENTER);
			scrollPane.getViewport().setView(backdropPanel);
			scrollPane.setBackground(COLOR_OUTER_BG);
			javax.swing.JScrollBar hBar = scrollPane.getHorizontalScrollBar();
			JScrollBar vBar = scrollPane.getVerticalScrollBar();
			hBar.setUI(new SlickerScrollBarUI(hBar, COLOR_OUTER_BG, new Color(
					40, 40, 40), new Color(70, 70, 70), 4, 13));
			vBar.setUI(new SlickerScrollBarUI(vBar, COLOR_OUTER_BG, new Color(
					40, 40, 40), new Color(70, 70, 70), 4, 13));
			// add header bar
			HeaderBar headerBar = new HeaderBar(algorithm.getName());
			headerBar.setHeight(40);
			rootPanel.add(headerBar, BorderLayout.NORTH);
			this.setBackground(COLOR_OUTER_BG);
		} else {
			// simply add to viewport
			scrollPane.getViewport().setView(algorithmPanel);
			// adjust south panel background
			southPanel.setBackground(algorithmPanel.getBackground());
			this.setBackground(algorithmPanel.getBackground());
		}

		this.setContentPane(rootPanel);
		pack();
	}

	public LogFile getFile() {
		return file;
	}

	public LogSummary getLogSummary() {
		return summary;
	}

	public MiningPlugin getAlgorithm() {
		return algorithm;
	}

	public LogFilter getLogFilter() {
		return sourceLog.getLogFilter();
	}

	protected void mine() {
		/*
		 * Invoking start() on the SwingWorker causes a new Thread to be created
		 * that will call construct(), and then finished(). Note that finished()
		 * is called even if the worker is interrupted because we catch the
		 * InterruptedException in doWork().
		 */
		final MiningPlugin plugin = this.algorithm;
		final LogReader log = getLogReader();
		SwingWorker worker = new SwingWorker() {
			MiningResult result;
			StopWatch timer = new StopWatch();

			public Object construct() {
				Message.add("Start process mining.");
				try {
					MainUI.getInstance().addAction(plugin,
							LogStateMachine.START, new Object[] { log });
					synchronized (log) {
						timer.start();
						if (plugin instanceof NewStyleMiningPlugin) {
							result = ((NewStyleMiningPlugin) plugin).mine(log,
									algorithmPanel);
						} else {
							result = plugin.mine(log);
						}
					}
					return result;
				} catch (OutOfMemoryError err) {
					Message.add("Out of memory while mining");
					return null;
				}
			}

			public void finished() {
				timer.stop();
				Message.add("Mining duration: " + timer.formatDuration());

				if (result != null && result instanceof FileResult) {
					FileResult fileResult = (FileResult) result;
					String filename = Utils.saveFileDialog(
							MainUI.getInstance(), new GenericFileFilter(
									fileResult.getExtension()));

					if (filename != null && !filename.equals("")) {
						try {
							FileOutputStream out = new FileOutputStream(
									filename);

							fileResult.saveResult(out);
							out.close();
							Message.add(filename + " written.");
						} catch (IOException ex) {
							JOptionPane.showMessageDialog(MainUI.getInstance(),
									"IO Error: " + ex.getMessage(), "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				} else if (result != null) {
					MainUI.getInstance().addAction(
							plugin,
							LogStateMachine.COMPLETE,
							(result instanceof Provider) ? ((Provider) result)
									.getProvidedObjects() : null);
					MainUI.getInstance().createVisualizationFrame(
							"Results - " + algorithm.getName() + " on "
									+ objectName, result, plugin);
				} else {
					MainUI.getInstance().addAction(plugin,
							LogStateMachine.ATE_ABORT,
							new Object[] { log, result });
				}
				Message.add("Process mining finished.");
				if (MiningSettings.this.playButton != null) {
					MiningSettings.this.playButton.setEnabled(true);
				}
			}
		};
		worker.start();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JInternalFrame#setSelected(boolean)
	 */
	@Override
	public void setSelected(boolean selected) throws PropertyVetoException {
		super.setSelected(selected);
		if (selected == true) {
			playButton.requestFocusInWindow();
		}
	}

}
