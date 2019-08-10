package org.processmining.analysis.performance.componentstate.ui;

import java.awt.Point;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JCheckBox;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.BorderFactory;
import java.awt.Font;
import java.awt.Dimension;
import java.util.Hashtable;
import javax.swing.event.ChangeListener;
import org.processmining.framework.log.AuditTrailEntry;

// import org.processmining.analysis.performance.dottedchart.DottedChartAnalysis;
// import org.processmining.analysis.performance.dottedchart.logutil.LogUnitList;
// import org.processmining.analysis.performance.dottedchart.ui.DottedChartPanel;

import org.processmining.analysis.performance.componentstate.ComponentStateAnalysis;
import org.processmining.analysis.performance.componentstate.ui.ComponentStatePanel;
import org.processmining.analysis.performance.componentstate.logutil.LogUnitList;

import java.util.Iterator;
import org.processmining.framework.log.ProcessInstance;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.awt.event.ItemEvent;
import org.processmining.framework.log.LogReader;
import java.util.Arrays;
import java.util.HashSet;
import org.processmining.framework.log.AuditTrailEntryList;
import javax.swing.JButton;
import java.awt.BorderLayout;

public class ComponentStateOptionPanel extends JPanel implements ChangeListener {

	protected ComponentStatePanel csPanel = null;
	private ComponentStateAnalysis csAnalysis = null;

	// label
	private long widthDivider = 2592000000L;
	private JLabel optionLabel = new JLabel("Options");
	private JLabel componentLabel = new JLabel("Component type:");
	private JLabel colorByLabel = new JLabel("Color By:");
	private JLabel shapeByLabel = new JLabel("Shape By:");
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
	private String[] timeOptions = { ComponentStatePanel.TIME_ACTUAL,
			ComponentStatePanel.TIME_RELATIVE_TIME,
			ComponentStatePanel.TIME_RELATIVE_RATIO,
			ComponentStatePanel.TIME_LOGICAL,
			ComponentStatePanel.TIME_LOGICAL_RELATIVE, }; // array of
	// "time option"
	// strings required
	// to fill the
	// timeOptionBox
	// combobox with
	private JComboBox timeOptionBox = new JComboBox(timeOptions);
	// array of "relative time classification" strings required to fill the
	// timeOptionBox combobox with
	private String[] relativeTimeOptions = { ComponentStateAnalysis.ST_INST };// ,
	// ComponentStateAnalysis.ST_TASK,
	// ComponentStateAnalysis.ST_ORIG,
	// ComponentStateAnalysis.ST_EVEN};
	private JComboBox relativeTimeOptionBox = new JComboBox(relativeTimeOptions);
	private JComboBox componentBox = new JComboBox();
	private JComboBox colorByBox = new JComboBox();
	private JComboBox shapeByBox = new JComboBox();
	private JButton jbuttonZoom = new JButton("Zoom out");
	private JComboBox mouseMode = new JComboBox();
	// private JCheckBox checkLineBox = new JCheckBox("Show Lines");
	protected JSlider zoomSliderX = null; // slider for zooming the view
	protected JSlider zoomSliderY = null; // slider for zooming the view

