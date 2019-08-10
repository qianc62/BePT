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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

/**
 * Class needed to draw sequence diagrams
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class SequenceDiagram extends JPanel implements MouseMotionListener {
	/**
	 * boolean that is set to true after initialization has taken place
	 */
	private boolean paintDiagram = false;
	/**
	 * The scale at which the paint diagram is to be drawn
	 */
	private float scale = 1.0F;
	/**
	 * The time divider
	 */
	private long timeDivider = 1000;
	/**
	 * The time per pixel (milliseconds)
	 */
	private double timePerPixel = 0;
	/**
	 * The list of data-elements in the diagram
	 */
	private ArrayList dataElts = new ArrayList();;
	/**
	 * The sequences to be drawn in the diagram
	 */
	private ArrayList sequences = new ArrayList();
	/**
	 * The sequences that are currently visible
	 */
	private HashSet visibleSequences = new HashSet();
	/**
	 * map that maps data-element name to the corresponding lifeLine
	 */
	private HashMap lifeLines = new HashMap();
	/**
	 * The date at which the first sequence starts
	 */
	private Date firstDate;
	/**
	 * The date at which the last sequence ends
	 */
	// private Date lastDate;
	/**
	 * Height of the sequence diagram
	 */
	private int height = 800;
	/**
	 * Variable to check whether tooltips should be displayed or not
	 */
	private boolean tooltipsOn = true;
	private double pixelsBetween = 0;

	/**
	 * Constructor (does nothing)
	 */
	public SequenceDiagram() {
	}

	/**
	 * Initializes the sequence diagram
	 * 
	 * @param sequences
	 *            ArrayList
	 * @param dataElts
	 *            ArrayList
	 * @param timeSort
	 *            String
	 * @param timeDivider
	 *            long
	 * @param scale
	 *            float
	 * @param beginDate
	 *            Date
	 * @param duration
	 *            double
	 */
	public void initialize(ArrayList sequences, ArrayList dataElts,
			String timeSort, long timeDivider, float scale, Date beginDate,
			double duration) {
		paintDiagram = false;
		this.sequences = sequences;
		this.dataElts = dataElts;
		this.timeDivider = timeDivider;
		this.scale = scale;
		this.addMouseMotionListener(this);
		initializePaint(beginDate, duration);
		paintDiagram = true;
	}

	/**
	 * Initializes paint (moved out of paint to increase interactiveness)
	 * 
	 * @param beginDate
	 *            Date
	 * @param duration
	 *            double
	 */
	public void initializePaint(Date beginDate, double duration) {
		firstDate = beginDate;
		lifeLines.clear();
		Insets i = getInsets();
		// obtain panel height
		height = getHeight() - i.top - i.bottom;
		Iterator it = dataElts.iterator();
		// run through data-elements
		while (it.hasNext()) {
			String elt = (String) it.next();
			// create a life line for each data-element
			LifeLine lifeLine = new LifeLine(elt, height);
			lifeLines.put(elt, lifeLine);
		}
		int height = Math.max(sequences.size() * 20, 800);
		if (timePerPixel == 0) {
			timePerPixel = duration / height;
		}
		// run through sequences once more, to initialize drawing
		for (int index = 0; index < sequences.size(); index++) {
			Sequence current = (Sequence) sequences.get(index);
			current.initializeDrawSequence(timePerPixel, firstDate);
		}
	}

	/**
	 * Actually paints the Sequence diagram
	 * 
	 * @param g
	 *            Graphics
	 */
	public void paintComponent(Graphics g) {
		// paint the panel first
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		// scale the graphics
		g2d.scale(scale, scale);
		// clear set of visible sequences
		visibleSequences.clear();
		if (paintDiagram) {
			// diagram has to be painted
			Iterator it = dataElts.iterator();
			int extra = 60;
			// run through data elements/life lines
			while (it.hasNext()) {
				String elt = (String) it.next();
				try {
					LifeLine lifeLine = (LifeLine) lifeLines.get(elt);
					// set begin position of the life line
					lifeLine.setBeginPosition(extra);
					// draw the life line and the data-element blocks on it
					lifeLine.drawLifeLine(g2d, scale);
					// adjust extra to the width of the (box of) the life line
					extra += lifeLine.getWidth() + 20;
				} catch (NullPointerException npe) {
					// can occur if lifeLines does not have a lifeline with elt,
					// though should not occur if called properly
				}
			}
			// obtain visible rectangle
			Rectangle visible = this.getVisibleRect();
			ListIterator lit = sequences.listIterator();
			while (lit.hasNext()) {
				Sequence current = (Sequence) lit.next();
				if (visible.getMinY() <= current.getEndY() * scale
						&& (current.getStartY() - 5) * scale <= visible
								.getMaxY()) {
					// draw the sequence only if it is in the visible rectangle
					current.drawSequence(lifeLines, g2d);
					// add current sequence to set of visible sequences
					visibleSequences.add(current);
				}
			}
			// draw time-axis if real-time has been selected by the user
			drawTimeAxis(g2d, height, firstDate);
		}
	}

	/**
	 * Draws a time axis left of the Sequence Diagram
	 * 
	 * @param g
	 *            Graphics2D
	 * @param height
	 *            int
	 * @param beginDate
	 *            Date
	 */
	private void drawTimeAxis(Graphics2D g, int height, Date beginDate) {
		// draw the first date
		g.setColor(Color.BLACK);
		g.setFont(new Font("SansSerif", Font.PLAIN, 10));
		if (beginDate != null) {
			g.drawString(beginDate.toString(), 2, 55);
		}
		int newHeight = ((Float) (height / scale)).intValue();// Math.round(Math.max(height,
		// height /
		// scale));
		// obtain step-size on axis (time) and distance in pixels of a step
		ArrayList stepList = obtainStep(new BigDecimal(1), timePerPixel,
				Double.MAX_VALUE);
		// step-size
		BigDecimal step = (BigDecimal) stepList.get(1);
		// distance
		pixelsBetween = (((Double) stepList.get(0)).doubleValue() + 25);
		// draw time-axis
		Line2D l = new Line2D.Double(5, 60, 5, newHeight);
		g.draw(l);
		int num = 0;
		for (double i = 60; i < newHeight; i += pixelsBetween) {
			// draw a line after every step
			float temp = ((Double) i).floatValue();
			Line2D sl = new Line2D.Double(5, temp, 10, temp);
			g.draw(sl);
			if (num != 0) {
				// draw a string at each line with the time
				g.drawString("+ " + step.multiply(new BigDecimal(num)), 13,
						temp + 4);
			}
			num++;
		}
	}

	/**
	 * Returns the best step size for the time-axis and the number of pixels of
	 * a step
	 * 
	 * @param n
	 *            BigDecimal
	 * @param timePerPixel
	 *            double
	 * @param distance
	 *            double
	 * @return ArrayList
	 */
	private ArrayList obtainStep(BigDecimal n, double timePerPixel,
			double distance) {
		ArrayList biggerList = obtainStepBiggerOne(n, timePerPixel, distance);
		if (((BigDecimal) biggerList.get(1)).doubleValue() > 1) {
			return biggerList;
		} else {
			ArrayList smallerList = obtainStepSmallerOne(n, timePerPixel,
					distance);
			return smallerList;
		}
	}

	/**
	 * Returns the best step size for the time-axis and the number of pixels of
	 * a step Obtained by recursion.
	 * 
	 * @param n
	 *            BigDecimal
	 * @param timePerPixel
	 *            double
	 * @param distance
	 *            double
	 * @return ArrayList
	 */
	private ArrayList obtainStepBiggerOne(BigDecimal n, double timePerPixel,
			double distance) {
		ArrayList stepList = new ArrayList();
		BigDecimal i = n;
		// time Per pixel is in milliseconds
		double dist = n.doubleValue() * timeDivider / timePerPixel - 25;
		double dist2 = 5 * n.doubleValue() * timeDivider / timePerPixel - 25;
		if (Math.abs(dist) > Math.abs(dist2)) {
			dist = dist2;
			i = n.multiply(new BigDecimal(5));
		}
		ArrayList list3 = new ArrayList();
		double dist3 = Double.MAX_VALUE;
		// check if the current distance is smaller than the one with which this
		// method was called
		if (Math.abs(distance) >= Math.abs(dist)) {
			// if so, call this method with n=(n*10) and distance = dist
			list3 = obtainStepBiggerOne(n.multiply(new BigDecimal(10)),
					timePerPixel, dist);
			dist3 = ((Double) list3.get(0)).doubleValue();
		}
		// check if the next distance is smaller than the current
		if (Math.abs(dist3) < Math.abs(dist) && list3 != null
				&& list3.size() > 0) {
			// if so, set distance to the next distance, and i to the stepsize
			// there
			dist = dist3;
			i = (BigDecimal) list3.get(1);
		}
		stepList.add(dist);
		stepList.add(i);
		return stepList;
	}

	/**
	 * Returns the best step size for the time-axis and the number of pixels of
	 * a step Obtained by recursion.
	 * 
	 * @param n
	 *            BigDecimal
	 * @param timePerPixel
	 *            double
	 * @param distance
	 *            double
	 * @return ArrayList
	 */
	private ArrayList obtainStepSmallerOne(BigDecimal n, double timePerPixel,
			double distance) {
		ArrayList stepList = new ArrayList();
		BigDecimal i = n;
		// time Per pixel is in milliseconds
		double dist = n.doubleValue() * timeDivider / timePerPixel - 25;
		double dist2 = 5 * n.doubleValue() * timeDivider / timePerPixel - 25;
		if (Math.abs(dist) > Math.abs(dist2)) {
			dist = dist2;
			i = n.multiply(new BigDecimal(5));
		}
		ArrayList list3 = new ArrayList();
		double dist3 = Double.MAX_VALUE;
		// check if the current distance is smaller than the one with which this
		// method was called
		if (Math.abs(distance) >= Math.abs(dist)) {
			// if so, call this method with n=(n/10) and distance = dist
			list3 = obtainStepSmallerOne(n.divide(new BigDecimal(10.0)),
					timePerPixel, dist);
			dist3 = ((Double) list3.get(0)).doubleValue();
		}
		// check if the next distance is smaller than the current
		if (Math.abs(dist3) < Math.abs(dist) && list3 != null
				&& list3.size() > 0) {
			// if so, set current distance to the next distance, and i to the
			// stepsize there
			dist = dist3;
			i = (BigDecimal) list3.get(1);
		}
		stepList.add(dist);
		stepList.add(i);
		return stepList;
	}

	// ////////////////////MOUSE LISTENER AND TOOLTIP METHODS/////////////////
	/**
	 * Required for mouselistener
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * Shows information in a tooltip about the sequence over which the mouse
	 * moved.
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		if (visibleSequences.size() > 0 && tooltipsOn) {
			String str = "<html>";
			Iterator it = visibleSequences.iterator();
			while (it.hasNext()) {
				Sequence seq = (Sequence) it.next();
				boolean onSequence = false;
				// only check the parts and arrows of those sequences in which
				// range p lies
				if (Math.round(seq.getStartY() * scale) <= p.getY() + 2
						&& p.getY() - 2 <= Math.round(seq.getEndY() * scale)) {
					ArrayList sortedDataElementBlocks = seq
							.getSortedDataEltBlocks();
					for (int i = 0; i < sortedDataElementBlocks.size()
							&& !onSequence; i++) {
						SequenceBlock block = (SequenceBlock) sortedDataElementBlocks
								.get(i);
						if (block.isInBlock(p, lifeLines, scale)) {
							str += "Process instance: "
									+ seq.getPiName()
									+ "&nbsp&nbsp&nbsp |&nbsp&nbsp&nbsp Throughput time:"
									+ seq.getThroughputTime()
									/ timeDivider
									+ "&nbsp&nbsp&nbsp |&nbsp&nbsp&nbsp Time in data-element block: "
									+ block.getTimeIn() / timeDivider + "<br>";
							onSequence = true;
						}
					}
					if (!onSequence) {
						// check if mouse cursor is on an arrow of the sequence
						ArrayList arrowList = seq.getArrowList();
						for (int i = 0; i < arrowList.size(); i++) {
							SequenceArrow arrow = (SequenceArrow) arrowList
									.get(i);
							if (arrow.isOnLine(p, lifeLines, scale)) {
								str += "Process instance: "
										+ seq.getPiName()
										+ "&nbsp&nbsp&nbsp |&nbsp&nbsp&nbsp Throughput time:"
										+ seq.getThroughputTime()
										/ timeDivider
										+ "&nbsp&nbsp&nbsp |&nbsp&nbsp&nbsp Time between "
										+ arrow.getSource()
										+ " and "
										+ arrow.getDestination()
										+ ": "
										+ ((arrow.getEndTimestamp().getTime() - arrow
												.getBeginTimestamp().getTime()) / timeDivider)
										+ "<br>";
								onSequence = true;
							}
						}
					}
				}
			}
			str += "</html>";
			this.setToolTipText(str);
		}
	}

	public double getTimePerPixel() {
		return timePerPixel;
	}

	public void setTimePerPixel(double tpp) {
		timePerPixel = tpp;
	}

	/**
	 * Sets whether tooltips should be displayed or not
	 * 
	 * @param tooltipsOn
	 *            boolean
	 */
	public void setTooltipsOn(boolean tooltipsOn) {
		this.tooltipsOn = tooltipsOn;
	}

	/**
	 * Overrides standard setToolTipText of JComponent, to make sure the tooltip
	 * text is displayed long enough.
	 * 
	 * @param text
	 *            String
	 */
	public void setToolTipText(String text) {
		String oldText = getToolTipText();
		putClientProperty(TOOL_TIP_TEXT_KEY, text);
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		// set dismiss delay to 50 seconds
		toolTipManager.setDismissDelay(50000);
		if (text != null) {
			if (oldText == null) {
				toolTipManager.registerComponent(this);
			}
		} else {
			toolTipManager.unregisterComponent(this);
		}
	}
}
