/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.util;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

import org.processmining.framework.ui.MainUI;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class CenterOnScreen {

	public static int width() {
		return (int) (0.8 * Toolkit.getDefaultToolkit().getScreenSize().width);
	}

	public static int height() {
		return (int) (0.8 * Toolkit.getDefaultToolkit().getScreenSize().height);
	}

	public static void center(Window frame) {
		int w = frame.getPreferredSize().width;
		int h = frame.getPreferredSize().height;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		frame.setSize(Math.min(w, width()) + frame.getInsets().left
				+ frame.getInsets().right + 20, Math.min(h, height())
				+ frame.getInsets().top + frame.getInsets().bottom + 20);

		frame.setLocation(Math.max(0,
				(screenSize.width - frame.getSize().width) / 2), Math.max(0,
				(screenSize.height - frame.getSize().height) / 2));
		frame.validate();
	}

	public static void centerOnMainUI(Window frame) {
		int width = MainUI.getInstance().getWidth();
		int height = MainUI.getInstance().getHeight();
		frame.setLocation(Math.max(0, (width - frame.getSize().width) / 2),
				Math.max(0, (height - frame.getSize().height) / 2));
		frame.validate();
	}
}
