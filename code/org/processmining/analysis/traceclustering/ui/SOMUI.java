package org.processmining.analysis.traceclustering.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.framework.log.ProcessInstance;

import java.util.Iterator;
import java.util.Random;

/**
 * @author Minseok
 * 
 */
public class SOMUI extends JComponent {

	protected ClusterSet clusters;
	protected List<Cluster> clusterList;

	protected BufferedImage buffer = null;
	protected int rasterSize;
	protected int border = 100;

	protected boolean mouseOver;
	protected int mouseX;
	protected int mouseY;
	protected int scatteredRatio;
	protected double[][] uMatrix;

	protected List<ProcessInstance> tracePointers;
	protected List<Integer> traceIndices;
	protected List<Cluster> clusterIndices;
	protected List<Integer> clusterSizes;

	protected int colNumber, rowNumber;
	private static final int IMAGE_WIDTH = 400;
	private static final int IMAGE_HEIGHT = 300;
	private int pixWidth;
	private int pixHeight;
	protected ColorReference colorReference;
	private String backgroundStyle;
	private String colorStyle;
	public static String ST_BLACK = "Black style";
	public static String ST_LANDSCALE = "Landscale";
	public static String ST_DENSITY = "Density";
	public static String ST_UMATRIX = "U Matrix";

	public SOMUI(ClusterSet aClusterSet, int colNumber, int rowNumber,
			int aScatteredRatio, String aBackgroundStyle, String aColorStyle,
			double[][] uMatrix) {
		this.colNumber = colNumber;
		this.rowNumber = rowNumber;
		clusters = aClusterSet;
		scatteredRatio = aScatteredRatio;
		colorReference = new ColorReference();
		backgroundStyle = aBackgroundStyle;
		colorStyle = aColorStyle;
		this.uMatrix = uMatrix;
	}

	/**
	 * overwritten as proxy, to update mill2pixels ratio
	 */
	public void setSize(int width, int height) {
		super.setSize(width, height);
		initBufferedImage();
		revalidate();
	}

	/**
	 * overwritten as proxy, to update mill2pixels ratio
	 */
	public void setSize(Dimension d) {
		super.setSize(d);
		initBufferedImage();
		revalidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int border = 5;
		int width = this.getWidth();
		int height = this.getHeight();
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// paint background
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);

		// calculateBackgroudImage
		pixWidth = getWidth() - 2 * border;
		pixHeight = getHeight() - 2 * border;
		int unitWidth = pixWidth / colNumber;
		int unitHeight = pixHeight / rowNumber;
		if (pixWidth % unitWidth != 0)
			pixWidth = unitWidth * (pixWidth / unitWidth);
		if (pixHeight % unitWidth != 0)
			pixHeight = unitHeight * (pixHeight / unitHeight);

		if (buffer == null)
			recalculateBackgroundImage(pixWidth, pixHeight);

		g2d.drawImage(buffer, border, border, this);

