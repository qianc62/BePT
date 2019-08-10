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

package org.processmining.converting;

import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.MainUI;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import javax.swing.JPanel;
import java.io.StringWriter;
import java.awt.TextArea;
import org.processmining.framework.util.CenterOnScreen;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.io.InputStream;
import javax.swing.JDialog;
import java.io.IOException;
import org.processmining.framework.ui.Message;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import org.processmining.exporting.petrinet.PnmlExport;
import org.processmining.importing.pnml.PnmlImport;

/**
 * <p>
 * Title: Yasper
 * </p>
 * 
 * <p>
 * Description: Enables a user to edit a Petri net using Yasper.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class Yasper implements ConvertingPlugin {

	// Available options
	private String options = "";

	public Yasper() {
	}

	public String getName() {
		return "Yasper";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:yasper";
	}

	/**
	 * Enbles the use rto edit a Petri net using Yasper. The edited Petri net
	 * will be loaded as a new Petri net.
	 * 
	 * @param object
	 *            ProvidedObject Should contain a Petri net.
	 * @return PetriNetResult The result of edit session using Yasper.
	 */
	public PetriNetResult convert(ProvidedObject object) {
		PetriNet provided = null;
		LogReader log = null;

		for (int i = 0; provided == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				provided = (PetriNet) object.getObjects()[i];
			}
			if (object.getObjects()[i] instanceof LogReader) {
				log = (LogReader) object.getObjects()[i];
			}
		}

		if (provided == null) {
			return null;
		}

		return result(provided);
	}

	/**
	 * Checks whether a Petri net is provided.
	 * 
	 * @param object
	 *            ProvidedObject The provided objects.
	 * @return boolean Whether a Petri net is among the provided objects.
	 */
	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts one Petri net into another using Yasper.
	 * 
	 * @param pn
	 *            PetriNet The Petri net to edit using Yasper
	 * @return PetriNetResult The edited Petri net (a copy, not a replacement)
	 */
	public PetriNetResult result(PetriNet pn) {
		Object[] objects = new Object[] { pn };
		ProvidedObject object = new ProvidedObject("temp", objects);
		PnmlExport exportPlugin = new PnmlExport();
		PnmlImport importPlugin = new PnmlImport();
		PetriNetResult target = null;

		// Show the options dialog
		YasperUI ui = new YasperUI(this);
		ui.setVisible(true);

		try {
			// Export the transition system to some file.
			File exportFile = File.createTempFile("pmt", ".pnml");
			exportFile.deleteOnExit();
			FileOutputStream outputStream = new FileOutputStream(exportFile);
			exportPlugin.export(object, outputStream);

			// Construct path and command line.
			String cmd = null, path = null;
			if (System.getProperty("os.name", "").toLowerCase().startsWith(
					"windows")) {
				path = System.getProperty("user.dir")
						+ System.getProperty("file.separator") + "lib"
						+ System.getProperty("file.separator") + "plugins"
						+ System.getProperty("file.separator") + "Yasper"
						+ System.getProperty("file.separator");
				cmd = path + "yasper " + options + "\""
						+ exportFile.getCanonicalPath() + "\"";

				// Load libraries required by Yasper.
				System.load(path + "PNML.dll");
				System.load(path + "PNMLUtilities.dll");
				System.load(path + "PNMLTransformation.dll");
				System.load(path + "PNMLImaging.dll");
				System.load(path + "PNMLSimulationReporter.dll");
				System.load(path + "BPMN2PNML.dll");
				System.load(path + "PNML2VDX.dll");
			}
			if (cmd != null) {
				// Run petrify.
				Process process = Runtime.getRuntime().exec(cmd);

				StreamReader errorReader = new StreamReader(process
						.getErrorStream());
				errorReader.start();
				StreamReader inputReader = new StreamReader(process
						.getInputStream());
				inputReader.start();

				Message.add("<yasper options=\"" + options + "\"/>",
						Message.TEST);

				process.waitFor();

				// Results of the edit session should be saved in the file that
				// was opened in Yasper.
				FileInputStream inputStream = new FileInputStream(exportFile);
				target = (PetriNetResult) importPlugin.importFile(inputStream);

				// (For sake of convenience, we copy stderr to ERROR, and stdout
				// to WARNING)
				Message.add(errorReader.getResult(), Message.ERROR);
				Message.add(inputReader.getResult(), Message.WARNING);

			} else {
				Message.add("Unable to execute Yasper on this platform: "
						+ System.getProperty("os.name", ""), Message.DEBUG);
				target = new PetriNetResult(new PetriNet());
			}
		} catch (Exception e) {
			Message.add("Unable to petrify transition system: " + e.toString(),
					Message.DEBUG);
		}
		return target;
	}

	/*
	 * Options, none available for the time being public void setOption(boolean
	 * result) { if (result) { options += "-option "; } }
	 */

	/**
	 * 
	 * <p>
	 * Title: StreamReader
	 * </p>
	 * 
	 * <p>
	 * Description: Reads a stream using a separate thread. Used to read
	 * petrify's stdout and stderr, as petrify might block on writing these
	 * streams while we're weaiting for it to complete.
	 * </p>
	 */
	static class StreamReader extends Thread {
		private InputStream is;
		private StringWriter sw;

		StreamReader(InputStream is) {
			this.is = is;
			sw = new StringWriter();
		}

		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1)
					sw.write(c);
			} catch (IOException e) {
				;
			}
		}

		String getResult() {
			return sw.toString();
		}
	}
}

/**
 * 
 * <p>
 * Title: YasperUI
 * </p>
 * 
 * <p>
 * Description: Dialog to obtain the available options.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
class YasperUI extends JDialog implements ActionListener {
	Yasper yasper;
	Checkbox someCheckbox;
	JButton doneButton;

	public YasperUI(Yasper yasper) {
		super(MainUI.getInstance(), "Select options...", true);
		this.yasper = yasper;

		try {
			setUndecorated(false);
			jbInit();
			pack();
			CenterOnScreen.center(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void jbInit() throws Exception {
		JPanel panel = new JPanel();
		JPanel aboutPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		panel.setLayout(layout);
		/*
		 * someCheckbox = new Checkbox("Description for option");
		 * constraints.anchor = GridBagConstraints.WEST; constraints.gridx = 0;
		 * constraints.gridy = 0; layout.setConstraints(someCheckbox,
		 * constraints); panel.add(someCheckbox);
		 */
		doneButton = new JButton("Done");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		constraints.gridy = 7;
		layout.setConstraints(doneButton, constraints);
		panel.add(doneButton);

		doneButton.addActionListener(this);

		String about = "This plug-in uses Yasper. Yasper is a tool by ASPT.\n";

		TextArea text = new TextArea(about, 9, 50, TextArea.SCROLLBARS_NONE);
		text.setEditable(false);
		aboutPanel.add(text);
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		this.add(aboutPanel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == doneButton) {
			/* petrify.setOption(someCheckbox.getState()); */
			dispose();
		}
	}
}
