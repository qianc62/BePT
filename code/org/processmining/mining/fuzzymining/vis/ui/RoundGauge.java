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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import org.processmining.mining.fuzzymining.vis.paint.ColorSettings;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RoundGauge extends JComponent {

	protected Color bgColor = new Color(0, 0, 0);
	protected Color colorEmpty = new Color(0, 0, 220, 160);
	protected Color colorFull = new Color(240, 0, 0, 250);

	protected int radius = 50;
	protected int border = 5;

	protected int segments = 36;

	protected float percentage = 0f;

	public RoundGauge() {
		this(50, 5);
	}

	public RoundGauge(int radius, int border) {
		this.radius = radius;
		this.border = border;
		Dimension dim = new Dimension(radius + border + border, radius + border
				+ border);
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setPreferredSize(dim);
		this.setOpaque(true);
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
		if (this.isDisplayable() && this.isVisible()) {
			repaint();
		}
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(g2d.getFont().deriveFont(9f));
		float width = this.getWidth();
		float height = this.getHeight();
		// fill background
		g2d.setColor(bgColor);
		g2d.fill(new Rectangle2D.Float(0, 0, width + 1, height + 1));
		// assemble element path
		float pathX = (width / 2);
		float heightThird = (height - border - border) / 3;
		float pathY1 = (height / 2) + (heightThird / 2);
		float pathY2 = height - border;
		float offset1 = heightThird * 0.025f;
		float offset2 = heightThird * 0.12f;
		GeneralPath path = new GeneralPath();
		path.moveTo(pathX - offset1, pathY1);
		path.lineTo(pathX + offset1, pathY1);
		path.lineTo(pathX + offset2, pathY2);
		path.lineTo(pathX - offset2, pathY2);
		path.closePath();
		// determine number of segments
		int paintSegments = (int) Math.ceil((segments - 8) * percentage);
		double rotateStep = (Math.PI * 2) / segments;
		g2d.rotate(rotateStep * 5, width / 2, height / 2);
		for (int i = 0; i < paintSegments; i++) {
			g2d.setColor(ColorSettings.mix(colorEmpty, colorFull,
					((float) i / (float) (segments - 8))));
			g2d.fill(path);
			g2d.rotate(rotateStep, width / 2, height / 2);
		}
		g2d.dispose();
	}

}
