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
package org.processmining.mining.fuzzymining.vis.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.NiceIntegerSlider;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerCheckBoxUI;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.TimestampInjectionFilter;
import org.processmining.framework.ui.MainUI;
import org.processmining.mining.fuzzymining.filter.FuzzyGraphProjectionFilter;
import org.processmining.mining.fuzzymining.ui.FuzzyMiningResult;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AnimationTriggerPanel extends JPanel {

	protected final FuzzyMiningResult result;
	protected NiceIntegerSlider lookaheadSlider;
	protected NiceIntegerSlider extraLookaheadSlider;

	public AnimationTriggerPanel(FuzzyMiningResult result) {
		this.result = result;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		this.setBackground(new Color(80, 80, 80));
		RoundedPanel innerPanel = new RoundedPanel(20);
		innerPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		innerPanel.setBackground(new Color(140, 140, 140));
		innerPanel.setMaximumSize(new Dimension(390, 250));
		final JCheckBox discreteAnimBox = new JCheckBox(
				"discrete animation (inject timestamps)");
		boolean discretize = false;
		try {
			discretize = (result.getLogReader().getInstance(0)
					.getAuditTrailEntryList().get(0).getTimestamp() == null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		discreteAnimBox.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
		discreteAnimBox.setSelected(discretize);
		discreteAnimBox.setForeground(new Color(40, 40, 40));
		discreteAnimBox.setUI(new SlickerCheckBoxUI());
		JButton animButton = new AutoFocusButton("view animation");
		animButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		final FuzzyMiningResult refResult = this.result;
		animButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LogFilter projectionFilter = new FuzzyGraphProjectionFilter(
						refResult.getGraph());
				projectionFilter.setLowLevelFilter(refResult.getLogReader()
						.getLogFilter());
				if (discreteAnimBox.isSelected()) {
					TimestampInjectionFilter tsFilter = new TimestampInjectionFilter();
					tsFilter.setLowLevelFilter(projectionFilter);
					projectionFilter = tsFilter;
				}
				try {
					LogReader filteredLog = LogReaderFactory.createInstance(
							projectionFilter, refResult.getLogReader());
					AnimationFrame animFrame = new AnimationFrame(filteredLog,
							refResult.getGraph(), lookaheadSlider.getValue(),
							extraLookaheadSlider.getValue());
					MainUI.getInstance().createFrame("Fuzzy graph animation",
							animFrame);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		JLabel warningLabel = new JLabel(
				"<html><b>Note:</b> Depending on the size of your log,<br>"
						+ "creating the animation data may take some time.<br>"
						+ "Fuzzy graph animation is an experimental feature<br>"
						+ "which requires a fast CPU and ample memory.</html>");
		warningLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		warningLabel.setForeground(new Color(40, 40, 40));
		warningLabel.setOpaque(false);
		// lookahead sliders
		lookaheadSlider = new NiceIntegerSlider("Lookahead", 1, 25, 5);
		lookaheadSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int max = lookaheadSlider.getValue() - 1;
				extraLookaheadSlider.getSlider().setMaximum(max);
				if (extraLookaheadSlider.getValue() > max) {
					extraLookaheadSlider.setValue(max);
				}
			}
		});
		extraLookaheadSlider = new NiceIntegerSlider("Extra lookahead", 0, 15,
				3);
		innerPanel.add(Box.createVerticalStrut(15));
		innerPanel.add(warningLabel);
		innerPanel.add(Box.createVerticalStrut(15));
		innerPanel.add(discreteAnimBox);
		innerPanel.add(Box.createVerticalStrut(15));
		innerPanel.add(lookaheadSlider);
		innerPanel.add(Box.createVerticalStrut(8));
		innerPanel.add(extraLookaheadSlider);
		innerPanel.add(Box.createVerticalStrut(15));
		innerPanel.add(animButton);
		innerPanel.add(Box.createVerticalStrut(15));
		this.add(Box.createHorizontalGlue());
		this.add(innerPanel);
		this.add(Box.createHorizontalGlue());
	}

}
