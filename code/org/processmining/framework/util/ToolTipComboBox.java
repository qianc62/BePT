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

import java.awt.Component;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

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
public class ToolTipComboBox extends JComboBox {
	/**
	 * Creates a <code>ToolTipComboBox</code> that takes it's items from an
	 * existing <code>ComboBoxModel</code>. Since the <code>ComboBoxModel</code>
	 * is provided, a combo box created using this constructor does not create a
	 * default combo box model and may impact how the insert, remove and add
	 * methods behave.
	 * 
	 * @param aModel
	 *            the <code>ComboBoxModel</code> that provides the displayed
	 *            list of items
	 * @see DefaultComboBoxModel
	 */
	public ToolTipComboBox(ComboBoxModel aModel) {
		super(aModel);
		setRenderer(new ToolTipComboBoxRenderer());
	}

	/**
	 * Creates a <code>ToolTipComboBox</code> that contains the elements in the
	 * specified array. By default the first item in the array (and therefore
	 * the data model) becomes selected.
	 * 
	 * @param items
	 *            an array of objects to insert into the combo box
	 * @see DefaultComboBoxModel
	 */
	public ToolTipComboBox(final Object items[]) {
		super(items);
		setRenderer(new ToolTipComboBoxRenderer());
	}

	/**
	 * Creates a <code>ToolTipComboBox</code> that contains the elements in the
	 * specified Vector. By default the first item in the vector and therefore
	 * the data model) becomes selected.
	 * 
	 * @param items
	 *            an array of vectors to insert into the combo box
	 * @see DefaultComboBoxModel
	 */
	public ToolTipComboBox(Vector items) {
		super(items);
		setRenderer(new ToolTipComboBoxRenderer());
	}

	/**
	 * Creates a <code>ToolTipComboBox</code> with a default data model. The
	 * default data model is an empty list of objects. Use <code>addItem</code>
	 * to add items. By default the first item in the data model becomes
	 * selected.
	 * 
	 * @see DefaultComboBoxModel
	 */
	public ToolTipComboBox() {
		super();
		setRenderer(new ToolTipComboBoxRenderer());
	}
}

class ToolTipComboBoxRenderer extends BasicComboBoxRenderer {
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
			if (-1 < index) {
				list.setToolTipText(value.toString());
			}
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setFont(list.getFont());
		setText((value == null) ? "" : value.toString());
		return this;
	}
}
