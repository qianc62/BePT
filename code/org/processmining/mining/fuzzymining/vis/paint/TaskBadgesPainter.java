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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.processmining.mining.fuzzymining.vis.anim.TaskAnimation;
import org.processmining.mining.fuzzymining.vis.anim.TaskAnimationKeyframe;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class TaskBadgesPainter extends Painter {

	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 2;
	public static final int MIDDLE = 3;
	public static final int BOTTOM = 4;

	/**
	 * @param canvas
	 */
	public TaskBadgesPainter(AnimationCanvas canvas) {
		super(canvas);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.fuzzymining.vis.paint.Painter#paint(long,
	 * java.awt.Graphics2D)
	 */
	@Override
	public void paint(long modelTime, Graphics2D g2d) {
		float fontSize = canvas.getFontSize();
		g2d.setFont(g2d.getFont().deriveFont(fontSize));
		FontMetrics fMetrics = g2d.getFontMetrics();
		long fadeTime = canvas.getAnimation().getFadeTime() * 2;
		List<TaskAnimation> taskAnimations = new ArrayList<TaskAnimation>();
		taskAnimations.addAll(canvas.getAnimation()
				.getPrimitiveTaskAnimations());
		taskAnimations.addAll(canvas.getAnimation().getClusterTaskAnimations());
		TaskAnimationKeyframe keyframe = null;
		for (TaskAnimation taskAnimation : taskAnimations) {
			keyframe = taskAnimation.getKeyframe(modelTime);
			long idleTime = modelTime - keyframe.getTime();
			Color emergedBgColor, swallowedBgColor, traversedBgColor, textColor;
			float percentage;
			if (idleTime >= fadeTime) {
				percentage = 0.7f;
			} else {
				percentage = (float) idleTime / (float) fadeTime;
				percentage = 1f - (percentage * 0.3f);
			}
			// derive colors
			emergedBgColor = ColorSettings.fade(canvas.getColorSettings()
					.getTaskBadgeBackgroundEmerged(), percentage);
			swallowedBgColor = ColorSettings.fade(canvas.getColorSettings()
					.getTaskBadgeBackgroundSwallowed(), percentage);
			traversedBgColor = ColorSettings.fade(canvas.getColorSettings()
					.getTaskBadgeBackgroundTraversed(), percentage);
			textColor = ColorSettings.fade(canvas.getColorSettings()
					.getTaskBadgeForeground(), percentage);
			// derive node coords
			float x = canvas.translateX(taskAnimation.getPositionX());
			float y = canvas.translateY(taskAnimation.getPositionY());
			float width = canvas.translateX(taskAnimation.getWidth());
			float height = canvas.translateY(taskAnimation.getHeight());
			float badgeHeight = height / 3;
			float badgeHBorder = badgeHeight / 3;
			float badgeRadius = badgeHeight / 3;
			float overlap = height / 4;
			// get counter values
			int emergeCount = keyframe.getEmergeCount();
			int swallowCount = keyframe.getSwallowCount();
			int traverseCount = keyframe.getEventCount();
			if (emergeCount > 0) {
				// paint emerge badge on upper right
				String emerged = Integer.toString(keyframe.getEmergeCount());
				Rectangle2D emergedBounds = fMetrics.getStringBounds(emerged,
						g2d);
				g2d.setColor(emergedBgColor);
				float badgeX = x + width - overlap;
				float badgeY = y + badgeHBorder;
				RoundRectangle2D.Float rrect = new RoundRectangle2D.Float(
						badgeX, badgeY, (int) emergedBounds.getWidth()
								+ badgeHBorder + badgeHBorder, badgeHeight,
						badgeRadius, badgeRadius);
				g2d.fill(rrect);
				// triangle pointer
				GeneralPath triangle = new GeneralPath();
				triangle.moveTo(badgeX + 0.5f, badgeY + badgeHBorder);
				triangle.lineTo(badgeX - badgeHBorder, badgeY
						+ (badgeHeight / 2));
				triangle.lineTo(badgeX + 0.5f, badgeY + badgeHeight
						- badgeHBorder);
				triangle.closePath();
				g2d.fill(triangle);
				// paint string
				float sX = badgeX + badgeHBorder;
				float sY = badgeY + (badgeHeight / 2)
						+ (float) (2 * (emergedBounds.getHeight() / 5));
				g2d.setColor(textColor);
				g2d.drawString(emerged, sX, sY);
			}
			if (swallowCount > 0) {
				// paint swallow badge on lower right
				String swallowed = Integer.toString(swallowCount);
				Rectangle2D swallowedBounds = fMetrics.getStringBounds(
						swallowed, g2d);
				g2d.setColor(swallowedBgColor);
				float badgeX = x + width - overlap;
				float badgeY = y + height - badgeHeight - badgeHBorder;
				RoundRectangle2D.Float rrect = new RoundRectangle2D.Float(
						badgeX, badgeY, (int) swallowedBounds.getWidth()
								+ badgeHBorder + badgeHBorder, badgeHeight,
						badgeRadius, badgeRadius);
				g2d.fill(rrect);
				// triangle pointer
				GeneralPath triangle = new GeneralPath();
				triangle.moveTo(badgeX + 0.5f, badgeY + badgeHBorder);
				triangle.lineTo(badgeX - badgeHBorder, badgeY
						+ (badgeHeight / 2));
				triangle.lineTo(badgeX + 0.5f, badgeY + badgeHeight
						- badgeHBorder);
				triangle.closePath();
				g2d.fill(triangle);
				// paint string
				float sX = badgeX + badgeHBorder;
				float sY = badgeY + (badgeHeight / 2)
						+ (float) (2 * (swallowedBounds.getHeight() / 5));
				g2d.setColor(textColor);
				g2d.drawString(swallowed, sX, sY);
			}
			if (traverseCount > 0) {
				// paint traverse badge on middle left
				String traversed = Integer.toString(traverseCount);
				Rectangle2D traversedBounds = fMetrics.getStringBounds(
						traversed, g2d);
				g2d.setColor(traversedBgColor);
				float badgeWidth = (float) traversedBounds.getWidth()
						+ badgeHBorder + badgeHBorder;
				float badgeX = x - badgeWidth + overlap;
				float badgeY = y + (height / 2) - (badgeHeight / 2);
				RoundRectangle2D.Float rrect = new RoundRectangle2D.Float(
						badgeX, badgeY, badgeWidth, badgeHeight, badgeRadius,
						badgeRadius);
				g2d.fill(rrect);
				// triangle pointer
				GeneralPath triangle = new GeneralPath();
				triangle.moveTo(badgeX + badgeWidth, badgeY + badgeHBorder);
				triangle.lineTo(badgeX + badgeWidth + badgeHBorder, badgeY
						+ (badgeHeight / 2));
				triangle.lineTo(badgeX + badgeWidth, badgeY + badgeHeight
						- badgeHBorder);
				triangle.closePath();
				g2d.fill(triangle);
				// paint string
				float sX = badgeX + badgeHBorder;
				float sY = badgeY + (badgeHeight / 2)
						+ (int) (2 * (traversedBounds.getHeight() / 5));
				g2d.setColor(textColor);
				g2d.drawString(traversed, sX, sY);
			}
		}
	}

}
