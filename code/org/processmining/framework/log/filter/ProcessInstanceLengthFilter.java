package org.processmining.framework.log.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;

public class ProcessInstanceLengthFilter extends LogFilter {
	protected int lowerLimitThreshold, upperLimitThreshold;

	public ProcessInstanceLengthFilter() {
		this(0, Integer.MAX_VALUE);
	}

	public ProcessInstanceLengthFilter(int lowerLimitThreshold,
			int upperLimitThreshold) {
		super(LogFilter.MODERATE, "Process Instance Length filter");
		this.lowerLimitThreshold = lowerLimitThreshold;
		this.upperLimitThreshold = upperLimitThreshold;
	}

	protected boolean doFiltering(ProcessInstance instance) {
		// System.out.println("Threshold: "+threshold);
		return (instance.getAuditTrailEntryList().size() < upperLimitThreshold && instance
				.getAuditTrailEntryList().size() > lowerLimitThreshold);
	}

	@Override
	protected String getHelpForThisLogFilter() {
		return "Removes process instances whose length is above/below a given threshold";
	}

	@Override
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				ProcessInstanceLengthFilter.this) {
			int lowerLimitLengthThreshold, upperLimitLengthThreshold;
			private JTextField eventLengthLowerLimitField,
					eventLengthUpperLimitField;

			public LogFilter getNewLogFilter() {
				return new ProcessInstanceLengthFilter(Integer
						.parseInt(eventLengthLowerLimitField.getText()),
						Integer.parseInt(eventLengthUpperLimitField.getText()));
			}

			protected JPanel getPanel() {
				eventLengthLowerLimitField = new JTextField(0 + "");
				eventLengthLowerLimitField.setEditable(true);

				eventLengthUpperLimitField = new JTextField(summary
						.getNumberOfAuditTrailEntries()
						+ "");
				eventLengthUpperLimitField.setEditable(true);

				JPanel p = new JPanel(new GridBagLayout());
				JLabel lengthLowerLimitLabel = new JLabel(
						"Min No. Events (Threshold)      ");
				JLabel lengthUpperLimitLabel = new JLabel(
						"Max No. Events (Threshold)      ");

				p.add(lengthLowerLimitLabel, new GridBagConstraints(0, 0, 1, 1,
						0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
				p.add(lengthUpperLimitLabel, new GridBagConstraints(0, 1, 1, 1,
						0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));

				eventLengthLowerLimitField.setPreferredSize(new Dimension(
						(int) lengthLowerLimitLabel.getPreferredSize()
								.getWidth(), (int) eventLengthLowerLimitField
								.getPreferredSize().getHeight()));
				p.add(eventLengthLowerLimitField, new GridBagConstraints(1, 0,
						1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));

				eventLengthUpperLimitField.setPreferredSize(new Dimension(
						(int) lengthUpperLimitLabel.getPreferredSize()
								.getWidth(), (int) eventLengthUpperLimitField
								.getPreferredSize().getHeight()));
				p.add(eventLengthUpperLimitField, new GridBagConstraints(1, 1,
						1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));

				JLabel help = new JLabel(
						"<html><br>All Process Instances with the number of events <br> above the max threshold and below the min threshold would be filtered");
				p.add(help, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(1, 1, 1, 1), 0, 0));
				return p;
			}

			protected boolean getAllParametersSet() {
				return !eventLengthLowerLimitField.getText().equals("")
						&& !eventLengthUpperLimitField.getText().equals("");
			}
		};
	}

	@Override
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean thisFilterChangesLog() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// TODO Auto-generated method stub

	}

}
