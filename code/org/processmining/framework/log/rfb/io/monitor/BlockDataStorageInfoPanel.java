/**
 * Project: ProM Framework
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Oct 14, 2006 11:23:23 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.rfb.io.monitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;

/**
 * Displays visual feedback of a block data storage instance
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class BlockDataStorageInfoPanel extends JLabel {

	public static Color freeColor = new Color(20, 200, 20);
	public static Color fillColor05 = new Color(224, 224, 20);
	public static Color fillColor10 = new Color(224, 216, 19);
	public static Color fillColor15 = new Color(224, 204, 18);
	public static Color fillColor20 = new Color(224, 192, 17);
	public static Color fillColor25 = new Color(224, 180, 16);
	public static Color fillColor30 = new Color(223, 168, 15);
	public static Color fillColor35 = new Color(223, 156, 14);
	public static Color fillColor40 = new Color(223, 144, 13);
	public static Color fillColor45 = new Color(223, 132, 12);
	public static Color fillColor50 = new Color(222, 120, 11);
	public static Color fillColor55 = new Color(222, 108, 10);
	public static Color fillColor60 = new Color(222, 96, 9);
	public static Color fillColor65 = new Color(221, 84, 8);
	public static Color fillColor70 = new Color(221, 72, 7);
	public static Color fillColor75 = new Color(221, 60, 6);
	public static Color fillColor80 = new Color(221, 48, 5);
	public static Color fillColor85 = new Color(220, 36, 4);
	public static Color fillColor90 = new Color(220, 24, 3);
	public static Color fillColor95 = new Color(220, 12, 2);
	public static Color fillColor100 = new Color(220, 00, 0);

	protected static Color getFillColor(int blockSize, int blockFillSize) {
		float fillRatio = (float) blockFillSize / (float) blockSize;
		if (fillRatio == 0) {
			return freeColor;
		} else if (fillRatio <= 0.05) {
			return fillColor05;
		} else if (fillRatio <= 0.1) {
			return fillColor10;
		} else if (fillRatio <= 0.15) {
			return fillColor15;
		} else if (fillRatio <= 0.2) {
			return fillColor20;
		} else if (fillRatio <= 0.25) {
			return fillColor25;
		} else if (fillRatio <= 0.3) {
			return fillColor30;
		} else if (fillRatio <= 0.35) {
			return fillColor35;
		} else if (fillRatio <= 0.4) {
			return fillColor40;
		} else if (fillRatio <= 0.45) {
			return fillColor45;
		} else if (fillRatio <= 0.5) {
			return fillColor50;
		} else if (fillRatio <= 0.55) {
			return fillColor55;
		} else if (fillRatio <= 0.6) {
			return fillColor60;
		} else if (fillRatio <= 0.65) {
			return fillColor65;
		} else if (fillRatio <= 0.7) {
			return fillColor70;
		} else if (fillRatio <= 0.75) {
			return fillColor75;
		} else if (fillRatio <= 0.8) {
			return fillColor80;
		} else if (fillRatio <= 0.85) {
			return fillColor85;
		} else if (fillRatio <= 0.9) {
			return fillColor90;
		} else if (fillRatio <= 0.95) {
			return fillColor95;
		} else {
			return fillColor100;
		}
	}

	protected int drawSize;
	protected Dimension mySize;
	protected BlockDataStorageInfo info;
	protected Color colorBg = new Color(170, 170, 160);
	protected Color colorBorder = new Color(50, 50, 40);
	protected Color colorFont = new Color(30, 30, 20);

	public BlockDataStorageInfoPanel(BlockDataStorageInfo aInfo, int aDrawSize) {
		info = aInfo;
		drawSize = aDrawSize;
		mySize = new Dimension(aDrawSize + 11, aDrawSize + 56);
		this.setMinimumSize(mySize);
		this.setMaximumSize(mySize);
		this.setPreferredSize(mySize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paintComponent(Graphics grx) {
		super.paintComponent(grx);
		Graphics g = grx.create();
		int width = this.getWidth();
		int height = this.getHeight();
		Font font = g.getFont().deriveFont(10.0f);
		g.setFont(font);
		// paint background
		g.setColor(colorBg);
		g.fillRect(0, 0, width, height);
		g.setColor(colorBorder);
		g.drawRect(0, 0, width - 2, height - 2);
		// writer header information
		g.setColor(colorFont);
		g.drawString("File: " + info.getFile().getName(), 5, 15);
		g.drawString(info.getNumberOfBlocks() + " blocks (PLevel "
				+ info.getPartitionLevel() + ")", 5, 30);
		g.drawString(info.getBlockSize() + " bytes per block", 5, 45);
		// write block table
		int numberOfBlocks = info.getNumberOfBlocks();
		int tmp = 1;
		int rows = 1;
		int columns = 1;
		while (tmp < numberOfBlocks) {
			columns <<= 1; // *= 2
			tmp <<= 1; // *= 2
			if (tmp < numberOfBlocks) {
				rows <<= 1; // *= 2
				tmp <<= 1; // *= 2
			}
		}
		int columnWidth = (int) ((float) drawSize / (float) columns);
		int rowHeight = (int) ((float) drawSize / (float) rows);
		int blockCounter = 0;
		int x = 5;
		int y = 50;
		int blockSize = info.getBlockSize();
		int[] blockFillSizes = info.getBlockFillSizes();
		for (int horiz = 0; horiz < columns; horiz++) {
			y = 50;
			for (int vert = 0; vert < rows; vert++) {
				g.setColor(BlockDataStorageInfoPanel.getFillColor(blockSize,
						blockFillSizes[blockCounter]));
				g.fillRect(x, y, columnWidth, rowHeight);
				if (columnWidth > 2 && rowHeight > 2) {
					g.setColor(colorBorder);
					g.drawRect(x, y, columnWidth, rowHeight);
				}
				y += rowHeight;
				blockCounter++;
			}
			x += columnWidth;
		}
		g.setColor(colorBorder);
		g.drawRect(5, 50, drawSize, drawSize);
	}

}
