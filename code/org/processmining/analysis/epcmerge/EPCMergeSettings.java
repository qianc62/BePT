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

package org.processmining.analysis.epcmerge;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.SlickerButton;
import org.processmining.analysis.conformance.ConformanceAnalysisResults;
import org.processmining.analysis.conformance.ConformanceAnalysisSettings;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReaderException;
import org.processmining.framework.models.LogEventProvider;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethod;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.BrowserLauncher;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyStringTextarea;
import org.processmining.framework.util.StringSimilarity;
import org.processmining.framework.util.ToolTipComboBox;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class EPCMergeSettings extends JPanel implements GuiNotificationTarget,
		Provider {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1089102605111880803L;
	private final static boolean FUNCTION = false;
	private static final boolean EVENT = true;
	// GUI related attributes
	protected boolean isPainted = false;
	private JPanel upperPanel = new JPanel(); // upper panel containing the
	// invitation to select analysis
	// options
	private JPanel buttonsPanel = new JPanel(); // lower panel containing the
	// buttons
	private JScrollPane scrolledOptions;
	private GuiPropertyStringTextarea description = new GuiPropertyStringTextarea(
			"The EPC Merge plugin merges two EPCs into one EPC by preserving all the behavior that was possible in the two models. In addition the resulting EPC allows combining the behavior from both the two models. ");
	private GuiPropertyStringTextarea mapDescription = new GuiPropertyStringTextarea(
			"For a successful merge it is required to specify which functions are identical and which events are identical.");
	protected JButton startButton;// starts the analysis
	private JButton docsButton = new SlickerButton("Help..."); // shows the
	// plugin
	// documentation
	public GUIPropertyInteger restrictedDepth;

	private ArrayList<EPCObject> eventsModel1 = new ArrayList<EPCObject>();
	private ArrayList<EPCObject> eventsModel2 = new ArrayList<EPCObject>();
	private ArrayList<EPCObject> functionsModel1 = new ArrayList<EPCObject>();
	private ArrayList<EPCObject> functionsModel2 = new ArrayList<EPCObject>();
	private ImportEventsUI eventsUI;
	private ImportEventsUI functionsUI;
	private int step = 0;
	ConfigurableEPC epc1, epc2, resultingEPC;
	private ModelGraphPanel epcVis = null;

	public EPCMergeSettings(ConfigurableEPC net1, ConfigurableEPC net2,
			boolean fuzzyMatch) {
		// super(MainUI.getInstance(),
		// "Settings for importing " + log.getFile().getShortName() + " using "
		// + pluginLabel, true);
		epc1 = new ConfigurableEPC();
		HashMap<Long, EPCFunction> org2new = new HashMap<Long, EPCFunction>();
		epc1.copyAllFrom(net1, org2new);
		epc2 = new ConfigurableEPC();
		org2new = new HashMap<Long, EPCFunction>();
		epc2.copyAllFrom(net2, org2new);

		Iterator<EPCFunction> functions = epc1.getFunctions().iterator();
		while (functions.hasNext()) {
			functionsModel1.add(functions.next());
		}
		functions = epc2.getFunctions().iterator();
		while (functions.hasNext()) {
			functionsModel2.add(functions.next());
		}
		Iterator<EPCEvent> events = epc1.getEvents().iterator();
		while (events.hasNext()) {
			eventsModel1.add(events.next());
		}
		events = epc2.getEvents().iterator();
		while (events.hasNext()) {
			eventsModel2.add(events.next());
		}

		try {
			eventsUI = new ImportEventsUI(eventsModel1, eventsModel2,
					fuzzyMatch, ImportEventsUI.EVENTS);
			functionsUI = new ImportEventsUI(functionsModel1, functionsModel2,
					fuzzyMatch, ImportEventsUI.FUNCTIONS);
			jbInit();
			// connect functionality to GUI elements
			registerGuiActionListener();
			// updateGui
			updateGUI();
		} catch (LogReaderException e) {
			throw e;
		}
	}

	private void registerGuiActionListener() {
		// show plug-in documentation
		docsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserLauncher
						.openURL("http://www.floriangottschalk.de/255.html");
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
				// start each analysis method in a separate thread to monitor
				// progress and be
				// able to abort it
				if (step == 0) {
					step = 1;

					jbInit();
				} else {
					HashMap<EPCObject, Object[]> mapping = getMapping(FUNCTION);
					Iterator<EPCObject> it = mapping.keySet().iterator();
					while (it.hasNext()) {
						EPCObject key = it.next();
						Object[] values = (Object[]) mapping.get(key);
						String newIdentifier = (String) values[1];
						if (key != null) {
							key.setIdentifier(newIdentifier);
						}
						EPCObject target = (EPCObject) values[0];
						if (target != null) {
							target.setIdentifier(newIdentifier);
						}
					}
					mapping = getMapping(EVENT);
					it = (mapping.keySet()).iterator();
					while (it.hasNext()) {
						EPCObject key = it.next();
						Object[] values = (Object[]) mapping.get(key);
						String newIdentifier = (String) values[1];
						if (key != null) {
							key.setIdentifier(newIdentifier);
						}
						EPCObject target = (EPCObject) values[0];
						if (target != null) {
							target.setIdentifier(newIdentifier);
						}
					}
					EPCMergeMethod result = new EPCMergeMethod(epc1, epc2);
					execute(result);
				}
			}
		});
	}

	/**
	 * Starts the given analyis method in a separate thread. This is necessar if
	 * one wants to be able that the method can be aborted by the user.
	 * 
	 * @param method
	 *            the AnalysisMethod to be performed
	 */
	public void execute(EPCMergeMethod method) {
		// start analysis method in a separate thread to monitor progress
		// and be
		// able to abort it
		EPCMergeExecutionThread worker = new EPCMergeExecutionThread(method,
				this);
		try {
			worker.start();
		} catch (OutOfMemoryError err) {
			Message.add("Out of memory while analyzing");
		}

		/** TEMPLATE: add further analysis methods here.. */
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
	}

	/**
	 * Called by the specified thread after it has finished.
	 * 
	 * @param thread
	 *            the thread that just finished
	 */
	public void threadDone(ConfigurableEPC result) {
		// TODO buildResultsFrame();
		resultingEPC = result;
		this.epcVis = resultingEPC.getGrappaVisualization();
		step = 2;

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public HashMap<EPCObject, Object[]> getMapping(boolean eventMapping) {
		HashMap<EPCObject, Object[]> result = new HashMap<EPCObject, Object[]>();
		if (eventMapping) {
			ArrayList<ToolTipComboBox> combos = eventsUI.getCombos();
			ArrayList<JTextField> labels = eventsUI.getLabels();

			for (int i = 0; i < eventsModel2.size(); i++) {
				EPCObject event = eventsModel2.get(i);
				ToolTipComboBox combo = (ToolTipComboBox) combos.get(i);
				ComboBoxEPCObject ce = (ComboBoxEPCObject) combo
						.getSelectedItem();
				EPCObject e = ce.getEPCObject();
				result.put(event, new Object[] { e,
						((JTextField) labels.get(i)).getText() });
			}
		} else {
			ArrayList<ToolTipComboBox> combos = functionsUI.getCombos();
			ArrayList<JTextField> labels = functionsUI.getLabels();

			for (int i = 0; i < functionsModel2.size(); i++) {
				EPCObject event = functionsModel2.get(i);
				ToolTipComboBox combo = (ToolTipComboBox) combos.get(i);
				ComboBoxEPCObject ce = (ComboBoxEPCObject) combo
						.getSelectedItem();
				EPCObject e = ce.getEPCObject();
				result.put(event, new Object[] { e,
						((JTextField) labels.get(i)).getText() });
			}
		}
		return result;
	}

	/*
	 * private HashMap<EPCObject,EPCObject> getEventMapping() {
	 * HashMap<EPCObject,EPCObject> result = new HashMap<EPCObject,EPCObject>();
	 * ArrayList<ToolTipComboBox> combos = eventsUI.getCombos();
	 * 
	 * for (int i = 0; i < eventsModel2.size(); i++) { EPCObject event =
	 * eventsModel2.get(i); ToolTipComboBox combo = (ToolTipComboBox)
	 * combos.get(i); ComboBoxEPCObject ce = (ComboBoxEPCObject)
	 * combo.getSelectedItem(); EPCObject e = ce.getEPCObject();
	 * result.put(event, e); } return result; }
	 */

	private void jbInit() {
		if (step == 0) {
			startButton = new AutoFocusButton("Map EPC Events");
			// build the GUI based on the configuration object

			functionsUI.setBorder(BorderFactory.createLineBorder(new Color(150,
					150, 150), 1));
			scrolledOptions = new JScrollPane(functionsUI);
			buttonsPanel
					.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
			buttonsPanel.add(docsButton);
			buttonsPanel.add(Box.createHorizontalGlue());
			buttonsPanel.add(startButton);
			buttonsPanel
					.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

			upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
			upperPanel.add(description.getPropertyPanel());
			upperPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			upperPanel.add(mapDescription.getPropertyPanel());
			// pack them
			this.setLayout(new BorderLayout());
			this.add(upperPanel, BorderLayout.NORTH);
			this.add(scrolledOptions, BorderLayout.CENTER);
			this.add(buttonsPanel, BorderLayout.SOUTH);
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			this.validate();
			this.repaint();
		} else if (step == 1) {
			startButton.setText("Start Merge");
			this.remove(upperPanel);
			this.remove(buttonsPanel);
			this.remove(scrolledOptions);
			this.setLayout(new BorderLayout());
			eventsUI.setBorder(BorderFactory.createLineBorder(new Color(150,
					150, 150), 1));
			scrolledOptions = new JScrollPane(eventsUI);

			this.setLayout(new BorderLayout());
			this.add(upperPanel, BorderLayout.NORTH);
			this.add(scrolledOptions, BorderLayout.CENTER);
			this.add(buttonsPanel, BorderLayout.SOUTH);
			this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			this.validate();
			this.repaint();
		} else {
			this.remove(upperPanel);
			this.remove(buttonsPanel);
			this.remove(scrolledOptions);
			this.setLayout(new BorderLayout());
			this.add(new JScrollPane(epcVis), BorderLayout.CENTER);
			this.validate();
			this.repaint();

		}

	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("Merged EPC",
				new Object[] { resultingEPC }) };
	}

}

