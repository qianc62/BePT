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

/*
 * Copyright (c) 2006 Eindhoven University of Technology
 * All rights reserved.
 */

package org.processmining.analysis.performance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.conformance.ConformanceAnalysisResults;
import org.processmining.analysis.conformance.ConformanceAnalysisSettings;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.Utils;

/**
 * Displays the analysis settings frame that precedes the actual performance
 * analysis. The user can define performance configuration settings here, such
 * as used timesort and bounds and colors of waiting time levels.
 * 
 * @see PerformanceConfiguration
 * @see PerformanceAnalysisResults
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */

public class PerformanceAnalysisSettings extends JPanel {

	// final attributes
	final PerformanceAnalysisPlugin myAlgorithm;
	final AnalysisInputItem[] myInput;
	final PetriNet myNet;
	final LogReader myLog;

	// This object contains the performance-settings used
	private PerformanceConfiguration options;

	private AnalysisResult currentResult;
	/**
	 * A list keeping a reference to each running execution thread, which is
	 * needed in order to check the status of each of them before building the
	 * results frame.
	 */
	private ArrayList myMethodExecutionThreads;

	// GUI related attributes
	private JPanel buttonsPanel = new JPanel(new BorderLayout()); // lower panel
	// containing
	// the
	// buttons
	private JPanel infoButtonsPanel = new JPanel(); // panel containing the log
	// summary and the
	// documentation button
	private JButton startButton = new JButton(new PerformanceCheckAction(this));
	private JButton docsButton = new JButton("Plugin documentation..."); // shows

	// the
	// plugin
	// documentation

