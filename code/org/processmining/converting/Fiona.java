package org.processmining.converting;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.models.petrinet.oWFNet;
import org.processmining.mining.dot.DotResult;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import java.awt.GridBagConstraints;
import org.processmining.framework.ui.MainUI;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import org.processmining.framework.util.CenterOnScreen;
import java.awt.TextArea;
import javax.swing.JButton;
import org.processmining.framework.ui.Message;
import java.io.IOException;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import org.processmining.exporting.petrinet.oWFNExport;
import org.processmining.importing.dot.DotImport;
import org.processmining.framework.models.dot.DotModel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JLabel;

/**
 * <p>
 * Title: Fiona conversion plug-in
 * </p>
 * 
 * <p>
 * Description: Calls Fiona to have either an interaction graph or an operating
 * guideline constructed.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class Fiona implements ConvertingPlugin {

	// Standard option: do not call dot to create a PNG file.
	private String options = "-p no-png";

	private String extension = "";

	/**
	 * Create the Fiona conversion plug-in.
	 */
	public Fiona() {
	}

	/**
	 * Menu-item.
	 * 
	 * @return String
	 */
	public String getName() {
		return "Fiona";
	}

	/**
	 * Help page.
	 * 
	 * @return String
	 */
	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:fiona";
	}

	/**
	 * Convert the given oWF net to either an intraction graph or an operating
	 * guideline.
	 * 
	 * @param object
	 *            ProvidedObject
	 * @return DotResult Either the interaction graph or an operating guideline
	 *         (dot files).
	 */
	public DotResult convert(ProvidedObject object) {
		oWFNet provided = null;
		LogReader log = null;

		for (int i = 0; provided == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof oWFNet) {
				provided = (oWFNet) object.getObjects()[i];
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
	 * Accepts an oWF net.
	 * 
	 * @param object
	 *            ProvidedObject
	 * @return boolean
	 */
	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof oWFNet) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert the given oWF net to either an interaction graph or an operating
	 * guideline, using Fiona.
	 * 
	 * @param source
	 *            oWFNet
	 * @return DotResult
	 */
	public DotResult result(oWFNet source) {
		Object[] objects = new Object[] { source };
		ProvidedObject object = new ProvidedObject("temp", objects);
		oWFNExport exportPlugin = new oWFNExport();
		DotImport importPlugin = new DotImport();
		DotResult target = null;

		// Query for the options to use.
		FionaUI ui = new FionaUI(this);
		ui.setVisible(true);

		try {
			// Export the oWF net.
			File owfnFile = File.createTempFile("pmt", ".owfn");
			owfnFile.deleteOnExit();
			FileOutputStream stream = new FileOutputStream(owfnFile);
			exportPlugin.export(object, stream);

			// Construct the command line.
			String cmd = null;
			if (System.getProperty("os.name", "").toLowerCase().startsWith(
					"windows")) {
				cmd = "lib" + System.getProperty("file.separator") + "plugins"
						+ System.getProperty("file.separator") + "Fiona"
						+ System.getProperty("file.separator") + "fiona.exe "
						+ options + " \"" + owfnFile.getCanonicalPath() + "\"";
			}
			Message.add(cmd, Message.DEBUG);
			if (cmd != null) {
				// Run Fiona.
				Process process = Runtime.getRuntime().exec(cmd);

				// Fiona writes the output to file. Read both its output and
				// error streams.
				StreamReader errorReader = new StreamReader(process
						.getErrorStream());
				errorReader.start();
				StreamReader inputReader = new StreamReader(process
						.getInputStream());
				inputReader.start();
				Message.add("<Fiona options=\"" + options + "\"/>",
						Message.TEST);

				// Wait for Fiona to finish.
				process.waitFor();

				// Import the results.
				String dot = owfnFile.getCanonicalPath() + extension;
				FileInputStream inputStream = new FileInputStream(dot);
				target = importPlugin.importFile(inputStream);
				if (errorReader.getResult() != null
						&& errorReader.getResult().length() > 0) {
					Message.add(errorReader.getResult(), Message.ERROR);
				}
				if (inputReader.getResult() != null
						&& inputReader.getResult().length() > 0) {
					Message.add(inputReader.getResult(), Message.DEBUG);
				}
			} else {
				Message.add("Unable to execute fiona on this platform: "
						+ System.getProperty("os.name", ""), Message.DEBUG);
				target = new DotResult(new DotModel(""));
			}

		} catch (Exception e) {
			for (StackTraceElement element : e.getStackTrace()) {
				Message.add(element.toString(), Message.DEBUG);
			}
			Message.add("Unable to Fiona oWFN Net: " + e.toString(),
					Message.DEBUG);
		}
		return target;
	}

	public void setControllability(boolean b) {
		if (b) {
			options += " -t ig";
			extension = ".IG.out";
		}
	}

	public void setOperatingGuideline(boolean b) {
		if (b) {
			options += " -t og";
			extension = ".OG.out";
		}
	}

	public void setAllStates(boolean b) {
		if (b) {
			options += " -s allstates";
		}
	}

	public void setMsgBound(String text) {
		options += " -m " + text;
	}

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

class FionaUI extends JDialog implements ActionListener {
	Fiona fiona;
	JRadioButton controlButton;
	JRadioButton ogButton;
	Checkbox allStatesCheckBox;
	JTextField msgBoundTextField;
	ButtonGroup group;
	JButton doneButton;

	public FionaUI(Fiona fiona) {
		super(MainUI.getInstance(), "Select options...", true);
		this.fiona = fiona;

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

		group = new ButtonGroup();

		controlButton = new JRadioButton("Controllability", true);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		layout.setConstraints(controlButton, constraints);
		group.add(controlButton);
		panel.add(controlButton);

		ogButton = new JRadioButton("Operating guideline", false);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		constraints.gridy = 0;
		layout.setConstraints(ogButton, constraints);
		group.add(ogButton);
		panel.add(ogButton);

		allStatesCheckBox = new Checkbox("Show all states");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 1;
		layout.setConstraints(allStatesCheckBox, constraints);
		panel.add(allStatesCheckBox);

		JLabel msgBoundLabel = new JLabel("Message bound: ");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 2;
		layout.setConstraints(msgBoundLabel, constraints);
		panel.add(msgBoundLabel);

		msgBoundTextField = new JTextField("1");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		constraints.gridy = 2;
		layout.setConstraints(msgBoundTextField, constraints);
		panel.add(msgBoundTextField);

		doneButton = new JButton("Done");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		constraints.gridy = 3;
		layout.setConstraints(doneButton, constraints);
		panel.add(doneButton);

		doneButton.addActionListener(this);

		String aboutFiona = "This plug-in uses Fiona ";
		aboutFiona += "Copyright (C) 2005, 2006, 2007 Peter Massuthe, Daniela Weinberg, Karsten Wolf, ";
		aboutFiona += "Jan Bretschneider, Kathrin Kaschner, and Niels Lohmann.\n";
		aboutFiona += "This is free software; see the source for copying conditions. There is NO ";
		aboutFiona += "warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n";

		TextArea text = new TextArea(aboutFiona, 9, 50,
				TextArea.SCROLLBARS_NONE);
		text.setEditable(false);
		aboutPanel.add(text);
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		this.add(aboutPanel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == doneButton) {
			fiona.setControllability(controlButton.isSelected());
			fiona.setOperatingGuideline(ogButton.isSelected());
			fiona.setAllStates(allStatesCheckBox.getState());
			fiona.setMsgBound(msgBoundTextField.getText());
			dispose();
		}
	}
}
