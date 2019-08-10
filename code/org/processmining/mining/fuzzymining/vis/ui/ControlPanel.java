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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.InspectorButton;
import org.deckfour.slickerbox.components.PlayButton;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.ui.SlickerSliderUI;
import org.processmining.mining.fuzzymining.vis.anim.AnimationListener;
import org.processmining.mining.fuzzymining.vis.anim.AnimationTimer;
import org.processmining.mining.fuzzymining.vis.paint.AnimationCanvas;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ControlPanel extends JPanel {

	protected final AnimationFrame parentFrame;
	protected final AnimationCanvas canvas;
	protected final AnimationTimer timer;
	protected boolean running;

	protected TimeDisplay timeDisplay;
	protected AnimationSeekPane seekPane;

	protected JSlider zoomSlider;
	protected JSlider speedSlider;

	protected InspectorButton fullScreenButton;

	public ControlPanel(AnimationFrame frame) {
		this.parentFrame = frame;
		this.canvas = frame.getCanvas();
		this.timer = frame.getTimer();
		this.seekPane = new AnimationSeekPane(canvas.getAnimation(), timer);
		this.timeDisplay = new TimeDisplay();
		this.timer.addListener(this.timeDisplay);
		running = false;
		final PlayButton playButton = new PlayButton();
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (running == false) {
					timer.start();
					running = true;
				} else {
					timer.stop();
					running = false;
				}
			}
		});
		this.timer.addListener(new AnimationListener() {
			public void started() {
				playButton.setPlay(false);
				running = true;
			}

			public void stopped() {
				playButton.setPlay(true);
				running = false;
			}

			public void updateModelTime(long modelTime) {
				// ignore for now
			}
		});
		zoomSlider = new JSlider(20, 1500, 500);
		zoomSlider.setUI(new SlickerSliderUI(zoomSlider, new Color(40, 40, 40),
				new Color(70, 70, 70), new Color(0, 0, 0),
				new Color(80, 80, 80), new Color(0, 0, 0),
				new Color(40, 40, 40), 8, 14));
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float zoom = (float) zoomSlider.getValue() / (float) 1000;
				canvas.setZoom(zoom);
			}
		});
		final double speedRef = timer.getSpeedFactor();
		speedSlider = new JSlider(1, 2000, 100);
		speedSlider.setUI(new SlickerSliderUI(speedSlider,
				new Color(40, 40, 40), new Color(70, 70, 70),
				new Color(0, 0, 0), new Color(80, 80, 80), new Color(0, 0, 0),
				new Color(40, 40, 40), 8, 14));
		speedSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double factor = (double) speedSlider.getValue() / 100.0;
				timer.setSpeedFactor(speedRef * factor);
			}
		});
		fullScreenButton = new InspectorButton();
		fullScreenButton.setToolTipText("show full screen");
		fullScreenButton.setAlignmentY(CENTER_ALIGNMENT);
		fullScreenButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		fullScreenButton.setMinimumSize(new Dimension(24, 24));
		fullScreenButton.setPreferredSize(new Dimension(24, 24));
		fullScreenButton.setColors(new Color(255, 0, 0), new Color(220, 220,
				220), new Color(140, 140, 140), new Color(0, 0, 0), new Color(
				20, 20, 20), new Color(30, 30, 30));
		fullScreenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setFullScreenButtonVisible(false);
				parentFrame.showFullScreen();
			}
		});
		// compose UI
		this.setBackground(new Color(30, 30, 30));
		this.setBorder(BorderFactory.createEmptyBorder(11, 15, 11, 15));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		// this.add(zoomSlider);
		this.add(playButton);
		this.add(Box.createHorizontalStrut(13));
		this.add(seekPane);
		this.add(Box.createHorizontalStrut(13));
		this.add(timeDisplay);
		this.add(Box.createHorizontalStrut(10));
		this.add(packSlider(speedSlider, "playback speed"));
		this.add(Box.createHorizontalStrut(10));
		this.add(packSlider(zoomSlider, "zoom view"));
		this.add(Box.createHorizontalStrut(10));
		this.add(fullScreenButton);
	}

	public void setFullScreenButtonVisible(boolean visible) {
		this.fullScreenButton.setVisible(visible);
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int width = getWidth();
		int height = getHeight();
		GradientPaint gradient = new GradientPaint(0, 0, new Color(70, 70, 70),
				0, height - (height / 3), new Color(60, 60, 60), false);
		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, width + 1, height + 1);
	}

	protected JPanel packSlider(JSlider slider, String name) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);
		JLabel label = new JLabel(name);
		label.setForeground(new Color(13, 13, 13));
		label.setOpaque(false);
		label.setFont(label.getFont().deriveFont(10f));
		label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		label.setHorizontalTextPosition(JLabel.LEFT);
		slider.setAlignmentX(JSlider.CENTER_ALIGNMENT);
		panel.add(Box.createVerticalGlue());
		panel.add(slider);
		panel.add(label);
		panel.add(Box.createVerticalGlue());
		return panel;
	}

}
