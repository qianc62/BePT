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

package org.processmining.importing.fsm;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TransitionSystemEdge;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertexSet;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.instancemining.ModelGraphResult;
import org.processmining.framework.models.ModelGraphVertex;

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
public class FSMImport implements LogReaderConnectionImportPlugin {
	StringBuffer buffer;

	public FSMImport() {
		buffer = new StringBuffer();
	}

	public String getName() {
		return "FSM file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("fsm");
	}

	public ModelGraphResult importFile(InputStream input) throws IOException {
		TransitionSystem ts = new TransitionSystem("FSM Import");
		parse(ts, input);
		return new ModelGraphResult(ts);
	}

	public String getHtmlDescription() {
		String s = "<html>";
		s += "<head><title>ProM Framework: FSM  Import plug-in</title</head>";
		s += "<body><h1>FSM Import plug-in</h1>";
		s += "<p>The FSM Import plug-in reads a transition system from an FSM formatted file.</p>";
		s += "</body></html>";
		return s;
	}

	/**
	 * Split line into three strings: fromState, toState, and label.
	 * 
	 * @param line
	 *            String The line.
	 * @return String[] The three strings.
	 */
	public String[] tokenize(String line) {
		String[] tokens = new String[3];
		tokens[0] = "";
		tokens[1] = "";
		tokens[2] = "";
		boolean quoted = false, separator = false;
		int i, c = 0;
		for (i = 0; c < 3 && i < line.length(); i++) {
			switch (line.charAt(i)) {
			case '"': {
				quoted = !quoted;
				separator = false;
				break;
			}
			case ' ': {
				if (separator) {
					break;
				} else if (!quoted) {
					c++;
					separator = true;
					break;
				}
				// no break, treat as regular character.
			}
			default: {
				tokens[c] = tokens[c] + line.charAt(i);
				separator = false;
			}
			}
		}
		return tokens;
	}

	public void parse(TransitionSystem ts, InputStream input)
			throws IOException {
		String s = readNextLine(input);
		HashMap<String, TransitionSystemVertexSet> map = new HashMap<String, TransitionSystemVertexSet>();
		// Skip attributes.
		while (s.compareTo("---") != 0) {
			s = readNextLine(input);
		}
		// Skip state definitions.
		s = readNextLine(input);
		while (s.compareTo("---") != 0) {
			s = readNextLine(input);
		}
		// Read transitions.
		while ((s = readNextLine(input)) != null) {
			String[] tokens = tokenize(s);
			String fromState = "s" + tokens[0];
			String toState = "s" + tokens[1];
			String label = tokens[2];
			TransitionSystemVertexSet fromVertex, toVertex;
			if (map.containsKey(fromState)) {
				fromVertex = map.get(fromState);
			} else {
				fromVertex = new TransitionSystemVertexSet(fromState, ts);
				fromVertex.addDocument(fromState);
				map.put(fromState, fromVertex);
				ts.addVertexQuick(fromVertex);
			}
			if (map.containsKey(toState)) {
				toVertex = map.get(toState);
			} else {
				toVertex = new TransitionSystemVertexSet(toState, ts);
				toVertex.addDocument(toState);
				map.put(toState, toVertex);
				ts.addVertexQuick(toVertex);
			}
			TransitionSystemEdge edge = new TransitionSystemEdge(label,
					fromVertex, toVertex);
			ts.addEdge(edge);
		}
		// The first source vertex will act as start state.
		// Every sink vertex will act as accepting state.
		boolean hasStartState = false;
		for (ModelGraphVertex vertex : ts.getVerticeList()) {
			if (!hasStartState && !vertex.getInEdgesIterator().hasNext()) {
				hasStartState = true;
				ts.setStartState((TransitionSystemVertexSet) vertex);
			}
			if (!vertex.getOutEdgesIterator().hasNext()) {
				ts.addAcceptState((TransitionSystemVertexSet) vertex);
			}
		}
	}

	public String readNextLine(InputStream input) throws IOException {
		int c;
		buffer = new StringBuffer();
		while ((c = input.read()) != -1 && c != '\n') {
			if (c != '\r') {
				buffer.append((char) c);
			}
		}
		return c != -1 ? buffer.toString() : null;
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
