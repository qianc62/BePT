package org.processmining.analysis.performance.fsmanalysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.analysis.Analyzer;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.fsm.AcceptFSM;
import org.processmining.framework.models.fsm.FSMState;
import org.processmining.framework.models.fsm.FSMTransition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.mining.fsm.FsmHorizonSettings;
import org.processmining.mining.fsm.FsmMinerPayload;
import org.processmining.mining.fsm.FsmSettings;

public class FSMAnalysisPlugin extends JPanel implements Provider {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9070406008378808395L;

	@Analyzer(name = "FSM analyzer", names = { "Log", "FSM net" })
	public JComponent analyze(AcceptFSM fsm) {
		acceptedFSM = fsm;
		initAnalysisMenuUI();
		return this;
	}

	protected static final String NO_SELECTION = "- no item selected -";
	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_FG = new Color(30, 30, 30);
	protected static Color COLOR_OUTER_BG = new Color(80, 80, 80);
	protected static Color COLOR_TEXT = new Color(50, 50, 50);

	protected FSMPerformanceAnalysisUI ui = null;
	protected Map<String, LogReader> logReaders;
	protected JComponent view;
	protected JPanel configurationPanel;
	protected JPanel rightPanel;
	protected JPanel metricsPanel;
	protected GUIPropertyListEnumeration logsEnumeration;
	protected JButton startButton;
	protected ProgressPanel progressPanel;

	protected FSMStatistics fsmStatistics;
	protected AcceptFSM acceptedFSM;

