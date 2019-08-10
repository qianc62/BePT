/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.analysis.performance.sequence;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.MiningResult;
import org.processmining.mining.logabstraction.LogRelationBasedAlgorithm;
import org.processmining.mining.logabstraction.LogRelations;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * Class needed to perform performance sequence analysis
 * 
 * @author Peter T.G. Hornix (p.t.g.hornix@student.tue.nl)
 */
public class PerformanceSequenceAnalysis extends JPanel implements Provider {
	// final attributes
	final PerformanceSequenceDiagramPlugin myAlgorithm;
	final AnalysisInputItem[] myInput;
	final LogReader inputLog;
	/**
	 * indices of the process instances used
	 */
	private int[] selectedInstanceIndices;
	/**
	 * ArrayList containing the sequences in the log
	 */
	private ArrayList sequences = new ArrayList();
	/**
	 * Map, where Sequences are keys and Patterns are values
	 */
	private ArrayList patterns = new ArrayList();
	/**
	 * Map, where frequency of occurence is the key and a Set that contains the
	 * patterns that have that frequency of occurence is the value.
	 */
	private HashMap patternMap = new HashMap();
	/**
	 * Logrelations used
	 */
	private LogRelations relations;
	/**
	 * list to store the names of data-elements of the selected
	 * data-element/component type
	 */
	private ArrayList dataElts = new ArrayList();
	/**
	 * The selected data-element type
	 */
	private String dataEltType = "TaskID";
	/**
	 * variables to keep track of the selected time sort
	 */
	private String timeSort = "seconds";
	/**
	 * the time divider
	 */
	private long timeDivider = 1000;
	/**
	 * scale at which diagrams are displayed;
	 */
	private float scale = 1;
	// private float oldScale = 1;
	/**
	 * noise ratio (not used)
	 */
	private double noiseRatio = 0;
	/**
	 * variable that is true is patternDiagram has been selected, else (sequence
	 * diagram selected) false
	 */
	private boolean patternSelected = false;
	/**
	 * variable that contains the data-element that is selected in the combobox
	 */
	private boolean tooltipsOn = true;
	/**
	 * boolean, set to true when the patterndiagram has been drawn
	 */
	private boolean patternDrawn = false;
	/**
	 * Variables to keep track of the position of the mouse
	 */
	private float xPos = 0;
	private float yPos = 0;
	// private double originalTppPattern = 1;
	// private double originalTppFull = 1;
	Date beginTotal = null;
	Date endTotal = null;
	/**
	 * frame which contains filter options screen (if opened)
	 */
	JInternalFrame frame;
	// GUI components
	private JPanel westPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private JPanel sequencePanel = new JPanel();
	private JPanel patternPanel = new JPanel();
	private JPanel metricsPanel = new JPanel();
	private SequenceDiagram sequenceDiagramPanel = new SequenceDiagram();
	private PatternDiagram patternDiagramPanel = new PatternDiagram();
	private JScrollPane sequencePane;
	private JScrollPane patternPane;
	private JScrollPane metricsPane;
	private JTabbedPane tp = new JTabbedPane();
	private JSplitPane sp = new JSplitPane();
	private JSplitPane split = new JSplitPane();
	// labels
	private JLabel optionLabel = new JLabel("Options");
	private JLabel patternTypeLabel = new JLabel("Pattern type:");
	private JLabel timeSortLabel = new JLabel("Time sort:");
	private JLabel componentLabel = new JLabel("Component type:");
	// private JLabel zmLabel = new JLabel("Zoom:");
	private JLabel zoomLabel = new JLabel("Zoom: 100.0%");
	// private JLabel detLabel = new JLabel("Detail:");
	// private JLabel detailLabel = new JLabel("100.0%");
	// comboboxes
	private JComboBox componentBox = new JComboBox();
	// array of "time" strings required to fill the timeBox combobox with
	private String[] timeSorts = { "milliseconds", "seconds", "minutes",
			"hours", "days", "weeks", "months", "years" };
	private long[] dividers = { 1, 1000, 60000, 3600000L, 86400000L,
			604800000L, 2592000000L, 31536000000L };
	private JComboBox timeBox = new JComboBox(timeSorts);
	// radio buttons + group to allow the user to select auto or manual settings
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton flexibleButton = new JRadioButton(
			"Flexible equivalent", true);
	private JRadioButton strictButton = new JRadioButton("Strict equivalent");
	// buttons
	private JButton showButton = new JButton("Show diagram");
	private JButton filterButton = new JButton("Filter options");
	// pop up menu
	private JPopupMenu pop = new JPopupMenu();
	private JMenuItem menuItem0 = new JMenuItem("Zoom in");
	private JMenuItem menuItem1 = new JMenuItem("Zoom out");
	private JMenuItem menuItem2 = new JMenuItem("Original size");
	private JMenuItem menuItem3 = new JMenuItem("Tooltips off");
	private JMenuItem menuItem4 = new JMenuItem("Increase detail");
	private JMenuItem menuItem5 = new JMenuItem("Decrease detail");
	// slider for zooming
	private JSlider scaleSlider = new JSlider(JSlider.VERTICAL, 5, 1000, 100);

	// private JSlider detailSlider = new JSlider(JSlider.VERTICAL, 5, 1000,
	// 100);
	public PerformanceSequenceAnalysis(
			PerformanceSequenceDiagramPlugin algorithm,
			AnalysisInputItem[] input, LogReader log) {
		myAlgorithm = algorithm;
		myInput = input;
		inputLog = log;
		int number = inputLog.getLogSummary().getNumberOfProcessInstances();
		// initially, all instances are selected
		selectedInstanceIndices = new int[number];
		for (int i = 0; i < number; i++) {
			selectedInstanceIndices[i] = i;
		}
		try {
			analyse();
		} catch (Exception ex) {

		}
	}

