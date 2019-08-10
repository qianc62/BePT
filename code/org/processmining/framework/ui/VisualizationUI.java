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

package org.processmining.framework.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class VisualizationUI extends JInternalFrame implements Provider {

	private MiningResult result;
	protected JComponent resultVisualization;
	protected boolean setColor = true;

	public VisualizationUI(String title, MiningResult result,
			JComponent resultVisualization) {
		super(title, true, true, true, true);
		this.result = result;
		this.resultVisualization = resultVisualization;

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(resultVisualization, BorderLayout.CENTER);
		pack();
	}

	public MiningResult getResult() {
		return result;
	}

	public ProvidedObject[] getProvidedObjects() {
		return result instanceof Provider ? ((Provider) result)
				.getProvidedObjects() : new ProvidedObject[0];
	}

	public void paint(Graphics g) {
		if (setColor == true) {
			setColor = false;
			Color bg = resultVisualization.getBackground();
			if (bg != null
					&& bg.equals((new JPanel()).getBackground()) == false) {
				setBackground(bg);
			}
		}
		super.paint(g);
	}
}
