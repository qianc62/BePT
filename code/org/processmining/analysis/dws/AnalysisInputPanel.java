package org.processmining.analysis.dws;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Gathers the input parameters used by the DWS analysis plugin.
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 */

public class AnalysisInputPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	// private LogSummary summary;
	private JLabel titolo;
	private JPanel firstPanel;
	private JPanel mainPanel;

	private JTextField sigma;
	private JLabel testoLabel1;
	private JTextField gamma;
	private JLabel testoLabel2;
	private JTextField kField;
	private JLabel testoLabel3;
	private JTextField lField;
	private JLabel testoLabel4;
	private JLabel testoLabel5;
	private JTextField maxFeatField;

	public AnalysisInputPanel() {

		// this.summary = summary;
		titolo = new JLabel();
		mainPanel = new JPanel();
		firstPanel = new JPanel();

		sigma = new JTextField();
		testoLabel1 = new JLabel();
		gamma = new JTextField();
		testoLabel2 = new JLabel();
		kField = new JTextField();
		testoLabel3 = new JLabel();
		lField = new JTextField();
		testoLabel4 = new JLabel();
		maxFeatField = new JTextField();
		testoLabel5 = new JLabel();

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getK() {
		try {
			return (Integer.parseInt(kField.getText()));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public int getFeatureLength() {
		try {
			return (Integer.parseInt(lField.getText()));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public float getSigma() {
		try {
			return (Float.parseFloat(sigma.getText()));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public float getGamma() {
		try {
			return (Float.parseFloat(gamma.getText()));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public int getMaxFeat() {
		try {
			return (Integer.parseInt(maxFeatField.getText()));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private void jbInit() throws Exception {
		int x = 0, y = 0;
		testoLabel1.setText("Frequency support - sigma  ");
		testoLabel2.setText("Feature relevance threshold - gamma ");
		testoLabel3.setText("(Max.) Number of clusters per split");
		testoLabel4.setText("(Max.) Lenght of features");
		testoLabel5.setText("(Max.) Number of features");

		titolo.setText(" ");
		sigma.setText("0.05");
		sigma.setPreferredSize(new Dimension(40, 21));
		sigma.setMinimumSize(new Dimension(6, 21));
		gamma.setText("0.01");
		gamma.setPreferredSize(new Dimension(40, 21));
		gamma.setMinimumSize(new Dimension(6, 21));

		kField.setText("4");
		kField.setPreferredSize(new Dimension(40, 21));
		kField.setMinimumSize(new Dimension(6, 21));

		lField.setText("5");
		lField.setPreferredSize(new Dimension(40, 21));
		lField.setMinimumSize(new Dimension(6, 21));

		maxFeatField.setText("2");
		maxFeatField.setPreferredSize(new Dimension(40, 21));
		maxFeatField.setMinimumSize(new Dimension(6, 21));

		firstPanel.setLayout(new GridBagLayout());

		firstPanel.add(testoLabel1, new GridBagConstraints(0, x + y, 1, 1,
				0.0D, 0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));
		firstPanel.add(sigma, new GridBagConstraints(1, x + y++, 1, 1, 0.0D,
				0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));
		firstPanel.add(testoLabel2, new GridBagConstraints(0, x + y, 1, 1,
				0.0D, 0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));
		firstPanel.add(gamma, new GridBagConstraints(1, x + y++, 1, 1, 0.0D,
				0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));
		firstPanel.add(testoLabel3, new GridBagConstraints(0, x + y, 1, 1,
				0.0D, 0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));
		firstPanel.add(kField, new GridBagConstraints(1, x + y++, 1, 1, 0.0D,
				0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));
		firstPanel.add(testoLabel4, new GridBagConstraints(0, x + y, 1, 1,
				0.0D, 0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));
		firstPanel.add(lField, new GridBagConstraints(1, x + y++, 1, 1, 0.0D,
				0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));
		firstPanel.add(testoLabel5, new GridBagConstraints(0, x + y, 1, 1,
				0.0D, 0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));
		firstPanel.add(maxFeatField, new GridBagConstraints(1, x + y++, 1, 1,
				0.0D, 0.0D, 17, 0, new Insets(0, 33, 0, 0), 0, 0));

		mainPanel.setLayout(new FlowLayout());
		mainPanel.add(firstPanel); // ,BorderLayout.CENTER);
		mainPanel.add(titolo, BorderLayout.NORTH);

		setLayout(new FlowLayout());
		add(mainPanel); // ,BorderLayout.NORTH);
	}
}
