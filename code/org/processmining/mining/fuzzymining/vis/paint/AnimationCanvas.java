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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.processmining.mining.fuzzymining.vis.anim.Animation;
import org.processmining.mining.fuzzymining.vis.anim.AnimationListener;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AnimationCanvas extends JComponent implements AnimationListener {

	protected static GraphicsConfiguration gc;
	protected static float SCALE_FACTOR = 97f;

	protected float zoom = 0.5f;
	protected float width;
	protected float height;

	protected float leftBorder;
	protected float rightBorder;
	protected float topBorder;
	protected float bottomBorder;

	protected float boxHeight;
	protected float minLineWidth;
	protected float maxLineWidth;
	protected float borderWidth;
	protected float fontSize;

	protected BufferedImage buffer;
	protected BufferedImage backBuffer;

	protected Animation animation;
	protected long animationTime;

	protected ColorSettings colorSettings;

	protected EdgeBackgroundPainter edgeBackgroundPainter;
	protected EdgeTrajectoryPainter edgeTrajectoryPainter;
	protected TaskAtomicPainter taskAtomicPainter;
	protected TaskClusterPainter taskClusterPainter;
	protected TokenPainter tokenPainter;
	protected TokenLabelPainter tokenLabelPainter;
	protected TaskBadgesPainter taskBadgesPainter;
	protected TokenRingsPainter tokenRingsPainter;

	public AnimationCanvas(Animation animation, ColorSettings settings) {
		this.animation = animation;
		this.colorSettings = settings;
		this.animationTime = animation.getStart();
		this.boxHeight = animation.getPrimitiveTaskAnimations().get(0)
				.getHeight();
		this.maxLineWidth = this.boxHeight * 0.56f;
		this.minLineWidth = this.boxHeight * 0.05f;
		this.borderWidth = this.boxHeight * 0.05f;
		this.fontSize = this.boxHeight * 0.23f;
		this.edgeBackgroundPainter = new EdgeBackgroundPainter(this);
		this.edgeTrajectoryPainter = new EdgeTrajectoryPainter(this);
		this.taskAtomicPainter = new TaskAtomicPainter(this);
		this.taskClusterPainter = new TaskClusterPainter(this);
		this.tokenPainter = new TokenPainter(this);
		this.tokenLabelPainter = new TokenLabelPainter(this);
		this.taskBadgesPainter = new TaskBadgesPainter(this);
		this.tokenRingsPainter = new TokenRingsPainter(this);
		setZoom(0.5f);
		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (colorSettings.equals(ColorSettings.SETTINGS_NIGHT)) {
					colorSettings = ColorSettings.SETTINGS_DAY;
				} else {
					colorSettings = ColorSettings.SETTINGS_NIGHT;
				}
				paintBuffered();
			}

			public void mouseEntered(MouseEvent e) { /* ignore */
			}

			public void mouseExited(MouseEvent e) { /* ignore */
			}

			public void mousePressed(MouseEvent e) { /* ignore */
			}

			public void mouseReleased(MouseEvent e) { /* ignore */
			}
		});
	}

	public Animation getAnimation() {
		return this.animation;
	}

	public ColorSettings getColorSettings() {
		return this.colorSettings;
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
		this.buffer = createCompatibleImage(imgWidth, imgHeight); // new
		// BufferedImage((int)this.width,
		// (int)this.height,
		// BufferedImage.TYPE_INT_ARGB);
		this.backBuffer = createCompatibleImage(imgWidth, imgHeight); // new
		// BufferedImage((int)this.width,
		// (int)this.height,
		// BufferedImage.TYPE_INT_ARGB);
		revalidate();
		paintBuffered();
	}

	public float getBoxHeight() {
		return this.boxHeight;
	}

	public float getLineWidth(float percentage) {
		float lineWidth = this.minLineWidth + (this.maxLineWidth * percentage);
		lineWidth *= this.height;
		if (lineWidth < 1.0f) {
			lineWidth = 1.0f;
		}
		return lineWidth;
	}

	public float getBorderWidth() {
		float bWidth = this.borderWidth * this.height;
		if (bWidth < 1.0f) {
			bWidth = 1.0f;
		}
		return bWidth;
	}

	public float getFontSize() {
		return this.fontSize * this.height;
	}

	public float translateX(float modelX) {
		return modelX * this.width;
	}

	public float translateY(float modelY) {
		return modelY * this.height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.anim.model.AnimationListener#
	 * updateModelTime(long)
	 */
	public void updateModelTime(long modelTime) {
		this.animationTime = modelTime;
		paintBuffered();
	}

	protected void paintComponent(Graphics g) {
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
		g.setColor(colorSettings.getCanvasBackground());
		g.fillRect(0, 0, actualWidth + 1, actualHeight + 1);
		g.drawImage(buffer, x, y, null);
	}

	protected void paintBuffered() {
		long modelTime = this.animationTime;
		// start by clearing back buffer
		Graphics2D g2d = backBuffer.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR,
		// 0.0f));
		g2d.setColor(colorSettings.getCanvasBackground());
		g2d.fill(new Rectangle2D.Float(0, 0, backBuffer.getWidth() + 1,
				backBuffer.getHeight() + 1));
		// translate origin
		g2d.translate(leftBorder, topBorder);
		// paint to back buffer
		// paint layer 0: arc base trajectory
		Graphics2D g2dCopy = (Graphics2D) g2d.create();
		this.edgeBackgroundPainter.paint(modelTime, g2dCopy);
		// paint layer 1: token trace trajectories
		g2dCopy.dispose();
		g2dCopy = (Graphics2D) g2d.create();
		this.edgeTrajectoryPainter.paint(modelTime, g2dCopy);
		// paint layer 2: task bases
		g2dCopy.dispose();
		g2dCopy = (Graphics2D) g2d.create();
		this.taskAtomicPainter.paint(modelTime, g2dCopy);
		g2dCopy.dispose();
		g2dCopy = (Graphics2D) g2d.create();
		this.taskClusterPainter.paint(modelTime, g2dCopy);
		// test: token rings
		g2dCopy.dispose();
		g2dCopy = (Graphics2D) g2d.create();
		this.tokenRingsPainter.paint(modelTime, g2dCopy);
		// paint layer 3: tokens
		g2dCopy.dispose();
		g2dCopy = (Graphics2D) g2d.create();
		this.tokenPainter.paint(modelTime, g2dCopy);
		// paint layer 4: token labels
		g2dCopy.dispose();
		g2dCopy = (Graphics2D) g2d.create();
		this.tokenLabelPainter.paint(modelTime, g2dCopy);
		// paint layer 5: task badges
		g2dCopy.dispose();
		g2dCopy = (Graphics2D) g2d.create();
		this.taskBadgesPainter.paint(modelTime, g2dCopy);
		// swap buffers
		BufferedImage tmp = this.backBuffer;
		this.backBuffer = this.buffer;
		this.buffer = tmp;
		// trigger visual update
		if (this.isDisplayable()) {
			repaint();
		}
		g2d.dispose();
	}

	protected BufferedImage createCompatibleImage(int width, int height) {
		if (gc == null) {
			gc = getGraphicsConfiguration();
			if (gc == null) {
				gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
						.getDefaultScreenDevice().getDefaultConfiguration();
			}
		}
		return gc.createCompatibleImage(width, height);
		// return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#started()
	 */
	public void started() {
		// ignore for now, we just paint the current state
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#stopped()
	 */
	public void stopped() {
		// ignore for now, we just paint the current state
	}

}
