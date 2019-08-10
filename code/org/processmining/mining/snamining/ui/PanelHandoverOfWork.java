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

public class PanelHandoverOfWork extends JPanel {

	private JLabel howMultipleTransfersLabel2 = new JLabel();
	private JLabel howMultipleTransfersLabel1 = new JLabel();
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JCheckBox howConsiderCausality = new JCheckBox();
	private JCheckBox howConsiderMultipleTransfers = new JCheckBox();
	private JCheckBox howConsiderDirectSuccession = new JCheckBox();
	private JLabel howBetaLabel = new JLabel();
	private JTextField howDepth = new JTextField();
	private JLabel howDepthLabel = new JLabel();
	private JTextField howBeta = new JTextField();

	public PanelHandoverOfWork() {
		init();
	}

	private void jbInit() throws Exception {

	}

	private void init() {

		this.setLayout(gridBagLayout);

		howMultipleTransfersLabel1.setFont(new java.awt.Font("Dialog", 0, 9));
		howMultipleTransfersLabel1
				.setText("(unchecked: # of instances where HW happened / # of instances");
		howMultipleTransfersLabel2.setFont(new java.awt.Font("Dialog", 0, 9));
		howMultipleTransfersLabel2
				.setText(" checked: # of HW between originators / # of possible HWs)");
		howConsiderCausality.setText("Consider causality");
		howConsiderCausality.setSelected(true);
		howConsiderMultipleTransfers
				.setText("Consider multiple transfers within one instance");
		howConsiderMultipleTransfers.setSelected(true);
		howConsiderDirectSuccession.setText("Consider only direct succession");
		howConsiderDirectSuccession.setSelected(true);
		howBetaLabel.setText("Beta:");
		howDepth.setPreferredSize(new Dimension(40, 21));
		howDepth.setText("5");
		howDepthLabel.setText("Depth of calculation:");
		howBeta.setMinimumSize(new Dimension(6, 21));
		howBeta.setPreferredSize(new Dimension(40, 21));
		howBeta.setText("0.5");

		howBeta.setEnabled(!howConsiderDirectSuccession.isSelected());
		howDepth.setEnabled(!howConsiderDirectSuccession.isSelected());
		howBetaLabel.setEnabled(!howConsiderDirectSuccession.isSelected());
		howDepthLabel.setEnabled(!howConsiderDirectSuccession.isSelected());

		this.add(howConsiderCausality, new GridBagConstraints(0, 0, 2, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(howConsiderMultipleTransfers, new GridBagConstraints(0, 1, 2,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(howConsiderDirectSuccession, new GridBagConstraints(0, 4, 2,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(howBetaLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						40, 0, 0), 0, 0));
		this.add(howDepth, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,
						5, 0, 0), 0, 0));
		this.add(howDepthLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,
						40, 0, 0), 0, 0));
		this.add(howBeta, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						5, 0, 0), 0, 0));
		this.add(howMultipleTransfersLabel2, new GridBagConstraints(0, 3, 2, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 25, 0, 0), 0, 0));
		this.add(howMultipleTransfersLabel1, new GridBagConstraints(0, 2, 2, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 25, 0, 0), 0, 0));

		howConsiderDirectSuccession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				howBeta.setEnabled(!howConsiderDirectSuccession.isSelected());
				howDepth.setEnabled(!howConsiderDirectSuccession.isSelected());
				howBetaLabel.setEnabled(!howConsiderDirectSuccession
						.isSelected());
				howDepthLabel.setEnabled(!howConsiderDirectSuccession
						.isSelected());
			}
		});
	}

	public String getDepth() {
		return howDepth.getText();
	}

	public String getBeta() {
		return howBeta.getText();
	}

	public boolean getConsiderCausality() {
		return howConsiderCausality.isSelected();
	}

	public boolean getConsiderMultipleTransfers() {
		return howConsiderMultipleTransfers.isSelected();
	}

	public boolean getConsiderDirectSuccession() {
		return howConsiderDirectSuccession.isSelected();
	}

}
