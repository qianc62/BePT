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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.RoundGauge;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.mining.fuzzymining.vis.anim.Animation;
import org.processmining.mining.fuzzymining.vis.anim.AnimationListener;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public abstract class AnimationMeter extends RoundedPanel implements
		AnimationListener {

	protected Color bgColor = new Color(0, 0, 0);
	protected Color labelColor = new Color(200, 200, 200, 80);
	protected Color valueColor = new Color(200, 200, 200, 120);

	protected Animation animation;

	protected long updateInterval = 500;
	protected long lastUpdate = 0;

	protected long modelTime;
	protected RoundGauge gauge;
	protected JLabel label;
	protected JLabel value;

	protected AnimationMeter(Animation anim, String name) {
		super(15, 5, 0);
		Dimension size = new Dimension(170, 70);
		this.setMinimumSize(size);
		this.setMaximumSize(size);
		this.setPreferredSize(size);
		this.animation = anim;
		this.setBackground(bgColor);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.gauge = new RoundGauge(50, 0);
		this.label = new JLabel(name);
		this.label.setOpaque(true);
		this.label.setBackground(bgColor);
		this.label.setFont(this.label.getFont().deriveFont(11f));
		this.label.setForeground(labelColor);
		this.label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		this.label.setHorizontalAlignment(JLabel.LEFT);
		this.label.setHorizontalTextPosition(JLabel.LEFT);
		this.value = new JLabel("");
		this.value.setOpaque(true);
		this.value.setBackground(bgColor);
		this.value.setFont(this.value.getFont().deriveFont(18f));
		this.value.setForeground(valueColor);
		this.value.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		this.value.setHorizontalAlignment(JLabel.LEFT);
		this.value.setHorizontalTextPosition(JLabel.LEFT);
		JPanel labelPanel = new JPanel();
		labelPanel.setOpaque(true);
		labelPanel.setBackground(bgColor);
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
		labelPanel.setBorder(BorderFactory.createEmptyBorder());
		labelPanel.add(this.label);
		labelPanel.add(Box.createVerticalStrut(4));
		labelPanel.add(this.value);
		labelPanel.add(Box.createVerticalGlue());
		this.add(this.gauge);
		this.add(Box.createHorizontalStrut(5));
		this.add(labelPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#started()
	 */
	public void started() {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#stopped()
	 */
	public void stopped() {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.vis.anim.AnimationListener#
	 * updateModelTime(long)
	 */
	public void updateModelTime(long modelTime) {
		long current = System.currentTimeMillis();
		if ((current - lastUpdate) > updateInterval) {
			lastUpdate = current;
			update(modelTime);
		}
	}

	protected Animation getAnimation() {
		return animation;
	}

	protected void setState(float percentage, String valueText) {
		this.gauge.setPercentage(percentage);
		this.value.setText(valueText);
	}

	protected abstract void update(long modelTime);

}
