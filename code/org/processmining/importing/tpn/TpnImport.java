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

package org.processmining.importing.tpn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.TPNWriter;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * Parses a TPN file and produces a petri net.
 * <p>
 * This implementation parses bounds and transition labels, but does not use them.
 * Undeclared places will be created automatically without warning
 * if they occur in the in set or out set of a transition.
 * The parser is case insensitive.
 * <p>
 * The parser is built with JavaCC, a free Java parser generator (like yacc).
 * See https://javacc.dev.java.net/ for documentation.
 * <p>
 * The grammar file for the TPN parser is TpnParser.jj.
 * The TpnParser class can be rebuilt with the command javacc TpnParser.jj.
 *
 * @author Peter van den Brand
 * @version 1.0
 */

public class TpnImport implements LogReaderConnectionImportPlugin {

	public TpnImport() {}


	public String getName() {
		return "TPN file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("tpn");
	}

	public PetriNetResult importFile(InputStream input) throws IOException {
		TpnParser parser = new TpnParser(input);
		LogEvents logEvents = null;
		PetriNet p = null;

		try {
			p = parser.Start();
			logEvents = new LogEvents();
			Iterator it = p.getTransitions().iterator();

			while (it.hasNext()) {
				Transition t = (Transition) it.next();
				String s = t.getIdentifier();

				String DELIM = "\\n";
				int i = s.indexOf(DELIM);
				if ((i == s.lastIndexOf(DELIM)) &&
						(i > 0)) {

					String s1 = s.substring(0, i);
					String s2 = s.substring(i + DELIM.length(), s.length());

					if (!s2.equals(TPNWriter.INVISIBLE_EVENT_TYPE)) {
						// t was not supposed to be invisible
						// make t visible with log event using the last occurance of the line break character
						LogEvent e = new LogEvent(s1, s2);
						logEvents.add(e);
						t.setLogEvent(e);
					}
				} else {
					// make t visible with predefined LogEvent(s, "unknown:normal");
					LogEvent e = new LogEvent(s, "unknown:normal");
					logEvents.add(e);
					t.setLogEvent(e);
				}
			}

			p.Test("TpnImport");

			return new PetriNetResult(p);
		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:import:tpn";
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
