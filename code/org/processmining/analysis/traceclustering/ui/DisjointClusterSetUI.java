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
package org.processmining.analysis.traceclustering.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.analysis.traceclustering.algorithm.ClusteringInput;
import org.processmining.analysis.traceclustering.distance.DistanceMatrix;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.ColorRepository;

/**
 * @author christian
 * 
 */
public class DisjointClusterSetUI extends JComponent implements MouseListener,
		MouseMotionListener {

	protected ClusterSet clusters;
	protected ClusteringInput input;
	DistanceMatrix distanceMatrix;
	protected List<Cluster> clusterList;

	protected BufferedImage buffer;
	protected int rasterSize;
	protected int border = 100;

	protected boolean mouseOver;
	protected int mouseX;
	protected int mouseY;

	protected List<ProcessInstance> tracePointers;
	protected List<Integer> traceIndices;
	protected List<Cluster> clusterIndices;
	protected List<Integer> clusterSizes;

	public DisjointClusterSetUI(ClusterSet aClusterSet, ClusteringInput anInput) {
		setBackground(Color.BLACK);
		mouseX = -1;
		mouseY = -1;
		mouseOver = false;
		clusters = aClusterSet;
		input = anInput;
		distanceMatrix = input.getDistanceMatrix();
		// create sorted list of clusters in ascending order
		clusterList = new ArrayList<Cluster>(clusters.getClusters());
		Collections.sort(clusterList, new Comparator<Cluster>() {
			public int compare(Cluster a, Cluster b) {
				int aSize = a.getTraceIndices().size();
				int bSize = b.getTraceIndices().size();
				if (aSize == bSize) {
					return 0;
				} else if (aSize > bSize) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		// insert ordered to indices of clusters and traces in
		// reverse order (i.e., starting from largest cluster)
		tracePointers = new ArrayList<ProcessInstance>();
		traceIndices = new ArrayList<Integer>();
		clusterIndices = new ArrayList<Cluster>();
		clusterSizes = new ArrayList<Integer>();
		for (int i = clusterList.size() - 1; i >= 0; i--) {
			Cluster currentCluster = clusterList.get(i);
			clusterSizes.add(currentCluster.getTraceIndices().size());
			for (int traceIndex : currentCluster.getTraceIndices()) {
				tracePointers.add(input.getLog().getInstance(traceIndex));
				traceIndices.add(traceIndex);
				clusterIndices.add(currentCluster);
			}
		}
		// start with invalid raster size to trigger buffering
		rasterSize = -1;
		// add self as mouse (motion) listener
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getMaximumSize()
	 */
	@Override
	public Dimension getMaximumSize() {
		int traces = tracePointers.size();
		int width = Math.max((traces * 8) + border, 4000);
		int height = Math.max((traces * 8) + border, 4000);
		return new Dimension(width, height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		int traces = tracePointers.size();
		int minWidth = traces + border;
		int minHeight = traces + border;
		return new Dimension(minWidth, minHeight);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int numberOfTraces = tracePointers.size();
		int width = this.getWidth();
		int height = this.getHeight();
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// paint black background
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		// determine optimal raster size
		int maxDimension = Math.min(width - border, height - border);
		int optimalRasterSize = maxDimension / numberOfTraces;
		int recomRasterSize = Math.max(optimalRasterSize, 1);
		if (recomRasterSize != this.rasterSize || this.buffer == null) {
			this.rasterSize = recomRasterSize;
			// re-create buffer
			int bufferSize = numberOfTraces * recomRasterSize;
			buffer = new BufferedImage(bufferSize, bufferSize,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D gBuf = buffer.createGraphics();
			for (int traceX = 0; traceX < numberOfTraces; traceX++) {
				for (int traceY = 0; traceY < numberOfTraces; traceY++) {
					int traceMappedX = traceIndices.get(traceX);
					int traceMappedY = traceIndices.get(traceY);
					double colorVal = 1.0 - distanceMatrix.get(traceMappedX,
							traceMappedY);
					Color color = ColorRepository
							.getGradualColorBlackZero(colorVal);
					gBuf.setColor(color);
					if (recomRasterSize == 1) {
						gBuf.drawLine(traceMappedX, traceMappedY, traceMappedX,
								traceMappedY);
					} else {
						int x = traceMappedX * recomRasterSize;
						int y = traceMappedY * recomRasterSize;
						gBuf.fillRect(x, y, recomRasterSize, recomRasterSize);
					}
				}
			}
			gBuf.dispose();
		}
		// copy buffer to OS framebuffer
		g2d.drawImage(buffer, border / 2, border / 2, this);
		// draw cluster boundary indicators
		int lastSize = border / 2;
		Color colorEven = new Color(120, 0, 0);
		Color colorEvenMouseOver = new Color(255, 0, 0);
		Color colorOdd = new Color(0, 0, 120);
		Color colorOddMouseOver = new Color(0, 0, 255);
		int fixA = (border / 2) - 5;
		int fixB = fixA - 10;
		int fixC = fixB - 7;
		for (int i = 0; i < clusterSizes.size(); i++) {
			int currentSize = clusterSizes.get(i) * this.rasterSize;
			int currentOuterCoord = lastSize + currentSize;
			// set color
			if ((i % 2) == 0) {
				if (mouseOver && mouseX > lastSize
						&& mouseX <= currentOuterCoord) {
					g2d.setColor(colorEvenMouseOver);
				} else {
					g2d.setColor(colorEven);
				}
				g2d.fillRect(lastSize, fixC, currentSize, fixA - fixC);
				if (mouseOver && mouseY > lastSize
						&& mouseY <= currentOuterCoord) {
					g2d.setColor(colorEvenMouseOver);
				} else {
					g2d.setColor(colorEven);
				}
				g2d.fillRect(fixC, lastSize, fixA - fixC, currentSize);
			} else {
				if (mouseOver && mouseX > lastSize
						&& mouseX <= currentOuterCoord) {
					g2d.setColor(colorOddMouseOver);
				} else {
					g2d.setColor(colorOdd);
				}
				g2d.fillRect(lastSize, fixB, currentSize, fixA - fixB);
				if (mouseOver && mouseY > lastSize
						&& mouseY <= currentOuterCoord) {
					g2d.setColor(colorOddMouseOver);
				} else {
					g2d.setColor(colorOdd);
				}
				g2d.fillRect(fixB, lastSize, fixA - fixB, currentSize);
			}
			lastSize += currentSize;
		}
		// draw on-screen cluster boundary mouse is hovering over
		if (mouseOver == true) {
			int selectX = -1, selectY = -1;
			int startX = 0, endX = 0, startY = 0, endY = 0;
			int sumSize = (border / 2);
			int boundary = sumSize + (tracePointers.size() * this.rasterSize);
			if (mouseX < sumSize || mouseY < sumSize || mouseX > boundary
					|| mouseY > boundary) {
				// invalid, ignore
			} else {
				// find selected cluster combination
				for (int i = 0; i < clusterSizes.size(); i++) {
					int outerBound = sumSize
							+ (clusterSizes.get(i) * this.rasterSize);
					if (selectX < 0 && mouseX <= outerBound) {
						selectX = i;
						startX = sumSize;
						endX = outerBound;
					}
					if (selectY < 0 && mouseY <= outerBound) {
						selectY = i;
						startY = sumSize;
						endY = outerBound;
					}
					sumSize = outerBound;
				}
				// draw boundary of current selection
				g2d.setColor(new Color(230, 230, 230));
				g2d.drawRect(startX, startY, endX - startX, endY - startY);
				drawInfo(g2d, width, height, selectX, selectY, startX, startY,
						endX, endY);
			}
		}
		// finished drawing
		g2d.dispose();
	}

	protected void drawInfo(Graphics2D g2d, int width, int height, int selectX,
			int selectY, int startX, int startY, int endX, int endY) {
		final int border = 10;
		final Color colorBg = new Color(30, 30, 30, 200);
		final Color colorFg = new Color(200, 200, 200, 225);
		Font font = g2d.getFont().deriveFont(11.0f);
		g2d.setFont(font);
		// assemble text to draw
		FontMetrics fontMetrics = g2d.getFontMetrics(font);
		List<String> lines = new ArrayList<String>();
		if (selectX == selectY) {
			// one cluster selected
			Cluster cluster = clusters.getClusters().get(selectX);
			lines.add(cluster.getName());
			lines.add("contains " + cluster.size() + " traces");
		} else {
			// comparison between two clusters
			lines.add("Comparison between: ");
			Cluster clusterX = clusters.getClusters().get(selectX);
			Cluster clusterY = clusters.getClusters().get(selectY);
			lines.add("  " + clusterX.getName() + " (contains "
					+ clusterX.size() + " traces)");
			lines.add("  " + clusterY.getName() + " (contains "
					+ clusterY.size() + " traces)");
		}
		// compute size and position of info overlay
		int textWidth = 0;
		for (String line : lines) {
			textWidth = Math.max(textWidth, (int) fontMetrics.getStringBounds(
					line, g2d).getWidth());
		}
		int textHeight = lines.size() * (fontMetrics.getHeight() + 3);
		int boxWidth = textWidth + border + border;
		int boxHeight = textHeight + border + border;
		int boxX = mouseX;
		if (boxX + boxWidth > width) {
			boxX = mouseX - boxWidth;
		}
		int boxY = mouseY;
		if (boxY + boxHeight > height) {
			boxY = mouseY - boxHeight;
		}
		// draw background
		g2d.setColor(colorBg);
		g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, border, border);
		// draw text
		g2d.setColor(colorFg);
		int fontX = boxX + border;
		int fontY = boxY + border + fontMetrics.getHeight();
		for (String line : lines) {
			g2d.drawString(line, fontX, fontY);
			fontY += fontMetrics.getHeight();
			fontY += 3;
		}
	}

	public void mouseDragged(MouseEvent e) {
		mouseOver = true;
		mouseX = e.getX();
		mouseY = e.getY();
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		mouseOver = true;
		mouseX = e.getX();
		mouseY = e.getY();
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		// ignore
	}

	public void mouseEntered(MouseEvent e) {
		mouseOver = true;
		mouseX = e.getX();
		mouseY = e.getY();
		repaint();
	}

	public void mouseExited(MouseEvent e) {
		mouseOver = false;
		mouseX = -1;
		mouseY = -1;
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		// ignore
	}

	public void mouseReleased(MouseEvent e) {
		// ignore
	}

}
