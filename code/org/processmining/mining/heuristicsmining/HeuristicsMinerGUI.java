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

package org.processmining.mining.heuristicsmining;

import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.LogSummary;

/**
 * @author Ton Weijters
 * @version 1.0
 */

// 1 private double relativeToBestThreshold = RELATIVE_TO_BEST_THRESHOLD;
// 2 1private int positiveObservationsThreshold =
// POSITIVE_OBSERVATIONS_THRESHOLD;
// 3 private double dependencyThreshold = DEPENDENCY_THRESHOLD;
// 4 private double l1lThreshold = L1L_THRESHOLD;
// 5 private double l2lThreshold = L2L_THRESHOLD;
// 5b private double LDThreshold = LONG_DISTANCE_THRESHOLD;
// 6 private int dependencyDivisor = DEPENDENCY_DIVISOR;
// 7 private double andThreshold = and_THRESHOLD;
// 8 extraInfo = false;
// 9 readUseAllConnectedHeuristics = true;
// 10 useLongDistanceDependency = flase;
public class HeuristicsMinerGUI extends JPanel implements FocusListener {
	private LogSummary summary;
	private HeuristicsMinerParameters parameters;

	JPanel jPanel1 = new JPanel();

	JLabel relativeToBestThresholdLabel = new JLabel();
	JTextField relativeToBestThresholdText = new JTextField(10);
	JLabel positiveObservationsThresholdLabel = new JLabel();
	JTextField positiveObservationsThresholdText = new JTextField(10);
	JLabel dependencyThresholdLabel = new JLabel();
	JTextField dependencyThresholdText = new JTextField(10);
	JLabel l1lThresholdLabel = new JLabel();
	JTextField l1lThresholdText = new JTextField(10);
	JLabel l2lThresholdLabel = new JLabel();
	JTextField l2lThresholdText = new JTextField(10);
	JLabel LDThresholdLabel = new JLabel();
	JTextField LDThresholdText = new JTextField(10);
	JLabel dependencyDivisorLabel = new JLabel();
	JTextField dependencyDivisorText = new JTextField(10);
	JLabel andThresholdLabel = new JLabel();
	JTextField andThresholdText = new JTextField(10);
	JLabel extraInfoLabel = new JLabel();
	JCheckBox extraInfo = new JCheckBox();
	JLabel useAllConnectedHeuristicsLabel = new JLabel();
	JCheckBox useAllConnectedHeuristics = new JCheckBox();
	JLabel useLongDistanceDependencyLabel = new JLabel();
	JCheckBox useLongDistanceDependency = new JCheckBox();

