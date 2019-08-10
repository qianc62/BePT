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

package org.processmining.framework.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.processmining.framework.plugin.Plugin;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class AlgorithmListItem implements Comparable<AlgorithmListItem> {

	private Plugin algorithm;
	private InputItem[] items;
	private JPanel panel;

	public AlgorithmListItem(final Plugin algorithm, InputItem[] items) {
		JButton docsButton = new JButton("Plugin documentation...");

		this.algorithm = algorithm;
		this.items = items;

		panel = new JPanel(new GridBagLayout());
		for (int i = 0; i < items.length; i++) {
			panel.add(items[i].getPanel(), new GridBagConstraints(0, i, 1, 1,
					0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.BOTH, new Insets(0, 5, 5, 0), 0, 0));
		}
		docsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(algorithm);
			}
		});
		panel.add(docsButton, new GridBagConstraints(0, items.length, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(30, 5, 5, 5), 0, 0));
	}

	public String toString() {
		return algorithm.getName();
	}

	public Plugin getAlgorithm() {
		return algorithm;
	}

	public InputItem[] getItems() {
		return items;
	}

	public JPanel getPanel() {
		return panel;
	}

	public void refresh() {
		for (int i = 0; i < items.length; i++) {
			items[i].refresh();
		}
	}

	public int compareTo(AlgorithmListItem o) {
		if (o == null)
			return -1;
		return -o.getAlgorithm().getName().toLowerCase().compareTo(
				getAlgorithm().getName().toLowerCase());
	}
}
