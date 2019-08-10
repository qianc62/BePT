package org.processmining.analysis.performance.dottedchart.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.analysis.performance.dottedchart.DottedChartAnalysis;
import org.processmining.analysis.performance.dottedchart.model.DottedChartModel;
import org.processmining.analysis.performance.dottedchart.model.ExtendedLogTable;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.util.GUIPropertyBoolean;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import org.processmining.framework.ui.filters.GenericMultipleExtFilter;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyListEnumeration;

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
public class SettingPanel extends JPanel {

	protected LogEvents eventsToKeep;
	protected ArrayList eventTypeToKeep;
	protected DoubleClickTable processInstanceIDsTable;
	private ArrayList instanceIDs = new ArrayList();
	private ArrayList selectedIDs = new ArrayList();
	private JComboBox fromEventComboBox;
	private JComboBox toEventComboBox;

	protected JButton applyEventTypeButton = new JButton("Apply");
	protected JButton applySelectedInstancesButton = new JButton(
			"Use Selected Instances");
	protected JLabel colorsettingLabel = new JLabel(
			"Change colors by pressing buttons");
	protected JCheckBox[] checks;
	protected ColorReference colorReference;
	private JScrollPane tableContainer;
	private JPanel colorPanel;
	private JScrollPane colorScrollPane;
	private JPanel colorMainPanel;
	private JCheckBox checkLineBox = new JCheckBox("Show Lines");
	// for bottleneck
	private JCheckBox checkBottleneckBox = new JCheckBox(
			"Filter based on interval");
	private GUIPropertyInteger percentileBoxL = new GUIPropertyInteger(
			"lower bound (percentile)", 90, 1, 100);
	private GUIPropertyInteger percentileBoxU = new GUIPropertyInteger(
			"upper bound (percentile)", 100, 1, 100);
	private JCheckBox checkBottleneckBoxforInstance = new JCheckBox(
			"Filter based on instances duration");
	private GUIPropertyInteger percentileBoxforInstanceL = new GUIPropertyInteger(
			"lower bound (percentile)", 90, 1, 100);
	private GUIPropertyInteger percentileBoxforInstanceU = new GUIPropertyInteger(
			"upper bound (percentile)", 100, 1, 100);

	private DottedChartAnalysis dottedChartAnalysis;
	private DottedChartPanel dottedChartPanel;
	private LogReader inputLog;
	private DottedChartModel dcModel;

	// for color reference
	private JLabel[] tempLable;
	private JButton[] tempButton;
	private JPanel colorChoosePanel;
	private JPanel colorMPanel;

	private JTextField colorFile = new JTextField();
	private JButton chooseColorButton = new JButton();

	public SettingPanel(LogReader aInputLog,
			DottedChartAnalysis aDottedChartAnalysis,
			DottedChartPanel aDottedChartPanel, DottedChartModel aDcModel) {
		dottedChartAnalysis = aDottedChartAnalysis;
		dottedChartPanel = aDottedChartPanel;
		inputLog = aInputLog;
		dcModel = aDcModel;
		eventTypeToKeep = dcModel.getEventTypeToKeep();
		instanceIDs = dcModel.getInstanceTypeToKeep();

	}

