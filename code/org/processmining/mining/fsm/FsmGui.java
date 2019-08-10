package org.processmining.mining.fsm;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.HashMap;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultTreeModel;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.Message;
import org.processmining.converting.fsm.FsmModificationGui;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * <p>Title: FSMUI</p>
 *
 * <p>Description: Dialog to obtain parameter values for the FSM miner</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: TU/e</p>
 *
 * @author Eric Verbeek
 * @version 1.0
 *
 * Code rating: Red
 *
 * Review rating: Red
 */

/**
 * Creates the GUI.
 */
public class FsmGui extends JPanel {
	// The tabbed pane to connect all panes to.
	private JTabbedPane tabbedPane;

	// The seperate GUIs for all horizon settings.
	private FsmHorizonGui[] bwdTab = new FsmHorizonGui[FsmMinerPayload.LAST];
	private FsmHorizonGui[] fwdTab = new FsmHorizonGui[FsmMinerPayload.LAST];

	// The list for the general filter.
	private JList visibleFilterList;

	// The check box to select whether or not the attribute settings should be
	// used.
	private JCheckBox useAttributesCheckBox;
	// A JTree to browse and change the attribute settings.
	/**
	 * The JTree will have three levels: 1. The attribute name. 2. The attribute
	 * value. 3. The value group. The user can edit the value group. Only
	 * attributes which have more than one value groups will be taken into
	 * account. In the FSM, the attributes will be assigned the value group (not
	 * the value itself, but the group). This way, it is possible to cluster
	 * certain values.
	 */
	private JTree attributeTree;

	// The list for the general filter.
	private JList attributeFilterList;

	private FsmModificationGui modTab;

	// The log summary.
	private LogSummary summary;
	// The default settings to display.
	private FsmSettings defaults;

	public FsmGui(FsmSettings defaults, LogSummary summary) {
		this.summary = summary;
		this.defaults = defaults;

		// Create a layout and a tabbed pane.
		this.setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		tabbedPane.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		tabbedPane.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		this.add("Center", tabbedPane);

		// The panes for the upper level horizon setting GUIs.
		// Upper level: model element, originator, event type.
		// Lower level: backward, forward.
		JTabbedPane panes[] = new JTabbedPane[FsmMinerPayload.LAST];
		// Create the panels in the tabbed pane.
		for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
			panes[mode] = new JTabbedPane();
			panes[mode].setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
			panes[mode].setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
			tabbedPane.add(FsmMinerPayload.getLabel(mode), panes[mode]);
			bwdTab[mode] = new FsmHorizonGui(defaults.getHorizonSettings(true,
					mode), panes[mode], "Backward");
			fwdTab[mode] = new FsmHorizonGui(defaults.getHorizonSettings(false,
					mode), panes[mode], "Forward");
		}

		// Add the tab for the general model element filter.
		addVisibleTab(summary, "Visible");

		// add the tab for the attribute settings.
		addAttributeTab(summary, "Attributes");