	public boolean analysis(LogReader log) {
		FsmSettings settings = ((FsmMinerPayload) acceptedFSM.getStartState()
				.getPayload()).getSettings();

		int progressCtr = 0;
		// First count how many steps we have to do for the progress bar.
		progressCtr = 0;
		for (ProcessInstance pi : log.getInstances()) {
			progressCtr += pi.getAuditTrailEntryList().size();
		}
		// Create the progress bar.
		progressPanel.setMinMax(0, progressCtr - 1);
		// And now for the real thing.
		progressCtr = 0;

		try {
			// And now for the real thing.
			for (ProcessInstance pi : log.getInstances()) {
				long startTime = getStartTime(pi);
				if (startTime == -1)
					continue;
				long endTime = getEndTime(pi);
				if (endTime == -1)
					continue;
				AuditTrailEntryList atel = pi.getAuditTrailEntryList();
				for (int i = 0; i < atel.size(); i++) {
					AuditTrailEntry ate = atel.get(i);
					progressPanel.setProgress(progressCtr++);

					/**
					 * An AuditTrailEntry corresponds to a transition in the
					 * FSM. First, construct the payload of the state preceding
					 * the transition.
					 */

					FsmMinerPayload fromPayload = new FsmMinerPayload(settings);
					// Use the horizon settings.
					for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
						mineBwd(atel, i, 1, settings.getHorizonSettings(true,
								mode), fromPayload, mode);
						mineFwd(atel, i, 0, settings.getHorizonSettings(false,
								mode), fromPayload, mode);
					}

					// Use the attribute settings.
					if (settings.getUseAttributes()) {
						for (int j = 0; j < i; j++) {
							AuditTrailEntry ate2 = atel.get(j);
							DataSection dataSection = ate2.getDataAttributes();
							for (String attribute : dataSection.keySet()) {
								if (settings.getAttributeSettings()
										.containsKey(attribute)) {
									String cluster = settings
											.getAttributeSettings().get(
													attribute).get(
													dataSection.get(attribute));
									if (cluster != null) {
										fromPayload.getAttributePayload().put(
												attribute, cluster);
									}
								}
							}
						}
					}

					/**
					 * Second, in a similar way, create the payload of the state
					 * succeding the transition.
					 */
					FsmMinerPayload toPayload = new FsmMinerPayload(settings);
					for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
						mineBwd(atel, i, 0, settings.getHorizonSettings(true,
								mode), toPayload, mode);
						mineFwd(atel, i, 1, settings.getHorizonSettings(false,
								mode), toPayload, mode);
					}

					if (settings.getUseAttributes()) {
						for (int j = 0; j <= i; j++) {
							AuditTrailEntry ate2 = atel.get(j);
							DataSection dataSection = ate2.getDataAttributes();
							for (String attribute : dataSection.keySet()) {
								if (settings.getAttributeSettings()
										.containsKey(attribute)) {
									String cluster = settings
											.getAttributeSettings().get(
													attribute).get(
													dataSection.get(attribute));
									if (cluster != null) {
										toPayload.getAttributePayload().put(
												attribute, cluster);
									}
								}
							}
						}
					}

					// for node
					if (i == 0) {
						if (fsmStatistics.getRemainingMap().get(
								fromPayload.toString()) != null) {
							if (ate.getTimestamp() != null)
								fsmStatistics.getRemainingMap().get(
										fromPayload.toString()).addValue(
										endTime - ate.getTimestamp().getTime());
						}
						if (fsmStatistics.getElapsedMap().get(
								fromPayload.toString()) != null)
							fsmStatistics.getElapsedMap().get(
									fromPayload.toString()).addValue(0);
						if (fsmStatistics.getSojournMap().get(
								fromPayload.toString()) != null)
							fsmStatistics.getSojournMap().get(
									fromPayload.toString()).addValue(0);
						// for edge
						String key = fromPayload.toString() + ":"
								+ toPayload.toString();
						if (fsmStatistics.getEdgeMap().get(key) != null)
							fsmStatistics.getEdgeMap().get(key).addValue(0.0);
					}

					if (fsmStatistics.getElapsedMap().get(toPayload.toString()) != null) {
						if (ate.getTimestamp() != null) {
							fsmStatistics.getElapsedMap().get(
									toPayload.toString()).addValue(
									ate.getTimestamp().getTime() - startTime);
						}
					}

					if (fsmStatistics.getRemainingMap().get(
							toPayload.toString()) != null) {
						if (ate.getTimestamp() != null) {
							fsmStatistics.getRemainingMap().get(
									toPayload.toString()).addValue(
									endTime - ate.getTimestamp().getTime());
						}
					}
					if ((i != atel.size() - 1)
							&& (fsmStatistics.getSojournMap().get(
									toPayload.toString()) != null)) {
						if (ate.getTimestamp() != null
								&& atel.get(i + 1).getTimestamp() != null) {
							fsmStatistics.getSojournMap().get(
									toPayload.toString()).addValue(
									atel.get(i + 1).getTimestamp().getTime()
											- ate.getTimestamp().getTime());
						}
					} else {
						if (fsmStatistics.getSojournMap().get(
								toPayload.toString()) != null)
							fsmStatistics.getSojournMap().get(
									toPayload.toString()).addValue(0);
					}

					// for edge
					String key = fromPayload.toString() + ":"
							+ toPayload.toString();
					if (fsmStatistics.getEdgeMap().get(key) != null) {
						if (i == 0) {
							fsmStatistics.getEdgeMap().get(key).addValue(0.0);
						} else {
							if (ate.getTimestamp() != null
									&& atel.get(i - 1).getTimestamp() != null) {
								fsmStatistics.getEdgeMap().get(key).addValue(
										ate.getTimestamp().getTime()
												- atel.get(i - 1)
														.getTimestamp()
														.getTime());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Message.add(e.toString(), Message.ERROR);
			return false;
		}
		return true;
	}

	/**
	 * Collect infromation for the payload of a given state by taking backwards
	 * step.
	 * 
	 * @param atel
	 *            AuditTrailEntryList the list of audti trail entries for this
	 *            process instance.
	 * @param i
	 *            int the index of the current audit trail entry in this list.
	 * @param offset
	 *            int offset from i to start taking steps.
	 * @param settings
	 *            FsmHorizonSettings the settings to use.
	 * @param payload
	 *            FsmMinerPayload the payload to store the results in.
	 * @param mode
	 *            int whether to collect model element info, originator info, or
	 *            event type info.
	 */
	private static void mineBwd(AuditTrailEntryList atel, int i, int offset,
			FsmHorizonSettings settings, FsmMinerPayload payload, int mode) {
		// Skip if these settings should not be used.
		if (settings.getUse()) {
			int k = 0;
			// Initialize number of visible steps to take.
			int horizon = settings.getFilteredHorizon();
			for (int j = i - offset; horizon != 0 && j >= 0; j--) {
				try {
					// Get the audit trail entry.
					AuditTrailEntry ate2 = atel.get(j);
					// Get the info. This depends on mode.
					String s;
					switch (mode) {
					case (FsmMinerPayload.MODELELEMENT): {
						s = ate2.getElement();
						break;
					}
					case (FsmMinerPayload.ORIGINATOR): {
						s = ate2.getOriginator();
						break;
					}
					case (FsmMinerPayload.EVENTTYPE): {
						s = ate2.getType();
						break;
					}
					default: {
						s = "";
					}
					}
					// Check whether not filtered out.
					if (settings.getFilter().contains(s)) {
						// Not filtered out, add to payload if number of steps
						// not exceeded.
						if (settings.getHorizon() < 0
								|| settings.getHorizon() + j + offset > i) {
							switch (settings.getAbstraction()) {
							case (FsmHorizonSettings.SEQ): {
								payload.addBwdSeq(mode, s);
								break;
							}
							case (FsmHorizonSettings.SET): {
								payload.addBwdSet(mode, s);
								break;
							}
							case (FsmHorizonSettings.BAG): {
								payload.addBwdBag(mode, s);
								break;
							}
							}
						}
						// Found an unfiltered step: decrease counter.
						horizon--;
					}
				} catch (Exception e) {
					Message.add(e.toString(), Message.ERROR);
				}
			}
		}
	}

	/**
	 * Collect infromation for the payload of a given state by taking forward
	 * steps.
	 * 
	 * @param atel
	 *            AuditTrailEntryList the list of audti trail entries for this
	 *            process instance.
	 * @param i
	 *            int the index of the current audit trail entry in this list.
	 * @param offset
	 *            int offset from i to start taking steps.
	 * @param settings
	 *            FsmHorizonSettings the settings to use.
	 * @param payload
	 *            FsmMinerPayload the payload to store the results in.
	 * @param mode
	 *            int whether to collect model element info, originator info, or
	 *            event type info.
	 */
	private static void mineFwd(AuditTrailEntryList atel, int i, int offset,
			FsmHorizonSettings settings, FsmMinerPayload payload, int mode) {
		/**
		 * See MineBwd for adiditonal comments.
		 */
		if (settings.getUse()) {
			int horizon = settings.getFilteredHorizon();
			for (int j = i + offset; horizon != 0 && j < atel.size(); j++) {
				try {
					AuditTrailEntry ate2 = atel.get(j);
					String s;
					switch (mode) {
					case (FsmMinerPayload.MODELELEMENT): {
						s = ate2.getElement();
						break;
					}
					case (FsmMinerPayload.ORIGINATOR): {
						s = ate2.getOriginator();
						break;
					}
					case (FsmMinerPayload.EVENTTYPE): {
						s = ate2.getType();
						break;
					}
					default: {
						s = "";
					}
					}
					if (settings.getFilter().contains(s)) {
						if (settings.getHorizon() < 0
								|| settings.getHorizon() + i + offset > j) {
							switch (settings.getAbstraction()) {
							case (FsmHorizonSettings.SEQ): {
								payload.addFwdSeq(mode, s);
								break;
							}
							case (FsmHorizonSettings.SET): {
								payload.addFwdSet(mode, s);
								break;
							}
							case (FsmHorizonSettings.BAG): {
								payload.addFwdBag(mode, s);
								break;
							}
							}
						}
						horizon--;
					}
				} catch (Exception e) {
					Message.add(e.toString(), Message.ERROR);
				}
			}
		}
	}

	private void buildDS(AcceptFSM graph) {
		for (ModelGraphVertex as : graph.getVerticeList()) {
			Message.add(as.getId() + "," + ((FSMState) as).getLabel(),
					Message.DEBUG);
			fsmStatistics.getElapsedMap().put(((FSMState) as).getLabel(),
					DescriptiveStatistics.newInstance());
			fsmStatistics.getRemainingMap().put(((FSMState) as).getLabel(),
					DescriptiveStatistics.newInstance());
			fsmStatistics.getSojournMap().put(((FSMState) as).getLabel(),
					DescriptiveStatistics.newInstance());
		}

		for (Object obj : graph.getEdges()) {
			ModelGraphEdge edg = (ModelGraphEdge) obj;
			Message.add(edg.getId() + ","
					+ ((FSMTransition) edg).getCondition(), Message.DEBUG);
			fsmStatistics.getEdgeMap().put(makeKey(edg),
					DescriptiveStatistics.newInstance());
		}
	}

	private String makeKey(ModelGraphEdge edg) {
		String start, end;
		start = ((FSMState) edg.getSource()).getLabel();
		end = ((FSMState) edg.getSource()).getLabel();
		return start + ":" + end;
	}

	private long getStartTime(ProcessInstance pi) {
		AuditTrailEntryList atel = pi.getAuditTrailEntryList();
		try {
			for (int i = 0; i < atel.size(); i++) {
				AuditTrailEntry ate = atel.get(i);
				if (ate.getTimestamp() != null)
					return ate.getTimestamp().getTime();
			}
		} catch (Exception ce) {
		}
		return -1;
	}

	private long getEndTime(ProcessInstance pi) {
		AuditTrailEntryList atel = pi.getAuditTrailEntryList();
		try {
			for (int i = 0; i < atel.size(); i++) {
				AuditTrailEntry ate = atel.get(atel.size() - i - 1);
				if (ate.getTimestamp() != null)
					return ate.getTimestamp().getTime();
			}
		} catch (Exception ce) {
		}
		return -1;
	}

	// for GUI
	public void initAnalysisMenuUI() {
		view = null;
		configurationPanel = null;
		logReaders = null;
		this.setLayout(new BorderLayout());
		this.setBackground(COLOR_OUTER_BG);
		HeaderBar header = new HeaderBar("FSM based Performance Analysis");
		header.setHeight(40);
		this.add(header, BorderLayout.NORTH);
		showConfigurationPanel();
	}

	public Map<String, LogReader> getLogReaders() {
		if (logReaders == null)
			updateFrameworkResources();
		return logReaders;
	}

	public void updateFrameworkResources() {
		logReaders = new HashMap<String, LogReader>();
		JInternalFrame[] frames = MainUI.getInstance().getDesktop()
				.getAllFrames();
		for (JInternalFrame frame : frames) {
			if (frame instanceof Provider) {
				ProvidedObject[] providedObjects = ((Provider) frame)
						.getProvidedObjects();
				for (ProvidedObject providedObject : providedObjects) {
					for (Object object : providedObject.getObjects()) {
						if (object instanceof LogReader) {
							logReaders.put(frame.getTitle() + " - "
									+ providedObject.getName(),
									(LogReader) object);
						}
					}
				}
			}
		}
	}

	protected void setView(JComponent component) {
		if (view != null) {
			this.remove(view);
		}
		this.add(component, BorderLayout.CENTER);
		view = component;
		this.revalidate();
		this.repaint();
	}

	public void checkStartEnabled() {
		startButton.setEnabled(true);
	}

	protected void showConfigurationPanel() {
		if (configurationPanel == null) {
			// setup configuration panel
			updateFrameworkResources();
			configurationPanel = new JPanel();
			configurationPanel.setLayout(new BorderLayout());
			configurationPanel.setBackground(COLOR_OUTER_BG);
			// setup logs panel
			ArrayList<String> values = new ArrayList<String>();
			Iterator<String> itr = logReaders.keySet().iterator();
			while (itr.hasNext()) {
				values.add(itr.next());
			}
			logsEnumeration = new GUIPropertyListEnumeration("Event Log :",
					null, values, null, 180);
			// initializing Logs
			RoundedPanel content = new RoundedPanel(10, 5, 5);
			content.setBackground(COLOR_BG);
			content.setLayout(new BoxLayout(content, BoxLayout.LINE_AXIS));
			content.add(logsEnumeration.getPropertyPanel());

			// setup reference model / log configuration panel
			JPanel startPanel = new JPanel();
			startPanel.setOpaque(false);
			startPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.X_AXIS));
			startButton = new AutoFocusButton("start calculation");
			if (RuntimeUtils.isRunningMacOsX() == true) {
				startButton.setOpaque(true);
			}
			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					fsmStatistics = new FSMStatistics(acceptedFSM);
					buildDS(acceptedFSM);
					startCalculation();
				}
			});
			startButton.setEnabled(true);
			content.add(startButton);
			startPanel.add(Box.createHorizontalGlue());
			startPanel.add(startButton);
			rightPanel = new JPanel();
			rightPanel.setOpaque(false);
			rightPanel.setBorder(BorderFactory.createEmptyBorder());
			rightPanel.setLayout(new BorderLayout());
			// blank panel
			JPanel blankPanel = new JPanel();
			blankPanel.setOpaque(false);
			blankPanel.setBorder(BorderFactory.createEmptyBorder());
			blankPanel.setLayout(new BorderLayout());

