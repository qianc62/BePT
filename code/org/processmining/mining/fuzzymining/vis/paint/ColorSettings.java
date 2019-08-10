/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.mining.fuzzymining.vis.paint;

import java.awt.Color;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ColorSettings {

	public static Color mix(Color a, Color b, float percentage) {
		if (percentage <= 0f) {
			return a;
		} else if (percentage >= 1f) {
			return b;
		} else {
			float antiPercentage = 1.0f - percentage;
			float red = (antiPercentage * a.getRed())
					+ (percentage * b.getRed());
			float green = (antiPercentage * a.getGreen())
					+ (percentage * b.getGreen());
			float blue = (antiPercentage * a.getBlue())
					+ (percentage * b.getBlue());
			float alpha = (antiPercentage * a.getAlpha())
					+ (percentage * b.getAlpha());
			return new Color(red / 255f, green / 255f, blue / 255f,
					alpha / 255f);
		}
	}

	public static Color fade(Color color, float percentage) {
		int red = (int) ((float) color.getRed() * percentage);
		int green = (int) ((float) color.getGreen() * percentage);
		int blue = (int) ((float) color.getBlue() * percentage);
		return new Color(red, green, blue, color.getAlpha());
	}

	public static Color darken(Color color, float percentage) {
		float antiPercentage = 1f - percentage;
		int medium = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
		medium -= 30;
		if (medium <= 0) {
			medium = 0;
		}
		int mediumVal = (int) (medium * antiPercentage);
		int red = (int) ((float) color.getRed() * percentage) + mediumVal;
		int green = (int) ((float) color.getGreen() * percentage) + mediumVal;
		int blue = (int) ((float) color.getBlue() * percentage) + mediumVal;
		return new Color(red, green, blue);
	}

	public static Color setAlpha(Color color, float alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(),
				(int) (alpha * 255));
	}

	protected Color canvasBackground = new Color(0, 0, 0);

	protected Color passiveEdgeBackground = new Color(40, 40, 40);
	protected Color activeEdgeBackground = new Color(200, 0, 0);
	protected Color activeEdgeTrajectoryStart = new Color(200, 0, 0, 20);

	protected Color animRingsBackground = new Color(200, 200, 200);

	protected Color tokensBackground = new Color(200, 200, 200);

	protected Color passiveTaskBackground = new Color(30, 30, 30);
	protected Color passiveTaskForeground = new Color(100, 100, 100);
	protected Color passiveTaskBorder = new Color(80, 80, 80);
	protected Color activeTaskBackground = new Color(190, 0, 0);
	protected Color activeTaskForeground = new Color(220, 220, 220);
	protected Color activeTaskBorder = new Color(180, 180, 180);

	protected Color tokenLabel = new Color(220, 180, 0, 160);

	protected Color taskBadgeForeground = new Color(220, 220, 220, 220);
	protected Color taskBadgeBackgroundSwallowed = new Color(200, 0, 0, 200);
	protected Color taskBadgeBackgroundEmerged = new Color(0, 180, 0, 200);
	protected Color taskBadgeBackgroundTraversed = new Color(0, 0, 200, 200);

	protected Color tokenRing = new Color(200, 200, 200);

	public ColorSettings() {
		// standard constructor
	}

	public static ColorSettings SETTINGS_NIGHT = new ColorSettings();

	public static ColorSettings SETTINGS_DAY = new ColorSettings(new Color(255,
			255, 255), new Color(120, 120, 120), new Color(255, 30, 0),
			new Color(255, 30, 30), new Color(0, 0, 0),
			new Color(210, 210, 210), new Color(80, 80, 80), new Color(80, 80,
					80), new Color(200, 20, 0), new Color(0, 0, 0), new Color(
					0, 0, 0), new Color(80, 60, 40, 200), new Color(20, 20, 20,
					220), new Color(200, 0, 0, 200), new Color(0, 180, 0, 200),
			new Color(0, 0, 200, 200), new Color(255, 0, 0, 20), new Color(180,
					40, 0));

	/**
	 * @param passiveEdgeBackground
	 * @param activeEdgeBackground
	 * @param animRingsBackground
	 * @param tokensBackground
	 * @param passiveTaskBackground
	 * @param passiveTaskForeground
	 * @param activeTaskBackground
	 * @param activeTaskForeground
	 * @param tokenLabel
	 * @param taskBadgeForeground
	 * @param taskBadgeBackgroundSwallowed
	 * @param taskBackgroundEmerged
	 * @param taskBackgroundTraversed
	 */
	public ColorSettings(Color canvasBackground, Color passiveEdgeBackground,
			Color activeEdgeBackground, Color animRingsBackground,
			Color tokensBackground, Color passiveTaskBackground,
			Color passiveTaskForeground, Color passiveTaskBorder,
			Color activeTaskBackground, Color activeTaskForeground,
			Color activeTaskBorder, Color tokenLabel,
			Color taskBadgeForeground, Color taskBadgeBackgroundSwallowed,
			Color taskBadgeBackgroundEmerged,
			Color taskBadgeBackgroundTraversed,
			Color activeEdgeTrajectoryStart, Color tokenRing) {
		super();
		this.canvasBackground = canvasBackground;
		this.passiveEdgeBackground = passiveEdgeBackground;
		this.activeEdgeBackground = activeEdgeBackground;
		this.animRingsBackground = animRingsBackground;
		this.tokensBackground = tokensBackground;
		this.passiveTaskBackground = passiveTaskBackground;
		this.passiveTaskForeground = passiveTaskForeground;
		this.passiveTaskBorder = passiveTaskBorder;
		this.activeTaskBackground = activeTaskBackground;
		this.activeTaskForeground = activeTaskForeground;
		this.activeTaskBorder = activeTaskBorder;
		this.tokenLabel = tokenLabel;
		this.taskBadgeForeground = taskBadgeForeground;
		this.taskBadgeBackgroundSwallowed = taskBadgeBackgroundSwallowed;
		this.taskBadgeBackgroundEmerged = taskBadgeBackgroundEmerged;
		this.taskBadgeBackgroundTraversed = taskBadgeBackgroundTraversed;
		this.activeEdgeTrajectoryStart = activeEdgeTrajectoryStart;
		this.tokenRing = tokenRing;
	}

	public Color getCanvasBackground() {
		return canvasBackground;
	}

	/**
	 * @return the passiveEdgeBackground
	 */
	public Color getPassiveEdgeBackground() {
		return passiveEdgeBackground;
	}

	/**
	 * @return the activeEdgeBackground
	 */
	public Color getActiveEdgeBackground() {
		return activeEdgeBackground;
	}

	public Color getActiveEdgeTrajectoryStart() {
		return activeEdgeTrajectoryStart;
	}

	/**
	 * @return the animRingsBackground
	 */
	public Color getAnimRingsBackground() {
		return animRingsBackground;
	}

	/**
	 * @return the tokensBackground
	 */
	public Color getTokensBackground() {
		return tokensBackground;
	}

	/**
	 * @return the passiveTaskBackground
	 */
	public Color getPassiveTaskBackground() {
		return passiveTaskBackground;
	}

	/**
	 * @return the passiveTaskForeground
	 */
	public Color getPassiveTaskForeground() {
		return passiveTaskForeground;
	}

	public Color getPassiveTaskBorder() {
		return passiveTaskBorder;
	}

	/**
	 * @return the activeTaskBackground
	 */
	public Color getActiveTaskBackground() {
		return activeTaskBackground;
	}

	/**
	 * @return the activeTaskForeground
	 */
	public Color getActiveTaskForeground() {
		return activeTaskForeground;
	}

	public Color getActiveTaskBorder() {
		return activeTaskBorder;
	}

	/**
	 * @return the tokenLabel
	 */
	public Color getTokenLabel() {
		return tokenLabel;
	}

	/**
	 * @return the taskBadgeForeground
	 */
	public Color getTaskBadgeForeground() {
		return taskBadgeForeground;
	}

	/**
	 * @return the taskBadgeBackgroundSwallowed
	 */
	public Color getTaskBadgeBackgroundSwallowed() {
		return taskBadgeBackgroundSwallowed;
	}

	/**
	 * @return the taskBackgroundEmerged
	 */
	public Color getTaskBadgeBackgroundEmerged() {
		return taskBadgeBackgroundEmerged;
	}

	/**
	 * @return the taskBackgroundTraversed
	 */
	public Color getTaskBadgeBackgroundTraversed() {
		return taskBadgeBackgroundTraversed;
	}

	public Color getTokenRing() {
		return tokenRing;
	}

}
