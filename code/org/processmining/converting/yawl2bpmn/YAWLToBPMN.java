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

package org.processmining.converting.yawl2bpmn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.bpmn.*;
import org.processmining.framework.models.yawl.*;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.bpmnmining.BpmnResult;
import org.processmining.framework.models.yawl.bpmn.YAWLDecompositionBPMN;
import org.processmining.converting.ConvertingPlugin;

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
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * @version 1.0
 */
public class YAWLToBPMN implements ConvertingPlugin {

	public YAWLToBPMN() {
	}

	public String getName() {
		return "YAWL to BPMN";
	}

	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/~cgunther/dev/prom/";
	}

	public MiningResult convert(ProvidedObject object) {
		YAWLModel providedYAWL = null;
		LogReader log = null;

		for (int i = 0; i < object.getObjects().length; i++) {
			if (providedYAWL == null
					&& object.getObjects()[i] instanceof YAWLModel) {
				providedYAWL = (YAWLModel) object.getObjects()[i];
			}
			if (log == null && object.getObjects()[i] instanceof LogReader) {
				log = (LogReader) object.getObjects()[i];
			}
		}

		if (providedYAWL == null) {
			return null;
		}
		BpmnGraph graph = convert(providedYAWL);
		BpmnResult result = new BpmnResult(null, graph);
		return result;
	}

	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof YAWLModel) {
				return true;
			}
		}
		return false;
	}

	private BpmnGraph convert(YAWLModel model) {
		Collection decompositions = model.getDecompositions();
		Iterator it = decompositions.iterator();
		BpmnProcessModel bpm = new BpmnProcessModel(null);
		while (it.hasNext()) {
			YAWLDecomposition decomposition = (YAWLDecomposition) it.next();
			if (decomposition.isRoot()) {
				addDecomposition(model, bpm, decomposition, null, false);
			}
		}
		return new BpmnGraph(null, bpm);
	}

	private void addDecomposition(YAWLModel model, BpmnProcessModel bpm,
			YAWLDecomposition decomposition, String parentId, boolean isAdhoc) {
		// if a task join or split code has property of or, xor, and
		// then, a gateway will be added, the gateway id change the original id
		// to connect with other nodes.
		HashMap<String, String> inReplaced = new HashMap<String, String>();
		HashMap<String, String> outReplaced = new HashMap<String, String>();

		String pid = parentId;
		if (pid == null) {
			pid = "n";
		}

		Collection<YAWLNode> collNodes = decomposition.getNodes();
		Iterator<YAWLNode> nodes = collNodes.iterator();
		while (nodes.hasNext()) {
			YAWLNode node = nodes.next();
			String id = pid + "_" + node.getId();
			String lane = (String) node
					.getAttributeValue(BpmnXmlTags.BPMN_POOLLANE);
			if (node instanceof YAWLTask) {
				YAWLTask task = (YAWLTask) node;
				String taskType = (String) task
						.getAttributeValue(BpmnXmlTags.BPMN_TASKTYPE);
				// the default value is task
				if (taskType == null) {
					taskType = BpmnXmlTags.BPMN_TASK;
				}
				// a BpmnTask
				BpmnTask bpmnTask = null;
				String strDecomp = task.getDecomposition();
				YAWLDecomposition objDecomp = model.getDecomposition(strDecomp);
				if (strDecomp == null || strDecomp.equals("")
						|| objDecomp == null
						|| objDecomp.getNodes().size() == 0) {
					if (taskType.equals(BpmnXmlTags.BPMN_TASK)) {
						bpmnTask = new BpmnTask(id);
						bpmnTask.setTypeTag(BpmnTaskType.Task);
						// set other attributes of this task
						String strLoopType = (String) task
								.getAttributeValue(BpmnXmlTags.BPMN_PROP_LOOPTYPE);
						bpmnTask.setProperty(BpmnXmlTags.BPMN_PROP_LOOPTYPE,
								strLoopType);
						String strTimingType = (String) task
								.getAttributeValue(BpmnXmlTags.BPMN_PROP_TIMING);
						bpmnTask.setProperty(BpmnXmlTags.BPMN_PROP_TIMING,
								strTimingType);
						String strcompensationactivity = (String) task
								.getAttributeValue(BpmnXmlTags.BPMN_PROP_COMPENSATIONACTIVITY);
						bpmnTask.setProperty(
								BpmnXmlTags.BPMN_PROP_COMPENSATIONACTIVITY,
								strcompensationactivity);
						String strtranaction = (String) task
								.getAttributeValue(BpmnXmlTags.BPMN_PROP_TRANSACTION);
						bpmnTask.setProperty(BpmnXmlTags.BPMN_PROP_TRANSACTION,
								strtranaction);
						bpmnTask.setLane(lane);
					} else if (taskType.equals(BpmnXmlTags.BPMN_GATEWAY)) {
						int splitType = task.getSplitType();
						BpmnGatewayType bgt = BpmnGatewayType.OR;
						if (splitType == YAWLTask.AND) {
							bgt = BpmnGatewayType.AND;
						} else if (splitType == YAWLTask.XOR) {
							bgt = BpmnGatewayType.XOR;
						} else {
							// or, subtype: or, complex, normal
							String subType = (String) task
									.getAttributeValue(BpmnXmlTags.BPMN_SUBTYPE);
							if (subType == null) {
								bgt = null;
							} else if (subType
									.equals(BpmnXmlTags.BPMN_GWT_COMPLEX)) {
								bgt = BpmnGatewayType.Complex;
							} else if (subType.equals(BpmnXmlTags.BPMN_GWT_OR)) {
								bgt = BpmnGatewayType.OR;
							} else if (subType.equals(BpmnXmlTags.BPMN_GWT_AND)) {
								bgt = BpmnGatewayType.AND;
							} else if (subType.equals(BpmnXmlTags.BPMN_GWT_XOR)) {
								bgt = BpmnGatewayType.XOR;
							} else {
								bgt = null;
							}
						}
						BpmnGateway gateway = new BpmnGateway(id);
						gateway.setType(bgt);
						gateway.setpid(parentId);
						bpm.addNode(gateway);
						gateway.setLane(lane);
					} else if (taskType.equals(BpmnEventTriggerType.Cancel
							.toString())
							|| taskType.equals(BpmnEventTriggerType.Exception
									.toString())
							|| taskType.equals(BpmnEventTriggerType.Error
									.toString())) {
						ArrayList<String> lanes = new ArrayList<String>();
						Iterator<YAWLNode> preds = task.getPredecessors()
								.iterator();
						while (preds.hasNext()) {
							YAWLNode pred = preds.next();
							String aLane = (String) pred
									.getAttributeValue(BpmnXmlTags.BPMN_POOLLANE);
							if (!lanes.contains(aLane)) {
								lanes.add(aLane);
							}
						}
						Iterator<YAWLNode> succs = task.getSuccessors()
								.iterator();
						while (succs.hasNext()) {
							YAWLNode succ = succs.next();
							String aLane = (String) succ
									.getAttributeValue(BpmnXmlTags.BPMN_POOLLANE);
							if (!lanes.contains(aLane)) {
								lanes.add(aLane);
							}
						}
						if (lanes.size() == 0 || lanes.size() == 1
								&& lanes.get(0) == null) {
							// converting a yawl model built manually
							BpmnEvent bpmnEvent = new BpmnEvent(id);
							bpmnEvent.setTypeTag(BpmnEventType.Intermediate);
							// trigger type
							bpmnEvent.setTrigger(BpmnEventTriggerType
									.valueOf(taskType));
							bpmnEvent.setpid(parentId);
							bpm.addNode(bpmnEvent);
						} else {
							// converting a yawl model converted from a bpmn
							// model
							// create a bpmn event for each lane and connect
							// them correctly by edges
							int n = 0;
							for (String aLane : lanes) {
								n++;
								String eventId = id + "_lane" + n;
								BpmnEvent bpmnEvent = new BpmnEvent(eventId);
								bpmnEvent
										.setTypeTag(BpmnEventType.Intermediate);
								// trigger type
								bpmnEvent.setTrigger(BpmnEventTriggerType
										.valueOf(taskType));
								bpmnEvent.setpid(parentId);
								bpm.addNode(bpmnEvent);
								bpmnEvent.setLane(aLane);

								preds = task.getPredecessors().iterator();
								while (preds.hasNext()) {
									YAWLNode pred = preds.next();
									String theLane = (String) pred
											.getAttributeValue(BpmnXmlTags.BPMN_POOLLANE);
									if (theLane == null
											&& aLane == null
											|| (theLane != null
													&& aLane != null && theLane
													.equals(aLane))) {
										// add a bpmn edge
										String fromId = pid + "_"
												+ pred.getId();
										BpmnEdge edge = new BpmnEdge(fromId,
												eventId);
										edge.setId(fromId + "_" + eventId);
										edge.setpid(parentId);
										edge.setType(BpmnEdgeType.Flow);
										bpm.addEdge(edge);
									}
								}
								succs = task.getSuccessors().iterator();
								while (succs.hasNext()) {
									YAWLNode succ = succs.next();
									String theLane = (String) succ
											.getAttributeValue(BpmnXmlTags.BPMN_POOLLANE);
									if (theLane == null
											&& aLane == null
											|| (theLane != null
													&& aLane != null && theLane
													.equals(aLane))) {
										// add a bpmn edge
										String toId = pid + "_" + succ.getId();
										BpmnEdge edge = new BpmnEdge(eventId,
												toId);
										edge.setId(eventId + "_" + toId);
										edge.setpid(parentId);
										edge.setType(BpmnEdgeType.Flow);
										bpm.addEdge(edge);
									}
								}
							}
						}
					}
				} else {
					// whether the tasks in the subprocess is adhoc
					boolean bAdhoc = false;
					String strAdhoc = (String) task
							.getAttributeValue(BpmnXmlTags.BPMN_PROP_ADHOC);
					if (strAdhoc != null && strAdhoc.equals("true")) {
						bAdhoc = true;
					}
					bpmnTask = new BpmnSubProcess(id);
					bpmnTask.setTypeTag(BpmnTaskType.SubProcess);
					BpmnSubProcess bsp = (BpmnSubProcess) bpmnTask;
					bsp.setLane(lane);
					// set other attributes of this subprocess
					bsp.setProperty(BpmnXmlTags.BPMN_PROP_ADHOC, strAdhoc);
					String strLoopType = (String) task
							.getAttributeValue(BpmnXmlTags.BPMN_PROP_LOOPTYPE);
					bsp
							.setProperty(BpmnXmlTags.BPMN_PROP_LOOPTYPE,
									strLoopType);
					String strTimingType = (String) task
							.getAttributeValue(BpmnXmlTags.BPMN_PROP_TIMING);
					bsp
							.setProperty(BpmnXmlTags.BPMN_PROP_TIMING,
									strTimingType);
					String strcompensationactivity = (String) task
							.getAttributeValue(BpmnXmlTags.BPMN_PROP_COMPENSATIONACTIVITY);
					bsp.setProperty(BpmnXmlTags.BPMN_PROP_COMPENSATIONACTIVITY,
							strcompensationactivity);
					String strexpanded = (String) task
							.getAttributeValue(BpmnXmlTags.BPMN_PROP_EXPANDED);
					bsp
							.setProperty(BpmnXmlTags.BPMN_PROP_EXPANDED,
									strexpanded);
					String strtranaction = (String) task
							.getAttributeValue(BpmnXmlTags.BPMN_PROP_TRANSACTION);
					bsp.setProperty(BpmnXmlTags.BPMN_PROP_TRANSACTION,
							strtranaction);
					// add decomposition recursively
					addDecomposition(model, ((BpmnSubProcess) bpmnTask)
							.getProcessModel(), objDecomp, id, bAdhoc);
					bsp.buildGraph();
				}
				if (bpmnTask != null) {
					bpmnTask.setpid(parentId);
					// store the property of task name
					String strId = task.getIdentifier();
					if (strId != null && !strId.equals("")) {
						bpmnTask.setName(strId);
					} else {
						bpmnTask.setName(strDecomp);
					}
					// store the bpmntask to BpmnProcessModel
					bpm.addNode(bpmnTask);

					// add necessary gateways for task for subprocess
					if ((taskType.equals(BpmnXmlTags.BPMN_TASK) || taskType
							.equals(BpmnXmlTags.BPMN_SUBPROCESS))
							&& !isAdhoc) {
						int joinType = task.getJoinType();
						if (joinType == 3) {
							// NONE
						}

						BpmnGateway gateway = null;
						String newId = id;
						if (joinType == 4) {
							// OR
							// construct a OR node

							newId += "_IN_OR";
							gateway = new BpmnGateway(newId);
							gateway.setType(BpmnGatewayType.OR);
						} else if (joinType == 5) {
							// AND
							newId += "_IN_AND";
							gateway = new BpmnGateway(newId);
							gateway.setType(BpmnGatewayType.AND);
						} else if (joinType == 6) {
							// XOR
							newId += "_IN_XOR";
							gateway = new BpmnGateway(newId);
							gateway.setType(BpmnGatewayType.XOR);
						}
						if (gateway != null) {
							gateway.setpid(parentId);
							gateway.setLane(lane);
							bpm.addNode(gateway);
							// store the new name for its predcessors,in future,
							// every node connect to the task
							// will change to connect with the gateway.
							inReplaced.put(id, newId);
						}

						// re initial
						gateway = null;

						int splitType = task.getSplitType();
						newId = id;
						if (splitType == 3) {
							// NONE
						}

						if (splitType == 4) {
							// OR
							// construct a OR node
							newId += "_OUT_OR";
							gateway = new BpmnGateway(newId);
							gateway.setType(BpmnGatewayType.OR);
						} else if (splitType == 5) {
							// AND
							newId += "_OUT_AND";
							gateway = new BpmnGateway(newId);
							gateway.setType(BpmnGatewayType.AND);
						} else if (splitType == 6) {
							// XOR
							newId += "_OUT_XOR";
							gateway = new BpmnGateway(newId);
							gateway.setType(BpmnGatewayType.XOR);
						}
						if (gateway != null) {
							gateway.setpid(parentId);
							gateway.setLane(lane);
							bpm.addNode(gateway);
							// store the new name for its successors,in future,
							// every node connect to the task
							// will change to connect with the gateway.
							outReplaced.put(id, newId);
						}
					}
				}
			} else if (node instanceof YAWLCondition) {
				YAWLCondition condition = (YAWLCondition) node;
				String nodeType = (String) condition
						.getAttributeValue(BpmnXmlTags.BPMN_NODETYPE);
				if (nodeType == null) {
					nodeType = BpmnXmlTags.BPMN_ORIGINAL;
				}
				if (nodeType.equals(BpmnXmlTags.BPMN_ORIGINAL)) {
					String eventType = (String) condition
							.getAttributeValue(BpmnXmlTags.BPMN_EVENTTYPE);
					// a BpmnEvent
					BpmnEvent bpmnEvent = new BpmnEvent(id);
					if (eventType == null) {
						int conditionType = 2;
						if (node.getPredecessors().size() == 0) {
							conditionType = 0;
						} else if (node.getSuccessors().size() == 0) {
							conditionType = 1;
						}
						if (conditionType == 0) {
							// Input condition
							bpmnEvent.setTypeTag(BpmnEventType.Start);
						} else if (conditionType == 1) {
							// Output condition
							bpmnEvent.setTypeTag(BpmnEventType.End);
						} else if (conditionType == 2) {
							// Noraml condition
							bpmnEvent.setTypeTag(BpmnEventType.Intermediate);
						}
					} else {
						bpmnEvent.setTypeTag(BpmnEventType.valueOf(eventType));
					}
					String triggerType = (String) condition
							.getAttributeValue(BpmnXmlTags.BPMN_PROP_TRIGGER);
					if (triggerType != null) {
						bpmnEvent.setTrigger(BpmnEventTriggerType
								.valueOf(triggerType));
					}
					bpmnEvent.setpid(parentId);
					bpmnEvent.setLane(lane);
					bpm.addNode(bpmnEvent);
				}
			}
		}

		HashMap<String, BpmnEdge> ytobEdges = new HashMap<String, BpmnEdge>();
		// handle all edges
		nodes = collNodes.iterator();
		while (nodes.hasNext()) {
			YAWLNode from = nodes.next();
			String fromId = pid + "_" + from.getId();
			String oldFromId = fromId;
			// the from node must exist in this bpmn
			if (bpm.getNode(fromId) == null) {
				continue;
			}
			String outRep = outReplaced.get(fromId);
			if (outRep != null) {
				// create an edge between from and out_rep
				BpmnEdge edge = new BpmnEdge(fromId, outRep);
				edge.setId(fromId + "_" + outRep);
				edge.setType(BpmnEdgeType.Flow);
				edge.setpid(parentId);
				bpm.addEdge(edge);
				fromId = outRep;
			}
			// handle all edges about its sucessors
			Iterator<YAWLNode> successors = from.getSuccessors().iterator();
			while (successors.hasNext()) {
				YAWLNode to = successors.next();
				String toId = pid + "_" + to.getId();
				String oldToId = toId;
				// the to node must exist in this bpmn
				if (bpm.getNode(toId) == null) {
					continue;
				}
				String inRep = inReplaced.get(toId);
				if (inRep != null) {
					// create an edge between in_rep and to
					BpmnEdge edge = new BpmnEdge(inRep, toId);
					edge.setId(inRep + "_" + toId);
					edge.setType(BpmnEdgeType.Flow);
					edge.setpid(parentId);
					bpm.addEdge(edge);
					toId = inRep;
				}

				// add the edge between from and to
				BpmnEdge edge = new BpmnEdge(fromId, toId);
				edge.setId(fromId + "_" + toId);
				edge.setpid(parentId);
				edge.setType(BpmnEdgeType.Flow);
				bpm.addEdge(edge);

				// store the info for later usage
				ytobEdges.put(oldFromId + "_" + oldToId, edge);
			}

			// rebuild all the pools and lanes as well as their relations
			String[] pools = (decomposition instanceof YAWLDecompositionBPMN) ? ((YAWLDecompositionBPMN) decomposition)
					.getPools()
					: (new String[0]);
			for (int i = 0; i < pools.length; i++) {
				String pool = pools[i];
				BpmnSwimPool bsp = new BpmnSwimPool("pool" + i);
				bsp.setpid(parentId);
				bsp.setType(BpmnSwimType.Pool);
				bsp.setName(pool);
				bpm.addNode(bsp);
			}
			// lane
			String[] lanes = (decomposition instanceof YAWLDecompositionBPMN) ? ((YAWLDecompositionBPMN) decomposition)
					.getLanes()
					: (new String[0]);
			for (int i = 0; i < lanes.length; i++) {
				String lanePool = lanes[i];
				int idx = lanePool.indexOf('@');
				String lane = lanePool.substring(0, idx);
				String poolId = lanePool.substring(idx + 1, lanePool.length());
				if (poolId.equals("")) {
					poolId = null;
				}
				BpmnSwimLane bsl = new BpmnSwimLane("lane" + i);
				if (poolId == null) {
					bsl.setpid(parentId);
				} else {
					bsl.setpid(poolId);
				}
				bsl.setLane(poolId);
				bsl.setName(lane);
				bsl.setType(BpmnSwimType.Lane);
				bpm.addNode(bsl);
			}
		}

		// handle all reset edges and default edges
		ArrayList<YAWLEdge> alEdges = decomposition.getEdges();
		HashMap<String, BpmnEvent> resetEvents = new HashMap();
		for (YAWLEdge edge : alEdges) {
			String fromId = pid + "_" + edge.getSource().getId();
			String toId = pid + "_" + edge.getDest().getId();
			String edgeType = (String) edge
					.getAttributeValue(BpmnXmlTags.BPMN_EDGETYPE);
			if (!edge.isNormal()) {
				if (edgeType == null
						|| !edgeType.equals(BpmnXmlTags.BPMN_MANUAL)) {
					// create an exception event
					String resetId = fromId + "_reset";
					BpmnEvent event = resetEvents.get(resetId);
					if (event == null) {
						event = new BpmnEvent(resetId);
						event.setpid(parentId);
						event.setTypeTag(BpmnEventType.Intermediate);
						event.setTrigger(BpmnEventTriggerType.Exception);
						bpm.addNode(event);
						// store the reset event
						resetEvents.put(resetId, event);

						// add edge between fromId and resetId
						BpmnEdge anEdge = new BpmnEdge(fromId, resetId);
						anEdge.setId(fromId + "_" + resetId);
						anEdge.setpid(parentId);
						anEdge.setType(BpmnEdgeType.Flow);
						bpm.addEdge(anEdge);
						// set the reset event's lane with its predecessor's
						// lane
						BpmnObject bo = bpm.getNode(fromId);
						event.setLane(bo.getLane());
					}

					String outId = outReplaced.get(fromId);
					String inId = inReplaced.get(toId);
					if (outId == null && inId == null) {
						BpmnEdge changeEdge = bpm.getEdge(fromId + "_" + toId);
						changeEdge.setFromId(resetId);
					} else if (outId != null && inId == null) {
						BpmnEdge changeEdge = bpm.getEdge(outId + "_" + toId);
						changeEdge.setFromId(resetId);
					} else if (outId == null && inId != null) {
						BpmnEdge changeEdge = bpm.getEdge(fromId + "_" + inId);
						changeEdge.setFromId(resetId);
						changeEdge.setToId(toId);
					} else {
						BpmnEdge changeEdge = bpm.getEdge(outId + "_" + inId);
						changeEdge.setFromId(resetId);
						changeEdge.setToId(toId);
					}
				} else {
					// set the reset event's lane
				}
			}
			// need to update YawlEdge funciton of isDeafultEdge , then you can
			// check whether an edge is default edge.
			else if (edge.isDefaultFlow()) {
				String outId = outReplaced.get(fromId);
				String inId = inReplaced.get(toId);
				if (outId == null && inId == null) {
					BpmnEdge changeEdge = bpm.getEdge(fromId + "_" + toId);
					changeEdge.setDefaultFlag(true);
				} else if (outId != null && inId == null) {
					BpmnEdge changeEdge = bpm.getEdge(outId + "_" + toId);
					changeEdge.setDefaultFlag(true);
				} else if (outId == null && inId != null) {
					BpmnEdge changeEdge = bpm.getEdge(fromId + "_" + inId);
					changeEdge.setDefaultFlag(true);
				} else {
					BpmnEdge changeEdge = bpm.getEdge(outId + "_" + inId);
					changeEdge.setDefaultFlag(true);
				}
			} else if (edgeType != null
					&& edgeType.equals(BpmnXmlTags.BPMN_MANUAL)
					&& fromId.equals(toId)) {
				// remove the self-loop edge for an event of
				// CANCEL/ERROR/EXCEPTION
				bpm.removeEdge(fromId + "_" + toId);
			}
		}
		// handle all edges' attributes
		for (YAWLEdge edge : alEdges) {
			String fromId = pid + "_" + edge.getSource().getId();
			String toId = pid + "_" + edge.getDest().getId();
			BpmnEdge bpmnEdge = ytobEdges.get(fromId + "_" + toId);
			if (bpmnEdge == null) {
				continue;
			}
			String strEdgeType = (String) edge
					.getAttributeValue(BpmnXmlTags.BPMN_EDGETYPE);
			if (strEdgeType != null) {
				if (strEdgeType.equals(BpmnEdgeType.Message.toString())) {
					bpmnEdge.setType(BpmnEdgeType.Message);
				}
			}
			String strCondition = (String) edge
					.getAttributeValue(BpmnXmlTags.BPMN_PROP_CONDITION);
			if (strCondition == null) {
				bpmnEdge.setProperty(BpmnXmlTags.BPMN_PROP_CONDITION, edge
						.getPredicate());
			} else {
				bpmnEdge.setProperty(BpmnXmlTags.BPMN_PROP_CONDITION,
						strCondition);
			}
			String strDefault = (String) edge
					.getAttributeValue(BpmnXmlTags.BPMN_PROP_DEFAULT);
			if (strDefault != null && strDefault.equals("true")) {
				bpmnEdge.setProperty(BpmnXmlTags.BPMN_PROP_DEFAULT, strDefault);
			}
			String strMessage = (String) edge
					.getAttributeValue(BpmnXmlTags.BPMN_PROP_MESSAGE);
			bpmnEdge.setProperty(BpmnXmlTags.BPMN_PROP_MESSAGE, strMessage);
		}
	}
}
