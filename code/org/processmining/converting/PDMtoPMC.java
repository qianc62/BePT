/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.converting;

import java.util.HashMap;
import java.util.HashSet;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.pdm.PDMDataElement;
import org.processmining.framework.models.pdm.PDMModel;
import org.processmining.framework.models.pdm.PDMOperation;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title: PDMtoPMC *
 * <p>
 * Description:
 * </p>
 * *
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * *
 * <p>
 * Company:
 * </p>
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */
public class PDMtoPMC implements ConvertingPlugin {
	public PDMtoPMC() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getName() {
		return "Product Data Model to Process Model algorithm Charlie";
	}

	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/staff/ivanderfeesten/ProM/documentation/PDM2PM.htm";
	}

	public MiningResult convert(ProvidedObject object) {
		PDMModel model = null;

		for (int i = 0; model == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PDMModel) {
				model = (PDMModel) object.getObjects()[i];
			}
		}

		if (model == null) {
			return null;
		}

		PetriNet petrinet = convert(model);
		return new PetriNetResult(petrinet);

	}

	public PetriNet convert(PDMModel model) {
		PetriNet result = new PetriNet();

		PDMDataElement root = model.getRootElement(); // the "root element of
		// the PDM

		HashMap places = new HashMap(); // set of places (P)
		HashMap transitions = new HashMap(); // set of transitions (T)
		HashSet edges = new HashSet(); // set of edges (F)
		HashMap dataEls = model.getDataElements();

		// Create an input place, a transition, and an output place for every
		// data element in the PDM
		Object[] dataArray = dataEls.values().toArray();
		for (int i = 0; i < dataArray.length; i++) {
			PDMDataElement d = (PDMDataElement) dataArray[i];
			Place p1 = new Place("in_" + d.getID(), result);
			places.put(p1.getIdentifier(), p1);
			result.addPlace(p1);
			Transition t = new Transition(d.getID(), result);
			transitions.put(t.getIdentifier(), t);
			result.addTransition(t);
			LogEvent fake = new LogEvent(d.getID(), "complete");
			t.setLogEvent(fake);
			Place p2 = new Place("out_" + d.getID(), result);
			places.put(p2.getIdentifier(), p2);
			result.addPlace(p2);
			PNEdge e1 = new PNEdge(p1, t);
			edges.add(e1);
			result.addEdge(e1);
			PNEdge e2 = new PNEdge(t, p2);
			edges.add(e2);
			result.addEdge(e2);
		}

		// Create a transition for every operation in the PDM, except for the
		// leaf operations
		HashMap ops = model.getOperations();
		Object[] opArray = ops.values().toArray();
		for (int j = 0; j < opArray.length; j++) {
			PDMOperation op = (PDMOperation) opArray[j];
			HashMap inputs = op.getInputElements();
			HashMap outputs = op.getOutputElements();
			if (!(inputs.isEmpty())) {
				Transition t = new Transition(op.getID(), result);
				transitions.put(t.getIdentifier(), t);
				result.addTransition(t);
				LogEvent fake = new LogEvent(op.getID(), "complete");
				t.setLogEvent(fake);
				// An edge from every input element to the transition
				Object[] ins = inputs.values().toArray();
				for (int k = 0; k < ins.length; k++) {
					PDMDataElement data1 = (PDMDataElement) ins[k];
					Place p = (Place) places.get("out_" + data1.getID());
					PNEdge e1 = new PNEdge(p, t);
					edges.add(e1);
					result.addEdge(e1);
				}
				// An edge from the transition to every output element.
				Object[] outs = outputs.values().toArray();
				for (int m = 0; m < outs.length; m++) {
					PDMDataElement data1 = (PDMDataElement) outs[m];
					Place p2 = (Place) places.get("in_" + data1.getID());
					PNEdge e2 = new PNEdge(t, p2);
					edges.add(e2);
					result.addEdge(e2);
				}
			}
		}
		// Add a start place and transition
		Place start = new Place("start", result);
		Transition tstart = new Transition("t_start", result);
		LogEvent fake = new LogEvent(tstart.getIdentifier(), "complete");
		tstart.setLogEvent(fake);
		PNEdge p = new PNEdge(start, tstart);
		places.put("start", start);
		transitions.put("t_start", tstart);
		edges.add(p);
		result.addPlace(start);
		result.addTransition(tstart);
		result.addEdge(p);
		// Add the start token to the start place
		// Token to = new Token();
		// start.addToken(to);
		// Add edges from start transition to all leaf elements of the PDM
		HashMap leafs = model.getLeafElements();
		Object[] leafArray = leafs.values().toArray();
		for (int ii = 0; ii < leafArray.length; ii++) {
			PDMDataElement d3 = (PDMDataElement) leafArray[ii];
			Place p3 = (Place) places.get("in_" + d3.getID());
			PNEdge e3 = new PNEdge(tstart, p3);
			edges.add(e3);
			result.addEdge(e3);
		}

		printTestOutput(result);
		return result;
	}

	/**
	 * This function tells the interface which results are accepted by this
	 * Plugin
	 * 
	 * @param original
	 *            The original mining result
	 * @return Whether or not this result is accepted
	 */
	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PDMModel) {
				return true;
			}
		}
		return false;
	}

	private void jbInit() throws Exception {
	}

	protected void printTestOutput(PetriNet model) {
		Message.add("<PDMtoPM Charlie>", Message.TEST);
		Message.add("<Number of transitions = " + model.getTransitions().size()
				+ " >", Message.TEST);
		Message.add("<Number of places = " + model.getPlaces().size() + " >",
				Message.TEST);
		Message.add("</PDMtoPM Charlie>", Message.TEST);
	}

}
