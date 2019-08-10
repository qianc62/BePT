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

package org.processmining.converting.epc2transitionsystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.processmining.converting.ConvertingPlugin;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCEdge;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertex;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertexSet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.instancemining.ModelGraphResult;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Jan Mendling
 * @version 1.0
 */

public class EpcToTransitionSystem implements ConvertingPlugin {

	public String getName() {
		return "EPC to State/Context Transition System";
	}

	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/trac/prom/wiki/ProMPlugins/EPC2STSConversion";
	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof EPC);
			b |= (original.getObjects()[i] instanceof ConfigurableEPC);
			i++;
		}
		return b;
	}

	public MiningResult convert(ProvidedObject original) {
		Object[] o = original.getObjects();
		ConfigurableEPC epc;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof ConfigurableEPC) {
				epc = (ConfigurableEPC) o[i];
				return new ModelGraphResult(convert(epc));
			}
		}
		TransitionSystem TS = new TransitionSystem("Empty");
		return new ModelGraphResult(TS);
	}

	public TransitionSystem convert(ConfigurableEPC epc) {
		Message.add("Starting conversion: EPC to Transition System");
		TransitionSystem ts = getTransitionSystem(epc);
		Message.add("Conversion finished.");
		return ts;
	}

	public TransitionSystem getTransitionSystem(ConfigurableEPC epc) {
		TransitionSystem TS = new TransitionSystem(epc.getIdentifier());
		Stack<EPCMarking> interMarkings = new Stack<EPCMarking>();
		EPCMarking currentMarking;
		ArrayList<EPCMarking> newMarkings;
		interMarkings.addAll(getStartMarkings(epc));
		// Message.add("Set of start markings is " + interMarkings.size());
		int count = 0;
		while (!interMarkings.isEmpty()) {
			currentMarking = interMarkings.pop();
			// Message.add("Current Marking "+currentMarking.toString(),
			// Message.DEBUG);
			newMarkings = currentMarking.nextMarkings(TS);
			for (EPCMarking mark : newMarkings) {
				if (TS.containsVertex(new TransitionSystemVertexSet(mark
						.toString(), TS)) == null) {
					interMarkings.push(mark);
				}
			}
			count++;
		}
		// showWarningDialog(checkSoundness(TS, Epc));

		/**
		 * Set start state(s) and accepting states.
		 */
		ArrayList<ModelGraphVertex> vertices = TS.getVerticeList();
		for (int i = 0; i < vertices.size(); i++) {
			ModelGraphVertex vertex = vertices.get(i);
			if (vertex instanceof TransitionSystemVertex) {
				TransitionSystemVertex v = (TransitionSystemVertex) vertex;
				if (v.getInEdges() == null) {
					TS.setStartState(v);
				}
				if (v.getOutEdges() == null) {
					TS.addAcceptState(v);
				}
			}
		}

		return TS;
	}

	public ArrayList<EPCMarking> getStartMarkings(ConfigurableEPC epc) {
		ArrayList<EPCMarking> startMarkings = new ArrayList<EPCMarking>();
		ArrayList<EPCObject> nodes = new ArrayList<EPCObject>(epc
				.getVerticeList().size());
		nodes.addAll(epc.getConnectors());
		nodes.addAll(epc.getEvents());
		nodes.addAll(epc.getFunctions());
		ArrayList<EPCEdge> startArcs = new ArrayList<EPCEdge>();
		for (EPCObject obj : nodes) {
			if (obj.getPredecessors().size() < 1
					&& obj.getSuccessors().size() > 0) {
				Iterator outs = obj.getOutEdgesIterator();
				while (outs.hasNext()) {
					EPCEdge edge = (EPCEdge) outs.next();
					// Message.add("object is "+obj.getIdentifier()+" "+edge.getId());
					startArcs.add((EPCEdge) edge);
				}
			}
		}
		int length = startArcs.size();
		// Message.add("Set of startArcs is " + startArcs.size(),
		// Message.DEBUG);
		Vector<EPCEdge> positives = new Vector<EPCEdge>();
		Vector<EPCEdge> negatives = new Vector<EPCEdge>();
		int number;
		EPCMarking newone;
		boolean allnegative;
		for (int c = 1; c <= Math.pow(2, length); c++) {
			allnegative = true;
			number = c;
			for (int z = 0; z < length; z++) {
				if ((number % 2) == 0) {
					// Message.add(" " + z + " + " + 1 + " " + number + " " +
					// startArcs.get(z).getId());
					if (!positives.contains(startArcs.get(z))) {
						positives.add(startArcs.get(z));
					}
					negatives.remove(startArcs.get(z));
					allnegative = false;
				} else {
					// Message.add(" " + z + " - " + 1);
					if (!negatives.contains(startArcs.get(z))) {
						negatives.add(startArcs.get(z));
					}
					positives.remove(startArcs.get(z));
				}
				number = number >> 1;
			}
			if (allnegative == false) {
				newone = new EPCMarking(epc, new Vector<EPCEdge>(negatives),
						new Vector<EPCEdge>(positives));
				Message
						.add("Marking added " + newone.toString(),
								Message.DEBUG);
				startMarkings.add(newone);
			}
		}
		return startMarkings;
	}

}
