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

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.epcmining.EPCResult;
import org.processmining.framework.models.protos.ProtosModel;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.protos.ProtosSubprocess;
import org.processmining.framework.models.protos.ProtosFlowElement;
import java.util.HashSet;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import java.util.HashMap;
import org.processmining.framework.models.protos.ProtosProcessArc;
import org.processmining.framework.models.epcpack.algorithms.ConnectorStructureExtractor;
import java.util.Iterator;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: ProtosToEPC
 * </p>
 * 
 * <p>
 * Description: Converts a Protos subprocess into an EPC model.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class ProtosToEPC implements ConvertingPlugin {
	public ProtosToEPC() {
	}

	public String getName() {
		return "Protos to EPC";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:protos2eps";
	}

	/**
	 * Converts a Protos subprocess into an EPC model.
	 * 
	 * @param object
	 *            ProvidedObject Should contain a Protos subprocess.
	 * @return EPCResult The resulting EPC model.
	 */
	public EPCResult convert(ProvidedObject object) {
		ProtosSubprocess provided = null;
		LogReader log = null;

		for (int i = 0; provided == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof ProtosSubprocess) {
				provided = (ProtosSubprocess) object.getObjects()[i];
			}
			if (object.getObjects()[i] instanceof ProtosModel) {
				ProtosModel model = (ProtosModel) object.getObjects()[i];
				provided = model.getSubprocesses().iterator().next();
			}
			if (object.getObjects()[i] instanceof LogReader) {
				log = (LogReader) object.getObjects()[i];
			}
		}

		if (provided == null) {
			return null;
		}

		ConfigurableEPC epc = result(provided);
		return new EPCResult(log, epc);
	}

	/**
	 * Checks whether a ProtosSubprocess or ProtosModel is provided.
	 * 
	 * @param object
	 *            ProvidedObject The provided objects.
	 * @return boolean Whether a Petri net is among the provided objects.
	 */
	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof ProtosSubprocess) {
				return true;
			}
			if (object.getObjects()[i] instanceof ProtosModel) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert the given ProtosSubprocess into a ConfigurableEPC
	 * 
	 * @param process
	 *            ProtosSubprocess
	 * @return ConfigurableEPC
	 */
	public ConfigurableEPC result(ProtosSubprocess process) {
		HashMap<String, EPCConnector> pointsOfEntry = new HashMap<String, EPCConnector>();
		HashMap<String, EPCConnector> pointsOfExit = new HashMap<String, EPCConnector>();
		ConfigurableEPC epc = new ConfigurableEPC();

		// Convert every activity into a chain of a join connector, an event, a
		// function, and a split connector.
		HashSet<ProtosFlowElement> activities = process.getActivities();
		for (ProtosFlowElement activity : activities) {
			// Create the function and the event.
			EPCFunction function = new EPCFunction(new LogEvent(activity
					.getName(), "complete"), epc);
			epc.addFunction(function);
			EPCEvent event = new EPCEvent("status change to "
					+ activity.getName(), epc);
			epc.addEvent(event);

			// Determine the types for the connectors.
			int joinType = activity.getJoinType();
			if (joinType == YAWLTask.NONE) {
				joinType = YAWLTask.AND;
			}
			int splitType = activity.getSplitType();
			if (splitType == YAWLTask.NONE) {
				splitType = YAWLTask.XOR;
			}

			// Create the connectors.
			EPCConnector joinConnector = new EPCConnector(joinType, epc);
			epc.addConnector(joinConnector);
			EPCConnector splitConnector = new EPCConnector(splitType, epc);
			epc.addConnector(splitConnector);

			// Create the chain.
			epc.addEdge(joinConnector, event);
			epc.addEdge(event, function);
			epc.addEdge(function, splitConnector);

			// Add the connectors as point of entry and point of exit for this
			// chain.
			pointsOfEntry.put(activity.getID(), joinConnector);
			pointsOfExit.put(activity.getID(), splitConnector);
		}

		// Convert every status into a chain of a join connector and a split
		// conenctor.
		HashSet<ProtosFlowElement> statuses = process.getStatuses();
		for (ProtosFlowElement status : statuses) {
			// Similar to activities, except for the fact that we do not add an
			// event and a function, and that
			// the types of the connectors is fixed to XOR.
			EPCConnector joinConnector = new EPCConnector(YAWLTask.XOR, epc);
			epc.addConnector(joinConnector);
			EPCConnector splitConnector = new EPCConnector(YAWLTask.XOR, epc);
			epc.addConnector(splitConnector);

			epc.addEdge(joinConnector, splitConnector);

			pointsOfEntry.put(status.getID(), joinConnector);
			pointsOfExit.put(status.getID(), splitConnector);
		}

		// Convert every edge.
		HashSet<ProtosProcessArc> arcs = process.getArcs();
		for (ProtosProcessArc arc : arcs) {
			EPCConnector pointOfEntry = pointsOfEntry.get(arc.getTarget());
			EPCConnector pointOfExit = pointsOfExit.get(arc.getSource());
			if (pointOfEntry != null && pointOfExit != null) {
				epc.addEdge(pointOfExit, pointOfEntry);
			}
		}

		// Some connectors will have no outgoing edges. Add a final event to
		// these connectors.
		HashSet<String> finalIDs = new HashSet<String>(pointsOfExit.keySet());
		for (ProtosProcessArc arc : arcs) {
			if (finalIDs.contains(arc.getSource())) {
				finalIDs.remove(arc.getSource());
			}
		}
		for (String finalID : finalIDs) {
			EPCEvent finalEvent = new EPCEvent("end " + finalID, epc);
			epc.addEvent(finalEvent);

			epc.addEdge(pointsOfExit.get(finalID), finalEvent);
		}

		// Reduce the connectors as much as possible, while leaving the events
		// and functions in tact.
		epc = ConnectorStructureExtractor.extract(epc, true, false, false,
				false, false, false, false, true, false, false);

		// Remove any source/sink connector.
		Iterator it = epc.getConnectors().iterator();
		while (it.hasNext()) {
			EPCConnector connector = (EPCConnector) it.next();
			if (connector.getPredecessors().isEmpty()
					|| connector.getSuccessors().isEmpty()) {
				epc.delConnector(connector);
				it = epc.getConnectors().iterator();
			}
		}

		int nofFunctions, nofEvents, nofConnectors[] = { 0, 0, 0, 0, 0, 0, 0 }, nofEdges;
		nofFunctions = epc.getFunctions().size();
		nofEvents = epc.getEvents().size();
		nofEdges = epc.getEdges().size();
		for (Object obj : epc.getConnectors()) {
			EPCConnector con = (EPCConnector) obj;
			nofConnectors[con.getType()]++;
		}
		Message.add("<ProtosToEPC nofFunctions=\"" + nofFunctions
				+ "\" nofEvents=\"" + nofEvents + "\" nofOrs=\""
				+ nofConnectors[EPCConnector.OR] + "\" nofAnds=\""
				+ nofConnectors[EPCConnector.AND] + "\" nofXors=\""
				+ nofConnectors[EPCConnector.XOR] + "\" nofEdges=\"" + nofEdges
				+ "\"/>", Message.TEST);

		// We're done.
		return epc;
	}
}
