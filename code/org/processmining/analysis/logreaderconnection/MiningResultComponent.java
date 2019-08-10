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

package org.processmining.analysis.logreaderconnection;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

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
public class MiningResultComponent extends JPanel implements Provider {
	private ProvidedObject[] pos = new ProvidedObject[0];

	public MiningResultComponent(MiningResult result) {
		setLayout(new BorderLayout());
		add(result.getVisualization(), BorderLayout.CENTER);
		if (result instanceof Provider) {
			pos = ((Provider) result).getProvidedObjects();
		}
		validate();
	}

	public ProvidedObject[] getProvidedObjects() {
		return pos;
	}
}