	public HeuristicsMinerGUI(LogSummary summary,
			HeuristicsMinerParameters parameters) {
		this.summary = summary;
		this.parameters = parameters;

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private int power10(int x) {
		int y = 1;
		int v = 10;
		while (x <= v) {
			v = 10 * v;
			y++;
		}
		return y;
	}

	private void jbInit() throws Exception {
		int nPI;
		jPanel1.setVisible(true);
		jPanel1.setLayout(new GridLayout(11, 2));

		nPI = summary.getNumberOfProcessInstances();
		relativeToBestThresholdLabel
				.setText(parameters.RELATIVE_TO_BEST_THRESHOLD_L);
		relativeToBestThresholdText.setText(Double
				.toString(parameters.RELATIVE_TO_BEST_THRESHOLD));
		positiveObservationsThresholdLabel
				.setText(parameters.POSITIVE_OBSERVATIONS_THRESHOLD_L);

		positiveObservationsThresholdText.setText(Integer
				.toString(parameters.POSITIVE_OBSERVATIONS_THRESHOLD));
		// positiveObservationsThresholdText.setText(Integer.toString(
		// 2 + power10(nPI)));

		dependencyThresholdLabel.setText(parameters.DEPENDENCY_THRESHOLD_L);
		dependencyThresholdText.setText(Double
				.toString(parameters.DEPENDENCY_THRESHOLD));
		l1lThresholdLabel.setText(parameters.L1L_THRESHOLD_L);
		l1lThresholdText.setText(Double.toString(parameters.L1L_THRESHOLD));
		l2lThresholdLabel.setText(parameters.L2L_THRESHOLD_L);
		l2lThresholdText.setText(Double.toString(parameters.L2L_THRESHOLD));
		LDThresholdLabel.setText(parameters.LONG_DISTANCE_THRESHOLD_L);
		LDThresholdText.setText(Double
				.toString(parameters.LONG_DISTANCE_THRESHOLD));

		dependencyDivisorLabel.setText(parameters.DEPENDENCY_DIVISOR_L);
		dependencyDivisorText.setText(Integer
				.toString(parameters.DEPENDENCY_DIVISOR));
		// dependencyDivisorText.setText(Integer.toString(power10(nPI)));

		andThresholdLabel.setText(parameters.AND_THRESHOLD_L);
		andThresholdText.setText(Double.toString(parameters.AND_THRESHOLD));
		// extraInfoLabel.setText("Radio button");
		extraInfo.setSelected(false);
		extraInfo.setText("Extra info");
		useAllConnectedHeuristics.setSelected(true);
		useAllConnectedHeuristics
				.setText("Use all-activities-connected-heuristic");
		useLongDistanceDependency.setSelected(false);
		useLongDistanceDependency
				.setText("Use long distance dependency heuristics");

		setHeuristicsMinerParameters(parameters);

		relativeToBestThresholdText.addFocusListener(this);
		positiveObservationsThresholdText.addFocusListener(this);
		dependencyThresholdText.addFocusListener(this);
		l1lThresholdText.addFocusListener(this);
		l2lThresholdText.addFocusListener(this);
		LDThresholdText.addFocusListener(this);
		dependencyDivisorText.addFocusListener(this);
		andThresholdText.addFocusListener(this);
		extraInfo.addFocusListener(this);
		useAllConnectedHeuristics.addFocusListener(this);
		useLongDistanceDependency.addFocusListener(this);

		this.add(jPanel1, null);

		// 1: relativeToBestThreshold = 0.00;
		jPanel1.add(relativeToBestThresholdLabel, null);
		jPanel1.add(relativeToBestThresholdText, null);

		// 2: positiveObservationsThreshold = 3;
		jPanel1.add(positiveObservationsThresholdLabel, null);
		jPanel1.add(positiveObservationsThresholdText, null);

		// 3: dependencyThreshold = 0.90;
		jPanel1.add(dependencyThresholdLabel, null);
		jPanel1.add(dependencyThresholdText, null);

		// 4: l1lThreshold = 0.90;
		jPanel1.add(l1lThresholdLabel, null);
		jPanel1.add(l1lThresholdText, null);

		// 5: l2lThreshold = 0.90;
		jPanel1.add(l2lThresholdLabel, null);
		jPanel1.add(l2lThresholdText, null);

		// 5b: LDThreshold = 0.90;
		jPanel1.add(LDThresholdLabel, null);
		jPanel1.add(LDThresholdText, null);

		// 6: dependencyDivisor = 1;
		jPanel1.add(dependencyDivisorLabel, null);
		jPanel1.add(dependencyDivisorText, null);

		// 7: andThreshold = 0.10;
		jPanel1.add(andThresholdLabel, null);
		jPanel1.add(andThresholdText, null);

		// 8: extraInfo
		jPanel1.add(extraInfoLabel, null);
		jPanel1.add(extraInfo, null);
		extraInfo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					parameters.setExtraInfo(true);
				}
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					parameters.setExtraInfo(false);
				}
			}
		});

		// 9: extraInfo
		jPanel1.add(useAllConnectedHeuristicsLabel, null);
		jPanel1.add(useAllConnectedHeuristics, null);
		useAllConnectedHeuristics.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					parameters.setUseAllConnectedHeuristics(true);
				}
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					parameters.setUseAllConnectedHeuristics(false);
				}
			}
		});

		// 10: longDistanceDependencyHeuristics
		jPanel1.add(useLongDistanceDependencyLabel, null);
		jPanel1.add(useLongDistanceDependency, null);
		useLongDistanceDependency.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					parameters.setUseLongDistanceDependency(true);
				}
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					parameters.setUseLongDistanceDependency(false);
				}
			}
		});

	}

	public void setHeuristicsMinerParameters(
			HeuristicsMinerParameters parameters) {
		parameters.setRelativeToBestThreshold(readRelativeToBestThreshold());
		parameters
				.setPositiveObservationsThreshold(readPositiveObservationsThreshold());
		parameters.setDependencyThreshold(readDependencyThreshold());
		parameters.setL1lThreshold(readL1lThreshold());
		parameters.setL2lThreshold(readL2lThreshold());
		parameters.setLDThreshold(readL2lThreshold());
		parameters.setDependencyDivisor(readDependencyDivisor());
		parameters.setAndThreshold(readAndThreshold());
		parameters.setExtraInfo(readExtraInfo());
		parameters
				.setUseAllConnectedHeuristics(readUseAllConnectedHeuristics());
		parameters
				.setUseLongDistanceDependency(readUseLongDistanceDependency());
	}

	// 1 private double relativeToBestThreshold = RELATIVE_TO_BEST_THRESHOLD;
	private double readRelativeToBestThreshold() {
		try {
			if (Double.parseDouble(relativeToBestThresholdText.getText()) < 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			relativeToBestThresholdText.setText(Double
					.toString(parameters.RELATIVE_TO_BEST_THRESHOLD));
		}
		return Double.parseDouble(relativeToBestThresholdText.getText());
	}

	// 2 1private int positiveObservationsThreshold =
	// POSITIVE_OBSERVATIONS_THRESHOLD;
	private int readPositiveObservationsThreshold() {
		try {
			if (Integer.parseInt(positiveObservationsThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			positiveObservationsThresholdText.setText(Double
					.toString(parameters.POSITIVE_OBSERVATIONS_THRESHOLD));
		}
		return Integer.parseInt(positiveObservationsThresholdText.getText());
	}

	// 3 private double dependencyThreshold = DEPENDENCY_THRESHOLD;
	private double readDependencyThreshold() {
		try {
			if (Double.parseDouble(dependencyThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			dependencyThresholdText.setText(Double
					.toString(parameters.DEPENDENCY_THRESHOLD));
		}
		return Double.parseDouble(dependencyThresholdText.getText());
	}

	// 4 private double l1lThreshold = L1L_THRESHOLD;
	private double readL1lThreshold() {
		try {
			if (Double.parseDouble(l1lThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			l1lThresholdText.setText(Double.toString(parameters.L1L_THRESHOLD));
		}
		return Double.parseDouble(l1lThresholdText.getText());
	}

	// 5 private double l2lThreshold = L2L_THRESHOLD;
	private double readL2lThreshold() {
		try {
			if (Double.parseDouble(l2lThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			l2lThresholdText.setText(Double.toString(parameters.L2L_THRESHOLD));
		}
		return Double.parseDouble(l2lThresholdText.getText());
	}

	// 5b private double l2lThreshold = L2L_THRESHOLD;
	private double readLDThreshold() {
		try {
			if (Double.parseDouble(LDThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			LDThresholdText.setText(Double
					.toString(parameters.LONG_DISTANCE_THRESHOLD));
		}
		return Double.parseDouble(LDThresholdText.getText());
	}

	// 6 private int dependencyDivisor = DEPENDENCY_DIVISOR;
	private int readDependencyDivisor() {
		try {
			if (Integer.parseInt(dependencyDivisorText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			dependencyDivisorText.setText(Double
					.toString(parameters.DEPENDENCY_DIVISOR));
		}
		return Integer.parseInt(dependencyDivisorText.getText());
	}

	// 7 private double andThreshold = and_THRESHOLD;
	private double readAndThreshold() {
		try {
			if (Double.parseDouble(andThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			andThresholdText.setText(Double.toString(parameters.AND_THRESHOLD));
		}
		return Double.parseDouble(andThresholdText.getText());
	}

	// 8 extraInfo
	private boolean readExtraInfo() {
		return extraInfo.isSelected();
	}

	// 9 useAllConnectedHeuristics
	private boolean readUseAllConnectedHeuristics() {
		return useAllConnectedHeuristics.isSelected();
	}

	// 10 useLongDistanceDependency
	private boolean readUseLongDistanceDependency() {
		return useLongDistanceDependency.isSelected();
	}

	// 1 private double relativeToBestThreshold = RELATIVE_TO_BEST_THRESHOLD;
	// 2 1private int positiveObservationsThreshold =
	// POSITIVE_OBSERVATIONS_THRESHOLD;
	// 3 private double dependencyThreshold = DEPENDENCY_THRESHOLD;
	// 4 private double l1lThreshold = L1L_THRESHOLD;
	// 5 private double l2lThreshold = L2L_THRESHOLD;
	// 5b private double LDThreshold = LONG_DISTANCE_THRESHOLD;
	// 6 private int dependencyDivisor = DEPENDENCY_DIVISOR;
	// 7 private double andThreshold = and_THRESHOLD;
	// 8 extraInfo
	// 9 readUseAllConnectedHeuristics
	// 10 readLongDistanceDependency

	public void focusLost(FocusEvent e) {
		if (e.getSource() == relativeToBestThresholdText) {
			// Message.add("1 relativeToBestThresholdText: M'n focus is weg!");
			parameters
					.setRelativeToBestThreshold(readRelativeToBestThreshold());
		} else if (e.getSource() == positiveObservationsThresholdText) {
			// Message.add("2 positiveObservationsThreshold: M'n focus is weg!");
			parameters
					.setPositiveObservationsThreshold(readPositiveObservationsThreshold());
		} else if (e.getSource() == dependencyThresholdText) {
			// Message.add("3 dependencyThreshold: M'n focus is weg!");
			parameters.setDependencyThreshold(readDependencyThreshold());
		} else if (e.getSource() == l1lThresholdText) {
			// Message.add("4 l1lThreshold: M'n focus is weg!");
			parameters.setL1lThreshold(readL1lThreshold());
		} else if (e.getSource() == l2lThresholdText) {
			// Message.add("5 l2lThreshold: M'n focus is weg!");
			parameters.setL2lThreshold(readL2lThreshold());
		} else if (e.getSource() == LDThresholdText) {
			// Message.add("5 l2lThreshold: M'n focus is weg!");
			parameters.setLDThreshold(readLDThreshold());
		} else if (e.getSource() == dependencyDivisorText) {
			// Message.add("6 DependencyDivesor: M'n focus is weg!");
			parameters.setDependencyDivisor(readDependencyDivisor());
		} else if (e.getSource() == andThresholdText) {
			// Message.add("7 andTreshHoldText: M'n focus is weg!");
			parameters.setAndThreshold(readAndThreshold());
		}

		// Message.add(parameters.toString());
	}

	public void focusGained(FocusEvent e) {
	}

}
