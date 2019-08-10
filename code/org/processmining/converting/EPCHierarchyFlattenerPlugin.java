package org.processmining.converting;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEdge;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCHierarchy;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.epcpack.EPCSubstFunction;
import org.processmining.framework.models.epcpack.algorithms.EPCCopier;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;

public class EPCHierarchyFlattenerPlugin implements ConvertingPlugin {
	public EPCHierarchyFlattenerPlugin() {
	}

	public String getName() {
		return "Flatten EPC Hierarchy";
	}

	public String getHtmlDescription() {
		return "This plugin converts a hierarchy of EPCs to one EPC, by substituting \"substitution functions\" with their underlying EPCs."
				+ "Thid plugin will only give a result if the underlying EPCs start and finish with the same events as the top-level "
				+ "\"substitution function\". As a base EPC, the selected EPC is used.";
	}

	public MiningResult convert(ProvidedObject original) {
		int i = 0;
		EPCHierarchy epcHier = null;
		EPC epc = null;
		LogReader log = null;
		while ((i < original.getObjects().length)) {
			if (original.getObjects()[i] instanceof EPCHierarchy) {
				epcHier = (EPCHierarchy) original.getObjects()[i];
			}
			if (original.getObjects()[i] instanceof LogReader) {
				log = (LogReader) original.getObjects()[i];
			}
			i++;
		}
		epc = (EPC) epcHier.getSelectedNode();

		HashMap org2dup = new HashMap();
		HashMap dup2org = new HashMap();

		ConfigurableEPC newEPC = EPCCopier.copy(epc, org2dup, dup2org);

		Iterator it = newEPC.getFunctions().iterator();
		while (it.hasNext()) {
			Iterator it2;
			EPCFunction f = (EPCFunction) it.next();
			if (!(f instanceof EPCSubstFunction)) {
				continue;
			}
			// f is a substitution function in EPC
			EPCSubstFunction sf = (EPCSubstFunction) f;
			// sf is a substitution function in newEPC
			ConfigurableEPC subEPC = sf.getSubstitutedEPC();

			ArrayList orgPreEvt = newEPC.getPreceedingEvents(sf);
			ArrayList orgSucEvt = newEPC.getSucceedingEvents(sf);
			HashMap evtMap = new HashMap();

			// remove the substitution function
			newEPC.delFunction(sf);

			// now for each of these preceeding events, add an extra and-split
			// connector
			it2 = orgPreEvt.iterator();
			while (it2.hasNext()) {
				EPCEvent e = (EPCEvent) it2.next();
				EPCObject dest = (e.outDegree() > 0 ? (EPCObject) e
						.getSuccessors().iterator().next() : null);
				EPCConnector and = newEPC.addConnector(new EPCConnector(
						EPCConnector.AND, newEPC));
				if (dest != null) {
					newEPC.delEdge(e, dest);
				}
				newEPC.addEdge(e, and);
				if (dest != null) {
					newEPC.addEdge(and, dest);
				}
				evtMap.put(e, and);
			}

			// now for each of these succeeding events, add an extra xor-join
			// connector
			it2 = orgSucEvt.iterator();
			while (it2.hasNext()) {
				EPCEvent e = (EPCEvent) it2.next();
				EPCObject src = (e.inDegree() > 0 ? (EPCObject) e
						.getPredecessors().iterator().next() : null);
				EPCConnector xor = newEPC.addConnector(new EPCConnector(
						EPCConnector.XOR, newEPC));
				if (src != null) {
					newEPC.delEdge(src, e);
				}
				newEPC.addEdge(xor, e);
				if (src != null) {
					newEPC.addEdge(src, xor);
				}
				evtMap.put(e, xor);
			}

			HashMap mapping = new HashMap();
			// copy the substituted EPC into newEPC
			// First, the functions
			it2 = subEPC.getFunctions().iterator();
			while (it2.hasNext()) {
				EPCFunction fun = (EPCFunction) it2.next();
				EPCFunction newFun;
				if (fun instanceof EPCSubstFunction) {
					newFun = newEPC.addFunction(new EPCSubstFunction(fun
							.getLogEvent(), fun.isConfigurable(), newEPC,
							((EPCSubstFunction) fun).getSubstitutedEPC()));
				} else {
					newFun = newEPC.addFunction(new EPCFunction(fun
							.getLogEvent(), fun.isConfigurable(), newEPC));
				}
				newFun.setIdentifier(fun.getIdentifier());
				mapping.put(fun.getIdKey(), newFun);
			}
			// Second, the connectors
			it2 = subEPC.getConnectors().iterator();
			while (it2.hasNext()) {
				EPCConnector c = (EPCConnector) it2.next();
				mapping.put(c.getIdKey(), newEPC.addConnector(new EPCConnector(
						c.getType(), newEPC)));
			}
			// Now, the events, except initial and final
			it2 = subEPC.getEvents().iterator();
			while (it2.hasNext()) {
				EPCEvent evt = (EPCEvent) it2.next();
				EPCObject newEvt = null;
				if (evt.inDegree() == 0) {
					Iterator it3 = orgPreEvt.iterator();
					while (it3.hasNext() && (newEvt == null)) {
						EPCEvent e = (EPCEvent) it3.next();
						if (e.getIdentifier().equals(evt.getIdentifier())) {
							newEvt = (EPCObject) evtMap.get(e);
						}
					}
				} else if (evt.outDegree() == 0) {
					Iterator it3 = orgSucEvt.iterator();
					while (it3.hasNext() && (newEvt == null)) {
						EPCEvent e = (EPCEvent) it3.next();
						if (e.getIdentifier().equals(evt.getIdentifier())) {
							newEvt = (EPCObject) evtMap.get(e);
						}
					}
				} else {
					newEvt = newEPC.addEvent(new EPCEvent(evt.getIdentifier(),
							newEPC));
				}
				if (newEvt == null) {
					// some initial or final event doesn't match
					Message
							.add("Some substitution function does not have the same initial and final events as the substituted EPC, aborting!");
					return null;
				}
				mapping.put(evt.getIdKey(), newEvt);
			}
			// Finally, add the edge
			it2 = subEPC.getEdges().iterator();
			while (it2.hasNext()) {
				EPCEdge e = (EPCEdge) it2.next();
				EPCObject source = (EPCObject) mapping.get(e.getSource()
						.getIdKey());
				EPCObject dest = (EPCObject) mapping
						.get(e.getDest().getIdKey());
				newEPC.addEdge(source, dest);
			}
			it = newEPC.getFunctions().iterator();
		}

		it = newEPC.getConnectors().iterator();
		while (it.hasNext()) {
			EPCConnector c = (EPCConnector) it.next();
			if (c.inDegree() == 0 || c.outDegree() == 0) {
				newEPC.delConnector(c);
				it = newEPC.getConnectors().iterator();
			} else if (c.inDegree() == 1 && c.outDegree() == 1) {
				EPCObject source = (EPCObject) c.getPredecessors().iterator()
						.next();
				EPCObject dest = (EPCObject) c.getSuccessors().iterator()
						.next();
				newEPC.delConnector(c);
				newEPC.addEdge(source, dest);
				it = newEPC.getConnectors().iterator();
			}

		}
		newEPC.Test("EPCflattenedTo");

		return new EPCResult(log, newEPC);
	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean epcHier = false;
		while (!(epcHier) && (i < original.getObjects().length)) {
			epcHier |= (original.getObjects()[i] instanceof EPCHierarchy)
					&& (((EPCHierarchy) original.getObjects()[i])
							.getSelectedNode() instanceof EPC);
			i++;
		}
		return epcHier;
	}

}
