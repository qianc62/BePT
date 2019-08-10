package org.processmining.framework.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A string list property that can be readily displayed as it maintains its own
 * GUI panel. The property will be graphically represented as a list including
 * buttons to add or remove string values. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property value. Furthermore, a {@link GuiNotificationTarget
 * notification target} may be specified in order to be informed as soon as the
 * list of values has been changed. <br>
 * <br>
 * A typical usage scenario looks as follows: <br>
 * <br>
 * <code>
 * JPanel testPanel = new Panel(); // create parent panel <br>
 * testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.PAGE_AXIS)); <br>
 * ArrayList<String> values = new ArrayList<String>();
 * values.add("Anne");
 * values.add("Maria");
 * GUIPropertyStringList names = new GUIPropertyStringList("Names", values); <br>
 * testPanel.add(names.getPropertyPanel()); // add property <br>
 * return testPanel; <br>
 * </code>
 * 
 * @see getAllValues
 * @see getPropertyPanel
 */
public class GuiPropertyStringList implements ListSelectionListener {

	// property attributes
	private String myName;
	private String myDescription;
	private GuiNotificationTarget myTarget;
	private int myPreferredHeight;
	// GUI attributes
	private JList myJList;
	private DefaultListModel myListModel;
	private JButton myAddButton;
	private JButton myRemoveButton;
	private JTextField myTextField;

	private List<String> myValues;

	/**
	 * Creates a string list property without a discription and notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param values
	 *            the initial string values of this property
	 */
	public GuiPropertyStringList(String name, List<String> values) {
		this(name, null, values);
	}

	/**
	 * Creates a string list property without notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param values
	 *            the initial string values of this property
	 */
	public GuiPropertyStringList(String name, String description,
			List<String> values) {
		this(name, description, values, null, 50);
	}

	/**
	 * Creates a string list without a discription.
	 * 
	 * @param name
	 *            the name of this property
	 * @param values
	 *            the initial string values of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GuiPropertyStringList(String name, List<String> values,
			GuiNotificationTarget target) {
		this(name, null, values, target, 50);
	}

	/**
	 * Creates a string list property.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param values
	 *            the initial string values of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GuiPropertyStringList(String name, String description,
			List<String> values, GuiNotificationTarget target) {
		this(name, description, values, target, 50);
	}

	/**
	 * Creates a string list property.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param values
	 *            the initial string values of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 * @param height
	 *            the preferred height for the list property (default value is
	 *            50)
	 */
	public GuiPropertyStringList(String name, String description,
			List<String> values, GuiNotificationTarget target, int height) {
		myName = name;
		myDescription = description;
		myTarget = target;
		myPreferredHeight = height;
		myValues = values;
	}

	/**
	 * Method to be automatically invoked as soon as the selection of the list
	 * changes.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {

			if (myJList.getSelectedIndex() == -1) {
				// No selection, disable "Remove" button.
				myRemoveButton.setEnabled(false);

			} else {
				// Selection, enable the "Remove" button.
				myRemoveButton.setEnabled(true);
			}
		}
	}

	/**
	 * Retrieves all the possible values specified for this property.
	 * 
	 * @return all possible values
	 */
	public List<String> getAllValues() {
		ArrayList<String> result = new ArrayList<String>();
		Object[] currentValues = myValues.toArray();
		for (int i = 0; i < currentValues.length; i++) {
			String val = (String) currentValues[i];
			result.add(val);
		}
		return result;
	}

	/**
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		if (myListModel == null) {
			myListModel = new DefaultListModel();
			if (myValues != null) {
				Iterator<String> it = myValues.iterator();
				while (it.hasNext()) {
					myListModel.addElement(it.next());
				}
			}
			myJList = new JList(myListModel);
			myJList
					.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			myJList.setLayoutOrientation(JList.VERTICAL);
			myJList.setVisibleRowCount(-1);
			myJList.addListSelectionListener(this);

			myAddButton = new JButton("Add");
			myAddButton.setToolTipText("Adds new value to list");
			myAddButton.addActionListener(new ActionListener() {
				// specify the action to be taken when pressing the "Add" button
				public void actionPerformed(ActionEvent e) {
					// get new value
					String newVal = myTextField.getText();
					Object[] currentValues = myListModel.toArray();
					for (int i = 0; i < currentValues.length; i++) {
						String val = (String) currentValues[i];
						if (val.equals(newVal) == true) {
							// do not add if String value is already present
							return;
						}
					}
					int index = myJList.getSelectedIndex(); // get selected
					// index
					if (index == -1) { // no selection, so insert at beginning
						index = 0;
					} else { // add after the selected item
						index++;
					}
					myListModel.insertElementAt(newVal, index);
					myValues.add(index, newVal);
					// Reset the text field
					myTextField.requestFocusInWindow();
					myTextField.setText("");
					// Select the new item and make it visible
					myJList.setSelectedIndex(index);
					myJList.ensureIndexIsVisible(index);
					if (myTarget != null) {
						// notify owner of this property if specified
						myTarget.updateGUI();
					}
				}
			});

			myRemoveButton = new JButton("Remove");
			myRemoveButton.setToolTipText("Removes selected value from list");
			// is disabled per default since no element is selected per default
			myRemoveButton.setEnabled(false);
			myRemoveButton.addActionListener(new ActionListener() {
				// specify the action to be taken when pressing the "Remove"
				// button
				public void actionPerformed(ActionEvent e) {
					int index = myJList.getSelectedIndex();
					myListModel.remove(index);
					myValues.remove(index);
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
					if (myTarget != null) {
						// notify owner of this property if specified
						myTarget.updateGUI();
					}
				}
			});

			myTextField = new JTextField(10);
			myTextField.setSize(new Dimension(100, (int) myTextField
					.getPreferredSize().getHeight()));
			myTextField.setPreferredSize(new Dimension(100, (int) myTextField
					.getPreferredSize().getHeight()));
			myTextField.setMaximumSize(new Dimension(100, (int) myTextField
					.getPreferredSize().getHeight()));
			myTextField.setMinimumSize(new Dimension(100, (int) myTextField
					.getPreferredSize().getHeight()));
		}

		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
		JLabel myNameLabel = new JLabel(" " + myName);
		if (myDescription != null) {
			myNameLabel.setToolTipText(myDescription);
		}
		namePanel.add(myNameLabel);
		namePanel.add(Box.createHorizontalGlue());
		resultPanel.add(namePanel);
		resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JScrollPane listScroller = new JScrollPane(myJList);

		listScroller.setPreferredSize(new Dimension(250, myPreferredHeight));

		resultPanel.add(listScroller);
		resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JPanel buttonsPanel = new JPanel();
		buttonsPanel
				.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		buttonsPanel.add(myAddButton);
		buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonsPanel.add(myTextField);
		buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(myRemoveButton);
		resultPanel.add(buttonsPanel);

		return resultPanel;
	}
}
