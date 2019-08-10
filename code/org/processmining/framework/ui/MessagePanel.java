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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class MessagePanel extends JPanel {
	BorderLayout borderLayout1 = new BorderLayout();
	JTextArea[] messageAreas = new JTextArea[Message.NUM_TYPES];
	JTabbedPane tabs = new JTabbedPane();

	private ActionListener messageListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (e.getModifiers() == Message.CLEAR_MESSAGES) {
				for (int i = 0; i < messageAreas.length; i++) {
					messageAreas[i].setText("");
				}
			} else {
				final JTextArea area = messageAreas[e.getModifiers()];

				area.append(e.getActionCommand());
				area.append("\n");

				// scroll to the end of the text area
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						area.setCaretPosition(area.getText().length());
					}
				});
				// tabs.setSelectedIndex(e.getModifiers());
			}
		}
	};

	public MessagePanel() {
		try {
			jbInit();
			startListening();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void stopListening() {
		Message.removeActionListener(messageListener);
	}

	public void startListening() {
		Message.addActionListener(messageListener);
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		this.add(tabs, BorderLayout.CENTER);
		// this.add(Message.getNewProgressBar(), BorderLayout.SOUTH);
		tabs.setTabPlacement(JTabbedPane.BOTTOM);

		for (int i = 0; i < Message.NUM_TYPES; i++) {
			JScrollPane scrollPane = new JScrollPane();

			messageAreas[i] = new JTextArea();
			messageAreas[i].setEditable(false);

			scrollPane.getViewport().add(messageAreas[i], null);
			scrollPane.setPreferredSize(new Dimension(getWidth(), getHeight()));
			scrollPane.setSize(new Dimension(getWidth(), getHeight()));
			if (i != Message.TEST || UISettings.getInstance().getTest()) {
				tabs.addTab(Message.TYPE_NAMES[i], null, scrollPane);
			}
		}
	}
}
