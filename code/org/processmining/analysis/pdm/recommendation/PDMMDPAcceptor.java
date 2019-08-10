package org.processmining.analysis.pdm.recommendation;

import org.processmining.framework.ui.MainUI;
import java.awt.event.ActionEvent;
import org.processmining.analysis.AnalysisInputItem;
import java.awt.Insets;
import org.processmining.framework.util.CenterOnScreen;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.processmining.framework.log.LogReader;
import java.awt.GridBagConstraints;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.processmining.framework.plugin.ProvidedObject;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import org.processmining.framework.models.pdm.*;
import org.processmining.analysis.AnalysisPlugin;

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

abstract public class PDMMDPAcceptor implements AnalysisPlugin {

	public PDMMDPAcceptor() {
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem(
				"PDM MDP Statespace") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				boolean isPDMStateSpace = false;
				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof PDMStateSpace) {
						isPDMStateSpace = true;
					}
				}
				return isPDMStateSpace;
			}
		} };
		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		PDMModel model = null;
		PDMStateSpace statespace = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PDMModel) {
				model = (PDMModel) o[i];
			}
			if (o[i] instanceof PDMStateSpace) {
				statespace = (PDMStateSpace) o[i];
			}
		}
		return analyse(statespace);

	}

	protected abstract JComponent analyse(PDMStateSpace statespace);

}
