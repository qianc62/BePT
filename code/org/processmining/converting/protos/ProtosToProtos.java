package org.processmining.converting.protos;

/*
 * Author: Mariska Netjes
 * (c) 2008 Technische Universiteit Eindhoven and STW
 */

import org.processmining.converting.Converter;
import org.processmining.framework.models.protos.*;
import java.util.HashMap;
import java.util.ArrayList;
import org.processmining.mining.protosmining.ProtosResult;
import org.processmining.converting.ConvertingPlugin;
import org.processmining.framework.ui.Message;

public class ProtosToProtos {

	/**********************************************************************
	 * Adding implicit conditions.
	 */

	@Converter(name = "Protos: Add implicit conditions", help = "http://prom.win.tue.nl/research/wiki/online/protos2protos")
	public static ProtosResult addImplicitConditions(ProtosModel model) {
		HashMap map = new HashMap();
		ProtosResult result = new ProtosResult(null, addImplicitConditions(
				model, map));
		return result;
	}

	/**
	 * Make a copy of the given Protos model that contains all implicit
	 * conditions / statusses.
	 * 
	 * @param model
	 *            ProtosModel The Protos model to copy.
	 * @param map
	 *            HashMap The map to fill (every Protos object will be mapped
	 *            onto its copy in this map).
	 * @return ProtosModel The copy of the Protos model.
	 */
	public static ProtosModel addImplicitConditions(ProtosModel model,
			HashMap map) {
		ProtosModel newModel = new ProtosModel(model.getName());
		map.put(model, newModel);
		/*
		 * Convert all subprocesses.
		 */
		for (ProtosSubprocess process : model.getSubprocesses()) {
			ProtosSubprocess newSubprocess = new ProtosSubprocess(process
					.getName());
			newModel.addSubprocess(newSubprocess);
			map.put(process, newSubprocess);
			addImplicitConditions(model, process, map);
		}
		return newModel;
	}

	/**
	 * Copy the subprocess, add status if none between two activities.
	 * 
	 * @param model
	 *            ProtosModel The Protos model.
	 * @param process
	 *            ProtosSubprocess The subprocess to copy.
	 * @param map
	 *            HashMap Maps every object to its copy.
	 */
	private static void addImplicitConditions(ProtosModel model,
			ProtosSubprocess process, HashMap map) {
		/*
		 * First, get the copy of the subprocess.
		 */
		ProtosSubprocess copyOfSubprocess = (ProtosSubprocess) map.get(process);
		/*
		 * Second, copy all nodes.
		 */

		for (ProtosFlowElement node : process.getFlowElements()) {
			if (node.isStatus()) {
				ProtosFlowElement status = (ProtosFlowElement) node;
				ProtosFlowElement copyOfStatus = copyOfSubprocess.addStatus(
						status.getID(), status.getName());
				map.put(status, copyOfStatus);
			} else if (node.isActivity()) {
				ProtosFlowElement activity = (ProtosFlowElement) node;
				ProtosFlowElement copyOfActivity = copyOfSubprocess
						.addActivity(activity);
				map.put(activity, copyOfActivity);
			}
		}
		/*
		 * Third, copy all arcs, but introduce a Status for an Activity-Activity
		 * edge.
		 */
		for (Object object : process.getArcs()) {
			ProtosProcessArc arc = (ProtosProcessArc) object;
			ProtosFlowElement fromNode = (ProtosFlowElement) map
					.get((ProtosFlowElement) process.getFlowElement(arc
							.getSource()));
			ProtosFlowElement toNode = (ProtosFlowElement) map
					.get((ProtosFlowElement) process.getFlowElement(arc
							.getTarget()));
			if ((fromNode.isActivity()) && (toNode.isActivity())) {
				/*
				 * Create a new status.
				 */
				String constructedName = "place_" + fromNode.getName() + "_"
						+ toNode.getName();
				ProtosFlowElement status = copyOfSubprocess.addStatus(
						constructedName, constructedName);
				/*
				 * Create an arc from the fromNode to the new status.
				 */
				ProtosProcessArc copyOfArc = (ProtosProcessArc) copyOfSubprocess
						.addArc(fromNode.getID(), status.getID());
				map.put(arc, copyOfArc);
				/*
				 * Create an arc from the new status to the toNode.
				 */
				ProtosProcessArc extraArc = (ProtosProcessArc) copyOfSubprocess
						.addArc(status.getID(), toNode.getID());
				map.put(arc, extraArc);
			} else {
				/*
				 * Copy the original edge.
				 */
				ProtosProcessArc copyOfArc = (ProtosProcessArc) copyOfSubprocess
						.addArc(fromNode.getID(), toNode.getID());
				map.put(arc, copyOfArc);
			}
		}

		int size, nofTransitions, nofPlaces, nofEdges;
		size = map.size();
		nofTransitions = copyOfSubprocess.getActivities().size();
		nofPlaces = copyOfSubprocess.getStatuses().size();
		nofEdges = copyOfSubprocess.getArcs().size(); // null

		Message.add("<ProtosToProtos nofTransitions=\"" + nofTransitions
				+ "\" nofPlaces=\"" + nofPlaces + "\" nofEdges=\"" + nofEdges
				+ "\" sizeMap=\"" + size + "\"/>", Message.TEST);
	}
}
