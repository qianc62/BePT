/*
 * Created on May 30, 2005
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
package org.processmining.mining.dmcscanning.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JPanel;

import org.processmining.mining.dmcscanning.logutils.LogItem;

/**
 * LogItemPanel.
 * 
 * The purpose of this class is to provide a view of a log, in form of a
 * horizontal ribbon, with abstract items (that have a start and end time) laid
 * out on it, equipped with handles to distinguish. This class expects
 * SortedSets of LogItems, which are grouped automatically with a specific color
 * and can be removed again bulk-wise. Make sure that the added LogItems fully
 * support the methods to retrieve start and end in temporal fashion, as the
 * implementation of this class depends on that.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class LogItemPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6465991886587018161L;
	/* static member encoding for item handle icons */
	public static String ITEM_HANDLE_NONE = "None";
	public static String ITEM_HANDLE_CIRCLE = "Circle";
	public static String ITEM_HANDLE_TRIANGLE = "Triangle";
	public static String ITEM_HANDLE_BOX = "Box";
	public static String ITEM_HANDLE_RHOMBUS = "Rhombus";

	public static String HANDLE_ICONS[] = { ITEM_HANDLE_NONE,
			ITEM_HANDLE_CIRCLE, ITEM_HANDLE_TRIANGLE, ITEM_HANDLE_BOX,
			ITEM_HANDLE_RHOMBUS };

	/* instance attributes */
	protected HashMap itemSets = null;
	protected Date logBoundaryLeft = null;
	protected Date logBoundaryRight = null;
	protected double milli2pixels = 1.0; // the ratio between milliseconds and
	// displayed pixels
	protected long logLaneStep = 0;
	protected int logLanePixStep = 0;
	protected double viewportZoom = 1.0;
	protected String itemHandle = null;
	protected Color colorBg = null;
	protected Color colorFg = null;
	protected Color colorLogDark = null;
	protected Color colorLogBright = null;
	protected ColorRepository itemColors = null;
	/* size attributes */
	protected int border = 5;
	protected double handleRatio = 0.5;

	/* helpers */
	protected Calendar cal = null;

	/**
	 * constructor
	 * 
	 * @param leftBoundary
	 *            Date representing the left boundary (start) of the log to be
	 *            visualized
	 * @param rightBoundary
	 *            Date representing the right boundary (end) of the log to be
	 *            visualized
	 */
	public LogItemPanel(Date leftBoundary, Date rightBoundary) {
		itemSets = new HashMap();
		logBoundaryLeft = leftBoundary;
		logBoundaryRight = rightBoundary;
		viewportZoom = 1.0;
		border = 10;
		handleRatio = 0.5;
		itemHandle = LogItemPanel.ITEM_HANDLE_CIRCLE;
		colorBg = new Color(240, 240, 220);
		colorFg = new Color(10, 10, 10);
		colorLogDark = new Color(170, 170, 160);
		colorLogBright = new Color(210, 210, 200);
		itemColors = new ColorRepository();
		cal = new GregorianCalendar();
		logLaneStep = 1000;
		logLanePixStep = 200;
		updateMilli2pixelsRatio();
		this.setOpaque(true);
	}

	/**
	 * Adds a SortedSet of LogItems to be displayed as a group of items within
	 * this viewport. They are referenced by a string key, to be able to e.g.
	 * selectively remove groups of items later on.
	 * 
	 * @param key
	 *            reference to this item set, for e.g. later selective removal
	 * @param anItemSet
	 *            expects a sorted set of 'LogItem' instances.
	 * @return the color assigned with this item set
	 */
	public Color addItemSet(String key, SortedSet anItemSet) {
		itemSets.put(key, anItemSet);
		adjustLogBoundaries();
		Color c = itemColors.getColor(key);
		repaint(); // update view
		return c;
	}

	/**
	 * Adds a new LogItem to the set of displayed items within this viewport.
	 * The supplied key is used to group items belonging together (e.g. of one
	 * cluster or process instance).
	 * 
	 * @param key
	 *            identifier used to group related items (by color etc.)
	 * @param item
	 *            the item to be added
	 * @return color used for item (group) display
	 */
	public Color addItem(String key, LogItem item) {
		SortedSet set = null;
		Color color = null;
		if (itemSets.containsKey(key)) {
			// set previously existing, add
			set = (SortedSet) itemSets.get(key);
			set.add(item);
			color = itemColors.getColor(key);
		} else {
			// create and insert new set
			set = new TreeSet();
			set.add(item);
			itemSets.put(key, set);
			color = itemColors.getColor(key);
		}
		adjustLogBoundaries();
		repaint();
		return color;
	}

	/**
	 * Removes the grouped set of LogItems referenced by the supplied key from
	 * the viewport
	 * 
	 * @param key
	 *            reference string for the set to remove
	 * @return success indicator: if false, the set was not contained and thus
	 *         not removed
	 */
	public boolean removeItemSet(String key) {
		if (itemSets.containsKey(key)) {
			itemSets.remove(key);
			itemColors.freeColor(key);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * initializes componente, i.e. all item sets formerly displayed are
	 * removed.
	 */
	public void clearItemSets() {
		itemSets.clear();
	}

	/**
	 * Retrieves the color mapped to an item set
	 * 
	 * @param key
	 *            the key referencing the item set
	 * @return color associated with this key
	 */
	public Color getColor(String key) {
		if (itemSets.containsKey(key)) {
			return itemColors.getColor(key);
		} else {
			return null;
		}
	}

	/**
	 * adjusts the log boundaries to be displayed. Notice: non-checking, care
	 * for sane values!
	 * 
	 * @param aBoundary
	 *            the left log boundary
	 */
	public void setLeftLogBoundary(Date aBoundary) {
		logBoundaryLeft = aBoundary;
		repaint(); // update view
	}

	/**
	 * adjusts the log boundaries to be displayed. Notice: non-checking, care
	 * for sane values!
	 * 
	 * @param aBoundary
	 *            the right log boundary
	 */
	public void setRightLogBoundary(Date aBoundary) {
		logBoundaryRight = aBoundary;
		repaint(); // update view
	}

	/**
	 * adjusts the log boundaries to be displayed. Notice: non-checking, care
	 * for sane values!
	 * 
	 * @param leftBoundary
	 *            the left log boundary
	 * @param rightBoundary
	 *            the right log boundary
	 */
	public void setLogBoundaries(Date leftBoundary, Date rightBoundary) {
		logBoundaryLeft = leftBoundary;
		logBoundaryRight = rightBoundary;
		repaint(); // update view
	}

	/**
	 * @return the left log viewport boundary
	 */
	public Date getLeftLogBoundary() {
		return logBoundaryLeft;
	}

	/**
	 * @return the right log viewport boundary
	 */
	public Date getRightLogBoundary() {
		return logBoundaryRight;
	}

	/**
	 * Updates the internally cached ratio between milliseconds (of events) and
	 * pixels on the screen. Interpretation: displayedYcoordinate = timeInMillis
	 * * milli2pixels;
	 */
	protected void updateMilli2pixelsRatio() {
		milli2pixels = (double) this.getWidth()
				/ (double) (logBoundaryRight.getTime() - logBoundaryLeft
						.getTime());
		if ((milli2pixels == 1) || (milli2pixels == 0)) {
			return;
		}
		logLaneStep = 1000;
		while ((logLaneStep * milli2pixels) < 150) {
			logLaneStep *= 2;
		}
		logLanePixStep = (int) (logLaneStep * milli2pixels) + 1;
	}

	/**
	 * overwritten as proxy, to update mill2pixels ratio
	 */
	public void setSize(int width, int height) {
		super.setSize(width, height);
		updateMilli2pixelsRatio();
		revalidate();
	}

	/**
	 * overwritten as proxy, to update mill2pixels ratio
	 */
	public void setSize(Dimension d) {
		super.setSize(d);
		updateMilli2pixelsRatio();
		revalidate();
	}

	/**
	 * adjusts the viewable are of the log (zoom)
	 * 
	 * @param aZoom
	 *            fraction of the log to be viewable (within (0,1] !)
	 */
	public void setViewportZoom(double aZoom) {
		if (aZoom > 1.0) { // ensure sane values
			viewportZoom = 1.0;
		} else if (aZoom <= 0.0) {
			viewportZoom = 0.00000000001;
		} else {
			viewportZoom = aZoom;
		}
		int updWidth = (int) ((double) getParent().getWidth() * (1.0 / aZoom));
		Dimension dim = new Dimension(updWidth, 10);
		this.setPreferredSize(dim);
		updateMilli2pixelsRatio();
		revalidate();
	}

	/**
	 * @return the fraction of the log that is visible (within (0,1])
	 */
	public double getViewportZoom() {
		return viewportZoom;
	}

	/**
	 * consolidates the global log viewing area. Iterates through all contained
	 * subsets and compares their boundaries to the current log boundaries set.
	 * If necessary, global log boundaries are adjusted to the outside (i.e.,
	 * only extended)
	 */
	protected void adjustLogBoundaries() {
		SortedSet sSet = null;
		Date dStart = null;
		Date dEnd = null;
		for (Iterator it = itemSets.values().iterator(); it.hasNext();) {
			sSet = (SortedSet) it.next();
			dStart = ((LogItem) sSet.first()).getLeftBoundaryTimestamp();
			dEnd = ((LogItem) sSet.last()).getRightBoundaryTimestamp();
			if (logBoundaryLeft.after(dStart)) {
				logBoundaryLeft = dStart;
			}
			if (logBoundaryRight.before(dEnd)) {
				logBoundaryRight = dEnd;
			}
		}
	}

	/**
	 * Sets the item handle visualization type
	 * 
	 * @param anItemHandle
	 *            use one of the static ITEM_HANDLE_* strings provided,
	 *            otherwise it will be a mess
	 */
	public void setItemHandle(String anItemHandle) {
		itemHandle = anItemHandle;
	}

	/**
	 * Sets the uniform border size for the viewport
	 * 
	 * @param aBorder
	 */
	public void setBorder(int aBorder) {
		border = aBorder;
	}

	/**
	 * Sets the ratio between log display and handle view height
	 * 
	 * @param aRatio
	 *            a value within [0.1, 0.9]
	 */
	public void setHandleRatio(double aRatio) {
		if ((aRatio > 0.1) && (aRatio < 0.9)) {
			handleRatio = aRatio;
		}
	}

	/**
	 * @return the height (in pixels) of the item handles pane
	 */
	protected int getHandleHeight() {
		return (int) ((getHeight() - (3 * border)) * handleRatio);
	}

	/**
	 * @return the height (in pixels) of the log display pane
	 */
	protected int getLogPaneHeight() {
		return (int) ((getHeight() - (3 * border)) * (1.0 - handleRatio));
	}

	/**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
	protected int time2coord(Date aTimestamp) {
		return (int) ((aTimestamp.getTime() - logBoundaryLeft.getTime()) * milli2pixels);
	}

	/**
	 * Convenience method. Transforms a timestamp (milliseconds) into the
	 * corresponding horizontal position within the viewport.
	 * 
	 * @param aTimeMillis
	 * @return
	 */
	protected int time2coord(long aTimeMillis) {
		return (int) ((aTimeMillis - logBoundaryLeft.getTime()) * milli2pixels);
	}

	/**
	 * convenience method. transforms a given horizontal coordinate within the
	 * viewport into the correspondig timestamp (Date) in the log space.
	 * 
	 * @param anX
	 * @return
	 */
	protected Date coord2time(double anX) {
		return new Date(coord2timeMillis(anX));
	}

	/**
	 * convenience method. transforms a given horizontal coordinate within the
	 * viewport into the correspondig timestamp (milliseconds) in the log space.
	 * 
	 * @param anX
	 * @return
	 */
	protected long coord2timeMillis(double anX) {
		return (long) ((double) anX / milli2pixels) + logBoundaryLeft.getTime();
	}

	/**
	 * Checks two time frames for intersection. Interpret as follows: one
	 * timeframe is delimited by d1Left on the right and d1Right on the right,
	 * this is the reference frame. the second timeframe is delimited by
	 * d2Left/Right. This method checks, if any part of the second timeframe is
	 * contained within the first timeframe.
	 * 
	 * @param d1Left
	 * @param d1Right
	 * @param d2Left
	 * @param d2Right
	 * @return
	 */
	protected static boolean intersects(Date d1Left, Date d1Right, Date d2Left,
			Date d2Right) {
		if (d2Left.after(d1Left)) {
			if (d2Left.before(d1Right)) {
				return true;
			} else {
				return false;
			}
		} else if (d2Right.after(d1Left)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * convenience method for internal use. paints a log item according to the
	 * given coordinates
	 * 
	 * @param xStart
	 *            horizontal start coordinate of the event
	 * @param xEnd
	 *            horizontal end coordinate of the event
	 * @param yLogTop
	 *            vertical coordinate of the log pane level (top)
	 * @param yLogHeight
	 *            vertical size (height) of log items
	 * @param yHandle
	 *            vertical coordinate of the handle level (center)
	 * @param g
	 *            the Graphics object used for painting
	 */
	protected void paintItem(int xStart, int xEnd, int yLogTop, int yLogHeight,
			int yHandle, Graphics g) {
		if ((xEnd - xStart) <= 1) {
			xEnd = xStart + 2;
		} // ensure minimum width of 2
		int xHandle = xStart + ((xEnd - xStart) / 2);
		g.fillRect(xStart, yLogTop, xEnd - xStart, yLogHeight); // draw item box
		g.drawLine(xHandle, yLogTop, xHandle, yHandle); // draw handle line
		paintHandle(xHandle, yHandle, g); // draw handle icon
	}

	/**
	 * convenience method for internal use. paints a log item handle
	 * visualization.
	 * 
	 * @param x
	 *            horizontal anchor coordinate of the handle
	 * @param y
	 *            vertical anchor coordinate of the handle
	 * @param g
	 *            the Graphics object used for painting
	 */
	protected void paintHandle(int x, int y, Graphics g) {
		if (itemHandle.equals(LogItemPanel.ITEM_HANDLE_NONE)) {
			return;
		}
		if (itemHandle.equals(LogItemPanel.ITEM_HANDLE_BOX)) {
			g.fillRect(x - 5, y - 5, 11, 11);
		} else if (itemHandle.equals(LogItemPanel.ITEM_HANDLE_CIRCLE)) {
			g.fillOval(x - 5, y - 5, 11, 11);
		} else if (itemHandle.equals(LogItemPanel.ITEM_HANDLE_RHOMBUS)) {
			int rhombX[] = { x, x - 5, x, x + 5 };
			int rhombY[] = { y - 5, y, y + 5, y };
			g.fillPolygon(rhombX, rhombY, 4);
		} else if (itemHandle.equals(LogItemPanel.ITEM_HANDLE_TRIANGLE)) {
			int triX[] = { x, x - 5, x + 5 };
			int triY[] = { y + 5, y - 5, y - 5 };
			g.fillPolygon(triX, triY, 3);
		}
	}

	/**
	 * convenience method for internal use. paints the log lane, with time
	 * indicators.
	 * 
	 * @param gOrig
	 * @param dMin
	 *            date on the left boundary
	 * @param dMax
	 *            date on the right boundary
	 */
	protected void paintLogLane(Graphics g) {
		g.setFont(g.getFont().deriveFont((float) 10.0));
		// set initial colors
		Color fgColor = null;
		Color bgColor = null;
		Color tmpColor = null;
		fgColor = null;
		bgColor = null;
		// calculate common coordinates
		int height = (int) ((double) (getHeight() - (3 * border)) * (1.0 - handleRatio));
		int yTop = this.getHeight() - height - border;
		int pixStart = 0;
		String dateStr, timeStr, millisStr = null;
		// calculate are to be painted
		int clipL = (int) g.getClipBounds().getMinX() - 1;
		int clipR = (int) g.getClipBounds().getMaxX() + 1;
		long clipLeftTs = (coord2timeMillis(clipL) / logLaneStep) * logLaneStep; // smallest
		// multiple
		// of
		// step
		// left
		// to
		// clip
		// border
		long clipRightTs = coord2timeMillis(clipR);
		// determine start color
		if (((clipLeftTs / logLaneStep) % 2) == 0) {
			fgColor = colorLogDark;
			bgColor = colorLogBright;
		} else {
			fgColor = colorLogBright;
			bgColor = colorLogDark;
		}
		// paint actual log lane (only the part in the clipping range
		// determined)
		for (long timeStart = clipLeftTs; timeStart < clipRightTs; timeStart += logLaneStep) {
			pixStart = time2coord(timeStart);
			cal.setTimeInMillis(timeStart);
			g.setColor(bgColor);
			g.fillRect(pixStart, yTop, logLanePixStep, height);
			g.setColor(fgColor);
			dateStr = cal.get(Calendar.DAY_OF_MONTH) + "."
					+ (cal.get(Calendar.MONTH) + 1) + "."
					+ cal.get(Calendar.YEAR);
			timeStr = cal.get(Calendar.HOUR_OF_DAY) + ":"
					+ cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
			millisStr = "." + cal.get(Calendar.MILLISECOND);
			g.drawString(dateStr, pixStart + 2, yTop + 10);
			g.drawString(timeStr, pixStart + 2, yTop + 20);
			g.drawString(millisStr, pixStart + 2, yTop + 30);
			// swap colors
			tmpColor = fgColor;
			fgColor = bgColor;
			bgColor = tmpColor;
		}
		// draw horizontal delimiters
		g.setColor(colorLogDark);
		g.drawLine(clipL, yTop, clipR, yTop);
		g.drawLine(clipL, yTop + height, clipR, yTop + height);
	}

	/**
	 * paints this log item panel and all contained log items as specified.
	 * 
	 * @param g
	 *            the graphics object used for painting
	 */
	public void paintComponent(Graphics grx) {
		Graphics g = grx.create();
		// System.out.println("Clip: " + clip.getMinX() + " - " +
		// clip.getMaxX());
		// paint background if required
		if (this.isOpaque()) {
			g.setColor(colorBg);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		// paint the time indicator equipped log lane
		paintLogLane(g);
		// calculate are to be painted
		int clipL = (int) g.getClipBounds().getMinX() - 1;
		int clipR = (int) g.getClipBounds().getMaxX() + 1;
		long clipLeftTs = (coord2timeMillis(clipL) / logLaneStep) * logLaneStep; // smallest
		// multiple
		// of
		// step
		// left
		// to
		// clip
		// border
		long clipRightTs = coord2timeMillis(clipR);
		Date clipLeftDt = new Date(clipLeftTs);
		Date clipRightDt = new Date(clipRightTs);
		// paint items
		if (itemSets.size() > 0) {
			String key = null;
			LogItem item = null;
			int handleHeight = getHandleHeight();
			int handleStep = (handleHeight - (2 * border)) / itemSets.size();
			if (handleStep == 0) {
				handleStep = 1;
			}
			int logItemHeight = getLogPaneHeight() / 3;
			int logItemTop = handleHeight + getLogPaneHeight() - (2 * border);
			// iterate through sets
			for (Iterator itSets = itemSets.keySet().iterator(); itSets
					.hasNext();) {
				key = (String) itSets.next();
				g.setColor(itemColors.getColor(key));
				// iterate through items
				for (Iterator itItm = ((SortedSet) itemSets.get(key))
						.iterator(); itItm.hasNext();) {
					item = (LogItem) itItm.next();
					if (intersects(clipLeftDt, clipRightDt, item
							.getLeftBoundaryTimestamp(), item
							.getRightBoundaryTimestamp())) {
						// paint only if visible
						this.paintItem(time2coord(item
								.getLeftBoundaryTimestamp()), time2coord(item
								.getRightBoundaryTimestamp()), logItemTop,
								logItemHeight, handleHeight, g);
					}
				}
				// move handles up
				handleHeight -= handleStep;
				if (handleHeight < border) { // prevent flip-over
					handleHeight = getHandleHeight();
				}
				logItemTop -= handleStep;
				if (logItemTop < (getLogPaneHeight() + border)) { // prevent
					// flip-over
					logItemTop = handleHeight + getLogPaneHeight()
							- (2 * border);
				}
			}
		}
	}

}
