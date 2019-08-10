/*
 * Created on October 30, 2008
 *
 * Author: Minseok Song
 * (c) 2005 Technische Universiteit Eindhoven, Minseok Song
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

import org.processmining.analysis.performance.advanceddottedchartanalysis.model.DottedChartModel;

/**
 * Utility methods for GUI.
 * 
 * @author Minseok Song
 */
public class CoordinationUtil {

	private int clipL = 0;
	private int clipR = DottedChartPanel.SCREENLENGTH;
	protected DottedChartOptionPanel dcop = null;
	protected DottedChartModel dcModel = null;

	private int clipU = 0;
	private int clipB = DottedChartPanel.SCREENLENGTH;

	public void setClipL(int clipL) {
		this.clipL = clipL;
	}

	public void setClipR(int clipR) {
		this.clipR = clipR;
	}

	public int getClipL() {
		return clipL;
	}

	public int getClipR() {
		return clipR;
	}

	public void setClipU(int clipU) {
		this.clipU = clipU;
	}

	public void setClipB(int clipB) {
		this.clipB = clipB;
	}

	public int getClipU() {
		return clipU;
	}

	public int getClipB() {
		return clipB;
	}

	/**
	 * constructor
	 */
	public CoordinationUtil(DottedChartOptionPanel dcop,
			DottedChartModel dcModel) {
		this.dcop = dcop;
		this.dcModel = dcModel;
	}

	protected double milli2pixels = 1.0; // the ratio between milliseconds and
	// displayed pixels
	protected double height2pixels = 1.0; // the ratio between heights and
	// displayed pixels
	protected static int border = 5;

	public double getMilli2pixels() {
		return milli2pixels;
	}

	public void updateMilli2pixelsRatio(int width, int border) {
		String str = dcop.getTimeOption();
		if (str.equals(DottedChartModel.TIME_ACTUAL)) {
			milli2pixels = (double) (width - border * 2)
					/ (double) (dcModel.getLogMaxValue() - dcModel
							.getLogMinValueforScreen()); // + 2 is added to
			// extend bound
		} else if (str.equals(DottedChartModel.TIME_RELATIVE_TIME)) {
			milli2pixels = (double) (width - border * 2)
					/ (double) (dcModel.getLogRelativeMaxValue() - 0); // + 2 is
			// added
			// to
			// extend
			// bound
		} else if (str.equals(DottedChartModel.TIME_RELATIVE_RATIO)) {
			milli2pixels = (double) (width - border * 2)
					/ (double) (dcModel.getLogRelativeRatioMaxValue() - 0); // +
			// 2
			// is
			// added
			// to
			// extend
			// bound
		} else if (str.equals(DottedChartModel.TIME_LOGICAL_RELATIVE)) {
			milli2pixels = (double) (width - border * 2)
					/ (double) (dcModel.getLogLogiclRelativeMaxValue() - 0); // +
			// 2
			// is
			// added
			// to
			// extend
			// bound
		}
		if ((milli2pixels == 1) || (milli2pixels == 0)) {
			return;
		}
	}

	public double updateMilli2pixelsRatioBuffer(int width, int border) {
		String str = dcop.getTimeOption();
		double milli2pixels = 0;
		if (str.equals(DottedChartModel.TIME_ACTUAL)) {
			milli2pixels = (double) (width - border * 2)
					/ (double) (dcModel.getLogMaxValue() - dcModel
							.getLogMinValueforScreen()); // + 2 is added to
			// extend bound
		} else if (str.equals(DottedChartModel.TIME_RELATIVE_TIME)) {
			milli2pixels = (double) (width - border * 2)
					/ (double) (dcModel.getLogRelativeMaxValue() - 0); // + 2 is
			// added
			// to
			// extend
			// bound
		} else if (str.equals(DottedChartModel.TIME_RELATIVE_RATIO)) {
			milli2pixels = (double) (width - border * 2)
					/ (double) (dcModel.getLogRelativeRatioMaxValue() - 0); // +
			// 2
			// is
			// added
			// to
			// extend
			// bound
		} else if (str.equals(DottedChartModel.TIME_LOGICAL_RELATIVE)) {
			milli2pixels = (double) (width - border * 2)
					/ (double) (dcModel.getLogLogiclRelativeMaxValue() - 0); // +
			// 2
			// is
			// added
			// to
			// extend
			// bound
		}
		return milli2pixels;
	}

