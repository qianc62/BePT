package org.processmining.analysis.redesign.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.framework.models.hlprocess.HLGlobal;
import org.processmining.framework.models.hlprocess.HLTypes.TimeUnit;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution;
import org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGui;
import org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGuiManager;
import org.processmining.framework.util.GUIPropertyDoubleTextField;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyStringTextarea;
import org.processmining.framework.util.GuiUtilities;

/**
 * Author: Mariska Netjes
 * (c) 2008 Technische Universiteit Eindhoven and STW
 */

/**
 * Creates a gui representation for the simulation settings of the CPN model. <br>
 * Allows to view and edit the simulation settings.
 */
public class SimSettingsUI implements GuiDisplayable, GuiNotificationTarget {

	/**
	 * the global process information taken from the original model Maybe it is
	 * better to read the file with simulation settings to always have the
	 * latest setting presented
	 */
	protected HLGlobal global;

	/**
	 * Gui attributes
	 */
	protected JPanel outmostPanel;
	protected JPanel caseGenerationPanel;
	protected GUIPropertyListEnumeration listEnumForTimeUnit;
	protected GUIPropertyDoubleTextField numberOfCases;
	protected GUIPropertyDoubleTextField numberOfSubRuns;

	protected String location = "";
	private String caseDest = "numberOfCases.sml";
	private String caseRead = "readNumberOfCases.txt";
	private String runDest = "numberOfRuns.txt";
	private String distrDest = "arrivalRate.sml";
	private String locDest = "valFolder.sml";

	public static Color bgColor = new Color(190, 190, 190);