	public void initSettingPanel() {
		// event panel

		JPanel p0 = new JPanel();
		p0.setLayout(new BoxLayout(p0, BoxLayout.PAGE_AXIS));
		JPanel p = new JPanel(new GridLayout(eventTypeToKeep.size() + 2, 1));
		JLabel eventLabel = new JLabel("  Event list");
		p.add(eventLabel);
		checks = new JCheckBox[eventTypeToKeep.size()];
		for (int i = 0; i < eventTypeToKeep.size(); i++) {
			checks[i] = new JCheckBox((String) eventTypeToKeep.get(i));
			checks[i].setSelected(true);
			p.add(checks[i]);
		}

		p.add(applyEventTypeButton);
		p.setBorder(BorderFactory.createEtchedBorder());

		p0.add(p);
		p0.add(Box.createRigidArea(new Dimension(5, 10)));

		JPanel p2 = new JPanel(new GridLayout(4, 1));
		p2.setBorder(BorderFactory.createEtchedBorder());
		fromEventComboBox = new JComboBox();
		toEventComboBox = new JComboBox();
		for (int i = 0; i < eventTypeToKeep.size(); i++) {
			String temp = (String) eventTypeToKeep.get(i);
			fromEventComboBox.addItem(temp);
			toEventComboBox.addItem(temp);
			if (temp.equals("start"))
				fromEventComboBox.setSelectedIndex(i);
			if (temp.equals("complete"))
				toEventComboBox.setSelectedIndex(i);
		}

		// for show line
		JPanel tempPanel0 = new JPanel();
		tempPanel0.setLayout(new BoxLayout(tempPanel0, BoxLayout.LINE_AXIS));
		JLabel lineOptionLabel = new JLabel("  Line option setting");
		tempPanel0.add(lineOptionLabel);
		p2.add(tempPanel0);
		// add "show line box"
		JPanel tempPanel3 = new JPanel();
		tempPanel3.setLayout(new BoxLayout(tempPanel3, BoxLayout.LINE_AXIS));
		checkLineBox.setAlignmentX(LEFT_ALIGNMENT);
		checkLineBox.setSelected(false);
		tempPanel3.add(checkLineBox);
		p2.add(tempPanel3);

		// fromEventComboBox
		JPanel tempPanel1 = new JPanel();
		JLabel fromEventLabel = new JLabel(" Line from:");
		tempPanel1.add(fromEventLabel);
		tempPanel1.add(Box.createRigidArea(new Dimension(5, 0)));
		tempPanel1.add(Box.createHorizontalGlue());
		tempPanel1.add(fromEventComboBox);
		p2.add(tempPanel1);
		// toEventComboBox
		JPanel tempPanel2 = new JPanel();
		JLabel toEventLabel = new JLabel(" Line to:     ");
		tempPanel2.add(toEventLabel);
		tempPanel2.add(Box.createRigidArea(new Dimension(5, 0)));
		tempPanel2.add(Box.createHorizontalGlue());
		tempPanel2.add(toEventComboBox);
		p2.add(tempPanel2);

		p0.add(p2);

		// todo: for bottleneck
		JPanel p25 = new JPanel(new GridLayout(6, 1));
		p25.setBorder(BorderFactory.createEtchedBorder());
		// add "show line box"
		JPanel tempPanel30 = new JPanel();
		tempPanel30.setLayout(new BoxLayout(tempPanel30, BoxLayout.LINE_AXIS));
		checkBottleneckBox.setAlignmentX(LEFT_ALIGNMENT);
		checkBottleneckBox.setSelected(false);
		tempPanel30.add(checkBottleneckBox);
		p25.add(tempPanel30);
		p25.add(percentileBoxL.getPropertyPanel());
		p25.add(percentileBoxU.getPropertyPanel());

		JPanel tempPanel31 = new JPanel();
		tempPanel31.setLayout(new BoxLayout(tempPanel31, BoxLayout.LINE_AXIS));
		checkBottleneckBoxforInstance.setAlignmentX(LEFT_ALIGNMENT);
		checkBottleneckBoxforInstance.setSelected(false);
		tempPanel31.add(checkBottleneckBoxforInstance);
		p25.add(tempPanel31);
		p25.add(percentileBoxforInstanceL.getPropertyPanel());
		p25.add(percentileBoxforInstanceU.getPropertyPanel());

		p0.add(p25);
		this.add(p0);

		// instance list panel
		JPanel p3 = new JPanel();
		processInstanceIDsTable = new DoubleClickTable(new ExtendedLogTable(
				inputLog, instanceIDs), null);
		selectInstances(dottedChartAnalysis.getSelectedInstanceIndices());
		tableContainer = new JScrollPane(processInstanceIDsTable);
		tableContainer.setPreferredSize(new Dimension(250, 400));
		p3.setLayout(new BorderLayout());
		p3.add(tableContainer, BorderLayout.CENTER);
		p3.add(applySelectedInstancesButton, BorderLayout.SOUTH);
		this.add(p3);

		// init paint panel
		initColorPanel();
	}