			JPanel leftPanel = new JPanel();
			leftPanel.setOpaque(false);
			leftPanel.setBorder(BorderFactory.createEmptyBorder());
			leftPanel.setLayout(new BorderLayout());
			leftPanel.add(content, BorderLayout.CENTER);
			leftPanel.add(startPanel, BorderLayout.EAST);
			// add benchmark item list to west
			rightPanel.add(blankPanel, BorderLayout.CENTER);
			configurationPanel.add(leftPanel, BorderLayout.NORTH);
			configurationPanel.add(rightPanel, BorderLayout.CENTER);
		}
		// switch to configuration view
		setView(configurationPanel);
	}

	public void startCalculation() {
		rightPanel.removeAll();
		progressPanel = new ProgressPanel("Calculation");
		progressPanel.setNote("Building performance measures from log...");
		rightPanel.add(progressPanel.getPanel(), BorderLayout.CENTER);
		boolean result = analysis(logReaders.get(logsEnumeration.getValue()));
		if (result) {
			ui = new FSMPerformanceAnalysisUI(acceptedFSM, fsmStatistics);
		} else {
			ui = null;
		}
		rightPanel.removeAll();
		rightPanel.add(ui, BorderLayout.CENTER);
		rightPanel.revalidate();
		rightPanel.repaint();
	}

	protected static JPanel packHorizontallyLeftAligned(Component[] components,
			int leftOffset) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		if (leftOffset > 0) {
			packed.add(Box.createHorizontalStrut(leftOffset));
		}
		int minW = 0, minH = 0;
		for (Component comp : components) {
			packed.add(comp);
			Dimension dim = comp.getMinimumSize();
			minW += dim.getWidth();
			minH = Math.max(minH, (int) dim.getHeight());
		}
		packed.add(Box.createHorizontalGlue());
		packed.setMinimumSize(new Dimension(minW, minH));
		packed.setMaximumSize(new Dimension(4000, minH));
		packed.setPreferredSize(new Dimension(4000, minH));
		return packed;
	}

	public String getSelectedLogs() {
		return logsEnumeration.getValue().toString();
	}

	public ProvidedObject[] getProvidedObjects() {
		if (ui != null)
			return ui.getProvidedObjects();
		return null;
	}
}
