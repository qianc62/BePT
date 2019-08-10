/*
 * Created on July. 10, 2008
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

package org.processmining.analysis.performance.advanceddottedchartanalysis.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import javax.swing.ToolTipManager;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.processmining.analysis.performance.advanceddottedchartanalysis.DottedChartAnalysis;
import org.processmining.analysis.performance.advanceddottedchartanalysis.model.DottedChartModel;

/**
 * DottedChartPanel.
 * 
 * The purpose of this class is to provide a view of dotted chart
 * 
 * @author Minseok Song
 */
public class DottedChartPanel extends JPanel implements MouseListener,
		MouseMotionListener {

	private static final long serialVersionUID = -2720292789575028502L;

	public static Color ColorBg = new Color(120, 120, 120);
	public static Color ColorInnerBg = new Color(140, 140, 140);
	public static Color ColorFg = new Color(30, 30, 30);
	public static Color ColorTextAreaBg = new Color(160, 160, 160);

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

	public static final String ST_NAME = "Component Name";
	public static final String ST_SIZE = "Number of Events";
	public static final String ST_SPAN = "Duration";
	public static final String ST_FIRST_EVENT = "First Event";
	public static final String ST_LAST_EVENT = "Last Event";
	public static final String ST_DURATION = "Actual duration";
	public static final String ST_START_TIME = "Actual start time";
	public static final String ST_END_TIME = "Actual end time";

	public static final String ST_ZOOMIN = "Zoom in";
	public static final String ST_DRAG = "Drag";
	public static final String ST_SELECT = "Select";
	protected BufferedImage modelBuffer = null;
	protected static final int SCREENLENGTH = 3000;
	protected static final int SCREENSIZE = SCREENLENGTH * SCREENLENGTH;

	protected long timeOffset;
	public static String HANDLE_ICONS[] = { STR_NONE, ITEM_HANDLE_CIRCLE,
			ITEM_HANDLE_TRIANGLE, ITEM_HANDLE_BOX, ITEM_HANDLE_RHOMBUS };

	protected DottedChartAnalysis dca = null; // parents
	protected DottedChartOptionPanel dcop = null;
	protected UIUtil uiUtil = new UIUtil();
	protected CoordinationUtil coUtil = null;
	/* instance attributes */
	protected DottedChartModel dcModel = null;
	protected double viewportZoomX = 1.0;
	protected double viewportZoomY = 1.0;
	protected int updWidth = 0;
	protected int updHight = 0;
	protected String itemHandle = null;
	protected Color colorBg = null;
	protected Color colorFg = null;
	// protected Color colorLogDark = null;
	// protected Color colorLogBright = null;
	protected Color colorTimeLine = null;
	protected Color colorBaseGreen = null;
	protected ShapeReference itemShapes = null;
	protected ColorReference itemColors = null;

	private BufferedImage overviewBuffer;

	// options
	private long[] widthDividers = { 1, 10, 100, 500, 1000, 60000, 3600000L,
			86400000L, 604800000L, 2592000000L, 31536000000L };

	// color and shapes
	int[] colorCodes;
	ArrayList<String> colorItems;
	int[] shapeCodes;
	ArrayList<String> shapeItems;

	/* size attributes */
	protected static int BORDER = 5;
	protected static int BUFFERBORDER = 0;
	protected double handleRatio = 0.5;

	/* helpers */
	protected Calendar cal = null;

	// variables for Item Line
	protected boolean bDrawLine = false;

	// variables for finding bottleneck
	protected boolean bBottleneck = false;
	protected boolean bBottleneckforInstances = false;

	// variables for zoom
	private Point p1;
	private Point p2;
	private boolean clicked = false;
	// variables for drag
	private Point p3;
	private Point p4;
	private int initDelay = 0;
	private int dismissDelay = 1000 * 60 * 5;

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
		dcop = aDCA.getDottedChartOptionPanel();
		coUtil = new CoordinationUtil(dcop, dcModel);
		dcModel.initTimeStatistics(dca.getDottedChartOptionPanel()
				.getComponentType());
		viewportZoomX = 1.0;
		viewportZoomY = 1.0;
		BORDER = 10;
		handleRatio = 0.5;
		updWidth = this.getWidth();
		updHight = this.getHeight();
		itemHandle = DottedChartPanel.ITEM_HANDLE_CIRCLE;
		colorBg = new Color(240, 240, 220);
		colorFg = new Color(10, 10, 10);
		// colorLogDark = aDCA.getSettingPanel().getFBcolor();
		// colorLogBright = new Color(210, 210, 200);
		colorTimeLine = new Color(225, 225, 225);
		colorBaseGreen = new Color(50, 100, 100);
		cal = new GregorianCalendar();
		itemColors = new ColorReference();
		itemShapes = new ShapeReference();
		coUtil.updateMilli2pixelsRatio(this.getWidth(), BORDER);
		timeOffset = dcModel.getTimeOffset();
		this.setAutoscrolls(true); // enable synthetic drag events
		this.setOpaque(true);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		ToolTipManager.sharedInstance().registerComponent(this);
		ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);
		ToolTipManager.sharedInstance().setInitialDelay(initDelay);
		ToolTipManager.sharedInstance().setReshowDelay(initDelay);
	}

	// /////////////////////////////////////////////////////////////////////////////////
	//
	// PUBLIC METHODS
	//
	public void changeDots() {
		dcModel.initTimeStatistics(dca.getDottedChartOptionPanel()
				.getComponentType());
		coUtil.updateMilli2pixelsRatio(this.getWidth(), BORDER);
	}

	public ArrayList<DescriptiveStatistics> getDescriptiveStatistics() {
		return dcModel.getTimeStatistics();
	}

	public ColorReference getColorReference() {
		return itemColors;
	}

	/**
	 * overwritten as proxy, to update mill2pixels ratio
	 */
	public void setSize(int width, int height) {
		super.setSize(width, height);
		coUtil.updateMilli2pixelsRatio(this.getWidth(), BORDER);
		initBufferedImage();
		revalidate();
	}

	/**
	 * overwritten as proxy, to update mill2pixels ratio
	 */
	public void setSize(Dimension d) {
		super.setSize(d);
		coUtil.updateMilli2pixelsRatio(this.getWidth(), BORDER);
		initBufferedImage();
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
		updWidth = (int) (getParent().getWidth() * (1.0 / aZoom));
		ratio = updWidth / ratio;
		Dimension dim = new Dimension(updWidth, updHight);
		Point d = dca.getScrollPane().getViewport().getViewPosition();
		Point p = new Point((int) (d.getX() * ratio), (int) (d.getY()));

		this.setPreferredSize(dim);
		coUtil.updateMilli2pixelsRatio(this.getWidth(), BORDER);

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
		updHight = (int) (getParent().getHeight() * (1.0 / aZoom));
		ratio = updHight / ratio;
		Dimension dim = new Dimension(updWidth, updHight);
		Point d = dca.getScrollPane().getViewport().getViewPosition();
		Point p = new Point((int) d.getX(), (int) (d.getY() * ratio));
		this.setPreferredSize(dim);
		coUtil.updateMilli2pixelsRatio(this.getWidth(), BORDER);

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

		int value = (int) (Math.log10(this.getWidth() * (d.getWidth() / width)
				/ dca.getViewportSize().getWidth()) * 1000.0);
		if (value > 3000)
			return;
		value = (int) (Math.log10(this.getHeight() * (d.getHeight() / height)
				/ dca.getViewportSize().getHeight()) * 1000.0);
		if (value > 3000)
			return;

		updWidth = (int) (this.getWidth() * (d.getWidth() / width));
		updHight = (int) (this.getHeight() * (d.getHeight() / height));
		Dimension dim = new Dimension(updWidth, updHight);
		int pos_x = Math.min(p1.x, p2.x);
		int pos_y = Math.min(p1.y, p2.y);

		Point p = new Point((int) (pos_x * d.getWidth() / width), (int) (pos_y
				* d.getHeight() / height));
		this.setPreferredSize(dim);
		coUtil.updateMilli2pixelsRatio(this.getWidth(), BORDER);
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

		int value = (int) (Math.log10(this.getWidth() * (d.getWidth() / width)
				/ dca.getViewportSize().getWidth()) * 1000.0);
		if (value > 3000)
			return null;
		value = (int) (Math.log10(this.getHeight() * (d.getHeight() / height)
				/ dca.getViewportSize().getHeight()) * 1000.0);
		if (value > 3000)
			return null;

		updWidth = (int) (this.getWidth() * (d.getWidth() / width));
		updHight = (int) (this.getHeight() * (d.getHeight() / height));

		Dimension dim = new Dimension(updWidth, updHight);
		int pos_x = Math.min(p1.x, p2.x);
		int pos_y = Math.min(p1.y, p2.y);

		this.setPreferredSize(dim);
		coUtil.updateMilli2pixelsRatio(this.getWidth(), BORDER);
		this.revalidate();
		p1 = null;
		p2 = null;
		adjustSlideBar();
		return new Point((int) (pos_x * d.getWidth() / width), (int) (pos_y
				* d.getHeight() / height));
	}

	public void adjustSlideBar() {
		int value = (int) (Math.log10(updWidth
				/ dca.getViewportSize().getWidth()) * 1000.0);

		if (value > 3000)
			value = 3000;
		dca.getDottedChartOptionPanel().getZoomSliderX().setValue(value);
		dca.getDottedChartOptionPanel().getZoomSliderX().repaint();

		value = (int) (Math.log10(updHight / dca.getViewportSize().getHeight()) * 1000.0);
		if (value > 3000)
			value = 3000;
		dca.getDottedChartOptionPanel().getZoomSliderY().setValue(value);
		dca.getDottedChartOptionPanel().getZoomSliderY().repaint();
	}

	public void initBufferedImage() {
		modelBuffer = null;
	}

	/**
	 * convenience method. adjust width on the screen
	 * 
	 * @param void
	 * @return
	 */
	public void adjustWidth() {
		long widthSort = dca.getDottedChartOptionPanel().getWidthDivider();

		int clipL = coUtil.getClipL();
		int clipR = coUtil.getClipR();
		long clipLeftTs = coUtil.coord2timeMillis(clipL);
		long clipRightTs = coUtil.coord2timeMillis(clipR);
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
				dca.getDottedChartOptionPanel().changeWidthSort(0);
			else
				dca.getDottedChartOptionPanel().changeWidthSort(idx);
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
				dca.getDottedChartOptionPanel().changeWidthSort(
						widthDividers.length - 1);
			else
				dca.getDottedChartOptionPanel().changeWidthSort(idx);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Painting charts
	//
	/**
	 * paints this log item panel and all contained log items as specified.
	 * 
	 * @param g
	 *            the graphics object used for painting
	 */
	public void paintComponent(Graphics grx) {
		Graphics gr = grx.create();

		if (this.isOpaque()) {
			gr.setColor(colorBg);
			gr.fillRect(0, 0, getWidth(), getHeight());
		}

		if ((int) gr.getClipBounds().getMinX() < coUtil.getClipL()
				|| (int) gr.getClipBounds().getMaxX() > coUtil.getClipR()
				|| (int) gr.getClipBounds().getMinY() < coUtil.getClipU()
				|| (int) gr.getClipBounds().getMaxY() > coUtil.getClipB()) {
			modelBuffer = null;
		}

		if (modelBuffer == null) {
			this.setToolTipText(null);

			if (this.getWidth() <= SCREENLENGTH) {
				coUtil.setClipL(0);
				coUtil.setClipR(this.getWidth());
			} else {
				int x1 = (SCREENLENGTH - gr.getClipBounds().width) / 2;
				int x2 = (int) gr.getClipBounds().getMinX() - x1;
				if (x2 <= 0) {
					coUtil.setClipL(0);
					coUtil.setClipR(SCREENLENGTH);
				} else if (x2 + SCREENLENGTH >= this.getWidth()) {
					coUtil.setClipR(this.getWidth());
					coUtil.setClipL(this.getWidth() - SCREENLENGTH);
				} else {
					coUtil.setClipL(x2);
					coUtil.setClipR(x2 + SCREENLENGTH);
				}
			}
			if (this.getHeight() <= SCREENLENGTH) {
				coUtil.setClipU(0);
				coUtil.setClipB(this.getHeight());
			} else {
				int y1 = (SCREENLENGTH - gr.getClipBounds().height) / 2;
				int y2 = (int) gr.getClipBounds().getMinY() - y1;
				if (y2 <= 0) {
					coUtil.setClipU(0);
					coUtil.setClipB(SCREENLENGTH);
				} else if (y2 + SCREENLENGTH >= this.getHeight()) {
					coUtil.setClipU(this.getHeight() - SCREENLENGTH);
					coUtil.setClipB(this.getHeight());
				} else {
					coUtil.setClipU(y2);
					coUtil.setClipB(y2 + SCREENLENGTH);
				}
			}
			coUtil.updateMilli2pixelsRatio(getWidth(), BORDER);

			if (dcop.isSizeCheckBoxSelected()) {
				drawModelBuffer(getWidth(), getHeight(), true);
			} else {
				drawModelBuffer_NO_SIZE(getWidth(), getHeight(), true);
			}
		}
		grx.drawImage(modelBuffer, coUtil.getClipL(), coUtil.getClipU(), this);

		// to do box for zoom
		if (p1 != null && p2 != null) {
			int x1 = Math.min(p1.x, p2.x);
			int y1 = Math.min(p1.y, p2.y);
			int width = Math.abs(p1.x - p2.x);
			int height = Math.abs(p1.y - p2.y);
			grx.drawRect(x1, y1, width, height);
		}
	}

	/*
	 * public void paintComponent(Graphics grx) { Graphics gr = grx.create();
	 * 
	 * if (this.isOpaque()) { gr.setColor(colorBg); gr.fillRect(0, 0,
	 * getWidth(), getHeight()); }
	 * 
	 * 
	 * 
	 * 
	 * if((int)gr.getClipBounds().getMinX()<coUtil.getClipL()||(int)gr.getClipBounds
	 * ().getMaxX()>coUtil.getClipR() ||
	 * (int)gr.getClipBounds().getMinY()<coUtil
	 * .getClipU()||(int)gr.getClipBounds().getMaxY()>coUtil.getClipB()){
	 * modelBuffer = null; }
	 * 
	 * if(modelBuffer==null){ this.setToolTipText(null);
	 * if(dcop.isSizeCheckBoxSelected()) { if(this.getWidth()<=SCREENLENGTH) {
	 * coUtil.setClipL(0); coUtil.setClipR(this.getWidth()); } else { int x1 =
	 * (SCREENLENGTH - gr.getClipBounds().width)/2; int x2 =
	 * (int)gr.getClipBounds().getMinX() - x1; if(x2<=0) { coUtil.setClipL(0);
	 * coUtil.setClipR(SCREENLENGTH); } else
	 * if(x2+SCREENLENGTH>=this.getWidth()) { coUtil.setClipR(this.getWidth());
	 * coUtil.setClipL(this.getWidth()-SCREENLENGTH); } else {
	 * coUtil.setClipL(x2); coUtil.setClipR(x2+SCREENLENGTH); } }
	 * if(this.getHeight()<=SCREENLENGTH) { coUtil.setClipU(0);
	 * coUtil.setClipB(this.getHeight()); } else { int y1 = (SCREENLENGTH -
	 * gr.getClipBounds().height)/2; int y2 = (int)gr.getClipBounds().getMinY()
	 * - y1; if(y2<=0) { coUtil.setClipU(0); coUtil.setClipB(SCREENLENGTH); }
	 * else if(y2+SCREENLENGTH>=this.getHeight()) {
	 * coUtil.setClipU(this.getHeight()-SCREENLENGTH);
	 * coUtil.setClipB(this.getHeight()); } else { coUtil.setClipU(y2);
	 * coUtil.setClipB(y2+SCREENLENGTH); } }
	 * coUtil.updateMilli2pixelsRatio(getWidth(), BORDER);
	 * drawModelBuffer(getWidth(),getHeight(), true); } else {
	 * drawModelBuffer_NO_SIZE(getWidth(),getHeight(), true); } }
	 * grx.drawImage(modelBuffer, coUtil.getClipL(), coUtil.getClipU(), this);
	 * 
	 * // to do box for zoom if(p1!=null&&p2!=null){ int x1 = Math.min(p1.x,
	 * p2.x); int y1 = Math.min(p1.y, p2.y); int width = Math.abs(p1.x - p2.x);
	 * int height = Math.abs(p1.y - p2.y); grx.drawRect(x1, y1, width, height);
	 * } }
	 */

	protected Graphics2D drawModelBuffer_Common(int imageWidth,
			int imageHeight, boolean isModel) {

		Graphics2D gBuf = null;
		if (isModel) {
			modelBuffer = new BufferedImage(Math.min(imageWidth, SCREENLENGTH),
					Math.min(imageHeight, SCREENLENGTH),
					BufferedImage.TYPE_INT_RGB);
			gBuf = modelBuffer.createGraphics();
		} else {
			overviewBuffer = new BufferedImage(imageWidth, imageHeight,
					BufferedImage.TYPE_INT_RGB);
			gBuf = overviewBuffer.createGraphics();
		}

		gBuf.setColor(colorBg);
		gBuf.fillRect(0, 0, imageWidth, imageHeight);
		// draw background
		paintComponentLane(gBuf, imageWidth, imageHeight, isModel);
		if (isModel) {
			drawVerticalLines(gBuf, imageWidth, imageHeight);
		}
		// paint items
		initColorAndShapes();
		return gBuf;
	}

	protected long getLeftRight(int k) {
		if (dcop.getTimeOption().equals(DottedChartModel.TIME_ACTUAL))
			return ((coUtil.coord2timeMillis(k) + timeOffset) / dcModel
					.getProperTimeUnit(dcop.getTimeOption(), coUtil
							.getMilli2pixels()));
		if (dcop.getTimeOption().equals(DottedChartModel.TIME_RELATIVE_RATIO)
				|| dcop.getTimeOption().equals(
						DottedChartModel.TIME_RELATIVE_TIME))
			return ((coUtil.coord2timeMillis(k)) / dcModel.getProperTimeUnit(
					dcop.getTimeOption(), coUtil.getMilli2pixels()));
		if (dcop.getTimeOption().equals(DottedChartModel.TIME_LOGICAL_RELATIVE))
			return coUtil.coord2timeMillis(k);
		return 0;
	}

	protected void drawModelBuffer(int imageWidth, int imageHeight,
			boolean isModel) {
		int tempBorder;
		int compNumber = dcModel.getComponentSize(dca
				.getDottedChartOptionPanel().getComponentType());
		if (isModel) {
			tempBorder = BORDER;
		} else {
			tempBorder = BUFFERBORDER;
		}

		double milli2pixelsBuffer = coUtil.updateMilli2pixelsRatioBuffer(
				imageWidth, tempBorder);
		Graphics2D gBuf = drawModelBuffer_Common(imageWidth, imageHeight,
				isModel);
		int unitHeight = coUtil.getUnitHeight(imageHeight, tempBorder);

		HashMap<String, HashMap<String, ArrayList<Integer>>> timeMap = dcModel
				.getProperTimeMap(dcop.getTimeOption()
						+ dcop.getComponentType(), coUtil.getMilli2pixels());

		long left, right;
		int upper, bottom;

		if (isModel) {
			left = getLeftRight(coUtil.getClipL());
			right = getLeftRight(coUtil.getClipR());
			upper = coUtil.getClipU();
			bottom = coUtil.getClipB();
		} else {
			left = getLeftRight(0);
			right = getLeftRight(this.getWidth());
			upper = 0;
			bottom = this.getHeight();
		}
		int currentTop = 0;
		// currentTop is different. But the performance reason, the code is
		// separated.
		if (dcop.getSortStandard().equals(DottedChartModel.STR_NONE)) {
			for (long j = left; j <= right; j++) {
				HashMap<String, ArrayList<Integer>> tempMap = timeMap
						.get(String.valueOf(j));
				if (tempMap == null)
					continue;
				for (String str : tempMap.keySet()) {
					currentTop = coUtil.unit2CordHeight(Integer.valueOf(str),
							compNumber, imageHeight, tempBorder)
							+ unitHeight / 2;
					if (currentTop < upper || currentTop > bottom)
						continue;
					assignColorByItem(tempMap.get(str), gBuf);
					long temp = j
							* dcModel.getProperTimeUnit(dcop.getTimeOption(),
									coUtil.getMilli2pixels());
					// if(dcop.getTimeOption().equals(DottedChartModel.TIME_ACTUAL))
					// temp -= timeOffset;
					if (isModel) {
						uiUtil.paintItemHigh(coUtil.time2coord(temp)
								+ tempBorder - coUtil.getClipL(), currentTop
								- upper, gBuf, ITEM_HANDLE_CIRCLE, tempMap.get(
								str).size());
					} else {
						uiUtil.paintItem_buffer(coUtil.time2coordOverview(temp,
								milli2pixelsBuffer)
								+ tempBorder, currentTop, gBuf,
								ITEM_HANDLE_CIRCLE, tempMap.get(str).size());
					}

				}
			}
		} else {
			HashMap<Integer, Integer> mapping = dcModel.getSortedMapModel()
					.getSortedMapping(dcop.getTimeOption(),
							dcop.getComponentType(), dcop.getSortStandard(),
							dcop.isDescCheckBoxSelected());
			for (long j = left; j <= right; j++) {
				HashMap<String, ArrayList<Integer>> tempMap = timeMap
						.get(String.valueOf(j));
				if (tempMap == null)
					continue;
				for (String str : tempMap.keySet()) {
					currentTop = coUtil.unit2CordHeight(mapping
							.get((int) Integer.valueOf(str)), compNumber,
							imageHeight, tempBorder)
							+ unitHeight / 2;
					if (currentTop < upper || currentTop > bottom)
						continue;
					assignColorByItem(tempMap.get(str), gBuf);
					long temp = j
							* dcModel.getProperTimeUnit(dcop.getTimeOption(),
									coUtil.getMilli2pixels());
					// if(dcop.getTimeOption().equals(DottedChartModel.TIME_ACTUAL))
					// temp -= timeOffset;
					if (isModel) {
						uiUtil.paintItemHigh(coUtil.time2coord(temp)
								+ tempBorder - coUtil.getClipL(), currentTop
								- upper, gBuf, ITEM_HANDLE_CIRCLE, tempMap.get(
								str).size());
					} else {
						uiUtil.paintItem_buffer(coUtil.time2coordOverview(temp,
								milli2pixelsBuffer)
								+ tempBorder, currentTop, gBuf,
								ITEM_HANDLE_CIRCLE, tempMap.get(str).size());
					}
				}
			}
		}
	}

	protected void drawModelBuffer_NO_SIZE(int imageWidth, int imageHeight,
			boolean isModel) {
		int tempBorder;
		int size = dcModel.getComponentSize(dca.getDottedChartOptionPanel()
				.getComponentType());
		if (isModel) {
			tempBorder = BORDER;
		} else {
			tempBorder = BUFFERBORDER;
		}
		double milli2pixelsBuffer = coUtil.updateMilli2pixelsRatioBuffer(
				imageWidth, tempBorder);
		Graphics2D gBuf = drawModelBuffer_Common(imageWidth, imageHeight,
				isModel);
		int unitHeight = coUtil.getUnitHeight(imageHeight, tempBorder);
		// paint items
		long[] items = dcModel.getTimeList(dcop.getTimeOption());
		int[] intCodes = dcModel.getSortedMapModel().getSortedCode(
				dcop.getTimeOption(), dcop.getComponentType(),
				dcop.getSortStandard(), dcop.isDescCheckBoxSelected());
		long clipLeftTs, clipRightTs;
		int upper, bottom;
		if (isModel) {
			clipLeftTs = coUtil.coord2timeMillis(coUtil.getClipL());
			clipRightTs = coUtil.coord2timeMillis(coUtil.getClipR());
			upper = coUtil.getClipU();
			bottom = coUtil.getClipB();
		} else {
			clipLeftTs = coUtil.coord2timeMillis(0);
			clipRightTs = coUtil.coord2timeMillis(this.getWidth());
			upper = 0;
			bottom = this.getHeight();
		}

		int currentTop = 0;
		for (int k = 0; k < items.length; k++) {
			if (items[k] < clipLeftTs || items[k] > clipRightTs)
				continue;
			currentTop = coUtil.unit2CordHeight(intCodes[k], size, imageHeight,
					tempBorder)
					+ unitHeight / 2;
			if (currentTop < upper || currentTop > bottom)
				continue;
			assignColorByItem(k, gBuf);
			if (isModel) {
				uiUtil.paintItem(coUtil.time2coord(items[k]) + tempBorder
						- coUtil.getClipL(), currentTop - upper, gBuf,
						assignShapeByItem(k));
			} else {
				uiUtil.paintItem_buffer(coUtil.time2coordOverview(items[k],
						milli2pixelsBuffer)
						+ tempBorder, currentTop, gBuf, assignShapeByItem(k));
			}
		}
	}

	protected void paintComponentLane(Graphics g, int imageWidth,
			int imageHeight, boolean isModel) {
		g.setFont(g.getFont().deriveFont((float) 10.0));
		// set initial colors
		Color fgColor = null;
		Color bgColor = null;
		Color tmpColor = null;
		fgColor = null;
		bgColor = null;
		int clipL, clipR;

		// calculate common coordinates
		int yTop, yBottom, tempBorder, upper, bottom;
		if (isModel) {
			yTop = BORDER;
			yBottom = imageHeight - BORDER;
			clipL = coUtil.getClipL();
			clipR = coUtil.getClipR();
			upper = coUtil.getClipU();
			bottom = coUtil.getClipB();
			tempBorder = BORDER;
		} else {
			yTop = 0;
			yBottom = imageHeight;
			clipL = 0;
			clipR = imageWidth;
			upper = 0;
			bottom = this.getHeight();
			tempBorder = BUFFERBORDER;
		}

		int pixStart = 0;

		// initialize start color
		fgColor = dca.getSettingPanel().getFBcolor();
		bgColor = dca.getSettingPanel().getSBcolor();
		;

		// paint actual log lane (only the part in the clipping range
		// determined)
		Iterator<String> itr = dcModel.getSortedMapModel()
				.getSortedItemArrayList(dcop.getTimeOption(),
						dcop.getComponentType(), dcop.getSortStandard(),
						dcop.isDescCheckBoxSelected()).iterator();
		g.setFont(new Font("Dialog", Font.BOLD, 13));

		int size = dcModel.getComponentSize(dca.getDottedChartOptionPanel()
				.getComponentType());
		int index = 0;

		while (itr.hasNext()) {
			String dimName = itr.next();
			g.setColor(bgColor);
			int top = coUtil.unit2CordHeight(index, size, imageHeight,
					tempBorder);
			int bot = coUtil.unit2CordHeight(index, size, imageHeight,
					tempBorder);
			if (bot >= upper && top <= bottom) {
				if (top < upper)
					top = upper;
				if (bot > bottom)
					bot = bottom;
				g.fillRect(pixStart, top - coUtil.getClipU(), clipR, bottom
						- coUtil.getClipU());
				g.setColor(fgColor);
				if (isModel) {
					if (top + 20 - coUtil.getClipU() <= imageHeight
							- tempBorder)
						g.drawString(dimName, pixStart + 5, top + 20
								- coUtil.getClipU());
				}
			}
			if (coUtil
					.unit2CordHeight(index + 1, size, imageHeight, tempBorder) > bottom)
				break;
			index++;
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
	}

	protected void drawVerticalLines(Graphics g, int imageWidth, int imageHeight) {
		int pixStart = 0;
		int yTop = BORDER;
		int yBottom = imageHeight - BORDER;
		String dateStr, timeStr;
		// adjust width
		adjustWidth();

		long timeStart = 0;
		if (dcop.getTimeOption().equals(DottedChartModel.TIME_ACTUAL)) {
			timeStart = dcModel.getLogMinValueforScreen();
		}
		long lastLable = Long.MIN_VALUE;
		long clipRightTs = coUtil.coord2timeMillis(coUtil.getClipR());

		for (; timeStart < clipRightTs; timeStart += dca
				.getDottedChartOptionPanel().getWidthDivider()) {
			pixStart = coUtil.time2coord(timeStart) + BORDER;
			cal.setTimeInMillis(timeStart - timeOffset);
			g.setColor(colorTimeLine);
			g.drawLine(pixStart, yTop, pixStart, yBottom);
			g.setColor(dca.getSettingPanel().getFBcolor());
			g.setColor(Color.black); // to be deleted

			if (dcop.getTimeOption().equals(DottedChartModel.TIME_ACTUAL)) {
				// if(lastLable+80>=pixStart+2) continue;
				// timeStr = String.valueOf(timeStart);
				// g.drawString(timeStr, pixStart+2, yTop);

				if (lastLable + 80 >= pixStart + 2)
					continue;
				dateStr = cal.get(Calendar.DAY_OF_MONTH) + "."
						+ (cal.get(Calendar.MONTH) + 1) + "."
						+ cal.get(Calendar.YEAR);
				g.drawString(dateStr, pixStart + 2, yTop);
				timeStr = cal.get(Calendar.HOUR_OF_DAY) + ":"
						+ cal.get(Calendar.MINUTE) + ":"
						+ cal.get(Calendar.SECOND);
				g.drawString(timeStr, pixStart + 2, yTop + 10);
			} else if (dcop.getTimeOption().equals(
					DottedChartModel.TIME_RELATIVE_TIME)) {
				if (lastLable + 80 >= pixStart + 2)
					continue;
				long days = timeStart / 1000 / 60 / 60 / 24;
				long hours = (timeStart - days * 24 * 60 * 60 * 1000) / 1000 / 60 / 60;
				long minutes = (timeStart - days * 24 * 60 * 60 * 1000 - hours * 60 * 60 * 1000) / 1000 / 60;
				long seconds = (timeStart - days * 24 * 60 * 60 * 1000 - hours
						* 60 * 60 * 1000 - minutes * 60 * 1000) / 1000;
				timeStr = days + "days:" + hours + ":" + minutes + ":"
						+ seconds;
				g.drawString(timeStr, pixStart + 2, yTop);
			} else if (dcop.getTimeOption().equals(
					DottedChartModel.TIME_RELATIVE_RATIO)) {
				if (lastLable + 40 >= pixStart + 2)
					continue;
				timeStr = timeStart / 100 + "."
						+ (timeStart - timeStart / 100 * 100) + "%";
				g.drawString(timeStr, pixStart + 2, yTop);
			} else if (dcop.getTimeOption().equals(
					DottedChartModel.TIME_LOGICAL)
					|| dcop.getTimeOption().equals(
							DottedChartModel.TIME_LOGICAL_RELATIVE)) {
				if (lastLable + 20 >= pixStart + 2)
					continue;
				timeStr = String.valueOf(timeStart);
				g.drawString(timeStr, pixStart + 2, yTop);
			}
			lastLable = pixStart + 2;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////
	//
	// PUBLIC METHODS for BUFFEREDIMAGE
	//
	public BufferedImage getBufferedImage() {
		return overviewBuffer;
	}

	public void generateBufferedImage(Graphics grx, int width, int height) {
		overviewBuffer = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D gr = overviewBuffer.createGraphics();

		gr.setColor(colorBg);
		gr.fillRect(0, 0, width, height);

		if (dcop.isSizeCheckBoxSelected()) {
			drawModelBuffer(width, height, false);
		} else {
			drawModelBuffer_NO_SIZE(width, height, false);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////
	//
	// Convenient AND TOOLTIP METHODS
	//
	protected void initColorAndShapes() {
		colorCodes = dcModel.getCode(dca.getDottedChartOptionPanel()
				.getColorStandard());
		colorItems = dcModel.getItemArrayList(dca.getDottedChartOptionPanel()
				.getColorStandard());
		shapeCodes = dcModel.getCode(dca.getDottedChartOptionPanel()
				.getShapeStandard());
		shapeItems = dcModel.getItemArrayList(dca.getDottedChartOptionPanel()
				.getShapeStandard());
	}

	protected String assignShapeByItem(int k) {
		String shapeStandard = dca.getDottedChartOptionPanel()
				.getShapeStandard();
		if (shapeStandard.equals(DottedChartModel.STR_NONE))
			return ITEM_HANDLE_CIRCLE;
		else
			return itemShapes.getShape(shapeItems.get(shapeCodes[k]));
	}

	protected void assignColorByItem(String name, Graphics g) {
		String colorStandard = dca.getDottedChartOptionPanel()
				.getColorStandard();
		if (colorStandard.equals(DottedChartModel.STR_NONE))
			g.setColor(colorBaseGreen);
		else
			g.setColor(itemColors.getColor(name));
	}

	protected void assignColorByItem(ArrayList<Integer> itemlist, Graphics g) {
		String colorStandard = dca.getDottedChartOptionPanel()
				.getColorStandard();
		Color colorArray[] = new Color[itemlist.size()];

		if (colorStandard.equals(DottedChartModel.STR_NONE))
			g.setColor(colorBaseGreen);
		else {
			for (int k = 0; k < itemlist.size(); k++) {
				colorArray[k] = itemColors.getColor(colorItems
						.get(colorCodes[itemlist.get(k)]));
			}
			g.setColor(uiUtil.getAverageColor(colorArray));
		}
	}

	protected void assignColorByItem(int k, Graphics g) {
		String colorStandard = dca.getDottedChartOptionPanel()
				.getColorStandard();
		if (colorStandard.equals(DottedChartModel.STR_NONE))
			g.setColor(colorBaseGreen);
		else
			g.setColor(itemColors.getColor(colorItems.get(colorCodes[k])));
	}

	protected void makeTooltip(Point p1, Point p2) {
		String strTooltip = "";

		if (dcop.isSizeCheckBoxSelected()) {
			strTooltip = hasDots(p1, p2);
		} else {
			strTooltip = hasDots_NO_SIZE(p1, p2);
		}
		if (!clicked) {
			if (!strTooltip.equals("")) {
				strTooltip = "<html>" + strTooltip + "</html>";
				this.setToolTipText(strTooltip);
			} else {
				this.setToolTipText(null);
				this.p1 = null;
				this.p2 = null;
			}
		}
		repaint();
	}

	// /////////////////////////////////////////////////////////////////////////////////
	//
	// MOUSE LISTENER
	//
	/**
	 * Shows information in a tooltip about the sequence over which the mouse
	 * moved.
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseMoved(MouseEvent e) {

	}

	private String hasDots_NO_SIZE(Point p1, Point p2) {
		String strTooltip = "";
		int min_x = Math.min(p1.x, p2.x);
		int max_x = Math.max(p1.x, p2.x);

		int min_y = Math.min(p1.y, p2.y);
		int max_y = Math.max(p1.y, p2.y);
		Graphics2D gBuf = modelBuffer.createGraphics();
		int currentTop = 0;
		int unitHeight = coUtil.getUnitHeight(getHeight(), BORDER);
		int size = dcModel.getComponentSize(dca.getDottedChartOptionPanel()
				.getComponentType());

		long[] items = dcModel.getTimeList(dcop.getTimeOption());
		int[] intCodes = dcModel.getSortedMapModel().getSortedCode(
				dcop.getTimeOption(), dcop.getComponentType(),
				dcop.getSortStandard(), dcop.isDescCheckBoxSelected());
		long clipLeftTs = coUtil.coord2timeMillis(Math.max(0, min_x - 5
				- BORDER));
		long clipRightTs = coUtil.coord2timeMillis(max_x + 5 - BORDER);
		for (int k = 0; k < items.length; k++) {
			if (items[k] < clipLeftTs || items[k] > clipRightTs)
				continue;
			currentTop = coUtil.unit2CordHeight(intCodes[k], size, this
					.getHeight(), BORDER)
					+ unitHeight / 2;
			if (currentTop > min_y - 5 && currentTop < max_y + 5) {

				assignColorByItem(k, gBuf);
				if (clicked) {
					uiUtil.paintItem(coUtil.time2coord(items[k]) + BORDER
							- coUtil.getClipL(), currentTop, gBuf,
							assignShapeByItem(k));
					continue;
				} else {
					uiUtil.paintItemExt(coUtil.time2coord(items[k]) + BORDER
							- coUtil.getClipL(), currentTop, gBuf,
							assignShapeByItem(k));
				}
				strTooltip += printTooltip(k);
			}
		}
		return strTooltip;
	}

	private String hasDots(Point p1, Point p2) {
		String strTooltip = "";
		int min_x = Math.min(p1.x, p2.x);
		int max_x = Math.max(p1.x, p2.x);

		int min_y = Math.min(p1.y, p2.y);
		int max_y = Math.max(p1.y, p2.y);

		int currentTop = 0;
		int unitHeight = coUtil.getUnitHeight(getHeight(), BORDER);
		int size = dcModel.getComponentSize(dca.getDottedChartOptionPanel()
				.getComponentType());

		long left = getLeftRight(Math.max(0, min_x - 10 - BORDER));
		long right = getLeftRight(max_x + 10 - BORDER);
		Graphics2D gBuf = modelBuffer.createGraphics();
		HashMap<String, HashMap<String, ArrayList<Integer>>> timeMap = dcModel
				.getProperTimeMap(dcop.getTimeOption()
						+ dcop.getComponentType(), coUtil.getMilli2pixels());
		HashMap<Integer, Integer> mapping = dcModel.getSortedMapModel()
				.getSortedMapping(dcop.getTimeOption(),
						dcop.getComponentType(), dcop.getSortStandard(),
						dcop.isDescCheckBoxSelected());
		for (long j = left; j <= right; j++) {
			HashMap<String, ArrayList<Integer>> tempMap = timeMap.get(String
					.valueOf(j));

			if (tempMap == null)
				continue;
			for (String str : tempMap.keySet()) {
				int diameter = 5;
				if (dcop.isSizeCheckBoxSelected())
					diameter = uiUtil.getDiameter(tempMap.get(str).size());
				if (dcop.getSortStandard().equals(DottedChartModel.STR_NONE)) {
					currentTop = coUtil.unit2CordHeight(Integer.valueOf(str),
							size, this.getHeight(), BORDER)
							+ unitHeight / 2;
				} else {
					currentTop = coUtil.unit2CordHeight(mapping
							.get((int) Integer.valueOf(str)), size, this
							.getHeight(), BORDER)
							+ unitHeight / 2;
				}
				if (currentTop > min_y - diameter
						&& currentTop < max_y + diameter) {
					long temp = j
							* dcModel.getProperTimeUnit(dcop.getTimeOption(),
									coUtil.getMilli2pixels());
					// if(dcop.getTimeOption().equals(DottedChartModel.TIME_ACTUAL))
					// temp -= timeOffset;
					int pointX = coUtil.time2coord(temp);
					if (pointX >= min_x - diameter - BORDER
							&& pointX <= max_x + diameter - BORDER) {
						assignColorByItem(tempMap.get(str), gBuf);
						if (clicked) {
							uiUtil.paintItemHigh(coUtil.time2coord(temp)
									+ BORDER - coUtil.getClipL(), currentTop,
									gBuf, ITEM_HANDLE_CIRCLE, tempMap.get(str)
											.size());
							continue;
						} else {
							uiUtil.paintItemHighExt(coUtil.time2coord(temp)
									+ BORDER - coUtil.getClipL(), currentTop,
									gBuf, ITEM_HANDLE_CIRCLE, tempMap.get(str)
											.size());
						}
						ArrayList<Integer> itemlist = tempMap.get(str);
						for (Integer index : itemlist) {
							strTooltip += printTooltip(index);
						}
					}
				}
			}
		}
		return strTooltip;
	}

	private String printTooltip(int index) {
		String str = "";
		str += dcModel.getInstance(index) + " | " + dcModel.getTask(index)
				+ " | " + dcModel.getOriginator(index);
		long temp = dcModel.getTime(dcop.getTimeOption(), index);
		if (dcop.getTimeOption().equals(DottedChartModel.TIME_ACTUAL)) {
			str += " | " + DateFormat.getInstance().format(temp);// -
			// timeOffset);
		} else if (dcop.getTimeOption().equals(
				DottedChartModel.TIME_RELATIVE_TIME)) {
			str += " | " + uiUtil.formatDate(temp);
		} else if (dcop.getTimeOption().equals(
				DottedChartModel.TIME_RELATIVE_RATIO)) {
			str += " | " + uiUtil.formatRatio(temp);
		} else if (dcop.getTimeOption().equals(
				DottedChartModel.TIME_LOGICAL_RELATIVE)) {
			str += " | " + uiUtil.formatString(temp, 5);
		}
		str += " | " + dcModel.getEvent(index);
		str += "<br>";
		return str;
	}

	/**
	 * Required for mouselistener
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseDragged(MouseEvent e) {
		Point p = e.getPoint();

		if (dcop.getMouseMode().equals(ST_DRAG)) {
			p4.x -= (p.x - p3.x);
			p4.y -= (p.y - p3.y);
			dca.setScrollBarPosition(new Point(p4.x, p4.y));
			dca.reDrawBoxOnOverview();
		} else if (dcop.getMouseMode().equals(ST_SELECT)) {
			p2 = e.getPoint();
			repaint();
		} else if (dcop.getMouseMode().equals(ST_ZOOMIN)) {
			p2 = e.getPoint();
			repaint();
		}
	}

	// /MOUSE Listener ///////////////////////////////////////////
	public void mousePressed(MouseEvent e) {
		clicked = true;
		if (clicked && dcop.getMouseMode().equals(ST_DRAG)) {
			p3 = e.getPoint();
			p4 = dca.getScrollPane().getViewport().getViewPosition();
			dca.reDrawBoxOnOverview();
		} else if (dcop.getMouseMode().equals(ST_SELECT)) {
			p1 = e.getPoint();
			p2 = e.getPoint();
			if (p3 != null && p4 != null)
				makeTooltip(p3, p4);
			repaint();
			p3 = e.getPoint();
		} else if (dcop.getMouseMode().equals(ST_ZOOMIN)) {
			p1 = e.getPoint();
			p2 = e.getPoint();
			repaint();
		}
	}

	public void mouseReleased(MouseEvent e) {
		clicked = false;
		if (dcop.getMouseMode().equals(ST_ZOOMIN)) {
			p2 = e.getPoint();
			if (p1 != null) {
				if (p2.x < p1.x && p2.y < p1.y) {
					dcop.zoomOut();
					p1 = null;
					p2 = null;
				} else {
					setViewportZoomIn();
					dca.reDrawBoxOnOverview();
				}
			}
		} else if (dcop.getMouseMode().equals(ST_SELECT)) {
			p2 = e.getPoint();
			p4 = e.getPoint();
			if (p1 != null) {
				makeTooltip(p1, e.getPoint());
			}
			p1 = null;
			p2 = null;
			repaint();
		} else if (dcop.getMouseMode().equals(ST_DRAG)) {
			dca.reDrawBoxOnOverview();
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

}
