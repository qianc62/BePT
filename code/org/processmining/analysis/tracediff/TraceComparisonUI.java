package org.processmining.analysis.tracediff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.slicker.logdialog.LogPreviewUI;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * Displays the Trace comparison result and allows to jump from difference to
 * difference.
 * 
 * GUI adapted from LogPreviewUI.
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public class TraceComparisonUI extends JPanel {

	protected JLabel instanceNameLabel;
	protected JLabel instanceSizeLabel;
	protected JLabel instanceNameLabelRef;
	protected JLabel instanceSizeLabelRef;
	protected JList eventsList;

	protected ProcessInstance left;
	protected ProcessInstance right;
	protected LogReader log;
	protected GuiNotificationTarget target;

	protected List<Integer> diffPositions;

	public TraceComparisonUI(ProcessInstance first, ProcessInstance second,
			LogReader theLog, GuiNotificationTarget aTarget) {
		this.left = first;
		this.right = second;
		this.log = theLog;
		this.target = aTarget;
		buildUI();
		fillTraces();
	}

	private void buildUI() {
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BorderLayout());
		// create event list header
		instanceNameLabel = new JLabel("(no instance selected)");
		instanceSizeLabel = new JLabel("select single instance to browse");
		createInstanceLabels(instanceNameLabel, instanceSizeLabel);
		instanceNameLabelRef = new JLabel("(no instance selected)");
		instanceSizeLabelRef = new JLabel("select single instance to browse");
		createInstanceLabels(instanceNameLabelRef, instanceSizeLabelRef);
		// create events list
		eventsList = new JList();
		createEventList(eventsList);
		// create button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		JButton backButton = new SlickerButton("Go Back ");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				target.updateGUI();
			}
		});
		JLabel diffLabel = new JLabel("Jump to Difference: ");
		diffLabel.setFont(diffLabel.getFont().deriveFont(13f));
		diffLabel.setOpaque(false);
		SlickerButton nextButton = new AutoFocusButton("Next");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (diffPositions.size() > 0) {
					findNextDiff();
				}
			}
		});
		SlickerButton prevButton = new SlickerButton("Previous");
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (diffPositions.size() > 0) {
					findPrevDiff();
				}
			}
		});
		nextButton.setMinimumSize(prevButton.getMinimumSize());
		nextButton.setPreferredSize(prevButton.getPreferredSize());
		nextButton.setMaximumSize(prevButton.getMaximumSize());
		buttonPanel.add(diffLabel);
		buttonPanel.add(Box.createHorizontalStrut(5));
		buttonPanel.add(prevButton);
		buttonPanel.add(nextButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(backButton);
		// assemble events list
		RoundedPanel eventsPanel = createEventsPanel(eventsList,
				instanceNameLabel, instanceSizeLabel, instanceNameLabelRef,
				instanceSizeLabelRef);
		// assemble GUI
		this.add(eventsPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void fillTraces() {
		LogEvents events = log.getLogSummary().getLogEvents();
		// make lists of log events for comparison
		AuditTrailEntryList ateListRef = left.getAuditTrailEntryList();
		LogEvent[] leftTrace = new LogEvent[ateListRef.size()];
		AuditTrailEntryList ateList = right.getAuditTrailEntryList();
		LogEvent[] rightTrace = new LogEvent[ateList.size()];
		try {
			AuditTrailEntry ate;
			LogEvent event;
			for (int i = 0; i < ateListRef.size(); i++) {
				ate = ateListRef.get(i);
				event = events.findLogEvent(ate.getName(), ate.getType());
				leftTrace[i] = event;
			}
			for (int i = 0; i < ateList.size(); i++) {
				ate = ateList.get(i);
				event = events.findLogEvent(ate.getName(), ate.getType());
				rightTrace[i] = event;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		// make diff
		Diff diff = new Diff(leftTrace, rightTrace);
		diff.diff();
		// fill GUI traces
		instanceNameLabelRef
				.setForeground(LogPreviewUI.COLOR_LIST_SELECTION_FG);
		instanceNameLabelRef.setText(left.getName());
		instanceSizeLabelRef
				.setForeground(LogPreviewUI.COLOR_LIST_SELECTION_FG);
		instanceSizeLabelRef.setText(ateListRef.size() + " events");
		instanceNameLabel.setForeground(LogPreviewUI.COLOR_LIST_SELECTION_FG);
		instanceNameLabel.setText(right.getName());
		instanceSizeLabel.setForeground(LogPreviewUI.COLOR_LIST_SELECTION_FG);
		instanceSizeLabel.setText(ateList.size() + " events");
		DiffedAuditTrailEntryListModel diffModel = new DiffedAuditTrailEntryListModel(
				ateListRef, ateList, diff);
		diffPositions = diffModel.getDiffIndicees();
		eventsList.setModel(diffModel);
		eventsList.ensureIndexIsVisible(0);
	}

	// ///////////// Diff Navigation Methods

	private void findNextDiff() {
		int[] selectedIndices = eventsList.getSelectedIndices();
		int newIndex;
		if (selectedIndices.length != 0) {
			int currentSelection = selectedIndices[0];
			Integer temp = new Integer(currentSelection);
			// todo..
			int diffIndex;
			if (diffPositions.contains(temp)) {
				diffIndex = diffPositions.indexOf(temp);
				if (diffIndex != diffPositions.size() - 1) {
					newIndex = diffPositions.get(diffIndex + 1);
				} else {
					newIndex = diffPositions.get(0);
				}
			} else {
				ArrayList<Integer> tempList = new ArrayList<Integer>();
				tempList.addAll(diffPositions);
				tempList.add(temp);
				Collections.sort(tempList);
				int tempPos = tempList.indexOf(temp);
				if (tempPos != tempList.size() - 1) {
					newIndex = tempList.get(tempPos + 1);
				} else {
					newIndex = tempList.get(0);
				}
			}
		} else {
			newIndex = diffPositions.get(0);
		}
		jumpToDiff(newIndex);
	}

	private void findPrevDiff() {
		int[] selectedIndices = eventsList.getSelectedIndices();
		int newIndex;
		if (selectedIndices.length != 0) {
			int currentSelection = selectedIndices[0];
			Integer temp = new Integer(currentSelection);
			int diffIndex;
			if (diffPositions.contains(temp)) {
				diffIndex = diffPositions.indexOf(temp);
				if (diffIndex != 0) {
					newIndex = diffPositions.get(diffIndex - 1);
				} else {
					newIndex = diffPositions.get(diffPositions.size() - 1);
				}
			} else {
				ArrayList<Integer> tempList = new ArrayList<Integer>();
				tempList.addAll(diffPositions);
				tempList.add(temp);
				Collections.sort(tempList);
				int tempPos = tempList.indexOf(temp);
				if (tempPos != 0) {
					newIndex = tempList.get(tempPos - 1);
				} else {
					newIndex = tempList.get(tempList.size() - 1);
				}
			}
		} else {
			newIndex = diffPositions.get(diffPositions.size() - 1);
		}
		jumpToDiff(newIndex);
	}

	private void jumpToDiff(int index) {
		eventsList.ensureIndexIsVisible(index);
		eventsList.clearSelection();
		eventsList.setSelectedIndex(index);
	}

	// ///////////// GUI Build-up Methods

	private RoundedPanel createEventsPanel(JList eventsDiffList,
			JLabel nameLabel, JLabel sizeLabel, JLabel nameRefLabel,
			JLabel sizeRefLabel) {
		RoundedPanel eventsPanelRef = new RoundedPanel(10, 5, 0);
		eventsPanelRef.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		eventsPanelRef.setBackground(LogPreviewUI.COLOR_ENCLOSURE_BG);
		eventsPanelRef
				.setLayout(new BoxLayout(eventsPanelRef, BoxLayout.Y_AXIS));
		// right panel
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setOpaque(false);
		rightPanel.add(nameLabel);
		rightPanel.add(sizeLabel);
		// left panel
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setOpaque(false);
		leftPanel.add(nameRefLabel);
		leftPanel.add(sizeRefLabel);
		// both left and right
		JPanel bothPanels = new JPanel();
		bothPanels.setLayout(new BoxLayout(bothPanels, BoxLayout.X_AXIS));
		bothPanels.setOpaque(false);
		bothPanels.add(Box.createHorizontalGlue());
		bothPanels.add(leftPanel);
		bothPanels.add(Box.createHorizontalGlue());
		bothPanels.add(rightPanel);
		bothPanels.add(Box.createHorizontalGlue());
		// list in scroll pane
		JScrollPane eventsScrollPane = new JScrollPane(eventsList);
		eventsScrollPane.setOpaque(false);
		eventsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		eventsScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		eventsScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar vBar = eventsScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), LogPreviewUI.COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(false);
		// assemble
		eventsPanelRef.add(bothPanels);
		eventsPanelRef.add(Box.createVerticalStrut(8));
		eventsPanelRef.add(eventsScrollPane);
		return eventsPanelRef;
	}

	private void createEventList(JList list) {
		list.setBackground(LogPreviewUI.COLOR_LIST_BG);
		list.setCellRenderer(new TraceDiffCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// eventsSelectionChanged();
			}
		});
	}

	private void createInstanceLabels(JLabel nameLabel, JLabel sizeLabel) {
		nameLabel.setOpaque(false);
		nameLabel.setForeground(LogPreviewUI.COLOR_NON_FOCUS);
		nameLabel.setFont(nameLabel.getFont().deriveFont(13f));
		nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		nameLabel.setHorizontalTextPosition(JLabel.CENTER);
		sizeLabel.setOpaque(false);
		sizeLabel.setForeground(LogPreviewUI.COLOR_NON_FOCUS);
		sizeLabel.setFont(sizeLabel.getFont().deriveFont(11f));
		sizeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		sizeLabel.setHorizontalAlignment(JLabel.CENTER);
		sizeLabel.setHorizontalTextPosition(JLabel.CENTER);
	}

}
