package org.processmining.analysis.hierarchicaldatavisualization;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.framework.models.DotFormatter;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.models.ontology.OntologyModel;

abstract public class AbstractHierarchicalData implements HierarchicalData {

	private List<String> availableFormats;
	private NumberFormat floatFormatter;
	private DecimalFormat twoDigitIntFormatter;

	public AbstractHierarchicalData() {
		this.availableFormats = new ArrayList<String>();
		this.availableFormats.add("General time (H:M:S)");
		this.availableFormats.add("Seconds");
		this.availableFormats.add("Minutes");
		this.availableFormats.add("Hours");
		this.availableFormats.add("Days");
		this.availableFormats.add("Weeks");
		this.availableFormats.add("Months");
		this.availableFormats.add("Years");

		this.floatFormatter = NumberFormat.getInstance();
		this.floatFormatter.setMaximumFractionDigits(2);
		this.twoDigitIntFormatter = new DecimalFormat("00");
	}

	public String getGraphName(String id) {
		return id;
	}

	public List<ModelGraph> graphsToExclude() {
		return null;
	}

	public String formatVertexLabel(String label) {
		return label;
	}

	protected Map<String, ModelGraphVertex> makeOntologyModels(
			OntologyCollection ontologies) {
		Map<String, ModelGraphVertex> mapping = new HashMap<String, ModelGraphVertex>();

		for (OntologyModel ontology : ontologies.getOntologies()) {
			ModelGraph model = ontology.toModelGraph();

			for (ModelGraphVertex vertex : model.getVerticeList()) {
				String uri = ontology.findConcept(
						OntologyModel.getConceptPart(vertex.getIdentifier()))
						.getName();
				mapping.put(uri, vertex);
			}
		}
		return mapping;
	}

	protected Map<String, ModelGraphVertex> makeDefaultModel(String graphName,
			String rootName, List<String> elements) {
		ModelGraph graph = new ModelGraph(graphName);
		List<ModelGraphVertex> vertices = new ArrayList<ModelGraphVertex>();
		ModelGraphVertex root = new ModelGraphVertex(graph);
		Map<String, ModelGraphVertex> mapping = new HashMap<String, ModelGraphVertex>();

		root.setIdentifier(rootName);
		graph.addVertex(root);

		for (String element : elements) {
			ModelGraphVertex vertex = new ModelGraphVertex(graph);

			graph.addEdge(new ModelGraphEdge(root, vertex));

			vertex.setIdentifier(element);
			graph.addVertex(vertex);
			vertices.add(vertex);
			mapping.put(element, vertex);
		}
		return mapping;
	}

	public DotFormatter getDotFormatter() {
		return new DotFormatter() {
			public Map<String, String> dotFormatHeader(ModelGraph graph) {
				Map<String, String> result = new HashMap<String, String>();
				result.put("rankdir", "TB");
				result.put("size", "6,10");
				result.put("fontname", "Arial");
				result.put("fontsize", "11");
				result.put("graphname", getGraphName(graph.getIdentifier()));
				return result;
			}

			public Map<String, String> dotFormatEdge(ModelGraph graph,
					ModelGraphEdge edge) {
				boolean isEdgeToOntologyNode = false; // TODO
				// edge.getSource().inDegree()
				// == 0;

				Map<String, String> result = new HashMap<String, String>();
				result.put("style", "filled");
				result.put("color", isEdgeToOntologyNode ? "gray" : "black");
				result.put("arrowtail", isEdgeToOntologyNode ? "none"
						: "normal");
				result.put("arrowhead", "none");
				result.put("label", "");
				return result;
			}

			public Map<String, String> dotFormatVertex(ModelGraph graph,
					ModelGraphVertex vertex) {
				String label = vertex.getIdentifier();
				boolean isOntologyNode = false; // TODO vertex.inDegree() == 0;

				label = formatVertexLabel(label);

				Map<String, String> result = new HashMap<String, String>();
				result.put("style", "filled");
				result.put("color", isOntologyNode ? "white" : "antiquewhite");
				result.put("fontname", "Arial");
				result.put("fontsize", "11");
				result.put("shape", isOntologyNode ? "rectangle" : "ellipse");
				result.put("label", label);
				if (isOntologyNode) {
					result.put("do_not_modify_color", "true");
				}
				return result;
			}
		};
	}

	public String formatNumber(int format, double value) {
		switch (format) {
		case 0: // general time
			return ((int) (value / (60 * 60))) + ":"
					+ twoDigitIntFormatter.format((int) ((value / 60) % 60))
					+ ":" + twoDigitIntFormatter.format((int) value % 60);
		case 1: // seconds
			return floatFormatter.format(value);
		case 2: // minutes
			return floatFormatter.format(value / 60);
		case 3: // hours
			return floatFormatter.format(value / (60 * 60));
		case 4: // days
			return floatFormatter.format(value / (60 * 60 * 24));
		case 5: // weeks
			return floatFormatter.format(value / (60 * 60 * 24 * 7));
		case 6: // months
			return floatFormatter.format(value / (60 * 60 * 24 * 365.0 / 12.0));
		case 7: // years
			return floatFormatter.format(value / (60 * 60 * 24 * 365));
		default:
			assert (false);
			return "Invalid format";
		}
	}

	public List<String> getAvailableNumberFormatNames() {
		return availableFormats;
	}
}
