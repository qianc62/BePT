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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

import org.processmining.mining.fuzzymining.vis.anim.Animation;
import org.processmining.mining.fuzzymining.vis.anim.AnimationListener;
import org.processmining.mining.fuzzymining.vis.anim.AnimationTimer;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AnimationSeekPane extends JComponent implements AnimationListener,
		MouseMotionListener, MouseListener {

	protected Color waveColor = new Color(200, 0, 0);

	protected boolean timerRunningBeforeSeek = false;
	protected boolean isSeeking = false;
	protected int seekOffset = 0;

	protected AnimationTimer timer;
	protected Animation animation;
	protected long start;
	protected long end;
	protected long duration;
	protected float position;

	protected float[] activity;
	protected int[] waveX;
	protected int[] waveY;

	protected int height = 40;
	protected int hBorder = 7;
	protected int topBorder = 10;
	protected int bottomBorder = 4;

	public AnimationSeekPane(Animation animation, AnimationTimer timer) {
		this.setDoubleBuffered(true);
		this.timer = timer;
		this.animation = animation;
		this.start = animation.getStart();
		this.end = animation.getEnd();
		this.duration = end - start;
		this.position = 0.0f;
		this.setMinimumSize(new Dimension(200, height));
		this.setMaximumSize(new Dimension(2000, height));
		this.setPreferredSize(new Dimension(700, height));
		measureActivity();
		timer.addListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.vis.anim.AnimationListener#
	 * updateModelTime(long)
	 */
	public void updateModelTime(long modelTime) {
		this.position = (float) (modelTime - this.start) / (float) duration;
		repaint();
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
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int width = getWidth();
		int height = getHeight();
		RoundRectangle2D.Float clip = new RoundRectangle2D.Float(0f, 0f,
				(float) width, (float) height, 10f, 10f);
		g2d.setColor(new Color(0, 0, 0));
		g2d.fill(clip);
		g2d.setClip(clip);
		// paint wave
		int wavePoints = activity.length;
		int waveHeight = height - topBorder - bottomBorder;
		float maxOffset = (float) waveHeight / 2;
		int waveCenter = topBorder + (waveHeight / 2);
		int waveWidth = width - hBorder - hBorder;
		waveX[0] = hBorder;
		waveY[0] = waveCenter;
		waveX[wavePoints + 1] = width - hBorder;
		waveY[wavePoints + 1] = waveCenter;
		float step = (float) waveWidth / (float) wavePoints;
		float startX = hBorder + (step / 2f);
		int x, offset;
		int index = 1;
		int counterIndex = waveX.length - 1;
		for (int i = 0; i < wavePoints; i++) {
			x = (int) (startX + (step * i));
			offset = (int) (maxOffset * activity[i]);
			waveX[index] = x;
			waveY[index] = waveCenter - offset - 1;
			waveX[counterIndex] = x;
			waveY[counterIndex] = waveCenter + offset;
			index++;
			counterIndex--;
		}
		g2d.setColor(waveColor);
		g2d.fillPolygon(waveX, waveY, waveX.length);
		// draw reflection
		GradientPaint gradient;
		gradient = new GradientPaint(0, 0, new Color(0, 0, 0, 0), 0, height,
				new Color(0, 0, 0, 60), false);
		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, width + 1, height + 1);
		gradient = new GradientPaint(0, 0, new Color(200, 200, 200, 10), 0,
				waveCenter, new Color(200, 200, 200, 50), false);
		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, width + 1, waveCenter);
		gradient = new GradientPaint(0, waveCenter,
				new Color(200, 200, 200, 50), 0, waveCenter + 3, new Color(200,
						200, 200, 0), false);
		g2d.setPaint(gradient);
		g2d.fillRect(0, waveCenter, width + 1, 3);
		// draw handle / progress indicator
		int handleCenter = hBorder + (int) (this.position * (float) waveWidth);
		int[] handleX = new int[] { handleCenter + 5, handleCenter + 5,
				handleCenter, handleCenter - 5, handleCenter - 5 };
		int[] handleY = new int[] { 0, waveCenter + 5, waveCenter + 10,
				waveCenter + 5, 0 };
		gradient = new GradientPaint(0, 0, new Color(200, 200, 200, 0), 0, 5,
				new Color(200, 200, 200, 110), false);
		g2d.setPaint(gradient);
		g2d.fillPolygon(handleX, handleY, 5);
		gradient = new GradientPaint(0, 0, new Color(200, 200, 200, 0), 0, 5,
				new Color(220, 220, 220, 120), false);
		g2d.setPaint(gradient);
		g2d.drawPolyline(handleX, handleY, 5);
		g2d.setColor(new Color(20, 0, 0, 180));
		g2d.drawLine(handleCenter, 0, handleCenter, waveCenter + 2);
		// paint border
		g2d.setColor(new Color(0, 0, 0, 220));
		g2d.draw(clip);
	}

	protected void measureActivity() {
		activity = new float[200];
		waveX = new int[(activity.length * 2) + 2];
		waveY = new int[waveX.length];
		long step = this.duration / activity.length;
		long segStart = start;
		float maxActivity = 0.0f;
		for (int i = 0; i < activity.length; i++) {
			activity[i] = this.animation.getActivityBetween(segStart, segStart
					+ step);
			segStart += step;
			if (activity[i] > maxActivity) {
				maxActivity = activity[i];
			}
		}
		// normalize
		for (int i = 0; i < activity.length; i++) {
			activity[i] /= maxActivity;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent e) {
		if (this.isSeeking) {
			float waveWidth = this.getWidth() - hBorder - hBorder;
			int mouseX = e.getX() + this.seekOffset;
			float percentage = (float) (mouseX - hBorder) / waveWidth;
			if (percentage < 0f) {
				percentage = 0f;
			} else if (percentage > 1f) {
				percentage = 1f;
			}
			this.position = percentage;
			this.timer.setRelativePosition(percentage);
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		if (this.isSeeking) {
			float waveWidth = this.getWidth() - hBorder - hBorder;
			int mouseX = e.getX() + this.seekOffset;
			float percentage = (float) (mouseX - hBorder) / waveWidth;
			if (percentage < 0f) {
				percentage = 0f;
			} else if (percentage > 1f) {
				percentage = 1f;
			}
			this.position = percentage;
			this.timer.setRelativePosition(percentage);
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		int mouseX = e.getX();
		int handleCenter = hBorder
				+ (int) (this.position * (float) (this.getWidth() - hBorder - hBorder));
		if (mouseX >= (handleCenter - 5) && mouseX <= (handleCenter + 5)) {
			this.isSeeking = true;
			this.seekOffset = handleCenter - mouseX;
			this.timerRunningBeforeSeek = this.timer.isRunning();
			this.timer.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		if (this.isSeeking) {
			float waveWidth = this.getWidth() - hBorder - hBorder;
			int mouseX = e.getX() + this.seekOffset;
			float percentage = (float) (mouseX - hBorder) / waveWidth;
			if (percentage < 0f) {
				percentage = 0f;
			} else if (percentage > 1f) {
				percentage = 1f;
			}
			this.timer.setRelativePosition(percentage);
			this.isSeeking = false;
			if (this.timerRunningBeforeSeek) {
				this.timer.start();
			}
		}
	}

}
