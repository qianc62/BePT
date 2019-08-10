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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.List;

import org.deckfour.gantzgraf.layout.GGCurve;
import org.processmining.mining.fuzzymining.vis.anim.ArcAnimation;
import org.processmining.mining.fuzzymining.vis.anim.ArcAnimationKeyframe;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class EdgeBackgroundPainter extends Painter {

	/**
	 * @param canvas
	 */
	public EdgeBackgroundPainter(AnimationCanvas canvas) {
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
		ArcAnimationKeyframe keyframe = null;
		GGCurve curve = null;
		float x[], y[];
		long fadeTime = canvas.getAnimation().getFadeTime();
		long idleTime;
		int traverseCount;
		int bestQuarterTraverseCount = canvas.getAnimation()
				.getBestQuarterTraverseCount();
		float percentage;
		List<ArcAnimation> arcAnimations = canvas.getAnimation()
				.getArcAnimations();
		for (ArcAnimation anim : arcAnimations) {
			keyframe = anim.getKeyframe(modelTime);
			// derive stroke width from traverse count and set
			traverseCount = keyframe.getTraverseCount();
			percentage = (float) traverseCount
					/ (float) bestQuarterTraverseCount;
			if (percentage > 1.0f) {
				percentage = 1.0f;
			}
			g2d.setStroke(new BasicStroke(canvas.getLineWidth(percentage),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			// derive stroke color from idel time and set
			idleTime = modelTime - keyframe.getLastCompleted();
			Color terminalColor;
			if (idleTime > fadeTime) {
				terminalColor = canvas.getColorSettings()
						.getPassiveEdgeBackground();
			} else {
				percentage = (float) idleTime / (float) fadeTime;
				terminalColor = ColorSettings.mix(canvas.getColorSettings()
						.getActiveEdgeBackground(), canvas.getColorSettings()
						.getPassiveEdgeBackground(), percentage);
			}
			// Color sourceColor = ColorSettings.fade(terminalColor, 0.6f);
			// draw arc background
			curve = anim.getCurve();

			x = curve.getControlPointsX();
			y = curve.getControlPointsY();
			GeneralPath path = new GeneralPath();
			float firstX = canvas.translateX(x[0]);
			float firstY = canvas.translateY(y[0]);
			float currentX = firstX, currentY = firstY, control1X, control1Y, control2X, control2Y;
			path.moveTo(firstX, firstY);
			for (int i = 1; i < x.length; i += 3) {
				control1X = canvas.translateX(x[i]);
				control1Y = canvas.translateY(y[i]);
				control2X = canvas.translateX(x[i + 1]);
				control2Y = canvas.translateY(y[i + 1]);
				currentX = canvas.translateX(x[i + 2]);
				currentY = canvas.translateY(y[i + 2]);
				path.curveTo(control1X, control1Y, control2X, control2Y,
						currentX, currentY);
			}
			/*
			 * GradientPaint gradient = new GradientPaint(firstX, firstY,
			 * sourceColor, currentX, currentY, terminalColor, false);
			 * g2d.setPaint(gradient);
			 */
			g2d.setColor(terminalColor);
			g2d.draw(path);
		}
	}

}
