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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.HashMap;

/**
 * Represents the transfer of work between two data-element instances.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class Arrow {
	/**
	 * Name of data-element that is the source of this arrow
	 */
	private String source;
	private DataElementBlock sourceBlock;
	/**
	 * Name of data-element that is the source of this arrow
	 */
	private String destination;
	private DataElementBlock destinationBlock;
	/**
	 * Start position at which this arrow is to be drawn
	 */
	private double startAt = -1;
	/**
	 * End position at which this arrow is to be drawn
	 */
	private double endAt = -1;

	/**
	 * constructor to initialize arrow
	 * 
	 * @param source
	 *            String
	 * @param destination
	 *            String
	 */
	public Arrow(String source, String destination) {
		this.source = source;
		this.destination = destination;
	}

	// ///////////////////Get Methods//////////////////////////
	/**
	 * Returns the name of the data element instance from which the arrow
	 * originates.
	 * 
	 * @return String
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Returns the name of the data element instance in which the arrow ends.
	 * 
	 * @return String
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * Returns the starting position of the arrow
	 * 
	 * @return double
	 */
	public double getStartAt() {
		return startAt;
	}

	/**
	 * Returns the end position of the arrow
	 * 
	 * @return double
	 */
	public double getEndAt() {
		return endAt;
	}

	/**
	 * Sets the starting position of the arrow.
	 * 
	 * @param startAt
	 *            double
	 */
	public void setStartAt(double startAt) {
		this.startAt = startAt;
	}

	/**
	 * Sets the end position of the arrow.
	 * 
	 * @param endAt
	 *            double
	 */
	public void setEndAt(double endAt) {
		this.endAt = endAt;
	}

	/**
	 * Returns the block from which the arrow originates.
	 * 
	 * @return DataElementBlock
	 */
	public DataElementBlock getSourceBlock() {
		return sourceBlock;
	}

	/**
	 * Sets the block from which the arrow originates to sourceBlock
	 * 
	 * @param sourceBlock
	 *            DataElementBlock
	 */
	public void setSourceBlock(DataElementBlock sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	/**
	 * Returns the block in which the arrow ends.
	 * 
	 * @return DataElementBlock
	 */
	public DataElementBlock getDestinationBlock() {
		return destinationBlock;
	}

	/**
	 * Sets the block in which the arrow ends to destinationBlock
	 * 
	 * @param destinationBlock
	 *            DataElementBlock
	 */
	public void setDestinationBlock(DataElementBlock destinationBlock) {
		this.destinationBlock = destinationBlock;
	}

	// ///////////////////Draw method///////////////////
	/**
	 * Draws the arrow
	 * 
	 * @param lifeLines
	 *            HashMap
	 * @param thisColor
	 *            Color
	 * @param g
	 *            Graphics2D
	 */
	public void drawArrow(HashMap lifeLines, Color thisColor, Graphics2D g) {
		LifeLine sourceLine = (LifeLine) lifeLines.get(source);
		LifeLine destLine = (LifeLine) lifeLines.get(destination);
		if (sourceLine != null && destLine != null) {
			double startX = sourceLine.getMiddle();
			double endX = destLine.getMiddle();
			if (startX < endX) {
				startX = startX + 10;
				endX = endX - 10;
			} else {
				startX = startX - 10;
				endX = endX + 10;
			}
			g.setColor(thisColor);
			Line2D l = new Line2D.Double(startX, startAt, endX, endAt);
			g.draw(l);
			// tangens of the angle is opposite/adjacent
			double tanAngle = Math.abs((endAt - startAt) / (endX - startX));
			double angle = Math.atan(tanAngle);
			angle += Math.PI / 4.0; // 45 degrees between arrow and each
			// 'arrow-end-line'
			double length = Math.sqrt(50);
			double xFirst = endX;
			double yFirst = endAt;
			if (endX > startX) {
				// arrow from left to right
				if (angle < Math.PI / 2.0) {
					xFirst -= Math.cos(angle) * length; // cosinus =
					// adjacent/hypotenuse
					yFirst -= Math.sin(angle) * length; // sinus =
					// opposite/hypotenuse
				} else {
					// angle larger than 90 degrees;
					angle = Math.PI - angle;
					xFirst += Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				}
				l = new Line2D.Double(xFirst, yFirst, endX, endAt);
				g.draw(l);
				angle = Math.atan(tanAngle) - Math.PI / 4.0; // 45 degrees
				// between arrow
				// and each
				// 'arrow-end-line'
				xFirst = endX;
				yFirst = endAt;
				if (angle >= 0) {
					xFirst -= Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				} else {
					angle = -angle;
					xFirst -= Math.cos(angle) * length;
					yFirst += Math.sin(angle) * length;
				}
				l = new Line2D.Double(xFirst, yFirst, endX, endAt);
				g.draw(l);
			} else {
				// startX > endX, arrow from right to left
				if (angle < Math.PI / 2.0) {
					xFirst += Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				} else {
					angle = Math.PI - angle;
					xFirst -= Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				}
				l = new Line2D.Double(xFirst, yFirst, endX, endAt);
				g.draw(l);
				angle = Math.atan(tanAngle) - Math.PI / 4.0; // 45 degrees
				// between arrow
				// and each
				// 'arrow-end-line'
				length = Math.sqrt(50);
				xFirst = endX;
				yFirst = endAt;
				if (angle >= 0) {
					xFirst += Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				} else {
					angle = Math.abs(angle);
					xFirst += Math.cos(angle) * length;
					yFirst += Math.sin(angle) * length;
				}
				l = new Line2D.Double(xFirst, yFirst, endX, endAt);
				g.draw(l);
			}
		}
	}

	// ///////////////////Comparison methods//////////////////////
	/**
	 * Returns true if point p is on the line of the arrow (or at most 2 pixels
	 * away)
	 * 
	 * @param p
	 *            Point
	 * @param lifeLines
	 *            HashMap
	 * @param scale
	 *            double
	 * @return boolean
	 */
	public boolean isOnLine(Point p, HashMap lifeLines, double scale) {
		LifeLine sourceLine = (LifeLine) lifeLines.get(source);
		LifeLine destLine = (LifeLine) lifeLines.get(destination);
		if (sourceLine != null && destLine != null) {
			double startX = sourceLine.getMiddle();
			double endX = destLine.getMiddle();
			if (startX < endX) {
				startX = startX + 10;
				endX = endX - 10;
			} else {
				startX = startX - 10;
				endX = endX + 10;
			}
			Line2D l = new Line2D.Double(Math.round(startX * scale), Math
					.round(startAt * scale), Math.round(endX * scale), Math
					.round(endAt * scale));
			if (Math.abs(l.ptSegDist(p)) <= 2) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

}
