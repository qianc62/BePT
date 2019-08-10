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

package org.processmining.framework.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.mining.logabstraction.LogRelations;
import org.processmining.mining.logabstraction.MutableLogRelations;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * Shows a dialog for editing log relations.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

public class LogRelationEditor extends JDialog {

	private LogRelations original;
	private RelationsUI relationsUI;
	private boolean ok;

	private JPanel mainPanel;
	private ToolTipComboBox selectBox;

	/**
	 * @param original
	 *            the log relations to use if the user presses the 'Revert to
	 *            original relations' button
	 */
	public LogRelationEditor(LogRelations original) {
		super(MainUI.getInstance(), "Edit log relations", true);
		this.original = original;
	}

	/**
	 * Returns the log relations as specified by the user.
	 * 
	 * @return the new log relations
	 */
	public LogRelations getLogRelations() {
		return relationsUI.getRelations();
	}

	/**
	 * Show the edit dialog. If this method returns true, the new log relations
	 * can be obtained using <code>getLogRelations()</code>.
	 * 
	 * @param current
	 *            the log relations to show initially in the dialog
	 * @param t
	 *            the log event to start editing (null if not specified)
	 * @return true if the user pressed ok, false otherwise
	 */
	public boolean edit(LogRelations current, LogEvent t) {
		if (current == null || current.getNumberElements() == 0) {
			JOptionPane
					.showMessageDialog(MainUI.getInstance(),
							"There are no log events, so there are no relations to edit.");
			return false;
		}

		relationsUI = new RelationsUI(current);
		try {
			jbInit();
			pack();

			// center dialog
			CenterOnScreen.center(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		int selectedIndex = Math.max(0, current.getLogEvents().indexOf(t));
		selectBox.setSelectedIndex(selectedIndex);
		relationsUI.selectSubpanel(selectedIndex);

		ok = false;
		setVisible(true);
		return ok;
	}

	/**
	 * Show edit dialog without pre-selecting a specific log event. Same as
	 * <code>edit(current, null)</code>.
	 * 
	 * @param current
	 *            the log relations to show initially in the dialog
	 * @return true if the user pressed ok, false otherwise
	 */
	public boolean edit(LogRelations current) {
		return edit(current, null);
	}

	private void jbInit() throws Exception {
		JButton okButton, cancelButton, revertButton;
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel selectEventPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				setVisible(false);
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		revertButton = new JButton("Revert to original relations");
		revertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				relationsUI.reinit(original);
				mainPanel.validate();
				mainPanel.repaint();
			}
		});

		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(revertButton);

		selectBox = new ToolTipComboBox(relationsUI);
		selectBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				relationsUI.selectSubpanel(selectBox.getSelectedIndex());
			}
		});
		selectEventPanel.add(new JLabel("Select element: "));
		selectEventPanel.add(selectBox);

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(selectEventPanel, BorderLayout.NORTH);
		mainPanel.add(relationsUI.getPanel(), BorderLayout.CENTER);
		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
		getContentPane().removeAll();
		getContentPane().add(mainPanel);
	}
}

class RelationsUI extends DefaultComboBoxModel {

	private MutableLogRelations relations;
	private JPanel mainPanel;
	private int selectedPanel;
	private JCheckBox[][] parallelBoxes;

	public RelationsUI(LogRelations relations) {
		this.relations = new MutableLogRelations(relations);
		mainPanel = new JPanel(new CardLayout());
		buildPanels();
		selectSubpanel(0);
	}

	public void reinit(LogRelations relations) {
		this.relations = new MutableLogRelations(relations);
		mainPanel.removeAll();
		buildPanels();
		selectSubpanel(selectedPanel);
	}

	public JPanel getPanel() {
		return mainPanel;
	}

	public void selectSubpanel(int index) {
		((CardLayout) mainPanel.getLayout()).show(mainPanel, "" + index);
		selectedPanel = index;
	}

	public LogRelations getRelations() {
		return relations;
	}

	public int getSize() {
		return relations.getLogEvents().size();
	}

	public Object getElementAt(int index) {
		LogEvent e = relations.getLogEvents().getEvent(index);
		return e.getModelElementName() + " (" + e.getEventType() + ")";
	}

	private void buildPanels() {
		int numEvents = relations.getLogEvents().size();

		parallelBoxes = new JCheckBox[numEvents][numEvents];
		for (int i = 0; i < numEvents; i++) {
			for (int j = 0; j < numEvents; j++) {
				LogEvent ev = relations.getLogEvents().getEvent(j);
				parallelBoxes[i][j] = new JCheckBox(ev.getModelElementName()
						+ " (" + ev.getEventType() + ")");
			}
		}

		for (int i = 0; i < numEvents; i++) {
			mainPanel.add(buildPanel(i), "" + i);
		}
	}

