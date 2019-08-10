/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2008 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.converting;

import org.processmining.converting.*;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.framework.models.petrinet.*;
import java.util.*;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.framework.models.pdm.*;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: PDMtoPMB
 * </p>
 * *
 * <p>
 * Description:
 * </p>
 * *
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * *
 * <p>
 * Company: TU/e and Pallas Athena
 * </p>
 * *
 * 
 * @author Johfra Kamphuis
 * @version 1.0
 */

public class PDMtoPMB implements ConvertingPlugin {
	public PDMtoPMB() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getName() {
		return "Product Data Model to Process Model algorithm Bravo";
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

		// Initialising step, create start and finish place
		Place Ps = new Place("P_s", result);
		result.addPlace(Ps);
		// Mark the start place
		// Token to = new Token();
		// Ps.addToken(to);
		Place Pf = new Place("P_f", result);
		result.addPlace(Pf);

		result = calculate(model, result, root, Ps, Pf);

		printTestOutput(result);
		return result;
	}

	public PetriNet calculate(PDMModel model, PetriNet result,
			PDMDataElement ie, Place in, Place out) {
		// model contains the data of the PDM model
		// result contains the translation to the PetriNet, as far as it is
		// ready yet
		// ie is the element considered now in the algorithm
		// in and out are the places inbetween which the translation of ie and
		// its children will be placed
		Object[] opArray = model.getOperationsWithOutputElement(ie).toArray();

		for (int j = 0; j < opArray.length; j++) {
			// Each operation (i, cs) with (i==ie) will now be added to the
			// Petri net,
			// as will the information elements that are in cs.
			// Next, all operations that produce the information elements in cs
			// will be added to the Petri net recursively.

			PDMOperation op = (PDMOperation) opArray[j];

			Transition t = new Transition("T_" + op.getID(), result);
			result.addTransition(t);
			LogEvent fake = new LogEvent(t.getIdentifier(), "complete");
			t.setLogEvent(fake);
			PNEdge e = new PNEdge(t, out);
			result.addEdge(e);

			HashMap inputs = op.getInputElements();
			if (inputs.isEmpty()) {

				PNEdge e1 = new PNEdge(in, t);
				result.addEdge(e1);

			} else if (inputs.size() == 1) {
				// cs = 1. Therefore it is enough to add a single transition,
				// that will be preceded
				// by the translation of the elements preceding current element.
				Object[] ins = inputs.values().toArray();
				for (int k = 0; k < ins.length; k++) {
					PDMDataElement data1 = (PDMDataElement) ins[k];
					Place P1 = new Place("P_" + data1.getID(), result);
					result.addPlace(P1);

					PNEdge e1 = new PNEdge(P1, t);
					result.addEdge(e1);

					result = calculate(model, result, (PDMDataElement) ins[k],
							in, P1);
				}
			} else if (inputs.size() > 1) {
				// cs > 1. Therefore the translation of the elements preceding
				// the current element
				// will be nested. Thus, as a nesting T(i, cs)_init and T(i,cs)
				// are created.
				Transition t2init = new Transition("T_" + op.getID() + "_init",
						result);
				result.addTransition(t2init);
				LogEvent fake2 = new LogEvent(t2init.getIdentifier(),
						"complete");
				t2init.setLogEvent(fake2);
				PNEdge e2a = new PNEdge(in, t2init);
				result.addEdge(e2a);

				Object[] ins = inputs.values().toArray();
				for (int k = 0; k < ins.length; k++) {
					// recursively the translation of all elements preceding the
					// current element
					// will be translated.
					PDMDataElement data2 = (PDMDataElement) ins[k];

					Place P2init = new Place("P_" + data2.getID() + "_init",
							result);
					result.addPlace(P2init);
					PNEdge e2b = new PNEdge(t2init, P2init);
					result.addEdge(e2b);

					Place P2 = new Place("P_" + data2.getID(), result);
					result.addPlace(P2);
					PNEdge e2c = new PNEdge(P2, t);
					result.addEdge(e2c);

					result = calculate(model, result, (PDMDataElement) ins[k],
							P2init, P2);
				}
			}
		}
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
		Message.add("<PDMtoPM Bravo>", Message.TEST);
		Message.add("<Number of transitions = " + model.getTransitions().size()
				+ " >", Message.TEST);
		Message.add("<Number of places = " + model.getPlaces().size() + " >",
				Message.TEST);
		Message.add("<Number of arcs = " + model.getEdges().size() + " >",
				Message.TEST);
		Message.add("</PDMtoPM Bravo>", Message.TEST);
	}

}
