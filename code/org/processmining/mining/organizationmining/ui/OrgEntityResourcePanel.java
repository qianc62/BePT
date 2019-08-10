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

import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;
import java.awt.Dimension;
import org.processmining.framework.models.orgmodel.OrgEntity;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import org.processmining.mining.organizationmining.OrgMiningResult;

public class OrgEntityResourcePanel extends JPanel {

	private OrgModel orgModel = null;
	private OrgEntityResourceTablePanel tablePanel = null;
	private OrgMiningResult parentPanel;

	private JButton changeOrgEntityButton = new JButton(
			"Change OrgEntity Property");
	private JButton changeResourceButton = new JButton(
			"Change Resource Property");
	private JButton addOrgEntityButton = new JButton("Add OrgEntity");
	/** adds a new group */
	private JButton removeOrgEntityButton = new JButton("Remove OrgEntity");
	/** removes existing group */
	private JButton addResourceButton = new JButton("Add Resource");
	/** adds a new resource */
	private JButton removeResourceButton = new JButton("Remove Resource");

	/** removes existing resource */

	public OrgEntityResourcePanel(OrgModel orgmod, OrgMiningResult parentpanel) {
		this.orgModel = orgmod;
		this.parentPanel = parentpanel;
		init();
	}

	public void init() {
		tablePanel = new OrgEntityResourceTablePanel(orgModel, parentPanel);

		this.setLayout(new BorderLayout());
		this.add(tablePanel, BorderLayout.CENTER);
		initButtonPanel();

	}

	private void initButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
		addOrgEntityButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(addOrgEntityButton);
		removeOrgEntityButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(removeOrgEntityButton);
		changeOrgEntityButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(changeOrgEntityButton);
		addResourceButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(addResourceButton);
		removeResourceButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(removeResourceButton);
		changeResourceButton.setMaximumSize(new Dimension(200, 28));
		buttonPanel.add(changeResourceButton);
		this.add(buttonPanel, BorderLayout.WEST);
		registerGuiActionListener();
	}

	/**
	 * Connect GUI elements with functionality to create interaction.
	 */
	private void registerGuiActionListener() {
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

		addResourceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Input Org Entity ID
				String id = JOptionPane.showInputDialog(null,
						"input Resource ID?");

				// check wheather ID is already in Org Entities
				if (id != null && !orgModel.hasResource(id)) {
					// Input Org Entity Name
					String name = JOptionPane.showInputDialog(null,
							"input Resource Name?");
					// Input Org Entity Type
					if (name != null) {
						Resource newRes = new Resource(id, name);
						orgModel.addResource(newRes);

						// Redraw tablePanel
						redrawTable();
					}
				} else
					JOptionPane.showMessageDialog(null,
							"ID is duplicated or has null value");
			}
		});
		removeResourceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Resource tempResource = tablePanel.getSeletedResource();
				if (tempResource == null)
					return;

				// remove Org Entity from Org Model
				orgModel.removeResource(tempResource);
				tempResource = null;

				// Redraw tablePanel
				redrawTable();
			}
		});
		changeResourceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Resource tempResource = tablePanel.getSeletedResource();
				if (tempResource == null)
					return;

				// input Org Entity ID
				String id = JOptionPane.showInputDialog(null,
						"input Resource ID?", tempResource.getID());

				if (id != null) {
					// check wheather ID is already in Org Entities
					// if not, change both ID and key of HashMap for Org
					// Entities
					if (orgModel.changeResourceID(tempResource.getID(), id)) {
						// change Org Entity Name
						String name = JOptionPane.showInputDialog(null,
								"input Resource Name?", tempResource.getName());
						if (name != null)
							tempResource.setName(name);
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
		// added by song
		parentPanel.updateActivitySet();

		this.remove(tablePanel);
		tablePanel = null;
		tablePanel = new OrgEntityResourceTablePanel(orgModel, parentPanel);
		this.add(tablePanel, BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}
}
