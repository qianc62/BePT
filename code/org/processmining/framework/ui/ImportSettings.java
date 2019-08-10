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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderException;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.filter.LogEventLogFilter;
import org.processmining.framework.models.LogEventProvider;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.util.StringSimilarity;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.importing.LogReaderConnectionImportPlugin;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ImportSettings extends JDialog {

	public static final int READY = 0;
	public static final int RETRY = 1;
	public static final int ABORT = 2;

	private ArrayList importedEvents;
	private LogSummary summary;
	private LogReader log;
	private ImportEventsUI eventsUI;
	private boolean ok = false;

	private LogReader logReaderToReturn = null;

	private boolean fuzzyMatch;

	public ImportSettings(LogReader log, String pluginLabel,
			ArrayList importedEvents, boolean fuzzyMatch) {
		super(MainUI.getInstance(), "Settings for importing "
				+ log.getFile().getShortName() + " using " + pluginLabel, true);
		this.log = log;
		this.importedEvents = importedEvents;
		this.fuzzyMatch = fuzzyMatch;

		try {
			setUndecorated(false);
			summary = log.getLogSummary();
			eventsUI = new ImportEventsUI(importedEvents, summary
					.getLogEvents(), fuzzyMatch);
			jbInit();
			pack();
			CenterOnScreen.center(this);
		} catch (LogReaderException e) {
			throw e;
		}
	}

	public HashMap getMapping() {
		HashMap result = new HashMap();
		ArrayList combos = eventsUI.getCombos();
		ArrayList labels = eventsUI.getLabels();

		for (int i = 0; i < importedEvents.size(); i++) {
			Object event = importedEvents.get(i);
			ToolTipComboBox combo = (ToolTipComboBox) combos.get(i);
			ComboBoxLogEvent ce = (ComboBoxLogEvent) combo.getSelectedItem();
			LogEvent e = ce.getLogEvent();
			if (e.getModelElementName().equals(ComboBoxLogEvent.INVISIBLE)
					&& e.getEventType().equals(ComboBoxLogEvent.INVISIBLE)) {
				e = null;
			} else if (e.getModelElementName().equals(ComboBoxLogEvent.VISIBLE)
					&& e.getEventType().equals(ComboBoxLogEvent.VISIBLE)) {
				e = eventsUI.findLogEventIfProvided(event);
				if (e == null) {
					e = new LogEvent("Not in log: " + event.toString(),
							"unknown:not in log");
				}
			}
			result.put(event, new Object[] { e,
					((JTextField) labels.get(i)).getText() });
		}
		return result;
	}

	private HashMap getEventMapping() {
		HashMap result = new HashMap();
		ArrayList combos = eventsUI.getCombos();

		for (int i = 0; i < importedEvents.size(); i++) {
			Object event = importedEvents.get(i);
			ToolTipComboBox combo = (ToolTipComboBox) combos.get(i);
			ComboBoxLogEvent ce = (ComboBoxLogEvent) combo.getSelectedItem();
			LogEvent e = ce.getLogEvent();
			if (e.getModelElementName().equals(ComboBoxLogEvent.INVISIBLE)
					&& e.getEventType().equals(ComboBoxLogEvent.INVISIBLE)) {
				e = null;
			} else if (e.getModelElementName().equals(ComboBoxLogEvent.VISIBLE)
					&& e.getEventType().equals(ComboBoxLogEvent.VISIBLE)) {
				e = eventsUI.findLogEventIfProvided(event);
				if (e == null) {
					e = new LogEvent("Not in log: " + event.toString(),
							"unknown:not in log");
				}
			}
			result.put(event, e);
		}
		return result;
	}

	public int isLogReaderReady() {
		return isLogReaderReady(false);
	}

	public int isLogReaderReady(boolean autoSelectYes) {
		try {
			// Check for the fact that each logEvent in
			// result.getLogSummary.getLogEvents() is assigned to
			// precisely one logEvent from the imported list

			HashMap mapping = getEventMapping();
			// Now, each LogEvent in result.getLogSummary().getLogEvents()
			// should be a
			// value in mapping
			Iterator it = log.getLogSummary().getLogEvents().iterator();
			LogEvents eventsToKeep = new LogEvents();
			eventsToKeep.addAll(log.getLogSummary().getLogEvents());
			while (it.hasNext()) {
				LogEvent e = (LogEvent) it.next();

				if (!mapping.containsValue(e)) {
					// Houston, we have a problem
					eventsToKeep.remove(e);
				}
			}

			if (eventsToKeep.size() != log.getLogSummary().getLogEvents()
					.size()) {
				// Unfortunately, we need to check it the hard way.
				// Ask the user for permission
				int i = JOptionPane.YES_OPTION;
				if (!autoSelectYes) {
					i = JOptionPane
							.showConfirmDialog(
									ImportSettings.this,
									"Not all LogEvents in the log file are linked to a element in the imported model. \n"
											+ "Ignoring these LogEvents can be done automatically. However, this requires the \n"
											+ "log to be read again once. For large logs, this can take a considerable amount of time. \n"
											+ "Should the LogEvents be ignored automatically? \n"
											+ "Click YES to ignore LogEvents automatically, \n"
											+ "click NO  to return to the dialog and try again manually, or \n"
											+ "click CANCEL to cancel the connection to the log.",
									"Warning", JOptionPane.YES_NO_CANCEL_OPTION);
				}
				if (i == JOptionPane.NO_OPTION) {
					logReaderToReturn = null;
					return RETRY;
				}
				if (i == JOptionPane.CANCEL_OPTION) {
					logReaderToReturn = null;
					return ABORT;
				}
				if (i == JOptionPane.YES_OPTION) {
					LogFilter filter = new LogEventLogFilter(eventsToKeep);
					filter.setLowLevelFilter(log.getLogFilter());
					logReaderToReturn = LogReaderFactory.createInstance(filter,
							log);
					if (log.isSelection()) {
						logReaderToReturn = LogReaderFactory
								.createInstance(logReaderToReturn, log
										.processInstancesToKeep());
					}
				}
			} else {
				LogFilter filter = new LogEventLogFilter(eventsToKeep);
				filter.setLowLevelFilter(log.getLogFilter());
				logReaderToReturn = LogReaderFactory
						.createInstance(filter, log);
				if (log.isSelection()) {
					logReaderToReturn = LogReaderFactory.createInstance(
							logReaderToReturn, log.processInstancesToKeep());
				}
			}
			return READY;
		} catch (Exception e) {
			e.printStackTrace();
			return ABORT;
		}
	}

	public LogReader getLogReader() {
		return logReaderToReturn;
	}

	public boolean showModal() {
		setVisible(true);
		return ok;
	}

	private void jbInit() {

		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton("   Ok   ");
		JButton cancelButton = new JButton(" Cancel ");

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				setVisible(false);
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

		JScrollPane rightScroll = new JScrollPane(eventsUI);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(rightScroll, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
	}

}

class ImportEventsUI extends JPanel {

	private ArrayList importedEvents;
	private LogEvents eventsInLog;
	private ArrayList combos;
	private ArrayList labels;
	private boolean fuzzyMatch;

	public ImportEventsUI(ArrayList importedEvents, LogEvents eventsInLog,
			boolean fuzzyMatch) {
		this.importedEvents = importedEvents;
		this.eventsInLog = eventsInLog;
		this.fuzzyMatch = fuzzyMatch;

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// returned list of combo boxes is in the same order as the log events in
	// importedEvents
	// as passed to the constructor.
	public ArrayList getCombos() {
		return combos;
	}

	// returned list of label-JTextFields is in the same order as the log events
	// in importedEvents
	// as passed to the constructor.
	public ArrayList getLabels() {
		return labels;
	}

	private void jbInit() throws Exception {
		ComboBoxLogEvent[] logEvents;

		setLayout(new BorderLayout());

		add(new JLabel("Mapping of workflow log events:"), BorderLayout.NORTH);

		GridBagLayout gbl = new GridBagLayout();
		JPanel panel = new JPanel(gbl);

		logEvents = new ComboBoxLogEvent[eventsInLog.size() + 2];
		ComboBoxLogEvent visible = new ComboBoxLogEvent(
				LogReaderConnectionImportPlugin.MAKE_VISIBLE);
		logEvents[0] = visible;
		ComboBoxLogEvent invisible = new ComboBoxLogEvent(
				LogReaderConnectionImportPlugin.MAKE_INVISIBLE);
		logEvents[1] = invisible;
		for (int i = 0; i < eventsInLog.size(); i++) {
			logEvents[i + 2] = new ComboBoxLogEvent(eventsInLog.getEvent(i));
		}
		Arrays.sort(logEvents);

		// determine maximal length of event name
		int ml = 0;
		JLabel lab = new JLabel("");
		lab.setFont(lab.getFont().deriveFont(Font.PLAIN));
		for (int i = 0; i < importedEvents.size(); i++) {
			lab.setText(importedEvents.get(i).toString());
			if (lab.getPreferredSize().getWidth() > ml) {
				ml = (int) lab.getPreferredSize().getWidth();
			}
		}
		ml += 4;
		ml = Math.max(ml, 250);

		{
			final JLabel evtInMod = new JLabel(
					"<html>Events found in<br>imported model:</html>");
			evtInMod.setPreferredSize(new Dimension(ml, (int) evtInMod
					.getPreferredSize().getHeight()));

			ToolTipComboBox newCombo = new ToolTipComboBox(logEvents);
			final JLabel evtInLog = new JLabel("<html>Events in Log:</html>");
			evtInLog.setPreferredSize(new Dimension((int) newCombo
					.getPreferredSize().getWidth(), (int) evtInLog
					.getPreferredSize().getHeight()));

			final JLabel newLabel = new JLabel(
					"<html>New label, after<br>attaching selected log<br>to imported model:</html>");
			newLabel.setPreferredSize(new Dimension(ml, (int) newLabel
					.getPreferredSize().getHeight()));

			JPanel p = new JPanel(new BorderLayout());

			p.add(evtInMod, BorderLayout.WEST);

			p.add(evtInLog, BorderLayout.CENTER);

			p.add(newLabel, BorderLayout.EAST);

			panel.add(p, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 1, 1), 0, 0));

		}

		labels = new ArrayList();
		combos = new ArrayList();
		for (int i = 0; i < importedEvents.size(); i++) {

			// get object that appears in the log
			Object event = importedEvents.get(i);

			// make a new label with the event name
			final JTextField label = new JTextField(event.toString());
			label.setFont(label.getFont().deriveFont(Font.PLAIN));
			label.setEditable(false);
			label.setPreferredSize(new Dimension(ml, (int) label
					.getPreferredSize().getHeight()));

			// set the label in the textfield according the the object in the
			// log
			final JTextField labelField = new JTextField(label.getText());
			labelField.setFont(label.getFont().deriveFont(Font.PLAIN));
			labelField.setPreferredSize(new Dimension(ml, (int) label
					.getPreferredSize().getHeight()));

			// make a new combo-list
			ToolTipComboBox newCombo = new ToolTipComboBox(logEvents);
			newCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String s = ((ToolTipComboBox) e.getSource())
							.getSelectedItem().toString();
					if (!(s.equals(ComboBoxLogEvent.VISIBLE) || s
							.equals(ComboBoxLogEvent.INVISIBLE))) {
						labelField.setText(((ToolTipComboBox) e.getSource())
								.getSelectedItem().toString());
					} else {
						labelField.setText(label.getText());
					}
				}
			});

			newCombo.setFont(newCombo.getFont().deriveFont(Font.PLAIN));
			newCombo.setBorder(null);
			boolean foundMatch = false;
			int mostMatching = 0;
			int prevMatch = -1;
			String toMatch = event.toString();
			// See if we can find a matching combo-item
			LogEvent wantedEvent = null;
			for (int j = 0; !foundMatch && j < newCombo.getItemCount(); j++) {
				LogEvent comboEvent = ((ComboBoxLogEvent) newCombo.getItemAt(j))
						.getLogEvent();
				String comboEventName = comboEvent.toString();

				LogEvent evt = findLogEventIfProvided(event);
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

				if (event != null) {
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
					newCombo.setSelectedItem(visible);
					// logEvents[0].setLogEvent(wantedEvent);
				} else if (fuzzyMatch) {
					// OK, so we could not find one yet. Let's look for the
					// closest match
					newCombo.setSelectedIndex(mostMatching);
				} else {
					newCombo.setSelectedItem(invisible);
				}
			}
			JPanel p = new JPanel(new BorderLayout());

			// Comment Anne: looks very ugly in OSX -> what is the reason to
			// make this blue??
			// p.setBackground(Color.BLUE);

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
