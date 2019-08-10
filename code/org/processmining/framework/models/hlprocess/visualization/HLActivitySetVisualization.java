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
package org.processmining.framework.models.hlprocess.visualization;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.activitygraph.ActivityGraph;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.HLTypes.Perspective;
import org.processmining.framework.models.hlprocess.hlmodel.HLActivitySet;

/**
 * 
 * @author anne
 * 
 */
public class HLActivitySetVisualization extends ActivityGraph {

	/** the high level activity set */
	protected HLActivitySet hlActivitySet;
	/** the perspectives that need to be shown in the picture */
	protected Set<Perspective> perspectivesToShow = new HashSet<Perspective>();
	/**
	 * the mapping for an object to its corresponding boxID and which is needed
	 * for DOT.
	 */
	protected HashMap mappingBoxesToBoxId = new HashMap();

	/**
	 * TODO: check why does not work just with adding vertices to model graph -
	 * redundant bookkeeping should not be necessary. (grappa complexity)
	 */
	// protected ArrayList<ModelGraphVertex> activities = new
	// ArrayList<ModelGraphVertex>();
	public HLActivitySetVisualization(HLActivitySet actSet,
			Set<Perspective> thePerspectivesToShow) {
		super();
		hlActivitySet = actSet;
		perspectivesToShow = thePerspectivesToShow;
		ActivityGraph graph = (ActivityGraph) hlActivitySet.getProcessModel();

		Iterator<ModelGraphVertex> vertices = graph.getActivityVertices()
				.iterator();
		while (vertices.hasNext()) {
			ModelGraphVertex vert = vertices.next();
			ModelGraphVertex copy = new ModelGraphVertex(vert);
			this.addActivityVertex(copy);
		}

		// ////
		// // arraylist for all nodes in the petri net
		// ArrayList<ModelGraphVertex> nodeList = new
		// ArrayList<ModelGraphVertex>();
		// Subgraph graph2 =
		// graph.getGrappaVisualization().getSubgraph().getGraph();
		// // add the nodes that are not in a cluster to the list of all nodes
		// Enumeration nodeElts = graph2.nodeElements();
		// while (nodeElts.hasMoreElements()) {
		// ModelGraphVertex e1 = (ModelGraphVertex) nodeElts.nextElement();
		// if (e1.object != null && e1.object instanceof ModelGraphVertex) {
		// nodeList.add(e1);
		// }
		// }
		//
		// //Iterator<ModelGraphVertex> vertices =
		// graph.getVerticeList().iterator();
		// Iterator<ModelGraphVertex> vertices = nodeList.iterator();
		// while (vertices.hasNext()) {
		// ModelGraphVertex vert = vertices.next();
		// ModelGraphVertex copy = new ModelGraphVertex(vert);
		// this.addVertex(copy);
		// copy.setSubgraph(this);
		// activities.add(copy);
		// }
	}

	//
	// public void addAndLinkActivityVertex(ModelGraphVertex av) {
	// vertices.add(av);
	// av.setSubgraph(this);
	// activities.add(av);
	// }

	// /**
	// * Sets the perspectives that should be visualized.
	// * <br>
	// * They will apply for successive getGrappaVisualization() calls
	// * @see ModelGraph#getGrappaVisualization
	// * @param perspectivesToShow the perspectives that should be visualized
	// */
	// public void setPerspectives(Set<Perspective> perspectives) {
	// if (perspectives != null) {
	// perspectivesToShow = perspectives;
	// }
	// //return this.getGrappaVisualization();
	// }

