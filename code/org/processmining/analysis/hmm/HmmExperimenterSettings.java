package org.processmining.analysis.hmm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiUtilities;
import org.processmining.framework.util.RuntimeUtils;

public class HmmExperimenterSettings extends JPanel implements
		GuiNotificationTarget {

	protected LogReader inputLog;
	protected PetriNet inputModel;
	protected AnalysisPlugin algorithm;
	protected HmmExpConfiguration config = HmmExpConfiguration.getInstance();

	protected GUIPropertyInteger noiseLevels = new GUIPropertyInteger(
			"Number of Noise Levels: ", config.getNoiseLevels(), 0, 100);
	protected GUIPropertyInteger traces = new GUIPropertyInteger(
			"Number of traces per Event Log: ", config.getTraces(), 1,
			Integer.MAX_VALUE);
	protected GUIPropertyInteger traceLength;
	protected GUIPropertyBoolean replicate = new GUIPropertyBoolean(
			"Run replicated experiments", config.isReplicate(), this);
	protected GUIPropertyInteger replications = new GUIPropertyInteger(
			"Number of replications per noise level: ", config
					.getReplications(), 2, Integer.MAX_VALUE);

	public HmmExperimenterSettings(PetriNet net, LogReader log,
			AnalysisPlugin plugin) {
		inputLog = log;
		inputModel = net;
		algorithm = plugin;
		createGui();
	}

	private void createGui() {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(80, 80, 80));
		HeaderBar header = new HeaderBar("HMM Experimenter");
		this.add(header, BorderLayout.NORTH);

		// create default of maximum trace length based on observation
		if (inputLog != null) {
			Process process;
			ProcessInstance instance;
			AuditTrailEntryList ateList;
			for (int i = 0; i < inputLog.numberOfProcesses(); i++) {
				process = inputLog.getProcess(i);
				int longestTraceLength = 0;
				for (int j = 0; j < process.size(); j++) {
					instance = process.getInstance(j);
					ateList = instance.getAuditTrailEntryList();
					if (longestTraceLength < ateList.size()) {
						longestTraceLength = ateList.size();
					}
				}
				traceLength = new GUIPropertyInteger(
						"Maximum number of events per trace: ",
						longestTraceLength, 1, 1000000);
			}
		} else {
			traceLength = new GUIPropertyInteger(
					"Maximum number of events per trace: ", 100, 1, 1000000);
		}

		RoundedPanel confPanel = new RoundedPanel(10, 5, 5);
		confPanel.setBackground(new Color(140, 140, 140));
		confPanel.setLayout(new BoxLayout(confPanel, BoxLayout.Y_AXIS));
		JButton startButton = new AutoFocusButton("Start analysis");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			startButton.setOpaque(false);
		}
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runAnalysis();
			}
		});
		JButton docsButton = new SlickerButton("Help...");
		docsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(algorithm);
			}
		});

		JPanel parameterPanel = new JPanel();
		parameterPanel
				.setLayout(new BoxLayout(parameterPanel, BoxLayout.Y_AXIS));
		parameterPanel.setOpaque(false);
		parameterPanel.add(noiseLevels.getPropertyPanel());
		parameterPanel.add(Box.createVerticalStrut(5));
		parameterPanel.add(traces.getPropertyPanel());
		parameterPanel.add(Box.createVerticalStrut(5));
		parameterPanel.add(traceLength.getPropertyPanel());
		parameterPanel.add(Box.createVerticalStrut(5));
		parameterPanel.add(replicate.getPropertyPanel());
		parameterPanel.add(Box.createVerticalStrut(5));
		parameterPanel.add(replications.getPropertyPanel());
		confPanel.add(GuiUtilities.packMiddleVertically((GuiUtilities
				.packCenterHorizontally(parameterPanel))));

		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalStrut(10));
		packed.add(docsButton);
		packed.add(Box.createHorizontalGlue());
		packed.add(startButton);
		int height = (int) startButton.getMinimumSize().getHeight();
		packed.setMinimumSize(startButton.getMinimumSize());
		packed.setMaximumSize(new Dimension(4000, (int) height));
		packed.setPreferredSize(new Dimension(4000, (int) height));
		confPanel.add(packed);

		this.add(confPanel, BorderLayout.CENTER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() {
		if (replicate.getValue() == false) {
			replications.disable();
		} else {
			replications.enable();
		}
	}

	/**
	 * Method to be called when pressing the "Start Analysis" button.
	 */
	private void runAnalysis() {
		HmmExpUtils.cleanup();
		config.setNoiseLevels(noiseLevels.getValue());
		config.setTraces(traces.getValue());
		config.setTraceLength(traceLength.getValue());
		config.setReplicate(replicate.getValue());
		config.setReplications(replications.getValue());
		Map<String, PetriNet> models = HmmExpUtils.readInputModels();
		ObservationNoiseGenerator obsNoise = new ObservationNoiseGenerator(
				models, config);
		TransitionNoiseGenerator transNoise = new TransitionNoiseGenerator(
				models, config);
		ObservationNoiseEvaluator obsNoiseEval = new ObservationNoiseEvaluator(
				models, config);
		TransitionNoiseEvaluator transNoiseEval = new TransitionNoiseEvaluator(
				models, config);
		AnalysisThread obsThread = new AnalysisThread(obsNoise, obsNoiseEval);
		obsThread.start();
		AnalysisThread transThread = new AnalysisThread(transNoise,
				transNoiseEval);
		transThread.start();
	}

	/**
	 * Thread starting the actual HMM experiment.
	 */
	class AnalysisThread extends Thread {

		private HmmNoiseGenerator generator;
		private HmmNoiseEvaluator evaluator;

		public AnalysisThread(HmmNoiseGenerator theGenerator,
				HmmNoiseEvaluator theEvaluator) {
			generator = theGenerator;
			evaluator = theEvaluator;
		}

		public void run() {
			generator.generate();
			evaluator.evaluate();
		}
	}

}
