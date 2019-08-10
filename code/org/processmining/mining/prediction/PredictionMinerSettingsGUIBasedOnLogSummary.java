package org.processmining.mining.prediction;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.*;
import java.text.*;
import java.lang.reflect.InvocationTargetException;

import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.*;

/**
 * @author Ronald Crooy
 * 
 */
public class PredictionMinerSettingsGUIBasedOnLogSummary extends JPanel {
	private PredictionMinerSettingsBasedOnLogSummary localSettings;

	private JComboBox kernelContBox;
	private JLabel kernelContlabel;

	private JComboBox kernelCatBox;
	private JLabel kernelCatlabel;

	private JComboBox kernelOrdBox;
	private JLabel kernelOrdlabel;

	private JComboBox timesizebox;
	private JLabel timesizeboxlabel;

	private JComboBox targetTypeBox;
	private JLabel targetTypelabel;

	private JComboBox targetElementBox;
	private JLabel targetElementlabel;

	private JComboBox startEventBox;
	private JLabel startEventLabel;

	private JComboBox completeEventBox;
	private JLabel completeEventLabel;

	private JList elementsList;
	private JLabel elementslabel;

	private JCheckBox useChanceBox;
	private JLabel useChanceLabel;

	private JFormattedTextField chancebox;
	private JLabel chanceLabel;

	private JSpinner KSpinner;
	private JLabel KLabel;

	private JSpinner BSpinner;
	private JLabel BLabel;

	private JSpinner RSpinner;
	private JLabel RLabel;

	private JCheckBox useAttribsbox;
	private JLabel useAttribslabel;

	private JCheckBox useCompleteBandwidthbox;
	private JLabel useCompleteBandwidthlabel;

	private JCheckBox useDurationsbox;
	private JLabel useDurationslabel;

	private JCheckBox useOccurancesbox;
	private JLabel useOccuranceslabel;

	private JCheckBox useCrossvalidate;
	private JLabel useCrossvalidateLabel;

	private JFormattedTextField tolerancebox;
	private JLabel tolerancelabel;
	private NumberFormat numform;

	private JButton addRconnectionButton;
	private JButton delRconnectionButton;
	private JList listofRconnections;

	private boolean onelocalconnection;

	public PredictionMinerSettingsGUIBasedOnLogSummary(
			PredictionMinerSettingsBasedOnLogSummary settings,
			LogSummary summary) {
		localSettings = settings;
		onelocalconnection = false;
		JTabbedPane tabs = new JTabbedPane();
		tabs.add("connections to R", RconnectionsTab());
		tabs.add("prediction settings", settingsGUI(summary));
		this.add(tabs);
	}

	public PredictionMinerSettingsBasedOnLogSummary getSettings() {
		double temp = ((Number) tolerancebox.getValue()).doubleValue();
		localSettings.tol = temp;
		return localSettings;
	}

