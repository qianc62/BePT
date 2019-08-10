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

package org.processmining.mining.logabstraction;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.util.ToolTipComboBox;

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

public class AddEntryDialog extends JDialog {
	private JPanel panel1 = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel jLabel1 = new JLabel();
	private ToolTipComboBox endEventBox = new ToolTipComboBox();
	private JLabel jLabel2 = new JLabel();
	private ToolTipComboBox startEventBox = new ToolTipComboBox();
	private JButton okButton = new JButton();
	private JButton cancelButton = new JButton();
	private boolean ok;

	public AddEntryDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public AddEntryDialog() {
		this(null, "", false);
	}

	private void jbInit() throws Exception {
		panel1.setLayout(gridBagLayout1);
		jLabel1.setText("End event");
		jLabel2.setText("Start event");
		okButton.setText("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = !(((String) startEventBox.getSelectedItem())
						.equals((String) endEventBox.getSelectedItem()));
				setVisible(false);
			}
		});
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = false;
				setVisible(false);
			}
		});
		this.getContentPane().add(panel1, BorderLayout.WEST);
		panel1.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		panel1.add(endEventBox, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		panel1.add(jLabel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));
		panel1.add(startEventBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 125, 0));
		if (isModal()) {
			panel1.add(okButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0), 0, 0));
			panel1.add(cancelButton, new GridBagConstraints(1, 2, 1, 1, 0.0,
					0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0), 0, 0));
		}
	}

	public boolean showModal(String[] types) {
		addEvents(types);
		pack();
		setVisible(true);
		return ok;
	}

	public void addEvents(String[] types) {
		for (int i = 0; i < types.length; i++) {
			startEventBox.addItem(types[i]);
			endEventBox.addItem(types[i]);
		}
	}

	public String getStartEvent() {
		return (String) startEventBox.getSelectedItem();
	}

	public String getEndEvent() {
		return (String) endEventBox.getSelectedItem();
	}
}
