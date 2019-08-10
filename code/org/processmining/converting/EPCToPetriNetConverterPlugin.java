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

package org.processmining.converting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.algorithms.EPCToPetriNetConverter;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.framework.ui.Message;

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

public class EPCToPetriNetConverterPlugin implements ConvertingPlugin {

	// TODO Eric: Can you please fill these mappings while
	// converting the epc model to a pn?
	protected HashMap<EPCFunction, Transition> functionActivityMapping;
	protected HashMap<EPCConnector, Place> xorconnectorChoiceMapping;

	public String getName() {
		return "EPC to Petrinet";
	}

	public String getHtmlDescription() {
		return "<p> <b>Plug-in: EPC to Petrinet</b>"
				+ "<p>This plug-in converts an EPC into a Petri net, with invisible/routing transitions. "
				+ "<p>This Plug-in is used by the EPC verification plugin. For more "
				+ "details about that plugin, see "
				+ org.processmining.framework.util.Constants.get_BVD_URLString(
						"EPC_verification", "this paper")
				+ " for a description of the verification approach, which includes the reduction."
				+ "<p>An application of this plugin to a real life dataset can "
				+ "be found "
				+ org.processmining.framework.util.Constants.get_BVD_URLString(
						"SAP_reduction", "here");
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
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof EPC);
			b |= (original.getObjects()[i] instanceof ConfigurableEPC);
			i++;
		}

		Object o = original.getObjects()[i - 1];
		ConfigurableEPC baseEPC = (ConfigurableEPC) o;
		PetriNet reduced = convert(baseEPC);
		reduced.Test("EPCconvertedTo");

		i = 0;
		b = false;
		while (!b && (i < original.getObjects().length)) {
			b = (original.getObjects()[i] instanceof LogReader);
			i++;
		}
		if (!b) {
			return new PetriNetResult(reduced);
		} else {
			return new PetriNetResult((LogReader) original.getObjects()[i - 1],
					reduced);
		}
	}

	/**
	 * Converts the given EPC model into a Petri net model.
	 * 
	 * @param model
	 *            the EPC model to convert
	 * @return the resulting Petri net model
	 */
	public PetriNet convert(ConfigurableEPC baseEPC) {
		// HV: Initialize the mappings.
		functionActivityMapping = new HashMap<EPCFunction, Transition>();
		xorconnectorChoiceMapping = new HashMap<EPCConnector, Place>();

		// Check to use the weights if necessary
		// HV: Add both mappings. On completion, these will be filledd.
		PetriNet petrinet = EPCToPetriNetConverter.convert(baseEPC,
				new HashMap(), functionActivityMapping,
				xorconnectorChoiceMapping);

		HashSet visible = new HashSet();

		// HV: The next block is taken care of by the functionActivityMapping
		// below.
		/*
		 * Iterator it = petrinet.getTransitions().iterator(); while
		 * (it.hasNext()) { Transition t = (Transition) it.next(); if (t.object
		 * instanceof EPCFunction) { // if (t.getLogEvent() != null) { // Add
		 * transitions with LogEvent (i.e. referring to functions)
		 * visible.add(t); } }
		 */

		// HV: Prevent the places mapped onto from being reduced.
		visible.addAll(functionActivityMapping.values());
		visible.addAll(xorconnectorChoiceMapping.values());
		Message.add(visible.toString(), Message.DEBUG);

		Iterator it = petrinet.getPlaces().iterator();
		while (it.hasNext()) {
			Place p = (Place) it.next();
			if (p.inDegree() * p.outDegree() == 0) {
				// Add Initial and final places to visible, i.e. places that
				// refer to in and output events
				visible.add(p);
			}
		}

		// Reduce the PetriNet with Murata rules, while keeping the visible ones
		PetriNetReduction pnred = new PetriNetReduction();
		pnred.setNonReducableNodes(visible);

		HashMap pnMap = new HashMap(); // Used to map pre-reduction nodes to
		// post-reduction nodes.
		PetriNet reduced = pnred.reduce(petrinet, pnMap);

		if (reduced != petrinet) {
			// Update both mappings from pre-reduction nodes to post-reduction
			// nodes.
			HashMap<EPCFunction, Transition> newFunctionActivityMapping = new HashMap<EPCFunction, Transition>();
			for (EPCFunction function : functionActivityMapping.keySet()) {
				Transition transition = (Transition) functionActivityMapping
						.get(function);
				if (pnMap.keySet().contains(transition)) {
					newFunctionActivityMapping.put(function, (Transition) pnMap
							.get(transition));
				}
			}
			functionActivityMapping = newFunctionActivityMapping;
			HashMap<EPCConnector, Place> newXorconnectorChoiceMapping = new HashMap<EPCConnector, Place>();
			for (EPCConnector connector : xorconnectorChoiceMapping.keySet()) {
				Place place = (Place) xorconnectorChoiceMapping.get(connector);
				if (pnMap.keySet().contains(place)) {
					newXorconnectorChoiceMapping.put(connector, (Place) pnMap
							.get(place));
				}
			}
			xorconnectorChoiceMapping = newXorconnectorChoiceMapping;
		}
		reduced.makeClusters();
		return reduced;
	}

	/**
	 * Returns a mapping of the input epc functions to the corresponding
	 * transitions in the converted model.
	 * 
	 * @return the mapping of epc functions to transitions
	 */
	public HashMap<EPCFunction, Transition> getEPCFuncttionToTransitionMapping() {
		return functionActivityMapping;
	}

	/**
	 * Returns a mapping of the input epc (e.g., xor) connectors to the
	 * corresponding places in the converted model.
	 * 
	 * @return the mapping of epc connectors to places
	 */
	public HashMap<EPCConnector, Place> getEPCConnectorToPlaceMapping() {
		return xorconnectorChoiceMapping;
	}
}
