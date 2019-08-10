/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2008 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.converting.bpmn2yawl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.bpmn.*;
import org.processmining.framework.models.yawl.YAWLCondition;
import org.processmining.framework.models.yawl.bpmn.YAWLDecompositionBPMN;
import org.processmining.framework.models.yawl.YAWLEdge;
import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.yawl.YAWLNode;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.yawlmining.YAWLResult;
import org.processmining.converting.ConvertingPlugin;

/**
 * <p>
 * Title: BPMN to YAWL model convertor
 * </p>
 * 
 * <p>
 * Description: Takes a BPMN and converts it into a YAWL model.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * @version 1.0
 */
public class BPMNToYAWL implements ConvertingPlugin {
	private ArrayList<BpmnEdge> m_edges = new ArrayList<BpmnEdge>();

	public BPMNToYAWL() {
	}

	public String getName() {
		return "BPMN to YAWL";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:wfn2yawl";
	}

	public MiningResult convert(ProvidedObject object) {
		BpmnGraph providedPN = null;
		LogReader log = null;

		for (int i = 0; i < object.getObjects().length; i++) {
			if (providedPN == null
					&& object.getObjects()[i] instanceof BpmnGraph) {
				providedPN = (BpmnGraph) object.getObjects()[i];
			}
			if (log == null && object.getObjects()[i] instanceof LogReader) {
				log = (LogReader) object.getObjects()[i];
			}
		}

		if (providedPN == null) {
			return null;
		}

		YAWLModel model = convert(providedPN);
		model.Test("BPMNToYAWL");

		return new YAWLResult(log, model);
	}

	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof BpmnGraph) {
				return true;
			}
		}
		return false;
	}

	public YAWLModel convert(BpmnGraph bpmn) {

		YAWLModel yawl = new YAWLModel("Bpmnnet");
		BpmnProcessModel bpm = bpmn.getProcess();
		YAWLDecompositionBPMN ydRoot = new YAWLDecompositionBPMN("root",
				"true", "NetFactsType");
		yawl.addDecomposition("root", ydRoot);

		addSubprocess(bpmn, yawl, bpm, ydRoot, false);

		return yawl;
	}

	private void addSubprocess(BpmnGraph bpmn, YAWLModel yawl,
			BpmnProcessModel bpm, YAWLDecompositionBPMN ydRoot, boolean isAdhoc) {
		// add the only input condition
		String inCondId = "inCond_" + ydRoot.getIdentifier();
		YAWLCondition inputCond = ydRoot.addInputCondition(inCondId);
		String inTaskId = "inTask_" + ydRoot.getIdentifier();
		YAWLTask inputTask = ydRoot.addTask(inTaskId, "or", isAdhoc ? "and"
				: "or", "", null);
		YAWLEdge inputEdge = ydRoot.addEdge(inCondId, inTaskId, false, null,
				null);
		// Manual for manual added input condition, will be deleted when
		// converted back to BPMN
		setAttributes(inputCond, BpmnXmlTags.EVENTYPE, new String[] {
				BpmnXmlTags.BPMN_MANUAL, null, BpmnXmlTags.BPMN_START, null });
		setAttributes(inputTask, BpmnXmlTags.TASKTYPE, new String[] {
				BpmnXmlTags.BPMN_MANUAL, null, null, null, null, null, null,
				null, null });
		setAttributes(inputEdge, BpmnXmlTags.EDGETYPE, new String[] {
				BpmnXmlTags.BPMN_MANUAL, null, null, null });
		// add the only output condition
		String outCondId = "outCond_" + ydRoot.getIdentifier();
		YAWLCondition outputCond = ydRoot.addOutputCondition(outCondId);
		String outTaskId = "outTask_" + ydRoot.getIdentifier();
		YAWLTask outputTask = ydRoot.addTask(outTaskId, isAdhoc ? "and" : "or",
				"or", "", null);
		YAWLEdge outputEdge = ydRoot.addEdge(outTaskId, outCondId, false, null,
				null);
		// Manual for manual added output condition, will be deleted when
		// converted back to BPMN
		setAttributes(outputCond, BpmnXmlTags.EVENTYPE, new String[] {
				BpmnXmlTags.BPMN_MANUAL, null, BpmnXmlTags.BPMN_END, null });
		setAttributes(outputTask, BpmnXmlTags.TASKTYPE, new String[] {
				BpmnXmlTags.BPMN_MANUAL, null, null, null, null, null, null,
				null, null });
		setAttributes(outputEdge, BpmnXmlTags.EDGETYPE, new String[] {
				BpmnXmlTags.BPMN_MANUAL, null, null, null });
		// the control condition for the ad hoc subprocess
		String controlCondId = "controlCond_" + ydRoot.getIdentifier();
		if (isAdhoc) {
			YAWLCondition controlCond = ydRoot.addCondition(controlCondId);
			controlCond.setAttribute("nodeid", "prefix_" + controlCondId);
			setAttributes(controlCond, BpmnXmlTags.EVENTYPE, new String[] {
					BpmnXmlTags.BPMN_MANUAL, null,
					BpmnXmlTags.BPMN_INTERMEDIATE, null });
			YAWLEdge inControlEdge = ydRoot.addEdge(inTaskId, controlCondId,
					false, null, null);
			setAttributes(inControlEdge, BpmnXmlTags.EDGETYPE, new String[] {
					BpmnXmlTags.BPMN_MANUAL, null, null, null });
			YAWLEdge outControlEdge = ydRoot.addEdge(controlCondId, outTaskId,
					false, null, null);
			setAttributes(outControlEdge, BpmnXmlTags.EDGETYPE, new String[] {
					BpmnXmlTags.BPMN_MANUAL, null, null, null });
		}

		inputTask.setAttribute("nodeid", "prefix_" + inTaskId);
		outputTask.setAttribute("nodeid", "prefix_" + outTaskId);

		ArrayList<YAWLTask> adhocTasks = new ArrayList<YAWLTask>();
		ArrayList<YAWLNode> arCancels = new ArrayList<YAWLNode>();
		HashMap<String, ArrayList<YAWLNode>> arCancelPreds = new HashMap<String, ArrayList<YAWLNode>>();
		HashMap<String, ArrayList<YAWLNode>> arCancelSuccs = new HashMap<String, ArrayList<YAWLNode>>();
		HashMap<String, YAWLEdge> edges = new HashMap<String, YAWLEdge>();

		// enumerate all subprocesses
		BpmnSubProcess[] arSubs = bpm.getBpmnSubProcesses();
		for (BpmnSubProcess bsp : arSubs) {
			String name = bsp.getNameAndId();
			YAWLDecompositionBPMN ydNextRoot = new YAWLDecompositionBPMN(name,
					"false", "NetFactsType");
			YAWLTask task = ydRoot.addTask(name, isAdhoc ? "and" : "or",
					isAdhoc ? "and" : "or", name, null);
			task.setAttribute("nodeid", "prefix_" + name);
			setAttributes(task, BpmnXmlTags.TASKTYPE, new String[] {
					BpmnXmlTags.BPMN_SUBPROCESS, bsp.getLane(), null,
					bsp.getLoopType(), bsp.isTiming(),
					bsp.isCompensation() ? "true" : null,
					bsp.isAdHoc() ? "true" : null,
					bsp.isExpanded() ? "true" : null, bsp.getTransaction() });
			if (isAdhoc) {
				adhocTasks.add(task);
			}
			yawl.addDecomposition(name, ydNextRoot);
			addSubprocess(bpmn, yawl, bsp.getProcessModel(), ydNextRoot, bsp
					.isAdHoc());
		}

		// enumerate all nodes
		BpmnObject[] arNodes = bpm.getBpmnNodes();
		for (BpmnObject node : arNodes) {
			String nameAndId = node.getNameAndId();
			if (node instanceof BpmnTask) {
				BpmnTask t = (BpmnTask) node;
				YAWLTask task = ydRoot.addTask(nameAndId, isAdhoc ? "and"
						: "or", isAdhoc ? "and" : "or", "", null);
				task.setAttribute("nodeid", "prefix_" + nameAndId);
				setAttributes(task, BpmnXmlTags.TASKTYPE, new String[] {
						BpmnXmlTags.BPMN_TASK, t.getLane(), null,
						t.getLoopType(), t.isTiming(),
						t.isCompensation() ? "true" : null, null, null,
						t.getTransaction() });
				if (isAdhoc) {
					adhocTasks.add(task);
				}
			} else if (node instanceof BpmnSwimPool) {
				ydRoot.addPoolLane(node.getName(), true);
			} else if (node instanceof BpmnSwimLane) {
				String pid = node.getpid();
				if (pid == null) {
					pid = "";
				}
				ydRoot.addPoolLane(node.getName() + "@" + pid, false);
			} else if (node instanceof BpmnEvent) {
				BpmnEvent event = (BpmnEvent) node;
				BpmnEventType bet = event.getTypeTag();
				BpmnEventTriggerType bett = event.getTrigger();
				YAWLCondition cond = ydRoot.addCondition(nameAndId);
				cond.setAttribute("nodeid", "prefix_" + nameAndId);
				setAttributes(cond, BpmnXmlTags.EVENTYPE, new String[] {
						BpmnXmlTags.BPMN_ORIGINAL, event.getLane(),
						bet.toString(), bett == null ? null : bett.toString() });
				if (bet == BpmnEventType.Start) {
					// create an edge to connect the inputTask and this start
					// event
					YAWLEdge edge = ydRoot.addEdge(inTaskId, nameAndId, false,
							bett == null ? null : bett.toString(), null);
					setAttributes(edge, BpmnXmlTags.EDGETYPE, new String[] {
							BpmnXmlTags.BPMN_MANUAL, null, null, null });
				} else if (bet == BpmnEventType.End) {
					// create an edge to connect this end event and the output
					// task
					YAWLEdge edge = ydRoot.addEdge(nameAndId, outTaskId, false,
							bett == null ? null : bett.toString(), null);
					setAttributes(edge, BpmnXmlTags.EDGETYPE, new String[] {
							BpmnXmlTags.BPMN_MANUAL, null, null, null });
				} else {
					if (bett == BpmnEventTriggerType.Cancel
							|| bett == BpmnEventTriggerType.Exception
							|| bett == BpmnEventTriggerType.Error) {
						arCancels.add(cond);
					}
				}
			} else if (node instanceof BpmnGateway) {
				BpmnGateway bg = (BpmnGateway) node;
				BpmnGatewayType bgt = bg.getType();
				YAWLTask task = null;
				String subType = null;
				if (bgt == BpmnGatewayType.AND) {
					task = ydRoot.addTask(nameAndId, BpmnXmlTags.BPMN_GWT_AND,
							BpmnXmlTags.BPMN_GWT_AND, "", null);
					subType = BpmnXmlTags.BPMN_GWT_AND;
				} else if (bgt == BpmnGatewayType.XOR) {
					task = ydRoot.addTask(nameAndId, BpmnXmlTags.BPMN_GWT_XOR,
							BpmnXmlTags.BPMN_GWT_XOR, "", null);
					subType = BpmnXmlTags.BPMN_GWT_XOR;
				} else if (bgt == BpmnGatewayType.OR) {
					task = ydRoot.addTask(nameAndId, BpmnXmlTags.BPMN_GWT_OR,
							BpmnXmlTags.BPMN_GWT_OR, "", null);
					subType = BpmnXmlTags.BPMN_GWT_OR;
				} else if (bgt == BpmnGatewayType.Complex) {
					task = ydRoot.addTask(nameAndId, BpmnXmlTags.BPMN_GWT_OR,
							BpmnXmlTags.BPMN_GWT_OR, "", null);
					subType = BpmnXmlTags.BPMN_GWT_COMPLEX;
				} else {
					task = ydRoot.addTask(nameAndId, BpmnXmlTags.BPMN_GWT_OR,
							BpmnXmlTags.BPMN_GWT_OR, "", null);
					subType = BpmnXmlTags.BPMN_GWT_NORMAL;
				}
				task.setAttribute("nodeid", "prefix_" + nameAndId);
				setAttributes(task, BpmnXmlTags.TASKTYPE, new String[] {
						BpmnXmlTags.BPMN_GATEWAY, bg.getLane(), subType, null,
						null, null, null, null, null });
			}
		}

		// enumerate all edges
		BpmnEdge[] arEdges = bpm.getBpmnEdges();
		for (BpmnEdge edge : arEdges) {
			if (!addEdge(edge, bpmn, yawl, edges)) {
				m_edges.add(edge);
			}
		}
		for (int i = m_edges.size() - 1; i >= 0; i--) {
			BpmnEdge edge = m_edges.get(i);
			if (addEdge(edge, bpmn, yawl, edges)) {
				m_edges.remove(i);
			}
		}

		// find all isolated tasks in this yawl model and connect them
		Iterator<YAWLNode> nodes = ydRoot.getNodes().iterator();
		while (nodes.hasNext()) {
			YAWLNode node = nodes.next();
			if (node instanceof YAWLTask) {
				if (node.getPredecessors().size() == 0) {
					if (node != outputTask) {
						YAWLEdge edge = ydRoot.addEdge(inTaskId, ((String) node
								.getAttributeValue("nodeid")).substring(7),
								false, null, null);
						setAttributes(edge, BpmnXmlTags.EDGETYPE, new String[] {
								BpmnXmlTags.BPMN_MANUAL, null, null, null });
						setAttributes(edge, BpmnXmlTags.EDGETYPE, new String[] {
								BpmnXmlTags.BPMN_MANUAL, null, null, null });
					}
				}
				if (node.getSuccessors().size() == 0) {
					if (node != inputTask) {
						YAWLEdge edge = ydRoot.addEdge(((String) node
								.getAttributeValue("nodeid")).substring(7),
								outTaskId, false, null, null);
						setAttributes(edge, BpmnXmlTags.EDGETYPE, new String[] {
								BpmnXmlTags.BPMN_MANUAL, null, null, null });
					}
				}
			} else if (node instanceof YAWLCondition) {
				if (node.getPredecessors().size() == 0
						&& node.getSuccessors().size() == 0) {
					String nodeId = ((String) node.getAttributeValue("nodeid"))
							.substring(7);
					YAWLEdge edge = ydRoot.addEdge(inTaskId, nodeId, false,
							null, null);
					setAttributes(edge, BpmnXmlTags.EDGETYPE, new String[] {
							BpmnXmlTags.BPMN_MANUAL, null, null, null });
					edges.put(inTaskId + "_" + nodeId, edge);
					edge = ydRoot.addEdge(nodeId, outTaskId, false, null, null);
					setAttributes(edge, BpmnXmlTags.EDGETYPE, new String[] {
							BpmnXmlTags.BPMN_MANUAL, null, null, null });
					edges.put(nodeId + "_" + outTaskId, edge);
				}
			}
		}

		// connect all adhoc tasks to the control condition
		for (YAWLTask task : adhocTasks) {
			String name = ((String) task.getAttributeValue("nodeid"))
					.substring(7);
			YAWLEdge fromControlEdge = ydRoot.addEdge(controlCondId, name,
					false, null, null);
			setAttributes(fromControlEdge, BpmnXmlTags.EDGETYPE, new String[] {
					BpmnXmlTags.BPMN_MANUAL, null, null, null });
			YAWLEdge toControlEdge = ydRoot.addEdge(name, controlCondId, false,
					null, null);
			setAttributes(toControlEdge, BpmnXmlTags.EDGETYPE, new String[] {
					BpmnXmlTags.BPMN_MANUAL, null, null, null });
		}

		// find all cancel events' predecessors and successors with the same
		// name
		ArrayList<String> arDiffCancels = new ArrayList<String>();
		ArrayList<String> arCancelEdges = new ArrayList<String>();
		for (YAWLNode node : arCancels) {
			String name = node.getAttributeValue(BpmnXmlTags.BPMN_PROP_TRIGGER)
					+ "_" + node.getIdentifier();
			if (!arDiffCancels.contains(name)) {
				arDiffCancels.add(name);
			}
			ArrayList<YAWLNode> alPreds = arCancelPreds.get(name);
			if (alPreds == null) {
				alPreds = new ArrayList<YAWLNode>();
				arCancelPreds.put(name, alPreds);
			}
			Iterator<YAWLNode> preds = node.getPredecessors().iterator();
			while (preds.hasNext()) {
				YAWLNode pred = preds.next();
				if (!alPreds.contains(pred)) {
					alPreds.add(pred);
				}
				arCancelEdges.add(((String) pred.getAttributeValue("nodeid"))
						.substring(7)
						+ "_"
						+ ((String) node.getAttributeValue("nodeid"))
								.substring(7));
			}
			ArrayList<YAWLNode> alSuccs = arCancelSuccs.get(name);
			if (alSuccs == null) {
				alSuccs = new ArrayList<YAWLNode>();
				arCancelSuccs.put(name, alSuccs);
			}
			Iterator<YAWLNode> succs = node.getSuccessors().iterator();
			while (succs.hasNext()) {
				YAWLNode succ = succs.next();
				if (!alSuccs.contains(succ)) {
					alSuccs.add(succ);
				}
				arCancelEdges.add(((String) node.getAttributeValue("nodeid"))
						.substring(7)
						+ "_"
						+ ((String) succ.getAttributeValue("nodeid"))
								.substring(7));
			}
		}
		// handle the conversion from cancel events to reset edges
		for (int i = 0; i < arDiffCancels.size(); i++) {
			String name = arDiffCancels.get(i);
			// add a manual task representing this cancel event
			String cancelId = name + "\tcancel_" + ydRoot.getIdentifier();
			YAWLTask cancelTask = ydRoot
					.addTask(cancelId, BpmnXmlTags.BPMN_GWT_OR,
							BpmnXmlTags.BPMN_GWT_AND, "", null);
			int idx = name.indexOf('_');
			setAttributes(cancelTask, BpmnXmlTags.TASKTYPE, new String[] {
					name.substring(0, idx), null, null, null, null, null, null,
					null, null });
			// add an edge to its self with manual type
			YAWLEdge selfEdge = ydRoot.addEdge(cancelId, cancelId, false, null,
					null);
			setAttributes(selfEdge, BpmnXmlTags.EDGETYPE, new String[] {
					BpmnXmlTags.BPMN_MANUAL, null, null, null });
			ArrayList<YAWLNode> alPreds = arCancelPreds.get(name);
			for (YAWLNode pred : alPreds) {
				String predId = ((String) pred.getAttributeValue("nodeid"))
						.substring(7);
				YAWLEdge inEdge = ydRoot.addEdge(predId, cancelId, false, null,
						null);
				setAttributes(inEdge, BpmnXmlTags.EDGETYPE, new String[] {
						BpmnXmlTags.BPMN_MANUAL, null, null, null });
			}
			ArrayList<YAWLNode> alSuccs = arCancelSuccs.get(name);
			for (YAWLNode succ : alSuccs) {
				String succId = ((String) succ.getAttributeValue("nodeid"))
						.substring(7);
				YAWLEdge outEdge = ydRoot.addEdge(cancelId, succId, false,
						null, null);
				setAttributes(outEdge, BpmnXmlTags.EDGETYPE, new String[] {
						BpmnXmlTags.BPMN_MANUAL, null, null, null });
				outEdge.setType(YAWLEdge.RESET);
			}
		}
		// delete all cancel edges
		for (String edgeName : arCancelEdges) {
			YAWLEdge edge = edges.get(edgeName);
			ydRoot.removeEdge(edge);
		}
		// delete all cancel events
		for (YAWLNode node : arCancels) {
			String name = ((String) node.getAttributeValue("nodeid"))
					.substring(7);
			ydRoot.removeYawlNode(name);
		}
	}

	private boolean addEdge(BpmnEdge edge, BpmnGraph bpmn, YAWLModel yawl,
			HashMap<String, YAWLEdge> edges) {
		String fromId = edge.getFromId();
		String toId = edge.getToId();
		String[] arParams = new String[] { "", fromId, toId };
		// search the common predecessor of the two nodes
		bpmn.constructEdge(arParams);
		// construct the edge in YAWL
		YAWLDecompositionBPMN thisDecomp = (YAWLDecompositionBPMN) yawl
				.getDecomposition(arParams[0]);
		YAWLEdge ye = thisDecomp.addEdge(arParams[1], arParams[2], edge
				.isDefaultFlag(), edge.getCondition() != null ? edge
				.getCondition() : edge.getMessage(), null);
		if (ye == null) {
			return false;
		}
		setAttributes(ye, BpmnXmlTags.EDGETYPE, new String[] {
				String.valueOf(edge.getType()), edge.getCondition(),
				edge.isDefaultFlag() ? "true" : null, edge.getMessage() });
		edges.put(arParams[1] + "_" + arParams[2], ye);
		return true;
	}

	private void setAttributes(att.grappa.Element elem, String[] arNames,
			String[] arValues) {
		if (arNames.length != arValues.length) {
			return;
		}

		for (int i = 0; i < arNames.length; i++) {
			if (arValues[i] != null) {
				elem.setAttribute(arNames[i], arValues[i]);
			}
		}
	}
}
