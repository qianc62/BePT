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
package org.processmining.mining.fuzzymining.vis.paint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.processmining.mining.fuzzymining.vis.anim.Animation;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class UnbufferedAnimationCanvas extends AnimationCanvas {

	private long lastPaintTime = System.currentTimeMillis();
	private NumberFormat format = new DecimalFormat("#0.00");

	/**
	 * @param animation
	 * @param settings
	 */
	public UnbufferedAnimationCanvas(Animation animation, ColorSettings settings) {
		super(animation, settings);
		this.backBuffer = null;
		this.buffer = null;
		this.setOpaque(true);
		this.setDoubleBuffered(true);
	}

	public void setZoom(float zoom) {
		this.zoom = zoom;
		leftBorder = boxHeight * animation.getParser().getBoundsHeight()
				* SCALE_FACTOR * zoom;
		topBorder = leftBorder / 2;
		bottomBorder = topBorder;
		rightBorder = leftBorder * 3;
		this.width = animation.getParser().getBoundsWidth() * SCALE_FACTOR
				* zoom;
		this.height = animation.getParser().getBoundsHeight() * SCALE_FACTOR
				* zoom;
		int imgWidth = (int) (width + leftBorder + rightBorder);
		int imgHeight = (int) (height + topBorder + bottomBorder);
		Dimension size = new Dimension(imgWidth, imgHeight);
		this.setMinimumSize(size);
		this.setPreferredSize(size);
		revalidate();
		if (this.isDisplayable()) {
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.anim.model.AnimationListener#
	 * updateModelTime(long)
	 */
	public void updateModelTime(long modelTime) {
		this.animationTime = modelTime;
		repaint();
	}

	protected long getRenderTime() {
		long current = System.currentTimeMillis();
		long renderTime = current - this.lastPaintTime;
		lastPaintTime = current;
		return renderTime;
	}

	protected double getFramesPerSecond() {
		return 1000.0 / (double) getRenderTime();
	}

	protected void paintComponent(Graphics g) {
		long modelTime = this.animationTime;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int x = 0;
		int y = 0;
		int actualWidth = this.getWidth();
		int actualHeight = this.getHeight();
		int canvasWidth = (int) (this.width + leftBorder + rightBorder);
		int canvasHeight = (int) (this.height + topBorder + bottomBorder);
		if (actualWidth > canvasWidth) {
			x = (int) (actualWidth - canvasWidth) / 2;
		}
		if (actualHeight > canvasHeight) {
			y = (int) (actualHeight - canvasHeight) / 2;
		}
		g2d.setColor(colorSettings.getCanvasBackground());
		g.fillRect(0, 0, actualWidth + 1, actualHeight + 1);
		// translate origin
		g2d.translate(leftBorder + x, topBorder + y);
		// paint to back buffer
		// paint layer 0: arc base trajectory
		Graphics2D g2dCopy = (Graphics2D) g2d.create();
		this.edgeBackgroundPainter.paint(modelTime, g2dCopy);
		// paint layer 1: token trace trajectories
		this.edgeTrajectoryPainter.paint(modelTime, g2dCopy);
		// paint layer 2: task bases
		this.taskAtomicPainter.paint(modelTime, g2dCopy);
		this.taskClusterPainter.paint(modelTime, g2dCopy);
		// test: token rings
		this.tokenRingsPainter.paint(modelTime, g2dCopy);
		// paint layer 3: tokens
		this.tokenPainter.paint(modelTime, g2dCopy);
		// paint layer 4: token labels
		this.tokenLabelPainter.paint(modelTime, g2dCopy);
		// paint layer 5: task badges
		this.taskBadgesPainter.paint(modelTime, g2dCopy);
	}

}
