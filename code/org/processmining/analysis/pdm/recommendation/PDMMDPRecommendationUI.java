package org.processmining.analysis.pdm.recommendation;

import org.processmining.analysis.recommendation.RecommendationCollection;
import org.processmining.analysis.recommendation.LogBasedRecommendationUI; //import org.processmining.analysis.recommendation.MyTableModel;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.processmining.framework.models.pdm.*;

import org.processmining.analysis.log.scale.*;
import org.processmining.analysis.recommendation.contrib.*;
import org.processmining.framework.log.*;
import org.processmining.framework.models.recommendation.*;
import org.processmining.framework.models.recommendation.net.*;
import org.processmining.framework.plugin.*;
import org.processmining.framework.remote.*;
import org.processmining.framework.ui.*;
import org.processmining.framework.util.*;
import javax.swing.table.AbstractTableModel;
import org.processmining.analysis.*;

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
public class PDMMDPRecommendationUI extends JPanel {
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JButton startButton = new JButton("Start Server");
	private JButton stopButton = new JButton("Stop Server");
	// private ToolTipComboBox scales = new ToolTipComboBox();
	// private Choice contributors = new Choice();
	private JTextField portField = new JTextField("  4444   ");
	private JLabel portLabel = new JLabel("Port Number:");
	private JPanel servicePanel = new JPanel(new BorderLayout());
	private JPanel topPanel = new JPanel();
	private JPanel buttonPanel = new JPanel(new FlowLayout());
	private JPanel portPanel = new JPanel(new FlowLayout());
	private JProgressBar waitBar = new JProgressBar();
	private JTextArea queryPane = new JTextArea();
	private JTextArea resultsPane = new JTextArea();
	private JButton showQueryButton = new JButton("show");
	private MyTableModel tableData = new MyTableModel();
	private DoubleClickTable queryTable = new DoubleClickTable(tableData,
			showQueryButton);
	// private JButton calculateStrategyButton = new
	// JButton("Calculate MDP stragegy");

	private JCheckBox writeResults = new JCheckBox("Show XML Results", true);

	private Service svc;

