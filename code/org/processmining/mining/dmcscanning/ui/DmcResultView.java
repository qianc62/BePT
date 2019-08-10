/*
 * Created on May 31, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
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
package org.processmining.mining.dmcscanning.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.mining.dmcscanning.Admc;
import org.processmining.mining.dmcscanning.Dmc;
import org.processmining.mining.dmcscanning.DmcScanningResult;

/**
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class DmcResultView extends JPanel implements ChangeListener,
		ListSelectionListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2281759919770390041L;

	/* non-visual attributes */
	protected DmcScanningResult result = null;

	/* process instance filter */
	protected String processInstanceFilter = null;

	/* user interface elements */
	protected JTabbedPane topLevelTabs = null; // top level tabs
	protected JSplitPane logItemsVerticalSplit = null; // splits the
	// LogItemPanel view
	// horizontally
	protected JPanel logItemViewTop = null; // top container for the
	// LogItemPanel view
	protected JScrollPane logItemPanelScroll = null; // scroll pane for
	// LogItemPanel
	protected LogItemPanel logItemPanel = null; // the LogItemPanel used for log
	// item visualization
	protected JSlider zoomSlider = null; // slider for zooming the view

	protected JTabbedPane madmcTabs = null; // tabbed pane to choose between
	// MDMC/ADMC display
	protected JList mdmcList = null; // high-level cluster set list for MDMC
	protected JList admcList = null; // high-level cluster set list for ADMC
	protected JScrollPane mdmcScrollPane = null; // scrollpane for mdmc list
	protected JScrollPane admcScrollPane = null; // scrollpane for admc list
	protected JList dmcList = null; // low-level cluster set list (DMCs)
	protected JList footprintList = null; // footprint list
	protected JTextField dmcIdField = null; // id of currently selected DMC

	protected JComboBox procInstSelect = null; // combo box for process instance
	// filtering/selection
	protected JComboBox handleVisSelect = null; // combo box for handle

	// visualization selection

	/**
	 * constructor
	 * 
	 * @param aResult
	 *            the result container to query data from
	 */
	public DmcResultView(DmcScanningResult aResult) {
		result = aResult;
		processInstanceFilter = null;
		initializeGui();
		if (result != null) {
			initializeBrowser();
		}
	}

	/**
	 * Sets up the user interface components and constructs the view
	 */
	protected void initializeGui() {
		topLevelTabs = new JTabbedPane();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(topLevelTabs);
		// construct upper view part
		logItemsVerticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		logItemsVerticalSplit.setDividerLocation(250);
		topLevelTabs.addTab("Log Visualization", logItemsVerticalSplit);
		logItemPanel = new LogItemPanel(result.getDmcSet()
				.getLeftBoundaryTimestamp(), result.getDmcSet()
				.getRightBoundaryTimestamp());
		logItemPanelScroll = new JScrollPane(logItemPanel,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		logItemPanelScroll.setMinimumSize(new Dimension(300, 150));
		logItemPanelScroll.setPreferredSize(new Dimension(1000, 300));
		logItemPanelScroll.getHorizontalScrollBar().setMaximum(100000);
		logItemPanelScroll.getHorizontalScrollBar().setMinimum(0);
		logItemPanelScroll.getHorizontalScrollBar().setUnitIncrement(300);
		logItemPanelScroll.getHorizontalScrollBar().setBlockIncrement(5000);
		logItemViewTop = new JPanel();
		logItemsVerticalSplit.setTopComponent(logItemViewTop);
		logItemViewTop
				.setLayout(new BoxLayout(logItemViewTop, BoxLayout.X_AXIS));
		logItemViewTop.add(logItemPanelScroll);
		// add zoom slider for log display
		zoomSlider = new JSlider(JSlider.VERTICAL, 0, 6000, 0); // zooms in view
		Hashtable zoomLabelTable = new Hashtable();
		zoomLabelTable.put(new Integer(0), new JLabel("1x"));
		zoomLabelTable.put(new Integer(1000), new JLabel("10x"));
		zoomLabelTable.put(new Integer(2000), new JLabel("(10^2)x"));
		zoomLabelTable.put(new Integer(3000), new JLabel("(10^3)x"));
		zoomLabelTable.put(new Integer(4000), new JLabel("(10^4)x"));
		zoomLabelTable.put(new Integer(5000), new JLabel("(10^5)x"));
		zoomLabelTable.put(new Integer(6000), new JLabel("(10^6)x"));
		zoomSlider.setLabelTable(zoomLabelTable);
		zoomSlider.setMajorTickSpacing(1000);
		zoomSlider.setMinorTickSpacing(200);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.addChangeListener(this);
		zoomSlider.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel zoomLabel = new JLabel("zoom");
		zoomLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		zoomLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		JPanel zoomPanel = new JPanel();
		zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.Y_AXIS));
		zoomPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		zoomPanel.setMinimumSize(new Dimension(100, 150));
		zoomPanel.add(zoomSlider);
		zoomPanel.add(zoomLabel);
		logItemViewTop.add(Box.createRigidArea(new Dimension(5, 50)));
		logItemViewTop.add(zoomPanel);
		logItemViewTop.add(Box.createRigidArea(new Dimension(5, 50)));
		// add the options field
		JPanel logViewOptions = new JPanel();
		logViewOptions
				.setLayout(new BoxLayout(logViewOptions, BoxLayout.Y_AXIS));
		logViewOptions.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		logViewOptions.setBorder(BorderFactory
				.createTitledBorder("View options"));
		logViewOptions.setMinimumSize(new Dimension(170, 200));
		logViewOptions.setMaximumSize(new Dimension(300, 300));
		logViewOptions.setPreferredSize(new Dimension(200, 200));
		// process instance filter
		JLabel procInstLabel = new JLabel("Process instance filter:");
		procInstLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		;
		logViewOptions.add(procInstLabel);
		procInstSelect = new JComboBox(result.getDmcSet().getProcessInstances()
				.toArray());
		procInstSelect.insertItemAt("show all", 0);
		procInstSelect.setSelectedIndex(0);
		procInstSelect.setMaximumSize(new Dimension(200, 30));
		procInstSelect.addActionListener(this);
		logViewOptions.add(procInstSelect);
		logViewOptions.add(Box.createVerticalStrut(15));
		// handle visualization
		JLabel handleVisLabel = new JLabel("Item handles:");
		logViewOptions.add(handleVisLabel);
		handleVisSelect = new JComboBox(LogItemPanel.HANDLE_ICONS);
		handleVisSelect.setSelectedItem(LogItemPanel.ITEM_HANDLE_CIRCLE);
		handleVisSelect.setMaximumSize(new Dimension(200, 30));
		handleVisSelect.addActionListener(this);
		logViewOptions.add(handleVisSelect);
		logViewOptions.add(Box.createVerticalStrut(15));

		logViewOptions.add(Box.createGlue());
		logItemViewTop.add(logViewOptions);

		// construct lower part (browser)
		mdmcList = new JList();
		admcList = new JList();
		dmcList = new JList();
		JSplitPane logItemLowSplitGeneral = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, true);
		JSplitPane logItemLowSplitBrowser = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, true);
		logItemLowSplitBrowser.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory
						.createLoweredBevelBorder()));
		logItemLowSplitGeneral.setLeftComponent(logItemLowSplitBrowser);
		mdmcList.addListSelectionListener(this);
		mdmcList.setCellRenderer(new DmcListCellRenderer());
		mdmcScrollPane = new JScrollPane(mdmcList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		admcList.addListSelectionListener(this);
		admcList.setCellRenderer(new DmcListCellRenderer());
		admcScrollPane = new JScrollPane(admcList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		madmcTabs = new JTabbedPane();
		madmcTabs.addChangeListener(this);
		madmcTabs.addTab("Minimal Conflict-free Set", mdmcScrollPane);
		madmcTabs.addTab("Aggregated Set", admcScrollPane);
		madmcTabs.setTabPlacement(JTabbedPane.TOP);
		logItemLowSplitBrowser.setLeftComponent(madmcTabs);
		dmcList.addListSelectionListener(this);
		dmcList.setCellRenderer(new DmcListCellRenderer());
		JScrollPane dmcScrollPane = new JScrollPane(dmcList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel dmcBrowserPanel = new JPanel();
		dmcBrowserPanel.setLayout(new BoxLayout(dmcBrowserPanel,
				BoxLayout.Y_AXIS));
		JLabel dmcBrowserLabel = new JLabel("Initial Clusters");
		dmcBrowserLabel.setBorder(BorderFactory.createEmptyBorder(3, 2, 2, 2));
		dmcBrowserLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		dmcBrowserPanel.add(dmcBrowserLabel);
		dmcBrowserPanel.add(dmcScrollPane);
		logItemLowSplitBrowser.setRightComponent(dmcBrowserPanel);
		logItemLowSplitBrowser.setResizeWeight(0.5);
		logItemLowSplitBrowser.setDividerLocation(0.5);

		JPanel dmcInfoPanel = new JPanel();
		dmcInfoPanel.setLayout(new BoxLayout(dmcInfoPanel, BoxLayout.Y_AXIS));
		dmcInfoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dmcInfoPanel.setPreferredSize(new Dimension(250, 300));
		JLabel dmcIdLabel = new JLabel("Cluster ID:");
		dmcIdLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		dmcInfoPanel.add(dmcIdLabel);
		dmcIdField = new JTextField(10);
		dmcIdField.setEditable(false);
		dmcIdField.setMaximumSize(new Dimension(400, 25));
		dmcInfoPanel.add(dmcIdField);
		dmcInfoPanel.add(Box.createVerticalStrut(15));
		JLabel footprintLabel = new JLabel("Footprint:");
		footprintLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		dmcInfoPanel.add(footprintLabel);
		footprintList = new JList();
		JScrollPane footprintScrollPane = new JScrollPane(footprintList,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		dmcInfoPanel.add(footprintScrollPane);
		logItemLowSplitGeneral.setRightComponent(dmcInfoPanel);

		logItemsVerticalSplit.setBottomComponent(logItemLowSplitGeneral);
		logItemsVerticalSplit.setResizeWeight(0.5);
		logItemLowSplitGeneral.setResizeWeight(0.7);
		logItemLowSplitGeneral.setDividerLocation(0.7);

	}

	/**
	 * initializes the {M|A|.}DMC browser components
	 */
	public void initializeBrowser() {
		mdmcList.setListData(reverseOrder(result.getMdmcSet().getAll()
				.toArray()));
		admcList.setListData(reverseOrder(result.getAdmcSet().getAllSorted()
				.toArray()));
		dmcList.setListData(new Object[0]);
	}

	/**
	 * convenience method: reverses an array order
	 * 
	 * @param input
	 * @return
	 */
	protected Object[] reverseOrder(Object[] input) {
		Object reversed[] = new Object[input.length];
		for (int i = 0; i < input.length; i++) {
			reversed[i] = input[input.length - (i + 1)];
		}
		return reversed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		// handle zoom events
		if ((e.getSource() == zoomSlider)) { // &&
			// !zoomSlider.getValueIsAdjusting())
			// {
			double zoom = StrictMath.pow(10.0,
					((double) zoomSlider.getValue() / 1000.0));
			logItemPanel.setViewportZoom(1.0 / zoom);
		} else if ((e.getSource() == madmcTabs)) {
			// clear all selections on major switch admc/mdmc
			mdmcList.clearSelection();
			admcList.clearSelection();
			dmcList.clearSelection();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		// react to changed selection in browser
		// if selection was changed in a high-level list ({A|M}DMC), we need
		// to adjust the content of the DMC list first
		if (e.getSource().equals(mdmcList)) {
			// new selection in mdmc list
			TreeSet selection = new TreeSet();
			Object values[] = mdmcList.getSelectedValues();
			for (int i = 0; i < values.length; i++) {
				selection.addAll(((Admc) values[i]).getDMCs());
			}
			dmcList.setListData(selection.toArray());
			dmcList.clearSelection();
			dmcIdField.setText("-/-");
			if (values.length == 1) {
				footprintList.setListData(((Admc) values[0]).footprint()
						.toArray());
			} else {
				footprintList.setListData(new Object[0]);
			}
		} else if (e.getSource().equals(admcList)) {
			// new selection in admc list
			TreeSet selection = new TreeSet();
			Object values[] = admcList.getSelectedValues();
			for (int i = 0; i < values.length; i++) {
				selection.addAll(((Admc) values[i]).getDMCs());
			}
			dmcList.setListData(selection.toArray());
			dmcList.clearSelection();
			dmcIdField.setText("-/-");
			if (values.length == 1) {
				footprintList.setListData(((Admc) values[0]).footprint()
						.toArray());
			} else {
				footprintList.setListData(new Object[0]);
			}
		} else if (e.getSource().equals(dmcList)) {
			// adjust info panel
			Dmc selected = (Dmc) dmcList.getSelectedValue();
			if (selected != null) {
				dmcIdField.setText(selected.getIdString());
				footprintList.setListData(selected.footprint().toArray());
			} else {
				dmcIdField.setText("-.-");
				footprintList.setListData(new Object[0]);
			}
		}
		updateLogItemPanel(); // adjust view to selection
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.procInstSelect) {
			// change process instance filter
			if (this.procInstSelect.getSelectedItem().equals("show all")) {
				processInstanceFilter = null;
			} else {
				processInstanceFilter = (String) this.procInstSelect
						.getSelectedItem();
			}
			updateLogItemPanel();
		} else if (e.getSource() == this.handleVisSelect) {
			// change handle visualization
			this.logItemPanel.setItemHandle((String) this.handleVisSelect
					.getSelectedItem());
			updateLogItemPanel();
		}
	}

	/**
	 * adjusts the LogItemPanel view to the current selection within the browser
	 * components.
	 */
	protected void updateLogItemPanel() {
		logItemPanel.clearItemSets();
		Object selected[] = dmcList.getSelectedValues();
		if (selected.length > 0) {
			// add all DMCs in dmc lane one by one
			for (int i = 0; i < selected.length; i++) {
				if ((processInstanceFilter == null)
						|| ((Dmc) selected[i]).getProcessInstance().getName()
								.equals(processInstanceFilter)) {
					logItemPanel.addItem(dmc2key((Dmc) selected[i]),
							(Dmc) selected[i]);
				}
			}
		} else {
			// no single DMCs selected; add all from currently selected {M|A}DMC
			Object admcSelection[] = null;
			if (madmcTabs.getSelectedComponent() == mdmcScrollPane) {
				admcSelection = mdmcList.getSelectedValues();
			} else {
				admcSelection = admcList.getSelectedValues();
			}
			for (int i = 0; i < admcSelection.length; i++) {
				if (processInstanceFilter == null) {
					logItemPanel.addItemSet(((Admc) admcSelection[i])
							.getIdString(), new TreeSet<Dmc>(
							((Admc) admcSelection[i]).getDMCs()));
				} else {
					SortedSet dmcs = new TreeSet<Dmc>(((Admc) admcSelection[i])
							.getDMCs());
					String admcKey = ((Admc) admcSelection[i]).getIdString();
					for (Iterator it = dmcs.iterator(); it.hasNext();) {
						Dmc dmc = (Dmc) it.next();
						if (dmc.getProcessInstance().getName().equals(
								processInstanceFilter)) {
							logItemPanel.addItem(admcKey, dmc);
						}
					}
				}
			}
		}
		logItemPanel.repaint();
	}

	/**
	 * convenience method. returns to a given DMC (from e.g. a selection) the id
	 * key string of the (first found) ADMC it is contained within
	 * 
	 * @param aDmc
	 * @return
	 */
	protected String dmc2key(Dmc aDmc) {
		Admc test = null;
		for (Iterator it = result.getAdmcSet().allAdmcIterator(); it.hasNext();) {
			test = (Admc) it.next();
			if (test.getDMCs().contains(aDmc)) {
				return test.getIdString();
			}
		}
		return null;
	}

	/**
	 * test main routine TODO: remove after ProM integration
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		// test setup
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.getContentPane().add(new DmcResultView(null));
		frame.setVisible(true);
	}

}
