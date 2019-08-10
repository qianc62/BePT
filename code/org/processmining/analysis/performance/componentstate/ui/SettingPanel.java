package org.processmining.analysis.performance.componentstate.ui;

import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.log.LogEvents;
import java.util.ArrayList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.analysis.performance.dottedchart.model.ExtendedLogTable;
import org.processmining.analysis.performance.dottedchart.ui.ColorReference;

import org.processmining.analysis.performance.componentstate.model.ComponentStateModel;
import org.processmining.analysis.performance.componentstate.ui.ComponentStatePanel;
import org.processmining.analysis.performance.componentstate.*;

import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import java.util.Set;
import java.awt.event.ActionListener;
import java.awt.Color;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.util.GUIPropertySetEnumeration;
import javax.swing.JComboBox;
import javax.swing.Box;

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

	// state events map: <event, state>
	protected HashMap selectedEventsMap;

	private ArrayList instanceIDs = new ArrayList();
	private ArrayList selectedIDs = new ArrayList();

	private JComboBox fromEventComboBox;
	private JComboBox toEventComboBox;

	protected JButton applyEventTypeButton = new JButton("Apply");
	protected JButton applySelectedInstancesButton = new JButton(
			"Use Selected Instances");

	// button for STATE settings
	protected JButton applyStateButton = new JButton("Apply State settings");
	private JPanel tempContainer;

	// private JComboBox stateEventsComboBox;
	private JList[] tempEventTypeButton;
	private JButton[] tempColorButton;
	private JButton newColorButton;
	private JComboBox[] tempPriorityButton;
	private DefaultListModel listModel;
	private DefaultListModel selectedListModel;
	private int[] selectedIndices;
	private JCheckBox checkCombineColorBox = new JCheckBox(" Combine colors ");
	private JCheckBox checkPrioritizeColorBox = new JCheckBox(
			" Prioritize colors ");
	private String elementState = null;

	private HashMap eventIndicesSelectionMap = new HashMap();
	// <state, int[] indices selected> :
	private HashMap<String, int[]> taskIndicesSelectionMap = new HashMap<String, int[]>();
	private HashMap<String, int[]> origIndicesSelectionMap = new HashMap<String, int[]>();
	private HashMap<String, int[]> dataIndicesSelectionMap = new HashMap<String, int[]>();

	private HashMap<String, Color> stateColorMap = new HashMap<String, Color>();
	private HashMap<String, Integer> statePriorityMap = new HashMap<String, Integer>();

	protected JLabel colorsettingLabel = new JLabel(
			"Change colors by pressing buttons");
	protected JCheckBox[] checks;
	protected ColorReference colorReference;
	private JScrollPane tableContainer;
	private JPanel colorPanel;
	private JScrollPane colorScrollPane;
	private JPanel colorMainPanel;
	private JPanel stateMainPanel = new JPanel();
	private JCheckBox checkLineBox = new JCheckBox("Show Lines");

	private JRadioButton combineColorBut = new JRadioButton("Combine colors");
	private JRadioButton prioritizeColorBut = new JRadioButton(
			"Prioritize colors");
	private ButtonGroup group = new ButtonGroup();

	private ComponentStateAnalysis componentStateAnalysis;
	private ComponentStatePanel componentStatePanel;
	private LogReader inputLog;
	private ComponentStateModel csModel;

	public SettingPanel(LogReader aInputLog,
			ComponentStateAnalysis aComponentStateAnalysis,
			ComponentStatePanel aComponentStatePanel,
			ComponentStateModel aCsModel) {
		componentStateAnalysis = aComponentStateAnalysis;
		componentStatePanel = aComponentStatePanel;
		inputLog = aInputLog;
		csModel = aCsModel;
		eventTypeToKeep = csModel.getEventTypeToKeep();
		instanceIDs = csModel.getInstanceTypeToKeep();

		// for states - get selected events map for the chosen element type:
		// might be null first time ???
		selectedEventsMap = csModel
				.getSelectedEventsForStates(componentStatePanel
						.getComponentStateOptionPanel().getComponentType());

	}

	public void initSettingPanel() {
		// System.out.println("===init Setting Panel===");

		// set the default selection of event types
		initEventTypeSelection();

		// state panel
		initStateSettings();

		// instance list panel
		JPanel p3 = new JPanel();
		processInstanceIDsTable = new DoubleClickTable(new ExtendedLogTable(
				inputLog, instanceIDs), null);
		selectInstances(componentStateAnalysis.getSelectedInstanceIndices());
		tableContainer = new JScrollPane(processInstanceIDsTable);
		tableContainer.setPreferredSize(new Dimension(250, 400));
		p3.setLayout(new BorderLayout());
		p3.add(tableContainer, BorderLayout.CENTER);
		p3.add(applySelectedInstancesButton, BorderLayout.SOUTH);
		this.add(p3);

		// initColorPanel();
	}

	protected void initEventTypeSelection() {

		// System.out.println("===init eventTypeSelection ...===");

		// prepare list with all events types
		listModel = new DefaultListModel();
		for (Iterator itSets = csModel.getItemMap(
				ComponentStateAnalysis.ST_EVEN).keySet().iterator(); itSets
				.hasNext();) {
			String tempEvent = (String) itSets.next();
			listModel.addElement(tempEvent);
		}

		// a temp array with selections for each state
		ArrayList selections = new ArrayList();
		Color tempColor = new Color(50, 100, 100); // init color

		// for tasks and PI
		String[] statesList = csModel
				.getElementStatesList(ComponentStateAnalysis.ST_TASK);
		int i = 0;
		while (i < statesList.length) {
			String tempState = (String) statesList[i];

			// initialize indices array for this state
			int k = 0;
			selections.clear();

			// set default values TASK :
			for (int j = 0; j < listModel.getSize(); j++) {

				String tempEvent = (String) listModel.get(j);

				// for running
				if (tempEvent.equalsIgnoreCase("start")
						&& tempState.equalsIgnoreCase("running")) {

					selections.add(j);
					selectedEventsMap.put(tempEvent, tempState);
					k++;

				}
				// for complete
				else if (tempEvent.equalsIgnoreCase("complete")
						&& tempState.equalsIgnoreCase("completed")) {

					selections.add(j);
					selectedEventsMap.put(tempEvent, tempState);
					k++;

				}
				// for assigned -- in case of the originator,the SUSPEND event
				// also determines this state
				else if ((tempEvent.equalsIgnoreCase("assign") || tempEvent
						.equalsIgnoreCase("reassign"))
						&& (tempState.equalsIgnoreCase("assigned"))) {

					selections.add(j);
					selectedEventsMap.put(tempEvent, tempState);
					k++;

				}

				// for suspended
				else if (tempEvent.equalsIgnoreCase("suspend")
						&& tempState.equalsIgnoreCase("suspended")) {

					selections.add(j);
					selectedEventsMap.put(tempEvent, tempState);
					k++;

				}
				// for aborted
				else if ((tempEvent.equalsIgnoreCase("ate_abort")
						|| tempEvent.equalsIgnoreCase("pi_abort") || tempEvent
						.equalsIgnoreCase("withdraw"))
						&& tempState.equalsIgnoreCase("aborted")) {

					selections.add(j);
					selectedEventsMap.put(tempEvent, tempState);
					k++;

				}
				// for manual skipped
				else if (tempEvent.equalsIgnoreCase("manualskip")
						&& tempState.equalsIgnoreCase("manualskipped")) {

					selections.add(j);
					selectedEventsMap.put(tempEvent, tempState);
					k++;

				}

			}

			int[] selectedIndicesTask = new int[selections.size()];
			for (int j = 0; j < selections.size(); j++) {
				selectedIndicesTask[j] = (Integer) selections.get(j);
			}

			// System.out.println("number selected: " + k);
			taskIndicesSelectionMap.put(tempState, selectedIndicesTask);

			// set PRIORITY for this state
			// set default prioritites:
			int priority = -1;
			if (tempState.equals(csModel.STATE_RUNNING)
					|| tempState.equals(csModel.STATE_WORKING)
					|| tempState.equals(csModel.STATE_DEFINED)) {
				priority = 1;
				tempColor = new Color(50, 200, 50); // green
			} else if (tempState.equals(csModel.STATE_ASSIGNED)
					|| tempState.equals(csModel.STATE_UNCONFIRMED)) {
				priority = 2;
				tempColor = Color.YELLOW; //
			} else if (tempState.equals(csModel.STATE_SUSPENDED)
					|| tempState.equals(csModel.STATE_UNDEFINED)) {
				priority = 3;
				tempColor = new Color(200, 100, 50); // orange
			} else if (tempState.equals(csModel.STATE_COMPLETED)) {
				priority = 3;
				tempColor = new Color(35, 130, 170); // nice blue-violet
			} else if (tempState.equals(csModel.STATE_ABORTED)) {
				priority = 3;
				tempColor = new Color(200, 50, 200); // violet
			} else if (tempState.equals(csModel.STATE_MANUALSKIP)) {
				priority = 3;
				tempColor = new Color(50, 50, 200); // blue
			} else {
				priority = 3;
			}

			// set selected priorities for this list
			statePriorityMap.put(statesList[i], priority);// priority for task
			// !!!
			stateColorMap.put(statesList[i], tempColor);
			i++;
		}
		// save default selection
		csModel.setStatePriorityMap(ComponentStateAnalysis.ST_TASK,
				statePriorityMap);
		csModel.setStatePriorityMap(ComponentStateAnalysis.ST_INST,
				statePriorityMap);

		csModel.setStateColorMap(ComponentStateAnalysis.ST_TASK, stateColorMap);
		csModel.setStateColorMap(ComponentStateAnalysis.ST_INST, stateColorMap);

		csModel.setSelectedEventsForStates(ComponentStateAnalysis.ST_TASK,
				selectedEventsMap);
		csModel.setSelectedEventsForStates(ComponentStateAnalysis.ST_INST,
				selectedEventsMap);
		// System.out.println("<- set init csModel.setSelectedEventsForStates for type TASK/INST");

		// put in eventTypeToKeep the selected events
		ArrayList<String> atr = new ArrayList<String>();
		String tempEvt = null;
		for (Iterator itSets = selectedEventsMap.keySet().iterator(); itSets
				.hasNext();) {
			tempEvt = (String) itSets.next();
			atr.add(tempEvt);
		}
		componentStatePanel.changeEventTypeToKeep(atr);
		// System.out.println("<- set init componentStatePanel.changeEventTypeToKeep for TASK/INST");

		// for originator
		statesList = csModel
				.getElementStatesList(ComponentStateAnalysis.ST_ORIG);
		i = 0;

		while (i < statesList.length) {
			String tempState = (String) statesList[i];
			// System.out.println(" ORIG state: " + tempState);
			int k = 0;
			selections.clear();

			// set default values :
			for (int j = 0; j < listModel.getSize(); j++) {

				String tempEvent = (String) listModel.get(j);

				// working
				if (tempEvent.equalsIgnoreCase("start")
						&& tempState.equalsIgnoreCase("working")) {
					selections.add(j);
					k++;

				}
				// for assigned -- in case of the originator,the SUSPEND event
				// also determines this state
				else if ((tempEvent.equalsIgnoreCase("assign")
						|| tempEvent.equalsIgnoreCase("reassign") || tempEvent
						.equalsIgnoreCase("suspend"))
						&& (tempState.equalsIgnoreCase("assigned"))) {
					selections.add(j);
					k++;

				}
			}

			int[] selectedIndicesOrig = new int[selections.size()];
			for (int j = 0; j < selections.size(); j++) {
				selectedIndicesOrig[j] = (Integer) selections.get(j);
			}
			// System.out.println("number selected: " + k);
			origIndicesSelectionMap.put(tempState, selectedIndicesOrig);

			// set PRIORITY for this state
			// set default prioritites:
			int priority = -1;
			if (tempState.equals(csModel.STATE_WORKING)) {
				priority = 1;
				tempColor = new Color(50, 200, 50); // green
			} else if (tempState.equals(csModel.STATE_ASSIGNED)) {
				priority = 2;
				tempColor = Color.YELLOW;
			} else {
				priority = 3;
			}

			// set selected priorities for this list
			statePriorityMap.put(statesList[i], priority);// priority for task
			// !!!
			stateColorMap.put(statesList[i], tempColor);

			i++;
		}
		// save default selection
		csModel.setStatePriorityMap(ComponentStateAnalysis.ST_ORIG,
				statePriorityMap);
		csModel.setStateColorMap(ComponentStateAnalysis.ST_ORIG, stateColorMap);

		// for DATA
		statesList = csModel
				.getElementStatesList(ComponentStateAnalysis.ST_DATA);
		i = 0;
		// allStatesSelections.clear();
		while (i < statesList.length) {
			String tempState = (String) statesList[i];
			int k = 0;
			selections.clear();

			// set default values :
			for (int j = 0; j < listModel.getSize(); j++) {

				String tempEvent = (String) listModel.get(j);

				// for update
				if (tempEvent.equalsIgnoreCase("update")
						&& tempState.equalsIgnoreCase("defined")) {
					selections.add(j);
					k++;
				}
			}

			int[] selectedIndicesData = new int[selections.size()];
			for (int j = 0; j < selections.size(); j++) {
				selectedIndicesData[j] = (Integer) selections.get(j);
			}
			// put the array for this state in the map

			dataIndicesSelectionMap.put(tempState, selectedIndicesData);

			// set PRIORITY for this state
			// set default prioritites:
			int priority = -1;
			if (tempState.equals(csModel.STATE_DEFINED)) {
				priority = 1;
				tempColor = new Color(50, 200, 50); // green
			} else if (tempState.equals(csModel.STATE_UNCONFIRMED)) {
				priority = 2;
				tempColor = Color.YELLOW;
			} else {
				priority = 3;
				tempColor = new Color(200, 100, 50); // orange
			}

			// set selected priorities for this list
			statePriorityMap.put(statesList[i], priority);// priority for task
			// !!!
			stateColorMap.put(statesList[i], tempColor);

			i++;
		}
		// save default selection
		csModel.setStatePriorityMap(ComponentStateAnalysis.ST_DATA,
				statePriorityMap);
		csModel.setStateColorMap(ComponentStateAnalysis.ST_DATA, stateColorMap);

	}

	// ==================== make the state panel=======================
	public void initStateSettings() {

		// add new state panel
		tempContainer = new JPanel();
		JPanel aStatePanel = initStatePanel();
		if (aStatePanel != null) {
			tempContainer.add(aStatePanel);
			// System.out.println("initStateSettings ---- new statePanel added to tempContainer!!!");
		}

		// paint method panel
		JPanel pPaint = new JPanel(new GridLayout(4, 1));
		pPaint.setBorder(BorderFactory.createEtchedBorder());

		JPanel temp0 = new JPanel();
		temp0.setLayout(new BoxLayout(temp0, BoxLayout.LINE_AXIS));
		JLabel pPaintLabel = new JLabel(" Paint method for overlapping states ");
		pPaintLabel.setAlignmentX(LEFT_ALIGNMENT);
		temp0.add(pPaintLabel);
		pPaint.add(temp0);

		JPanel temp1 = new JPanel();
		temp1.setLayout(new BoxLayout(temp1, BoxLayout.LINE_AXIS));
		temp1.add(Box.createRigidArea(new Dimension(0, 15)));
		pPaint.add(temp1);

		group.add(combineColorBut);
		group.add(prioritizeColorBut);
		JPanel temp2 = new JPanel();
		temp2.setLayout(new BoxLayout(temp2, BoxLayout.LINE_AXIS));
		combineColorBut.setAlignmentX(LEFT_ALIGNMENT);
		combineColorBut.setSelected(true); // if not originator!!
		temp2.add(combineColorBut);
		pPaint.add(temp2);
		JPanel temp3 = new JPanel();
		temp3.setLayout(new BoxLayout(temp3, BoxLayout.LINE_AXIS));
		prioritizeColorBut.setAlignmentX(LEFT_ALIGNMENT);
		prioritizeColorBut.setSelected(false); // if not originator!!
		temp3.add(prioritizeColorBut);
		pPaint.add(temp3);

		// add all to main panel

		stateMainPanel
				.setLayout(new BoxLayout(stateMainPanel, BoxLayout.Y_AXIS));
		// stateMainPanel.setPreferredSize(new Dimension(600, 330));
		// stateMainPanel.setMaximumSize(new Dimension(600, 330));

		stateMainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		tempContainer.setAlignmentX(LEFT_ALIGNMENT);
		stateMainPanel.add(tempContainer);
		stateMainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		pPaint.setAlignmentX(LEFT_ALIGNMENT);
		stateMainPanel.add(pPaint);
		stateMainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		applyStateButton.setAlignmentX(LEFT_ALIGNMENT);
		stateMainPanel.add(applyStateButton);

		this.add(stateMainPanel);
		// System.out.println("Setting Panel - added to settings window");
		registerGUIListener();

	}

	public JPanel initStatePanel() {

		// System.out.println("init State Panel");
		colorReference = componentStatePanel.getColorReference();
		selectedEventsMap = csModel
				.getSelectedEventsForStates(componentStatePanel
						.getComponentStateOptionPanel().getComponentType());

		// main panel: State definition for component type
		String type = componentStatePanel.getComponentStateOptionPanel()
				.getComponentType();
		// System.out.println("Setting Panel for type - " + type);

		if (type.equals(ComponentStateAnalysis.STR_NONE)
				|| type.equals(ComponentStateAnalysis.ST_EVEN))// have to
			// eliminate the
			// EVEN type!!
			return null;

		// get the available selection for colors and priorities
		stateColorMap = csModel.getStateColorMap(type);
		statePriorityMap = csModel.getStatePriorityMap(type);

		String[] statesList = csModel.getElementStatesList(type);

		// initializing the new STATE panel:
		JPanel statePanel = new JPanel(new GridLayout(statesList.length + 2, 1));
		statePanel.setPreferredSize(new Dimension(600,
				(statesList.length + 2) * 80));
		statePanel.setMaximumSize(new Dimension(600,
				(statesList.length + 2) * 80));
		statePanel.setBorder(BorderFactory.createEtchedBorder());
		statePanel.setAlignmentX(LEFT_ALIGNMENT);

		// add label
		JLabel stateElementLabel = new JLabel("  Define " + type + " states ");
		stateElementLabel.setSize(800, 20);
		// stateElementLabel.setAlignmentX(CENTER_ALIGNMENT);
		JPanel panelStateElementLabel = new JPanel();
		panelStateElementLabel.setSize(800, 200);
		// panelStateElementLabel.setLayout(new
		// GridLayout(panelStateElementLabel, BoxLayout.X_AXIS));
		panelStateElementLabel.setLayout(new GridLayout());
		panelStateElementLabel.add(stateElementLabel);
		statePanel.add(panelStateElementLabel);

		JLabel[] tempLable = new JLabel[statesList.length];
		JButton[] tempColorButton = new JButton[statesList.length];
		// list with all event types present in the log:
		// tempEventTypeButton = new
		// JList[csModel.getItemMap(ComponentStateAnalysis.ST_EVEN).keySet().size()];
		tempEventTypeButton = new JList[statesList.length];
		tempPriorityButton = new JComboBox[statesList.length];

		// clear maps for new component
		// stateColorMap.clear();
		// statePriorityMap.clear();

		// OnTop Labels

		JLabel jlLabel = new JLabel("State");
		jlLabel.setPreferredSize(new Dimension(60, 20));
		jlLabel.setMaximumSize(new Dimension(60, 20));

		JLabel jbButton = new JLabel("Color");
		jbButton.setPreferredSize(new Dimension(60, 20));
		jbButton.setMaximumSize(new Dimension(60, 20));

		JLabel jlEventType = new JLabel("Event");
		jlEventType.setPreferredSize(new Dimension(60, 20));
		jlEventType.setMaximumSize(new Dimension(60, 20));
		// jlEventType.setLayoutOrientation(JList.VERTICAL);
		// jlEventType.setSize(200,300);

		JLabel jcPrio = new JLabel("Priority");
		jcPrio.setPreferredSize(new Dimension(60, 20));
		jcPrio.setMaximumSize(new Dimension(60, 20));

		JPanel tempPan = new JPanel();
		tempPan.setLayout(new BoxLayout(tempPan, BoxLayout.X_AXIS));
		jlLabel.setAlignmentY(TOP_ALIGNMENT);
		tempPan.add(Box.createRigidArea(new Dimension(2, 0)));
		tempPan.add(jlLabel);
		// tempPamela.add(Box.createRigidArea(new Dimension(2, 0)));
		tempPan.add(Box.createHorizontalGlue());
		jbButton.setAlignmentY(TOP_ALIGNMENT);
		tempPan.add(jbButton);
		tempPan.add(Box.createRigidArea(new Dimension(2, 0)));
		tempPan.add(Box.createHorizontalGlue());
		jlEventType.setAlignmentY(TOP_ALIGNMENT);
		tempPan.add(jlEventType);

		tempPan.add(Box.createRigidArea(new Dimension(2, 0)));
		tempPan.add(Box.createHorizontalGlue());
		jcPrio.setAlignmentY(TOP_ALIGNMENT);
		tempPan.add(jcPrio);
		statePanel.add(tempPan);

		int i = 0;
		while (i < statesList.length) {
			String tempState = (String) statesList[i];
			// global defined = elementState
			elementState = tempState;

			tempLable[i] = new JLabel(" " + tempState + ":");
			tempLable[i].setPreferredSize(new Dimension(100, 20));
			tempLable[i].setMaximumSize(new Dimension(100, 20));

			// ========================================================================
			// set default color based on state !!!!!!!!!!!
			// we could make static colors for states...to make clear the
			// combination...

			tempColorButton[i] = new JButton("change color");
			// tempColorButton[i].setPreferredSize(new Dimension(80, 20));
			if (stateColorMap.get(tempState) != null) {
				tempColorButton[i].setBackground(stateColorMap.get(tempState));
			} else
				tempColorButton[i].setBackground(colorReference
						.getColor(tempState));
			tempColorButton[i].setName(statesList[i]);
			tempColorButton[i].setActionCommand(tempState);
			tempColorButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton tempButton = (JButton) e.getSource();
					Color newColor = JColorChooser.showDialog(tempButton,
							"Choose Background Color", tempButton
									.getBackground());
					if (newColor != null) {
						// System.out.println("new color chosen");
						tempButton.setBackground(newColor);
						assignColor(tempButton.getActionCommand(), newColor);
						stateColorMap.put(tempButton.getName(), newColor);
					}

				}
			});

			// set available colors for states
			stateColorMap
					.put(statesList[i], tempColorButton[i].getBackground()); // /
			// can
			// this
			// be
			// null????

			// ========================================================================
			// select the events that determine the states

			tempEventTypeButton[i] = new JList(listModel);
			tempEventTypeButton[i]
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			tempEventTypeButton[i].setLayoutOrientation(JList.VERTICAL);
			tempEventTypeButton[i].setName(statesList[i]);

			// update the selected event types for the current type and state
			// !!!

			if (type.equals(ComponentStateAnalysis.ST_TASK)
					|| type.equals(ComponentStateAnalysis.ST_INST)) {
				selectedIndices = (int[]) (taskIndicesSelectionMap
						.get(tempState));
			} else if (type.equals(ComponentStateAnalysis.ST_ORIG)) {
				selectedIndices = (int[]) (origIndicesSelectionMap
						.get(tempState));
			} else if (type.equals(ComponentStateAnalysis.ST_DATA)) {
				selectedIndices = (int[]) (dataIndicesSelectionMap
						.get(tempState));
			}

			int sel = 0;
			for (int j = 0; j < selectedIndices.length; j++) {
				int val = selectedIndices[j];
				if (val < listModel.getSize())
					sel++;
			}

			if (selectedIndices != null && selectedIndices.length != 0) {
				tempEventTypeButton[i].setSelectedIndices(selectedIndices);
			}

			// overwrites the old selection, with same selections
			for (int j = 0; j < tempEventTypeButton[i].getSelectedIndices().length; j++) {
				int index = tempEventTypeButton[i].getSelectedIndices()[j];
				String tpEvent = (String) listModel.get(index);
				// System.out.println(" selected ev put in the map: " +
				// tpEvent);
				selectedEventsMap.put(tpEvent, statesList[i]); // this can
				// overwrite, so
				// must check
				// consist!!!!
			}

			// user action ...

			tempEventTypeButton[i]
					.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent evt) {
							if (evt.getValueIsAdjusting() == false) // user
							// stoped
							// manipulating
							// the list
							{

								String type = componentStatePanel
										.getComponentStateOptionPanel()
										.getComponentType();
								JList list = (JList) evt.getSource();
								String nameState = list.getName();
								// System.out.println("current state: " +
								// nameState);

								int selections[] = list.getSelectedIndices();
								Object selectedValues[] = list
										.getSelectedValues();
								ArrayList vals = new ArrayList();

								for (int j = 0; j < selectedValues.length; j++) {
									// System.out.println("CHANGE - selected values: "
									// + selectedValues[j].toString());
									vals.add(selectedValues[j].toString());
								}
								// remove deselected items from the same list
								// map
								for (Iterator it = selectedEventsMap.keySet()
										.iterator(); it.hasNext();) {
									String tmpEvent = (String) it.next();
									if (nameState.equals(selectedEventsMap
											.get(tmpEvent))
											&& (!vals.contains(tmpEvent))) {
										selectedEventsMap.remove(tmpEvent);
										// redefine the iterator!!!!!
										it = selectedEventsMap.keySet()
												.iterator();
										// System.out.println("CHANGE - remove from selectedEventMap: "
										// + tmpEvent+ " for state "+
										// nameState);
									}
								}
								for (int j = 0; j < selections.length; j++) {
									String tmpEvent = (String) listModel
											.get(selections[j]);
									if ((selectedEventsMap
											.containsKey(tmpEvent))
											&& (!nameState
													.equals(selectedEventsMap
															.get(tmpEvent)))) {
										list.removeSelectionInterval(
												selections[j], selections[j]); // remove
										// selection
										// index
										// System.out.println("CHANGE - remove selected index: "
										// + tmpEvent);
										// set a bg color for this index?!
									} else if (!selectedEventsMap
											.containsKey(tmpEvent)) {
										selectedEventsMap.put(tmpEvent,
												nameState);
										// System.out.println("CHANGE - add to selectedEventMap: "
										// + tmpEvent+ " with new state " +
										// nameState);

									}
								}

								// update the selected maps
								if (type.equals(ComponentStateAnalysis.ST_TASK)
										|| type
												.equals(ComponentStateAnalysis.ST_INST)) {
									taskIndicesSelectionMap.put(nameState, list
											.getSelectedIndices());
								} else if (type
										.equals(ComponentStateAnalysis.ST_ORIG)) {
									origIndicesSelectionMap.put(nameState, list
											.getSelectedIndices());
								} else if (type
										.equals(ComponentStateAnalysis.ST_DATA)) {
									dataIndicesSelectionMap.put(nameState, list
											.getSelectedIndices());
								}
								// System.out.println("Value changed from first index "
								// + evt.getFirstIndex() + " to last index " +
								// evt.getLastIndex());
							}

						}
					});

			JScrollPane listScroller = new JScrollPane(tempEventTypeButton[i]);
			listScroller.setPreferredSize(new Dimension(200, 60));
			listScroller.setMaximumSize(new Dimension(200, 60));

			// ==========================================================================
			// select the priorities for the states
			tempPriorityButton[i] = new JComboBox();
			tempPriorityButton[i].setPreferredSize(new Dimension(60, 20));
			tempPriorityButton[i].setMaximumSize(new Dimension(60, 20));
			tempPriorityButton[i].setName(statesList[i]);

			// init comboBox, and set default selection
			for (int j = 0; j < statesList.length; j++) {
				tempPriorityButton[i].addItem(j + 1);
			}
			if (statePriorityMap.get(tempState) != null) {
				tempPriorityButton[i].setSelectedItem(statePriorityMap
						.get(tempState));
				// System.out.println("priority set " +
				// statePriorityMap.get(tempState)+ "for state "+ tempState);
			}

			// add action listener
			tempPriorityButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox tempButton = (JComboBox) e.getSource();
					int priority = (Integer) tempButton.getSelectedItem();
					statePriorityMap.put(tempButton.getName(), priority);

				}
			});

			JPanel tempPanel = new JPanel();
			tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
			tempLable[i].setAlignmentY(TOP_ALIGNMENT);
			tempPanel.add(tempLable[i]);

			// tempPanel.add(Box.createRigidArea(new Dimension(2, 0)));
			// tempPanel.add(Box.createHorizontalGlue());
			// tempColorButton[i].setAlignmentY(TOP_ALIGNMENT);

			if (type.equals(ComponentStateAnalysis.ST_ORIG)
					&& tempState.equals(csModel.STATE_UNASSIGNED)) {

				// System.out.println(">>>for UNASSIGNED don't display options ");
				newColorButton = new JButton("static color");
				newColorButton.setPreferredSize(new Dimension(110, 25));
				newColorButton.setMaximumSize(new Dimension(110, 25));
				Color laneColorRed = new Color(200, 50, 50);
				newColorButton.setBackground(laneColorRed);
				newColorButton.setName(statesList[i]);
				newColorButton.setAlignmentX(LEFT_ALIGNMENT);
				newColorButton.setAlignmentY(TOP_ALIGNMENT);
				tempPanel.add(Box.createRigidArea(new Dimension(40, 0)));
				tempPanel.add(newColorButton);

			} else {
				tempPanel.add(Box.createHorizontalGlue());
				tempColorButton[i].setAlignmentY(TOP_ALIGNMENT);
				tempPanel.add(tempColorButton[i]);

			}

			tempPanel.add(Box.createRigidArea(new Dimension(2, 0)));
			tempPanel.add(Box.createHorizontalGlue());
			listScroller.setAlignmentY(TOP_ALIGNMENT);
			if (type.equals(ComponentStateAnalysis.ST_ORIG)
					&& tempState.equals(csModel.STATE_UNASSIGNED)) {
				// tempPanel.add(listScroller);
				// System.out.println(">>>for UNASSIGNED don't display options ");
			} else {
				tempPanel.add(listScroller);
			}

			tempPanel.add(Box.createRigidArea(new Dimension(2, 0)));
			tempPanel.add(Box.createHorizontalGlue());
			tempPriorityButton[i].setAlignmentY(TOP_ALIGNMENT);
			if (type.equals(ComponentStateAnalysis.ST_ORIG)
					&& tempState.equals(csModel.STATE_UNASSIGNED)) {
				// tempPanel.add(listScroller);
				// System.out.println(">>>for UNASSIGNED don't display options ");
			} else {
				tempPanel.add(tempPriorityButton[i]);
			}

			statePanel.add(tempPanel);
			i++;
		}
		return statePanel;
	}

	public JPanel initStatePanelOLD() {
		// STATE panel
		// System.out.println("init State Panel");
		colorReference = componentStatePanel.getColorReference();
		selectedEventsMap = csModel
				.getSelectedEventsForStates(componentStatePanel
						.getComponentStateOptionPanel().getComponentType());

		// main panel: State definition for component type
		String type = componentStatePanel.getComponentStateOptionPanel()
				.getComponentType();
		// System.out.println("Setting Panel for type - " + type);

		if (type.equals(ComponentStateAnalysis.STR_NONE)
				|| type.equals(ComponentStateAnalysis.ST_EVEN))// have to
			// eliminate the
			// EVEN type!!
			return null;

		// get the available selection for colors and priorities
		stateColorMap = csModel.getStateColorMap(type);
		statePriorityMap = csModel.getStatePriorityMap(type);

		String[] statesList = csModel.getElementStatesList(type);
		// System.out.println("State Panel - number of elements: "+
		// statesList.length);

		// initializing the new STATE panel:
		JPanel statePanel = new JPanel(new GridLayout(statesList.length + 1, 1));
		statePanel.setPreferredSize(new Dimension(600,
				(statesList.length + 1) * 80));
		statePanel.setMaximumSize(new Dimension(600,
				(statesList.length + 1) * 80));
		statePanel.setBorder(BorderFactory.createEtchedBorder());
		statePanel.setAlignmentX(LEFT_ALIGNMENT);

		// add label
		JLabel stateElementLabel = new JLabel("  Define " + type + " states ");
		stateElementLabel.setAlignmentX(CENTER_ALIGNMENT);
		statePanel.add(stateElementLabel);

		JLabel[] tempLable = new JLabel[statesList.length];
		JButton[] tempColorButton = new JButton[statesList.length];
		// list with all event types present in the log:
		// tempEventTypeButton = new
		// JList[csModel.getItemMap(ComponentStateAnalysis.ST_EVEN).keySet().size()];
		tempEventTypeButton = new JList[statesList.length];
		tempPriorityButton = new JComboBox[statesList.length];

		// clear maps for new component
		// stateColorMap.clear();
		// statePriorityMap.clear();

		int i = 0;
		while (i < statesList.length) {
			String tempState = (String) statesList[i];
			// global defined = elementState
			elementState = tempState;
			// System.out.println("state: " + tempState);
			tempLable[i] = new JLabel(" " + tempState + ":");
			tempLable[i].setPreferredSize(new Dimension(100, 20));
			tempLable[i].setMaximumSize(new Dimension(100, 20));

			// ========================================================================
			// set default color based on state !!!!!!!!!!!
			// we could make static colors for states...to make clear the
			// combination...

			tempColorButton[i] = new JButton("change color");
			// tempColorButton[i].setPreferredSize(new Dimension(80, 20));
			tempColorButton[i]
					.setBackground(colorReference.getColor(tempState));
			tempColorButton[i].setName(statesList[i]);
			tempColorButton[i].setActionCommand(tempState);
			tempColorButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton tempButton = (JButton) e.getSource();
					Color newColor = JColorChooser.showDialog(tempButton,
							"Choose Background Color", tempButton
									.getBackground());
					if (newColor != null) {
						// System.out.println("new color chosen");
						tempButton.setBackground(newColor);
						assignColor(tempButton.getActionCommand(), newColor);
						stateColorMap.put(tempButton.getName(), newColor);
					}

				}
			});

			// set available colors for states
			stateColorMap
					.put(statesList[i], tempColorButton[i].getBackground()); // /
			// can
			// this
			// be
			// null????
			// System.out.println("<<< state button color is set for state -"+
			// statesList[i]);

			// ========================================================================
			// select the events that determine the states

			tempEventTypeButton[i] = new JList(listModel);
			tempEventTypeButton[i]
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			tempEventTypeButton[i].setLayoutOrientation(JList.VERTICAL);
			tempEventTypeButton[i].setName(statesList[i]);

			// update the selected event types for the current type and state
			// !!!

			if (type.equals(ComponentStateAnalysis.ST_TASK)
					|| type.equals(ComponentStateAnalysis.ST_INST)) {
				selectedIndices = (int[]) (taskIndicesSelectionMap
						.get(tempState));
			} else if (type.equals(ComponentStateAnalysis.ST_ORIG)) {
				selectedIndices = (int[]) (origIndicesSelectionMap
						.get(tempState));
			} else if (type.equals(ComponentStateAnalysis.ST_DATA)) {
				selectedIndices = (int[]) (dataIndicesSelectionMap
						.get(tempState));
			}

			int sel = 0;
			for (int j = 0; j < selectedIndices.length; j++) {
				int val = selectedIndices[j];
				// System.out.println("<> value for indice to be selected: "+
				// val);
				if (val < listModel.getSize())
					sel++;
			}

			if (selectedIndices != null && selectedIndices.length != 0) {
				tempEventTypeButton[i].setSelectedIndices(selectedIndices);
				// System.out.println("dimension is "+ selectedIndices.length +
				// " for indices selected for state - "+ tempState);
			}

			// overwrites the old selection, with same selections
			for (int j = 0; j < tempEventTypeButton[i].getSelectedIndices().length; j++) {
				int index = tempEventTypeButton[i].getSelectedIndices()[j];
				String tpEvent = (String) listModel.get(index);
				// System.out.println(" selected ev put in the map: " +
				// tpEvent);
				selectedEventsMap.put(tpEvent, statesList[i]); // this can
				// overwrite, so
				// must check
				// consist!!!!
			}

			// user action ...

			tempEventTypeButton[i]
					.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent evt) {
							if (evt.getValueIsAdjusting() == false) // user
							// stoped
							// manipulating
							// the list
							{

								String type = componentStatePanel
										.getComponentStateOptionPanel()
										.getComponentType();
								JList list = (JList) evt.getSource();
								String nameState = list.getName();
								// System.out.println("current state: " +
								// nameState);

								int selections[] = list.getSelectedIndices();
								Object selectedValues[] = list
										.getSelectedValues();
								ArrayList vals = new ArrayList();

								for (int j = 0; j < selectedValues.length; j++) {
									// System.out.println("CHANGE - selected values: "
									// + selectedValues[j].toString());
									vals.add(selectedValues[j].toString());
								}
								// remove deselected items from the same list
								// map
								for (Iterator it = selectedEventsMap.keySet()
										.iterator(); it.hasNext();) {
									String tmpEvent = (String) it.next();
									if (nameState.equals(selectedEventsMap
											.get(tmpEvent))
											&& (!vals.contains(tmpEvent))) {
										selectedEventsMap.remove(tmpEvent);
										// redefine the iterator!!!!!
										it = selectedEventsMap.keySet()
												.iterator();
										// System.out.println("CHANGE - remove from selectedEventMap: "
										// + tmpEvent+ " for state "+
										// nameState);
									}
								}
								for (int j = 0; j < selections.length; j++) {
									String tmpEvent = (String) listModel
											.get(selections[j]);
									if ((selectedEventsMap
											.containsKey(tmpEvent))
											&& (!nameState
													.equals(selectedEventsMap
															.get(tmpEvent)))) {
										list.removeSelectionInterval(
												selections[j], selections[j]); // remove
										// selection
										// index
										// System.out.println("CHANGE - remove selected index: "
										// + tmpEvent);
										// set a bg color for this index?!
									} else if (!selectedEventsMap
											.containsKey(tmpEvent)) {
										selectedEventsMap.put(tmpEvent,
												nameState);
										// System.out.println("CHANGE - add to selectedEventMap: "
										// + tmpEvent+ " with new state " +
										// nameState);

									}
								}

								// update the selected maps
								if (type.equals(ComponentStateAnalysis.ST_TASK)
										|| type
												.equals(ComponentStateAnalysis.ST_INST)) {
									taskIndicesSelectionMap.put(nameState, list
											.getSelectedIndices());
								} else if (type
										.equals(ComponentStateAnalysis.ST_ORIG)) {
									origIndicesSelectionMap.put(nameState, list
											.getSelectedIndices());
								} else if (type
										.equals(ComponentStateAnalysis.ST_DATA)) {
									dataIndicesSelectionMap.put(nameState, list
											.getSelectedIndices());
								}
								// System.out.println("Value changed from first index "
								// + evt.getFirstIndex() + " to last index " +
								// evt.getLastIndex());
							}

						}
					});

			JScrollPane listScroller = new JScrollPane(tempEventTypeButton[i]);
			listScroller.setPreferredSize(new Dimension(200, 60));
			listScroller.setMaximumSize(new Dimension(200, 60));

			// ==========================================================================
			// select the priorities for the states
			tempPriorityButton[i] = new JComboBox();
			tempPriorityButton[i].setPreferredSize(new Dimension(60, 20));
			tempPriorityButton[i].setMaximumSize(new Dimension(60, 20));
			tempPriorityButton[i].setName(statesList[i]);

			// init comboBox, and set default selection
			for (int j = 0; j < statesList.length; j++) {
				tempPriorityButton[i].addItem(j + 1);
			}
			if (statePriorityMap.get(tempState) != null) {
				tempPriorityButton[i].setSelectedItem(statePriorityMap
						.get(tempState));
				// System.out.println("priority set " +
				// statePriorityMap.get(tempState)+ "for state "+ tempState);
			}

			// add action listener
			tempPriorityButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox tempButton = (JComboBox) e.getSource();
					int priority = (Integer) tempButton.getSelectedItem();
					statePriorityMap.put(tempButton.getName(), priority);

				}
			});

			JPanel tempPanel = new JPanel();
			tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
			tempLable[i].setAlignmentY(TOP_ALIGNMENT);
			tempPanel.add(tempLable[i]);
			// tempPanel.add(Box.createRigidArea(new Dimension(2, 0)));
			tempPanel.add(Box.createHorizontalGlue());
			tempColorButton[i].setAlignmentY(TOP_ALIGNMENT);
			tempPanel.add(tempColorButton[i]);
			tempPanel.add(Box.createRigidArea(new Dimension(2, 0)));
			tempPanel.add(Box.createHorizontalGlue());

			listScroller.setAlignmentY(TOP_ALIGNMENT);
			tempPanel.add(listScroller);

			tempPanel.add(Box.createRigidArea(new Dimension(2, 0)));
			tempPanel.add(Box.createHorizontalGlue());
			tempPriorityButton[i].setAlignmentY(TOP_ALIGNMENT);
			tempPanel.add(tempPriorityButton[i]);
			statePanel.add(tempPanel);

			i++;
		}
		return statePanel;
	}

	// method dealing with color
	private void initColorPanelOLD() {
		colorReference = componentStatePanel.getColorReference();

		String type = componentStatePanel.getComponentStateOptionPanel()
				.getColorStandard();

		if (type.equals(ComponentStateAnalysis.STR_NONE))
			return;
		Set keySet = csModel.getItemMap(type).keySet();

		colorPanel = new JPanel(new GridLayout(keySet.size(), 1));
		colorPanel.setPreferredSize(new Dimension(250, keySet.size() * 15));
		colorPanel.setMaximumSize(new Dimension(250, keySet.size() * 15));

		JLabel[] tempLable = new JLabel[keySet.size()];
		JButton[] tempButton = new JButton[keySet.size()];

		int i = 0;
		for (Iterator itr = keySet.iterator(); itr.hasNext();) {
			String tempString = (String) itr.next();
			tempLable[i] = new JLabel(tempString + ":");
			tempButton[i] = new JButton("push to change");
			tempButton[i].setBackground(colorReference.getColor(tempString));
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
			JPanel tempPanel = new JPanel(new GridLayout(1, 2));
			tempPanel.add(tempLable[i]);
			tempPanel.add(tempButton[i]);
			colorPanel.add(tempPanel);
			i++;
		}
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		p1.add(colorPanel);
		colorScrollPane = new JScrollPane(p1);
		colorScrollPane.setPreferredSize(new Dimension(260, 410));
		colorMainPanel = new JPanel();
		colorMainPanel
				.setLayout(new BoxLayout(colorMainPanel, BoxLayout.Y_AXIS));
		JLabel changeColorLabel = new JLabel("           Set colors");
		colorMainPanel.add(changeColorLabel);
		colorMainPanel.add(colorScrollPane);
		this.add(colorMainPanel);
		registerGUIListener();
	}

	// methods for the STATE panel
	public boolean combineColor() {
		return combineColorBut.isSelected();
	}

	public boolean prioritizeColor() {
		return prioritizeColorBut.isSelected();
	}

	// ***

	public boolean isDrawLine() {
		return checkLineBox.isSelected();
	}

	public String getStartEvent() {
		return (String) fromEventComboBox.getSelectedItem();
	}

	public String getEndEvent() {
		return (String) toEventComboBox.getSelectedItem();
	}

	public void assignColor(String name, Color newColor) {
		colorReference.assignColor(name, newColor);
	}

	// not necessary anymore
	public void changeColorPanel() {
		if (colorMainPanel != null)
			this.remove(colorMainPanel);
		colorMainPanel = null;
		// initColorPanel();
		this.repaint();
	}

	public void changeStatePanel() {
		// System.out.println("changeStatePanel called....");
		if ((tempContainer.getComponentCount() != 0)
				&& (tempContainer.getComponent(0) != null)) {
			tempContainer.remove(tempContainer.getComponent(0));
			// System.out.println("StatePanel removed from tempContainer....");
		}

		if (this.initStatePanel() != null) {
			tempContainer.add(this.initStatePanel());
			// System.out.println("changeStatePanel ---- new statePanel added to tempContainer!!!");

			// System.out.println("changeStatePanel ---- update componentStatePanel.changeEventTypeToKeep  !!!");

			// update the eventTypeToKeep list from the priviously selected
			// events for the new component type
			HashMap eventsMap = csModel
					.getSelectedEventsForStates(componentStatePanel
							.getComponentStateOptionPanel().getComponentType());
			// put in eventTypeToKeep the available selected events
			ArrayList<String> atr = new ArrayList<String>();
			String tempEvent = null;
			for (Iterator itSets = eventsMap.keySet().iterator(); itSets
					.hasNext();) {
				tempEvent = (String) itSets.next();
				atr.add(tempEvent);
			}
			componentStatePanel.changeEventTypeToKeep(atr);
		}

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
		// OLDDD.........
		applyEventTypeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> atr = new ArrayList<String>();
				for (int i = 0; i < eventTypeToKeep.size(); i++) {
					if (checks[i].isSelected())
						atr.add((String) eventTypeToKeep.get(i));
				}
				componentStatePanel.changeEventTypeToKeep(atr);

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
				componentStatePanel.changeInstanceTypeToKeep(selectedIDs);
				componentStateAnalysis
						.setSelectedInstanceIndices(getSelectionStatus());

			}
		});

		// new listener for the STATE button
		applyStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// update the state maps from CSMOdel ???
				String type = componentStatePanel
						.getComponentStateOptionPanel().getComponentType();
				String[] statesList = csModel.getElementStatesList(type);

				csModel.setStateColorMap(type, stateColorMap);
				csModel.setStatePriorityMap(type, statePriorityMap);
				// csModel.setSelectedEventsForStates(type, selectedEventsMap);
				componentStatePanel.changeSelectedEventsForStates(type,
						selectedEventsMap);

				// put in eventTypeToKeep the selected events
				ArrayList<String> atr = new ArrayList<String>();
				String tempEvent = null;
				for (Iterator itSets = selectedEventsMap.keySet().iterator(); itSets
						.hasNext();) {
					tempEvent = (String) itSets.next();
					atr.add(tempEvent);
				}
				componentStatePanel.changeEventTypeToKeep(atr);

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
