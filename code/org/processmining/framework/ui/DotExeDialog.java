/**
 * Project: ProM Framework
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Oct 29, 2006 4:41:43 AM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.deckfour.gantzgraf.util.GGDotPathFinder;
import org.processmining.framework.util.RuntimeUtils;

/**
 * This dialog allows the user to specify a custom dot executable to be used for
 * graph layouting in ProM.
 * 
 * TODO: clean up code and document properly! TODO: add options to specify
 * custom dot command line parameters!
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class DotExeDialog extends JDialog {

	protected static DotExeDialog instance = null;

	public synchronized static DotExeDialog instance() {
		if (DotExeDialog.instance == null) {
			DotExeDialog.instance = new DotExeDialog(null);
		}
		return DotExeDialog.instance;
	}

	protected File dotLocation;
	protected JTextField locationField;
	protected JButton browseButton;

	protected DotExeDialog(File dotFile) {
		if (dotFile != null) {
			dotLocation = dotFile;
		} else {
			dotLocation = UISettings.getInstance().getCustomDotExecutable();
		}
		if (dotLocation != null
				&& (dotLocation.getName().equalsIgnoreCase("dot") || dotLocation
						.getName().equalsIgnoreCase("dot.exe")) == false) {
			dotLocation = null;
		}
		this.setTitle("Choose location of dot executable...");
		this.setModal(true);
		this.setSize(650, 270);
		this.setLocationRelativeTo(MainUI.getInstance());
		this.setLayout(new BorderLayout());
		JPanel locationPanel = new JPanel();
		locationPanel.setLayout(new BoxLayout(locationPanel, BoxLayout.X_AXIS));
		locationPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		locationField = new JTextField(60);
		locationField.setMaximumSize(new Dimension(1024, 30));
		locationField.setAlignmentY(TOP_ALIGNMENT);
		if (dotLocation != null) {
			locationField.setText(dotLocation.getAbsolutePath());
		}
		browseButton = new JButton("browse...");
		browseButton.setAlignmentY(TOP_ALIGNMENT);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileDialog chooser = new FileDialog(DotExeDialog.instance(),
						"Choose dot executable...", FileDialog.LOAD);
				if (dotLocation != null) {
					chooser.setFile(dotLocation.getAbsolutePath());
				} else {
					if (RuntimeUtils.isRunningUnix()
							|| RuntimeUtils.isRunningMacOsX()) {
						chooser.setDirectory("/Applications");
					} else {
						chooser.setDirectory(File.listRoots()[0].getParent());
					}
				}
				chooser.setFilenameFilter(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.equalsIgnoreCase("dot")
								|| name.equalsIgnoreCase("dot.exe")
								|| name.equalsIgnoreCase("Graphviz.app")) {
							return true;
						} else {
							return false;
						}
					}
				});
				chooser.setVisible(true);
				String chosenFile = chooser.getFile();
				if (chosenFile != null) {
					File dot = null;
					if (chosenFile.equalsIgnoreCase("dot")
							|| chosenFile.equalsIgnoreCase("dot.exe")) {
						dot = new File(chooser.getDirectory() + File.separator
								+ chooser.getFile());
					} else if (chosenFile.equalsIgnoreCase("Graphviz.app")) {
						dot = new File(chooser.getDirectory() + File.separator
								+ chooser.getFile() + "/Contents/MacOS/dot");
					}
					if (dot != null && dot.exists()) {
						dotLocation = dot;
						locationField.setText(dot.getAbsolutePath());
					}
				}
			}
		});
		JButton autoButton = new JButton("auto-detect");
		autoButton.setAlignmentY(TOP_ALIGNMENT);
		autoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				autoDetectDot();
			}
		});
		JButton resetButton = new JButton("reset");
		resetButton.setAlignmentY(TOP_ALIGNMENT);
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				locationField.setText("");
			}
		});
		locationPanel.add(locationField);
		locationPanel.add(Box.createHorizontalStrut(15));
		locationPanel.add(browseButton);
		locationPanel.add(Box.createHorizontalStrut(8));
		locationPanel.add(autoButton);
		locationPanel.add(Box.createHorizontalStrut(10));
		locationPanel.add(resetButton);
		JLabel label = new JLabel(
				"<html>Please specify the location of your preferred <b>dot</b> "
						+ "executable. On Unix and Mac OS X systems, this is usually an executable "
						+ "named <b>dot</b>, on Win32 systems it is called <b>dot.exe</b>.<br>"
						+ "On Mac OS X, you can alternatively also select a <b>Graphviz</b> "
						+ "application package from Pixelglow (<code>http://pixelglow.com/graphviz/</code>) "
						+ "for using the included <b>dot</b> executable.<br>"
						+ "You can find Graphviz binaries for all supported platforms at "
						+ "<code>http://www.graphviz.org/</code>.<br>"
						+ "If the input field is left blank, ProM will attempt to auto-detect the "
						+ "default <b>dot</b> executable.</html>");
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
		JPanel okPanel = new JPanel();
		okPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));
		okPanel.setLayout(new BoxLayout(okPanel, BoxLayout.X_AXIS));
		JButton okButton = new JButton("set dot location");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File dotFile = new File(locationField.getText().trim());
				if (locationField.getText().trim().length() == 0
						|| (dotFile.exists() && (dotFile.getName()
								.equalsIgnoreCase("dot") || dotFile.getName()
								.equalsIgnoreCase("dot.exe")))) {
					UISettings.getInstance().setCustomDotLocation(
							locationField.getText().trim());
					DotExeDialog.instance().setVisible(false);
					if (locationField.getText().trim().length() == 0) {
						JOptionPane
								.showMessageDialog(
										MainUI.getInstance(),
										"Dot executable auto-detection has been enabled!",
										"Success",
										JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(MainUI.getInstance(),
								"Dot executable to be used has been set to:\n"
										+ locationField.getText().trim(),
								"Success", JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(DotExeDialog.instance(),
							"Specified location is no dot executable!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		JButton cancelButton = new JButton("cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		okPanel.add(Box.createHorizontalGlue());
		okPanel.add(cancelButton);
		okPanel.add(Box.createHorizontalStrut(10));
		okPanel.add(okButton);
		this.getRootPane().setBorder(
				BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.add(label, BorderLayout.NORTH);
		this.add(locationPanel, BorderLayout.CENTER);
		this.add(okPanel, BorderLayout.SOUTH);
	}

	public void setVisible(boolean visible) {
		if (visible == true) {
			this.setSize(650, 270);
			this.setLocationRelativeTo(MainUI.getInstance());
			dotLocation = UISettings.getInstance().getCustomDotExecutable();
			if (dotLocation != null
					&& (dotLocation.getName().equalsIgnoreCase("dot") || dotLocation
							.getName().equalsIgnoreCase("dot.exe")) == false) {
				dotLocation = null;
				locationField.setText("");
			} else if (dotLocation != null) {
				locationField.setText(dotLocation.getAbsolutePath());
			} else {
				locationField.setText("");
			}
		}
		super.setVisible(visible);
	}

	public void autoDetectDot() {
		String dotLocation = GGDotPathFinder.getDotPath();
		if (dotLocation != null) {
			locationField.setText(dotLocation);
			this.dotLocation = new File(dotLocation);
			JOptionPane.showMessageDialog(this,
					"Successfully auto-detected dot executable at:\n"
							+ dotLocation, "Auto-detection successful!",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this,
					"Could not find dot executable on your system!",
					"Auto-detection failed", JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
