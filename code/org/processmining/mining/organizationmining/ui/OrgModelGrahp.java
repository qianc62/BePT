package org.processmining.mining.organizationmining.ui;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GuiNotificationTarget;

public class OrgModelGrahp extends JPanel implements GuiNotificationTarget {

	private JPanel graphPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private OrgModel orgModel;

	private GUIPropertyBoolean myOrgEntitySelected = new GUIPropertyBoolean(
			"Show OrgEntity nodes", true, this);
	private GUIPropertyBoolean myResourceSelected = new GUIPropertyBoolean(
			"Show Resource nodes", true, this);
	private GUIPropertyBoolean myTaskSelected = new GUIPropertyBoolean(
			"Show Task nodes", true, this);

	public OrgModelGrahp() {
	}

	public OrgModelGrahp(OrgModel orgmodel) {
		orgModel = orgmodel;

		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(orgModel.getGraphPanel(myOrgEntitySelected.getValue(),
				myResourceSelected.getValue(), myTaskSelected.getValue()),
				BorderLayout.CENTER);

		buttonPanel.add(myResourceSelected.getPropertyPanel());
		buttonPanel.add(myOrgEntitySelected.getPropertyPanel());
		buttonPanel.add(myTaskSelected.getPropertyPanel());

		JButton jRedrawButton = new JButton("Redraw Graph");
		buttonPanel.add(jRedrawButton);

		this.setLayout(new BorderLayout());
		this.add(graphPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

		jRedrawButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				redraw();
			}
		});
	}

	public void redraw() {
		graphPanel.removeAll();
		graphPanel.add(orgModel.getGraphPanel(myOrgEntitySelected.getValue(),
				myResourceSelected.getValue(), myTaskSelected.getValue()),
				BorderLayout.CENTER);
		graphPanel.validate();
		graphPanel.repaint();
	}

	public void updateGUI() {
		myOrgEntitySelected.setSelected(myOrgEntitySelected.getValue());
		myResourceSelected.setSelected(myResourceSelected.getValue());
		myTaskSelected.setSelected(myTaskSelected.getValue());
	}
}
