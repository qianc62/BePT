package org.processmining.framework.ui.slicker.logdialog;

import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.deckfour.slickerbox.components.GradientPanel;
import org.processmining.framework.log.AuditTrailEntry;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class EventCellRenderer extends GradientPanel implements
		ListCellRenderer {

	protected int height = 60;
	protected DateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss.SSS");

	protected JLabel nameLabel;
	protected JLabel numberLabel;
	protected JLabel originatorLabel;
	protected JLabel typeLabel;
	protected JLabel timestampLabel;

	/**
	 * @param colorTop
	 * @param colorBottom
	 */
	public EventCellRenderer() {
		super(LogPreviewUI.COLOR_LIST_BG, LogPreviewUI.COLOR_LIST_BG_LOWER);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setMinimumSize(new Dimension(180, height));
		setMaximumSize(new Dimension(500, height));
		setPreferredSize(new Dimension(250, height));
		nameLabel = new JLabel("name");
		nameLabel.setOpaque(false);
		nameLabel.setForeground(LogPreviewUI.COLOR_LIST_FG);
		nameLabel.setFont(nameLabel.getFont().deriveFont(13f));
		nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		nameLabel.setHorizontalTextPosition(JLabel.CENTER);
		originatorLabel = new JLabel("originator");
		originatorLabel.setOpaque(false);
		originatorLabel.setForeground(LogPreviewUI.COLOR_LIST_FG);
		originatorLabel.setFont(originatorLabel.getFont().deriveFont(9f));
		numberLabel = new JLabel("#");
		numberLabel.setOpaque(false);
		numberLabel.setForeground(LogPreviewUI.COLOR_LIST_FG);
		numberLabel.setFont(numberLabel.getFont().deriveFont(9f));
		typeLabel = new JLabel("type");
		typeLabel.setOpaque(false);
		typeLabel.setForeground(LogPreviewUI.COLOR_LIST_FG);
		typeLabel.setFont(typeLabel.getFont().deriveFont(11f));
		timestampLabel = new JLabel("timestamp");
		timestampLabel.setOpaque(false);
		timestampLabel.setForeground(LogPreviewUI.COLOR_LIST_FG);
		timestampLabel.setFont(timestampLabel.getFont().deriveFont(12f));
		timestampLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		timestampLabel.setHorizontalAlignment(JLabel.CENTER);
		timestampLabel.setHorizontalTextPosition(JLabel.CENTER);
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
		middlePanel.setOpaque(false);
		middlePanel.setBorder(BorderFactory.createEmptyBorder());
		middlePanel.add(Box.createHorizontalGlue());
		middlePanel.add(numberLabel);
		middlePanel.add(Box.createHorizontalStrut(10));
		middlePanel.add(typeLabel);
		middlePanel.add(Box.createHorizontalStrut(10));
		middlePanel.add(originatorLabel);
		middlePanel.add(Box.createHorizontalGlue());
		this.add(Box.createVerticalGlue());
		this.add(nameLabel);
		this.add(Box.createVerticalGlue());
		this.add(middlePanel);
		this.add(Box.createVerticalGlue());
		this.add(timestampLabel);
		this.add(Box.createVerticalGlue());
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
			this.setColors(LogPreviewUI.COLOR_LIST_SELECTION_BG,
					LogPreviewUI.COLOR_LIST_SELECTION_BG_LOWER);
		} else {
			this.setColors(LogPreviewUI.COLOR_LIST_BG,
					LogPreviewUI.COLOR_LIST_BG_LOWER);
		}
		AuditTrailEntry ate = (AuditTrailEntry) value;
		nameLabel.setText(ate.getElement());
		typeLabel.setText(ate.getType());
		numberLabel.setText("#" + (index + 1));
		if (ate.getOriginator() != null) {
			originatorLabel.setText("@" + ate.getOriginator());
		} else {
			originatorLabel.setText("");
		}
		if (ate.getTimestamp() != null) {
			timestampLabel.setText(dateFormat.format(ate.getTimestamp()));
		} else {
			timestampLabel.setText("- no timestamp -");
		}
		return this;
	}

}