class ImportEventsUI extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1749047641742727444L;
	public final static boolean EVENTS = true;
	public final static boolean FUNCTIONS = false;

	private ArrayList<EPCObject> elementsModel1;
	private ArrayList<EPCObject> elementsModel2;
	private ArrayList<ToolTipComboBox> combos;
	private ArrayList<JTextField> labels;
	private boolean type;
	private EPCObject none = new EPCEvent(ComboBoxEPCObject.NONE,
			new ConfigurableEPC());
	private boolean fuzzyMatch;

	public ImportEventsUI(ArrayList<EPCObject> elementsModel1,
			ArrayList<EPCObject> elementsModel2, boolean fuzzyMatch,
			boolean type) {
		this.elementsModel1 = elementsModel1;
		this.elementsModel2 = elementsModel2;
		this.fuzzyMatch = fuzzyMatch;
		this.type = type;

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// returned list of combo boxes is in the same order as the log events in
	// importedEvents
	// as passed to the constructor.
	public ArrayList<ToolTipComboBox> getCombos() {
		return combos;
	}

	// returned list of label-JTextFields is in the same order as the log events
	// in importedEvents
	// as passed to the constructor.
	public ArrayList<JTextField> getLabels() {
		return labels;
	}

	private void jbInit() throws Exception {

		ComboBoxEPCObject[] epcObjects;

		setLayout(new BorderLayout());
		if (type == EVENTS) {
			add(new JLabel("Mapping of EPC events:"), BorderLayout.NORTH);
		} else {
			add(new JLabel("Mapping of EPC functions:"), BorderLayout.NORTH);
		}

		GridBagLayout gbl = new GridBagLayout();
		JPanel panel = new JPanel(gbl);
		elementsModel1.add(none);

		epcObjects = new ComboBoxEPCObject[(elementsModel1.size())];
		for (int i = 0; i < elementsModel1.size(); i++) {
			epcObjects[i] = new ComboBoxEPCObject(elementsModel1.get(i));
		}
		Arrays.sort(epcObjects);

		// determine maximal length of event name
		int ml = 0;
		JLabel lab = new JLabel("");
		lab.setFont(lab.getFont().deriveFont(Font.PLAIN));
		for (int i = 0; i < elementsModel1.size(); i++) {
			lab.setText(elementsModel1.get(i).toString());
			if (lab.getPreferredSize().getWidth() > ml) {
				ml = (int) lab.getPreferredSize().getWidth();
			}
		}
		ml += 4;
		ml = Math.max(ml, 250);

		{
			String label;
			if (type == EVENTS) {
				label = "<html>Event found in EPC 1:</html>";
			} else {
				label = "<html>Function found in EPC 1:</html>";
			}
			final JLabel evtInMod1 = new JLabel(label);
			evtInMod1.setPreferredSize(new Dimension(ml, (int) evtInMod1
					.getPreferredSize().getHeight()));
			if (type == EVENTS) {
				label = "<html>Event found in EPC 2:</html>";
			} else {
				label = "<html>Function found in EPC 2:</html>";
			}

			ToolTipComboBox newCombo = new ToolTipComboBox(epcObjects);
			final JLabel evtInMod2 = new JLabel(label);
			evtInMod2.setPreferredSize(new Dimension((int) newCombo
					.getPreferredSize().getWidth(), (int) evtInMod2
					.getPreferredSize().getHeight()));
			if (type == EVENTS) {
				label = "<html>New event name after<br>merging the two EPCs:</html>";
			} else {
				label = "<html>New function name after<br>merging the two EPCs:</html>";
			}

			final JLabel newLabel = new JLabel(label);
			newLabel.setPreferredSize(new Dimension(ml, (int) newLabel
					.getPreferredSize().getHeight()));

			JPanel p = new JPanel(new BorderLayout());

			p.add(evtInMod1, BorderLayout.WEST);

			p.add(evtInMod2, BorderLayout.CENTER);

			p.add(newLabel, BorderLayout.EAST);

			panel.add(p, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 1, 1), 0, 0));

		}

		labels = new ArrayList<JTextField>();
		combos = new ArrayList<ToolTipComboBox>();
		for (int i = 0; i < elementsModel2.size(); i++) {

			// get object that appears in the model
			Object elementModel2 = elementsModel2.get(i);

			// make a new label with the element name
			final JTextField label = new JTextField(elementModel2.toString());
			label.setFont(label.getFont().deriveFont(Font.PLAIN));
			label.setEditable(false);
			label.setPreferredSize(new Dimension(ml, (int) label
					.getPreferredSize().getHeight()));

			// set the label in the textfield according the the object in the
			// EPC
			final JTextField labelField = new JTextField(label.getText());
			labelField.setFont(label.getFont().deriveFont(Font.PLAIN));
			labelField.setPreferredSize(new Dimension(ml, (int) label
					.getPreferredSize().getHeight()));

			// make a new combo-list
			ToolTipComboBox newCombo = new ToolTipComboBox(epcObjects);
			newCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (((ToolTipComboBox) e.getSource()).getSelectedItem()
							.equals(none)) {
						labelField.setText(label.getText());
					} else {
						labelField.setText(((ToolTipComboBox) e.getSource())
								.getSelectedItem().toString());
					}
				}
			});

			newCombo.setFont(newCombo.getFont().deriveFont(Font.PLAIN));
			newCombo.setBorder(null);
			boolean foundMatch = false;
			int mostMatching = 0;
			int prevMatch = -1;
			String toMatch = elementModel2.toString();
			// See if we can find a matching combo-item
			LogEvent wantedEvent = null;
			for (int j = 0; !foundMatch && j < newCombo.getItemCount(); j++) {
				EPCObject comboEvent = ((ComboBoxEPCObject) newCombo
						.getItemAt(j)).getEPCObject();
				String comboEventName = newCombo.getItemAt(j).toString();

				LogEvent evt = findLogEventIfProvided(elementModel2);
				if (evt != null) {
					if (evt.equals(comboEvent)) {
						newCombo.setSelectedIndex(j);
						foundMatch = true;
					} else {
						if (!fuzzyMatch) {
							wantedEvent = evt;
						}
					}
				}

				if (elementModel2 != null) {
					if (!foundMatch && fuzzyMatch) {
						int match = StringSimilarity.similarity(toMatch,
								comboEventName);
						int l = toMatch.length() + comboEventName.length();
						// Set this one to be the best match, if it is a
						// better match then we saw before, and the distance is
						// less then
						// half of the sum ot the number of characters.
						if ((prevMatch == -1 || match < prevMatch)
								&& match < l / 2) {
							mostMatching = j;
							prevMatch = match;
						}
					}
				}
			}
			if (!foundMatch) {
				if (wantedEvent != null) {
					// set the event to wantedEvent
					newCombo.setSelectedItem(none);
					// logEvents[0].setLogEvent(wantedEvent);
				} else if (fuzzyMatch) {
					// OK, so we could not find one yet. Let's look for the
					// closest match
					newCombo.setSelectedIndex(mostMatching);
				} else {
					newCombo.setSelectedItem(none);
				}
			}
			JPanel p = new JPanel(new BorderLayout());

			p.setBackground(Color.BLUE);

			p.add(label, BorderLayout.WEST);
			labels.add(labelField);

			p.add(newCombo, BorderLayout.CENTER);
			combos.add(newCombo);

			p.add(labelField, BorderLayout.EAST);

			panel.add(p, new GridBagConstraints(0, i + 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 1, 1), 0, 0));

		}

		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(null);
		add(scrollPane, BorderLayout.CENTER);
	}

	LogEvent findLogEventIfProvided(Object providedObject) {
		// compare event to comboEvent
		if (providedObject instanceof LogEvent) {
			// Happens for example in Heuristic Nets
			return (LogEvent) providedObject;
		}
		if (providedObject instanceof LogEventProvider) {
			// Happens for example in Petri nets and/or EPCs
			return ((LogEventProvider) providedObject).getLogEvent();
		}
		return null;
	}
}
