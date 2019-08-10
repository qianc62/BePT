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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.processmining.exporting.fsm.FSMPetrifyExport;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.fsm.AcceptFSM;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.importing.petrify.Petrify2PetriNet;
import org.processmining.mining.petrinetmining.PetriNetResult;
import java.util.HashMap;
import org.processmining.framework.models.fsm.FSMState;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.fsm.FSMTransition;
import java.util.ArrayList;
import java.awt.Insets;
import javax.swing.JLabel;
import java.util.TreeSet;
import java.util.List;
import org.processmining.framework.models.transitionsystem.PetrifyConstants;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogStateMachine;
import javax.swing.JCheckBox;

/**
 * <p>
 * Title: Petrify
 * </p>
 * 
 * <p>
 * Description: Converts a state space to a Petri net using the petrify tool.
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
public class Petrify implements ConvertingPlugin {

	// Available options
	private String options = "";

	public Petrify() {
	}

	public String getName() {
		return "Petrify";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:petrify";
	}

	/**
	 * Converts a transition system into a Petri net using petrify.
	 * 
	 * @param object
	 *            ProvidedObject Should contain a transition system.
	 * @return PetriNetResult The result of converting the provided object using
	 *         petrify.
	 */
	public PetriNetResult convert(ProvidedObject object) {
		AcceptFSM provided = null;
		LogReader log = null;

		for (int i = 0; provided == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof AcceptFSM) {
				provided = (AcceptFSM) object.getObjects()[i];
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
	 * Checks whether a transition system is provided.
	 * 
	 * @param object
	 *            ProvidedObject The provided pbjects.
	 * @return boolean Whether a transition system is among the provided
	 *         objects.
	 */
	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof AcceptFSM) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts a transition system into a Petri net using petrify.
	 * 
	 * @param ts
	 *            TransitionSystem The transition system
	 * @return PetriNetResult The converted transition system
	 */
	public PetriNetResult result(AcceptFSM fsm) {
		FSMPetrifyExport exportPlugin = new FSMPetrifyExport();
		Petrify2PetriNet importPlugin = new Petrify2PetriNet();
		PetriNetResult target = null;

		// Show the options dialog
		final PetrifyUI ui = new PetrifyUI(this);
		ui.setVisible(true);

		// Build a set of all possible labels (conditions)
		TreeSet<String> conditions = new TreeSet<String>();
		for (FSMTransition t : (ArrayList<FSMTransition>) fsm.getEdges()) {
			conditions.add(t.getCondition());
		}
		// put the labels in a list for identification
		List<String> condList = new ArrayList<String>(conditions);

		// build a map from the indices in the list to the original labels
		HashMap<String, String> id2Condition = new HashMap<String, String>();
		for (FSMTransition t : (ArrayList<FSMTransition>) fsm.getEdges()) {
			id2Condition.put("trans" + condList.indexOf(t.getCondition()), t
					.getCondition());
			t.setCondition("trans" + condList.indexOf(t.getCondition()));
		}

		try {
			// Export the transition system to some file.
			File exportFile = File.createTempFile("pmt", ".g");
			exportFile.deleteOnExit();
			FileOutputStream stream = new FileOutputStream(exportFile);
			exportPlugin.FSMPetrifyExport(fsm, stream);

			// Construct path and command line.
			String cmd = null, path = null;
			if (System.getProperty("os.name", "").toLowerCase().startsWith(
					"windows")) {
				// Run petrify on a Windows machine
				path = System.getProperty("user.dir")
						+ System.getProperty("file.separator") + "lib"
						+ System.getProperty("file.separator") + "plugins"
						+ System.getProperty("file.separator") + "Petrify"
						+ System.getProperty("file.separator");
				cmd = path + "petrify4.1 -d2 -dead -ip " + options + "\""
						+ exportFile.getCanonicalPath() + "\"";

				// Load libraries required by petrify.
				System.load(path + "cygwin1.dll");
				System.load(path + "petrify.dll");
			} else if (System.getProperty("os.name", "").toLowerCase()
					.startsWith("linux")) {
				// Run petrify on a Linux machine.
				path = System.getProperty("user.dir")
						+ System.getProperty("file.separator") + "lib"
						+ System.getProperty("file.separator") + "plugins"
						+ System.getProperty("file.separator") + "Petrify"
						+ System.getProperty("file.separator");
				cmd = path + "bin" + System.getProperty("file.separator")
						+ "petrify -d2 -dead -ip " + options
						+ exportFile.getCanonicalPath();
				/*
				 * Do not use double quotes around the canonical path on linux,
				 * as these quotes will end up as part of the path (they will
				 * *not* be removed!).
				 */

				// Load libraries required by petrify.
				// System.load(path + "lib" +
				// System.getProperty("file.separator") +
				// "petrify.lib");
			}
			if (cmd != null) {
				// Run petrify.
				Process process = Runtime.getRuntime().exec(cmd);

				StreamReader errorReader = new StreamReader(process
						.getErrorStream());
				errorReader.start();
				Message.add("<petrify options=\"" + options + "\"/>",
						Message.TEST);
				target = (PetriNetResult) importPlugin.importFile(process
						.getInputStream());
				// (For sake of convenience, we copy stderr to ERROR)
				String err = errorReader.getResult();
				if ((err != null) && (err.length() > 0)) {
					Message.add(err, Message.ERROR);
				}
			} else {
				Message.add("Unable to execute petrify on this platform: "
						+ System.getProperty("os.name", ""), Message.DEBUG);
				target = new PetriNetResult(new PetriNet());
			}
		} catch (Exception e) {
			Message.add("Unable to petrify transition system: " + e.toString(),
					Message.DEBUG);
		}
		if (target != null) {
			PetriNet pn = target.getPetriNet();
			pn.Test("Petrify");
			for (Transition t : pn.getTransitions()) {
				// map the labels of the transitions to the orignal labels.
				String orgId = t.getIdentifier();
				int index = orgId
						.lastIndexOf(PetrifyConstants.EDGEDOCSSEPARATOR);
				if (index >= 0) {
					orgId = t.getIdentifier().substring(0, index);
				}
				t.setIdentifier(id2Condition.get(orgId));
				t.setLogEvent(new LogEvent(t.getIdentifier(),
						LogStateMachine.UNKNOWN));
			}
		}
		// replace the new labels with the originals again
		for (FSMTransition t : (ArrayList<FSMTransition>) fsm.getEdges()) {
			t.setCondition(id2Condition.get(t.getCondition()));
		}
		return target;
	}

	/*
	 * Options
	 */
	public void setAll(boolean result) {
		if (result) {
			options += "-all ";
		}
	}

	public void setSat(boolean result) {
		if (result) {
			options += "-sat ";
		}
	}

	public void setMin(boolean result) {
		if (result) {
			options += "-min ";
		}
	}

	public void setOpt(boolean result) {
		if (result) {
			options += "-opt ";
		}
	}

	public void setPure(boolean result) {
		if (result) {
			options += "-p ";
		}
	}

	public void setFC(boolean result) {
		if (result) {
			options += "-fc ";
		}
	}

	public void setER(boolean result) {
		if (result) {
			options += "-er ";
		}
	}

	public void setSM(boolean result) {
		if (result) {
			options += "-sm ";
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
				while ((c = is.read()) != -1) {
					sw.write(c);
					System.err.write(c);
				}
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
 * Title: PetrifyUI
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
class PetrifyUI extends JDialog implements ActionListener {
	Petrify petrify;
	JCheckBox allCheckbox;
	JCheckBox satCheckbox;
	JCheckBox minCheckbox;
	JCheckBox optCheckbox;
	JCheckBox pureCheckbox;
	JCheckBox fcCheckbox;
	JCheckBox erCheckbox;
	JCheckBox smCheckbox;
	JButton doneButton;

	private final static String about = "<html><center>This plug-in uses Petrify. Petrify is a tool by:<br>"
			+ "Jordi Cortadella, Michael Kishinevsky, Alex Kondratyev,<br>"
			+ "Luciano Lavagno, Enric Pastor and Alexandre Yakovlev.<br>"
			+ "<br>"
			+ "On the Windows platform, this plug-in relies on the cygwin1.dll. <br>"
			+ "This DLL may crash the Java VM if it cannot allocate sufficient memory.<br>"
			+ "If this happens, try to reduce the memory used by the Java VM (-Xmx option).</center></html>";

	public PetrifyUI(Petrify petrify) {
		super(MainUI.getInstance(), "Select options...", true);
		this.petrify = petrify;

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
		int i = 0;
		getContentPane().setLayout(new GridBagLayout());

		allCheckbox = new JCheckBox("Search for all minimal regions");
		getContentPane().add(
				allCheckbox,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		satCheckbox = new JCheckBox("Generate a minimal satured Petri net");
		getContentPane().add(
				satCheckbox,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		minCheckbox = new JCheckBox(
				"Generate a Petri net with minimal regions only (no place merging)");
		getContentPane().add(
				minCheckbox,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		optCheckbox = new JCheckBox(
				"Find the best result (may override other options)");
		getContentPane().add(
				optCheckbox,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		pureCheckbox = new JCheckBox("Generate a pure Petri net");
		getContentPane().add(
				pureCheckbox,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		fcCheckbox = new JCheckBox("Generate a Free-Choice net");
		getContentPane().add(
				fcCheckbox,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		erCheckbox = new JCheckBox(
				"Generate a different label for each excitation region");
		getContentPane().add(
				erCheckbox,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		smCheckbox = new JCheckBox("Generate an SM-decomposable net");
		getContentPane().add(
				smCheckbox,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		doneButton = new JButton("Done");
		getContentPane().add(
				doneButton,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		doneButton.addActionListener(this);
		doneButton.requestFocusInWindow();

		getContentPane().add(
				new JLabel(about),
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == doneButton) {
			petrify.setAll(allCheckbox.isSelected());
			petrify.setSat(satCheckbox.isSelected());
			petrify.setMin(minCheckbox.isSelected());
			petrify.setOpt(optCheckbox.isSelected());
			petrify.setPure(pureCheckbox.isSelected());
			petrify.setFC(fcCheckbox.isSelected());
			petrify.setER(erCheckbox.isSelected());
			petrify.setSM(smCheckbox.isSelected());
			dispose();
		}
	}
}
