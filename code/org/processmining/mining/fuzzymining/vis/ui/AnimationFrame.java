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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.deckfour.slickerbox.util.FullScreenView;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.actions.FullScreenAction;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.vis.anim.Animation;
import org.processmining.mining.fuzzymining.vis.anim.AnimationTimer;
import org.processmining.mining.fuzzymining.vis.paint.AnimationCanvas;
import org.processmining.mining.fuzzymining.vis.paint.ColorSettings;
import org.processmining.mining.fuzzymining.vis.paint.UnbufferedAnimationCanvas;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class AnimationFrame extends JPanel {

	protected LogReader log;
	protected FuzzyGraph graph;
	protected int maxLookahead;
	protected int maxExtraLookahead;
	protected Animation animation;
	protected AnimationTimer timer;
	protected AnimationCanvas canvas;
	protected ControlPanel controlPanel;
	protected JScrollPane scrollPane;
	protected AnimationSidebar sidebar;
	protected ProgressPanel progress;
	protected final JPanel content;

	public AnimationFrame(LogReader log, FuzzyGraph graph, int maxLookahead,
			int maxExtraLookahead) {
		this.log = log;
		this.graph = graph;
		this.maxLookahead = maxLookahead;
		this.maxExtraLookahead = maxExtraLookahead;
		this.progress = new ProgressPanel("Creating animation");
		this.content = new JPanel();
		this.content.setBorder(BorderFactory.createEmptyBorder());
		this.content.setLayout(new BorderLayout());
		HeaderBar header = new HeaderBar("Fuzzy animation");
		header.setHeight(40);
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout());
		content.add(header, BorderLayout.NORTH);
		content.add(this.progress.getPanel());
		this.add(content, BorderLayout.CENTER);
		setupAnimation();
	}

	public LogReader getLog() {
		return log;
	}

	public Animation getAnimation() {
		return animation;
	}

	public FuzzyGraph getGraph() {
		return graph;
	}

	public void setupAnimation() {
		final AnimationFrame animFrame = this;
		Thread setupThread = new Thread() {
			public void run() {
				try {
					animation = Animation.generate(graph, log, maxLookahead,
							maxExtraLookahead, progress);
				} catch (IndexOutOfBoundsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				double speedFactor = (double) animation.getMeanBetweenTime() / 1000.0;
				timer = new AnimationTimer(animation.getStart(), animation
						.getEnd(), speedFactor, 20);
				canvas = new UnbufferedAnimationCanvas(animation,
						ColorSettings.SETTINGS_NIGHT);
				timer.addListener(canvas);
				controlPanel = new ControlPanel(animFrame);
				sidebar = new AnimationSidebar(animation, timer);
				sidebar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
				scrollPane = new JScrollPane(canvas);
				scrollPane.getHorizontalScrollBar().setUI(
						new SlickerScrollBarUI(scrollPane
								.getHorizontalScrollBar()));
				scrollPane.getVerticalScrollBar().setUI(
						new SlickerScrollBarUI(scrollPane
								.getVerticalScrollBar()));
				scrollPane
						.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane
						.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setBorder(BorderFactory.createEmptyBorder());
				scrollPane.setBackground(Color.BLACK);
				content.setBorder(BorderFactory.createEmptyBorder());
				content.setLayout(new BorderLayout());
				content.removeAll();
				content.add(scrollPane, BorderLayout.CENTER);
				content.add(controlPanel, BorderLayout.SOUTH);
				content.add(sidebar, BorderLayout.WEST);
				content.revalidate();
			}
		};
		setupThread.start();
	}

	public void showFullScreen() {
		this.remove(content);
		final AnimationFrame animFrame = this;
		FullScreenView.enterFullScreen(content, "Fuzzy Model Animation",
				animFrame, new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						controlPanel.setFullScreenButtonVisible(true);
						animFrame.add(content, BorderLayout.CENTER);
						animFrame.invalidate();
						animFrame.revalidate();
						animFrame.repaint();
						try {
							Thread.sleep(60);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, FullScreenAction.icon);
	}

	public AnimationCanvas getCanvas() {
		return this.canvas;
	}

	public AnimationTimer getTimer() {
		return this.timer;
	}

}
