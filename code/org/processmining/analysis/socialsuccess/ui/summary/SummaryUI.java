package org.processmining.analysis.socialsuccess.ui.summary;

import java.awt.Color;

import javax.swing.*;
import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.ui.SettingsTab;
import org.processmining.framework.models.orgmodel.OrgModel;

public class SummaryUI extends JPanel {

	private static final long serialVersionUID = -6308520109800562063L;
	protected PersonalityData sum;

	public SummaryUI(PersonalityData socialSuccessData) {
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(3, 10, 5, 10));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.sum = socialSuccessData;
		FlatTabbedPane tabbedPane = new FlatTabbedPane("Summary", new Color(
				240, 240, 240, 230), new Color(180, 180, 180, 120), new Color(
				220, 220, 220, 150));
		tabbedPane.addTab("Content", new UIInputSummary(this));
		tabbedPane.addTab("Settings", new SettingsTab(this.sum));
		this.add(tabbedPane);
	}

	/**
	 * This functions returns the objects orgmodel when called.
	 * 
	 * @return the objects Org Model
	 */
	public OrgModel getOrgModel() {
		return sum.getOrgModel();
	}

	/**
	 * This function return
	 * 
	 * @return the summary of the inputs (which are currently a log file and a
	 *         org-model)
	 */
	public String getInputSummary() {
		return sum.toString();
	}

}
