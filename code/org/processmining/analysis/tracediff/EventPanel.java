package org.processmining.analysis.tracediff;

import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.GradientPanel;
import org.processmining.framework.ui.slicker.logdialog.LogPreviewUI;

/**
 * Panel representing an audit trail entry. The object can be reused
 * 
 * @author Anne Rozinat (a.ro
 * 
 */
public class EventPanel extends GradientPanel {

	protected int height = 60;
	protected DateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss.SSS");

	private JLabel nameLabel = new JLabel("name");
	private JLabel numberLabel = new JLabel("#");
	private JLabel originatorLabel = new JLabel("originator");
	private JLabel typeLabel = new JLabel("type");
	private JLabel timestampLabel = new JLabel("timestamp");

	public EventPanel() {
		super(LogPreviewUI.COLOR_LIST_BG, LogPreviewUI.COLOR_LIST_BG_LOWER);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setMinimumSize(new Dimension(180, height));
		this.setMaximumSize(new Dimension(500, height));
		this.setPreferredSize(new Dimension(250, height));
		nameLabel.setOpaque(false);
		nameLabel.setForeground(LogPreviewUI.COLOR_LIST_FG);
		nameLabel.setFont(nameLabel.getFont().deriveFont(13f));
		nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		nameLabel.setHorizontalTextPosition(JLabel.CENTER);
		originatorLabel.setOpaque(false);
		originatorLabel.setForeground(LogPreviewUI.COLOR_LIST_FG);
		originatorLabel.setFont(originatorLabel.getFont().deriveFont(9f));
		numberLabel.setOpaque(false);
		numberLabel.setForeground(LogPreviewUI.COLOR_LIST_FG);
		numberLabel.setFont(numberLabel.getFont().deriveFont(9f));
		typeLabel.setOpaque(false);
		typeLabel.setForeground(LogPreviewUI.COLOR_LIST_FG);
		typeLabel.setFont(typeLabel.getFont().deriveFont(11f));
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

	public void setNameLabel(String newName) {
		nameLabel.setText(newName);
	}

	public void setNumberLabel(String newIndex) {
		if (newIndex != null) {
			numberLabel.setText("#" + newIndex);
		} else {
			numberLabel.setText("");
		}
	}

	public void setOriginatorLabel(String newOriginator) {
		if (newOriginator != null) {
			originatorLabel.setText("@" + newOriginator);
		} else {
			originatorLabel.setText("");
		}
	}

	public void setTypeLabel(String newType) {
		typeLabel.setText(newType);
	}

	public void setTimestampLabel(Date newTimeStamp) {
		if (newTimeStamp != null) {
			timestampLabel.setText(dateFormat.format(newTimeStamp));
		} else {
			timestampLabel.setText("");
		}
	}
}