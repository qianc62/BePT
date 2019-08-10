package org.processmining.analysis.recommendation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.processmining.analysis.log.scale.ProcessInstanceScale;
import org.processmining.analysis.log.scale.ScaleCollection;
import org.processmining.analysis.recommendation.contrib.LogBasedContributor;
import org.processmining.analysis.recommendation.contrib.NoFactoryException;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.recommendation.RecommendationProvider;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.framework.models.recommendation.RecommendationResult;
import org.processmining.framework.models.recommendation.net.RecommendationServiceHandler;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.remote.Service;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.PluginComboItem;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.plugin.ProvidedObject;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

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
public class LogBasedRecommendationUI extends JPanel implements Provider {
	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JButton startButton = new JButton("Start Server");
	private JButton stopButton = new JButton("Stop Server");
	private ToolTipComboBox scales = new ToolTipComboBox();
	private ToolTipComboBox contributors = new ToolTipComboBox();
	private JTextField portField = new JTextField("  4444   ");
	private JLabel portLabel = new JLabel("Port Number:");
	private JPanel servicePanel = new JPanel(new BorderLayout());
	private JPanel topPanel = new JPanel();
	private JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
	private JPanel portPanel = new JPanel(new FlowLayout());
	private LogReader log;

	private JProgressBar waitBar = new JProgressBar();

	private JTextArea queryPane = new JTextArea();
	private JTextArea resultsPane = new JTextArea();
	private JButton showQueryButton = new JButton("show");
	private MyTableModel tableData = new MyTableModel();
	private DoubleClickTable queryTable = new DoubleClickTable(tableData,
			showQueryButton);

	private JCheckBox writeResults = new JCheckBox("Show XML Results", true);
	private JCheckBox ignoreClose = new JCheckBox(
			"Ignore \"close ProM\" message", false);

	private Service svc;
	private LogBasedContributor logBasedContributor;

	public LogBasedContributor getContributor() {
		return logBasedContributor;
	}

	public LogBasedRecommendationUI(final LogReader log,
			final RecommendationProvider provider) {
		this(log, provider, false);
	}

