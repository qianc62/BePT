package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.ui.Message;
import org.w3c.dom.Node;
import org.wsml.reasoner.api.InternalReasonerException;
import org.wsml.reasoner.api.inconsistency.InconsistencyException;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.wsml.ParserException;

public class OntologyAbstractionFilter extends LogFilter {

	private List<ConceptSelection> conceptPriority = null;
	private boolean usePriority = false;
	private List<List<String>> selection = null;
	private boolean replaceInstancesElement = false;
	private boolean replaceInstancesType = false;
	private boolean replaceInstancesOriginator = false;
	private boolean removePI = false;
	private boolean removeWfme = false;
	private boolean removeType = false;
	private boolean removeDataAttr = false;
	private boolean removeOriginator = false;

	private Map<String, OntologyModel> ontologies = null;

	// constants for XML export / import
	private final static String REPLACE_ELEMENT_NODE = "replaceinstanceselement";
	private final static String REPLACE_TYPE_NODE = "replaceinstancestype";
	private final static String REPLACE_ORIGINATOR_NODE = "replaceinstancesoriginator";
	private final static String REMOVE_PI_NODE = "removepi";
	private final static String REMOVE_WFME_NODE = "removewfme";
	private final static String REMOVE_TYPE_NODE = "removetype";
	private final static String REMOVE_DATA_ATTR_NODE = "removedataattr";
	private final static String REMOVE_ORIGINATOR_NODE = "removeoriginator";
	private final static String USE_PRIORITY_NODE = "usepriority";
	private final static String VALUE_ATTRIBUTE = "value";
	private final static String TRUE = "true";
	private final static String FALSE = "false";
	private final static String SELECTION_NODE = "selection";
	private final static String PATH_NODE = "path";
	private final static String ITEM_NODE = "item";
	private final static String CONCEPTS_NODE = "concepts";
	private final static String CONCEPT_NODE = "concept";
	private final static String SELECTED_ATTRIBUTE = "selected";

	public OntologyAbstractionFilter() {
		super(LogFilter.MODERATE, "Ontology Abstraction Filter");
	}

	public OntologyAbstractionFilter(List<List<String>> selection,
			List<ConceptSelection> conceptPriority, boolean usePriority,
			boolean replaceInstancesElement, boolean replaceInstancesType,
			boolean replaceInstancesOriginator, boolean removePI,
			boolean removeWfme, boolean removeType, boolean removeDataAttr,
			boolean removeOriginator) {
		this();
		this.selection = selection;
		this.conceptPriority = conceptPriority;
		this.usePriority = usePriority;
		this.replaceInstancesElement = replaceInstancesElement;
		this.replaceInstancesType = replaceInstancesType;
		this.replaceInstancesOriginator = replaceInstancesOriginator;
		this.removePI = removePI;
		this.removeWfme = removeWfme;
		this.removeType = removeType;
		this.removeDataAttr = removeDataAttr;
		this.removeOriginator = removeOriginator;
	}

	public List<ConceptSelection> getConceptPriority() {
		return conceptPriority;
	}

	public boolean getReplaceInstancesElement() {
		return replaceInstancesElement;
	}

	public boolean getReplaceInstancesType() {
		return replaceInstancesType;
	}

	public boolean getReplaceInstancesOriginator() {
		return replaceInstancesOriginator;
	}

	public List<List<String>> getSelection() {
		return selection;
	}

	public boolean getUsePriority() {
		return usePriority;
	}

	public boolean getRemovePI() {
		return removePI;
	}

	public boolean getRemoveWfme() {
		return removeWfme;
	}

	public boolean getRemoveType() {
		return removeType;
	}

	public boolean getRemoveDataAttr() {
		return removeDataAttr;
	}

	public boolean getRemoveOriginator() {
		return removeOriginator;
	}

