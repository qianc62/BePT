/*
 * Created on July. 04, 2007
 *
 * Author: Minseok Song
 * (c) 2006 Technische Universiteit Eindhoven, Minseok Song
 * all rights reserved
 *
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */

package org.processmining.analysis.performance.dottedchart.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JCheckBox;

import org.processmining.analysis.performance.dottedchart.DottedChartAnalysis;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import java.awt.event.ItemEvent;
import org.processmining.analysis.performance.dottedchart.model.DottedChartModel;

public class DottedChartOptionPanel extends JPanel implements ChangeListener,
		ItemListener {

	protected DottedChartPanel dcPanel = null;
	private DottedChartAnalysis dcAnalysis = null;

	// label
	private long widthDivider = 2592000000L;
	private JLabel optionLabel = new JLabel("Options");
	private JLabel componentLabel = new JLabel("Component type:");
	private JLabel colorByLabel = new JLabel("Color By:");
	private JLabel shapeByLabel = new JLabel("Shape By:");
	private JLabel sortByLabel = new JLabel("Sort By:");
	private JLabel mouseModeLabel = new JLabel("Mouse Mode:");
	private JLabel timeOptionLabel = new JLabel("Time option:");
	private JLabel relativeTimeOptionLabel = new JLabel("Relative Time option:");
	private String[] widthSorts = { "L-1", "L-10", "L-100", "L-500", "seconds",
			"minutes", "hours", // width option
			"days", "weeks", "months", "years" };
	private long[] widthDividers = { 1, 10, 100, 500, 1000, 60000, 3600000L,
			86400000L, 604800000L, 2592000000L, 31536000000L };
	private JComboBox widthBox = new JComboBox(widthSorts);
	private JLabel widthSortLabel = new JLabel("Time sort (chart):");
	private String[] timeOptions = { DottedChartPanel.TIME_ACTUAL,
			DottedChartPanel.TIME_RELATIVE_TIME,
			DottedChartPanel.TIME_RELATIVE_RATIO,
			DottedChartPanel.TIME_LOGICAL,
			DottedChartPanel.TIME_LOGICAL_RELATIVE, }; // array of "time option"
	// strings required to
	// fill the
	// timeOptionBox
	// combobox with
	private JComboBox timeOptionBox = new JComboBox(timeOptions);
	private String[] relativeTimeOptions = { DottedChartAnalysis.ST_INST,
			DottedChartAnalysis.ST_TASK, DottedChartAnalysis.ST_ORIG,
			DottedChartAnalysis.ST_EVEN }; // array of
	// "relative time classification"
	// strings required to fill the
	// timeOptionBox combobox with
	private JComboBox relativeTimeOptionBox = new JComboBox(relativeTimeOptions);
	private JComboBox componentBox = new JComboBox();
	private JComboBox colorByBox = new JComboBox();
	private JComboBox shapeByBox = new JComboBox();
	private JComboBox sortByBox = new JComboBox();
	private JCheckBox descCheckBox = new JCheckBox("descending");
	private JButton jbuttonZoom = new JButton("Zoom out");
	private JComboBox mouseMode = new JComboBox();
	protected JSlider zoomSliderX = null; // slider for zooming the view
	protected JSlider zoomSliderY = null; // slider for zooming the view

	public DottedChartOptionPanel(DottedChartPanel aDcPanel,
			DottedChartAnalysis aDCA) {
		dcPanel = aDcPanel;
		dcAnalysis = aDCA;

		initOptionPanel();
		initializeComponentBox();
		registerGUIListener();
	}

	public String getColorStandard() {
		return (String) colorByBox.getSelectedItem();
	}

	public String getShapeStandard() {
		return (String) shapeByBox.getSelectedItem();
	}

	public String getSortStandard() {
		return (String) sortByBox.getSelectedItem();
	}

	public String getMouseMode() {
		return (String) mouseMode.getSelectedItem();
	}

	public String getComponentType() {
		return (String) componentBox.getSelectedItem();
	}

	public long getWidthDivider() {
		return widthDivider;
	}

	public JSlider getZoomSliderX() {
		return zoomSliderX;
	}

	public JSlider getZoomSliderY() {
		return zoomSliderY;
	}

	public String getRelativeTimeOption() {
		return (String) relativeTimeOptionBox.getSelectedItem();
	}

	public String getTimeOption() {
		return (String) timeOptionBox.getSelectedItem();
	}

	public void changeWidthSort(int idx) {
		widthBox.setSelectedIndex(idx);
	}

	public boolean isDescCheckBoxSelected() {
		return descCheckBox.isSelected();
	}

	private void initOptionPanel() {
		// initialize westpanel
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setPreferredSize(new Dimension(170, 400));
		this.add(Box.createRigidArea(new Dimension(5, 2)));
		optionLabel.setAlignmentX(LEFT_ALIGNMENT);
		optionLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		this.add(optionLabel);
		// add component option
		this.add(Box.createRigidArea(new Dimension(5, 2)));
		componentLabel.setAlignmentX(LEFT_ALIGNMENT);
		this.add(componentLabel);
		componentBox.setMaximumSize(new Dimension(160, 20));
		componentBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(componentBox);
		// add time option
		this.add(Box.createRigidArea(new Dimension(5, 2)));
		this.add(timeOptionLabel);
		timeOptionBox.setMaximumSize(new Dimension(160, 20));
		timeOptionBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(timeOptionBox);
		// add time option
		this.add(Box.createRigidArea(new Dimension(5, 2)));
		this.add(relativeTimeOptionLabel);
		relativeTimeOptionBox.setMaximumSize(new Dimension(160, 20));
		relativeTimeOptionBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(relativeTimeOptionBox);
		// add width sort option
		this.add(Box.createRigidArea(new Dimension(5, 2)));
		this.add(widthSortLabel);
		widthBox.setSelectedIndex(9); // seconds selected in widthBox
		widthBox.setMaximumSize(new Dimension(160, 20));
		widthBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(widthBox);
		// add colorBy option
		this.add(Box.createRigidArea(new Dimension(5, 2)));
		colorByLabel.setAlignmentX(LEFT_ALIGNMENT);
		this.add(colorByLabel);
		colorByBox.setMaximumSize(new Dimension(160, 20));
		colorByBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(colorByBox);
		// add shapeBy option
		this.add(Box.createRigidArea(new Dimension(5, 2)));
		shapeByLabel.setAlignmentX(LEFT_ALIGNMENT);
		this.add(shapeByLabel);
		shapeByBox.setMaximumSize(new Dimension(160, 20));
		shapeByBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(shapeByBox);
		// add sortBy option
		this.add(Box.createRigidArea(new Dimension(5, 2)));
		sortByLabel.setAlignmentX(LEFT_ALIGNMENT);
		this.add(sortByLabel);
		sortByBox.setMaximumSize(new Dimension(160, 20));
		sortByBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(sortByBox);
		this.add(descCheckBox);
		// add mouseMode option
		// this.add(Box.createRigidArea(new Dimension(5, 2)));
		mouseModeLabel.setAlignmentX(LEFT_ALIGNMENT);
		this.add(mouseModeLabel);
		mouseMode.setMaximumSize(new Dimension(160, 20));
		mouseMode.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(mouseMode);
		// // add "show line box"
		// this.add(Box.createRigidArea(new Dimension(5, 10)));
		// checkLineBox.setAlignmentX(LEFT_ALIGNMENT);
		// checkLineBox.setSelected(false);
		// this.add(checkLineBox);
		// add zoom slide bar for X
		zoomSliderX = new JSlider(JSlider.VERTICAL, 0, 3000, 0); // zooms in
		// view
		zoomSliderX.setMaximumSize(new Dimension(80, 120));
		Hashtable zoomLabelTableX = new Hashtable();
		zoomLabelTableX.put(new Integer(0), new JLabel("1x"));
		zoomLabelTableX.put(new Integer(1000), new JLabel("10x"));
		zoomLabelTableX.put(new Integer(2000), new JLabel("(10^2)x"));
		zoomLabelTableX.put(new Integer(3000), new JLabel("(10^3)x"));
		zoomSliderX.setLabelTable(zoomLabelTableX);
		zoomSliderX.setMajorTickSpacing(1000);
		zoomSliderX.setMinorTickSpacing(500);
		zoomSliderX.setPaintTicks(true);
		zoomSliderX.setPaintLabels(true);
		zoomSliderX.addChangeListener(this);
		zoomSliderX.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel zoomLabelX = new JLabel("zoom (X)");
		zoomLabelX.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		zoomLabelX.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		JPanel zoomPanelX = new JPanel();
		zoomPanelX.setLayout(new BoxLayout(zoomPanelX, BoxLayout.Y_AXIS));
		zoomPanelX.setBorder(BorderFactory.createLoweredBevelBorder());
		zoomPanelX.setMinimumSize(new Dimension(80, 120));
		zoomPanelX.add(zoomSliderX);
		zoomPanelX.add(zoomLabelX);
		// add zoom slide bar for Y
		zoomSliderY = new JSlider(JSlider.VERTICAL, 0, 3000, 0); // zooms in
		// view
		zoomSliderY.setMaximumSize(new Dimension(80, 120));
		Hashtable zoomLabelTableY = new Hashtable();
		zoomLabelTableY.put(new Integer(0), new JLabel("1x"));
		zoomLabelTableY.put(new Integer(1000), new JLabel("10x"));
		zoomLabelTableY.put(new Integer(2000), new JLabel("(10^2)x"));
		zoomLabelTableY.put(new Integer(3000), new JLabel("(10^3)x"));
		zoomSliderY.setLabelTable(zoomLabelTableY);
		zoomSliderY.setMajorTickSpacing(1000);
		zoomSliderY.setMinorTickSpacing(500);
		zoomSliderY.setPaintTicks(true);
		zoomSliderY.setPaintLabels(true);
		zoomSliderY.addChangeListener(this);
		zoomSliderY.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel zoomLabelY = new JLabel("zoom (Y)");
		zoomLabelY.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		zoomLabelY.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		JPanel zoomPanelY = new JPanel();
		zoomPanelY.setLayout(new BoxLayout(zoomPanelY, BoxLayout.Y_AXIS));
		zoomPanelY.setBorder(BorderFactory.createLoweredBevelBorder());
		zoomPanelY.setMinimumSize(new Dimension(80, 120));
		zoomPanelY.add(zoomSliderY);
		zoomPanelY.add(zoomLabelY);

		JPanel zoomPanel = new JPanel();
		zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.X_AXIS));
		zoomPanel.setMaximumSize(new Dimension(160, 120));
		zoomPanel.setPreferredSize(new Dimension(160, 120));
		zoomPanel.add(zoomPanelX);
		zoomPanel.add(zoomPanelY);

		this.add(Box.createRigidArea(new Dimension(5, 15)));
		zoomPanel.setAlignmentX(LEFT_ALIGNMENT);
		this.add(zoomPanel);
		// add "zoom out button"
		this.add(jbuttonZoom);
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		// end west
	}

	private void initializeComponentBox() {
		colorByBox.addItem("None");
		shapeByBox.addItem("None");
		sortByBox.addItem("None");
		boolean flag = false;
		if (dcAnalysis.getDottedChartModel().getItemMap(
				DottedChartModel.ST_TASK).size() > 0) {
			componentBox.addItem(DottedChartPanel.ST_TASK);
			colorByBox.addItem(DottedChartPanel.ST_TASK);
			colorByBox.setSelectedIndex(1);
			shapeByBox.addItem(DottedChartPanel.ST_TASK);
			flag = true;
		}
		if (dcAnalysis.getDottedChartModel().getItemMap(
				DottedChartModel.ST_ORIG).size() > 0) {
			componentBox.addItem(DottedChartPanel.ST_ORIG);
			colorByBox.addItem(DottedChartPanel.ST_ORIG);
			shapeByBox.addItem(DottedChartPanel.ST_ORIG);
			flag = true;

		}
		if (dcAnalysis.getDottedChartModel().getItemMap(
				DottedChartModel.ST_INST).size() > 0) {
			componentBox.addItem(DottedChartPanel.ST_INST);
			colorByBox.addItem(DottedChartPanel.ST_INST);
			shapeByBox.addItem(DottedChartPanel.ST_INST);
			flag = true;
		}
		if (dcAnalysis.getDottedChartModel().getItemMap(
				DottedChartModel.ST_EVEN).size() > 0) {
			componentBox.addItem(DottedChartPanel.ST_EVEN);
			colorByBox.addItem(DottedChartPanel.ST_EVEN);
			shapeByBox.addItem(DottedChartPanel.ST_EVEN);
			flag = true;
		}
		if (dcAnalysis.getDottedChartModel().getItemMap(
				DottedChartModel.ST_DATA).size() > 0) {
			componentBox.addItem(DottedChartPanel.ST_DATA);
			flag = true;
		}
		if (!flag)
			componentBox.addItem("None");

		// initialize sort by box
		sortByBox.addItem(DottedChartPanel.ST_NAME);
		sortByBox.addItem(DottedChartPanel.ST_SIZE);
		sortByBox.addItem(DottedChartPanel.ST_DURATION);
		sortByBox.addItem(DottedChartPanel.ST_START_TIME);
		sortByBox.addItem(DottedChartPanel.ST_END_TIME);

		// setup mouse mode box
		mouseMode.addItem(DottedChartPanel.ST_ZOOMIN);
		mouseMode.addItem(DottedChartPanel.ST_DRAG);
	}

	private void registerGUIListener() {
		componentBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dcPanel.changeComponentType();
				dcAnalysis.getDottedChartModel().sortKeySet(getSortStandard(),
						isDescCheckBoxSelected());
				dcPanel.repaint();
				dcAnalysis.getOverviewPanel().setDrawBox(true);
				dcAnalysis.getMetricsPanel().displayPerformanceMetrics(); // update
				// MetricsPanel
			}
		});
		colorByBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dcAnalysis.getSettingPanel().changeColorPanel();
				dcPanel.repaint();
				dcAnalysis.getOverviewPanel().setDrawBox(true);
				dcAnalysis.getOverviewPanel().repaint();
			}
		});
		shapeByBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dcPanel.repaint();
				dcAnalysis.getOverviewPanel().setDrawBox(true);
				dcAnalysis.getOverviewPanel().repaint();
			}
		});
		sortByBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dcAnalysis.getDottedChartModel().sortKeySet(getSortStandard(),
						isDescCheckBoxSelected());
				dcPanel.repaint();
				dcAnalysis.getOverviewPanel().setDrawBox(true);
				dcAnalysis.getOverviewPanel().repaint();
				dcAnalysis.getMetricsPanel().displayPerformanceMetrics();
			}
		});
		widthBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				widthDivider = widthDividers[widthBox.getSelectedIndex()];
				dcPanel.changeWidthSort();
				dcPanel.repaint();
			}
		});
		timeOptionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String type = (String) timeOptionBox.getSelectedItem();
				if (type.equals(dcPanel.getTimeOption()))
					return;
				dcPanel.changeTimeOption();
				dcPanel.repaint();
				dcAnalysis.getOverviewPanel().setDrawBox(true);
				dcAnalysis.getMetricsPanel().displayPerformanceMetrics();
			}
		});
		relativeTimeOptionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String type = (String) timeOptionBox.getSelectedItem();
				String base = (String) relativeTimeOptionBox.getSelectedItem();
				if (base.equals(dcPanel.getRelativeTimeBase()))
					return;
				if (type.equals(DottedChartPanel.TIME_RELATIVE_TIME)
						|| type.equals(DottedChartPanel.TIME_RELATIVE_RATIO)) {
					dcPanel.setTimeBaseHaspMap(dcPanel.getDottedChartModel()
							.getStartDateMap(base));
					dcPanel.setEndTimeBaseHaspMap(dcPanel.getDottedChartModel()
							.getEndDateMap(base));
					dcPanel.setRelativeTimeBase(base);
					if (type.equals(DottedChartPanel.TIME_RELATIVE_TIME)
							|| type
									.equals(DottedChartPanel.TIME_RELATIVE_RATIO))
						dcPanel.changeTimeOption();
					dcAnalysis.getMetricsPanel().displayPerformanceMetrics();

				}
				dcAnalysis.getOverviewPanel().setDrawBox(true);
				dcAnalysis.getOverviewPanel().repaint();
			}
		});
		jbuttonZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zoomSliderX.setValue(0);
				zoomSliderX.repaint();
				zoomSliderY.setValue(0);
				zoomSliderY.repaint();
				dcPanel.adjustWidth();
				dcPanel.revalidate();
				dcPanel.changeWidthSort();
				dcAnalysis.getOverviewPanel().repaint();
			}
		});
		descCheckBox.addItemListener(this);
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

	// Listener for GUI
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		// handle zoom events
		if ((e.getSource() == zoomSliderX)) {
			double zoom = StrictMath.pow(10.0,
					((double) zoomSliderX.getValue() / 1000.0));
			dcPanel.setViewportZoomX(1.0 / zoom);
			dcPanel.adjustWidth();
			dcPanel.revalidate();
			dcAnalysis.getOverviewPanel().repaint();
		} else if ((e.getSource() == zoomSliderY)) {
			double zoom = StrictMath.pow(10.0,
					((double) zoomSliderY.getValue() / 1000.0));
			dcPanel.setViewportZoomY(1.0 / zoom);
			dcPanel.revalidate();
			dcAnalysis.getOverviewPanel().repaint();
		}

	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if (source == descCheckBox) {
			dcAnalysis.getDottedChartModel().sortKeySet(getSortStandard(),
					isDescCheckBoxSelected());
			dcPanel.repaint();
			dcAnalysis.getOverviewPanel().setDrawBox(true);
			dcAnalysis.getOverviewPanel().repaint();
			dcAnalysis.getMetricsPanel().displayPerformanceMetrics();
		}
	}

}
