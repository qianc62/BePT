/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.framework.ui.slicker.logdialog;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.SwingWorker;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LogSummaryUI extends JPanel {

	protected LogReader log;
	protected SlickerOpenLogSettings parent;
	protected JTextPane summaryPane = null;

	public LogSummaryUI(SlickerOpenLogSettings settings) {
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(3, 10, 5, 10));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.parent = settings;
		this.log = null;
		this.summaryPane = new JTextPane();
		this.summaryPane.setBorder(BorderFactory.createEmptyBorder());
		this.summaryPane.setContentType("text/html");
		// pre-populate the text pane with some teaser message
		this.summaryPane
				.setText("<html><body bgcolor=\"#888888\" text=\"#333333\">"
						+ "<br><br><br><br><br><center><font face=\"helvetica,arial,sans-serif\" size=\"4\">"
						+ "Please wait while the summary is created...</font></center></body></html>");
		this.summaryPane.setEditable(false);
		this.summaryPane.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(this.summaryPane);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		JScrollBar vBar = scrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(20, 20, 20), new Color(60, 60, 60), 4, 12));
		vBar.setOpaque(false);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		RoundedPanel scrollEnclosure = new RoundedPanel(10, 0, 0);
		scrollEnclosure.setBackground(Color.decode("#888888"));
		scrollEnclosure.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		scrollEnclosure.setLayout(new BorderLayout());
		scrollEnclosure.add(scrollPane, BorderLayout.CENTER);
		JLabel header = new JLabel("Log Summary");
		header.setOpaque(false);
		header.setForeground(new Color(200, 200, 200, 180));
		header.setFont(header.getFont().deriveFont(15f));
		JButton saveButton = new SlickerButton("save HTML...");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser saveDialog = new JFileChooser();
				saveDialog.setSelectedFile(new File("ProM_LogSummary.html"));
				if (saveDialog.showSaveDialog(MainUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
					File outFile = saveDialog.getSelectedFile();
					try {
						BufferedWriter outWriter = new BufferedWriter(
								new FileWriter(outFile));
						outWriter.write(summaryPane.getText());
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
		JPanel headerPanel = new JPanel();
		headerPanel.setOpaque(false);
		headerPanel.setBorder(BorderFactory.createEmptyBorder());
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		headerPanel.add(header);
		headerPanel.add(Box.createHorizontalGlue());
		headerPanel.add(saveButton);
		this.add(headerPanel);
		this.add(Box.createVerticalStrut(7));
		this.add(scrollEnclosure);
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				checkRecompileSummary();
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});
	}

	protected void checkRecompileSummary() {
		// create summary HTML asynchronously (can take a while with large logs)
		SwingWorker worker = new SwingWorker() {
			protected String htmlSummary = null;

			public Object construct() {
				Thread.yield();
				htmlSummary = summaryPane.getText();
				summaryPane
						.setText("<html><body bgcolor=\"#888888\" text=\"#333333\">"
								+ "<br><br><br><br><br><center><font face=\"helvetica,arial,sans-serif\" size=\"4\">"
								+ "Please wait while the summary is created...</font></center></body></html>");
				repaint();
				Thread.yield();
				LogReader nLog = parent.getLog();
				if (log == null || log.equals(nLog) == false) {
					// recreate log summary text
					log = nLog;
					htmlSummary = log.getLogSummary().toString();
				}
				return null;
			}

			public void finished() {
				if (htmlSummary != null) {
					summaryPane.setText(htmlSummary);
					summaryPane.setCaretPosition(0);
				}
				repaint();
			}

		};
		worker.start();
	}

	public ActionListener getActivationListener() {
		ActionListener activationListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkRecompileSummary();
			}
		};
		return activationListener;
	}

}
