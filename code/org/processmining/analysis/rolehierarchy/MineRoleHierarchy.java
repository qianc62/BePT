package org.processmining.analysis.rolehierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.Func;
import org.processmining.mining.Miner;
import org.processmining.mining.NoMiningSettings;

public class MineRoleHierarchy {
	@Miner(name = "Role Hierarchy Miner", settings = NoMiningSettings.class)
	public static JPanel mine(LogReader inputLog) throws Exception {
		return new RoleHierarchyResult(inputLog, mineRoleHierarchy(inputLog, 0,
				0));
	}

	public static RoleHierarchy mineRoleHierarchy(LogReader log,
			int absoluteThreshold, double relativeThreshold) throws Exception {
		Map<String, OntologyModel> taskOntologies = new HashMap<String, OntologyModel>();
		OntologyCollection ontologies = log.getLogSummary().getOntologies();
		Map<String, TaskSet> originator2task = new HashMap<String, TaskSet>();
		Map<String, Set<String>> visibleTasks = new HashMap<String, Set<String>>();

		if (!ontologies.getOntologies().isEmpty()) { // skip reading the whole
			// log if we know we're
			// not going to find any
			// annotations
			for (ProcessInstance pi : log.getInstances()) {
				AuditTrailEntryList ates = pi.getAuditTrailEntryList();

				for (int index = 0; index < ates.size(); index++) {
					AuditTrailEntry ate = ates.get(index);

					for (String uri : ate.getElementModelReferences()) {
						ConceptModel concept = ontologies
								.findConceptByUriInLog(uri);

						if (concept != null) {
							OntologyModel taskOntology = concept.getOntology();
							taskOntologies.put(taskOntology.getName(),
									taskOntology);
						}
					}

					if (ate.getOriginator() != null
							&& ate.getOriginator().length() > 0) {
						String originatorName = ate.getOriginator();

						if (!originator2task.containsKey(originatorName)) {
							originator2task.put(originatorName, new TaskSet());
							visibleTasks.put(originatorName,
									new HashSet<String>());
						}
						for (String uri : ate.getElementModelReferences()) {
							ConceptModel concept = ontologies
									.findConceptByUriInLog(uri);
							if (concept != null) {
								originator2task.get(originatorName).add(
										concept.getName());
								visibleTasks.get(originatorName).add(
										concept.getName());

								for (ConceptModel superConcept : concept
										.getOntology()
										.getSuperConcepts(concept)) {
									originator2task.get(originatorName).add(
											superConcept.getName());
								}
							}
						}
					}
				}
			}
		}

		ModelGraph taskGraph;
		NodeLabelFormatter nodeLabelFormatter;

		if (!taskOntologies.isEmpty()) {
			OntologyModel selectedTaskOntology;

			if (taskOntologies.size() > 1) {
				OntologyModel[] possibleOntologies = taskOntologies.values()
						.toArray(new OntologyModel[0]);
				selectedTaskOntology = (OntologyModel) JOptionPane
						.showInputDialog(
								MainUI.getInstance(),
								"Please select the task ontology on which you want to base the role hierarchy:",
								"Multiple task ontologies found",
								JOptionPane.QUESTION_MESSAGE, null,
								possibleOntologies, possibleOntologies[0]);
				if (selectedTaskOntology == null) {
					// user cancelled
					return null;
				}
			} else {
				selectedTaskOntology = taskOntologies.values().iterator()
						.next();
			}
			taskGraph = selectedTaskOntology.toModelGraph();
			nodeLabelFormatter = new NodeLabelFormatter() {
				public String getLabel(Set<String> nodeNames,
						boolean useShortLabels) {
					List<String> conceptNames = Func.map(nodeNames,
							new Func.Fun1<String, String>() {
								public String apply(String uri) {
									return OntologyModel.getConceptPart(uri);
								}
							});
					if (useShortLabels) {
						return Func.joinAtMost(
								RoleHierarchy.MIN_NUMBER_LABELS_PER_NODE,
								conceptNames, "", "_et_al");
					} else {
						return Func.joinAtMost(
								RoleHierarchy.MAX_NUMBER_LABELS_PER_NODE,
								conceptNames, "\\n", "... and ", " more ...");
					}
				}
			};
		} else {
			Map<String, String> task2node = new HashMap<String, String>();
			final Map<String, String> node2task = new HashMap<String, String>();
			int n = 0;

			// no task ontology found, so we use the 'default task ontology':
			// simply a single node for every task
			// and we use the originator by task matrix to build the
			// originator2task map

			taskGraph = new ModelGraph("Tasks");
			for (String taskName : log.getLogSummary().getModelElements()) {
				ModelGraphVertex node = taskGraph
						.addVertex(new ModelGraphVertex(taskGraph));
				String nodeName = "node" + n;

				node.setIdentifier(nodeName);
				node.setDotAttribute("label", taskName);
				task2node.put(taskName, nodeName);
				node2task.put(nodeName, taskName);
				n++;
			}

			originator2task.clear();
			visibleTasks.clear();
			for (String originator : log.getLogSummary().getOriginators()) {
				Map<LogEvent, Integer> events = log.getLogSummary()
						.getEventsForOriginator(originator);

				if (events != null) {
					for (Map.Entry<LogEvent, Integer> event : events.entrySet()) {
						String taskName = event.getKey().getModelElementName();
						String nodeName = task2node.get(taskName);

						if (nodeName != null) {
							if (!originator2task.containsKey(originator)) {
								originator2task.put(originator, new TaskSet());
								visibleTasks.put(originator,
										new HashSet<String>());
							}
							originator2task.get(originator).add(nodeName,
									event.getValue());
							visibleTasks.get(originator).add(nodeName);
						}
					}
				}
			}

			nodeLabelFormatter = new NodeLabelFormatter() {
				public String getLabel(Set<String> nodeNames,
						boolean useShortLabels) {
					List<String> conceptNames = Func.map(nodeNames,
							new Func.Fun1<String, String>() {
								public String apply(String node) {
									return node2task.get(node);
								}
							});
					if (useShortLabels) {
						return Func.joinAtMost(
								RoleHierarchy.MIN_NUMBER_LABELS_PER_NODE,
								conceptNames, "", "_et_al");
					} else {
						return Func.joinAtMost(
								RoleHierarchy.MAX_NUMBER_LABELS_PER_NODE,
								conceptNames, "\\n", "... and ", " more ...");
					}
				}
			};
		}

		RoleHierarchy hierarchy = new RoleHierarchy(taskGraph.getName(),
				visibleTasks, nodeLabelFormatter);

		mineNodeSet(taskGraph.getVerticeList(), originator2task, hierarchy,
				absoluteThreshold, relativeThreshold);
		return hierarchy;
	}

