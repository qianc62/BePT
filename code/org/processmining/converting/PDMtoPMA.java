/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *         Copyright (c) 2003-2007 TU/e Eindhoven          *
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
import java.util.Iterator;
import java.util.Vector;

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
 * Title: PDMtoPMA
 * </p>
 * *
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
public class PDMtoPMA implements ConvertingPlugin {
	public PDMtoPMA() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getName() {
		return "Product Data Model to Process Model algorithm Alpha";
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

	/**
	 * Converts the PDMModel into a petri net by starting with the root element
	 * and creating a prepare and produce transition for every data element.
	 * 
	 * @param model
	 *            PDMModel
	 * @return PetriNet
	 */
	public PetriNet convert(PDMModel model) {
		PetriNet result = new PetriNet();

		PDMDataElement root = model.getRootElement(); // the "root element of
		// the PDM

		HashMap places = new HashMap(); // set of places (P)
		HashMap transitions = new HashMap(); // set of transitions (T)
		HashSet edges = new HashSet(); // set of edges (F)
		Vector trsVector = new Vector();

		// Wil's algorithm to transform a PDM into a PN!!! See literature:
		// Computers in Industry (39),
		// pp. 97-111, On the automatic generation of workflow processes based
		// on product structures.

		// Wil's algorithm STEP 1: construct the basic net PN

		// Create an input place for the "root" element; a new place object is
		// created,
		// then this object is addes to the set of places (defined above) and
		// the place
		// is addes to the PetriNet that will be returned when this method has
		// finished.
		Place inRoot = new Place("in_" + root.getID(), result);
		places.put("in_" + root.getID(), inRoot);
		result.addPlace(inRoot);
		// Create an output place for the "root" element
		Place outRoot = new Place("out_" + root.getID(), result);
		places.put("out_" + root.getID(), outRoot);
		result.addPlace(outRoot);
		// Create a transition for the "root" element
		Transition rootTransition = new Transition(root.getID(), result);
		result.addTransition(rootTransition);
		trsVector.add(rootTransition);
		// Create an edge from the input place to the transition of the "root"
		// element
		PNEdge edgeIn = new PNEdge(inRoot, rootTransition);
		result.addEdge(inRoot, rootTransition);
		edges.add(edgeIn);
		// Create an edge frome the transition to the output place of the "root"
		// element
		PNEdge edgeOut = new PNEdge(rootTransition, outRoot);
		result.addEdge(rootTransition, outRoot);
		edges.add(edgeOut);

		// Wil's algorithm STEP 2

		// Determine the intersection of T, the transitions, and C, the data
		// elements in the PDM
		Vector intersectionTC = new Vector();
		intersectionTC = determineIntersectionWithVector(trsVector, model);
		// When the intersectionTC is not empty STEP 2 can be further executed,
		// else when the intersection TC is empty we go to STEP 4
		while (!(intersectionTC.isEmpty())) {
			// Put all elements from the intersection in an array so we can walk
			// through it
			// translate the multiset containing the intersecting elements to an
			// array. Be careful: one transition can occur more than once!
			Iterator v = intersectionTC.iterator();
			while (v.hasNext()) {
				// Take one transition from the array[j]
				Transition trans = (Transition) v.next();
				// Find the corresponding data element in the PDM
				PDMDataElement data2 = model.getDataElement(trans
						.getIdentifier());
				// Find the preceeding elements of this data element
				HashMap precs = model.getPrecedingElements(data2);
				// If there are non, then the transition is relabeled and go to
				// STEP 2 again to select another element
				if ((precs.isEmpty())) {
					// relabel the transition 'trans' to 'produce_trans'
					Transition t = new Transition("produce_"
							+ trans.getIdentifier(), result);
					result.addTransition(t);
					trsVector.add(t);
					// do some administration to make the edges go to the right
					// object:
					// add the original arcs from input and output places of
					// trans to produce_trans
					HashSet edges3 = (HashSet) edges.clone();
					Iterator it4 = edges3.iterator();
					while (it4.hasNext()) {
						PNEdge edge1 = (PNEdge) it4.next();
						if (edge1.getDest().equals(trans)) {
							Place place3 = (Place) edge1.getSource();
							PNEdge edge3 = new PNEdge(place3, t);
							result.addEdge(place3, t);
							edges.add(edge3);
							result.removeEdge(edge1);
							edges.remove(edge1);
						} else if (edge1.getSource().equals(trans)) {
							Place place2 = (Place) edge1.getDest();
							PNEdge edge2 = new PNEdge(t, place2);
							result.addEdge(t, place2);
							edges.add(edge2);
							result.removeEdge(edge1);
							edges.remove(edge1);
						}
					}
					result.delTransition(trans);
					transitions.remove(trans.getIdentifier());
					trsVector.remove(trans);
				}

				// Wil's algorithm STEP 3

				// Replace transition 'data2' by a subnet
				else if (!(precs.isEmpty())) {
					// Create prepare_data2 and produce_data2 transitions
					// (transition trans is replaced by prepare_data2 and
					// produce_data2 transitions)
					Transition t3 = new Transition("prepare_"
							+ trans.getIdentifier(), result);
					result.addTransition(t3);
					trsVector.add(t3);
					Transition t4 = new Transition("produce_"
							+ trans.getIdentifier(), result);
					result.addTransition(t4);
					trsVector.add(t4);

					// Add the edges that flow into trans to flow into
					// prepare_data2 and remove the edges to trans
					HashSet edges2 = (HashSet) edges.clone();
					Iterator it = edges2.iterator();
					while (it.hasNext()) {
						PNEdge edge1 = (PNEdge) it.next();
						if (edge1.getDest().equals(trans)) {
							Place place3 = (Place) edge1.getSource();
							PNEdge edge3 = new PNEdge(place3, t3);
							result.addEdge(place3, t3);
							edges.add(edge3);
							result.removeEdge(edge1);
							edges.remove(edge1);
						}
					}
					// Add the edges that flow out of trans to flow out of
					// produce_data2 and remove the edges to trans
					HashSet edges3 = (HashSet) edges.clone();
					Iterator it2 = edges3.iterator();
					while (it2.hasNext()) {
						PNEdge edge1 = (PNEdge) it2.next();
						if (edge1.getSource().equals(trans)) {
							Place place2 = (Place) edge1.getDest();
							PNEdge edge2 = new PNEdge(t4, place2);
							result.addEdge(t4, place2);
							edges.add(edge2);
							result.removeEdge(edge1);
							edges.remove(edge1);
						}
					}

					// Check how many operations can produce data element
					// 'data2' produced by trans
					HashSet ops = model.getOperationsWithOutputElement(data2);
					// There are more than one operations in the set 'ops' when
					// there are alternative paths to produce data element
					// 'data2'
					if (ops.size() == 1) {

						// Create an output place for every data element in
						// 'precs'
						// Create an input place for every data element in
						// 'precs'
						// Create a transition for every data element in 'precs'
						Object[] precel = precs.values().toArray();
						for (int k = 0; k < precel.length; k++) {
							PDMDataElement dataElt = (PDMDataElement) precel[k];
							Place output = new Place("out_" + dataElt.getID(),
									result);
							result.addPlace(output);
							places.put("out_" + dataElt.getID(), output);
							Place input = new Place("in_" + dataElt.getID(),
									result);
							result.addPlace(input);
							places.put("in_" + dataElt.getID(), input);
							Transition t2 = new Transition(dataElt.getID(),
									result);
							result.addTransition(t2);
							trsVector.add(t2);

							// Add the edge from place in_x to transition x
							PNEdge e1 = new PNEdge(input, t2);
							result.addEdge(input, t2);
							edges.add(e1);
							// Add the edge from transition x to out_x
							PNEdge e2 = new PNEdge(t2, output);
							result.addEdge(t2, output);
							edges.add(e2);
							// Add the edge from prepare_y to in_x
							PNEdge e3 = new PNEdge(t3, input);
							result.addEdge(t3, input);
							edges.add(e3);
							// Add the edge from out_x to produce_y
							PNEdge e4 = new PNEdge(output, t4);
							result.addEdge(output, t4);
							edges.add(e4);
						}
						// Remove the original transition for 'data2'.
						if (transitions.containsKey(trans.getIdentifier())) {
							transitions.remove(trans.getIdentifier());
							result.delTransition(trans);
							trsVector.remove(trans);
						}
					}
					// There are more than one operations in the set 'ops' when
					// there are alternative paths to produce data element
					// 'data2'
					else if (ops.size() > 1) {
						// create the structure for the operation
						// First, the input and output places
						Place place5 = new Place("in_" + precs.toString(),
								result);
						Place place6 = new Place("out_" + precs.toString(),
								result);
						result.addPlace(place5);
						result.addPlace(place6);
						places.put("in_" + precs.toString(), place5);
						places.put("out_" + precs.toString(), place6);
						// Then the transitions
						Iterator iterator = ops.iterator();
						while (iterator.hasNext()) {
							PDMOperation op = (PDMOperation) iterator.next();
							Transition t5 = new Transition(op.getID(), result);
							result.addTransition(t5);
							trsVector.add(t5);
							PNEdge e5 = new PNEdge(place5, t5);
							PNEdge e6 = new PNEdge(t5, place6);
							result.addEdge(place5, t5);
							edges.add(e5);
							result.addEdge(t5, place6);
							edges.add(e6);
						}
						// And, the edges from the prepare_y to the input place
						// and from the output place to produce_y
						PNEdge e7 = new PNEdge(t3, place5);
						PNEdge e8 = new PNEdge(place6, t4);
						result.addEdge(t3, place5);
						result.addEdge(place6, t4);
						edges.add(e7);
						edges.add(e8);

						// create the structure for the input elements of the
						// operation
						// Create prepare_data2 and produce_data2 transitions
						Iterator it6 = ops.iterator();
						while (it6.hasNext()) {
							PDMOperation op = (PDMOperation) it6.next();
							HashMap input = op.getInputElements();
							Transition t6 = getTransitionWithID(trsVector, op
									.getID());
							if (input.size() == 1) {
								// transition t6 is replaced by the a transition
								// producing the only input element of the
								// operation t6
								Object[] ar = input.values().toArray();
								for (int i = 0; i < ar.length; i++) {
									PDMDataElement data = (PDMDataElement) ar[i];
									Transition t9 = new Transition(
											data.getID(), result);
									result.addTransition(t9);
									trsVector.add(t9);
									// Add the edges that flow into t6 to flow
									// into prepare_t6 and remove the edges to
									// t6
									HashSet edges5 = (HashSet) edges.clone();
									Iterator it11 = edges5.iterator();
									while (it11.hasNext()) {
										PNEdge edge1 = (PNEdge) it11.next();
										if (edge1.getDest().equals(t6)) {
											Place place3 = (Place) edge1
													.getSource();
											PNEdge edge3 = new PNEdge(place3,
													t9);
											result.addEdge(place3, t9);
											edges.add(edge3);
											result.removeEdge(edge1);
											edges.remove(edge1);
										}
									}
									// Add the edges that flow out of t6 to flow
									// out of produce_t6 and remove the edges to
									// t6
									HashSet edges6 = (HashSet) edges.clone();
									Iterator it12 = edges6.iterator();
									while (it12.hasNext()) {
										PNEdge edge1 = (PNEdge) it12.next();
										if (edge1.getSource().equals(t6)) {
											Place place2 = (Place) edge1
													.getDest();
											PNEdge edge2 = new PNEdge(t9,
													place2);
											result.addEdge(t9, place2);
											edges.add(edge2);
											result.removeEdge(edge1);
											edges.remove(edge1);
										}
									}
								}
							} else if (input.size() > 1) {
								// (transition t6 is replaced by prepare_data2
								// and produce_data2 transitions)
								Transition t9 = new Transition("prepare__"
										+ t6.getIdentifier(), result);
								result.addTransition(t9);
								trsVector.add(t9);
								Transition t10 = new Transition("produce__"
										+ t6.getIdentifier(), result);
								result.addTransition(t10);
								trsVector.add(t10);

								// Add the edges that flow into t6 to flow into
								// prepare_t6 and remove the edges to t6
								HashSet edges5 = (HashSet) edges.clone();
								Iterator it11 = edges5.iterator();
								while (it11.hasNext()) {
									PNEdge edge1 = (PNEdge) it11.next();
									if (edge1.getDest().equals(t6)) {
										Place place3 = (Place) edge1
												.getSource();
										PNEdge edge3 = new PNEdge(place3, t9);
										result.addEdge(place3, t9);
										edges.add(edge3);
										result.removeEdge(edge1);
										edges.remove(edge1);
									}
								}
								// Add the edges that flow out of t6 to flow out
								// of produce_t6 and remove the edges to t6
								HashSet edges6 = (HashSet) edges.clone();
								Iterator it12 = edges6.iterator();
								while (it12.hasNext()) {
									PNEdge edge1 = (PNEdge) it12.next();
									if (edge1.getSource().equals(t6)) {
										Place place2 = (Place) edge1.getDest();
										PNEdge edge2 = new PNEdge(t10, place2);
										result.addEdge(t10, place2);
										edges.add(edge2);
										result.removeEdge(edge1);
										edges.remove(edge1);
									}
								}

								Object[] in = input.values().toArray();
								for (int i = 0; i < in.length; i++) {
									// Add a transition an an input and output
									// place for each data element
									PDMDataElement data3 = (PDMDataElement) in[i];
									Place in2 = new Place(
											"in_" + data3.getID(), result);
									places.put("in_" + data3.getID(), in2);
									result.addPlace(in2);
									// Create an output place for the data
									// element
									Place out2 = new Place("out_"
											+ data3.getID(), result);
									places.put("out_" + data3.getID(), out2);
									result.addPlace(out2);
									// Create a transition for the data element
									Transition t7 = new Transition(data3
											.getID(), result);
									result.addTransition(t7);
									trsVector.add(t7);
									// System.out.println(transitions.values().toString());

									// Create an edge from the input place to
									// the transition of the data element
									PNEdge edgeIn2 = new PNEdge(in2, t7);
									result.addEdge(in2, t7);
									edges.add(edgeIn2);
									// Create an edge from the transition to the
									// output place of the data element
									PNEdge edgeOut2 = new PNEdge(t7, out2);
									result.addEdge(t7, out2);
									edges.add(edgeOut2);
									// Create an edge from the prepare to input
									// place
									PNEdge edge9 = new PNEdge(t9, in2);
									result.addEdge(t9, in2);
									edges.add(edge9);
									PNEdge edge10 = new PNEdge(out2, t10);
									result.addEdge(out2, t10);
									edges.add(edge10);
								}
							}
							// remove transition Op_x
							result.delTransition(t6);
							transitions.remove(t6.toString());
							trsVector.remove(t6);
						}
						// Remove the original transition for 'data2'.
					}
				}
				// Remove the original transition for 'data2'.
				transitions.remove(trans.getIdentifier());
				result.delTransition(trans);
				trsVector.remove(trans);
			}
			// Again the intersection of transition in the petrinet and data
			// elements in the PDM is determined to go to STEP2
			intersectionTC = determineIntersectionWithVector(trsVector, model);
		}

		// Wil's algorithm STEP 4

		// Remove each preparation transition which having just one input place
		// and one output place, and fuse the input and output place together.

		// Object[] trs = transitions.values().toArray();
		Iterator itv = trsVector.iterator();
		while (itv.hasNext()) {
			// Check all elements in graph
			Transition t5 = (Transition) itv.next();
			String t5id = t5.getIdentifier();
			if (t5id.startsWith("prep")) {
				HashSet edges4 = (HashSet) edges.clone();
				Iterator it5 = edges4.iterator();
				HashSet ins = new HashSet();
				HashSet outs = new HashSet();
				while (it5.hasNext()) {
					PNEdge edge5 = (PNEdge) it5.next();
					if (edge5.getSource().equals(t5)) {
						Place p = (Place) edge5.getDest();
						outs.add(p);
					}
					if (edge5.getDest().equals(t5)) {
						Place p2 = (Place) edge5.getSource();
						ins.add(p2);
					}
				}
				if (ins.size() == 1 && outs.size() == 1) {
					// Take the input place
					Iterator it6 = ins.iterator();
					Place in = (Place) it6.next();
					Iterator it7 = outs.iterator();
					Place out = (Place) it7.next();
					Iterator it8 = edges4.iterator();
					while (it8.hasNext()) {
						PNEdge edge6 = (PNEdge) it8.next();
						if (edge6.getSource().equals(out)) {
							Transition t6 = (Transition) edge6.getDest();
							PNEdge edge7 = new PNEdge(in, t6);
							result.addEdge(in, t6);
							edges.add(edge7);
							result.removeEdge(edge6);
							edges.remove(edge6);
						}
					}
					// remove output place
					result.delPlace(out);
					places.remove(out.toString());

					// remove transition
					result.delTransition(t5);
					transitions.remove(t5.toString());
				}
			}
		}
		// Add a fake Logevent to make the identifiers of the transitions
		// visible in the graph
		// LogEvent fake = new LogEvent("Fake", "complete");
		Object[] resTrans = result.getTransitions().toArray();
		for (int i = 0; i < resTrans.length; i++) {
			Transition tr = (Transition) resTrans[i];
			String trid = tr.getIdentifier();
			// if (!(trid.startsWith("prepare__")) &&
			// !(trid.startsWith("produce__"))) {
			LogEvent fake = new LogEvent(trid, "complete");
			tr.setModelElement(fake);
			// }
		}
		printTestOutput(result);
		return result;
	}

	/**
	 * Returns the identifier (or name) of the transition on place i in the
	 * vector
	 * 
	 * @param vector
	 *            Vector
	 * @param i
	 *            int
	 * @return String
	 */
	private String getNameOfVectorObject(Vector vector, int i) {
		String result = new String();
		Transition t = (Transition) vector.get(i);
		result = t.getIdentifier();
		return result;
	}

	private Vector determineIntersectionWithVector(Vector vector, PDMModel model) {
		Vector result = new Vector();
		Iterator it = vector.iterator();
		HashMap dataElements = model.getDataElements();
		while (it.hasNext()) {
			Transition t = (Transition) it.next();
			if (dataElements.containsKey(t.getIdentifier())) {
				result.add(t);
			}
		}
		return result;
	}

	private Transition getTransitionWithID(Vector vector, String id) {
		Iterator it = vector.iterator();
		Boolean found = false;
		Integer index = new Integer(0);
		while (!(found) && it.hasNext()) {
			Transition t = (Transition) it.next();
			String s = t.getIdentifier();
			if (s.equals(id)) {
				found = true;
				index = vector.indexOf(t);
			}
		}
		Transition result = (Transition) vector.get(index);
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
		Message.add("<PDMtoPM Alpha>", Message.TEST);
		Message.add("<Number of transitions = " + model.getTransitions().size()
				+ " >", Message.TEST);
		Message.add("<Number of places = " + model.getPlaces().size() + " >",
				Message.TEST);
		Message.add("</PDMtoPM Alpha>", Message.TEST);
	}

}