	public ComponentStateOptionPanel(ComponentStatePanel aCSPanel,
			ComponentStateAnalysis aCSA) {
		csPanel = aCSPanel;
		csAnalysis = aCSA;

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

	private void initOptionPanel() {
		// initialize westpanel
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setPreferredSize(new Dimension(170, 400));
		this.add(Box.createRigidArea(new Dimension(5, 5)));
		optionLabel.setAlignmentX(LEFT_ALIGNMENT);
		optionLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		this.add(optionLabel);
		// add component option
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		componentLabel.setAlignmentX(LEFT_ALIGNMENT);
		this.add(componentLabel);
		componentBox.setMaximumSize(new Dimension(160, 20));
		componentBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(componentBox);
		// add time option
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		this.add(timeOptionLabel);
		timeOptionBox.setMaximumSize(new Dimension(160, 20));
		timeOptionBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(timeOptionBox);
		// add time option
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		this.add(relativeTimeOptionLabel);
		relativeTimeOptionBox.setMaximumSize(new Dimension(160, 20));
		relativeTimeOptionBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(relativeTimeOptionBox);
		// add width sort option
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		this.add(widthSortLabel);
		widthBox.setSelectedIndex(9); // seconds selected in widthBox
		widthBox.setMaximumSize(new Dimension(160, 20));
		widthBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(widthBox);

		/*
		 * // add colorBy option this.add(Box.createRigidArea(new Dimension(5,
		 * 10))); colorByLabel.setAlignmentX(LEFT_ALIGNMENT);
		 * this.add(colorByLabel); colorByBox.setMaximumSize(new Dimension(160,
		 * 20)); colorByBox.setAlignmentX(LEFT_ALIGNMENT);
		 * this.add(Box.createRigidArea(new Dimension(5, 0)));
		 * this.add(colorByBox); // add shapeBy option
		 * this.add(Box.createRigidArea(new Dimension(5, 10)));
		 * shapeByLabel.setAlignmentX(LEFT_ALIGNMENT); this.add(shapeByLabel);
		 * shapeByBox.setMaximumSize(new Dimension(160, 20));
		 * shapeByBox.setAlignmentX(LEFT_ALIGNMENT);
		 * this.add(Box.createRigidArea(new Dimension(5, 0)));
		 * this.add(shapeByBox);
		 */

		// add mouseMode option
		this.add(Box.createRigidArea(new Dimension(5, 10)));
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
		// this.add(Box.createRigidArea(new Dimension(5, 15)));
		// add "zoom out button"
		this.add(jbuttonZoom);
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		// end west
	}

	private void initializeComponentBox() {
		colorByBox.addItem("None");
		shapeByBox.addItem("None");
		// still have to reset, even though instanceIterator() is used
		LogReader inputLog = csAnalysis.getLogReader();
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
					componentBox.addItem(ComponentStatePanel.ST_TASK);
					// colorByBox.addItem(ComponentStatePanel.ST_TASK);
					// colorByBox.setSelectedIndex(1);
					// shapeByBox.addItem(ComponentStatePanel.ST_TASK);
					if (ate.getOriginator() != null) {
						// the audit trail entry has an originator attached to
						// it
						componentBox.addItem(ComponentStatePanel.ST_ORIG);
						// colorByBox.addItem(ComponentStatePanel.ST_ORIG);
						// shapeByBox.addItem(ComponentStatePanel.ST_ORIG);
					}
					componentBox.addItem(ComponentStatePanel.ST_INST);
					// colorByBox.addItem(ComponentStatePanel.ST_INST);
					// shapeByBox.addItem(ComponentStatePanel.ST_INST);

					// componentBox.addItem(ComponentStatePanel.ST_EVEN);
					// colorByBox.addItem(ComponentStatePanel.ST_EVEN);
					// shapeByBox.addItem(ComponentStatePanel.ST_EVEN);
					// get the other data elements in the audit trail entry
					String[] otherElts = getOtherDataElements(pi);
					if (otherElts.length > 0) {
						componentBox.addItem(ComponentStatePanel.ST_DATA);
					}
				}
			} catch (Exception e) {
			}

		}
		// setup mouse mode box
		mouseMode.addItem(ComponentStatePanel.ST_ZOOMIN);
		mouseMode.addItem(ComponentStatePanel.ST_DRAG);
	}

	private void registerGUIListener() {
		componentBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				csPanel.changeComponentType();
				csPanel.changeTimeOption();
				// refresh the STATE panel
				csAnalysis.getSettingPanel().changeStatePanel();
				System.out.println("componentBox action change!!!");
				csPanel.repaint();

				// ====================================================================
				// csAnalysis.getOverviewPanel().setDrawBox(true);
				// csAnalysis.getMetricsPanel().displayPerformanceMetrics(); //
				// update MetricsPanel
			}
		});
		colorByBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				csAnalysis.getSettingPanel().changeColorPanel();
				csPanel.repaint();

				// ====================================================================
				// csAnalysis.getOverviewPanel().setDrawBox(true);
				// csAnalysis.getOverviewPanel().repaint();
			}
		});
		shapeByBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				csPanel.repaint();

				// ====================================================================
				// csAnalysis.getOverviewPanel().setDrawBox(true);
				// csAnalysis.getOverviewPanel().repaint();
			}
		});
		widthBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				widthDivider = widthDividers[widthBox.getSelectedIndex()];
				csPanel.changeWidthSort();
				csPanel.repaint();
			}
		});
		timeOptionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String type = (String) timeOptionBox.getSelectedItem();
				if (type.equals(csPanel.getTimeOption()))
					return;
				csPanel.changeTimeOption();
				csPanel.repaint();

				// ====================================================================
				// csAnalysis.getOverviewPanel().setDrawBox(true);
				// csAnalysis.getMetricsPanel().displayPerformanceMetrics();
			}
		});
		relativeTimeOptionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String type = (String) timeOptionBox.getSelectedItem();
				String base = (String) relativeTimeOptionBox.getSelectedItem();
				if (base.equals(csPanel.getRelativeTimeBase()))
					return;
				if (type.equals(ComponentStatePanel.TIME_RELATIVE_TIME)
						|| type.equals(ComponentStatePanel.TIME_RELATIVE_RATIO)) {
					csPanel.setTimeBaseHaspMap(csPanel.getComponentStateModel()
							.getStartDateMap(base));
					csPanel.setEndTimeBaseHaspMap(csPanel
							.getComponentStateModel().getEndDateMap(base));
					csPanel.setRelativeTimeBase(base);
					if (type.equals(ComponentStatePanel.TIME_RELATIVE_TIME)
							|| type
									.equals(ComponentStatePanel.TIME_RELATIVE_RATIO))
						csPanel.changeTimeOption();

					// ====================================================================
					// csAnalysis.getMetricsPanel().displayPerformanceMetrics();

				}
				// csAnalysis.getOverviewPanel().setDrawBox(true);
				// csAnalysis.getOverviewPanel().repaint();
			}
		});
		jbuttonZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zoomSliderX.setValue(0);
				zoomSliderX.repaint();
				zoomSliderY.setValue(0);
				zoomSliderY.repaint();
				csPanel.adjustWidth();
				csPanel.revalidate();
				// ====================================================================
				// csAnalysis.getOverviewPanel().repaint();
			}
		});

		// jbuttonZoom.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// Point p = csPanel.zoomInViewPort();
		// if (p != null) {
		// csAnalysis.setScrollBarPosition(p);
		// csAnalysis.getOverviewPanel().setDrawBox(true);
		// }
		// }
		// });
		// checkLineBox.addItemListener(new ItemListener() {
		// public void itemStateChanged(ItemEvent e) {
		// if(e.getStateChange() == ItemEvent.DESELECTED)
		// csPanel.setDrawLine(false);
		// else csPanel.setDrawLine(true);
		// }
		// });
	}

	public void changeWidthSort(int idx) {
		widthBox.setSelectedIndex(idx);
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
			csPanel.setViewportZoomX(1.0 / zoom);
			csPanel.adjustWidth();
			csPanel.revalidate();

			// we don't display the metrics
			// now...====================================================
			// csAnalysis.getOverviewPanel().repaint();
		} else if ((e.getSource() == zoomSliderY)) {
			double zoom = StrictMath.pow(10.0,
					((double) zoomSliderY.getValue() / 1000.0));
			csPanel.setViewportZoomY(1.0 / zoom);
			csPanel.revalidate();

			// we don't display
			// analysis...====================================================
			// csAnalysis.getOverviewPanel().repaint();
		}

	}

}
