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

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.List;

import org.deckfour.gantzgraf.layout.GGCurve;
import org.processmining.mining.fuzzymining.vis.anim.ArcAnimation;
import org.processmining.mining.fuzzymining.vis.anim.ArcAnimationKeyframe;
import org.processmining.mining.fuzzymining.vis.anim.TokenAnimation;

/**
 * @author christian
 * 
 */
public class TokenPainter extends Painter {

	/**
	 * @param canvas
	 */
	public TokenPainter(AnimationCanvas canvas) {
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
		g2d.setColor(canvas.getColorSettings().getTokensBackground());
		float radius = canvas.translateY(canvas.getBoxHeight()) * 0.25f;
		float diameter = 2 * radius;
		ArcAnimationKeyframe keyframe = null;
		GGCurve curve = null;
		List<ArcAnimation> arcAnimations = canvas.getAnimation()
				.getArcAnimations();
		for (ArcAnimation anim : arcAnimations) {
			keyframe = anim.getKeyframe(modelTime);
			curve = anim.getCurve();
			// derive stroke color from idel time and set
			for (TokenAnimation tokenAnim : keyframe.getTokenAnimations()) {
				float[] pos = curve.getRelativePoint(tokenAnim
						.completedPercentage(modelTime));
				float x = canvas.translateX(pos[0]);
				float y = canvas.translateY(pos[1]);
				Ellipse2D.Float circle = new Ellipse2D.Float(x - radius, y
						- radius, diameter, diameter);
				g2d.fill(circle);
			}
		}
	}

}
