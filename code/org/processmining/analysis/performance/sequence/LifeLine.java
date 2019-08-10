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

package org.processmining.analysis.performance.sequence;

import java.awt.Stroke;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

/**
 * Class that represents the lifeline of a data-element instance.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 * @version 1.0
 */
public class LifeLine {
	/**
	 * name of the data-element of this lifeline
	 */
	private String dataElt = "";
	/**
	 * Starting position of the lifeline
	 */
	private int beginPosition = 0;
	/**
	 * height of the lifeline
	 */
	private double height = 0;
	/**
	 * middle point of the lifeline
	 */
	private double middle = 0;
	/**
	 * width of the lifeline
	 */
	private double width = 0;

	/**
	 * Constructor to initialize the lifeline
	 * 
	 * @param dataElt
	 *            String
	 * @param height
	 *            double
	 */
	public LifeLine(String dataElt, double height) {
		this.dataElt = dataElt;
		this.height = height;
	}

	// //////////////////GET AND SET METHODS////////////////////
	public String getElt() {
		return dataElt;
	}

	/**
	 * Returns the width of the data-element box
	 * 
	 * @return double
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Returns the x-coordinate of the middle position of the data-element box
	 * 
	 * @return double
	 */
	public double getMiddle() {
		return middle;
	}

	/**
	 * Sets the begin position
	 * 
	 * @param beginPosition
	 *            int
	 */
	public void setBeginPosition(int beginPosition) {
		this.beginPosition = beginPosition;
	}

	// //////////////DRAW METHOD////////////////////////
	/**
	 * Draws the name of a data-element in a box and draws a life line from the
	 * middle of this rectangle to the bottom of the sequence diagram panel.
	 * 
	 * @param g
	 *            Graphics2D
	 * @param scale
	 *            double
	 */
	public void drawLifeLine(Graphics2D g, double scale) {
		// draw the name of the data element
		g.drawString(dataElt, beginPosition + 15, 30);
		FontMetrics fm = g.getFontMetrics();
		// obtain width and middle of the drawn string
		Rectangle2D r2 = fm.getStringBounds(dataElt, g);
		width = r2.getWidth() + 30;
		middle = beginPosition + width * 0.5;
		// draw rectangle around the name
		Rectangle2D r = new Rectangle2D.Double(beginPosition, 10, width, 30);
		g.draw(r);
		Stroke initialStroke = g.getStroke();
		// set stroke to dashed
		Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 12, 12 }, 0);
		g.setStroke(stroke);
		// draw dashed life line going down from the middle of the rectangle to
		// the bottom of the panel
		Line2D l = new Line2D.Double((beginPosition + width * 0.5), 40,
				(beginPosition + width * 0.5), height / scale);
		g.draw(l);
		// set stroke back to normal
		g.setStroke(initialStroke);
	}
}