	public LogBasedRecommendationUI(final LogReader log,
			final RecommendationProvider provider,
			final boolean startServerImmediately) {
		addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentShown(ComponentEvent e) {
			}

			public void componentHidden(ComponentEvent e) {
				closeDown();
			}
		});
		setLayout(new BorderLayout());
		this.log = log;
		Iterator<Plugin> it = ScaleCollection.getInstance().getPlugins()
				.iterator();
		while (it.hasNext()) {
			scales.addItem(new PluginComboItem(it.next()));
		}
		it = RecommendationCollection.getInstance().getPlugins().iterator();
		while (it.hasNext()) {
			Plugin p = (Plugin) it.next();
			if (p instanceof LogBasedContributor) {
				contributors.addItem(new PluginComboItem(p));
			}
		}

		topPanel.setLayout(new FlowLayout());
		JPanel selPanel = new JPanel(new GridLayout(4, 1));
		{ // Scale part
			selPanel.add(new JLabel("Select scale"));
			scales.setPreferredSize(new Dimension(200, (int) scales
					.getPreferredSize().getHeight()));
			selPanel.add(scales);
		}
		{ // Contributor part
			selPanel.add(new JLabel("Select contributor"));
			contributors.setPreferredSize(new Dimension(200, (int) scales
					.getPreferredSize().getHeight()));
			selPanel.add(contributors);
		}
		topPanel.add(selPanel);
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

		{ // start button
			buttonPanel.add(startButton);
			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					logBasedContributor = (LogBasedContributor) ((PluginComboItem) contributors
							.getSelectedItem()).getPlugin();

					ProcessInstanceScale scale = (ProcessInstanceScale) ((PluginComboItem) scales
							.getSelectedItem()).getPlugin();
					// We only deal with log based recommendations for now
					try {
						logBasedContributor.initialize(log, scale);
					} catch (NoFactoryException ex2) {
						Message.add(ex2.toString(), Message.ERROR);
					}

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
						Message.add("Recommendation serivce started!");
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						Message.add("Recommendation serivce NOT started!");
						Message.add(ex.getMessage(), Message.ERROR);
						Message.add(ex.toString(), Message.ERROR);
					}
				}
			});
		}
		{ // end button
			buttonPanel.add(stopButton);
			stopButton.setEnabled(false);
			stopButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					startButton.setEnabled(true);
					stopButton.setEnabled(false);
					servicePanel.validate();
					waitBar.setIndeterminate(false);
					waitBar.setString("Server stopped");
					svc.stop();
					Message.add("Recommendation serivce stopped!");
				}
			});
		}
		{
			// writing xml
			writeResults.setEnabled(true);
			buttonPanel.add(writeResults);
			ignoreClose.setEnabled(true);
			buttonPanel.add(ignoreClose);
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
						if (queryCaret != -1) {
							queryPane.setCaretPosition(queryCaret);
						}
						if (resultCaret != -1) {
							resultsPane.setCaretPosition(resultCaret);
						}
					}
				});

			}
		});
		if (startServerImmediately) {
			startButton.doClick();
			writeResults.setSelected(false);
		}
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

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject(
				"Log file used for recommendations", new Object[] { log }) };
	}

	public void requestRestart(String contributor, String scaleName)
			throws Exception {
		for (int i = 0; i < contributors.getItemCount(); i++) {
			LogBasedContributor contributor_i = (LogBasedContributor) ((PluginComboItem) contributors
					.getItemAt(i)).getPlugin();

			if (contributor_i.getClass().getName().equals(contributor)) {
				contributors.setSelectedIndex(i);
				this.logBasedContributor = contributor_i;
				break;
			}
		}
		ProcessInstanceScale scale = null;
		for (int i = 0; i < scales.getItemCount(); i++) {
			ProcessInstanceScale scale_i = (ProcessInstanceScale) ((PluginComboItem) scales
					.getItemAt(i)).getPlugin();

			if (scale_i.getClass().getName().equals(scaleName)) {
				scales.setSelectedIndex(i);
				scale = scale_i;
				break;
			}
		}

		// We only deal with log based recommendations for now
		try {
			logBasedContributor.initialize(log, scale);
		} catch (NoFactoryException ex2) {
			Message.add(ex2.toString(), Message.ERROR);
		}

	}

	public void closeDown() {
		stopButton.doClick();

	}

	public boolean shouldKillProM() {
		return !ignoreClose.isSelected();
	}
}

class MyTableModel extends AbstractTableModel {

	private ArrayList<String[]> queries = new ArrayList();
	private HashMap<String, Integer[]> carets = new HashMap();

	public MyTableModel() {
	}

	public String getColumnName(int col) {
		if (col == 0) {
			return "Process";
		} else if (col == 1) {
			return "Case";
		} else {
			return "Query";
		}
	}

	public int getRowCount() {
		return queries.size();
	}

	public int getColumnCount() {
		return 3;
	}

	public Object getValueAt(int row, int column) {
		return queries.get(row)[column];
	}

	public int getQueryCaret(int row) {
		return carets.get(queries.get(row)[2])[0].intValue();
	}

	public int getResultCaret(int row) {
		return carets.get(queries.get(row)[2])[1].intValue();
	}

	public void setResultCaret(String queryID, int caret) {
		int queryCaret = carets.get(queryID)[0].intValue();
		carets.put(queryID, new Integer[] { queryCaret, caret });
	}

	public void setQueryCaret(String queryID, int caret) {
		carets.put(queryID, new Integer[] { caret, -1 });
	}

	public void insertRow(RecommendationQuery query) {
		queries.add(new String[] { query.getProcessId(),
				query.getProcessInstanceId(), query.getId() });
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableRowsInserted(queries.size() - 1, queries.size() - 1);
			}
		});
	}

}
