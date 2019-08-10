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

package org.processmining.mining.organizationmining.ui;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import java.awt.Dimension;
import org.processmining.framework.models.orgmodel.Task;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.OrgEntity;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import org.processmining.mining.organizationmining.OrgMiningResult;

/**
 * @author not attributable
 * @version 1.0
 */
public class TaskOrgEntityPanel extends JSplitPane {

	private OrgModel orgModel = null;
	private OrgMiningResult parentPanel = null;
	private TaskOrgEntityTablePanel tablePanel = null;

	private JButton changeTaskButton = new JButton("Change Task Property");
	private JButton addTaskButton = new JButton("Add Task");
	/** adds a new group */
	private JButton removeTaskButton = new JButton("Remove Task");
	/** removes existing group */

	private JButton changeOrgEntityButton = new JButton(
			"Change OrgEntity Property");
	private JButton addOrgEntityButton = new JButton("Add OrgEntity");
	/** adds a new resource */
	private JButton removeOrgEntityButton = new JButton("Remove OrgEntity");

	/** removes existing resource */

	public TaskOrgEntityPanel(OrgModel orgmod, OrgMiningResult parentpanel) {

		this.orgModel = orgmod;
		this.parentPanel = parentpanel;
		init();
	}

	public void init() {
		tablePanel = new TaskOrgEntityTablePanel(orgModel, parentPanel);

		this.setLayout(new BorderLayout());
		this.add(tablePanel, BorderLayout.CENTER);
		initButtonPanel();

	}

	private void initButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
		addTaskButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(addTaskButton);
		removeTaskButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(removeTaskButton);
		changeTaskButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(changeTaskButton);
		addOrgEntityButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(addOrgEntityButton);
		removeOrgEntityButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(removeOrgEntityButton);
		changeOrgEntityButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(changeOrgEntityButton);
		this.add(buttonPanel, BorderLayout.WEST);
		registerGuiActionListener();
	}

	/**
	 * Connect GUI elements with functionality to create interaction.
	 */
	private void registerGuiActionListener() {
		addTaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Input Task ID
				String id = JOptionPane.showInputDialog(null, "input Task ID?");

				// check wheather ID is already in Org Entities
				if (id != null && !orgModel.hasTask(id)) {
					// Input Task Name
					String name = JOptionPane.showInputDialog(null,
							"input Task Name?");
					// Input Task Type
					String type = JOptionPane.showInputDialog(null,
							"input Type?");
					/*
					 * (String) JOptionPane.showInputDialog( null,
					 * "Which type?", "Type", JOptionPane.QUESTION_MESSAGE,
					 * null, // Use default icon
					 * OrgEntity.ORGENTITYTYPE_ARRAYLIST.toArray(),
					 * OrgEntity.ORGENTITYTYPE_ARRAYLIST.size() - 1);
					 */
					if (name != null && type != null) {
						Task newTask = new Task(id, name, type);
						orgModel.addTask(newTask);
						// Redraw tablePanel
						redrawTable();
					}

				} else
					JOptionPane.showMessageDialog(null,
							"ID is duplicated or has null value");
			}
		});
		removeTaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Task tempTask = tablePanel.getSeletedTask();
				if (tempTask == null)
					return;
				// remove Task from Org Model
				orgModel.removeTask(tempTask);
				tempTask = null;

				// Redraw tablePanel
				redrawTable();
			}
		});
		changeTaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Task tempTask = tablePanel.getSeletedTask();
				if (tempTask == null)
					return;
				// input Task ID
				String id = JOptionPane.showInputDialog(null, "input Task ID?",
						tempTask.getID());

				if (id != null) {
					// check wheather ID is already in Org Entities
					// if not, change both ID and key of HashMap for Org
					// Entities
					if (orgModel.changeTaskID(tempTask.getID(), id)) {
						// change Task Name
						String name = JOptionPane.showInputDialog(null,
								"input Task Name?", tempTask.getName());
						if (name != null)
							tempTask.setName(name);
						// change Org Entity Type
						String type = JOptionPane.showInputDialog(null,
								"input Task Name?", tempTask.getEventType());
						if (type != null)
							tempTask.setEventType(type);
						// Redraw tablePanel
						redrawTable();
					} else
						JOptionPane.showMessageDialog(null,
								"ID is duplicated or has null value");
				}
			}
		});
		addOrgEntityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Input Org Entity ID
				String id = JOptionPane.showInputDialog(null,
						"input Org Entity ID?");

				// check wheather ID is already in Org Entities
				if (id != null && !orgModel.hasOrgEntity(id)) {
					// Input Org Entity Name
					String name = JOptionPane.showInputDialog(null,
							"input Org Entity Name?");
					// Input Org Entity Type
					String type = (String) JOptionPane.showInputDialog(null,
							"Which type?", "Type",
							JOptionPane.QUESTION_MESSAGE,
							null, // Use default icon
							OrgEntity.ORGENTITYTYPE_ARRAYLIST.toArray(),
							OrgEntity.ORGENTITYTYPE_ARRAYLIST.size() - 1);

					if (name != null && type != null) {
						OrgEntity newEntity = new OrgEntity(id, name, type);
						orgModel.addOrgEntity(newEntity);
						// Redraw tablePanel
						redrawTable();
					}

				} else
					JOptionPane.showMessageDialog(null,
							"ID is duplicated or has null value");
			}
		});
		removeOrgEntityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OrgEntity tempEntity = tablePanel.getSeletedOrgEntity();
				if (tempEntity == null)
					return;
				// remove Org Entity from Org Model
				orgModel.removeOrgEntity(tempEntity);
				tempEntity = null;

				// Redraw tablePanel
				redrawTable();
			}
		});
		changeOrgEntityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OrgEntity tempEntity = tablePanel.getSeletedOrgEntity();
				if (tempEntity == null)
					return;

				// input Org Entity ID
				String id = JOptionPane.showInputDialog(null,
						"input Org Entity ID?", tempEntity.getID());

				if (id != null) {
					// check wheather ID is already in Org Entities
					// if not, change both ID and key of HashMap for Org
					// Entities
					if (orgModel.changeOrgEntityID(tempEntity.getID(), id)) {
						// change Org Entity Name
						String name = JOptionPane.showInputDialog(null,
								"input Org Entity Name?", tempEntity.getName());
						if (name != null)
							tempEntity.setName(name);

						// change Org Entity Type
						String type = (String) JOptionPane.showInputDialog(
								null, "choose Org Entity Type?",
								"Org Entity Type",
								JOptionPane.QUESTION_MESSAGE,
								null, // Use default icon
								OrgEntity.ORGENTITYTYPE_ARRAYLIST.toArray(),
								tempEntity.getEntityType());
						if (type != null)
							tempEntity.setEntityType(type);

						// Redraw tablePanel
						redrawTable();
					} else
						JOptionPane.showMessageDialog(null,
								"ID is duplicated or has null value");
				}
			}
		});
	}

	void redrawTable() {
		// added by Song
		parentPanel.updateActivitySet();

		this.remove(tablePanel);
		tablePanel = null;
		tablePanel = new TaskOrgEntityTablePanel(orgModel, parentPanel);
		this.add(tablePanel, BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}
}
