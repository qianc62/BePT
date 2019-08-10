package org.processmining.analysis.clustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

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
public class LogClusteringUI extends JPanel implements ActionListener,
		MiningResult, Provider {

	private LogReader log = null;
	private LogClusteringPlugin plugin = null;
	private LogClusteringEngine engine = null;

	// FROM
	private JCheckBoxMenuItem checkMenu;
	// protected HeuristicsNet net;

	private JSplitPane splitter = null;
	private JPanel graphPanel = null;
	private JPanel descriptionPanel = null;
	private boolean showSplitJoinSemantics = false;

	public JComponent getVisualization() {

		buildPanels();
		showIndividual();

		return splitter;
	}

	public LogReader getLogReader() {
		return log;
	}

	private void buildPanels() {

		graphPanel = new JPanel(new BorderLayout());
		graphPanel.setBackground(Color.YELLOW);

		descriptionPanel = new JPanel(new BorderLayout());
		descriptionPanel.setBackground(Color.YELLOW);

		splitter = new JSplitPane();
		splitter.setContinuousLayout(true);
		splitter.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitter.setTopComponent(graphPanel);
		splitter.setBottomComponent(descriptionPanel);
		splitter.setOneTouchExpandable(true);
		splitter.setResizeWeight(1.0);

		checkMenu = new JCheckBoxMenuItem("Display split/join semantics");
		checkMenu.setSelected(showSplitJoinSemantics);

		checkMenu.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showSplitJoinSemantics = (e.getStateChange() == e.SELECTED);
				showIndividual();
			}
		});
	}

	/*
	 * public ModelGraphPanel getGrappaVisualization() { ModelGraphPanel p = new
	 * DuplicateTasksHeuristicsNetModelGraph(this,
	 * false).getGrappaVisualization(); p.setOriginalObject(this); return p; }
	 * 
	 * public ModelGraphPanel getGrappaVisualizationWithSplitJoinSemantics() {
	 * ModelGraphPanel p = new DuplicateTasksHeuristicsNetModelGraph(this,
	 * true).getGrappaVisualization(); p.setOriginalObject(this); return p; }
	 */

	private GrappaAdapter grappaAdapter = new GrappaAdapter() {

		/**
		 * The method is called when a mouse press occurs on a displayed
		 * subgraph. The returned menu is added to the end of the default
		 * right-click menu
		 * 
		 * @param subg
		 *            displayed subgraph where action occurred
		 * @param elem
		 *            subgraph element in which action occurred
		 * @param pt
		 *            the point where the action occurred (graph coordinates)
		 * @param modifiers
		 *            mouse modifiers in effect
		 * @param panel
		 *            specific panel where the action occurred
		 */
		protected JMenuItem getCustomMenu(Subgraph subg, Element elem,
				GrappaPoint pt, int modifiers, GrappaPanel panel) {
			return checkMenu;
		}
	};

	public JPanel getGraphPanel() {
		return graphPanel;
	}

	private void showIndividual() {

		JScrollPane scrollPane = null;
		JTextArea text = null;
		ModelGraphPanel gp = null;

		// graph representation
		if (this.showSplitJoinSemantics) {
			// gp = getGrappaVisualizationWithSplitJoinSemantics();

		} else {
			// gp = getGrappaVisualization();
		}
		gp.addGrappaListener(grappaAdapter);

		scrollPane = new JScrollPane(gp);
		graphPanel.removeAll();
		graphPanel.add(scrollPane, BorderLayout.CENTER);
		// internal description

		// scrollPane = new JScrollPane();
		text = new JTextArea("aaaaaaaaaaaaaaaaaaaaa", 15, 40);
		text.setEditable(false);

		descriptionPanel.removeAll();
		descriptionPanel.add(new JScrollPane(text), BorderLayout.CENTER);

		graphPanel.validate();
		graphPanel.repaint();

		descriptionPanel.validate();
		descriptionPanel.repaint();
	}

	// To

	public LogClusteringUI(LogReader log, LogClusteringPlugin plugin) {

		this.log = log;
		this.plugin = plugin;
		this.engine = new LogClusteringEngine(log);

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[0];
		if (log != null) {
			LogFilter filter = log.getLogFilter();
			objects = new ProvidedObject[] { new ProvidedObject("Log reader",
					new Object[] { log }) };
		}
		return objects;
	}

	private double defaultSim = 0.0;

	// overall
	private JPanel bodyPanel = new JPanel();
	private JPanel leftPanel = new JPanel();
	private JPanel rightPanel = new JPanel();
	private JScrollPane leftScrPane = new JScrollPane();
	private JScrollPane rightScrPane = new JScrollPane();
	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			leftScrPane, rightScrPane);

	private JPanel summaryPanel = new JPanel();
	private JPanel settingPanel = new JPanel();
	private JPanel startPanel = new JPanel();
	private JPanel logSeqPanel = new JPanel();
	private JPanel logSeqTablePanel = new JPanel();
	private JPanel logActTablePanel = new JPanel();

	// summaryPanel
	private JPanel jp0 = new JPanel();
	private JPanel similarityPanel = new JPanel();
	private JPanel complexityPanel = new JPanel();
	private JPanel ratioSimPanel = new JPanel();
	private JTextField ratioSimText = new JTextField();

	// settingPanel
	private JPanel settingBodyPanel = new JPanel();
	private JPanel selectSimTypePanel = new JPanel();
	private JPanel simTypePanelGrid = new JPanel();
	private JPanel simMeasurePanelGrid = new JPanel();
	private JPanel selectAlgorithmPanelGrid = new JPanel();
	private JPanel selectFrequencyPanelGrid = new JPanel();
	private JPanel simTypeRatioPanel = new JPanel();

	private JPanel diagramSettingPanel = new JPanel();

	// button
	private ButtonGroup simTypeGroup = new ButtonGroup();
	private JRadioButton actTypeButton = null;
	private JRadioButton tranTypeButton = null;
	private JRadioButton totalTypeButton = null;

	private ButtonGroup simMeasureGroup = new ButtonGroup();
	private JRadioButton jaccardButton = null;
	private JRadioButton cosineButton = null;

	private ButtonGroup algTypeGroup = new ButtonGroup();
	private JRadioButton KMeansButton = null;
	private JRadioButton AHCButton = null;
	private JRadioButton ROCKButton = null;

	private JLabel simTypeRatioLabel = null;
	private JLabel tranTypeLabel = null;
	private JLabel kLabel = null;
	private JLabel thresholdLabel = null;
	private JLabel neighborLabel = null;
	private JTextField simTypeRatioText = new JTextField();
	private JComboBox tranTypeCombo = new JComboBox();
	private JComboBox kCombo = new JComboBox();
	private JComboBox compCombo = new JComboBox();
	private JTextField thresholdText = new JTextField();
	private JTextField neighborText = new JTextField();
	private JTextField compSimText = new JTextField("" + 0.0);

	private JCheckBox termFrequencyButton = null;
	private JCheckBox inverseFrequencyButton = null;

	// startPanel
	private JButton jbStart = null;
	private JButton jbHelp = null;

	// general
	private JLabel jLabel = null;
	private EmptyBorder border0 = new EmptyBorder(0, 0, 0, 0);
	private EmptyBorder border5 = new EmptyBorder(5, 5, 5, 5);
	private EmptyBorder border10 = new EmptyBorder(10, 10, 10, 10);
	private LineBorder line = new LineBorder(Color.GRAY, 1);
	private Font f12 = new Font("Dialog", Font.PLAIN, 12);
	private java.text.DecimalFormat dformat3 = new java.text.DecimalFormat(
			"###.######");

	private void jbInit() throws Exception {

		// Layout
		this.setLayout(new BorderLayout());
		bodyPanel.setLayout(new BorderLayout());
		leftPanel.setLayout(new BorderLayout());
		summaryPanel.setLayout(new BorderLayout());
		similarityPanel.setLayout(new BorderLayout());
		complexityPanel.setLayout(new BorderLayout());
		settingPanel.setLayout(new BorderLayout());
		settingBodyPanel.setLayout(new BorderLayout());
		selectSimTypePanel.setLayout(new BorderLayout());
		rightPanel.setLayout(new BorderLayout());
		logSeqPanel.setLayout(new BorderLayout());
		logSeqTablePanel.setLayout(new BorderLayout());
		logActTablePanel.setLayout(new BorderLayout());
		jp0.setLayout(new BorderLayout());
		simTypePanelGrid.setLayout(new GridLayout(3, 2));
		simMeasurePanelGrid.setLayout(new GridLayout(1, 2));
		selectAlgorithmPanelGrid.setLayout(new GridLayout(4, 2));
		selectFrequencyPanelGrid.setLayout(new GridLayout(2, 1));
		diagramSettingPanel.setLayout(new FlowLayout());

		// Border
		bodyPanel.setBorder(border5);
		summaryPanel.setBorder(border10);
		settingPanel.setBorder(border10);
		startPanel.setBorder(border10);
		logSeqPanel.setBorder(border10);
		logSeqTablePanel.setBorder(border5);
		logActTablePanel.setBorder(border5);
		// selectSimTypePanel.setBorder(border5);
		simTypePanelGrid.setBorder(new TitledBorder("Similarity type"));
		simMeasurePanelGrid.setBorder(new TitledBorder("Similarity measure"));
		selectAlgorithmPanelGrid.setBorder(new TitledBorder(
				"Clustering algorithm"));
		selectFrequencyPanelGrid
				.setBorder(new TitledBorder("Frequency weights"));

		// **overall composition
		this.add(bodyPanel, BorderLayout.CENTER);
		splitPane.setDividerLocation(400);
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		bodyPanel.add(splitPane, BorderLayout.CENTER);

		leftScrPane.getViewport().add(settingPanel);
		// leftScrPane.getViewport().add(leftPanel);
		// leftPanel.add(summaryPanel, BorderLayout.NORTH);
		// leftPanel.add(settingPanel, BorderLayout.CENTER);

		rightScrPane.getViewport().add(rightPanel);
		rightPanel.add(logSeqPanel, BorderLayout.CENTER);
		rightPanel.add(diagramSettingPanel, BorderLayout.SOUTH);

		// **startPanel Composition
		jbHelp = new JButton("Help");
		jbStart = new JButton("Start Clustering");
		startPanel.add(jbHelp);
		startPanel.add(jbStart);

		// **summaryPanel Composition
		summaryPanel.add(similarityPanel, BorderLayout.CENTER);
		summaryPanel.add(complexityPanel, BorderLayout.SOUTH);

		// similarityPanel
		jLabel = new JLabel("Overall Similarity of Process Log");
		similarityPanel.add(jp0, BorderLayout.NORTH);
		similarityPanel.add(getSimTablePane(), BorderLayout.CENTER);
		jp0.add(jLabel, BorderLayout.CENTER);
		// ////jp0.add(ratioSimPanel, BorderLayout.EAST);
		ratioSimPanel.setSize(50, 0);
		ratioSimPanel.add(new Label("ratio (\u03B1) = "));
		ratioSimPanel.add(ratioSimText);

		// complexityPanel
		jLabel = new JLabel("Overall Complexity of Process Log");
		complexityPanel.add(jLabel, BorderLayout.NORTH);
		complexityPanel.add(getCompleixityTablePane(), BorderLayout.CENTER);

		// **settingPanel Composition
		jLabel = new JLabel("Setting for Process Log Clustering");
		settingPanel.add(jLabel, BorderLayout.NORTH);
		settingPanel.add(settingBodyPanel, BorderLayout.CENTER);
		settingPanel.add(startPanel, BorderLayout.SOUTH);
		settingBodyPanel.add(selectSimTypePanel, BorderLayout.NORTH);
		settingBodyPanel.add(selectAlgorithmPanelGrid, BorderLayout.CENTER);
		settingBodyPanel.add(selectFrequencyPanelGrid, BorderLayout.SOUTH);

		// selectSimTypePanel composition
		// selectSimTypePanel.add(new JLabel("Similarity type"),
		// BorderLayout.NORTH);
		selectSimTypePanel.add(simTypePanelGrid, BorderLayout.CENTER);
		selectSimTypePanel.add(simMeasurePanelGrid, BorderLayout.SOUTH);

		actTypeButton = new JRadioButton("activity similarity");
		tranTypeButton = new JRadioButton("transition similarity");
		totalTypeButton = new JRadioButton("weighted sum");
		simTypeGroup.add(actTypeButton);
		simTypeGroup.add(tranTypeButton);
		simTypeGroup.add(totalTypeButton);
		simTypeRatioLabel = new JLabel("ratio (\u03B1) = ");
		simTypeRatioLabel.setFont(f12);
		simTypeRatioPanel.add(simTypeRatioLabel, BorderLayout.WEST);
		simTypeRatioPanel.add(simTypeRatioText, BorderLayout.CENTER);

		JPanel jp1 = new JPanel(new FlowLayout());
		tranTypeLabel = new JLabel("");
		tranTypeLabel.setFont(f12);
		jp1.add(tranTypeLabel);
		tranTypeCombo.addItem("explicit transition");
		tranTypeCombo.addItem("implicit transition");
		tranTypeCombo.setFont(f12);
		jp1.add(tranTypeCombo);

		simTypePanelGrid.add(actTypeButton);
		simTypePanelGrid.add(new JLabel(""));
		simTypePanelGrid.add(tranTypeButton);
		simTypePanelGrid.add(jp1);
		simTypePanelGrid.add(totalTypeButton);
		simTypePanelGrid.add(simTypeRatioPanel);

		jaccardButton = new JRadioButton("Jaccard coefficient");
		cosineButton = new JRadioButton("Cosine measure");
		simMeasureGroup.add(jaccardButton);
		simMeasureGroup.add(cosineButton);
		simMeasurePanelGrid.add(jaccardButton);
		simMeasurePanelGrid.add(cosineButton);

		termFrequencyButton = new JCheckBox("Activity/Transition frequency");
		inverseFrequencyButton = new JCheckBox("Inverse process frequency");
		selectFrequencyPanelGrid.add(termFrequencyButton);
		selectFrequencyPanelGrid.add(inverseFrequencyButton);

		// selectAlgorithmPanel composition
		KMeansButton = new JRadioButton("K-means algorithm");
		AHCButton = new JRadioButton("AHC algorithm");
		ROCKButton = new JRadioButton("ROCK algorithm");
		algTypeGroup.add(KMeansButton);
		algTypeGroup.add(AHCButton);
		algTypeGroup.add(ROCKButton);

		JPanel jp2 = new JPanel(new FlowLayout());
		EmptyBorder leftBd = new EmptyBorder(0, 20, 0, 0);
		jp2.setBorder(leftBd);

		kLabel = new JLabel("# of Cluster (K) ");
		kLabel.setFont(f12);
		jp2.add(kLabel);
		kCombo.addItem(new Integer(2));
		kCombo.addItem(new Integer(3));
		kCombo.addItem(new Integer(4));
		kCombo.addItem(new Integer(5));
		kCombo.setFont(f12);
		jp2.add(kCombo);

		JPanel jp3 = new JPanel(new FlowLayout());
		jp3.setBorder(leftBd);
		thresholdLabel = new JLabel("threshold (\u03B8) = ");
		thresholdLabel.setFont(f12);
		jp3.add(thresholdLabel);
		jp3.add(thresholdText);

		JPanel jp4 = new JPanel(new FlowLayout());
		jp4.setBorder(leftBd);
		neighborLabel = new JLabel("neighbor est.(f) = ");
		neighborLabel.setFont(f12);
		jp4.add(neighborLabel);
		jp4.add(neighborText);

		selectAlgorithmPanelGrid.add(AHCButton);
		selectAlgorithmPanelGrid.add(new JLabel(""));
		selectAlgorithmPanelGrid.add(KMeansButton);
		selectAlgorithmPanelGrid.add(jp2);
		selectAlgorithmPanelGrid.add(ROCKButton);
		selectAlgorithmPanelGrid.add(jp3);
		selectAlgorithmPanelGrid.add(new JLabel(""));
		selectAlgorithmPanelGrid.add(jp4);

		// **Right Side Composition
		logSeqPanel.add(logActTablePanel, BorderLayout.NORTH);
		logSeqPanel.add(logSeqTablePanel, BorderLayout.CENTER);

		// logSeqTablePanel.setSize(200, 0);
		logActTablePanel.setSize(200, 0);

		jLabel = new JLabel("Log Sequences");
		logSeqTablePanel.add(jLabel, BorderLayout.NORTH);
		logSeqTablePanel.add(engine.getProcTablePane(), BorderLayout.CENTER);
		jLabel = new JLabel("Activity Descriptions");
		logActTablePanel.add(jLabel, BorderLayout.NORTH);
		logActTablePanel.add(engine.getActTablePane(), BorderLayout.CENTER);

		// diagramSettingPanel
		diagramSettingPanel.setSize(50, 0);
		diagramSettingPanel.add(new JLabel("similarity"));
		diagramSettingPanel.add(compCombo);
		diagramSettingPanel.add(compSimText);
		compCombo.addItem(">");
		compCombo.addItem("<");
		compCombo.setSize(20, 0);
		compSimText.setColumns(4);

		// diagramPanel.add(new
		// JLabel("Please select a clustering algorithm and press \"Start clustering\""),
		// BorderLayout.CENTER);

		// text alignment & size
		ratioSimText.setHorizontalAlignment(SwingConstants.RIGHT);
		simTypeRatioText.setHorizontalAlignment(SwingConstants.RIGHT);
		thresholdText.setHorizontalAlignment(SwingConstants.RIGHT);
		neighborText.setHorizontalAlignment(SwingConstants.RIGHT);

		ratioSimText.setPreferredSize(new Dimension(35, 20));
		simTypeRatioText.setPreferredSize(new Dimension(35, 20));
		thresholdText.setPreferredSize(new Dimension(35, 20));
		neighborText.setPreferredSize(new Dimension(35, 20));

		// ActionListener
		actTypeButton.addActionListener(this);
		tranTypeButton.addActionListener(this);
		totalTypeButton.addActionListener(this);
		AHCButton.addActionListener(this);
		KMeansButton.addActionListener(this);
		ROCKButton.addActionListener(this);
		jbHelp.addActionListener(this);
		jbStart.addActionListener(this);

		initialize();

	}

	public void initialize() {

		// font setting
		/*
		 * actTypeButton.setFont(f12); tranTypeButton.setFont(f12);
		 * totalTypeButton.setFont(f12);
		 * 
		 * jaccardButton.setFont(f12); cosineButton.setFont(f12);
		 * 
		 * KMeansButton.setFont(f12); AHCButton.setFont(f12);
		 * ROCKButton.setFont(f12);
		 */
		// set Text
		simTypeRatioText.setText("0.5");
		thresholdText.setText("0.3");
		neighborText.setText("0.5");
		ratioSimText.setText("0.5");

		// setEnabled
		actTypeButton.setSelected(true);
		cosineButton.setSelected(true);
		AHCButton.setSelected(true);
		tranTypeLabel.setEnabled(false);
		tranTypeCombo.setEnabled(false);
		simTypeRatioLabel.setEnabled(false);
		simTypeRatioText.setEnabled(false);

		kLabel.setEnabled(false);
		kCombo.setEnabled(false);
		thresholdLabel.setEnabled(false);
		thresholdText.setEnabled(false);
		neighborLabel.setEnabled(false);
		neighborText.setEnabled(false);

	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == actTypeButton) {
			tranTypeCombo.setEnabled(false);
			tranTypeLabel.setEnabled(false);
			simTypeRatioText.setEnabled(false);
			simTypeRatioLabel.setEnabled(false);
		} else if (e.getSource() == tranTypeButton) {
			tranTypeCombo.setEnabled(true);
			tranTypeLabel.setEnabled(true);
			simTypeRatioText.setEnabled(false);
			simTypeRatioLabel.setEnabled(false);
		} else if (e.getSource() == totalTypeButton) {
			tranTypeCombo.setEnabled(true);
			tranTypeLabel.setEnabled(true);
			simTypeRatioText.setEnabled(true);
			simTypeRatioLabel.setEnabled(true);
		}

		else if (e.getSource() == AHCButton) {
			kCombo.setEnabled(false);
			kLabel.setEnabled(false);
			thresholdText.setEnabled(false);
			thresholdLabel.setEnabled(false);
			neighborText.setEnabled(false);
			neighborLabel.setEnabled(false);
		} else if (e.getSource() == KMeansButton) {
			kCombo.setEnabled(true);
			kLabel.setEnabled(true);
			thresholdText.setEnabled(false);
			thresholdLabel.setEnabled(false);
			neighborText.setEnabled(false);
			neighborLabel.setEnabled(false);
		} else if (e.getSource() == ROCKButton) {
			thresholdText.setEnabled(true);
			thresholdLabel.setEnabled(true);
			neighborText.setEnabled(true);
			neighborLabel.setEnabled(true);
			kCombo.setEnabled(false);
			kLabel.setEnabled(false);
		}

		else if (e.getSource() == jbHelp) {
			MainUI.getInstance().showReference(plugin);
		}

		else if (e.getSource() == jbStart) {
			// diagramPanel.removeAll();
			Message.add("Start Clustering...");
			int tranType = getTranType(); // if 0, exp; if 1, imp;
			double alpha = getSimTypeRatio(); // if 0, tranSim; if 1, actSim;
			// otherwise, total
			int measureType = getMeasureType(); // if 0, jaccard; if 1, cosine;
			int freqOption = getFreqOption();

			engine.calculateSim(alpha, tranType, measureType, freqOption);

			JPanel diagramPanel = new JPanel();
			diagramPanel.setLayout(new BorderLayout());
			diagramPanel.setBorder(border10);

			if (AHCButton.isSelected()) {
				engine.clusteringByAHC();
			} else if (KMeansButton.isSelected()) {
				engine.clusteringByAHC();
			} else if (ROCKButton.isSelected()) {
				engine.clusteringByAHC();
			}

			/*
			 * JFrame f = new
			 * JFrame("Clustering Result : "+getSettingString(alpha, tranType,
			 * measureType, freqOption)); f.setSize(600, 400);
			 * f.setVisible(true); f.getContentPane().add(diagramPanel,
			 * BorderLayout.CENTER);
			 * 
			 * f.validate(); f.repaint();
			 * 
			 * 
			 * String tmp = "Clustering Result : "+getSettingString(ratio,
			 * tranType, measureType, freqOption)); LogClusteringResult cResult
			 * = new LogClusteringResult(tmp);
			 */

			LogClusteringResultUI f2 = new LogClusteringResultUI(null, engine,
					compCombo.getSelectedIndex(), Double.valueOf(compSimText
							.getText()));
			f2.setSize(600, 400);
			f2.setVisible(true);
			// f2.getContentPane().add(diagramPanel, BorderLayout.CENTER);

			f2.validate();
			f2.repaint();

			engine.setLogClusteringResultUI(f2);

			MainUI.getInstance().createFrame(
					" Result - Log Clustering :: "
							+ getSettingString(alpha, tranType, measureType,
									freqOption), f2);

		}

	}

	public String getSettingString(double ratio, int tranType, int measureType,
			int freqOption) {
		String str = "";
		if (ratio == 0) {
			str += "Transition similarity (";
		} else if (ratio == 1) {
			str += "Activity similarity (";
		} else {
			str += "[Weighted similarity (\u03B1=" + ratio + "), ";
		}

		if (ratio != 1 && tranType == 0) {
			str += "explicit tran., ";
		} else if (ratio != 1 && tranType == 1) {
			str += "implicit tran., ";
		}

		if (measureType == 0) {
			str += "Jaccard coefficient";
		} else {
			str += "Cosine measure";
		}

		if (freqOption == 1) {
			str += ", Inverse frequency weighting)";
		} else {
			str += ")";
		}

		return str;
	}

	public int getTranType() {

		return tranTypeCombo.getSelectedIndex();
	}

	public int getMeasureType() {
		if (jaccardButton.isSelected()) {
			return 0;
		}
		return 1; // if cosine
	}

	public int getFreqOption() {
		if (inverseFrequencyButton.isSelected()) {
			return 1;
		}
		return 0; // if cosine
	}

	public double getSimTypeRatio() {
		if (actTypeButton.isSelected()) {
			return 1.0;
		} else if (tranTypeButton.isSelected()) {
			return 0.0;
		}
		Double d = Double.valueOf(simTypeRatioText.getText().trim());
		return d.doubleValue();
	}

	/*
	 * public String getSummaryString(){ String str = ""; str = str +
	 * "Activity Similarity = "
	 * +dformat3.format(engine.getMeanActSimilarityOfProcLog()); str = str +
	 * "<br>Transition Similarity = "
	 * +dformat3.format(engine.getMeanTranSimilarityOfProcLog()); str = str +
	 * "<br>Complexity = "+dformat3.format(0.0)+"<br>"; return str; }
	 */
	public JScrollPane getSimTablePane() {

		// String[][] str = new String[3][2];
		// String[][] str = {{"activity", "0.7"}, {"explicit transition","0.2"},
		// {"explicit transition","0.2"}};
		JTable table = new JTable(3, 2);
		table.setTableHeader(null);
		table.setBorder(border0);
		table.setFocusable(false);
		table.setValueAt("activity", 0, 0);
		table.setValueAt("explicit transition", 1, 0);
		table.setValueAt("implicit transition", 2, 0);
		SimCalculator simC = engine.getSimCalculator();
		// Loading time saving
		/*
		 * table.setValueAt(""+dformat3.format(simC.getMeanActSimilarity()), 0,
		 * 1);
		 * table.setValueAt(""+dformat3.format(simC.getMeanExpTranSimilarity()),
		 * 1, 1);
		 * table.setValueAt(""+dformat3.format(simC.getMeanImpTranSimilarity()),
		 * 2, 1);
		 */table.setPreferredScrollableViewportSize(new Dimension(0, 48));
		JScrollPane scrollPane = new JScrollPane(table);
		return scrollPane;
	}

	public JScrollPane getCompleixityTablePane() {

		JTable table = new JTable(1, 2);
		table.setTableHeader(null);
		table.setBorder(border0);
		table.setFocusable(false);
		table.setValueAt("compleixty", 0, 0);
		// Loading time saving
		table.setValueAt("0.6", 0, 1);
		table.setPreferredScrollableViewportSize(new Dimension(0, 16));
		JScrollPane scrollPane = new JScrollPane(table);
		return scrollPane;
	}

}
