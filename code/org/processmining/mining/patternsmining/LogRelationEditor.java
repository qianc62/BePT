/*
 * Created on 16 janv. 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.processmining.mining.patternsmining;

/**
 * @author WALID
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.log.LogEvent;

public class LogRelationEditor extends JDialog {
	private DependenciesTables original;
	private RelationsUI relationsUI;
	private boolean ok;

	private JPanel mainPanel;
	private JComboBox selectBox;

	// *********************************************** walid
	// TO DO MainUI a remplacer par MainUI.getInstance()
	private Frame MainUI;

	public LogRelationEditor(Frame owner, DependenciesTables original) {
		// super(MainUI.getInstance(), "Edit log relations", true);
		super(owner, "Edit log relations", true);
		MainUI = owner;
		this.original = original;
	}

	/**
	 * Returns the log relations as specified by the user.
	 * 
	 * @return the new log relations
	 */
	public DependenciesTables getLogRelations() {
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
	public boolean edit(JFrame frametest, DependenciesTables current, LogEvent t) {
		// ////////*************walid enle
		MainUI = frametest;

		if (current == null || current.getLogEvents().size() == 0) {
			JOptionPane
					.showMessageDialog(MainUI,
							"There are no log events, so there are no relations to edit.");
			return false;
		}

		relationsUI = new RelationsUI(current);
		try {
			jbInit();
			pack();

			// center dialog
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation(Math.max(0, (screenSize.width - getSize().width) / 2),
					Math.max(0, (screenSize.height - getSize().height) / 2));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		int selectedIndex = Math.max(0, current.getLogEvents().indexOf(t));
		selectBox.setSelectedIndex(selectedIndex);
		relationsUI.selectSubpanel(selectedIndex);

		ok = false;
		show();
		return ok;
	}

	private void jbInit() throws Exception {
		JButton okButton, cancelButton, revertButton;
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel selectEventPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				hide();
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});

		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

		selectBox = new JComboBox(relationsUI);
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

	private DependenciesTables relations;
	private JPanel mainPanel;
	private int selectedPanel;
	private JCheckBox[][] parallelBoxes;

	public RelationsUI(DependenciesTables relations) {
		this.relations = relations;
		mainPanel = new JPanel(new CardLayout());
		buildPanels();
		selectSubpanel(0);
	}

	public void reinit(DependenciesTables relations) {
		this.relations = relations;
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

	public DependenciesTables getRelations() {
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
		start = new JCheckBox("Is a start task");
		if (relations.getStartInfo().get(index) == 0) {
			start.setSelected(true);
		}
		start.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				relations.getStartInfo().set(finalIndex,
						e.getStateChange() == ItemEvent.SELECTED ? 1.0 : 0.0);
			}
		});

		// build 'end' checkbox
		end = new JCheckBox("Is an end task");
		if (relations.getEndInfo().get(index) == 0) {
			end.setSelected(true);
		}
		end.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				relations.getEndInfo().set(finalIndex,
						e.getStateChange() == ItemEvent.SELECTED ? 1.0 : 0.0);
			}
		});

		// build lists
		JPanel listsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
		listsPanel.add(constructCompletePreceederList("Always preceed(s)",
				index));
		listsPanel
				.add(constructCompletefollowersList("Always follow(s)", index));
		listsPanel.add(constructConcurrentList("Parallel with ", index));
		listsPanel.add(constructPartialPreceederList("Possibly  preceed(s)",
				index));
		listsPanel.add(constructPartialfollowersList("Possibly follow(s)",
				index));
		listsPanel.add(constructconcurrentwidth(index));
		listsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		// glue UI together
		checkBoxPanel.add(start, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		checkBoxPanel.add(end, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));

		outerCheckBoxPanel.add(checkBoxPanel);
		panel.add(outerCheckBoxPanel, BorderLayout.NORTH);
		panel.add(listsPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel constructconcurrentwidth(final int index) {
		JPanel panel = new JPanel(new BorderLayout());
		double caption = relations.getACWWidthMatrix().get(index);
		panel.add(new JLabel("Concurrent Window Width = \n" + caption));
		return panel;
	}

	private JPanel constructCompletePreceederList(String caption,
			final int index) {
		JPanel panel = new JPanel(new BorderLayout());
		JList list = new JList();
		Vector completePreceeder = relations.getcompletePreceeder()[index];
		int nbPreceeder = completePreceeder.size();
		final String[] boxes = new String[nbPreceeder];

		for (int i = 0; i < nbPreceeder; i++) {
			int temppreceder = ((Integer) completePreceeder.get(i)).intValue();
			LogEvent ev = relations.getLogEvents().getEvent(temppreceder);
			final int finalI = i;

			boxes[i] = ev.getModelElementName() + " (" + ev.getEventType()
					+ ")";
		}

		list.setListData(boxes);

		panel.add(new JLabel(caption), BorderLayout.NORTH);
		panel.add(new JScrollPane(list), BorderLayout.CENTER);
		return panel;
	}

	private JPanel constructPartialPreceederList(String caption, final int index) {
		JPanel panel = new JPanel(new BorderLayout());
		JList list = new JList();
		Vector partialPreceeder = relations.getpartialPreceeder()[index];
		int nbPreceeder = partialPreceeder.size();
		final String[] boxes = new String[nbPreceeder];

		for (int i = 0; i < nbPreceeder; i++) {
			int temppreceder = ((Integer) partialPreceeder.get(i)).intValue();
			LogEvent ev = relations.getLogEvents().getEvent(temppreceder);
			double finalI = relations.getfinalcausalprecedents().get(index,
					temppreceder) * 100;

			boxes[i] = ev.getModelElementName() + " (" + ev.getEventType()
					+ ")  " + finalI + "%";
		}

		list.setListData(boxes);

		panel.add(new JLabel(caption), BorderLayout.NORTH);
		panel.add(new JScrollPane(list), BorderLayout.CENTER);
		return panel;
	}

	private JPanel constructCompletefollowersList(String caption,
			final int index) {
		JPanel panel = new JPanel(new BorderLayout());
		JList list = new JList();
		Vector completefollowers = relations.getcompletefollowers()[index];
		int nbPreceeder = completefollowers.size();
		final String[] boxes = new String[nbPreceeder];

		for (int i = 0; i < nbPreceeder; i++) {
			int temppreceder = ((Integer) completefollowers.get(i)).intValue();
			LogEvent ev = relations.getLogEvents().getEvent(temppreceder);
			final int finalI = i;

			boxes[i] = ev.getModelElementName() + " (" + ev.getEventType()
					+ ")";
		}

		list.setListData(boxes);

		panel.add(new JLabel(caption), BorderLayout.NORTH);
		panel.add(new JScrollPane(list), BorderLayout.CENTER);
		return panel;
	}

	private JPanel constructPartialfollowersList(String caption, final int index) {
		JPanel panel = new JPanel(new BorderLayout());
		JList list = new JList();
		Vector partialfollowers = relations.getpartialfollowers()[index];
		int nbPreceeder = partialfollowers.size();
		final String[] boxes = new String[nbPreceeder];

		for (int i = 0; i < nbPreceeder; i++) {
			int temppreceder = ((Integer) partialfollowers.get(i)).intValue();
			LogEvent ev = relations.getLogEvents().getEvent(temppreceder);
			double finalI = relations.getfinalcausalfollowers().get(index,
					temppreceder) * 100;

			boxes[i] = ev.getModelElementName() + " (" + ev.getEventType()
					+ ")  " + finalI + "%";
		}

		list.setListData(boxes);

		panel.add(new JLabel(caption), BorderLayout.NORTH);
		panel.add(new JScrollPane(list), BorderLayout.CENTER);
		return panel;
	}

	private JPanel constructConcurrentList(String caption, final int index) {
		JPanel panel = new JPanel(new BorderLayout());
		JList list = new JList();
		Vector Concurrent = relations.getConcurrent()[index];
		int nbPreceeder = Concurrent.size();
		final String[] boxes = new String[nbPreceeder];

		for (int i = 0; i < Concurrent.size(); i++) {
			int temppreceder = ((Integer) Concurrent.get(i)).intValue();
			LogEvent ev = relations.getLogEvents().getEvent(temppreceder);
			final int finalI = i;

			boxes[i] = ev.getModelElementName() + " (" + ev.getEventType()
					+ ")";
		}

		list.setListData(boxes);

		panel.add(new JLabel(caption), BorderLayout.NORTH);
		panel.add(new JScrollPane(list), BorderLayout.CENTER);
		return panel;
	}

	private JPanel constructPreceederList(String caption, final int index) {
		JPanel panel = new JPanel(new BorderLayout());
		JList list = new JList();
		Vector completePreceeder = relations.getcompletePreceeder()[index];
		Vector partialPreceeder = relations.getpartialPreceeder()[index];
		int nbPreceeder = completePreceeder.size() + partialPreceeder.size();
		final String[] boxes = new String[nbPreceeder];

		for (int i = 0; i < completePreceeder.size(); i++) {
			int temppreceder = ((Integer) completePreceeder.get(i)).intValue();
			LogEvent ev = relations.getLogEvents().getEvent(temppreceder);
			final int finalI = i;

			boxes[i] = ev.getModelElementName() + " (" + ev.getEventType()
					+ ")";
		}

		for (int i = completePreceeder.size(); i < nbPreceeder; i++) {
			int temppreceder = ((Integer) partialPreceeder.get(i)).intValue();
			LogEvent ev = relations.getLogEvents().getEvent(temppreceder);
			final int finalI = i;

			boxes[i] = ev.getModelElementName() + " (" + ev.getEventType()
					+ ")";
		}

		list.setListData(boxes);

		panel.add(new JLabel(caption), BorderLayout.NORTH);
		panel.add(new JScrollPane(list), BorderLayout.CENTER);
		return panel;
	}

	private JPanel constructfollowersList(String caption, final int index) {
		JPanel panel = new JPanel(new BorderLayout());
		JList list = new JList();
		Vector completefollowers = relations.getcompletefollowers()[index];
		Vector partialfollowers = relations.getpartialfollowers()[index];
		int nbPreceeder = completefollowers.size() + partialfollowers.size();
		final String[] boxes = new String[nbPreceeder];

		for (int i = 0; i < completefollowers.size(); i++) {
			int temppreceder = ((Integer) completefollowers.get(i)).intValue();
			LogEvent ev = relations.getLogEvents().getEvent(temppreceder);
			final int finalI = i;

			boxes[i] = ev.getModelElementName() + " (" + ev.getEventType()
					+ ")";
		}

		for (int i = completefollowers.size(); i < nbPreceeder; i++) {
			int temppreceder = ((Integer) partialfollowers.get(i)).intValue();
			LogEvent ev = relations.getLogEvents().getEvent(temppreceder);
			final int finalI = i;

			boxes[i] = ev.getModelElementName() + " (" + ev.getEventType()
					+ ")";
		}

		list.setListData(boxes);

		panel.add(new JLabel(caption), BorderLayout.NORTH);
		panel.add(new JScrollPane(list), BorderLayout.CENTER);
		return panel;
	}

}