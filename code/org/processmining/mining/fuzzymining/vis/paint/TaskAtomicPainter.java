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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.processmining.mining.fuzzymining.vis.anim.TaskAnimation;
import org.processmining.mining.fuzzymining.vis.anim.TaskAnimationKeyframe;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class TaskAtomicPainter extends Painter {

	/**
	 * @param canvas
	 */
	public TaskAtomicPainter(AnimationCanvas canvas) {
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
		BasicStroke stroke = new BasicStroke(canvas.getBorderWidth(),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2d.setStroke(stroke);
		float fontSize = canvas.getFontSize();
		g2d.setFont(g2d.getFont().deriveFont(fontSize));
		FontMetrics fMetrics = g2d.getFontMetrics();
		long fadeTime = canvas.getAnimation().getFadeTime();
		List<TaskAnimation> taskAnimations = canvas.getAnimation()
				.getPrimitiveTaskAnimations();
		TaskAnimationKeyframe keyframe = null;
		for (TaskAnimation taskAnimation : taskAnimations) {
			keyframe = taskAnimation.getKeyframe(modelTime);
			long idleTime = modelTime - keyframe.getTime();
			Color bgColor, textColor, borderColor;
			if (idleTime >= fadeTime) {
				bgColor = canvas.getColorSettings().getPassiveTaskBackground();
				textColor = canvas.getColorSettings()
						.getPassiveTaskForeground();
				borderColor = canvas.getColorSettings().getPassiveTaskBorder();
			} else {
				float percentage = (float) idleTime / (float) fadeTime;
				bgColor = ColorSettings.mix(canvas.getColorSettings()
						.getActiveTaskBackground(), canvas.getColorSettings()
						.getPassiveTaskBackground(), percentage);
				textColor = ColorSettings.mix(canvas.getColorSettings()
						.getActiveTaskForeground(), canvas.getColorSettings()
						.getPassiveTaskForeground(), percentage);
				borderColor = ColorSettings.mix(canvas.getColorSettings()
						.getActiveTaskBorder(), canvas.getColorSettings()
						.getPassiveTaskBorder(), percentage);
			}
			float x = canvas.translateX(taskAnimation.getPositionX());
			float y = canvas.translateY(taskAnimation.getPositionY());
			float width = canvas.translateX(taskAnimation.getWidth());
			float height = canvas.translateY(taskAnimation.getHeight());
			// paint background
			GradientPaint gradient = new GradientPaint(x, y, bgColor, x, y
					+ height, ColorSettings.darken(bgColor, 0.8f), false);
			g2d.setPaint(gradient);
			Rectangle2D rect = new Rectangle2D.Float(x, y, width, height);
			g2d.fill(rect);
			// paint text
			String name = taskAnimation.getEventName();
			String type = taskAnimation.getEventType();
			Rectangle2D nameBounds = fMetrics.getStringBounds(name, g2d);
			Rectangle2D typeBounds = fMetrics.getStringBounds(type, g2d);
			float vSlack = height - (float) nameBounds.getHeight()
					- (float) typeBounds.getHeight();
			float nameX = x + (width - (float) nameBounds.getWidth()) / 2;
			float nameY = y + (vSlack / 3) + (float) nameBounds.getHeight();
			float typeX = x + (width - (float) typeBounds.getWidth()) / 2;
			float typeY = nameY + (vSlack / 3) + (float) typeBounds.getHeight();
			g2d.setColor(textColor);
			g2d.drawString(name, nameX, nameY);
			g2d.drawString(type, typeX, typeY);
			// paint border
			gradient = new GradientPaint(x, y, borderColor, x, y + height,
					ColorSettings.fade(borderColor, 0.7f), false);
			g2d.setPaint(gradient);
			g2d.draw(rect);
		}
	}

}