	// method dealing with color
	private void initColorPanel() {
		colorReference = dottedChartPanel.getColorReference();

		String type = dottedChartPanel.getDottedChartOptionPanel()
				.getColorStandard();

		if (type.equals(DottedChartAnalysis.STR_NONE))
			return;
		Set keySet = dcModel.getItemMap(type).keySet();

		colorPanel = new JPanel(new GridLayout(keySet.size(), 1));
		colorPanel.setPreferredSize(new Dimension(250, keySet.size() * 15));
		colorPanel.setMaximumSize(new Dimension(250, keySet.size() * 15));

		tempLable = new JLabel[keySet.size()];
		tempButton = new JButton[keySet.size()];

		int i = 0;
		for (Iterator itr = keySet.iterator(); itr.hasNext();) {
			String tempString = (String) itr.next();
			tempLable[i] = new JLabel(tempString + ":");
			tempLable[i].setToolTipText(tempString);
			tempButton[i] = new JButton("push to change");
			tempButton[i].setBackground(colorReference.getColor(tempString));
			tempButton[i].setToolTipText(tempString);
			tempButton[i].setActionCommand(tempString);
			tempButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton tempButton = (JButton) e.getSource();
					Color newColor = JColorChooser.showDialog(tempButton,
							"Choose Background Color", tempButton
									.getBackground());
					if (newColor != null) {
						tempButton.setBackground(newColor);
						assignColor(tempButton.getActionCommand(), newColor);
					}
				}
			});
			colorChoosePanel = new JPanel(new GridLayout(1, 2));
			colorChoosePanel.add(tempLable[i]);
			colorChoosePanel.add(tempButton[i]);
			colorPanel.add(colorChoosePanel);
			i++;
		}
		colorMPanel = null;
		colorMPanel = new JPanel();
		colorMPanel.setLayout(new BoxLayout(colorMPanel, BoxLayout.Y_AXIS));
		colorMPanel.add(colorPanel);
		colorScrollPane = new JScrollPane(colorMPanel);
		colorScrollPane.setPreferredSize(new Dimension(260, 410));
		colorMainPanel = new JPanel();
		colorMainPanel
				.setLayout(new BoxLayout(colorMainPanel, BoxLayout.Y_AXIS));
		JLabel changeColorLabel = new JLabel("           Set colors");
		colorMainPanel.add(changeColorLabel);
		colorMainPanel.add(colorScrollPane);

		// todo for color
		colorFile.setMinimumSize(new Dimension(150, 21));
		colorFile.setPreferredSize(new Dimension(150, 21));
		colorFile.setEditable(false);
		colorMainPanel.add(colorFile);
		chooseColorButton.setMaximumSize(new Dimension(120, 25));
		chooseColorButton.setMinimumSize(new Dimension(120, 25));
		chooseColorButton.setPreferredSize(new Dimension(120, 25));
		chooseColorButton.setActionCommand("");
		chooseColorButton.setText("Browse...");
		chooseColorButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						chooseColorButton_actionPerformed(e);
					}
				});

		colorMainPanel.add(chooseColorButton);

		this.add(colorMainPanel);
		registerGUIListener();
	}

	private void redrawColorPart() {
		String type = dottedChartPanel.getDottedChartOptionPanel()
				.getColorStandard();
		colorMPanel.remove(colorPanel);
		this.remove(colorMainPanel);

		if (type.equals(DottedChartAnalysis.STR_NONE))
			return;
		Set keySet = dcModel.getItemMap(type).keySet();

		colorPanel = null;
		colorPanel = new JPanel(new GridLayout(keySet.size(), 1));
		colorPanel.setPreferredSize(new Dimension(250, keySet.size() * 15));
		colorPanel.setMaximumSize(new Dimension(250, keySet.size() * 15));

		int i = 0;
		for (Iterator itr = keySet.iterator(); itr.hasNext();) {
			String tempString = (String) itr.next();
			tempLable[i] = new JLabel(tempString + ":");
			tempLable[i].setToolTipText(tempString);
			tempButton[i] = new JButton("push to change");
			tempButton[i].setBackground(colorReference.getColor(tempString));
			tempButton[i].setToolTipText(tempString);
			tempButton[i].setActionCommand(tempString);
			tempButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton tempButton = (JButton) e.getSource();
					Color newColor = JColorChooser.showDialog(tempButton,
							"Choose Background Color", tempButton
									.getBackground());
					if (newColor != null) {
						tempButton.setBackground(newColor);
						assignColor(tempButton.getActionCommand(), newColor);
					}
				}
			});
			colorChoosePanel = new JPanel(new GridLayout(1, 2));
			colorChoosePanel.add(tempLable[i]);
			colorChoosePanel.add(tempButton[i]);
			colorPanel.add(colorChoosePanel);
			i++;
		}
		colorMPanel.add(colorPanel);
		colorMPanel = null;
		colorMPanel = new JPanel();
		colorMPanel.setLayout(new BoxLayout(colorMPanel, BoxLayout.Y_AXIS));
		colorMPanel.add(colorPanel);
		colorMPanel.repaint();
		colorMPanel.revalidate();
		colorScrollPane = new JScrollPane(colorMPanel);
		colorScrollPane.setPreferredSize(new Dimension(260, 410));
		colorMainPanel = null;
		colorMainPanel = new JPanel();
		colorMainPanel
				.setLayout(new BoxLayout(colorMainPanel, BoxLayout.Y_AXIS));
		JLabel changeColorLabel = new JLabel("           Set colors");
		colorMainPanel.add(changeColorLabel);
		colorMainPanel.add(colorScrollPane);

		colorMainPanel.add(colorFile);
		colorMainPanel.add(chooseColorButton);

		this.add(colorMainPanel);
		this.repaint();
		this.revalidate();
	}

	private void chooseColorButton_actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();

		chooser.setFileFilter(new GenericMultipleExtFilter(
				new String[] { "xml" }, "XML file (*.xml)"));
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getPath();
			setChosenXMLFile(name);
			colorReference.readFile(name);
			redrawColorPart();
		}
	}

	private void setChosenXMLFile(String logFileName) {
		colorFile.setText(logFileName);
	}

	public boolean isDrawLine() {
		return checkLineBox.isSelected();
	}

	public String getStartEvent() {
		return (String) fromEventComboBox.getSelectedItem();
	}

	public String getEndEvent() {
		return (String) toEventComboBox.getSelectedItem();
	}

	public boolean isBottleneck() {
		return checkBottleneckBox.isSelected();
	}

	public int getPercentileL() {
		return percentileBoxL.getValue();
	}

	public int getPercentileU() {
		return percentileBoxU.getValue();
	}

	public boolean isBottleneckforInstance() {
		return checkBottleneckBoxforInstance.isSelected();
	}

	public int getPercentileforInstanceU() {
		return percentileBoxforInstanceU.getValue();
	}

	public int getPercentileforInstanceL() {
		return percentileBoxforInstanceL.getValue();
	}

	public void assignColor(String name, Color newColor) {
		colorReference.assignColor(name, newColor);
	}

	public void changeColorPanel() {
		if (colorMainPanel != null)
			this.remove(colorMainPanel);
		colorMainPanel = null;
		initColorPanel();
		this.repaint();
	}

	/**
	 * selects those instances in the process instance table that have an index
	 * that is in the indices list
	 * 
	 * @param indices
	 *            int[]
	 */
	private void selectInstances(int[] indices) {
		processInstanceIDsTable.getSelectionModel().removeSelectionInterval(0,
				processInstanceIDsTable.getRowCount() - 1);
		HashSet intervals = new HashSet();
		if (indices.length > 0) {
			Arrays.sort(indices);
			int firstOfInterval = indices[0];
			int lastOfInterval = firstOfInterval;
			for (int i = 1; i < indices.length; i++) {
				int index = indices[i];
				if (!(lastOfInterval == index - 1)) {
					int[] interval = new int[2];
					interval[0] = firstOfInterval;
					interval[1] = lastOfInterval;
					intervals.add(interval);
					firstOfInterval = index;
				}
				lastOfInterval = index;
			}
			int[] interval = new int[2];
			interval[0] = firstOfInterval;
			interval[1] = lastOfInterval;
			intervals.add(interval);
		}

		Iterator its = intervals.iterator();
		while (its.hasNext()) {
			int[] interval = (int[]) its.next();
			// select interval
			processInstanceIDsTable.getSelectionModel().addSelectionInterval(
					interval[0], interval[1]);
		}
	}

	/**
	 * Connects GUI with listener-methods
	 */
	private void registerGUIListener() {
		applyEventTypeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> atr = new ArrayList<String>();
				for (int i = 0; i < eventTypeToKeep.size(); i++) {
					if (checks[i].isSelected())
						atr.add((String) eventTypeToKeep.get(i));
				}
				dottedChartPanel.changeLogical(false);
				dottedChartPanel.changeEventTypeToKeep(atr);

				dottedChartAnalysis.getMetricsPanel()
						.displayPerformanceMetrics();
				dottedChartAnalysis.getOverviewPanel().setDrawBox(true);
				dottedChartAnalysis.getDottedChartModel().sortKeySet(
						dottedChartPanel.getDottedChartOptionPanel()
								.getSortStandard(),
						dottedChartPanel.getDottedChartOptionPanel()
								.isDescCheckBoxSelected());
				dottedChartAnalysis.getOverviewPanel().repaint();
			}
		});
		applySelectedInstancesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] index = getSelectionStatus();
				selectedIDs = null;
				selectedIDs = new ArrayList<String>();
				for (int i = 0; i < index.length; i++) {
					selectedIDs.add((String) instanceIDs.get(index[i]));
				}
				dottedChartPanel.changeInstanceTypeToKeep(selectedIDs);
				dottedChartAnalysis
						.setSelectedInstanceIndices(getSelectionStatus());
				dottedChartAnalysis.getMetricsPanel()
						.displayPerformanceMetrics();
				dottedChartPanel.changeLogical(false);
				dottedChartPanel.changeOptions(true);
				dottedChartAnalysis.getDottedChartModel().sortKeySet(
						dottedChartPanel.getDottedChartOptionPanel()
								.getSortStandard(),
						dottedChartPanel.getDottedChartOptionPanel()
								.isDescCheckBoxSelected());
			}
		});
	}

	/**
	 * Retrieves the current selection status based on table indices.
	 * 
	 * @return int[] an array of indices indicating those instances that are
	 *         currently selected
	 */
	private int[] getSelectionStatus() {
		return processInstanceIDsTable.getSelectedRows();
	}
}
