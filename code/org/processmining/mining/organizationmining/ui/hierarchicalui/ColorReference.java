package org.processmining.mining.organizationmining.ui.hierarchicalui;

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

import java.awt.Color;
import java.util.HashMap;
import java.util.Random;

/**
 * ColorRepository.
 * 
 * For assigning colors to keys automatically, and preserving these associations
 * over time. Keeps string ids assigned to colors and preserves mappings.
 * Provides a set of 10 standard colors for mappings, when full it will return
 * random colors.
 * 
 * @author Minseok Song
 */
public class ColorReference {

	protected HashMap mappings = null;
	protected Color[] colors = null;
	protected boolean[] assigned = null;
	protected Random rnd = null;

	/**
	 * constructor
	 */
	public ColorReference() {
		mappings = new HashMap();
		rnd = new Random();
		// standard colors
		colors = new Color[10];
		colors[0] = new Color(238, 221, 130); // light golden
		colors[1] = new Color(105, 139, 34); // loght green
		colors[2] = new Color(187, 255, 255); // light blue
		colors[3] = new Color(255, 106, 106); // indian red
		colors[4] = new Color(200, 50, 200); // violet
		colors[5] = new Color(150, 150, 50); // dark yellow
		colors[6] = new Color(20, 20, 20); // dark grey
		colors[7] = new Color(50, 50, 100); // dark blue
		colors[8] = new Color(100, 50, 50); // dark red
		colors[9] = new Color(50, 100, 100); // dark green
		// assignment map
		assigned = new boolean[10];
		for (int i = 0; i < assigned.length; i++) {
			assigned[i] = false;
		}
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
	public Color getColor(String key) {
		if (mappings.containsKey(key)) {
			// return previously mapped color
			return (Color) mappings.get(key);
		} else {
			// check for available standard color
			for (int i = 0; i < assigned.length; i++) {
				if (assigned[i] == false) {
					assigned[i] = true;
					mappings.put(key, colors[i]);
					return colors[i];
				}
			}
			// create random color
			int x1, x2, x3;
			while (true) {
				x1 = rnd.nextInt(256);
				x2 = rnd.nextInt(256);
				x3 = rnd.nextInt(256);
				int dis = Math.abs(x1 - x2);
				int dis2 = Math.abs(x2 - x3);
				if (dis > 40)
					break;
				if (dis2 > 40)
					break;
				if (x1 > 150)
					break;
			}
			Color random = new Color(x1, x2, x3);
			mappings.put(key, random);
			return random;
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
		Color c = (Color) mappings.remove(key);
		for (int i = 0; i < colors.length; i++) {
			if (colors[i].equals(c)) {
				assigned[i] = false;
				return;
			}
		}
	}

	public void assignColor(String key, Color color) {
		// freeColor(key);
		mappings.put(key, color);
	}

}
