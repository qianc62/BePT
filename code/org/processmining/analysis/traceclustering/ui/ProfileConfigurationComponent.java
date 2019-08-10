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
package org.processmining.analysis.traceclustering.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.traceclustering.profile.Profile;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ProfileConfigurationComponent extends SmoothPanel implements
		ChangeListener {

	protected static Color COLOR_FG = new Color(50, 50, 50);
	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_BG_HILIGHT = new Color(160, 160, 160);
	protected static DecimalFormat format = new DecimalFormat("0.000");

	protected JCheckBox activeBox;
	protected JCheckBox invertBox;
	protected JSlider slider;
	protected JLabel weightLabel;
	protected double rememberWeight;
	protected Profile profile;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public ProfileConfigurationComponent(Profile aProfile) {
		profile = aProfile;
		this.setBackground(COLOR_BG);
		this.setHighlight(COLOR_BG_HILIGHT);
		this.setToolTipText(profile.getDescription());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		this.setMinimumSize(new Dimension(400, 70));
		this.setMaximumSize(new Dimension(1000, 120));
		this.setPreferredSize(new Dimension(390, 120));
		JLabel profileLabel = new JLabel(profile.getName());
		profileLabel.setOpaque(false);
		profileLabel.setForeground(new Color(240, 240, 240));
		activeBox = new JCheckBox("active");
		activeBox.setOpaque(false);
		activeBox.setSelected(profile.getNormalizationMaximum() > 0.0);
		activeBox.addChangeListener(this);
		activeBox.setForeground(COLOR_FG);
		invertBox = new JCheckBox("invert");
		invertBox.setOpaque(false);
		invertBox.setSelected(profile.isInverted());
		invertBox.addChangeListener(this);
		invertBox.setForeground(COLOR_FG);
		slider = new JSlider(JSlider.HORIZONTAL, 0, 1000, (int) (profile
				.getNormalizationMaximum() * 1000));
		slider.setOpaque(false);
		slider.addChangeListener(this);
		JLabel sliderLabel = new JLabel("weight:");
		sliderLabel.setOpaque(false);
		sliderLabel.setForeground(COLOR_FG);
		weightLabel = new JLabel(format.format(profile
				.getNormalizationMaximum()));
		weightLabel.setOpaque(false);
		weightLabel.setForeground(COLOR_FG);
		JPanel upperPanel = new JPanel();
		upperPanel.setOpaque(false);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
		upperPanel.add(profileLabel);
		upperPanel.add(Box.createHorizontalGlue());
		upperPanel.add(invertBox);
		upperPanel.add(Box.createHorizontalStrut(10));
		upperPanel.add(activeBox);
		JPanel lowerPanel = new JPanel();
		lowerPanel.setOpaque(false);
		lowerPanel.setLayout(new BorderLayout());
		lowerPanel.add(sliderLabel, BorderLayout.WEST);
		lowerPanel.add(slider, BorderLayout.CENTER);
		lowerPanel.add(weightLabel, BorderLayout.EAST);
		JLabel descrLabel = new JLabel(profile.getDescription() + " ("
				+ profile.numberOfItems() + " items)");
		descrLabel.setAlignmentX(LEFT_ALIGNMENT);
		descrLabel.setForeground(COLOR_FG);
		descrLabel.setOpaque(false);
		descrLabel.setFont(descrLabel.getFont().deriveFont(10.0f).deriveFont(
				Font.ITALIC));
		JPanel midPanel = new JPanel();
		midPanel.setOpaque(false);
		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.X_AXIS));
		midPanel.add(descrLabel);
		midPanel.add(Box.createHorizontalGlue());
		this.add(upperPanel);
		this.add(Box.createVerticalStrut(4));
		this.add(midPanel);
		this.add(Box.createVerticalStrut(4));
		this.add(lowerPanel);
		this.add(Box.createVerticalGlue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == slider) {
			double value = (double) slider.getValue() / 1000.0;
			profile.setNormalizationMaximum(value);
			weightLabel.setText(format.format(value));
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() == activeBox) {
			if (activeBox.isSelected() == true) {
				slider.setEnabled(true);
				profile
						.setNormalizationMaximum((double) slider.getValue() / 1000.0);
			} else {
				slider.setEnabled(false);
				rememberWeight = profile.getNormalizationMaximum();
				profile.setNormalizationMaximum(0.0);
			}
			repaint();
		} else if (evt.getSource() == invertBox) {
			profile.setInvert(invertBox.isSelected());
			repaint();
		} else if (evt.getSource() == slider) {
			double value = (double) slider.getValue() / 1000.0;
			profile.setNormalizationMaximum(value);
			weightLabel.setText(format.format(value));
			repaint();
		}
	}

}
