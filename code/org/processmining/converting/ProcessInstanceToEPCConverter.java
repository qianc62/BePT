package org.processmining.converting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEdge;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.epcpack.algorithms.ConnectorStructureExtractor;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;

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
public class ProcessInstanceToEPCConverter implements ConvertingPlugin {
	public ProcessInstanceToEPCConverter() {
	}

	public String getName() {
		return "Partial order to EPC";
	}

	public String getHtmlDescription() {
		return "This plugin converts process instances with partial order information to an EPC";
	}

	public MiningResult convert(ProvidedObject original) {
		int k = 0;
		ProcessInstance pi = null;
		while ((pi == null) && (k < original.getObjects().length)) {
			if ((original.getObjects()[k] instanceof ProcessInstance)
					&& (((ProcessInstance) original.getObjects()[k])
							.getAttributes().containsKey(
									ProcessInstance.ATT_PI_PO) && ((ProcessInstance) original
							.getObjects()[k]).getAttributes().get(
							ProcessInstance.ATT_PI_PO).equals("true"))) {
				pi = (ProcessInstance) original.getObjects()[k];
			}
			k++;
		}

		ConfigurableEPC net = new EPC(false);
		Iterator i;

		net.setIdentifier(pi.getName());

		HashMap mapping = new HashMap();

		// copy nodes as transitions

		i = pi.getAuditTrailEntryList().iterator();

		while (i.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) i.next();

			EPCFunction f = new EPCFunction(new LogEvent(ate.getElement(), ate
					.getType()), net);
			net.addFunction(f);
			mapping.put(ate.getAttributes().get(ProcessInstance.ATT_ATE_ID), f);
		}

		i = pi.getAuditTrailEntryList().iterator();
		while (i.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) i.next();

			String post = ate.getAttributes().get(ProcessInstance.ATT_ATE_POST);
			if (post == null) {
				post = "";
			}
			StringTokenizer st = new StringTokenizer(post, ",", false);

			if (!st.hasMoreTokens()) {
				EPCEvent e = net.addEvent(new EPCEvent(
						"Status changed to \\n case complete", net));
				net.addEdge((EPCFunction) mapping.get(ate.getAttributes().get(
						ProcessInstance.ATT_ATE_ID)), e);
			} else {

				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.AND, net));
				net.addEdge((EPCFunction) mapping.get(ate.getAttributes().get(
						ProcessInstance.ATT_ATE_ID)), c);

				while (st.hasMoreTokens()) {
					EPCFunction f = (EPCFunction) mapping.get(st.nextToken());
					EPCEvent e = net.addEvent(new EPCEvent(
							"Status changed to \\n" + f.getIdentifier(), net));
					net.addEdge(e, f);
					net.addEdge(c, e);
				}
			}
		}

		i = net.getFunctions().iterator();

		while (i.hasNext()) {
			EPCFunction f = (EPCFunction) i.next();
			if (f.inDegree() == 0) {
				EPCEvent e = net.addEvent(new EPCEvent("Status changed to \\n"
						+ f.getIdentifier(), net));
				net.addEdge(e, f);
			} else if (f.inDegree() > 1) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.AND, net));
				Iterator it2 = f.getInEdgesIterator();
				while (it2.hasNext()) {
					EPCEdge edge = (EPCEdge) it2.next();
					net.addEdge((EPCObject) edge.getSource(), c);
					net.delEdge(edge.getSource(), edge.getDest());
				}
				net.addEdge(c, f);
			}

		}

		net = ConnectorStructureExtractor.extract(net, true, false, false,
				false, false, false, false, true, false, false);
		return new EPCResult(null, net);

	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof ProcessInstance)
					&& (((ProcessInstance) original.getObjects()[i])
							.getAttributes().containsKey(
									ProcessInstance.ATT_PI_PO) && ((ProcessInstance) original
							.getObjects()[i]).getAttributes().get(
							ProcessInstance.ATT_PI_PO).equals("true"));
			i++;
		}
		return b;
	}
}
