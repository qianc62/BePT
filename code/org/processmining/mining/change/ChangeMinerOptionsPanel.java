/**
 * Project: ProM
 * File: ChangeMinerOptionsPanel.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 18, 2006, 6:50:08 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 ***********************************************************
 * 
 * This software is part of the ProM package          
 * http://www.processmining.org/               
 *                                                         
 * Copyright (c) 2003-2006 TU/e Eindhoven
 * and is licensed under the
 * Common Public License, Version 1.0
 * by Eindhoven University of Technology 
 * Department of Information Systems        
 * http://is.tm.tue.nl            
 *                                               
 ***********************************************************/
package org.processmining.mining.change;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Configuration panel for the change mining plugin.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class ChangeMinerOptionsPanel extends JPanel {

	private static final long serialVersionUID = -935510401142425919L;
	public static final String CAUSALITY_CONFLICTING = "allow conflicting";
	public static final String CAUSALITY_NONCONFLICTING = "remove conflicting";
	public static final String CAUSALITY_OPT[] = {
			ChangeMinerOptionsPanel.CAUSALITY_CONFLICTING,
			ChangeMinerOptionsPanel.CAUSALITY_NONCONFLICTING };

	protected JComboBox causalityBox = null;

	public ChangeMinerOptionsPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		JPanel dependencyPanel = new JPanel();
		JLabel dependencyLabel = new JLabel("Causality inference:");
		causalityBox = new JComboBox(ChangeMinerOptionsPanel.CAUSALITY_OPT);
		dependencyPanel.setLayout(new BoxLayout(dependencyPanel,
				BoxLayout.X_AXIS));
		dependencyPanel.setMaximumSize(new Dimension(1000, 30));
		dependencyPanel.add(dependencyLabel);
		dependencyPanel.add(Box.createHorizontalStrut(10));
		dependencyPanel.add(causalityBox);
		dependencyPanel.add(Box.createHorizontalGlue());
		this.add(dependencyPanel);
		this.add(Box.createVerticalGlue());
	}

	public boolean isConflictingCausalityAllowed() {
		return (causalityBox.getSelectedItem()
				.equals(ChangeMinerOptionsPanel.CAUSALITY_CONFLICTING));
	}
}