	public JPanel RconnectionsTab() {
		JPanel panel = new JPanel();
		panel.setLayout(new SpringLayout());
		addRconnectionButton = new JButton("Add R connection");
		addRconnectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (RconnectGUI()) {
					listofRconnections.setListData(localSettings.Rconnections
							.keySet().toArray());
				}
			};
		});
		delRconnectionButton = new JButton("Delete R connection");
		delRconnectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = listofRconnections.getSelectedValue().toString();
				localSettings.Rconnections.remove(name);
				listofRconnections.setListData(localSettings.Rconnections
						.keySet().toArray());
			}
		});

		listofRconnections = new JList();
		listofRconnections
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panel.add(addRconnectionButton);
		panel.add(delRconnectionButton);
		panel.add(listofRconnections);
		SpringUtils.makeCompactGrid(panel, 3, 1, // rows, cols
				6, 2, // initX, initY
				6, 2); // xPad, yPad
		return panel;
	}

	private boolean RconnectGUI() {
		String host = null;
		Integer port = null;
		String name = null;
		RConnector rconnector = new RConnector();
		Boolean localchosen = false;
		Boolean adddefaults = false;
		int again = JOptionPane.YES_OPTION;
		while (!rconnector.succes && again == JOptionPane.YES_OPTION) {
			if (!this.onelocalconnection) {
				// String[]
				// options={"local R installation","local or remote R server","add all 4 sviscl0x.win.tue.nl servers"};
				String[] options = { "local or remote Rserve-server",
						"add all 4 sviscl0x.win.tue.nl servers" };
				int n = JOptionPane
						.showOptionDialog(this, "Choose R connection:",
								"R connection", JOptionPane.YES_NO_OPTION,
								// JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);
				localchosen = (n == JOptionPane.YES_OPTION);
				// adddefaults=(n==JOptionPane.CANCEL_OPTION);
			}
			// if (localchosen){
			// //local chosen
			// onelocalconnection= rconnector.testLocalR();
			// name="local R connection";
			// }else if(!localchosen&&!adddefaults){
			if (localchosen) {
				// remote chosen
				host = JOptionPane.showInputDialog(null, "specify host");
				String Sport = JOptionPane
						.showInputDialog(null, "specify port");
				try {
					port = Integer.valueOf(Sport);
				} catch (NumberFormatException e) {
					port = 6311;
				}
				rconnector.testRconnection(host, port);
				String servername = host + ":" + port;
				String servename = servername;
				Integer x = 0;
				while (localSettings.Rconnections.containsKey(servename)) {
					servename = servername + "." + x.toString();
					x++;
				}
				name = servename;
				// }else if(adddefaults){
			} else if (!localchosen) {
				for (Integer i = 1; i < 4; i++) {
					String servername = "sviscl0" + i.toString()
							+ ".win.tue.nl";
					RConnector rconnector1 = new RConnector();
					rconnector1.testRconnection(servername, 6311);
					if (rconnector1.succes) {
						servername = servername + ":6311";
						String servename = servername;
						Integer x = 0;
						while (localSettings.Rconnections
								.containsKey(servename)) {
							servename = servername + "." + x.toString();
							x++;
						}
						rconnector1.name = servename;
						localSettings.Rconnections.put(servename, rconnector1);
					}
				}// last entry cannot be stored because of default code below
				String servername = "sviscl04.win.tue.nl";
				rconnector = new RConnector();
				rconnector.testRconnection(servername, 6311);
				if (rconnector.succes) {
					servername = servername + ":6311";
					String servename = servername;
					Integer x = 0;
					while (localSettings.Rconnections.containsKey(servename)) {
						servename = servername + "." + x.toString();
						x++;
					}
					name = servename;
				}
				// nothing is missing, just see below
			}
			if (!rconnector.succes) {
				again = JOptionPane.showConfirmDialog(null,
						"Connection failed! try again?",
						"Connection failed! try again?",
						JOptionPane.YES_NO_OPTION);
			}
		}
		if (rconnector.succes) {
			rconnector.name = name;
			localSettings.Rconnections.put(name, rconnector);
			return true;
		} else {
			return false;
		}
	}

	private JPanel settingsGUI(LogSummary summary) {
		JPanel panel = new JPanel();
		panel.setLayout(new SpringLayout());

		elementsList = new JList(summary.getModelElements());
		elementslabel = new JLabel("Select elements to use:");
		elementsList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		elementsList.setLayoutOrientation(JList.VERTICAL);
		elementsList.setSelectionInterval(0,
				summary.getModelElements().length - 1);
		elementsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList ls = (JList) e.getSource();
				Object[] els = ls.getSelectedValues();
				localSettings.useElements.clear();
				for (int i = 0; i < els.length; i++) {
					localSettings.useElements.add(els[i].toString());
				}
			};
		});

		String[] ob = summary.getModelElements();
		for (int i = 0; i < ob.length; i++) {
			localSettings.useElements.add(ob[i]);
		}

		panel.add(elementslabel);
		panel.add(elementsList);

		Vector conalgs = new Vector(3);
		conalgs.add("Second Order Gaussian");
		conalgs.add("Second Order Epanechnikov");
		conalgs.add("Uniform");
		kernelContlabel = new JLabel(
				"Kernel function for continuous variables:");
		kernelContBox = new JComboBox(conalgs);
		kernelContBox.setSelectedIndex(localSettings.contKernel);
		kernelContBox.setMaximumSize(kernelContBox.preferredSize());
		kernelContlabel.setLabelFor(kernelContBox);
		kernelContBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				localSettings.contKernel = cb.getSelectedIndex();
			};
		});
		panel.add(kernelContlabel);
		panel.add(kernelContBox);

		Vector disalgs = new Vector(3);
		disalgs.add("Aitchison and Aitken");
		disalgs.add("Li and Racine");
		kernelCatlabel = new JLabel(
				"Kernel function for categorical variables:");
		kernelCatBox = new JComboBox(disalgs);
		kernelCatBox.setSelectedIndex(localSettings.catKernel);
		kernelCatBox.setMaximumSize(kernelCatBox.preferredSize());
		kernelCatlabel.setLabelFor(kernelCatBox);
		kernelCatBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				localSettings.catKernel = cb.getSelectedIndex();
			};
		});
		panel.add(kernelCatlabel);
		panel.add(kernelCatBox);

		Vector ordalgs = new Vector(3);
		ordalgs.add("Wang and van Ryzin");
		ordalgs.add("Li and Racine");
		kernelOrdlabel = new JLabel(
				"Kernel function for ordered discrete variables:");
		kernelOrdBox = new JComboBox(ordalgs);
		kernelOrdBox.setSelectedIndex(localSettings.ordKernel);
		kernelOrdBox.setMaximumSize(kernelOrdBox.preferredSize());
		kernelOrdlabel.setLabelFor(kernelOrdBox);
		kernelOrdBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				localSettings.ordKernel = cb.getSelectedIndex();
			};
		});
		panel.add(kernelOrdlabel);
		panel.add(kernelOrdBox);

		timesizeboxlabel = new JLabel("choose type of time measurement");
		String[] options2 = { "second", "minute", "hour", "day" };
		timesizebox = new JComboBox(options2);
		timesizebox.setSelectedIndex(2);
		timesizebox.setMaximumSize(timesizebox.preferredSize());
		timesizeboxlabel.setLabelFor(timesizebox);
		timesizebox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// change timesize
				JComboBox cb = (JComboBox) e.getSource();
				String value = (String) cb.getSelectedItem();
				changeTimeSize(value);
			};
		});
		panel.add(timesizeboxlabel);
		panel.add(timesizebox);

		Vector targets = new Vector(3);
		targets.add("Total remaining cycle time");
		targets.add("Remaining cycle time until element : ");
		targets.add("Occurance of element : ");
		targetTypelabel = new JLabel("Predict the : ");
		targetTypeBox = new JComboBox(targets);
		targetTypeBox.setSelectedIndex(localSettings.target);
		targetTypeBox.setMaximumSize(targetTypeBox.preferredSize());
		targetTypelabel.setLabelFor(targetTypeBox);
		targetTypeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				localSettings.target = cb.getSelectedIndex();
				if (cb.getSelectedIndex() > 0) {
					targetElementBox.enable();
				} else {
					targetElementBox.disable();
				}
			};
		});
		panel.add(targetTypelabel);
		panel.add(targetTypeBox);

		targetElementlabel = new JLabel("- ");
		targetElementBox = new JComboBox(summary.getModelElements());
		targetElementBox.setSelectedIndex(0);
		targetElementBox.setMaximumSize(targetElementBox.preferredSize());
		targetElementBox.disable();
		targetElementBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String temp = (String) cb.getSelectedItem();
				localSettings.targetElement = temp;
			};
		});
		panel.add(targetElementlabel);
		panel.add(targetElementBox);

		startEventLabel = new JLabel("select 'start'-event");
		startEventBox = new JComboBox(summary.getEventTypes());
		startEventBox.setSelectedItem("start");
		startEventBox.setMaximumSize(startEventBox.preferredSize());
		startEventBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				localSettings.startEvents.clear();
				localSettings.startEvents.add((String) cb.getSelectedItem());
			};
		});
		panel.add(startEventLabel);
		panel.add(startEventBox);

		completeEventLabel = new JLabel("select 'complete'-event");
		completeEventBox = new JComboBox(summary.getEventTypes());
		completeEventBox.setSelectedItem("complete");
		completeEventBox.setMaximumSize(completeEventBox.preferredSize());
		completeEventBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				localSettings.targetElement = (String) cb.getSelectedItem();
			};
		});
		panel.add(completeEventLabel);
		panel.add(completeEventBox);

		useCrossvalidate = new JCheckBox();
		useCrossvalidateLabel = new JLabel("use K-fold Crossvalidation");
		useCrossvalidate.setSelected(localSettings.crossvalidate);
		useCrossvalidate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				localSettings.crossvalidate = cb.isSelected();
				// BSpinner.setVisible(localSettings.crossvalidate&&!localSettings.bychance);
				chancebox.setVisible(localSettings.crossvalidate
						&& localSettings.bychance);
				useChanceBox.setVisible(localSettings.crossvalidate);
				KSpinner.setVisible(localSettings.crossvalidate);
				RSpinner.setVisible(localSettings.crossvalidate);

				if (cb.isSelected()) {
					BSpinner.setValue(Math.min((Integer) BSpinner.getValue(),
							localSettings.casesize - localSettings.casesize
									/ localSettings.cvsize));
					SpinnerModel Bmodel = new SpinnerNumberModel(
							localSettings.bandSize.intValue(), 2,
							localSettings.casesize - localSettings.casesize
									/ localSettings.cvsize, 1);
					BSpinner.setModel(Bmodel);
				} else {
					SpinnerModel Bmodel = new SpinnerNumberModel(
							localSettings.bandSize.intValue(), 2,
							localSettings.casesize.intValue(), 1);
					BSpinner.setModel(Bmodel);
				}
			};
		});
		panel.add(useCrossvalidateLabel);
		panel.add(useCrossvalidate);

		// useCompleteBandwidth
		useCompleteBandwidthbox = new JCheckBox();
		useCompleteBandwidthlabel = new JLabel(
				"use All cases for bandwidth selection");
		useCompleteBandwidthbox
				.setSelected(localSettings.useCompleteTrainingForBandwidth);
		useCompleteBandwidthbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				localSettings.useCompleteTrainingForBandwidth = cb.isSelected();
				BSpinner
						.setVisible(!localSettings.useCompleteTrainingForBandwidth
								&& !localSettings.bychance);
				chancebox
						.setVisible(!localSettings.useCompleteTrainingForBandwidth
								&& localSettings.bychance);
				useChanceBox
						.setVisible(!localSettings.useCompleteTrainingForBandwidth);
			};
		});
		panel.add(useCompleteBandwidthlabel);
		panel.add(useCompleteBandwidthbox);

		useChanceBox = new JCheckBox();
		useChanceLabel = new JLabel(
				"choose cases by chance(or choose N random cases)?");
		useChanceBox.setSelected(localSettings.bychance);
		useChanceBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				localSettings.bychance = cb.isSelected();
				BSpinner.setVisible(!localSettings.bychance);
				chancebox.setVisible(localSettings.bychance);
			};
		});
		panel.add(useChanceLabel);
		panel.add(useChanceBox);

		chancebox = new JFormattedTextField(numform);
		chancebox.setValue(localSettings.chance);
		chanceLabel = new JLabel("select chance");
		chancebox.hide();
		chancebox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFormattedTextField cb = (JFormattedTextField) e.getSource();
				localSettings.chance = ((Number) cb.getValue()).doubleValue();
			};
		});
		panel.add(chanceLabel);
		panel.add(chancebox);

		SpinnerModel Bmodel = new SpinnerNumberModel(localSettings.bandSize
				.intValue(), 2, localSettings.casesize - localSettings.casesize
				/ localSettings.cvsize, 1);
		BSpinner = new JSpinner(Bmodel);
		BLabel = new JLabel("Select size for bandwidth selection");
		BSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner source = (JSpinner) e.getSource();
				int val = (Integer) source.getValue();
				localSettings.bandSize = val;
			}
		});
		panel.add(BLabel);
		panel.add(BSpinner);

		SpinnerModel Kmodel = new SpinnerNumberModel(localSettings.cvsize
				.intValue(), 2, localSettings.casesize.intValue(), 1);
		KSpinner = new JSpinner(Kmodel);
		KLabel = new JLabel("Select K for K-fold-cross-validation");
		KSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner source = (JSpinner) e.getSource();
				int val = (Integer) source.getValue();
				localSettings.cvsize = val;
				SpinnerModel Bmodel = new SpinnerNumberModel(
						localSettings.bandSize.intValue(), 2,
						localSettings.casesize - localSettings.casesize
								/ localSettings.cvsize, 1);
				BSpinner.setModel(Bmodel);
			}
		});
		panel.add(KLabel);
		panel.add(KSpinner);

		SpinnerModel Rmodel = new SpinnerNumberModel(localSettings.repeatOpt
				.intValue(), 1, 10, 1);
		RSpinner = new JSpinner(Rmodel);
		RLabel = new JLabel("Repeat bandwidth selection");
		RSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner source = (JSpinner) e.getSource();
				int val = (Integer) source.getValue();
				localSettings.repeatOpt = val;
			}
		});
		panel.add(RLabel);
		panel.add(RSpinner);

		useAttribsbox = new JCheckBox();
		useAttribslabel = new JLabel("use Data Attributes?");
		useAttribsbox.setSelected(localSettings.useAttributes);
		useAttribsbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				localSettings.useAttributes = cb.isSelected();
			};
		});
		panel.add(useAttribslabel);
		panel.add(useAttribsbox);

		useDurationsbox = new JCheckBox();
		useDurationslabel = new JLabel("use Durations?");
		useDurationsbox.setSelected(localSettings.useDurations);
		useDurationsbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				localSettings.useDurations = cb.isSelected();
			};
		});
		panel.add(useDurationslabel);
		panel.add(useDurationsbox);

		useOccurancesbox = new JCheckBox();
		useOccuranceslabel = new JLabel("use Occurances?");
		useOccurancesbox.setSelected(localSettings.useOccurrences);
		useOccurancesbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				localSettings.useOccurrences = cb.isSelected();
			};
		});
		panel.add(useOccuranceslabel);
		panel.add(useOccurancesbox);

		tolerancebox = new JFormattedTextField(numform);
		tolerancebox.setValue(localSettings.tol);
		tolerancelabel = new JLabel(
				"Set the tolerance for the bandwith selection");
		tolerancebox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFormattedTextField cb = (JFormattedTextField) e.getSource();
				double temp = ((Number) cb.getValue()).doubleValue();
				localSettings.tol = temp;
			};
		});
		panel.add(tolerancelabel);
		panel.add(tolerancebox);

		SpringUtils.makeCompactGrid(panel, 20, 2, // rows, cols
				6, 2, // initX, initY
				6, 2); // xPad, yPad
		// Message.add("Prediction Miner Settings GUI closed");
		return panel;
	}

	private void changeTimeSize(String value) {
		if (value == "second")
			localSettings.timeSize = 1000;
		if (value == "minute")
			localSettings.timeSize = 1000 * 60;
		if (value == "hour")
			localSettings.timeSize = 1000 * 60 * 60;
		if (value == "day")
			localSettings.timeSize = 1000 * 60 * 60 * 24;
	}

}