		modTab = new FsmModificationGui(defaults.getModificationSettings(),
				tabbedPane, "Modifications");
	}

	/**
	 * Adds a tab for the general model element filter.
	 * 
	 * @param summary
	 *            LogSummary the smmary to get the model elements from.
	 * @param title
	 *            String the label of the tab.
	 */
	private void addVisibleTab(LogSummary summary, String title) {
		JPanel visiblePanel = new JPanel(new BorderLayout());
		tabbedPane.addTab(title, visiblePanel);
		tabbedPane.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		tabbedPane.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		String elements[] = summary.getModelElements();

		visibleFilterList = new JList(elements);
		visibleFilterList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		visibleFilterList.setVisibleRowCount(-1);
		visibleFilterList.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		visibleFilterList.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);

		// Initially, all model elements are selected.
		visibleFilterList.setSelectionInterval(0, elements.length - 1);
		TitledBorder border = new TitledBorder(
				"Select the visible model elements");
		visiblePanel.setBorder(border);
		visiblePanel.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		visiblePanel.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		visiblePanel.add("Center", visibleFilterList);
	}

	/**
	 * Adds a tab for the attribute settings.
	 * 
	 * @param summary
	 *            LogSummary the summary to get the attrbiutes andsofroth from.
	 * @param title
	 *            String the label of the tab.
	 */
	private void addAttributeTab(LogSummary summary, String title) {
		JPanel attributePanel = new JPanel(new BorderLayout());
		tabbedPane.addTab(title, attributePanel);
		tabbedPane.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		tabbedPane.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);

		// Get the attribute info from the summary.
		// Results are stored in the default settings.
		getAttributeInfo();

		String elements[] = new String[defaults.getAttributeSettings().keySet()
				.size()];
		int i = 0;
		for (String element : defaults.getAttributeSettings().keySet()) {
			elements[i++] = element;
		}

		/*
		 * Use a simple GUI (a JList) to select visible attributes. This does
		 * not allow values to be grouped, though.
		 */
		attributeFilterList = new JList(elements);
		attributeFilterList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		attributeFilterList.setVisibleRowCount(-1);
		attributeFilterList.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		attributeFilterList
				.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);

		// Initially, all model elements are selected.
		// attributeFilterList.setSelectionInterval(0, elements.length - 1);
		TitledBorder border = new TitledBorder("Select the visible attributes");
		attributePanel.setBorder(border);
		attributePanel.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		attributePanel.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		attributePanel.add("Center", attributeFilterList);

		/*
		 * Forget about the complex (JTree) GUI that did allow for values to be
		 * grouped.
		 * 
		 * JPanel attributePanel = new JPanel(new BorderLayout());
		 * tabbedPane.addTab(title, attributePanel);
		 * tabbedPane.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		 * tabbedPane.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		 * 
		 * // Get the aatribute info from the summary. // Results are stored in
		 * the default settings. getAttributeInfo();
		 * 
		 * // Create the editable tree. DefaultMutableTreeNode rootNode = new
		 * DefaultMutableTreeNode( "Attributes");
		 * 
		 * for (Object attribute : defaults.getAttributeSettings().keySet()) {
		 * DefaultMutableTreeNode attrNode = new DefaultMutableTreeNode(
		 * attribute); rootNode.add(attrNode); HashMap map =
		 * defaults.getAttributeSettings().get(attribute); for (Object value :
		 * map.keySet()) { DefaultMutableTreeNode valueNode = new
		 * DefaultMutableTreeNode( value); attrNode.add(valueNode);
		 * DefaultMutableTreeNode clusterNode = new DefaultMutableTreeNode(
		 * map.get(value)); valueNode.add(clusterNode); } } DefaultTreeModel
		 * treeModel = new DefaultTreeModel(rootNode); attributeTree = new
		 * JTree(treeModel); attributeTree.setEditable(true);
		 * attributeTree.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		 * attributeTree.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		 * DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
		 * treeRenderer
		 * .setBackgroundNonSelectionColor(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR
		 * ); treeRenderer.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		 * attributeTree.setCellRenderer(treeRenderer); JScrollPane treePane =
		 * new JScrollPane(attributeTree);
		 * treePane.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		 * treePane.setBackground(FsmGuiColorScheme.FIELDBACKGROUNDCOLOR);
		 * attributePanel.setForeground(FsmGuiColorScheme.FOREGROUNDCOLOR);
		 * attributePanel.setBackground(FsmGuiColorScheme.BACKGROUNDCOLOR);
		 * attributePanel.setBorder(newTitledBorder(
		 * "Set labels for the attribute values (only attributes with different labels will be taken into account)"
		 * )); attributePanel.add("Center", treePane);
		 */
	}

	private void getAttributeInfo() {
		// This is awkward: the summary provides no information on data
		// attributes.
		// Scanning the log itself... first, get the process instances.
		LogEvents logEvents = summary.getLogEvents();
		HashSet<ProcessInstance> processInstances = new HashSet<ProcessInstance>();
		for (LogEvent logEvent : logEvents) {
			processInstances.addAll(summary.getInstancesForEvent(logEvent));
		}
		// Next iterate over all process instances and all audit trails to
		// collect data attribute information.
		// Note that the information found is stored in the default settings.
		for (ProcessInstance processInstance : processInstances) {
			AuditTrailEntryList ateList = processInstance
					.getAuditTrailEntryList();
			for (int i = 0; i < ateList.size(); i++) {
				try {
					AuditTrailEntry ate = ateList.get(i);
					DataSection dataSection = ate.getDataAttributes();
					HashMap<String, String> cluster;
					String defaultCluster = new String("label");
					for (String attribute : dataSection.keySet()) {
						if (!defaults.getAttributeSettings().containsKey(
								attribute)) {
							cluster = new HashMap<String, String>();
						} else {
							cluster = defaults.getAttributeSettings().get(
									attribute);
						}
						cluster.put(dataSection.get(attribute), defaultCluster);
						defaults.getAttributeSettings().put(attribute, cluster);
					}
				} catch (Exception e) {
					Message.add(e.toString(), Message.ERROR);
				}
			}
		}
	}

	/**
	 * Gets the settings from the GUI.
	 * 
	 * @return FsmSettings
	 */
	public FsmSettings getSettings() {
		FsmSettings settings = new FsmSettings(summary);
		int i;

		// Get the horizon settings.
		for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
			settings.setHorizonSettings(true, mode, bwdTab[mode].GetSettings());
			settings
					.setHorizonSettings(false, mode, fwdTab[mode].GetSettings());
		}

		// Get the general filter.
		i = 0;
		settings.getVisibleFilter().clear();
		for (String element : summary.getModelElements()) {
			if (visibleFilterList.isSelectedIndex(i)) {
				settings.getVisibleFilter().add(element);
			}
			i++;
		}

		/*
		 * Read the simple attribute GUI.
		 */
		settings.setUseAttributes(false);
		settings.getAttributeSettings().clear();
		i = 0;
		for (String element : defaults.getAttributeSettings().keySet()) {
			if (attributeFilterList.isSelectedIndex(i)) {
				settings.setUseAttributes(true);
				HashMap<String, String> map = new HashMap<String, String>();
				HashMap<String, String> defaultMap = defaults
						.getAttributeSettings().get(element);
				for (String value : defaultMap.keySet()) {
					map.put(value, value);
				}
				Message.add(map.toString(), Message.DEBUG);
				settings.getAttributeSettings().put(element, map);
			}
			i++;
		}

		/*
		 * Forget about reading the complex attribute GUI.
		 * 
		 * // Get the attribute settings. settings.setUseAttributes(false);
		 * settings.getAttributeSettings().clear(); DefaultMutableTreeNode
		 * rootNode = (DefaultMutableTreeNode)
		 * attributeTree.getModel().getRoot(); if (rootNode.getChildCount() > 0)
		 * { for (DefaultMutableTreeNode attributeNode =
		 * (DefaultMutableTreeNode) rootNode. getFirstChild(); attributeNode !=
		 * null; attributeNode = attributeNode.getNextSibling()) {
		 * HashMap<String, String> map = new HashMap<String, String>(); for
		 * (DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode)
		 * attributeNode. getFirstChild(); valueNode != null; valueNode =
		 * valueNode.getNextSibling()) { DefaultMutableTreeNode clusterNode =
		 * valueNode.getFirstLeaf(); String clusterName =
		 * clusterNode.toString(); for (String mappedClusterName : map.values())
		 * { if (clusterName.compareTo(mappedClusterName) == 0) { clusterName =
		 * mappedClusterName; } } map.put(valueNode.toString(), clusterName); }
		 * 
		 * // The map seems to be bale to contain multiple keys which have the
		 * same string value. // Convert the map to a TreeMap and check whiheter
		 * this one contains // multiple string values. TreeSet set = new
		 * TreeSet(map.values()); if (set.size() > 1) { // Multiple values
		 * found, add it. settings.setUseAttributes(true);
		 * settings.getAttributeSettings().put(attributeNode.toString(), map); }
		 * } }
		 */

		settings.setModificationSettings(modTab.getSettings());

		// Obviously, we have a GUI.
		settings.setHasGUI(true);
		return settings;
	}
}
