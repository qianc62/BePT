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

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.mining.semanticorganizationmining.SemanticOrgMiningResult;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;
import org.processmining.framework.models.orgmodel.OrgModelConcept;

/**
 * @author Minseok Song
 * @version 1.0
 */
public class OrgMiningResultPanel extends JTabbedPane {

	private OrgModelConcept orgModel = null;
	private SocialNetworkMatrix snMatrix = null;
	private SemanticOrgMiningResult parentPanel = null;

	// GUI attributes
	OrgMiningResultPanel() {
	}

	// new
	private OrgEditingPanel orgEditingPanel = null;
	private SimilarTaskResultPanel similarTaskResultPanel = null;

	public OrgMiningResultPanel(OrgModelConcept orgmod,
			SocialNetworkMatrix snmatrix, SemanticOrgMiningResult parent) {
		this.orgModel = orgmod;
		this.snMatrix = snmatrix;
		this.parentPanel = parent;
		if (snMatrix != null) {
			similarTaskResultPanel = new SimilarTaskResultPanel(orgModel,
					snMatrix, parentPanel);
			this.add(similarTaskResultPanel,
					"Mining Result: adjust threshold value");
		}
		orgEditingPanel = new OrgEditingPanel(orgModel, parentPanel);
		this.add(orgEditingPanel, "Organizational Model");

		this.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane pane = (JTabbedPane) evt.getSource();
				// Get current tab
				int sel = pane.getSelectedIndex();
				if (sel == 1)
					redrawOrgModel();
			}
		});

	}

	public void redrawOrgModel() {
		orgEditingPanel.redrawOrgModel();
	}
}
