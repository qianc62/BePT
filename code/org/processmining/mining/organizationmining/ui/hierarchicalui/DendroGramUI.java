package org.processmining.mining.organizationmining.ui.hierarchicalui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import org.processmining.mining.organizationmining.model.AHCTreeNode;

/**
 * @author Minseok Song
 * 
 */
public class DendroGramUI extends JPanel {

	protected AHCTreeNode rootNode;
	protected int rasterSize;
	protected int border = 100;
	protected int ratioY;
	protected int ratioX;
	protected int height;
	protected int width;
	protected BufferedImage buffer;
	protected int bufferSizeX;
	protected int bufferSizeY;
	protected double cutPoint;
	protected DecimalFormat format = new DecimalFormat("0.0000");
	protected Color normalColor = new Color(150, 100, 220);
	protected Color clusterColor0 = Color.GRAY;
	protected Color clusterColor1 = Color.lightGray;
	protected boolean bCurrentColor = true;
	protected int numberOfClusters = 0;
	protected ColorReference colorReference;

	public DendroGramUI(AHCTreeNode aRootNode, double aCutPoint) {
		rootNode = aRootNode;
		cutPoint = aCutPoint;
		colorReference = new ColorReference();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getMaximumSize()
	 */
	@Override
	public Dimension getMaximumSize() {
		int numberOfTraces = rootNode.size();
		int recomSize = Math.max(
				ratioX = (width - 4 * border) / numberOfTraces, 2);
		int width = numberOfTraces * recomSize;
		int height = 4000;
		return new Dimension(width, height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics grx) {
		numberOfClusters = 0;
		int numberOfTraces = rootNode.size();
		double maxValue = rootNode.getDistance();

		width = this.getWidth();
		height = this.getHeight();
		final Graphics2D g2d = (Graphics2D) grx;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// paint black background
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		g2d.setColor(Color.white);

		border = 5;
		ratioY = (int) ((height - 7 * border) / (maxValue + 0.005 * maxValue));
		ratioX = (width - 4 * border) / numberOfTraces;

		int recomRasterSize = Math.max(ratioX, 2);

		// if(recomRasterSize != this.rasterSize || this.buffer == null) {
		this.rasterSize = recomRasterSize;
		// re-create buffer
		bufferSizeX = numberOfTraces * recomRasterSize;
		bufferSizeY = height - border * 4;
		buffer = new BufferedImage(bufferSizeX, bufferSizeY,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D gBuf = buffer.createGraphics();

		// draw boxes for instances
		for (int i = 0; i < numberOfTraces; i++) {
			if (i % 2 == 0)
				gBuf.setColor(new Color(240, 240, 220));
			else
				gBuf.setColor(new Color(150, 100, 220));
			gBuf.fillRect(i * rasterSize, bufferSizeY - border * 3, rasterSize,
					border * 3);
		}

		bufferSizeY -= border * 3;
		// draw lines
		gBuf.setColor(Color.white);
		drawLines(rootNode, 0, gBuf);

		// write number of clusters
		gBuf.setColor(Color.green);
		gBuf.drawString("Number of Clusters = " + numberOfClusters, border, 13);

		// draw cutOff lines
		int tempY = bufferSizeY - (int) ((double) cutPoint * ratioY);
		gBuf.setColor(Color.YELLOW);
		gBuf.drawLine(0, tempY, bufferSizeX, tempY);

		gBuf.dispose();
		g2d.drawImage(buffer, 2 * border, 2 * border, this);
		if (bufferSizeX > width) {
			Dimension dim = new Dimension(bufferSizeX + 4 * border, this.height);
			this.setPreferredSize(dim);
			revalidate();
		}

		// draw outer line
		g2d.setColor(Color.white);
		g2d.drawRect(border, border, bufferSizeX + border * 2, this.getHeight()
				- border * 2);

		g2d.dispose();
		// }
	}

	protected void drawLines(AHCTreeNode node, int pivotX, Graphics g) {
		AHCTreeNode leftNode = (AHCTreeNode) node.getLeft();
		AHCTreeNode rightNode = (AHCTreeNode) node.getRight();
		if (leftNode == null || rightNode == null) {
			return;
		}
		if (node.getDistance() > cutPoint)
			g.setColor(Color.white);
		int x1 = pivotX + (int) (rasterSize * leftNode.size() / 2.0);
		int x2 = x1 + (int) (node.size() / 2.0 * rasterSize);
		int y = bufferSizeY - (int) (node.getDistance() * ratioY);
		g.drawLine(x1, y, x2, y);
		int y2 = bufferSizeY - (int) ((double) leftNode.getDistance() * ratioY);
		g.drawLine(x1, y, x1, y2);
		y2 = bufferSizeY - (int) ((double) rightNode.getDistance() * ratioY);
		g.drawLine(x2, y, x2, y2);

		// write values
		g.setFont(new Font("Dialog", Font.PLAIN, 12));
		if (node.getDistance() != 0) {
			if ((x1 + 2 + 100) > bufferSizeX)
				g.drawString(format.format(node.getDistance()), x1 + 2 - 40,
						y + 13);
			else
				g.drawString(format.format(node.getDistance()), x1 + 2, y + 13);
		}

		if (node.getDistance() >= cutPoint
				&& ((AHCTreeNode) node.getLeft()).getDistance() < cutPoint) {
			changeCurrentColor(g);
			g.fillRect(pivotX, bufferSizeY, rasterSize
					* ((AHCTreeNode) node.getLeft()).size(), border * 3);
			numberOfClusters++;
		}
		drawLines(leftNode, pivotX, g);
		if (node.getDistance() >= cutPoint
				&& ((AHCTreeNode) node.getRight()).getDistance() < cutPoint) {
			changeCurrentColor(g);
			g.fillRect(pivotX + (int) (leftNode.size() * rasterSize),
					bufferSizeY, rasterSize
							* ((AHCTreeNode) node.getRight()).size(),
					border * 3);
			numberOfClusters++;
		}
		drawLines(rightNode, pivotX + (int) (leftNode.size() * rasterSize), g);
	}

	private void changeCurrentColor(Graphics g) {
		Color tempColor = colorReference
				.getColor("Cluster " + numberOfClusters);
		g.setColor(tempColor);
	}
}
