/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.framework.ui.actions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.deckfour.slickerbox.util.FullScreenView;
import org.deckfour.slickerbox.util.GraphicsUtilities;
import org.processmining.framework.ui.About;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.Utils;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FullScreenAction extends CatchOutOfMemoryAction {

	public static BufferedImage icon;
	static {
		try {
			icon = ImageIO.read(new File(About.IMAGELOCATION()
					+ "actionbarlogo.png"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Icon getIcon() {
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/toolbar_fullscreen.png";
		if ((new File(customIconPath).exists())) {
			return new ImageIcon(customIconPath);
		} else {
			return Utils.getStandardIcon("general/Open24");
		}
	}

	public FullScreenAction(MDIDesktopPane desktop, String label) {
		super(label, desktop);
		putValue(SHORT_DESCRIPTION, "Show Full Screen  " + getShortcut());
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
	}

	public FullScreenAction(MDIDesktopPane desktop) {
		super("Full Screen", FullScreenAction.getIcon(), desktop);
		putValue(SHORT_DESCRIPTION, "Show Full Screen  " + getShortcut());
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
	}

	public String getShortcut() {
		// get shortcut according to platform
		String shortCut = null;
		if (RuntimeUtils.isRunningMacOsX() == true) {
			shortCut = "Command-F";
		} else {
			shortCut = "Ctrl+F";
		}
		return shortCut;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.ui.actions.CatchOutOfMemoryAction#execute
	 * (java.awt.event.ActionEvent)
	 */
	@Override
	public void execute(ActionEvent e) {
		JInternalFrame frames[] = MainUI.getInstance().getDesktop()
				.getAllFrames();
		for (JInternalFrame frame : frames) {
			if (frame.isSelected()) {
				final JInternalFrame selectedFrame = frame;
				final Container content = selectedFrame.getContentPane();
				selectedFrame.setContentPane(new JPanel());
				JPanel wrapper = new JPanel();
				wrapper.setLayout(new BorderLayout());
				wrapper.setBorder(BorderFactory.createEmptyBorder());
				wrapper.add(content, BorderLayout.CENTER);
				FullScreenView.enterFullScreen(wrapper, frame.getTitle(), null,
						new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								selectedFrame.setContentPane(content);
								selectedFrame.revalidate();
								selectedFrame.repaint();
							}
						}, icon);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.ui.actions.CatchOutOfMemoryAction#handleOutOfMem
	 * ()
	 */
	@Override
	protected void handleOutOfMem() {
		// TODO Auto-generated method stub

	}

}
