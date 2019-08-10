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
package org.processmining.analysis.conformance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.analysis.AnalysisInputItem;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.hlprocess.gui.HLProcessGui;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethod;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.models.petrinet.algorithms.logReplay.LogReplayAnalysisMethod;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * Displays the analysis settings frame that precedes the actual conformance
 * analysis. The user are offered certain analysis options, which are specified
 * in a logical way using a hierarchical {@link AnalysisConfiguration
 * AnalysisConfiguration} object. The analysis categories themselves are
 * associated to {@link AnalysisMethod AnalysisMethod} objects, which are an
 * orthorgonal concept reflecting the kind of technique that is used to collect
 * the data needed for a specific metric. The actual analysis will only be
 * carried out for those methods that were selected by the user.
 * 
 * @see ConformanceAnalysisResults
 * @see AnalysisMethodExecutionThread
 * 
 * @author arozinat
 */
public class ConformanceAnalysisSettings extends JPanel implements
		ThreadNotificationTarget, GuiNotificationTarget {

	/**
	 * Required for a serializable class (generated quickfix). Not directly
	 * used.
	 */
	private static final long serialVersionUID = 5456767348699358296L;

	/**
	 * This configuration object specifies the available analysis options in a
	 * hierarchical way and displays them automatically to the user whithin the
	 * settings frame.
	 */
	private AnalysisConfiguration analysisOptions;

	/**
	 * This is a place holder for a cloned object of the current configuration,
	 * which is needed in order to decouple the results frame from the settings
	 * frame (which remains visible and might spawn another results frame).
	 */
	private AnalysisConfiguration resultFrameOptions;

	/**
	 * A list keeping a reference to each running execution thread, which is
	 * needed in order to check the status of each of them before building the
	 * results frame.
	 */
	private ArrayList<AnalysisMethodExecutionThread> myMethodExecutionThreads;

	final ConformanceAnalysisPlugin myAlgorithm;
	final AnalysisInputItem[] myInput;
	final PetriNet myNet;
	final LogReader myLog;

	// GUI related attributes
	protected boolean isPainted = false;
	private JPanel upperPanel = new JPanel(); // upper panel containing the
	// invitation to select analysis
	// options
	private JPanel restrictionPanel = new JPanel(); // panel containing the
	// depth restriction check
	// box and the spinner
	private JPanel buttonsPanel = new JPanel(); // lower panel containing the
	// buttons
	private GuiPropertyStringTextarea optionsStatement = new GuiPropertyStringTextarea(
			"Furthermore, you can choose which kind of analysis you would like to perform. "
					+ "The computation process may speed up if you deselect the categories (fitness, precision, structure), or specific metrics, in which you are not interested. ");
	private GuiPropertyStringTextarea restrictionStatement = new GuiPropertyStringTextarea(
			"The Conformance Checker has automatically determined the maximum search depth "
					+ "needed to transparently fire invisible tasks during the replay of your model (if any). In the case of computability problems, one might want to decrease the search depth to get a response "
					+ "(setting it to 0 will result in not searching at all). However, this is likely to yield pessimistic measurements.");
	protected JButton startButton;// starts the analysis
	private JButton docsButton = new SlickerButton("Help..."); // shows the
	// plugin
	// documentation
	private GUIPropertyBoolean isRestricted;
	public GUIPropertyInteger restrictedDepth;
	protected GUIPropertyBoolean bestInvisibles = new GUIPropertyBoolean(
			"Choose best shortest sequence of invisible tasks",
			"<html>Tell Log replay to look into future for choosing best shortest path of invisible tasks<br>(try this if you want to replay EPC models containing OR splits after conversion).<html>",
			false, this);

	/**
	 * Creates the analysis settings frame.
	 */
	public ConformanceAnalysisSettings(ConformanceAnalysisPlugin algorithm,
			AnalysisInputItem[] input, PetriNet net, LogReader log) {
		myAlgorithm = algorithm;
		myInput = input;
		myNet = net;
		myLog = log;
		// Builds the conformance-specific configuration object, which links the
		// semantic view at the
		// possible analysis options with the analysis methods needed to compute
		// them
		analysisOptions = new ConformanceAnalysisConfiguration();
		// automatically with estimated search depth needed for replaying this
		// model
		int searchDepth = MaximumSearchDepthDiagnosis
				.determineMaximumSearchDepth(net);
		if (searchDepth == -1) {
			isRestricted = new GUIPropertyBoolean(
					"Restrict search depth for invisible tasks",
					"Restricts search for sequences of invisible tasks that might enable another task during log replay",
					false, this);
			restrictedDepth = new GUIPropertyInteger(
					"Maximum depth: ",
					"Corresponds to the maximum length of a sequence of invisible tasks that can be found",
					0, 0, 100);
		} else {
			isRestricted = new GUIPropertyBoolean(
					"Restrict search depth for invisible tasks",
					"Restricts search for sequences of invisible tasks that might enable another task during log replay",
					true, this);
			restrictedDepth = new GUIPropertyInteger(
					"Maximum depth: ",
					"Corresponds to the maximum length of a sequence of invisible tasks that can be found",
					searchDepth, 0, 100);
		}
		// build GUI
		jbInit();
		// connect functionality to GUI elements
		registerGuiActionListener();
		// updateGui
		updateGUI();
	}

	// /////////////////// GUI CONSTRUCTION
	// /////////////////////////////////////

	/**
	 * Build the Conformance Analysis Settings GUI, that is to let people
	 * specify which kind of conformance analysis they want.
	 */
	private void jbInit() {
		startButton = new AutoFocusButton("Start Analysis");
		// build the GUI based on the configuration object
		JPanel optionsPanel = analysisOptions.getOptionsPanel(null, 0, 0,
				analysisOptions.getTreeDepth(0, 0));
		optionsPanel.setBorder(BorderFactory.createLineBorder(new Color(150,
				150, 150), 1));
		JScrollPane scrolledOptions = new JScrollPane(optionsPanel);

		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		buttonsPanel.add(docsButton);
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(startButton);
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
		upperPanel.add(restrictionStatement.getPropertyPanel());
		restrictionPanel.setLayout(new BoxLayout(restrictionPanel,
				BoxLayout.X_AXIS));
		restrictionPanel.add(isRestricted.getPropertyPanel());
		restrictionPanel.add(Box.createHorizontalGlue());
		restrictionPanel.add(restrictedDepth.getPropertyPanel());
		upperPanel.add(restrictionPanel);
		upperPanel.add(bestInvisibles.getPropertyPanel());
		upperPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		upperPanel.add(optionsStatement.getPropertyPanel());

		// pack them
		this.setLayout(new BorderLayout());
		this.add(upperPanel, BorderLayout.NORTH);
		this.add(scrolledOptions, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.SOUTH);
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		this.setBackground(HLProcessGui.bgColor);
		SlickerSwingUtils.injectTransparency(this);

		this.validate();
		this.repaint();
	}

	/**
	 * Connect GUI elements like, e.g., buttons with functionality to create
	 * interaction.
	 */
	private void registerGuiActionListener() {
		// show plug-in documentation
		docsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(myAlgorithm);
			}
		});
		startButton.addActionListener(new ActionListener() {
			/**
			 * Gets invoked as soon as the user presses the "start analysis"
			 * button. Starts each selected {@link AnalysisMethod
			 * AnalysisMethod} in a separate {@link CustomSwingworker
			 * CustomSwingworker} and waits for their return before building the
			 * {@link ConformanceAnalysisResults ConformanceAnalysisResults}
			 * frame. Note that the {@link AnalysisConfiguration
			 * AnalysisConfiguration} object is cloned before passed to the
			 * analyis methods in order to disconnect it from the
			 * {@link ConformanceAnalysisSettings ConformanceAnalysisSettings}
			 * frame, which remains to exist (and may invoke the analysis
			 * multiple times).
			 * 
			 * @param e
			 *            not used
			 */
			public void actionPerformed(ActionEvent e) {
				initAnalysis();
				// start each analysis method in a separate thread to monitor
				// progress and be
				// able to abort it
				LogReplayAnalysisMethod logReplayAnalysis = new LogReplayAnalysisMethod(
						myNet, myLog, new ConformanceMeasurer(), new Progress(
								"Log replay analysis.."));
				if (restrictedDepth.isEnabled() == true) {
					logReplayAnalysis.setMaxDepth(restrictedDepth.getValue());
				}
				if (bestInvisibles.getValue() == true) {
					logReplayAnalysis.findBestShortestSequence = true;
				}
				execute(logReplayAnalysis);
				execute(new StructuralAnalysisMethod(myNet));
				execute(new StateSpaceExplorationMethod(myNet, new Progress(
						"State space analysis..")));
				/** TEMPLATE: add further analysis methods here.. */
			}
		});
	}

	/**
	 * Will be called as soon the user restricts or unrestricts the search depth
	 * for invisible tasks during log replay. <br>
	 * Per default the restriction is selected and a depth value can be
	 * provided. However, as soon as the user deselects the depth limitation the
	 * corresponding spinner will be disabled (and it will be enabled as soon as
	 * the restriction is selected again).
	 */
	public void updateGUI() {
		if (isRestricted.getValue() == false) {
			restrictedDepth.disable();
		} else {
			restrictedDepth.enable();
		}
	}

	// //////////////// ANALYSIS RELATED METHODS
	// //////////////////////////////////

	/**
	 * Starts the given analyis method in a separate thread. This is necessar if
	 * one wants to be able that the method can be aborted by the user.
	 * 
	 * @param method
	 *            the AnalysisMethod to be performed
	 */
	public void execute(AnalysisMethod method) {
		// start analysis method in a separate thread to monitor progress
		// and be
		// able to abort it
		AnalysisMethodExecutionThread worker = new AnalysisMethodExecutionThread(
				method, this);
		this.addMethodExecutionThread(worker);
		try {
			worker.start();
		} catch (OutOfMemoryError err) {
			Message.add("Out of memory while analyzing");
		}
	}

	/**
	 * Prepares the actual analysis. Should be called each time the
	 * "Start Analysis" has been pressed.
	 */
	public void initAnalysis() {
		// prevent user to press button again while analysis is performed
		startButton.setEnabled(false);
		// clone configuration and init thread list
		// (actual analysis may be invoked multiple times from the same results
		// frame)
		resultFrameOptions = null;
		resultFrameOptions = (AnalysisConfiguration) analysisOptions.clone();
		myMethodExecutionThreads = new ArrayList<AnalysisMethodExecutionThread>();
	}

	/**
	 * Retrieves the copy of the current analysis configuration in order to
	 * carry out the analysis based on the chosen options. Should only be
	 * invoked after {@link #initAnalysis initAnalysis} has been called. <br>
	 * Note that all the method is
	 * <code>synchronized<code> as it will be accessed
	 * by concurrent threads executing the different analysis methods.
	 * 
	 * @return the current AnalysisConfiguration
	 */
	public synchronized AnalysisConfiguration getAnalysisConfiguration() {
		return resultFrameOptions;
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
			AnalysisMethodExecutionThread thread) {
		myMethodExecutionThreads.add(thread);
	}

	/**
	 * Called by each thread after having finished. Checks whether there are
	 * still other analysis methods running. Builds the result frame if not,
	 * does nothing otherwise. <br>
	 * Note that all the method is
	 * <code>synchronized<code> as it will be accessed
	 * by concurrent threads executing the different analysis methods.
	 * 
	 * @param thread
	 *            the thread that just finished
	 */
	public synchronized void threadDone(AnalysisMethodExecutionThread thread) {
		// remove thread from list and check whether was the last one
		myMethodExecutionThreads.remove(thread);
		if (myMethodExecutionThreads.size() == 0) {
			buildResultsFrame();
		}
	}

	/**
	 * Builds the {@link ConformanceAnalysisResults ConformanceAnalysisResults}
	 * frame. This procedure is invoked after each thread has indicated being
	 * ready via the {@link #threadDone threadDone} method.
	 */
	private void buildResultsFrame() {

		// / PLUGIN TEST START
		Message.add("<ConformanceAnalysis>", Message.TEST);
		// PLUGIN TEST END

		// build UI for every main category
		Iterator<AnalysisConfiguration> tabCategories = getAnalysisConfiguration()
				.getChildConfigurations().iterator();
		while (tabCategories.hasNext()) {
			// TODO - think about how this can be done without this string
			// comparison
			AnalysisConfiguration current = tabCategories.next();
			if (current.getName() == "Fitness") {
				Set<AnalysisResult> fitnessResultObjects = current
						.getResultObjects();
				current.setResultPanel(new FitnessAnalysisGUI(
						fitnessResultObjects));
			}
			if (current.getName() == "Precision") {
				Set<AnalysisResult> behApprResultObjects = current
						.getResultObjects();
				current.setResultPanel(new BehAppropriatenessAnalysisGUI(
						behApprResultObjects));
			}
			if (current.getName() == "Structure") {
				Set<AnalysisResult> structApprResultObjects = current
						.getResultObjects();
				current.setResultPanel(new StructAppropriatenessAnalysisGUI(
						structApprResultObjects));
			}
			/** TEMPLATE: build GUI for other tabs here.. */
		}

		// / PLUGIN TEST START
		Message.add("</ConformanceAnalysis>", Message.TEST);
		// PLUGIN TEST END

		// set search depth for invisible tasks
		int depthValue = -1;
		if (restrictedDepth.isEnabled() == true) {
			depthValue = restrictedDepth.getValue();
		}

		// spawn result window
		ConformanceAnalysisResults resultWindow = new ConformanceAnalysisResults(
				getAnalysisConfiguration(), depthValue);
		resultWindow.setVisible(true);
		MainUI.getInstance().createAnalysisResultFrame(myAlgorithm, myInput,
				resultWindow);
		// sets the "Start Analysis" button active, so that it can be invoked
		// again
		startButton.setEnabled(true);
	}
}