	private static void mineNodeSet(List<ModelGraphVertex> nodes,
			Map<String, TaskSet> originator2task, RoleHierarchy hierarchy,
			int absoluteThreshold, double relativeThreshold) {
		Map<String, TaskSet> localOTMatrix = new HashMap<String, TaskSet>();

		// Step 1: construct the originator by task matrix for the given nodes
		// only
		for (Map.Entry<String, TaskSet> item : originator2task.entrySet()) {
			String originator = item.getKey();
			TaskSet tasks = item.getValue();

			for (ModelGraphVertex node : nodes) {
				if (tasks.contains(node.getIdentifier())
						&& satisfiesThresholds(tasks, node.getIdentifier(),
								absoluteThreshold, relativeThreshold)) {
					if (!localOTMatrix.containsKey(originator)) {
						localOTMatrix.put(originator, new TaskSet());
					}
					localOTMatrix.get(originator).add(node.getIdentifier(),
							tasks.getFrequency(node.getIdentifier()));
				}
			}
		}

		// Step 2: add it to the role hierarchy we're building up
		for (Map.Entry<String, TaskSet> item : localOTMatrix.entrySet()) {
			hierarchy.add(item.getValue(), item.getKey());
		}
	}

	private static boolean satisfiesThresholds(TaskSet tasks, String taskName,
			int absoluteThreshold, double relativeThreshold) {
		int freq = tasks.getFrequency(taskName);

		if (freq < absoluteThreshold) {
			return false;
		}
		if (relativeThreshold > 0
				&& freq < tasks.getTotalFrequency()
						* (relativeThreshold / 100.0)) {
			return false;
		}
		return true;
	}
}

class TaskSet {

	private Map<String, Integer> tasks;

	public TaskSet() {
		tasks = new HashMap<String, Integer>();
	}

	public void add(String name, int freq) {
		Integer oldFreq = tasks.get(name);

		if (oldFreq == null) {
			tasks.put(name, freq);
		} else {
			tasks.put(name, oldFreq + freq);
		}
	}

	public void add(String name) {
		add(name, 1);
	}

	public boolean contains(String name) {
		return tasks.containsKey(name);
	}

	public Set<String> getTasks() {
		return tasks.keySet();
	}

	public Set<Map.Entry<String, Integer>> entrySet() {
		return tasks.entrySet();
	}

	public int getFrequency(String name) {
		Integer result = tasks.get(name);
		return result == null ? 0 : result;
	}

	public int getTotalFrequency() {
		int sum = 0;

		for (Integer freq : tasks.values()) {
			sum += freq;
		}
		return sum;
	}
}
