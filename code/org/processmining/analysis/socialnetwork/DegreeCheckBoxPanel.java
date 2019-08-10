package org.processmining.analysis.socialnetwork;

import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

public class DegreeCheckBoxPanel extends JPanel implements ItemListener {
	private JCheckBox[] degreeCentralityOptionsCheckBoxes;
	private SocialNetworkAnalysisUI parentClass;
	private JButton catButton = new JButton("Calculate Degree");
	private JButton closeButton = new JButton("Close");
	private boolean[] selectedDegreeOptions;
	private JInternalFrame internalFrame = null;

	public DegreeCheckBoxPanel(SocialNetworkAnalysisUI parentClass,
			boolean[] selectedDegreeOptions) {
		this.parentClass = parentClass;
		this.selectedDegreeOptions = selectedDegreeOptions;
		degreeCentralityOptionsCheckBoxes = new JCheckBox[SocialNetworkAnalysisUI.DEGREE_CENTRALITY_OPTIONS.length];
		for (int i = 0; i < degreeCentralityOptionsCheckBoxes.length; i++) {
			degreeCentralityOptionsCheckBoxes[i] = new JCheckBox(
					SocialNetworkAnalysisUI.DEGREE_CENTRALITY_OPTIONS[i]);
			degreeCentralityOptionsCheckBoxes[i].addItemListener(this);
			degreeCentralityOptionsCheckBoxes[i]
					.setSelected(this.selectedDegreeOptions[i]);
		}

		// Put the check boxes in a column in a panel
		JPanel checkPanel = new JPanel(new GridLayout(0, 1));
		for (int i = 0; i < degreeCentralityOptionsCheckBoxes.length; i++) {
			checkPanel.add(degreeCentralityOptionsCheckBoxes[i]);
		}
		add(checkPanel);
		add(catButton);
		add(closeButton);

		catButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPerformed();
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClosed();
			}
		});
	}

	public void setInternalFrame(JInternalFrame internalFrame) {
		this.internalFrame = internalFrame;
	}

	public void setSelectedDegreeOptions(boolean[] selectedDegreeOptions) {
		this.selectedDegreeOptions = selectedDegreeOptions;
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		int index;
		for (index = 0; index < degreeCentralityOptionsCheckBoxes.length; index++) {
			if (source == degreeCentralityOptionsCheckBoxes[index]) {
				break;
			}
		}
		// Now that we know which button was pushed, find out
		// whether it was selected or deselected.
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			selectedDegreeOptions[index] = false;
			if (index == 3)
				degreeCentralityOptionsCheckBoxes[2].setEnabled(true);
		} else {
			selectedDegreeOptions[index] = true;
			if (index == 3)
				degreeCentralityOptionsCheckBoxes[2].setEnabled(false);
		}

	}

	public void doPerformed() {
		parentClass.calculateDegree();
	}

	public void doClosed() {
		parentClass.closeAdvancedFrame(internalFrame);
	}
}
