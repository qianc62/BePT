package org.processmining.mining.semanticorganizationmining.ui;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.mining.semanticorganizationmining.SemanticOrgMinerOptions;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class OrgMiningOptionTabbedPane extends JTabbedPane {

	private OrgModel orgModel = null;
	private SimilarTaskPanel similarTaskPanel = new SimilarTaskPanel();

	// GUI attributes
	// private TaskOrgEntityPanel taskOrgEntityPanel = null;
	// private OrgEntityResourcePanel orgEntityResourcePanel = null;
	// private OrgModelManagementPanel orgModelManagementPanel = null;

	public OrgMiningOptionTabbedPane() {

		JPanel defaultMiningPanel = new JPanel();
		this.add(defaultMiningPanel, "Default Mining Panel");

		SimilarTaskPanel similarTaskPanel = new SimilarTaskPanel();
		this.add(similarTaskPanel, "Doing Similar Task");

		JPanel caAnalysisPanel = new JPanel();
		this.add(caAnalysisPanel, "Correspondence Analysis");

	}

	public OrgMiningOptionTabbedPane(OrgModel orgmod) {

		this.orgModel = orgmod;

		JPanel defaultMiningPanel = new JPanel();
		this.add(defaultMiningPanel, "Default Mining Panel");

		this.add(similarTaskPanel, "Doing Similar Task");

		JPanel caAnalysisPanel = new JPanel();
		this.add(caAnalysisPanel, "Correspondence Analysis");

		/*
		 * this.addChangeListener(new ChangeListener() { // This method is
		 * called whenever the selected tab changes public void
		 * stateChanged(ChangeEvent evt) { JTabbedPane pane =
		 * (JTabbedPane)evt.getSource(); // Get current tab int sel =
		 * pane.getSelectedIndex(); if(sel == 1) changeTaskOrgEntityPanel();
		 * else if(sel == 2) changeOrgEntityResourcePanel(); } });
		 */
	}

	public int getSelectedMetrics() {
		int nIndex = 0;
		switch (this.getSelectedIndex()) {
		case 0:
			nIndex = SemanticOrgMinerOptions.DEFAULT_MINING;
			break;
		case 1:
			nIndex = SemanticOrgMinerOptions.SIMILAR_TASK;
			break;
		case 2:
			nIndex = SemanticOrgMinerOptions.CORRESPONDENCE_ANALYSIS;
			break;
		}
		return nIndex;
	}

	/**
	 * Returns the currently selected option for SIMILAR_TASK. Can be one of the
	 * constants: EUCLIDIAN_DISTANCE, CORRELATION_COEFFICIENT,
	 * SIMILARITY_COEFFICIENT or HAMMING_DISTANCE.
	 * 
	 * @return the currently selected option for SIMILAR_TASK
	 */
	public int getSimilarTaskSetting() {
		return similarTaskPanel.getSimilarTaskSetting();
		/*
		 * if (similarTaskPanel.getEuclidianDistance()) { return
		 * OrgMinerOptions.EUCLIDIAN_DISTANCE; } else if
		 * (similarTaskPanel.getCorrelationCoefficient()) { return
		 * OrgMinerOptions.CORRELATION_COEFFICIENT; } else if
		 * (similarTaskPanel.getSimilarityCoefficient()) { return
		 * OrgMinerOptions.SIMILARITY_COEFFICIENT; } else { return
		 * OrgMinerOptions.HAMMING_DISTANCE; }
		 */
	}
}
