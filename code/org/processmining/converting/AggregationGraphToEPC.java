package org.processmining.converting;

import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;
import org.processmining.mining.partialordermining.AggregationGraph;

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
public class AggregationGraphToEPC implements ConvertingPlugin {
	public AggregationGraphToEPC() {
	}

	public String getName() {
		return "Aggregation graph to EPC";
	}

	public String getHtmlDescription() {
		return "This plugin converts an aggregation graph to an EPC";
	}

	public MiningResult convert(ProvidedObject original) {
		int i = 0;
		AggregationGraph g = null;
		LogReader log = null;
		while (((g == null) || (log == null))
				&& (i < original.getObjects().length)) {
			if (original.getObjects()[i] instanceof AggregationGraph) {
				g = (AggregationGraph) original.getObjects()[i];
			}
			if (original.getObjects()[i] instanceof LogReader) {
				log = (LogReader) original.getObjects()[i];
			}
			i++;
		}
		EPC epc = toEPC(g);
		epc.Test("AggregationGraphConvertedTo");
		return new EPCResult(log, epc);
	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof AggregationGraph);
			i++;
		}
		return b;
	}

	private EPC toEPC(AggregationGraph aggregated) {
		EPC epc = new EPC(true);
		HashMap mapping = new HashMap();
		int counter = 0;

		// First, copy nodes as EPCFunctions
		// and introduce connectors

		HashMap in = new HashMap();
		HashMap out = new HashMap();

		Iterator it = aggregated.getVerticeList().iterator();
		while (it.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) it.next();
			Object o = v.object;
			EPCFunction f = epc.addFunction(o == null ? new EPCFunction(
					(LogEvent) null, epc) : new EPCFunction((LogEvent) o, epc));
			if (o == null) {
				f.setIdentifier("f" + counter);
				counter++;
			}

			if (v.inDegree() == 0) {
				EPCEvent event = epc.getEvent("status change to\\n"
						+ f.getIdentifier());
				if (event == null) {
					event = epc.addEvent(new EPCEvent("status change to\\n"
							+ f.getIdentifier(), epc));
					epc.addEdge(event, f);
				}

			}
			if (v.outDegree() == 0) {
				EPCEvent event = epc.getEvent(f.getIdentifier()
						+ "\\n finished");
				if (event == null) {
					event = epc.addEvent(new EPCEvent(f.getIdentifier()
							+ "\\n finished", epc));
					epc.addEdge(f, event);
				}

			}

			mapping.put(v, f);
			if (v.inDegree() > 1) {
				double s = sumOverEdgeValues(v.getInEdgesIterator());
				if (s == v.getValue()) {
					// Introduce XOR-join
					in.put(f, epc.addConnector(new EPCConnector(
							EPCConnector.XOR, epc)));
				} else if (s == (v.inDegree() * v.getValue())) {
					// Introduce AND-join
					in.put(f, epc.addConnector(new EPCConnector(
							EPCConnector.AND, epc)));
				} else {
					// Introduce OR-join
					in.put(f, epc.addConnector(new EPCConnector(
							EPCConnector.OR, epc)));
				}
			}
			if (v.outDegree() > 1) {
				double s = sumOverEdgeValues(v.getOutEdgesIterator());
				if (s == v.getValue()) {
					// Introduce XOR-split
					out.put(f, epc.addConnector(new EPCConnector(
							EPCConnector.XOR, epc)));
				} else if (s == (v.outDegree() * v.getValue())) {
					// Introduce AND-split
					out.put(f, epc.addConnector(new EPCConnector(
							EPCConnector.AND, epc)));
				} else {
					// Introduce OR-split
					out.put(f, epc.addConnector(new EPCConnector(
							EPCConnector.OR, epc)));
				}
				// Draw the edge to the output of f
				epc.addEdge(f, (EPCConnector) out.get(f));
			}
		}

		// Build the EPC by translating edges to events and connecting them with
		// connectors
		// and functionds
		it = aggregated.getEdges().iterator();
		while (it.hasNext()) {
			ModelGraphEdge e = (ModelGraphEdge) it.next();
			ModelGraphVertex n1 = (ModelGraphVertex) e.getSource();
			ModelGraphVertex n2 = (ModelGraphVertex) e.getDest();
			EPCFunction source = (EPCFunction) mapping.get(n1);
			EPCFunction dest = (EPCFunction) mapping.get(n2);
			/*
			 * if (source == null) { // initial edge. if (n1.outDegree() == 1) {
			 * // Only one outgoing arc at the start event if (in.get(dest) ==
			 * null) { epc.addEdge(start, dest); } else { epc.addEdge(start,
			 * (EPCConnector) in.get(dest)); } } else { // Multiple outgoing
			 * arcs from the start event if (out.get(start) == null) { double s
			 * = sumOverEdgeValues(n1.getOutEdgesIterator()); if (s ==
			 * n1.getValue()) { out.put(start, epc.addConnector(new
			 * EPCConnector(EPCConnector.XOR, epc))); } else if (s ==
			 * n1.getValue() * n1.outDegree()) { out.put(start,
			 * epc.addConnector(new EPCConnector(EPCConnector.AND, epc))); }
			 * else { out.put(start, epc.addConnector(new
			 * EPCConnector(EPCConnector.OR, epc))); } epc.addEdge(start,
			 * (EPCConnector) out.get(start)); } if (in.get(dest) == null) {
			 * epc.addEdge((EPCConnector) out.get(start), dest); } else {
			 * epc.addEdge((EPCConnector) out.get(start), (EPCConnector)
			 * in.get(dest)); } } continue; }
			 * 
			 * if (dest == null) { if (n2.inDegree() == 1) { // Only one
			 * Incoming arc at final event if (out.get(source) == null) {
			 * epc.addEdge(source, end); } else { epc.addEdge((EPCConnector)
			 * out.get(source), end); } } else { // Multiple Incoming arcs at
			 * final event if (in.get(end) == null) { double s =
			 * sumOverEdgeValues(n2.getInEdgesIterator()); if (s ==
			 * n2.getValue()) { in.put(end, epc.addConnector(new
			 * EPCConnector(EPCConnector.XOR, epc))); } else if (s ==
			 * n2.getValue() * n2.inDegree()) { in.put(end, epc.addConnector(new
			 * EPCConnector(EPCConnector.AND, epc))); } else { in.put(end,
			 * epc.addConnector(new EPCConnector(EPCConnector.OR, epc))); }
			 * epc.addEdge((EPCConnector) in.get(end), end); } if
			 * (out.get(source) == null) { epc.addEdge(source, (EPCConnector)
			 * in.get(end)); } else { epc.addEdge((EPCConnector)
			 * out.get(source), (EPCConnector) in.get(end)); } }
			 * 
			 * continue; }
			 */

			// A normal edge, not final or initial
			EPCEvent event = epc.getEvent("status change to\\n"
					+ dest.getIdentifier());
			if (event == null) {
				event = epc.addEvent(new EPCEvent("status change to\\n"
						+ dest.getIdentifier(), epc));
				epc.addEdge(event, dest);
			}

			if (in.get(dest) == null) {
				if (out.get(source) == null) {
					epc.addEdge(source, event);
				} else {
					epc.addEdge((EPCConnector) out.get(source), event);
				}
			} else {
				epc.addEdge((EPCConnector) in.get(dest), event);
				if (out.get(source) == null) {
					epc.addEdge(source, (EPCConnector) in.get(dest));
				} else {
					epc.addEdge((EPCConnector) out.get(source),
							(EPCConnector) in.get(dest));
				}
			}
		}

		return epc;
	}

	private double sumOverEdgeValues(Iterator i) {
		double r = 0;
		while (i.hasNext()) {
			r += ((ModelGraphEdge) i.next()).getValue();
		}
		return r;
	}

}
