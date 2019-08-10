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
package org.processmining.framework.ui.slicker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerProgressBar;
import org.processmining.framework.ui.Progress;

/**
 * Implements the 'Progress' interface of ProM in form of a panel.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 */
public class ProgressPanel extends Progress {

	protected static Color colorBgUp = new Color(80, 80, 80);
	protected static Color colorBgDown = new Color(40, 40, 40);
	protected static Color colorBgInner = new Color(200, 200, 200, 120);
	protected static Color colorFg = new Color(40, 40, 40);

	protected JPanel panel;
	protected JProgressBar progress;
	protected JLabel label;
	protected JLabel title;
	protected JButton abort;

	public ProgressPanel(String aTitle) {
		this(aTitle, true, null);
	}

	public ProgressPanel(String aTitle, boolean isOpaque) {
		this(aTitle, isOpaque, null);
	}

	public ProgressPanel(String aTitle, boolean isOpaque,
			ActionListener abortListener) {
		if (isOpaque == true) {
			panel = new GradientPanel(colorBgUp, colorBgDown);
		} else {
			panel = new JPanel();
			panel.setOpaque(false);
			panel.setBorder(BorderFactory.createEmptyBorder());
		}
		if (abortListener != null) {
			abort = new AutoFocusButton("cancel");
			abort.setEnabled(true);
			abort.addActionListener(abortListener);
		} else {
			abort = null;
		}
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		RoundedPanel innerPanel = new RoundedPanel(20, 0, 0);
		innerPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		innerPanel.setBackground(colorBgInner);
		innerPanel.setMinimumSize(new Dimension(300, 120));
		innerPanel.setMaximumSize(new Dimension(600, 130));
		innerPanel.setPreferredSize(new Dimension(500, 130));
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
		progress = new SlickerProgressBar();
		// progress = new JProgressBar();
		// progress.setUI(new SlickerRoundProgressBarVerboseUI(40, new Color(60,
		// 60, 60)));
		progress.setOpaque(false);
		progress.setIndeterminate(true);
		title = new JLabel(aTitle);
		title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		title.setOpaque(false);
		title.setFont(title.getFont().deriveFont(16.0f));
		label = new JLabel("");
		label.setFont(label.getFont().deriveFont(12.0f));
		label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		label.setOpaque(false);
		innerPanel.add(Box.createVerticalGlue());
		innerPanel.add(alignLeft(title));
		innerPanel.add(Box.createVerticalStrut(10));
		innerPanel.add(progress);
		innerPanel.add(Box.createVerticalStrut(8));
		innerPanel.add(alignLeft(label));
		if (abort != null) {
			innerPanel.add(alignRight(abort));
		}
		innerPanel.add(Box.createVerticalGlue());
		panel.add(Box.createHorizontalGlue());
		panel.add(innerPanel);
		panel.add(Box.createHorizontalGlue());
	}

	public JPanel getPanel() {
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#close()
	 */
	@Override
	public void close() {
		// ignore this one
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#getMaximum()
	 */
	@Override
	public int getMaximum() {
		return progress.getMaximum();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setMaximum(int)
	 */
	@Override
	public void setMaximum(int m) {
		progress.setMaximum(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setMinimum(int)
	 */
	@Override
	public void setMinimum(int m) {
		progress.setMinimum(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setMinMax(int, int)
	 */
	@Override
	public void setMinMax(int min, int max) {
		progress.setMinimum(min);
		progress.setMaximum(max);
		if (panel.isDisplayable()) {
			progress.repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setNote(java.lang.String)
	 */
	@Override
	public void setNote(String note) {
		label.setText(note);
		label.revalidate();
		if (panel.isDisplayable()) {
			label.repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#setProgress(int)
	 */
	@Override
	public void setProgress(int nv) {
		if (nv >= 0) {
			progress.setIndeterminate(false);
			progress.setValue(nv);
		} else {
			progress.setIndeterminate(true);
		}
		if (panel.isDisplayable()) {
			progress.repaint();
		}
	}

	public JProgressBar getProgressBar() {
		return progress;
	}

	protected JPanel alignLeft(JComponent component) {
		JPanel enclosure = new JPanel();
		enclosure.setOpaque(false);
		enclosure.setBorder(BorderFactory.createEmptyBorder());
		enclosure.setLayout(new BoxLayout(enclosure, BoxLayout.X_AXIS));
		enclosure.add(component);
		enclosure.add(Box.createHorizontalGlue());
		return enclosure;
	}

	protected JPanel alignRight(JComponent component) {
		JPanel enclosure = new JPanel();
		enclosure.setOpaque(false);
		enclosure.setBorder(BorderFactory.createEmptyBorder());
		enclosure.setLayout(new BoxLayout(enclosure, BoxLayout.X_AXIS));
		enclosure.add(Box.createHorizontalGlue());
		enclosure.add(component);
		return enclosure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.ui.Progress#inc()
	 */
	@Override
	public void inc() {
		setProgress(progress.getValue() + 1);
	}

	public int getValue() {
		return progress.getValue();
	}

}
