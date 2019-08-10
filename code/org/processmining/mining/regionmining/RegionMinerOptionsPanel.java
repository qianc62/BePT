package org.processmining.mining.regionmining;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.processmining.framework.log.LogSummary;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
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
 * @author not attributable
 * @version 1.0
 */
public class RegionMinerOptionsPanel extends JPanel {

	private LogSummary summary;

	public RegionMinerOptionsPanel(LogSummary summary) {
		this.summary = summary;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean minimalOnly() {
		return jRadioButton2.isSelected();
	}

	public boolean doPostProcessing() {
		return doCleanupCheckBox.isSelected();
	}

	public int maxSize() {
		return (jRadioButton3.isSelected() ? sm.getNumber().intValue() : 0);
	}

	public boolean nonCompOnly() {
		return jRadioButton4.isSelected();
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		topLabel = new JLabel("<html>The current log contains "
				+ summary.getNumberOfProcessInstances()
				+ " process instances, <br>" + "with "
				+ summary.getNumberOfAuditTrailEntries()
				+ " audit trail entries <br>refering to "
				+ summary.getLogEvents().size() + " activities.</html>");
		setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();

		jPanel1.setLayout(gridLayout1);

		jRadioButton1
				.setText("Use all regions for calculation. This is the default setting.");
		jRadioButton1.setSelected(true);
		group.add(jRadioButton1);

		jRadioButton2.setText("Use only minimal regions for calculation.");
		group.add(jRadioButton2);

		jRadioButton3.setText("Use regions with maximal size: ");
		group.add(jRadioButton3);

		jRadioButton4
				.setText("Use only non-complimentary regions for calculation.");
		group.add(jRadioButton4);

		add(topLabel, BorderLayout.NORTH);
		mainPanel.add(jPanel1);
		add(mainPanel, java.awt.BorderLayout.CENTER);

		jPanel1.add(jRadioButton1, new GridBagConstraints(0, 0, 2, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 1, 0));

		jPanel1.add(jRadioButton2, new GridBagConstraints(0, 1, 2, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 1, 0));

		jPanel1.add(jRadioButton3, new GridBagConstraints(0, 2, 1, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 1, 0));
		jPanel1.add(jSpinner1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));

		jPanel1.add(jRadioButton4, new GridBagConstraints(0, 3, 2, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 1, 0));

		jPanel1.add(doCleanupCheckBox, new GridBagConstraints(0, 4, 2, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 1, 0));

	}

	private BorderLayout borderLayout1 = new BorderLayout();
	private JLabel topLabel;
	private JPanel jPanel1 = new JPanel();
	private GridBagLayout gridLayout1 = new GridBagLayout();

	private JRadioButton jRadioButton1 = new JRadioButton();

	private JRadioButton jRadioButton2 = new JRadioButton();

	private JRadioButton jRadioButton3 = new JRadioButton();

	private JRadioButton jRadioButton4 = new JRadioButton();

	private JCheckBox doCleanupCheckBox = new JCheckBox(
			"<html>Perform post-processing (i.e. regions to <br>Petri net to statespace to regions to Petrinet again)</html>",
			true);

	private ButtonGroup group = new ButtonGroup();
	private SpinnerNumberModel sm = new SpinnerNumberModel(1, 1, 1000, 1);
	private JSpinner jSpinner1 = new JSpinner(sm);

}
