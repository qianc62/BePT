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

package org.processmining.analysis.summary;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.SwingWorker;

/**
 * 
 *<p>
 * Shows the statistical information about a log.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla Alves de Medeiros
 * @author Christian W. Guenther
 * @version 1.0
 */
public class LogSummaryUI extends JPanel implements Provider {

	protected LogSummary summary = null;
	protected LogReader log = null;
	protected JTextPane textPane = null;

	public LogSummaryUI(LogReader log) {
		this(log.getLogSummary());
	}

	public LogSummaryUI(LogSummary summary) {
		this.summary = summary;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void jbInit() throws Exception {

		// Create a text area.
		textPane = createTextPane();

		JScrollPane netContainer = new JScrollPane(textPane);

		this.setLayout(new BorderLayout());
		this.add(netContainer, BorderLayout.CENTER);

		// add a panel with button to allow saving the log summary to
		// a HTML file
		JButton saveButton = new JButton("save as HTML...");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser saveDialog = new JFileChooser();
				saveDialog.setSelectedFile(new File("ProM_LogSummary.html"));
				if (saveDialog.showSaveDialog(MainUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
					File outFile = saveDialog.getSelectedFile();
					try {
						BufferedWriter outWriter = new BufferedWriter(
								new FileWriter(outFile));
						outWriter.write(textPane.getText());
						outWriter.flush();
						outWriter.close();
						JOptionPane.showMessageDialog(MainUI.getInstance(),
								"Log summary has been saved\nto HTML file!",
								"Log summary saved.",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		JPanel savePanel = new JPanel();
		savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.X_AXIS));
		savePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
		savePanel.add(Box.createHorizontalGlue());
		savePanel.add(saveButton);
		this.add(savePanel, BorderLayout.SOUTH);

		// create summary HTML asynchronously (can take a while with large logs)
		SwingWorker worker = new SwingWorker() {

			protected String htmlSummary = null;

			public Object construct() {
				htmlSummary = summary.toString();
				return null;
			}

			public void finished() {
				textPane.setText(htmlSummary);
				textPane.setCaretPosition(0);
			}

		};
		worker.start();
	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[0];
		if (log != null) {
			objects = new ProvidedObject[] { new ProvidedObject("Log reader",
					new Object[] { log }) };
		}
		return objects;
	}

	protected JTextPane createTextPane() {
		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		// pre-populate the text pane with some teaser message
		textPane
				.setText("<html><body bgcolor=\"BBBBAA\" text=\"555544\">"
						+ "<br><br><br><br><br><center><font face=\"helvetica,arial,sans-serif\" size=\"+1\">"
						+ "please wait while the summary is created...</font></center></body></html>");
		textPane.setEditable(false);
		textPane.setCaretPosition(0);
		return textPane;
	}

}