	/*
	 * public String getSelectedStrategy() { String selectedStrategy;
	 * selectedStrategy = contributors.getSelectedItem(); return
	 * selectedStrategy; }
	 */
	public PDMMDPRecommendationUI(final PDMStateSpace statespace,
			final RecommendationProvider provider) {
		setLayout(new BorderLayout());

		{ // Strategy selection part
			JPanel contribPanel = new JPanel(new GridLayout(2, 1));
			// contribPanel.add(new JLabel("Select optimization property"));
			// contributors.setPreferredSize(new Dimension(200,
			// (int) scales.getPreferredSize().getHeight()));
			// contributors.add("Minimize cost");
			// contributors.add("Minimize processing time");
			// contributors.add("Maximize cost");
			// contributors.add("Maximize processing time");
			// contribPanel.add(contributors);
			// topPanel.add(contribPanel);
		}
		{
			portPanel.add(portLabel);
			portPanel.add(portField);
			portField.setPreferredSize(new Dimension(50, (int) portField
					.getPreferredSize().getHeight()));
			try {
				portPanel.add(new JLabel(" @ IP: "
						+ InetAddress.getLocalHost().getHostAddress()));
			} catch (UnknownHostException ex) {
				// IP could not be found, no big deal
			}
			servicePanel.add(portPanel, BorderLayout.NORTH);
			servicePanel.add(buttonPanel, BorderLayout.CENTER);
		}

		/*
		 * { //calculate strategy button
		 * buttonPanel.add(calculateStrategyButton);
		 * calculateStrategyButton.addActionListener(new ActionListener() {
		 * public void actionPerformed(ActionEvent e) { if
		 * (getSelectedStrategy().equals("Minimize cost")){ Vector strategy =
		 * statespace.calculateStrategyMinCost(); } if
		 * (getSelectedStrategy().equals("Minimize processing time")){ Vector
		 * strategy = statespace.calculateStrategyMinTime(); }
		 * calculateStrategyButton.setEnabled(false);
		 * stopButton.setEnabled(false); startButton.setEnabled(true);
		 * servicePanel.validate(); } }); }
		 */
		{ // start button
			buttonPanel.add(startButton);
			startButton.setEnabled(true);
			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					RecommendationServiceHandler handler = new RecommendationServiceHandler(
							provider);
					int port;
					try {
						port = Integer.parseInt(portField.getText());
					} catch (NumberFormatException ex) {
						port = 4444;
						portField.setText("4444");
					}
					svc = new Service(port, handler);
					try {
						Message.add("Start server for recommendation");
						svc.start();
						stopButton.setEnabled(true);
						startButton.setEnabled(false);
						servicePanel.validate();
						waitBar.setIndeterminate(true);
						waitBar.setString("Server running");
						Message.add("Recommendation service started!");
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						Message.add("Recommendation service NOT started!");
						Message.add(ex.getMessage(), Message.ERROR);
						Message.add(ex.toString(), Message.ERROR);
					}
					Message.add("<PDMRecommendations>", Message.TEST);
				}
			});
		}
		{ // end button
			buttonPanel.add(stopButton);
			stopButton.setEnabled(true);
			stopButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					startButton.setEnabled(true);
					stopButton.setEnabled(false);
					servicePanel.validate();
					waitBar.setIndeterminate(false);
					waitBar.setString("Server stopped");
					svc.stop();
					Message.add("Recommendation service stopped!");
					Message.add("</PDMRecommendations>", Message.TEST);
				}
			});
		}
		{
			// writing xml
			writeResults.setEnabled(true);
			buttonPanel.add(writeResults);
		}
		topPanel.add(servicePanel);

		mainPanel.add(topPanel, BorderLayout.NORTH);
		{ // waitbar
			waitBar.setString("Server not running yet");
			waitBar.setStringPainted(true);
			waitBar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
			waitBar.setIndeterminate(false);
			JPanel waitBarPanel = new JPanel();
			waitBarPanel.setOpaque(false);
			waitBarPanel
					.setLayout(new BoxLayout(waitBarPanel, BoxLayout.X_AXIS));
			waitBarPanel.add(Box.createHorizontalGlue());
			waitBarPanel.add(waitBar);
			waitBarPanel.add(Box.createHorizontalGlue());
			mainPanel.add(waitBarPanel, BorderLayout.SOUTH);
		}
		this.add(mainPanel, BorderLayout.NORTH);

		{
			JPanel p1 = new JPanel(new BorderLayout());
			p1.add(new JScrollPane(queryPane));
			queryPane.setEditable(false);

			JPanel p2 = new JPanel(new BorderLayout());
			p2.add(new JScrollPane(resultsPane));
			resultsPane.setEditable(false);

			JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, p1,
					p2);
			split.setDividerLocation(0.5);
			split.setContinuousLayout(true);
			split.setResizeWeight(0.5);
			split.setOneTouchExpandable(true);
			this.add(split, BorderLayout.CENTER);

			JPanel p3 = new JPanel(new BorderLayout());
			JScrollPane sp = new JScrollPane(queryTable);
			p3.add(showQueryButton, BorderLayout.SOUTH);
			p3.add(sp, BorderLayout.CENTER);
			queryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			this.add(p3, BorderLayout.WEST);
		}

		showQueryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = queryTable.getSelectedRow();
				if (row < 0 || row >= queryTable.getRowCount()) {
					return;
				}
				final int queryCaret = tableData.getQueryCaret(row);
				final int resultCaret = tableData.getResultCaret(row);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (queryCaret != -1)
							queryPane.setCaretPosition(queryCaret);
						if (resultCaret != -1)
							resultsPane.setCaretPosition(resultCaret);
					}
				});

			}
		});

	}

	/**
	 * writeResult
	 * 
	 * @param result
	 *            RecommendationResult
	 */
	public void writeResult(final RecommendationResult result) {
		if (writeResults.isSelected()) {
			resultsPane.append(result.toString() + "\n");
			// scroll to the end of the text area
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					tableData.setResultCaret(result.getQueryId(), resultsPane
							.getCaretPosition());
					resultsPane
							.setCaretPosition(resultsPane.getText().length());
				}
			});
		}
	}

	/**
	 * writeResult
	 * 
	 * @param result
	 *            RecommendationResult
	 */
	public void writeQuery(final RecommendationQuery query) {
		if (writeResults.isSelected()) {
			tableData.insertRow(query);
			queryPane.append(query.toString() + "\n");
			// scroll to the end of the text area
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					tableData.setQueryCaret(query.getId(), queryPane
							.getCaretPosition());
					queryPane.setCaretPosition(queryPane.getText().length());
				}
			});
		}
	}

}