	/**
	 * Actually builds the GUI
	 */
	private void jbInit() {
		// initialize pop-up menu
		pop.removeAll();
		pop.add(menuItem0);
		pop.add(menuItem1);
		pop.add(menuItem2);
		pop.addSeparator();
		pop.add(menuItem3);
		pop.addSeparator();
		pop.add(menuItem4);
		pop.add(menuItem5);
		// initialize westpanel
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
		westPanel.setPreferredSize(new Dimension(160, 400));
		westPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		optionLabel.setAlignmentX(LEFT_ALIGNMENT);
		optionLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		westPanel.add(optionLabel);
		westPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		componentLabel.setAlignmentX(LEFT_ALIGNMENT);
		westPanel.add(componentLabel);
		componentBox.setMaximumSize(new Dimension(150, 20));
		componentBox.setAlignmentX(LEFT_ALIGNMENT);
		westPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		westPanel.add(componentBox);
		westPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		westPanel.add(timeSortLabel);
		// seconds selected in timeBox
		timeBox.setSelectedIndex(1);
		timeBox.setMaximumSize(new Dimension(150, 20));
		timeBox.setAlignmentX(LEFT_ALIGNMENT);
		westPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		westPanel.add(timeBox);
		westPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		westPanel.add(patternTypeLabel);
		buttonGroup.add(flexibleButton);
		buttonGroup.add(strictButton);
		westPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		westPanel.add(flexibleButton);
		westPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		westPanel.add(strictButton);
		westPanel.add(Box.createRigidArea(new Dimension(5, 15)));
		showButton.setMaximumSize(new Dimension(120, 24));
		showButton.setPreferredSize(new Dimension(120, 24));
		westPanel.add(showButton);
		westPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		filterButton.setMaximumSize(new Dimension(120, 24));
		filterButton.setPreferredSize(new Dimension(120, 24));
		westPanel.add(filterButton);
		scaleSlider.setPaintLabels(false);
		scaleSlider.setPaintTicks(true);
		scaleSlider.setMajorTickSpacing(50);
		// detailSlider.setPaintLabels(false);
		// detailSlider.setPaintTicks(true);
		// detailSlider.setMajorTickSpacing(50);
		westPanel.add(Box.createRigidArea(new Dimension(5, 10)));
		JPanel prettyPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 5;
		scaleSlider.setPreferredSize(new Dimension(80, 150));
		scaleSlider.setMinimumSize(new Dimension(80, 100));
		prettyPanel.add(scaleSlider, gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 5;
		// detailSlider.setPreferredSize(new Dimension(80,150));
		// detailSlider.setMinimumSize(new Dimension(80,100));
		// prettyPanel.add(detailSlider, gbc);
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridheight = 1;
		// prettyPanel.add(zmLabel, gbc);
		// gbc.gridx=1; gbc.gridy=6;gbc.gridheight=1;
		// prettyPanel.add(detLabel, gbc);
		// gbc.gridx=0; gbc.gridy=7;gbc.gridheight=1;
		prettyPanel.add(zoomLabel, gbc);
		// gbc.gridx=1; gbc.gridy=7;gbc.gridheight=1;
		// prettyPanel.add(detailLabel, gbc);
		prettyPanel.setBorder(BorderFactory.createEtchedBorder());
		prettyPanel.setPreferredSize(new Dimension(80, 150));
		prettyPanel.setAlignmentX(LEFT_ALIGNMENT);
		westPanel.add(prettyPanel);
		int height = this.getHeight() - this.getInsets().bottom
				- this.getInsets().top - 50;
		int width = this.getWidth() - this.getInsets().left
				- this.getInsets().right - 200;
		sequencePanel.setMinimumSize(new Dimension(width, height));
		tp.add("Full diagram", sequencePanel);
		patternPanel.setMinimumSize(new Dimension(width, height));
		tp.add("Pattern diagram", patternPanel);
		tp.setSelectedIndex(0);
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(tp, BorderLayout.WEST);
		centerPanel.setMinimumSize(new Dimension(width, height));
		centerPanel.setAlignmentX(LEFT_ALIGNMENT);
		sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPanel, centerPanel);
		sp.setDividerLocation(160);
		sp.setDividerSize(3);
		sp.setOneTouchExpandable(true);
		sequenceDiagramPanel.setBackground(Color.WHITE);
		sequenceDiagramPanel.setMinimumSize(new Dimension(width, height - 10));
		sequenceDiagramPanel.setToolTipText("");
		sequencePane = new JScrollPane(sequenceDiagramPanel);
		sequencePane.setMinimumSize(new Dimension(width, height - 10));
		sequencePane.setPreferredSize(new Dimension(width, height - 10));
		sequencePanel.add(sequencePane);

		patternDiagramPanel.setBackground(Color.WHITE);
		patternDiagramPanel.setMinimumSize(new Dimension(width - 200,
				height - 10));
		patternDiagramPanel.setToolTipText("");
		patternPane = new JScrollPane(patternDiagramPanel);
		patternPane.setPreferredSize(new Dimension(width - 200, height - 10));
		metricsPanel.setPreferredSize(new Dimension(200, height - 10));
		metricsPanel.setMinimumSize(new Dimension(200, height - 10));
		metricsPane = new JScrollPane(metricsPanel);
		metricsPane.setPreferredSize(new Dimension(200, height - 10));
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, patternPane,
				metricsPane);
		split.setDividerLocation(split.getSize().width
				- split.getInsets().right - split.getDividerSize() - 200);

