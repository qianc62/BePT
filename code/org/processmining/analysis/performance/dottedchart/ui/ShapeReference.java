/*
 * Created on Dec 7, 2006
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

package org.processmining.analysis.performance.dottedchart.ui;

import java.awt.Color;
import java.util.HashMap;

public class ShapeReference {
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
	public static String[] shapeList = new String[] { ITEM_HANDLE_CIRCLE,
			ITEM_HANDLE_TRIANGLE, ITEM_HANDLE_BOX, ITEM_HANDLE_RHOMBUS,
			ITEM_HANDLE_ROUND_BOX, ITEM_HANDLE_DRAW_CIRCLE,
			ITEM_HANDLE_DRAW_TRIANGLE, ITEM_HANDLE_DRAW_BOX,
			ITEM_HANDLE_DRAW_ROUND_BOX, ITEM_HANDLE_DRAW_RHOMBUS };
	public static int index = 0;

	protected HashMap mappings = null;
	protected Color[] colors = null;
	protected boolean[] assigned = null;

	// protected Random rnd = null;

	/**
	 * constructor
	 */
	public ShapeReference() {
		mappings = new HashMap();
	}

	/**
	 * Retrieves the color mapped to the given key (identity preserved). If no
	 * color was previously mapped, a new one is taken from the standard
	 * repository and, if all taken, a random color is assigned.
	 * 
	 * @param key
	 *            the key to map a color to
	 * @return mapped color instance
	 */
	public String getShape(String key) {
		if (mappings.containsKey(key)) {
			// return previously mapped color
			return (String) mappings.get(key);
		} else {
			// assign shape
			String str = shapeList[index % 8];
			index++;
			mappings.put(key, str);
			return str;
		}
	}

	/**
	 * Frees a keyed color object for new assignment, i.e. it is not used
	 * anymore.
	 * 
	 * @param key
	 *            previously assigned key
	 */
	public void freeColor(String key) {
		/*
		 * Color c = (Color)mappings.remove(key); for(int i=0; i<colors.length;
		 * i++) { if(colors[i].equals(c)) { assigned[i]=false; return; } }
		 */
	}

}

/*
 * /** convenience method for internal use. paints a log item handle
 * visualization.
 * 
 * @param x horizontal anchor coordinate of the handle
 * 
 * @param y vertical anchor coordinate of the handle
 * 
 * @param g the Graphics object used for painting
 */
/*
 * protected void paintItem(int x, int y, Graphics g) {
 * if(itemHandle.equals(DottedChartPanel.ITEM_HANDLE_NONE)) { return; }
 * if(itemHandle.equals(DottedChartPanel.ITEM_HANDLE_BOX)) { g.fillRect(x-5,
 * y-5, 11, 11); } else
 * if(itemHandle.equals(DottedChartPanel.ITEM_HANDLE_CIRCLE)) { g.fillOval(x-5,
 * y-5, 11, 11); } else
 * if(itemHandle.equals(DottedChartPanel.ITEM_HANDLE_RHOMBUS)) { int rhombX[] =
 * {x, x-5, x, x+5}; int rhombY[] = {y-5, y, y+5, y}; g.fillPolygon(rhombX,
 * rhombY, 4); } else
 * if(itemHandle.equals(DottedChartPanel.ITEM_HANDLE_TRIANGLE)) { int triX[] =
 * {x, x-5, x+5}; int triY[] = {y+5, y-5, y-5}; g.fillPolygon(triX, triY, 3); }
 * }
 */
