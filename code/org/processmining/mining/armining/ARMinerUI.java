package org.processmining.mining.armining;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.log.LogSummary;

/**
 * <p>
 * Title: ARMinerUI
 * </p>
 * 
 * <p>
 * Description:Builds the Input GUI
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
 * @author Shaifali Gupta (s.gupta@student.tue.nl)
 * @version 1.0
 */
public class ARMinerUI extends JPanel {

	private AssociationAnalyzer analyzer = new AprioriAnalyzer();
	private AssociationAnalyzer p_Analyzer = new PredictiveAprioriAnalyzer();

	private JPanel myPanel = new JPanel();
	private JPanel myContentPanel = new JPanel();
	private JPanel selectAlgorithmPanel = new JPanel();
	private JPanel aprioriDescriptionPanel = new JPanel();
	private JPanel aprioriParametersPanel = new JPanel();
	private JPanel p_AprioriDescriptionPanel = new JPanel();
	private JPanel p_AprioriParametersPanel = new JPanel();
	private JComboBox algorithmToUseComboBox = new JComboBox();

	public ARMinerUI(LogSummary summary) {
	}

	public ARMinerUI() {
		jbInit();
	}

	// return the combo box's current selection made by the user (type of
	// algorithm)
	public AssociationAnalyzer getAnalyzerObject() {
		AssociationAnalyzer currentlySelectedAlgorithm = (AssociationAnalyzer) algorithmToUseComboBox
				.getSelectedItem();
		currentlySelectedAlgorithm.resetAssociator();
		return currentlySelectedAlgorithm;
	}

