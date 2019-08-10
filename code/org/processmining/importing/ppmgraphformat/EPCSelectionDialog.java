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

package org.processmining.importing.ppmgraphformat;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.CenterOnScreen;
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

public class EPCSelectionDialog extends JDialog {
	JPanel jPanel1 = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	JButton jButton1 = new JButton();
	JButton jButton2 = new JButton();
	JPanel jPanel2 = new JPanel();
	ToolTipComboBox jComboBox1;
	JLabel jLabel1 = new JLabel();
	ArrayList EPCs = new ArrayList();
	private boolean ok;

	public EPCSelectionDialog(ArrayList EPCs) throws HeadlessException {
		super(MainUI.getInstance(), "Select EPC to Import", true);
		this.EPCs = EPCs;
		try {
			jbInit();
			centerDialog();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void centerDialog() {
		pack();
		CenterOnScreen.center(this);
	}

	public boolean showDialog() {
		setVisible(true);
		return ok;
	}

	void selectButton_actionPerformed(ActionEvent e) {
		ok = true;
		setVisible(false);

	}

	void cancelButton_actionPerformed(ActionEvent e) {
		ok = false;
		setVisible(false);
	}

	private void jbInit() throws Exception {
		this.getContentPane().setLayout(borderLayout1);
		jButton1.setText("Select");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				selectButton_actionPerformed(actionEvent);
			}
		});

		jButton2.setText("Cancel");
		jButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				cancelButton_actionPerformed(actionEvent);
			}
		});

		jLabel1.setText("Select one EPC");
		this.getContentPane().add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(jButton1, null);
		jPanel1.add(jButton2, null);
		this.getContentPane().add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jLabel1, null);
		jComboBox1 = new ToolTipComboBox(EPCs.toArray());
		jPanel2.add(jComboBox1, null);
		pack();
		repaint();
	}

	public int getEPCId() {
		return jComboBox1.getSelectedIndex();
	}

}
