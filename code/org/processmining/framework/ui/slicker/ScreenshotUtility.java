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

import java.awt.FileDialog;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JInternalFrame;

import org.processmining.framework.ui.MainUI;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ScreenshotUtility {

	public static void captureApplicationScreenshot() {
		MainUI ui = MainUI.getInstance();
		BufferedImage img = new BufferedImage(ui.getWidth(), ui.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		ui.paint(img.createGraphics());
		FileDialog dialog = new FileDialog(ui,
				"Save application screenshot...", FileDialog.SAVE);
		dialog.setFilenameFilter(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith("png") || name.endsWith("PNG"));
			}
		});
		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			File outFile = new File(dialog.getDirectory() + File.separator
					+ dialog.getFile());
			try {
				ImageIO.write(img, "PNG", outFile);
			} catch (IOException e) {
				// oops...
				e.printStackTrace();
			}
		}
	}

	public static void captureActiveFrameScreenshot() {
		JInternalFrame frame = MainUI.getInstance().getDesktop()
				.getSelectedFrame();
		if (frame == null) {
			return;
		}
		BufferedImage img = new BufferedImage(frame.getWidth(), frame
				.getHeight(), BufferedImage.TYPE_INT_ARGB);
		frame.paint(img.createGraphics());
		FileDialog dialog = new FileDialog(MainUI.getInstance(),
				"Save frame screenshot...", FileDialog.SAVE);
		dialog.setFilenameFilter(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith("png") || name.endsWith("PNG"));
			}
		});
		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			File outFile = new File(dialog.getDirectory() + File.separator
					+ dialog.getFile());
			try {
				ImageIO.write(img, "PNG", outFile);
			} catch (IOException e) {
				// oops...
				e.printStackTrace();
			}
		}
	}

}
