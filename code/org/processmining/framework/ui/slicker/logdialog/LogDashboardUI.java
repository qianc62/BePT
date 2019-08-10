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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.DistributionUI;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.MainUI;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LogDashboardUI extends JPanel {

	protected Color colorEnclosureBg = new Color(250, 250, 250, 105);
	protected Color colorTitleFg = new Color(20, 20, 20, 230);
	protected Color colorInfoBg = new Color(60, 60, 60, 160);
	protected Color colorInfoBgMouseOver = new Color(60, 60, 60, 240);
	protected Color colorInfoLabel = new Color(210, 210, 210);
	protected Color colorInfoValue = new Color(255, 255, 255);

	protected SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	protected SlickerOpenLogSettings parent;
	protected LogSummary summary;

	protected int numberOfProcesses = 0;
	protected int numberOfCases = 0;
	protected int numberOfEvents = 0;
	protected int minEventsPerCase = 0;
	protected int maxEventsPerCase = 0;
	protected int meanEventsPerCase = 0;
	protected int[] eventsPerCases = {};
	protected int minEventClassesPerCase = 0;
	protected int maxEventClassesPerCase = 0;
	protected int meanEventClassesPerCase = 0;
	protected int[] eventClassesPerCases = {};
	protected int numberOfEventTypes = 0;
	protected int numberOfEventClasses = 0;
	protected int numberOfOriginators = 0;

	public LogDashboardUI(SlickerOpenLogSettings parent) {
		this.parent = parent;
		calculateData();
		setupGui();
	}

	protected void calculateData() {
		LogReader log = parent.getOriginalLog();
		this.summary = log.getLogSummary();
		this.numberOfProcesses = log.numberOfProcesses();
		this.numberOfCases = log.numberOfInstances();
		this.numberOfEvents = 0;
		this.numberOfEventTypes = this.summary.getEventTypes().length;
		this.numberOfEventClasses = this.summary.getLogEvents().size();
		this.numberOfOriginators = this.summary.getOriginators().length;
		this.meanEventsPerCase = 0;
		this.meanEventClassesPerCase = 0;
		this.eventClassesPerCases = new int[log.numberOfInstances()];
		this.eventsPerCases = new int[log.numberOfInstances()];
		for (int i = 0; i < log.numberOfInstances(); i++) {
			int events = log.getInstance(i).getAuditTrailEntryList().size();
			this.eventsPerCases[i] = events;
			this.numberOfEvents += events;
			this.meanEventsPerCase += events;
			int eventClasses = this.summary.getEventsForInstance(
					log.getInstance(i)).size();
			this.eventClassesPerCases[i] = eventClasses;
			this.meanEventClassesPerCase += eventClasses;
		}
		if (this.numberOfEvents == 0) {
			return;
		}
		Arrays.sort(this.eventsPerCases);
		this.minEventsPerCase = this.eventsPerCases[0];
		this.maxEventsPerCase = this.eventsPerCases[this.eventsPerCases.length - 1];
		this.meanEventsPerCase /= log.numberOfInstances();
		Arrays.sort(this.eventClassesPerCases);
		this.minEventClassesPerCase = this.eventClassesPerCases[0];
		this.maxEventClassesPerCase = this.eventClassesPerCases[this.eventClassesPerCases.length - 1];
		this.meanEventClassesPerCase /= log.numberOfInstances();
	}

	protected void setupGui() {
		this.setOpaque(false);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(1, 2, 4, 4));
		// create key data panel
		RoundedPanel keyPanel = new RoundedPanel(15, 0, 3);
		keyPanel.setMinimumSize(new Dimension(180, 100));
		keyPanel.setMaximumSize(new Dimension(300, 1000));
		keyPanel.setPreferredSize(new Dimension(200, 500));
		keyPanel.setBackground(colorEnclosureBg);
		keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.Y_AXIS));
		keyPanel.add(getLeftAlignedHeader("Key data"));
		keyPanel.add(Box.createVerticalStrut(15));
		keyPanel.add(packInfo("Processes", Integer
				.toString(this.numberOfProcesses)));
		keyPanel.add(Box.createVerticalStrut(3));
		keyPanel.add(packInfo("Cases", Integer.toString(this.numberOfCases)));
		keyPanel.add(Box.createVerticalStrut(3));
		keyPanel.add(packInfo("Events", Integer.toString(this.numberOfEvents)));
		keyPanel.add(Box.createVerticalStrut(15));
		keyPanel.add(packInfo("Event classes", Integer
				.toString(this.numberOfEventClasses)));
		keyPanel.add(Box.createVerticalStrut(3));
		keyPanel.add(packInfo("Event types", Integer
				.toString(this.numberOfEventTypes)));
		keyPanel.add(Box.createVerticalStrut(15));
		keyPanel.add(packInfo("Originators", Integer
				.toString(this.numberOfOriginators)));
		keyPanel.add(Box.createVerticalGlue());
		// create info panel
		RoundedPanel infoPanel = new RoundedPanel(15, 0, 5);
		infoPanel.setMinimumSize(new Dimension(170, 100));
		infoPanel.setMaximumSize(new Dimension(300, 1000));
		infoPanel.setPreferredSize(new Dimension(190, 500));
		infoPanel.setBackground(colorEnclosureBg);
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.add(getLeftAlignedHeader("Log info"));
		infoPanel.add(Box.createVerticalStrut(12));
		infoPanel
				.add(getInfoPanel("Source", this.summary.getSource().getName()));
		String sourceProgram = this.summary.getSource().getData()
				.get("program");
		if (sourceProgram != null && sourceProgram.length() > 0) {
			infoPanel.add(Box.createVerticalStrut(6));
			infoPanel.add(getInfoPanel("Source program", sourceProgram));
		}
		infoPanel.add(Box.createVerticalStrut(10));
		infoPanel.add(getInfoPanel("Start date", this.getStartTime(parent
				.getOriginalLog())));
		infoPanel.add(Box.createVerticalStrut(6));
		infoPanel.add(getInfoPanel("End date", this.getEndTime(parent
				.getOriginalLog())));
		String description = this.summary.getWorkflowLog().getDescription();
		if (description != null && description.length() > 0) {
			infoPanel.add(Box.createVerticalStrut(10));
			infoPanel.add(getInfoPanel("Description", description));
		}
		infoPanel.add(Box.createVerticalGlue());
		// create action panel
		RoundedPanel actionPanel = new RoundedPanel(15, 0, 0);
		actionPanel.setBackground(new Color(250, 250, 250, 80));
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		AutoFocusButton actionButton = new AutoFocusButton(
				"start analyzing this log");
		actionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MainUI.getInstance().showLauncher();
			}
		});
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.add(actionButton);
		actionPanel.add(Box.createHorizontalGlue());
		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createEmptyBorder());
		rightPanel.setMinimumSize(new Dimension(180, 100));
		rightPanel.setMaximumSize(new Dimension(300, 1000));
		rightPanel.setPreferredSize(new Dimension(200, 500));
		rightPanel.setOpaque(false);
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(infoPanel);
		rightPanel.add(Box.createVerticalStrut(10));
		rightPanel.add(actionPanel);
		// right side
		JPanel distributionPanel = new JPanel();
		distributionPanel.setOpaque(false);
		distributionPanel.setBorder(BorderFactory.createEmptyBorder());
		distributionPanel.setLayout(new BoxLayout(distributionPanel,
				BoxLayout.Y_AXIS));
		distributionPanel.add(getDistributionPanel("Events per case",
				this.eventsPerCases, this.meanEventsPerCase));
		distributionPanel.add(Box.createVerticalStrut(10));
		distributionPanel.add(getDistributionPanel("Event classes per case",
				this.eventClassesPerCases, this.meanEventClassesPerCase));
		// assemble
		this.add(keyPanel);
		this.add(Box.createHorizontalStrut(10));
		this.add(distributionPanel);
		this.add(Box.createHorizontalStrut(10));
		this.add(rightPanel);
		revalidate();
	}

	protected RoundedPanel packInfo(String name, String value) {
		RoundedPanel packed = new RoundedPanel(10, 0, 0);
		packed.setBackground(colorInfoBg);
		final RoundedPanel target = packed;
		packed.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) { /* ignore */
			}

			public void mouseEntered(MouseEvent arg0) {
				target.setBackground(colorInfoBgMouseOver);
				target.repaint();
			}

			public void mouseExited(MouseEvent arg0) {
				target.setBackground(colorInfoBg);
				target.repaint();
			}

			public void mousePressed(MouseEvent arg0) { /* ignore */
			}

			public void mouseReleased(MouseEvent arg0) { /* ignore */
			}
		});
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		JLabel nameLabel = new JLabel(name);
		nameLabel.setOpaque(false);
		nameLabel.setForeground(colorInfoLabel);
		nameLabel.setFont(nameLabel.getFont().deriveFont(12f));
		JLabel valueLabel = new JLabel(value);
		valueLabel.setOpaque(false);
		valueLabel.setForeground(colorInfoValue);
		valueLabel.setFont(valueLabel.getFont().deriveFont(14f));
		packed.add(Box.createHorizontalStrut(5));
		packed.add(nameLabel);
		packed.add(Box.createHorizontalGlue());
		packed.add(valueLabel);
		packed.add(Box.createHorizontalStrut(5));
		packed.revalidate();
		return packed;
	}

	protected RoundedPanel getDistributionPanel(String title, int[] values,
			int meanValue) {
		// create distribution panel
		RoundedPanel instancePanel = new RoundedPanel(15, 0, 0);
		instancePanel.setBackground(colorEnclosureBg);
		instancePanel.setLayout(new BoxLayout(instancePanel, BoxLayout.Y_AXIS));
		instancePanel.add(getLeftAlignedHeader(title));
		instancePanel.add(Box.createVerticalStrut(6));
		if (values.length == 0) {
			return instancePanel;
		}
		RoundedPanel instanceDistPanel = new RoundedPanel(10, 0, 0);
		instanceDistPanel.setBackground(new Color(20, 20, 20));
		instanceDistPanel.setLayout(new BorderLayout());
		DistributionUI instanceDistUI = new DistributionUI(values);
		instanceDistPanel.add(instanceDistUI, BorderLayout.CENTER);
		JPanel keyPanel = new JPanel();
		keyPanel.setOpaque(false);
		keyPanel.setBorder(BorderFactory.createEmptyBorder());
		keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.X_AXIS));
		keyPanel.add(packInfo("Min", Integer.toString(values[0])));
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(packInfo("Mean", Integer.toString(meanValue)));
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(packInfo("Max", Integer
				.toString(values[values.length - 1])));
		instancePanel.add(instanceDistPanel);
		instancePanel.add(Box.createVerticalStrut(4));
		instancePanel.add(keyPanel);
		return instancePanel;
	}

	protected JPanel getLeftAlignedHeader(String title) {
		JLabel hLabel = new JLabel(title);
		hLabel.setOpaque(false);
		hLabel.setForeground(colorTitleFg);
		hLabel.setFont(hLabel.getFont().deriveFont(15f));
		return alignLeft(hLabel);
	}

	protected JPanel alignLeft(JComponent component) {
		JPanel hPanel = new JPanel();
		hPanel.setBorder(BorderFactory.createEmptyBorder());
		hPanel.setOpaque(false);
		hPanel.setLayout(new BoxLayout(hPanel, BoxLayout.X_AXIS));
		hPanel.add(component);
		hPanel.add(Box.createHorizontalGlue());
		return hPanel;
	}

	protected JPanel getInfoPanel(String name, String value) {
		JPanel infoPanel = new JPanel();
		infoPanel.setOpaque(false);
		infoPanel.setBorder(BorderFactory.createEmptyBorder());
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		JLabel nameLabel = new JLabel(name);
		nameLabel.setOpaque(false);
		nameLabel.setFont(nameLabel.getFont().deriveFont(10f));
		nameLabel.setForeground(new Color(60, 60, 60));
		JLabel valueLabel = new JLabel("<html>" + value + "</html>");
		valueLabel.setOpaque(false);
		valueLabel.setFont(nameLabel.getFont().deriveFont(12f));
		valueLabel.setForeground(new Color(10, 10, 10));
		infoPanel.add(alignLeft(nameLabel));
		infoPanel.add(Box.createVerticalStrut(2));
		infoPanel.add(alignLeft(valueLabel));
		return infoPanel;
	}

	protected String getStartTime(LogReader log) {
		Date startTime = null;
		for (int i = 0; i < log.numberOfProcesses(); i++) {
			Date start = this.summary.getStartTime(log.getProcess(i).getName());
			if (start != null) {
				if (startTime != null) {
					if (start.before(startTime)) {
						startTime = start;
					}
				} else {
					startTime = start;
				}
			}
		}
		if (startTime != null) {
			return dateFormat.format(startTime);
		} else {
			return "no timestamp information";
		}
	}

	protected String getEndTime(LogReader log) {
		Date endTime = null;
		for (int i = 0; i < log.numberOfProcesses(); i++) {
			Date end = this.summary.getEndTime(log.getProcess(i).getName());
			if (end != null) {
				if (endTime != null) {
					if (end.after(endTime)) {
						endTime = end;
					}
				} else {
					endTime = end;
				}
			}
		}
		if (endTime != null) {
			return dateFormat.format(endTime);
		} else {
			return "no timestamp information";
		}
	}

}
