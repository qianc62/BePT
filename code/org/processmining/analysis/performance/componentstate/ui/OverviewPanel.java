package org.processmining.analysis.performance.componentstate.ui;

import javax.swing.JPanel;
import java.util.HashMap;
import java.util.Date;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.Color;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.awt.Point;

import org.processmining.analysis.performance.componentstate.logutil.LogUnitList;
import org.processmining.analysis.performance.componentstate.logutil.AbstractLogUnit;
import org.processmining.analysis.performance.componentstate.ComponentStateAnalysis;
import org.processmining.analysis.performance.componentstate.model.ComponentStateModel;
import org.processmining.analysis.performance.dottedchart.DottedChartAnalysis;

import java.util.Iterator;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import java.awt.Graphics;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class OverviewPanel extends JPanel implements MouseMotionListener,
		MouseListener {

	private ComponentStateAnalysis csa = null; // parents
	private boolean drawImage = true;
	private int x1, y1, width, height;
	private int p_x, p_y;
	private int x1_o, y1_o;
	private boolean clicked = false;

	/**
	 * constructor
	 * 
	 * @param aItemSets
	 *            HashMap representing Item (start) of the log to be visualized
	 * @param rightBoundary
	 *            Date representing the right boundary (end) of the log to be
	 *            visualized
	 */
	public OverviewPanel(ComponentStateAnalysis aCSA) {
		csa = aCSA;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void setDrawBox(boolean b) {
		drawImage = b;
	}

	/**
	 * paints this log item panel and all contained log items as specified.
	 * 
	 * @param g
	 *            the graphics object used for painting
	 */
	public void paintComponent(Graphics grx) {
		if (drawImage) {
			csa.getComponentStatePanel().generateBufferedImage(200, 130);
			drawImage = false;
		}
		grx.drawImage(csa.getComponentStatePanel().getBufferedImage(), 0, 0,
				this);
		drawBox(grx);
	}

	public void drawBox(Graphics grx) {
		double width_o, height_o, width_v, height_v;
		Point p;

		width_o = csa.getComponentStatePanel().getWidth();
		height_o = csa.getComponentStatePanel().getHeight();
		width_v = csa.getScrollPane().getViewport().getWidth();
		height_v = csa.getScrollPane().getViewport().getHeight();
		p = csa.getScrollPane().getViewport().getViewPosition();

		x1 = (int) ((200.0 / width_o) * p.x);
		y1 = (int) ((130.0 / height_o) * p.y);
		width = (int) (199.0 * (width_v / width_o));
		height = (int) (129.0 * (height_v / height_o));
		if (width < 5)
			width = 5;
		if (height < 5)
			height = 5;
		grx.setColor(Color.red);
		grx.drawRect(x1, y1, width, height);
	}

	// //// mouse events
	public void mouseMoved(MouseEvent e) {

	}

	public void mouseDragged(MouseEvent e) {
		Point p = e.getPoint();

		if (clicked) {
			double width_o = csa.getComponentStatePanel().getWidth();
			double height_o = csa.getComponentStatePanel().getHeight();
			int x2 = x1_o + p.x - p_x;
			int y2 = y1_o + p.y - p_y;
			int x3 = (int) (x2 * (width_o / 200));
			int y3 = (int) (y2 * (height_o / 130));
			csa.setScrollBarPosition(new Point(x3, y3));
		}

	}

	// //// mouse listener
	// /MOUSE Listener ///////////////////////////////////////////
	public void mousePressed(MouseEvent e) {
		Point p = e.getPoint();
		if ((p.x >= x1) && (p.x <= x1 + width) && (p.y > y1)
				&& (p.y <= y1 + height)) {
			clicked = true;
			x1_o = x1;
			y1_o = y1;
			p_x = (int) p.x;
			p_y = (int) p.y;
		}
	}

	public void mouseReleased(MouseEvent e) {
		clicked = false;

	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {

	}
}
