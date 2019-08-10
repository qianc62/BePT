package org.processmining.framework.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.table.AbstractTableModel;

import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.ui.DoubleClickTable;

/**
 * Represents a panel that consists of a table model on the left and displays
 * the contents of the double-clicked element on the right. <br>
 * Holds any list of {@link GuiDisplayable} objects.
 * 
 * @author arozinat
 */
public class GenericTableModelPanel extends JPanel {

	private List<GuiDisplayable> myList = new ArrayList<GuiDisplayable>();
	private String myName;
	// GUI attributes
	private DoubleClickTable myTable;
	private JButton updateViewButton = new JButton("Update view");
	private JPanel myViewPanel = new JPanel();
	private GuiNotificationTargetStateful notificationTarget = null;
	protected static Color bgColor;

	/**
	 * Constructor taking a set of displayable objects.
	 * 
	 * @param set
	 *            the set of objects to be displayed
	 * @param name
	 *            the name to be displayed in the table
	 * @param target
	 *            the object to be informed as soon a new element has been
	 *            double-clicked in this table model
	 */
	public GenericTableModelPanel(Set<GuiDisplayable> set, String name,
			GuiNotificationTargetStateful target) {
		this(set, name);
		notificationTarget = target;
	}

	/**
	 * Constructor taking a set of displayable objects.
	 * 
	 * @param set
	 *            the set of objects to be displayed
	 * @param name
	 *            the name to be displayed in the table
	 */
	public GenericTableModelPanel(Set<GuiDisplayable> set, String name) {
		myName = name;
		// convert set into list
		if (set != null) {
			Iterator<GuiDisplayable> it = set.iterator();
			while (it.hasNext()) {
				myList.add(it.next());
			}
		}
		// create the double click table and the content view
		createGUI();
	}

	/**
	 * Constructor taking a list of displayable objects.
	 * 
	 * @param list
	 *            the list of objects to be displayed
	 * @param name
	 *            the name to be displayed in the table
	 */
	public GenericTableModelPanel(List<GuiDisplayable> list, String name) {
		myList = list;
		myName = name;
		// create the double click table and the content view
		createGUI();
	}

	/**
	 * Constructor taking a list of displayable objects.
	 * 
	 * @param list
	 *            the list of objects to be displayed
	 * @param name
	 *            the name to be displayed in the table
	 * @param helpText
	 *            the user help text to be displayed above the table and its
	 *            contents
	 * @param target
	 *            the object to be informed as soon a new element has been
	 *            double-clicked in this table model
	 */
	public GenericTableModelPanel(List<GuiDisplayable> list, String name,
			String helpText, GuiNotificationTargetStateful target) {
		this(list, name, helpText, target, null);
	}

	public GenericTableModelPanel(List<GuiDisplayable> list, String name,
			String helpText, GuiNotificationTargetStateful target, Color color) {
		this(list, name, helpText);
		notificationTarget = target;
		bgColor = color;
	}

	/**
	 * Constructor taking a list of displayable objects.
	 * 
	 * @param list
	 *            the list of objects to be displayed
	 * @param name
	 *            the name to be displayed in the table
	 * @param helpText
	 *            the user help text to be displayed above the table and its
	 *            contents
	 */
	public GenericTableModelPanel(List<GuiDisplayable> list, String name,
			String helpText) {
		myList = list;
		myName = name;
		// create the double click table and the content view
		createGUI(helpText);
	}

	/**
	 * Retrieves the currently selected objects in the table.
	 * 
	 * @return the objects that belong to the currently selected rows in the
	 *         table
	 */
	public List<GuiDisplayable> getSelected() {
		List<GuiDisplayable> selected = new ArrayList<GuiDisplayable>();
		int[] indexArray = myTable.getSelectedRows();
		for (int i = 0; i < indexArray.length; i++) {
			selected.add(myList.get(indexArray[i]));
		}
		return selected;
	}

	/**
	 * Method actually creating the GUI representation (without a helpful
	 * comment at the top of the "table element content" window).
	 */
	private void createGUI() {
		createGUI(null);
	}

	/**
	 * Method actually creating the GUI representation.
	 * 
	 * @param helpDescription
	 *            the help text to be displayed at the top of the content frame
	 */
	private void createGUI(String helpDescription) {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		if (helpDescription != null) {
			GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
					helpDescription);
			this.add(helpText.getPropertyPanel(), BorderLayout.NORTH);
		}

		// fill the table
		myTable = new DoubleClickTable(new DisplayableObjectsTable(),
				updateViewButton);
		// build left GUI part
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		leftPanel.add(new JScrollPane(myTable));

		myViewPanel.setLayout(new BoxLayout(myViewPanel, BoxLayout.PAGE_AXIS));
		myViewPanel.setOpaque(false);
		myViewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, myViewPanel);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.setDividerLocation(150);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(3);

		JPanel outmostLayer = new JPanel(new BorderLayout());
		outmostLayer.setBorder(BorderFactory.createEmptyBorder());
		outmostLayer.add(splitPane);

		this.add(outmostLayer, BorderLayout.CENTER);

		if (bgColor != null) {
			this.setBackground(bgColor);
			SlickerSwingUtils.injectTransparency(this);
			this.setBorder(BorderFactory.createEmptyBorder());
		}

		// connect functionality to GUI elements
		registerGuiActionListener();
	}

	/**
	 * Connect GUI elements with functionality to create interaction.
	 */
	private void registerGuiActionListener() {
		// specify button actions
		updateViewButton.addActionListener(new ActionListener() {
			// specifies the action to be taken when a decision point has been
			// double-clicked
			public void actionPerformed(ActionEvent e) {
				updateViews();
			}
		});
	}

	/**
	 * Updates the view according to the currently selected HLActivity.
	 */
	private void updateViews() {
		// determine currently selected task - only one since double-clicked
		int[] indexArray = myTable.getSelectedRows();
		GuiDisplayable obj = myList.get(indexArray[0]);
		// update view for new activity
		myViewPanel.removeAll();
		myViewPanel.add(Box.createVerticalGlue());

		JComponent scroller;
		JPanel content = obj.getPanel();
		if (bgColor != null) {
			scroller = GuiUtilities.getSimpleScrollable(content, bgColor);
			SlickerSwingUtils.injectTransparency(content);
		} else {
			scroller = new JScrollPane(content);
		}

		scroller.setBorder(BorderFactory.createEmptyBorder());
		myViewPanel.add(scroller);

		myViewPanel.add(Box.createVerticalGlue());
		myViewPanel.validate();
		myViewPanel.repaint();

		// potentially also notify the parent about the newly selected object
		if (notificationTarget != null) {
			notificationTarget.stateHasChanged(obj);
		}
	}

	/**
	 * Private data structure for the table containing the displayable objects.
	 */
	private class DisplayableObjectsTable extends AbstractTableModel {

		/**
		 * Specify the headings for the columns.
		 * 
		 * @param col
		 *            The column specified.
		 * @return The heading of the respective column.
		 */
		public String getColumnName(int col) {
			// heading of single column
			return myName;
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of displayable objects in the model
		 */
		public int getRowCount() {
			return myList.size();
		}

		/**
		 * Specifiy the number of columns.
		 * 
		 * @return Always 1.
		 */
		public int getColumnCount() {
			return 1;
		}

		/**
		 * Method to fill a certain field of the table with contents.
		 * 
		 * @param row
		 *            The specified row.
		 * @param column
		 *            The specified column.
		 * @return The content to display at the table field specified.
		 */
		public Object getValueAt(int row, int column) {
			// fill in name of the decision point
			return myList.get(row);
		}
	}
}
