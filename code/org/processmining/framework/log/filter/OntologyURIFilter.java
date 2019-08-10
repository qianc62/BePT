package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.ui.Message;
import org.w3c.dom.Node;

public class OntologyURIFilter extends LogFilter {

	private Map<String, String> mapping;

	public OntologyURIFilter() {
		super(LogFilter.MODERATE, "Ontology URI Renaming Filter");
		mapping = new TreeMap<String, String>();
	}

	public OntologyURIFilter(Map<String, String> mapping) {
		this();
		this.mapping = new TreeMap<String, String>(mapping);
	}

	public Map<String, String> getMapping() {
		return mapping;
	}

	protected boolean doFiltering(ProcessInstance instance) {
		// TODO also translate WorkflowLog (+data), Source (+data), Process
		// (+data)

		instance.setModelReferences(translate(instance.getModelReferences()));
		translate(instance.getDataAttributes());

		try {
			AuditTrailEntryList ateList = instance.getAuditTrailEntryList();

			for (int i = 0; i < ateList.size(); i++) {
				AuditTrailEntry ate = ateList.get(i);

				translate(ate.getDataAttributes());
				ate.setElementModelReferences(translate(ate
						.getElementModelReferences()));
				ate.setTypeModelReferences(translate(ate
						.getTypeModelReferences()));
				ate.setOriginatorModelReferences(translate(ate
						.getOriginatorModelReferences()));

				ateList.replace(ate, i);
			}
		} catch (IOException e) {
			Message.add(e.getMessage(), Message.ERROR);
			return false;
		}
		return true;
	}

	private void translate(DataSection dataAttributes) {
		for (String name : dataAttributes.keySet()) {
			dataAttributes.setModelReferences(name, translate(dataAttributes
					.getModelReferences(name)));
		}
	}

	private List<String> translate(List<String> modelReferences) {
		List<String> result = new ArrayList<String>(modelReferences.size());

		for (String uri : modelReferences) {
			String ontology = OntologyModel.getOntologyPart(uri);
			String concept = OntologyModel.getConceptPart(uri);
			String translated = mapping.get(ontology);

			if (translated == null) {
				translated = ontology;
			}
			if (translated.length() > 0) {
				result.add(translated + OntologyModel.ONTOLOGY_SEPARATOR
						+ concept);
			}
		}
		return result;
	}

	protected String getHelpForThisLogFilter() {
		return "Renames ontology URIs in the log file. "
				+ "This filter can be used to rename URIs pointing to (possibly non-existing) http URLs "
				+ "to local files for example."
				+ "For more information, please see <b>http://prom.win.tue.nl/research/wiki/online/semanticprocessmining</b>.";
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		for (String uri : summary.getConceptsAndInstances().keySet()) {
			String ontologyPart = OntologyModel.getOntologyPart(uri);

			if (!mapping.containsKey(ontologyPart)) {
				mapping.put(ontologyPart, ontologyPart);
			}
		}
		return new OntologyURIFilterParamDialog(summary, this);
	}

	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		mapping = new TreeMap<String, String>();
		for (int i = 0; i < logFilterSpecifcNode.getChildNodes().getLength(); i++) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);

			if (n.getNodeName().equals("map")) {
				String from = n.getAttributes().getNamedItem("from")
						.getNodeValue();
				String to = n.getAttributes().getNamedItem("to").getNodeValue();

				mapping.put(from, to);
			}
		}
	}

	protected boolean thisFilterChangesLog() {
		return true;
	}

	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		for (Map.Entry<String, String> item : mapping.entrySet()) {
			output.write("<map from=\"" + item.getKey() + "\" to=\""
					+ item.getValue() + "\" />\n");
		}
	}

}
