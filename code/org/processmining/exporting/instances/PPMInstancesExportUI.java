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

package org.processmining.exporting.instances;

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
import javax.swing.JTextField;

import org.processmining.framework.util.CenterOnScreen;

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

class PPMInstancesExportUI extends JDialog {
	private JTextField proctypetext = new JTextField();
	private JTextField proctypegrouptext = new JTextField();
	private JLabel proctypelabel = new JLabel("Process type");
	private JLabel proctypegrouplabel = new JLabel("Process type group");
	private JButton okButton = new JButton();
	private JPanel panel = new JPanel();
	private boolean ok;

	public PPMInstancesExportUI(Frame frame, String title) {
		super(frame, title, true);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		panel.setLayout(new GridBagLayout());

		okButton.setText("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = (proctypegrouptext.getText() != "")
						&& (proctypetext.getText() != "");
				setVisible(false);
			}
		});

		panel.add(proctypegrouplabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 0, 5), 0, 0));
		panel.add(proctypegrouptext, new GridBagConstraints(0, 1, 1, 1, 1.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 5, 0, 5), 0, 0));
		panel.add(proctypelabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						5, 0, 5), 0, 0));
		panel.add(proctypetext, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 5, 0, 5), 0, 0));
		panel.add(okButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(panel, BorderLayout.CENTER);
	}

	public boolean showModal() {
		pack();
		setSize(getSize().width * 2, getSize().height);
		CenterOnScreen.center(this);
		setVisible(true);
		return ok;
	}

	public String getProcessTypeGroup() {
		return proctypegrouptext.getText();
	}

	public String getProcessType() {
		return proctypetext.getText();
	}
}