		split.setOneTouchExpandable(true);
		split.setResizeWeight(1.0);
		split.setDividerSize(3);
		split.setOneTouchExpandable(true);
		patternPanel.add(split);

		this.setLayout(new BorderLayout());
		this.add(sp, BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}

	/**
	 * Connects GUI with listener-methods
	 */
	private void registerGUIListener() {
		// add componentlistener, which resizes the panes in which the diagrams
		// are drawn,
		// when the window is resized
		this.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
				// resize panes as well
				int height = getHeight() - getInsets().bottom - getInsets().top
						- 50;
				int width = getWidth() - getInsets().left - getInsets().right
						- 200;
				sequencePane
						.setPreferredSize(new Dimension(width, height - 10));
				patternPane.setPreferredSize(new Dimension(width - 200,
						height - 10));
			}

			public void componentHidden(ComponentEvent e) {
			}

			public void componentShown(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}
		});
		// tabs listener: show right kind of diagram if user has switched tab
		tp.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				patternSelected = !patternSelected;
				if (patternSelected) {
					showPatternDiagram();
				} else {
					showFullDiagram();
				}
			}
		});
		// componentBox listener: set dataElt to the component that is selected
		// in the box
		componentBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataEltType = (String) componentBox.getSelectedItem();
			}
		});
		// timeBox listener: set timeSort to the sort that is selected in the
		// box
		// and set timeDivider to the corresponding divider.
		timeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeSort = (String) timeBox.getSelectedItem();
				timeDivider = dividers[timeBox.getSelectedIndex()];
			}
		});
		// showButton listener: perform the actual analysis and display the
		// selected diagram
		showButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				construct();
				if (patternSelected) {
					showPatternDiagram();
				} else {
					showFullDiagram();
				}
			}
		});
		filterButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the button
			public void actionPerformed(ActionEvent e) {
				// create and show the filter options screen
				createFilterOptionsScreen();
			}
		});
		// connect MouseListener to sequence diagram panel. Pop-up menu will be
		// displayed when the user presses his right mouse button
		sequenceDiagramPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					pop.show(e.getComponent(), e.getX(), e.getY());
					xPos = e.getX();
					yPos = e.getY();
				}
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		});
		// connect MouseListener to pattern diagram panel. Pop-up menu will be
		// displayed when the user presses his right mouse button
		patternDiagramPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					pop.show(e.getComponent(), e.getX(), e.getY());
					xPos = e.getX();
					yPos = e.getY();
				}
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		});
		/**
		 * add actionlistener to menuItem2 to zoom in
		 */
		menuItem0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float oldScale = scale;
				scale = scale * 1.5F;
				if (scale > 10.0F) {
					scale = 10.0F;
				}
				zoom(oldScale);
			}
		});
		/**
		 * add actionlistener to menuItem1 to zoom out
		 */
		menuItem1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float oldScale = scale;
				scale = scale / 1.5F;
				if (scale < 0.05F) {
					scale = 0.05F;
				}
				zoom(oldScale);
			}
		});
		/**
		 * add actionlistener to menuItem2 to zoom to original size
		 */
		menuItem2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float oldScale = scale;
				scale = 1.0F;
				zoom(oldScale);
			}
		});
		/**
		 * add actionlistener to menuItem3 to set tooltips on/off
		 */
		menuItem3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tooltipsOn = !tooltipsOn;
				sequenceDiagramPanel.setTooltipsOn(tooltipsOn);
				patternDiagramPanel.setTooltipsOn(tooltipsOn);
				if (tooltipsOn) {
					menuItem3.setText("Tooltips off");
				} else {
					menuItem3.setText("Tooltips on");
				}
			}
		});
		/**
		 * Increase the detail level by reducing the time per pixel
		 */
		menuItem4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (patternSelected) {
					patternDiagramPanel.setTimePerPixel(patternDiagramPanel
							.getTimePerPixel() * 0.666666666);
					showPatternDiagram();
					patternPane.doLayout();
					int x = Math.max(0, (int) (xPos - 0.5 * patternPane
							.getViewport().getWidth()));
					int y = Math.max(0, (int) (yPos * 1.5 - 0.5 * patternPane
							.getViewport().getHeight()));
					patternPane.getViewport().setViewPosition(new Point(x, y));
				} else {
					sequenceDiagramPanel.setTimePerPixel(sequenceDiagramPanel
							.getTimePerPixel() * 0.666666666);
					showFullDiagram();
					sequencePane.doLayout();
					int x = Math.max(0, (int) (xPos - 0.5 * sequencePane
							.getViewport().getWidth()));
					int y = Math.max(0, (int) (yPos * 1.5 - 0.5 * sequencePane
							.getViewport().getHeight()));
					sequencePane.getViewport().setViewPosition(new Point(x, y));
				}
			}
		});
		/**
		 * Decrease the detail level by increasing the time per pixel
		 */
		menuItem5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (patternSelected) {
					patternDiagramPanel.setTimePerPixel(patternDiagramPanel
							.getTimePerPixel() * 1.5);
					showPatternDiagram();
					int x = Math.max(0, (int) (xPos - 0.5 * patternPane
							.getViewport().getWidth()));
					int y = Math.max(0,
							(int) (yPos * 0.666666666 - 0.5 * patternPane
									.getViewport().getHeight()));
					patternPane.getViewport().setViewPosition(new Point(x, y));
					patternPane.doLayout();
				} else {
					sequenceDiagramPanel.setTimePerPixel(sequenceDiagramPanel
							.getTimePerPixel() * 1.5);
					showFullDiagram();
					int x = Math.max(0, (int) (xPos - 0.5 * sequencePane
							.getViewport().getWidth()));
					int y = Math.max(0,
							(int) (yPos * 0.666666666 - 0.5 * sequencePane
									.getViewport().getHeight()));
					sequencePane.getViewport().setViewPosition(new Point(x, y));
					sequencePane.doLayout();
				}
				// double oldValue = detailSlider.getValue();
				// detailSlider.setValue(((Double) (oldValue *
				// 0.666666666)).intValue());
				// detailLabel.setText((oldValue * 0.666666666) + "%");
			}
		});
		/**
		 * Add ChangeListener to scaleSlider, so it can be used for zooming
		 * purposes
		 */
		scaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float oldScale = scale;
				JPanel currentPanel;
				if (patternSelected) {
					currentPanel = patternDiagramPanel;
				} else {
					currentPanel = sequenceDiagramPanel;
				}
				xPos = Math.round(currentPanel.getVisibleRect().getMinX()
						+ (currentPanel.getVisibleRect().getWidth() * 0.5F));
				yPos = Math
						.round(currentPanel.getVisibleRect().getMinY()
								+ (int) (currentPanel.getVisibleRect()
										.getHeight() * 0.5F));
				DecimalFormat df = new DecimalFormat("0.00");
				scale = scaleSlider.getValue() / 100.0F;
				zoomLabel.setText("Zoom: " + df.format(scale * 100) + "%");
				zoom(oldScale);
			}
		});
		/*
		 * detailSlider.addChangeListener(new ChangeListener() { public void
		 * stateChanged(ChangeEvent e) { JPanel currentPanel; double oldTpp = 1;
		 * if (patternSelected) { currentPanel = patternDiagramPanel; xPos =
		 * Math.round(currentPanel.getVisibleRect().getMinX() +
		 * (currentPanel.getVisibleRect().getWidth() * 0.5F)); yPos =
		 * Math.round(currentPanel.getVisibleRect().getMinY() + (int)
		 * (currentPanel.getVisibleRect().getHeight() * 0.5F)); oldTpp =
		 * patternDiagramPanel.getTimePerPixel();
		 * patternDiagramPanel.setTimePerPixel(originalTppPattern /
		 * (detailSlider.getValue() / 100.0F)); showPatternDiagram();
		 * patternPane.doLayout(); int x = Math.max(0, (int) (xPos - 0.5 *
		 * patternPane.getViewport().getWidth())); int y = Math.max(0, (int)
		 * (yPos * oldTpp/patternDiagramPanel.getTimePerPixel() - 0.5 *
		 * patternPane.getViewport().getHeight()));
		 * patternPane.getViewport().setViewPosition(new Point(Math.round(x),
		 * y)); } else { currentPanel = sequenceDiagramPanel; xPos =
		 * Math.round(currentPanel.getVisibleRect().getMinX() +
		 * (currentPanel.getVisibleRect().getWidth() * 0.5F)); yPos =
		 * Math.round(currentPanel.getVisibleRect().getMinY() + (int)
		 * (currentPanel.getVisibleRect().getHeight() * 0.5F)); oldTpp =
		 * sequenceDiagramPanel.getTimePerPixel();
		 * sequenceDiagramPanel.setTimePerPixel(originalTppFull /
		 * (detailSlider.getValue() / 100.0F)); showFullDiagram();
		 * sequencePane.doLayout(); int x = Math.max(0, (int) (xPos - 0.5 *
		 * sequencePane.getViewport().getWidth())); int y = Math.max(0, (int)
		 * (yPos * oldTpp/sequenceDiagramPanel.getTimePerPixel() - 0.5 *
		 * sequencePane.getViewport().getHeight()));
		 * sequencePane.getViewport().setViewPosition(new Point(x, y)); }
		 * DecimalFormat df = new DecimalFormat("0.00");
		 * detailLabel.setText(df.format(detailSlider.getValue()) + "%");
		 * 
		 * } });
		 */
	}

	/**
	 * Zooms visible diagram to the position of the mouse. oldscale/scale
	 * determines in which direction (in or out) is zoomed
	 * 
	 * @param oldScale
	 *            float
	 */
	private void zoom(float oldScale) {
		// Only allowed to zoom up to 1000%
		if (scale == 10.0F) {
			menuItem0.setEnabled(false);
		} else {
			menuItem0.setEnabled(true);
		}
		// Only allowed to zoom down to 5%
		if (scale == 0.05F) {
			menuItem1.setEnabled(false);
		} else {
			menuItem1.setEnabled(true);
		}
		if (patternSelected) {
			showPatternDiagram();
			patternPane.doLayout();
			int x = Math.max(0,
					(int) (xPos * scale / oldScale - 0.5 * patternPane
							.getViewport().getWidth()));
			int y = Math.max(0,
					(int) (yPos * scale / oldScale - 0.5 * patternPane
							.getViewport().getHeight()));
			patternPane.getViewport().setViewPosition(new Point(x, y));

		} else {
			showFullDiagram();
			int x = Math.max(0,
					(int) (xPos * scale / oldScale - 0.5 * sequencePane
							.getViewport().getWidth()));
			int y = Math.max(0,
					(int) (yPos * scale / oldScale - 0.5 * sequencePane
							.getViewport().getHeight()));
			sequencePane.getViewport().setViewPosition(new Point(x, y));
			sequencePane.doLayout();
		}
		scaleSlider.setValue(Math.round(scale * 100));
	}

	/**
	 * Creates a screen in which filter options can be viewed and adjusted
	 */
	private void createFilterOptionsScreen() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		FilterOptions fo = new FilterOptions(inputLog, selectedInstanceIndices,
				sequences, patterns, timeSort, timeDivider, patternDrawn, this);
		MainUI.getInstance().createFrame("Filter Options", fo);
		if (frame != null) {
			frame.doDefaultCloseAction();
		}
		frame = MainUI.getInstance().getDesktop().getSelectedFrame();
		frame.setSize(new Dimension(700, 400));
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Closes filter option screen (if opened), and sets selectedInstanceIndices
	 * to the indices of the instances that are selected in the option screen
	 * 
	 * @param selectedInstanceIndices
	 *            int[]
	 */
	public void closeFrame(int[] selectedInstanceIndices) {
		if (frame != null) {
			frame.doDefaultCloseAction();
			frame = null;
		}
		if (selectedInstanceIndices != null) {
			this.selectedInstanceIndices = selectedInstanceIndices;
			construct();
			if (patternSelected) {
				showPatternDiagram();
			} else {
				showFullDiagram();
			}
		}
	}

	/**
	 * Initializes the componentBox comboBox
	 */
	private void initializeComponentBox() {
		// still have to reset, even though instanceIterator() is used
		inputLog.reset();
		Iterator log = inputLog.instanceIterator();
		if (log.hasNext()) {
			ProcessInstance pi = (ProcessInstance) log.next();
			try {
				int index = 0;
				AuditTrailEntry ate = pi.getAuditTrailEntryList().get(index++);
				while (ate.getTimestamp() == null
						&& index != pi.getAuditTrailEntryList().size()) {
					// get the first audit trail entry that has a timestamp
					// attached to it
					ate = pi.getAuditTrailEntryList().get(index++);
				}
				if (ate.getTimestamp() == null) {
					// There is no audit trail etry that has a timeStamp
					// attached to it
					componentBox.addItem("None");
				} else {
					// There is an audit trail etry that has a timeStamp
					// attached to it
					componentBox.addItem("Task ID");
					if (ate.getOriginator() != null) {
						// the audit trail entry has an originator attached to
						// it
						componentBox.addItem("Originator");
					}
					// get the other data elements in the audit trail entry
					String[] otherElts = getOtherDataElements(pi);
					for (int i = 0; i < otherElts.length; i++) {
						// add the other data elements to the componentBox
						componentBox.addItem(otherElts[i]);
					}
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * Walks through process instance pi, and returns the data-elements that
	 * appear in it as a sorted array
	 * 
	 * @param pi
	 *            ProcessInstance
	 * @return String[]
	 */
	private String[] getOtherDataElements(ProcessInstance pi) {
		AuditTrailEntryList ates = pi.getAuditTrailEntryList();
		Iterator it = ates.iterator();
		HashSet elts = new HashSet();
		while (it.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) it.next();
			Iterator it2 = ate.getAttributes().keySet().iterator();
			// run through attributes
			while (it2.hasNext()) {
				String tempString = (String) it2.next();
				if (tempString != "") {
					// add tempString to elts if it is not equal to the empty
					// String
					elts.add(tempString);
				}
			}
		}
		// put the data elements in an array
		String[] set = (String[]) elts.toArray(new String[0]);
		// sort the array
		Arrays.sort(set);
		// return the sorted array
		return set;
	}

	/**
	 * Creates thread in which the log relations are mined out of the used log
	 */
	private void analyse() {
		SequenceAnalysisThread thread = new SequenceAnalysisThread(this);
		try {
			thread.start();
		} catch (OutOfMemoryError err) {
			Message.add("Out of memory while analyzing");
		}
	}

	/**
	 * Called when the thread, in which the log relations are mined out of the
	 * log, is done. Calls methods to initialize the GUI
	 */
	public void threadDone() {
		jbInit();
		registerGUIListener();
		initializeComponentBox();
	}

	/**
	 * Mines the log relations out of the log
	 */
	public void mineRelations() {
		LogRelationBasedAlgorithm im = new LogRelationBasedAlgorithm() {
			public MiningResult mine(LogReader logReader,
					LogRelations logRelations, Progress progress) {
				return null;
			}

			public String getName() {
				return "";
			}

			public String getHtmlDescription() {
				return "";
			}
		};
		Progress progress = new Progress("Mining "
				+ inputLog.getFile().getShortName()
				+ " to derive log relations");
		relations = im.getLogRelations(inputLog, progress);
	}

	/**
	 * Replays the log to derive the sequence diagram and the pattern diagram
	 */
	private void construct() {
		// initialize list that is used to keep track of the data-elements that
		// appear in the process log
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		patternDrawn = false;
		Date endDate = null;
		patterns.clear();
		sequences.clear();
		dataElts.clear();
		if (relations != null) {
			DoubleMatrix2D causalMatrix = relations.getCausalFollowerMatrix();
			// still have to reset, even though instanceIterator() is used
			inputLog.reset();
			LogEvents events = inputLog.getLogSummary().getLogEvents();
			int index = 0, selectedIndex = 0;
			Iterator instances = inputLog.instanceIterator();
			while (instances.hasNext()) {
				// run through process instances
				ProcessInstance pi = (ProcessInstance) instances.next(); // myLog.getInstance(selectedInstanceIndices[index]);
				// only use selected process instances
				if (selectedIndex < selectedInstanceIndices.length
						&& index++ == selectedInstanceIndices[selectedIndex]) {
					selectedIndex++;
					// initialize list that is used to store relations between
					// ates
					ArrayList relationList = new ArrayList();
					// initialize hashmap that maps event numbers to the ates
					// that have that number
					HashMap numberToAteMap = new HashMap();
					// initialize list that is used to store the event-numbers
					// in the order they
					// appear in the process instance
					ArrayList ateNumbers = new ArrayList();
					// obtain audit trail entries of the pi
					AuditTrailEntryList ates = pi.getAuditTrailEntryList();
					Iterator it = ates.iterator();
					// initialize variables
					Date firstDate = null;
					int number = 0;
					while (it.hasNext()) {
						// run through audit trail entries
						AuditTrailEntry ate = (AuditTrailEntry) it.next();
						// obtain the event number belonging to the ate
						int eventNumber = events.findLogEventNumber(ate
								.getElement(), ate.getType());
						// keep track of order of appearance of event numbers in
						// each trace
						ateNumbers.add(number, eventNumber);
						// keep track of which ates belong to each eventnumber
						ArrayList belongingAtes = new ArrayList();
						if (numberToAteMap.get(eventNumber) != null) {
							// number already occurred
							belongingAtes = (ArrayList) numberToAteMap
									.get(eventNumber);
						}
						belongingAtes.add(ate);
						numberToAteMap.put(eventNumber, belongingAtes);
						boolean noPrecessor = true;
						for (int i = 0; i < number; i++) {
							// obtain event numbers that occured before
							int num = ((Integer) ateNumbers.get(i)).intValue(); // ((Integer)
							// tasks.next()).intValue();
							if (!((relations.getOneLengthLoopsInfo().get(
									eventNumber) > 0) && num == eventNumber)) {
								if (causalMatrix.get(num, eventNumber) > noiseRatio
										* inputLog.numberOfInstances()) {
									// Check if num is the closest causal
									// precessor of eventNumber
									boolean isClosest = true;
									noPrecessor = false;
									for (int j = i + 1; j < number && isClosest; j++) {
										int k = ((Integer) ateNumbers.get(j))
												.intValue();
										if (((causalMatrix.get(num, k) > noiseRatio
												* inputLog.numberOfInstances()) && (causalMatrix
												.get(k, eventNumber) > noiseRatio
												* inputLog.numberOfInstances()))
												|| k == num) {
											// k is closer causal precessor, or
											// k is the same event
											isClosest = false;
										}
									}
									if (isClosest) {
										// create relation between ate of num
										// and ate of eventNumber
										AuditTrailEntry[] rl = new AuditTrailEntry[2];
										ArrayList ates0 = (ArrayList) numberToAteMap
												.get(num);
										ArrayList ates1 = (ArrayList) numberToAteMap
												.get(eventNumber);
										rl[0] = (AuditTrailEntry) ates0
												.get(ates0.size() - 1);
										rl[1] = (AuditTrailEntry) ates1
												.get(ates1.size() - 1);
										if (rl[0].getTimestamp() != null
												&& rl[1].getTimestamp() != null) {
											// and add it to the relationlist if
											// the timestamps are valid
											relationList.add(rl);
										}
									}
								}
							} else {
								// loop of length one occurred
								AuditTrailEntry[] rl = new AuditTrailEntry[2];
								ArrayList ates0 = (ArrayList) numberToAteMap
										.get(num);
								try {
									rl[0] = (AuditTrailEntry) ates0.get(ates0
											.size() - 1);
									rl[1] = (AuditTrailEntry) ates0.get(ates0
											.size() - 2);
									if (rl[0].getTimestamp() != null
											&& rl[1].getTimestamp() != null) {
										relationList.add(rl);
									}
								} catch (ArrayIndexOutOfBoundsException e) {
									// Can occur if ates0.size() is 0 or 1
									Message
											.add("Exception occurred during sequence diagram build");
								}
							}
						}
						if (noPrecessor) {
							AuditTrailEntry[] rl = new AuditTrailEntry[2];
							rl[0] = ate;
							rl[1] = ate;
							relationList.add(rl);
						}
						number++; // occurredNumbers.add(eventNumber);
						String current = null;
						if (dataEltType.equalsIgnoreCase("Task ID")) {
							// get task name of this audit trail entry
							current = ate.getElement();
						} else if (dataEltType.equalsIgnoreCase("Originator")) {
							// get originator of this audit trail entry
							current = ate.getOriginator();
						} else {
							// get data-element which can be found in the
							// data-part of the audit trail entry
							current = (String) ate.getAttributes().get(
									dataEltType);
						}
						if (current != null) {
							// keep track of the data elements that occurred
							String[] elts = (String[]) dataElts
									.toArray(new String[0]);
							Arrays.sort(elts);
							if (Arrays.binarySearch(elts, current) <= -1) {
								// current element is not yet present, so add it
								dataElts.add(current);
							}
						}
						if (ate.getTimestamp() != null) {
							// set endDate to the date of the last audit trail
							// entry in the current
							// process instance
							endDate = ate.getTimestamp();
							// and firstDate to the date of the first audit
							// trail entry
							if (firstDate == null
									|| firstDate.after(ate.getTimestamp())) {
								firstDate = ate.getTimestamp();
							}
						}
					}
					// create sequence
					Sequence seq = new Sequence(firstDate, endDate, pi
							.getName());
					if (beginTotal == null || firstDate.before(beginTotal)) {
						// first date of all dates in diagram
						beginTotal = firstDate;
					}
					if (endTotal == null || endDate.after(endTotal)) {
						// last date of all dates in diagram
						endTotal = endDate;
					}
					seq.initializeSequence(relationList, dataEltType,
							strictButton.isSelected());
					// generate a random color for the sequence
					Random generator = new Random();
					int randomR = generator.nextInt(256);
					int randomG = generator.nextInt(256);
					int randomB = generator.nextInt(256);
					seq.setColor(new Color(randomR, randomG, randomB));
					// add seq to sequences
					sequences.add(seq);
					compareAndAddToPattern(seq);
				}
			}
		}
	}

	/**
	 * Compares the sequence to all existing patterns, until a match is found.
	 * When a match is found, the sequence is added to that pattern. If no match
	 * is found, a new pattern is created based on the sequence
	 * 
	 * @param sequence
	 *            Sequence
	 * 
	 */
	private void compareAndAddToPattern(Sequence sequence) {
		boolean inPattern = false;
		ListIterator pts = patterns.listIterator();
		// run through existing patterns
		while (pts.hasNext() && !inPattern) {
			Pattern pattern = (Pattern) pts.next();
			if (pattern.compareToSequence(sequence, strictButton.isSelected())) {
				// sequence matches pattern, add it
				pattern.addSequence(sequence);
				inPattern = true;
			}
		}
		if (!inPattern) {
			// no match with an existing pattern, create a new one
			Pattern pattern = new Pattern(sequence);
			// and add it to the list of patterns
			patterns.add(pattern);
		}
	}

	/**
	 * Displays the full diagram
	 */
	private void showFullDiagram() {
		// JOptionPane.showMessageDialog(null, "0");
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		Iterator it = dataElts.iterator();
		int width = 50;
		double duration = 0;
		if (endTotal != null && beginTotal != null) {
			duration = endTotal.getTime() - beginTotal.getTime();
		}
		if (sequenceDiagramPanel.getTimePerPixel() == 0) {
			double dps = duration / sequences.size();
			sequenceDiagramPanel.setTimePerPixel(dps / 20.0);
			// originalTppFull = dps / 20.0;
		}
		while (it.hasNext()) {
			String elt = (String) it.next();
			width += elt.length() * 8 + 50;
		}
		width = Math.round(width * scale);
		int height = Math.max(((Double) (((duration / sequenceDiagramPanel
				.getTimePerPixel()) + 100) * scale)).intValue(), // Math.round(200
				// * scale +
				// sequences.size()
				// * 20 *
				// scale),
				Math.round(600 * scale));
		sequenceDiagramPanel.setSize(new Dimension(width, height));
		sequenceDiagramPanel.setPreferredSize(new Dimension(width, height));
		sequenceDiagramPanel.setMinimumSize(new Dimension(width, height));
		sequenceDiagramPanel.initialize(sequences, dataElts, timeSort,
				timeDivider, scale, beginTotal, duration);
		sequenceDiagramPanel.repaint();
		height = Math.round(height + 200 * scale);
		sequenceDiagramPanel.setPreferredSize(new Dimension(width, height));
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

	}

	/**
	 * Displays the pattern diagram
	 */
	private void showPatternDiagram() {
		patternMap.clear();
		ListIterator it = patterns.listIterator();
		double totalthrp = 0;
		double timePerPixel = patternDiagramPanel.getTimePerPixel();
		while (it.hasNext()) {
			Pattern pt = (Pattern) it.next();
			// place each pattern in the hashMap patternMap, to be able to sort
			// on frequency
			HashSet tempSet = (HashSet) patternMap.get(pt.getFrequency());
			if (tempSet == null) {
				tempSet = new HashSet();
			}
			tempSet.add(pt);
			patternMap.put(pt.getFrequency(), tempSet);
			// calculate throughput time to determine the height of the panel on
			// which the patterns are to be drawn
			pt.calculateTimes();
			totalthrp += pt.getMeanThroughputTime();
		}
		Iterator elts = dataElts.iterator();
		// determine width of the screen
		int width = 50;
		while (elts.hasNext()) {
			String elt = (String) elts.next();
			// more than enough
			width += elt.length() * 8 + 50;
		}
		width = Math.round(width * scale);
		int height = 900;
		if (patterns.size() > 0 && timePerPixel == 0) {
			double avgTime = totalthrp / patterns.size();
			// on average 100 pixels per pattern
			timePerPixel = avgTime / 100;
			// originalTppPattern = timePerPixel;
		}
		if (timePerPixel > 0) {
			Double screenHeight = totalthrp / timePerPixel;
			height = Math.round((screenHeight.floatValue() + 50
					* patterns.size() + 60)
					* scale);
		}
		// set size of the pattern diagram (panel)
		patternDiagramPanel.setSize(new Dimension(width, height));
		patternDiagramPanel.setPreferredSize(new Dimension(width, height));
		patternDiagramPanel.setMinimumSize(new Dimension(width, height));
		// initialize the painting of the pattern diagram
		patternDiagramPanel.initializePaint(patternMap, dataElts, timeDivider,
				scale, timePerPixel);
		// paint the pattern diagram
		patternDiagramPanel.repaint();
		displayPatternMetrics(patternDiagramPanel.getSortedArray());
		height = Math.round(height + 200 * scale);
		patternDiagramPanel.setPreferredSize(new Dimension(width, height));
		patternDrawn = true;
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Displays the performance metrics of each pattern on the east side of the
	 * plug-in window.
	 * 
	 * @param sortedArray
	 *            int[]
	 */
	private void displayPatternMetrics(int[] sortedArray) {
		int patternNumber = 0;
		metricsPanel.removeAll();
		metricsPanel.setLayout(new BoxLayout(metricsPanel, BoxLayout.Y_AXIS));
		// for each frequency get the set of patterns that have that frequency
		// (run from high frequency to low)
		for (int j = sortedArray.length - 1; j >= 0; j--) {
			try {
				HashSet pats = (HashSet) patternMap.get(sortedArray[j]);
				Iterator itr = pats.iterator();
				while (itr.hasNext()) {
					// get each pattern of the set of patterns
					Pattern current = (Pattern) itr.next();
					// create labels that contains information about the pattern
					OneMetricTableModel otm = new OneMetricTableModel();
					otm.setHeadings("", "Throughput time");
					DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
					dtcr.setBackground(new Color(235, 235, 235));
					JTable table = new JTable(otm);
					table.setPreferredSize(new Dimension(200, 55));
					table.setMaximumSize(new Dimension(200, 55));
					table.getColumnModel().getColumn(0).setPreferredWidth(50);
					table.getColumnModel().getColumn(0).setMaxWidth(100);
					table.getTableHeader().setFont(
							new Font("SansSerif", Font.PLAIN, 12));
					table.getColumnModel().getColumn(0).setCellRenderer(dtcr);
					table.setBorder(BorderFactory.createEtchedBorder());
					// place throughput times in table
					table.setValueAt(formatString(current
							.getMeanThroughputTime()
							/ timeDivider, 5), 0, 1);
					table.setValueAt(formatString(current
							.getMinThroughputTime()
							/ timeDivider, 5), 1, 1);
					table.setValueAt(formatString(current
							.getMaxThroughputTime()
							/ timeDivider, 5), 2, 1);
					table.setValueAt(formatString(current
							.getStdevThroughputTime()
							/ timeDivider, 5), 3, 1);
					JPanel tempPanel = new JPanel(new BorderLayout());
					table.setAlignmentX(CENTER_ALIGNMENT);
					tempPanel.setPreferredSize(new Dimension(160, 80));
					tempPanel.setMaximumSize(new Dimension(180, 80));
					tempPanel.add(table.getTableHeader(), BorderLayout.NORTH);
					tempPanel.add(table, BorderLayout.CENTER);
					JPanel tempPanel2 = new JPanel(new BorderLayout());
					JLabel patternLabel = new JLabel("Pattern "
							+ patternNumber++ + ":");
					patternLabel.setAlignmentX(LEFT_ALIGNMENT);
					JLabel frequencyLabel = new JLabel("Frequency: "
							+ current.getFrequency());
					frequencyLabel.setAlignmentX(LEFT_ALIGNMENT);
					frequencyLabel
							.setFont(new Font("SansSerif", Font.PLAIN, 12));
					tempPanel2.add(patternLabel, BorderLayout.NORTH);
					tempPanel2.add(frequencyLabel, BorderLayout.CENTER);
					tempPanel2.add(tempPanel, BorderLayout.SOUTH);
					metricsPanel.add(tempPanel2);
					metricsPanel.add(Box.createRigidArea(new Dimension(5, 10)));
				}
				if (patterns.size() == 1) {
					metricsPanel
							.add(Box.createRigidArea(new Dimension(5, 280)));
				}
				if (patterns.size() == 2) {
					metricsPanel.add(Box.createRigidArea(new Dimension(5, 50)));
				}
			} catch (NullPointerException ex) {
				// can occur when patternMap does not contain a pattern with
				// this
				// frequency
			}
		}
		// make sure the pattern performance information is displayed properly
		metricsPanel.setPreferredSize(new Dimension(200, 140 * patternNumber));
		metricsPanel.revalidate();
		metricsPanel.repaint();
	}

	/**
	 * Formats a double to display it in the right manner, with 'places' being
	 * the maximum number of decimal places allowed
	 * 
	 * @param val
	 *            double
	 * @param places
	 *            int
	 * @return String
	 */
	private String formatString(double val, int places) {
		String cur = "";
		DecimalFormat df;
		double bound = Math.pow(10.0, (0 - places));
		String tempString = "0";
		for (int i = 0; i < places - 1; i++) {
			tempString += "#";
		}
		if ((val != 0.0) && (val < bound)) {
			// display scientific notation
			if (places == 0) {
				df = new DecimalFormat("0E0");
			} else {
				df = new DecimalFormat("0." + tempString + "E0");
			}
			cur = df.format(val);
		} else {
			if (places == 0) {
				df = new DecimalFormat("0");
			} else {
				df = new DecimalFormat("0." + tempString);
			}
			cur = df.format(val);
		}
		return cur;
	}

	/**
	 * Specifiy provided objects of the analysis that can be further used to,
	 * e.g., export an item.
	 * 
	 * @return An Array containing provided objects
	 */
	public ProvidedObject[] getProvidedObjects() {
		// if provided objects are asked before the GUI is actually created the
		// log selection cannot be provided yet
		// furthermore the log selection is only offered if there is something
		// selected
		try {
			if (selectedInstanceIndices != null) {
				// fill the high level PN with simulation information
				// fillTransitionsHighLevelPN();
				ProvidedObject[] objects = {
						new ProvidedObject("Whole Log",
								new Object[] { inputLog }),
						new ProvidedObject("Log Selection",
								new Object[] { LogReaderFactory.createInstance(
										inputLog, selectedInstanceIndices) }) };
				return objects;
			} else {
				ProvidedObject[] objects = { new ProvidedObject("Whole Log",
						new Object[] { inputLog }) };
				return objects;
			}
		} catch (Exception e) {
			System.err.println("Fatal error creating new log reader instance:");
			System.err.println("(" + this.getClass() + ")");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Private data structure for tables containing one column with metrics
	 * (e.g. throughput time)
	 */
	private static class OneMetricTableModel extends AbstractTableModel {

		private String[] columnNames = { "", "Throughput time" };
		private Object[][] data = { { "avg", "" }, { "min", "" },
				{ "max", "" }, { "stdev", "" } };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		/*
		 * Set value at field[row, col] in the table data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

		public void setHeadings(String one, String two) {
			columnNames[0] = one;
			columnNames[1] = two;
		}
	}
}
