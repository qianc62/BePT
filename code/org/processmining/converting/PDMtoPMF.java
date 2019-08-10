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
 * Title: PDMtoPMF
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

public class PDMtoPMF implements ConvertingPlugin {
	public PDMtoPMF() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getName() {
		return "Product Data Model to Process Model algorithm Foxtrot";
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

		HashMap done = new HashMap();

		// Initialising step, create start and finish place
		Place ps = new Place("P_{}", result);
		places.put(ps.getIdentifier(), ps);
		result.addPlace(ps);
		// Mark the start place
		// Token to = new Token();
		// Ps.addToken(to);
		Place pf = new Place("P_f", result);
		places.put(pf.getIdentifier(), pf);
		result.addPlace(pf);

		// Determine the entire set of information elements in the PDM.
		HashMap FullPath = getFullPath(model);
		// Run calculate on the FullPath.
		// done is empty

		result = calculate(model, result, places, transitions, edges, FullPath,
				done);

		Object[] placeloop = places.values().toArray();
		for (int l = 0; l < placeloop.length; l++) {
			// loop through all places
			// if the place "containes" the root element
			// add an 'empty' transition from that place to P_f
			// else do nothing
			String rootname = root.getID();
			String placename = placeloop[l].toString();

			if (placename.contains(rootname) == true) {
				// perform an 'extra' security check
				// to prevent the rootname being a substring of another
				// element's name
				if ((placename.contains("{" + rootname + ", ") == true)
						|| (placename.contains("{" + rootname + "}") == true)
						|| (placename.contains(", " + rootname + ", ") == true)
						|| (placename.contains(", " + rootname + "}") == true)) {
					// add an 'empty' transition from that place to P_f

					Transition tloos = new Transition("T_", result);
					transitions.put(tloos.getIdentifier(), tloos);
					result.addTransition(tloos);
					LogEvent fakeloos = new LogEvent(tloos.getIdentifier(),
							"complete");
					tloos.setLogEvent(fakeloos);

					Place endplace = (Place) places.get(placename);
					PNEdge end0 = new PNEdge(endplace, tloos);
					edges.add(end0);
					result.addEdge(end0);
					PNEdge end1 = new PNEdge(tloos, pf);
					edges.add(end1);
					result.addEdge(end1);
				}
			}
		}

		printTestOutput(result);
		return result;
	}

	public PetriNet calculate(PDMModel model, PetriNet result, HashMap places,
			HashMap transitions, HashSet edges, HashMap todo, HashMap done) {

		HashMap ops = model.getOperations();
		Object[] opArray = ops.values().toArray();
		for (int j = 0; j < opArray.length; j++) {

			PDMOperation op = (PDMOperation) opArray[j];
			HashMap inputs = op.getInputElements();
			HashMap outputs = op.getOutputElements();

			if ((issubset(todo, outputs)) && (issubset(done, inputs))) {
				// Operation opArray[j] (i, cs) with i element todo and cs
				// subset done is found

				// First create todo_after = (todo \ i) and done_after = (done u
				// i)
				HashMap done_after = new HashMap();
				HashMap todo_after = new HashMap();

				PDMDataElement dataout;
				if (outputs.size() == 1) {
					// Select the correct place
					Object[] outs = outputs.values().toArray();

					/**/
					/**/
					// Note the hard-coded 0 here!
					// It can be used since outputs.size() == 1.
					/**/
					/**/
					dataout = (PDMDataElement) outs[0];

					Object[] doneloop = done.values().toArray();
					for (int dl = 0; dl < doneloop.length; dl++) {
						done_after.put(done_after.size(), doneloop[dl]);
					}
					done_after.put(done_after.size(), dataout.getID());

					Object[] todoloop = todo.values().toArray();
					for (int tl = 0; tl < todoloop.length; tl++) {
						if ((todoloop[tl] == dataout.getID()) == false) {
							todo_after.put(todo_after.size(), todoloop[tl]);
						}
					}

				} else
					Message
							.add(
									"This should never appear! There is an operation with more than 1 output element!",
									Message.ERROR);
				// todo_after = (todo \ i) and done_after = (done u i) are now
				// created.

				// Check to see if place P_done_after exists, and if not create
				// it!
				// First we sort the values from done_after
				Object[] sort_after = bubblesort(done_after.values().toArray());

				// The sorted result is put in the string pdoneaftername
				String pdoneaftername = "{";
				for (int aftersort = 0; aftersort < sort_after.length; aftersort++) {
					pdoneaftername = pdoneaftername
							+ sort_after[aftersort].toString();
					if (aftersort < (sort_after.length - 1)) {
						pdoneaftername = pdoneaftername + ", ";
					}
				}
				pdoneaftername = pdoneaftername + "}";

				// Then check if the place already exists, and if not, create
				// it.
				Place pdoneafter;
				if ((places.containsKey("P_" + pdoneaftername)) == false) {
					pdoneafter = new Place("P_" + pdoneaftername, result);
					places.put(pdoneafter.getIdentifier(), pdoneafter);
					result.addPlace(pdoneafter);
				} else {
					pdoneafter = (Place) places.get("P_" + pdoneaftername);
				}

				// Pdone always exists!
				Object[] sort = bubblesort(done.values().toArray());
				// We put the sorted result in the string pdonename
				String pdonename = "{";
				for (int s1 = 0; s1 < sort.length; s1++) {
					pdonename = pdonename + sort[s1].toString();
					if (s1 < (sort.length - 1)) {
						pdonename = pdonename + ", ";
					}
				}
				pdonename = pdonename + "}";
				Place pdone = (Place) places.get("P_" + pdonename);

				// Check if P_done and P_done_after are connected
				Boolean connected = true;
				if ((transitions.containsKey("T_" + op.getID())) == false) {
					connected = false;
				} else {
					// The connecting transition exists. But is this true for
					// the edges?
					Transition tconn = (Transition) transitions.get("T_"
							+ op.getID());

					PNEdge econ0 = new PNEdge(pdone, tconn);
					PNEdge econ1 = new PNEdge(tconn, pdoneafter);
					Object[] egs = edges.toArray();
					Boolean econ0exists = false;
					Boolean econ1exists = false;
					for (int e = 0; e < egs.length; e++) {
						if ((egs[e].toString().equals(econ0.toString())) == true) {
							econ0exists = true;
						}
						if ((egs[e].toString().equals(econ1.toString())) == true) {
							econ1exists = true;
						}
					}
					if (econ1exists && econ0exists) {
						// Both edge (pdone -> tconnected) and (tconnected ->
						// pdoneafter) exist
						// Nothing happenes.
					} else {
						// P_done and P_done_after are not connected via
						// transition T_op
						connected = false;
					}
				}

				if (connected == false) {
					// the connecting transition does not exist, so create:
					// - the transition
					// - the edge connecting P_done with the transition
					// - the edge connecting the transition with P_done_after
					Transition tconnect = new Transition("T_" + op.getID(),
							result);
					transitions.put(tconnect.getIdentifier(), tconnect);
					result.addTransition(tconnect);
					LogEvent fakeconnect = new LogEvent(tconnect
							.getIdentifier(), "complete");
					tconnect.setLogEvent(fakeconnect);

					PNEdge e0 = new PNEdge(pdone, tconnect);
					edges.add(e0);
					result.addEdge(e0);

					PNEdge e1 = new PNEdge(tconnect, pdoneafter);
					edges.add(e1);
					result.addEdge(e1);
				}

				result = calculate(model, result, places, transitions, edges,
						todo_after, done_after);
			}
		}

		return result;
	}

