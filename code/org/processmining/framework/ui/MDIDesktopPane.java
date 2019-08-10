/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.ui;

// This file comes from a tutorial on JavaWorld.com

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.deckfour.slickerbox.util.GraphicsUtilities;
import org.processmining.framework.plugin.Plugin;

/**
 * An extension of WDesktopPane that supports often used MDI functionality. This
 * class also handles setting scroll bars for when windows move too far to the
 * left or bottom, providing the MDIDesktopPane is in a ScrollPane.
 */
public class MDIDesktopPane extends JDesktopPane {

	protected static Color colorShade = new Color(10, 10, 10, 220);
	protected static Color colorTransparent = new Color(0, 0, 0, 0);
	protected static Color colorOverlay = new Color(10, 10, 10, 70);

	protected static Random random = new Random();
	protected static int REG_FRAME_WIDTH = 900;
	protected static int REG_FRAME_HEIGHT = 500;
	protected static int FRAME_OFFSET = 30;

	protected HashMap<JInternalFrame, Plugin> pluginMapping = new HashMap<JInternalFrame, Plugin>();
	protected HashMap<String, Integer> titleCountMap = new HashMap<String, Integer>();
	protected Image bgTileImage;
	protected SoftReference<BufferedImage> buffer = null;

	public MDIDesktopPane() {
		super();
		bgTileImage = null;
		String bgPath = UISettings.getInstance()
				.getPreferredDesktopBackground();
		if (bgPath.equals("NONE") == false) {
			try {
				bgTileImage = GraphicsUtilities.loadCompatibleImage((new File(
						bgPath)).toURL());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// ((new ImageIcon(bgPath))).getImage();
		}
		GraphicsEnvironment graphicsEnv = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice screenDev = graphicsEnv.getDefaultScreenDevice();
		DisplayMode mode = screenDev.getDisplayMode();
		int screenWidth = mode.getWidth();
		int screenHeight = mode.getHeight();
		this.setMinimumSize(new Dimension(screenWidth / 2, screenHeight / 2));
		this.setMaximumSize(new Dimension(screenWidth, screenHeight));
		this.setPreferredSize(new Dimension(screenWidth, screenHeight - 50));
	}

	protected synchronized String createUniqueFrameTitle(String givenTitle) {
		if (titleCountMap.containsKey(givenTitle) == false) {
			titleCountMap.put(givenTitle, 2);
			return givenTitle;
		} else {
			int counter = titleCountMap.get(givenTitle);
			titleCountMap.put(givenTitle, counter + 1);
			return givenTitle + " (" + counter + ")";
		}
	}

	public synchronized void setBackground(String bgFilePath) {
		if (bgFilePath.equals("NONE")) {
			bgTileImage = null;
		} else {
			bgTileImage = (new ImageIcon(bgFilePath)).getImage();
		}
		buffer = null;
		if (this.isDisplayable()) {
			repaint();
		}
	}

	protected void paintComponent(Graphics g) {
		if (bgTileImage == null) {
			super.paintComponent(g);
		} else {
			// paint tiled background
			int height = this.getHeight();
			int width = this.getWidth();
			if (buffer == null || buffer.get() == null
					|| buffer.get().getWidth() != width
					|| buffer.get().getHeight() != height) {
				// create new back buffer
				buffer = new SoftReference<BufferedImage>(GraphicsUtilities
						.createCompatibleImage(width, height));
				Graphics2D g2d = buffer.get().createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				int tileWidth = this.bgTileImage.getWidth(null);
				int tileHeight = this.bgTileImage.getHeight(null);
				// draw tiled background
				for (int y = 0; y < height; y += tileHeight) {
					for (int x = 0; x < width; x += tileWidth) {
						g2d.drawImage(bgTileImage, x, y, null);
					}
				}
				// draw overlay
				g2d.setColor(colorOverlay);
				g2d.fillRect(0, 0, width, height);
				// draw top shade
				GradientPaint gradient = new GradientPaint(0, 0, colorShade, 0,
						height / 5, colorTransparent);
				g2d.setPaint(gradient);
				g2d.fillRect(0, 0, width, height / 5);
				// draw bottom shade
				gradient = new GradientPaint(0, height - (height / 4),
						colorTransparent, 0, height, colorShade);
				g2d.setPaint(gradient);
				g2d.fillRect(0, height - (height / 4), width, height / 4);
				// draw border shades
				int alpha = 180;
				for (int i = 0; i < 15; i++) {
					g2d.setColor(new Color(10, 10, 10, alpha));
					g2d.drawRect(i, i, width - (2 * i), height - (2 * i));
					alpha -= (alpha / 5);
				}
				g2d.dispose();
			}
			Rectangle clip = g.getClipBounds();
			g.drawImage(buffer.get(), clip.x, clip.y, clip.x + clip.width,
					clip.y + clip.height, clip.x, clip.y, clip.x + clip.width,
					clip.y + clip.height, null);
		}
	}

	public synchronized Component add(JInternalFrame frame) {
		return add(frame, null);
	}

	public synchronized Component add(JInternalFrame frame, Plugin plugin) {
		if (!frame.isEnabled()) {
			return null;
		}
		frame.setTitle(createUniqueFrameTitle(frame.getTitle()));
		JInternalFrame[] array = getAllFrames();
		Point p;
		int w = 0;
		int h = 0;
		Component retval = super.add(frame);
		// Calculate the size of the frame if necessary
		if (frame.isResizable()) {
			w = this.getWidth() - (this.getWidth() / 6);
			h = this.getHeight() - (this.getHeight() / 4);
			w = Math.min(w, REG_FRAME_WIDTH);
			h = Math.min(h, REG_FRAME_HEIGHT);
			if (w < frame.getMinimumSize().getWidth()) {
				w = (int) frame.getMinimumSize().getWidth();
			}
			if (h < frame.getMinimumSize().getHeight()) {
				h = (int) frame.getMinimumSize().getHeight();
			}
			frame.setSize(w, h);
		}
		// Calculate the position of the frame if necessary
		p = new Point(FRAME_OFFSET, FRAME_OFFSET);
		if (array.length > 0) {
			p = array[0].getLocation();
			p.x = p.x + FRAME_OFFSET;
			p.y = p.y + FRAME_OFFSET;
		}
		if ((array.length == 0) || (p.x + w > getWidth())
				|| (p.y + h > getHeight())) {
			p = new Point(FRAME_OFFSET, FRAME_OFFSET);
		}
		frame.setLocation(p.x, p.y);
		moveToFront(frame);
		frame.setVisible(true);
		try {
			frame.setSelected(true);
		} catch (PropertyVetoException e) {
			frame.toBack();
		}
		if (plugin != null) {
			pluginMapping.put(frame, plugin);
		}
		return retval;
	}

	public synchronized void remove(Component c) {
		super.remove(c);
		pluginMapping.remove(c);
	}

	protected synchronized void removeQuick(Component c) {
		super.remove(c);
		pluginMapping.remove(c);
	}

	public Plugin getPluginSelectedFrame() {
		JInternalFrame f = getSelectedFrame();
		if ((f == null) || (!pluginMapping.containsKey(f))) {
			return null;
		}
		return (Plugin) pluginMapping.get(f);
	}

	/**
	 * Cascade all internal frames
	 */
	public void cascadeFrames() {
		int x = 0;
		int y = 0;
		JInternalFrame allFrames[] = getAllFrames();
		int frameHeight = (getBounds().height - 5) - allFrames.length
				* FRAME_OFFSET;
		int frameWidth = (getBounds().width - 5) - allFrames.length
				* FRAME_OFFSET;
		for (int i = allFrames.length - 1; i >= 0; i--) {
			allFrames[i].setSize(frameWidth, frameHeight);
			allFrames[i].setLocation(x, y);
			x = x + FRAME_OFFSET;
			y = y + FRAME_OFFSET;
		}
	}

	/**
	 * Tile all internal frames
	 */
	public void tileFrames() {
		java.awt.Component allFrames[] = getAllFrames();
		int frameHeight = getBounds().height / allFrames.length;
		int y = 0;
		for (int i = 0; i < allFrames.length; i++) {
			allFrames[i].setSize(getBounds().width, frameHeight);
			allFrames[i].setLocation(0, y);
			y = y + frameHeight;
		}
	}

	public void tileFramesHorizontally(Collection<JInternalFrame> frames) {
		int frameHeight = getBounds().height / frames.size();
		int y = 0;

		for (JInternalFrame frame : frames) {
			frame.setSize(getBounds().width, frameHeight);
			frame.setLocation(0, y);
			frame.toFront();
			y += frameHeight;
		}
	}

	public void tileFramesVertically(Collection<JInternalFrame> frames) {
		int frameWidth = getBounds().width / frames.size();
		int x = 0;

		for (JInternalFrame frame : frames) {
			frame.setSize(frameWidth, getBounds().height);
			frame.setLocation(x, 0);
			frame.toFront();
			x += frameWidth;
		}
	}

	public void rescueAllFrames() {
		JInternalFrame frames[] = getAllFrames();
		for (JInternalFrame frame : frames) {
			rescueFrame(frame);
		}
	}

	public void rescueFrame(JInternalFrame frame) {
		int width = this.getWidth();
		int height = this.getHeight();
		int frameX = frame.getX();
		int frameY = frame.getY();
		int frameWidth = frame.getWidth();
		int frameHeight = frame.getHeight();
		if (frameWidth > width) {
			frameWidth = width - 10;
		}
		if (frameHeight > height) {
			frameHeight = height - 10;
		}
		frame.setSize(frameWidth, frameHeight);
		if (frameX < 0) {
			frameX = 5;
		} else {
			int rightBorder = frameX + frameWidth;
			if (rightBorder > width) {
				frameX = frameX - (rightBorder - width);
			}
		}
		if (frameY < 0) {
			frameY = 5;
		} else {
			int lowerBorder = frameY + frameHeight;
			if (lowerBorder > height) {
				frameY = frameY - (lowerBorder - height);
			}
		}
		frame.setLocation(frameX, frameY);
	}

	/**
	 * Sets all component size properties ( maximum, minimum, preferred) to the
	 * given dimension.
	 */
	public void setAllSize(Dimension d) {
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
	}

	/**
	 * Sets all component size properties ( maximum, minimum, preferred) to the
	 * given width and height.
	 */
	public void setAllSize(int width, int height) {
		setAllSize(new Dimension(width, height));
	}

}
