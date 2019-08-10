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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

import javax.swing.JComponent;

/**
 * @author christian
 * 
 */
public class CloudChamberFastRenderer {

	private static Color[][] COLOR_CHART = {
			{ new Color(189, 0, 38), new Color(240, 59, 32),
					new Color(253, 141, 60), new Color(254, 204, 92),
					new Color(255, 255, 178) },
			{ new Color(122, 1, 119), new Color(197, 27, 138),
					new Color(247, 104, 161), new Color(251, 180, 185),
					new Color(254, 235, 226) } };

	private static int encodeColor(Color color) {
		return (255 << 24) | (color.getRed() << 16) | (color.getGreen() << 8)
				| color.getBlue();
	}

	private static int[][] COLOR_ENCODED = {
			{ encodeColor(COLOR_CHART[0][0]), encodeColor(COLOR_CHART[0][1]),
					encodeColor(COLOR_CHART[0][2]),
					encodeColor(COLOR_CHART[0][3]),
					encodeColor(COLOR_CHART[0][4]) },
			{ encodeColor(COLOR_CHART[1][0]), encodeColor(COLOR_CHART[1][1]),
					encodeColor(COLOR_CHART[1][2]),
					encodeColor(COLOR_CHART[1][3]),
					encodeColor(COLOR_CHART[1][4]) } };

	private static int mapColor(double value) {
		if (value == 0.0) {
			return 0xff000000;
		} else if (value <= 0.2) {
			return COLOR_ENCODED[0][0];
		} else if (value <= 0.4) {
			return COLOR_ENCODED[0][1];
		} else if (value <= 0.6) {
			return COLOR_ENCODED[0][2];
		} else if (value <= 0.8) {
			return COLOR_ENCODED[0][3];
		} else {
			return COLOR_ENCODED[0][4];
		}
	}

	protected JComponent component;
	protected CloudChamberStats stats;
	protected int size, oldX, oldY, imgWidth, imgHeight;
	protected int[] pixels;
	protected MemoryImageSource imageSource;
	protected Image image;

	public CloudChamberFastRenderer(CloudChamberStats ccStats,
			JComponent aComponent) {
		component = aComponent;
		stats = ccStats;
		size = -1;
		oldX = -1;
		oldY = -1;
		imgWidth = -1;
		imgHeight = -1;
		pixels = null;
		imageSource = null;
		image = null;
	}

	public Image getSection(int x, int y, int width, int height) {
		if (width > imgWidth || height > imgHeight) {
			// create new image source and pixel array
			size = width * height;
			pixels = new int[size];
			imageSource = new MemoryImageSource(width, height, pixels, 0, width);
			imageSource.setAnimated(true);
			// imageSource.setFullBufferUpdates(true);
			image = Toolkit.getDefaultToolkit().createImage(imageSource);
		}
		if (width > imgWidth || height > imgHeight || x != oldX || y != oldY) {
			// recreate pixel array
			int skipWidth = 0;
			if (width < imgWidth && height <= imgHeight) {
				skipWidth = imgWidth - width;
			}
			int index = 0;
			for (int aY = 0; aY < height; aY++) {
				for (int aX = 0; aX < width; aX++) {
					if (index >= size) {
						System.out.println("oops...");
					}
					// pixels[index] = encodeColor(stats.getValueAt(x + aX, y +
					// aY));
					pixels[index] = mapColor(stats.getValueAt(x + aX, y + aY));
					index++;
				}
				index += skipWidth; // skip pixels to the right of clip area
			}
			oldX = x;
			oldY = y;
			imageSource.newPixels(0, 0, width, height);
			// imageSource.newPixels();
		}
		if (width > imgWidth || height > imgHeight) {
			imgWidth = width;
			imgHeight = height;
		}
		return image;
	}

	public static int encodeColor(double value) {
		if (value == 0.0) {
			return 0xff000000;
		} else {
			int red, green, blue;
			if (value < 0.5) {
				red = 0;
				green = (int) (510 * value);
				blue = 255 - green;
			} else {
				red = (int) ((value - 0.5) * 510);
				green = 255 - red;
				blue = 0;
			}
			return (255 << 24) | (red << 16) | (green << 8) | blue;
		}
	}

	public static BufferedImage getGradient(int width, int height) {
		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		if (width > height) {
			for (int x = 0; x < width; x++) {
				// g2d.setColor(new Color(encodeColor((double)x / (double)(width
				// - 1))));
				g2d.setColor(new Color(mapColor((double) x
						/ (double) (width - 1))));
				g2d.drawLine(x, 0, x, height - 1);
			}
		} else {
			for (int y = 0; y < height; y++) {
				// g2d.setColor(new Color(encodeColor(1.0 - ((double)y /
				// (double)(height - 1)))));
				g2d.setColor(new Color(
						mapColor(1.0 - ((double) y / (double) (height - 1)))));
				g2d.drawLine(0, y, width - 1, y);
			}
		}
		g2d.dispose();
		return img;
	}
}
