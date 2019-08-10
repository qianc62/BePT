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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.ui.UISettings;

import att.grappa.Graph;
import att.grappa.Parser;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class Dot {

	public static String getDotPath() {
		String customDot = UISettings.getInstance().getCustomDotLocation();
		if (customDot != null) {
			return customDot;
			// On Windows systems, use the dot.exe distributed with ProM by
			// default
		} else if (RuntimeUtils.isRunningWindows()) {
			return "E:\\dot\\dot.exe";
			//return DOTDialog.getWebAppRoot() + System.getProperty("file.separator") + "/dot" + System.getProperty("file.separator") + "dot.exe";
			// On Mac OS X, use the dot executable distributed within
			// Graphviz.app (from Pixelglow)
			// by default (assumes standard installation into system-wide
			// /Applications folder)
		} else if (RuntimeUtils.isRunningMacOsX()) {
			//return "/Applications/Graphviz.app/Contents/MacOS/dot";
			return "/usr/local/bin/dot";
		} else {
			// assume UNIX-like OS with dot executable in $PATH
			return "dot";
		}
	}

	private Dot() {
	}

	/**
	 * @deprecated The deleteFileAfterwards parameter is not taken into
	 *             consideration anymore (please use {@link execute #execute
	 *             execute} instead).
	 * @param dotFilename
	 *            String
	 * @param deleteFileAfterwards
	 *            not used
	 * @return Graph
	 * @throws Exception
	 */
	public static Graph execute(String dotFilename, boolean deleteFileAfterwards)
			throws Exception {
		return execute(dotFilename);
	}

	public static Graph execute(String dotFilename) throws Exception {

		DOTDialog dialog = new DOTDialog(dotFilename);
		//dialog.showDialog();

		//Graph ret = dialog.getResult();
		Graph ret = dialog.runDot();
		dialog.dispose();
		// System.out.println("in execute");

		return ret;
	}
}

class DOTDialog extends JDialog {

	private Graph result = null;
	private Process dot;
	private String dotFilename;
	private SwingWorker worker;

	public DOTDialog(String dotFilename) throws Exception {
		super(MainUI.getInstance(), "Running dot", true);
		this.dotFilename = dotFilename;

		JPanel main = new JPanel(new BorderLayout());
		getContentPane().add(main);

		JPanel p = new JPanel();
		p.add(new JLabel("<html><center>Click \"cancel\" to abort <br> the execution of DOT</center></html>"));

		main.add(p, BorderLayout.CENTER);
		JButton button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				dot.destroy();
				result = null;
				// setVisible(false);
				dispose();
			}
		});
		JPanel cancelPanel = new JPanel(new FlowLayout());
		cancelPanel.add(button);

		main.add(cancelPanel, BorderLayout.SOUTH);

		worker = new SwingWorker() {
			public Object construct() {
				try {
					// sleep(20);
					return runDot();
				} catch (Exception ex) {
					Message.add("Error while performing graph layout: "
							+ ex.getMessage(), Message.ERROR);
				}
				return null;
			}

			public void finished() {
				if (get() != null) {
					result = (Graph) get();
				}
				// setVisible(false);
				// System.out.println("in worker finished");
				dispose();
			}
		};

		pack();

		setSize(Math.min(700, getSize().width) + 65, Math.min(500,
				getSize().height));		
		CenterOnScreen.center(this);
		repaint();
	}

	public Graph runDot() throws Exception {
		File dotFile = new File(dotFilename);
		Parser parser;
		Graph graph;

		Message.add("Starting DOT on: " + dotFile.getAbsolutePath(),
				Message.DEBUG);

		// use custom user-defined dot executable, if specified
		/*
		 * old implementation: lots of checks.. File customDotExe =
		 * UISettings.getInstance().getCustomDotExecutable(); if(customDotExe !=
		 * null && customDotExe.exists() &&
		 * (customDotExe.getName().equalsIgnoreCase("dot") ||
		 * customDotExe.getName().equalsIgnoreCase("dot.exe"))) { String
		 * dotCommandString = customDotExe.getAbsolutePath() + " -q 5 ";
		 */
		String customDot = UISettings.getInstance().getCustomDotLocation();
		if (customDot != null) {
			String dotCommandString = customDot + " -q 5 ";
			if (RuntimeUtils.isRunningWindows()) {
				// windows dot needs quotes around input the file parameter
				dotCommandString += "\"" + dotFile.getAbsolutePath() + "\"";
			} else {
				// unix doesn't
				dotCommandString += dotFile.getAbsolutePath();
			}
			Message.add("Using custom dot executable at: " + customDot,
					Message.DEBUG);
			dot = Runtime.getRuntime().exec(dotCommandString);
			// On Windows systems, use the dot.exe distributed with ProM by
			// default
		}
//		} else if (RuntimeUtils.isRunningWindows()) {
//			dot = Runtime.getRuntime().exec(
//					"dot" + System.getProperty("file.separator")
//							+ "dot.exe -q5 \"" + dotFile.getAbsolutePath()
//							+ "\"");
//			// On Mac OS X, use the dot executable distributed within
//			// Graphviz.app (from Pixelglow)
//			// by default (assumes standard installation into system-wide
//			// /Applications folder)
//		} 
		 else if (RuntimeUtils.isRunningWindows()) {
			dot = Runtime.getRuntime().exec(
					DOTDialog.getWebAppRoot() + System.getProperty("file.separator") + "dot" + System.getProperty("file.separator")
							+ "dot.exe -q5 \"" + dotFile.getAbsolutePath()
							+ "\"");
			// On Mac OS X, use the dot executable distributed within
			// Graphviz.app (from Pixelglow)
			// by default (assumes standard installation into system-wide
			// /Applications folder)
		} else if (RuntimeUtils.isRunningMacOsX()) {
			dot = Runtime.getRuntime().exec(
					"/usr/local/bin/dot -q5 "
							+ dotFile.getAbsolutePath());
		} else {
			// assume UNIX-like OS with dot executable in $PATH
			dot = Runtime.getRuntime().exec(
					"dot -q5 " + dotFile.getAbsolutePath());
		}

		parser = new Parser(dot.getInputStream(), System.err);
		parser.parse();
		graph = parser.getGraph();
		parser.done_parsing();
		parser = null;

		dot.destroy();
		dot = null;
		Message.add("DOT finished on: " + dotFile.getAbsolutePath(),
				Message.DEBUG);

		return graph;

	}

	public void showDialog() {
		worker.start();	
		super.setVisible(true);		
		//super.setOpacity(0);
	}

	public Graph getResult() {
		return result;
	}
	
	// get webapp root
	public static String getWebAppRoot() {
		Map<String, String> envMap = System.getenv();
		if(envMap.containsKey("PROCESSPROFILE")) {
			String processProfile = envMap.get("PROCESSPROFILE");
			if(!processProfile.endsWith(File.separator)) {
				processProfile += File.separator;
			}
			return processProfile;
		}
		return "";
	}
}
