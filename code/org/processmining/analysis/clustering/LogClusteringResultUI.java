package org.processmining.analysis.clustering;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.OpenLogSettings;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.mining.DummyMiningPlugin;
import org.processmining.mining.DummyMiningResult;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

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
public class LogClusteringResultUI extends JPanel implements ActionListener,
		Provider {

	public MiningResult result = null;
	LogClusteringEngine engine = null;

	private JSplitPane splitPane = null;
	private JPanel bodyPanel = new JPanel();
	private JScrollPane leftScrPane = new JScrollPane();
	private JScrollPane rightScrPane = new JScrollPane();

	private JPanel diagramPanel = null;
	private JPanel piListPanel = new JPanel();

	private JPanel startPanel = new JPanel();
	private JButton jbPreview = new JButton("Update Dendrogram");
	private JPanel ratioSimPanel = new JPanel();
	private JLabel ratioSimLabel = new JLabel("  similarity  ");
	private JComboBox compCombo = new JComboBox();
	private int comparison = 0;
	private double defaultSim = 0.0;
	private JTextField ratioSimText = null;

	JScrollPane jp = new JScrollPane();

	public LogClusteringResultUI(MiningResult result,
			LogClusteringEngine engine, int comparison, double defaultSim) {

		this.engine = engine;
		this.defaultSim = defaultSim;
		this.diagramPanel = engine.getAHCDiagram(0, defaultSim);

		compCombo.addItem(">");
		compCombo.addItem("<");

		if (comparison == 0) {
			compCombo.setSelectedIndex(0);
		} else {
			compCombo.setSelectedIndex(1);
		}
		ratioSimText = new JTextField("" + defaultSim);

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public ProvidedObject[] getProvidedObjects() {
		// pass provided objects of result variable
		return result instanceof Provider ? ((Provider) result)
				.getProvidedObjects() : new ProvidedObject[0];
	}

	private void jbInit() throws Exception {

		// Layout
		this.setLayout(new BorderLayout());
		bodyPanel.setLayout(new BorderLayout());
		piListPanel.setLayout(new BorderLayout());

		// overall composition
		this.add(bodyPanel, BorderLayout.CENTER);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrPane,
				rightScrPane);
		splitPane.setDividerLocation(900);
		// splitPane.setAutoscrolls(false);
		splitPane.setLastDividerLocation(900);
		// splitPane.setResizeWeight(0.5);
		// splitPane.setContinuousLayout(true);
		// splitPane.setOneTouchExpandable(true);
		bodyPanel.add(splitPane, BorderLayout.CENTER);

		leftScrPane.getViewport().add(diagramPanel);
		rightScrPane.getViewport().add(piListPanel);

		visualizePIListPanel();
		jbPreview.addActionListener(this);

		compCombo.setSize(10, 0);

		ratioSimPanel.setLayout(new GridLayout());
		ratioSimPanel.setSize(50, 0);
		ratioSimPanel.add(ratioSimLabel);
		ratioSimPanel.add(compCombo);
		ratioSimPanel.add(ratioSimText);

		// startPanel.setLayout(new BorderLayout());
		startPanel.add(ratioSimPanel, BorderLayout.WEST);
		startPanel.add(jbPreview, BorderLayout.CENTER);
		this.add(startPanel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == jbPreview) {

			leftScrPane = null;
			diagramPanel = null;
			diagramPanel = engine.getAHCDiagram(compCombo.getSelectedIndex(),
					Double.valueOf(ratioSimText.getText()));
			leftScrPane = new JScrollPane(diagramPanel);

			/*
			 * rightScrPane = null; piListPanel = new JPanel(new
			 * BorderLayout()); piListPanel.add(new JScrollPane(jTable1));
			 */
			splitPane.setLeftComponent(leftScrPane);
			// splitPane.setRightComponent(rightScrPane);
			splitPane.setDividerLocation(1100);
			splitPane.validate();
			splitPane.repaint();
		}
	}

	OpenLogSettings frame;
	LogReader log;
	JComponent dummyVis;
	JTable jTable1;

	private static final MiningPlugin algorithm = new DummyMiningPlugin();

	public void visualizePIListPanel() {
		JInternalFrame[] iframe = MainUI.getInstance().getDesktop()
				.getAllFrames();
		for (int i = 0; i < iframe.length; i++) {
			if (iframe[i] != null && iframe[i] instanceof OpenLogSettings) {
				frame = (OpenLogSettings) iframe[i];
				break;
			}
		}
		log = frame.getSelectedLogReader();

		SwingWorker worker = new SwingWorker() {

			public Object construct() {
				try {
					MainUI.getInstance().addAction(algorithm,
							LogStateMachine.START, new Object[] { log });
					synchronized (log) {
						result = (DummyMiningResult) algorithm.mine(log);
					}
					return result;
				} catch (OutOfMemoryError err) {
					Message.add("Out of memory while mining");
					result = null;
					return null;
				}
			}

			public JComponent findTable(JComponent com) {
				Component[] c = com.getComponents();
				JTable jTable = null;
				for (int i = 0; i < c.length; i++) {
					if (c[i] instanceof JTable) {
						jTable = (JTable) c[i];
						// Message.add("FOUND TABLE");
						return jTable;
					} else if (c[i] instanceof JComponent) {
						JComponent c2 = findTable((JComponent) c[i]);
						if (c2 instanceof JTable) {
							return (JTable) c2;
						}
					}
				}
				return new JPanel();
			}

			public void finished() {
				if (result != null) {

					piListPanel.removeAll();
					dummyVis = result.getVisualization();
					jTable1 = (JTable) findTable(dummyVis);
					piListPanel.add(new JScrollPane(jTable1),
							BorderLayout.CENTER);
					piListPanel.setPreferredSize(new Dimension(100, 0));
					piListPanel.validate();
					piListPanel.repaint();
				}
				setEnabled(true);
			}
		};
		worker.start();

	}

}
