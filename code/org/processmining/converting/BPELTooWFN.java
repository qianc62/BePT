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

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.processmining.exporting.bpel.BPELExport;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.bpel.BPEL;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.oWFNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.importing.owfn.oWFNImport;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title: BPELTooWFN
 * </p>
 * 
 * <p>
 * Description: Converts a BPEL proces sinto an open WF net.
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
public class BPELTooWFN implements ConvertingPlugin {

	// Available options
	private String options = "";

	public BPELTooWFN() {
	}

	public String getName() {
		return "BPEL process to oWF net";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:bepl2ofwn";
	}

	/**
	 * 
	 * @param object
	 *            ProvidedObject Should contain a BPEL object
	 * @return PetriNetResult The BPEL object converted into a PetriNetResult.
	 */
	public PetriNetResult convert(ProvidedObject object) {
		BPEL provided = null;
		LogReader log = null;

		for (int i = 0; provided == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof BPEL) {
				provided = (BPEL) object.getObjects()[i];
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
	 * 
	 * @param object
	 *            ProvidedObject
	 * @return boolean Whether object contains a BPEL object.
	 */
	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof BPEL) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param source
	 *            BPEL The BPEL object
	 * @return PetriNetResult The BPEL object converted into a PetriNetResult.
	 */
	public PetriNetResult result(BPEL source) {
		Object[] objects = new Object[] { source };
		ProvidedObject object = new ProvidedObject("temp", objects);
		BPELExport exportPlugin = new BPELExport();
		oWFNImport importPlugin = new oWFNImport();
		PetriNetResult target = null;

		// First, we get the available options.
		BPELTooWFNetUI ui = new BPELTooWFNetUI(this);
		ui.setVisible(true);

		try {
			// Second, we export the BPEL object to a temporary file.
			File bpelFile = File.createTempFile("pmt", ".bpel");
			bpelFile.deleteOnExit();
			FileOutputStream stream = new FileOutputStream(bpelFile);
			exportPlugin.export(object, stream);

			// Third, we construct the command line.
			String cmd = null;
			if (System.getProperty("os.name", "").toLowerCase().startsWith(
					"windows")) {
				cmd = "lib" + System.getProperty("file.separator") + "plugins"
						+ System.getProperty("file.separator")
						+ "bpel2owfn -i \"" + bpelFile.getCanonicalPath()
						+ "\" -f owfn -m petrinet " + options;
			}
			if (cmd != null) {
				// Fourth, we execute the command line ...
				Process process = Runtime.getRuntime().exec(cmd);

				StreamReader errorReader = new StreamReader(process
						.getErrorStream());
				errorReader.start();
				Message.add("<BPEL2oWFN options=\"" + options + "\"/>",
						Message.TEST);
				target = importPlugin.importFile(process.getInputStream());
				// (For sake of convenience, we copy stderr to ERROR)
				Message.add(errorReader.getResult(), Message.ERROR);

			} else {
				Message.add("Unable to execute bpel2owfn on this platform: "
						+ System.getProperty("os.name", ""), Message.DEBUG);
				target = new PetriNetResult(new oWFNet());
			}
		} catch (Exception e) {
			Message.add("Unable to convert BPEL process to oWFN Net: "
					+ e.toString(), Message.DEBUG);
		}
		return target;
	}

	/**
	 * 
	 * @param result
	 *            boolean Whether or not to restrict the resulting net to
	 *            communication only.
	 */
	public void setCommunicationOnly(boolean result) {
		if (result) {
			// Tell bpel2owfn to restrict to communication only.
			// Option changed in 2.0.3. options += "-p communicationonly ";
			options += "-p small ";
		}
	}

	/**
	 * 
	 * @param result
	 *            boolean Whether or not to simplify the resuting oWF net.
	 */
	public void setSimplify(boolean result) {
		if (result) {
			// Tell bpel2owfn to simplify the resulting net.
			options += "-p reduce ";
		}
	}

	/**
	 * 
	 * <p>
	 * Title: StreamReader
	 * </p>
	 * 
	 * <p>
	 * Description: Reads a stream using a separate thread. Used to read
	 * BPEL2PNML's stdout and stderr, as BPEL2PNML might block on writing these
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
 * Title: BPELTooWFNetUI
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
class BPELTooWFNetUI extends JDialog implements ActionListener {
	BPELTooWFN bpeltoowfnet;
	HashSet<Place> sourcePlaces;
	HashSet<Place> sinkPlaces;
	Checkbox commCheckbox;
	Checkbox simpCheckbox;
	JButton doneButton;

	public BPELTooWFNetUI(BPELTooWFN bpeltoowfnet) {
		super(MainUI.getInstance(), "Select options...", true);
		this.bpeltoowfnet = bpeltoowfnet;
		this.sourcePlaces = sourcePlaces;
		this.sinkPlaces = sinkPlaces;

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

		// Option 1: communication only
		commCheckbox = new Checkbox("Communication only");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		layout.setConstraints(commCheckbox, constraints);
		panel.add(commCheckbox);

		// Option 2: simplify the oWF net.
		simpCheckbox = new Checkbox("Simplify");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 1;
		layout.setConstraints(simpCheckbox, constraints);
		panel.add(simpCheckbox);

		doneButton = new JButton("Done");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		constraints.gridy = 2;
		layout.setConstraints(doneButton, constraints);
		panel.add(doneButton);

		doneButton.addActionListener(this);

		String aboutBPEL2oWFN = "This plug-in uses GNU BPEL2oWFN 2.0.3. ";
		aboutBPEL2oWFN += "BPEL2oWFN 2.0.3 was written by Niels Lohmann, Christian Gierds and Martin Znamirowski.\n";
		aboutBPEL2oWFN += "See www.informatik.hu-berlin.de/top/tools4bpel/bpel2owfn\nfor more information.\n\n";

		aboutBPEL2oWFN += "Copyright (C) 2006, 2007 Niels Lohmann, Christian Gierds and Martin Znamirowski Copyright (C) 2005 Niels Lohmann and Christian Gierds.\n\n";

		aboutBPEL2oWFN += "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/ gpl.html> GNU BPEL2oWFN is free software: you are free to change and redistribute it.";
		aboutBPEL2oWFN += "There is NO WARRANTY, to the extent permitted by law.\n\n";

		aboutBPEL2oWFN += "See http://www.gnu.org/software/bpel2owfn for more information.\n";

		TextArea text = new TextArea(aboutBPEL2oWFN, 9, 50,
				TextArea.SCROLLBARS_NONE);
		text.setEditable(false);
		aboutPanel.add(text);
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		this.add(aboutPanel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == doneButton) {
			bpeltoowfnet.setCommunicationOnly(commCheckbox.getState());
			bpeltoowfnet.setSimplify(simpCheckbox.getState());
			dispose();
		}
	}
}
