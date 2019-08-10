package org.processmining.analysis.tracediff;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.deckfour.slickerbox.components.GradientPanel;
import org.processmining.analysis.tracediff.EventDiff.DiffType;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.ui.slicker.logdialog.LogPreviewUI;

/**
 * Renderer displaying the diff of two events in one list cell. Each element in
 * the list consists of two aligned audit trail entries and their type of
 * difference at this position.
 * 
 * @see DiffedAuditTrailEntryListModel
 * 
 * @author Anne Rozinat (a.rozinat at tuel.nl)
 */
public class TraceDiffCellRenderer extends GradientPanel implements
		ListCellRenderer {

	protected int height = 60;
	protected static Color COLOR_ONE_DIFF = new Color(0, 155, 0, 140);
	protected static Color COLOR_BOTH_DIFF = new Color(155, 0, 0, 140);
	protected static Color COLOR_DIFFED_CELL = new Color(0, 80, 0);
	protected static Color COLOR_DIFFED_CELL_LOWER = new Color(0, 30, 0);
	protected static Color COLOR_SELECTED_BG = new Color(200, 200, 200);
	protected static Color COLOR_SELECTED_BG_LOWER = new Color(150, 150, 150);

	protected EventPanel leftPanel;
	protected EventPanel rightPanel;
	protected JPanel middlePanel;
	protected MiddleCell middleCell;

	/**
	 * Create cell renderer that can be used in a list providing 'EventDiff'
	 * objects.
	 * 
	 * @see EventDiff
	 */
	public TraceDiffCellRenderer() {
		super(LogPreviewUI.COLOR_LIST_BG, LogPreviewUI.COLOR_LIST_BG_LOWER);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		leftPanel = new EventPanel();
		rightPanel = new EventPanel();
		middlePanel = new JPanel();
		middlePanel.setOpaque(false);
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
		middlePanel.setMinimumSize(new Dimension(70, height));
		middlePanel.setMaximumSize(new Dimension(70, height));
		middlePanel.setPreferredSize(new Dimension(70, height));
		middleCell = new MiddleCell(DiffType.none);
		middlePanel.add(middleCell);
		this.add(leftPanel);
		this.add(middlePanel);
		this.add(rightPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		// configure yourself
		if (isSelected) {
			this.setColors(COLOR_SELECTED_BG, COLOR_SELECTED_BG_LOWER);
		} else {
			this.setColors(LogPreviewUI.COLOR_LIST_BG,
					LogPreviewUI.COLOR_LIST_BG_LOWER);
		}
		// get diff contents at this position
		EventDiff evDiff = (EventDiff) value;
		AuditTrailEntry leftAte = evDiff.getLeft();
		AuditTrailEntry rightAte = evDiff.getRight();
		// update panel appearances
		middleCell.setType(evDiff.getType());
		if (evDiff.getType() == DiffType.both) {
			leftPanel.setOpaque(true);
			leftPanel.setColors(LogPreviewUI.COLOR_LIST_SELECTION_BG,
					LogPreviewUI.COLOR_LIST_SELECTION_BG_LOWER);
			rightPanel.setOpaque(true);
			rightPanel.setColors(LogPreviewUI.COLOR_LIST_SELECTION_BG,
					LogPreviewUI.COLOR_LIST_SELECTION_BG_LOWER);
		} else if (evDiff.getType() == DiffType.left) {
			leftPanel.setOpaque(true);
			leftPanel.setColors(COLOR_DIFFED_CELL, COLOR_DIFFED_CELL_LOWER);
			rightPanel.setColors(LogPreviewUI.COLOR_LIST_BG,
					LogPreviewUI.COLOR_LIST_BG_LOWER);
			rightPanel.setOpaque(false);
		} else if (evDiff.getType() == DiffType.right) {
			leftPanel.setColors(LogPreviewUI.COLOR_LIST_BG,
					LogPreviewUI.COLOR_LIST_BG_LOWER);
			leftPanel.setOpaque(false);
			rightPanel.setOpaque(true);
			rightPanel.setColors(COLOR_DIFFED_CELL, COLOR_DIFFED_CELL_LOWER);
		} else {
			leftPanel.setOpaque(true);
			leftPanel.setColors(LogPreviewUI.COLOR_LIST_BG,
					LogPreviewUI.COLOR_LIST_BG_LOWER);
			rightPanel.setOpaque(true);
			rightPanel.setColors(LogPreviewUI.COLOR_LIST_BG,
					LogPreviewUI.COLOR_LIST_BG_LOWER);
		}
		middlePanel.validate();
		middlePanel.repaint();
		// fill event contents
		fillEventContent(leftPanel, leftAte, evDiff.getLeftIndex());
		fillEventContent(rightPanel, rightAte, evDiff.getRightIndex());
		return this;
	}

	/**
	 * Fills event cell with the appropriate content or leaves empty.
	 * 
	 * @param panel
	 *            the event cell panel to be filled
	 * @param ate
	 *            the event to be represented (can be null)
	 * @param ateIndex
	 *            the original index of the given audit trail entry to be
	 *            displayed
	 */
	private void fillEventContent(EventPanel panel, AuditTrailEntry ate,
			int ateIndex) {
		if (ate != null) {
			panel.setNameLabel(ate.getElement());
			panel.setTypeLabel(ate.getType());
			panel.setNumberLabel("" + (ateIndex + 1));
			panel.setOriginatorLabel(ate.getOriginator());
			panel.setTimestampLabel(ate.getTimestamp());
		} else {
			panel.setNameLabel("");
			panel.setTypeLabel("");
			panel.setNumberLabel(null);
			panel.setOriginatorLabel(null);
			panel.setTimestampLabel(null);
		}
	}

	/**
	 * Custom component painting the diff symbol to be displayed between two
	 * diffed events in each row.
	 * 
	 * @author Christian Günther (c.w.gunther at tue.nl)
	 */
	protected class MiddleCell extends JComponent {

		protected DiffType type;

		public MiddleCell(DiffType type) {
			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder());
		}

		public void setType(DiffType type) {
			this.type = type;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			float width = getWidth();
			float height = getHeight();
			float xCenter = width / 2;
			float yCenter = height / 2;
			float offset = (Math.min(xCenter, yCenter) * 0.8f);
			float distance = offset * 0.1f;
			if (type.equals(DiffType.left)) {
				g2d.setColor(COLOR_ONE_DIFF);
				GeneralPath triPath = new GeneralPath();
				triPath.moveTo(xCenter - distance, yCenter - offset);
				triPath.lineTo(xCenter - distance, yCenter + offset);
				triPath.lineTo(xCenter - distance - offset, yCenter);
				triPath.closePath();
				g2d.fill(triPath);
			} else if (type.equals(DiffType.right)) {
				g2d.setColor(COLOR_ONE_DIFF);
				GeneralPath triPath = new GeneralPath();
				triPath.moveTo(xCenter + distance, yCenter - offset);
				triPath.lineTo(xCenter + distance, yCenter + offset);
				triPath.lineTo(xCenter + distance + offset, yCenter);
				triPath.closePath();
				g2d.fill(triPath);
			} else if (type.equals(DiffType.both)) {
				g2d.setColor(COLOR_BOTH_DIFF);
				// paint left triangle
				GeneralPath triPath = new GeneralPath();
				triPath.moveTo(xCenter - distance, yCenter - offset);
				triPath.lineTo(xCenter - distance, yCenter + offset);
				triPath.lineTo(xCenter - distance - offset, yCenter);
				triPath.closePath();
				g2d.fill(triPath);
				// paint right triangle
				triPath = new GeneralPath();
				triPath.moveTo(xCenter + distance, yCenter - offset);
				triPath.lineTo(xCenter + distance, yCenter + offset);
				triPath.lineTo(xCenter + distance + offset, yCenter);
				triPath.closePath();
				g2d.fill(triPath);
			}
			g2d.dispose();
		}
	}

}