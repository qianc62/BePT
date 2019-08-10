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

package org.processmining.mining.conversion;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * @author Eric Verbeek
 * @version 1.0
 */

public class CaseDataExtractorOptions extends JPanel /* implements ActionListener */{

	// The corresponding convertor to call when the Save button is pressed.
	private CaseDataExtractor theCaseDataExtractor = null;

	// We're using tabs.
	private JTabbedPane theTabbedPane = new JTabbedPane();

	// Except for the Save tab, all tabs use JLists.
	// Using this JList the user can select the items s/he's interested in.
	private JList theList0 = null;
	private JList theList1 = null;
	private JList theList2 = null;
	private JList theList3 = null;

	// Arrays to store the selected list items.
	private boolean[] theSelected0 = null;
	private boolean[] theSelected1 = null;
	private boolean[] theSelected2 = null;
	private boolean[] theSelected3 = null;

	/*
	 * // The Save tab (nr 9) uses a panel with a FileChooser and a Button. //
	 * The panel uses a GridBagLayout. private JPanel thePanel9 = new JPanel();
	 * private JFileChooser theFileChooser = new JFileChooser(); private JButton
	 * theSaveButton = new JButton("Save"); private BorderLayout theLayout9 =
	 * new BorderLayout();
	 */

	// Constructor. Stores the convertor and initializes.
	public CaseDataExtractorOptions(CaseDataExtractor vc) {
		theCaseDataExtractor = vc;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Initialization. Not much to do, we have to wait until the user
	// has pressed the Mine button. Only then we have access to the log.
	private void jbInit() throws Exception {
		// thePanel9.setLayout(theLayout9);

		this.setLayout(new BorderLayout());
		this.add(theTabbedPane);
	}

	// Clear the tabs.
	public void Clear() {
		theTabbedPane.removeAll();
	}

	// Add a tab labeled s containing a Jlist with the items from ss
	// to theTabbedPane. Select all items. Return the JList.
	private JList Add(String[] ss, String s) {
		// if (ss.length > 0) {
		JList l = new JList(ss);
		int indices[] = new int[ss.length];
		for (int i = 0; i < ss.length; i++) {
			indices[i] = i;
		}
		l.setSelectedIndices(indices);
		theTabbedPane.addTab(s, new JScrollPane(l));
		return l;
		// }
		// return null;
	}

	// Add a first tab.
	public void Add0(String[] ss, String s) {
		theList0 = Add(ss, s);
		theSelected0 = new boolean[ss.length];
	}

	// Add a second tab.
	public void Add1(String[] ss, String s) {
		theList1 = Add(ss, s);
		theSelected1 = new boolean[ss.length];
	}

	// Add a third tab.
	public void Add2(String[] ss, String s) {
		theList2 = Add(ss, s);
		theSelected2 = new boolean[ss.length];
	}

	// Add a fourth tab.
	public void Add3(String[] ss, String s) {
		theList3 = Add(ss, s);
		theSelected3 = new boolean[ss.length];
	}

	// Add the Save tab.
	public void Add9(String s) {
		// theFileChooser.setCurrentDirectory(new File("."));
		// theFileChooser.setFileFilter(new CsvFileFilter());
		// theFileChooser.setControlButtonsAreShown(false);
		// theFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		// thePanel9.removeAll();
		// thePanel9.add(theFileChooser, BorderLayout.CENTER);
		// thePanel9.add(theSaveButton, BorderLayout.SOUTH);
		// theFileChooser.addActionListener(this);
		// theSaveButton.addActionListener(this);
		// theTabbedPane.addTab(s, thePanel9);
	}

	// Return whether the i-th item on the first tab is selected.
	public boolean IsSelected0(int i) {
		return theSelected0[i];
	}

	// Return whether the i-th item on the second tab is selected.
	public boolean IsSelected1(int i) {
		return theSelected1[i];
	}

	// Return whether the i-th item on the third tab is selected.
	public boolean IsSelected2(int i) {
		return theSelected2[i];
	}

	// Return whether the i-th item on the fourth tab is selected.
	public boolean IsSelected3(int i) {
		return theSelected3[i];
	}

	// Convert the array with selected indices to an array of booelans.
	private boolean[] SetSelected(boolean[] selected, JList list) {
		int[] indices = list.getSelectedIndices();
		for (int i = 0; i < selected.length; i++) {
			selected[i] = false;
		}
		for (int i = 0; i < indices.length; i++) {
			selected[indices[i]] = true;
		}
		return selected;
	}

	// Kinda obsolete.
	public int getKind() {
		return 1;
	}

	/*
	 * // Check actions. public void actionPerformed(ActionEvent e) { // Check
	 * whether the action involved pressing the Save button. if
	 * (e.getActionCommand().equals("ApproveSelection")) { //if (e.getSource()
	 * == theSaveButton) { //theFileChooser.approveSelection(); // Check whether
	 * the user selected a file. if (theFileChooser.getSelectedFile() != null) {
	 * // Convert selection data. theSelected0 = SetSelected(theSelected0,
	 * theList0); theSelected1 = SetSelected(theSelected1, theList1);
	 * theSelected2 = SetSelected(theSelected2, theList2); theSelected3 =
	 * SetSelected(theSelected3, theList3); // Print the log to the file. try {
	 * theCaseDataExtractor.PrintLog(new FileOutputStream(theFileChooser.
	 * getSelectedFile().getAbsolutePath())); } catch (FileNotFoundException ex)
	 * { ex.printStackTrace(); } } } else if
	 * (e.getActionCommand().equals("CancelSelection")) {
	 * 
	 * } }
	 */
	public void printLog(BufferedWriter aWriter, String tag) throws IOException {
		theSelected0 = SetSelected(theSelected0, theList0);
		theSelected1 = SetSelected(theSelected1, theList1);
		theSelected2 = SetSelected(theSelected2, theList2);
		theSelected3 = SetSelected(theSelected3, theList3);
		theCaseDataExtractor.PrintLog(aWriter, tag);
	}
}
