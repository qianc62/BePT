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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Represents a period of activity of a data-element instance.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class DataElementBlock {
	/**
	 * data element instance to which this block belongs
	 */
	private String dataElement;
	/**
	 * The index of the block
	 */
	private int similarIndex = 0;
	/**
	 * Start Y-coordinate where data element block should be drawn
	 */
	private double startAt = -1;
	/**
	 * End Y-coordinate where data element block should be drawn
	 */
	private double endAt = -1;
	/**
	 *
	 */
	private HashSet inArrows = new HashSet();
	private HashSet outArrows = new HashSet();

	/**
	 * constructor to initialize the block
	 * 
	 * @param dataElement
	 *            String
	 */
	public DataElementBlock(String dataElement) {
		this.dataElement = dataElement;
	}

	// /////////////////////GET AND SET METHODS///////////////////////////
	/**
	 * Returns the name of the data-element of this block
	 * 
	 * @return String
	 */
	public String getDataElement() {
		return dataElement;
	}

	/**
	 * Sets the starting position of this block
	 * 
	 * @param startAt
	 *            double
	 */
	public void setStartAt(double startAt) {
		this.startAt = startAt;
	}

	/**
	 * Sets the end position of this block
	 * 
	 * @param endAt
	 *            double
	 */
	public void setEndAt(double endAt) {
		this.endAt = endAt;
	}

	/**
	 * sets similarIndex
	 * 
	 * @param similarIndex
	 *            int
	 */
	public void setSimilarIndex(int similarIndex) {
		this.similarIndex = similarIndex;
	}

	/**
	 * returns the similarIndex
	 * 
	 * @return int
	 */
	public int getSimilarIndex() {
		return similarIndex;
	}

	/**
	 * Returns the arrows that end in this block
	 * 
	 * @return HashSet
	 */
	public HashSet getInArrows() {
		return inArrows;
	}

	/**
	 * Sets the arrows that end in this block
	 * 
	 * @param inArrows
	 *            HashSet
	 */
	public void setInArrows(HashSet inArrows) {
		this.inArrows = inArrows;
	}

	/**
	 * Returns the arrows that originate from this block
	 * 
	 * @return HashSet
	 */
	public HashSet getOutArrows() {
		return outArrows;
	}

	/**
	 * Sets the arrows that originate from this block
	 * 
	 * @param outArrows
	 *            HashSet
	 */
	public void setOutArrows(HashSet outArrows) {
		this.outArrows = outArrows;
	}

	// //////////////////////Comparison methods//////////////////////
	/**
	 * Returns true if point p is in this block
	 * 
	 * @param p
	 *            Point
	 * @param lifeLines
	 *            HashMap
	 * @param scale
	 *            double
	 * @return boolean
	 */
	public boolean isInBlock(Point p, HashMap lifeLines, double scale) {
		try {
			LifeLine eltLine = (LifeLine) lifeLines.get(dataElement);
			double xMiddle = eltLine.getMiddle();
			if (p.getX() >= Math.round((xMiddle - 10) * scale)
					&& p.getX() <= Math.round((xMiddle + 10) * scale)
					&& p.getY() >= Math.round(startAt * scale)
					&& p.getY() <= Math.round(endAt * scale)) {
				return true;
			} else {
				return false;
			}
		} catch (NullPointerException ex) {
			return false;
		}
	}

	// ///////////////////Draw Method/////////////////////////
	/**
	 * Draws the data-element block on top of the the lifeline
	 * 
	 * @param startX
	 *            double
	 * @param thisColor
	 *            Color
	 * @param g
	 *            Graphics2D
	 */
	public void drawBlock(double startX, Color thisColor, Graphics2D g) {
		Rectangle2D r = new Rectangle2D.Double(startX, startAt, 20, endAt
				- startAt);
		Paint initialPaint = g.getPaint();
		GradientPaint towhite = new GradientPaint(((Double) startX)
				.floatValue(), ((Double) startAt).floatValue(), thisColor,
				((Double) startX).floatValue() + 20, ((Double) startAt)
						.floatValue(), Color.WHITE);
		g.setPaint(towhite);
		g.fill(r);
		g.setPaint(initialPaint);
		g.setColor(Color.BLACK);
		g.draw(r);
	}

}
