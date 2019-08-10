package org.processmining.framework.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.util.SlickerSwingUtils;

/**
 * An object set property that can be readily displayed as it maintains its own
 * GUI panel. The property will be graphically represented as a list including
 * buttons to add or remove values available in the initially provided object
 * set. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values. Furthermore, a {@link GuiNotificationTarget
 * notification target} may be specified in order to be informed as soon as the
 * list of values has been changed. <br>
 * <br>
 * A typical usage scenario looks as follows: <br>
 * <br>
 * <code>
 * JPanel testPanel = new Panel(); // create parent panel <br>
 * testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.PAGE_AXIS)); <br>
 * HashSet<String> values = new HashSet<String>();
 * values.add("Anne");
 * values.add("Maria");
 * GuiPropertyObjectSet names = new GuiPropertyObjectSet("Names", values); <br>
 * testPanel.add(names.getPropertyPanel()); // add property <br>
 * return testPanel; <br>
 * </code>
 * 
 * @see getAllValues
 * @see getPropertyPanel
 */
public class GuiPropertyObjectSet implements ListSelectionListener,
		GuiNotificationTarget {

	// property attributes
	private String myName;
	private String myDescription;
	private Set myAvailableValues;
	private GUIPropertySetEnumeration myAvailableList;
	private GuiNotificationTarget myTarget;
	private int myPreferredHeight;
	// GUI attributes
	private JPanel myPanel;
	private JList myJList;
	private DefaultListModel myListModel;
	private JButton myAddButton;
	private JButton myRemoveButton;
	private JPanel myPropertyInitialValuesPanel;

	// For each of the values in myJList it is saved what the value is of the
	// property that belongs to it
	private HashMap myInitialValuesAndProperty = new HashMap<Object, GUIPropertyListEnumeration>();
	private List myPropertyInitialValues;

	/**
	 * Creates a string list property without a discription and notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param initialValues
	 *            the initial values of this property. The objects in this list
	 *            should either be simple strings or override the toString()
	 *            method, which is then displayed as the name of this value in
	 *            the ComboBox
	 * @param availableValues
	 *            the available values of this property. The objects in this
	 *            list should be of the same type as the initial values.
	 *            Furthermore they should contain the initial values
	 */
	public GuiPropertyObjectSet(String name, List initialValues,
			Set availableValues) {
		this(name, null, initialValues, availableValues);
	}

	/**
	 * Creates a string list property without notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param initialValues
	 *            the initial values of this property. The objects in this list
	 *            should either be simple strings or override the toString()
	 *            method, which is then displayed as the name of this value in
	 *            the ComboBox
	 * @param availableValues
	 *            the available values of this property. The objects in this
	 *            list should be of the same type as the initial values.
	 *            Furthermore they should contain the initial values
	 */
	public GuiPropertyObjectSet(String name, String description,
			List initialValues, Set availableValues) {
		this(name, description, initialValues, availableValues, null, 70);
	}

	/**
	 * Creates a string list without a discription.
	 * 
	 * @param name
	 *            the name of this property
	 * @param initialValues
	 *            the initial values of this property. The objects in this list
	 *            should either be simple strings or override the toString()
	 *            method, which is then displayed as the name of this value in
	 *            the ComboBox
	 * @param availableValues
	 *            the available values of this property. The objects in this
	 *            list should be of the same type as the initial values.
	 *            Furthermore they should contain the initial values
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GuiPropertyObjectSet(String name, List initialValues,
			Set availableValues, GuiNotificationTarget target) {
		this(name, null, initialValues, availableValues, target, 70);
	}

	/**
	 * Creates a string list property.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param initialValues
	 *            the initial values of this property. The objects in this list
	 *            should either be simple strings or override the toString()
	 *            method, which is then displayed as the name of this value in
	 *            the ComboBox
	 * @param availableValues
	 *            the available values of this property. The objects in this
	 *            list should be of the same type as the initial values.
	 *            Furthermore they should contain the initial values
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GuiPropertyObjectSet(String name, String description,
			List initialValues, Set availableValues,
			GuiNotificationTarget target) {
		this(name, description, initialValues, availableValues, target, 70);
	}

	/**
	 * Creates a string list property. Furthermore, for each value in the string
	 * list it is also possible to select between a set of different values that
	 * belongs to each value in the string list and which is presented as a
	 * separate combobox in the GUI. So for each value in the string list there
	 * exists one additional property for which one value can be chosen.
	 * 
	 * @param name
	 *            String the name of this property
	 * @param description
	 *            String the description of this property (to be displayed as a
	 *            tool tip)
	 * @param initialValues
	 *            List the initial values of this property. The objects in this
	 *            list should either be simple strings or override the
	 *            toString() method, which is then displayed as the name of this
	 *            value in the ComboBox
	 * @param availableValues
	 *            Set the available values of this property. The objects in this
	 *            list should be of the same type as the initial values.
	 *            Furthermore they should contain the initial values
	 * @param propertyInitialValues
	 *            List a list of different values that belongs to each value in
	 *            the string list and from which one can be selected
	 * @param initialValuesAndInitValProp
	 *            HashMap for each value in the string list a default value
	 *            needs to be defined for the set of different values that
	 *            belongs to each value in the string list (for the property
	 *            that belongs to each value in the string list).
	 * @param target
	 *            GuiNotificationTarget the object to be notified as soon the
	 *            state of this property changes
	 */
	public GuiPropertyObjectSet(String name, String description,
			List initialValues, Set availableValues,
			List propertyInitialValues, HashMap initialValuesAndInitValProp,
			GuiNotificationTarget target) {
		this(name, description, initialValues, availableValues, target, 70);
		myPropertyInitialValues = propertyInitialValues;
		myPropertyInitialValuesPanel = new JPanel();
		myPropertyInitialValuesPanel.setBorder(BorderFactory.createEmptyBorder(
				0, 0, 0, 0));
		Iterator initVals = initialValues.iterator();
		while (initVals.hasNext()) {
			Object initVal = initVals.next();
			Object initValProperty = initialValuesAndInitValProp.get(initVal);
			// ensure that initValProperty is the first element in the set that
			// is
			// sent to the set enumeration
			ArrayList initValPropFirst = new ArrayList();
			initValPropFirst.add(initValProperty);
			Iterator propVals = propertyInitialValues.iterator();
			while (propVals.hasNext()) {
				Object propVal = propVals.next();
				if (!initValPropFirst.contains(propVal)) {
					initValPropFirst.add(propVal);
				}
			}
			// create a set enumeration
			myInitialValuesAndProperty.put(initVal,
					new GUIPropertyListEnumeration("", null, initValPropFirst,
							myTarget, 150));
		}
		// add a listener to myJList
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1
						&& myJList.getSelectedValue() != null) {
					Object value = myJList.getSelectedValue();
					// show the combobox for the selected item
					GUIPropertyListEnumeration enumProp = (GUIPropertyListEnumeration) myInitialValuesAndProperty
							.get(value);
					myPropertyInitialValuesPanel.removeAll();
					myPropertyInitialValuesPanel.add(enumProp
							.getPropertyPanel());
					// redraw
					myPanel.validate();
					myPanel.repaint();
				}
			}
		};
		myJList.addMouseListener(mouseListener);
	}

	/**
	 * Creates a string list property.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param initialValues
	 *            the initial values of this property. The objects in this list
	 *            should either be simple strings or override the toString()
	 *            method, which is then displayed as the name of this value in
	 *            the ComboBox
	 * @param availableValues
	 *            the available values of this property. The objects in this
	 *            list should be of the same type as the initial values.
	 *            Furthermore they should contain the initial values
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 * @param height
	 *            the preferred height for the list property (default value is
	 *            70)
	 */
	public GuiPropertyObjectSet(String name, String description,
			List initialValues, Set availableValues,
			GuiNotificationTarget target, int height) {
		myName = name;
		myDescription = description;
		myTarget = target;
		myPreferredHeight = height;
		// GUI attributes
		myAvailableValues = new HashSet();
		myAvailableValues.addAll(availableValues);
		myListModel = new DefaultListModel();
		if (initialValues != null) {
			Iterator it = initialValues.iterator();
			while (it.hasNext()) {
				Object current = it.next();
				myListModel.addElement(current);
				// remove initial values from list of available values
				myAvailableValues.remove(current);
			}
		}
		myAvailableList = new GUIPropertySetEnumeration("", "",
				myAvailableValues, null, 150);
		myJList = new JList(myListModel);
		myJList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		myJList.setLayoutOrientation(JList.VERTICAL);
		myJList.setVisibleRowCount(-1);
		myJList.addListSelectionListener(this);

		myAddButton = new SlickerButton("Add");
		myAddButton.setToolTipText("Adds current value from list to the right");
		myAddButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the "Add" button
			public void actionPerformed(ActionEvent e) {
				int index = myJList.getSelectedIndex(); // get selected index
				if (index == -1) { // no selection, so insert at beginning
					index = 0;
				} else { // add after the selected item
					index++;
				}

				Object toBeAdded = myAvailableList.getValue();
				myListModel.insertElementAt(toBeAdded, index);
				// Reset the combo box property
				myAvailableValues.remove(toBeAdded);
				myAvailableList = new GUIPropertySetEnumeration("", "",
						myAvailableValues, null, 150);
				// if no further attributes available anymore disable "Add"
				// button
				if (myAvailableValues.size() == 0) {
					myAddButton.setEnabled(false);
				}

				// Select the new item and make it visible
				myJList.setSelectedIndex(index);
				myJList.ensureIndexIsVisible(index);

				// Add the toBeAdded object to myInitalValuesAndProperty and
				// create a GUIPropertySetEnumeration for it
				createProp(toBeAdded);
				// redraw this property panel
				updateGUI();

				if (myTarget != null) {
					// notify owner of this property if specified
					myTarget.updateGUI();
				}
			}
		});

		myRemoveButton = new SlickerButton("Remove");
		myRemoveButton.setToolTipText("Removes selected value from list above");
		// is disabled per default since no element is selected per default
		myRemoveButton.setEnabled(false);
		myRemoveButton.addActionListener(new ActionListener() {
			// specify the action to be taken when pressing the "Remove" button
			public void actionPerformed(ActionEvent e) {
				int index = myJList.getSelectedIndex();
				Object toBeRemoved = myListModel.get(index);
				// Reset the combo box property
				myAvailableValues.add(toBeRemoved);
				myAvailableList = new GUIPropertySetEnumeration("", "",
						myAvailableValues, null, 150);
				// enable "Add" button (could have been enabled before if no
				// values were available anymore)
				myAddButton.setEnabled(true);
				myListModel.remove(index);
				int size = myListModel.getSize();
				if (size == 0) { // no value left, disable removing
					myRemoveButton.setEnabled(false);
				} else { // Select an index
					if (index == myListModel.getSize()) {
						// removed item in last position
						index--;
					}
					myJList.setSelectedIndex(index);
					myJList.ensureIndexIsVisible(index);
				}

				// Remove the toBeRemoved object from myInitalValuesAndProperty
				removeProp(toBeRemoved);

				// redraw this property panel
				updateGUI();

				if (myTarget != null) {
					// notify owner of this property if specified
					myTarget.updateGUI();
				}
			}
		});
	}

	/**
	 * Method to be automatically invoked as soon as the selection of the list
	 * changes.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {

			if (myJList.getSelectedIndex() == -1) {
				// No selection, disable "Remove" button
				myRemoveButton.setEnabled(false);

				// property list should also disappear, if provided
				// if (myPropertyInitialValuesPanel != null) {
				// myPropertyInitialValuesPanel.removeAll();
				// // redraw
				// myPanel.validate();
				// myPanel.repaint();
				// }

			} else {
				// Selection, enable the "Remove" button
				myRemoveButton.setEnabled(true);

				// property list should also be adapted, if provided
				// if (myPropertyInitialValuesPanel != null) {
				// Object value = myJList.getSelectedValue();
				// // show the combobox for the selected item
				// NotificationTargetPropertyListEnumeration enumProp =
				// (NotificationTargetPropertyListEnumeration)
				// myInitialValuesAndProperty.get(value);
				// myPropertyInitialValuesPanel.removeAll();
				// myPropertyInitialValuesPanel.add(enumProp.getPropertyPanel(),
				// BorderLayout.EAST);
				// // redraw
				// myPanel.validate();
				// myPanel.repaint();
				// }
			}
		}
	}

	/**
	 * Retrieves all the possible values specified for this property.
	 * 
	 * @return all possible values
	 */
	public List getAllValues() {
		ArrayList result = new ArrayList();
		Object[] currentValues = myListModel.toArray();
		for (int i = 0; i < currentValues.length; i++) {
			Object val = currentValues[i];
			result.add(val);
		}
		return result;
	}

	/**
	 * Retrieves all the possible values specified for this property together
	 * with the property that is specified for each possible value
	 * 
	 * @return TreeMap
	 */
	public HashMap getAllValuesWithProperty() {
		HashMap returnHMap = new HashMap();
		Iterator initVals = myInitialValuesAndProperty.keySet().iterator();
		while (initVals.hasNext()) {
			Object initVal = initVals.next();
			Object property = ((GUIPropertyListEnumeration) myInitialValuesAndProperty
					.get(initVal)).getValue();
			returnHMap.put(initVal, property);
		}
		return returnHMap;
	}

	/**
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		if (myPanel == null) {
			myPanel = new JPanel();
			myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.PAGE_AXIS));
			updateGUI();
		}
		return myPanel;
	}

	/*
	 * Helper method to redraw property panel as soon as list of selected values
	 * has changed.
	 */
	public void updateGUI() {
		myPanel.removeAll();

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
		JLabel myNameLabel = new JLabel(" " + myName);
		if (myDescription != null) {
			myNameLabel.setToolTipText(myDescription);
		}
		namePanel.add(myNameLabel);
		namePanel.add(Box.createHorizontalGlue());
		myPanel.add(namePanel);
		myPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JScrollPane listScroller = new JScrollPane(myJList);
		listScroller.setPreferredSize(new Dimension(250, myPreferredHeight));
		myPanel.add(listScroller);
		myPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JPanel buttonsPanel = new JPanel();
		buttonsPanel
				.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		buttonsPanel.add(myAddButton);
		buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonsPanel.add(myAvailableList.getPropertyPanel());
		buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(myRemoveButton);
		// in the case that a property exists for the data attributes add the
		// panel for
		// this property to the buttonsPanel
		if (myPropertyInitialValuesPanel != null) {
			buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			buttonsPanel.add(Box.createHorizontalGlue());
			buttonsPanel.add(myPropertyInitialValuesPanel);
		}
		myPanel.add(buttonsPanel);
		SlickerSwingUtils.injectTransparency(myPanel);

		// redraw
		myPanel.validate();
		myPanel.repaint();
	}

	/**
	 * In the case that a new value is added to the list, a separate
	 * ListEnumeration has to be created for the property of that value
	 * 
	 * @param toBeAdded
	 *            Object the new value to be added to the list
	 */
	private void createProp(Object toBeAdded) {
		GUIPropertyListEnumeration enumProp = new GUIPropertyListEnumeration(
				"", null, myPropertyInitialValues, myTarget, 150);
		// add the GUIPropertyListEnumeration to the myPropertyInitialValues
		// list
		myInitialValuesAndProperty.put(toBeAdded, enumProp);
	}

	/**
	 * In the case that a value is removed from the list, the property for that
	 * value needs to be removed.
	 * 
	 * @param toBeRemoved
	 *            Object the value to be removed from the list
	 */
	private void removeProp(Object toBeRemoved) {
		myInitialValuesAndProperty.remove(toBeRemoved);
		// remove the propertySetEnumeration from the panel
		myPropertyInitialValuesPanel.removeAll();
		// update the GUI
		myPanel.validate();
		myPanel.repaint();
	}

}
