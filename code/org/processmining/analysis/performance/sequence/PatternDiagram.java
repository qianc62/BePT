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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

/**
 * Represents a pattern diagram.
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class PatternDiagram extends JPanel implements MouseMotionListener {
	/**
	 * boolean that is set to true after initialization has taken place
	 */
	private boolean paintDiagram = false;
	/**
	 * The scale at which the paint diagram is to be drawn
	 */
	private float scale = 1.0F;
	/**
	 * The timedivider
	 */
	private long timeDivider = 1000;
	/**
	 * The time per pixel (milliseconds)
	 */
	private double timePerPixel = 0;
	/**
	 * The set of data-elements in the diagram
	 */
	private ArrayList dataElts = new ArrayList();
	/**
	 * map in which the key is frequence of occurence of patterns and value the
	 * set of patterns in the diagram that have this frequency
	 */
	private HashMap patterns = new HashMap();
	/**
	 * map that maps data-element to the corresponding lifeLine
	 */
	private HashMap lifeLines = new HashMap();
	/**
	 * The patterns that are currently visible
	 */
	private HashSet visiblePatterns = new HashSet();
	/**
	 * array containing the frequencies of occurrence of patterns
	 */
	private int[] sortedArray;
	/**
	 * Variable to check whether tooltips should be displayed or not
	 */
	private boolean tooltipsOn = true;

	/**
	 * Constructor (does nothing)
	 */
	public PatternDiagram() {
	}

	/**
	 * Returns a sorted array, containing the (sorted) frequencies of the
	 * patterns in the diagram
	 * 
	 * @return int[]
	 */
	public int[] getSortedArray() {
		return sortedArray;
	}

	// //////////////////DRAW-RELATED METHODS//////////////////////
	/**
	 * Initializes drawing, so the paintComponent method can draw easily
	 * 
	 * @param patterns
	 *            HashMap : map in which the key is frequence of occurence of
	 *            patterns and value the set of patterns in the diagram that
	 *            have this frequency
	 * @param dataElts
	 *            ArrayList : list containing the data-elements in this diagram
	 * @param timeDivider
	 *            long : the timedivider used
	 * @param scale
	 *            float : the scale at which the diagram is to be drawn
	 * @param timePerPixel
	 *            double : the time per pixel
	 */
	public void initializePaint(HashMap patterns, ArrayList dataElts,
			long timeDivider, float scale, double timePerPixel) {
		this.patterns = patterns;
		this.dataElts = dataElts;
		this.timeDivider = timeDivider;
		this.scale = scale;
		this.timePerPixel = timePerPixel;
		this.addMouseMotionListener(this);
		paintDiagram = true;
		sortedArray = new int[patterns.size()];
		Iterator keys = patterns.keySet().iterator();
		int index = 0;
		while (keys.hasNext()) {
			// place all keys in sortedArray
			int key = (Integer) keys.next();
			sortedArray[index++] = key;// Integer.parseInt(tempStr)
		}
		// sortedArray is not sorted until now
		Arrays.sort(sortedArray);

		lifeLines.clear();
		Insets i = getInsets();
		// obtain panel height
		int height = getHeight() - i.top - i.bottom;
		Iterator it = dataElts.iterator();
		// run through data-elements
		while (it.hasNext()) {
			String elt = (String) it.next();
			// create a life line for each data-element
			LifeLine lifeLine = new LifeLine(elt, height);
			lifeLines.put(elt, lifeLine);
		}
		// run through patterns to initialize drawing
		int startY = 60;
		int patternNumber = 0;
		for (int j = sortedArray.length - 1; j >= 0; j--) {
			// get the set of patterns that have a certain frequency
			HashSet pats = (HashSet) patterns.get(sortedArray[j]);
			Iterator itr = pats.iterator();
			while (itr.hasNext()) {
				// get each pattern of the set of patterns
				Pattern current = (Pattern) itr.next();
				// initialize drawing of the pattern
				current.initializeDrawPattern(patternNumber++, startY,
						timePerPixel);
				startY += current.getMeanThroughputTime() / timePerPixel + 50;
			}
		}
	}

	/**
	 * Actually paints the Pattern diagram
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
		// clear set of visible patterns;
		visiblePatterns.clear();
		if (paintDiagram) {
			// diagram is to be painted
			int extra = 60;
			Iterator it = dataElts.iterator();
			while (it.hasNext()) {
				try {
					LifeLine lifeLine = (LifeLine) lifeLines.get((String) it
							.next());
					lifeLine.setBeginPosition(extra);
					lifeLine.drawLifeLine(g2d, scale);
					// store mid x-coordinate of the data element in
					// eltLocations
					extra += lifeLine.getWidth() + 20;
				} catch (NullPointerException ex) {
					// can occur if no lifeLine exists with the name of the
					// current data-element
					// though should not occur, since there should be a lifeline
					// for every data-element
				}
			}
			int startY = 60;
			int patternNumber = 0;
			Rectangle visible = this.getVisibleRect();
			for (int j = sortedArray.length - 1; j >= 0; j--) {
				// get the set of patterns that have a certain frequency
				HashSet pats = (HashSet) patterns.get(sortedArray[j]);
				Iterator itr = pats.iterator();
				while (itr.hasNext()) {
					// get each pattern of the set of patterns until done
					// drawing
					Pattern current = (Pattern) itr.next();
					current.setPatternNumber(patternNumber++);
					int endY = startY;
					endY += current.getMeanThroughputTime() / timePerPixel + 50;
					if (visible.getMinY() <= endY * scale
							&& (startY - 5) * scale <= visible.getMaxY()) {
						// draw the pattern only if it is in the visible
						// rectangle
						current.drawPattern(lifeLines, startY, g2d);
						visiblePatterns.add(current);
					}
					startY = endY;
				}
			}
		}
	}

	public double getTimePerPixel() {
		return timePerPixel;
	}

	public void setTimePerPixel(double tpp) {
		timePerPixel = tpp;
	}

	// //////////////////////MOUSELISTENER AND TOOLTIP
	// METHODS/////////////////////////
	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * Check if the cursor is on a data-element block or on an arrows, if so
	 * display, information about the pattern that the arrow or data-element
	 * belongs to
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		if (patterns.size() > 0 && tooltipsOn) {
			String str = "<html>";
			boolean onPattern = false;
			Iterator itr = visiblePatterns.iterator();
			while (itr.hasNext() && !onPattern) {
				Pattern pattern = (Pattern) itr.next();
				ArrayList sortedDataElementBlocks = pattern
						.getSortedDataElementBlocks();
				for (int i = 0; i < sortedDataElementBlocks.size(); i++) {
					PatternBlock block = (PatternBlock) sortedDataElementBlocks
							.get(i);
					if (block.isInBlock(p, lifeLines, scale)) {
						str += "Pattern "
								+ pattern.getPatternNumber()
								+ ":&nbsp&nbsp&nbsp Mean throughput time:"
								+ pattern.getMeanThroughputTime()
								/ timeDivider
								+ "&nbsp&nbsp&nbsp |&nbsp&nbsp&nbsp Mean time in data-element block: "
								+ block.getTimeIn() / timeDivider + "<br>";
						onPattern = true;
					}
				}
				ArrayList arrowList = pattern.getArrowList();
				if (!onPattern) {
					boolean onArrow = false;
					// check whether mouse cursor is on an arrow of the pattern
					for (int i = 0; i < arrowList.size(); i++) {
						Arrow arrow = (Arrow) arrowList.get(i);
						if (arrow.isOnLine(p, lifeLines, scale)) {
							// Add arrow to list
							str += "Pattern "
									+ pattern.getPatternNumber()
									+ ":&nbsp&nbsp&nbsp Throughput time:"
									+ pattern.getMeanThroughputTime()
									/ timeDivider
									+ "&nbsp&nbsp&nbsp |&nbsp&nbsp&nbsp Time between "
									+ arrow.getSource()
									+ " and "
									+ arrow.getDestination()
									+ ": "
									+ ((arrow.getEndAt() - arrow.getStartAt()) * timePerPixel)
									/ timeDivider + "<br>";
							onArrow = true;
						}
					}
					onPattern = onArrow;
				}
			}
			str += "</html>";
			this.setToolTipText(str);
		}
	}

	/**
	 * Sets whether tooltips should be on or off
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
