package org.processmining.mining.organizationmining.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.mining.organizationmining.OrgMiningResult;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class OrgEditingPanel extends JPanel {

	private OrgModel orgModel = new OrgModel();

	private OrgMiningResult parentPanel;
	private OrgModelGrahp graphPanel;

	public OrgEditingPanel(OrgModel orgmodel, OrgMiningResult parentpanel) {
		this.orgModel = orgmodel;
		this.parentPanel = parentpanel;
		// parentPanel.updateActivitySet();
		init();
	}

	public JComponent getVisualization() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	private void jbInit() throws Exception {

	}

	private void init() {
		graphPanel = new OrgModelGrahp(orgModel);
		OrgMiningTabbedPane gPanel = new OrgMiningTabbedPane(orgModel,
				parentPanel);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				gPanel, graphPanel);
		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
	}

	public void redrawOrgModel() {
		graphPanel.redraw();
	}

}
