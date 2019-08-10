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
package org.processmining.framework.ui.slicker.launch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.processmining.framework.ui.actions.ConvertInternalAction;
import org.processmining.framework.ui.actions.ExportAction;
import org.processmining.framework.ui.actions.MineAction;
import org.processmining.framework.ui.menus.AnalysisAction;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LaunchActionListCellRenderer extends JLabel implements
		ListCellRenderer {

	protected Color colorMining = new Color(40, 0, 0);
	protected Color colorMiningHilight = new Color(70, 0, 0);
	protected Color colorAnalysis = new Color(0, 40, 0);
	protected Color colorAnalysisHilight = new Color(0, 70, 0);
	protected Color colorConverting = new Color(0, 0, 40);
	protected Color colorConvertingHilight = new Color(0, 0, 70);
	protected Color colorExport = new Color(30, 30, 0);
	protected Color colorExportHilight = new Color(60, 60, 0);

	protected Color colorActive = new Color(0, 0, 0);
	protected Color colorActiveHilight = new Color(40, 40, 40);
	protected Color colorPassiveText = new Color(180, 180, 180);
	protected Color colorActiveText = new Color(250, 250, 250);

	protected int height;
	protected BufferedImage bgMining;
	protected BufferedImage bgAnalysis;
	protected BufferedImage bgConverting;
	protected BufferedImage bgExport;
	protected BufferedImage bgActive;

	protected BufferedImage bg;
	protected boolean selected;
	protected String text;

	public LaunchActionListCellRenderer(int height) {
		this.height = height;
		this.setMinimumSize(new Dimension(100, height));
		this.setMaximumSize(new Dimension(1000, height));
		this.setPreferredSize(new Dimension(100, height));
		this.bgMining = createBackground(this.colorMining,
				this.colorMiningHilight);
		this.bgAnalysis = createBackground(this.colorAnalysis,
				this.colorAnalysisHilight);
		this.bgConverting = createBackground(this.colorConverting,
				this.colorConvertingHilight);
		this.bgExport = createBackground(this.colorExport,
				this.colorExportHilight);
		this.bgActive = createBackground(this.colorActive,
				this.colorActiveHilight);
	}

	protected BufferedImage createBackground(Color color, Color hilight) {
		BufferedImage bg = new BufferedImage(1, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2dBuf = (Graphics2D) bg.getGraphics();
		GradientPaint gradient = new GradientPaint(0, 0, color, 0, height / 3,
				hilight, false);
		g2dBuf.setPaint(gradient);
		g2dBuf.fillRect(0, 0, 1, height / 3);
		gradient = new GradientPaint(0, height / 3, hilight, 0, height, color,
				false);
		g2dBuf.setPaint(gradient);
		g2dBuf.fillRect(0, height / 3, 1, height);
		g2dBuf.dispose();
		return bg;
	}

	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(bg, 0, 0, width, height, this);
		g2d.setFont(g2d.getFont().deriveFont(13f));
		g2d.setColor(new Color(0, 0, 0, 200));
		g2d.drawString(text, 11, height - 8);
		if (selected == true) {
			g2d.setColor(colorActiveText);
			g2d.drawString(text, 10, height - 9);
			int yCenter = height / 2;
			int x[] = { width - 26, width - 10, width - 26 };
			int y[] = { yCenter - 8, yCenter, yCenter + 8 };
			g2d.setColor(new Color(220, 220, 220, 200));
			g2d.fillPolygon(x, y, 3);
		} else {
			g2d.setColor(colorPassiveText);
			g2d.drawString(text, 10, height - 9);
		}
		g2d.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean hasFocus) {
		LaunchActionListCellRenderer renderer = this;
		if (value instanceof MineAction) {
			renderer.text = ((MineAction) value).getPlugin().getName();
			renderer.bg = this.bgMining;
		} else if (value instanceof AnalysisAction) {
			renderer.text = ((AnalysisAction) value).getPlugin().getName();
			renderer.bg = this.bgAnalysis;
		} else if (value instanceof ConvertInternalAction) {
			renderer.text = ((ConvertInternalAction) value).getPlugin()
					.getName();
			renderer.bg = this.bgConverting;
		} else if (value instanceof ExportAction) {
			renderer.text = ((ExportAction) value).getPlugin().getName();
			renderer.bg = this.bgExport;
		}
		renderer.selected = isSelected;
		if (renderer.selected == true) {
			renderer.bg = renderer.bgActive;
		}
		return renderer;
	}

}
