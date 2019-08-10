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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.ProcessInstance;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LogPreviewUI extends JPanel {

	public static final Color COLOR_ENCLOSURE_BG = new Color(40, 40, 40);
	public static final Color COLOR_NON_FOCUS = new Color(70, 70, 70);
	public static final Color COLOR_LIST_BG = new Color(60, 60, 60);
	public static final Color COLOR_LIST_BG_LOWER = new Color(45, 45, 45);
	public static final Color COLOR_LIST_FG = new Color(180, 180, 180);
	public static final Color COLOR_LIST_SELECTION_BG = new Color(80, 0, 0);
	public static final Color COLOR_LIST_SELECTION_BG_LOWER = new Color(30, 10,
			10);
	public static final Color COLOR_LIST_SELECTION_FG = new Color(240, 240, 240);

	protected SlickerOpenLogSettings parent;
	protected LogReader log;

	protected JList instancesList;
	protected JLabel instanceNameLabel;
	protected JLabel instanceSizeLabel;
	protected JList eventsList;
	protected JLabel eventLabel;
	protected JList attributesList;

	public LogPreviewUI(SlickerOpenLogSettings parent) {
		this.parent = parent;
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				updateView();
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});
		// create instances list
		instancesList = new JList();
		instancesList.setBackground(COLOR_LIST_BG);
		instancesList.setForeground(COLOR_LIST_FG);
		instancesList.setSelectionBackground(COLOR_LIST_SELECTION_BG);
		instancesList.setSelectionForeground(COLOR_LIST_SELECTION_FG);
		instancesList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		instancesList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				instancesSelectionChanged();
			}
		});
		JScrollPane instancesScrollPane = new JScrollPane(instancesList);
		instancesScrollPane.setOpaque(false);
		// instancesScrollPane.setViewportBorder(BorderFactory.createLineBorder(colorListBorder));
		instancesScrollPane.setBorder(BorderFactory.createEmptyBorder());
		instancesScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		instancesScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar vBar = instancesScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(false);
		// assemble instances list
		JLabel instancesListLabel = new JLabel("Instances");
		instancesListLabel.setOpaque(false);
		instancesListLabel.setForeground(COLOR_LIST_SELECTION_FG);
		instancesListLabel
				.setFont(instancesListLabel.getFont().deriveFont(13f));
		instancesListLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instancesListLabel.setHorizontalAlignment(JLabel.CENTER);
		instancesListLabel.setHorizontalTextPosition(JLabel.CENTER);
		RoundedPanel instancesPanel = new RoundedPanel(10, 5, 0);
		instancesPanel.setBackground(COLOR_ENCLOSURE_BG);
		instancesPanel
				.setLayout(new BoxLayout(instancesPanel, BoxLayout.Y_AXIS));
		instancesPanel.setMaximumSize(new Dimension(180, 1000));
		instancesPanel.setPreferredSize(new Dimension(180, 1000));
		instancesPanel.add(instancesListLabel);
		instancesPanel.add(Box.createVerticalStrut(8));
		instancesPanel.add(instancesScrollPane);
		// create event list header
		instanceNameLabel = new JLabel("(no instance selected)");
		instanceNameLabel.setOpaque(false);
		instanceNameLabel.setForeground(COLOR_NON_FOCUS);
		instanceNameLabel.setFont(instanceNameLabel.getFont().deriveFont(13f));
		instanceNameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instanceNameLabel.setHorizontalAlignment(JLabel.CENTER);
		instanceNameLabel.setHorizontalTextPosition(JLabel.CENTER);
		instanceSizeLabel = new JLabel("select single instance to browse");
		instanceSizeLabel.setOpaque(false);
		instanceSizeLabel.setForeground(COLOR_NON_FOCUS);
		instanceSizeLabel.setFont(instanceSizeLabel.getFont().deriveFont(11f));
		instanceSizeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instanceSizeLabel.setHorizontalAlignment(JLabel.CENTER);
		instanceSizeLabel.setHorizontalTextPosition(JLabel.CENTER);
		// create events list
		eventsList = new JList();
		eventsList.setBackground(COLOR_LIST_BG);
		eventsList.setCellRenderer(new EventCellRenderer());
		eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		eventsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				eventsSelectionChanged();
			}
		});
		JScrollPane eventsScrollPane = new JScrollPane(eventsList);
		eventsScrollPane.setOpaque(false);
		eventsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		// eventsScrollPane.setViewportBorder(BorderFactory.createLineBorder(colorListBorder));
		eventsScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		eventsScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		vBar = eventsScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(false);
		// assemble events list
		RoundedPanel eventsPanel = new RoundedPanel(10, 5, 0);
		eventsPanel.setBackground(COLOR_ENCLOSURE_BG);
		eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
		eventsPanel.add(instanceNameLabel);
		eventsPanel.add(instanceSizeLabel);
		eventsPanel.add(Box.createVerticalStrut(8));
		eventsPanel.add(eventsScrollPane);
		// create attributes list
		attributesList = new JList();
		attributesList.setBackground(COLOR_LIST_BG);
		attributesList.setForeground(COLOR_LIST_FG);
		attributesList.setSelectionBackground(COLOR_LIST_BG);
		attributesList.setSelectionForeground(COLOR_LIST_FG);
		attributesList.setFocusable(false);
		JScrollPane attributesScrollPane = new JScrollPane(attributesList);
		attributesScrollPane.setOpaque(false);
		// attributesScrollPane.setBackground(colorListBg);
		attributesScrollPane.setBorder(BorderFactory.createEmptyBorder());
		// attributesScrollPane.setViewportBorder(BorderFactory.createLineBorder(colorListBorder));
		attributesScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		attributesScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		vBar = attributesScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(200, 200, 200), COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(false);
		vBar = attributesScrollPane.getHorizontalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(200, 200, 200), COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(false);
		// assemble attributes panel
		eventLabel = new JLabel("(no event selected)");
		eventLabel.setOpaque(false);
		eventLabel.setForeground(COLOR_NON_FOCUS);
		eventLabel.setFont(eventLabel.getFont().deriveFont(13f));
		eventLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		eventLabel.setHorizontalAlignment(JLabel.CENTER);
		eventLabel.setHorizontalTextPosition(JLabel.CENTER);
		RoundedPanel attributesPanel = new RoundedPanel(10, 5, 0);
		attributesPanel.setBackground(COLOR_ENCLOSURE_BG);
		attributesPanel.setLayout(new BoxLayout(attributesPanel,
				BoxLayout.Y_AXIS));
		attributesPanel.setMaximumSize(new Dimension(230, 1000));
		attributesPanel.setPreferredSize(new Dimension(230, 1000));
		attributesPanel.add(eventLabel);
		attributesPanel.add(Box.createVerticalStrut(8));
		attributesPanel.add(attributesScrollPane);
		// assemble GUI
		this.add(instancesPanel);
		this.add(eventsPanel);
		this.add(attributesPanel);
	}

	protected void instancesSelectionChanged() {
		int[] selectedIndices = instancesList.getSelectedIndices();
		if (selectedIndices.length == 0 || selectedIndices.length > 1) {
			eventsList.setListData(new Object[] {});
			eventsList.clearSelection();
			instanceNameLabel.setForeground(COLOR_NON_FOCUS);
			instanceNameLabel.setText("(no instance selected)");
			instanceSizeLabel.setForeground(COLOR_NON_FOCUS);
			instanceSizeLabel.setText("select single instance to browse");
		} else {
			ProcessInstance instance = log.getInstance(selectedIndices[0]);
			AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
			instanceNameLabel.setForeground(COLOR_LIST_SELECTION_FG);
			instanceNameLabel.setText(instance.getName());
			instanceSizeLabel.setForeground(COLOR_LIST_SELECTION_FG);
			instanceSizeLabel.setText(ateList.size() + " events");
			eventsList.setModel(new AuditTrailEntryListModel(ateList));
			eventsList.ensureIndexIsVisible(0);
		}
		showSelectedInstanceData();
	}

	protected void eventsSelectionChanged() {
		AuditTrailEntry ate = (AuditTrailEntry) eventsList.getSelectedValue();

		if (ate != null) {
			eventLabel.setForeground(COLOR_LIST_SELECTION_FG);
			eventLabel.setText("Attributes for event "
					+ (eventsList.getSelectedIndex() + 1));
			attributesList.setModel(new AttributesListModel(ate));
		} else {
			showSelectedInstanceData();
		}
	}

	protected void showSelectedInstanceData() {
		int[] selectedIndices = instancesList.getSelectedIndices();

		if (selectedIndices.length == 1) {
			ProcessInstance instance = log.getInstance(selectedIndices[0]);

			eventLabel.setForeground(COLOR_LIST_SELECTION_FG);
			eventLabel.setText("Attributes for case " + instance.getName());
			attributesList.setModel(new AttributesListModel(instance));
		} else {
			eventLabel.setForeground(COLOR_NON_FOCUS);
			eventLabel.setText("(no case or event selected)");
			attributesList.setModel(new AttributesListModel(
					(ProcessInstance) null));
		}
	}

	public ActionListener getActivationListener() {
		ActionListener activationListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateView();
			}
		};
		return activationListener;
	}

	protected void updateView() {
		LogReader uLog = parent.getLog();
		if (log == null || uLog.equals(log) == false) {
			log = uLog;
			// repopulate instance names list
			String[] instanceNames = new String[log.numberOfInstances()];
			for (int i = 0; i < instanceNames.length; i++) {
				instanceNames[i] = log.getInstance(i).getName();
			}
			instancesList.setListData(instanceNames);
			// reset events list
			instancesList.clearSelection();
			eventsList.clearSelection();
			revalidate();
			repaint();
		}
	}

	public LogReader getResultReader() {
		if (log == null || instancesList.getSelectedIndices().length == 0) {
			return null;
		} else {
			try {
				return LogReaderFactory.createInstance(log, instancesList
						.getSelectedIndices());
			} catch (Exception e) {
				// oops...
				e.printStackTrace();
				return null;
			}
		}
	}

}
