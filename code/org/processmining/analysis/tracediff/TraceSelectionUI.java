package org.processmining.analysis.tracediff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.slicker.logdialog.LogPreviewUI;
import org.processmining.framework.util.GuiNotificationTarget;

public class TraceSelectionUI extends JPanel {

	protected JList leftList;
	protected JList rightList;

	protected LogReader log;
	protected GuiNotificationTarget target;

	/**
	 * Creates a GUI that allows to select two traces from a log. If there is
	 * notifcation target provided, then it will be invoked upon pressing the
	 * start button.
	 * 
	 * @param aLog
	 *            the log from which two traces should be selected
	 * @param aTarget
	 *            a target to notify when selection is completed (can be null)
	 */
	public TraceSelectionUI(LogReader aLog, GuiNotificationTarget aTarget) {
		log = aLog;
		target = aTarget;
		buildGUI();
		fillTraces();
	}

	public String getLeftSelection() {
		return (String) leftList.getModel().getElementAt(
				leftList.getSelectedIndex());
	}

	public String getRightSelection() {
		return (String) rightList.getModel().getElementAt(
				rightList.getSelectedIndex());
	}

	private void buildGUI() {
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BorderLayout());
		// create instances lists
		leftList = new JList();
		JPanel leftPanel = createInstanceList(leftList, "Reference Trace");
		rightList = new JList();
		JPanel rightPanel = createInstanceList(rightList, "Compared Trace");
		// create button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		JButton startButton = new AutoFocusButton("Start Diff");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				target.updateGUI();
			}
		});
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(startButton);
		// assemble GUI
		JPanel selectionPanel = new JPanel();
		selectionPanel.setOpaque(false);
		selectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		selectionPanel
				.setLayout(new BoxLayout(selectionPanel, BoxLayout.X_AXIS));
		selectionPanel.add(Box.createHorizontalGlue());
		selectionPanel.add(leftPanel);
		selectionPanel.add(rightPanel);
		selectionPanel.add(Box.createHorizontalGlue());
		this.add(selectionPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}

	private RoundedPanel createInstanceList(JList list, String title) {
		list.setBackground(LogPreviewUI.COLOR_LIST_BG);
		list.setForeground(LogPreviewUI.COLOR_LIST_FG);
		list.setSelectionBackground(LogPreviewUI.COLOR_LIST_SELECTION_BG);
		list.setSelectionForeground(LogPreviewUI.COLOR_LIST_SELECTION_FG);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane instancesScrollPane = new JScrollPane(list);
		instancesScrollPane.setOpaque(false);
		instancesScrollPane.setBorder(BorderFactory.createEmptyBorder());
		instancesScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		instancesScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar vBar = instancesScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), LogPreviewUI.COLOR_NON_FOCUS, 4, 12));
		vBar.setOpaque(false);
		// assemble instances list
		JLabel instancesListLabel = new JLabel(title);
		instancesListLabel.setOpaque(false);
		instancesListLabel.setForeground(LogPreviewUI.COLOR_LIST_SELECTION_FG);
		instancesListLabel
				.setFont(instancesListLabel.getFont().deriveFont(13f));
		instancesListLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instancesListLabel.setHorizontalAlignment(JLabel.CENTER);
		instancesListLabel.setHorizontalTextPosition(JLabel.CENTER);
		RoundedPanel instancesPanel = new RoundedPanel(10, 5, 0);
		instancesPanel.setBackground(LogPreviewUI.COLOR_ENCLOSURE_BG);
		instancesPanel
				.setLayout(new BoxLayout(instancesPanel, BoxLayout.Y_AXIS));
		instancesPanel.setMaximumSize(new Dimension(180, 1000));
		instancesPanel.setPreferredSize(new Dimension(180, 1000));
		instancesPanel.add(instancesListLabel);
		instancesPanel.add(Box.createVerticalStrut(8));
		instancesPanel.add(instancesScrollPane);
		return instancesPanel;
	}

	protected void fillTraces() {
		if (log != null) {
			// repopulate instance names list
			String[] instanceNames = new String[log.numberOfInstances()];
			for (int i = 0; i < instanceNames.length; i++) {
				instanceNames[i] = log.getInstance(i).getName();
			}
			leftList.setListData(instanceNames);
			rightList.setListData(instanceNames);
			leftList.setSelectedIndex(0);
			if (log.getInstances().size() > 1) {
				rightList.setSelectedIndex(1);
			} else {
				rightList.setSelectedIndex(0);
			}
		}
	}
}
