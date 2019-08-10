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
package org.processmining.mining.fuzzymining.vis.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JComponent;

import org.processmining.mining.fuzzymining.vis.anim.Animation;
import org.processmining.mining.fuzzymining.vis.anim.AnimationListener;
import org.processmining.mining.fuzzymining.vis.anim.ArcAnimationKeyframe;
import org.processmining.mining.fuzzymining.vis.anim.TokenAnimation;
import org.processmining.mining.fuzzymining.vis.paint.ColorSettings;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class CaseAnimationPanel extends JComponent implements AnimationListener {

	protected int width = 150;
	protected int border = 10;
	protected float flagWidth = 30;
	protected float flagHeight = 40;
	protected float halfFlagWidth = flagWidth / 2;
	protected float quarterFlagHeight = flagHeight / 4;
	protected float threeQuarterFlagheight = flagHeight * 0.75f;

	protected Color bgColor = new Color(0, 0, 0);
	protected Color textColor = new Color(200, 200, 200, 120);
	protected Color youngColor = new Color(0, 0, 240, 230);
	protected Color oldColor = new Color(220, 0, 0, 160);

	protected Animation anim;
	protected long modelTime;

	protected float currentOffset = border;
	protected HashMap<String, Float> offsetMap = new HashMap<String, Float>();
	protected Random random = new Random();

	public CaseAnimationPanel(Animation anim) {
		this.anim = anim;
		this.modelTime = anim.getStart();
		this.setMinimumSize(new Dimension(width, 100));
		this.setMaximumSize(new Dimension(width, 2000));
		this.setPreferredSize(new Dimension(width, 1000));
	}

	protected void paintComponent(Graphics g) {
		long time = this.modelTime;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(g2d.getFont().deriveFont(9f));
		FontMetrics fontMetrics = g2d.getFontMetrics();
		float width = this.getWidth();
		float height = this.getHeight();
		// fill background
		g2d.setColor(bgColor);
		g2d.fill(new Rectangle2D.Float(0, 0, width + 1, height + 1));
		// draw flags
		long coolTime = this.anim.getMeanCaseDuration();
		float flagPaneHeight = height - border - border + flagHeight;
		ArcAnimationKeyframe keyframe = this.anim.getCaseAnimation()
				.getKeyframe(time);
		for (TokenAnimation anim : keyframe.getTokenAnimations()) {
			drawFlag(g2d, fontMetrics, flagPaneHeight, anim, time, coolTime);
		}
		// draw shadings
		GradientPaint gradient = new GradientPaint(0, border, new Color(0, 0,
				0, 160), 0, border + border, new Color(0, 0, 0, 0), false);
		g2d.setPaint(gradient);
		g2d.fill(new Rectangle2D.Float(0, 0, width + 1, border + border + 1));
		gradient = new GradientPaint(0, height - border - border, new Color(0,
				0, 0, 0), 0, height - border, new Color(0, 0, 0, 160), false);
		g2d.setPaint(gradient);
		g2d.fill(new Rectangle2D.Float(0, height - border - border, width + 1,
				border + border + 1));
		g2d.setColor(new Color(0, 0, 0));
		g2d.fill(new Rectangle2D.Float(0, 0, width + 1, border));
		g2d.fill(new Rectangle2D.Float(0, height - border, width + 1,
				border + 1));
	}

	protected void drawFlag(Graphics2D g2d, FontMetrics fontMetrics,
			float height, TokenAnimation anim, long time, long coolTime) {
		// calculate flag coordinates
		float flagY = border + (height * anim.completedPercentage(time))
				- flagHeight;
		float flagX = getOffset(anim.getCaseId());
		// determine flag background color
		Color flagBg;
		long runTime = time - anim.getStart();
		if (runTime >= coolTime) {
			flagBg = oldColor;
		} else {
			float pct = (float) runTime / (float) coolTime;
			flagBg = ColorSettings.mix(youngColor, oldColor, pct);
		}
		// assemble flag path
		GeneralPath flagPath = new GeneralPath();
		flagPath.moveTo(flagX, flagY);
		flagPath.lineTo(flagX, flagY + threeQuarterFlagheight);
		flagPath.lineTo(flagX + halfFlagWidth, flagY + flagHeight);
		flagPath.lineTo(flagX + flagWidth, flagY + threeQuarterFlagheight);
		flagPath.lineTo(flagX + flagWidth, flagY);
		flagPath.lineTo(flagX + halfFlagWidth, flagY + quarterFlagHeight);
		flagPath.closePath();
		// set background color and draw flag background
		GradientPaint gradient = new GradientPaint(flagX, flagY, ColorSettings
				.setAlpha(flagBg, 0.4f), flagX, flagY + flagHeight, flagBg,
				false);
		g2d.setPaint(gradient);
		g2d.fill(flagPath);
		// set border color and draw flag border
		Color borderColor = ColorSettings.darken(flagBg, 0.5f);
		gradient = new GradientPaint(flagX, flagY, ColorSettings.setAlpha(
				borderColor, 0.5f), flagX, flagY + flagHeight, borderColor,
				false);
		g2d.setPaint(gradient);
		g2d.draw(flagPath);
		// draw instance name
		String name = anim.getCaseId();
		if (name.length() > 12) {
			name = "..." + name.substring(name.length() - 11);
		}
		Rectangle2D sBounds = fontMetrics.getStringBounds(name, g2d);
		g2d.setColor(textColor);
		g2d.drawString(name, flagX + halfFlagWidth
				- (float) (sBounds.getWidth() / 2f), flagY + (flagHeight / 2f)
				+ (float) (sBounds.getHeight() * 0.6f));
	}

	protected float getOffset(String caseId) {
		Float ofObj = offsetMap.get(caseId);
		if (ofObj != null) {
			return ofObj;
		} else {
			// calculate new offset
			float offset = currentOffset;
			offsetMap.put(caseId, offset);
			currentOffset = currentOffset + 5
					+ (random.nextFloat() * flagWidth);
			if (currentOffset > (width - border - border - flagWidth)) {
				currentOffset = border + (random.nextFloat() * (flagWidth / 2));
			}
			return offset;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#started()
	 */
	public void started() {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#stopped()
	 */
	public void stopped() {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.vis.anim.AnimationListener#
	 * updateModelTime(long)
	 */
	public void updateModelTime(long modelTime) {
		this.modelTime = modelTime;
		repaint();
	}

}
