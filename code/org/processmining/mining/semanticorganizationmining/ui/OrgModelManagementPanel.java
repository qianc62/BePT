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

package org.processmining.mining.semanticorganizationmining.ui;

import org.processmining.framework.models.orgmodel.OrgModel;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.processmining.framework.models.orgmodel.algorithms.OrgModelUtil;
import org.processmining.mining.semanticorganizationmining.SemanticOrgMiningResult;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class OrgModelManagementPanel extends JPanel {

	private OrgModel orgModel = null;
	private SemanticOrgMiningResult parentPanel;

	private JButton aggregateEventButton = new JButton(
			"Aggregate events with the same task name");
	private JButton removeRedundantOrgModel = new JButton(
			"Remove redundant OrgEntity");

	public OrgModelManagementPanel(OrgModel orgmod,
			SemanticOrgMiningResult parentpanel) {

		this.orgModel = orgmod;
		this.parentPanel = parentpanel;
		init();
	}

	public void init() {
		// this.add(aggregateEventButton);
		this.add(removeRedundantOrgModel);

		// aggregateEventButton.setMaximumSize(new Dimension(200,28));
		removeRedundantOrgModel.setMaximumSize(new Dimension(200, 28));

		registerGuiActionListener();
	}

	/**
	 * Connect GUI elements with functionality to create interaction.
	 */
	private void registerGuiActionListener() {
		aggregateEventButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OrgModelUtil.aggregateTasks(orgModel);
				JOptionPane.showMessageDialog(null, "Aggregated");
			}
		});
		removeRedundantOrgModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OrgModelUtil.removeRedundantOrgEntity(orgModel);
				JOptionPane.showMessageDialog(null, "Removed");
			}
		});
	}
}