	private JPanel buildPanel(int index) {
		JPanel outerCheckBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		;
		JPanel checkBoxPanel = new JPanel(new GridBagLayout());
		JPanel panel = new JPanel(new BorderLayout());
		JCheckBox start, end, oll;
		LogEvent e = relations.getLogEvents().getEvent(index);
		final int finalIndex = index;

		// build 'start' checkbox
		start = new JCheckBox("Is a possible start task");
		if (relations.getStartInfo().get(index) > 0) {
			start.setSelected(true);
		}
		start.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				relations.getStartInfo().set(finalIndex,
						e.getStateChange() == ItemEvent.SELECTED ? 1.0 : 0.0);
			}
		});

		// build 'end' checkbox
		end = new JCheckBox("Is a possible end task");
		if (relations.getEndInfo().get(index) > 0) {
			end.setSelected(true);
		}
		end.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				relations.getEndInfo().set(finalIndex,
						e.getStateChange() == ItemEvent.SELECTED ? 1.0 : 0.0);
			}
		});

		// build 'oll' checkbox
		oll = new JCheckBox("Has a one length loop");
		if (relations.getOneLengthLoopsInfo().get(index) > 0) {
			oll.setSelected(true);
		}
		oll.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				relations.getOneLengthLoopsInfo().set(finalIndex,
						e.getStateChange() == ItemEvent.SELECTED ? 1.0 : 0.0);
			}
		});

		// build lists
		JPanel listsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
		listsPanel.add(constructCausalList("Followed by (->)", index,
				INDEX_TO_ALL));
		listsPanel
				.add(constructCausalList("Follows (<-)", index, ALL_TO_INDEX));
		listsPanel.add(constructParallelList("Parallel with (||)", index));
		listsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		// glue UI together
		checkBoxPanel.add(start, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		checkBoxPanel.add(end, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		checkBoxPanel.add(oll, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));

		outerCheckBoxPanel.add(checkBoxPanel);
		panel.add(outerCheckBoxPanel, BorderLayout.NORTH);
		panel.add(listsPanel, BorderLayout.CENTER);
		return panel;
	}

	private static final int INDEX_TO_ALL = 0, ALL_TO_INDEX = 1;

	private JPanel constructCausalList(String caption, final int index,
			final int direction) {
		final JCheckBox[] boxes = new JCheckBox[relations.getLogEvents().size()];
		JPanel panel = new JPanel(new BorderLayout());
		JList list = new CheckBoxList();
		DoubleMatrix2D causalMatrix = relations.getCausalFollowerMatrix();

		for (int i = 0; i < relations.getLogEvents().size(); i++) {
			LogEvent ev = relations.getLogEvents().getEvent(i);
			final int finalI = i;

			boxes[i] = new JCheckBox(ev.getModelElementName() + " ("
					+ ev.getEventType() + ")");

			if ((direction == INDEX_TO_ALL && causalMatrix.get(index, i) > 0)
					|| (direction == ALL_TO_INDEX && causalMatrix.get(i, index) > 0)) {
				boxes[i].setSelected(true);
			}
			boxes[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					relations.setCausalFollower(
							direction == INDEX_TO_ALL ? index : finalI,
							direction == INDEX_TO_ALL ? finalI : index,
							boxes[finalI].isSelected() ? 1.0 : 0.0);
				}
			});
		}
		list.setListData(boxes);

		panel.add(new JLabel(caption), BorderLayout.NORTH);
		panel.add(new JScrollPane(list), BorderLayout.CENTER);
		return panel;
	}

	private JPanel constructParallelList(String caption, final int index) {
		final JCheckBox[] boxes = parallelBoxes[index];
		JPanel panel = new JPanel(new BorderLayout());
		JList list = new CheckBoxList();
		DoubleMatrix2D parallelMatrix = relations.getParallelMatrix();

		for (int i = 0; i < relations.getLogEvents().size(); i++) {
			LogEvent ev = relations.getLogEvents().getEvent(i);
			final int finalI = i;

			if (parallelMatrix.get(index, i) > 0
					|| parallelMatrix.get(i, index) > 0) {
				boxes[i].setSelected(true);
			}
			boxes[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					relations.setParallel(index, finalI, boxes[finalI]
							.isSelected() ? 1.0 : 0.0);
					relations.setParallel(finalI, index, boxes[finalI]
							.isSelected() ? 1.0 : 0.0);
					parallelBoxes[finalI][index].setSelected(boxes[finalI]
							.isSelected());
				}
			});
		}
		list.setListData(boxes);

		panel.add(new JLabel(caption), BorderLayout.NORTH);
		panel.add(new JScrollPane(list), BorderLayout.CENTER);
		return panel;
	}
}

class CheckBoxList extends JList {

	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public CheckBoxList() {
		setCellRenderer(new CheckBoxCellRenderer());
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());

				if (index != -1) {
					JCheckBox checkbox = (JCheckBox) getModel().getElementAt(
							index);
					checkbox.doClick();
					repaint();
				}
			}
		});
		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == ' ' || e.getKeyChar() == '\n'
						|| e.getKeyChar() == '\r' && getSelectedIndex() != -1) {
					JCheckBox checkbox = (JCheckBox) getModel().getElementAt(
							getSelectedIndex());
					checkbox.doClick();
					repaint();
				}
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	protected class CheckBoxCellRenderer implements ListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JCheckBox checkbox = (JCheckBox) value;
			checkbox.setBackground(isSelected ? getSelectionBackground()
					: getBackground());
			checkbox.setForeground(isSelected ? getSelectionForeground()
					: getForeground());
			checkbox.setEnabled(isEnabled());
			checkbox.setFont(getFont());
			checkbox.setFocusPainted(false);
			checkbox.setBorderPainted(true);
			checkbox
					.setBorder(isSelected ? UIManager
							.getBorder("List.focusCellHighlightBorder")
							: noFocusBorder);
			return checkbox;
		}
	}
}
