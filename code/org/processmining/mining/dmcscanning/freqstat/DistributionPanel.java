/*
 * Created on Jun 17, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning.freqstat;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class DistributionPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int values[] = {};
	protected int maxVal = 0;
	protected int xCoords[] = {};
	protected int curveFillXCoords[] = {};
	protected int curveFillYCoords[] = {};

	protected Color bgColor = null;
	protected Color curveColor = null;
	protected Color curveFillColor = null;
	protected Color gridColor = null;

	protected boolean prepared = false;

	public DistributionPanel(int distValues[], int maxValue) {
		prepared = false;
		values = distValues;
		maxVal = maxValue;
		bgColor = new Color(220, 220, 200);
		curveColor = new Color(200, 50, 50);
		curveFillColor = new Color(220, 100, 100);
		gridColor = new Color(120, 120, 100);
		doTheMath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (prepared == false) {
			doTheMath();
		}
		int width = (int) this.getSize().getWidth();
		int height = (int) this.getSize().getHeight();
		if (this.isOpaque()) {
			g.setColor(bgColor);
			g.drawRect(0, 0, width, height);
		}
		// draw curve fill
		g.setColor(curveFillColor);
		g.drawPolygon(curveFillXCoords, curveFillYCoords,
				curveFillXCoords.length);
		// draw grid
		g.setColor(gridColor);
		for (int x = 0; x < width; x += 10) {
			g.drawLine(x, 0, x, height);
		}
		for (int y = 0; y < height; y += 10) {
			g.drawLine(0, y, width, y);
		}
		// draw curve line
		g.setColor(curveColor);
		g.drawPolyline(xCoords, values, values.length);
	}

	protected void doTheMath() {
		adjustValues();
		xCoords = new int[values.length];
		curveFillXCoords = new int[values.length + 2];
		for (int i = 0; i < values.length; i++) {
			xCoords[i] = (int) (((double) (i) / (double) values.length) * this
					.getWidth());
			curveFillXCoords[i + 1] = xCoords[i];
		}
		curveFillXCoords[0] = curveFillXCoords[1];
		curveFillXCoords[values.length + 1] = curveFillXCoords[values.length];
		curveFillYCoords = new int[values.length + 2];
		curveFillYCoords[0] = this.getHeight();
		curveFillYCoords[values.length + 1] = this.getHeight();
		for (int j = 0; j < values.length; j++) {
			curveFillYCoords[j + 1] = values[j];
		}
		prepared = true;
	}

	protected void adjustValues() {
		for (int i = 0; i < values.length; i++) {
			values[i] = (this.getHeight() - (int) (((double) values[i] / (double) maxVal) * this
					.getHeight()));
		}
	}
}