		Iterator itr = clusters.getClusters().iterator();
		int k = 0;
		Random rnd = new Random(1976);
		while (itr.hasNext()) {
			Cluster cluster = (Cluster) itr.next();
			// draw points
			if (cluster.size() > 0) {
				int x = (k % colNumber) * unitWidth + unitWidth / 2;
				int y = (k / colNumber) * unitHeight + unitHeight / 2;
				Color pointColor = colorReference.getColor(cluster.getName());
				g2d.setColor(pointColor);
				if (scatteredRatio > 0) {
					for (int i = 0; i < cluster.size(); i++) {
						double pertX = 0.0d;
						double pertY = 0.0d;

						double lowerBound = -unitWidth / 2.0d;
						double upperBound = unitWidth / 2.0d;
						pertX = ((rnd.nextDouble() * (upperBound - lowerBound)) + lowerBound)
								* (scatteredRatio / 80.0d);
						lowerBound = -unitHeight / 2.0d;
						upperBound = unitHeight / 2.0d;
						pertY = ((rnd.nextDouble() * (upperBound - lowerBound)) + lowerBound)
								* (scatteredRatio / 80.0d);

						int x_point = (int) (pertX + x);
						int y_point = (int) (pertY + y);

						g2d.fillOval(x_point - 3, y_point - 3, 7, 7);
					}
				} else {
					g2d.fillOval(x - 3, y - 3, 7, 7);
				}
				// write number of instances in a cluster
				if (backgroundStyle.equals(ST_BLACK))
					g2d.setColor(Color.yellow);
				else
					g2d.setColor(Color.black);
				g2d.setFont(new Font("Dialog", Font.BOLD, 10));
				g2d.drawString(String.valueOf(cluster.size()), x + 6, y + 6);
			}
			k++;
		}
		g2d.dispose();
	}

	private void recalculateBackgroundImage(int imageWidth, int imageHeight) {
		buffer = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D gBuf = buffer.createGraphics();
		int width = imageWidth / colNumber;
		int height = imageHeight / rowNumber;

		if (backgroundStyle.equals(ST_BLACK)) {
			gBuf.setBackground(Color.black);
		} else {
			double[][] printMatrix = new double[colNumber][rowNumber];
			Iterator itr = clusters.getClusters().iterator();
			int k = 0;
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			if (colorStyle.equals(ST_DENSITY)) {
				while (itr.hasNext()) {
					Cluster cluster = (Cluster) itr.next();
					printMatrix[(k - (k / colNumber) * colNumber) % colNumber][k
							/ colNumber] = cluster.size();
					max = Math.max(max, cluster.size());
					k++;
				}
				min = 0.0;
				// painting Matrix
				for (int i = 0; i < colNumber; i++) {
					for (int j = 0; j < rowNumber; j++) {
						interpolateRect(buffer, width * i, height * j, width,
								height, printMatrix, i, j, max, min,
								new SOMLandscapeColorizer());
					}
				}
			}
			// U-matrix
			else {
				for (int i = 0; i < colNumber; i++) {
					for (int j = 0; j < rowNumber; j++) {
						max = Math.max(max, uMatrix[i][j]);
						min = Math.min(min, uMatrix[i][j]);
					}
				}
				// painting Matrix
				for (int i = 0; i < colNumber; i++) {
					for (int j = 0; j < rowNumber; j++) {
						interpolateRect(buffer, width * i, height * j, width,
								height, uMatrix, i, j, max, min,
								new SOMLandscapeColorizer());
					}
				}
			}
		}
		// painting axis lines
		gBuf.setColor(Color.GRAY);
		for (int i = 1; i < colNumber; i++) {
			gBuf.drawLine(width * i, 0, width * i, imageHeight);
		}
		for (int i = 1; i < rowNumber; i++) {
			gBuf.drawLine(0, height * i, imageWidth, height * i);
		}
	}

	private void interpolateRect(BufferedImage image, int posX, int posY,
			double width, double height, double[][] matrix, int matrixX,
			int matrixY, double colorScale, double colorMin,
			SOMLandscapeColorizer colorizer) {

		double p11 = matrix[matrixX][matrixY];
		double p21 = matrix[(matrixX + 1) % colNumber][matrixY];
		double p12 = matrix[matrixX][(matrixY + 1) % rowNumber];
		double p22 = matrix[(matrixX + 1) % colNumber][(matrixY + 1)
				% rowNumber];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double interpolatedValue = ((p11 * (width - i) * (height - j))
						+ (p21 * i * (height - j)) + (p12 * (width - i) * j) + (p22
						* i * j))
						/ (height * width);
				// double colorValue = interpolatedValue / colorScale;

				double colorValue = (interpolatedValue - colorMin)
						/ (colorScale - colorMin);
				colorValue = Math.min(1.0d, Math.max(0.0d, colorValue));
				int rgbColor = colorizer.getPointColor(colorValue).getRGB();
				image.setRGB((int) (posX + i + width / 2) % pixWidth,
						(int) (posY + j + height / 2) % pixHeight, rgbColor);
			}
		}
	}

	public void setScatteredRatio(int aScatteredRatio) {
		scatteredRatio = aScatteredRatio;
	}

	public void setBackground(String str, String str2) {
		backgroundStyle = str;
		colorStyle = str2;
		initBufferedImage();
		repaint();
	}

	protected void initBufferedImage() {
		buffer = null;
	}

}

class SOMLandscapeColorizer {
	private Color[] colors = new Color[] { new Color(0, 0, 255),
			new Color(0, 255, 255), new Color(255, 255, 185),
			new Color(255, 255, 100), new Color(110, 255, 110),
			new Color(0, 170, 0), new Color(255, 195, 105),
			new Color(155, 95, 0), new Color(195, 195, 195),
			new Color(255, 255, 255) };

	private double[] intervalls = new double[] { 0, 0.2, 0.25, 0.6, 0.85, 1 };

	public Color getPointColor(double value) {
		// finding fitting intervall
		int intervall;
		double intervallPosition = 0;
		for (intervall = 0; intervall < 5; intervall++) {
			double lowerBound = intervalls[intervall];
			double upperBound = intervalls[intervall + 1];
			if (value >= lowerBound && value <= upperBound) {
				intervallPosition = (value - lowerBound)
						/ (upperBound - lowerBound);
				break;
			}
		}
		if (intervall >= 5)
			return Color.BLACK;
		// returning linear scaled Color of intervall
		intervall = intervall * 2;
		int redLow = colors[intervall].getRed();
		int redHigh = colors[intervall + 1].getRed();
		int red = (int) (redLow + (redHigh - redLow) * intervallPosition);
		int greenLow = colors[intervall].getGreen();
		int greenHigh = colors[intervall + 1].getGreen();
		int green = (int) (greenLow + (greenHigh - greenLow)
				* intervallPosition);
		int blueLow = colors[intervall].getBlue();
		int blueHigh = colors[intervall + 1].getBlue();
		int blue = (int) (blueLow + (blueHigh - blueLow) * intervallPosition);
		return new Color(red, green, blue);
	}
}
