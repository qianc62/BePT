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
package org.processmining.framework.ui.slicker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JComponent;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;

/**
 * @author christian
 * 
 */
public class ProcessInstanceView extends JComponent implements MouseListener,
		MouseMotionListener {

	protected static Color colorAttenuationDark = new Color(0, 0, 0, 160);
	protected static Color colorAttenuationBright = new Color(0, 0, 0, 80);
	protected static Color colorBgInstanceflag = new Color(70, 70, 70, 210);
	protected static Color colorBgEventFlag = new Color(45, 45, 45, 200);

	protected static DecimalFormat format = new DecimalFormat("##0.00%");
	protected static DateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss.SSS");

	protected static int trackPadding = 80;
	protected static int trackY = 40;
	protected static int trackHeight = 35;
	protected static int elementWidth = 4;
	protected static int elementTriOffset = 6;

	protected LogSummary summary;
	protected int maxOccurrenceCount;
	protected ProcessInstance instance;
	protected boolean mouseOver = false;
	protected int mouseX;
	protected int mouseY;

	public ProcessInstanceView(ProcessInstance instance, LogSummary summary) {
		this.instance = instance;
		this.summary = summary;
		maxOccurrenceCount = summary.getLogEvents().getMaxOccurrenceCount();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		int width = (instance.getAuditTrailEntryList().size() * elementWidth)
				+ trackPadding + 300;
		this.setMinimumSize(new Dimension(width, 80));
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
		this.setPreferredSize(new Dimension(width, 80));
		this.setDoubleBuffered(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		Rectangle clip = getVisibleRect();// g.getClipBounds();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// draw background
		g2d.setColor(new Color(30, 30, 30));
		g2d.fillRect(clip.x, clip.y, clip.width, clip.height);
		// determine active event
		int activeEvent = -1;
		if (mouseOver == true) {
			activeEvent = mapEventIndex(mouseX, mouseY);
		}
		// draw events
		int clipRightX = clip.x + clip.width;
		int trackRightX = trackPadding
				+ (instance.getAuditTrailEntryList().size() * elementWidth);
		int startX = clip.x - (clip.x % elementWidth); // shift to left if
		// necessary
		if (startX < trackPadding) {
			startX = trackPadding;
		}
		int eventIndex = (startX - trackPadding) / elementWidth;
		for (int x = startX; (x < clipRightX && x < trackRightX); x += elementWidth) {
			this.drawEvent(g2d, eventIndex, (eventIndex == activeEvent), x,
					trackY, elementWidth, trackHeight);
			eventIndex++;
		}
		if (clip.x <= trackRightX) {
			// draw instance flag
			this.drawInstanceFlag(g2d, clip.x, 25, 35);
			// draw event flag
			if (activeEvent >= 0) {
				int eventX = trackPadding + (activeEvent * elementWidth);
				try {
					this.drawEventFlag(g2d, activeEvent, eventX, 5, 30);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	protected int mapEventIndex(int x, int y) {
		if (y >= trackY && y <= (trackY + trackHeight)) {
			// y-coordinate matches, remap x to index
			x -= trackPadding;
			x /= elementWidth;
			if (x >= 0 && x < instance.getAuditTrailEntryList().size()) {
				return x;
			} else {
				return -1;
			}
		} else {
			return -1;
		}

	}

	protected void drawInstanceFlag(Graphics2D g2d, int x, int y, int height) {
		String name = instance.getName();
		String size = instance.getAuditTrailEntryList().size() + " events";
		// calculate width
		g2d.setFont(g2d.getFont().deriveFont(11f));
		FontMetrics fm = g2d.getFontMetrics();
		int nameWidth = fm.stringWidth(name);
		int sizeWidth = fm.stringWidth(size);
		int width = (nameWidth > sizeWidth) ? nameWidth + 15 : sizeWidth + 15;
		width = Math.max(width, trackPadding - 10);
		// draw flag shadow
		int shadowOffset = 4;
		int[] xSCoords = new int[] { x,
				x + width - elementTriOffset + shadowOffset,
				x + width + shadowOffset,
				x + width - elementTriOffset + shadowOffset, x };
		int[] ySCoords = new int[] { y + shadowOffset, y + shadowOffset,
				y + (height / 2) + shadowOffset, y + height + shadowOffset,
				y + height + shadowOffset };
		g2d.setColor(new Color(0, 0, 0, 100));
		g2d.fillPolygon(xSCoords, ySCoords, 5);
		// draw flag background
		g2d.setColor(colorBgInstanceflag);
		int[] xCoords = new int[] { x, x + width - elementTriOffset, x + width,
				x + width - elementTriOffset, x };
		int[] yCoords = new int[] { y, y, y + (height / 2), y + height,
				y + height };
		g2d.fillPolygon(xCoords, yCoords, 5);
		// draw string
		int fontHeight = fm.getHeight();
		int fontOffset = (height - fontHeight - fontHeight) / 3;
		g2d.setColor(new Color(220, 220, 220));
		g2d.drawString(name, x + 5, y + fontOffset + fontHeight - 1);
		g2d.setColor(new Color(200, 200, 200));
		g2d.drawString(size, x + 5, y + height - fontOffset - 3);
	}

	protected void drawEventFlag(Graphics2D g2d, int index, int x, int y,
			int height) throws IndexOutOfBoundsException, IOException {
		AuditTrailEntry ate = instance.getAuditTrailEntryList().get(index);
		int occurrence = summary.getLogEvents().findLogEvent(ate.getElement(),
				ate.getType()).getOccurrenceCount();
		double frequency = (double) occurrence / (double) maxOccurrenceCount;
		String name = index + ": " + ate.getElement() + " (" + ate.getType()
				+ ")";
		String originator = ate.getOriginator() + "; freq: "
				+ format.format(frequency);
		Date ts = ate.getTimestamp();
		String timestamp;
		if (ts != null) {
			timestamp = dateFormat.format(ate.getTimestamp());
		} else {
			timestamp = "<no timestamp>";
		}
		// calculate width
		g2d.setFont(g2d.getFont().deriveFont(9f));
		FontMetrics fm = g2d.getFontMetrics();
		int widthA = fm.stringWidth(name) + 10;
		int widthB = fm.stringWidth(originator) + 10;
		int widthC = fm.stringWidth(timestamp) + 10;
		int width = (widthA > widthB) ? widthA : widthB;
		if (widthC > width) {
			width = widthC;
		}
		// draw background
		colorBgEventFlag = new Color(30, 30, 30, 200);
		g2d.setColor(colorBgEventFlag);
		g2d.fillRect(x, y, width, height);
		// set color
		Color color = encodeColor(frequency);
		g2d.setColor(color);
		// draw anchor line
		g2d.drawLine(x, y, x, y + height);
		// draw strings
		int fontHeight = fm.getHeight();
		int fontOffset = (height - fontHeight - fontHeight - fontHeight) / 4;
		g2d.drawString(name, x + 5, y + fontOffset + fontHeight - 2);
		g2d.drawString(originator, x + 5, y + fontOffset + fontHeight
				+ fontOffset + fontHeight - 3);
		g2d.drawString(timestamp, x + 5, y + height - fontOffset - 2);
	}

	protected void drawEvent(Graphics2D g2d, int index, boolean active, int x,
			int y, int width, int height) {
		// set correct color for event
		AuditTrailEntry ate;
		try {
			ate = instance.getAuditTrailEntryList().get(index);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int occurrence = summary.getLogEvents().findLogEvent(ate.getElement(),
				ate.getType()).getOccurrenceCount();
		double frequency = (double) occurrence / (double) maxOccurrenceCount;
		Color color = encodeColor(frequency);
		if (active == false) {
			color = attenuateColor(color);
		}
		g2d.setColor(color);
		// draw triangularish shape
		int midPointBX = x + elementTriOffset;
		int midPointAX = x + width + elementTriOffset;
		int midPointY = y + (height / 2);
		int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x,
				midPointBX };
		int[] yCoords = new int[] { y, y, midPointY, y + height, y + height,
				midPointY };
		if (active == true) {
			for (int i = 0; i < xCoords.length; i++) {
				xCoords[i] -= 1;
				yCoords[i] -= 3;
			}
		}
		g2d.fillPolygon(xCoords, yCoords, 6);
		// draw attenuations for 3d-effect
		if (active == true) {
			g2d.setColor(colorAttenuationDark);
			g2d.drawPolyline(new int[] { x - 3, midPointBX - 3, x - 3 },
					new int[] { y - 3, midPointY - 3, y + height - 3 }, 3);
			g2d.setColor(colorAttenuationBright);
			g2d.drawPolyline(new int[] { x - 2, midPointBX - 2, x - 2 },
					new int[] { y - 3, midPointY - 3, y + height - 3 }, 3);
		} else {
			g2d.setColor(colorAttenuationDark);
			g2d.drawPolyline(new int[] { x, midPointBX, x }, new int[] { y,
					midPointY, y + height }, 3);
			g2d.setColor(colorAttenuationBright);
			g2d.drawPolyline(new int[] { x + 1, midPointBX + 1, x + 1 },
					new int[] { y, midPointY, y + height }, 3);
		}
	}

	protected Color encodeColor(double value) {
		if (value < 0.0) {
			return Color.BLACK;
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

	protected Color attenuateColor(Color color) {
		int red = (int) ((double) color.getRed() * 0.7);
		int green = (int) ((double) color.getGreen() * 0.7);
		int blue = (int) ((double) color.getBlue() * 0.7);
		return new Color(red, green, blue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent evt) {
		mouseX = evt.getX();
		mouseY = evt.getY();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent evt) {
		mouseX = evt.getX();
		mouseY = evt.getY();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
		mouseOver = true;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
		mouseOver = false;
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