	// GUI for the input
	private void HowARMinerLooks() {
		// OUTERMOST PANEL-myPanel
		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.LINE_AXIS));
		myContentPanel.setLayout(new BoxLayout(myContentPanel,
				BoxLayout.PAGE_AXIS));
		myContentPanel.add(Box.createVerticalGlue());
		// ----------------------------------------------------------------------------------------------------------------
		// Choose which algorithm-Apriori or PredictiveApriori
		selectAlgorithmPanel.setLayout(new BoxLayout(selectAlgorithmPanel,
				BoxLayout.LINE_AXIS));
		JLabel algorithmToUseLabel = new JLabel("Use algorithm:");
		// add items to combo box- Apriori algorithm and predictive apriori
		// algorithm
		algorithmToUseComboBox.addItem(analyzer);
		algorithmToUseComboBox.addItem(p_Analyzer);
		algorithmToUseComboBox.setMaximumSize(algorithmToUseComboBox
				.getPreferredSize());

		selectAlgorithmPanel.add(algorithmToUseLabel);
		selectAlgorithmPanel.add(Box.createRigidArea(new Dimension(67, 0)));
		selectAlgorithmPanel.add(algorithmToUseComboBox);
		selectAlgorithmPanel.add(Box.createHorizontalGlue());
		myContentPanel.add(selectAlgorithmPanel);
		myContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// ---------------------------------------------------------------------------------------------------------------------------
		// For Apriori Algorithm-----
		// 1st--->
		// Panel for describing the Apriori algorithm-
		aprioriDescriptionPanel.setLayout(new BoxLayout(
				aprioriDescriptionPanel, BoxLayout.X_AXIS));
		// Inner Panel-descriptionPanel
		JPanel descriptionPanel = new JPanel();
		descriptionPanel.setLayout(new BoxLayout(descriptionPanel,
				BoxLayout.X_AXIS));
		JLabel descriptionLabel = new JLabel("Algorithm description: ");
		JLabel descriptionContent = new JLabel(analyzer.getDescription());
		descriptionPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		descriptionPanel.add(descriptionLabel);
		descriptionPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		descriptionPanel.add(descriptionContent);
		descriptionPanel.add(Box.createHorizontalGlue());
		aprioriDescriptionPanel.add(descriptionPanel);

		// 2nd--->
		// Panel for Apriori Parameters-
		aprioriParametersPanel.setLayout(new BoxLayout(aprioriParametersPanel,
				BoxLayout.Y_AXIS));
		// Inner Panel-aprioriParametersLabelPanel
		JPanel aprioriParametersLabelPanel = new JPanel();
		aprioriParametersLabelPanel.setLayout(new BoxLayout(
				aprioriParametersLabelPanel, BoxLayout.X_AXIS));
		JLabel aprioriParametersLabel = new JLabel(
				"The following parameters are available:");
		aprioriParametersLabelPanel.add(aprioriParametersLabel);
		aprioriParametersLabelPanel.add(Box.createHorizontalGlue());
		aprioriParametersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		aprioriParametersPanel.add(aprioriParametersLabelPanel);
		aprioriParametersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		aprioriParametersPanel.add(analyzer.getParametersPanel());
		aprioriParametersPanel.add(Box.createVerticalGlue());

		// ----add both 1st and 2nd (apriori panels) to the main content panel
		// i.e. myContentPanel
		myContentPanel.add(aprioriDescriptionPanel);
		myContentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
		myContentPanel.add(aprioriParametersPanel);
		myContentPanel.add(Box.createHorizontalGlue());
		myContentPanel.setBorder(BorderFactory
				.createEmptyBorder(30, 30, 30, 30));

		// ---------------------------------------------------------------------------------------------------------------------------
		// For Predictive Apriori algorithm----
		// 1st--->
		// Panel for describing the Predictive Apriori algorithm-
		p_AprioriDescriptionPanel.setLayout(new BoxLayout(
				p_AprioriDescriptionPanel, BoxLayout.X_AXIS));
		// Inner Panel-descriptionPanel
		JPanel p_DescriptionPanel = new JPanel();
		p_DescriptionPanel.setLayout(new BoxLayout(p_DescriptionPanel,
				BoxLayout.X_AXIS));
		JLabel p_DescriptionLabel = new JLabel("Algorithm description: ");
		JLabel p_DescriptionContent = new JLabel(p_Analyzer.getDescription());
		p_DescriptionPanel.add(p_DescriptionLabel);
		p_DescriptionPanel.add(Box.createRigidArea(new Dimension(8, 0)));
		p_DescriptionPanel.add(p_DescriptionContent);
		p_DescriptionPanel.add(Box.createHorizontalGlue());
		p_AprioriDescriptionPanel.add(p_DescriptionPanel);

		// 2nd--->
		p_AprioriParametersPanel.setLayout(new BoxLayout(
				p_AprioriParametersPanel, BoxLayout.Y_AXIS));
		// Inner Panel-p_ParametersPanel
		JPanel p_ParametersPanel = new JPanel();
		p_ParametersPanel.setLayout(new BoxLayout(p_ParametersPanel,
				BoxLayout.LINE_AXIS));
		JLabel p_ParametersLabel = new JLabel("Available parameter is:");
		p_ParametersPanel.add(p_ParametersLabel);
		p_ParametersPanel.add(Box.createHorizontalGlue());
		p_AprioriParametersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		p_AprioriParametersPanel.add(p_ParametersPanel);
		p_AprioriParametersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		p_AprioriParametersPanel.add(p_Analyzer.getParametersPanel());
		// ------------------------------------------------------------------------------------------------------------
		algorithmToUseComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				AssociationAnalyzer newAssociationAlgorithm = (AssociationAnalyzer) cb
						.getSelectedItem();

				if (newAssociationAlgorithm == analyzer) {
					myContentPanel.removeAll();
					myContentPanel.add(selectAlgorithmPanel);
					myContentPanel.add(Box
							.createRigidArea(new Dimension(0, 20)));
					myContentPanel.add(aprioriDescriptionPanel);
					myContentPanel.add(aprioriParametersPanel);
				} else {
					myContentPanel.removeAll();
					myContentPanel.add(selectAlgorithmPanel);
					myContentPanel.add(Box
							.createRigidArea(new Dimension(0, 20)));
					myContentPanel.add(p_AprioriDescriptionPanel);
					myContentPanel.add(p_AprioriParametersPanel);
				}

				myContentPanel.validate();
				myContentPanel.repaint();
			}
		});

		myPanel.add(Box.createHorizontalGlue());
		myPanel.add(myContentPanel);
		myPanel.add(Box.createHorizontalGlue());
		myPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
		this.add(myPanel);
	}

	/**
	 * jbInit
	 */
	private void jbInit() {
		HowARMinerLooks();
	}

}
