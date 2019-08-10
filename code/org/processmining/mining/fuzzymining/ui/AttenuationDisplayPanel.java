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
package org.processmining.mining.fuzzymining.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import org.processmining.mining.fuzzymining.attenuation.Attenuation;

/**
 * @author christian
 * 
 */
public class AttenuationDisplayPanel extends JComponent {

	protected Attenuation attenuation;
	protected int maxDistance;
	protected Color colorAxis = new Color(50, 50, 50);
	protected Color textColor = new Color(120, 120, 120);

	public AttenuationDisplayPanel(Attenuation attenuation, int maxDistance) {
		this.attenuation = attenuation;
		this.maxDistance = maxDistance;
		this.setBackground(Color.BLACK);
		this.setOpaque(true);
	}

	public void setAttenuation(Attenuation attenuation) {
		this.attenuation = attenuation;
	}

	public void setMaxDistance(int maxDistance) {
		this.maxDistance = maxDistance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		Insets insets = this.getInsets();
		int width = getWidth() - insets.left - insets.right;
		int height = getHeight() - insets.top - insets.bottom;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.BLACK);
		g2d.fillRect(insets.left, insets.top, width, height);
		g2d.setColor(colorAxis);
		g2d.drawLine(insets.left + 10, insets.top + 10, insets.left + 10,
				insets.top + height - 40); // y-axis
		g2d.drawLine(insets.left + 10, insets.top + height - 40, width - 10,
				insets.top + height - 40); // x-axis
		int barWidth = (width - 20) / ((maxDistance * 2) + 1);
		int barX = insets.left + barWidth + 10;
		int maxBarHeight = height - 50;
		int barHeight;
		int barY;
		g2d.setFont(g2d.getFont().deriveFont(10.0f));
		for (int i = 1; i <= maxDistance; i++) {
			g2d.setColor(encodeColor(attenuation.getAttenuationFactor(i)));
			barHeight = (int) ((double) maxBarHeight * attenuation
					.getAttenuationFactor(i));
			barY = insets.top + 10 + maxBarHeight - barHeight;
			g2d.fillRect(barX, barY, barWidth, barHeight);
			g2d.setColor(textColor);
			g2d.drawString(Integer.toString(i), barX + 3, insets.top + height
					- 15);
			barX += barWidth;
			barX += barWidth;
		}
		g2d.dispose();
	}

	protected Color encodeColor(double intensity) {
		float green = 0.2f + (float) intensity * 0.5f;
		return new Color(0.0f, green, 0.0f);
	}

}
