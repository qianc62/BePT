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

package org.processmining.analysis.petrinet.cpnexport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.processmining.analysis.redesign.ui.RedesignAnalysisUI;
import org.processmining.framework.models.hlprocess.gui.HLProcessGui;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Utils;
import org.processmining.framework.ui.filters.GenericFileFilter;

/**
 * Displays the export settings that are available before actually exporting the
 * given high-level Petri net simulation model.
 * 
 * @author Anne Rozinat
 * @author Ronny Mans
 */
public class CpnExportSettings extends JPanel {

	/**
	 * Required for a serializable classes (generated quickfix). Not directly
	 * used.
	 */
	private static final long serialVersionUID = -1990267131106264024L;
	// only needed to show help page
	final CpnExport20 myAlgorithm;
	// technical attributes
	private ColoredPetriNet mySimulatedPN;
	/** The simulated PetriNet to be exported to CPN Tools */
	private HLPetriNet hlProcess;
	/** The high-level Petri net simulation model */
	private HLProcessGui hlProcessGui;
	// user interface related attributes
	private JButton exportButton = new AutoFocusButton("Export");
	/** invoking the actual export */
	private JButton saveButton = new AutoFocusButton("Save");
	/** invoking the actual export */
	private FlatTabbedPane tabPane = new FlatTabbedPane("CPN Export Settings",
			new Color(200, 200, 200), new Color(120, 120, 120, 200), new Color(
					200, 200, 200));
	private JButton docsButton = new SlickerButton("Help..."); // shows the
	// plugin
	// documentation

	private boolean saveDialogue;

	/**
	 * Normally, there is a save dialogue, in redesign mode this is disabled,
	 * i.e., false.
	 */

	/**
	 * Creates the settings frame for the CPN Tools export.
	 * 
	 * @param netToExport
	 *            the high-level Petri net simulation model to be exported to
	 *            CPN Tools
	 */
	public CpnExportSettings(CpnExport20 algorithm, ColoredPetriNet netToExport) {
		this(algorithm, netToExport, true);
	}

	/**
	 * Creates the settings frame for the CPN Tools export.
	 * 
	 * @param netToExport
	 *            the high-level Petri net simulation model to be exported to
	 *            CPN Tools
	 * @param saveOn
	 *            boolean indicating if a save dialogue is needed, true: it is,
	 *            false: redesign mode, no save dialogue.
	 */
	public CpnExportSettings(CpnExport20 algorithm,
			ColoredPetriNet netToExport, boolean saveOn) {
		this.saveDialogue = saveOn;
		myAlgorithm = algorithm;
		mySimulatedPN = netToExport;
		hlProcess = (HLPetriNet) mySimulatedPN.getHighLevelProcess();
		hlProcessGui = new HLProcessGui(hlProcess);
		// build GUI
		jbInit();
		// connect functionality to GUI elements
		registerGuiActionListener();
	}

	/**
	 * Construct the user interface for the CPN Tools export settings.
	 */
	private void jbInit() {
		// set up all help texts
		exportButton.setToolTipText("Start export to CPN Tools");
		saveButton.setToolTipText("Save as CPN file");
		docsButton.setToolTipText("Open the help page for the CPN Export");
		// build configuration and process tab views
		if (this.saveDialogue) {
			// use the original configuration
			tabPane.addTab("Configure Simulation Model", ManagerConfiguration
					.getInstance().getPanel());
			tabPane.addTab("Edit Process Details", hlProcessGui.getPanel());
			tabPane.addTab("Adjust Layout", ManagerLayout.getInstance()
					.getPanel());
		} else {
			// use the redesign configuration
			tabPane.addTab("Edit Process Details", hlProcessGui.getPanel());
			ManagerConfiguration.getInstance().setRedesignConfiguration();
			tabPane.addTab("Configure Simulation Model", ManagerConfiguration
					.getInstance().getPanel());
			tabPane.addTab("Adjust Layout", ManagerLayout.getInstance()
					.getPanel());
		}
		// add buttons
		GradientPanel buttonsPanel = new GradientPanel(
				new Color(120, 120, 120), new Color(90, 90, 90));
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		buttonsPanel.add(docsButton);
		buttonsPanel.add(Box.createHorizontalGlue());
		if (this.saveDialogue) {
			// use the original configuration
			buttonsPanel.add(exportButton);
		} else {
			// use the redesign configuration
			buttonsPanel.add(saveButton);
		}
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.setLayout(new BorderLayout());
		GradientPanel gradient = new GradientPanel(new Color(50, 50, 50),
				new Color(30, 30, 30));
		gradient.setLayout(new BorderLayout());
		gradient.add(tabPane, BorderLayout.CENTER);
		gradient.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 8));
		this.add(gradient, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.SOUTH);
	}

	/**
	 * Connect button actions.
	 */
	private void registerGuiActionListener() {
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String filename = Utils.saveFileDialog(MainUI
						.getInstance().getDesktop(), new GenericFileFilter(
						"cpn"));
				if (filename != null && !filename.equals("")) {
					FileOutputStream out = null;
					FileOutputStream sml = null;
					String smlName = null;
					// check for current state option
					if (ManagerConfiguration.getInstance()
							.isCurrentStateSelected()) {
						String[] splitName = filename.split(".cpn");
						smlName = splitName[0];
					}
					// generate cpn (and sml) file
					try {
						out = new FileOutputStream(filename);
						Message.add("<CPNexport>", Message.TEST);
						BufferedWriter outWriter = new BufferedWriter(
								new OutputStreamWriter(out));
						if (smlName != null) {
							sml = new FileOutputStream(smlName + ".sml");
							BufferedWriter smlWriter = new BufferedWriter(
									new OutputStreamWriter(sml));
							mySimulatedPN.writeToFile(outWriter, smlWriter,
									smlName);
							smlWriter.close();
						} else {
							mySimulatedPN.writeToFile(outWriter, null, null);
						}
						outWriter.close();
						Message.add("<CPNexport/>", Message.TEST);
						Message.add("Export of CPN model finished.",
								Message.NORMAL);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		// get and use ID of the redesigned model to save distinctively.
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCPNmodel();
			}
		});
		docsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(myAlgorithm);
			}
		});
	}

	public void saveCPNmodel() {
		String filename = RedesignAnalysisUI.locationForCurrentSimModels + "//"
				+ hlProcess.getPNModel().getIdentifier() + ".cpn";
		try {
			FileOutputStream out = new FileOutputStream(filename);
			BufferedWriter outWriter = new BufferedWriter(
					new OutputStreamWriter(out));
			mySimulatedPN.isRedesign(true);
			mySimulatedPN.writeToFile(outWriter, null, null);
			// close UI
			// setVisible(false);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

}
