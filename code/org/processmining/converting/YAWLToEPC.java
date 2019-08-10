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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.algorithms.ConnectorStructureExtractor;
import org.processmining.framework.models.yawl.YAWLCondition;
import org.processmining.framework.models.yawl.YAWLDecomposition;
import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.yawl.YAWLNode;
import org.processmining.framework.models.yawl.YAWLTask;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;
import org.processmining.framework.models.yawl.YAWLNode;
import java.util.HashSet;

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
public class YAWLToEPC implements ConvertingPlugin {
	private HashMap<String, EPCConnector> pointsOfEntry;
	private HashMap<String, EPCConnector> pointsOfExit;

	// TODO Eric: Can you please fill these mappings while
	// converting the yawl model to an EPC?
	protected HashMap<YAWLTask, EPCFunction> taskActivityMapping;
	protected HashMap<YAWLCondition, EPCConnector> conditionChoiceMapping;
	protected HashMap<YAWLTask, EPCConnector> xortaskChoiceMapping;

	public YAWLToEPC() {
		pointsOfEntry = new HashMap<String, EPCConnector>();
		pointsOfExit = new HashMap<String, EPCConnector>();
		taskActivityMapping = new HashMap<YAWLTask, EPCFunction>();
		conditionChoiceMapping = new HashMap<YAWLCondition, EPCConnector>();
		xortaskChoiceMapping = new HashMap<YAWLTask, EPCConnector>();
	}