	public Boolean issubset(HashMap set, HashMap subset) {
		// This function checks is subset is a subset of set.
		// Returnvalue = true if this is the case.
		// Returnvalue = false if subset isn't a subset of set.
		Boolean returnvalue = true;
		if (subset.isEmpty()) {
			// Do nothing, the empty set IS a subset of any set!
		} else {

			Object[] subs = subset.values().toArray();

			for (int k = 0; k < subs.length; k++) {
				PDMDataElement sub = (PDMDataElement) subs[k];
				if (set.containsValue(sub.getID()) == false) {
					returnvalue = false;
				}
			}
		}
		return returnvalue;
	}

	public Object[] bubblesort(Object[] tosort) {
		// Sort the array tosort using a simple (though inefficient) sorting
		// algorithm.
		Boolean sorting;
		int upperlimit = tosort.length - 1;
		do {
			sorting = false;
			for (int s0 = 0; s0 < upperlimit; s0++) {
				if (tosort[s0].toString().compareTo(tosort[s0 + 1].toString()) < 0) {
					// tosort[s0] comes before tosort[s0 + 1]
					// Do nothing
				} else if (tosort[s0].toString().compareTo(
						tosort[s0 + 1].toString()) == 0) {
					// tosort[s0] en tosort[s0 + 1] are equal
					// Elements in the provided array are supposed to be unique!
					// However, since dealing with an array, not a set,
					// duplicates are possible.
					// So we remove the duplicate!

					Object[] tosortnew = new Object[tosort.length - 1];
					for (int tmp = 0; tmp < s0; tmp++) {
						tosortnew[tmp] = tosort[tmp];
					}
					for (int tmp = s0; tmp < tosortnew.length; tmp++) {
						tosortnew[tmp] = tosort[tmp + 1];
					}
					tosort = tosortnew;
					upperlimit = upperlimit - 1;
					s0 = s0 - 1;

				} else if (tosort[s0].toString().compareTo(
						tosort[s0 + 1].toString()) > 0) {
					// tosort[s0] comes after tosort[s0 + 1]
					// Swap the elements
					String swap = (String) tosort[s0];
					tosort[s0] = tosort[s0 + 1];
					tosort[s0 + 1] = swap;
					sorting = true;
				}
			}
			upperlimit = upperlimit - 1;
		} while (sorting);
		return tosort;
	}

	public HashMap getFullPath(PDMModel model) {
		// Retrievs the "path" with all elements
		Object[] elemArray = model.getDataElements().values().toArray();
		HashMap currentpath = new HashMap();
		for (int j = 0; j < elemArray.length; j++) {
			PDMDataElement currentelement = (PDMDataElement) elemArray[j];
			currentpath.put(currentpath.size(), currentelement.getID());
		}
		return currentpath;
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
		Message.add("<PDMtoPM Foxtrot>", Message.TEST);
		Message.add("<Number of transitions = " + model.getTransitions().size()
				+ " >", Message.TEST);
		Message.add("<Number of places = " + model.getPlaces().size() + " >",
				Message.TEST);
		Message.add("<Number of arcs = " + model.getEdges().size() + " >",
				Message.TEST);
		Message.add("</PDMtoPM Foxtrot>", Message.TEST);
	}

}
