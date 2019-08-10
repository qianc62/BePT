package org.processmining.analysis.performance.executiontimes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.analysis.hierarchicaldatavisualization.AbstractHierarchicalData;
import org.processmining.analysis.hierarchicaldatavisualization.HierarchicalDataElement;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyModel;

public class HierarchicalExecutionTimesInOntologies extends
		AbstractHierarchicalData {

	private LogReader log;
	private ArrayList<HierarchicalDataElement> dataElements;
	private Map<String, ModelGraphVertex> taskMapping;
	private Map<String, ModelGraphVertex> origMapping;
	private Map<String, ModelGraphVertex> conceptMapping;
	private final boolean useTaskOntologies;
	private final boolean useOriginatorOntologies;

	public HierarchicalExecutionTimesInOntologies(LogReader log,
			ExecutionTimes executionTimes, boolean useTaskOntologies,
			boolean useOriginatorOntologies) {
		this.log = log;
		this.useTaskOntologies = useTaskOntologies;
		this.useOriginatorOntologies = useOriginatorOntologies;
		this.dataElements = new ArrayList<HierarchicalDataElement>();

		taskMapping = makeDefaultModel("Tasks", "Task", executionTimes
				.getTasks());
		origMapping = makeDefaultModel("Originators", "Originator",
				executionTimes.getOriginators());
		conceptMapping = useTaskOntologies || useOriginatorOntologies ? makeOntologyModels(log
				.getLogSummary().getOntologies())
				: null;

		for (ExecutionTimeMeasurement measurement : executionTimes
				.getRawMeasurements()) {
			ModelGraphVertex taskVertex = useTaskOntologies ? conceptMapping
					.get(measurement.getTask()) : taskMapping.get(measurement
					.getTask());
			ModelGraphVertex origVertex = useOriginatorOntologies ? conceptMapping
					.get(measurement.getOriginator())
					: origMapping.get(measurement.getOriginator());

			dataElements.add(new DataElement(taskVertex, origVertex,
					measurement.getValue()));
		}
	}

	public List<ModelGraph> graphsToExclude() {
		List<ModelGraph> exclude = new ArrayList<ModelGraph>();

		if (useTaskOntologies && origMapping.size() > 0) {
			exclude.add((ModelGraph) origMapping.values().iterator().next()
					.getGraph());
		}
		if (useOriginatorOntologies && taskMapping.size() > 0) {
			exclude.add((ModelGraph) taskMapping.values().iterator().next()
					.getGraph());
		}
		return exclude;
	}

	public Iterator<HierarchicalDataElement> iterator() {
		return dataElements.iterator();
	}

	public String getPluralHierarchyName() {
		return (this.useOriginatorOntologies || this.useTaskOntologies ? "Ontologies"
				: "Graphs");
	}

	public String getSingularHierarchyName() {
		return (this.useOriginatorOntologies || this.useTaskOntologies ? "Ontology"
				: "Graph");
	}

	@Override
	public String formatVertexLabel(String label) {
		if (useTaskOntologies || useOriginatorOntologies) {
			ConceptModel concept = log.getLogSummary().getOntologies()
					.findConceptByUriInOntology(label);

			if (concept != null) {
				label = label
						.replace(concept.getName(), concept.getShortName());
			}
		}
		return label;
	}

	@Override
	public String getGraphName(String id) {
		return OntologyModel.getConceptPart(id);
	}
}

class DataElement implements HierarchicalDataElement {

	private final double value;
	private Set<ModelGraphVertex> vertices;

	public DataElement(ModelGraphVertex task, ModelGraphVertex originator,
			double value) {
		this.vertices = new HashSet<ModelGraphVertex>();
		this.value = value;

		this.vertices.add(task);
		this.vertices.add(originator);
	}

	public Set<ModelGraphVertex> getNodes() {
		return vertices;
	}

	public double getValue() {
		return value;
	}
}
