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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.pdm.PDMDataElement;
import org.processmining.framework.models.pdm.PDMModel;
import org.processmining.framework.models.pdm.PDMOperation;
import org.processmining.framework.models.yawl.YAWLCondition;
import org.processmining.framework.models.yawl.YAWLDecomposition;
import org.processmining.framework.models.yawl.YAWLEdge;
import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.yawlmining.YAWLResult;

/**
 * <p>
 * Title: PDMtoPMD *
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
public class PDMtoPMD implements ConvertingPlugin {
	public PDMtoPMD() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getName() {
		return "Product Data Model to Process Model algorithm Delta";
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
		YAWLModel yawlmodel = convert(model);
		LogReader log = null;
		return new YAWLResult(log, yawlmodel);
	}

	public YAWLModel convert(PDMModel model) {
		YAWLModel result = new YAWLModel("YAWL model");
		PDMDataElement root = model.getRootElement(); // the "root element of
		// the PDM

		HashMap places = new HashMap(); // set of places (P)
		HashMap transitions = new HashMap(); // set of transitions (T)
		HashSet edges = new HashSet(); // set of edges (F)
		HashMap dataEls = model.getDataElements();

		YAWLDecomposition decomp = new YAWLDecomposition("main", "true",
				"NetFactsType");
		result.addDecomposition("main", decomp);

		// HV: Keep track whether decompositions are required for task, router,
		// and start.
		boolean hasTask = false, hasRouter = false, hasStart = false;

		// Create an input place, a transition, and an output place for every
		// data element in the PDM, plus a control input place
		Object[] dataArray = dataEls.values().toArray();
		for (int i = 0; i < dataArray.length; i++) {
			PDMDataElement d = (PDMDataElement) dataArray[i];
			YAWLCondition p1 = new YAWLCondition(result, 2, "in_" + d.getID());
			places.put(p1.getIdentifier(), p1);
			decomp.addCondition(p1.getIdentifier());
			YAWLTask t = new YAWLTask(result, 3, 3, null, d.getID(), null);
			transitions.put(t.getIdentifier(), t);
			decomp.addTask(d.getID(), "and", "and", "task", null);
			hasTask = true; // HV: At least one task requires the task
			// decomposition.
			// LogEvent fake = new LogEvent("Fake", "complete");
			// t.setLogEvent(fake);
			YAWLCondition p2 = new YAWLCondition(result, 2, "out_" + d.getID());
			places.put(p2.getIdentifier(), p2);
			if (d.equals(root)) {
				decomp.addOutputCondition(p2.getIdentifier());
			} else if (!(d.equals(root))) {
				decomp.addCondition(p2.getIdentifier());
			}
			YAWLCondition p3 = new YAWLCondition(result, 2, "C_in_" + d.getID());
			places.put(p3.getIdentifier(), p3);
			decomp.addCondition(p3.getIdentifier());

			YAWLEdge e1 = new YAWLEdge(p1, t);
			edges.add(e1);
			decomp.addEdge(p1.getIdentifier(), t.getIdentifier(), false,
					"true", null);
			YAWLEdge e2 = new YAWLEdge(t, p2);
			edges.add(e2);
			decomp.addEdge(t.getIdentifier(), p2.getIdentifier(), false,
					"true", null);
			YAWLEdge e3 = new YAWLEdge(p3, t);
			edges.add(e3);
			decomp.addEdge(p3.getIdentifier(), t.getIdentifier(), false,
					"true", null);

		}

		// Create an invisible transition for every operation in the PDM
		HashMap ops = model.getOperations();
		Object[] opArray = ops.values().toArray();
		for (int j = 0; j < opArray.length; j++) {
			PDMOperation op = (PDMOperation) opArray[j];
			HashMap inputs = op.getInputElements();
			HashMap outputs = op.getOutputElements();
			if (!(inputs.isEmpty())) {
				YAWLTask t = new YAWLTask(result, 3, 3, null, op.getID(), null);
				transitions.put(t.getIdentifier(), t);
				decomp.addTask(op.getID(), "and", "and", "router", null);
				hasRouter = true; // HV: At least one task requires the router
				// decomposition.
				// Also add an initial input/control place to every invisible
				// transition to make sure it can only fire once
				YAWLCondition c = new YAWLCondition(result, 2, "C_"
						+ op.getID());
				places.put(c.getIdentifier(), c);
				decomp.addCondition(c.getIdentifier());
				YAWLEdge e = new YAWLEdge(c, t);
				edges.add(e);
				decomp.addEdge(c.getIdentifier(), t.getIdentifier(), false,
						"true", null);

				// An edge from every input element to the invisible transition
				// (AND back)
				Object[] ins = inputs.values().toArray();
				for (int k = 0; k < ins.length; k++) {
					PDMDataElement data1 = (PDMDataElement) ins[k];
					YAWLCondition p = (YAWLCondition) places.get("out_"
							+ data1.getID());
					YAWLEdge e1 = new YAWLEdge(p, t);
					YAWLEdge e2 = new YAWLEdge(t, p);
					edges.add(e1);
					edges.add(e2);
					decomp.addEdge(p.getIdentifier(), t.getIdentifier(), false,
							"true", null);
					decomp.addEdge(t.getIdentifier(), p.getIdentifier(), false,
							"true", null);
				}
				// An edge from the transition to every output element.
				Object[] outs = outputs.values().toArray();
				for (int m = 0; m < outs.length; m++) {
					PDMDataElement data1 = (PDMDataElement) outs[m];
					YAWLCondition p2 = (YAWLCondition) places.get("in_"
							+ data1.getID());
					YAWLEdge e2 = new YAWLEdge(t, p2);
					edges.add(e2);
					decomp.addEdge(t.getIdentifier(), p2.getIdentifier(),
							false, "true", null);
				}
			}
		}
		// Add a start place and start transition
		YAWLCondition start = new YAWLCondition(result, 2, "start");
		YAWLTask tstart = new YAWLTask(result, 3, 3, null, "t_start", null);
		YAWLEdge p = new YAWLEdge(start, tstart);
		places.put("start", start);
		transitions.put("t_start", tstart);
		edges.add(p);
		decomp.addInputCondition(start.getIdentifier());
		decomp.addTask(tstart.getIdentifier(), "and", "and", "start", null);
		hasStart = true; // HV: At least one task requires the start
		// decomposition.
		decomp.addEdge(start.getIdentifier(), tstart.getIdentifier(), false,
				"true", null);

		// Add edges from start transition to all leaf elements of the PDM
		HashMap leafs = model.getLeafElements();
		Object[] leafArray = leafs.values().toArray();
		for (int ii = 0; ii < leafArray.length; ii++) {
			PDMDataElement d3 = (PDMDataElement) leafArray[ii];
			YAWLCondition p3 = (YAWLCondition) places.get("in_" + d3.getID());
			YAWLEdge e3 = new YAWLEdge(tstart, p3);
			edges.add(e3);
			decomp.addEdge(tstart.getIdentifier(), p3.getIdentifier(), false,
					"true", null);
		}

		// Add edges from start transition to all enabling places to the
		// transitions
		Object[] pls2 = places.values().toArray();
		for (int ii = 0; ii < pls2.length; ii++) {
			YAWLCondition c = (YAWLCondition) pls2[ii];
			String cc = c.getIdentifier();
			if (cc.startsWith("C_")) {
				YAWLEdge e3 = new YAWLEdge(tstart, c);
				edges.add(e3);
				decomp.addEdge(tstart.getIdentifier(), c.getIdentifier(),
						false, "true", null);
			}
		}

		// Add resetEdges from all places to the root element, except from the
		// start place and out_root places.
		YAWLTask task = (YAWLTask) transitions.get(root.getID());
		Object[] pls = places.values().toArray();
		for (int jj = 0; jj < pls.length; jj++) {
			YAWLCondition p4 = (YAWLCondition) pls[jj];
			if ((!p4.equals(start))
					&& (!p4.equals(places.get("out_" + root.getID())))) {
				YAWLEdge e4 = new YAWLEdge(task, p4);
				edges.add(e4);
				decomp.addResetEdge(task.getIdentifier(), p4.getIdentifier());
			}
		}

		// HV: Add the decompositions required for the tasks.
		if (hasTask) {
			result.addDecomposition("task", new YAWLDecomposition("task",
					"false", "WebServiceGatewayFactsType"));
		}
		if (hasRouter) {
			result.addDecomposition("router", new YAWLDecomposition("router",
					"false", "WebServiceGatewayFactsType"));
		}
		if (hasStart) {
			result.addDecomposition("start", new YAWLDecomposition("start",
					"false", "WebServiceGatewayFactsType"));
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

	protected void printTestOutput(YAWLModel model) {
		Message.add("<PDMtoPM Delta>", Message.TEST);
		Collection col = model.getDecompositions();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			YAWLDecomposition dec = (YAWLDecomposition) it.next();
			Collection nodes = dec.getNodes();
			ArrayList edges = dec.getEdges();
			Message.add("<Number of YAWL nodes = " + nodes.size() + " >",
					Message.TEST);
			Message.add("<Number of YAWL edges = " + edges.size() + " >",
					Message.TEST);
		}
		Message.add("</PDMtoPM Delta>", Message.TEST);
	}

}
