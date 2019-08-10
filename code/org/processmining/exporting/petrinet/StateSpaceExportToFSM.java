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

package org.processmining.exporting.petrinet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class StateSpaceExportToFSM implements ExportPlugin {
	public StateSpaceExportToFSM() {
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof StateSpace) {
				return true;
			}
		}
		return false;
	}

	public String getFileExtension() {
		return "fsm";
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {

		StateSpace s = null;
		Object[] o = object.getObjects();

		for (int i = 0; (i < o.length) && (s == null); i++) {
			if (o[i] instanceof StateSpace) {
				s = (StateSpace) o[i];
			}
		}

		// s is the given state space
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));

		boolean b = s.setUseIdentifier(true);
		s.writeToFSM(bw);
		s.setUseIdentifier(b);
		bw.close();
	}

	public String getName() {
		return "Place-attributed FSM file";
	}

	public String getHtmlDescription() {
		return "<p> <b>Plug-in: FSM Visualization export</b>"
				+ "<p>This Plug-in allows the user export a statespace to "
				+ "<a href=\"http://www.win.tue.nl/~fvham/fsm/\">FSMView</a>  "
				+ "for visualization purposes.";
	}

}
