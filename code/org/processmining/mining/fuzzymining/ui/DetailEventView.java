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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;

import javax.swing.JComponent;

import org.processmining.mining.fuzzymining.replay.DetailNodeAnalysis;
import org.processmining.mining.fuzzymining.replay.DetailNodeAnalysis.SimplificationType;

/**
 * @author christian
 * 
 */
public class DetailEventView extends JComponent {

	protected static Color preservedColor = new Color(180, 120, 0);
	protected static Color clusteredColor = new Color(0, 200, 0);
	protected static Color removedColor = new Color(200, 0, 0);

	protected static DecimalFormat format = new DecimalFormat("0.000");

	protected DetailNodeAnalysis analysis;

	public DetailEventView(DetailNodeAnalysis analysis) {
		this.analysis = analysis;
		this.setMinimumSize(new Dimension(200, 40));
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		this.setPreferredSize(new Dimension(1000, 40));
		this.setOpaque(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		if (this.isOpaque() == true) {
			// draw background
			g2d.setColor(new Color(20, 20, 20));
			g2d.fillRect(0, 0, width, height);
		}
		// prepare
		String announcement;
		if (analysis.getType() == SimplificationType.PRESERVED) {
			g2d.setColor(preservedColor);
			announcement = "preserved";
		} else if (analysis.getType() == SimplificationType.CLUSTERED) {
			g2d.setColor(clusteredColor);
			announcement = "clustered";
		} else {
			g2d.setColor(removedColor);
			announcement = "removed";
		}
		g2d.setFont(g2d.getFont().deriveFont(12).deriveFont(Font.ITALIC));
		// draw announcement
		g2d.drawString(announcement, 20, 25);
		// draw gauge
		g2d.drawRect(100, 10, 199, 19);
		int gaugeWidth = (int) (196 * analysis.getNode().getSignificance());
		g2d.fillRect(102, 12, gaugeWidth, 16);
		// draw significance value
		g2d.setFont(g2d.getFont().deriveFont(12).deriveFont(Font.PLAIN));
		g2d.drawString("sig.: "
				+ format.format(analysis.getNode().getSignificance()), 310, 25);
		// draw event identifier
		String event = analysis.getEvent().getModelElementName() + " ("
				+ analysis.getEvent().getEventType() + ")";
		g2d.drawString(event, 410, 25);
	}

}
