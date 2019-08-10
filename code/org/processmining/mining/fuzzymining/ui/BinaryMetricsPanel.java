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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

import javax.swing.JComponent;

import org.processmining.framework.log.LogEvents;
import org.processmining.framework.util.ColorRepository;
import org.processmining.mining.fuzzymining.metrics.binary.BinaryMetric;

/**
 * @author christian
 * 
 */
public class BinaryMetricsPanel extends JComponent implements MouseListener,
		MouseMotionListener {

	protected static NumberFormat numberFormat = NumberFormat.getInstance();
	{
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
	}

	protected BinaryMetric metric;
	protected int mouseX;
	protected int mouseY;
	protected LogEvents events;

	protected BufferedImage matrixBuffer;
	protected int oldBlockSize;

	public BinaryMetricsPanel(BinaryMetric aMetric, LogEvents events) {
		this.events = events;
		metric = aMetric;
		this.setMinimumSize(new Dimension(metric.size(), metric.size()));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.matrixBuffer = null;
		this.oldBlockSize = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int width = getWidth();
		int height = getHeight();
		// draw background
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		int size = metric.size();
		int blockSize = calculateBlockSize(size, width, height);
		if (oldBlockSize != blockSize || matrixBuffer == null) {
			// create new matrix buffer
			matrixBuffer = new BufferedImage(size * blockSize,
					size * blockSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D gBuf = matrixBuffer.createGraphics();
			int x = 0;
			int y = 0;
			for (int a = 0; a < size; a++) {
				y = 0;
				for (int b = 0; b < size; b++) {
					gBuf.setColor(ColorRepository
							.getGradualColorBlackZero(metric.getMeasure(a, b)));
					if (blockSize > 1) {
						gBuf.fillRect(x, y, blockSize, blockSize);
					} else {
						gBuf.drawLine(x, y, x, y);
					}
					y += blockSize;
				}
				x += blockSize;
			}
			oldBlockSize = blockSize;
			gBuf.dispose();
		}
		g2d.drawImage(matrixBuffer, 0, 0, this);
		if (mouseX >= 0 && mouseX < (size * blockSize) && mouseY >= 0
				&& mouseY < (size * blockSize)) {
			// draw info
			int eventX = mouseX / blockSize;
			int eventY = mouseY / blockSize;
			paintInfo(g2d, eventX, eventY, mouseX, mouseY);
		}
	}

	protected int calculateBlockSize(int size, int width, int height) {
		int maxRes = Math.min(width, height);
		if (maxRes <= (size * 2)) {
			return 1;
		} else {
			int bSize = maxRes / size;
			if ((bSize * size) > maxRes) {
				bSize--;
			}
			return bSize;
		}
	}

	protected void paintInfo(Graphics2D g2d, int eventX, int eventY, int x,
			int y) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		double value = metric.getMeasure(eventX, eventY);
		String evtX = events.get(eventX).getModelElementName() + " ("
				+ events.get(eventX).getEventType() + ")";
		String evtY = events.get(eventY).getModelElementName() + " ("
				+ events.get(eventY).getEventType() + ")";
		String valueStr = "value: " + numberFormat.format(value);
		FontMetrics fontMetrics = this.getFontMetrics(g2d.getFont().deriveFont(
				11.0f));
		int widthA = fontMetrics.stringWidth(evtX);
		int widthB = fontMetrics.stringWidth(evtY);
		int widthC = fontMetrics.stringWidth(valueStr);
		int width = Math.max(widthA, widthB);
		width = Math.max(width, widthC);
		Color bgColor = new Color(0, 0, 0, 0.75f);
		Color fgColor = new Color(220, 220, 220, 220);
		g2d.setColor(bgColor);
		g2d.fillRoundRect(x + 18, y - 12, width + 12, 51, 5, 5);
		g2d.drawOval(x - 3, y - 3, 8, 8);
		g2d.drawLine(x + 3, y + 1, x + 18, y + 1);
		g2d.setFont(g2d.getFont().deriveFont(11.0f));
		g2d.setColor(fgColor);
		g2d.drawOval(x - 4, y - 4, 8, 8);
		g2d.drawLine(x + 4, y, x + 18, y);
		g2d.drawString(evtX, x + 24, y + 2);
		g2d.drawString(evtY, x + 24, y + 17);
		g2d.drawString(valueStr, x + 24, y + 32);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX() - 10;
		mouseY = e.getY() - 10;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX() - 10;
		mouseY = e.getY() - 10;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		mouseX = e.getX() - 10;
		mouseY = e.getY() - 10;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		mouseX = e.getX() - 10;
		mouseY = e.getY() - 10;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
		mouseX = -1;
		mouseY = -1;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent arg0) {
		// ignore
	}

}