	/**
	 * When a activity graph is asked for its visualization, a temporary dot
	 * file is written and afterwards read by grappa to convert it into a java
	 * frame. For the activity graph, only the activities will be displayed in
	 * the picture.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 */
	public void writeToDot(Writer bw) throws IOException {
		nodeMapping.clear();
		bw
				.write("digraph G {ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Arial\";rankdir=\"TB\";compound=\"true\" \n");
		bw.write("edge [arrowsize=\"0.5\"];\n");
		bw
				.write("node [height=\".2\",width=\"1\",fontname=\"Arial\",fontsize=\"8\"];\n");
		//
		// in the case that separate boxes needs to be created and that
		// connections need
		// to be made between these boxes and transitions, some additional code
		// needs
		// can be filled in here. (e.g. for the data perspective)
		if (perspectivesToShow.contains(HLTypes.Perspective.DATA_AT_TASKS)) {
			// visualize the data attributes
			Iterator<HLAttribute> dataAttrs = hlActivitySet.getHLProcess()
					.getAttributes().iterator();
			int counterDataAttrs = 0;
			while (dataAttrs.hasNext()) {
				HLAttribute dataAttr = dataAttrs.next();
				counterDataAttrs++;
				String idBox = "data" + counterDataAttrs;
				dataAttr.writeDistributionToDot(idBox, "", "", bw);
				// add the data attribute to the mapping together with the
				// corresponding box id
				mappingBoxesToBoxId.put(dataAttr, idBox);
			}
		}
		// present organizational information
		if (perspectivesToShow.contains(HLTypes.Perspective.ROLES_AT_TASKS)) {
			// visualize the groups
			Iterator<HLGroup> groups = hlActivitySet.getHLProcess().getGroups()
					.iterator();
			int counterGroups = 0;
			while (groups.hasNext()) {
				HLGroup group = groups.next();
				counterGroups++;
				String idBox = "group" + counterGroups;
				group.writeDistributionToDot(idBox, "", "", bw);
				// add the group to the mapping together with the corresponding
				// box id
				mappingBoxesToBoxId.put(group, idBox);
			}
			// visualize the resources in one box
			Iterator<HLResource> resources = hlActivitySet.getHLProcess()
					.getResources().iterator();
			String resourcesLabel = "";
			while (resources.hasNext()) {
				HLResource resource = resources.next();
				resourcesLabel = resourcesLabel + resource.getName() + "\\n";
			}
			if (!resourcesLabel.equals("")) {
				bw.write("resources" + " [shape=\"ellipse\", label=\""
						+ resourcesLabel + "\"];\n");
			}
		}

		// write the activities to dot
		for (int i = 0; i < activities.size(); i++) {
			ModelGraphVertex av = (ModelGraphVertex) activities.get(i);
			bw
					.write("node"
							+ av.getId()
							+ " [shape=\"box\", style=\"filled\",fillcolor=\"lavenderblush1\",label=\""
							+ av.getIdentifier() + "\",fontsize=6];\n");
			nodeMapping.put(new String("node" + av.getId()), av);
			// get the corresponding high level activity
			HLActivity act = hlActivitySet.findActivity(av);
			if (perspectivesToShow
					.contains(HLTypes.Perspective.TIMING_EXECTIME)) {
				act.getExecutionTime().writeDistributionToDot("i" + i,
						"node" + av.getId(), "Execution time:", bw);
			}
			if (perspectivesToShow
					.contains(HLTypes.Perspective.TIMING_WAITTIME)) {
				act.getWaitingTime().writeDistributionToDot("i" + i,
						"node" + av.getId(), "Waiting time:", bw);
			}
			if (perspectivesToShow.contains(HLTypes.Perspective.TIMING_SOJTIME)) {
				act.getSojournTime().writeDistributionToDot("i" + i,
						"node" + av.getId(), "Sojourn time:", bw);
			}

			// if the data perspective has been selected, then the data
			// attributes of the highlevelactivities need
			// to be connected to the box of the corresponding data attribute
			if (perspectivesToShow.contains(HLTypes.Perspective.DATA_AT_TASKS)) {
				// connect input attributes
				Iterator<HLAttribute> inputDataAttrs = act
						.getInputDataAttributes().iterator();
				while (inputDataAttrs.hasNext()) {
					HLAttribute dataAttr = inputDataAttrs.next();
					// get the corresponding boxID
					String boxID = (String) mappingBoxesToBoxId.get(dataAttr);
					// write the edge
					bw.write("node" + av.getId() + " -> " + boxID
							+ " [dir=none, style=dotted];\n");
				}
				// connect output attributes
				Iterator<HLAttribute> outputDataAttrs = act
						.getOutputDataAttributes().iterator();
				while (outputDataAttrs.hasNext()) {
					HLAttribute dataAttr = outputDataAttrs.next();
					// get the corresponding boxID
					String boxID = (String) mappingBoxesToBoxId.get(dataAttr);
					// write the edge
					bw.write("node" + av.getId() + " -> " + boxID
							+ " [dir=none, style=dotted];\n");
				}
			}
			// if the organizational perspective has been selected, then the
			// groups of the highlevelactivities need
			// to be connected to the box of the corresponding group
			if (perspectivesToShow.contains(HLTypes.Perspective.ROLES_AT_TASKS)) {
				if (act.getGroup() != null) {
					HLGroup group = act.getGroup();
					// get the corresponding boxID
					String boxID = (String) mappingBoxesToBoxId.get(group);
					// write the edge
					bw.write("node" + av.getId() + " -> " + boxID
							+ " [dir=none, style=dotted];\n");
				}
			}
		}
		bw.write("}\n");
	}

}
