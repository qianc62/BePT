package org.processmining.mining.snamining.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PanelSubcontracting extends JPanel {

	private JCheckBox subConsiderMultipleTransfers = new JCheckBox();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private JLabel subDepthLabel = new JLabel();
	private JLabel subBetaLabel = new JLabel();
	private JTextField subDepth = new JTextField();
	private JCheckBox subConsiderDirectSuccession = new JCheckBox();
	private JTextField subBeta = new JTextField();
	private JCheckBox subConsiderCausality = new JCheckBox();
	private JLabel subMultipleTransfersLabel1 = new JLabel();
	private JLabel subMultipleTransfersLabel2 = new JLabel();

	public PanelSubcontracting() {
		init();
	}

	public void init() {
		subMultipleTransfersLabel1.setFont(new java.awt.Font("Dialog", 0, 9));
		subMultipleTransfersLabel1
				.setText("(unchecked: # of instances where SC happened / # of instances");
		subMultipleTransfersLabel2.setFont(new java.awt.Font("Dialog", 0, 9));
		subMultipleTransfersLabel2
				.setText(" checked: # of SCs between originators / # of possible SCs)");
		this.setLayout(gridBagLayout2);
		subConsiderCausality.setText("Consider causality");
		subConsiderCausality.setSelected(true);
		subConsiderMultipleTransfers
				.setText("Consider multiple transfers within one instance");
		subConsiderMultipleTransfers.setSelected(true);
		subDepthLabel.setText("Depth of calculation:");
		subBetaLabel.setText("Beta:");
		subDepth.setText("5");
		subDepth.setPreferredSize(new Dimension(40, 21));
		subConsiderDirectSuccession.setText("Consider only direct subcontract");
		subConsiderDirectSuccession.setSelected(true);
		subBeta.setText("0.5");
		subBeta.setPreferredSize(new Dimension(40, 21));
		subBeta.setMinimumSize(new Dimension(6, 21));

		subBeta.setEnabled(!subConsiderDirectSuccession.isSelected());
		subDepth.setEnabled(!subConsiderDirectSuccession.isSelected());
		subBetaLabel.setEnabled(!subConsiderDirectSuccession.isSelected());
		subDepthLabel.setEnabled(!subConsiderDirectSuccession.isSelected());

		this.add(subConsiderCausality, new GridBagConstraints(0, 0, 2, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(subConsiderMultipleTransfers, new GridBagConstraints(0, 1, 2,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(subConsiderDirectSuccession, new GridBagConstraints(0, 4, 2,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(subBetaLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						40, 0, 0), 0, 0));
		this.add(subDepth, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,
						5, 0, 0), 0, 0));
		this.add(subDepthLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,
						40, 0, 0), 0, 0));
		this.add(subBeta, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						5, 0, 0), 0, 0));
		this.add(subMultipleTransfersLabel1, new GridBagConstraints(0, 2, 2, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 25, 0, 0), 0, 0));
		this.add(subMultipleTransfersLabel2, new GridBagConstraints(0, 3, 2, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 25, 0, 0), 0, 0));
		subConsiderDirectSuccession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				subBeta.setEnabled(!subConsiderDirectSuccession.isSelected());
				subDepth.setEnabled(!subConsiderDirectSuccession.isSelected());
				subBetaLabel.setEnabled(!subConsiderDirectSuccession
						.isSelected());
				subDepthLabel.setEnabled(!subConsiderDirectSuccession
						.isSelected());
			}
		});
	}

	public void initcomponent() {

	}

	private void jbInit() throws Exception {

	}

	public String getDepth() {
		return subDepth.getText();
	}

	public String getBeta() {
		return subBeta.getText();
	}

	public boolean getConsiderCausality() {
		return subConsiderCausality.isSelected();
	}

	public boolean getConsiderMultipleTransfers() {
		return subConsiderMultipleTransfers.isSelected();
	}

	public boolean getConsiderDirectSuccession() {
		return subConsiderDirectSuccession.isSelected();
	}

}
