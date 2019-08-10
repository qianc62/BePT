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

package org.processmining.analysis.ltlchecker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.ProcessInstanceVisualization;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;

/**
 * LTLVerificationResult is a JPanel representing two lists, one with good
 * process instances, and one with the bad ones. Between those lists, a
 * visualisation of an ProcessInstance can be made.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public class LTLVerificationResult extends JPanel implements Provider,
		ActionListener {

	// FIELDS
	/** The analysis algorithm. */
	LTLChecker checker;
	/** The log checked. */
	LogReader log;
	/** The list with the good ones. */
	ArrayList correctOnes;
	/** The intlist with ids of the good ones. */
	int[] correctIdList;
	/** NUmber of PIs represented by the list */
	int numCorrectPIs;
	/** The list with the bad ones. */
	ArrayList badOnes;
	/** The intlist with ids of the bas ones. */
	int[] badIdList;
	/** NUmber of PIs represented by the list */
	int numBadPIs;
	/** View the good ones: */
	JTable correctView;
	/** View the bad ones: */
	JTable badView;
	/** Current visualised good process Instance. */
	ProcessInstance piCorrect;
	/** Current visualised bad process instance. */
	ProcessInstance piBad;
	/** viewpane for good visjes. */
	JScrollPane correctViewPane;
	/** viewpane for bad visjes. */
	JScrollPane badViewPane;
	/** Formula name. */
	String formulaName;

	// CONSTRUCTORS
	/**
	 * Constructs a LTLVerificationResult for the given formula tested on the
	 * given log. The indices of the (in)correct instances are passed trough two
	 * arraylists. The number of instances represented by these lists in the two
	 * ints. The latter is in case of grouped traces.
	 * 
	 * @param formulaName
	 *            String
	 * @param log
	 *            LogReader
	 * @param correctOnes
	 *            ArrayList
	 * @param totalCorrectOnes
	 *            int
	 * @param badOnes
	 *            ArrayList
	 * @param totalBadOnes
	 *            int
	 */
	public LTLVerificationResult(String formulaName, LogReader log,
			ArrayList correctOnes, int totalCorrectOnes, ArrayList badOnes,
			int totalBadOnes) {
		this(formulaName, log, correctOnes, totalCorrectOnes, badOnes,
				totalBadOnes, null);

	}

	/**
	 * Constructs a LTLVerificationResult for the given formula tested on the
	 * given log. The indices of the (in)correct instances are passed trough two
	 * arraylists. The number of instances represented by these lists in the two
	 * ints. The latter is in case of grouped traces.
	 * 
	 * @param formulaName
	 *            String
	 * @param log
	 *            LogReader
	 * @param correctOnes
	 *            ArrayList
	 * @param totalCorrectOnes
	 *            int
	 * @param badOnes
	 *            ArrayList
	 * @param totalBadOnes
	 *            int
	 * @param sss
	 *            Substitutes
	 */
	public LTLVerificationResult(String formulaName, LogReader log,
			ArrayList correctOnes, int totalCorrectOnes, ArrayList badOnes,
			int totalBadOnes, Substitutes sss) {

		this.formulaName = formulaName;
		this.checker = checker;
		this.log = log;
		this.correctOnes = correctOnes;
		this.badOnes = badOnes;
		this.numCorrectPIs = totalCorrectOnes;
		this.numBadPIs = totalBadOnes;
		// Fill the list with good ids.
		Iterator i = correctOnes.iterator();
		correctIdList = new int[correctOnes.size()];
		int j = 0;

		while (i.hasNext()) {

			correctIdList[j] = ((CheckResult) i.next()).getNumberInLog();
			j++;

		}
		// Fill the list with bad ids.
		i = badOnes.iterator();
		badIdList = new int[badOnes.size()];
		j = 0;

		while (i.hasNext()) {

			badIdList[j] = ((CheckResult) i.next()).getNumberInLog();
			j++;

		}
		// create gui
		createGui(sss);
	}

	// METHODS

	public ProvidedObject[] getProvidedObjects() {
		ArrayList a = new ArrayList();

		if (this.piCorrect != null) {
			a.add(new ProvidedObject("Visualised correct process instance",
					new Object[] { piCorrect }));
		}

		if (this.piBad != null) {
			a.add(new ProvidedObject("Visualised incorrect process instance",
					new Object[] { piBad }));
		}

		if (correctIdList.length > 0) {
			try {
				a.add(new ProvidedObject("Correct instances (" + numCorrectPIs
						+ ")", new Object[] { LogReaderFactory.createInstance(
						log, correctIdList) }));
			} catch (Exception ex) {
				Message.add(ex.toString());
			}

		}

		if (badIdList.length > 0) {

			try {
				a.add(new ProvidedObject("Incorrect instances (" + numBadPIs
						+ ")", new Object[] { LogReaderFactory.createInstance(
						log, badIdList) }));
			} catch (Exception ex1) {
				Message.add(ex1.toString());
			}

		}

		try {
			a.add(new ProvidedObject("Whole log with results",
					new Object[] { log }));
		} catch (Exception ex) {
		}

		ProvidedObject[] o = new ProvidedObject[a.size()];

		for (int i = 0; i < a.size(); i++) {

			o[i] = (ProvidedObject) a.get(i);

		}

		return o;
	}

	public void actionPerformed(ActionEvent ae) {
		String action = ae.getActionCommand();

		if (action.equals("badvis")) {

			// visualise selected in bad list
			int row = badView.getSelectedRow();

			if (row >= 0) {

				createVisualisation(((CheckResult) badOnes.get(row))
						.getProcessInstance(log), false);

			}
			// else do nothing because there is nothing selected.

		} else if (action.equals("goodvis")) {

			// visualise selected in good list
			int row = correctView.getSelectedRow();

			if (row >= 0) {

				createVisualisation(((CheckResult) correctOnes.get(row))
						.getProcessInstance(log), true);

			}
			// else do nothing because there is nothing selected.

		}

	}

	private void createGui(Substitutes sss) {

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		String html = "<html><table>";
		html += "<tr valign=top, halign=left><td>Parameters:</td><td>"
				+ sss.toString() + "</td></tr>";
		html += "</table>";
		html += "</html>";

		JLabel formulaLabel = new JLabel("<html>Checked formula :  <b>"
				+ formulaName + "</b></html>");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10, 10, 10, 10);
		this.add(formulaLabel, c);
		// Show two lists with CheckResults
		JTabbedPane tp = new JTabbedPane();

		tp.add(
				"<html>Correct process instances (" + numCorrectPIs
						+ ")</html>", createCorrectTab());

		tp.add("<html>Incorrect process instances (" + numBadPIs + ")</html>",
				createBadTab());

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;

		JScrollPane scroll = new JScrollPane(new JLabel(html));
		scroll.setPreferredSize(new Dimension((int) scroll.getPreferredSize()
				.getWidth(), 50));

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				scroll, tp);
		split.setOneTouchExpandable(true);

		this.add(split, c);
	}

	private JSplitPane createCorrectTab() {
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		sp.setDividerLocation(200);
		sp.setOneTouchExpandable(true);
		// Create visualisationpane:
		correctViewPane = new JScrollPane();
		correctViewPane.setMinimumSize(new Dimension(300, 300));
		correctViewPane.setPreferredSize(new Dimension(300, 300));
		sp.setRightComponent(correctViewPane);
		// Table with vis knob
		JPanel pan = new JPanel();
		pan.setLayout(new BorderLayout());

		correctView = new JTable(new CheckResultModel(this.correctOnes,
				"name (nr similar)"));
		correctView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		correctView.setColumnSelectionAllowed(false);
		correctView.setRowSelectionAllowed(true);
		correctView.setCellSelectionEnabled(true);
		MouseListener correctMouse = new MouseAdapter() {

			public void mouseClicked(MouseEvent me) {

				if ((me.getClickCount() == 2)
						&& (me.getButton() == MouseEvent.BUTTON1)) {
					// There is a double click with left mouse button
					// check if the component clicked on is the JTable

					if (me.getComponent() == correctView) {
						// it is the table which is selected, so visualise the
						// selected item, if any.
						// Visualise selected row in good list
						int row = correctView.getSelectedRow();

						if (row >= 0) {

							createVisualisation(((CheckResult) correctOnes
									.get(row)).getProcessInstance(log), true);

						}
						// else do nothing because there is nothing selected.
					}

				}

			}
		};

		correctView.addMouseListener(correctMouse);
		JScrollPane correctSP = new JScrollPane(correctView);
		correctSP.setMinimumSize(new Dimension(200, 250));
		correctSP.setPreferredSize(new Dimension(200, 250));
		pan.add(correctSP, BorderLayout.CENTER);

		JButton goodVisButton = new JButton("Visualize selected");
		goodVisButton.setActionCommand("goodvis");
		goodVisButton.setMnemonic(KeyEvent.VK_V);
		goodVisButton.addActionListener(this);
		JPanel bpb = new JPanel();
		bpb.add(goodVisButton);
		pan.add(bpb, BorderLayout.PAGE_END);

		sp.setLeftComponent(pan);

		return sp;
	}

	private JSplitPane createBadTab() {
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		sp.setDividerLocation(200);
		sp.setOneTouchExpandable(true);
		// Create visualisationpane:
		badViewPane = new JScrollPane();
		badViewPane.setMinimumSize(new Dimension(300, 300));
		badViewPane.setPreferredSize(new Dimension(300, 300));
		sp.setRightComponent(badViewPane);
		// Table with vis knob
		JPanel pan = new JPanel();
		pan.setLayout(new BorderLayout());

		badView = new JTable(new CheckResultModel(this.badOnes,
				"name (nr similar)"));
		badView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		badView.setColumnSelectionAllowed(false);
		badView.setRowSelectionAllowed(true);
		badView.setCellSelectionEnabled(true);
		MouseListener badMouse = new MouseAdapter() {

			public void mouseClicked(MouseEvent me) {

				if ((me.getClickCount() == 2)
						&& (me.getButton() == MouseEvent.BUTTON1)) {
					// There is a double click with left mouse button
					// check if the component clicked on is the JTable

					if (me.getComponent() == badView) {
						// it is the table which is selected, so visualise the
						// selected item, if any.
						// Visualise selected row in good list
						int row = badView.getSelectedRow();

						if (row >= 0) {

							createVisualisation(
									((CheckResult) badOnes.get(row))
											.getProcessInstance(log), false);
						}
						// else do nothing because there is nothing selected.
					}

				}

			}
		};

		badView.addMouseListener(badMouse);
		JScrollPane badSP = new JScrollPane(badView);
		badSP.setMinimumSize(new Dimension(200, 250));
		badSP.setPreferredSize(new Dimension(200, 250));
		pan.add(badSP, BorderLayout.CENTER);

		JButton badVisButton = new JButton("Visualize selected");
		badVisButton.setActionCommand("badvis");
		badVisButton.setMnemonic(KeyEvent.VK_V);
		badVisButton.addActionListener(this);
		JPanel bpb = new JPanel();
		bpb.add(badVisButton);
		pan.add(bpb, BorderLayout.PAGE_END);

		sp.setLeftComponent(pan);

		return sp;
	}

	private void createVisualisation(ProcessInstance pi, boolean correct) {

		if (correct) {

			this.piCorrect = pi;
			correctViewPane.getViewport().add(
					new ProcessInstanceVisualization(pi)
							.getGrappaVisualization(), null);

		} else {

			this.piBad = pi;
			badViewPane.getViewport().add(
					new ProcessInstanceVisualization(pi)
							.getGrappaVisualization(), null);

		}
	}
}
