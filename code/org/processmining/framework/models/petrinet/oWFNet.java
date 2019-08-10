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

package org.processmining.framework.models.petrinet;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/**
 * <p>
 * Title: oWFNet
 * </p>
 * 
 * <p>
 * Description: Class for open workflow nets (owF nets)
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
 * @author Eric Verbeek
 * @version 1.0
 */
public class oWFNet extends WFNet {

	/*
	 * Maps the communication places (either input or output) to the transitions
	 * that are connected to these places.
	 */
	protected HashMap<String, HashSet<Transition>> inputs, outputs;

	public oWFNet() {
		inputs = new HashMap<String, HashSet<Transition>>();
		outputs = new HashMap<String, HashSet<Transition>>();
	}

	public HashMap<String, HashSet<Transition>> getInputs() {
		return inputs;
	}

	public HashMap<String, HashSet<Transition>> getOutputs() {
		return outputs;
	}

	public void addInput(String name, Transition transition) {
		HashSet<Transition> hashSet;
		if (inputs.containsKey(name)) {
			hashSet = new HashSet<Transition>(inputs.get(name));
		} else {
			hashSet = new HashSet<Transition>();
		}
		hashSet.add(transition);
		inputs.put(name, hashSet);
	}

	public void addOutput(Transition transition, String name) {
		HashSet<Transition> hashSet;
		if (outputs.containsKey(name)) {
			hashSet = new HashSet<Transition>(outputs.get(name));
		} else {
			hashSet = new HashSet<Transition>();
		}
		hashSet.add(transition);
		outputs.put(name, hashSet);
	}

	public void writeToDot(Writer bw) throws IOException {
		initDotWriting(bw);
		writeTransitionsToDot(bw);
		writePlacesToDot(bw);
		writeCommPlacesToDot(bw);
		writeEdgesToDot(bw);
		writeCommEdgesToDot(bw);
		writeClustersToDot(bw);
		writeCommClustersToDot(bw);
		finishDotWriting(bw);
	}

	protected void writeCommPlacesToDot(Writer bw) throws IOException {
		Place prevPlace = null;
		int i = 0;
		for (String name : inputs.keySet()) {
			bw.write("c" + i + " [shape=\"circle\",label=\"" + name + "\"];\n");
			if (i > 0) {
				bw.write("c" + (i - 1) + " -> c" + i
						+ " [style=\"invis\",weight=\"0\"];\n");
			}
			i++;
		}
		for (String name : outputs.keySet()) {
			bw.write("c" + i + " [shape=\"circle\",label=\"" + name + "\"];\n");
			if (i > 0) {
				bw.write("c" + (i - 1) + " -> c" + i
						+ " [style=\"invis\",weight=\"0\"];\n");
			}
			i++;
		}
	}

	protected void writeCommEdgesToDot(Writer bw) throws IOException {
		int i = 0;
		for (String name : inputs.keySet()) {
			for (Transition transition : inputs.get(name)) {
				bw.write("c" + i + " -> t" + transition.getNumber() + ";\n");
			}
			i++;
		}
		for (String name : outputs.keySet()) {
			for (Transition transition : outputs.get(name)) {
				bw.write("t" + transition.getNumber() + " -> c" + i + ";\n");
			}
			i++;
		}
	}

	protected void writeCommClustersToDot(Writer bw) throws IOException {
		Iterator it;
		bw.write("subgraph \"cluster_comm0\" {\n");
		bw
				.write("style=\"filled\"; fillcolor=\"lightyellow\"; label=\"communication\"");
		int i = 0;
		for (String name : inputs.keySet()) {
			bw.write("c" + i + ";\n");
			i++;
		}
		for (String name : outputs.keySet()) {
			bw.write("c" + i + ";\n");
			i++;
		}
		bw.write("}\n");
		bw.write("subgraph \"cluster_comm1\" {");
		it = this.getPlaces().iterator();
		while (it.hasNext()) {
			Place p = (Place) (it.next());
			bw.write("p" + p.getNumber() + ";\n");
		}
		it = this.getTransitions().iterator();
		while (it.hasNext()) {
			Transition t = (Transition) (it.next());
			bw.write("t" + t.getNumber() + ";\n");
		}
		bw.write("}\n");
	}
}