	protected boolean doFiltering(ProcessInstance instance) {
		instance.setModelReferences(getUris(replace(instance
				.getModelReferences())));

		if (removePI && instance.getModelReferences().isEmpty()) {
			return false;
		}

		replace(instance.getDataAttributes());

		try {
			AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
			int i = 0;

			while (i < ateList.size()) {
				AuditTrailEntry ate = ateList.get(i);
				Map<String, String> elementRefs = replace(ate
						.getElementModelReferences());
				Map<String, String> typeRefs = replace(ate
						.getTypeModelReferences());
				Map<String, String> originatorRefs = replace(ate
						.getOriginatorModelReferences());

				boolean shouldRemove = false;
				shouldRemove = shouldRemove
						|| ((removeWfme || replaceInstancesElement) && elementRefs
								.isEmpty());
				shouldRemove = shouldRemove
						|| ((removeType || replaceInstancesType) && typeRefs
								.isEmpty());

				if (shouldRemove) {
					ateList.remove(i);
					continue;
				}

				ate.setElementModelReferences(getUris(elementRefs));
				ate.setTypeModelReferences(getUris(typeRefs));
				ate.setOriginatorModelReferences(getUris(originatorRefs));

				if (replaceInstancesElement) {
					assert (!elementRefs.isEmpty());
					ate.setElement(getReplacementString(elementRefs.values()));
				}
				if (replaceInstancesType) {
					assert (!typeRefs.isEmpty());
					ate.setType(getReplacementString(typeRefs.values()));
				}
				if (replaceInstancesOriginator) {
					if (originatorRefs.isEmpty()) {
						ate.setOriginator(null);
					} else {
						ate.setOriginator(getReplacementString(originatorRefs
								.values()));
					}
				}

				if (removeOriginator && originatorRefs.isEmpty()) {
					ate.setOriginator(null);
				}

				replace(ate.getDataAttributes());

				ateList.replace(ate, i);
				i++;
			}
		} catch (IOException e) {
			Message.add(e.getMessage(), Message.ERROR);
			return false;
		}
		return !instance.isEmpty();
	}

	private String getReplacementString(Collection<String> modelReferences) {
		SortedSet<String> refs = new TreeSet<String>(modelReferences);
		StringBuffer result = new StringBuffer();

		for (String ref : refs) {
			result.append(ref);
			result.append('_');
		}
		return result.substring(0, result.length() - 1);
	}

	private List<String> getUris(Map<String, String> replacements) {
		return new ArrayList<String>(replacements.keySet());
	}

	private OntologyModel getOntology(String uriInLog) {
		if (ontologies == null) {
			ontologies = new TreeMap<String, OntologyModel>();
		}
		String ontologyUri = OntologyModel.getOntologyPart(uriInLog);
		OntologyModel model = ontologies.get(ontologyUri);

		if (model == null) {
			try {
				model = new OntologyModel(ontologyUri,
						new HashMap<String, Set<String>>(0));
				ontologies.put(ontologyUri, model);
			} catch (MalformedURLException e) {
				Message.add(ontologyUri + ": malformed url: " + e.getMessage(),
						Message.ERROR);
			} catch (IOException e) {
				Message.add(ontologyUri + ": could not read ontology: "
						+ e.getMessage(), Message.ERROR);
			} catch (ParserException e) {
				Message.add(ontologyUri + ": could not parse ontology: "
						+ e.getMessage(), Message.ERROR);
			} catch (InvalidModelException e) {
				Message.add(ontologyUri + ": invalid model: " + e.getMessage(),
						Message.ERROR);
			} catch (InconsistencyException e) {
				Message.add(ontologyUri + ": inconsistency error: "
						+ e.getMessage(), Message.ERROR);
			} catch (InternalReasonerException e) {
				Message.add(ontologyUri
						+ ": internal ontology reasoner error: "
						+ e.getMessage(), Message.ERROR);
			} catch (org.processmining.framework.models.ontology.InvalidModelException e) {
				Message.add(ontologyUri + ": invalid model: " + e.getMessage(),
						Message.ERROR);
			}
		}
		return model;
	}

