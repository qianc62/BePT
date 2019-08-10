/*
 * Created on Nov. 20, 2006
 *
 * Author: Minseok Song
 * (c) 2006 Technische Universiteit Eindhoven, Minseok Song
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

package org.processmining.analysis.performance.dottedchart.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.processmining.analysis.performance.dottedchart.DottedChartAnalysis;
import org.processmining.analysis.performance.dottedchart.logutil.AbstractLogUnit;
import org.processmining.analysis.performance.dottedchart.logutil.LogUnitList;
import org.processmining.analysis.performance.dottedchart.model.DottedChartModel;
import org.processmining.framework.ui.Message;
import java.util.HashSet;

/**
 * DottedChartPanel.
 * 
 * The purpose of this class is to provide a view of dotted chart
 * 
 * @author Minseok Song
 */
public class DottedChartPanel extends JPanel implements MouseListener,
		MouseMotionListener {

	/* static member encoding for item handle icons */
	public static String STR_NONE = "None";
	public static String ITEM_HANDLE_DOT = "Dot";
	public static String ITEM_HANDLE_CIRCLE = "Circle";
	public static String ITEM_HANDLE_TRIANGLE = "Triangle";
	public static String ITEM_HANDLE_BOX = "Box";
	public static String ITEM_HANDLE_RHOMBUS = "Rhombus";
	public static String ITEM_HANDLE_ROUND_BOX = "RoundBox";
	public static String ITEM_HANDLE_DRAW_BOX = "DrawBox";
	public static String ITEM_HANDLE_DRAW_CIRCLE = "Circle";
	public static String ITEM_HANDLE_DRAW_TRIANGLE = "Triangle";
	public static String ITEM_HANDLE_DRAW_RHOMBUS = "DrawRhombusBox";
	public static String ITEM_HANDLE_DRAW_ROUND_BOX = "DrawRoundBox";

	public static final String TIME_ACTUAL = "Actual";
	public static final String TIME_RELATIVE_TIME = "Relative(Time)";
	public static final String TIME_RELATIVE_RATIO = "Relative(Ratio)";
	public static final String TIME_LOGICAL = "Logical";
	public static final String TIME_LOGICAL_RELATIVE = "Logical(Relative)";

	public static final String ST_ORIG = "Originator";
	public static final String ST_TASK = "Task ID";
	public static final String ST_INST = "Instance ID";
	public static final String ST_EVEN = "Event";
	public static final String ST_DATA = "Data";

	public static final String ST_NAME = "Component Name";
	public static final String ST_SIZE = "Number of Events";
	public static final String ST_DURATION = "Duration";
	public static final String ST_START_TIME = "Start Time";
	public static final String ST_END_TIME = "End Time";

	public static final String ST_ZOOMIN = "Zoom in";
	public static final String ST_DRAG = "Drag";

	private BufferedImage matrixBuffer;

	public static String HANDLE_ICONS[] = { STR_NONE, ITEM_HANDLE_CIRCLE,
			ITEM_HANDLE_TRIANGLE, ITEM_HANDLE_BOX, ITEM_HANDLE_RHOMBUS };

	DottedChartAnalysis dca = null; // parents
	DottedChartOptionPanel dcOptionPanel = null; // option panel

	/* instance attributes */
	protected DottedChartModel dcModel = null;
	protected double milli2pixels = 1.0; // the ratio between milliseconds and
	// displayed pixels
	protected double height2pixels = 1.0; // the ratio between heights and
	// displayed pixels
	protected long logLaneStep = 0;
	protected int logLanePixStep = 0;
	protected double viewportZoomX = 1.0;
	protected double viewportZoomY = 1.0;
	protected int updWidth = 0;
	protected int updHight = 0;
	protected String itemHandle = null;
	protected Color colorBg = null;
	protected Color colorFg = null;
	protected Color colorLogDark = null;
	protected Color colorLogBright = null;
	protected Color colorTimeLine = null;
	protected Color colorBaseGreen = null;
	protected ShapeReference itemShapes = null;
	protected String timeOption = TIME_ACTUAL;
	protected String relativeTimeBase = ST_INST;
	protected HashMap timeBaseHashMap = null;
	protected HashMap endTimeBaseHashMap = null;
	protected ColorReference itemColors = null;

	// to improve the performance
	private String stRalativeTimeOptions = null;
	private String stRalativeTime = null;
	private boolean bChangeOption = false;
	private boolean bLogical = false;
	private boolean bAdjust = true;
	private boolean bTooltip = false;

	// options
	private long[] widthDividers = { 1, 10, 100, 500, 1000, 60000, 3600000L,
			86400000L, 604800000L, 2592000000L, 31536000000L };
	private long timeSort = 1000;
	long clipLeftTs;
	long clipRightTs;
	int clipL = 0;
	int clipR = 0;
	/* size attributes */
	protected int border = 5;
	protected double handleRatio = 0.5;

	/* helpers */
	protected Calendar cal = null;

	// variables for Item Line
	protected boolean bDrawLine = false;

	// variables for finding bottleneck
	protected boolean bBottleneck = false;
	protected boolean bBottleneckforInstances = false;

	// for exporting
	protected ArrayList selectedIDs = new ArrayList();
	protected HashSet selectedIDIndices = new HashSet();

	// variables for zoom
	private Point p1;
	private Point p2;
	private boolean clicked = false;
	// variables for drag
	private Point p3;
	private Point p4;

	/**
	 * constructor
	 * 
	 * @param aDCA
	 *            DottedChartAnalysis the Dotted Chart Analysis to be displayed
	 * @param aDcModel
	 *            DottedChartModel the dottedchartmodel that includes data
	 *            structure for dotted chart
	 */
	public DottedChartPanel(DottedChartAnalysis aDCA) {
		dca = aDCA;
		dcModel = aDCA.getDottedChartModel();

		dcOptionPanel = new DottedChartOptionPanel(this, aDCA);

		dcModel.initTimeStatistics();

		calculateCurrentTime();
		dcModel.adjustLogBoundaries(timeOption);

		viewportZoomX = 1.0;
		viewportZoomY = 1.0;
		border = 10;
		handleRatio = 0.5;
		updWidth = this.getWidth();
		updHight = this.getHeight();
		itemHandle = DottedChartPanel.ITEM_HANDLE_CIRCLE;
		colorBg = new Color(240, 240, 220);
		colorFg = new Color(10, 10, 10);
		colorLogDark = new Color(170, 170, 160);
		colorLogBright = new Color(210, 210, 200);
		colorTimeLine = new Color(225, 225, 225);
		colorBaseGreen = new Color(50, 100, 100);
		itemColors = new ColorReference();
		cal = new GregorianCalendar();
		logLaneStep = 1000;
		logLanePixStep = 200;
		itemShapes = new ShapeReference();
		updateMilli2pixelsRatio();

		this.setAutoscrolls(true); // enable synthetic drag events
		this.setOpaque(true);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
	}

	public DottedChartModel getDottedChartModel() {
		return dcModel;
	}

	public DottedChartOptionPanel getDottedChartOptionPanel() {
		return dcOptionPanel;
	}

	public void changeLogical(boolean b) {
		bLogical = b;
	}

	public void changeOptions(boolean b) {
		bChangeOption = b;
	}

	public void changeComponentType() {
		// time statistics
		dcModel.setTypeHashMap(dcOptionPanel.getComponentType());
		dcModel.initTimeStatistics();

		if (timeOption.equals(TIME_ACTUAL)
				|| timeOption.equals(TIME_RELATIVE_TIME)
				|| timeOption.equals(TIME_RELATIVE_RATIO))
			dcModel.calculateStatistics();
		else if (timeOption.equals(TIME_LOGICAL))
			dcModel.calculateStatisticsLogical();
		else {
			dcModel.calculateCurrentTimeLogical_Relative();
			dcModel.calculateStatisticsLogical();
		}
		dcModel.adjustLogBoundaries(timeOption);
		updateMilli2pixelsRatio();
	}

	// for exporting
	public void setSelectedIDs(ArrayList tempSelectedIDs) {
		selectedIDs = tempSelectedIDs;
	}

	public void setTimeOption(String str) {
		timeOption = str;
	}

	public String getTimeOption() {
		return timeOption;
	}

	public void setTimeBaseHaspMap(HashMap aHashMap) {
		timeBaseHashMap = null;
		timeBaseHashMap = aHashMap;
	}

	public HashMap getTimeBaseHaspMap() {
		return timeBaseHashMap;
	}

	public void setEndTimeBaseHaspMap(HashMap aHashMap) {
		endTimeBaseHashMap = null;
		endTimeBaseHashMap = aHashMap;
	}

	public HashMap getEndTimeBaseHaspMap() {
		return endTimeBaseHashMap;
	}

	public void setRelativeTimeBase(String str) {
		relativeTimeBase = str;
	}

	public String getRelativeTimeBase() {
		return relativeTimeBase;
	}

	public ArrayList<DescriptiveStatistics> getDescriptiveStatistics() {
		return dcModel.getTimeStatistics();
	}

	public void changeEventTypeToKeep(ArrayList<String> aEventTypeToKeep) {
		dcModel.setEventTypeToKeep(aEventTypeToKeep);
		this.changeOptions(true);
		this.calculateCurrentTime();
		dcModel.adjustLogBoundaries(timeOption);
		this.updateMilli2pixelsRatio();
	}

	public void changeInstanceTypeToKeep(ArrayList anInstanceIDs) {
		dcModel.setInstanceTypeToKeep(anInstanceIDs);
		this.calculateCurrentTime();
		dcModel.adjustLogBoundaries(timeOption);
		this.updateMilli2pixelsRatio();
	}

	public int getHashMapSize() {
		if (!dcModel.getTypeHashMap().equals(ST_INST))
			return dcModel.getItemMap().size();
		else
			return dcModel.getInstanceTypeToKeep().size();
	}

	public ColorReference getColorReference() {
		return itemColors;
	}

	protected String assignShapeByItem(AbstractLogUnit logUnit) {
		String shapeStandard = dcOptionPanel.getShapeStandard();
		String str = ITEM_HANDLE_CIRCLE;
		if (shapeStandard.equals(DottedChartAnalysis.ST_ORIG))
			str = itemShapes.getShape(logUnit.getOriginator());
		else if (shapeStandard.equals(DottedChartAnalysis.ST_TASK))
			str = itemShapes.getShape(logUnit.getElement());
		else if (shapeStandard.equals(DottedChartAnalysis.ST_EVEN))
			str = itemShapes.getShape(logUnit.getType());
		else if (shapeStandard.equals(DottedChartAnalysis.ST_INST))
			str = itemShapes.getShape(logUnit.getProcessInstance().getName());
		return str;
	}

	protected void assignColorByItem(AbstractLogUnit logUnit, Graphics g) {

		String colorStandard = dcOptionPanel.getColorStandard();
		if (colorStandard.equals(DottedChartAnalysis.STR_NONE))
			g.setColor(colorBaseGreen);
		else if (colorStandard.equals(DottedChartAnalysis.ST_ORIG))
			g.setColor(itemColors.getColor(logUnit.getOriginator()));
		else if (colorStandard.equals(DottedChartAnalysis.ST_TASK))
			g.setColor(itemColors.getColor(logUnit.getElement()));
		else if (colorStandard.equals(DottedChartAnalysis.ST_EVEN))
			g.setColor(itemColors.getColor(logUnit.getType()));
		else if (colorStandard.equals(DottedChartAnalysis.ST_INST))
			g.setColor(itemColors.getColor(logUnit.getProcessInstance()
					.getName()));
	}

	public void changeTimeOption() {
		String base = dcOptionPanel.getRelativeTimeOption();
		timeOption = dcOptionPanel.getTimeOption();
		if (timeOption.equals(TIME_RELATIVE_TIME)
				|| timeOption.equals(TIME_RELATIVE_RATIO)) {
			setTimeBaseHaspMap(dcModel.getStartDateMap(base));
			setEndTimeBaseHaspMap(dcModel.getEndDateMap(base));
		}
		calculateCurrentTime();
		dcModel.adjustLogBoundaries(timeOption);
		updateMilli2pixelsRatio();
		clipLeftTs = coord2timeMillis(clipL);
		clipRightTs = coord2timeMillis(clipR);
		bAdjust = true;
	}

	public void changeWidthSort() {
		bAdjust = true;
	}

	public long adjustWidthSort(long sort) {
		return (clipRightTs - clipLeftTs) / sort;
	}

	public void setTimeSort(long sort) {
		timeSort = sort;
	}

	protected void updateMilli2pixelsRatio() {
		milli2pixels = (double) (this.getWidth() - border * 2)
				/ (double) (dcModel.getLogBoundaryRight().getTime() - dcModel
						.getLogBoundaryLeft().getTime()); // + 2 is added to
		// extend bound
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
	public void setViewportZoomX(double aZoom) {
		if (aZoom > 1.0) { // ensure sane values
			viewportZoomX = 1.0;
		} else if (aZoom <= 0.0) {
			viewportZoomX = 0.00000000001;
		} else {
			viewportZoomX = aZoom;
		}
		double ratio = updWidth;
		updWidth = (int) ((double) getParent().getWidth() * (1.0 / aZoom));
		ratio = updWidth / ratio;
		Dimension dim = new Dimension(updWidth, updHight);
		Point d = dca.getScrollPane().getViewport().getViewPosition();
		Point p = new Point((int) ((double) d.getX() * ratio),
				(int) ((double) d.getY()));

		this.setPreferredSize(dim);
		updateMilli2pixelsRatio();

		this.revalidate();
		dca.setScrollBarPosition(p);
	}

	/**
	 * adjusts the viewable are of the log (zoom)
	 * 
	 * @param aZoom
	 *            fraction of the log to be viewable (within (0,1] !)
	 */
	public void setViewportZoomY(double aZoom) {
		if (aZoom > 1.0) { // ensure sane values
			viewportZoomY = 1.0;
		} else if (aZoom <= 0.0) {
			viewportZoomY = 0.00000000001;
		} else {
			viewportZoomY = aZoom;
		}
		double ratio = updHight;
		updHight = (int) ((double) getParent().getHeight() * (1.0 / aZoom));
		ratio = updHight / ratio;
		Dimension dim = new Dimension(updWidth, updHight);
		Point d = dca.getScrollPane().getViewport().getViewPosition();
		Point p = new Point((int) d.getX(), (int) ((double) d.getY() * (ratio)));
		this.setPreferredSize(dim);
		updateMilli2pixelsRatio();

		this.revalidate();
		dca.setScrollBarPosition(p);
	}

	/**
	 * adjusts the viewable are of the log (zoom)
	 */
	public void setViewportZoomIn() {
		Dimension d = dca.getViewportSize();
		int width = Math.abs(p1.x - p2.x);
		int height = Math.abs(p1.y - p2.y);

		int value = (int) (Math.log10((double) this.getWidth()
				* (d.getWidth() / width)
				/ (double) dca.getViewportSize().getWidth()) * 1000.0);
		if (value > 3000)
			return;
		value = (int) (Math.log10((double) this.getHeight()
				* (d.getHeight() / height)
				/ (double) dca.getViewportSize().getHeight()) * 1000.0);
		if (value > 3000)
			return;

		updWidth = (int) ((double) this.getWidth() * (d.getWidth() / width));
		updHight = (int) ((double) this.getHeight() * (d.getHeight() / height));
		Dimension dim = new Dimension(updWidth, updHight);
		int pos_x = Math.min(p1.x, p2.x);
		int pos_y = Math.min(p1.y, p2.y);

		Point p = new Point((int) (pos_x * d.getWidth() / width), (int) (pos_y
				* d.getHeight() / height));
		this.setPreferredSize(dim);
		updateMilli2pixelsRatio();
		this.revalidate();
		dca.setScrollBarPosition(p);
		p1 = null;
		p2 = null;
		adjustSlideBar();
	}

	/**
	 * adjusts the viewable are of the log (zoom)
	 */
	public Point zoomInViewPort() {
		if (p1 == null || p2 == null)
			return null;
		Dimension d = dca.getViewportSize();
		int width = Math.abs(p1.x - p2.x);
		int height = Math.abs(p1.y - p2.y);

		int value = (int) (Math.log10((double) this.getWidth()
				* (d.getWidth() / width)
				/ (double) dca.getViewportSize().getWidth()) * 1000.0);
		if (value > 3000)
			return null;
		value = (int) (Math.log10((double) this.getHeight()
				* (d.getHeight() / height)
				/ (double) dca.getViewportSize().getHeight()) * 1000.0);
		if (value > 3000)
			return null;

		updWidth = (int) ((double) this.getWidth() * (d.getWidth() / width));
		updHight = (int) ((double) this.getHeight() * (d.getHeight() / height));

		Dimension dim = new Dimension(updWidth, updHight);
		int pos_x = Math.min(p1.x, p2.x);
		int pos_y = Math.min(p1.y, p2.y);

		this.setPreferredSize(dim);
		updateMilli2pixelsRatio();
		this.revalidate();
		p1 = null;
		p2 = null;
		adjustSlideBar();
		return new Point((int) (pos_x * d.getWidth() / width), (int) (pos_y
				* d.getHeight() / height));
	}

	public void adjustSlideBar() {
		int value = (int) (Math.log10((double) updWidth
				/ (double) dca.getViewportSize().getWidth()) * 1000.0);

		if (value > 3000)
			value = 3000;
		dcOptionPanel.getZoomSliderX().setValue(value);
		dcOptionPanel.getZoomSliderX().repaint();

		value = (int) (Math.log10((double) updHight
				/ (double) dca.getViewportSize().getHeight()) * 1000.0);
		if (value > 3000)
			value = 3000;
		dcOptionPanel.getZoomSliderY().setValue(value);
		dcOptionPanel.getZoomSliderY().repaint();
	}

	/**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
	protected int time2coord(Date aTimestamp) {
		return (int) ((aTimestamp.getTime() - dcModel.getLogBoundaryLeft()
				.getTime()) * milli2pixels);
	}

	/**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
	protected int time2coord_buffer(Date aTimestamp, double milli2pixels) {
		return (int) ((aTimestamp.getTime() - dcModel.getLogBoundaryLeft()
				.getTime()) * milli2pixels);
	}

	/**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
	protected int unit2Cord(int unit_number) {
		return (int) (border + ((double) (this.getHeight() - 2 * border)
				/ getHashMapSize() * unit_number));
	}

	/**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
	protected int unit2Cord_buffer(int unit_number, int height) {
		return (int) (((double) (height) / getHashMapSize() * unit_number));
	}

	/**
	 * Convenience method. Transforms a timestamp (milliseconds) into the
	 * corresponding horizontal position within the viewport.
	 * 
	 * @param aTimeMillis
	 * @return
	 */
	protected int time2coord(long aTimeMillis) {
		return (int) ((aTimeMillis - dcModel.getLogBoundaryLeft().getTime()) * milli2pixels);
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
		return (long) ((double) anX / milli2pixels)
				+ dcModel.getLogBoundaryLeft().getTime();
	}

	/**
	 * convenience method. transforms a given horizontal coordinate within the
	 * viewport into the correspondig timestamp (milliseconds) in the log space.
	 * 
	 * @param anX
	 * @return
	 */
	protected long coord2timeMillis_buffer(double anX, double milli2pixels) {
		return (long) ((double) anX / milli2pixels)
				+ dcModel.getLogBoundaryLeft().getTime();
	}

	/**
	 * convenience method. adjust width on the screen
	 * 
	 * @param void
	 * @return
	 */
	public void adjustWidth() {
		long widthSort = dcOptionPanel.getWidthDivider();
		long number = (clipRightTs - clipLeftTs) / widthSort;

		int idx = 0;
		if (number == 0) {
			for (idx = 0; idx < widthDividers.length; idx++)
				if (widthDividers[idx] == widthSort)
					break;
			if (idx == 0)
				return;
			idx--;
			while (idx >= 0) {
				widthSort = widthDividers[idx];
				number = (clipRightTs - clipLeftTs) / widthSort;
				if (number > 0) {
					break;
				}
				idx--;
			}
			if (idx < 0)
				dcOptionPanel.changeWidthSort(0);
			else
				dcOptionPanel.changeWidthSort(idx);
		} else if (number > 100) {
			for (idx = 0; idx < widthDividers.length; idx++)
				if (widthDividers[idx] == widthSort)
					break;
			idx++;
			while (idx < widthDividers.length) {
				widthSort = widthDividers[idx];
				number = (clipRightTs - clipLeftTs) / widthSort;
				if (number <= 100) {
					break;
				}
				idx++;
			}
			if (idx >= widthDividers.length)
				dcOptionPanel.changeWidthSort(widthDividers.length - 1);
			else
				dcOptionPanel.changeWidthSort(idx);
		}
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
	 * calcuate current time
	 */
	protected void calculateCurrentTime() {

		if (timeOption.equals(TIME_LOGICAL)) {
			if (!bLogical) {
				dcModel.calculateCurrentTimeLogical();
				dcModel.calculateStatisticsLogical();
				bLogical = true;
			} else {
				dcModel.setLogicalTime();
				dcModel.calculateStatisticsLogical();
			}
			return;
		} else if (timeOption.equals(TIME_LOGICAL_RELATIVE)) {
			dcModel.calculateCurrentTimeLogical_Relative();
			dcModel.calculateStatisticsLogical();
			return;
		} else if (timeOption.equals(TIME_RELATIVE_TIME)
				|| timeOption.equals(TIME_RELATIVE_RATIO)) {
			if ((!bChangeOption) && stRalativeTime != null
					&& stRalativeTime.equals(timeOption)) {
				if (stRalativeTimeOptions != null
						&& stRalativeTimeOptions.equals(relativeTimeBase)) {
					dcModel.setRelativeTime();
					dcModel.calculateStatistics();
					return;
				}
			}
			bChangeOption = false;
			stRalativeTime = timeOption;
			stRalativeTimeOptions = relativeTimeBase;
		}

		// paint items
		if (dcModel.getItemMap().size() <= 0)
			return;

		String key = null;
		AbstractLogUnit item = null;

		// iterate through sets
		int index = -1;
		for (Iterator itSets = dcModel.getSortedKeySetList().iterator(); itSets
				.hasNext();) {
			index++;
			key = (String) itSets.next();
			DescriptiveStatistics tempDS = dcModel.getTimeStatistics().get(
					index + 1);
			tempDS.clear();

			if (dcModel.getTypeHashMap().equals(ST_INST)
					&& !dcModel.getInstanceTypeToKeep().contains(key))
				continue;

			LogUnitList tempLogUnit = (LogUnitList) dcModel.getItemMap().get(
					key);

			for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext();) {
				item = (AbstractLogUnit) itItm.next();
				if (dcModel.getEventTypeToKeep() != null
						&& (!dcModel.getEventTypeToKeep().contains(
								item.getType()) || !dcModel
								.getInstanceTypeToKeep().contains(
										item.getProcessInstance().getName())))
					continue;

				if (timeOption.equals(TIME_ACTUAL)) {
					item.setCurrentTimeStamp();
				} else if (timeOption.equals(TIME_RELATIVE_TIME)) {
					if (relativeTimeBase.equals(ST_INST))
						item.setCurrentTimeStampRelative_Instance();
					else if (relativeTimeBase.equals(ST_TASK))
						item
								.setCurrentTimeStampRelative_Others((Date) timeBaseHashMap
										.get(item.getElement()));
					else if (relativeTimeBase.equals(ST_ORIG)) {
						if (timeBaseHashMap.containsKey(item.getOriginator()))
							item
									.setCurrentTimeStampRelative_Others((Date) timeBaseHashMap
											.get(item.getOriginator()));
					} else if (relativeTimeBase.equals(ST_EVEN))
						item
								.setCurrentTimeStampRelative_Others((Date) timeBaseHashMap
										.get(item.getType()));
				} else if (timeOption.equals(TIME_RELATIVE_RATIO)) {
					if (relativeTimeBase.equals(ST_INST))
						item.setCurrentTimeStampRelativeRatio_Instance();
					else if (relativeTimeBase.equals(ST_TASK))
						item.setCurrentTimeStampRelativeRatio_Others(
								(Date) timeBaseHashMap.get(item.getElement()),
								(Date) endTimeBaseHashMap
										.get(item.getElement()));
					else if (relativeTimeBase.equals(ST_ORIG)) {
						if (timeBaseHashMap.containsKey(item.getOriginator()))
							item.setCurrentTimeStampRelativeRatio_Others(
									(Date) timeBaseHashMap.get(item
											.getOriginator()),
									(Date) endTimeBaseHashMap.get(item
											.getOriginator()));
					} else if (relativeTimeBase.equals(ST_EVEN))
						item.setCurrentTimeStampRelativeRatio_Others(
								(Date) timeBaseHashMap.get(item.getType()),
								(Date) endTimeBaseHashMap.get(item.getType()));
				}
			}
		}
		dcModel.calculateStatistics();
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
	protected void paintItem(int x, int y, Graphics g, String shape) {
		if (shape.equals(STR_NONE)) {
			return;
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DOT)) {
			g.fillOval(x - 2, y - 2, 4, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_BOX)) {
			g.fill3DRect(x - 5, y - 5, 10, 10, false);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_CIRCLE)) {
			g.fillOval(x - 5, y - 5, 11, 11);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_RHOMBUS)) {
			int rhombX[] = { x, x - 5, x, x + 5 };
			int rhombY[] = { y - 5, y, y + 5, y };
			g.fillPolygon(rhombX, rhombY, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_TRIANGLE)) {
			int triX[] = { x, x - 5, x + 5 };
			int triY[] = { y + 5, y - 5, y - 5 };
			g.fillPolygon(triX, triY, 3);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_ROUND_BOX)) {
			g.fillRoundRect(x - 5, y - 5, 10, 10, 2, 2);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_BOX)) {
			g.drawRect(x - 5, y - 5, 10, 10);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_CIRCLE)) {
			g.drawOval(x - 5, y - 5, 11, 11);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_RHOMBUS)) {
			int rhombX[] = { x, x - 5, x, x + 5 };
			int rhombY[] = { y - 5, y, y + 5, y };
			g.drawPolygon(rhombX, rhombY, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_TRIANGLE)) {
			int triX[] = { x, x - 5, x + 5 };
			int triY[] = { y + 5, y - 5, y - 5 };
			g.drawPolygon(triX, triY, 3);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_ROUND_BOX)) {
			g.drawRoundRect(x - 5, y - 5, 10, 10, 2, 2);
		}
	}

	protected void addExportingList(AbstractLogUnit item) {
		for (int i = 0; i < selectedIDs.size(); i++) {
			if (selectedIDs.get(i).equals(item.getProcessInstance().getName())) {
				selectedIDIndices.add(i);
				return;
			}
		}
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
	protected void paintHighligtedItem(int x, int y, Graphics g, String shape) {
		Color color = g.getColor();
		if (shape.equals(STR_NONE)) {
			return;
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DOT)) {
			if (!color.equals(Color.red))
				g.setColor(Color.red);
			else
				g.setColor(Color.black);
			g.fillOval(x - 3, y - 3, 6, 6);
			g.setColor(color);
			g.fillOval(x - 2, y - 2, 4, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_BOX)) {
			if (!color.equals(Color.black))
				g.setColor(Color.black);
			else
				g.setColor(Color.red);
			g.fillRect(x - 6, y - 6, 12, 12);
			g.setColor(color);
			g.fill3DRect(x - 5, y - 5, 10, 10, false);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_CIRCLE)) {
			if (!color.equals(Color.black))
				g.setColor(Color.black);
			else
				g.setColor(Color.red);
			g.fillOval(x - 6, y - 6, 13, 13);
			g.setColor(color);
			g.fillOval(x - 5, y - 5, 11, 11);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_RHOMBUS)) {
			int rhombX[] = { x, x - 5, x, x + 5 };
			int rhombY[] = { y - 5, y, y + 5, y };
			g.fillPolygon(rhombX, rhombY, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_TRIANGLE)) {
			int triX[] = { x, x - 5, x + 5 };
			int triY[] = { y + 5, y - 5, y - 5 };
			g.fillPolygon(triX, triY, 3);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_ROUND_BOX)) {
			if (!color.equals(Color.black))
				g.setColor(Color.black);
			else
				g.setColor(Color.red);
			g.fillRoundRect(x - 6, y - 6, 13, 13, 2, 2);
			g.setColor(color);
			g.fillRoundRect(x - 5, y - 5, 10, 10, 2, 2);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_BOX)) {
			g.drawRect(x - 5, y - 5, 10, 10);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_CIRCLE)) {
			if (!color.equals(Color.black))
				g.setColor(Color.black);
			else
				g.setColor(Color.red);
			g.fillOval(x - 6, y - 6, 13, 13);
			g.setColor(color);
			g.drawOval(x - 5, y - 5, 11, 11);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_RHOMBUS)) {
			int rhombX[] = { x, x - 5, x, x + 5 };
			int rhombY[] = { y - 5, y, y + 5, y };
			g.drawPolygon(rhombX, rhombY, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_TRIANGLE)) {
			int triX[] = { x, x - 5, x + 5 };
			int triY[] = { y + 5, y - 5, y - 5 };
			g.drawPolygon(triX, triY, 3);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_ROUND_BOX)) {
			g.drawRoundRect(x - 5, y - 5, 10, 10, 2, 2);
		}
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
	protected void paintItem_buffer(int x, int y, Graphics g, String shape) {
		if (shape.equals(STR_NONE)) {
			return;
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DOT)) {
			g.fillOval(x - 2, y - 2, 7, 7);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_BOX)) {
			g.fill3DRect(x - 3, y - 3, 6, 6, false);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_CIRCLE)) {
			g.fillOval(x - 2, y - 2, 7, 7);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_RHOMBUS)) {
			int rhombX[] = { x, x - 3, x, x + 3 };
			int rhombY[] = { y - 3, y, y + 3, y };
			g.fillPolygon(rhombX, rhombY, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_TRIANGLE)) {
			int triX[] = { x, x - 3, x + 3 };
			int triY[] = { y + 3, y - 3, y - 3 };
			g.fillPolygon(triX, triY, 3);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_ROUND_BOX)) {
			g.fillRoundRect(x - 3, y - 3, 6, 6, 2, 2);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_BOX)) {
			g.drawRect(x - 3, y - 3, 6, 6);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_CIRCLE)) {
			g.drawOval(x - 2, y - 2, 7, 7);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_RHOMBUS)) {
			int rhombX[] = { x, x - 3, x, x + 3 };
			int rhombY[] = { y - 3, y, y + 3, y };
			g.drawPolygon(rhombX, rhombY, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_TRIANGLE)) {
			int triX[] = { x, x - 3, x + 3 };
			int triY[] = { y + 3, y - 3, y - 3 };
			g.drawPolygon(triX, triY, 3);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_ROUND_BOX)) {
			g.drawRoundRect(x - 3, y - 3, 6, 6, 2, 2);
		}
	}

	protected void paintItemLine(int x1, int y1, int x2, Graphics g) {
		g.fillRoundRect(x1, y1 - 2, x2 - x1, 4, 2, 2);
	}

	protected void paintItemLineBottleneck(int x1, int y1, int x2, Graphics g) {
		Color tempColor = g.getColor();
		g.setColor(Color.red);
		g.fillRoundRect(x1, y1 - 1, x2 - x1, 2, 1, 1);
		g.setColor(tempColor);
	}

	protected void paintItemLine_buffer(int x1, int y1, int x2, Graphics g) {
		g.fillRoundRect(x1, y1 - 1, x2 - x1, 2, 1, 1);
	}

	protected void paintComponentLane(Graphics g) {

		double percentileL = dcModel.getTimeStatistics().get(0).getPercentile(
				dca.getSettingPanel().getPercentileforInstanceL());
		double percentileU = dcModel.getTimeStatistics().get(0).getPercentile(
				dca.getSettingPanel().getPercentileforInstanceU());
		g.setFont(g.getFont().deriveFont((float) 10.0));
		// set initial colors
		Color fgColor = null;
		Color bgColor = null;
		Color tmpColor = null;
		fgColor = null;
		bgColor = null;
		// calculate common coordinates
		int unitHeight = (this.getHeight() - 2 * border) / getHashMapSize();
		int yTop = border;
		int yBottom = this.getHeight() - border;
		int pixStart = 0;
		String dateStr, timeStr, millisStr = null;

		// calculate area to be painted
		clipL = (int) g.getClipBounds().getMinX() - 1;
		clipR = (int) g.getClipBounds().getMaxX() + 1;

		// initialze start color
		fgColor = colorLogDark;
		bgColor = colorLogBright;

		// calculate current top
		int currentTop = yTop;

		// paint actual log lane (only the part in the clipping range
		// determined)
		Iterator itr = dcModel.getSortedKeySetList().iterator();
		g.setFont(new Font("Dialog", Font.BOLD, 13));
		int index = 0;
		currentTop = yTop;
		while (itr.hasNext()) {

			String dimName = (String) itr.next();
			LogUnitList tempList = ((LogUnitList) dcModel.getItemMap().get(
					dimName));
			long tempDuration;
			try {
				tempDuration = (tempList.getRightBoundaryTimestamp(
						dcModel.getEventTypeToKeep(),
						dcModel.getInstanceTypeToKeep()).getTime() - tempList
						.getLeftBoundaryTimestamp(dcModel.getEventTypeToKeep(),
								dcModel.getInstanceTypeToKeep()).getTime());
			} catch (Exception ce) {
				tempDuration = 0;
			}

			if (dcModel.getTypeHashMap().equals(ST_INST)
					&& !dcModel.getInstanceTypeToKeep().contains(dimName))
				continue;
			g.setColor(bgColor);
			g.fillRect(pixStart, currentTop, clipR, currentTop + unitHeight);

			g.setColor(fgColor);

			// for bottleneck
			if (dcOptionPanel.getComponentType().equals(
					DottedChartPanel.ST_INST)
					&& bBottleneckforInstances
					&& tempDuration >= percentileL
					&& tempDuration <= percentileU)
				g.setColor(Color.red);
			else
				g.setColor(Color.black);
			g.drawString(dimName, pixStart + 5, currentTop + 20);

			index++;
			currentTop = unit2Cord(index);

			// swap colors
			tmpColor = fgColor;
			fgColor = bgColor;
			bgColor = tmpColor;

		}

		g.setFont(new Font("Dialog", Font.PLAIN, 12));

		// draw horizontal delimiters
		g.setColor(colorTimeLine);
		g.drawLine(clipL, yTop, clipR, yTop);
		g.drawLine(clipL, yBottom, clipR, yBottom);

		clipLeftTs = coord2timeMillis(clipL);
		clipRightTs = coord2timeMillis(clipR);

		// draw vertical lines
		// adjust width
		if (bAdjust) {
			adjustWidth();
			bAdjust = false;
		}

		for (long timeStart = dcModel.getLogBoundaryLeft().getTime(); timeStart < clipRightTs; timeStart += dcOptionPanel
				.getWidthDivider()) {
			pixStart = time2coord(timeStart) + border;
			cal.setTimeInMillis(timeStart);
			g.setColor(colorTimeLine);
			g.drawLine(pixStart, yTop, pixStart, yBottom);
			g.setColor(colorLogDark);
			g.setColor(Color.black); // to be deleted
			if (timeOption.equals(TIME_ACTUAL)) {
				dateStr = cal.get(Calendar.DAY_OF_MONTH) + "."
						+ (cal.get(Calendar.MONTH) + 1) + "."
						+ cal.get(Calendar.YEAR);
				g.drawString(dateStr, pixStart + 2, yTop);
				timeStr = cal.get(Calendar.HOUR_OF_DAY) + ":"
						+ cal.get(Calendar.MINUTE) + ":"
						+ cal.get(Calendar.SECOND);
				g.drawString(timeStr, pixStart + 2, yTop + 10);
			} else if (timeOption.equals(TIME_RELATIVE_TIME)) {
				long days = timeStart / 1000 / 60 / 60 / 24;
				long hours = (timeStart - days * 24 * 60 * 60 * 1000) / 1000 / 60 / 60;
				long minutes = (timeStart - days * 24 * 60 * 60 * 1000 - hours * 60 * 60 * 1000) / 1000 / 60;
				long seconds = (timeStart - days * 24 * 60 * 60 * 1000 - hours
						* 60 * 60 * 1000 - minutes * 60 * 1000) / 1000;
				timeStr = days + "days:" + hours + ":" + minutes + ":"
						+ seconds;
				g.drawString(timeStr, pixStart + 2, yTop);
			} else if (timeOption.equals(TIME_RELATIVE_RATIO)) {
				timeStr = timeStart / 100 + "."
						+ (timeStart - timeStart / 100 * 100) + "%";
				g.drawString(timeStr, pixStart + 2, yTop);
			} else if (timeOption.equals(TIME_LOGICAL)
					|| timeOption.equals(TIME_LOGICAL_RELATIVE)) {
				timeStr = String.valueOf(timeStart);
				g.drawString(timeStr, pixStart + 2, yTop);
			}

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
	protected void paintComponentLane(Graphics2D g, int width, int hight) {
		g.setFont(g.getFont().deriveFont((float) 10.0));
		// set initial colors
		Color fgColor = null;
		Color bgColor = null;
		Color tmpColor = null;
		fgColor = null;
		bgColor = null;
		// calculate common coordinates
		int unitHeight = hight / getHashMapSize();
		int yTop = 0;
		int yBottom = hight;
		int pixStart = 0;
		String dateStr, timeStr, millisStr = null;

		// calculate area to be painted
		clipL = 0;
		clipR = width;

		// initialze start color
		fgColor = colorLogDark;
		bgColor = colorLogBright;

		// calculate current top
		int currentTop = yTop;

		// paint actual log lane (only the part in the clipping range
		// determined)
		Iterator itr = dcModel.getSortedKeySetList().iterator(); // dcModel.getItemMap().keySet().iterator();
		g.setFont(new Font("Dialog", Font.BOLD, 13));
		int index = 0;
		currentTop = yTop;
		while (itr.hasNext()) {
			String dimName = (String) itr.next();
			if (dcModel.getTypeHashMap().equals(ST_INST)
					&& !dcModel.getInstanceTypeToKeep().contains(dimName))
				continue;
			g.setColor(bgColor);
			g.fillRect(pixStart, currentTop, clipR, currentTop + unitHeight);

			g.setColor(fgColor);

			index++;
			currentTop = unit2Cord_buffer(index, hight);

			// swap colors
			tmpColor = fgColor;
			fgColor = bgColor;
			bgColor = tmpColor;
		}

		g.setFont(new Font("Dialog", Font.PLAIN, 12));

		// draw horizontal delimiters
		g.setColor(colorTimeLine);
		g.drawLine(clipL, yTop, clipR, yTop);
		g.drawLine(clipL, yBottom, clipR, yBottom);

		clipLeftTs = coord2timeMillis(clipL);
		clipRightTs = coord2timeMillis(clipR);
	}

	/**
	 * paints this log item panel and all contained log items as specified.
	 * 
	 * @param g
	 *            the graphics object used for painting
	 */
	public void paintComponent(Graphics grx) {
		selectedIDIndices.clear(); // for exporting
		bDrawLine = dca.getSettingPanel().isDrawLine();
		// todo
		bBottleneck = dca.getSettingPanel().isBottleneck();
		bBottleneckforInstances = dca.getSettingPanel()
				.isBottleneckforInstance();
		double percentileL = dcModel.getOverallStatistics().getPercentile(
				dca.getSettingPanel().getPercentileL());
		double percentileU = dcModel.getOverallStatistics().getPercentile(
				dca.getSettingPanel().getPercentileU());

		Graphics gr = grx.create();

		if (this.isOpaque()) {
			gr.setColor(colorBg);
			gr.fillRect(0, 0, getWidth(), getHeight());
		}

		// paint the time indicator equipped component lane
		paintComponentLane(gr);

		// calculate are to be painted
		int height = (int) ((double) (getHeight() - (2 * border)));
		int unitHeight = height / getHashMapSize();
		int currentTop = 0;

		// paint items
		if (dcModel.getItemMap().size() > 0) {
			String key = null;
			AbstractLogUnit item = null;
			// iterate through sets
			int index = 0;
			for (Iterator itSets = dcModel.getSortedKeySetList().iterator(); itSets
					.hasNext();) {
				key = (String) itSets.next();
				// if key is not in instanceIDs, skip..
				if (dcModel.getTypeHashMap().equals(ST_INST)
						&& !dcModel.getInstanceTypeToKeep().contains(key))
					continue;

				currentTop = unit2Cord(index) + unitHeight / 2;

				LogUnitList tempUnitList = new LogUnitList();
				AbstractLogUnit olditem = null;

				// for bottleneck
				boolean bInstances = false;
				boolean flag = true;
				if (dcOptionPanel.getComponentType().equals(
						DottedChartPanel.ST_INST)
						&& bBottleneckforInstances) {
					LogUnitList tempUnitList2;
					tempUnitList2 = ((LogUnitList) dcModel.getItemMap()
							.get(key));
					double percentile2L = dcModel.getTimeStatistics().get(0)
							.getPercentile(
									dca.getSettingPanel()
											.getPercentileforInstanceL());
					double percentile2U = dcModel.getTimeStatistics().get(0)
							.getPercentile(
									dca.getSettingPanel()
											.getPercentileforInstanceU());
					long tempDuration = (tempUnitList2
							.getRightBoundaryTimestamp(
									dcModel.getEventTypeToKeep(),
									dcModel.getInstanceTypeToKeep()).getTime() - tempUnitList2
							.getLeftBoundaryTimestamp(
									dcModel.getEventTypeToKeep(),
									dcModel.getInstanceTypeToKeep()).getTime());
					if (dcOptionPanel.getComponentType().equals(
							DottedChartPanel.ST_INST)
							&& bBottleneckforInstances
							&& tempDuration >= percentile2L
							&& tempDuration <= percentile2U)
						bInstances = true;
				}
				// end for bottleneck ////////

				// iterate through items
				for (Iterator itItm = ((LogUnitList) dcModel.getItemMap().get(
						key)).iterator(); itItm.hasNext();) {
					item = (AbstractLogUnit) itItm.next();
					if (dcModel.getEventTypeToKeep() != null
							&& (!dcModel.getEventTypeToKeep().contains(
									item.getType()) || !dcModel
									.getInstanceTypeToKeep()
									.contains(
											item.getProcessInstance().getName())))
						continue;
					if (bDrawLine
							&& item.getType().equals(
									dca.getSettingPanel().getStartEvent()))
						tempUnitList.addEvent(item);
					assignColorByItem(item, gr);
					clipL = (int) gr.getClipBounds().getMinX() - 1;
					clipR = (int) gr.getClipBounds().getMaxX() + 1;
					long clipLeftTs2 = coord2timeMillis(clipL);
					long clipRightTs2 = coord2timeMillis(clipR);
					// if line is added
					if (bDrawLine
							&& item.getType().equals(
									dca.getSettingPanel().getEndEvent())) {
						for (Iterator itr = tempUnitList.iterator(); itr
								.hasNext();) {
							AbstractLogUnit item2 = (AbstractLogUnit) itr
									.next();
							if (item2.getElement().equals(item.getElement())
									&& item2.getProcessInstance().equals(
											item.getProcessInstance())) {
								paintItemLine(time2coord(item2
										.getCurrentTimeStamp())
										+ border, currentTop, time2coord(item
										.getCurrentTimeStamp())
										+ border, gr);
								tempUnitList.removeEvent(item2);
								break;
							}
						}
					}

					// if item is not shown on the screen, ship drawing
					if (item.getCurrentTimeStamp() == null
							|| item.getCurrentTimeStamp().getTime() < clipLeftTs2
							|| item.getCurrentTimeStamp().getTime() > clipRightTs2)
						continue;

					// for botteleneck
					if (dcOptionPanel.getComponentType().equals(
							DottedChartPanel.ST_INST)
							&& bBottleneck
							&& olditem != null
							&& (item.getCurrentTimeStamp().getTime() - olditem
									.getCurrentTimeStamp().getTime()) >= percentileL
							&& (item.getCurrentTimeStamp().getTime() - olditem
									.getCurrentTimeStamp().getTime()) <= percentileU) {
						paintItemLineBottleneck(time2coord(olditem
								.getCurrentTimeStamp())
								+ border, currentTop, time2coord(item
								.getCurrentTimeStamp())
								+ border, gr);
						this.paintHighligtedItem(time2coord(olditem
								.getCurrentTimeStamp())
								+ border, currentTop, gr,
								assignShapeByItem(olditem));
						this.paintHighligtedItem(time2coord(item
								.getCurrentTimeStamp())
								+ border, currentTop, gr,
								assignShapeByItem(item));
						this.addExportingList(item);
						olditem = item;
						continue;
					}
					// paint an item
					if (bInstances) {
						this.paintHighligtedItem(time2coord(item
								.getCurrentTimeStamp())
								+ border, currentTop, gr,
								assignShapeByItem(item));
						if (flag) {
							this.addExportingList(item);
							flag = false;
						}
					} else {
						this.paintItem(time2coord(item.getCurrentTimeStamp())
								+ border, currentTop, gr,
								assignShapeByItem(item));
					}
					olditem = item;
				}
				// move y point
				index++;
			}
		}
		// to do box for zoom
		if (p1 != null && p2 != null) {
			int x1 = Math.min(p1.x, p2.x);
			int y1 = Math.min(p1.y, p2.y);
			int width = Math.abs(p1.x - p2.x);
			height = Math.abs(p1.y - p2.y);
			grx.drawRect(x1, y1, width, height);
		}
		// for exporting
		if (selectedIDIndices.size() > 0) {
			int tempArray[] = new int[selectedIDIndices.size()];
			int i = 0;
			for (Iterator itr = selectedIDIndices.iterator(); itr.hasNext();) {
				tempArray[i++] = (int) ((Integer) itr.next());
			}
			dca.setSelectedInstanceIndicesfromScreen(tempArray);
		}
	}

	public BufferedImage getBufferedImage() {
		return matrixBuffer;
	}

	public void generateBufferedImage(int width, int height) {
		bDrawLine = dca.getSettingPanel().isDrawLine();
		matrixBuffer = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D gr = matrixBuffer.createGraphics();

		gr.setColor(colorBg);
		gr.fillRect(0, 0, width, height);

		paintComponentLane(gr, width, height);

		// calculate are to be painted
		int unitHeight = height / getHashMapSize();
		int currentTop = 0;

		// paint items
		if (dcModel.getItemMap().size() > 0) {
			String key = null;
			AbstractLogUnit item = null;
			// iterate through sets
			int index = 0;
			for (Iterator itSets = dcModel.getSortedKeySetList().iterator(); itSets
					.hasNext();) {
				key = (String) itSets.next();
				// if key is not in instanceIDs, skip..
				if (dcModel.getTypeHashMap().equals(ST_INST)
						&& !dcModel.getInstanceTypeToKeep().contains(key))
					continue;

				currentTop = unit2Cord_buffer(index, height) + unitHeight / 2;

				LogUnitList tempUnitList = new LogUnitList();

				// iterate through items
				for (Iterator itItm = ((LogUnitList) dcModel.getItemMap().get(
						key)).iterator(); itItm.hasNext();) {
					item = (AbstractLogUnit) itItm.next();
					if (dcModel.getEventTypeToKeep() != null
							&& (!dcModel.getEventTypeToKeep().contains(
									item.getType()) || !dcModel
									.getInstanceTypeToKeep()
									.contains(
											item.getProcessInstance().getName())))
						continue;
					// to do
					if (bDrawLine
							&& item.getType().equals(
									dca.getSettingPanel().getStartEvent()))
						tempUnitList.addEvent(item);
					assignColorByItem(item, gr);
					clipL = 0;
					clipR = width;

					double milli2pixels = (double) ((width) - 10)
							/ (double) (dcModel.getLogBoundaryRight().getTime() - dcModel
									.getLogBoundaryLeft().getTime()); // + 2 is
					// added
					// to
					// extend
					// bound

					long clipLeftTs2 = coord2timeMillis_buffer(clipL,
							milli2pixels);
					long clipRightTs2 = coord2timeMillis_buffer(clipR,
							milli2pixels);
					// to do : if line is added
					if (bDrawLine
							&& item.getType().equals(
									dca.getSettingPanel().getEndEvent())) {
						for (Iterator itr = tempUnitList.iterator(); itr
								.hasNext();) {
							AbstractLogUnit item2 = (AbstractLogUnit) itr
									.next();
							if (item2.getElement().equals(item.getElement())
									&& item2.getProcessInstance().equals(
											item.getProcessInstance())) {
								paintItemLine_buffer(
										time2coord_buffer(item2
												.getCurrentTimeStamp(),
												milli2pixels) + 3, currentTop,
										time2coord_buffer(item
												.getCurrentTimeStamp(),
												milli2pixels) + 3, gr);
								tempUnitList.removeEvent(item2);
								break;
							}
						}
					}

					// if item is not shown on the screen, skip drawing
					if (item.getCurrentTimeStamp().getTime() < clipLeftTs2
							|| item.getCurrentTimeStamp().getTime() > clipRightTs2)
						continue;
					// paint an item
					this.paintItem_buffer(time2coord_buffer(item
							.getCurrentTimeStamp(), milli2pixels) + 3,
							currentTop, gr, assignShapeByItem(item));

				}
				// move y point
				index++;
			}
		}
	}

	public String timeFormat(Date aDate) {
		String str = null;

		if (timeOption.equals(TIME_ACTUAL))
			str = String.valueOf(aDate);
		else if (timeOption.equals(TIME_RELATIVE_TIME))
			str = String.valueOf((float) aDate.getTime() / (float) timeSort);
		else if (timeOption.equals(TIME_RELATIVE_RATIO))
			str = String.valueOf((float) aDate.getTime() / 100) + "%";
		else if (timeOption.equals(TIME_LOGICAL)
				|| timeOption.equals(TIME_LOGICAL_RELATIVE))
			str = String.valueOf(aDate.getTime());
		return str;
	}

	public void redrawTitle(int xPos) {
		Graphics g = this.getGraphics();
		Color fgColor = null;
		Color bgColor = null;
		Color tmpColor = null;
		fgColor = null;
		bgColor = null;
		// calculate common coordinates
		int yTop = border;

		// initialze start color
		fgColor = colorLogDark;
		bgColor = colorLogBright;

		// calculate current top
		int currentTop = yTop;

		// paint actual log lane (only the part in the clipping range
		// determined)
		Iterator itr = dcModel.getSortedKeySetList().iterator();
		g.setFont(new Font("Dialog", Font.BOLD, 13));
		int index = 0;
		currentTop = yTop;
		while (itr.hasNext()) {
			String dimName = (String) itr.next();
			if (dcModel.getTypeHashMap().equals(ST_INST)
					&& !dcModel.getInstanceTypeToKeep().contains(dimName))
				continue;

			// g.setColor(fgColor);
			g.setColor(Color.black);
			g.drawString(dimName, xPos + 5, currentTop + 20);

			index++;
			currentTop = unit2Cord(index);

			// swap colors
			tmpColor = fgColor;
			fgColor = bgColor;
			bgColor = tmpColor;
		}

	}

	// ////////////////////MOUSE LISTENER AND TOOLTIP METHODS/////////////////
	/**
	 * Shows information in a tooltip about the sequence over which the mouse
	 * moved.
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseMoved(MouseEvent e) {

		Point p = e.getPoint();

		String str = "<html>";
		int height = (int) ((double) (getHeight() - (2 * border)));
		int unitHeight = height / getHashMapSize();
		int currentTop = 0;

		String key = null;
		AbstractLogUnit item = null;
		boolean flag = false;
		// for dot
		int index = -1;
		for (Iterator itSets = dcModel.getSortedKeySetList().iterator(); itSets
				.hasNext();) {
			key = (String) itSets.next();

			if (dcModel.getTypeHashMap().equals(ST_INST)
					&& !dcModel.getInstanceTypeToKeep().contains(key))
				continue;
			index++;
			LogUnitList tempLogUnit = (LogUnitList) dcModel.getItemMap().get(
					key);
			currentTop = unit2Cord(index) + unitHeight / 2;
			if (currentTop - 5 >= p.getY() || p.getY() >= currentTop + 5)
				continue;
			// get the descriptiveStatistics object
			for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext();) {
				item = (AbstractLogUnit) itItm.next();
				if (dcModel.getEventTypeToKeep() != null
						&& (!dcModel.getEventTypeToKeep().contains(
								item.getType()) || !dcModel
								.getInstanceTypeToKeep().contains(
										item.getProcessInstance().getName())))
					continue;
				int x = 0;

				x = time2coord(item.getCurrentTimeStamp()) + border;
				if (x - 5 <= p.getX() && p.getX() <= x + 5) {
					flag = true;
					str += item.getProcessInstance().getName() + " | "
							+ item.getElement() + " | " + item.getOriginator()
							+ " | " + item.getTimestamp() + " | "
							+ item.getType();
					if (!timeOption.equals(TIME_ACTUAL))
						str += " | Position:"
								+ timeFormat(item.getCurrentTimeStamp());
					str += "<br>";
				}
			}
		}
		// for item line
		index = -1;
		for (Iterator itSets = dcModel.getSortedKeySetList().iterator(); itSets
				.hasNext();) {
			key = (String) itSets.next();

			if (dcModel.getTypeHashMap().equals(ST_INST)
					&& !dcModel.getInstanceTypeToKeep().contains(key))
				continue;
			index++;
			LogUnitList tempLogUnit = (LogUnitList) dcModel.getItemMap().get(
					key);
			currentTop = unit2Cord(index) + unitHeight / 2;
			if (currentTop - 5 >= p.getY() || p.getY() >= currentTop + 5)
				continue;
			LogUnitList tempUnitList = new LogUnitList();
			// get the descriptiveStatistics object
			for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext();) {
				item = (AbstractLogUnit) itItm.next();
				if (dcModel.getEventTypeToKeep() != null
						&& (!dcModel.getEventTypeToKeep().contains(
								item.getType()) || !dcModel
								.getInstanceTypeToKeep().contains(
										item.getProcessInstance().getName())))
					continue;
				if (bDrawLine
						&& item.getType().equals(
								dca.getSettingPanel().getStartEvent())) {
					tempUnitList.addEvent(item);
					continue;
				}
				if (bDrawLine
						&& item.getType().equals(
								dca.getSettingPanel().getEndEvent())) {
					for (Iterator itr = tempUnitList.iterator(); itr.hasNext();) {
						AbstractLogUnit item2 = (AbstractLogUnit) itr.next();
						if (item2.getElement().equals(item.getElement())
								&& item2.getProcessInstance().equals(
										item.getProcessInstance())) {
							int x1 = time2coord(item2.getCurrentTimeStamp())
									+ border;
							int x2 = time2coord(item.getCurrentTimeStamp())
									+ border;
							if (x1 + 5 <= p.getX() && p.getX() <= x2 - 5) {
								flag = true;
								str += item.getProcessInstance().getName()
										+ " | " + item.getElement() + " | "
										+ item.getOriginator() + " |(start)"
										+ item.getTimestamp() + "-(complete)"
										+ item2.getTimestamp() + "<br>";
								tempUnitList.removeEvent(item2);
							}
							break;
						}
					}
				}
			}
		}

		str += "</html>";
		if (flag) {
			this.setToolTipText(str);
			repaint();
			bTooltip = true;
		} else {
			this.setToolTipText(null);
			if (bTooltip) {
				repaint();
				bTooltip = false;
			}
		}
	}

	/**
	 * Required for mouselistener
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseDragged(MouseEvent e) {
		Point p = e.getPoint();

		if (dcOptionPanel.getMouseMode().equals(ST_DRAG)) {
			int x2 = p4.x - (p.x - p3.x);
			int y2 = p4.y - (p.y - p3.y);
			if (x2 < 0)
				x2 = 0;
			if (y2 < 0)
				y2 = 0;
			dca.setScrollBarPosition(new Point(x2, y2));
		} else {
			p2 = e.getPoint();
			repaint();
		}
	}

	// /MOUSE Listener ///////////////////////////////////////////
	public void mousePressed(MouseEvent e) {
		if (!clicked && dcOptionPanel.getMouseMode().equals(ST_ZOOMIN)) {
			clicked = true;
			p1 = e.getPoint();
			p2 = e.getPoint();
			repaint();
		} else {
			p3 = e.getPoint();
			p4 = dca.getScrollPane().getViewport().getViewPosition();
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (dcOptionPanel.getMouseMode().equals(ST_ZOOMIN)) {
			clicked = false;
			p2 = e.getPoint();
			setViewportZoomIn();
			dca.getOverviewPanel().setDrawBox(true);
			dca.getOverviewPanel().repaint();
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void drawSelectionBox(Point p2) {
		Graphics g = this.getGraphics();
		int x1 = Math.min(p1.x, p2.x);
		int y1 = Math.min(p1.y, p2.y);
		int width = Math.abs(p1.x - p2.x);
		int height = Math.abs(p1.y - p2.y);
		g.drawRect(x1, y1, width, height);
		repaint();
	}
}
