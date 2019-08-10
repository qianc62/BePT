package org.processmining.analysis.socialsuccess.ui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.processmining.analysis.socialsuccess.BigFive;
import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.clustering.KMeans5D;
import org.processmining.analysis.socialsuccess.clustering.KMeansnD;

public class SettingsTab extends JPanel {
	private static final long serialVersionUID = 4621883302072077570L;
	private static final int VALID_INPUT = 1;
	private static final int INVALID_FORMAT = 2;
	private static final int OUT_OF_RANGE = 3;
	protected PersonalityData parent;
	private JComboBox nrOfClustersField;
	private JTextField startDateField;
	private JTextField endDateField;
	private JButton saveButton;
	private JButton calcOptimalButton;

	public SettingsTab(PersonalityData pd) {
		this.parent = pd;
		this.fillScreen();
	}

	private void fillScreen() {
		Vector<String> clusterSizes = new Vector<String>();
		for (int i = 2; i <= KMeansnD.MAX_CLUSTERS; i++) {
			clusterSizes.add(Integer.toString(i));
		}
		// Select the amount of clusters
		JLabel nrOfClustersLabel = new JLabel("Select the amount of clusters:");
		nrOfClustersField = new JComboBox(clusterSizes);
		nrOfClustersField.setSelectedItem(Integer.toString(parent
				.getNrOfClusters()));

		// Enter the begin / end date
		JLabel startDateLabel = new JLabel("Enter begin date (YYYY-mm-dd): ");
		startDateField = new JTextField(parent.getStartTimeString(), 12);
		JLabel minStartDateLabel = new JLabel("At least "
				+ parent.getStartTimeFromDataString());
		minStartDateLabel.setForeground(Color.GRAY);
		JLabel endDateLabel = new JLabel("Enter end date (YYYY-mm-dd): ");
		endDateField = new JTextField(parent.getEndTimeString(), 12);
		JLabel maxEndDateLabel = new JLabel("At max "
				+ parent.getEndTimeFromDataString());
		maxEndDateLabel.setForeground(Color.GRAY);

		// the save button
		saveButton = new JButton("Save");
		saveButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				saveData();
			}
		});

		// calculate clusters
		calcOptimalButton = new JButton("Calculate optimal.");
		calcOptimalButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				calcOptimalNrOfClustersClick();
			}
		});

		// add all to the panel
		SpringLayout layout = new SpringLayout();
		this.setLayout(layout);
		this.add(nrOfClustersLabel);
		this.add(nrOfClustersField);
		this.add(calcOptimalButton);
		this.add(startDateLabel);
		this.add(startDateField);
		this.add(minStartDateLabel);
		this.add(endDateLabel);
		this.add(endDateField);
		this.add(maxEndDateLabel);
		this.add(saveButton);
		// Adjust constraints for the label's
		layout.putConstraint(SpringLayout.WEST, nrOfClustersLabel, 15,
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, nrOfClustersLabel, 15,
				SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, startDateLabel, 15,
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, startDateLabel, 15,
				SpringLayout.SOUTH, nrOfClustersLabel);
		layout.putConstraint(SpringLayout.WEST, endDateLabel, 15,
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, endDateLabel, 15,
				SpringLayout.SOUTH, startDateLabel);
		// Adjust constraints for the fields
		layout.putConstraint(SpringLayout.WEST, nrOfClustersField, 30,
				SpringLayout.EAST, nrOfClustersLabel);
		layout.putConstraint(SpringLayout.NORTH, nrOfClustersField, 15,
				SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, startDateField, 30,
				SpringLayout.EAST, nrOfClustersLabel);
		layout.putConstraint(SpringLayout.NORTH, startDateField, 15,
				SpringLayout.SOUTH, nrOfClustersLabel);
		layout.putConstraint(SpringLayout.WEST, endDateField, 30,
				SpringLayout.EAST, nrOfClustersLabel);
		layout.putConstraint(SpringLayout.NORTH, endDateField, 15,
				SpringLayout.SOUTH, startDateLabel);
		// Adjust the constraints of the extra labels
		layout.putConstraint(SpringLayout.WEST, minStartDateLabel, 30,
				SpringLayout.EAST, endDateField);
		layout.putConstraint(SpringLayout.NORTH, minStartDateLabel, 15,
				SpringLayout.SOUTH, nrOfClustersLabel);
		layout.putConstraint(SpringLayout.WEST, maxEndDateLabel, 30,
				SpringLayout.EAST, endDateField);
		layout.putConstraint(SpringLayout.NORTH, maxEndDateLabel, 15,
				SpringLayout.SOUTH, startDateLabel);
		// Locate the save button
		layout.putConstraint(SpringLayout.WEST, saveButton, 15,
				SpringLayout.EAST, nrOfClustersLabel);
		layout.putConstraint(SpringLayout.NORTH, saveButton, 30,
				SpringLayout.SOUTH, endDateField);
		// Locate the save button
		layout.putConstraint(SpringLayout.WEST, calcOptimalButton, 15,
				SpringLayout.EAST, nrOfClustersField);
		layout.putConstraint(SpringLayout.NORTH, calcOptimalButton, 15,
				SpringLayout.NORTH, this);
	}

	private int validateInput() {
		// als het een geldig datum formaat is, dan kan deze herkend worden
		SimpleDateFormat fd = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date newStartDate = fd.parse(startDateField.getText());
			Date newEndDate = fd.parse(endDateField.getText());
			// we substract one day
			Calendar startTime = Calendar.getInstance();
			startTime.setTime(parent.getStartTimeFromData());
			startTime.add(Calendar.DATE, -1);
			// this to fix a problem with a start which falls in hours before
			// the
			// startdate in in the data file
			if (newStartDate.before(startTime.getTime())
					|| newEndDate.after(parent.getEndTimeFromData()))
				return OUT_OF_RANGE;
			else
				return VALID_INPUT;
		} catch (ParseException e) {
			// Dus geen geldig data formaat
			return INVALID_FORMAT;
		}
	}

	private void saveData() {
		if (validateInput() == VALID_INPUT) {
			parent.setNrOfClusters(Integer.parseInt((String) nrOfClustersField
					.getSelectedItem()));
			parent.setStartTime(startDateField.getText());
			parent.setEndTime(endDateField.getText());
			JOptionPane.showMessageDialog(this, "The values are updated.",
					"Values are saved", JOptionPane.INFORMATION_MESSAGE);
		} else if (validateInput() == INVALID_FORMAT) {
			JOptionPane
					.showMessageDialog(
							this,
							"The dates should be in the YYYY-MM-DD format, this is currently not the case, the values are NOT saved.",
							"Invalid Input", JOptionPane.INFORMATION_MESSAGE);
		} else if (validateInput() == OUT_OF_RANGE) {
			JOptionPane
					.showMessageDialog(
							this,
							"The dates should be in the range of the dates in the file, this is currently not the case, the values are NOT saved.",
							"Invalid Input", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	protected void calcOptimalNrOfClustersClick() {
		BigFive bigFive = new BigFive(parent);
		KMeans5D k = new KMeans5D(0, bigFive.getResults());
		int optK = k.getOptimalNrOfClusters();
		parent.setNrOfClusters(optK);
		nrOfClustersField.setSelectedItem(Integer.toString(optK));
	}

}
