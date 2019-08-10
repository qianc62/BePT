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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JComponent;

import org.processmining.mining.fuzzymining.vis.anim.AnimationListener;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class TimeDisplay extends JComponent implements AnimationListener {

	protected static final String[] DAY = new String[] { "Sun", "Mon", "Tue",
			"Wed", "Thu", "Fri", "Sat" };
	protected static final String[] MONTH = new String[] { "Jan", "Feb", "Mar",
			"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	protected static Color colorActive = new Color(240, 0, 0);
	protected static Color colorPassive = new Color(20, 20, 20);

	protected int cipherWidth = 13;
	protected int cipherHeight = 25;
	protected int segmentWidth = 3;

	protected int border = 7;

	protected int x1 = 0;
	protected int x2 = segmentWidth;
	protected int x3 = cipherWidth - segmentWidth;
	protected int x4 = cipherWidth;

	protected int y1 = 0;
	protected int y2 = segmentWidth;
	protected int y3 = (cipherHeight - segmentWidth) / 2;
	protected int y4 = cipherHeight / 2;
	protected int y5 = (cipherHeight + segmentWidth) / 2;
	protected int y6 = cipherHeight - segmentWidth;
	protected int y7 = cipherHeight;

	protected int[] TOP_X = new int[] { x1, x4, x3, x2 };
	protected int[] TOP_Y = new int[] { y1, y1, y2, y2 };

	protected int[] LEFTTOP_X = new int[] { x1, x2, x2, x1 };
	protected int[] LEFTTOP_Y = new int[] { y1, y2, y3, y4 };

	protected int[] RIGHTTOP_X = new int[] { x3, x4, x4, x3 };
	protected int[] RIGHTTOP_Y = new int[] { y2, y1, y4, y3 };

	protected int[] LEFTBOTTOM_X = new int[] { x1, x2, x2, x1 };
	protected int[] LEFTBOTTOM_Y = new int[] { y4, y5, y6, y7 };

	protected int[] RIGHTBOTTOM_X = new int[] { x3, x4, x4, x3 };
	protected int[] RIGHTBOTTOM_Y = new int[] { y5, y4, y7, y6 };

	protected int[] MIDDLE_X = new int[] { x1, x2, x3, x4, x3, x2 };
	protected int[] MIDDLE_Y = new int[] { y4, y3, y3, y4, y5, y5 };

	protected int[] BOTTOM_X = new int[] { x1, x2, x3, x4 };
	protected int[] BOTTOM_Y = new int[] { y7, y6, y6, y7 };

	public TimeDisplay() {
		this.setDoubleBuffered(true);
		this.calendar = null;
		int width = (6 * cipherWidth) + (9 * segmentWidth) + (2 * border) + 60;
		int height = cipherHeight + border + border;
		Dimension dim = new Dimension(width, height);
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setPreferredSize(dim);
		this.setSize(dim);
		this.setOpaque(false);
		// this.setDoubleBuffered(true);
	}

	protected GregorianCalendar calendar;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.vis.anim.AnimationListener#
	 * updateModelTime(long)
	 */
	public void updateModelTime(long modelTime) {
		if (modelTime > 0) {
			if (this.calendar == null) {
				this.calendar = new GregorianCalendar();
			}
			this.calendar.setTimeInMillis(modelTime);
			if (this.isDisplayable()) {
				repaint();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#started()
	 */
	public void started() {
		// ignore for now
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#stopped()
	 */
	public void stopped() {
		// ignore for now
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int width = this.getWidth();
		int height = this.getHeight();
		Graphics2D g2d = (Graphics2D) g;
		RoundRectangle2D.Float clip = new RoundRectangle2D.Float(0f, 0f,
				(float) width, (float) height, 2 * border, 2 * border);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// draw background
		g2d.setColor(new Color(0, 0, 0));
		g2d.fill(clip);
		g2d.setClip(clip);
		// derive ciphers
		int hourA = 0, hourB = 0, minuteA = 0, minuteB = 0, secondA = 0, secondB = 0;
		String day = "";
		String date = "ready";
		if (this.calendar != null) {
			int hour = this.calendar.get(Calendar.HOUR_OF_DAY);
			hourA = hour / 10;
			hourB = hour % 10;
			int minute = this.calendar.get(Calendar.MINUTE);
			minuteA = minute / 10;
			minuteB = minute % 10;
			int second = this.calendar.get(Calendar.SECOND);
			secondA = second / 10;
			secondB = second % 10;
			day = DAY[this.calendar.get(Calendar.DAY_OF_WEEK) - 1] + " "
					+ this.calendar.get(Calendar.DAY_OF_MONTH);
			int year = this.calendar.get(Calendar.YEAR) % 100;
			String yearStr = Integer.toString(year);
			if (year < 10) {
				yearStr = "0" + yearStr;
			}
			date = MONTH[this.calendar.get(Calendar.MONTH)] + " '" + yearStr;
		}
		// draw hours
		int x = border + 5;
		int dotYa = border + (cipherHeight / 2) - segmentWidth - segmentWidth;
		int dotYb = border + (cipherHeight / 2) + segmentWidth;
		paintCipher(hourA, g2d, x, border);
		x = x + cipherWidth + segmentWidth;
		paintCipher(hourB, g2d, x, border);
		x = x + cipherWidth + segmentWidth;
		// draw hour dots
		g2d.setColor(colorActive);
		g2d.fillRect(x, dotYa, segmentWidth, segmentWidth);
		g2d.fillRect(x, dotYb, segmentWidth, segmentWidth);
		x = x + segmentWidth + segmentWidth;
		// draw minutes
		paintCipher(minuteA, g2d, x, border);
		x = x + cipherWidth + segmentWidth;
		paintCipher(minuteB, g2d, x, border);
		x = x + cipherWidth + segmentWidth;
		// draw minute dots
		g2d.setColor(colorActive);
		g2d.fillRect(x, dotYa, segmentWidth, segmentWidth);
		g2d.fillRect(x, dotYb, segmentWidth, segmentWidth);
		x = x + segmentWidth + segmentWidth;
		// draw seconds
		paintCipher(secondA, g2d, x, border);
		x = x + cipherWidth + segmentWidth;
		paintCipher(secondB, g2d, x, border);
		x = x + cipherWidth + segmentWidth + segmentWidth + segmentWidth;
		// draw date
		g2d.setColor(colorActive);
		g2d.setFont(g2d.getFont().deriveFont(12f));
		g2d.drawString(day, x, border + 11);
		g2d.drawString(date, x, border + 23);
		// draw reflection
		GradientPaint gradient;
		gradient = new GradientPaint(0, 0, new Color(0, 0, 0, 0), 0, height,
				new Color(0, 0, 0, 60), false);
		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, width + 1, height + 1);
		gradient = new GradientPaint(0, 0, new Color(200, 200, 200, 10), 0,
				border + (cipherHeight / 2), new Color(200, 200, 200, 40),
				false);
		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, width + 1, border + (cipherHeight / 2));
		gradient = new GradientPaint(0, cipherHeight / 2 + border, new Color(
				200, 200, 200, 40), 0, cipherHeight / 2 + border + 3,
				new Color(200, 200, 200, 0), false);
		g2d.setPaint(gradient);
		g2d.fillRect(0, cipherHeight / 2 + border, width + 1, 3);
		// paint border
		g2d.setColor(new Color(0, 0, 0, 220));
		g2d.draw(clip);
	}

	protected void paintCipher(int n, Graphics2D g2d, int x, int y) {
		// paint top segment
		if (n != 1 && n != 4) {
			g2d.setColor(colorActive);
		} else {
			g2d.setColor(colorPassive);
		}
		g2d.fillPolygon(translate(TOP_X, x), translate(TOP_Y, y), 4);
		// paint upper left segment
		if (n == 4 || n == 5 || n == 6 || n == 8 || n == 9 || n == 0) {
			g2d.setColor(colorActive);
		} else {
			g2d.setColor(colorPassive);
		}
		g2d.fillPolygon(translate(LEFTTOP_X, x), translate(LEFTTOP_Y, y), 4);
		// paint upper right segment
		if (n != 5 && n != 6) {
			g2d.setColor(colorActive);
		} else {
			g2d.setColor(colorPassive);
		}
		g2d.fillPolygon(translate(RIGHTTOP_X, x), translate(RIGHTTOP_Y, y), 4);
		// paint middle segment
		if (n != 1 && n != 7 && n != 0) {
			g2d.setColor(colorActive);
		} else {
			g2d.setColor(colorPassive);
		}
		g2d.fillPolygon(translate(MIDDLE_X, x), translate(MIDDLE_Y, y), 6);
		// paint lower left segment
		if (n == 2 || n == 6 || n == 8 || n == 0) {
			g2d.setColor(colorActive);
		} else {
			g2d.setColor(colorPassive);
		}
		g2d.fillPolygon(translate(LEFTBOTTOM_X, x), translate(LEFTBOTTOM_Y, y),
				4);
		// paint lower right segment
		if (n != 2) {
			g2d.setColor(colorActive);
		} else {
			g2d.setColor(colorPassive);
		}
		g2d.fillPolygon(translate(RIGHTBOTTOM_X, x),
				translate(RIGHTBOTTOM_Y, y), 4);
		// paint bottom segment
		if (n != 1 && n != 4 && n != 7) {
			g2d.setColor(colorActive);
		} else {
			g2d.setColor(colorPassive);
		}
		g2d.fillPolygon(translate(BOTTOM_X, x), translate(BOTTOM_Y, y), 4);
	}

	protected int[] translate(int[] coords, int offset) {
		int translated[] = new int[coords.length];
		for (int i = 0; i < translated.length; i++) {
			translated[i] = coords[i] + offset;
		}
		return translated;
	}

}