	/**
	 * Creates a new gui object.
	 */
	public SimSettingsUI(HLGlobal glob, String loc) {
		this.global = glob;
		this.location = loc;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
	 */
	public JPanel getPanel() {
		if (outmostPanel == null) {
			outmostPanel = new JPanel(new BorderLayout());
		}
		createPanel();
		return outmostPanel;
	}

	/**
	 * Creates a panel with the option to set the simulation settings. The
	 * possible settings are the number of cases, the number of sub runs, and
	 * the case generation scheme.
	 */
	private void createPanel() {
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.LINE_AXIS));
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		String description = new String(
				"Here you can specify the simulation settings of the processes. "
						+ "For example, the case generation scheme determines in which distribution new cases "
						+ "(that is, new process instances) arrive in the process.");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description);
		outmostPanel.add(BorderLayout.NORTH, helpText.getPropertyPanel());
		content.add(Box.createVerticalGlue());
		/**
		 * add the setting of the number of cases
		 */
		double cVal = readCaseFromFile();
		numberOfCases = new GUIPropertyDoubleTextField(
				"Number of cases per run", null, cVal, 0.0, 1000.0);
		content.add(numberOfCases.getPropertyPanel());
		content.add(Box.createVerticalGlue());
		/**
		 * add the setting of the number of sub runs
		 */
		double rVal = readRunFromFile();
		numberOfSubRuns = new GUIPropertyDoubleTextField(
				"Number of sub runs per simulation", null, rVal, 0.0, 100.0);
		content.add(numberOfSubRuns.getPropertyPanel());
		/**
		 * add the setting of the case generation scheme
		 */
		createCaseGenerationPanel();
		content.add(caseGenerationPanel);
		/**
		 * add the time unit to the panel
		 */
		listEnumForTimeUnit = new GUIPropertyListEnumeration("Time unit:",
				"Time unit used in the simulation model", TimeUnit
						.getAllTypes(), new TimeUnitChangeListener());
		/**
		 * set the current time unit as selected value for the combobox
		 */
		listEnumForTimeUnit.setValue(global.getTimeUnit());
		content.add(listEnumForTimeUnit.getPropertyPanel());
		content.add(Box.createVerticalGlue());
		/**
		 * add "OK" button to save set values (TO DO: close window).
		 */
		SlickerButton okButton = new SlickerButton("OK");
		okButton.setAlignmentX(SlickerButton.RIGHT_ALIGNMENT);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/**
				 * save the current settings to file
				 */
				saveSettingsToFile();
				// to do: CLOSE
			}
		});
		content.add(Box.createVerticalStrut(10));
		content.add(okButton);
		resultPanel.add(Box.createHorizontalGlue());
		resultPanel.add(content);
		resultPanel.add(Box.createHorizontalGlue());
		outmostPanel.add(
				GuiUtilities.getSimpleScrollable(resultPanel, bgColor),
				BorderLayout.CENTER);
		outmostPanel.setBackground(bgColor);
		SlickerSwingUtils.injectTransparency(outmostPanel);
	}

	/**
	 * Create the panel for the setting of the case generation scheme.
	 * 
	 */
	private void createCaseGenerationPanel() {
		if (caseGenerationPanel == null) {
			caseGenerationPanel = new JPanel();
			caseGenerationPanel.setLayout(new BoxLayout(caseGenerationPanel,
					BoxLayout.PAGE_AXIS));
			caseGenerationPanel.setOpaque(false);
		}
		caseGenerationPanel.removeAll();
		JPanel stPanel = new JPanel();
		stPanel.setLayout(new BoxLayout(stPanel, BoxLayout.LINE_AXIS));
		JLabel stLabel = new JLabel("Case generation scheme:");
		stPanel.add(stLabel);
		stPanel.add(Box.createHorizontalGlue());
		caseGenerationPanel.add(Box.createVerticalGlue());
		caseGenerationPanel.add(stPanel);
		caseGenerationPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		HLDistributionGui distGui = HLDistributionGuiManager
				.getDistributionGui(global.getCaseGenerationScheme(), this);
		caseGenerationPanel.add(distGui.getPanel());
		caseGenerationPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		caseGenerationPanel.validate();
		caseGenerationPanel.repaint();
	}

	/**
	 * Creates the initial setting files before the first experiment
	 * 
	 * @param simDest
	 *            String the location of the file
	 * @param d
	 *            HLDistribution the case generation scheme
	 */
	public void createSettingFiles(String simDest, HLDistribution d) {
		/**
		 * simDest is the name of the folder to write to, this folder should
		 * have been created.
		 */
		/**
		 * create the number of cases file the number is written into a function
		 * that is used in simulation
		 */
		String caseLoc = simDest + "\\" + caseDest;
		File caseFile = new File(caseLoc);
		if (!caseFile.exists()) {
			try {
				caseFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FileWriter cout = null;
		try {
			cout = new FileWriter(caseLoc);
			cout.write("fun readNumberOfCases() = 100;");
			cout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		/**
		 * create the second number of cases file with only the number itself
		 * the number is read for display in this UI.
		 */
		String case2Loc = simDest + "\\" + caseRead;
		try {
			(new File(case2Loc)).createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileWriter c2out = null;
		try {
			c2out = new FileWriter(case2Loc);
			c2out.write("" + (int) 100);
			c2out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		/**
		 * create the number of sub runs file
		 */
		String runLoc = simDest + "\\" + runDest;
		try {
			(new File(runLoc)).createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileWriter rout = null;
		try {
			rout = new FileWriter(runLoc);
			rout.write("" + (int) 3);
			rout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		/**
		 * create the arrival rate file with the function for the simulation
		 */
		String distrLoc = simDest + "\\" + distrDest;
		try {
			(new File(distrLoc)).createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileWriter dout = null;
		try {
			dout = new FileWriter(distrLoc);
			dout.write("fun readArrivalRate() = "
					+ CpnUtils.getCpnDistributionFunction(global
							.getCaseGenerationScheme()) + ";");
			dout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		/**
		 * create the location file for the simulation results
		 */
		String locLoc = simDest + "\\" + locDest;
		try {
			(new File(locLoc)).createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileWriter lout = null;
		try {
			lout = new FileWriter(locLoc);
			// Note that the following value will be overwritten before
			// simulation.
			lout
					.write("val FOLDER = \"C:/RedesignAnalysis/experiment_1/Original_0/sim_1\"");
			lout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * write the settings entered in the UI to the files, assumes files are
	 * already there.
	 */
	private void saveSettingsToFile() {
		FileWriter cout1 = null;
		FileWriter cout2 = null;
		FileWriter rout = null;
		FileWriter dout = null;
		try {
			cout1 = new FileWriter(location + "\\" + caseDest);
			cout1.write("fun readNumberOfCases() = "
					+ (int) numberOfCases.getValue() + ";");
			cout1.close();
			cout2 = new FileWriter(location + "\\" + caseRead);
			cout2.write("" + (int) numberOfCases.getValue());
			cout2.close();
			rout = new FileWriter(location + "\\" + runDest);
			rout.write("" + (int) numberOfSubRuns.getValue());
			rout.close();
			dout = new FileWriter(location + "\\" + distrDest);
			dout.write("fun readArrivalRate() = "
					+ CpnUtils.getCpnDistributionFunction(global
							.getCaseGenerationScheme()) + ";");
			dout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * overwrite the arrival rate, assumes corresponding file is there.
	 */
	public void saveArrivalRateToFile() {
		FileWriter dout = null;
		try {
			dout = new FileWriter(location + "\\" + distrDest);
			dout.write("fun readArrivalRate() = "
					+ CpnUtils.getCpnDistributionFunction(global
							.getCaseGenerationScheme()) + ";");
			dout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * reads the number of cases from file the number in file is read as a
	 * string and translated to a double.
	 * 
	 * @return c double the number of cases
	 */
	public double readCaseFromFile() {
		double c = 0;
		try {
			FileInputStream fis1 = null;
			BufferedInputStream bis1 = null;
			DataInputStream dis1 = null;
			fis1 = new FileInputStream(location + "\\" + caseRead);
			// Here BufferedInputStream is added for fast reading.
			bis1 = new BufferedInputStream(fis1);
			dis1 = new DataInputStream(bis1);
			while (dis1.available() != 0) {
				// this statement reads the line from the file
				String readVal = dis1.readLine();
				c = Double.parseDouble(readVal);
			}
			// dispose all the resources after using them.
			fis1.close();
			bis1.close();
			dis1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	/**
	 * reads the number of sub runs from file the number in file is read as a
	 * string and translated to a double.
	 * 
	 * @return r double the number of sub runs
	 */
	public double readRunFromFile() {
		double r = 0;
		try {
			FileInputStream fis2 = null;
			BufferedInputStream bis2 = null;
			DataInputStream dis2 = null;
			fis2 = new FileInputStream(location + "\\" + runDest);
			// Here BufferedInputStream is added for fast reading.
			bis2 = new BufferedInputStream(fis2);
			dis2 = new DataInputStream(bis2);
			while (dis2.available() != 0) {
				// this statement reads the line from the file
				String val = dis2.readLine();
				r = Double.parseDouble(val);
			}
			// dispose all the resources after using them.
			fis2.close();
			bis2.close();
			dis2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * read the settings entered in the files and write them to one file
	 * 
	 * @param fileName
	 *            String the location to write to.
	 */
	public void copySettingsToOneFile(String fileName) {
		FileWriter out = null;
		try {
			out = new FileWriter(fileName);
			out.write("The simulation settings of this experiment are: \n");
			out
					.write("The number of cases: " + (int) readCaseFromFile()
							+ "\n");
			out.write("The number of sub runs: " + (int) readRunFromFile()
					+ "\n");
			// out.write("The arrival rate: " +
			// CpnUtils.getCpnDistributionFunction(readArrivalRateFromFile()) +
			// "\n");
			out.write("The arrival rate: "
					+ CpnUtils.getCpnDistributionFunction(global
							.getCaseGenerationScheme()) + "\n");
			out.close();
		} catch (IOException excep) {
			excep.printStackTrace();
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 *      Update after time unit change as this leads to a re-calculation of
	 *      time-related properties (values need to be converted to match new
	 *      time unit).
	 */
	public void updateGUI() {
		// update attributes that might have changed
		global.setTimeUnit((TimeUnit) listEnumForTimeUnit.getValue());

		// also update the gui as, for example, the distribution type might have
		// changed
		createCaseGenerationPanel();
		outmostPanel.validate();
		outmostPanel.repaint();
		SlickerSwingUtils.injectTransparency(outmostPanel);
	}

	/**
	 * Invokes the conversion of all time-related values and issues a redraw of
	 * the global GUI.
	 */
	protected void updateTimeUnit() {
		global.changeTimeUnit((TimeUnit) listEnumForTimeUnit.getValue());
		updateGUI();
	}

	/**
	 * Listener for time unit change as this leads to a re-calculation of
	 * time-related properties (values need to be converted to match new time
	 * unit).
	 */
	class TimeUnitChangeListener implements GuiNotificationTarget {

		public void updateGUI() {
			if (global.getTimeUnit() != listEnumForTimeUnit.getValue()) {
				updateTimeUnit();
			}
		}
	}

}
