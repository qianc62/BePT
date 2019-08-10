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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JComponent;

import org.processmining.framework.log.LogEvents;
import org.processmining.framework.util.ColorRepository;
import org.processmining.mining.fuzzymining.metrics.unary.UnaryMetric;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class UnaryMetricsPanel extends JComponent implements MouseListener,
		MouseMotionListener {

	protected LogEvents events;
	protected ArrayList<UnaryMetric> metrics;
	protected ArrayList<Boolean> switches;
	protected static Color textColor = new Color(120, 120, 100);
	protected int mouseX;

	protected static NumberFormat numberFormat = NumberFormat.getInstance();
	{
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
	}

	public UnaryMetricsPanel(LogEvents events) {
		this.events = events;
		this.setBackground(Color.BLACK);
		metrics = new ArrayList<UnaryMetric>();
		switches = new ArrayList<Boolean>();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		mouseX = -1;
	}

	public void addMetric(UnaryMetric metric) {
		metrics.add(metric);
		switches.add(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		if (metrics.size() == 0) {
			// don't bother drawing empty collection
			super.paintComponent(g);
			return;
		}
		// prepare Colors
		Color[] colors = new Color[metrics.size()];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = ColorRepository.getGradualColor(((double) (i))
					/ ((double) colors.length - 1));
		}
		// prepare drawing
		int measurePoints = metrics.get(0).size();
		int chartHeight = height - (metrics.size() * 17) - 25;
		// draw charts
		if (width > measurePoints) {
			// more space than measure points
			int stepSize = width / measurePoints;
			for (int i = 0; i < metrics.size(); i++) {
				if (switches.get(i) == false) {
					continue;
				}
				UnaryMetric metric = metrics.get(i);
				g2d.setColor(colors[i]);
				int x = 0;
				int lastY = 5 + chartHeight
						- (int) (metric.getMeasure(0) * (double) chartHeight);
				for (int k = 1; k < measurePoints; k++) {
					int y = 5
							+ chartHeight
							- (int) (metric.getMeasure(k) * (double) chartHeight);
					g2d.drawLine(x, lastY, x + stepSize, y);
					lastY = y;
					x += stepSize;
				}
			}
			// draw info
			if (mouseX >= 0) {
				int eventIndex = mouseX / stepSize;
				if (eventIndex < metrics.get(0).size()) {
					int chartX = mouseX - (mouseX % stepSize);
					drawInfo(g2d, chartX, eventIndex, chartHeight, colors);
				}
			}
		} else {
			// more measure points than space
			int clusterSize = measurePoints / width;
			for (int i = 0; i < metrics.size(); i++) {
				if (switches.get(i) == false) {
					continue;
				}
				UnaryMetric metric = metrics.get(i);
				g2d.setColor(colors[i]);
				int actualWidth = measurePoints / clusterSize;
				int lastY = 5
						+ chartHeight
						- (int) (aggregateValues(0, clusterSize, metric) * (double) chartHeight);
				for (int x = 1; x < actualWidth; x++) {
					int y = 5
							+ chartHeight
							- (int) (aggregateValues(x, clusterSize, metric) * (double) chartHeight);
					g2d.drawLine(x - 1, lastY, x, y);
					lastY = y;
				}
			}
			if (mouseX >= 0 && mouseX < (measurePoints / clusterSize)) {
				int eventIndex = mouseX * clusterSize;
				int chartX = mouseX;
				drawInfo(g2d, chartX, eventIndex, chartHeight, colors);
			}
		}
		// draw legend
		int y = chartHeight + 10;
		g2d.setFont(g2d.getFont().deriveFont(11.0f));
		for (int i = 0; i < metrics.size(); i++) {
			g2d.setColor(colors[i]);
			g2d.fillRect(50, y + 1, 50, 15);
			g2d.setColor(textColor);
			g2d.drawString(metrics.get(i).getName(), 110, y + 14);
			y += 17;
		}
	}

	protected void drawInfo(Graphics2D g2d, int chartX, int eventIndex,
			int chartHeight, Color[] colors) {
		if (mouseX < 0) {
			return; // mouse outside
		}
		// determine info field width
		String lineA = events.get(eventIndex).getModelElementName();
		String lineB = "(" + events.get(eventIndex).getEventType() + ")";
		FontMetrics fontMetrics = this.getFontMetrics(g2d.getFont().deriveFont(
				12.0f));
		int widthA = fontMetrics.stringWidth(lineA);
		int widthB = fontMetrics.stringWidth(lineB);
		int width = Math.max(widthA, widthB);
		// prepare drawing
		Color bgColor = new Color(0, 0, 0, 0.6f);
		g2d.setFont(g2d.getFont().deriveFont(10.0f));
		UnaryMetric metric;
		// paint backgrounds
		g2d.setColor(bgColor);
		for (int i = 0; i < metrics.size(); i++) {
			if (switches.get(i) == false) {
				continue;
			}
			metric = metrics.get(i);
			double value = metric.getMeasure(eventIndex);
			int y = 5 + chartHeight - (int) (value * chartHeight);
			g2d.fillRect(chartX + 7, y - 9, 35, 16);
		}
		g2d.fillRect(chartX + 37, chartHeight - 57, width + 6, 32);
		// paint foregrounds
		for (int i = 0; i < metrics.size(); i++) {
			if (switches.get(i) == false) {
				continue;
			}
			metric = metrics.get(i);
			double value = metric.getMeasure(eventIndex);
			int y = 5 + chartHeight - (int) (value * chartHeight);
			g2d.setColor(colors[i]);
			g2d.fillOval(chartX - 2, y - 2, 5, 5);
			g2d.drawString(numberFormat.format(value), chartX + 10, y + 3);
		}
		g2d.setFont(g2d.getFont().deriveFont(12.0f));
		g2d.setColor(new Color(200, 200, 200));
		g2d.drawString(lineA, chartX + 40, chartHeight - 43);
		g2d.setColor(new Color(160, 160, 160));
		g2d.drawString(lineB, chartX + 40, chartHeight - 30);
	}

	protected double aggregateValues(int step, int clusterSize,
			UnaryMetric metric) {
		double value = 0.0;
		int start = step * clusterSize;
		for (int i = start; i < start + clusterSize; i++) {
			if (i >= metric.size()) {
				clusterSize--;
				break;
			}
			value += metric.getMeasure(i);
		}
		value /= (double) clusterSize;
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int upperY = this.getHeight() - (metrics.size() * 17) - 15;
		for (int i = 0; i < metrics.size(); i++) {
			if (x >= 50 && x <= 100) {
				if (y >= upperY && y < (upperY + 17)) {
					switches.set(i, !switches.get(i)); // flip the right switch
					repaint();
					return;
				}
			}
			upperY += 17;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		mouseX = e.getX();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		mouseX = -1;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		repaint();
	}

}