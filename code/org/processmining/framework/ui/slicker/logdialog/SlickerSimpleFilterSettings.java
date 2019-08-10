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
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.filter.SimpleLogFilter;
import org.processmining.framework.ui.slicker.logdialog.SlickerEventTypeConfiguration.EventTypeAction;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class SlickerSimpleFilterSettings extends JPanel {

	protected Color colorBg = new Color(140, 140, 140);
	protected Color colorOuterBg = new Color(100, 100, 100);
	protected Color colorListBg = new Color(60, 60, 60);
	protected Color colorListBgSelected = new Color(10, 90, 10);
	protected Color colorListFg = new Color(200, 200, 200, 160);
	protected Color colorListFgSelected = new Color(230, 230, 230, 200);
	protected Color colorListEnclosureBg = new Color(150, 150, 150);
	protected Color colorListHeader = new Color(10, 10, 10);
	protected Color colorListDescription = new Color(60, 60, 60);

	protected LogReader log;
	protected SimpleLogFilter filter = null;
	protected ChangeListener updateListener = null;
	protected JList processList;
	protected JList eventFilterList;
	protected JList startEventList;
	protected JList endEventList;
	protected SlickerEventTypeConfiguration eventTypeConfiguration;
	protected ArrayList<String> processNames = new ArrayList<String>();
	protected ArrayList<LogEvent> events = new ArrayList<LogEvent>();
	protected ArrayList<LogEvent> startEvents = new ArrayList<LogEvent>();
	protected ArrayList<LogEvent> endEvents = new ArrayList<LogEvent>();

	public SlickerSimpleFilterSettings(LogReader log,
			ChangeListener updateListener) {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.updateListener = updateListener;
		final ChangeListener listener = this.updateListener;
		// this.setBackground(colorOuterBg);
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				if (listener != null) {
					listener.stateChanged(new ChangeEvent(this));
				}
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});
		JPanel listPanel = new JPanel();
		listPanel.setOpaque(false);
		listPanel.setBorder(BorderFactory.createEmptyBorder());
		listPanel.setLayout(new GridLayout(1, 3));
		events.addAll(log.getLogSummary().getLogEvents());
		eventFilterList = new JList(events.toArray());
		startEvents.addAll(log.getLogSummary().getStartingLogEvents().keySet());
		startEventList = new JList(startEvents.toArray());
		endEvents.addAll(log.getLogSummary().getEndingLogEvents().keySet());
		endEventList = new JList(endEvents.toArray());
		String processes[] = new String[log.numberOfProcesses()];
		for (int i = 0; i < processes.length; i++) {
			processes[i] = log.getProcess(i).getName();
			this.processNames.add(processes[i]);
		}
		processList = new JList(processes);
		eventTypeConfiguration = new SlickerEventTypeConfiguration(log
				.getLogSummary().getEventTypes());
		JPanel leftPanel = new JPanel();
		leftPanel.setOpaque(false);
		leftPanel.setBorder(BorderFactory.createEmptyBorder());
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add(configureList(processList, "Processes",
				"Only green processes will be used."));
		// leftPanel.add(Box.createVerticalStrut(5));
		leftPanel
				.add(configureAnyScrollable(eventTypeConfiguration,
						"Event types",
						"Event types may be removed, or their enclosing instances discarded."));
		JPanel middlePanel = new JPanel();
		middlePanel.setOpaque(false);
		middlePanel.setBorder(BorderFactory.createEmptyBorder());
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
		middlePanel.add(configureList(startEventList, "Start events",
				"Only instances starting with a green event will be used."));
		// middlePanel.add(Box.createVerticalStrut(5));
		middlePanel.add(configureList(endEventList, "End events",
				"Only instances ending with a green event will be used."));
		listPanel.add(leftPanel);
		listPanel.add(middlePanel);
		listPanel.add(configureList(eventFilterList, "Event filter",
				"Only green events will be used."));
		this.add(listPanel, BorderLayout.CENTER);
		// setup filter
		filter = new SimpleLogFilter();
		filter.setProcesses(processNames.toArray(new String[0]));
		filter.setFilterEvents(events.toArray(new LogEvent[0]));
		filter.setStartEvents(startEvents.toArray(new LogEvent[0]));
		filter.setEndEvents(endEvents.toArray(new LogEvent[0]));
		filter.setRemoveTypes(eventTypeConfiguration
				.getFilteredEventTypes(EventTypeAction.REMOVE));
		filter.setSkipInstanceTypes(eventTypeConfiguration
				.getFilteredEventTypes(EventTypeAction.SKIP_INSTANCE));
		// add listeners
		eventFilterList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (listener != null) {
					events.clear();
					for (Object obj : eventFilterList.getSelectedValues()) {
						events.add((LogEvent) obj);
					}
					filter = new SimpleLogFilter(filter);
					filter.setFilterEvents(events.toArray(new LogEvent[0]));
					listener.stateChanged(new ChangeEvent(this));
				}
			}
		});
		startEventList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (listener != null) {
					startEvents.clear();
					for (Object obj : startEventList.getSelectedValues()) {
						startEvents.add((LogEvent) obj);
					}
					filter = new SimpleLogFilter(filter);
					filter.setStartEvents(startEvents.toArray(new LogEvent[0]));
					listener.stateChanged(new ChangeEvent(this));
				}
			}
		});
		endEventList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (listener != null) {
					endEvents.clear();
					for (Object obj : endEventList.getSelectedValues()) {
						endEvents.add((LogEvent) obj);
					}
					filter = new SimpleLogFilter(filter);
					filter.setEndEvents(endEvents.toArray(new LogEvent[0]));
					listener.stateChanged(new ChangeEvent(this));
				}
			}
		});
		processList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (listener != null) {
					processNames.clear();
					for (Object obj : processList.getSelectedValues()) {
						processNames.add((String) obj);
					}
					filter = new SimpleLogFilter(filter);
					filter.setProcesses(processNames.toArray(new String[0]));
					listener.stateChanged(new ChangeEvent(this));
				}
			}
		});
		eventTypeConfiguration.setUpdateListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				filter = new SimpleLogFilter(filter);
				filter.setRemoveTypes(eventTypeConfiguration
						.getFilteredEventTypes(EventTypeAction.REMOVE));
				filter.setSkipInstanceTypes(eventTypeConfiguration
						.getFilteredEventTypes(EventTypeAction.SKIP_INSTANCE));
				listener.stateChanged(new ChangeEvent(this));
			}
		});
	}

	public LogFilter getLogFilter() {
		return filter;
	}

	protected JComponent configureList(JList list, String title,
			String description) {
		list.setFont(list.getFont().deriveFont(13f));
		list.setBackground(colorListBg);
		list.setForeground(colorListFg);
		list.setSelectionBackground(colorListBgSelected);
		list.setSelectionForeground(colorListFgSelected);
		list.setFont(list.getFont().deriveFont(12f));
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setSelectionInterval(0, list.getModel().getSize() - 1);
		return configureAnyScrollable(list, title, description);
	}

	protected JComponent configureAnyScrollable(JComponent scrollable,
			String title, String description) {
		RoundedPanel enclosure = new RoundedPanel(10, 5, 5);
		enclosure.setBackground(colorListEnclosureBg);
		enclosure.setLayout(new BoxLayout(enclosure, BoxLayout.Y_AXIS));
		JLabel headerLabel = new JLabel(title);
		headerLabel.setOpaque(false);
		headerLabel.setForeground(colorListHeader);
		headerLabel.setFont(headerLabel.getFont().deriveFont(14f));
		JLabel descriptionLabel = new JLabel("<html>" + description + "</html>");
		descriptionLabel.setOpaque(false);
		descriptionLabel.setForeground(colorListDescription);
		descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(11f));
		JScrollPane listScrollPane = new JScrollPane(scrollable);
		listScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listScrollPane.setViewportBorder(BorderFactory
				.createLineBorder(new Color(40, 40, 40)));
		listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		JScrollBar vBar = listScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, colorListEnclosureBg,
				new Color(30, 30, 30), new Color(80, 80, 80), 4, 12));
		enclosure.add(packLeftAligned(headerLabel));
		enclosure.add(Box.createVerticalStrut(3));
		enclosure.add(packLeftAligned(descriptionLabel));
		enclosure.add(Box.createVerticalStrut(5));
		enclosure.add(listScrollPane);
		return enclosure;
	}

	protected JComponent packLeftAligned(JComponent component) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setBorder(BorderFactory.createEmptyBorder());
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(component);
		packed.add(Box.createHorizontalGlue());
		return packed;
	}

}
