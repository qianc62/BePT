package org.processmining.analysis.tracediff;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.HeaderBar;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.GuiNotificationTarget;

public class TraceDiffPluginUI extends JPanel implements GuiNotificationTarget {

	public enum TraceDiffMode {
		selection, // selection of traces to diff
		differences
		// display of diffed traces
	}

	protected TraceSelectionUI selectionUI;
	protected TraceComparisonUI comparisonUI;
	protected JPanel contentPanel;
	protected TraceDiffMode mode = TraceDiffMode.selection;

	protected LogReader log;

	public TraceDiffPluginUI(LogReader theLog) {
		log = theLog;
		buildGUI();
	}

	private void buildGUI() {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(80, 80, 80));
		HeaderBar header = new HeaderBar("Trace Diff Analysis");
		this.add(header, BorderLayout.NORTH);
		contentPanel = new JPanel(new BorderLayout());
		contentPanel.setOpaque(false);
		selectionUI = new TraceSelectionUI(log, this);
		contentPanel.add(selectionUI);
		this.add(contentPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() {
		if (mode == TraceDiffMode.selection) {
			String leftTrace = selectionUI.getLeftSelection();
			String rightTrace = selectionUI.getRightSelection();
			ProcessInstance left = null;
			ProcessInstance right = null;
			for (ProcessInstance pi : log.getInstances()) {
				if (pi.getName().equals(leftTrace)) {
					left = pi;
				} else if (pi.getName().equals(rightTrace)) {
					right = pi;
				}
			}
			if (left != null && right != null) {
				comparisonUI = new TraceComparisonUI(left, right, log, this);
				contentPanel.removeAll();
				contentPanel.add(comparisonUI);
				contentPanel.validate();
				contentPanel.repaint();
				mode = TraceDiffMode.differences;
			}
		} else if (mode == TraceDiffMode.differences) {
			contentPanel.removeAll();
			contentPanel.add(selectionUI);
			contentPanel.validate();
			contentPanel.repaint();
			mode = TraceDiffMode.selection;
		}
	}

}
