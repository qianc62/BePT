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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.framework.ui.slicker.console.ExpandButton;
import org.processmining.mining.fuzzymining.vis.anim.Animation;
import org.processmining.mining.fuzzymining.vis.anim.AnimationListener;
import org.processmining.mining.fuzzymining.vis.anim.AnimationTimer;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AnimationSidebar extends JPanel implements AnimationListener {

	protected Color colorLeft = new Color(40, 40, 40);
	protected Color colorRight = new Color(20, 20, 20);
	protected int width = 190;

	protected boolean visible = true;
	protected Animation animation;
	protected AnimationTimer timer;

	protected ActivityMeter activityMeter;
	protected CompletionMeter completionMeter;
	protected CaseAnimationPanel caseAnimationPanel;

	protected ExpandButton expandButton;
	protected JPanel expandPanel;
	protected JPanel contentPanel;

	public AnimationSidebar(Animation animation, AnimationTimer timer) {
		contentPanel = new JPanel();
		contentPanel.setOpaque(false);
		contentPanel.setMinimumSize(new Dimension(width, 200));
		contentPanel.setMaximumSize(new Dimension(width, 2000));
		contentPanel.setPreferredSize(new Dimension(width, 1000));
		this.animation = animation;
		this.timer = timer;
		timer.addListener(this);
		this.setOpaque(true);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		this.activityMeter = new ActivityMeter(animation);
		this.activityMeter.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		this.completionMeter = new CompletionMeter(animation);
		this.completionMeter.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		this.caseAnimationPanel = new CaseAnimationPanel(animation);
		RoundedPanel caseView = new RoundedPanel(15, 5, 0);
		caseView.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		caseView.setMinimumSize(new Dimension(170, 100));
		caseView.setMaximumSize(new Dimension(170, 2000));
		caseView.setPreferredSize(new Dimension(170, 800));
		caseView.setBackground(new Color(0, 0, 0));
		caseView.setLayout(new BorderLayout());
		JLabel caseViewLabel = new JLabel("case progress");
		caseViewLabel.setFont(caseViewLabel.getFont().deriveFont(12f));
		caseViewLabel.setBackground(Color.BLACK);
		caseViewLabel.setForeground(new Color(60, 60, 60));
		caseView.add(caseViewLabel, BorderLayout.NORTH);
		caseView.add(this.caseAnimationPanel);
		contentPanel.add(this.activityMeter);
		contentPanel.add(this.completionMeter);
		contentPanel.add(Box.createHorizontalStrut(10));
		contentPanel.add(caseView);
		contentPanel.add(Box.createHorizontalStrut(5));
		expandButton = new ExpandButton();
		expandButton.setAlignmentX(ExpandButton.LEFT_ALIGNMENT);
		expandButton.setExpanded(true);
		expandButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (expandButton.isExpanded() == true) {
					// fold
					visible = false;
					expandButton.setExpanded(false);
					removeAll();
					add(expandPanel);
					add(Box.createVerticalGlue());
					revalidate();
					repaint();
				} else {
					// expand
					visible = true;
					expandButton.setExpanded(true);
					removeAll();
					add(expandPanel);
					add(contentPanel);
					revalidate();
					repaint();
				}
			}
		});
		expandPanel = new JPanel();
		expandPanel.setMaximumSize(new Dimension(200, 10));
		expandPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		expandPanel.setOpaque(false);
		expandPanel.setLayout(new BoxLayout(expandPanel, BoxLayout.X_AXIS));
		expandPanel.add(expandButton);
		expandPanel.add(Box.createHorizontalGlue());
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(expandPanel);
		this.add(contentPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#started()
	 */
	public void started() {
		activityMeter.started();
		completionMeter.started();
		caseAnimationPanel.started();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.vis.anim.AnimationListener#stopped()
	 */
	public void stopped() {
		activityMeter.stopped();
		completionMeter.stopped();
		caseAnimationPanel.stopped();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.mining.fuzzymining.vis.anim.AnimationListener#
	 * updateModelTime(long)
	 */
	public void updateModelTime(long modelTime) {
		if (this.visible == true) {
			activityMeter.updateModelTime(modelTime);
			completionMeter.updateModelTime(modelTime);
			caseAnimationPanel.updateModelTime(modelTime);
		}
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(g2d.getFont().deriveFont(9f));
		float width = this.getWidth();
		float height = this.getHeight();
		// fill background
		g2d.setColor(Color.BLACK);
		g2d.fill(new Rectangle2D.Float(0, 0, width + 1, height + 1));
		if (this.visible == true) {
			GradientPaint gradient = new GradientPaint(width - 4, 0, colorLeft,
					width, 0, colorRight, false);
			g2d.setPaint(gradient);
			g2d.fill(new RoundRectangle2D.Float(-30, 0, width + 30, height,
					30f, 30f));
		}
	}

}
