package org.processmining.mining.tsmining;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.processmining.framework.log.InfoItem;
import org.processmining.framework.log.LogSummary;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class TSGeneratorOptionsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LogSummary summary;

	private JLabel topLabel;
	private JRadioButton jBasicAlgoSetsButton;
	private JRadioButton jBasicAlgoBagsButton;
	private JCheckBox jExtendStrategyButton;

	// private JLabel jActivityLogMiningLabel;
	// private JLabel jDocumentLogMiningLabel;
	private JCheckBox jEventTypesButton;
	private JCheckBox jStateNamesButton;
	private JCheckBox jLogTimestampsButton;
	private JCheckBox jExplicitEndButton;
	private JCheckBox jKillLoopsButton;

	public TSGeneratorOptionsPanel(LogSummary summary) {
		this.summary = summary;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(new BorderLayout());
		this.setSize(100, 200);

		topLabel = new JLabel("<html>The current log contains "
				+ summary.getNumberOfProcessInstances()
				+ " process instances, <br>" + "with "
				+ summary.getNumberOfAuditTrailEntries()
				+ " audit trail entries <br>refering to "
				+ summary.getLogEvents().size() + " activities.</html>");
		this.add(topLabel, BorderLayout.NORTH);

		JPanel mainPanel = new JPanel();
		GridBagLayout aLayout = new GridBagLayout();
		mainPanel.setLayout(aLayout);

		jBasicAlgoSetsButton = new JRadioButton();
		jBasicAlgoSetsButton
				.setText("<html>Generate Transition System with Sets<br>( Basic Algorithm )</html>");
		jBasicAlgoSetsButton.setSelected(true);
		mainPanel.add(jBasicAlgoSetsButton, new GridBagConstraints(0, 0, 1, 1,
				0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(15, 15, 25, 25), 1, 0));
		jBasicAlgoBagsButton = new JRadioButton();
		jBasicAlgoBagsButton
				.setText("<html>Generate Transition System with Bags<br>( Basic Algorithm )</html>");
		jBasicAlgoBagsButton.setSelected(false);
		mainPanel.add(jBasicAlgoBagsButton, new GridBagConstraints(0, 1, 1, 1,
				0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(15, 15, 25, 25), 1, 0));

		jExtendStrategyButton = new JCheckBox();
		jExtendStrategyButton
				.setText("<html>Extend Strategy<br>( Algorithm adds Additional Transitions )</html>");
		jExtendStrategyButton.setSelected(false);
		mainPanel.add(jExtendStrategyButton, new GridBagConstraints(1, 0, 1, 1,
				0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(15, 15, 25, 25), 0, 0));
		ButtonGroup generatorGroupSetsBags = new ButtonGroup();
		generatorGroupSetsBags.add(jBasicAlgoSetsButton);
		generatorGroupSetsBags.add(jBasicAlgoBagsButton);

		jLogTimestampsButton = new JCheckBox();
		jLogTimestampsButton.setText("The Log has Timestamps.");
		// jLogTimestampsButton.setEnabled(false);
		InfoItem[] prs = summary.getProcesses();
		Date dt = summary.getStartTime(prs[0].getName());
		if (dt == null)
			jLogTimestampsButton.setSelected(false);
		else
			jLogTimestampsButton.setSelected(true);
		mainPanel.add(jLogTimestampsButton, new GridBagConstraints(0, 2, 2, 1,
				0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(15, 15, 25, 25), 0, 0));

		jEventTypesButton = new JCheckBox();
		jEventTypesButton.setText("Consider Event Types.");
		jEventTypesButton.setSelected(false);
		mainPanel.add(jEventTypesButton, new GridBagConstraints(0, 3, 1, 1, 0,
				0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(15, 15, 25, 25), 0, 0));

		jStateNamesButton = new JCheckBox();
		jStateNamesButton.setText("Use IDs (numbers) as State Names.");
		jStateNamesButton.setSelected(false);
		mainPanel.add(jStateNamesButton, new GridBagConstraints(1, 3, 1, 1, 0,
				0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(15, 15, 25, 25), 0, 0));

		jKillLoopsButton = new JCheckBox();
		jKillLoopsButton.setText("Kill Loops.");
		jKillLoopsButton.setSelected(true);
		mainPanel.add(jKillLoopsButton, new GridBagConstraints(0, 4, 2, 1, 0,
				0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(15, 15, 25, 25), 0, 0));

		jExplicitEndButton = new JCheckBox();
		jExplicitEndButton.setText("Add Explicit End State.");
		jExplicitEndButton.setSelected(true);
		mainPanel.add(jExplicitEndButton, new GridBagConstraints(1, 4, 1, 1, 0,
				0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(15, 15, 25, 25), 0, 0));

		this.add(mainPanel, BorderLayout.CENTER);
	}

	public boolean isBasicTransitionSystemSets() {
		return jBasicAlgoSetsButton.isSelected();
	}

	public boolean isBasicTransitionSystemBags() {
		return jBasicAlgoBagsButton.isSelected();
	}

	public boolean isEventTypes() {
		return jEventTypesButton.isSelected();
	}

	public boolean isIDNames() {
		return jStateNamesButton.isSelected();
	}

	public boolean haveTimestamps() {
		return jLogTimestampsButton.isSelected();
	}

	public boolean isExplicitEnd() {
		return jExplicitEndButton.isSelected();
	}

	public boolean isKillLoops() {
		return jKillLoopsButton.isSelected();
	}

	public boolean useExtendedStrategy() {
		return jExtendStrategyButton.isSelected();
	}
}