	// /////////////////////
	/**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param unit_number
	 * @param size
	 * @param height
	 * @param border
	 * @return
	 */
	public int unit2CordHeight(int unit_number, int size, int height, int border) {
		return (int) (border + ((double) (height - 2 * border) / size * unit_number));
	}

	/**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
	public int unit2CordOverview(int unit_number, int height, int size) {
		return (int) (((double) (height) / size * unit_number));
	}

	/**
	 * Convenience method. Transforms a timestamp (milliseconds) into the
	 * corresponding horizontal position within the viewport.
	 * 
	 * @param aTimeMillis
	 * @return
	 */
	public int time2coord(long aTimeMillis) {
		String str = dcop.getTimeOption();
		if (str.equals(DottedChartModel.TIME_ACTUAL))
			return (int) ((aTimeMillis - dcModel.getLogMinValueforScreen()) * getMilli2pixels());
		else
			return (int) ((aTimeMillis) * getMilli2pixels());
	}

	/**
	 * Convenience method. Transforms a timestamp (milliseconds) into the
	 * corresponding horizontal position within the viewport.
	 * 
	 * @param aTimeMillis
	 * @return
	 */
	public int time2coordOverview(long aTimeMillis, double milli2pixels) {
		String str = dcop.getTimeOption();
		if (str.equals(DottedChartModel.TIME_ACTUAL))
			return (int) ((aTimeMillis - dcModel.getLogMinValueforScreen()) * milli2pixels);
		else
			return (int) ((aTimeMillis) * milli2pixels);
	}

	/**
	 * convenience method. transforms a given horizontal coordinate within the
	 * viewport into the corresponding timestamp (milliseconds) in the log
	 * space.
	 * 
	 * @param anX
	 * @return
	 */
	public long coord2timeMillis(double anX) {
		if (dcop.getTimeOption().equals(DottedChartModel.TIME_ACTUAL))
			return (long) (anX / getMilli2pixels())
					+ dcModel.getLogMinValueforScreen();
		else
			return (long) (anX / getMilli2pixels());
	}

	// convenient methods for unit height
	public int getUnitHeight(int imageHeight, int border) {
		int height = imageHeight - (2 * border);
		return (height / dcModel.getComponentSize(dcop.getComponentType()));
	}

	// /**
	// * Convenience method.
	// * Transforms a timestamp (Date) into the corresponding
	// * horizontal position within the viewport.
	// * @param aTimestamp
	// * @return
	// */
	// protected int time2coord(Date aTimestamp) {
	// return time2coord(aTimestamp.getTime(), dcop.getTimeOption(), dcModel);
	// }

	// /**
	// * convenience method.
	// * transforms a given horizontal coordinate within the viewport
	// * into the corresponding timestamp (Date) in the log space.
	// * @param anX
	// * @return
	// */
	// protected Date coord2time(double anX) {
	// return new Date(coord2timeMillis(anX));
	// }

	// /**
	// * convenience method.
	// * transforms a given horizontal coordinate within the viewport
	// * into the corresponding timestamp (milliseconds) in the log space.
	// * @param anX
	// * @return
	// */
	// protected long coord2timeMillis_buffer(double anX, double milli2pixels) {
	// if(dcop.getTimeOption().equals(DottedChartModel.TIME_ACTUAL))
	// return (long)((double)anX / milli2pixels) + dcModel.getLogMinValue();
	// else
	// return (long)((double)anX / milli2pixels);
	// }

}
