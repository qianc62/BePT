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

/*
 * Copyright (c) 2006 Eindhoven University of Technology
 * All rights reserved.
 */

package org.processmining.analysis.performance;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/**
 * This class is needed to allow the pop-up menu of a combobox to be of a
 * different size than the size of the combobox itself.
 * 
 * @author: Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */

public class SteppedComboBoxUI extends BasicComboBoxUI {

	// Creates the pop up
	protected ComboPopup createPopup() {
		Popup popup = new Popup(comboBox);
		popup.getAccessibleContext().setAccessibleParent(comboBox);
		return popup;
	}

	private static class Popup extends BasicComboPopup {
		private JComboBox box = comboBox;

		public Popup(JComboBox comboBox) {
			super(comboBox);
			box = comboBox;
			this.setLayout(new GridLayout(comboBox.getModel().getSize(), 1, 5,
					5));
		}

		// This is where the width and height of the pop up is adjusted
		public Dimension getPreferredSize() {
			Dimension size = super.getPreferredSize();
			// determine height of the pop-up
			int tempHeight = comboBox.getModel().getSize();
			if (tempHeight > 8) {
				// allow at most 8 items in pop-up
				size.height = super.getPopupHeightForRowCount(8);
			} else {
				size.height = super.getPopupHeightForRowCount(tempHeight);
			}
			// determine width of the pop-up, this being the width of the
			// largest
			// item in the combobox
			size.width = box.getPreferredSize().width;
			return size;
		}
	}
}
