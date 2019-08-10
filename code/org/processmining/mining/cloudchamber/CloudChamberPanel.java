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
package org.processmining.mining.cloudchamber;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.processmining.framework.ui.UISettings;
import org.processmining.framework.util.ColorRepository;

/**
 * @author christian
 * 
 */
public class CloudChamberPanel extends JComponent implements MouseListener,
		MouseMotionListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
	 */

	protected CloudChamberStats stats;
	protected CloudChamberFastRenderer renderer;
	protected int mouseX, mouseY, x1, x2, y1, y2, clipMaxX, clipMaxY;
	protected boolean mouseSwitch;
	protected BufferedImage gradient;
	protected Image helpImage;
	protected float gradientAlpha;
	protected float gradientHelp;

	/**
	 *
	 */
	public CloudChamberPanel(CloudChamberStats ccStats) {
		stats = ccStats;
		renderer = new CloudChamberFastRenderer(stats, this);
		Dimension size = new Dimension(stats.size(), stats.size());
		this.setDoubleBuffered(true);
		this.setSize(size);
		this.setMinimumSize(size);
		this.setMaximumSize(size);
		this.setPreferredSize(size);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		mouseX = 0;
		mouseY = 0;
		mouseSwitch = true;
		gradient = CloudChamberFastRenderer.getGradient(20, 120);
		gradientAlpha = 1.0f;
		helpImage = (new ImageIcon(UISettings.getProMDirectoryPath()
				+ "images/cloudchamberdoc.png").getImage());
		gradientHelp = 0.0f;
	}

	public void showHelp() {
		if (gradientHelp == 1.0f) {
			fadeHelp(true);
		} else if (gradientHelp == 0.0f) {
			fadeHelp(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		int size = stats.size();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Color color;
		Rectangle clip = this.getVisibleRect();
		x1 = clip.x;
		y1 = clip.y;
		clipMaxX = x1 + clip.width;
		clipMaxY = y1 + clip.height;
		x2 = Math.min(clipMaxX, size);
		y2 = Math.min(clipMaxY, size);
		int renderWidth = Math.min(clip.width, size);
		int renderHeight = Math.min(clip.height, size);
		if (renderWidth < clip.width || renderHeight < clip.height) {
			// draw background
			g2d.setColor(Color.BLACK);
			g2d.fillRect(x1, y1, clip.width, clip.height);
		}
		g2d.drawImage(renderer.getSection(x1, y1, renderWidth, renderHeight),
				x1, y1, null);
		// draw instance boundaries
		int[] instanceBoundaries = stats.getInstanceBoundaries();
		g2d.setColor(new Color(0, 30, 0));
		for (int i = 1; i < instanceBoundaries.length; i++) {
			if (instanceBoundaries[i] >= x1 || instanceBoundaries[i] >= y1) {
				if (instanceBoundaries[i] > x2 && instanceBoundaries[i] > y2) {
					break;
				}
				g2d.drawLine(x1, instanceBoundaries[i], x2,
						instanceBoundaries[i]);
				g2d.drawLine(instanceBoundaries[i], y1, instanceBoundaries[i],
						y2);
			}
		}
		// draw inspector
		int ptX = mouseX - 10;
		int ptY = mouseY - 10;
		if (ptX >= x1 && ptX < x2 && ptY >= y1 && ptY < y2) {
			// valid mouse position;
			g2d.setColor(new Color(150, 150, 100));
			g2d.drawOval(ptX - 3, ptY - 3, 7, 7);
			String[] description = stats.getDescriptionAt(ptX, ptY);
			if (description != null) {
				g2d.drawLine(ptX + 4, ptY, ptX + 30, ptY);
				for (int i = 0; i < description.length; i++) {
					int txtX = ptX + 33;
					int txtY = ptY + 3 + (i * 17);
					g2d.drawString(description[i], txtX, txtY);
				}
			}
		}
		// draw legend
		if (gradientAlpha > 0.0f) {
			if (gradientAlpha < 1.0f) {
				AlphaComposite alphaComp = AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, gradientAlpha);
				g2d.setComposite(alphaComp);
			}
			g2d.drawImage(gradient, clipMaxX - 40, y1 + 20, null);
		} else {
			g2d.setColor(new Color(255, 255, 255, 70));
			g2d.drawRect(clipMaxX - 40, y1 + 20, 20, 120);
		}
		// draw help
		if (gradientHelp != 0.0f) {
			AlphaComposite alphaComp = AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, gradientHelp * 0.75f);
			g2d.setComposite(alphaComp);
			g2d.setColor(Color.BLACK);
			g2d.fillRect(x1, y1, renderWidth, renderHeight);
			alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					gradientHelp);
			g2d.setComposite(alphaComp);
			int offset = (clip.height - helpImage.getHeight(null)) / 2;
			if (offset < 0) {
				offset = 0;
			}
			g2d.drawImage(helpImage, x1 + offset, y1 + offset, null);
		}
		g2d.dispose();
	}

	protected void fadeGradient(final boolean fadeOut) {
		Thread fader = new Thread() {
			public void run() {
				if (gradientAlpha != 0.0f && gradientAlpha != 1.0f) {
					return; // fading still in progress
				}
				for (float a = 1.0f; a >= 0.0f; a -= 0.05) {
					if (fadeOut == true) {
						gradientAlpha = a;
					} else {
						gradientAlpha = 1.0f - a;
					}
					repaint();
					try {
						sleep(50);
					} catch (InterruptedException e) {
						// nevermind
						e.printStackTrace();
					}
				}
				if (fadeOut == true) {
					gradientAlpha = 0.0f;
				} else {
					gradientAlpha = 1.0f;
				}
				repaint();
			}
		};
		fader.start();
	}

	protected void fadeHelp(final boolean fadeOut) {
		if (gradientHelp != 0.0f && gradientHelp != 1.0f) {
			return; // fading still in progress
		}
		Thread fader = new Thread() {
			public void run() {
				for (float a = 1.0f; a >= 0.0f; a -= 0.025) {
					if (fadeOut == true) {
						gradientHelp = a;
					} else {
						gradientHelp = 1.0f - a;
					}
					repaint();
					try {
						sleep(60);
					} catch (InterruptedException e) {
						// nevermind
						e.printStackTrace();
					}
				}
				if (fadeOut == true) {
					gradientHelp = 0.0f;
				} else {
					gradientHelp = 1.0f;
				}
				repaint();
			}
		};
		fader.start();
	}

	protected Color getColor(int x, int y) {
		double value = stats.getValueAt(x, y);
		if (value == 0.0) {
			return null;
		} else {
			return ColorRepository.getGradualColor(value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent event) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent event) {
		if (mouseSwitch == true) {
			mouseX = event.getX();
			mouseY = event.getY();
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent event) {
		if (gradientHelp == 1.0f) {
			fadeHelp(true);
		} else if (mouseX >= (clipMaxX - 40) && mouseY >= (y1 + 20)
				&& mouseX < (clipMaxX - 20) && mouseY < (y1 + 120)) {
			fadeGradient(gradientAlpha == 1.0f);
		} else {
			mouseX = event.getX();
			mouseY = event.getY();
			mouseSwitch = !mouseSwitch;
		}
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
		if (mouseSwitch == true) {
			mouseX = -1;
			mouseY = -1;
			repaint();
		}
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