	private Map<String, String> replace(List<String> modelReferences) {
		Map<String, ConceptModel> newConcepts = expand(modelReferences);

		if (usePriority && !newConcepts.isEmpty()) {
			// only keep the highest priority concept
			Map.Entry<String, ConceptModel> highest = null;
			int priority = conceptPriority.size();

			for (Map.Entry<String, ConceptModel> item : newConcepts.entrySet()) {
				int index = conceptPriority.indexOf(new ConceptSelection(item
						.getValue().getName(), true));
				if (index < priority) {
					priority = index;
					highest = item;
				}
			}
			assert highest != null;
			newConcepts.clear();
			newConcepts.put(highest.getKey(), highest.getValue());
		}

		Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<String, ConceptModel> item : newConcepts.entrySet()) {
			String uriInLog = item.getKey();
			ConceptModel concept = item.getValue();
			result
					.put(uriInLog, OntologyModel.getConceptPart(concept
							.getOntology().getName())
							+ OntologyModel.ONTOLOGY_SEPARATOR
							+ concept.getShortName());
		}
		return result;
	}

	private Map<String, ConceptModel> expand(List<String> modelReferences) {
		Map<String, ConceptModel> result = new HashMap<String, ConceptModel>();

		for (String uri : modelReferences) {
			OntologyModel model = getOntology(uri);
			String conceptName = OntologyModel.getConceptPart(uri);

			if (model != null) {
				ConceptModel concept = model.findConcept(conceptName);

				if (concept != null) {
					String conceptUri = concept.getName();

					if (conceptPriority.contains(new ConceptSelection(
							conceptUri, true))) {
						result.put(uri, concept);
					} else {
						// we have to replace the concept by its super concepts
						// which are in the priority list
						// and optionally only keeping the highest-priority
						// concept
						List<ConceptModel> todo = new ArrayList<ConceptModel>();

						todo.add(concept);
						while (!todo.isEmpty()) {
							ConceptModel c = todo.remove(todo.size() - 1);

							for (ConceptModel superConcept : model
									.getAllDirectSuperConcepts(c)) {
								String superConceptUri = superConcept.getName();

								if (conceptPriority
										.contains(new ConceptSelection(
												superConceptUri, true))) {
									result.put(model
											.getConceptURIInLog(superConcept),
											superConcept);
								} else {
									todo.add(superConcept);
								}
							}
						}
					}
				}
			}
		}

		if (result.size() > 1) {
			// check whether resulting concepts are super concepts of other
			// resulting concepts
			// if so, these super concepts can be removed from the result
			List<String> toRemove = new ArrayList<String>();
			Set<String> conceptUris = new HashSet<String>();
			for (ConceptModel concept : result.values()) {
				conceptUris.add(concept.getName());
			}
			for (Map.Entry<String, ConceptModel> item : result.entrySet()) {
				OntologyModel model = getOntology(OntologyModel
						.getOntologyPart(item.getKey()));

				for (ConceptModel subconcept : model.getSubConcepts(item
						.getValue())) {
					if (!subconcept.getName().equals(item.getValue().getName())
							&& conceptUris.contains(subconcept.getName())) {
						toRemove.add(item.getKey());
					}
				}
			}
			for (String key : toRemove) {
				result.remove(key);
			}
		}
		return result;
	}

	private void replace(DataSection dataAttributes) {
		List<String> toRemove = new ArrayList<String>();

		for (String name : dataAttributes.keySet()) {
			List<String> newRefs = getUris(replace(dataAttributes
					.getModelReferences(name)));

			if (removeDataAttr && newRefs.isEmpty()) {
				toRemove.add(name);
			} else {
				dataAttributes.setModelReferences(name, newRefs);
			}
		}
		for (String name : toRemove) {
			dataAttributes.remove(name);
		}
	}

	protected String getHelpForThisLogFilter() {
		return "Abstracts concepts in the log file. "
				+ "This filter replaces concepts in the log by the chosen abstractions. "
				+ "For more information, please see <b>http://tabu.tm.tue.nl/user/pvdbrand/ontology_abstraction_filter.htm</b>.";
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new OntologyAbstractionParamDialog(summary, this);
	}

	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		selection = new ArrayList<List<String>>();
		conceptPriority = new ArrayList<ConceptSelection>();
		usePriority = false;
		replaceInstancesElement = false;
		replaceInstancesType = false;
		replaceInstancesOriginator = false;
		removePI = false;
		removeWfme = false;
		removeType = false;
		removeDataAttr = false;
		removeOriginator = false;

		for (int i = 0; i < logFilterSpecifcNode.getChildNodes().getLength(); i++) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);

			if (n.getNodeName().equals(USE_PRIORITY_NODE)) {
				usePriority = n.getAttributes().getNamedItem(VALUE_ATTRIBUTE)
						.getNodeValue().equals(TRUE);
			} else if (n.getNodeName().equals(REPLACE_ELEMENT_NODE)) {
				replaceInstancesElement = n.getAttributes().getNamedItem(
						VALUE_ATTRIBUTE).getNodeValue().equals(TRUE);
			} else if (n.getNodeName().equals(REPLACE_TYPE_NODE)) {
				replaceInstancesType = n.getAttributes().getNamedItem(
						VALUE_ATTRIBUTE).getNodeValue().equals(TRUE);
			} else if (n.getNodeName().equals(REPLACE_ORIGINATOR_NODE)) {
				replaceInstancesOriginator = n.getAttributes().getNamedItem(
						VALUE_ATTRIBUTE).getNodeValue().equals(TRUE);
			} else if (n.getNodeName().equals(REMOVE_PI_NODE)) {
				removePI = n.getAttributes().getNamedItem(VALUE_ATTRIBUTE)
						.getNodeValue().equals(TRUE);
			} else if (n.getNodeName().equals(REMOVE_WFME_NODE)) {
				removeWfme = n.getAttributes().getNamedItem(VALUE_ATTRIBUTE)
						.getNodeValue().equals(TRUE);
			} else if (n.getNodeName().equals(REMOVE_TYPE_NODE)) {
				removeType = n.getAttributes().getNamedItem(VALUE_ATTRIBUTE)
						.getNodeValue().equals(TRUE);
			} else if (n.getNodeName().equals(REMOVE_DATA_ATTR_NODE)) {
				removeDataAttr = n.getAttributes()
						.getNamedItem(VALUE_ATTRIBUTE).getNodeValue().equals(
								TRUE);
			} else if (n.getNodeName().equals(REMOVE_ORIGINATOR_NODE)) {
				removeOriginator = n.getAttributes().getNamedItem(
						VALUE_ATTRIBUTE).getNodeValue().equals(TRUE);
			} else if (n.getNodeName().equals(CONCEPTS_NODE)) {
				for (int sel = 0; sel < n.getChildNodes().getLength(); sel++) {
					Node conceptnode = n.getChildNodes().item(sel);

					if (conceptnode.getNodeName().equals(CONCEPT_NODE)) {
						boolean selected = conceptnode.getAttributes()
								.getNamedItem(SELECTED_ATTRIBUTE)
								.getNodeValue().equals(TRUE);
						conceptPriority.add(new ConceptSelection(conceptnode
								.getTextContent().trim(), selected));
					}
				}
			} else if (n.getNodeName().equals(SELECTION_NODE)) {
				for (int sel = 0; sel < n.getChildNodes().getLength(); sel++) {
					Node selnode = n.getChildNodes().item(sel);

					if (selnode.getNodeName().equals(PATH_NODE)) {
						List<String> path = new LinkedList<String>();

						for (int p = 0; p < selnode.getChildNodes().getLength(); p++) {
							Node itemnode = selnode.getChildNodes().item(p);

							if (itemnode.getNodeName().equals(ITEM_NODE)) {
								path.add(itemnode.getTextContent().trim());
							}
						}
						selection.add(path);
					}
				}
			}
		}
	}

	protected boolean thisFilterChangesLog() {
		return true;
	}

	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		output.write("<" + USE_PRIORITY_NODE + " " + VALUE_ATTRIBUTE + "=\""
				+ (usePriority ? TRUE : FALSE) + "\" />\n");
		output.write("<" + REPLACE_ELEMENT_NODE + " " + VALUE_ATTRIBUTE + "=\""
				+ (replaceInstancesElement ? TRUE : FALSE) + "\" />\n");
		output.write("<" + REPLACE_TYPE_NODE + " " + VALUE_ATTRIBUTE + "=\""
				+ (replaceInstancesType ? TRUE : FALSE) + "\" />\n");
		output.write("<" + REPLACE_ORIGINATOR_NODE + " " + VALUE_ATTRIBUTE
				+ "=\"" + (replaceInstancesOriginator ? TRUE : FALSE)
				+ "\" />\n");
		output.write("<" + REMOVE_PI_NODE + " " + VALUE_ATTRIBUTE + "=\""
				+ (removePI ? TRUE : FALSE) + "\" />\n");
		output.write("<" + REMOVE_WFME_NODE + " " + VALUE_ATTRIBUTE + "=\""
				+ (removeWfme ? TRUE : FALSE) + "\" />\n");
		output.write("<" + REMOVE_TYPE_NODE + " " + VALUE_ATTRIBUTE + "=\""
				+ (removeType ? TRUE : FALSE) + "\" />\n");
		output.write("<" + REMOVE_DATA_ATTR_NODE + " " + VALUE_ATTRIBUTE
				+ "=\"" + (removeDataAttr ? TRUE : FALSE) + "\" />\n");
		output.write("<" + REMOVE_ORIGINATOR_NODE + " " + VALUE_ATTRIBUTE
				+ "=\"" + (removeOriginator ? TRUE : FALSE) + "\" />\n");
		output.write("<" + SELECTION_NODE + ">\n");
		for (List<String> path : selection) {
			output.write("  <" + PATH_NODE + ">\n");
			for (String item : path) {
				output.write("    <" + ITEM_NODE + ">" + item + "</"
						+ ITEM_NODE + ">\n");
			}
			output.write("  </" + PATH_NODE + ">\n");
		}
		output.write("</" + SELECTION_NODE + ">\n");

		output.write("<" + CONCEPTS_NODE + ">\n");
		for (ConceptSelection concept : conceptPriority) {
			output.write("  <" + CONCEPT_NODE + " " + SELECTED_ATTRIBUTE
					+ "=\"" + (concept.isSelected() ? TRUE : FALSE) + "\">"
					+ concept.getConceptURI() + "</" + CONCEPT_NODE + ">\n");
		}
		output.write("</" + CONCEPTS_NODE + ">\n");
	}

}
