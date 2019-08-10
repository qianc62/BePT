package org.processmining.mining.partialorderminingTimeUnit;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyListEnumeration;

public class PartialOrderMiningUI extends JPanel {

	private JPanel mainPanel = new JPanel();

	private GUIPropertyBoolean parallel = new GUIPropertyBoolean(
			"add partial order between events with the same timestamp?", false);
	private GUIPropertyListEnumeration dboxTimeUnits = null;

	public PartialOrderMiningUI() {
		jbInit();
	}

	public void jbInit() {
		TimeUnit[] tUnitArray = TimeUnit.values();
		ArrayList valuesTimeUnit = new ArrayList();
		for (int i = 0; i < tUnitArray.length; i++) {
			valuesTimeUnit.add(tUnitArray[i]);
		}
		// add the timeunit to the drop down box
		dboxTimeUnits = new GUIPropertyListEnumeration("TimeUnit",
				valuesTimeUnit);
		// set the default value of the drop down box
		dboxTimeUnits.setValue(TimeUnit.DAYS);
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(parallel.getPropertyPanel());
		mainPanel.add(dboxTimeUnits.getPropertyPanel());
	}

	public boolean isParOptionChecked() {
		return parallel.getValue();
	}

	public TimeUnit getSelectedTimeUnit() {
		return (TimeUnit) dboxTimeUnits.getValue();
	}

}
