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
import java.awt.RenderingHints;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JToolTip;

import org.deckfour.slickerbox.components.PToolTip;
import org.processmining.mining.fuzzymining.replay.ReplayListener;

/**
 * @author christian
 * 
 */
public class LedGauge extends JComponent implements ReplayListener {

	protected static Color bgColor = new Color(100, 100, 100);
	protected static Color borderColor = new Color(120, 120, 120);
	protected static Color passiveColor = new Color(100, 100, 100);
	protected static DecimalFormat format = new DecimalFormat("###.##%");
	protected double value;
	protected double progress;
	protected boolean isValid;
	protected String name;
	protected String description;
	protected int exponent;

	public LedGauge(String aName, String aDescription, int anExponent) {
		value = 0.0;
		progress = 0.0;
		isValid = false;
		name = aName;
		description = aDescription;
		exponent = anExponent;
	}

	public void setValue(double value) {
		if (value < 0.0 || value > 1.0) {
			System.err.println("invalid value! " + value);
			return;
		}
		this.value = value;
		isValid = true;
		this.setToolTipText(name + ": " + format.format(value) + "\n"
				+ description);
		if (this.isDisplayable()) {
			this.repaint();
		}
	}

	public void setProgress(double progress) {
		if (progress < 0.0 || progress > 1.0) {
			System.err.println("invalid progress! " + progress);
			return;
		}
		this.progress = progress;
		isValid = false;
		if (this.isDisplayable()) {
			this.repaint();
		}
	}

	public void setValid(boolean valid) {
		isValid = valid;
		if (this.isDisplayable()) {
			this.repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// draw background
		g2d.setColor(new Color(45, 45, 45));
		g2d.fillRect(0, 0, width, height);
		// draw gauge
		if (isValid == true) {
			double expValue = Math.pow(value, exponent);
			g2d.setColor(encodeColor(expValue));
			int barHeight = ((int) ((height - 12) * expValue));
			int y = (height - 6) - barHeight;
			g2d.fillRect(6, y, width - 12, barHeight);
		} else {
			g2d.setColor(passiveColor);
			int barHeight = ((int) ((height - 12) * progress));
			int y = (height - 6) - barHeight;
			g2d.fillRect(6, y, width - 12, barHeight);
		}
		// draw eye-candy
		Color dark1 = new Color(0, 0, 0, 90);
		Color dark2 = new Color(0, 0, 0, 120);
		for (int y = 8; y < height - 5; y += 3) {
			g2d.setColor(dark2);
			g2d.drawLine(6, y, width - 7, y);
			g2d.setColor(dark1);
			g2d.drawLine(6, y - 1, width - 7, y - 1);
		}
		// draw outer border
		g2d.setColor(bgColor);
		g2d.fillRect(0, 0, 3, height);
		g2d.fillRect(width - 3, 0, 3, height);
		g2d.fillRect(0, 0, width, 3);
		g2d.fillRect(0, height - 3, width, 3);
		// draw border
		g2d.setColor(borderColor);
		g2d.drawRect(3, 3, width - 7, height - 7);
	}

	protected Color encodeColor(double value) {
		float red, green, blue;
		if (value > 0.5) {
			red = (1f - (float) value) * 2f;
			green = 1.0f;
			blue = 0f;
		} else {
			red = 1.0f;
			green = (float) value * 2f;
			blue = 0f;
		}
		red *= 0.8f;
		green *= 0.8f;
		return new Color(red, green, blue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.replay.ReplayListener#setCoverage
	 * (double)
	 */
	public void setCoverage(double coverage) {
		setValue(coverage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#createToolTip()
	 */
	@Override
	public JToolTip createToolTip() {
		return new PToolTip();
	}

}
