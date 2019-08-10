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

package org.processmining.mining.partialordermining;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

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

public class PartialOrderAggregationPlugin implements MiningPlugin {

	public PartialOrderAggregationPlugin() {
	}

	public String getName() {
		return "Partial Order Aggregator";
	}

	public String getHtmlDescription() {
		return "This plugin takes a log where each instance represents a partial order on events."
				+ " These instances are then aggregated into an aggregation graph.";
	}

	public MiningResult mine(LogReader log) {
		Progress p = new Progress("Aggregating partial order of instance:", 0,
				log.numberOfInstances());
		MiningResult r = mine(log, p, check.isSelected());
		p.close();
		return r;
	}

	public MiningResult mine(LogReader log, Progress p, boolean useTransRed) {
		int nrTreatPis = 0;

		LogEvents events = log.getLogSummary().getLogEvents();
		HashMap event2Node = new HashMap(events.size());

		ModelGraph result = new AggregationGraph("Aggregation graph of :"
				+ log.getProcess(0).getName());

		// Generate a node for each LogEvent
		for (LogEvent evt : events) {
			ModelGraphVertex v = result.addVertex(new ModelGraphVertex(evt,
					result));
			event2Node.put(evt, v);
			v.setIdentifier(evt.getModelElementName() + "\\n"
					+ evt.getEventType());
		}
		ModelGraphVertex start = result.addVertex(new ModelGraphVertex(result));
		start.setIdentifier("Process Start");
		start.setValue(0);

		ModelGraphVertex end = result.addVertex(new ModelGraphVertex(result));
		end.setIdentifier("Process End");
		end.setValue(0);

		Iterator<ProcessInstance> it = log.instanceIterator();
		int j = 0;
		while (it.hasNext()) {
			p.inc();
			ProcessInstance pi = it.next();
			p.setNote("Process Instance:" + pi.getName());

			if (!pi.getAttributes().containsKey(ProcessInstance.ATT_PI_PO)
					|| !pi.getAttributes().get(ProcessInstance.ATT_PI_PO)
							.equals("true")) {
				// skip the process instance if it already is not a partial
				// order
				Message.add("Process Instance " + pi.getName()
						+ " is not a partial order.");
				continue;
			}
			nrTreatPis++;

			start.setValue(start.getValue() + 1);
			end.setValue(end.getValue() + 1);

			HashMap id2node = new HashMap();

			// First increase the occurences of ates
			Iterator<AuditTrailEntry> ateIt = pi.getAuditTrailEntryList()
					.iterator();
			while (ateIt.hasNext()) {
				AuditTrailEntry ate = ateIt.next();
				LogEvent le = events.findLogEvent(ate.getElement(), ate
						.getType());
				ModelGraphVertex v = (ModelGraphVertex) event2Node.get(le);
				// Increase the occurences of v
				v.setValue(v.getValue() + 1);
				id2node.put(
						ate.getAttributes().get(ProcessInstance.ATT_ATE_ID), v);
			}

			ModelGraph instanceGraph = getModelGraphInstance(pi, useTransRed);

			for (ModelGraphVertex v : (ArrayList<ModelGraphVertex>) instanceGraph
					.getVerticeList()) {
				AuditTrailEntry ate = (AuditTrailEntry) v.object;
				LogEvent le = events.findLogEvent(ate.getElement(), ate
						.getType());
				ModelGraphVertex node = (ModelGraphVertex) event2Node.get(le);
				if (v.inDegree() == 0) {
					// This is an initial node
					ModelGraphEdge edge = result.addEdge(start, node);
					edge.setValue(edge.getValue() + 1);
				}

				if (v.outDegree() == 0) {
					// This is a final node
					ModelGraphEdge edge = result.addEdge(node, end);
					edge.setValue(edge.getValue() + 1);
				}

			}

			for (ModelGraphEdge e : (ArrayList<ModelGraphEdge>) instanceGraph
					.getEdges()) {
				AuditTrailEntry ate = (AuditTrailEntry) e.getSource().object;
				LogEvent le = events.findLogEvent(ate.getElement(), ate
						.getType());
				ModelGraphVertex source = (ModelGraphVertex) event2Node.get(le);

				ate = (AuditTrailEntry) e.getDest().object;
				le = events.findLogEvent(ate.getElement(), ate.getType());
				ModelGraphVertex dest = (ModelGraphVertex) event2Node.get(le);

				ModelGraphEdge edge = result.addEdge(source, dest);
				edge.setValue(edge.getValue() + 1);

			}

		}
		p.close();
		Message.add("<ParitalOrderAggregator>", Message.TEST);
		Message.add("  <numberOfTreatedInstances=" + nrTreatPis + "/>",
				Message.TEST);
		result.Test("ResultGraph");
		Message.add("</ParitalOrderAggregator>", Message.TEST);
		return new AggregationGraphResult(log, result);
	}

	private ModelGraph getModelGraphInstance(ProcessInstance pi,
			boolean useTransRed) {
		ModelGraph g = new ModelGraph(pi.getName());

		AuditTrailEntryList ateList = pi.getAuditTrailEntryList();

		HashMap<String, ModelGraphVertex> Id2Vertex = new HashMap<String, ModelGraphVertex>();

		Iterator ateIt = ateList.iterator();
		String firstId = null;
		HashSet<String> ateIDs = new HashSet<String>();
		while (ateIt.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) ateIt.next();
			String ateID = ate.getAttributes().get(ProcessInstance.ATT_ATE_ID);
			if (firstId == null) {
				firstId = ateID;
			}
			ateIDs.add(ateID);

			Id2Vertex.put(ateID, g.addVertex(new ModelGraphVertex(ate, g)));

		}

		ateIt = ateList.iterator();
		while (ateIt.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) ateIt.next();
			String ateID = ate.getAttributes().get(ProcessInstance.ATT_ATE_ID);
			String ateSucs = ate.getAttributes().get(
					ProcessInstance.ATT_ATE_POST);
			StringTokenizer st = new StringTokenizer(ateSucs, ",", false);
			while (st.hasMoreTokens()) {
				String nextAteId = st.nextToken();
				if (ateIDs.contains(nextAteId)) {
					g.addEdge(Id2Vertex.get(ateID), Id2Vertex.get(nextAteId));
				}
			}
		}

		if (useTransRed) {
			g.reduceTransitively();
		}
		return g;
	}

	private JCheckBox check;

	public JPanel getOptionsPanel(LogSummary summary) {

		JPanel p = new JPanel(new FlowLayout());
		check = new JCheckBox(
				"<html>Check this box if transitive reduction should "
						+ "be used on the instance. Use this option if to many \"OR\" joins "
						+ "and splits are introduced<html>");
		p.add(check);
		return p;
	}

}
