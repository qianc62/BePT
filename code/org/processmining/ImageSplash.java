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

package org.processmining;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JWindow;

import org.processmining.framework.ui.About;
import org.processmining.framework.ui.MainUI;

public class ImageSplash extends JWindow implements ProMSplash {

	protected Image imgBg;
	protected Image imgLogo;
	protected static int MAX_PROGRESS = 100;
	protected static int textHeight = 22;
	protected static Color COLOR_PROGRESS1 = new Color(255, 255, 255, 180);
	protected static Color COLOR_PROGRESS2 = new Color(255, 255, 255, 60);
	protected static Color COLOR_TEXTBG = new Color(20, 20, 20, 60);
	protected static Color COLOR_TEXT = new Color(220, 220, 220, 140);

	protected String message;
	protected int progress;
	protected int logoX;
	protected int logoY;
	protected int width;
	protected int height;
	protected int progressX;
	protected int progressY;
	protected int progressWidth;
	protected int progressHeight;
	protected int textBgY;
	protected ArrayList log;
	protected BufferedImage buffer;
	protected GradientPaint gradient;

	ImageSplash() throws HeadlessException {
		super();
		this.setBackground(new Color(80, 80, 80));
		// Initialize
		log = new ArrayList();
		progress = 0;
		message = "Initializing...";
		progress = 0;
		String imgPath = System.getProperty("user.dir") + File.separator
				+ "images" + File.separator + "splash" + File.separator;
		imgBg = Toolkit.getDefaultToolkit().createImage(
				imgPath + "splash_background.png");
		imgLogo = Toolkit.getDefaultToolkit().createImage(
				imgPath + "splash_logo.png");
		// Load the image
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(imgBg, 0);
		mt.addImage(imgLogo, 1);
		try {
			mt.waitForID(0);
			mt.waitForID(1);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		// Center the window on the screen
		width = imgBg.getWidth(this);
		height = imgBg.getHeight(this);
		this.setSize(width, height);
		GraphicsEnvironment graphicsEnv = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice screenDev = graphicsEnv.getDefaultScreenDevice();
		DisplayMode mode = screenDev.getDisplayMode();
		this.setLocation((mode.getWidth() - width) / 2,
				(mode.getHeight() - height) / 2);
		// do some pre-calculations for drawing later on
		int logoWidth = imgLogo.getWidth(this);
		int logoHeight = imgLogo.getHeight(this);
		logoX = width - logoWidth - 5;
		logoY = height - logoHeight - textHeight - 5;
		progressX = 15;
		progressY = logoY + 20;
		progressWidth = width - logoWidth - 40;
		progressHeight = logoHeight - 40;
		textBgY = height - textHeight;
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		gradient = new GradientPaint(0, progressY, ImageSplash.COLOR_PROGRESS1,
				0, progressY + progressHeight, ImageSplash.COLOR_PROGRESS2);
		// make visible
		this.setAlwaysOnTop(true);
		this.setVisible(true);
	}

	// heavy lifting
	public void paint(Graphics g) {
		Graphics2D g2d = buffer.createGraphics();
		// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		// draw background
		g2d.setColor(this.getBackground());
		g2d.fillRect(0, 0, this.getWidth() + 1, this.getHeight() + 1);
		g2d.drawImage(imgBg, 0, 0, this);
		// draw logo
		g2d.drawImage(imgLogo, logoX, logoY, this);
		// draw progress
		g2d.setPaint(gradient);
		// g2d.setColor(ImageSplash.COLOR_PROGRESS);
		g2d.drawRect(progressX, progressY, progressWidth, progressHeight);
		int pBarWidth = ((progressWidth - 3) * progress)
				/ ImageSplash.MAX_PROGRESS;
		g2d.fillRect(progressX + 2, progressY + 2, pBarWidth,
				progressHeight - 3);
		// draw text background
		g2d.setColor(ImageSplash.COLOR_TEXTBG);
		g2d.fillRect(0, textBgY, width, ImageSplash.textHeight);
		// draw text
		g2d.setFont(g2d.getFont().deriveFont(10.0f));
		g2d.setColor(ImageSplash.COLOR_TEXT);
		g2d.drawString(message, 18, height - 8);
		g2d.dispose();
		g.drawImage(buffer, 0, 0, this);
	}

	// no need to paint background or children; flat component
	public void update(Graphics g) {
		this.paint(g);
	}

	public void close() {
		Thread closeThread = new Thread() {
			public void run() {
				setVisible(true);
				message = "Welcome to " + About.NAME + " " + About.VERSION
						+ "!";
				progress = 100;
				repaint();
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				setAlwaysOnTop(false);
				setVisible(false);
				MainUI.getInstance().setVisible(true);
				MainUI.getInstance().toFront();
				dispose();
			}
		};
		// SwingUtilities.invokeLater(closeThread);
		if (MainUI.getInstance() != null) {
			closeThread.start();
		}
	}

	public void changeText(String s, int status) {
		message = s;
		log.add(s);
		log.add(status);
		repaint();
	}

	public ArrayList getLog() {
		return log;
	}

	public void open() {
		// make visible
		this.setVisible(true);
	}

	public void setProgress(int progress) {
		this.progress = progress;
		repaint();
	}

}