	public String getName() {
		return "YAWL to EPC";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:yawl2epc";
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

		ConfigurableEPC epc = convert(providedYAWL);
		epc.Test("YAWLToEPC");

		return new EPCResult(log, epc);
	}

	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof YAWLModel) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a task mapping of the input YAWL tasks to the corresponding EPC
	 * functions in the converted model.
	 * 
	 * @return the mapping of yawl tasks to epc functions
	 */
	public HashMap<YAWLTask, EPCFunction> getYAWLTaskToEPCFunctionMapping() {
		return taskActivityMapping;
	}

	/**
	 * Returns a task mapping of the input YAWL conditions to the corresponding
	 * EPC connectors in the converted model.
	 * 
	 * @return the mapping of yawl conditions to epc connectors
	 */
	public HashMap<YAWLCondition, EPCConnector> getYAWLConditionToEPCConnectorMapping() {
		return conditionChoiceMapping;
	}

	/**
	 * Returns a task mapping of the input YAWL tasks (e.g., XOR tasks) to the
	 * corresponding EPC connectors in the converted model.
	 * 
	 * @return the mapping of yawl tasks to epc connectors
	 */
	public HashMap<YAWLTask, EPCConnector> getYAWLTaskToEPCConnectorMapping() {
		return xortaskChoiceMapping;
	}

	/**
	 * Converts the given YAWL model into an EPC model.
	 * 
	 * @param model
	 *            the YAWL model to convert
	 * @return the resulting EPC model
	 */
	public ConfigurableEPC convert(YAWLModel model) {
		ConfigurableEPC epc = new ConfigurableEPC();
		Collection decompositions = model.getDecompositions();
		Iterator it = decompositions.iterator();
		while (it.hasNext()) {
			YAWLDecomposition decomposition = (YAWLDecomposition) it.next();
			if (decomposition.isRoot()) {
				HashMap ancestors = new HashMap();
				ancestors.put(decomposition.getName(), "/[]");
				addDecomposition(epc, model, "/", decomposition, true,
						ancestors);
				ancestors.remove(decomposition.getName());
			}
		}

		HashMap epcMap = new HashMap();
		epc = ConnectorStructureExtractor.extract(epc, true, false, false,
				false, false, false, false, true, false, false, epcMap);

		// HV: Possibly, mapped object were removed during the previous
		// reduction phase. Remove these mapped objects.
		HashMap<YAWLCondition, EPCConnector> newConditionChoiceMapping = new HashMap<YAWLCondition, EPCConnector>();
		for (YAWLCondition condition : conditionChoiceMapping.keySet()) {
			EPCConnector oldConnector = conditionChoiceMapping.get(condition);
			if (epcMap.keySet().contains(oldConnector.getIdKey())) {
				EPCConnector newConnector = (EPCConnector) epcMap
						.get(oldConnector.getIdKey());
				if (epc.getConnectors().contains(newConnector)) {
					newConditionChoiceMapping.put(condition, newConnector);
				}
			}
		}
		conditionChoiceMapping = newConditionChoiceMapping;
		HashMap<YAWLTask, EPCFunction> newTaskActivityMapping = new HashMap<YAWLTask, EPCFunction>();
		for (YAWLTask task : taskActivityMapping.keySet()) {
			EPCFunction oldFunction = taskActivityMapping.get(task);
			if (epcMap.keySet().contains(oldFunction.getIdKey())) {
				EPCFunction newFunction = (EPCFunction) epcMap.get(oldFunction
						.getIdKey());
				if (epc.getFunctions().contains(newFunction)) {
					newTaskActivityMapping.put(task, newFunction);
				}
			}
		}
		taskActivityMapping = newTaskActivityMapping;
		HashMap<YAWLTask, EPCConnector> newXortaskChoiceMapping = new HashMap<YAWLTask, EPCConnector>();
		for (YAWLTask task : xortaskChoiceMapping.keySet()) {
			EPCConnector oldConnector = xortaskChoiceMapping.get(task);
			if (epcMap.keySet().contains(oldConnector.getIdKey())) {
				EPCConnector newConnector = (EPCConnector) epcMap
						.get(oldConnector.getIdKey());
				if (epc.getConnectors().contains(newConnector)) {
					newXortaskChoiceMapping.put(task, newConnector);
				}
			}
		}
		xortaskChoiceMapping = newXortaskChoiceMapping;

		// Remove any source/sink connector.
		it = epc.getConnectors().iterator();
		while (it.hasNext()) {
			EPCConnector connector = (EPCConnector) it.next();
			if (connector.getPredecessors().isEmpty()
					|| connector.getSuccessors().isEmpty()) {
				epc.delConnector(connector);
				it = epc.getConnectors().iterator();
			}
		}
		return epc;
	}

	private void addDecomposition(ConfigurableEPC epc, YAWLModel model,
			String parentId, YAWLDecomposition decomposition, boolean isRoot,
			HashMap ancestors) {
		Collection objects = decomposition.getNodes();
		Iterator it = objects.iterator();
		// First, the conditions
		while (it.hasNext()) {
			Object object = it.next();
			if (object instanceof YAWLCondition) {
				YAWLCondition condition = (YAWLCondition) object;
				if (condition.getPredecessors().isEmpty()) {
					boolean needStartEvent = true;
					if (condition.getSuccessors().size() == 1) {
						YAWLTask task = (YAWLTask) condition.getSuccessors()
								.iterator().next();
						if (task.getPredecessors().size() == 1) {
							needStartEvent = false;
						}
					}
					EPCConnector splitConnector = new EPCConnector(
							YAWLTask.XOR, epc);
					epc.addConnector(splitConnector);
					if (isRoot && needStartEvent) {
						EPCFunction function = new EPCFunction(null, false, epc);
						epc.addFunction(function);
						EPCEvent event = new EPCEvent("Start", epc);
						epc.addEvent(event);
						epc.addEdge(event, function);
						epc.addEdge(function, splitConnector);
					}
					// Input condition
					pointsOfEntry.put(parentId + "[]", splitConnector);
					// Message.add("put entry " + parentId + "[]",
					// Message.DEBUG);
					pointsOfExit
							.put(parentId + "/"
									+ String.valueOf(condition.getId()),
									splitConnector);
					// Message.add("put exit " + parentId + "/" +
					// String.valueOf(condition.getId()), Message.DEBUG);
				} else if (condition.getSuccessors().isEmpty()) {
					// Output condition
					EPCConnector joinConnector = new EPCConnector(YAWLTask.XOR,
							epc);
					epc.addConnector(joinConnector);
					pointsOfEntry.put(parentId + "/"
							+ String.valueOf(condition.getId()), joinConnector);
					// Message.add("put entry " + parentId + "/" +
					// String.valueOf(condition.getId()), Message.DEBUG);
					pointsOfExit.put(parentId + "[]", joinConnector);
					// Message.add("put exit " + parentId + "[]",
					// Message.DEBUG);
					if (isRoot) {
						EPCEvent event = new EPCEvent("the end", epc);
						epc.addEvent(event);
						epc.addEdge(joinConnector, event);
					}
				} else {
					// Ordinary condition
					EPCConnector joinConnector = new EPCConnector(YAWLTask.XOR,
							epc);
					EPCConnector splitConnector = new EPCConnector(
							YAWLTask.XOR, epc);
					epc.addConnector(joinConnector);
					epc.addConnector(splitConnector);
					epc.addEdge(joinConnector, splitConnector);
					pointsOfEntry.put(parentId + "/"
							+ String.valueOf(condition.getId()), joinConnector);
					// Message.add("put entry " + parentId + "/" +
					// String.valueOf(condition.getId()), Message.DEBUG);
					pointsOfExit
							.put(parentId + "/"
									+ String.valueOf(condition.getId()),
									splitConnector);
					// Message.add("put exit " + parentId + "/" +
					// String.valueOf(condition.getId()), Message.DEBUG);

					// HV: Remember that this condition is mapped onto this
					// split connector.
					conditionChoiceMapping.put(condition, splitConnector);
				}
			}
		}
		// Second, the tasks
		it = objects.iterator();
		while (it.hasNext()) {
			Object object = it.next();
			if (object instanceof YAWLTask) {
				YAWLTask task = (YAWLTask) object;
				int joinType, splitType;
				String taskId = parentId + "/" + String.valueOf(task.getId());
				joinType = task.getJoinType();
				if (joinType == YAWLTask.NONE) {
					joinType = YAWLTask.AND;
				}
				splitType = task.getSplitType();
				if (splitType == YAWLTask.NONE) {
					splitType = YAWLTask.XOR;
				}
				EPCConnector joinConnector = new EPCConnector(joinType, epc);
				epc.addConnector(joinConnector);
				EPCConnector splitConnector = new EPCConnector(splitType, epc);
				epc.addConnector(splitConnector);

				pointsOfEntry.put(taskId, joinConnector);
				// Message.add("put entry " + taskId, Message.DEBUG);
				pointsOfExit.put(taskId, splitConnector);
				// Message.add("put exit " + taskId, Message.DEBUG);

				// Task
				if (model.isComposite(task.getDecomposition())
						&& !ancestors.containsKey(task.getDecomposition())) {
					YAWLDecomposition taskDecomposition = model
							.getDecomposition(task.getDecomposition());
					ancestors.put(task.getDecomposition(), taskId + "[]");
					addDecomposition(epc, model, taskId, taskDecomposition,
							false, ancestors);
					ancestors.remove(task.getDecomposition());
					String decompositionId = taskId + "[]";
					EPCConnector pointOfEntry = pointsOfEntry
							.get(decompositionId);
					// Message.add("get entry " + decompositionId +
					// (pointOfEntry == null ? " failed" : " succeeded"),
					// Message.DEBUG);
					EPCConnector pointOfExit = pointsOfExit
							.get(decompositionId);
					// Message.add("get exit " + decompositionId + (pointOfExit
					// == null ? " failed" : " succeeded"), Message.DEBUG);
					epc.addEdge(joinConnector, pointOfEntry);
					epc.addEdge(pointOfExit, splitConnector);

					// HV: Remember that this task is mapped onto this
					// splitConnector.
					// HV: Composite task is no tmapped onto a single function.
					xortaskChoiceMapping.put(task, splitConnector);
				} else {
					EPCEvent event = new EPCEvent("status change to "
							+ task.getIdentifier(), epc);
					EPCFunction function = new EPCFunction(task.getLogEvent(),
							epc);
					function.setIdentifier(task.getIdentifier());

					epc.addEvent(event);
					epc.addFunction(function);

					epc.addEdge(joinConnector, event);
					epc.addEdge(event, function);
					epc.addEdge(function, splitConnector);

					// HV: Remember that this task is mapped onto this function
					// and this splitConnector.
					taskActivityMapping.put(task, function);
					xortaskChoiceMapping.put(task, splitConnector);
				}
			}
		}
		// Third, the edges
		it = objects.iterator();
		while (it.hasNext()) {
			Object object = it.next();
			if (object instanceof YAWLTask) {
				YAWLTask task = (YAWLTask) object;
				EPCConnector pointOfEntry, pointOfExit;
				String taskId = parentId + "/" + String.valueOf(task.getId());

				pointOfEntry = pointsOfEntry.get(taskId);
				// Message.add("get entry " + taskId + (pointOfEntry == null ?
				// " failed" : " succeeded"), Message.DEBUG);
				Iterator it2 = task.getPredecessors().iterator();
				while (it2.hasNext()) {
					Object object2 = it2.next();
					if (decomposition.hasNormalEdges((YAWLNode) object2,
							(YAWLNode) object)) {
						String object2Id;
						if (object2 instanceof YAWLTask) {
							YAWLTask task2 = (YAWLTask) object2;
							object2Id = parentId + "/"
									+ String.valueOf(task2.getId());
						} else {
							YAWLCondition condition2 = (YAWLCondition) object2;
							object2Id = parentId + "/"
									+ String.valueOf(condition2.getId());
						}
						pointOfExit = pointsOfExit.get(object2Id);
						// Message.add("get exit " + object2Id + (pointOfExit ==
						// null ? " failed" : " succeeded"), Message.DEBUG);
						epc.addEdge(pointOfExit, pointOfEntry);
						// Message.add(pointOfExit.toString() + " -> " +
						// pointOfEntry.toString());
					}
				}

				pointOfExit = pointsOfExit.get(taskId);
				// Message.add("get exit " + taskId + (pointOfExit == null ?
				// " failed" : " succeeded"), Message.DEBUG);
				it2 = task.getSuccessors().iterator();
				while (it2.hasNext()) {
					Object object2 = it2.next();
					if (decomposition.hasNormalEdges((YAWLNode) object,
							(YAWLNode) object2)) {
						String object2Id;
						if (object2 instanceof YAWLTask) {
							YAWLTask task2 = (YAWLTask) object2;
							object2Id = parentId + "/"
									+ String.valueOf(task2.getId());
						} else {
							YAWLCondition condition2 = (YAWLCondition) object2;
							object2Id = parentId + "/"
									+ String.valueOf(condition2.getId());
						}
						pointOfEntry = pointsOfEntry.get(object2Id);
						// Message.add("get entry " + object2Id + (pointOfEntry
						// == null ? " failed" : " succeeded"), Message.DEBUG);
						epc.addEdge(pointOfExit, pointOfEntry);
						// Message.add(pointOfExit.toString() + " -> " +
						// pointOfEntry.toString());
					}
				}
			}
		}
	}
}
