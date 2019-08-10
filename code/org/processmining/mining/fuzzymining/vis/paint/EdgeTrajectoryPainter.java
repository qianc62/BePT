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
import org.processmining.mining.fuzzymining.vis.anim.TokenAnimation;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class EdgeTrajectoryPainter extends Painter {

	/**
	 * @param canvas
	 */
	public EdgeTrajectoryPainter(AnimationCanvas canvas) {
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
		BasicStroke stroke = new BasicStroke(canvas.getLineWidth(0.6f),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2d.setStroke(stroke);
		ArcAnimationKeyframe keyframe = null;
		GGCurve curve = null;
		long fadeTime = canvas.getAnimation().getFadeTime();
		List<ArcAnimation> arcAnimations = canvas.getAnimation()
				.getArcAnimations();
		for (ArcAnimation anim : arcAnimations) {
			keyframe = anim.getKeyframe(modelTime);
			curve = anim.getCurve();
			// derive stroke color from idel time and set
			for (TokenAnimation tokenAnim : keyframe.getTokenAnimations()) {
				drawCurveSegment(g2d, modelTime, fadeTime, curve, tokenAnim);
			}
		}
	}

	protected void drawCurveSegment(Graphics2D g2d, long modelTime,
			long fadeTime, GGCurve curve, TokenAnimation anim) {
		long idleTime = modelTime - anim.getStart();
		// determine start color
		Color startColor;
		if (idleTime > fadeTime) {
			startColor = canvas.getColorSettings()
					.getActiveEdgeTrajectoryStart();
		} else {
			float percentage = (float) idleTime / (float) fadeTime;
			startColor = ColorSettings.mix(canvas.getColorSettings()
					.getActiveEdgeBackground(), canvas.getColorSettings()
					.getActiveEdgeTrajectoryStart(), percentage);
			percentage = 1f - (percentage * 0.5f);
		}
		// determine trajectory coordinates
		float[][] points = curve.getPartialPath(anim
				.completedPercentage(modelTime));
		GeneralPath path = new GeneralPath();
		float firstX = canvas.translateX(points[0][0]);
		float firstY = canvas.translateY(points[1][0]);
		float lastX = firstX;
		float lastY = firstY;
		float currentX, currentY;
		path.moveTo(firstX, firstY);
		for (int i = 1; i < points[0].length; i++) {
			currentX = canvas.translateX(points[0][i]);
			currentY = canvas.translateY(points[1][i]);
			if (currentX != lastX || currentY != lastY) {
				// add point to path
				path.lineTo(currentX, currentY);
			}
			lastX = currentX;
			lastY = currentY;
		}
		GradientPaint gradient = new GradientPaint(firstX, firstY, startColor,
				lastX, lastY, canvas.getColorSettings()
						.getActiveEdgeBackground(), false);
		g2d.setPaint(gradient);
		g2d.draw(path);
	}

}