	public PerformanceAnalysisSettings(PerformanceAnalysisPlugin algorithm,
			AnalysisInputItem[] input, PetriNet net, LogReader log) {
		myAlgorithm = algorithm;
		myInput = input;
		myNet = net;
		myLog = log;
		// build GUI
		try {
			jbInit();
			registerGuiActionListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Build the performance analysis settings GUI, in which the user can
	 * specify performance settings, before starting the actual analysis.
	 */
	private void jbInit() {
		// build the GUI based on the configuration object
		options = new PerformanceConfiguration(myNet);
		JPanel optionsPanel = options;
		optionsPanel.setBorder(BorderFactory.createLineBorder(new Color(150,
				150, 150), 1));
		JScrollPane scrolledOptions = new JScrollPane(optionsPanel);
		buttonsPanel.add(startButton, BorderLayout.EAST);
		infoButtonsPanel.add(docsButton);
		buttonsPanel.add(infoButtonsPanel, BorderLayout.WEST);
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		// pack
		this.setLayout(new BorderLayout());
		this.add(scrolledOptions, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.SOUTH);
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.validate();
		this.repaint();
	}

	/**
	 * Connects the GUI elements with functionality to create interaction.
	 */
	private void registerGuiActionListener() {
		// show plug-in documentation
		docsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(myAlgorithm);
			}
		});
		// start analysis
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startAnalysis();
			}
		});
	}

	/**
	 * Creates a LogReplayAnalysisMethod-object, using the input log, the input
	 * Petri net and a newly created PerformanceMeasurer-object, and afterwards
	 * calls on the method buildResultsFrame() to create the PerformanceGUI
	 */
	public void startAnalysis() {
		// prevent user to press button again while analysis is performed
		startButton.setEnabled(false);
		options.disableAll();
		myMethodExecutionThreads = new ArrayList();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	/**
	 * Called by each thread after having finished. Builds the result frame. <br>
	 * Note that all the method is
	 * <code>synchronized<code> as it will be accessed
	 * by concurrent threads executing the different analysis methods.
	 * 
	 * @param thread
	 *            the thread that just finished
	 */
	public synchronized void threadDone(
			PerformanceAnalysisMethodExecutionThread thread) {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		myMethodExecutionThreads.remove(thread);
		if (myMethodExecutionThreads.size() == 0) {
			buildResultsFrame();
		}
	}

	/**
	 * Sets currentResult to res
	 * 
	 * @param res
	 *            AnalysisResult
	 */
	public void setCurrentResult(AnalysisResult res) {
		currentResult = res;
	}

	/**
	 * Builds the {@link PerformanceAnalysisGUI PerformanceAnalysisGUI} frame.
	 */
	private void buildResultsFrame() {
		HashSet tempSet = new HashSet();
		tempSet.add(currentResult);
		// build PerformanceAnalysisUI
		PerformanceAnalysisGUI perfWindow = new PerformanceAnalysisGUI(tempSet,
				options.getManualSelected(), options.getBoundaries(), options
						.getColors(), options.getTimeDivider(), options
						.getTimesort(), options.getDecimalPlaces(), options
						.getAdvancedSettings().clone());

		perfWindow.setVisible(true);
		// spawn result window
		MainUI.getInstance().createAnalysisResultFrame(myAlgorithm, myInput,
				perfWindow);
		// sets the "Start Analysis" button active, so that it can be invoked
		// again
		startButton.setEnabled(true);
		options.enableAll();
	}

	/**
	 * Registers another custom thread with this settings frame. This is used in
	 * order to ensure every method execution being finished before their
	 * analysis results can be used for building the results frame.
	 * 
	 * @see #buildResultsFrame
	 * @param thread
	 *            the method execution thread to be added
	 */
	public synchronized void addMethodExecutionThread(
			PerformanceAnalysisMethodExecutionThread thread) {
		myMethodExecutionThreads.add(thread);
	}

	/**
	 * Invokes a seperate thread for each {@link LogReplayAnalysisMethod
	 * LogReplayAnalysisMethod} and builds the
	 * {@link PerformanceAnalysisResults PerformanceAnalysisResults} frame with
	 * the results obtained.
	 * 
	 * @see CustomSwingworker
	 */
	class PerformanceCheckAction extends AbstractAction {

		/**
		 * field containing the corresponding performance analysis settings
		 */
		private PerformanceAnalysisSettings settings;

		/**
		 * Builds this listener object.
		 * 
		 * @param frame
		 *            the calling settings frame containing the referenced
		 *            objects needed to carry out the analysis
		 */
		public PerformanceCheckAction(PerformanceAnalysisSettings frame) {
			super("<html><B>Start analysis<html>", Utils
					.getStandardIcon("media/Play24"));
			putValue(SHORT_DESCRIPTION, "Start analysis");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
			settings = frame;
		}

		/**
		 * Gets invoked as soon as the user presses the "start analysis" button.
		 * Starts each selected {@link AnalysisMethod AnalysisMethod} in a
		 * separate {@link CustomSwingworker CustomSwingworker} and waits for
		 * their return before building the {@link ConformanceAnalysisResults
		 * ConformanceAnalysisResults} frame. Note that the
		 * {@link AnalysisConfiguration AnalysisConfiguration} object is cloned
		 * before passed to the analyis methods in order to disconnect it from
		 * the {@link ConformanceAnalysisSettings ConformanceAnalysisSettings}
		 * frame, which remains to exist (and may invoke the analysis multiple
		 * times).
		 * 
		 * @param e
		 *            not used
		 */
		public void actionPerformed(ActionEvent e) {
			settings.startAnalysis();
			// start each analysis method in a separate thread to monitor
			// progress and be
			// able to abort it
			execute(new LogReplayAnalysisMethod(myNet, myLog,
					new PerformanceMeasurer(), new Progress("")));
		}

		/**
		 * Starts the given analyis method in a separate thread. This is
		 * necessar if one wants to be able that the method can be aborted by
		 * the user.
		 * 
		 * @param method
		 *            the AnalysisMethod to be performed
		 */
		public void execute(LogReplayAnalysisMethod method) {
			// start analysis method in a separate thread to monitor progress
			// and be
			// able to abort it
			/*
			 * NOTE modified by Anne in order to support restricted search depth
			 * (see AdvancedOptions.java)
			 */
			if (options.getAdvancedSettings()[4] >= 0) {
				method.setMaxDepth(options.getAdvancedSettings()[4]);
			}
			PerformanceAnalysisMethodExecutionThread worker = new PerformanceAnalysisMethodExecutionThread(
					method, options, settings);
			settings.addMethodExecutionThread(worker);
			try {
				worker.start();
			} catch (OutOfMemoryError err) {
				handleOutOfMem(err);
			}

		}

		/**
		 * Called if an analyis method runs out of memory while being executed,
		 * it propagates an error message to the user.
		 * 
		 * @param err
		 *            the OutOfMemoryError being passed from the caller
		 */
		public void handleOutOfMem(OutOfMemoryError err) {
			Message.add("Out of memory while analyzing");
		}
	}

}
