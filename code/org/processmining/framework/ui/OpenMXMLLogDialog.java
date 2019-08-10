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
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.ui.actions.MxmlFilenameFilter;
import org.processmining.framework.ui.filters.GenericMultipleExtFilter;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class OpenMXMLLogDialog extends JDialog {

	// as a serializable class we need a serial version uid
	private static final long serialVersionUID = -8385166814255792636L;

	private boolean ok;
	private String lastOpenedLog = "";

	private JPanel panel1 = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JLabel filenameLabel = new JLabel();
	private JTextField workflowLogFile = new JTextField();
	private JButton chooseLogButton = new JButton();
	private JPanel settingsPanel = new JPanel();
	private JPanel zipContents = new JPanel(new BorderLayout());
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private JPanel buttonsPanel = new JPanel();
	private JButton openButton = new JButton();
	private JButton cancelButton = new JButton();
	private JList zipEntries;
	protected FileDialog fileDialog;

	public OpenMXMLLogDialog(String lastOpenedLog) {
		super(MainUI.getInstance(), "Open workflow log", true);

		if (lastOpenedLog.startsWith("zip://")) {
			lastOpenedLog = lastOpenedLog.substring("zip://".length(),
					lastOpenedLog.indexOf('#'));
		}
		this.lastOpenedLog = lastOpenedLog;
		try {
			jbInit();
			centerDialog();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean showDialog(boolean fileSet) {
		if (!fileSet) {
			chooseLogButton_actionPerformed(null);
		}
		// check if a zip file was chosen
		if (!zipContents.isVisible()) {
			// no zip file, so we have a xml log file
			if (!workflowLogFile.getText().equals("")) {
				openButton_actionPerformed(null);
			}
		} else {
			setVisible(true);
		}
		return ok;
	}

	public LogFile getFile() {
		return LogFile.getInstance(workflowLogFile.getText().toLowerCase()
				.endsWith(".zip") ? "zip://" + workflowLogFile.getText() + "#"
				+ zipEntries.getSelectedValue().toString() : workflowLogFile
				.getText());
	}

	public void setSelectedFile(String filename) {
		if (filename.startsWith("zip://")) {
			String zipFile = filename.substring("zip://".length(), filename
					.indexOf('#'));
			String logFile = filename.substring(filename.indexOf('#') + 1);
			setChosenZipFile(zipFile, logFile);
		} else {
			setChosenXMLFile(filename);
		}
	}

	private void setChosenXMLFile(String logFileName) {
		zipContents.setVisible(false);
		workflowLogFile.setText(logFileName);
	}

	public void setChosenZipFile(String zipFileName, String logFileName) {
		try {
			ZipFile zip = new ZipFile(zipFileName);
			Enumeration enu = zip.entries();
			Vector entries = new Vector();
			int logIndex = 0;
			int i = 0;
			while (enu.hasMoreElements()) {
				String entryName = ((ZipEntry) enu.nextElement()).getName();
				if (entryName.toLowerCase().endsWith(".xml")
						|| entryName.toLowerCase().endsWith(".mxml")) {
					entries.add(entryName);
					if (entryName.toLowerCase().startsWith(
							zipFileName.toLowerCase().substring(0,
									zipFileName.length() - ".zip".length()))) {
						logIndex = i;
					}
				}
				i++;
			}
			zipEntries = new JList(entries);
			zipEntries.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			zipContents.removeAll();
			zipContents.add(new JScrollPane(zipEntries), BorderLayout.CENTER);
			if (logIndex != -1) {
				zipEntries.setSelectedIndex(logIndex);
				zipEntries.ensureIndexIsVisible(logIndex);
			}
			if (entries.size() > 1) {
				zipContents.setVisible(true);
				centerDialog();
			} else {
				zipContents.setVisible(false);
			}
			workflowLogFile.setText(zipFileName);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(),
					"Error opening zip file", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void centerDialog() {
		pack();
		CenterOnScreen.center(this);
	}

	private void jbInit() throws Exception {
		panel1.setLayout(borderLayout1);
		filenameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		filenameLabel.setText("Process log file:");
		workflowLogFile.setMinimumSize(new Dimension(150, 21));
		workflowLogFile.setPreferredSize(new Dimension(350, 21));
		workflowLogFile.setEditable(false);
		chooseLogButton.setMaximumSize(new Dimension(20, 25));
		chooseLogButton.setMinimumSize(new Dimension(20, 25));
		chooseLogButton.setPreferredSize(new Dimension(20, 25));
		chooseLogButton.setActionCommand("");
		chooseLogButton.setText("Browse...");
		chooseLogButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseLogButton_actionPerformed(e);
			}
		});
		settingsPanel.setLayout(gridBagLayout2);
		openButton.setText("Open");
		openButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openButton_actionPerformed(e);
			}
		});
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton_actionPerformed(e);
			}
		});

		zipContents.setVisible(false);

		getContentPane().add(panel1);
		settingsPanel.add(filenameLabel, new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(10, 5, 0, 0), 10, 0));
		settingsPanel
				.add(workflowLogFile, new GridBagConstraints(1, 1, 1, 1, 1.0,
						0.0, GridBagConstraints.WEST,
						GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 0),
						-44, 4));
		settingsPanel.add(chooseLogButton, new GridBagConstraints(2, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 10, 0, 10), 67, 0));
		settingsPanel.add(zipContents, new GridBagConstraints(1, 2, 2, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 20), 0, 0));
		panel1.add(buttonsPanel, BorderLayout.SOUTH);
		buttonsPanel.add(openButton, null);
		buttonsPanel.add(cancelButton, null);
		panel1.add(settingsPanel, BorderLayout.CENTER);

	}

	void chooseLogButton_actionPerformed(ActionEvent e) {
		if (RuntimeUtils.isRunningMacOsX()) {
			// The AWT FileDialog class provides a more native user experience
			// on Mac OS X
			if (fileDialog == null) {
				fileDialog = new FileDialog(MainUI.getInstance(),
						"Choose log file...", FileDialog.LOAD);
				fileDialog.setFilenameFilter(new MxmlFilenameFilter());
			}
			fileDialog.setLocationRelativeTo(MainUI.getInstance());
			fileDialog.setVisible(true);
			if (fileDialog.getFile() != null
					&& fileDialog.getFile().length() > 0) {
				String path = fileDialog.getDirectory() + fileDialog.getFile();
				if (path.toLowerCase().endsWith(".zip")) {
					this.setChosenZipFile(path, "");
				} else {
					this.setChosenXMLFile(path);
				}
			} else {
				setChosenXMLFile("");
			}

		} else {
			// use the swing JFileChooser for the rest
			JFileChooser chooser = new JFileChooser(
					workflowLogFile.getText() == null
							|| workflowLogFile.getText().equals("") ? lastOpenedLog
							: workflowLogFile.getText());
			chooser.setFileFilter(new GenericMultipleExtFilter(new String[] {
					"xml", "zip", "mxml", "mxml.gz" },
					"MXML and ZIP files (*.mxml, *mxml.gz, *.xml, *.zip)"));
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				String name = chooser.getSelectedFile().getPath();
				// Specific log might still have to be chosen (in case of zip
				// file)!
				if (name.toLowerCase().endsWith(".zip")) {
					setChosenZipFile(name, "");
				} else {
					setChosenXMLFile(name);
				}
			} else {
				setChosenXMLFile("");
			}
		}
	}

	void openButton_actionPerformed(ActionEvent e) {
		if (workflowLogFile.getText() == null
				|| workflowLogFile.getText().equals("")) {
			JOptionPane.showMessageDialog(MainUI.getInstance(),
					"Please choose a log file first.");
		} else if (workflowLogFile.getText().toLowerCase().endsWith(".zip")
				&& zipEntries.getSelectedIndex() < 0) {
			JOptionPane.showMessageDialog(MainUI.getInstance(),
					"Please choose a log file in the zip file.");
		} else {
			ok = true;
			setVisible(false);
		}
	}

	void cancelButton_actionPerformed(ActionEvent e) {
		ok = false;
		setVisible(false);
	}
}
