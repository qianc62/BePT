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
package org.processmining.analysis.benchmark.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.analysis.benchmark.BenchmarkItem;
import org.processmining.analysis.benchmark.metric.BenchmarkMetric;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class BenchmarkResultBarView extends JComponent {

	protected int topRightBorder = 10;
	protected int hBorder = 200;
	protected int vBorder = 40;
	protected double barRatio = 0.9;

	protected static Color COLOR_TXT = new Color(160, 160, 160);
	protected static Color TRANSPARENT = new Color(0, 0, 0, 0);
	protected float FONT_SIZE = 11f;
	protected Font font;
	protected FontMetrics fontMetrics;

	protected List<BenchmarkMetric> metrics;
	protected List<BenchmarkItem> items;
	protected double[][] measurements;

	public BenchmarkResultBarView(List<BenchmarkMetric> metricList,
			List<BenchmarkItem> itemList) {
		setBackground(Color.BLACK);
		// invert metric order as will be painted from bottom to top (to
		// preserve displayed order)
		metrics = new ArrayList<BenchmarkMetric>();
		Iterator<BenchmarkMetric> allMetrics = metricList.iterator();
		while (allMetrics.hasNext()) {
			BenchmarkMetric current = allMetrics.next();
			metrics.add(0, current); // add to start of list
		}
		items = itemList;
		measurements = new double[items.size()][metrics.size()];
		for (int x = items.size() - 1; x >= 0; x--) {
			for (int y = metrics.size() - 1; y >= 0; y--) {
				measurements[x][y] = items.get(x).getMeasurement(
						metrics.get(y).name());
			}
		}
		font = (new JLabel("test")).getFont().deriveFont(FONT_SIZE);
		fontMetrics = this.getFontMetrics(font);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		// calculate coordinate helpers
		int width = this.getWidth();
		int height = this.getHeight();
		int barOffset = (width - hBorder - topRightBorder) / items.size();
		int barHeight = (height - vBorder - topRightBorder);
		int barWidth = (int) (((double) barOffset * barRatio));
		// start drawing
		final Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(font);
		// fill background
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		// alternate bg with stripes
		int yStep = (int) Math.floor(((double) barHeight / (double) ((metrics
				.size() * 2) + 1)));
		int yStart = topRightBorder + barHeight - yStep;
		g2d.setColor(new Color(40, 40, 40));
		for (int i = 0; i < metrics.size(); i++) {
			g2d.fillRect(0, yStart, width, yStep);
			yStart -= yStep;
			yStart -= yStep;
		}
		// paint items
		int xStart = hBorder + ((barOffset - barWidth) / 2);
		for (int i = 0; i < items.size(); i++) {
			paintItemBar(g2d, i, xStart, topRightBorder, barWidth, barHeight);
			g2d.setColor(COLOR_TXT);
			String name = items.get(i).getName();
			int stringWidth = fontMetrics.stringWidth(name);
			g2d.drawString(name, xStart + (barWidth / 2) - (stringWidth / 2),
					height - vBorder + 20);
			xStart += barOffset;
		}
		// paint metrics names
		yStart = topRightBorder + barHeight - yStep - yStep;
		g2d.setColor(COLOR_TXT);
		for (int i = 0; i < metrics.size(); i++) {
			g2d.drawString(metrics.get(i).name(), 10, yStart + yStep - 3);
			yStart -= yStep;
			yStart -= yStep;
		}
		g2d.dispose();
	}

	protected void paintItemBar(Graphics2D g2d, int index, int x, int y,
			int width, int height) {
		// prepare data
		int centerX = x + (width / 2);
		int yStep = (int) Math.floor(((double) height / (double) ((metrics
				.size() * 2) + 1)));
		int[] xOffsets = new int[metrics.size()];
		Color[] yColors = new Color[metrics.size()];
		for (int v = metrics.size() - 1; v >= 0; v--) {
			double xOffset = measurements[index][v];
			if (xOffset < 0.0) {
				xOffset = 0.0;
			}
			xOffsets[v] = (int) ((width * xOffset) / 2);
			yColors[v] = createBackgroundColor(measurements[index][v]);
		}
		// render bar
		int lastY = y + height;
		int lastOffset = 0;
		Color lastColor = BenchmarkResultBarView.TRANSPARENT;
		for (int vert = 0; vert < metrics.size(); vert++) {
			// paint (potentially) skewed segment
			paintSegment(g2d, centerX, lastY, lastY - yStep, lastOffset,
					xOffsets[vert], lastColor, yColors[vert]);
			// paint straight segment
			lastY -= yStep;
			paintSegment(g2d, centerX, lastY, lastY - yStep, xOffsets[vert],
					xOffsets[vert], yColors[vert], yColors[vert]);
			lastY -= yStep;
			lastOffset = xOffsets[vert];
			lastColor = yColors[vert];
		}
		// paint final skewed segment (top closure)
		paintSegment(g2d, centerX, lastY, lastY - yStep, lastOffset, 0,
				lastColor, BenchmarkResultBarView.TRANSPARENT);
	}

	protected void paintSegment(Graphics2D g2d, int xCenter, int yA, int yB,
			int offsetA, int offsetB, Color colorA, Color colorB) {
		if (colorA.equals(colorB)) {
			g2d.setPaint(colorA);
		} else {
			GradientPaint gradient = new GradientPaint(xCenter, yA, colorA,
					xCenter, yB, colorB, false);
			g2d.setPaint(gradient);
		}
		int[] xCoords = { xCenter - offsetA, xCenter - offsetB,
				xCenter + offsetB, xCenter + offsetA };
		int[] yCoords = { yA, yB, yB, yA };
		g2d.fillPolygon(xCoords, yCoords, 4);
	}

	protected Color createBackgroundColor(double value) {
		if (value < 0.0) {
			return BenchmarkResultBarView.TRANSPARENT;
		}
		int red, green;
		if (value < 0.5) {
			red = 255;
			green = (int) (510.0 * value);
		} else {
			green = 255;
			red = (int) (510 * (1.0 - value));
		}
		return new Color(red, green, 0);
	}

}
