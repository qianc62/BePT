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

import java.awt.Color;
import java.awt.Graphics;
import java.lang.Math;
import java.text.DecimalFormat;

/**
 * Utility methods for GUI.
 * 
 * @author Minseok Song
 */
public class UIUtil {

	/**
	 * constructor
	 */
	public UIUtil() {
	}

	// size of a dot
	private int size(int number) {
		int size = 2;
		size = (int) Math.log(number) * 2;
		return size;
	}

	public int sizeBuffer(int number) {
		int size = 1;
		size = (int) (Math.log(number) * 1.5);
		return size;
	}

	public int getDiameter(int number) {
		return (5 + size(number) - 1);
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
	public void paintItemHigh(int x, int y, Graphics g, String shape, int number) {
		int size = size(number);
		if (shape.equals(DottedChartPanel.STR_NONE)) {
			return;
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DOT)) {
			g.fillOval(x - size(number), y - size(number), size(number) * 2,
					size(number) * 2);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_BOX)) {
			g.fill3DRect(x - 5, y - 5, 10, 10, false);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_CIRCLE)) {
			g.fillOval(x - (5 + size - 1), y - (5 + size - 1),
					(5 + size - 1) * 2 + 1, (5 + size - 1) * 2 + 1);
			// g.fillOval(x-5, y-5, 11, 11);
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

	public void paintItemHighExt(int x, int y, Graphics g, String shape,
			int number) {
		int size = size(number);
		if (shape.equals(DottedChartPanel.ITEM_HANDLE_CIRCLE)) {
			g.fillOval(x - (5 + size - 1), y - (5 + size - 1),
					(5 + size - 1) * 2 + 1, (5 + size - 1) * 2 + 1);
			g.setColor(Color.white);
			g.fillOval(x - (5 + size - 3), y - (5 + size - 3),
					(5 + size - 3) * 2 + 1, (5 + size - 3) * 2 + 1);
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
	public void paintItem(int x, int y, Graphics g, String shape) {
		if (shape.equals(DottedChartPanel.STR_NONE)) {
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
	public void paintItemExt(int x, int y, Graphics g, String shape) {

		if (shape.equals(DottedChartPanel.ITEM_HANDLE_DOT)) {
			g.fillOval(x - 2, y - 2, 4, 4);
			g.setColor(Color.white);
			g.fillOval(x - 1, y - 1, 2, 2);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_BOX)) {
			g.fill3DRect(x - 5, y - 5, 10, 10, false);
			g.setColor(Color.white);
			g.fill3DRect(x - 3, y - 3, 6, 6, false);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_CIRCLE)) {
			g.fillOval(x - 5, y - 5, 11, 11);
			g.setColor(Color.white);
			g.fillOval(x - 3, y - 3, 6, 6);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_RHOMBUS)) {
			int rhombX[] = { x, x - 5, x, x + 5 };
			int rhombY[] = { y - 5, y, y + 5, y };
			g.fillPolygon(rhombX, rhombY, 4);
			g.setColor(Color.white);
			int rhombX2[] = { x + 1, x - 4, x + 1, x + 4 };
			int rhombY2[] = { y - 4, y + 1, y + 4, y + 1 };
			g.fillPolygon(rhombX2, rhombY2, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_TRIANGLE)) {
			int triX[] = { x, x - 5, x + 5 };
			int triY[] = { y + 5, y - 5, y - 5 };
			g.fillPolygon(triX, triY, 3);
			g.setColor(Color.white);
			int triX2[] = { x + 1, x - 3, x + 3 };
			int triY2[] = { y + 3, y - 3, y - 3 };
			g.fillPolygon(triX2, triY2, 3);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_ROUND_BOX)) {
			g.fillRoundRect(x - 5, y - 5, 10, 10, 2, 2);
			g.setColor(Color.white);
			g.fillRoundRect(x - 3, y - 3, 6, 6, 2, 2);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_BOX)) {
			g.drawRect(x - 5, y - 5, 10, 10);
			g.setColor(Color.white);
			g.drawRect(x - 3, y - 3, 6, 6);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_CIRCLE)) {
			g.drawOval(x - 5, y - 5, 11, 11);
			g.setColor(Color.white);
			g.drawOval(x - 3, y - 3, 7, 7);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_RHOMBUS)) {
			int rhombX[] = { x, x - 5, x, x + 5 };
			int rhombY[] = { y - 5, y, y + 5, y };
			g.drawPolygon(rhombX, rhombY, 4);
			g.setColor(Color.white);
			int rhombX2[] = { x + 1, x - 4, x + 1, x + 4 };
			int rhombY2[] = { y - 4, y + 1, y + 4, y + 1 };
			g.drawPolygon(rhombX2, rhombY2, 4);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_TRIANGLE)) {
			int triX[] = { x, x - 5, x + 5 };
			int triY[] = { y + 5, y - 5, y - 5 };
			g.drawPolygon(triX, triY, 3);
			g.setColor(Color.white);
			int triX2[] = { x + 1, x - 4, x + 4 };
			int triY2[] = { y + 4, y - 4, y - 4 };
			g.drawPolygon(triX2, triY2, 3);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DRAW_ROUND_BOX)) {
			g.drawRoundRect(x - 5, y - 5, 10, 10, 2, 2);
			g.setColor(Color.white);
			g.drawRoundRect(x - 3, y - 3, 6, 6, 2, 2);
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
	protected void paintItem_buffer(int x, int y, Graphics g, String shape,
			int number) {
		if (shape.equals(DottedChartPanel.STR_NONE)) {
			return;
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_DOT)) {
			g.fillOval(x - 2, y - 2, 7, 7);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_BOX)) {
			g.fill3DRect(x - 3, y - 3, 6, 6, false);
		} else if (shape.equals(DottedChartPanel.ITEM_HANDLE_CIRCLE)) {
			// g.fillOval(x-2, y-2, 7, 7);
			g.fillOval(x - (2 + sizeBuffer(number)), y
					- (2 + sizeBuffer(number)),
					(2 + sizeBuffer(number)) * 2 + 1,
					(2 + sizeBuffer(number)) * 2 + 1);
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
		if (shape.equals(DottedChartPanel.STR_NONE)) {
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

	// calcuate averageColors
	public Color getAverageColor(Color colorArray[]) {
		int valueRed = 0, valueGreen = 0, valueBlue = 0;

		for (int i = 0; i < colorArray.length; i++) {
			valueRed += colorArray[i].getRed();
			valueGreen += colorArray[i].getGreen();
			valueBlue += colorArray[i].getBlue();
		}
		return new Color(valueRed / colorArray.length, valueGreen
				/ colorArray.length, valueBlue / colorArray.length);
	}

	public String formatDate(long timeStart) {

		long days = timeStart / 1000 / 60 / 60 / 24;
		long hours = (timeStart - days * 24 * 60 * 60 * 1000) / 1000 / 60 / 60;
		long minutes = (timeStart - days * 24 * 60 * 60 * 1000 - hours * 60 * 60 * 1000) / 1000 / 60;
		long seconds = (timeStart - days * 24 * 60 * 60 * 1000 - hours * 60
				* 60 * 1000 - minutes * 60 * 1000) / 1000;
		return String.valueOf(days + "days:" + hours + ":" + minutes + ":"
				+ seconds);
	}

	public String formatRatio(long timeStart) {
		return String.valueOf(timeStart / 100 + "."
				+ (timeStart - timeStart / 100 * 100) + "%");
	}

	/*
	 * Formats a double to display it in the right manner, with 'places' being
	 * the maximum number of decimal places allowed
	 * 
	 * @param val double
	 * 
	 * @param places int
	 * 
	 * @return String
	 */
	public String formatString(double val, int places) {
		if (Double.valueOf(val).equals(Double.NaN))
			return "0.0";
		String cur = "";
		DecimalFormat df;
		double bound = Math.pow(10.0, (0 - places));
		String tempString = "0";
		for (int i = 0; i < places - 1; i++) {
			tempString += "#";
		}
		if ((val != 0.0) && (val < bound)) {
			// display scientific notation
			if (places == 0) {
				df = new DecimalFormat("0E0");
			} else {
				df = new DecimalFormat("0." + tempString + "E0");
			}
			cur = df.format(val);
		} else {
			if (places == 0) {
				df = new DecimalFormat("0");
			} else {
				df = new DecimalFormat("0." + tempString);
			}
			cur = df.format(val);
		}
		return cur;
	}

}
